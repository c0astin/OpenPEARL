/*
 [The "BSD license"]
 Copyright (c) 2012-2014 Rainer Mueller
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

#include "Control.h"
#include "Fixed.h"
#include "Log.h"

namespace pearlrt {
   /* ceateSystemElements becomes created by IMC  */
   extern int createSystemElements();

   int Control::exitCode = 0; // the exit code of the application
   Control::Initializer * Control::first = NULL;

   void Control::setExitCode(const Fixed<15> x) {
      exitCode = x.x;
   }

   int Control::getExitCode() {
      return exitCode;
   }

   int Control::addInitializer(Initializer * addMe) {
       addMe->next = first;
       first = addMe;
       return 0;
   }

   void Control::initModules() {
      int nbrOfCtorFails = pearlrt::createSystemElements();
      InitFunction next=NULL;

      if (nbrOfCtorFails > 0) {
         Log::error("*** %d system element(s) failed to initialize --> exit",
           nbrOfCtorFails);
         exit(1);
      }

      while (first) {
         next = first->init;
         first = first->next;
         if (next) {
            (*next)();
        }
     }
   } 

}


