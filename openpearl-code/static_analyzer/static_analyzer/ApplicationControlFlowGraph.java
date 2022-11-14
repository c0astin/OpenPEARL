/*
 * [A "BSD license"]
 *  Copyright (c) 2022 Jan Knoblauch
 *  
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
