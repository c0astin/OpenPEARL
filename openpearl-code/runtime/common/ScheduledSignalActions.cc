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
        int level1Action, level2Action, level3Action;
        int result = 0;

        //printf("got signal %d\n", s->whichRST());
        level1Action = -1;
        level2Action = -1;
        level3Action = -1;

        for (i=0; i<numberOfActions; i++) {
            if (actions[i].isEnabled() ) {
                if (actions[i].getSignal() == NULL) {
                    //printf("action[%d] has no signal yet\n", i);
                } else {
                    //printf("action[%d] has signal %d\n", i,actions[i].getSignal()->whichRST());
                    groupLevelOfAction = 0;
                    int signalOfAction =  actions[i].getSignal()->whichRST();
                    if (signalOfAction / 10 * 10 == signalOfAction){
                        groupLevelOfAction= 1;
                    } else if (signalOfAction / 100 * 100 == signalOfAction){
                        groupLevelOfAction= 2;
                    } else if (signalOfAction / 1000 * 1000 == signalOfAction){
                        groupLevelOfAction= 3;
                    }
                    //printf("actions[%d]: registered signal has level %d\n", i, groupLevelOfAction);

                    switch (groupLevelOfAction) {
                    case 0:
                        if (actions[i].getSignal()->whichRST() == s->whichRST()) {
                            //printf(".. exact match --> result=%d\n", i+1);
                            result = i+1;
                        }
                        break;
                    case 1:
                        if (actions[i].getSignal()->whichRST() == s->whichRST()/10*10) {
                            //printf(".. level1 match --> result=%d\n", i+1);
                            level1Action = i+1;
                        }
                        break;
                    case 2:
                        if (actions[i].getSignal()->whichRST() == s->whichRST()/100*100) {
                            //printf(".. level2 match --> result=%d\n", i+1);
                            level2Action = i+1;
                        }
                        break;
                    case 3:
                        if (actions[i].getSignal()->whichRST() == s->whichRST()/1000*1000) {
                            //printf(".. level3 match --> result=%d\n", i+1);
                            level3Action = i+1;
                        }
                        break;
                    }
                }
            }
        }

        if (result == 0 && level1Action>0)
            result= level1Action;  // found action for group level 1 (9 signals)
        if (result== 0 && level2Action>0) 
            result = level2Action;  // found action for group level 2 (99 signals)
        if (result == 0 && level3Action>0) 
            result = level3Action;  // found action for group level 3 (999 signals)
        if (result == 0) {
            //printf("no match found\n\n");
        } else {
            actions[result-1].updateRst(s);
	    //printf("disable action[%d]\n",result-1);
            actions[result-1].disable();
        }
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
            //printf("  ... rst should become set .. returning\n");
            return;
        }
        printf("ScheduledSignalActions:  ... throw\n");
        throw *s;
    };
}

