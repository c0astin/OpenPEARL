/**
  * ESP32 UART Comms Driver Wrapper
  * Author: Patrick Scherer
  *
  * A very basic OpenPEARL device wrapper used to communicate via uartComms.
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


#include "FreeRTOSConfig.h"
#include "FreeRTOS.h"
#include "task.h"
#include "semphr.h"
//#include "allTaskPriorities.h"

#include "Esp32UartCommsDriver.h"
#include "Log.h"

extern "C" {
  #include "uartComms.h"
}

#define HAS_UNGETCHAR 1 //We could return the last char to the front of the RX queue if necessary.

//Implementation work-in-progress, may function in its current state but is not guaranteed.
namespace pearlrt {

  bool Esp32UartCommsDriver::doNewLineTranslation = false;  //This is required.
  bool Esp32UartCommsDriver::setupComplete = false;

  Esp32UartCommsDriver::Esp32UartCommsDriver() {
  /**
    * Esp32 Uart Comms Driver Constructor
    *
    * Prepares the driver and uartComms for usage.
    */

    //Due to dationRead() not returning when there is no UART input, we'll seperate IO mutexes
    //devMutex serves as a whole-device mutex to ensure no device access happens twice.
    inMutex.name("Esp32UartCommsDriverIn");
    outMutex.name("Esp32UartCommsDriverOut");
    devMutex.name("Esp32UartCommsDriver");
    openUserDationsCount = 0;
    hasUnGetChar = false;
    doNewLineTranslation = false;

    if (!setupUartComms()) {
      Log::error("Esp32UartCommsDriver: could not initialize uartComms");
      throw theInternalDationSignal;
    }

    setupComplete = true;
  }

  Esp32UartCommsDriver* Esp32UartCommsDriver::dationOpen(const char * idf, int openParam) {
  /**
    * Esp32 Uart Comms Driver Dation Open
    *
    * Opens a dation, assuming setup has completed.
    */

    if (openParam & (Dation::IDF | Dation::CAN)) {
       Log::error("Esp32UartCommsDriver: does not support IDF and CAN");
       throw theDationParamSignal;
    }

    devMutex.lock();

    if (!setupComplete) {
       Log::error("Esp32UartCommsDriver: setup not completed");
       devMutex.unlock();
       throw theOpenFailedSignal;
    }

    if (openUserDationsCount == 0) {
      enableUartComms();
    }

    openUserDationsCount++;

    devMutex.unlock();

    return this;

  }

  void Esp32UartCommsDriver::dationClose(int closeParam) {
  /**
    * Esp32 Uart Comms Driver Dation Close
    *
    * Closes a dation, if possible.
    */

    devMutex.lock();

    if (openUserDationsCount == 0) {
       Log::error("Esp32UartCommsDriver: no dation opened");
       devMutex.unlock();
       throw theCloseFailedSignal;
    }

    if (closeParam & Dation::CAN) {
       Log::error("Esp32UartCommsDriver: CAN not supported");
       devMutex.unlock();
       throw theDationParamSignal;
    }

    openUserDationsCount--;

    if (openUserDationsCount == 0) {
      disableUartComms();
    }

    devMutex.unlock();
  }

  void Esp32UartCommsDriver::dationRead(void * destination, size_t size) {
  /**
    * Esp32 Uart Comms Driver Dation Read
    *
    * Reads a certain amount of items from the uartComms driver and writes it to the destination pointer.
    * Reading more items than are currently available will force the driver to wait for uartComms' RX queue.
    * If there is an unGetChar available, it will be returned first.
    */

    devMutex.lock();
    char* dataPointer = (char*) destination;

    if (openUserDationsCount == 0) {
       Log::error("Esp32UartCommsDriver: not opened");
       devMutex.unlock();
       throw theDationNotOpenSignal;
    }

    if (!setupComplete) {
       Log::error("Esp32UartCommsDriver: setup incomplete");
       devMutex.unlock();
       throw theReadingFailedSignal;
    }

    if (hasUnGetChar) {
       hasUnGetChar = false;
       *dataPointer = unGetChar;
       size--;
       dataPointer++;
    }

    inMutex.lock(); //Access input mutex before releasing device mutex. Ensure we have access to the IO first
    devMutex.unlock();


    for (int receivedCharacterCount = 0; receivedCharacterCount < size; receivedCharacterCount++) {
      *dataPointer = (char)receiveSingleUartCharacter();  //This function returns an uint8_t so casting should be fine
      dataPointer++;
    }

    inMutex.unlock();

  }

  void Esp32UartCommsDriver::dationWrite(void * destination, size_t size) {
  /**
    * Esp32 Uart Comms Driver Dation Write
    *
    * Writes a given amount of characters from the given destination to the uart.
    */

    devMutex.lock();

    char* dataPointer = (char*) destination;

    if (openUserDationsCount == 0) {
       Log::error("Esp32UartCommsDriver: not opened");
       devMutex.unlock();
       throw theDationNotOpenSignal;
    }

    if (!setupComplete) {
       Log::error("Esp32UartCommsDriver: setup incomplete");
       devMutex.unlock();
       throw theReadingFailedSignal;
    }


    uint8_t charsToTransfer[size];
    for (int charsTransferred = 0; charsTransferred < size; charsTransferred++) {
      charsToTransfer[charsTransferred] = (uint8_t) *dataPointer;
      dataPointer++;
    }

    outMutex.lock();
    devMutex.unlock();

    sendMultipleUartCharacters(charsToTransfer, (uint8_t) size);

    outMutex.unlock();

  }

  void Esp32UartCommsDriver::dationUnGetChar(const char c) {
  /**
    * Esp32 Uart Comms Driver Dation UnGet Char
    *
    * UnGets a given character. This is currently done by simply setting a boolean and saving the character.
    * We cannot unget multiple characters at this time.
    //TODO: Return character to RX Queue instead, thus allowing multiple characters to be returned to the device.
    */

    devMutex.lock();

    if (openUserDationsCount == 0) {
       Log::error("Esp32UartCommsDriver: not opened");
       devMutex.unlock();
       throw theDationNotOpenSignal;
    }

    if (!setupComplete) {
       Log::error("Esp32UartCommsDriver: setup incomplete");
       devMutex.unlock();
       throw theReadingFailedSignal;
    }


    hasUnGetChar = true;
    unGetChar = c;

    devMutex.unlock();

  }

  int Esp32UartCommsDriver::capabilities() {
  /**
    * Esp32 Uart Comms Driver Capabilities
    *
    * Identical to the LPC USB Keyboard implementation. Capable of FORWARD, IN, PRM, ANY OUT and INOUT
    */

    return (Dation::FORWARD | Dation::IN | Dation::PRM | Dation::ANY | Dation::OUT | Dation::INOUT);
  }

  void Esp32UartCommsDriver::translateNewLine(bool doNewLineTranslation) {
  /**
    * Esp32 Uart Comms Driver Translate New Line
    *
    * Simply sets the doNewLineTranslation boolean to true.
    */

    this->doNewLineTranslation = doNewLineTranslation;
  }


  //The following is only included due to esp-idf compiler complaints (pure virtual functions from Dation)
  void Esp32UartCommsDriver::suspend() {
    Log::error("Esp32UartCommsDriver: cannot suspend");
    throw theDationNotSupportedSignal;
  }

  void Esp32UartCommsDriver::terminate() {
    Log::error("Esp32UartCommsDriver: cannot terminate");
    throw theDationNotSupportedSignal;
  }

}
