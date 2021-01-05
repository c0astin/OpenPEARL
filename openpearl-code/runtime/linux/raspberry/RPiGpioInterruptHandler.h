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
#ifndef RPIGPIOINTERRUPTHANDLER_H_INCLUDED
#define RPIGPIOINTERRUPTHANDLER_H_INCLUDED
/**
\file

\brief manage interrupt sources from raspberry pi gpio Digital Input

*/

#include "RPiGpioInterrupt.h"


namespace pearlrt {
   /**
   \addtogroup io_linux_driver
   @{
   */


   /**
   \file

   \brief manage Interrupt sources from Raspberry Pi Digital Input

   */

   class RPiGpioInterruptHandler {

   private:
      RPiGpioInterrupt* requestedLines[32]; // we have a max of 32 gpio lines
      int numberOfRequestedLines;

      /**
      constructor for manager of interrupt source on the given gpio bit

      */
      RPiGpioInterruptHandler();
      static int eventCallback(int type, unsigned int line,
                    const struct timespec* tm, void * data); 

   public:
      /**
      RPiGpioInterruptHandler is realized as singleton

      \returns a pointer to the exesting/created object
      */
      static RPiGpioInterruptHandler * getInstance();

      /**
      add a RPiGpioInterrupt line to the list

      \param irupt the new line to be monitored 
      */
      void add(RPiGpioInterrupt * irupt);

      /**
      start monitoring
      */
      void start();

      /**
      retrieve the RPiInterrupt object vie the gpio line

      \param line the line number of the serched RPiInterrupt object

      \returns a pointer to the RPiInterrupt object, if found<br>
               else, NULL 
      */
      RPiGpioInterrupt * lookupIrq(int line);

   };

   /* @} */

}
#endif
