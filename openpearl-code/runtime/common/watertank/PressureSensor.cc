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
#include "PressureSensor.h"
#include "WatertankInt.h"

/**
 \brief Implementation of PressureSensor Systemdation


*/
namespace pearlrt {

   PressureSensor::PressureSensor(int addr) :
      addr(addr){
      dationStatus = CLOSED;
   }

   PressureSensor::~PressureSensor() {
   }

   SystemDationB* PressureSensor::dationOpen(const RefCharacter * idf, int openParam) {
      if (idf != 0) {
         Log::error("IDF not allowed for PressureSensor device");
         throw theDationParamSignal;
      }

      if ((openParam & IN) != IN) {
         Log::error("PressureSensor must be an input device (%x)", openParam);
         throw theDationParamSignal;
      }

      if (dationStatus != CLOSED) {
         Log::error("PressureSensor: Dation already open");
         throw theOpenFailedSignal;
      }

      ns_SimWatertank::WatertankInt::instance()->start_simulation(pearlrt::Task::currentTask());
      dationStatus = OPENED;
      return this;
   }

   void PressureSensor::dationClose(int closeParam) {
      if (dationStatus != OPENED) {
         Log::error("PressureSensor: Dation not open");
         throw theDationNotOpenSignal;
      }

      ns_SimWatertank::WatertankInt::instance()->stop_simulation(Task::currentTask());
      dationStatus = CLOSED;
   }


   void PressureSensor::dationWrite(void* data, size_t size) {
      throw theInternalDationSignal;
   }

   void PressureSensor::dationRead(void* data, size_t size) {
      int d;
      static char value = 0;
      
      //check size of parameter!
      // it is expected that a BitString<width> object is passed
      // with a maximum of 32 bits. This fits into 4 byte.
      // Therefore size must be less than 4
      if (size > 4) {
         Log::error("PressureSensor: max 32 bits expected");
         throw theDationParamSignal;
      }

      if (dationStatus != OPENED) {
         Log::error("PressureSensor: Dation not open");
         throw theDationParamSignal;
      }

      *(char*)data = value;
   }

   int PressureSensor::capabilities() {
      int cap = IN | ANY | PRM;
      return cap;
   }

}

