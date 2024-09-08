/*
 [A "BSD license"]
 Copyright (c) 2023  Marcel Schaible
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

#include "Log.h"
#include "Signals.h"
#include "SpoolerMotor.h"
#include "FilExtruderInt.h"

namespace pearlrt {

   SpoolerMotor::SpoolerMotor(int start) :
     start(start) {
      dationStatus = CLOSED;
   }

   SpoolerMotor::~SpoolerMotor() {
   }

   SystemDationB* SpoolerMotor::dationOpen(const RefCharacter * idf, int openParam) {
      if (idf != 0) {
         Log::error("IDF not allowed for SpoolerMotor device");
         throw theDationParamSignal;
      }

      if (dationStatus != CLOSED) {
         Log::error("SpoolerMotor: Dation already open");
         throw theOpenFailedSignal;
      }

      ns_SimFilExtruder::FilExtruderInt::getInstance()->start_simulation(pearlrt::Task::currentTask());
      dationStatus = OPENED;
      return this;
   }

   void SpoolerMotor::dationClose(int closeParam) {
      // if (closeParam != 0) {
      //    Log::error("No close parameters allowed for Pump device");
      //    throw theDationParamSignal;
      // }

      if (dationStatus != OPENED) {
         Log::error("SpoolerMotor: Dation not open");
         throw theDationNotOpenSignal;
      }

      pearlrt::Fixed<15> rpm;
      rpm = 0;

      ns_SimFilExtruder::FilExtruderInt::getInstance()->set_spoolermotor_rotational_speed(pearlrt::Task::currentTask(), rpm);

      ns_SimFilExtruder::FilExtruderInt::getInstance()->stop_simulation(Task::currentTask());
      dationStatus = CLOSED;
   }

   void SpoolerMotor::dationWrite(void* data, size_t size) {
     uint16_t value;
     if (size > 4) {
       Log::error("SpoolerMotor: max 32 bits expected");
       throw theDationParamSignal;
     }
     
     if (dationStatus != OPENED) {
       Log::error("SpoolerMotor: Dation not open");
       throw theDationParamSignal;
     }
     
     pearlrt::Fixed<15> rpm;
     value = *(uint16_t*)data;

      // expect BitString<width> as data
      // bits are left adjusted in data, thus the data must be
      // shifted according the concrete size of data
      if (size == 1)
         value = (*(char*)data) << 16;
      else
	value = (*(int16_t*)data);

      rpm = value;

      ns_SimFilExtruder::FilExtruderInt::getInstance()->set_spoolermotor_rotational_speed(pearlrt::Task::currentTask(), rpm);
   }
  
   void SpoolerMotor::dationRead(void* data, size_t size) {
      if (size > 4) {
         Log::error("SpoolerMotor: max 31 bits expected");
         throw theDationParamSignal;
      }

      if (dationStatus != OPENED) {
         Log::error("SpoolerMotor: Dation not open");
         throw theDationParamSignal;
      }

      pearlrt::Fixed<31> rpm;

      rpm = ns_SimFilExtruder::FilExtruderInt::getInstance()->get_spoolermotor_rotational_speed(pearlrt::Task::currentTask());

      (*(uint16_t*)data) = rpm.x;
   }

   int SpoolerMotor::capabilities() {
      int cap = INOUT | ANY | PRM;
      return cap;
   }
}
