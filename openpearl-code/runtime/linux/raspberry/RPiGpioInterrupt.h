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
#ifndef RPIGPIOINTERRUPT_H_INCLUDED
#define RPIGPIOINTERRUPT_H_INCLUDED
/**
\file

\brief interrupt source from raspberry pi gpio Digital Input

*/

#include "Interrupt.h"
#include "Fixed.h"
#include "Character.h"
#include "Signals.h"
#include "Log.h"


namespace pearlrt {
   /**
   \addtogroup io_linux_driver
   @{
   */


   /**
   \file

   \brief Interrupt source from Raspberry Pi digital input

   The treament of the interrupts via gpio lines is done with the helper 
   classes RPiGpioInteruptHandler and RPiGpioInterruptTask

   The base of the implementation is libgpiod. This library provides a 
   function which invokes a callback-function on eg. falling edge of 
   one or many gpio line(s).
   This monitoring function must be called in a separate thread,
   since the is function returns only on a specific return code of the callback,
   or if the parameters are invalid.
   All lines to monitor must be passed at once.

   RPiGpioInterrupt adds the given line to a pool of lines in the 
   RPiGpioInterruptHandler. The IMC enshures in the system part setup
   that RPiGpioInterruptTask is invoked after the last RPiGpioInterrupt
   definition. Thus the list of lines to monitor is complete at this point.

   */

   class RPiGpioInterrupt: public Interrupt {

   private:
      const int gpioBit;

   public:
      /**
      constructor to create an interrupt source on the given
      gpio bit

      \param bit bit number 
      */
      RPiGpioInterrupt(int bit);

      /**
      enable the interrupt generation via falling edge detection
      enable bit
      */
      void devEnable();

      /**
      disable the interrupt generation via falling edge detection
      enable bit
      */
      void devDisable();

      /**
      get the gpio bit number of the interrupt

      \returns gpio bit number
      */
      int getGpio();

   };

   /** @} */
}
#endif
