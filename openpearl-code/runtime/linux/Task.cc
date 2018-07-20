/*
 [A "BSD license"]
 Copyright (c) 2012-2018 Rainer Mueller
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**

\brief tasking

\author R. Mueller

*/

#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <bits/local_lim.h> // PTHREAD_STACK_MIN
#include <sched.h>
#include <errno.h>
#include <signal.h>
#include <unistd.h>

#include "SignalMapper.h"
#include "Task.h"
#include "Log.h"
#include "TaskList.h"
#include "TaskMonitor.h"
#include "Interrupt.h"
#include "PrioMapper.h"
#include "BitString.h"
#include "Semaphore.h"
#include "Bolt.h"

//          remove this vv comment to enable debug messages
#define DEBUG(fmt, ...)  // Log::debug(fmt, ##__VA_ARGS__)

namespace pearlrt {

   /*
   we need a signal handler function to convince the system calls
   to abort with EINTR. But there is nothing to do inside of the
   signal handler
   */
   static void eintrSignalHandler(int sig) {
      // Log::info("%s: eintrSignalHandler got signal %d",
      //          Task::currentTask()->getName(), sig);
   }

   static __thread Task* mySelf;

   Task::Task() : TaskCommon(NULL, Prio(), BitString<1>(0)) {
      Log::error("task %s: illegal call to default constructor of Task::",
                 name);
      throw theInternalTaskSignal;
   }

   // tasks are created statically - no mutex protection needed
   Task::Task(void (*entry)(Task *),
              char * n,
              Prio prio, BitString<1> isMain) : TaskCommon(n, prio, isMain) {
      int ret;

      entryPoint = entry;   //set thread execute function

      schedActivateData.taskTimer = &activateTaskTimer;
      schedContinueData.taskTimer = &continueTaskTimer;

      //create activate timer for SIG_ACTIVATE if expired and
      //create continue timer for SIG_CONTINUE if expired
      ((TaskTimer*)schedActivateData.taskTimer)->create(
         this, SIG_ACTIVATE, activateHandler);
      ((TaskTimer*)schedContinueData.taskTimer)->create(
         this, SIG_CONTINUE, continueHandler);

      if (pthread_attr_init(&attr) != 0) {
         Log::error("task %s: error initializing pthread_attr", name);
         throw theInternalTaskSignal;
      }

#if 0
      // the stack size is taken from the ulimit setting
      // is is possible to modify the maximum value of the stack
      // per thread beyond the limit of 'ulimit -s xxx'
      // I see no indication to set the stack size to a dedicated value
      //
      // the version with PTHREAD_STACK_MIN (16kB) came from 32 bit linux
      // to avoid the limit for the number of tasks due to the stack
      // adress restrictions on only 4 GB total memory addresses
      {
         size_t ss = 64 * 1024 * 1024; // 64 MB

         //if (pthread_attr_setstacksize(&attr, PTHREAD_STACK_MIN) < 0) {
         if (pthread_attr_setstacksize(&attr, ss) < 0) {
            Log::error("task %s: error setting pthread_attr_stacksize", name);
            throw theInternalTaskSignal;
         }

         pthread_attr_getstacksize(&attr, &ss);
         printf("stack size :%zu\n", ss);
      }
#endif

      //no inheritance of the main thread priority
      if (pthread_attr_setinheritsched(&attr, PTHREAD_EXPLICIT_SCHED) != 0) {
         Log::error("task %s: error setting pthread inheritance attributes",
                    name);
         throw theInternalTaskSignal;
      }

      ret = pipe(pipeResume);

      if (ret) {
         Log::error("task %s: error creating pipeResume (%s)",
                    name, strerror(errno));
         throw theInternalTaskSignal;
      }

      TaskList::Instance().add(this);
   }


   void Task::continueSuspended() {
      DEBUG("%s: send continuation data", name);
      char dummy = 'c';
      continueWaiters++;

      // be not disturbed by application threads
      switchToSchedPrioMax();
      {
         write(pipeResume[1], &dummy, 1);
         mutexUnlock();
         continueDone.request();
         DEBUG("%s: continuation data received and acknowledged", name);
         mutexLock();
      }

      // perhaps the just continued task has terminated in the interval
      // between sending the continuation data and this point
      if (taskState != TERMINATED) {
         switchToSchedPrioCurrent();
      }
   }

   void Task::suspendIO() {
      // send the SIG_CANCEL_IO signal to the i/O performing thread
      // this will produce an EINTR error of the system call
      // The driver will detect this and perform the selfTermination
      //
      // there is a duration between setting the taskState to IO+BLOCKED
      // and entering the system call
      // if the termination request occurs in this period, we do not
      // get the EINTR error code
      //
      // Thus, we set the asyncTerminationRequested flag
      // and send the signal repetitive until the flag is reset by the
      // device driver

      asyncSuspendRequested = true;

      suspendWaiters ++;
      // be not disturbed by application threads
      switchToSchedPrioMax();
      {

         while (asyncSuspendRequested) {
            Log::info("%s: send SIG_CANCEL", name);
            pthread_kill(threadPid, SIG_CANCEL_IO);
            usleep(100000);
         }

         suspendDone.request();
      }
      switchToSchedPrioCurrent();

   }


   void Task::suspendRunning() {
      asyncSuspendRequested = true;
      DEBUG("%s: set suspend request flag", name);
      suspendWaiters ++;

      // be not disturbed by application threads
      switchToSchedPrioMax();
      {
         mutexUnlock();
         suspendDone.request();
         mutexLock();
      }
      switchToSchedPrioCurrent();
   }

   void Task::suspendMySelf() {
      char dummy;

      while (suspendWaiters > 0) {
         suspendWaiters --;
         suspendDone.release();
      }

      DEBUG("%s:   suspended (wait for contine data) ", name);
      taskState = SUSPENDED;

      // be not disturbed by application threads
      switchToSchedPrioMax();
      {
         mutexUnlock();
         read(pipeResume[0], &dummy, 1);
         mutexLock();
      }
      switchToSchedPrioCurrent();
      DEBUG("%s:   suspendMySelf: got data %c", name, dummy);

      switch (dummy) {
      case 't' :
         terminateMySelf();
         break;

      case 'c' :
         DEBUG("%s: suspendMySelf: continued: old taskState=%d",
               name, taskState);

         if (taskState == SUSPENDED) {
            taskState = Task::RUNNING;
         } else if (taskState == SUSPENDED_BLOCKED) {
            taskState = Task::BLOCKED;
         } else {
            Log::error("suspendMySelf: unexpected taskState = %d", taskState);
            mutexUnlock();
            throw theInternalTaskSignal;
         }

         while (continueWaiters > 0) {
            continueWaiters --;
            continueDone.release();
         }

         DEBUG("%s:  continue from suspend done", name);
         switchToSchedPrioCurrent();
         break;

      default:
         Log::error("%s:   resume: received unknown continue (%c)",
                    name, dummy);
         break;
      }
   }

   void Task::switchToSchedPrioMax() {
      setThreadPrio(schedPrioMax);
   }

   void Task::switchToSchedPrioCurrent() {
      changeThreadPrio(currentPrio);
   }


   void Task::setPearlPrio(const Fixed<15>& prio) {
      int p;

      if (! useNormalSchedulerFlag) {
         try {
            p = PrioMapper::getInstance()->fromPearl(prio);
         } catch (PriorityNotMapableSignal s) {
            mutexUnlock();
            return;
         }

         setThreadPrio(p);
      }
   }

   void Task::setThreadPrio(int p) {
      DEBUG("%s: set ThreadPrio to %d", name, p);

      if (! useNormalSchedulerFlag) {
         struct sched_param sp;
         int error;

         param.sched_priority = p;
         sp.sched_priority = p;

         //setting up the new priority
         error = pthread_setschedparam(threadPid, SCHED_RR, &sp);

         if (error) {
            mutexUnlock();
            Log::error(
               "%s: error on setting new priority (%s)",
               name, strerror(error));
            throw theInternalTaskSignal;
         }
      }
   }


   void Task::scheduleCallback(void) {
      mutexLock();

      if (asyncTerminateRequested) {
         asyncTerminateRequested = false;

         if (taskState == BLOCKED && blockParams.why.reason == IO) {
            DEBUG("scheduleCallback: throw terminateRequestSignal");
            mutexUnlock();
            throw theTerminateRequestSignal;
         }

         DEBUG("scheduleCallback: terminateMySelf");
         terminateMySelf();
      }

      if (asyncSuspendRequested) {
         DEBUG("%s: scheduledCallback : suspend request detected",
               name);

         asyncSuspendRequested = false;
         suspendMySelf();
      }

      mutexUnlock();
   }

   void Task::entry() {

      enableCancelIOSignalHandler();
      asyncTerminateRequested = false;
      asyncSuspendRequested = false;
      suspendWaiters = 0;
      continueWaiters = 0;
      terminateWaiters = 0;
      // store own pid
      threadPid = pthread_self();
      taskState = Task::RUNNING;

      // set the pointer to the current object in the thread's local data
      mySelf = this;

      activateDone.release();
      switchToSchedPrioCurrent();
      DEBUG("%s: task activation completed", name);
   }


   void Task::directActivate(const Fixed<15>& prio) {
      int ret;
      currentPrio = prio;

      //set up the new scheduling policies for preemptive priority
      if (!useNormalSchedulerFlag) {
         // the task will be activate with best priority. After
         // finished initialization it will fall back to currentPrio.
         // This happens in entry(). A non mapable priority must
         // give a signal to the executing task. Thats why we test
         // whether priority is mapable now.
         PrioMapper::getInstance()->fromPearl(currentPrio);

         // the possibility of setting SCHED_RR is detected at
         // system start. Therefore this attribute is not set
         // statically in the constructor.
         if (pthread_attr_setschedpolicy(&attr, SCHED_RR) != 0) {
            Log::error("%s: error setting SCHED_RR scheduler", name);
            throw theInternalTaskSignal;
         }

         // start with maximum priority --> switch to given
         // priority occurs in thread start routine
         param.sched_priority = schedPrioMax;

         if (pthread_attr_setschedparam(&attr, &param) != 0) {
            Log::error("%s: error on setting priority", name);
            throw theInternalTaskSignal;
         }
      }

      ret = pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS, NULL);

      if (ret != 0) {
         Log::error("%s: error on setting cancellation type", name);
         throw theInternalTaskSignal;
      }


      //create the thread
      if (pthread_create(&threadPid, &attr,
                         (void * (*)(void*))entryPoint,
                         (void*)this) != 0) {
         Log::error("%s: could not create thread (%s))",
                    name, strerror(errno));
         throw theInternalTaskSignal;
      }

      Log::debug("wait for activateDone.request");
      activateDone.request();
      Log::debug("got activateDone.request");

      return;
   }

   int Task::detailedTaskState(char * line[3]) {
      int i = 0;
      char help[20];
      mutexLock();

      if (schedActivateData.taskTimer->isActive()) {
         ((TaskTimer*)(schedActivateData.taskTimer))->detailedStatus(
            (char*)"ACT", line[i]);
         i++;
      }

      if (schedContinueData.taskTimer->isActive()) {
         ((TaskTimer*)(schedContinueData.taskTimer))->detailedStatus(
            (char*)"CONT", line[i]);
         i++;
      }


      if (taskState == BLOCKED) {
         switch (blockParams.why.reason) {
         case REQUEST:
            sprintf(line[i], "REQUESTing %d SEMAs:",
                    blockParams.why.u.sema.nsemas);

            for (int j = 0; j < blockParams.why.u.sema.nsemas; j++) {
               Semaphore * s = blockParams.why.u.sema.semas[j] ;
               sprintf(help, " %s(%d)", s->getName(), s->getValue());

               if (strlen(line[i]) + strlen(help) < 80) {
                  strcat(line[i], help);
               }
            }

            break;

         case RESERVE:
            sprintf(line[i], "RESERVEing %d BOLTs:",
                    blockParams.why.u.bolt.nbolts);

            for (int j = 0; j < blockParams.why.u.bolt.nbolts; j++) {
               Bolt * s = blockParams.why.u.bolt.bolts[j];
               sprintf(help, " %s(%s)", s->getName(), s->getStateName());

               if (strlen(line[i]) + strlen(help) < 80) {
                  strcat(line[i], help);
               }
            }

            break;

         case ENTER:
            sprintf(line[i], "ENTERing %d BOLTs:",
                    blockParams.why.u.bolt.nbolts);

            for (int j = 0; j < blockParams.why.u.bolt.nbolts; j++) {
               Bolt * s = blockParams.why.u.bolt.bolts[j] ;
               sprintf(help, " %s(%s)", s->getName(), s->getStateName());

               if (strlen(line[i]) + strlen(help) < 80) {
                  strcat(line[i], help);
               }
            }

            break;

         default:
            sprintf(line[i], "unknown blocking reason(%d)",
                    blockParams.why.reason);
            break;
         }

         i++;
      }

      mutexUnlock();
      return i;
   }

   void Task::terminateIO() {
      // send the SIG_CANCEL_IO signal to the i/O performing thread
      // this will produce an EINTR error of the system call
      // The driver will detect this and perform the selfTermination
      //
      // there is a duration between setting the taskState to IO+BLOCKED
      // and entering the system call
      // if the termination request occurs in this period, we do not
      // get the EINTR error code
      //
      // Thus, we set the asyncTerminationRequested flag
      // and send the signal repetitive until the flag is reset by the
      // thread which executes the device driver

      DEBUG("%s:   terminateFromOtherTask: in IO_BLOCKED",
            name);

      asyncTerminateRequested = true;

      while (asyncTerminateRequested) {
         DEBUG("send SIG_CANCEL_IO to %d", (int)threadPid);
         pthread_kill(threadPid, SIG_CANCEL_IO);
         usleep(100000);
      }
   }


   void Task::terminateSuspended() {
      char dummy;
      terminateWaiters ++;   // increment before treatment from target task
      DEBUG("%s:   terminateRemote suspended task", name);
      dummy = 't';
      mutexUnlock();
      write(pipeResume[1], &dummy, 1);
   }


   void Task::terminateRunning() {
      asyncTerminateRequested = true;
      DEBUG("%s:  terminateRemote running task", name);

      mutexUnlock();
   }

   void Task::terminateSuspendedIO() {
      // send the SIG_CANCEL_IO signal to the i/O performing thread
      // this will produce an EINTR error of the system call
      // The driver will detect this and perform the selfTermination
      //
      // there is a duration between setting the taskState to IO+BLOCKED
      // and entering the system call
      // if the termination request occurs in this period, we do not
      // get the EINTR error code
      //
      // Thus, we set the asyncTerminationRequested flag
      // and send the signal repetitive until the flag is reset by the
      // device driver

      asyncTerminateRequested = true;

      while (asyncTerminateRequested) {
         pthread_kill(threadPid, SIG_CANCEL_IO);
         usleep(1000);
      }
   }

   void Task::terminateMySelf() {
      DEBUG("%s:   terminateMySelf: start state=%d",
            name, taskState);


      if (suspendWaiters > 0) {
         Log::info("%s:   terminates while %d other tasks wait"
                   " to suspend it",
                   name, suspendWaiters);
      }

      while (suspendWaiters > 0) {
         suspendWaiters --;
         suspendDone.release();
      }

      if (terminateWaiters > 0) {
         DEBUG("%s:   terminates while %d other tasks wait"
               " to terminate it",
               name, terminateWaiters);
      }

      while (terminateWaiters > 0) {
         terminateWaiters --;
         terminateDone.release();
      }

      if (schedActivateOverrun) {
         // missed one scheduled activation --> do it immediatelly now
         DEBUG("%s:   terminates with missed scheduled"
               " activate pending", name);
         schedActivateOverrun = false;
         directActivate(schedActivateData.prio);
      } else {
         /* still pending ? */
         if (schedActivateData.taskTimer->isActive() == false &&
               schedActivateData.whenRegistered == false) {
            TaskMonitor::Instance().decPendingTasks();
         }
      }

      taskState = Task::TERMINATED;
      threadPid = 0; // invalidate thread id
      DEBUG("%s: terminates", name);
      mutexUnlock();
      pthread_exit(0);
   }

   int Task::schedPrioMax = 0;

   void Task::setSchedPrioMax(int p) {
      schedPrioMax = p;
   }

   int Task::useNormalSchedulerFlag = 0;

   void Task::useNormalScheduler() {
      useNormalSchedulerFlag = 1;
   }

   pthread_t Task::getPid() {
      return threadPid;
   }

   Task::TaskEntry Task::getEntry() {
      return entryPoint;
   }

   Task* Task::currentTask(void) {
      return mySelf;
   }

   void Task::enableCancelIOSignalHandler() {
      struct sigaction sa = {{0}};
      sa.sa_handler = &eintrSignalHandler;
      sigemptyset(&sa.sa_mask);
      sa.sa_flags = 0;

      if (sigaction(SIG_CANCEL_IO, & sa, NULL) != 0) {
         Log::error("%s: could not register SIG_CANCEL_IO", name);
      }
   }


   void Task::treatCancelIO(void) {
      DEBUG("%s: treatCancelIO", name);

      // we do not need to lock the global task lock, since this
      // was already done where the signal was raises in suspendFromOtherTask
      // or terminateFromOtherTask.

      if (asyncTerminateRequested) {
         asyncTerminateRequested = false;
         DEBUG("%s: terminate during system IO device", name);
         mutexUnlock();
         throw theTerminateRequestSignal;
      }

      if (asyncSuspendRequested) {
         asyncSuspendRequested = false;

         // reactivate the signal processing in system calls
         enableCancelIOSignalHandler();  // reactivate the signal processing

         DEBUG("%s: Task::treatIOCancelIO: suspending ...", name);
         suspendMySelf();
         DEBUG("%s: Task::treatIOCancelIO: continued", name);
      }

   }
}
