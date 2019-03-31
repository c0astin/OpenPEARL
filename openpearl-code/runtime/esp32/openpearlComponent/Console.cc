//Similar implementation to Linux variant
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
\brief Implementation of ESP Console Systemdation

*/

#include <stdio.h>
#include <errno.h>
//#include <unistd.h>  // write

#include "Task.h"
#include "Console.h"
#include "Dation.h"
#include "Log.h"
#include "Signals.h"
#include "BitString.h"
#include "GenericTask.h"
#include "TaskCommon.h"

SPCTASK(stbp);

DCLTASK(stbp, pearlrt::Prio(1), pearlrt::BitString<1>(1)) {
  //Equivalent to the Linux implementation, open subDations, call treat in a loop and unblock the returned task
   pearlrt::Console * console;
   pearlrt::TaskCommon * taskEntered;

   console = pearlrt::Console::getInstance();
   console->openSubDations();

   while (1) {
      taskEntered = console->treat();

      if (taskEntered) {
         pearlrt::TaskCommon::mutexLock();
         taskEntered->unblock();
         pearlrt::TaskCommon::mutexUnlock();
      }
   }
}

namespace pearlrt {

   Console* Console::instance = NULL;

   Console* Console::getInstance() {
      return instance;
   }

   void Console::openSubDations() {
      uartCommsDevice.dationOpen(NULL, 0);
   }

   TaskCommon* Console::treat() {
      TaskCommon* taskEntered;
      // the method returns to adress and length of the input record
      taskEntered =
         consoleCommon.treatLine(&bufferFromConsoleCommon,
                                 &lengthOfConsoleCommonBuffer);
      deliveredCharacters = 0;
      return taskEntered;
   }

   Console::Console() : SystemDationNB() {
      /* ctor is called before multitasking starts --> no mutex required */
      consoleMutex.name("ConsoleESP");
      inUse = false;
      cap = FORWARD;
      cap |= PRM;
      cap |= ANY;
      cap |= IN | OUT | INOUT;
      consoleCommon.setSystemDations(&uartCommsDevice, &uartCommsDevice); //uartComms is both our in and out device

      if (! instance) {
         instance = this;
      } else {
         Log::error("Console: only allowed once");
         throw theInternalDationSignal;
      }


   }
   Console::~Console() {
      instance = NULL;
   }

   int Console::capabilities() {
      return cap;
   }

   Console* Console::dationOpen(const char * idf, int openParams) {
      if (openParams & (Dation::IDF | Dation::CAN)) {
         Log::error("Console: does not support IDF and CAN");
         throw theDationParamSignal;
      }

      consoleMutex.lock();
      inUse = true;

      //No need to do anything as the subDation is supposed to stay open.

      consoleMutex.unlock();
      return this;
   }

   void Console::dationClose(int closeParams) {
      consoleMutex.lock();
      inUse = false;

      //No need to close the subDation as the task will always access it anyway

      if (closeParams & Dation::CAN) {
         Log::error("Console: CAN not supported");
         consoleMutex.unlock();
         throw theDationParamSignal;
      }

      consoleMutex.unlock();
   }

   void Console::dationRead(void * destination, size_t size) {
      char * dest = (char*) destination;

      consoleMutex.lock();

      for (size_t i = 0;
            i < size && deliveredCharacters < lengthOfConsoleCommonBuffer;
            i++) {
         dest[i] = bufferFromConsoleCommon[deliveredCharacters + i];
         deliveredCharacters ++;
      }

      consoleMutex.unlock();
   }


   void Console::dationWrite(void * source, size_t size) {
     consoleMutex.lock();
     uartCommsDevice.dationWrite(source, size);
     consoleMutex.unlock();
   }

   void Console::dationUnGetChar(const char x) {
      consoleMutex.lock();
      uartCommsDevice.dationUnGetChar(x); //Should work without issues
      consoleMutex.unlock();
   }


   void Console::translateNewLine(bool doNewLineTranslation) {
      // do nothing
   }

   bool Console::allowMultipleIORequests() {
      return true;
   }

   void Console::registerWaitingTask(void * task, int direction) {
      // just delegate to the platform independent part
      consoleCommon.registerWaitingTask(task, direction);
   }

   void Console::suspend(TaskCommon * ioPerformingTask) {
   }
   void Console::terminate(TaskCommon * ioPerformingTask) {
   }

}
