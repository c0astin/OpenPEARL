package static_analyzer.ControlFlowInterpreter;

import static_analyzer.DeadlockResource;

public class ControlFlowInterpreterResource
{
    DeadlockResource deadlockResource = null;
    int presetValue = 0;
    int currentOrder = -1;

    public String dump()
    {
        return deadlockResource.resourceType + " " + deadlockResource.resourceIdentifier + " = " + presetValue;
    }

    public String dumpOrder()
    {
        return deadlockResource.resourceType + " " + deadlockResource.resourceIdentifier + " = " + presetValue + " (Order: " + currentOrder + ")";
    }
}
