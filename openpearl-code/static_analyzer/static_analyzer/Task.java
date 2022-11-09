package static_analyzer;

import static_analyzer.ControlFlowGraphEntities.AbstractTaskControl;

import java.util.ArrayList;
import java.util.List;

public class Task
{
    List<Edge> taskEdges = new ArrayList<>();

    String startNode = "";
    String endNode = "";

    public String taskIdentifier = "";
    public boolean runAtStartup = false;

    List<AbstractTaskControl> taskControlStatements = new ArrayList<>();

    boolean global = false;
}
