package static_analyzer;

import static_analyzer.ControlFlowGraphEntities.FlowControl;

import java.util.*;

public class ApplicationControlFlowGraph
{
    List<Subgraph> subgraphs = new ArrayList<>();
    Subgraph root = new Subgraph();
    public Map<String, Task> tasks = new HashMap<>();
    public Map<String, DeadlockResource> deadlockResources = new HashMap<>();
    Map<String, Object> objectsByNodeId = new HashMap<>();

    List<Edge> edges = new ArrayList<>();

    public String generateDotFile()
    {
        DotWriter dotWriter = new DotWriter();

        dotWriter.writeLine("digraph G {");

        for (Subgraph subgraph : subgraphs)
        {
            dotWriter.writeLine("subgraph " + dotWriter.getClusterName() + " {");
            dotWriter.writeLine("label = \"" + subgraph.label + "\"");
            for (IGraphNode node : subgraph.nodes) {
                dotWriter.writeLine(node.getId() + " [ label=\"" + node.getLabel() + "\" ]");
            }

            writeEdges(dotWriter, subgraph.edges);

            dotWriter.writeLine("}");
        }

        writeEdges(dotWriter, root.edges);

        dotWriter.writeLine("}");

        return dotWriter.getContent();
    }

    private void writeEdges(DotWriter dotWriter, List<Edge> edges)
    {
        for (Edge edge : edges)
        {
            String lineContent = getNodeLabel(edge.source) + " -> " + getNodeLabel(edge.target);

            if ((edge.label != null) && (!edge.label.isEmpty()))
            {
                lineContent += " [ label=\"" + edge.label + "\" ]";
            }

            dotWriter.writeLine(lineContent);
        }
    }

    public String getNodeLabel(IGraphNode graphNode)
    {
        if (graphNode instanceof ApplicationControlFlowGraphNode)
        {
            ApplicationControlFlowGraphNode applicationGraphNode = (ApplicationControlFlowGraphNode) graphNode;

            if ((applicationGraphNode.getLabel() != null) && (applicationGraphNode.getLabel().length() > 0))
            {
                return applicationGraphNode.getLabel();
            }

            if (applicationGraphNode.deadlockOperation != null)
            {
                return applicationGraphNode.deadlockOperation.getLabel();
            }
        }
        else if (graphNode instanceof FlowControl)
        {
            FlowControl flowControl = (FlowControl) graphNode;

            return flowControl.getNodeLabel();
        }

        return graphNode.getId();
    }

    public void reduce()
    {
        List<String> allowedStartNodeTypePatterns = Arrays.asList("applicationStart", /*"Start_%",*/ "End_%", "%_ACTIVATE_%");

        Graph<IGraphNode> graph = new Graph<>();

        for (Subgraph subgraph : subgraphs)
        {
            for (Edge edge : subgraph.edges)
            {
                graph.addEdge(edge);
            }
        }

        for (Edge edge : root.edges)
        {
            graph.addEdge(edge);
        }

        List<IGraphNode> startNodes = graph.getStartNodes();

        for (IGraphNode startNode : startNodes)
        {
            if (!Utils.matchesPatternInList(allowedStartNodeTypePatterns, startNode.getId(), "%"))
            {
                List<IGraphNode> pathNodes = graph.getExclusivePathFromNode(startNode, true);

                for (IGraphNode pathNode : pathNodes)
                {
                    graph.removeNode(pathNode);
                }

                graph.removeNode(startNode);
            }
        }

        for (Subgraph subgraph : subgraphs)
        {
            removeDifferenceInSublist(subgraph.edges, graph);
        }

        removeDifferenceInSublist(root.edges, graph);

        edges.clear();

        for (Subgraph subgraph : subgraphs)
        {
            edges.addAll(subgraph.edges);
        }

        edges.addAll(root.edges);
    }

    private void removeDifferenceInSublist(List<Edge> subList, Graph<IGraphNode> graph)
    {
        List<Edge> removedEdges = new ArrayList<>();

        for (Edge edge : subList)
        {
            if (!graph.containsEdge(edge.source, edge.target, edge.label))
            {
                removedEdges.add(edge);
            }
        }

        subList.removeAll(removedEdges);
    }
}
