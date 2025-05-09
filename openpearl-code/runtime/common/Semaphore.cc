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
\file

\brief semaphore implementation for posix threads using simultaneous
locking pattern

*/

#define __STDC_LIMIT_MACROS   // enable UINT32_MAX-macro
// must be set before stdint.h
#include <stdint.h>
#include "TaskCommon.h"
#include "Semaphore.h"
#include "Log.h"
#include "DynamicDeadlockDetection/DeadlockOperation.h"
#include "DynamicDeadlockDetection/DynamicDeadlockDetection.h"

namespace pearlrt {

   PriorityQueue Semaphore::waiters;

   Semaphore::Semaphore(uint32_t preset, const char * n) {
      value = preset;
      name = n;
      Log::debug("Sema %s created with preset %u", n, (int)preset);
   }

   const char * Semaphore::getName(void) {
      return name;
   }

   uint32_t Semaphore::getValue(void) {
      return value;
   }

   void Semaphore::decrement(void) {
      value --;
   }

   void Semaphore::increment(void) {
      if (value == UINT32_MAX) {
         throw theSemaOverflowSignal;
      }

      value ++;
   }

   int Semaphore::internalDoTry(TaskCommon * taskCommon, BlockData::BlockReasons::BlockSema *bd) {
      int wouldBlock = 0;
      int i;

      for (i = 0; i < bd->nsemas; i++) {
         Log::debug("   Semaphore %s is %d",
                    bd->semas[i]->getName(), (int)bd->semas[i]->getValue());
      }

      if (DynamicDeadlockDetection::isEnabled() ) {
          DeadlockOperation deadlockOperation;
          deadlockOperation.codeFilename = taskCommon->getLocationFile();
          deadlockOperation.codeLineNumber = taskCommon->getLocationLine();
          deadlockOperation.resourcesType = DeadlockOperation::RESOURCE_TYPE_SEMAPHORE;
          deadlockOperation.actionType = DeadlockOperation::ACTION_TYPE_REQUEST;
          deadlockOperation.executingTaskIdentifier = taskCommon->getName();
          for (i = 0; i < bd->nsemas; i++) {
             deadlockOperation.addResourceIdentifierWithValue(bd->semas[i]->getName(), (int)bd->semas[i]->getValue());
          }
          DynamicDeadlockDetection::performDeadlockOperation(deadlockOperation);
          /* bool isInDeadlockSituation = */ DynamicDeadlockDetection::getDeadlockSituation();
      }

      for (i = 0; i < bd->nsemas; i++) {
         if (bd->semas[i]->getValue() == 0) {
            wouldBlock = 1;
            // revert all previous decrements of this operation
            for (int j=0; j<i; j++) {
               bd->semas[j]->increment(); 
            }
            return wouldBlock;
         } else {
            bd->semas[i]->decrement(); 
         }

      }

      // task could claim the resources

      return wouldBlock;
   }

   void Semaphore::request(TaskCommon* me,
                           int nbrOfSemas,
                           Semaphore** semas) {
      int wouldBlock = 0;
      BlockData bd;

      bd.reason = REQUEST;
      bd.u.sema.nsemas = nbrOfSemas;
      bd.u.sema.semas = semas;

      // critical region start
      TaskCommon::mutexLock();
      Log::debug("request from task %s for %d semaphores", me->getName(),
                nbrOfSemas);

      wouldBlock = internalDoTry(me, &(bd.u.sema));

      if (! wouldBlock) {
         // critical region end
         // task could claim the resources
         TaskCommon::mutexUnlock();
      } else {
         Log::debug("   task: %s going to blocked", me->getName());
         waiters.insert(me);
         // task must wait for at least one resource

         // critical region ends in block()
         me->block(&bd);
         me->scheduleCallback();
      }
   }

   void Semaphore::release(TaskCommon* me,
                           int nbrOfSemas,
                           Semaphore** semas) {
      BlockData bd;
      int i;
      int wouldBlock;

      // start critical region - end after doing all possible releases
      TaskCommon::mutexLock();
      Log::debug("release from task %s for %d semaphores", me->getName(),
                 nbrOfSemas);

      if (DynamicDeadlockDetection::isEnabled() ) {
          DeadlockOperation deadlockOperation;
          deadlockOperation.codeFilename = me->getLocationFile();
          deadlockOperation.codeLineNumber = me->getLocationLine();
          deadlockOperation.resourcesType = DeadlockOperation::RESOURCE_TYPE_SEMAPHORE;
          deadlockOperation.actionType = DeadlockOperation::ACTION_TYPE_RELEASE;
          deadlockOperation.executingTaskIdentifier = me->getName();
          for (i = 0; i < nbrOfSemas; i++) {
             deadlockOperation.addResourceIdentifierWithValue(semas[i]->getName(), semas[i]->getValue());
          }
          DynamicDeadlockDetection::performDeadlockOperation(deadlockOperation);
      }
      try {
         for (i = 0; i < nbrOfSemas; i++) {
            semas[i]->increment();
            // resource released
         }
      } catch (SemaOverflowSignal x) {
         Log::error("SemaOverflowSignal for %s",
                    semas[i]->getName());
         TaskCommon::mutexUnlock();
         throw;
      }

      // all resources released

      for (i = 0; i < nbrOfSemas; i++) {
         Log::debug("   Semaphore %s is now %u",
                       semas[i]->getName(), (int)semas[i]->getValue());
      }

      TaskCommon * t = waiters.getHead();;

      while (t != 0) {
         t->getBlockingRequest(&bd);
         wouldBlock = internalDoTry(t, &(bd.u.sema));

         if (!wouldBlock)  {
            //for (i = 0; i < bd.u.sema.nsemas; i++) {
            //   bd.u.sema.semas[i]->decrement();
            //}

            // task could claim requested resources

            waiters.remove(t);
            t->unblock();
            Log::debug("   unblocking: %s", t->getName());
         } else {
            Log::debug("   task %s still blocked", t->getName());
         }

         t = waiters.getNext(t);
      }

      TaskCommon::mutexUnlock();
   }

   BitString<1> Semaphore::dotry(TaskCommon* me,  int nbrOfSemas, Semaphore** semas) {
      int wouldBlock = 0;
      BlockData bd;
      BitString<1> result(1);  // true

      bd.reason = REQUEST;
      bd.u.sema.nsemas = nbrOfSemas;
      bd.u.sema.semas = semas;

      // start critical region
      TaskCommon::mutexLock();
      Log::debug("try from task %s for %d semaphores", me->getName(),
                 nbrOfSemas);
      wouldBlock = internalDoTry(me, &(bd.u.sema));

      //if (! wouldBlock) {
      //   for (i = 0; i < nbrOfSemas; i++) {
      //      semas[i]->decrement();
      //   }
      //}

      TaskCommon::mutexUnlock();

      if (wouldBlock) {
         result.x = 0;   // false
      }

      return result;
   }

   void Semaphore::removeFromWaitQueue(TaskCommon * t) {
      waiters.remove(t);
   }

   void Semaphore::addToWaitQueue(TaskCommon * t) {
      BlockData bd;
      int wouldBlock;

      t->getBlockingRequest(&bd);
      wouldBlock = internalDoTry(t, &(bd.u.sema));

      if (!wouldBlock)  {
      //   for (int i = 0; i < bd.u.sema.nsemas; i++) {
      //      bd.u.sema.semas[i]->decrement();
      //   }

         waiters.remove(t);
         t->unblock();
         Log::debug("   unblocking: %s", t->getName());
      }  else {
         waiters.insert(t);
      }

   }

   void Semaphore::updateWaitQueue(TaskCommon * t) {
      if (waiters.remove(t)) {
         waiters.insert(t);
      }
   }
}
