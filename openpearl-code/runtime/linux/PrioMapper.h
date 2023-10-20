/*
 [A "BSD license"]
 Copyright (c) 2012-2023 Rainer Mueller
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

#ifndef PRIOMAPPER_INCLUDED
#define PRIOMAPPER_INCLUDED

#include "../../configuration/include/autoconf.h"

/**
\file

\brief priority mapper

this module provides the mapping of PEARL priorities to real-time priorities
with the round-robin scheduler



*/


namespace pearlrt {
   /**
   \addtogroup tasking_linux
   @{
   */

   /**
   \brief priority mapper

    The range of PEARL priorities is from 1 to 255, where 1 is best and
    255 is lowest priority.

    We have usually 49 real-time priorities available for OpenPEARL, since priority 50 is used
    by device driver kernel thread, which should not become delayed by application tasks.

    The best priority (49) is reserved for the TimerThread and some operations in application threads,
    which should not delayed by other application threads.
    As long as the number of OpenPEARL tasks is less than 49 a 1-by-1-mapping is possible.

    If there are more than 48 thread priorities are requested some pearl priorities
    are mapped to one real-time priority.

    Example with 4 real-time priorities (1-4) and 5 tasks with different pearl priority.

    <table>
    <tr><td>Task</td><td>PEARL prio</td><td>RT prio</td></tr>
    <tr><td>t1</td><td>10</td><td>4</td></tr>
    <tr><td>t2</td><td>20</td><td>3</td></tr>
    <tr><td>t3</td><td>30</td><td>2</td></tr>
    <tr><td>t4</td><td>40</td><td>1</td></tr>
    <tr><td>t5</td><td>50</td><td>1</td></tr>
    </table>

    Tasks t4 and t5 share the same real-time priority. Only tasks which are not terminated
    are regarded.
    The sharing mechanism starts at the low priority end and
    grows towards the high priority end.
    If more than 10 priorities would be required, the binning becomes extended.
    For p required pearl priorities and r available real-time priorities (p>r),
    the number of pearl priorities sharing one real-time priority is p/r+1

    Only the PEARL statements ACTIVATE and CONTINUE may affect the priority.

    <ol>
    <li>A list of requested pearl priority becomes updated at each ACTIVATE/CONTINUE.</li>
    <li>If the number of requested pearl priorities exceeds the available real-time priorities,
    the list becomes cleaned if some tasks terminated meanwhile.
    This is solved by an iteration through the TaskList</li>
    <li>If the cleanup leads to a "p<=r" situation the mapping 1-by-1 is possible</li>
    <li>If the cleanup leads to a "p>r" situation the binning procedure is applied</li>
    <li>If there were changes of a previously assigned real-time priority the
       TaskList will be iterated and all tasks with the related pearl priority are
       assigned the new real-time priority.
       Since this operation runs with the best real-time priority, there is no need
       to regard sequence of the iteration.
    </li>
    </ol>

    The mechanism is realized as a singleton.
   */
   class PrioMapper {
   private:
      PrioMapper();

      /**
       max real-time priority for OpenPEARL.
       This is used for operating system like operations in the OpenPEARL runtime system, e.g.
       the time thread.
       */
      int max;
      static PrioMapper* instance; 	///< the one and only instance

      /**
       index is the pearl priority decremented by 1

       value =0: not mapped
       value >0: already mapped to this real-time priority
       */
      uint8_t mappedPearlPrios[255];
      uint8_t previousMapping[255];

      int usedPearlPrios;
      int usedRtPrios;
      int minUsedPearlPrio;
      int maxUsedPearlPrio;
      //int maxUsedRtPrio;
      bool emitErrorAtFirstTimeBinning;

      void resetUsedPearlPrios(int reserveRequestedPrio); ///< reset used prios to requested by active tasks
      void applyBinning();
      void dump();

   public:
      /**
         get access to the mapper (Singleton pattern)
         \returns pointer to the (one and only) Prio-Object
      */
      static PrioMapper* getInstance();

      /**
       transform PEARL priority to system priority

       If binning occurs, a notice to Log::error is emitted once.

       \param p the PEARL priority running from 1 (best) to 255 (least)

       \returns the mapped system priority
      */

      int fromPearl(const Fixed<15> p);

      /**
       return the best priority - just better than all PEARL tasks
       \returns the system priority
      */
      int getSystemPrio();

      /**
       show the remaining PEARL priorities on the log as INFO
      */
      void logPriorities();
   };

   /** @} */
}

#endif
