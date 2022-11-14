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


#ifndef RESOURCEALLOCATIONGRAPH_H
#define RESOURCEALLOCATIONGRAPH_H

#include <iostream>
#include <set>
#include <map>
#include <vector>
#include <algorithm>
#include <sstream>
#include <cstring>
#include <stack>

#include "DynamicDeadlockDetection/Graph.h"
#include "DynamicDeadlockDetection/DeadlockOperation.h"
#include "DynamicDeadlockDetection/PerformedDeadlockOperation.h"

namespace pearlrt {

   class ResourceAllocationGraph {
   private:
      Graph* graph = nullptr;
      map<string, PerformedDeadlockOperation> edgeMetadata;
      vector<string> taskIdentifiers;

      vector<PerformedDeadlockOperation> deadlockOperationTrace;

      vector<DeadlockOperation> registeredDeadlockOperations;

      void addGraphEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier, DeadlockOperation& deadlockOperation, const string& taskIdentifier, bool resourceAssignedToTask);
      void removeGraphEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier);

      static string getTaskAssignedToResource(const char* resourceIdentifier, const char* resourceType, vector<PerformedDeadlockOperation>& deadlockOperations);

      PerformedDeadlockOperation getPerformedDeadlockOperation(const string& taskIdentifier, const string& resourceIdentifier, bool isAllocated);

      void requestResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible);
      void releaseResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible);

      static void printResourceAllocationGraphOperation(const string& string);

   public:

      void insertDeadlockOperationInTrace(DeadlockOperation& deadlockOperation, const string& taskIdentifier, bool resourceAssignedToTask);

      bool isDeadlocked();
      void print();

      void requestCountableResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible);
      void releaseCountableResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible);

      void registerDeadlockOperation(const DeadlockOperation& deadlockOperation);

      ResourceAllocationGraph(const int resourceAllocationGraphNodeCount)
      {
         graph = new Graph(resourceAllocationGraphNodeCount);
      }
   };
}

#endif //RUNTIME_RESOURCEALLOCATIONGRAPH_H
