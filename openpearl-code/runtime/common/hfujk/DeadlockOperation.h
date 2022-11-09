// hfujk

#ifndef RUNTIME_DEADLOCKOPERATION_H
#define RUNTIME_DEADLOCKOPERATION_H

#include <string>
#include <vector>

//using namespace std;

namespace pearlrt {

   class DeadlockOperation {
   public:
      const char* contextType = "";
      const char* contextIdentifier = "";
      const char* actionType = "";
      const char* codeFilename = "";
      int codeLineNumber = 0;
      const char* resourcesType = "";

      std::vector<const char*> resourceIdentifiers;
      std::vector<int> resourceValues;
      std::vector<const char*> executingTaskIdentifiers;
      bool reserveBoltOperationsPossible = false;

      const char* executingTaskIdentifier = "";

      static constexpr const char* RESOURCE_TYPE_SEMAPHORE = "SEMA";
      static constexpr const char* ACTION_TYPE_REQUEST = "REQUEST";
      static constexpr const char* ACTION_TYPE_RELEASE = "RELEASE";

      static constexpr const char* RESOURCE_TYPE_BOLT = "BOLT";
      static constexpr const char* ACTION_TYPE_ENTER = "ENTER";
      static constexpr const char* ACTION_TYPE_LEAVE = "LEAVE";
      static constexpr const char* ACTION_TYPE_RESERVE = "RESERVE";
      static constexpr const char* ACTION_TYPE_FREE = "FREE";

      static constexpr const char* CONTEXT_TYPE_TASK = "TASK";
      static constexpr const char* CONTEXT_TYPE_PROCEDURE = "PROCEDURE";

      static DeadlockOperation duy_registerSeaRequest(const char* contextTaskIdentifier, const char* resourceIdentifier, const int codeLineNumber = 0)
      {
         DeadlockOperation deadlockOperation;
         deadlockOperation.contextType = DeadlockOperation::CONTEXT_TYPE_TASK;
         deadlockOperation.contextIdentifier = contextTaskIdentifier;
         deadlockOperation.codeLineNumber = codeLineNumber;
         deadlockOperation.resourcesType = DeadlockOperation::RESOURCE_TYPE_SEMAPHORE;
         deadlockOperation.actionType = DeadlockOperation::ACTION_TYPE_REQUEST;
         deadlockOperation.addResourceIdentifier(resourceIdentifier);
         deadlockOperation.addExecutingTaskIdentifier(contextTaskIdentifier);
         return deadlockOperation;
      }

      static DeadlockOperation duy_registerSeaRelease(const char* contextTaskIdentifier, const char* resourceIdentifier, const int codeLineNumber = 0)
      {
         DeadlockOperation deadlockOperation;
         deadlockOperation.contextType = DeadlockOperation::CONTEXT_TYPE_TASK;
         deadlockOperation.contextIdentifier = contextTaskIdentifier;
         deadlockOperation.codeLineNumber = codeLineNumber;
         deadlockOperation.resourcesType = DeadlockOperation::RESOURCE_TYPE_SEMAPHORE;
         deadlockOperation.actionType = DeadlockOperation::ACTION_TYPE_RELEASE;
         deadlockOperation.addResourceIdentifier(resourceIdentifier);
         deadlockOperation.addExecutingTaskIdentifier(contextTaskIdentifier);
         return deadlockOperation;
      }

      void addResourceIdentifier(const char* resourceIdentifier);
      void addResourceIdentifierWithValue(const char* resourceIdentifier, int resourceValue);
      void addExecutingTaskIdentifier(const char* executingTaskIdentifier);
   };

   class DissolveDeadlockOperationCycleEntry
   {
   public:
      DeadlockOperation deadlockOperation;
      std::string executingTaskIdentifier;
      std::string releasedResourceIdentifier;
   };
}

#endif //RUNTIME_DEADLOCKOPERATION_H
