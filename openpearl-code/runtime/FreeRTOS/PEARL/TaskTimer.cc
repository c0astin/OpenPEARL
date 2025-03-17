/*
 [The "BSD license"]
 Copyright (c) 2015 Rainer Mueller, Jonas Meyer
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

\brief timer facility for tasking

\author R. Mueller

*/

#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <inttypes.h> // printf int64_t

//#define _POSIX_TIMERS    // not required for esp32 v4.2
#include <time.h>
#include <errno.h>

#include "Signals.h"
#include "TaskTimer.h"
#include "Log.h"
#include "Task.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/timers.h"

namespace pearlrt {

   void TaskTimer::callbackFromFreeRTOS(FakeTimerHandle_t timer) {
      uint64_t period;
      TickType_t ticks;
      int result;
      TaskTimer * tt = (TaskTimer*)(pvTimerGetTimerID(timer));
      switch (tt->state) {
         case DOINGAFTER:
            if (tt->condition & TaskCommon::ALL) {
              tt->state = DOINGALL;
              vTimerSetReloadMode(tt->timerHandle, pdTRUE); //  autoreload
              period = tt->all.getSec()*1000 + tt->all.getUsec()/1000;
              ticks = period / portTICK_PERIOD_MS;
//printf("set new period %d all=%" PRId64 " %d\n",ticks, tt->all.getSec(),tt->all.getUsec());
              result = xTimerChangePeriod(tt->timerHandle,ticks,10); // 10 ms block time
              if (result != pdPASS) {
                 Log::error("could not modify timer within 10 ms");
                 throw theInternalTaskSignal;
              }
	      tt->startTimer();
            }
            break;
         case DOINGALL:
            break;
         default: break;
      }
      tt->update();
   }

   void TaskTimer::create(TaskCommon * task, int signalNumber,
                          TimerCallback cb) {

      this->callback = cb;
      this->task = task;

      timerHandle = xTimerCreateStatic(NULL,  // name
                         1, // period
                         0, // auto reload
                         this, // timer id
                         callbackFromFreeRTOS, // callback
		         (StaticTimer_t*)(&timerBuffer));
       if (timerHandle == NULL) {
         Log::error("could not create timer");
         throw theInternalTaskSignal;
      }
      state = STOPPED;
   }

   void TaskTimer::setTimer(int condition,
            Duration after, Duration all, int count) {
      uint64_t period;
      TickType_t ticks;
      BaseType_t result;
      this->condition = condition;
      this->all = all;
      if (condition & TaskCommon::AFTER) {
         vTimerSetReloadMode(timerHandle, pdFALSE); // no autoreload
         period = after.getSec()*1000 + after.getUsec()/1000;
         ticks = period / portTICK_PERIOD_MS;
         state = DOINGAFTER;
         result = xTimerChangePeriod(timerHandle,ticks,10); // 10 ms block time
         if (result != pdPASS) {
            Log::error("could not modify timer within 10 ms");
            throw theInternalTaskSignal;
         }
         counts ++;  
      } else if (condition & TaskCommon::ALL) {
         vTimerSetReloadMode(timerHandle, pdTRUE); //  autoreload
         period = all.getSec()*1000 + all.getUsec()/1000;
         ticks = period / portTICK_PERIOD_MS;
         state = DOINGALL;
         result = xTimerChangePeriod(timerHandle,ticks,10); // 10 ms block time
         if (result != pdPASS) {
            Log::error("could not modify timer within 10 ms");
            throw theInternalTaskSignal;
         }
      }
   }

   int TaskTimer::startTimer() {
      BaseType_t result;
      result = xTimerStart(timerHandle,10); // 10 ms block time
      if (result != pdPASS) {
         Log::error("could not start timer within 10 ms");
         throw theInternalTaskSignal;
      }
      return 0;
   }

   int TaskTimer::stopTimer() {
      BaseType_t result;
      result = xTimerStop(timerHandle,10); // 10 ms block time
      if (result != pdPASS) {
         Log::error("could not stop timer within 10 ms");
         throw theInternalTaskSignal;
      }
      return 0;
   }


}
