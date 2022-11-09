package static_analyzer.ControlFlowGraphEntities;

public class FlowControl extends AbstractControlFlowGraphEntity
{
    public String type = "";
    public String statement = "";

    public String getNodeLabel()
    {
        return "n_" + nodeId.replace("node", "") + "_" + type.trim().replace(" ", "_");
    }
}
