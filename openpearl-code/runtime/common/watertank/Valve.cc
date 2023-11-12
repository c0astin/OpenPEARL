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
#include "Valve.h"
#include "WatertankInt.h"

/**
 \brief Implementation of Valve Systemdation
*/

namespace pearlrt {

  Valve::Valve() {
      dationStatus = CLOSED;
   }

   Valve::~Valve() {
     dationStatus = CLOSED;
   }

   SystemDationB* Valve::dationOpen(const RefCharacter * idf, int openParam) {
      if (idf != 0) {
         Log::error("IDF not allowed for Valve device");
         throw theDationParamSignal;
      }

      if ((openParam & INOUT) != INOUT) {
         Log::error("Valve must be an input/output device (%x)", openParam);
         throw theDationParamSignal;
      }

      if (dationStatus != CLOSED) {
         Log::error("Valve: Dation already open");
         throw theOpenFailedSignal;
      }

      ns_SimWatertank::WatertankInt::getInstance()->start_simulation(pearlrt::Task::currentTask());
      dationStatus = OPENED;
      return this;
   }

   void Valve::dationClose(int closeParam) {
      if (dationStatus != OPENED) {
         Log::error("Valve: Dation not open");
         throw theDationNotOpenSignal;
      }

      ns_SimWatertank::WatertankInt::getInstance()->stop_simulation(Task::currentTask());
      dationStatus = CLOSED;
   }

   void Valve::dationWrite(void* data, size_t size) {
      static char value = 0;

      //check size of parameter!
      if (size > 2) {
         Log::error("Valve: max 16 bits expected");
         throw theDationParamSignal;
      }

      if (dationStatus != OPENED) {
         Log::error("Valve: Dation not open");
         throw theDationParamSignal;
      }

      value = *(uint16_t*)data;

      // expect BitString<width> as data
      // bits are left adjusted in data, thus the data must be
      // shifted according the concrete size of data
      if (size == 1)
         value = (*(char*)data) << 16;
      else
	value = (*(int16_t*)data);

      if ( value == 1 )
	ns_SimWatertank::WatertankInt::getInstance()->open_valve(pearlrt::Task::currentTask());
      else
	ns_SimWatertank::WatertankInt::getInstance()->close_valve(pearlrt::Task::currentTask());
   }

   void Valve::dationRead(void* data, size_t size) {
      static char value = 0;

      //check size of parameter!
      if (size > 2) {
         Log::error("Valve: max 16 bits expected");
         throw theDationParamSignal;
      }

      if (dationStatus != OPENED) {
         Log::error("Valve: Dation not open");
         throw theDationParamSignal;
      }

      pearlrt::BitString<1> state;
      state = ns_SimWatertank::WatertankInt::getInstance()->get_valve_state(pearlrt::Task::currentTask());

      value = state.x;
      if ( value == 0 )
	*(int16_t*)data = 0;
      else
	*(int16_t*)data = 1;
   }

int Valve::capabilities() {
      int cap = INOUT | ANY | PRM;
      return cap;
   }

}
