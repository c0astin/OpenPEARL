package static_analyzer.ControlFlowInterpreter;

import static_analyzer.AnalyzerMessage.Message;
import static_analyzer.ApplicationControlFlowGraphNode;
import static_analyzer.DeadlockOperation;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowInterpreterPath
{
    List<ApplicationControlFlowGraphNode> path = new ArrayList<>();

    boolean correct = false;

    boolean notAllRequestedSema = false;
    boolean notAllReleasedSema = false;
    boolean notAllReservedBolt = false;
    boolean notAllFreedBolt = false;
    boolean notAllEnteredBolt = false;
    boolean notAllLeavedBolt = false;

    List<DeadlockOperation> performedDeadlockOperations = new ArrayList<>();
    List<Message> messageStackReferences = new ArrayList<>();

    public void apply()
    {
        if (path == null)
        {
            path = new ArrayList<>();
        }

        performedDeadlockOperations.clear();

        for (ApplicationControlFlowGraphNode node : path)
        {
            if (node.deadlockOperation != null)
            {
                performedDeadlockOperations.add(node.deadlockOperation);
            }
        }
    }
}
