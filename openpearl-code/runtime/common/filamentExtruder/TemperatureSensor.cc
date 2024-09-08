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
#include "TemperatureSensor.h"
#include "FilExtruderInt.h"

/**
 \brief Implementation of TemperatureSensor Systemdation


*/
namespace pearlrt {

   TemperatureSensor::TemperatureSensor(int addr) :
      addr(addr){
      dationStatus = CLOSED;
      // std::cout << "***** TemperatureSensor: addr=" << addr << std::endl;
   }

   TemperatureSensor::~TemperatureSensor() {
   }

   SystemDationB* TemperatureSensor::dationOpen(const RefCharacter * idf, int openParam) {
      if (idf != 0) {
         Log::error("IDF not allowed for TemperatureSensor device");
         throw theDationParamSignal;
      }

      if ((openParam & IN) != IN) {
         Log::error("TemperatureSensor must be an input device (%x)", openParam);
         throw theDationParamSignal;
      }

      if (dationStatus != CLOSED) {
         Log::error("TemperatureSensor: Dation already open");
         throw theOpenFailedSignal;
      }

      ns_SimFilExtruder::FilExtruderInt::getInstance()->start_simulation(pearlrt::Task::currentTask());
      dationStatus = OPENED;
      return this;
   }

   void TemperatureSensor::dationClose(int closeParam) {
      if (dationStatus != OPENED) {
         Log::error("TemperatureSensor: Dation not open");
         throw theDationNotOpenSignal;
      }

      ns_SimFilExtruder::FilExtruderInt::getInstance()->stop_simulation(Task::currentTask());
      dationStatus = CLOSED;
   }


   void TemperatureSensor::dationWrite(void* data, size_t size) {
      throw theInternalDationSignal;
   }

   void TemperatureSensor::dationRead(void* data, size_t size) {
//      static char value = 0;
      
      //check size of parameter!
      if (size != 4) {
         Log::error("TemperatureSensor: data with type float expected");
         throw theDationParamSignal;
      }

      if (dationStatus != OPENED) {
         Log::error("TemperatureSensor: Dation not open");
         throw theDationParamSignal;
      }

      pearlrt::Float<23> f;

	f = ns_SimFilExtruder::FilExtruderInt::getInstance()->get_temperature_sensor(Task::currentTask());


      *(float*)data = f.x;
   }

   int TemperatureSensor::capabilities() {
      int cap = IN | ANY | PRM;
      return cap;
   }

}

