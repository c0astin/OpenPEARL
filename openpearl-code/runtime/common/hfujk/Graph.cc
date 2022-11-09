// hfujk

#include "Graph.h"

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
