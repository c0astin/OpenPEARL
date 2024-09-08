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
#include "ScrewHeater.h"
#include "FilExtruderInt.h"

namespace pearlrt {

   ScrewHeater::ScrewHeater(int start) :
     start(start) {
      dationStatus = CLOSED;
   }

   ScrewHeater::~ScrewHeater() {
   }

   SystemDationB* ScrewHeater::dationOpen(const RefCharacter * idf, int openParam) {
      if (idf != 0) {
         Log::error("IDF not allowed for ScrewHeater device");
         throw theDationParamSignal;
      }

      if (dationStatus != CLOSED) {
         Log::error("ScrewHeater: Dation already open");
         throw theOpenFailedSignal;
      }

      ns_SimFilExtruder::FilExtruderInt::getInstance()->start_simulation(pearlrt::Task::currentTask());
      dationStatus = OPENED;
      return this;
   }

   void ScrewHeater::dationClose(int closeParam) {


      if (dationStatus != OPENED) {
         Log::error("ScrewHeater: Dation not open");
         throw theDationNotOpenSignal;
      }

      pearlrt::Fixed<15> pwm;
      pwm = 0;

      ns_SimFilExtruder::FilExtruderInt::getInstance()->set_screwheater_pwm(pearlrt::Task::currentTask(), pwm);

      ns_SimFilExtruder::FilExtruderInt::getInstance()->stop_simulation(Task::currentTask());
      dationStatus = CLOSED;
   }

   void ScrewHeater::dationWrite(void* data, size_t size) {
     uint16_t value;
     if (size > 4) {
       Log::error("ScrewHeater: max 32 bits expected");
       throw theDationParamSignal;
     }
     
     if (dationStatus != OPENED) {
       Log::error("ScrewHeater: Dation not open");
       throw theDationParamSignal;
     }
     
     pearlrt::Fixed<15> pwm;
     value = *(uint16_t*)data;

      // expect BitString<width> as data
      // bits are left adjusted in data, thus the data must be
      // shifted according the concrete size of data
      if (size == 1)
         value = (*(char*)data) << 16;
      else
	value = (*(int16_t*)data);

      pwm = value;

      ns_SimFilExtruder::FilExtruderInt::getInstance()->set_screwheater_pwm(pearlrt::Task::currentTask(), pwm);
   }
  
   void ScrewHeater::dationRead(void* data, size_t size) {
      if (size > 4) {
         Log::error("ScrewHeater: max 31 bits expected");
         throw theDationParamSignal;
      }

      if (dationStatus != OPENED) {
         Log::error("ScrewHeater: Dation not open");
         throw theDationParamSignal;
      }

      pearlrt::Fixed<31> pwm;

      pwm = ns_SimFilExtruder::FilExtruderInt::getInstance()->get_screwheater_pwm(pearlrt::Task::currentTask());

      (*(uint16_t*)data) = pwm.x;
   }

   int ScrewHeater::capabilities() {
      int cap = INOUT | ANY | PRM;
      return cap;
   }
}
