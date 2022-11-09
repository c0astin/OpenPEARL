// hfujk

#include "HfujkControl.h"

namespace pearlrt {

   int HfujkControl::resourceAllocationGraphNodeCount = 0;
   HfujkControl* HfujkControl::instance = nullptr;
   std::mutex HfujkControl::instanceMutex;

   bool HfujkControl::configure(int setResourceAllocationGraphNodeCount)
   {
      // public, Not critical
      HfujkControl::resourceAllocationGraphNodeCount = setResourceAllocationGraphNodeCount;
      return true;
   }

   void HfujkControl::registerDeadlockOperation(const DeadlockOperation& deadlockOperation)
   {
      // public
      instanceMutex.lock();
      getInstance()->resourceAllocationGraph->registerDeadlockOperation(deadlockOperation);
      instanceMutex.unlock();
   }

   void HfujkControl::printDeadlockOperations()
   {
      // public
      instanceMutex.lock();
      //DeadlockOperationState::getInstance().printDeadlockOperations();
      instanceMutex.unlock();
   }

   bool HfujkControl::getDeadlockSituation()
   {
      // public
      instanceMutex.lock();
      bool result = getInstance()->resourceAllocationGraph->isDeadlocked();
      instanceMutex.unlock();
      return result;
   }

   unsigned long long HfujkControl::getCurrentTimestamp()
   {
      // public, Not critical
      using namespace std::chrono;
      return duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
   }

   string HfujkControl::formatTimestamp(unsigned long long timestamp, bool inMilliseconds)
   {
      // public, Not critical
      unsigned long long timestampSeconds = timestamp;
      unsigned long long milliseconds = 0;

      if (inMilliseconds)
      {
         milliseconds = timestamp % 1000;
         timestampSeconds = timestamp / 1000;
      }

      auto timestampTime = (const time_t) timestampSeconds;
      struct tm* timeInfo;
      timeInfo = localtime (&timestampTime);
      char timeStringBuffer[256];

      //strftime(timeStringBuffer, sizeof(timeStringBuffer), "%d.%m.%Y %H:%M:%S", timeInfo);
      strftime(timeStringBuffer, sizeof(timeStringBuffer), "%Y-%m-%d %H:%M:%S", timeInfo);

      string result = timeStringBuffer;

      if (inMilliseconds)
      {
         string millisecondsAsString = to_string(milliseconds);

         while (millisecondsAsString.length() < 3) {
            millisecondsAsString = "0" + millisecondsAsString;
         }

         result.append(".").append(millisecondsAsString);
      }

      return result;
   }

   string HfujkControl::formatIdentifier(const string& identifier)
   {
      // public, Not critical
      if (identifier.at(0) == '_')
      {
         return identifier.substr(1);
      }

      return identifier;
   }

   void HfujkControl::performDeadlockOperation(DeadlockOperation& deadlockOperation)
   {
      // public
      instanceMutex.lock();

      bool operationIsPossible = true;
      bool isRequestOperation = false;

      map<string, int> currentResourceValues;

      for (std::size_t i = 0; i < deadlockOperation.resourceIdentifiers.size(); ++i)
      {
         currentResourceValues.insert(pair<string, int>(deadlockOperation.resourceIdentifiers[i], deadlockOperation.resourceValues[i]));
      }

      if (strcmp(deadlockOperation.actionType, DeadlockOperation::ACTION_TYPE_REQUEST) == 0)
      {
         isRequestOperation = true;

         for (auto & resourceIdentifier : deadlockOperation.resourceIdentifiers)
         {
            auto currentResourceValueEntry = currentResourceValues.find(resourceIdentifier);

            if (currentResourceValueEntry != currentResourceValues.end())
            {
               if (currentResourceValueEntry->second > 0)
               {
                  currentResourceValues[resourceIdentifier] = currentResourceValueEntry->second - 1;
               }
               else
               {
                  operationIsPossible = false;
                  break;
               }
            }
         }
      }
      else if (strcmp(deadlockOperation.actionType, DeadlockOperation::ACTION_TYPE_RESERVE) == 0)
      {
         isRequestOperation = true;
         operationIsPossible = deadlockOperation.reserveBoltOperationsPossible;
         int resourceValue = (operationIsPossible) ? 1 : 0;

         for (std::size_t i = 0; i < deadlockOperation.resourceValues.size(); ++i)
         {
            deadlockOperation.resourceValues[i] = resourceValue;
         }
      }
      else if ((strcmp(deadlockOperation.actionType, DeadlockOperation::ACTION_TYPE_ENTER) == 0) || (strcmp(deadlockOperation.actionType, DeadlockOperation::ACTION_TYPE_LEAVE) == 0))
      {
         instanceMutex.unlock();
         return;
      }

      for (std::size_t i = 0; i < deadlockOperation.resourceIdentifiers.size(); ++i)
      {
         currentResourceValues[deadlockOperation.resourceIdentifiers[i]] = deadlockOperation.resourceValues[i];
      }

      getInstance()->resourceAllocationGraph->insertDeadlockOperationInTrace(deadlockOperation, deadlockOperation.executingTaskIdentifier, operationIsPossible);

      for (std::size_t i = 0; i < deadlockOperation.resourceIdentifiers.size(); ++i)
      {
         auto currentResourceValueEntry = currentResourceValues.find(deadlockOperation.resourceIdentifiers[i]);
         int newResourceValue = 0;

         if (currentResourceValueEntry != currentResourceValues.end())
         {
            newResourceValue = currentResourceValueEntry->second;
         }

         if (isRequestOperation)
         {
            getInstance()->resourceAllocationGraph->requestCountableResource(
                  deadlockOperation.executingTaskIdentifier,
                  deadlockOperation.resourceIdentifiers[i],
                  deadlockOperation,
                  newResourceValue,
                  operationIsPossible
            );

            newResourceValue--;
         }
         else
         {
            getInstance()->resourceAllocationGraph->releaseCountableResource(
                  deadlockOperation.executingTaskIdentifier,
                  deadlockOperation.resourceIdentifiers[i],
                  deadlockOperation,
                  newResourceValue,
                  operationIsPossible
            );

            newResourceValue++;
         }

         currentResourceValues[deadlockOperation.resourceIdentifiers[i]] = newResourceValue;
      }

      instanceMutex.unlock();
   }
}
