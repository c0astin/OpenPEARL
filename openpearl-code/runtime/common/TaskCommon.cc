/*
 [The "BSD license"]
 Copyright (c) 2012-2013 Rainer Mueller
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
#include <signal.h>

#include "Task.h"
#include "Prio.h"
#include "PrioMapper.h"
#include "BitString.h"
#include "CSema.h"
#include "Log.h"
#include "TaskList.h"
#include "TaskMonitor.h"
#include "Interrupt.h"
#include "Signals.h"
#include "Semaphore.h"
#include "Bolt.h"

//          remove this vv comment to enable debug messages
#define DEBUG(fmt, ...) // Log::debug(fmt, ##__VA_ARGS__)

namespace pearlrt {

   TaskCommon::TaskCommon(char * n, Prio prio, BitString<1> isMain) {
      taskState = TERMINATED;       //set taskState

      if (prio.get().x != 0) {
         defaultPrio = prio.get();     //set priority
      } else {
         defaultPrio.x = 255;
      }

      currentPrio = defaultPrio;
      name = n;
      this->isMain = isMain.x ? 1 : 0;
      sourceLine = -1;
      fileName = (char*)"source file name not set";
      //set all conditions to none
      schedActivateData.whenRegistered = false;
      schedActivateData.prio = 0;
      schedActivateOverrun = false;
      schedContinueData.whenRegistered = false;
      //schedContinueData.counts = 0;
      schedContinueData.prio = 0;
   }

   CSema TaskCommon::mutexTasks(1);
   static volatile int cccc = 1;

   void TaskCommon::mutexLock() {
      mutexTasks.name("MutexTasks");
//    Log::debug("MUTEX TASK: LOCKING...cccc= %d", cccc);
//    printf("MUTEX TASK: LOCKING...cccc= %d\n", cccc);

      mutexTasks.request();

      // decrement monitoring variable
      cccc --;

//    Log::debug("MUTEX TASK: LOCKED   cccc= %d", cccc);
//    printf("MUTEX TASK: LOCKED   cccc= %d\n", cccc);
   }

   void TaskCommon::mutexUnlock() {
//    printf("MUTEX TASK: UNLOCKING...cccc= %d\n", cccc);

      // increment monitoring variable
      cccc ++;

      if (cccc > 1) {
         Log::error("MUTEX TASK: ....UNLOCKED --> %d\n", cccc);
      } else {
//         Log::debug("MUTEX TASK: ....UNLOCKED --> %d\n", cccc);
      }

      mutexTasks.release();
//    printf("MUTEX TASK: UNLOCKED...cccc= %d\n", cccc);
   }

   char* TaskCommon::getName() {
      return name;
   }

   int TaskCommon::getIsMain() {
      return isMain;
   }

   Fixed<15> TaskCommon::getPrio() {
      return currentPrio;
   }

   TaskCommon::TaskState TaskCommon::getTaskState() {
//    Log::info("%s : taskState = %d", name, taskState);
      return taskState;
   }

   void TaskCommon::scheduleCallback() {
      mutexLock();

      if (asyncTerminateRequested) {
         asyncTerminateRequested = false;

         if (taskState == BLOCKED && blockParams.why.reason == IO) {
            // if the task is in an I/O operation we must
            //  enshure that the dations semaphres becomes unlocked
            // thus we throw a special exception which is treated
            // in every level of the io-operation stack
            DEBUG("schedCB: throw terminateRequestSignal");
            mutexUnlock();
            throw theTerminateRequestSignal;
         }

         DEBUG("schedCB: terminateMySelf");
         terminateMySelf();
      }

      mutexUnlock();
   }

   void TaskCommon::setLocation(int l, const char *f) {
      sourceLine = l;
      fileName = f;
      scheduleCallback();
   }

   const char * TaskCommon::getLocationFile() {
      return fileName;
   }

   int TaskCommon::getLocationLine() {
      return sourceLine;
   }


   void TaskCommon::testScheduleCondition(int condition,
                                          Duration during, Duration all) {
      if ((condition & TaskCommon::AT) && (condition & TaskCommon::AFTER)) {
         Log::error("task %s: AT + AFTER set", name);
         throw theInternalTaskSignal;
      }

      if ((condition & TaskCommon::AT) && (condition & TaskCommon::WHEN)) {
         Log::error("task %s: AT + WHEN set", name);
         throw theInternalTaskSignal;
      }

      if ((condition & TaskCommon::UNTIL) &&
            (condition &  TaskCommon::DURING)) {
         Log::error("task %s: UNTIL + DURING set", name);
         throw theInternalTaskSignal;
      }

      if ((condition & (TaskCommon::UNTIL | TaskCommon::DURING)) &&
            !(condition & TaskCommon::ALL)) {
         Log::error("task %s: UNTIL or DURING set without ALL", name);
         throw theInternalTaskSignal;
      }

      if ((condition & TaskCommon::ALL) && (all.get().get() <= 0)) {
         Log::error("task %s: non positive duration for ALL", name);
         throw theIllegalSchedulingSignal;
      }

      if ((condition & TaskCommon::DURING) && (during.get().get() <= 0)) {
         Log::error("task %s: non positive duration for DURING", name);
         throw theIllegalSchedulingSignal;
      }

   }

   void TaskCommon::block(BlockData *w) {
      taskState = BLOCKED;
      blockParams.semaBlock.name(name);
      blockParams.why = *w;
      mutexUnlock();
      blockParams.semaBlock.request();
      Log::debug("blocked task %s continued", name);
   }

   void TaskCommon::unblock() {
//      if (taskState == SEMA_BLOCKED)  {
      taskState = RUNNING;
      blockParams.semaBlock.release();
//      }
   }

   void TaskCommon::getBlockingRequest(BlockData *w) {
      *w = blockParams.why;
   }

   void TaskCommon::setNext(TaskCommon*t) {
      blockParams.next = t;
   }

   TaskCommon* TaskCommon::getNext() {
      return blockParams.next;
   }

   void TaskCommon::enterIO(UserDation * d) {
      // note: the task lock is already locked by the user dation
      taskState = BLOCKED;
      Log::info("%s: enterIO: taskstate=%d reason=%d", name, taskState, IO);
      blockParams.why.reason = IO;
      blockParams.why.u.io.dation = d;
   }

   void TaskCommon::leaveIO() {
//      mutexLock();
      taskState = RUNNING;
      Log::info("%s: leaveIO: taskstate=%d", name, taskState);
//      mutexUnlock();
   }

   bool TaskCommon::isMySelf(TaskCommon  *me) {
      return me == this;
   }


   void TaskCommon::activate(TaskCommon * me,
                             int condition,
                             Prio prio,
                             Clock at,
                             Duration after,
                             Duration all,
                             Clock until,
                             Duration during,
                             Interrupt* when) {
      Fixed<15> p;

      if (condition & PRIO) {
         p = prio.get();
      } else {
         p = defaultPrio;
      }

      testScheduleCondition(condition, during, all);  // may throw exception
      // thread safe protection starts here; above code is reentrant
      mutexLock();

      if ((condition & ~ PRIO) != 0) {
         Log::debug("task %s: delayed activate task %s", me->getName(), name);

         try {
            // test calculation of system priority.
            // the return value is discarded - just test if calculation
            // is possible
            // This may throw an exception if the priority is not
            // mappable to the system priorities
            PrioMapper::getInstance()->fromPearl(p);

            scheduledActivate(condition, p, at, after,
                              all, until, during, when);
         } catch (Signal s) {
            mutexUnlock();
            throw;
         }

         mutexUnlock();
         return;
      }

      if (taskState != Task::TERMINATED) {
         Log::error("task %s: Task %s already running", me->getName(), name);
         mutexUnlock();
         throw theTaskRunningSignal;
      } else {
         Log::debug("task %s: activate %s with prio %d",
                    me->getName(), name, p.x);
         currentPrio = p;

         // kill scheduled activate
         if (schedActivateData.taskTimer->isActive() ||
               schedActivateData.whenRegistered) {
            // decrement pending task may be done since the task calling
            // this method is still running
            TaskMonitor::Instance().decPendingTasks();


            if (schedActivateData.taskTimer->cancel()) {
               Log::error("%s cancel activate timer", name);
               mutexUnlock();
               throw theInternalTaskSignal;
            }
         }

         if (schedActivateData.whenRegistered) {
            schedActivateData.when->unregisterActivate(this);
            schedActivateData.when = 0;
            schedActivateData.whenRegistered = false;
         }

         try {
            directActivate(currentPrio);
         } catch (Signal s) {
            mutexUnlock();
            throw;
         }

         TaskMonitor::Instance().incPendingTasks();
         mutexUnlock();
      }

      return;
   }


   void TaskCommon::terminateFromOtherTask() {
      //char dummy;
      asyncTerminateRequested = true;

//???      switchToSchedPrioMax();

//      DEBUG("%s: terminateFromOtherTask: state=%d  "
//                 "asyncTerminateRequested = %d",
//                 name, taskState, asyncTerminateRequested);

      switch (taskState) {

      case BLOCKED:
         terminateWaiters ++;   // increment before treatment from target task

         // remove the task from the wait queue and release block
         switch (blockParams.why.reason) {
         case  REQUEST:
            taskState = Task::TERMINATED;
            Semaphore::removeFromWaitQueue(this);
            blockParams.semaBlock.release();
            DEBUG("task %s: terminateFromOtherTask: block/queue updated",
                  name);
            mutexUnlock();
            break;

         case ENTER:
         case RESERVE:
            taskState = Task::TERMINATED;
            Bolt::removeFromWaitQueue(this);
            blockParams.semaBlock.release();
            DEBUG("task %s: terminateFromOtherTask: block/queue updated",
                  name);
            mutexUnlock();
            break;

         case IOWAITQUEUE:
            Log::error("terminateFromOtherTask: IOWAITQUEUE start");

            blockParams.why.u.ioWaitQueue.dation->getWaitQueue()->remove(this);
            blockParams.semaBlock.release();
            DEBUG("task %s: terminateFromOtherTask: IOWAITQUEUE",
                  name);
            mutexUnlock();
            break;

         case IO:
            terminateIO();
            break;

         default:
            Log::error("%s: untreated block reason in terminateFromOtherTask",
                       name);
            throw theInternalTaskSignal;
         }

         break;

      case SUSPENDED:
         terminateSuspended();
         break;

      case RUNNING:
         terminateWaiters ++;   // increment before treatment from target task
         terminateRunning();
         break;

      case SUSPENDED_BLOCKED:
         terminateWaiters ++;   // increment before treatment from target task
         DEBUG("%s:  terminateRemote susp_blocked task", name);

         // unblock the task - the task is not in any wait queue
         switch (blockParams.why.reason) {
         case  REQUEST:
            blockParams.semaBlock.release();
            break;

         case ENTER:
         case RESERVE:
            blockParams.semaBlock.release();
            break;

         case IO:
            terminateSuspendedIO();
            break;

         default:
            Log::error("%s: untreated block reason in terminateFromOtherTask",
                       name);
            throw theInternalTaskSignal;
         }

         taskState = Task::RUNNING;
         break;

      default:
         Log::error("%s:   unhandled taskState (%d) at TERMINATE",
                    name, taskState);
         mutexUnlock();
         throw theInternalTaskSignal;
      }


      DEBUG("%s:   terminateRemote: "
            "wait for completion state=%d", name, taskState);
      terminateDone.request();

      DEBUG("%s:   terminateRemote: done state=%d", name, taskState);

   }

   void TaskCommon::terminate(TaskCommon * me) {
      Log::debug("task %s: terminate %s request: received (taskState=%d)",
                 me->getName(), name, taskState);
      mutexLock();

      if (isMySelf(me)) {
         terminateMySelf();
      } else {
         switch (taskState) {
         case TERMINATED:
            Log::error("%s: already terminated at TERMINATE", name);
            mutexUnlock();
            throw theTaskTerminatedSignal;

         case RUNNING:
         case SUSPENDED:
         case SUSPENDED_BLOCKED:
         case BLOCKED:
            try {
               terminateFromOtherTask();
            } catch (Signal s) {
               mutexUnlock();
               throw;
            }

            break;

         default:
            Log::error("%s: terminate: unhandled taskState (%d)"
                       " at TERMINATE",
                       name, taskState);
            mutexUnlock();
            throw theInternalTaskSignal;
         }
      }
   }

   void TaskCommon::suspend(TaskCommon * me) {
      Log::debug("%s: suspend for %s request: received", me->getName(), name);
      mutexLock();

      switch (taskState) {
      case TERMINATED:
         Log::error("%s: not running at SUSPEND", name);
         mutexUnlock();
         throw theTaskTerminatedSignal;

      case SUSPENDED:
      case SUSPENDED_BLOCKED:
         Log::error("%s: already suspended at SUSPEND", name);
         mutexUnlock();
         throw theTaskSuspendedSignal;

      case RUNNING:

         try {
            if (isMySelf(me)) {
               Log::debug("   suspendMySelf()");
               suspendMySelf();
               Log::debug("   suspendMySelf done");
            } else {
               Log::debug("   suspendFrom Other()");
               suspendRunning();
               Log::debug("   suspendFrom Other() done");
            }
         } catch (Signal s) {
            Log::debug("   got signal ");
            mutexUnlock();
            throw;
         }

         taskState = SUSPENDED;
         break;

      case BLOCKED:
         Log::debug("%s: SUSPEND at BLOCKED ",
                    name);

         // leave the task blocked on its block-semaphore
         // and remove it from the wait queue
         switch (blockParams.why.reason) {
         case  REQUEST:
            Semaphore::removeFromWaitQueue(this);
            break;

         case ENTER:
         case RESERVE:
            Bolt::removeFromWaitQueue(this);
            break;

         case IOWAITQUEUE:
            Log::error("suspendFromOtherTask: IOWAITQUEUE start");

            blockParams.why.u.ioWaitQueue.dation->getWaitQueue()->remove(this);
            DEBUG("task %s: suspendFromOtherTask: removed from queue",
                  name);
            break;

         case IO:
            suspendIO();
            break;

         default:
            Log::error("%s: untreated block reason (%d) in"
                       " continueFromOtherTask",
                       name, blockParams.why.reason);
            throw theInternalTaskSignal;
         }

         taskState = SUSPENDED_BLOCKED;
         break;

      default:
         Log::error("%s: unhandled taskState (%d) at SUSPEND",
                    name, taskState);
         mutexUnlock();
         throw theInternalTaskSignal;
      }

      Log::debug("%s:   Task: suspend done", name);
      mutexUnlock();
   }


   void TaskCommon::continueFromOtherTask(
      int condition,
      Prio prio) {
      // this function is called with locked tasks mutex
      // unlocking is done in calling function, except
      // this function throws an exception

      DEBUG("%s: continueFromOtherTask: taskState=%d bp.reason=%d",
            name, taskState, blockParams.why.reason);

      if (condition & PRIO) {
         changeThreadPrio(prio.get());
      }

      switch (taskState) {
      case SUSPENDED_BLOCKED:

         // reinsert the task in the semaphores wait queue
         switch (blockParams.why.reason) {
         case  REQUEST:
            taskState = BLOCKED;
            Semaphore::addToWaitQueue(this);
            break;

         case ENTER:
         case RESERVE:
            taskState = BLOCKED;
            Bolt::addToWaitQueue(this);
            break;

         case IO:
            continueSuspended();
            break;

         case IOWAITQUEUE:
            taskState = BLOCKED;
            // restart the i/o request
            blockParams.why.u.ioWaitQueue.dation->restart(
               this,
               blockParams.why.u.ioWaitQueue.direction);
            DEBUG("task %s: continueFromOtherTask: added to queue", name);
            break;

         default:
            Log::error("%s: in SUSPENDED_BLOCKED: "
                       "untreated block reason in continueFromOtherTask (%d)",
                       name, blockParams.why.reason);
            throw theInternalTaskSignal;
         }

         break;

      case BLOCKED:

         // adjust the tasks position in the semaphores wait queue
         // according the tasks (new) priority

         // reinsert the task in the semaphores wait queue
         switch (blockParams.why.reason) {
         case  REQUEST:
            Semaphore::updateWaitQueue(this);
            break;

         case ENTER:
         case RESERVE:
            Bolt::updateWaitQueue(this);
            break;

         case IO:
            // just update the thread priority if given
            break;

         case IOWAITQUEUE: {
            PriorityQueue *q;
            q =  blockParams.why.u.ioWaitQueue.dation->getWaitQueue();
            q->remove(this);
            q->insert(this);
         }
         break;

         default:
            Log::error("in BLOCKED: untreated "
                       "block reason in contineFromOtherTask (%d)",
                       blockParams.why.reason);
            throw theInternalTaskSignal;
         }

         break;

      case RUNNING:
         // nothing to do here; priority change ocuurs common for
         // all alternatives just at the beginning of this method
         break;

      case SUSPENDED:
         continueSuspended();
         break;


      case TERMINATED:
         Log::error("task %s: continue at terminated state", name);
         mutexUnlock();
         throw theTaskTerminatedSignal;

      default:
         Log::error("   task %s: continue at untreated state %d",
                    name, taskState);
         mutexUnlock();
         throw theInternalTaskSignal;
      }
   }

   void TaskCommon::cont(TaskCommon *me,
                         int condition,
                         Prio prio,
                         Clock at,
                         Duration after,
                         Duration all,
                         Clock until,
                         Duration during,
                         Interrupt* when) {
      Fixed<15> p;
      // struct itimerspec its;

      Log::debug("%s: cont() called: condition=%d", name, condition);
      p = currentPrio;

      if (condition & PRIO) {
         p = prio.get();
      }


      if (condition & (AT | AFTER | WHEN | ALL | DURING | UNTIL)) {
         testScheduleCondition(condition, during, all); // may throw exception

         // test calculation of system priority.
         // the return value is discarded - just test if calculation
         // is possible
         // This may throw an exception if the priority is not
         // mappable to the system priorities
         PrioMapper::getInstance()->fromPearl(p);

         // thread safe protection starts here; above code is reentrant
         mutexLock();

         if (schedContinueData.whenRegistered) {
            schedContinueData.when->unregisterContinue(this);
            schedContinueData.whenRegistered = false;
            schedContinueData.when = 0;
         }

         // setup timer
         if (condition & TaskCommon::PRIO) {
            schedContinueData.prio = p;
         } else {
            schedContinueData.prio = defaultPrio;
         }

         try {
            schedContinueData.taskTimer->set(condition, at, after, all,
                                             until, during);
         } catch (...) {
            mutexUnlock();
            throw;
         }

         // if already pending --> just replace the schedule
         if (condition & WHEN) {
            when->registerContinue(this, &nextContinue);
            schedContinueData.when = when;
            schedContinueData.whenRegistered = true;
         } else {

            if (schedContinueData.taskTimer->start()) {
               mutexUnlock();
               throw theInternalTaskSignal;
            }
         }

         mutexUnlock();
      } else {
         Log::debug("%s: continue task %s with prio %d",
                    me->getName(), name, p.x);
         mutexLock();

         // cancel continue schedules if set
         if (schedContinueData.whenRegistered) {
            schedContinueData.when->unregisterContinue(this);
            schedContinueData.whenRegistered = false;
            schedContinueData.when = 0;
         }

         if (schedContinueData.taskTimer->cancel()) {
            mutexUnlock();
            throw theInternalTaskSignal;
         }

         continueFromOtherTask(condition, p);
      }

      mutexUnlock();
      Log::debug("%s: continue task %s completed",
                 me->getName(), name);

      return;
   }


   void TaskCommon::resume(int condition,
                           Clock at,
                           Duration after,
                           Interrupt* when) {
      if (condition == 0) {
         return;
      }

      Log::debug("%s: resume cond=%d", name, condition);

      if (condition != Task::AT && condition != Task::AFTER) {
         Log::error("task %s: resume nether AT nor AFTER", name);
         throw theInternalTaskSignal;
      } else if (condition == Task::AT) {
         after =  at - Clock::now();
      }

      mutexLock();

      schedContinueData.prio = 0; // don't change the prio

      try {
         schedContinueData.taskTimer->set(Task::AFTER, Clock(),
                                          after, Duration(),
                                          Clock(), Duration());
      } catch (...) {
         mutexUnlock();
         throw;
      }

      if (schedContinueData.whenRegistered) {
         schedContinueData.when->unregisterContinue(this);
         schedContinueData.when = 0;
         schedContinueData.whenRegistered = false;
         // no need to update number of pending tasks since this
         // method it called from the task by itsself
      }

      // start the timer
      if (schedContinueData.taskTimer->start()) {
         mutexUnlock();
         throw theInternalTaskSignal;
      }

      // do the plattform specific part ... and release the mutexTasks lock
      Log::debug("%s: call suspendMySelf", name);
      suspendMySelf();
      mutexUnlock();
   }


// is called from try-catch protected block --> may throw
   void TaskCommon::scheduledActivate(int condition,
                                      Fixed<15>& prio,
                                      Clock& at,
                                      Duration& after,
                                      Duration& all,
                                      Clock& until,
                                      Duration& during,
                                      Interrupt* when) {
      if (schedActivateData.taskTimer->isActive() == false &&
            schedActivateData.whenRegistered == false &&
            taskState == TERMINATED) {
         /* was not pending and not activated */
         TaskMonitor::Instance().incPendingTasks();
      }

      // setup timer
      if (condition & TaskCommon::PRIO) {
         schedActivateData.prio = prio;
      } else {
         schedActivateData.prio = defaultPrio;
      }

      // exception throwing is safe here
      schedActivateData.taskTimer->set(condition, at, after, all,
                                       until, during);

      if (condition & WHEN) {
         if (schedActivateData.whenRegistered) {
            schedActivateData.when-> unregisterActivate(this);
         }

         schedActivateData.when = when;
         schedActivateData.whenRegistered = true;
         when->registerActivate(this, &nextActivate);
      } else {
         if (schedActivateData.taskTimer->start()) {
            throw theInternalTaskSignal;
         }
      }

      return;
   }


   void TaskCommon::prevent(TaskCommon*me) {
      //struct itimerspec its;
      bool schedActivateWasSet = false;

      Log::debug("%s: prevent %s start", me->getName(), name);
      mutexLock();

      // check if number of pending must be reduced
      if (schedActivateData.taskTimer->isActive()) {
         schedActivateWasSet = true;
      }

      schedActivateWasSet |= schedActivateData.whenRegistered;


      if (schedActivateData.whenRegistered) {
         schedActivateData.when->unregisterActivate(this);
         schedActivateData.when = 0;
         schedActivateData.whenRegistered = false;
      }

      // cancel activate timer
      if (schedActivateData.taskTimer->cancel()) {
         mutexUnlock();
         throw theInternalTaskSignal;
      }

      if (schedContinueData.taskTimer->cancel()) {
         mutexUnlock();
         throw theInternalTaskSignal;
      }

      if (schedContinueData.whenRegistered) {
         schedContinueData.when->unregisterContinue(this);
         schedContinueData.when = 0;
         schedContinueData.whenRegistered = false;
      }

      Log::debug("   task %s: prevent %s done", me->getName(), name);

      /* still running ? */
      if (taskState == TERMINATED && schedActivateWasSet) {
         TaskMonitor::Instance().decPendingTasks();
         mutexUnlock();
      } else {
         mutexUnlock();
      }
   }


   void TaskCommon::timedActivate() {

      mutexLock();

      if (taskState == Task::TERMINATED) {
         Log::debug("%s: scheduled activate: starts", name);
         directActivate(schedActivateData.prio);
//         activateDone.request();
      } else {
         if (schedActivateOverrun) {
            // warn that this activation is skipped
            Log::warn("task %s: scheduled activate: overrun", name);
         }

         schedActivateOverrun = true;
      }

      mutexUnlock();

   }

   void TaskCommon::activateHandler(TaskCommon* tsk) {
      ((TaskCommon*)tsk)->timedActivate();
   }


   void TaskCommon::timedContinue() {
      mutexLock();

      if (taskState == Task::SUSPENDED ||
            taskState == Task::SUSPENDED_BLOCKED ||
            taskState == Task::RUNNING) {
         Log::debug("%s: scheduled continue: timeout reached", name);

         if (schedContinueData.prio.x != 0) {
            continueFromOtherTask(PRIO, Prio(schedContinueData.prio));
         } else {
            continueFromOtherTask(0, Prio());
         }

         Log::debug("%s: scheduled continue done", name);

      } else {
         Log::warn("%s: scheduled continue: skipped", name);
      }

      mutexUnlock();
   }

   void TaskCommon::continueHandler(TaskCommon* tsk) {
      ((TaskCommon*)tsk)->timedContinue();
   }


// call with locked task lock
   void TaskCommon::triggeredContinue() {
      Log::debug("%s: triggeredContinue called", name);
      schedContinueData.whenRegistered = false;  // automatically unregistered

      if (schedContinueData.taskTimer->isSet()) {
         if (schedContinueData.taskTimer->start()) {
            mutexUnlock();
            throw theInternalTaskSignal;
         }
      } else {
         // do the internal continue stuff
         if (schedContinueData.prio.x != 0) {
            continueFromOtherTask(PRIO, Prio(schedContinueData.prio));
         } else {
            continueFromOtherTask(0, Prio());
         }
      }
   }

// call with locked task lock
   void TaskCommon::triggeredActivate() {
      Log::debug("%s: triggeredActivate called", name);

      if (schedActivateData.taskTimer->isSet()) {
         if (schedActivateData.taskTimer->start()) {
            mutexUnlock();
            throw theInternalTaskSignal;
         }
      } else {
         if (taskState == Task::TERMINATED) {
            Log::debug("%s: scheduled activate (when): starts", name);
            directActivate(schedActivateData.prio);
         } else {
            if (schedActivateOverrun) {
               // warn that this activation is skipped
               Log::warn("%s: scheduled activate: overrun", name);
            }

            schedActivateOverrun = true;
         }
      }
   }

   void TaskCommon::changeThreadPrio(const Fixed<15>& prio) {
      currentPrio = prio;
      setPearlPrio(prio);
   }
}
