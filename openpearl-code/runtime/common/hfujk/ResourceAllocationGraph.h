// hfujk

#ifndef RUNTIME_RESOURCEALLOCATIONGRAPH_H
#define RUNTIME_RESOURCEALLOCATIONGRAPH_H

#include <iostream>
#include <set>
#include <map>
#include <vector>
#include <algorithm>
#include <sstream>
#include <cstring>
#include <stack>

#include "Graph.h"
#include "DeadlockOperation.h"
#include "PerformedDeadlockOperation.h"

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
