// hfujk

package org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities;

import java.util.ArrayList;
import java.util.List;

public class DeadlockOperation extends AbstractControlFlowGraphEntity
{
    public static String RESOURCE_TYPE_SEMAPHORE = "SEMA";
    public static String ACTION_TYPE_REQUEST = "REQUEST";
    public static String ACTION_TYPE_RELEASE = "RELEASE";

    public static String RESOURCE_TYPE_BOLT = "BOLT";
    public static String ACTION_TYPE_ENTER = "ENTER";
    public static String ACTION_TYPE_LEAVE = "LEAVE";
    public static String ACTION_TYPE_RESERVE = "RESERVE";
    public static String ACTION_TYPE_FREE = "FREE";

    public static String CONTEXT_TYPE_TASK = "TASK";
    public static String CONTEXT_TYPE_PROCEDURE = "PROCEDURE";

    public String contextType = "";
    public String contextIdentifier = "";
    public String actionType = "";
    public String resourcesType = "";
    public List<String> resourceIdentifiers = new ArrayList<>();

    public List<String> executingTasks = new ArrayList<>();
}
