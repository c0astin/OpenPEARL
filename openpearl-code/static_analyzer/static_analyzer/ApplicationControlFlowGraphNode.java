package static_analyzer;

public class ApplicationControlFlowGraphNode extends GraphNode
{
    public DeadlockOperation deadlockOperation;

    public ApplicationControlFlowGraphNode(String id)
    {
        this(id, "");
    }

    public ApplicationControlFlowGraphNode(String id, String label)
    {
        super(id, label);
    }
}
