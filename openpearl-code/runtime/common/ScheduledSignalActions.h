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

#ifndef SCHEDULEDSIGNALACTIONS_INCLUDES
#define SCHEDULEDSIGNALACTIONS_INCLUDES

#include <stdlib.h>
#include "Signals.h"
#include "SignalAction.h"

/**
\file
\brief container for all schedules signal actions inside o PROC od TASK


*/

namespace pearlrt {

   /**
   the signal handling requires a lookup of currently handled
   signals.
   */
   class ScheduledSignalActions {
   private:
      SignalAction* actions;
      int numberOfActions;

   public:
      /** ctor
      \param nbrOfActions the numer of entries in the array actions
      \param actions the array of signal reaction
      */
      ScheduledSignalActions(int nbrOfActions, SignalAction * actions);

      /**
      lookup the index of the action if the given signal is contained
      in the list of ScheduledSignalActions

      If the action was found, the errorVariable becomes updated if an RST
      variable was set and the signal becomes disabled

      \param s pointer to the current signal

      \returns current index of the action of the given signal<br>
               0, if the signal is not contained in the list
      */
      int getActionIndexAndSetRstAndDisableHandler(Signal *s);

      template <int S>
      void setAction(int actionIndex, Signal * s, Fixed<S> & rstVariable){
	   // setAction decrements the index, thus we must do this
	   // manually for setting the rstVariable
	   actions[actionIndex-1].setRstVariable(&rstVariable);
	   setAction(actionIndex, s);
      }

      void setAction(int actionIndex, Signal * s);

      /**
      check if the signal has a registered reaction

      if the signal is scheduled with no reaction but has an error variable, 
      the error variable becomes updated

      if the signal has no scheduled reaction, just throw the same signal

      \param s pointer to the current signal

      \returns current index of the action of the given signal<br>
               0, if the signal is not contained in the list
      */
      void setErrorOrThrow(Signal *s);


   };
}

#endif
