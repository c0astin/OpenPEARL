// hfujk

package org.openpearl.compiler.DeadLockDetection.GraphUtils;

public class Edge
{
    public IGraphNode source;
    public IGraphNode target;
    public String label = "";

    public Edge(IGraphNode source, IGraphNode target)
    {
        this(source, target, "");
    }

    public Edge(IGraphNode source, IGraphNode target, String label)
    {
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public static Edge copy(Edge edge)
    {
        return new Edge(edge.source.copy(), edge.target.copy(), edge.label);
    }

    public boolean isSame(Edge edge, boolean compareNodeLabels)
    {
        if ((!source.isSame(edge.source, compareNodeLabels)) || (!target.isSame(edge.target, compareNodeLabels)))
        {
            return false;
        }

        if (label == null)
        {
            return (edge.label == null);
        }
        else
        {
            return (label.equals(edge.label));
        }
    }

    public String dump()
    {
        return source.getId() + " -> " + target.getId();
    }
}
