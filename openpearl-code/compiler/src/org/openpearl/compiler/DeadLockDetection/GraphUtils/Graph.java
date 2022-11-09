// hfujk

package org.openpearl.compiler.DeadLockDetection.GraphUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph<T extends IGraphNode>
{
    private final IntegerGraph integerGraph = new IntegerGraph();

    private final Map<String, T> nodesById = new HashMap<>();
    private final List<String> idIndexes = new ArrayList<>();
    private int autoIncrementIndex = 0;

    private int addWithIndex(T node)
    {
        int result = -1;

        if (!nodesById.containsKey(node.getId()))
        {
            result = autoIncrementIndex;
            autoIncrementIndex++;
            nodesById.put(node.getId(), node);
            idIndexes.add(node.getId());
        }
        else
        {
            result = idIndexes.indexOf(node.getId());
        }

        return result;
    }

    private String getIdentifier(T node)
    {
        addWithIndex(node);

        return node.getId();
    }

    private int getIndex(T node)
    {
        return getIndex(getIdentifier(node));
    }

    private int getIndex(String nodeId)
    {
        if (nodeId == null)
        {
            return -1;
        }

        return idIndexes.indexOf(nodeId);
    }

    public T get(String nodeIdentifier)
    {
        if ((nodeIdentifier == null) || (!nodesById.containsKey(nodeIdentifier)))
        {
            return null;
        }

        return nodesById.get(nodeIdentifier);
    }

    public T get(int nodeIndex)
    {
        return get(idIndexes.get(nodeIndex));
    }

    public void addEdge(Edge edge)
    {
        addEdge((T)edge.source, (T)edge.target, edge.label);
    }

    public void addEdge(T sourceNode, T targetNode, String label)
    {
        int sourceIndex = addWithIndex(sourceNode);
        int targetIndex = addWithIndex(targetNode);

        integerGraph.addEdge(sourceIndex, targetIndex, label);
    }

    public void removeEdge(T sourceNode, T targetNode)
    {
        int sourceIndex = getIndex(sourceNode);
        int targetIndex = getIndex(targetNode);

        integerGraph.removeEdge(sourceIndex, targetIndex);
    }

    public void removeNode(T node)
    {
        int nodeIndex = getIndex(node);

        integerGraph.removeNode(nodeIndex);
    }

    public List<T> getNodesTopologicalSorted()
    {
        List<Integer> indexes = integerGraph.topologicalSort();

        List<T> nodes = new ArrayList<>();

        for (Integer nodeIndex : indexes)
        {
            nodes.add(get(nodeIndex));
        }

        return nodes;
    }

    public void printGraph()
    {
        for (Map.Entry<Integer, List<Integer>> nodeEntry : integerGraph.getEdges().entrySet())
        {
            String sourceNodeIndex = idIndexes.get(nodeEntry.getKey());

            T sourceNode = nodesById.get(sourceNodeIndex);

            System.out.print(sourceNode.getId() + " -> ");

            for (Integer targetNodeIndex : nodeEntry.getValue())
            {
                String targetNodeIdentifier = idIndexes.get(targetNodeIndex);

                T targetNode = nodesById.get(targetNodeIdentifier);

                System.out.print(targetNode.getId() + " " + targetNode.getLabel() + ", ");
            }

            System.out.println("");
        }
    }

    public List<List<T>> getPathsByLabel(T startNode, String label)
    {
        List<List<T>> resultPaths = new ArrayList<>();

        if (startNode == null)
        {
            return resultPaths;
        }

        if (!containsNode(startNode))
        {
            return resultPaths;
        }

        for (List<Integer> currentPath : integerGraph.getPathsByLabel(getIndex(startNode), label))
        {
            resultPaths.add(getNodesByIndex(currentPath));
        }

        return resultPaths;
    }

    public List<T> getCycle()
    {
        return getNodesByIndex(integerGraph.getCycle());
    }

    public List<T> getStartNodes()
    {
        return getNodesByIndex(integerGraph.getStartNodes());
    }

    public List<T> getExclusivePathFromNode(T node, boolean allowMultipleSubPaths)
    {
        return getNodesByIndex(integerGraph.getExclusivePathFromNode(getIndex(node), allowMultipleSubPaths));
    }

    private List<T> getNodesByIndex(List<Integer> nodeIndexes)
    {
        List<T> nodes = new ArrayList<>();

        if (nodeIndexes.size() == 0)
        {
            return nodes;
        }

        for (int index : nodeIndexes)
        {
            nodes.add(get(index));
        }

        return nodes;
    }

    public boolean containsEdge(T source, T target, String label)
    {
        int sourceIndex = getIndex(source);
        int targetIndex = getIndex(target);

        return integerGraph.isConnected(sourceIndex, targetIndex, label);
    }

    public boolean containsNode(T node)
    {
        if (node == null)
        {
            return false;
        }

        return nodesById.containsKey(node.getId());
    }
}
