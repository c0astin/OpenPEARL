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


#include "RPiGpioInterruptHandler.h"
#include "Log.h"
#include "Signals.h"
#include <gpiod.h>


/**
 \brief manager of RPiGpioInterrupts


*/
namespace pearlrt {

   int RPiGpioInterruptHandler::eventCallback(int type, unsigned int line,
	 const struct timespec* tm, void * data) {
     //const char * types[] = {"timeout","rising", "falling"};
     //printf("got type: %s  for line %d\n", types[type-1],line);
     RPiGpioInterrupt * irupt = 
          RPiGpioInterruptHandler::getInstance()->lookupIrq(line);
     if (irupt != NULL) {
        Log::info("RPiGpioInterruptTask: call trigger ");
        irupt->trigger();
        return(GPIOD_CTXLESS_EVENT_CB_RET_OK);  // _STOP to finish
     }
     Log::warn("RPiInterruptHandler: suprious interrupt");        
     return(GPIOD_CTXLESS_EVENT_CB_RET_OK);  // _STOP to finish

   }

   RPiGpioInterruptHandler::RPiGpioInterruptHandler() {
      numberOfRequestedLines = 0;
   }

   RPiGpioInterruptHandler* RPiGpioInterruptHandler::getInstance() {
     static RPiGpioInterruptHandler* _instance = NULL;
       if (_instance == NULL) {
          _instance = new RPiGpioInterruptHandler();
       }
       return _instance;
   } 

   void RPiGpioInterruptHandler::add(RPiGpioInterrupt * irupt) {
      requestedLines[numberOfRequestedLines++] = irupt;
   }

   RPiGpioInterrupt* RPiGpioInterruptHandler::lookupIrq(int line) {
      for (int i=0;i<numberOfRequestedLines; i++) {
        if (requestedLines[i]->getGpio() == line) {
           return (requestedLines[i]);
        };
      }
      return (NULL);
   }
   

   void RPiGpioInterruptHandler::start() {
      unsigned int lines[32];
      for (int i=0;i<numberOfRequestedLines; i++) {
        lines[i] = requestedLines[i]->getGpio();
      }
      int ret = gpiod_ctxless_event_monitor_multiple_ext(
       		 "gpiochip0",
        	 GPIOD_CTXLESS_EVENT_FALLING_EDGE,
                 lines, numberOfRequestedLines,
		  0,     // active low
                 "RPiGpioInterrupt", 
		 NULL,  // timeout
                 NULL, //callback,
                 eventCallback,
                 NULL, // parameter for callback
        	 0  // flags: setup of the lines is already done
        );
      if (ret < 0) {
         Log::error("RPiGpioInterruptHandler: failed to monitor via libgpiod");
         throw theInternalSignal;
      }

   }
}
