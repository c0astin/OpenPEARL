// hfujk

#ifndef PLAYGROUND_PERFORMEDDEADLOCKOPERATION_H
#define PLAYGROUND_PERFORMEDDEADLOCKOPERATION_H

#include <string>

#include "DeadlockOperation.h"

using namespace std;

namespace pearlrt {

   class PerformedDeadlockOperation {
   public:
      bool initialized = false;
      DeadlockOperation deadlockOperation;
      string taskIdentifier;
      bool resourceAssignedToTask = false;
      unsigned long long callTimestamp = 0;
   };

}

#endif //PLAYGROUND_PERFORMEDDEADLOCKOPERATION_H
