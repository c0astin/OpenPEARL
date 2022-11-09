// hfujk

#ifndef RUNTIME_GRAPH_H
#define RUNTIME_GRAPH_H

#include <iostream>
#include <set>
#include <map>
#include <vector>
#include <algorithm>
#include <cstring>
#include <stack>

using namespace std;

namespace pearlrt {

   class Graph {
   private:
      static const int NODE_STATE_NOT_VISITED = 0;
      static const int NODE_STATE_FINISHED_VISITING = 1;
      static const int NODE_STATE_IS_BEING_VISITED = -1;

      vector<int>* graph;
      bool cycleDetected = false;
      stack<int> cycleDetectionPathStack;
      vector<string> cycleDetectionPath;
      int nodeCount = 0;
      vector<string> nodeIdentifiers;

      void performDFS(int currentNode, int visitedNodes[]);

      int graphNodeCount = 0;

   public:
      int getNodeId(const string& nodeIdentifier);
      string getNodeIdentifier(int nodeId);
      void addEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier);
      void removeEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier);
      vector<string> getCycle();
      void printGraph();
      string getFirstEdge(const string& nodeIdentifier);
      vector<string> getOutgoingEdges(const string& nodeIdentifier);
      vector<string> getIncomingEdges(const string& nodeIdentifier);

      int addNode(const string& nodeIdentifier);

      Graph(const int graphNodeCount)
      {
         graph = new vector<int>[graphNodeCount];
         this->graphNodeCount = graphNodeCount;
      }
   };
}

#endif //RUNTIME_GRAPH_H
