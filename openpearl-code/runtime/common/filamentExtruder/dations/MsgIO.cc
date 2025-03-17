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
#include "MsgIO.h"
#include "../SimFilExtruderInt.h"

namespace pearlrt {

   MsgIO::MsgIO(int start) :
     start(start) {
      dationStatus = CLOSED;
   }

   MsgIO::~MsgIO() {
   }

   SystemDationB* MsgIO::dationOpen(const RefCharacter * idf, int openParam) {
      if (idf != 0) {
         Log::error("IDF not allowed for MsgIO device");
         throw theDationParamSignal;
      }

      if (dationStatus != CLOSED) {
         Log::error("MsgIO: Dation already open");
         throw theOpenFailedSignal;
      }

      ns_SimFilExtruder::SimFilExtruderInt::getInstance()->start_simulation(pearlrt::Task::currentTask());
      dationStatus = OPENED;
      return this;
   }

   void MsgIO::dationClose(int closeParam) {


      if (dationStatus != OPENED) {
         Log::error("MsgIO: Dation not open");
         throw theDationNotOpenSignal;
      }

      pearlrt::BitString<64> msg;
      msg = 0;

      ns_SimFilExtruder::SimFilExtruderInt::getInstance()->send_msg(pearlrt::Task::currentTask(), msg);

      ns_SimFilExtruder::SimFilExtruderInt::getInstance()->stop_simulation(Task::currentTask());
      dationStatus = CLOSED;
   }

   void MsgIO::dationWrite(void* data, size_t size) {
     uint64_t value;
     if (size > 8) {
       Log::error("MsgIO: max 64 bits expected");
       throw theDationParamSignal;
     }
     
     if (dationStatus != OPENED) {
       Log::error("MsgIO: Dation not open");
       throw theDationParamSignal;
     }
     
     pearlrt::BitString<64> msg;
     value = *(uint64_t*)data;

      // expect BitString<width> as data
      // bits are left adjusted in data, thus the data must be
      // shifted according the concrete size of data
      if (size == 1)
         value = (*(char*)data) << 64;
      else
	value = (*(int64_t*)data);

      msg = value;

      ns_SimFilExtruder::SimFilExtruderInt::getInstance()->send_msg(pearlrt::Task::currentTask(), msg);
   }
  
   void MsgIO::dationRead(void* data, size_t size) {
      if (size > 8) {
         Log::error("MsgIO: max 64 bits expected");
         throw theDationParamSignal;
      }

      if (dationStatus != OPENED) {
         Log::error("MsgIO: Dation not open");
         throw theDationParamSignal;
      }

      pearlrt::BitString<64> msg;

      msg = ns_SimFilExtruder::SimFilExtruderInt::getInstance()->read_msg(pearlrt::Task::currentTask());

      (*(uint64_t*)data) = msg.x;
   }

   int MsgIO::capabilities() {
      int cap = INOUT | ANY | PRM;
      return cap;
   }
}
