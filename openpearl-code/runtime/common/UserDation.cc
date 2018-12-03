/*
 [The "BSD license"]
 Copyright (c) 2012-2014 Rainer Mueller
 Copyright (c) 2013-2014 Holger Koelle
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
\file

\brief class which provides mutual exclusion locking for dation operations

*/
#include <stdio.h>
#include "UserDation.h"
#include "Signals.h"
#include "TaskCommon.h"

namespace pearlrt {

   UserDation::UserDation() {
      //currentTask = NULL;
      //rstValue = NULL;
      rstVoidPointer = NULL;
      isBusy = false;
      mutexUserDation.name("UserDation");
   }

   void UserDation::internalDationClose(const int p) {

      assertOpen();

      if (p & CLOSEMASK) {
         if (!!(p & PRM) && !!(p & CAN)) {
            Log::error("UserDation: ether CAN or PRM allowed");
            throw theInternalDationSignal;
         }

         // superseed previous settings
         dationParams &= ~CLOSEMASK;
         dationParams |= p;
      }

      // mark the dation to be closed, even if errors during
      // closing the system dation occur
      dationStatus = CLOSED;
      closeSystemDation(dationParams);
   }

   void UserDation::restart(TaskCommon * me,
                                  Dation::DationParams direction) {
       // to be called from when the task was suspended while beeing
       // registered in the wait queue
       // test if the device is free --> start i/o processing
       // else add to the wait queue
       // note:
       //  (1) global task lock is locked
       //  (2) the task is blocked

     // for testing purpose it is possible to use
     // without a task object
      if (me) {

         if (isBusy) {
             // user dation is busy --> add current task to wait queue
             // and wait for end of the i/o operation
             waitQueue.insert(me);
          
             Log::info("%s: added to wait queue and waits...", me->getName());
         } else {
            me->enterIO(this);
            isBusy = true;
            me->unblock();
         }
      }
   }

   void UserDation::beginSequence(TaskCommon * me,
                                  Dation::DationParams direction) {
      // this method is called before any dation operation starts
      // in the application
      struct BlockData bd;
      bool blockThread;

//printf("beginSequence this=%p task=%s  isBusy=%d dir=%d systemDation=%p\n",
//     this, me->getName(),isBusy, direction, systemDation);

      // verify that the dation is really open
      assertOpen();

      mutexUserDation.lock();

     // for testing purpose it is possible to use
     // without a task object
      if (me) {
         // the taskState of at least one task will change - maybe 
         // more than one task is affected
         // lock the global task lock, and treat async requests 
         // if pending, after the lock is gained
         //TaskCommon::mutexLock();

         rstVoidPointer = NULL;

         me->enterIO(this);

         if (systemDation->allowMultipleIORequests()) {
            // register current task in system dation
            //TaskCommon::mutexLock(); // we need access to the global task lock
            systemDation->registerWaitingTask(me, direction);
            bd.reason = IO_MULTIPLE_IO;
            bd.u.io.dation = this;
            blockThread = true;
         } else if (isBusy) {
              // user dation is busy --> add current task to wait queue
               // and wait for end of the i/o operation
               waitQueue.insert(me);
               bd.reason = IOWAITQUEUE;
               bd.u.ioWaitQueue.dation = this;
               bd.u.ioWaitQueue.direction = direction;
          
               Log::info("%s: added to wait queue and waits...", me->getName());
               blockThread = true;
         } else {
            isBusy = true;
            blockThread = false;
         }


         if (blockThread) {
            // aquired task block sema, since unblock expects this
            mutexUserDation.unlock();
            TaskCommon::mutexLock(); // we need access to the global task lock
            me->block(&bd);    // block releases the global task lock
            mutexUserDation.lock();

         }

      }

      currentDirection = direction;
      //rstValue = NULL;
      rstVoidPointer = NULL;

      // if multiple IO-requests are allowed, the beginSequenceHook
      // must deal with the task blocking
      beginSequenceHook(me);
      //TaskCommon::mutexUnlock();
      mutexUserDation.unlock();
   }

   void UserDation::endSequence(TaskCommon * me) {
      TaskCommon* pendingTask;

      // gain global task lock, since the task state of at least one
      // task changes
      //TaskCommon::mutexLock();
      mutexUserDation.lock();

      endSequenceHook();

      if (me) {
         // for testing purpose it it possible to use
         // without a task object
         me->leaveIO();
     

         // check whether another task waits for this user dation
         Log::info("%s: check wait queue", me->getName());
         pendingTask = waitQueue.getHead();
         if (pendingTask == NULL) {
            Log::info("nobody waits --> done");
            isBusy = false;
         } else {
            Log::info("%s waits --> goon", pendingTask->getName());
            waitQueue.remove(pendingTask);
            //currentTask = pendingTask;
            pendingTask->unblock();
         } 
      }
      //TaskCommon::mutexUnlock();
      mutexUserDation.unlock();
      if (me) {
         me->scheduleCallback();
      }
   }

   PriorityQueue* UserDation::getWaitQueue() {
       return & waitQueue;
   }


   void UserDation::assertOpen() {
      if (dationStatus == CLOSED) {
         Log::error("dation open required");
         throw theDationNotOpenSignal;
      }
   }

   void UserDation::suspend() {
      systemDation->suspend();
   }

   void UserDation::terminate() {
      systemDation->terminate();
   }

}
