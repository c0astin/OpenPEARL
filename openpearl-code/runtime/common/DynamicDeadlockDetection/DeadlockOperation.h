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


#ifndef DEADLOCKOPERATION_H
#define DEADLOCKOPERATION_H

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
