/*
 [A "BSD license"]
 Copyright (c) 2017 Rainer Mueller
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

\brief BOLT implementation for posix threads using simultaneous
locking pattern

*/

#define __STDC_LIMIT_MACROS   // enable UINT32_MAX-macro
// must be set before stdint.h
#include <stdint.h>

#include "TaskCommon.h"
#include "Bolt.h"
#include "Signals.h"
#include "Log.h"
#include "DynamicDeadlockDetection/DeadlockOperation.h"
#include "DynamicDeadlockDetection/DynamicDeadlockDetection.h"

namespace pearlrt {

   PriorityQueue Bolt::waiters;

   Bolt::Bolt(const char * n) {
      name = n;
      Log::debug("%s: Bolt created", n);
      nbrOfEnterOperations = 0;
   }

   const char * Bolt::getName(void) {
      return name;
   }

   int Bolt::getState(void) {
      return state;
   }

   char* Bolt::getStateName(void) {
      switch (state) {
      default:
         return ((char*)"???");

      case FREE:
         return ((char*)"'lock possible'");

      case ENTERED:
         return ((char*)"'lock not possible'");

      case RESERVED:
         return ((char*)"'locked'");
      }
   }


   void Bolt::setState(int newState, TaskCommon* taskCommon, int oldState) {
      state = newState;
      if (state == FREE)
      {
         if (DynamicDeadlockDetection::isEnabled() ) {
             DeadlockOperation deadlockOperation;
             deadlockOperation.codeFilename = taskCommon->getLocationFile();
             deadlockOperation.codeLineNumber = taskCommon->getLocationLine();
             deadlockOperation.resourcesType = DeadlockOperation::RESOURCE_TYPE_BOLT;
             // leave -> entered
             // free -> reserved
             if (oldState == ENTERED) {
                deadlockOperation.actionType = DeadlockOperation::ACTION_TYPE_LEAVE;
             } else if (oldState == RESERVED) {
                deadlockOperation.actionType = DeadlockOperation::ACTION_TYPE_FREE;
             }
             deadlockOperation.executingTaskIdentifier = taskCommon->getName();
             deadlockOperation.addResourceIdentifierWithValue(this->getName(), 0);
             DynamicDeadlockDetection::performDeadlockOperation(deadlockOperation);
          }
      }
   }

   int Bolt::check(BlockReason r, BlockData::BlockReasons::BlockBolt *bd) {
      int wouldBlock = 0;
      int i;
     
      // check for duplicate bolts in list at reservea
      if (r == RESERVE) {
         for (int i = 0; i < bd->nbolts-1; i++) {
            for (int j = i+1; j < bd->nbolts; j++) {
               if (bd->bolts[i] == bd->bolts[j]) {
                  Log::error("RESERVE: multiple bolt '%s' would cause eternal lock", bd->bolts[i]->getName());
                  TaskCommon::mutexUnlock();
                  throw theBoltReserveDuplicateSignal;
               }
            }
         }
      }

      for (i = 0; i < bd->nbolts; i++) {
         if (bd->bolts[i]->getState() == RESERVED ||
               (bd->bolts[i]->getState() == ENTERED && r == RESERVE)) {
            wouldBlock = 1;
         }

         if (bd->bolts[i]->getState() == ENTERED) {
            Log::debug("  Bolt %s is %s (%d times)",
                    bd->bolts[i]->getName(), bd->bolts[i]->getStateName(),
                    (int)bd->bolts[i]->getNbrOfEnterOperations());
         } else {
            Log::debug("  Bolt %s is %s",
                    bd->bolts[i]->getName(), bd->bolts[i]->getStateName());
	 }
      }

      return wouldBlock;
   }

   void Bolt::enter(TaskCommon* me,
                    int nbrOfBolts,
                    Bolt** bolts) {
      enterOrReserve(me, ENTER, ENTERED, nbrOfBolts, bolts);
   }

   void Bolt::reserve(TaskCommon* me,
                      int nbrOfBolts,
                      Bolt** bolts) {
      enterOrReserve(me, RESERVE , RESERVED, nbrOfBolts, bolts);
   }

   uint32_t  Bolt::getNbrOfEnterOperations() {
      return nbrOfEnterOperations;
   }

   void Bolt::decrementEnter() {
      nbrOfEnterOperations --;
   }

   void Bolt::incrementEnter() {
      if (nbrOfEnterOperations == UINT32_MAX) {
         // this may happen if more 2^32 tasks are waiting -- not
         // propable in the near future !
         Log::error("too many ENTER operations on bolt %s", name);
         TaskCommon::mutexUnlock();
         throw theInternalTaskSignal;
      }

      nbrOfEnterOperations ++;
   }

   void Bolt::enterOrReserve(TaskCommon* me,
                             BlockReason operation,
                             int newState,
                             int nbrOfBolts,
                             Bolt** bolts) {
      int i;
      int wouldBlock = 0;
      BlockData bd;

      bd.reason = operation;
      bd.u.bolt.nbolts = nbrOfBolts;
      bd.u.bolt.bolts = bolts;

      TaskCommon::mutexLock();

      if (operation == ENTER) {
         Log::debug("%s: ENTER for %d bolts", me->getName(),
                   nbrOfBolts);
      } else {
         Log::debug("%s: RESERVE for %d bolts", me->getName(),
                   nbrOfBolts);
      }

      wouldBlock = check(operation, &(bd.u.bolt));

      if (operation == RESERVE)
      {
         if (DynamicDeadlockDetection::isEnabled() ) {
             DeadlockOperation deadlockOperation;
             deadlockOperation.codeFilename = me->getLocationFile();
             deadlockOperation.codeLineNumber = me->getLocationLine();
             deadlockOperation.resourcesType = DeadlockOperation::RESOURCE_TYPE_BOLT;
             deadlockOperation.actionType = DeadlockOperation::ACTION_TYPE_RESERVE;
             deadlockOperation.executingTaskIdentifier = me->getName();
             for (i = 0; i < nbrOfBolts; i++) {
                deadlockOperation.addResourceIdentifierWithValue(bolts[i]->getName(), 0);
             }
             deadlockOperation.reserveBoltOperationsPossible = (wouldBlock == 0);
             DynamicDeadlockDetection::performDeadlockOperation(deadlockOperation);
             /* bool isInDeadlockSituation = */ DynamicDeadlockDetection::getDeadlockSituation();
          }
      }

      if (! wouldBlock) {
         for (i = 0; i < nbrOfBolts; i++) {
            bolts[i]->setState(newState, me);

            if (operation == ENTER) {
               bolts[i]->incrementEnter();
            }
         }

         // critical region end
         TaskCommon::mutexUnlock();
      } else {
         Log::debug("%s: going to blocked on BOLT", me->getName());
         waiters.insert(me);
         // critical region ends in block()
         me->block(&bd);
         me->scheduleCallback();  // check for suspend/terminate
      }
   }

   void Bolt::leave(TaskCommon* me,
                    int nbrOfBolts,
                    Bolt** bolts) {
      leaveOrFree(me, ENTERED, nbrOfBolts, bolts);
   }

   void Bolt::free(TaskCommon* me,
                   int nbrOfBolts,
                   Bolt** bolts) {
      leaveOrFree(me, RESERVED, nbrOfBolts, bolts);
   }

   void Bolt::leaveOrFree(TaskCommon* me,
                          int oldState,
                          int nbrOfBolts,
                          Bolt** bolts) {
      BlockData bd;
      int i;
      int wouldBlock;
      int reserveIsWaiting;
      TaskCommon * t;

      // start critical region - end after done all possible unblocking
      TaskCommon::mutexLock();

      if (oldState == ENTERED) {
         Log::debug("%s: LEAVE for %d bolts", me->getName(),
                    nbrOfBolts);
      } else {
         Log::debug("%s: FREE for %d bolts", me->getName(),
                    nbrOfBolts);
      }

      for (i = 0; i < nbrOfBolts; i++) {
         if (bolts[i]->getState() != oldState) {
            Log::error("%s:   bolt has wrong state (%d)",
                       bolts[i]->getName(),
                       bolts[i]->getState());
            TaskCommon::mutexUnlock();
            throw theBoltStateSignal;
         }

         if (bolts[i]->getState() == ENTERED) {
            bolts[i]->decrementEnter();

            if (bolts[i]->getNbrOfEnterOperations() == 0) {
               bolts[i]->setState(FREE, me, oldState);
               Log::debug("   bolt: %s is now %s",
                          bolts[i]->getName(), bolts[i]->getStateName());
            } else {
               Log::debug("   bolt: %s is now %s (%d times)",
                          bolts[i]->getName(), bolts[i]->getStateName(),
                          bolts[i]->getNbrOfEnterOperations());
            }
         } else {
            // RESERVED
            bolts[i]->setState(FREE, me, oldState);
            Log::debug("%s: BOLT is now %s",
                       bolts[i]->getName(), bolts[i]->getStateName());
         }
      }

      reserveIsWaiting = 0;

      // pass 1 - test for task waiting for RESERVE
      for (t = waiters.getHead(); t != 0; t = waiters.getNext(t)) {
         t->getBlockingRequest(&bd);
         wouldBlock = check(bd.reason, &(bd.u.bolt));

         if (bd.reason == RESERVE)  {
            if (!wouldBlock) {
               for (i = 0; i < bd.u.bolt.nbolts; i++) {
                  bd.u.bolt.bolts[i]->setState(RESERVED, me, oldState);
               }

               waiters.remove(t);
               t->unblock();
               Log::debug("%s: unblocking from RESERVE", t->getName());
            } else {
               reserveIsWaiting = 1;
            }
         }
      }

      // perform pass 2 only if no task waits with RESERVE
      if (!reserveIsWaiting) {
         for (t = waiters.getHead(); t != 0; t = waiters.getNext(t)) {
            t->getBlockingRequest(&bd);
            wouldBlock = check(bd.reason, &(bd.u.bolt));

            if (bd.reason == ENTER)  {
               if (!wouldBlock) {
                  for (i = 0; i < bd.u.bolt.nbolts; i++) {
                     bd.u.bolt.bolts[i]->setState(ENTERED, me, oldState);
                     bd.u.bolt.bolts[i]->incrementEnter();
                  }

                  waiters.remove(t);
                  t->unblock();
                  Log::debug("%s: unblocking from ENTER", t->getName());
               }
            }
         }
      }

      TaskCommon::mutexUnlock();
   }


   void Bolt::removeFromWaitQueue(TaskCommon * t) {
      waiters.remove(t);
   }

   void Bolt::addToWaitQueue(TaskCommon * t) {
      BlockData bd;
      int wouldBlock;

      t->getBlockingRequest(&bd);
      wouldBlock = check(bd.reason, &(bd.u.bolt));

      if (!wouldBlock)  {
         for (int i = 0; i < bd.u.bolt.nbolts; i++) {
            if (bd.reason == ENTER) {
               bd.u.bolt.bolts[i]->setState(ENTERED, t);
            } else {
               bd.u.bolt.bolts[i]->setState(RESERVED, t);
            }
         }

         t->unblock();
         Log::debug("%s: unblocking from BOLT", t->getName());
      }  else {
         waiters.insert(t);
      }

   }

   void Bolt::updateWaitQueue(TaskCommon * t) {
      if(waiters.remove(t)) {
         // reinsert the task only if it was in the queue
         waiters.insert(t);
      }
   }
}
