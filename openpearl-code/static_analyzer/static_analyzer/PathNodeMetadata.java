package static_analyzer;

import static_analyzer.ControlFlowGraphEntities.FlowControl;
import static_analyzer.ControlFlowGraphEntities.TaskControlActivate;
import static_analyzer.ControlFlowGraphEntities.UnknownStatement;

public class PathNodeMetadata
{
    public String className = "";
    public String codeFilename = "";
    public int codeLineNumber = 0;
    public String statement = "";

    DeadlockOperation deadlockOperation = null;
    Task task = null;
    DeadlockResource deadlockResource = null;
    Procedure procedure = null;
    TaskControlActivate taskControlActivate = null;
    FlowControl flowControl = null;
    UnknownStatement unknownStatement = null;

    public void apply()
    {
        if (deadlockOperation != null)
        {
            className = "DeadlockOperation";
            codeFilename = deadlockOperation.codeFilename;
            codeLineNumber = deadlockOperation.codeLineNumber;
            statement = deadlockOperation.getStatement(false);
        }
        else if (task != null)
        {
            className = "Task";
            statement = "Task " + task.taskIdentifier;
        }
        else if (deadlockResource != null)
        {
            className = "DeadlockResource";
            codeFilename = deadlockResource.codeFilename;
            codeLineNumber = deadlockResource.codeLineNumber;
            statement = deadlockResource.resourceType + " " + deadlockResource.resourceIdentifier + " (" + deadlockResource.presetValue + ")";
        }
        else if (procedure != null)
        {
            className = "Procedure";
            codeFilename = procedure.codeFilename;
            codeLineNumber = procedure.codeLineNumber;
            statement = "Procedure " + procedure.procedureName;
        }
        else if (taskControlActivate != null)
        {
            className = "TaskControlActivate";
            codeFilename = taskControlActivate.codeFilename;
            codeLineNumber = taskControlActivate.codeLineNumber;
            statement = "ACTIVATE " + taskControlActivate.action;
        }
        else if (flowControl != null)
        {
            className = "FlowControl";
            codeFilename = flowControl.codeFilename;
            codeLineNumber = flowControl.codeLineNumber;
            statement = (flowControl.type + " " + flowControl.statement).trim();
        }
        else if (unknownStatement != null)
        {
            className = "UnknownStatement";
            codeFilename = unknownStatement.codeFilename;
            codeLineNumber = unknownStatement.codeLineNumber;
            statement = unknownStatement.statement;
        }

        if ((codeFilename == null) || (codeFilename.isEmpty()))
        {
            codeFilename = StaticAnalyzer.pearlFilename + ".prl";
        }

        statement = statement.replace(";", "");
    }
}
