package static_analyzer.ControlFlowInterpreter;

import static_analyzer.AnalyzerMessage.Message;
import static_analyzer.*;

import java.util.*;

public class ControlFlowInterpreter
{
    Map<String, ControlFlowInterpreterResource> resources = new HashMap<>();
    Map<String, ControlFlowInterpreterTask> tasks = new HashMap<>();

    ControlFlowInterpreterUsage semaRequestRelease = new ControlFlowInterpreterUsage(DeadlockOperation.RESOURCE_TYPE_SEMAPHORE, DeadlockOperation.ACTION_TYPE_REQUEST, DeadlockOperation.ACTION_TYPE_RELEASE);
    ControlFlowInterpreterUsage boltReserveFree = new ControlFlowInterpreterUsage(DeadlockOperation.RESOURCE_TYPE_BOLT, DeadlockOperation.ACTION_TYPE_RESERVE, DeadlockOperation.ACTION_TYPE_FREE);
    ControlFlowInterpreterUsage boltEnterLeave = new ControlFlowInterpreterUsage(DeadlockOperation.RESOURCE_TYPE_BOLT, DeadlockOperation.ACTION_TYPE_ENTER, DeadlockOperation.ACTION_TYPE_LEAVE);

    private Graph<GraphNode> resourceOrderIntegerGraph;
    private Map<String, String> lastTaskResource;

    public boolean interpret(ApplicationControlFlowGraph applicationControlFlowGraph, Graph<ApplicationControlFlowGraphNode> graph)
    {
        resourceOrderIntegerGraph = new Graph<>();
        lastTaskResource = new HashMap<>();

        resources.clear();

        semaRequestRelease.reset();
        boltReserveFree.reset();
        boltEnterLeave.reset();

        for (Map.Entry<String, DeadlockResource> resourceEntry : applicationControlFlowGraph.deadlockResources.entrySet())
        {
            ControlFlowInterpreterResource resource = new ControlFlowInterpreterResource();
            resource.deadlockResource = resourceEntry.getValue();
            resource.currentOrder = -1;
            resource.presetValue = resourceEntry.getValue().presetValue;
            resources.put(resourceEntry.getKey(), resource);
        }

        for (Map.Entry<String, Task> taskEntry : applicationControlFlowGraph.tasks.entrySet())
        {
            ControlFlowInterpreterTask task = new ControlFlowInterpreterTask();
            task.task = taskEntry.getValue();
            task.currentOrderIndex = -1;
            tasks.put(taskEntry.getKey(), task);
        }

        for (Map.Entry<String, Task> taskEntry : applicationControlFlowGraph.tasks.entrySet())
        {
            semaRequestRelease.resetLevel(ControlFlowInterpreterUsage.LEVEL_TASK);
            boltReserveFree.resetLevel(ControlFlowInterpreterUsage.LEVEL_TASK);
            boltEnterLeave.resetLevel(ControlFlowInterpreterUsage.LEVEL_TASK);

            ApplicationControlFlowGraphNode startNode = graph.get("Start_" + taskEntry.getKey());

            if (startNode == null)
            {
                continue;
            }

            List<List<ApplicationControlFlowGraphNode>> pathsOfTask = graph.getPathsByLabel(startNode, taskEntry.getKey());

            ControlFlowInterpreterTask task = tasks.get(taskEntry.getKey());

            String currentProcedure = "";

            List<ControlFlowInterpreterPath> analyzedCorrectPathsOfTask = new ArrayList<>();
            List<ControlFlowInterpreterPath> analyzedIncorrectPathsOfTask = new ArrayList<>();

            for (List<ApplicationControlFlowGraphNode> pathOfTask : pathsOfTask)
            {
                semaRequestRelease.resetLevel(ControlFlowInterpreterUsage.LEVEL_FLOW);
                boltReserveFree.resetLevel(ControlFlowInterpreterUsage.LEVEL_FLOW);
                boltEnterLeave.resetLevel(ControlFlowInterpreterUsage.LEVEL_FLOW);

                ApplicationControlFlowGraphNode lastNode = null;

                boolean canCombine = false;

                List<ApplicationControlFlowGraphNode> combineNodes = new ArrayList<>();

                DeadlockOperation lastPathDeadlockOperation = null;
                Map<String, String> currentBoltResourceStates = new HashMap<>();

                for (ApplicationControlFlowGraphNode node : pathOfTask)
                {
                    canCombine = false;

                    if (node.getLabel() != null)
                    {
                        if (node.getLabel().startsWith("Entry_"))
                        {
                            currentProcedure = node.getLabel().substring(6);
                        }
                        else if (node.getLabel().equals("End_" + currentProcedure))
                        {
                            currentProcedure = "";
                        }
                    }

                    if (node.deadlockOperation != null)
                    {
                        visitDeadlockOperation(node.deadlockOperation);

                        performDeadlockOperationSetOrder(task, node.deadlockOperation);

                        if ((lastNode != null) && (lastNode.deadlockOperation != null))
                        {
                            if (canCombineDeadlockOperations(node.deadlockOperation, lastNode.deadlockOperation))
                            {
                                canCombine = true;

                                if (combineNodes.size() == 0)
                                {
                                    combineNodes.add(lastNode);
                                }

                                combineNodes.add(node);
                            }
                        }

                        checkDeadlockOperationSemantics(taskEntry.getValue().taskIdentifier, node.deadlockOperation, lastPathDeadlockOperation, currentBoltResourceStates);

                        if (node.deadlockOperation.resourcesType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT))
                        {
                            for (String resourceIdentifier : node.deadlockOperation.resourceIdentifiers)
                            {
                                currentBoltResourceStates.put(resourceIdentifier, node.deadlockOperation.actionType);
                            }
                        }

                        lastPathDeadlockOperation = node.deadlockOperation;
                    }

                    if (!canCombine)
                    {
                        if (combineNodes.size() > 0)
                        {
                            Message combineNodesMessage = new Message(Message.Severity.INFO);

                            if ((combineNodes.get(0).deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_ENTER))
                                    || (combineNodes.get(0).deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_LEAVE)))
                            {
                                combineNodesMessage.content = "Combine these statements to simplify the code:\n";
                            }
                            else
                            {
                                combineNodesMessage.content = "Combine these statements to reduce the risk of deadlocks:\n";
                            }

                            for (ApplicationControlFlowGraphNode currentCombineNode : combineNodes)
                            {
                                combineNodesMessage.content += " - " + currentCombineNode.deadlockOperation.getStatement(true) + "\n";
                            }

                            combineNodesMessage.problem = combineNodesMessage.content;
                            StaticAnalyzer.messageStack.add(combineNodesMessage);
                        }

                        combineNodes.clear();
                    }

                    lastNode = node;
                }

                String type = "Possible incorrect path in task";
                String typeIdentifier = task.task.taskIdentifier;

                ControlFlowInterpreterPath controlFlowInterpreterPath = new ControlFlowInterpreterPath();
                controlFlowInterpreterPath.path = pathOfTask;

                analyzeDeadlockOperationVisits(controlFlowInterpreterPath, type, 1, typeIdentifier, ControlFlowInterpreterUsage.LEVEL_FLOW);

                if (controlFlowInterpreterPath.correct)
                {
                    analyzedCorrectPathsOfTask.add(controlFlowInterpreterPath);
                }
                else
                {
                    analyzedIncorrectPathsOfTask.add(controlFlowInterpreterPath);
                }
            }

            for (ControlFlowInterpreterPath incorrectPath : analyzedIncorrectPathsOfTask)
            {
                ControlFlowInterpreterPath nearestCorrectPath = getNearestCorrectPath(analyzedCorrectPathsOfTask, incorrectPath);

                String incorrectPathMessageStackReferenceContent = "";

                if (nearestCorrectPath != null)
                {
                    List<DeadlockOperation> missingDeadlockOperations = getMissingDeadlockOperations(nearestCorrectPath, incorrectPath);

                    List<ApplicationControlFlowGraphNode> relatedCorrectStatements = getMissingNodesOfPath(nearestCorrectPath.path, incorrectPath.path);
                    List<ApplicationControlFlowGraphNode> relatedInCorrectStatements = getMissingNodesOfPath(incorrectPath.path, nearestCorrectPath.path);

                    incorrectPathMessageStackReferenceContent += "\n - Missing resource operation:\n";

                    for (DeadlockOperation deadlockOperation : missingDeadlockOperations)
                    {
                        incorrectPathMessageStackReferenceContent += "   - " + deadlockOperation.getStatement(true) + "\n";
                    }

                    if (relatedInCorrectStatements.size() > 0)
                    {
                        incorrectPathMessageStackReferenceContent += "\n - Possible incorrect statements in path:\n";

                        incorrectPathMessageStackReferenceContent += printPathStatements(relatedInCorrectStatements);
                    }

                    if (relatedCorrectStatements.size() > 0)
                    {
                        incorrectPathMessageStackReferenceContent += "\n - Most similar correct statements in path:\n";

                        incorrectPathMessageStackReferenceContent += printPathStatements(relatedCorrectStatements);
                    }
                }
                else
                {
                    incorrectPathMessageStackReferenceContent += "\n - List of performed operations:\n";

                    for (DeadlockOperation deadlockOperation : incorrectPath.performedDeadlockOperations)
                    {
                        incorrectPathMessageStackReferenceContent += "   - " + deadlockOperation.getStatement(true) + "\n";
                    }
                }

                for (Message currentMessageStackReference : incorrectPath.messageStackReferences)
                {
                    if ((currentMessageStackReference != null) && (currentMessageStackReference.content != null))
                    {
                        currentMessageStackReference.content += incorrectPathMessageStackReferenceContent;
                    }
                }
            }

            String type = "Task";
            String typeIdentifier = task.task.taskIdentifier;

            analyzeDeadlockOperationVisits(null, type, 5, typeIdentifier, ControlFlowInterpreterUsage.LEVEL_TASK);
        }

        List<GraphNode> topologicalSort = resourceOrderIntegerGraph.getNodesTopologicalSorted();

        int currentIndex = 0;

        for (GraphNode graphNode : topologicalSort)
        {
            if (graphNode.getId().equals("start"))
            {
                continue;
            }

            ControlFlowInterpreterResource currentResource = resources.get(graphNode.getId());

            if (currentResource != null)
            {
                currentResource.currentOrder = currentIndex;

                currentIndex++;
            }
        }

        analyzeDeadlockOperationVisits(null, "Module", 10, "", ControlFlowInterpreterUsage.LEVEL_MODULE);

        Map<ControlFlowInterpreterUsage, String> onlyRequestedReleasedMessageFilters = new HashMap<>();
        onlyRequestedReleasedMessageFilters.put(semaRequestRelease, DeadlockOperation.RESOURCE_TYPE_SEMAPHORE + " " + DeadlockOperation.ACTION_TYPE_REQUEST + " " + DeadlockOperation.ACTION_TYPE_RELEASE);
        onlyRequestedReleasedMessageFilters.put(boltReserveFree, DeadlockOperation.RESOURCE_TYPE_BOLT + " " + DeadlockOperation.ACTION_TYPE_RESERVE + " " + DeadlockOperation.ACTION_TYPE_FREE);
        onlyRequestedReleasedMessageFilters.put(boltEnterLeave, DeadlockOperation.RESOURCE_TYPE_BOLT + " " + DeadlockOperation.ACTION_TYPE_ENTER + " " + DeadlockOperation.ACTION_TYPE_LEAVE);

        for (Map.Entry<ControlFlowInterpreterUsage, String> entry : onlyRequestedReleasedMessageFilters.entrySet())
        {
            for (String onlyRequestedResourceTypeIdentifier : entry.getKey().getRequestedResources(ControlFlowInterpreterUsage.LEVEL_ALL))
            {
                if (entry.getKey().getReleasedResources(ControlFlowInterpreterUsage.LEVEL_ALL).contains(onlyRequestedResourceTypeIdentifier))
                {
                    StaticAnalyzer.messageStack.remove(entry.getValue() + " requested not released " + onlyRequestedResourceTypeIdentifier);
                    StaticAnalyzer.messageStack.remove(entry.getValue() + " released not requested " + onlyRequestedResourceTypeIdentifier);
                }
            }
        }

        for (Map.Entry<String, Task> taskEntry : applicationControlFlowGraph.tasks.entrySet())
        {
            ControlFlowInterpreterTask task = tasks.get(taskEntry.getKey());

            List<List<ApplicationControlFlowGraphNode>> pathsOfTask = graph.getPathsByLabel(graph.get("Start_" + taskEntry.getKey()), taskEntry.getKey());

            for (List<ApplicationControlFlowGraphNode> pathOfTask : pathsOfTask)
            {
                task.allocatedResources.clear();
                task.allocations.clear();
                task.currentOrderIndex = -1;
                task.performedDeadlockOperations.clear();

                for (ApplicationControlFlowGraphNode node : pathOfTask)
                {
                    if (node.deadlockOperation != null)
                    {
                        performDeadlockOperationValidateOrder(task, node.deadlockOperation);
                    }
                }
            }
        }

        for (Map.Entry<String, ControlFlowInterpreterResource> resourceEntry : resources.entrySet())
        {
            String resourceLabel = resourceEntry.getValue().deadlockResource.resourceType + " " + resourceEntry.getValue().deadlockResource.resourceIdentifier;

            if (resourceEntry.getValue().deadlockResource.resourceType.equals(DeadlockOperation.RESOURCE_TYPE_SEMAPHORE))
            {
                if ((!semaRequestRelease.getRequestedResources(ControlFlowInterpreterUsage.LEVEL_MODULE).contains(resourceLabel))
                        && (!semaRequestRelease.getReleasedResources(ControlFlowInterpreterUsage.LEVEL_MODULE).contains(resourceLabel)))
                {
                    Message resourceNotUsedMessage = new Message(Message.Severity.INFO);
                    resourceNotUsedMessage.problem = "resource unused " + resourceEntry.getKey();
                    resourceNotUsedMessage.content = "Unused resource: " + resourceEntry.getValue().dump();
                    StaticAnalyzer.messageStack.add(resourceNotUsedMessage);
                }
            }
            else if (resourceEntry.getValue().deadlockResource.resourceType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT))
            {
                if ((!boltReserveFree.getRequestedResources(ControlFlowInterpreterUsage.LEVEL_MODULE).contains(resourceLabel))
                        && (!boltReserveFree.getReleasedResources(ControlFlowInterpreterUsage.LEVEL_MODULE).contains(resourceLabel))
                        && (!boltEnterLeave.getRequestedResources(ControlFlowInterpreterUsage.LEVEL_MODULE).contains(resourceLabel))
                        && (!boltEnterLeave.getReleasedResources(ControlFlowInterpreterUsage.LEVEL_MODULE).contains(resourceLabel)))
                {
                    Message resourceNotUsedMessage = new Message(Message.Severity.INFO);
                    resourceNotUsedMessage.problem = "resource unused " + resourceEntry.getKey();
                    resourceNotUsedMessage.content = "Unused resource: " + resourceEntry.getValue().dump();
                    StaticAnalyzer.messageStack.add(resourceNotUsedMessage);
                }
            }
        }

        return true;
    }

    private String printPathStatements(List<ApplicationControlFlowGraphNode> pathNodes)
    {
        StringBuilder result = new StringBuilder();

        int lastCodeLineNumber = 0;

        for (ApplicationControlFlowGraphNode node : pathNodes)
        {
            List<PathNodeMetadata> pathNodeMetadataEntries = StaticAnalyzer.getPathNodeMetadataByNodeId(node.getId());

            if (pathNodeMetadataEntries.size() > 0)
            {
                for (PathNodeMetadata current : pathNodeMetadataEntries)
                {
                    int currentCodeLineNumber = current.codeLineNumber;

                    if (currentCodeLineNumber == 0)
                    {
                        currentCodeLineNumber = lastCodeLineNumber;
                    }

                    result.append("   - ").append(current.codeFilename).append(":").append(currentCodeLineNumber).append(": ").append(current.statement).append("\n");

                    lastCodeLineNumber = currentCodeLineNumber;
                }
            }
        }

        return result.toString();
    }

    private void checkDeadlockOperationSemantics(String taskIdentifier, DeadlockOperation currentOperation, DeadlockOperation previousOperation, Map<String, String> currentBoltResourceStates)
    {
        if (currentOperation == null)
        {
            return;
        }

        if (currentOperation.resourcesType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT))
        {
            List<String> duplicates = Utils.getDuplicates(currentOperation.resourceIdentifiers);

            if (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RESERVE))
            {
                if (duplicates.size() > 0)
                {
                    generateMessage(Message.Severity.ERROR,
                            "duplicates bolt single reserve operation " + currentOperation.getCodePosition() + " " + Arrays.toString(duplicates.toArray()),
                            "Duplicates in RESERVE BOLT operation is a runtime error",
                            " - " + currentOperation.getStatement(true)
                    );
                }
            }
            else if (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_FREE))
            {
                if (duplicates.size() > 0)
                {
                    generateMessage(Message.Severity.ERROR,
                            "duplicates bolt single free operation " + currentOperation.getCodePosition() + " " + Arrays.toString(duplicates.toArray()),
                            "Duplicates in FREE BOLT operation is a runtime error",
                            " - " + currentOperation.getStatement(true)
                    );
                }
            }
            else if (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_ENTER))
            {
                if (duplicates.size() > 0)
                {
                    generateMessage(Message.Severity.INFO,
                            "duplicates bolt single enter operation " + currentOperation.getCodePosition() + " " + Arrays.toString(duplicates.toArray()),
                            "Check if the duplicates in this ENTER BOLT operation are necessary",
                            " - " + currentOperation.getStatement(true)
                    );
                }
            }
            else if (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_LEAVE))
            {
                if (duplicates.size() > 0)
                {
                    generateMessage(Message.Severity.WARNING,
                            "duplicates bolt single leave operation " + currentOperation.getCodePosition() + " " + Arrays.toString(duplicates.toArray()),
                            "Check if the duplicates in this LEAVE BOLT operation are necessary, LEAVE BOLT while it is already in free state is a runtime error",
                            " - " + currentOperation.getStatement(true)
                    );
                }
            }

            for (String currentResourceIdentifier : currentOperation.resourceIdentifiers)
            {
                String lastResourceState = currentBoltResourceStates.get(currentResourceIdentifier);

                if (lastResourceState != null)
                {
                    boolean mixedUsage = false;

                    // Enter/Leave -> Reserve/Free
                    if ((currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_ENTER)) || (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_LEAVE)))
                    {
                        if ((lastResourceState.equals(DeadlockOperation.ACTION_TYPE_RESERVE)) || (lastResourceState.equals(DeadlockOperation.ACTION_TYPE_FREE)))
                        {
                            mixedUsage = true;
                        }
                    }

                    // Reserve/Free -> Enter/Leave
                    if ((currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RESERVE)) || (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_FREE)))
                    {
                        if ((lastResourceState.equals(DeadlockOperation.ACTION_TYPE_ENTER)) || (lastResourceState.equals(DeadlockOperation.ACTION_TYPE_LEAVE)))
                        {
                            mixedUsage = true;
                        }
                    }

                    if (mixedUsage)
                    {
                        String examples = "";

                        if (previousOperation != null)
                        {
                            examples = " - " + previousOperation.getStatement(true) + "\n";
                        }

                        examples += " - " + currentOperation.getStatement(true);

                        generateMessage(Message.Severity.WARNING,
                                "mixed bolt usage in task " + taskIdentifier,
                                "Mixed usage of BOLT " + currentResourceIdentifier + " in task " + taskIdentifier,
                                examples
                        );
                    }

                    if (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RESERVE))
                    {
                        if (lastResourceState.equals(DeadlockOperation.ACTION_TYPE_RESERVE))
                        {
                            generateMessage(Message.Severity.WARNING,
                                    "bolt reserve operation in reserve state " + currentOperation.getCodePosition(),
                                    "RESERVE BOLT " + currentResourceIdentifier + " while it is in reserved state, make sure that another task frees this BOLT",
                                    " - " + currentOperation.getStatement(true)
                            );
                        }
                    }
                    else if (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_FREE))
                    {
                        if ((previousOperation != null) && (previousOperation.resourcesType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT)) && (previousOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_FREE)) && (previousOperation.resourceIdentifiers.contains(currentResourceIdentifier)))
                        {
                            generateMessage(Message.Severity.ERROR,
                                    "bolt free operations direct " + currentOperation.getCodePosition(),
                                    "Multiple FREE BOLT " + currentResourceIdentifier + " operations without other synchronizing operations between is very likely a runtime error",
                                    " - " + previousOperation.getStatement(true) + "\n" + " - " + currentOperation.getStatement(true)
                            );
                        }
                        else if (lastResourceState.equals(DeadlockOperation.ACTION_TYPE_FREE))
                        {
                            generateMessage(Message.Severity.WARNING,
                                    "bolt free operation in free state " + currentOperation.getCodePosition(),
                                    "FREE BOLT " + currentResourceIdentifier + " while it is in free state, make sure that another task reserves this BOLT, otherwise this is a runtime error",
                                    " - " + currentOperation.getStatement(true)
                            );
                        }
                    }
                    else if (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_ENTER))
                    {
                        if (lastResourceState.equals(DeadlockOperation.ACTION_TYPE_ENTER))
                        {
                            generateMessage(Message.Severity.INFO,
                                    "bolt enter operation in enter state " + currentOperation.getCodePosition(),
                                    "Check if it is necessary that the task performs multiple ENTER operations on BOLT " + currentResourceIdentifier,
                                    " - " + currentOperation.getStatement(true)
                            );
                        }
                    }
                    else if (currentOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_LEAVE))
                    {
                        if (lastResourceState.equals(DeadlockOperation.ACTION_TYPE_LEAVE))
                        {
                            generateMessage(Message.Severity.WARNING,
                                    "leave enter operation in leaved state " + currentOperation.getCodePosition(),
                                    "LEAVE BOLT " + currentResourceIdentifier + " while it is possibly already in free state is a runtime error",
                                    " - " + currentOperation.getStatement(true)
                            );
                        }
                    }
                }
            }
        }
    }

    private void generateMessage(Message.Severity severity, String problem, String content, String examples)
    {
        if ((problem == null) || (problem.isEmpty()))
        {
            problem = content;
        }

        if ((examples != null) && (!examples.isEmpty()))
        {
            examples = "\n" + examples;
        }

        Message currentMessage = StaticAnalyzer.messageStack.getByProblem(problem);

        if (currentMessage != null)
        {
            currentMessage.content += examples;
            return;
        }

        Message message = new Message(severity);
        message.content = content + examples;

        message.problem = problem;
        StaticAnalyzer.messageStack.add(message);
    }

    private boolean canCombineDeadlockOperations(DeadlockOperation operation1, DeadlockOperation operation2)
    {
        if ((operation1 == null) || (operation2 == null))
        {
            return false;
        }

        if (!operation1.codeFilename.equals(operation2.codeFilename))
        {
            return false;
        }

        if ((operation1.resourcesType.equals(operation2.resourcesType)) && (operation1.actionType.equals(operation2.actionType)))
        {
            if (operation1.resourcesType.equals(DeadlockOperation.RESOURCE_TYPE_SEMAPHORE))
            {
                return true;
            }
            else if (operation1.resourcesType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT))
            {
                List<String> mergedResourceIdentifiers = new ArrayList<>(operation1.resourceIdentifiers);
                mergedResourceIdentifiers.addAll(operation2.resourceIdentifiers);

                return (Utils.getDuplicates(mergedResourceIdentifiers).size() == 0);
            }
        }

        return false;
    }

    private void visitDeadlockOperation(DeadlockOperation deadlockOperation)
    {
        for (String resourceIdentifier : deadlockOperation.resourceIdentifiers)
        {
            String resourceLabel = deadlockOperation.resourcesType + " " + resourceIdentifier;

            if (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_REQUEST))
            {
                semaRequestRelease.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_MODULE, resourceLabel);
                semaRequestRelease.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_TASK, resourceLabel);
                semaRequestRelease.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_FLOW, resourceLabel);
            }
            else if (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RELEASE))
            {
                semaRequestRelease.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_MODULE, resourceLabel);
                semaRequestRelease.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_TASK, resourceLabel);
                semaRequestRelease.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_FLOW, resourceLabel);
            }
            else if (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RESERVE))
            {
                boltReserveFree.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_MODULE, resourceLabel);
                boltReserveFree.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_TASK, resourceLabel);
                boltReserveFree.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_FLOW, resourceLabel);
            }
            else if (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_FREE))
            {
                boltReserveFree.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_MODULE, resourceLabel);
                boltReserveFree.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_TASK, resourceLabel);
                boltReserveFree.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_FLOW, resourceLabel);
            }
            else if (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_ENTER))
            {
                boltEnterLeave.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_MODULE, resourceLabel);
                boltEnterLeave.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_TASK, resourceLabel);
                boltEnterLeave.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_FLOW, resourceLabel);
            }
            else if (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_LEAVE))
            {
                boltEnterLeave.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_MODULE, resourceLabel);
                boltEnterLeave.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_TASK, resourceLabel);
                boltEnterLeave.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_FLOW, resourceLabel);
            }
        }
    }

    private void analyzeDeadlockOperationVisits(ControlFlowInterpreterPath controlFlowInterpreterPath, String type, int typePriority, String typeIdentifier, String level)
    {
        if (controlFlowInterpreterPath != null)
        {
            controlFlowInterpreterPath.correct = true;
            controlFlowInterpreterPath.notAllRequestedSema = false;
            controlFlowInterpreterPath.notAllReleasedSema = false;
            controlFlowInterpreterPath.notAllReservedBolt = false;
            controlFlowInterpreterPath.notAllFreedBolt = false;
            controlFlowInterpreterPath.notAllEnteredBolt = false;
            controlFlowInterpreterPath.notAllLeavedBolt = false;
        }

        String typeName = (type + " " + typeIdentifier).trim();

        Message messageSemaRequestRelease = analyzeDeadlockOperationVisits(
                semaRequestRelease.getRequestedResources(level),
                semaRequestRelease.getReleasedResources(level),
                DeadlockOperation.RESOURCE_TYPE_SEMAPHORE, DeadlockOperation.ACTION_TYPE_REQUEST, DeadlockOperation.ACTION_TYPE_RELEASE,
                typePriority, typeName, controlFlowInterpreterPath
        );

        Message messageBoltReserveFree = analyzeDeadlockOperationVisits(
                boltReserveFree.getRequestedResources(level),
                boltReserveFree.getReleasedResources(level),
                DeadlockOperation.RESOURCE_TYPE_BOLT, DeadlockOperation.ACTION_TYPE_RESERVE, DeadlockOperation.ACTION_TYPE_FREE,
                typePriority, typeName, controlFlowInterpreterPath
        );

        Message messageBoltEnterLeave = analyzeDeadlockOperationVisits(
                boltEnterLeave.getRequestedResources(level),
                boltEnterLeave.getReleasedResources(level),
                DeadlockOperation.RESOURCE_TYPE_BOLT, DeadlockOperation.ACTION_TYPE_ENTER, DeadlockOperation.ACTION_TYPE_LEAVE,
                typePriority, typeName, controlFlowInterpreterPath
        );

        if (controlFlowInterpreterPath != null)
        {
            controlFlowInterpreterPath.apply();

            List<Message> messages = new ArrayList<>();

            if (messageSemaRequestRelease != null)
            {
                messages.add(messageSemaRequestRelease);
            }

            if (messageBoltReserveFree != null)
            {
                messages.add(messageBoltReserveFree);
            }

            if (messageBoltEnterLeave != null)
            {
                messages.add(messageBoltEnterLeave);
            }

            controlFlowInterpreterPath.messageStackReferences = messages;
        }
    }

    private Message analyzeDeadlockOperationVisits(Set<String> requests, Set<String> releases, String resourceType, String requestAction, String releaseAction, int typePriority, String typeName, ControlFlowInterpreterPath controlFlowInterpreterPath)
    {
        Message message = null;

        String key = resourceType + " " + requestAction + " " + releaseAction + " ";

        String errorMessageOnlyRequested = "";
        String errorMessageOnlyReleased = "";

        if ((resourceType.equals(DeadlockOperation.RESOURCE_TYPE_SEMAPHORE)) && (requestAction.equals(DeadlockOperation.ACTION_TYPE_REQUEST)) && (releaseAction.equals(DeadlockOperation.ACTION_TYPE_RELEASE)))
        {
            errorMessageOnlyRequested = "is requested but never released";
            errorMessageOnlyReleased = "is released but never requested";
        }
        else if ((resourceType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT)) && (requestAction.equals(DeadlockOperation.ACTION_TYPE_RESERVE)) && (releaseAction.equals(DeadlockOperation.ACTION_TYPE_FREE)))
        {
            errorMessageOnlyRequested = "is reserved but never freed";
            errorMessageOnlyReleased = "is freed but never reserved";
        }
        else if ((resourceType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT)) && (requestAction.equals(DeadlockOperation.ACTION_TYPE_ENTER)) && (releaseAction.equals(DeadlockOperation.ACTION_TYPE_LEAVE)))
        {
            errorMessageOnlyRequested = "is entered but never leaved";
            errorMessageOnlyReleased = "is leaved but never entered";
        }

        for (String resourceIdentifier : requests)
        {
            if (!releases.contains(resourceIdentifier))
            {
                message = new Message(Message.Severity.WARNING);
                message.problem = key + "requested not released " + resourceIdentifier;
                message.priority = typePriority;
                message.content = typeName + ": The resource " + resourceIdentifier + " " + errorMessageOnlyRequested;

                semaRequestRelease.addRequestedResource(ControlFlowInterpreterUsage.LEVEL_ALL, resourceIdentifier);

                StaticAnalyzer.messageStack.add(message);

                if (controlFlowInterpreterPath != null)
                {
                    controlFlowInterpreterPath.correct = false;

                    if ((resourceType.equals(DeadlockOperation.RESOURCE_TYPE_SEMAPHORE)) && (releaseAction.equals(DeadlockOperation.ACTION_TYPE_RELEASE)))
                    {
                        controlFlowInterpreterPath.notAllReleasedSema = true;
                    }
                    else if ((resourceType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT)) && (releaseAction.equals(DeadlockOperation.ACTION_TYPE_FREE)))
                    {
                        controlFlowInterpreterPath.notAllFreedBolt = true;
                    }
                    else if ((resourceType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT)) && (releaseAction.equals(DeadlockOperation.ACTION_TYPE_LEAVE)))
                    {
                        controlFlowInterpreterPath.notAllLeavedBolt = true;
                    }
                }
            }
        }

        for (String resourceIdentifier : releases)
        {
            if (!requests.contains(resourceIdentifier))
            {
                message = new Message(Message.Severity.WARNING);
                message.problem = key + "released not requested " + resourceIdentifier;
                message.priority = typePriority;
                message.content = typeName + ": The resource " + resourceIdentifier + " " + errorMessageOnlyReleased;

                semaRequestRelease.addReleasedResource(ControlFlowInterpreterUsage.LEVEL_ALL, resourceIdentifier);

                StaticAnalyzer.messageStack.add(message);

                if (controlFlowInterpreterPath != null)
                {
                    controlFlowInterpreterPath.correct = false;

                    if ((resourceType.equals(DeadlockOperation.RESOURCE_TYPE_SEMAPHORE)) && (requestAction.equals(DeadlockOperation.ACTION_TYPE_REQUEST)))
                    {
                        controlFlowInterpreterPath.notAllRequestedSema = true;
                    }
                    else if ((resourceType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT)) && (requestAction.equals(DeadlockOperation.ACTION_TYPE_RESERVE)))
                    {
                        controlFlowInterpreterPath.notAllReservedBolt = true;
                    }
                    else if ((resourceType.equals(DeadlockOperation.RESOURCE_TYPE_BOLT)) && (requestAction.equals(DeadlockOperation.ACTION_TYPE_ENTER)))
                    {
                        controlFlowInterpreterPath.notAllEnteredBolt = true;
                    }
                }
            }
        }

        return message;
    }

    private void performDeadlockOperationSetOrder(ControlFlowInterpreterTask task, DeadlockOperation deadlockOperation)
    {
        for (String resourceIdentifier : deadlockOperation.resourceIdentifiers)
        {
            resourceIdentifier = Utils.trim(resourceIdentifier, "_");

            if ((deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_REQUEST)) || (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RESERVE)))
            {
                if (!resourceOrderIntegerGraph.containsNode(new GraphNode(resourceIdentifier)))
                {
                    String currentLastTaskResource = lastTaskResource.get(task.task.taskIdentifier);

                    if (currentLastTaskResource == null)
                    {
                        currentLastTaskResource = "start";
                    }

                    resourceOrderIntegerGraph.addEdge(new GraphNode(currentLastTaskResource), new GraphNode(resourceIdentifier), task.task.taskIdentifier);

                    lastTaskResource.put(task.task.taskIdentifier, resourceIdentifier);
                }
            }
        }
    }

    private void performDeadlockOperationValidateOrder(ControlFlowInterpreterTask task, DeadlockOperation deadlockOperation)
    {
        task.performedDeadlockOperations.add(deadlockOperation);

        int currentResourcesMaximalOrder = -1;
        int currentResourcesMinimalOrder = -1;

        for (String resourceIdentifier : deadlockOperation.resourceIdentifiers)
        {
            resourceIdentifier = Utils.trim(resourceIdentifier, "_");

            ControlFlowInterpreterResource currentResource = resources.get(resourceIdentifier);

            if (currentResource != null)
            {
                if ((deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_REQUEST)) || (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RESERVE)))
                {
                    if ((currentResourcesMinimalOrder == -1) || (currentResourcesMinimalOrder > currentResource.currentOrder))
                    {
                        currentResourcesMinimalOrder = currentResource.currentOrder;
                    }

                    if (currentResourcesMaximalOrder < currentResource.currentOrder)
                    {
                        currentResourcesMaximalOrder = currentResource.currentOrder;
                    }
                }
            }
        }

        for (String resourceIdentifier : deadlockOperation.resourceIdentifiers)
        {
            resourceIdentifier = Utils.trim(resourceIdentifier, "_");

            ControlFlowInterpreterResource resource = resources.get(resourceIdentifier);

            if ((deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_REQUEST)) || (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RESERVE)))
            {
                if (task.currentOrderIndex >= currentResourcesMinimalOrder)
                {
                    Message lockOrderMessage = new Message(Message.Severity.WARNING);
                    lockOrderMessage.content = "Possible deadlock because of inconsistent lock order:\n";

                    if (task.currentOrderIndex == currentResourcesMinimalOrder)
                    {
                        lockOrderMessage.content += " - " + deadlockOperation.codeFilename + ":" + deadlockOperation.codeLineNumber + ": Task " + task.task.taskIdentifier + " requested " + deadlockOperation.resourcesType + " " + resourceIdentifier + " [order " + resources.get(resourceIdentifier).currentOrder + "] while holding this resource already:\n";
                    }
                    else
                    {
                        lockOrderMessage.content += " - " + deadlockOperation.codeFilename + ":" + deadlockOperation.codeLineNumber + ": Task " + task.task.taskIdentifier + " requested " + deadlockOperation.resourcesType + " " + resourceIdentifier + " [order " + resources.get(resourceIdentifier).currentOrder + "] while holding resources with higher ordering:\n";
                    }

                    for (ControlFlowInterpreterTaskResourceAllocation allocation : task.allocations)
                    {
                        String resourceOrderHint = "";

                        if (allocation.deadlockResource.currentOrder > resources.get(resourceIdentifier).currentOrder)
                        {
                            resourceOrderHint = "Higher order, critical";
                        }
                        else if (allocation.deadlockResource.currentOrder == resources.get(resourceIdentifier).currentOrder)
                        {
                            resourceOrderHint = "Same resource, critical";
                        }
                        else
                        {
                            resourceOrderHint = "Lower order, not critical";
                        }

                        lockOrderMessage.content += " - " + allocation.deadlockOperation.codeFilename + ":" + allocation.deadlockOperation.codeLineNumber + ": " + allocation.deadlockOperation.resourcesType + " " + allocation.deadlockResource.deadlockResource.resourceIdentifier + " [order " + allocation.deadlockResource.currentOrder + "]: " + resourceOrderHint + "\n";
                    }

                    lockOrderMessage.problem = lockOrderMessage.content;
                    StaticAnalyzer.messageStack.add(lockOrderMessage);
                }

                task.allocatedResources.add(resource);

                ControlFlowInterpreterTaskResourceAllocation controlFlowInterpreterTaskResourceAllocation = new ControlFlowInterpreterTaskResourceAllocation();
                controlFlowInterpreterTaskResourceAllocation.deadlockResource = resource;
                controlFlowInterpreterTaskResourceAllocation.deadlockOperation = deadlockOperation;
                task.allocations.add(controlFlowInterpreterTaskResourceAllocation);
            }
            else if ((deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RELEASE)) || (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_FREE)))
            {
                task.allocatedResources.remove(resource);

                if (task.currentOrderIndex == resource.currentOrder)
                {
                    int currentMaxOrder = -1;

                    for (ControlFlowInterpreterResource cuResource : task.allocatedResources)
                    {
                        if (cuResource.currentOrder > currentMaxOrder)
                        {
                            currentMaxOrder = cuResource.currentOrder;
                        }
                    }

                    task.currentOrderIndex = currentMaxOrder;
                }

                ControlFlowInterpreterTaskResourceAllocation removedAllocation = null;

                for (int i = task.allocations.size() - 1; i >= 0; i--)
                {
                    if (task.allocations.get(i).deadlockResource == resource)
                    {
                        removedAllocation = task.allocations.get(i);
                        break;
                    }
                }

                if (removedAllocation != null)
                {
                    task.allocations.remove(removedAllocation);
                }
            }
        }

        if ((deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_REQUEST)) || (deadlockOperation.actionType.equals(DeadlockOperation.ACTION_TYPE_RESERVE)))
        {
            task.currentOrderIndex = currentResourcesMaximalOrder;
        }
    }

    public static ControlFlowInterpreterPath getNearestCorrectPath(List<ControlFlowInterpreterPath> correctPaths, ControlFlowInterpreterPath incorrectPath)
    {
        ControlFlowInterpreterPath currentNearestCorrectPath = null;
        int similarityValue = 0;

        for (ControlFlowInterpreterPath currentCorrectPath : correctPaths)
        {
            int currentSimilarityValue = comparePaths(currentCorrectPath.path, incorrectPath.path);

            if ((currentNearestCorrectPath == null) || (currentSimilarityValue > similarityValue))
            {
                currentNearestCorrectPath = currentCorrectPath;
                similarityValue = currentSimilarityValue;
            }
        }

        return currentNearestCorrectPath;
    }

    public static int comparePaths(List<ApplicationControlFlowGraphNode> pathA, List<ApplicationControlFlowGraphNode> pathB)
    {
        int similarityValue = 0;
        int idxA = 0;
        int idxB = 0;

        while (true)
        {
            if ((pathA.size() > idxA) && (pathB.size() > idxB))
            {
                if (pathA.get(idxA).isSame(pathB.get(idxB), true))
                {
                    similarityValue++;
                    idxA++;
                    idxB++;
                }
                else
                {
                    boolean hasAnotherSame = false;

                    for (int iA = idxA; iA < pathA.size(); iA++)
                    {
                        int iB = getGraphNodeListIndexOf(pathB, pathA.get(iA), idxB, true);

                        if (iB > -1)
                        {
                            idxA = iA;
                            idxB = iB;
                            hasAnotherSame = true;
                            break;
                        }
                    }

                    if (!hasAnotherSame)
                    {
                        break;
                    }
                }
            }
            else
            {
                break;
            }
        }

        return similarityValue;
    }

    public static int getGraphNodeListIndexOf(List<ApplicationControlFlowGraphNode> path, ApplicationControlFlowGraphNode searched, int startIndex, boolean compareLabels)
    {
        for (int i = startIndex; i < path.size(); i++)
        {
            if (path.get(i).isSame(searched, compareLabels))
            {
                return i;
            }
        }

        return -1;
    }

    public static List<ApplicationControlFlowGraphNode> getMissingNodesOfPath(List<ApplicationControlFlowGraphNode> completePath, List<ApplicationControlFlowGraphNode> incompletePath)
    {
        List<ApplicationControlFlowGraphNode> missingNodes = new ArrayList<>();

        ApplicationControlFlowGraphNode lastSimilarNode = null;

        for (ApplicationControlFlowGraphNode pathNode : completePath)
        {
            if ((!incompletePath.contains(pathNode)) && (!missingNodes.contains(pathNode)))
            {
                if (lastSimilarNode != null)
                {
                    missingNodes.add(lastSimilarNode);
                    lastSimilarNode = null;
                }

                missingNodes.add(pathNode);
            }
            else
            {
                lastSimilarNode = pathNode;
            }
        }

        return missingNodes;
    }

    public static List<DeadlockOperation> getMissingDeadlockOperations(ControlFlowInterpreterPath correctPath, ControlFlowInterpreterPath incorrectPath)
    {
        List<DeadlockOperation> missingDeadlockOperations = new ArrayList<>();

        for (DeadlockOperation deadlockOperation : correctPath.performedDeadlockOperations)
        {
            if (!incorrectPath.performedDeadlockOperations.contains(deadlockOperation))
            {
                missingDeadlockOperations.add(deadlockOperation);
            }
        }

        return missingDeadlockOperations;
    }
}
