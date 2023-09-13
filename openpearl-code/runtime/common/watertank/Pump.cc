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

/*

 Volumenstrom einer Außen-Zahnradpumpe - zweirädrig, gleiche Zähnezahl

V = 2 * pi * d * m *b * n

V  = Volumenstrom (m3/s)
d  = Teilkreisdurchmesser (m)
m  = Modul
b  = Zahnradbreite (m)
n  = Drehzahl (1/s)

Kolbenpumpe 
Bestimmung der Fördermenge Q

Mit der Fördermenge Q wird errechnet, wie viel Volumen die 
Pumpe durchschleusen muss. Die Formel dazu ist:

Q = (Vg x n) / 1000

Vg = Füllvolumen der Pumpe
n = Anzahl der Umdrehungen



Pumpe fördert pro Umdrehung 

*/

#include "Log.h"
#include "Signals.h"
#include "Pump.h"
#include "WatertankInt.h"

namespace pearlrt {

   Pump::Pump(int start, int width) :
     start(start), width(width), m_rpm(0), m_enabled(false) {
      dationStatus = CLOSED;
   }

   Pump::~Pump() {
     m_rpm = 0;
     m_enabled = false;
   }

   SystemDationB* Pump::dationOpen(const RefCharacter * idf, int openParam) {
      if (idf != 0) {
         Log::error("IDF not allowed for Pump device");
         throw theDationParamSignal;
      }

       if ((openParam & INOUT) != INOUT) {
         Log::error("Pump must be an output device (%x)", openParam);
         throw theDationParamSignal;
      }

      if (dationStatus != CLOSED) {
         Log::error("Pump: Dation already open");
         throw theOpenFailedSignal;
      }

      ns_SimWatertank::WatertankInt::instance()->start_simulation(pearlrt::Task::currentTask());
      dationStatus = OPENED;
      return this;
   }

   void Pump::dationClose(int closeParam) {
      // if (closeParam != 0) {
      //    Log::error("No close parameters allowed for Pump device");
      //    throw theDationParamSignal;
      // }

      if (dationStatus != OPENED) {
         Log::error("Pump: Dation not open");
         throw theDationNotOpenSignal;
      }

      ns_SimWatertank::WatertankInt::instance()->stop_simulation(Task::currentTask());
      dationStatus = CLOSED;
   }

   void Pump::dationWrite(void* data, size_t size) {
     uint32_t value;
      //check size of parameter!
      // it is expected that a BitString<width> object is passed
      // with a maximum of 32 bits. This fits into 4 byte.
      // Therefore size must be less than 4
      if (size > 4) {
         Log::error("Pump: max 32 bits expected");
         throw theDationParamSignal;
      }

      if (dationStatus != OPENED) {
         Log::error("Pump: Dation not open");
         throw theDationParamSignal;
      }

      value = *(uint32_t*)data;

      if ( (value & 0x80) == 0x80) {
	//	printf("Pump: ** SWITCH ON\n"); fflush(stdout);
      }
      else {
	//	printf("Pump: *** SWITCH OFF\n"); fflush(stdout);
      }
   }
  
   void Pump::dationRead(void* data, size_t size) {
     throw theInternalDationSignal;
   }

   int Pump::capabilities() {
      int cap = OUT | ANY | PRM;
      return cap;
   }

}
