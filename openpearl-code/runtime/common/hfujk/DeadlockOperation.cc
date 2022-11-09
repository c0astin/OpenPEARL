// hfujk

#include "DeadlockOperation.h"

namespace pearlrt {

   void DeadlockOperation::addResourceIdentifier(const char* resourceIdentifier)
   {
      if (resourceIdentifier[0] == '_')
      {
         resourceIdentifier++;
      }

      resourceIdentifiers.push_back(resourceIdentifier);
   }

   void DeadlockOperation::addResourceIdentifierWithValue(const char* resourceIdentifier, int resourceValue)
   {
      if (resourceIdentifier[0] == '_')
      {
         resourceIdentifier++;
      }

      resourceIdentifiers.push_back(resourceIdentifier);
      resourceValues.push_back(resourceValue);
   }

   void DeadlockOperation::addExecutingTaskIdentifier(const char* executingTaskIdentifier)
   {
      executingTaskIdentifiers.push_back(executingTaskIdentifier);
   }
}
