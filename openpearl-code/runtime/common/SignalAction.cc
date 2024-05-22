/*
[A "BSD license"]
Copyright (c) 2024 Rainer Mueller
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
\brief a helper class to determine whether a signal has
   currently a handler.
*/
#include "SignalAction.h"
#include "Signals.h"
#include "Fixed.h"

namespace pearlrt {

   SignalAction::SignalAction() {
      signal = NULL;
      //rst = NULL;
      enabled = true;
   }

   SignalAction::SignalAction(Signal * s) {
      signal = s;
      //rst = NULL;
      enabled = true;
   }

//   Fixed<15>* SignalAction::getRstVariable() {
//      return rstVariable;
//   }
   bool SignalAction::updateRst(Signal * s) {
      return rst.updateRst(s);
   }

   void SignalAction::setSignal(Signal * signal) {
      this->signal = signal;
   }

   Signal* SignalAction::getSignal() {
      return signal;
   }

   void SignalAction::enable() {
      enabled = true;
   }     

   void SignalAction::disable() {
      enabled = false;
   }     
   bool SignalAction::isEnabled() {
       return enabled;
   }
}
