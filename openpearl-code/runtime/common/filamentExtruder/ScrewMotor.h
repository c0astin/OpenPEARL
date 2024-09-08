/*
 [A "BSD license"]
 Copyright (c) 2023 Marcel Schaible
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
#ifndef SCREWMOTOR_H_INCLUDED
#define SCREWMOTOR_H_INCLUDED

#include <cstdint> 

#include "SystemDationB.h"
#include "Fixed.h"
#include "Character.h"
#include "RefChar.h"
#include "Signals.h"
#include "Log.h"

namespace pearlrt {

  class ScrewMotor: public SystemDationB {

   public:
      ScrewMotor(int start);
      ~ScrewMotor();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();

   private:
      const int start;
  };
}

#endif
