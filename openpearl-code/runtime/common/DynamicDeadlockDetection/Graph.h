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
