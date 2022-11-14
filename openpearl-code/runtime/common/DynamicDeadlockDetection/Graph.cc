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


#include "../DynamicDeadlockDetection/Graph.h"

namespace pearlrt {

   void Graph::performDFS(int currentNode, int visitedNodes[])
   {
      cycleDetectionPathStack.push(currentNode);

      if (visitedNodes[currentNode] == NODE_STATE_IS_BEING_VISITED)
      {
         vector<int> cycleDetectionPathVector;

         while (!cycleDetectionPathStack.empty())
         {
            cycleDetectionPathVector.push_back(cycleDetectionPathStack.top());
            cycleDetectionPathStack.pop();
         }

         reverse(cycleDetectionPathVector.begin(), cycleDetectionPathVector.end());

         int lastCycleNode = cycleDetectionPathVector[cycleDetectionPathVector.size() - 1];
         bool cycleStarted = false;

         for (int currentCycleNode : cycleDetectionPathVector)
         {
            if (!cycleStarted)
            {
               if (currentCycleNode == lastCycleNode)
               {
                  cycleStarted = true;
               }
            }

            if (cycleStarted)
            {
               cycleDetectionPath.push_back(getNodeIdentifier(currentCycleNode));
            }
         }

         cycleDetected = true;

         return;
      }

      visitedNodes[currentNode] = NODE_STATE_IS_BEING_VISITED;

      for (int reachableNode : graph[currentNode])
      {
         if (visitedNodes[reachableNode] != NODE_STATE_FINISHED_VISITING)
         {
            performDFS(reachableNode, visitedNodes);
         }
      }

      visitedNodes[currentNode] = NODE_STATE_FINISHED_VISITING;

      if (!cycleDetectionPathStack.empty())
      {
         cycleDetectionPathStack.pop();
      }
   }

   int Graph::addNode(const string& nodeIdentifier)
   {
      int nodeId = getNodeId(nodeIdentifier);

      if (nodeId >= 0)
      {
         return nodeId;
      }

      nodeIdentifiers.push_back(nodeIdentifier);

      nodeId = nodeCount;

      nodeCount++;

      return nodeId;
   }

   int Graph::getNodeId(const string& nodeIdentifier)
   {
      int index = 0;

      for (const string& currentIdentifier : nodeIdentifiers)
      {
         if (nodeIdentifier == currentIdentifier)
         {
            return index;
         }

         index++;
      }

      return -1;
   }

   string Graph::getNodeIdentifier(int nodeId)
   {
      if ((nodeId < 0) || (nodeId >= nodeCount))
      {
         return "";
      }

      return nodeIdentifiers[nodeId];
   }

   void Graph::addEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier)
   {
      int sourceId = addNode(sourceNodeIdentifier);
      int targetId = addNode(targetNodeIdentifier);

      if (find(graph[sourceId].begin(), graph[sourceId].end(), targetId) == graph[sourceId].end())
      {
         graph[sourceId].push_back(targetId);
      }
   }

   void Graph::removeEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier)
   {
      int sourceId = getNodeId(sourceNodeIdentifier);
      int targetId = getNodeId(targetNodeIdentifier);

      if ((sourceId < 0) || (targetId < 0))
      {
         return;
      }

      graph[sourceId].erase(remove(graph[sourceId].begin(), graph[sourceId].end(), targetId), graph[sourceId].end());
   }

   vector<string> Graph::getCycle()
   {
      while (!cycleDetectionPathStack.empty())
      {
         cycleDetectionPathStack.pop();
      }

      cycleDetectionPath.clear();

      cycleDetected = false;

      int visited[graphNodeCount] = {};

      for (int i = 0; i < nodeCount; i++)
      {
         if (visited[i] == NODE_STATE_NOT_VISITED)
         {
            performDFS(i, visited);
         }

         if (cycleDetected)
         {
            break;
         }
      }

      return cycleDetectionPath;
   }

   void Graph::printGraph()
   {
      for (int nodeId = 0; nodeId < nodeCount; nodeId++)
      {
         cout << "Node " << getNodeIdentifier(nodeId) << ": ";

         for (auto nodeEdge : graph[nodeId])
         {
            cout << getNodeIdentifier(nodeEdge) << ", ";
         }

         cout << endl;
      }
   }

   string Graph::getFirstEdge(const string& nodeIdentifier)
   {
      int nodeId = getNodeId(nodeIdentifier);

      if (nodeId < 0)
      {
         return "";
      }

      for (auto nodeEdge : graph[nodeId])
      {
         return getNodeIdentifier(nodeEdge);
      }

      return "";
   }

   vector<string> Graph::getOutgoingEdges(const string& nodeIdentifier)
   {
      vector<string> outgoingNodes;

      int nodeId = getNodeId(nodeIdentifier);

      if (nodeId < 0)
      {
         return outgoingNodes;
      }

      for (auto nodeEdge : graph[nodeId])
      {
         outgoingNodes.push_back(getNodeIdentifier(nodeEdge));
      }

      return outgoingNodes;
   }

   vector<string> Graph::getIncomingEdges(const string& nodeIdentifier)
   {
      vector<string> incomingNodes;

      int targetNodeId = getNodeId(nodeIdentifier);

      if (targetNodeId < 0)
      {
         return incomingNodes;
      }

      for (int currentNodeId = 0; currentNodeId < nodeCount; currentNodeId++)
      {
         for (auto nodeEdge : graph[currentNodeId])
         {
            if (nodeEdge == targetNodeId)
            {
               incomingNodes.push_back(getNodeIdentifier(currentNodeId));

               goto outerLoop;
            }
         }

         outerLoop:;
      }

      return incomingNodes;
   }
}
