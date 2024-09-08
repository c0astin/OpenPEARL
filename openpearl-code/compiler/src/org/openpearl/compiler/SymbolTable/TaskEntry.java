/*
 * [The "BSD license"]
 * *  Copyright (c) 2012-2021 Marcel Schaible
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

package org.openpearl.compiler.SymbolTable;

import org.openpearl.compiler.Defaults;
import org.openpearl.compiler.Log;
import org.openpearl.compiler.OpenPearlParser;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.ControlFlowGraph;

public class TaskEntry extends SymbolTableEntry {

    private Boolean m_isMain;
    private OpenPearlParser.PriorityContext m_priority;
    private ControlFlowGraph m_controlFlowGraph;

    public TaskEntry() {}
    
    /**
     * task specification 
     * @param name name of task
     * @param ctx context in AST
     */
    public TaskEntry(String name, OpenPearlParser.IdentifierContext ctx) {
        super(name);
        this.m_ctx = ctx;
        m_controlFlowGraph = null;
        m_isMain = false;
    }

    public TaskEntry(String name, OpenPearlParser.PriorityContext priority, Boolean isMain,
             OpenPearlParser.TaskDeclarationContext ctx, SymbolTable scope) {
        super(name);

        m_controlFlowGraph = null;
        m_priority = priority;
        m_isMain = isMain;
        Log.warn("TaskEntry@64: global treatment still incomplete");
        

        this.m_ctx = ctx.nameOfModuleTaskProc();
        this.scope = scope;
    }

    public String toString(int level) {
        String taskPriority;
        if (m_priority != null) {
            taskPriority = m_priority.expression().getText();
        } else {
            taskPriority = Integer.toString(Defaults.DEFAULT_TASK_PRIORITY);

        }

        return indentString(level) + super.toString(level) + "task"
                + (isMain()? " MAIN" : "" )
                + (!isSpecified()? " priority(" + taskPriority + ")" : "" )
                + scopeString(level);
    }

    protected String scopeString(int m_level) {
        return scope == null ? "" : "\n" + scope.toString(m_level);
    }

    public void setControlFlowGraph(ControlFlowGraph cfg) {
        m_controlFlowGraph = cfg;
    }
    
    public ControlFlowGraph getControlFlowGraph() {
        return m_controlFlowGraph;
    }

    public SymbolTable scope;

    public Boolean isMain() {
        return m_isMain;
    }

}
