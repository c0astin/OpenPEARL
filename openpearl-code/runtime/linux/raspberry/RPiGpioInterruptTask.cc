/*
 [A "BSD license"]
 Copyright (c) 2021      Rainer Mueller
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

#include <pthread.h>
#include <errno.h>
#include <bits/local_lim.h>  // PTHREAD_STACK_MIN
#include <signal.h>

#include "RPiGpioInterruptTask.h"
#include "RPiGpioInterruptHandler.h"
#include "Log.h"
#include "Signals.h"
#include "SignalMapper.h"


/**
 \brief manager of RPiGpioInterrupts


*/
namespace pearlrt {

   static void* threadCode(void* dummy) {
      // block signals, which are treated by signalThread
      sigset_t set;
      sigemptyset(&set);
      sigaddset(&set, SIG_ACTIVATE);  // activate
      sigaddset(&set, SIG_RESUME);    // resume
      sigaddset(&set, SIG_CONTINUE);  // continue
      sigaddset(&set, SIG_SUSPEND);   // suspend

      if (sigprocmask(SIG_BLOCK, &set, NULL) < 0) {
         Log::error("RPiGpioInterruptTask: error blocking signals");
         throw theInternalTaskSignal;
      }

      RPiGpioInterruptHandler::getInstance()->start();
      return (NULL);
   }

   RPiGpioInterruptTask::RPiGpioInterruptTask() {
      pthread_attr_t threadAttrib;
      pthread_t threadPid;
      sched_param threadParam;
      int ret;

      if (pthread_attr_init(&threadAttrib) != 0) {
         Log::error("timerTask: error initializing pthread_attr");
         throw theInternalTaskSignal;
      }

      ret = pthread_attr_setstacksize(&threadAttrib, PTHREAD_STACK_MIN);

      if (ret < 0) {
         Log::error("timerTask: error setting pthread_attr_stacksize");
         throw theInternalTaskSignal;
      }

      //no inheritance of the main thread priority
      ret = pthread_attr_setinheritsched(&threadAttrib,
                                         PTHREAD_EXPLICIT_SCHED);

      if (ret != 0) {
         Log::error("timerTask: error setting pthread inheritance attributes");
         throw theInternalTaskSignal;
      }

      ret = pthread_attr_setschedpolicy(&threadAttrib, SCHED_RR);
      if (ret == 0) {
         // RT-scheduler available
	 int max = sched_get_priority_max(SCHED_RR);
         threadParam.sched_priority = max;
         ret = pthread_attr_setschedparam(&threadAttrib,
                                          &threadParam);

         if (ret != 0) {
            Log::error(
               "RPiGpioInterruptTask: error on setting priority");
            throw theInternalTaskSignal;
         }
      }


      //create the thread
      if (pthread_create(&threadPid, &threadAttrib,
                         threadCode,
                         NULL) != 0) {
         Log::error("RPiGpioInterruptTask: could not create thread (%s))",
                    strerror(errno));
         throw theInternalTaskSignal;
      }
   
   }

}
