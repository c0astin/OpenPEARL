package static_analyzer;

public class GraphNode implements IGraphNode
{
    protected String id = "";
    protected String label = "";

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

    @Override
    public String getInfo()
    {
        if ((label != null) && (!label.isEmpty()) && (!id.equals(label)))
        {
            return id + " " + label;
        }

        return id;
    }
}
