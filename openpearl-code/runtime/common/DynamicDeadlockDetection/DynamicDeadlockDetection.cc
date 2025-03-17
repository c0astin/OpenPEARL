/*
 * [A "BSD license"]
 *  Copyright (c) 2022 Jan Knoblauch
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


#include "DynamicDeadlockDetection.h"

namespace pearlrt {

   int DynamicDeadlockDetection::resourceAllocationGraphNodeCount = 0;
   DynamicDeadlockDetection* DynamicDeadlockDetection::instance = nullptr;
   std::mutex DynamicDeadlockDetection::instanceMutex;

   bool DynamicDeadlockDetection::configure(int setResourceAllocationGraphNodeCount)
   {
      // public, Not critical
      DynamicDeadlockDetection::resourceAllocationGraphNodeCount += setResourceAllocationGraphNodeCount;
      return true;
   }

   void DynamicDeadlockDetection::registerDeadlockOperation(const DeadlockOperation& deadlockOperation)
   {
      // public
      instanceMutex.lock();
      getInstance()->resourceAllocationGraph->registerDeadlockOperation(deadlockOperation);
      instanceMutex.unlock();
   }

   void DynamicDeadlockDetection::printDeadlockOperations()
   {
      // public
      instanceMutex.lock();
      //DeadlockOperationState::getInstance().printDeadlockOperations();
      instanceMutex.unlock();
   }

   bool DynamicDeadlockDetection::getDeadlockSituation()
   {
      // public
      instanceMutex.lock();
      bool result = getInstance()->resourceAllocationGraph->isDeadlocked();
      instanceMutex.unlock();
      return result;
   }

   unsigned long long DynamicDeadlockDetection::getCurrentTimestamp()
   {
      // public, Not critical
      using namespace std::chrono;
      return duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
   }

   string DynamicDeadlockDetection::formatTimestamp(unsigned long long timestamp, bool inMilliseconds)
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

   string DynamicDeadlockDetection::formatIdentifier(const string& identifier)
   {
      // public, Not critical
      if (identifier.at(0) == '_')
      {
         return identifier.substr(1);
      }

      return identifier;
   }

   void DynamicDeadlockDetection::performDeadlockOperation(DeadlockOperation& deadlockOperation)
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
