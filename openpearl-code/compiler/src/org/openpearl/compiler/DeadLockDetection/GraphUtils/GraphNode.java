// hfujk

package org.openpearl.compiler.DeadLockDetection.GraphUtils;

public class GraphNode implements IGraphNode
{
    protected String id = "";
    protected String label = "";
    protected String level = "";

    public GraphNode(String id)
    {
        this(id, "");
    }

    public GraphNode(String id, String label)
    {
        this.id = id;
        this.label = label;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public void setLabel(String label)
    {
        this.label = label;
    }

    @Override
    public void setLevel(String level)
    {
        this.level = level;
    }

    @Override
    public String getLevel()
    {
        return level;
    }

    @Override
    public String getId()
    {
        return id;
    }

    public static GraphNode copy(GraphNode graphNode)
    {
        return new GraphNode(graphNode.getId(), graphNode.getLabel());
    }

    @Override
    public boolean isSame(IGraphNode graphNode, boolean compareLabels)
    {
        if (!getId().equals(graphNode.getId()))
        {
            return false;
        }

        if (!compareLabels)
        {
            return true;
        }

        if (label == null)
        {
            return (graphNode.getLabel() == null);
        }
        else
        {
            return (label.equals(graphNode.getLabel()));
        }
    }

    @Override
    public IGraphNode copy()
    {
        return new GraphNode(id, label);
    }
}
