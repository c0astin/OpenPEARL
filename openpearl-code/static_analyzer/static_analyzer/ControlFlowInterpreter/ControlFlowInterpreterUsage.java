package static_analyzer.ControlFlowInterpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ControlFlowInterpreterUsage
{
    public static final String LEVEL_MODULE = "module";
    public static final String LEVEL_TASK = "task";
    public static final String LEVEL_FLOW = "flow";
    public static final String LEVEL_ALL = "all";

    public String resourceType;
    public String requestAction;
    public String releaseAction;

    public Map<String, ControlFlowInterpreterUsageEntry> entries = new HashMap<>();

    public ControlFlowInterpreterUsage(String resourceType, String requestAction, String releaseAction)
    {
        this.resourceType = resourceType;
        this.requestAction = requestAction;
        this.releaseAction = releaseAction;

        entries.put(LEVEL_MODULE, new ControlFlowInterpreterUsageEntry(LEVEL_MODULE));
        entries.put(LEVEL_TASK, new ControlFlowInterpreterUsageEntry(LEVEL_TASK));
        entries.put(LEVEL_FLOW, new ControlFlowInterpreterUsageEntry(LEVEL_FLOW));
        entries.put(LEVEL_ALL, new ControlFlowInterpreterUsageEntry(LEVEL_ALL));
    }

    public void reset()
    {
        for (Map.Entry<String, ControlFlowInterpreterUsageEntry> entry : entries.entrySet())
        {
            entry.getValue().reset();
        }
    }

    public void resetLevel(String level)
    {
        if ((entries.containsKey(level)) && (entries.get(level) != null))
        {
            entries.get(level).reset();
        }
    }

    public void resetLevels(String[] levels)
    {
        for (String level : levels)
        {
            resetLevel(level);
        }
    }

    public Set<String> getRequestedResources(String level)
    {
        Set<String> result = new TreeSet<>();

        if ((entries.containsKey(level)) && (entries.get(level) != null))
        {
            return entries.get(level).getRequestedResources();
        }

        return result;
    }

    public Set<String> getReleasedResources(String level)
    {
        Set<String> result = new TreeSet<>();

        if ((entries.containsKey(level)) && (entries.get(level) != null))
        {
            return entries.get(level).getReleasedResources();
        }

        return result;
    }

    public void addRequestedResource(String level, String resourceIdentifier)
    {
        if ((entries.containsKey(level)) && (entries.get(level) != null))
        {
            entries.get(level).addRequestedResource(resourceIdentifier);
        }
    }

    public void addReleasedResource(String level, String resourceIdentifier)
    {
        if ((entries.containsKey(level)) && (entries.get(level) != null))
        {
            entries.get(level).addReleasedResource(resourceIdentifier);
        }
    }
}
