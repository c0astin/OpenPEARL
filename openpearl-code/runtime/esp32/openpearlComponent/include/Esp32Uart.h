/**
  * ESP32 UART Comms Driver Wrapper
  * Author: Patrick Scherer
  *
  * This driver serves as a wrapper for communication via uartComms.
  */

  /*
   [The "BSD license"]
   Copyright (c) 2018-2019 Patrick Scherer
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

   THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR
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

  /* 2019 (rm) migrated to Esp32Uart with integration of 
               Esp32UartCommsDriver and the required 
               elements of the previous uartComms module
  */


#ifndef ESP32UART_INCLUDED
#define ESP32UART_INCLUDED

#include "SystemDationNB.h"
#include "Mutex.h"

namespace pearlrt {

   /**
   driver for the Uarts of the Esp32 systems

   UART0 is currently reserved for the ESP-Logging stuff
   */
   class Esp32Uart : public SystemDationNB {
   private:
      Mutex inMutex;  //dationRead exclusion
      Mutex outMutex; //dationWrite exclusion
      Mutex devMutex; //device access exclusion
      int openUserDationsCount;
      char unGetChar;
      bool hasUnGetChar;
      static bool doNewLineTranslation; //This is required, apparently
      static bool setupComplete;
      int uartNumber;  // the number of the UART
   public:
     /**
      define the uart device

      \param port the number (1 or 2)
      \param baudRate the baud rate; all usual values from 300 to 115200 are
        accepted
      \param bitsPerCharacter  5-8  is allowed
      \param stopBits number of stop bits
      \param parity the desired type of parity ('O', 'E', 'N')

      \throws theInternalDationSignal in case of illegal parameter values
                               or if the rxbuffer could not be allocated
      */

      Esp32Uart(int port, int baudRate, int bitsPerCharacter,
                  int stopBits, char* parity, bool xon);

      /**
      open the system dation

      \param idf must be null, since no named files aree supported here
      \param openParam must be 0, since no open params are supported here
      \returns pointer to this object as working object
      */
      Esp32Uart* dationOpen(const char * idf, int openParam);

      /**
      close the systen dation

      \param closeParam mut be 0, since no parameters are supported
      */
      void dationClose(int closeParam);
      void dationRead(void * destination, size_t size);
      void dationWrite(void * destination, size_t size);
      void dationUnGetChar(const char c);
      int capabilities();

      /**
      translate newline<br>
      in input: CR -> \n<br>
      in output: \n -> CR+LF

      \param doNewLineTranslation true enables the translation, <br>
              false disables the translation
      */
      void translateNewLine(bool doNewLineTranslation);

      //The following is only included due to esp-idf compiler
      // complaints (pure virtual functions from Dation)
      void suspend();
      void terminate();
   };
}


#endif
