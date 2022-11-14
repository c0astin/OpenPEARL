/*
 * [A "BSD license"]
 *  Copyright (c) 2022 Jan Knoblauch
 *  
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
