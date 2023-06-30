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

// 2023-03-06 (rm) the filename is set by default in the new export of the deadlock-CFG        
//        if ((codeFilename == null) || (codeFilename.isEmpty()))
//        {
//        	System.err.println("codeFilename not given");
////            codeFilename = StaticAnalyzer.pearlFilename + ".prl";
//        }

        statement = statement.replace(";", "");
    }
}
