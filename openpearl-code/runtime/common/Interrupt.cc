/*
 [The "BSD license"]
 Copyright (c) 2012-2014 Rainer Mueller
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

\brief type interrupt

This module realizes the PEARL type interrupt.

\author R. Mueller

*/

#include <stdio.h>
#include "Interrupt.h"
#include "Log.h"
#include "TaskWhenLinks.h"
#include "TaskCommon.h"

namespace pearlrt {

   Interrupt::Interrupt() {
      isEnabled = false;
      headContinueTaskQueue = 0;
      headActivateTaskQueue = 0;
   }

   void Interrupt::enable() {
      TaskCommon::mutexLock();
      isEnabled = true;
      devEnable();
      TaskCommon::mutexUnlock();
   }

   void Interrupt::disable() {
      TaskCommon::mutexLock();
      isEnabled = false;
      devDisable();
      TaskCommon::mutexUnlock();
   }

   void Interrupt::trigger() {
      TaskWhenLinks * current;
      TaskCommon::mutexLock();
      Log::info("%s: Interrupt: triggered (enable=%d)",
         TaskCommon::getCallingTaskName(),isEnabled);

      mutexOfInterrupt.lock();

      if (isEnabled) {
          if (!headContinueTaskQueue) {
              Log::info("trigger: no task waits for continue");
          }
          while (headContinueTaskQueue) {
            current = headContinueTaskQueue;
            headContinueTaskQueue = headContinueTaskQueue->getNextContinue();
            Log::info("%s: trigger: waiting task triggered",
                  TaskCommon::getCallingTaskName());
            current -> triggeredContinue();
         }

         for (TaskWhenLinks * h = headActivateTaskQueue;
               h != NULL;
               h = h->getNextActivate()) {
            h->triggeredActivate();
         }

      }
      Log::info("Interrupt::trigger released interrupt.lock");
      mutexOfInterrupt.unlock();
      TaskCommon::mutexUnlock();
   }

   // register is called with mutexTasks locked
   void Interrupt::registerActivate(TaskWhenLinks * t, TaskWhenLinks ** next) {
      mutexOfInterrupt.lock();
      *next = headActivateTaskQueue;
      headActivateTaskQueue = t;
      mutexOfInterrupt.unlock();
   }

   // register is called with mutexTasks locked
   void Interrupt::registerContinue(TaskWhenLinks * t, TaskWhenLinks ** next) {
      mutexOfInterrupt.lock();
Log::info("registerContinue task");
      *next = headContinueTaskQueue;
      headContinueTaskQueue = t;
      mutexOfInterrupt.unlock();
   }

   void Interrupt::unregisterActivate(TaskWhenLinks * t) {
      TaskWhenLinks * last = 0;

      mutexOfInterrupt.lock();
      for (TaskWhenLinks * h = headActivateTaskQueue;
            h != 0;
            h = h->getNextActivate()) {
         if (t == h) {
            if (last == 0) {
               // first item to remove
               headActivateTaskQueue = h->getNextActivate();
            } else {
               last->setNextActivate(h->getNextActivate());
            }

            break;
         }

         last = h;
      }
      mutexOfInterrupt.unlock();
   }

   void Interrupt::unregisterContinue(TaskWhenLinks * t) {
      TaskWhenLinks * last = 0;

      mutexOfInterrupt.lock();

      for (TaskWhenLinks * h = headContinueTaskQueue;
            h != 0;
            h = h->getNextContinue()) {
         if (t == h) {
            if (last == 0) {
               // first item to remove
               headContinueTaskQueue = h->getNextContinue();
            } else {
               last->setNextContinue(h->getNextContinue());
            }

            break;
         }

         last = h;
      }
      mutexOfInterrupt.unlock();
   }

}
