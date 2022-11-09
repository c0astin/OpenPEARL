// hfujk

#include "ResourceAllocationGraph.h"
#include "HfujkControl.h"

namespace pearlrt {

   void ResourceAllocationGraph::addGraphEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier, DeadlockOperation& deadlockOperation, const string& taskIdentifier, bool resourceAssignedToTask)
   {
      string edgeId = sourceNodeIdentifier + "#" + targetNodeIdentifier;

      PerformedDeadlockOperation performedDeadlockOperation;
      performedDeadlockOperation.initialized = true;
      performedDeadlockOperation.taskIdentifier = taskIdentifier;
      performedDeadlockOperation.callTimestamp = 0;
      performedDeadlockOperation.resourceAssignedToTask = resourceAssignedToTask;

      // TODO Kopien vermeiden
      performedDeadlockOperation.deadlockOperation = deadlockOperation;
      performedDeadlockOperation.deadlockOperation.resourceIdentifiers.clear();
      performedDeadlockOperation.deadlockOperation.executingTaskIdentifiers.clear();

      for (const auto resourceIdentifier : deadlockOperation.resourceIdentifiers)
      {
         performedDeadlockOperation.deadlockOperation.addResourceIdentifier(resourceIdentifier);
      }

      for (const auto executingTaskIdentifier : deadlockOperation.executingTaskIdentifiers)
      {
         performedDeadlockOperation.deadlockOperation.addExecutingTaskIdentifier(executingTaskIdentifier);
      }

      edgeMetadata[edgeId] = performedDeadlockOperation;

      if (std::find(taskIdentifiers.begin(), taskIdentifiers.end(), taskIdentifier) == taskIdentifiers.end())
      {
         taskIdentifiers.push_back(taskIdentifier);
      }

      graph->addEdge(sourceNodeIdentifier, targetNodeIdentifier);
   }

   void ResourceAllocationGraph::removeGraphEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier)
   {
      string edgeId = sourceNodeIdentifier + "#" + targetNodeIdentifier;

      edgeMetadata.erase(edgeId);

      graph->removeEdge(sourceNodeIdentifier, targetNodeIdentifier);
   }

   void ResourceAllocationGraph::requestResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible)
   {
      string resourceAllocator = graph->getFirstEdge(resourceIdentifier);

      bool resourceAssignedToTask;

      if ((currentResourceValue > 0) && (operationIsPossible))
      {
         printResourceAllocationGraphOperation("Resource is available:      (" + taskIdentifier + ")<---[" + resourceIdentifier + "]");
         resourceAssignedToTask = true;
         removeGraphEdge(taskIdentifier, resourceIdentifier);

         if (resourceAllocator.empty())
         {
            addGraphEdge(resourceIdentifier, taskIdentifier, deadlockOperation, taskIdentifier, true);
         }
      }
      else
      {
         printResourceAllocationGraphOperation("Resource is allocated:      (" + taskIdentifier + ")--->[" + resourceIdentifier + "]");
         resourceAssignedToTask = false;
         addGraphEdge(taskIdentifier, resourceIdentifier, deadlockOperation, taskIdentifier, false);
      }
   }

   void ResourceAllocationGraph::releaseResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible)
   {
      printResourceAllocationGraphOperation("Resource is released:       (" + taskIdentifier + ") // [" + resourceIdentifier + "]");

      //removeGraphEdge(taskIdentifier, resourceIdentifier);
      removeGraphEdge(resourceIdentifier, taskIdentifier);
   }

   void ResourceAllocationGraph::insertDeadlockOperationInTrace(DeadlockOperation& deadlockOperation, const string& taskIdentifier, bool resourceAssignedToTask)
   {
      PerformedDeadlockOperation performedDeadlockOperation;
      performedDeadlockOperation.initialized = true;
      performedDeadlockOperation.taskIdentifier = taskIdentifier;
      performedDeadlockOperation.callTimestamp = HfujkControl::getCurrentTimestamp();
      performedDeadlockOperation.resourceAssignedToTask = resourceAssignedToTask;

      // TODO Kopien vermeiden
      performedDeadlockOperation.deadlockOperation = deadlockOperation;
      performedDeadlockOperation.deadlockOperation.resourceIdentifiers.clear();
      performedDeadlockOperation.deadlockOperation.executingTaskIdentifiers.clear();

      for (const auto resourceIdentifier : deadlockOperation.resourceIdentifiers)
      {
         performedDeadlockOperation.deadlockOperation.addResourceIdentifier(resourceIdentifier);
      }

      for (const auto executingTaskIdentifier : deadlockOperation.executingTaskIdentifiers)
      {
         performedDeadlockOperation.deadlockOperation.addExecutingTaskIdentifier(executingTaskIdentifier);
      }

      deadlockOperationTrace.push_back(performedDeadlockOperation);
   }

   bool ResourceAllocationGraph::isDeadlocked()
   {
      vector<string> cycle = graph->getCycle();

      vector<PerformedDeadlockOperation> deadlockOperations;

      if (!cycle.empty())
      {
         /*
         cout << endl << "Deadlock cycle: ";
         for (const string& cyclePathNode : cycle)
         {
            cout << cyclePathNode << " -> ";
         }
         cout << endl;
         */

         // TODO Berichte wiederholen sich, wenn mehrere Ressourcen (oder die gleiche Ressource mehrfach) belegt werden soll

         string reportLine = "!----------------------------------";
         string reportLeft = "!  ";

         stringstream deadlockReportDependencyCycle;
         stringstream deadlockReportTasksInCycle;
         stringstream deadlockReportOtherTasksWithOperations;
         stringstream deadlockReportOperationsTrace;

         string currentStartEdge;

         for (const string& cyclePathNode : cycle)
         {
            if (currentStartEdge.empty())
            {
               currentStartEdge = cyclePathNode;

               continue;
            }

            string edgeMetadataKey = currentStartEdge.append("#").append(cyclePathNode);

            if (edgeMetadata.count(edgeMetadataKey) > 0)
            {
               deadlockOperations.push_back(edgeMetadata[edgeMetadataKey]);
            }

            currentStartEdge = cyclePathNode;
         }

         vector<string> cycleTaskIdentifiers;
         vector<string> cycleResourceIdentifiers;

         deadlockReportDependencyCycle << reportLeft << "Dependency cycle:" << endl;

         for (auto& currentOperation : deadlockOperations)
         {
            if (!currentOperation.initialized)
            {
               continue;
            }

            for (const auto resourceIdentifier : currentOperation.deadlockOperation.resourceIdentifiers)
            {
               string taskResourceDescription;

               if (currentOperation.resourceAssignedToTask)
               {
                  continue;
               }

               deadlockReportDependencyCycle << reportLeft << "- Task "
                    << currentOperation.taskIdentifier << " waits for " << currentOperation.deadlockOperation.resourcesType
                    << " " << resourceIdentifier << " ("
                    << currentOperation.deadlockOperation.codeFilename << ":" << currentOperation.deadlockOperation.codeLineNumber << ")" << endl;

               deadlockReportDependencyCycle << reportLeft << "  which is assigned to: " << getTaskAssignedToResource(resourceIdentifier, currentOperation.deadlockOperation.resourcesType, deadlockOperations) << endl;

               if (std::find(cycleTaskIdentifiers.begin(), cycleTaskIdentifiers.end(), currentOperation.taskIdentifier) == cycleTaskIdentifiers.end())
               {
                  cycleTaskIdentifiers.push_back(currentOperation.taskIdentifier);
               }

               if (std::find(cycleResourceIdentifiers.begin(), cycleResourceIdentifiers.end(), resourceIdentifier) == cycleResourceIdentifiers.end())
               {
                  cycleResourceIdentifiers.push_back(resourceIdentifier);
               }
            }
         }

         deadlockReportDependencyCycle << reportLeft << endl;

         // Kann der Zyklus noch aufgeloest werden?
         // Diese Tasks sind am Zyklus beteiligt: cycleTaskIdentifiers
         // Diese Ressourcen sind am Zyklus beteiligt: cycleResourceIdentifiers

         vector<DissolveDeadlockOperationCycleEntry> possibleSolvingDeadlockOperations;

         for (auto& currentRegisteredDeadlockOperation : registeredDeadlockOperations)
         {
            if ((strcmp(currentRegisteredDeadlockOperation.actionType, DeadlockOperation::ACTION_TYPE_RELEASE) == 0) || (strcmp(currentRegisteredDeadlockOperation.actionType, DeadlockOperation::ACTION_TYPE_FREE) == 0))
            {
               // Diese Operation gibt Ressourcen frei

               for (auto& currentExecutingTaskIdentifier : currentRegisteredDeadlockOperation.executingTaskIdentifiers)
               {
                  std::string currentExecutingTaskIdentifierStr(currentExecutingTaskIdentifier);

                  if (std::find(cycleTaskIdentifiers.begin(), cycleTaskIdentifiers.end(), currentExecutingTaskIdentifierStr) == cycleTaskIdentifiers.end())
                  {
                     // Diese Operation wird nicht von einem blockierten Task ausgefuehrt

                     for (string& currentResourceOfCycle : cycleResourceIdentifiers)
                     {
                        bool containsResourceOfCycle = false;

                        for (auto& currentResourceOfOperation : currentRegisteredDeadlockOperation.resourceIdentifiers)
                        {
                           std::string currentResourceOfOperationStr(currentResourceOfOperation);

                           if (currentResourceOfCycle == currentResourceOfOperationStr)
                           {
                              containsResourceOfCycle = true;
                              break;
                           }
                        }

                        if (containsResourceOfCycle)
                        {
                           // Diese Operation gibt eine beteiligte Ressource frei
                           // Der Deadlock ist nicht endgueltig
                           DissolveDeadlockOperationCycleEntry dissolveDeadlockOperationEntry;
                           dissolveDeadlockOperationEntry.deadlockOperation = currentRegisteredDeadlockOperation;
                           dissolveDeadlockOperationEntry.executingTaskIdentifier = currentExecutingTaskIdentifier;
                           dissolveDeadlockOperationEntry.releasedResourceIdentifier = currentResourceOfCycle;
                           possibleSolvingDeadlockOperations.push_back(dissolveDeadlockOperationEntry);
                        }
                     }
                  }
               }
            }
         }

         deadlockReportTasksInCycle << reportLeft << "Tasks which caused deadlock:" << endl;

         for (const auto& currentTaskIdentifier : cycleTaskIdentifiers)
         {
            deadlockReportTasksInCycle << reportLeft << " - " << currentTaskIdentifier << ":" << endl;

            for (auto& currentOperation : deadlockOperations)
            {
               if (currentOperation.taskIdentifier != currentTaskIdentifier)
               {
                  continue;
               }

               for (const auto resourceIdentifier : currentOperation.deadlockOperation.resourceIdentifiers)
               {
                  if (!currentOperation.resourceAssignedToTask)
                  {
                     continue;
                  }

                  deadlockReportTasksInCycle << reportLeft << "    - " << " Holding "
                       << currentOperation.deadlockOperation.resourcesType << " " << resourceIdentifier << " ("
                       << currentOperation.deadlockOperation.codeFilename << ":" << currentOperation.deadlockOperation.codeLineNumber << ")" << endl;
               }
            }

            for (auto& currentOperation : deadlockOperations)
            {
               if (currentOperation.taskIdentifier != currentTaskIdentifier)
               {
                  continue;
               }

               for (const auto resourceIdentifier : currentOperation.deadlockOperation.resourceIdentifiers)
               {
                  if (currentOperation.resourceAssignedToTask)
                  {
                     continue;
                  }

                  deadlockReportTasksInCycle << reportLeft << "    - " << " Waiting for "
                       << currentOperation.deadlockOperation.resourcesType << " " << resourceIdentifier << " ("
                       << currentOperation.deadlockOperation.codeFilename << ":" << currentOperation.deadlockOperation.codeLineNumber << ")" << endl;

                  deadlockReportTasksInCycle << reportLeft << "       which is assigned to: " << getTaskAssignedToResource(resourceIdentifier, currentOperation.deadlockOperation.resourcesType, deadlockOperations) << endl;
               }
            }

            deadlockReportTasksInCycle << reportLeft << endl;
         }

         deadlockReportOtherTasksWithOperations << reportLeft << "Other tasks with synchronizing operations:" << endl;

         int otherTasksWithOperationsCount = 0;

         for (const auto& currentTaskIdentifier : taskIdentifiers)
         {
            if (std::find(cycleTaskIdentifiers.begin(), cycleTaskIdentifiers.end(), currentTaskIdentifier) != cycleTaskIdentifiers.end())
            {
               continue;
            }

            deadlockReportOtherTasksWithOperations << reportLeft << " - " << currentTaskIdentifier << ":" << endl;
            otherTasksWithOperationsCount++;

            vector<string> holdingResources = graph->getIncomingEdges(currentTaskIdentifier);

            for (const auto& currentResourceIdentifier : holdingResources)
            {
               PerformedDeadlockOperation deadlockOperation = getPerformedDeadlockOperation(currentTaskIdentifier, currentResourceIdentifier, true);

               if (!deadlockOperation.initialized)
               {
                  continue;
               }

               deadlockReportOtherTasksWithOperations << reportLeft << "    - " << " Holding "
                    << deadlockOperation.deadlockOperation.resourcesType << " " << currentResourceIdentifier << " ("
                    << deadlockOperation.deadlockOperation.codeFilename << ":" << deadlockOperation.deadlockOperation.codeLineNumber << ")" << endl;
            }

            vector<string> waitingResources = graph->getOutgoingEdges(currentTaskIdentifier);

            // TODO Waiting for SEMA semA obwohl RELEASE semA durchgefuehrt wird

            for (const auto& currentResourceIdentifier : waitingResources)
            {
               PerformedDeadlockOperation deadlockOperation = getPerformedDeadlockOperation(currentTaskIdentifier, currentResourceIdentifier, false);

               if (!deadlockOperation.initialized)
               {
                  continue;
               }

               deadlockReportOtherTasksWithOperations << reportLeft << "    - " << " Waiting for "
                    << deadlockOperation.deadlockOperation.resourcesType << " " << deadlockOperation.deadlockOperation.resourceIdentifiers[0] << " ("
                    << deadlockOperation.deadlockOperation.codeFilename << ":" << deadlockOperation.deadlockOperation.codeLineNumber << ")" << endl;

               vector<PerformedDeadlockOperation> allDeadlockOperations;

               for (auto const& edgeMetadataEntry : edgeMetadata)
               {
                  allDeadlockOperations.push_back(edgeMetadataEntry.second);
               }

               deadlockReportOtherTasksWithOperations << reportLeft << "       which is assigned to: " << getTaskAssignedToResource(deadlockOperation.deadlockOperation.resourceIdentifiers[0], deadlockOperation.deadlockOperation.resourcesType, allDeadlockOperations) << endl;
            }

            deadlockReportOtherTasksWithOperations << reportLeft << endl;
         }

         if (otherTasksWithOperationsCount == 0)
         {
            deadlockReportOtherTasksWithOperations << reportLeft << "  None" << endl << reportLeft << endl;
         }

         deadlockReportOperationsTrace << reportLeft << "Synchronizing operations trace:" << endl;

         auto currentIndex = deadlockOperationTrace.size();

         for (auto i = deadlockOperationTrace.rbegin(); i != deadlockOperationTrace.rend(); ++i)
         {
            string resourceIdentifiers;

            for (auto resourceIdentifier : (*i).deadlockOperation.resourceIdentifiers)
            {
               resourceIdentifiers.append(resourceIdentifier).append(", ");
            }

            if (resourceIdentifiers.length() > 2)
            {
               resourceIdentifiers = resourceIdentifiers.substr(0, resourceIdentifiers.length() - 2);
            }

            deadlockReportOperationsTrace << reportLeft << " - #" << currentIndex << " [" << HfujkControl::formatTimestamp((*i).callTimestamp) << "] " <<
                 "Task " << (*i).taskIdentifier << ": " <<
                 (*i).deadlockOperation.actionType << " " << resourceIdentifiers <<
                 " (" << (*i).deadlockOperation.codeFilename << ":" << (*i).deadlockOperation.codeLineNumber << ")" <<
                 endl;

            currentIndex--;
         }

         deadlockReportOperationsTrace << reportLeft << endl;

         if (!possibleSolvingDeadlockOperations.empty())
         {
            cout << endl << reportLine << endl << reportLeft << "WARNING: Possible Deadlock detected" << endl << reportLeft << endl;

            cout <<
               deadlockReportDependencyCycle.str() <<
               deadlockReportTasksInCycle.str() <<
               deadlockReportOtherTasksWithOperations.str() <<
               deadlockReportOperationsTrace.str();

            cout << reportLeft << "Possible solving operations:" << endl;

            for (auto& dissolveDeadlockOperationEntry : possibleSolvingDeadlockOperations)
            {
               cout << reportLeft << " - Task " << dissolveDeadlockOperationEntry.executingTaskIdentifier << ": " <<
                  dissolveDeadlockOperationEntry.deadlockOperation.actionType << " " << dissolveDeadlockOperationEntry.deadlockOperation.resourcesType <<" " <<
                  dissolveDeadlockOperationEntry.releasedResourceIdentifier;

               if (strcmp(dissolveDeadlockOperationEntry.deadlockOperation.contextType, "PROCEDURE") == 0)
               {
                  cout << " in procedure " << dissolveDeadlockOperationEntry.deadlockOperation.contextIdentifier;
               }

               cout << " (" << dissolveDeadlockOperationEntry.deadlockOperation.codeFilename << ":" << dissolveDeadlockOperationEntry.deadlockOperation.codeLineNumber << ")" << endl;
            }

            cout << reportLine << endl << endl;

            return false;
         }
         else
         {
            cout << endl << reportLine << endl << reportLeft << "ERROR: Deadlock detected" << endl << reportLeft << endl;

            cout <<
                 deadlockReportDependencyCycle.str() <<
                 deadlockReportTasksInCycle.str() <<
                 deadlockReportOtherTasksWithOperations.str() <<
                 deadlockReportOperationsTrace.str();

            cout << reportLine << endl << endl;

            return true;
         }
      }

      return false;
   }

   string ResourceAllocationGraph::getTaskAssignedToResource(const char* resourceIdentifier, const char* resourceType, vector<PerformedDeadlockOperation>& deadlockOperations)
   {
      string assignedTaskIdentifiersText;

      set<string> assignedTaskIdentifiers;

      for (auto& deadlockOperation : deadlockOperations)
      {
         for (const auto innerResourceIdentifier : deadlockOperation.deadlockOperation.resourceIdentifiers)
         {
            if ((deadlockOperation.resourceAssignedToTask) &&
                (innerResourceIdentifier == resourceIdentifier) &&
                (deadlockOperation.deadlockOperation.resourcesType == resourceType))
            {
               string current;
               current
                     .append(deadlockOperation.taskIdentifier)
                     .append(" (")
                     .append(deadlockOperation.deadlockOperation.codeFilename)
                     .append(":")
                     .append(to_string(deadlockOperation.deadlockOperation.codeLineNumber))
                     .append(")")
                     .append(", ");

               assignedTaskIdentifiers.insert(current);
               goto continueLoop;
            }
         }
         continueLoop:;
      }

      for (const auto& assignedTaskIdentifier : assignedTaskIdentifiers)
      {
         assignedTaskIdentifiersText.append(assignedTaskIdentifier);
      }

      if (!assignedTaskIdentifiers.empty())
      {
         assignedTaskIdentifiersText = assignedTaskIdentifiersText.substr(0, assignedTaskIdentifiersText.length() - 2);
      }

      return assignedTaskIdentifiersText;
   }

   PerformedDeadlockOperation ResourceAllocationGraph::getPerformedDeadlockOperation(const string& taskIdentifier, const string& resourceIdentifier, const bool isAllocated)
   {
      string edgeId;

      if (isAllocated)
      {
         edgeId = resourceIdentifier + "#" + taskIdentifier;
      }
      else
      {
         edgeId = taskIdentifier + "#" + resourceIdentifier;
      }

      return edgeMetadata[edgeId];
   }

   void ResourceAllocationGraph::print()
   {
      graph->printGraph();
   }

   void ResourceAllocationGraph::requestCountableResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible)
   {
      if (currentResourceValue > 1)
      {
         printResourceAllocationGraphOperation("Resource is not exclusive:  (" + taskIdentifier + ") <- [" + resourceIdentifier + "]");
      }
      else if (currentResourceValue <= 1)
      {
         requestResource(taskIdentifier, resourceIdentifier, deadlockOperation, currentResourceValue, operationIsPossible);
      }
   }

   void ResourceAllocationGraph::releaseCountableResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible)
   {
      if (operationIsPossible)
      {
         releaseResource(taskIdentifier, resourceIdentifier, deadlockOperation, currentResourceValue, operationIsPossible);
      }
   }

   void ResourceAllocationGraph::registerDeadlockOperation(const DeadlockOperation& deadlockOperation)
   {
      for (const auto resourceIdentifier : deadlockOperation.resourceIdentifiers)
      {
         graph->addNode(HfujkControl::formatIdentifier(resourceIdentifier));
      }

      registeredDeadlockOperations.push_back(deadlockOperation);
   }

   void ResourceAllocationGraph::printResourceAllocationGraphOperation(const string& string)
   {
      if (HfujkControl::ACTIVATE_STD_OUT)
      {
         cout << "---> " << string << endl;
      }
   }
}
