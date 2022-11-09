// hfujk

package org.openpearl.compiler.DeadLockDetection.GraphUtils;

public interface IGraphNode
{
    String getId();
    String getLabel();
    void setLabel(String label);
    void setLevel(String level);
    String getLevel();
    boolean isSame(IGraphNode graphNode, boolean compareLabels);
    IGraphNode copy();
}
