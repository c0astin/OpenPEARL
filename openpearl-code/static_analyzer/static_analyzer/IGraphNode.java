package static_analyzer;

public interface IGraphNode
{
    String getId();
    String getLabel();
    String getInfo();
    void setLabel(String label);
    boolean isSame(IGraphNode graphNode, boolean compareLabels);
    IGraphNode copy();
}
