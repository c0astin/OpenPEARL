/*
 [A "BSD license"]
 Copyright (c) 2016 Rainer Mueller
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

#ifndef RPIGPIO_INCLUDED
#define RPIGPIO_INCLUDED

#include <stdint.h>

namespace pearlrt {

   /**
   \addtogroup io_linux_driver
   @{
   */

   /**
   helper class for access to the gpio bits of the raspberry pi

   this class provides access to pull-up/-down and edge detection registers

   note that the broadcom gpio supports write access to output only
   with set and clear bit - thus no mutex protection is needed at this level.
   */
   class RPiGpio {
   private:
      int availableBits;
      RPiGpio();
      volatile uint32_t *gpio_map;

      /**
      test if gpio bit is set as alternate function
      this would conflict with usage as digila in, out or interrupt

      \param gpioNum is the numbe of the gpio-pin; only 0..31 are supporteded
      */
      void checkAlternateFunction(int gpioNum);


      /**
       set pullup/down or none for the iven gpio bit
       emmits error if pulldown is selected for i2c-lines
       \param gpio gpio bit number (0..31)
       \param pullUpDownFlag as #defined in RPiGpio.cc
      */
      void setupPullUpDown(int gpio, int pullUpDownFlag); 

   public:
      /**
         supported modes of the i/o-usage

         <ul>
         <li>DIGIN provides digital input mode
         <li>DIGOUT provides digital output mode
         </ul>
      */
      enum RPiGpioMode {DIGOUT, DIGIN};

      /** selectors for pull-up or pull-down */
      enum RPiGpioPud {NONE, DOWN, UP};

      /**
      the RPiGpio class operates as singleton

      \returns a pointer to the existing/created object
      */
      static RPiGpio* instance();

      /**
      set gpio direction

      \param start starting bit number (large value)
               (2 to 27 are available on RPi model 2)
      \param width number of bits to be used
      \param direction GpioMode::DIGOUT or GpioMode::DIGIN
      \param pud pull-up pull-down configuration
      */
      void useBits(int start, int width,
                   RPiGpioMode direction, RPiGpioPud pud);

      /**
      write the bits from value to the gpiobits from (start .. start-width+1)

      \param start first (leftmost) gpio bit number (large value)
      \param width number of consecutive gpio bits (2,...26)
      \param value the value to be written. The value is left adjusted
                    shifting occurs inside of this method
      */
      void writeBits(int start, int width, int32_t value);

      /**
      read the bits from the gpio bits from (start .. start-width+1)

      \param start first (leftmost) gpio bit number (large value)
      \param width number of consecutive gpio bits (1,...26)
      \returns the value to be written. The value is right adjusted
      */
      int32_t readBits(int start, int width);

      /**
       set or reset the falling edge detction flag
       \param gpioNum gpio bit number (0..31)
       \param enable true, enable edge detection; 
		false, disable edge detection
      */
      void enableFallingEdgeDetection(int gpioNum, bool enable); 

   public:
   };
   /** @} */
}
#endif
