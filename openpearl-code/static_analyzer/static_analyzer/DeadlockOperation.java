package static_analyzer;

import java.util.ArrayList;
import java.util.List;

public class DeadlockOperation
{
    public static String RESOURCE_TYPE_SEMAPHORE = "SEMA";
    public static String ACTION_TYPE_REQUEST = "REQUEST";
    public static String ACTION_TYPE_RELEASE = "RELEASE";

    public static String RESOURCE_TYPE_BOLT = "BOLT";
    public static String ACTION_TYPE_ENTER = "ENTER";
    public static String ACTION_TYPE_LEAVE = "LEAVE";
    public static String ACTION_TYPE_RESERVE = "RESERVE";
    public static String ACTION_TYPE_FREE = "FREE";

    public String taskIdentifier = "";
    public List<String> resourceIdentifiers = new ArrayList<>();
    public String resourcesType = "";
    public String actionType = "";
    public String codeFilename = "";
    public int codeLineNumber = 0;

    public String id = "";

    public String getLabel()
    {
        return "l_" + codeLineNumber + "_" + actionType + "_" + resourcesType + "_" + Utils.listToString(resourceIdentifiers, "_");
    }

    public String getStatement(boolean withCodePosition)
    {
        if (withCodePosition)
        {
            return codeFilename + ":" + codeLineNumber + ": " + actionType + " " + resourcesType + " " + Utils.listToString(resourceIdentifiers, ", ");
        }

        return actionType + " " + resourcesType + " " + Utils.listToString(resourceIdentifiers, ", ");
    }

    public String getCodePosition()
    {
        return codeFilename + ":" + codeLineNumber;
    }
}
