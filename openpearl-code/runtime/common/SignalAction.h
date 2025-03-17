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

#ifndef SIGNALACTION_INCLUDES
#define SIGNALACTION_INCLUDES

#include <stdlib.h>
#include "Signals.h"
#include "Fixed.h"
#include "Rst.h"

/**
\file
\brief a helper class to determine whether a signal has
   currently a handler and/or RST(error) variable set.
*/

namespace pearlrt {

   /**
   the signal handling requires a lookup of currently handled
   signals.
   */
   class SignalAction {

   private:
      Signal* signal;
      Rst rst;
      bool enabled;

   public:
      SignalAction();  // default ctor for array declaractions

      /** ctor
      \param s the signal which should be treated by this handler
      */
      SignalAction(Signal * s);

      Signal* getSignal();
      void setSignal(Signal* s);
   
      /**
       set the RST variable if specified in the ON-statement
	\tparam S precission of the FIXED variable
        \param rstVariable pointer to the rst-variable
      */  
      template <int S> 
      void setRstVariable(Fixed<S> * rstVariable) {
	this->rst.rst(rstVariable,S);
      }

      /**
	set the rst variable if t was sepcified which the curent signal number
        \returns true, if the variable was set
        \returns false, if no rst-variable was defined
      */
      bool updateRst(Signal * s);

      /**
      disable the action.

      This method is used after the signal handler to disable the signalAction during a signal handler.
      */
      void disable();
      /**
      enable the action.

      This method is used after the signal handler to reenable the signalAction again.
      */
      void enable();

      /**
      check if the handler is currently enabled
      \returns true , if enabled
      \returns false , if disabled
      */
      bool isEnabled() ;
   };
}

#endif
