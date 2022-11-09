package static_analyzer.ControlFlowInterpreter;

import static_analyzer.DeadlockOperation;
import static_analyzer.Task;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowInterpreterTask
{
    Task task = null;
    int currentOrderIndex = 0;

    List<ControlFlowInterpreterResource> allocatedResources = new ArrayList<>();

    List<DeadlockOperation> performedDeadlockOperations = new ArrayList<>();

    List<ControlFlowInterpreterTaskResourceAllocation> allocations = new ArrayList<>();
}
