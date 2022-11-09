// hfujk

package org.openpearl.compiler.DeadLockDetection.GraphUtils;

import java.util.*;

public class IntegerGraph {

    private final Map<Integer, List<IntegerGraphVertex>> vertices = new HashMap<>();
    private int minNodeValue = -1;
    private int maxNodeValue = -1;

    private static final int NODE_STATE_NOT_VISITED = 0;
    private static final int NODE_STATE_FINISHED_VISITING = 1;
    private static final int NODE_STATE_IS_BEING_VISITED = -1;

    private boolean cycleDetected = false;
    private final Stack<Integer> cycleDetectionPathStack = new Stack<>();
    private final List<Integer> cycleDetectionPath = new ArrayList<>();

    private void performDFS(int currentNode, int[] visitedNodes)
    {
        cycleDetectionPathStack.push(currentNode);

        if (visitedNodes[currentNode] == NODE_STATE_IS_BEING_VISITED)
        {
            List<Integer> cycleDetectionPathVector = new ArrayList<>();

            while (!cycleDetectionPathStack.empty())
            {
                cycleDetectionPathVector.add(cycleDetectionPathStack.peek());
                cycleDetectionPathStack.pop();
            }

            Collections.reverse(cycleDetectionPathVector);

            int lastCycleNode = cycleDetectionPathVector.get(cycleDetectionPathVector.size() - 1);
            boolean cycleStarted = false;

            for (int currentCycleNode: cycleDetectionPathVector)
            {
                if (!cycleStarted)
                {
                    if (currentCycleNode == lastCycleNode) {
                        cycleStarted = true;
                    }
                }

                if (cycleStarted)
                {
                    cycleDetectionPath.add(currentCycleNode);
                }
            }

            cycleDetected = true;

            return;
        }

        visitedNodes[currentNode] = NODE_STATE_IS_BEING_VISITED;

        for (IntegerGraphVertex reachableNode: vertices.get(currentNode))
        {
            if (visitedNodes[reachableNode.vertex] != NODE_STATE_FINISHED_VISITING)
            {
                performDFS(reachableNode.vertex, visitedNodes);
            }
        }

        visitedNodes[currentNode] = NODE_STATE_FINISHED_VISITING;

        if (!cycleDetectionPathStack.empty())
        {
            cycleDetectionPathStack.pop();
        }
    }

    public List<Integer> getCycle()
    {
        while (!cycleDetectionPathStack.empty())
        {
            cycleDetectionPathStack.pop();
        }

        cycleDetectionPath.clear();

        cycleDetected = false;

        int[] visited = new int[vertices.size()];

        for (int i = 0; i < vertices.size(); i++)
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

    public void addEdge(int source, int target)
    {
        addEdge(source, target, null);
    }

    public void addEdge(int source, int target, String label)
    {
        if (!vertices.containsKey(source))
        {
            vertices.put(source, new ArrayList<>());

            updateNodeIndexRange(source);
        }

        if (!vertices.containsKey(target))
        {
            vertices.put(target, new ArrayList<>());

            updateNodeIndexRange(target);
        }

        IntegerGraphVertex integerGraphVertex = new IntegerGraphVertex();
        integerGraphVertex.vertex = target;
        integerGraphVertex.label = label;

        vertices.get(source).add(integerGraphVertex);
    }

    private void updateNodeIndexRange(int nodeIndex)
    {
        if ((minNodeValue == -1) || (minNodeValue > nodeIndex))
        {
            minNodeValue = nodeIndex;
        }

        if ((maxNodeValue == -1) || (maxNodeValue < nodeIndex))
        {
            maxNodeValue = nodeIndex;
        }
    }

    public void removeEdge(int source, int target)
    {
        if (vertices.containsKey(source))
        {
            vertices.get(source).remove(target);
        }
    }

    public void removeNode(int node)
    {
        vertices.remove(node);

        for (Map.Entry<Integer, List<IntegerGraphVertex>> entry : vertices.entrySet())
        {
            List<IntegerGraphVertex> removedEntries = new ArrayList<>();

            for (IntegerGraphVertex integerGraphVertex : entry.getValue())
            {
                if (integerGraphVertex.vertex == node)
                {
                    removedEntries.add(integerGraphVertex);
                }
            }

            entry.getValue().removeAll(removedEntries);
        }
    }

    private void dfs(int start)
    {
        boolean[] isVisited = new boolean[vertices.size()];

        dfsRecursive(start, isVisited);
    }

    private void dfsRecursive(int current, boolean[] isVisited)
    {
        isVisited[current] = true;
        visit(current);

        for (IntegerGraphVertex dest : vertices.get(current))
        {
            if (!isVisited[dest.vertex])
            {
                dfsRecursive(dest.vertex, isVisited);
            }
        }
    }

    private void topologicalSortUtil(int v, boolean[] visited, Stack<Integer> stack)
    {
        visited[v] = true;

        int i;

        for (IntegerGraphVertex dest : vertices.get(v))
        {
            i = dest.vertex;

            if (!visited[i])
            {
                topologicalSortUtil(i, visited, stack);
            }
        }

        stack.push(v);
    }

    public List<Integer> topologicalSort()
    {
        List<Integer> sort = new ArrayList<>();

        Stack<Integer> stack = new Stack<>();

        boolean[] visited = new boolean[vertices.size()];

        Arrays.fill(visited, false);

        for (int i = 0; i < visited.length; i++)
        {
            if (!visited[i])
            {
                topologicalSortUtil(i, visited, stack);
            }
        }

        while (!stack.empty())
        {
            sort.add(stack.pop());
        }

        return sort;
    }

    private void visit(int value)
    {
        //System.out.print(" " + value);
    }

    public Map<Integer, List<Integer>> getEdges()
    {
        Map<Integer, List<Integer>> edges = new HashMap<>();

        for (Map.Entry<Integer, List<IntegerGraphVertex>> entry : vertices.entrySet())
        {
            List<Integer> destinations = new ArrayList<>();

            for (IntegerGraphVertex destination : entry.getValue())
            {
                destinations.add(destination.vertex);
            }

            edges.put(entry.getKey(), destinations);
        }

        return edges;
    }

    public List<Integer> getOutgoingNodeIndexes(Integer nodeIndex, String label)
    {
        return getOutgoingNodeIndexes(getByIndex(nodeIndex), label);
    }

    public List<Integer> getOutgoingNodeIndexes(IntegerGraphVertex vertex, String label)
    {
        List<Integer> outgoingNodeIndexes = new ArrayList<>();

        if (this.vertices.containsKey(vertex.vertex))
        {
            for (IntegerGraphVertex current : this.vertices.get(vertex.vertex))
            {
                if ((GraphUtils.toDef(label).equals("")) || (GraphUtils.equalStrings(label, current.label)))
                {
                    outgoingNodeIndexes.add(current.vertex);
                }
            }
        }

        return outgoingNodeIndexes;
    }

    public List<Integer> getIncomingNodeIndexes(Integer vertexIndex, String label)
    {
        List<Integer> incomingNodeIndexes = new ArrayList<>();

        for (Map.Entry<Integer, List<IntegerGraphVertex>> current : this.vertices.entrySet())
        {
            for (IntegerGraphVertex currentVertex : current.getValue())
            {
                if ((currentVertex.vertex == vertexIndex) && ((GraphUtils.toDef(label).equals("")) || (GraphUtils.equalStrings(label, currentVertex.label))))
                {
                    incomingNodeIndexes.add(current.getKey());
                }
            }
        }

        return incomingNodeIndexes;
    }

    public boolean isConnected(IntegerGraphVertex start, IntegerGraphVertex target, String label)
    {
        return getOutgoingNodeIndexes(start, label).contains(target.vertex);
    }

    public boolean isConnected(int start, int target, String label)
    {
        IntegerGraphVertex startNode = new IntegerGraphVertex();
        startNode.vertex = start;

        IntegerGraphVertex targetNode = new IntegerGraphVertex();
        targetNode.vertex = target;

        return isConnected(startNode, targetNode, label);
    }

    public List<Integer> getStartNodes()
    {
        List<Integer> startNodes = new ArrayList<>();

        for (Map.Entry<Integer, List<IntegerGraphVertex>> currentVertex : this.vertices.entrySet())
        {
            boolean hasIngoingEdge = false;

            for (Map.Entry<Integer, List<IntegerGraphVertex>> testVertex : this.vertices.entrySet())
            {
                if (hasIngoingEdge)
                {
                    break;
                }

                if (Objects.equals(currentVertex.getKey(), testVertex.getKey()))
                {
                    continue;
                }

                for (IntegerGraphVertex target : testVertex.getValue())
                {
                    if (target.vertex == currentVertex.getKey())
                    {
                        hasIngoingEdge = true;

                        break;
                    }
                }
            }

            if (!hasIngoingEdge)
            {
                startNodes.add(currentVertex.getKey());
            }
        }

        return startNodes;
    }

    public List<Integer> getEndNodes()
    {
        List<Integer> endNodes = new ArrayList<>();

        for (Map.Entry<Integer, List<IntegerGraphVertex>> currentVertex : this.vertices.entrySet())
        {
            if (currentVertex.getValue().size() == 0)
            {
                endNodes.add(currentVertex.getKey());
            }
        }

        return endNodes;
    }

    public List<Integer> getExclusivePathFromNode(Integer vertexIndex, boolean allowMultipleSubPaths)
    {
        return getExclusivePathFromNode(getByIndex(vertexIndex), allowMultipleSubPaths, null);
    }

    private List<Integer> getExclusivePathFromNode(IntegerGraphVertex vertex, boolean allowMultipleSubPaths, List<Integer> visitedOutgoingNodes)
    {
        if ((allowMultipleSubPaths) && (visitedOutgoingNodes == null))
        {
            visitedOutgoingNodes = new ArrayList<>();
        }

        List<Integer> path = new ArrayList<>();

        IntegerGraphVertex currentVertex = vertex;

        while (true)
        {
            List<Integer> outgoingNodeIndexes = getOutgoingNodeIndexes(currentVertex, null);

            int outgoingNodeIndexesCount = outgoingNodeIndexes.size();

            if ((outgoingNodeIndexesCount < 1) || ((!allowMultipleSubPaths) && (outgoingNodeIndexesCount != 1)))
            {
                if (path.size() > 0)
                {
                    path.remove(path.size() - 1);
                }

                return path;
            }
            else if ((allowMultipleSubPaths) && (outgoingNodeIndexesCount > 1))
            {
                for (Integer outgoingIndex : outgoingNodeIndexes)
                {
                    List<Integer> result = getExclusivePathFromNode(getByIndex(outgoingIndex), true, visitedOutgoingNodes);

                    path.addAll(result);
                    visitedOutgoingNodes.addAll(result);
                }

                return path;
            }
            else if (outgoingNodeIndexesCount == 1)
            {
                for (Integer outgoingIndex : outgoingNodeIndexes)
                {
                    List<Integer> thisIncomingIndexes = getIncomingNodeIndexes(outgoingIndex, null);

                    if (thisIncomingIndexes.size() == 1)
                    {
                        path.add(outgoingIndex);

                        if (visitedOutgoingNodes != null)
                        {
                            visitedOutgoingNodes.add(outgoingIndex);
                        }

                        currentVertex = getByIndex(outgoingIndex);
                    }
                    else if ((allowMultipleSubPaths))
                    {
                        boolean valid = true;

                        for (Integer incoming : thisIncomingIndexes)
                        {
                            if (!visitedOutgoingNodes.contains(incoming))
                            {
                                valid = false;
                                break;
                            }
                        }

                        if (valid)
                        {
                            path.add(outgoingIndex);
                            visitedOutgoingNodes.add(outgoingIndex);

                            currentVertex = getByIndex(outgoingIndex);
                        }
                        else
                        {
                            return path;
                        }
                    }
                    else
                    {
                        return path;
                    }
                }
            }
        }
    }

    public static IntegerGraphVertex getByIndex(int vertex)
    {
        IntegerGraphVertex integerGraphVertex = new IntegerGraphVertex();
        integerGraphVertex.vertex = vertex;
        integerGraphVertex.label = null;
        return integerGraphVertex;
    }

    public List<Integer> getNodeIndexes()
    {
        List<Integer> nodeIndexes = new ArrayList<>();

        for (Map.Entry<Integer, List<IntegerGraphVertex>> entry : vertices.entrySet())
        {
            nodeIndexes.add(entry.getKey());
        }

        return nodeIndexes;
    }

    public List<List<Integer>> getPathsByLabel(int startNode, String label)
    {
        List<Integer> endNodes = getEndNodes();

        List<List<Integer>> paths = new ArrayList<>();

        for (Integer endNode : endNodes)
        {
            paths.addAll(getPathsByLabel(startNode, endNode, label));
        }

        return paths;
    }

    public List<List<Integer>> getPathsByLabel(int startNode, int endNode, String label)
    {
        List<List<Integer>> paths = new ArrayList<>();
        boolean[] isVisited = new boolean[maxNodeValue + 1];
        ArrayList<Integer> currentPath = new ArrayList<>();

        currentPath.add(startNode);

        getPathsByLabel(paths, startNode, endNode, label, isVisited, currentPath);

        return paths;
    }

    private void getPathsByLabel(List<List<Integer>> paths, Integer startNode, Integer endNode, String label, boolean[] isVisited, List<Integer> currentPath)
    {
        if (startNode.equals(endNode))
        {
            paths.add(new ArrayList<>(currentPath));
            return;
        }

        isVisited[startNode] = true;

        for (IntegerGraphVertex currentVertex : vertices.get(startNode))
        {
            if (!isVisited[currentVertex.vertex])
            {
                if ((currentVertex.label == null) || (currentVertex.label.isEmpty()) || (currentVertex.label.equals(label)))
                {
                    currentPath.add(currentVertex.vertex);
                    getPathsByLabel(paths, currentVertex.vertex, endNode, label, isVisited, currentPath);
                    currentPath.remove((Integer) currentVertex.vertex);
                }
            }
        }

        isVisited[startNode] = false;
    }
}
