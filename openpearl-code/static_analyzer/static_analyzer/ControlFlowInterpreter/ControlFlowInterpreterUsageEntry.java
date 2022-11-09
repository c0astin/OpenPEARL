package static_analyzer.ControlFlowInterpreter;

import java.util.Set;
import java.util.TreeSet;

public class ControlFlowInterpreterUsageEntry
{
    private String level;
    private final Set<String> requestedResources = new TreeSet<>();
    private final Set<String> releasedResources = new TreeSet<>();

    public ControlFlowInterpreterUsageEntry(String level)
    {
        this.level = level;
    }

    public void reset()
    {
        requestedResources.clear();
        releasedResources.clear();
    }

    public Set<String> getRequestedResources()
    {
        return requestedResources;
    }

    public Set<String> getReleasedResources()
    {
        return releasedResources;
    }

    public void addRequestedResource(String resourceIdentifier)
    {
        requestedResources.add(resourceIdentifier);
    }

    public void addReleasedResource(String resourceIdentifier)
    {
        releasedResources.add(resourceIdentifier);
    }
}
