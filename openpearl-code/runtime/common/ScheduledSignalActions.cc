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

#include "ScheduledSignalActions.h"


namespace pearlrt {

    /**
   the signal handling requires a lookup of currently handled
   signals.
     */
    ScheduledSignalActions::ScheduledSignalActions(int nbrOfActions, SignalAction* actions) {
        numberOfActions = nbrOfActions;
        this->actions = actions;
    }

    int ScheduledSignalActions::getActionIndexAndSetRstAndDisableHandler(Signal *s) {
        int i;
        int groupLevelOfAction;
        int groupLevelAction, allLevelAction;
        int result = 0;
	int currentSignal = s->whichRST();

        //printf("got signal %d\n", s->whichRST());
        groupLevelAction = -1;  // 99 signals
        allLevelAction = -1;    // all signals

        for (i=0; i<numberOfActions; i++) {
            if (!actions[i].isEnabled() ) {
		continue;
 	    }
            if (actions[i].getSignal() == NULL) {
                //printf("action[%d] has no signal yet\n", i);
		continue;
	    }

            int signalOfAction =  actions[i].getSignal()->whichRST();
            //printf("action[%d] has signal %d\n", i,signalOfAction);

            if (currentSignal == signalOfAction) {
	      result = i+1;
	      break; // no need for further iterations
            } else if (currentSignal/100*100 == signalOfAction) {
		groupLevelAction = i+1;
            } else if (currentSignal/1000*1000 == signalOfAction) {
		allLevelAction = i+1;
            }
	}
		

        if (result == 0 && groupLevelAction>0) {
            // found action for 'group level'  (99 signals)
            result= groupLevelAction; 
	}
        if (result== 0 && allLevelAction>0) { 
            // found action for 'all signals' (2) (all signals)
            result = allLevelAction; 
        }
        if (result != 0) {
            actions[result-1].updateRst(s);
	    //printf("disable action[%d]\n",result-1);
            actions[result-1].disable();
        }
	//printf("result = %d\n\n", result);
        return result;
    }

    void ScheduledSignalActions::setAction(int actionIndex, Signal *s) {
        //printf("set action %d for signal %d\n", actionIndex-1, s->whichRST());

        // reset all scheduled actions for this signal
        for (int i=0; i<numberOfActions; i++) {
            if (actions[i].getSignal() != NULL) {
                if (actions[i].getSignal()->whichRST() == s->whichRST()) {
                    actions[i].setSignal(NULL);
                }
            }
        }

        // set reaction for this signal
        actions[actionIndex-1].setSignal(s);
        //printf("action[%d] has now a signal \n", actionIndex-1);
    }

    void ScheduledSignalActions::setErrorOrThrow(Signal *s) {
        //printf("set error for signal %d\n", s->whichRST());
        int index = getActionIndexAndSetRstAndDisableHandler(s) ;
        if (index > 0) {
           // printf("  ... rst should become set .. returning\n");
            return;
        }
        //printf("ScheduledSignalActions:  ... throw\n");
        throw *s;
    };
}

