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

\brief dation default implementation of virtual methods of SystemDation

*/


#include "SystemDation.h"
#include "Log.h"
#include "TaskCommon.h"

namespace pearlrt {

   bool SystemDation::allowMultipleIORequests() {
      return false;
   }

  void SystemDation::registerWaitingTask(void * task, int direction) {
      //Log::error("SystemDationNB: missing overload in concrete system dation");
      fprintf(stderr,"SystemDationNB: missing overload in concrete system dation\n");
      throw theInternalDationSignal;
   }

  void SystemDation::triggerWaitingTask(void * task, int direction) {
printf("SystemDation:: triggerWaitingTask called\n");
//      Log::error("SystemDationNB: missing overload in concrete system dation");
      fprintf(stderr,"SystemDationNB: missing overload in concrete system dation\n");
      throw theInternalDationSignal;
   }
   void SystemDation::suspend(TaskCommon * ioPerformingTask) {
      ioPerformingTask->doAsyncSuspend();
   }

   void SystemDation::terminate(TaskCommon * ioPerformingTask) {
      ioPerformingTask->doAsyncTerminate();
   }

}
