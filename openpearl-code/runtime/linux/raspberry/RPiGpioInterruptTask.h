/*
 [A "BSD license"]
 Copyright (c) 2021 Rainer Mueller
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
#ifndef RPIGPIOINTERRUPTTASK_H_INCLUDED
#define RPIGPIOINTERRUPTTASK_H_INCLUDED
/**
\file

\brief task receiving the events from  interrupt sources from raspberry pi gpio Digital Input

*/

#include "RPiGpioInterruptHandler.h"


namespace pearlrt {
   /**
   \addtogroup io_linux_driver
   @{
   */


   /**
   \file

   \brief task receivinge the events from interrupt sources from Raspberry Pi Digital Input

   */

   class RPiGpioInterruptTask {

   private:

   public:
      /**
      constructor for the task. The ctor creates a separate thread. 
      The main operation of the thread is the call of the start()-method
      of the RPiInterruptHandler
      */
      RPiGpioInterruptTask();


   };

   /** @} */
}
#endif
