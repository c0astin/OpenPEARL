/*
 [The "BSD license"]
 Copyright (c) 2015 Rainer Mueller
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

#ifndef TASKTIMERINCLUDED
#define TASKTIMERINCLUDED

/**
\file

\brief timer facility for task scheduling

*/


#include "Clock.h"
#include "Duration.h"
#include "Log.h"
#include "Fixed.h"
#include "TaskCommon.h"
#include "TaskTimerCommon.h"

#include <time.h>

namespace pearlrt {
   /** \addtogroup tasking_freertos
       @{
   */

   /**
   \brief timer facility for task scheduling

   This class provides the timer functions required for task scheduling.

   A timer may be set using the PEARL scheduling parameters.
   A timer may be started. Using the start operation repeatedly will
   restart the timer if it is not expired, or will restart it.
   A timer may be cancelled to disable any further start unless
   it is set again.

   FreeRTOS timers are used. They operate on tick base. 
   For a AFTER ALL-construct the timer ist restarted in the first hit
   to the ALL-period.

   */
   class TaskTimer : public TaskTimerCommon {
   public:

      /**
      pointer to a callback function defined for easier coding
      */
      typedef void (*TimerCallback)(TaskCommon*);

      typedef void (*TimerCallbackFunction_t)( FakeTimerHandle_t xTimer );

   private:
      FakeTimerHandle_t timerHandle;
      FakeStaticTimer_t timerBuffer;

      enum TimerStates{STOPPED, DOINGAFTER, DOINGALL} state;

      // information needed for AFTER-ALL statements
      // we must reconfigure the timer after the initial delay
      Duration all; 
      int condition; // required to know if AFTER/ALL is used

      static void callbackFromFreeRTOS(FakeTimerHandle_t time);

   protected:
      void setTimer(int condition,
            Duration after, Duration all, int count);
      int startTimer();
      int stopTimer();

   public:

      /**
      initialize the timer facility

      This method must be called at system startup

      \param p the timer threads priority. If -1 the normal scheduling
               is used. If > 0 the FIFO-scheduling with this priority
               is used.

      linux specific initializations:
      <ul>
      <li>setup of time thread
      <li>check for availability of RT-Signals
      </ul>
      */
      static void init(int p);

      /**
      create the timer internal data

      \param task pointer to the task which will use this timer
      \param signalNumber number of the realtime signal for the
             timer
      \param cb callback function, which will be called on expiration
             of the timer
      */
      void create(TaskCommon * task, int signalNumber, TimerCallback cb);

      void detailedStatus(char*id, char* line);

   };

   /** @} */

}
#endif

