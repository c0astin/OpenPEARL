/*
 [The "BSD license"]
 Copyright (c) 2012-2013 Rainer Mueller
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

\brief  priority mapper

This module provides the mapping algorithm from PEARL
priorities to system priorities.

*/

#include <stdio.h>
#include <sched.h>
#include <stdlib.h>

#include "Log.h"
#include "Fixed.h"
#include "Signals.h"
#include "PrioMapper.h"
#include "Task.h"
#include "TaskList.h"

namespace pearlrt {

   PrioMapper* PrioMapper::instance = NULL;

   PrioMapper* PrioMapper::getInstance() {
      if (instance == NULL) {
         instance = new PrioMapper();
      }

      return instance;
   }

   PrioMapper::PrioMapper() {
      max = Task::getThreadPrioMax();
      emitErrorAtFirstTimeBinning = true;
      resetUsedPearlPrios(0);
   }


   void PrioMapper::resetUsedPearlPrios(int reserveNewPrio) {
       usedPearlPrios = 0;
       usedRtPrios = 0;
       minUsedPearlPrio=0;
       maxUsedPearlPrio=0;

       for (int i=0; i<255; i++) {
           mappedPearlPrios[i] = 0;
       }

       if (reserveNewPrio > 0) {
           mappedPearlPrios[reserveNewPrio-1] = 1;
           usedPearlPrios++;
       }

       for (int i=0; i<TaskList::Instance().size(); i++) {
           Task * t = TaskList::Instance().getTaskByIndex(i);
           int state = t->getTaskState();
           // printf("%s state %d\n", t->getName(), state);
           if (state != Task::TaskState::TERMINATED) {
               int prio = t->getPrio().x;
               // printf("must map %s prio %d\n", t->getName(), prio);
               if (mappedPearlPrios[prio-1] == 0) {
                   usedPearlPrios ++;
               }
               mappedPearlPrios[prio-1] = 1; // mark prio as in use
               if (maxUsedPearlPrio < prio) {
                   maxUsedPearlPrio = prio;
               }
           }
       }
       applyBinning();
   }

   void PrioMapper::applyBinning(void) {

       int binSize = (usedPearlPrios/(max-1)) + 1;

       if (binSize > 1 && emitErrorAtFirstTimeBinning==true) {
           emitErrorAtFirstTimeBinning = false;
           Log::error("not enough real-time priorities -> some priorities merged");
       }

       //printf("apply binning pearlPrios = %d  rtPrios = %d  max=%d  binsize=%d\n", usedPearlPrios,
       //   usedRtPrios, max, binSize);
       int rtPrio = 1;
       usedRtPrios = 0;
       int countInBin = 0;
       int remainingPearlPriosToMap = usedPearlPrios;
       for (int i=254; i>= 0; i--) {
           if (mappedPearlPrios[i] != 0) {
               mappedPearlPrios[i] = rtPrio;
               // printf("insert in bin: i=%d countInBin=%d remainingPRL=%d binSize=%d rtPrio=%d usedRtPrio=%d\n",
               // i,countInBin,remainingPearlPriosToMap,binSize, rtPrio, usedRtPrios);

               countInBin ++;
               remainingPearlPriosToMap--;
               if (countInBin >= binSize) {
                   // bin is full
                   countInBin = 0;
                   rtPrio ++;
                   usedRtPrios ++;
               }
               // check if remaining pearl prios may mapped 1-by-1
               if (remainingPearlPriosToMap <= max-rtPrio) {
                   binSize = 1;
               }

           }
       }
       // dump();
   }

   void PrioMapper::dump() {
       printf("PrioMapper: max=%d  usedRtPrio=%d\n", max, usedRtPrios);
       printf("PrioMapper:: mapped pearl prios:\n");
       for (int i=0; i<255; i++) {
           if (mappedPearlPrios[i]) printf("  %d -> %d\n",i+1, mappedPearlPrios[i]);
       }
   }

   int PrioMapper::fromPearl(const Fixed<15> p) {
      // max is reserved for the os-thread

      int rtPrio = 1;
      memcpy(previousMapping,  mappedPearlPrios, sizeof(mappedPearlPrios));
      //printf("\n\nrequested pearl prio %d usedRtPrio=%d maxUsedPearlPrio=%d\n", p.x, usedRtPrios, maxUsedPearlPrio);
      //dump();

      if (usedPearlPrios == 0) {
          // list is empty
          mappedPearlPrios[p.x-1] = 1;
          minUsedPearlPrio = p.x;
          maxUsedPearlPrio = minUsedPearlPrio;
          usedPearlPrios++;
          usedRtPrios++;
      } else if (mappedPearlPrios[p.x-1] != 0) {
          // priority already mapped
          rtPrio = mappedPearlPrios[p.x-1];
      } else if (usedRtPrios < max-1) {
         //  printf("lets insert the prio\n");
         // we must insert in the list and shift the rt prios
         if (p.x > maxUsedPearlPrio) {
            // insert as lowest prio
            rtPrio = 1;
            maxUsedPearlPrio = p.x;
            mappedPearlPrios[p.x-1] = rtPrio;
            usedRtPrios++;
            usedPearlPrios++;
         } else {
            // lets find the rtPrio of the next task with better priority
            rtPrio = 0;
            for (int i=p.x-1; i>= 0 && rtPrio == 0; i--) {
               if (mappedPearlPrios[i]) rtPrio = mappedPearlPrios[i];
               //printf("maxUsedPeearlPrio=%d i=%d rtPrio=%d\n", maxUsedPearlPrio,i,rtPrio);
            }
            if (rtPrio==0) {
                // no task with better priority found
                rtPrio = mappedPearlPrios[p.x]+1;
            }
            mappedPearlPrios[p.x-1] = rtPrio;
            usedRtPrios ++;
            usedPearlPrios++;
         }
         // decrement all mappedPearlPrios above 
         for (int i= p.x-2; i>=0 ; i--) {
             if (mappedPearlPrios[i]) {
                 mappedPearlPrios[i]++;
             } 
         } 

      } else {
          // printf("*** clean priority list: reserve prio: %d\n",p.x);
          resetUsedPearlPrios(p.x);
          //dump();
          applyBinning();
      }

      // check if real-time priorities were changed
      TaskList & tl = TaskList::Instance();
      tl.sort();

      // lets start with the low priority tasks

      for (int i=tl.size()-1; i>= 0; i--) {
          Task * t = tl.getTaskByIndex(i);
          int prio = t->getPrio().x;
          if (t->getTaskState() != Task::TERMINATED &&
                  previousMapping[prio-1] != mappedPearlPrios[prio-1]) {
              //printf("adjust rtprio %s to %d\n",t->getName(), mappedPearlPrios[prio-1]);
              t->setThreadPrio((int)(mappedPearlPrios[prio-1]));
          }
      }
      // dump();

      return mappedPearlPrios[p.x-1];

//      int sys = max - p.x ;
//
//      if (p.x == 255) {
//         sys = min;
//      } else if (sys <= min) {
//         Log::error((char*)"the requested priority %d is not available", p.x);
//         throw thePriorityNotMapableSignal;
//      }

//      return sys;
   }

   int PrioMapper::getSystemPrio() {
      return max;
   }

   void PrioMapper::logPriorities() {
      int nbrOfTasks = TaskList::Instance().size();
      Log::info("PEARL priorities: 1,..., 255 --> RR-Prio %d,..., 1",max-1);
      Log::info("%d tasks are defined", nbrOfTasks); 
   }

}  // end of name space
