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
\author Jonas Meyer

*/

#include <string.h>
#include <errno.h>
#include <unistd.h>

#define _POSIX_TIMERS
#include <time.h>
#include <errno.h>

#include "Signals.h"
#include "TaskTimer.h"
#include "Log.h"
#include "Task.h"

// with gcc 7.2.1 this causes an error 
//struct sigevent {}; //shoehorn for casting

namespace pearlrt {

   static void freeRtosTimerCallback(void* tt_ptr) {
      TaskTimer* tt = (TaskTimer*)tt_ptr;
      tt->update();
   }

   void TaskTimer::create(TaskCommon * task, int signalNumber,
                          TimerCallback cb) {
      int e; // error code for timer_create

      this->timer_callback.cb = (void *)&freeRtosTimerCallback;
      this->timer_callback.th = (void *)this;
      this->callback = cb;
      this->task = task;
  
      e = timer_create(CLOCK_REALTIME,
                       (sigevent *)&timer_callback,
                       &timer);
      if (e == -1) {
         Log::error("task %s: could not create timer for signal %d",
                    task->getName(), signalNumber);
         throw theInternalTaskSignal;
      }
   }

}
