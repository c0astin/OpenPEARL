/*
 * [A "BSD license"]
 * *  Copyright (c) 2024-2021 Rainer Mueller
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

package org.openpearl.compiler.SemanticAnalysis;

import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.*;
import org.openpearl.compiler.OpenPearlParser.StatementContext;
import org.openpearl.compiler.OpenPearlParser.Task_control_statementContext;
import org.openpearl.compiler.OpenPearlParser.Task_terminatingContext;
import org.openpearl.compiler.OpenPearlParser.Unlabeled_statementContext;
import org.openpearl.compiler.SymbolTable.SymbolTable;


public class CheckSignalSchedulingAndReactions extends OpenPearlBaseVisitor<Void>
implements OpenPearlVisitor<Void> {
    private boolean m_isInTask;
    private boolean m_OnForbidden;  // is set if we are in a BEGIN/END or REPEAT END 
    private boolean m_isInSignalReaction;

    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private AST m_ast;
    private ParserRuleContext m_taskOrProcContext;


    public CheckSignalSchedulingAndReactions(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_isInTask = false;
        m_isInSignalReaction = false;
        m_ast = ast;

        Log.info("Semantic Check: Check SIGNAL scheduling and reactions");
    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable =
                ((org.openpearl.compiler.SymbolTable.ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        m_isInTask = false;
        m_OnForbidden = false;
        ASTAttribute attr = m_ast.lookup(ctx);
        if (attr == null) {
            attr = new ASTAttribute(new TypeProcedure(null, null) );
        }
        m_ast.put(ctx, attr);
        m_taskOrProcContext = ctx;        
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        m_isInTask = true;
        m_OnForbidden = false;
        ASTAttribute attr = m_ast.lookup(ctx);
        if (attr == null) {
            attr = new ASTAttribute(new TypeTask());
        }
        m_ast.put(ctx, attr);
        m_taskOrProcContext = ctx;

        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitUnlabeled_statement(OpenPearlParser.Unlabeled_statementContext ctx) {
        if (m_isInSignalReaction) {
        }
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        boolean saveOnForbidden = m_OnForbidden;
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        m_OnForbidden = true;
        visitChildren(ctx);
        if (m_isInSignalReaction) {
            boolean endOfHandlerIsOk = false;
            // check last statement of signal handler to BE GOTO, RETURN, INDUCE, TERMINATE
            // this is easier to implement as to enshure that all paths of control
            // it would be better to parse the control flow graph of the handler to
            // enshure that no path exist with a singakTerminationStatement
            List<StatementContext> stmts = ctx.statement();
            int last = stmts.size();
            if (last > 0) {
                OpenPearlParser.StatementContext lastStmnt = stmts.get(last - 1);
                Unlabeled_statementContext checkMe =lastStmnt.unlabeled_statement();
                if ( checkMe != null) {
                    if (checkMe.gotoStatement() != null || 
                            checkMe.induceStatement() != null) {
                        endOfHandlerIsOk = true;
                    } else if (checkMe.returnStatement() != null ) {
                        endOfHandlerIsOk = true; // avoid duplicate error message
                        if (m_isInTask) {
                            ErrorStack.add(checkMe.returnStatement(),"signal handler", "RETURN is not allowed is a signal reaction of a task");
                        }
                    } else if (checkMe.realtime_statement() != null) {
                        Task_control_statementContext tcs = checkMe.realtime_statement().task_control_statement();
                        if ( tcs != null && tcs.task_terminating() != null) {
                            if (tcs.task_terminating().name() != null) {
                                ErrorStack.add(tcs.task_terminating().name(),
                                        "signal handler","TERMINATE as signal termination statement forbids a name for task");
                                endOfHandlerIsOk = true; // avoid duplicate error on same statement 
                            } else {
                                endOfHandlerIsOk = true;  // TERMINATE without task name
                            }

                        }
                    }
                }

                if (!endOfHandlerIsOk) {
                    ErrorStack.add(lastStmnt,"signal handler","must end with RETURN, TERMINATE, INDUCE or GOTO");
                }
            } else {
                ErrorStack.add(ctx.endOfBlockLoopProcOrTask(),"signal handler","must end with RETURN, TERMINATE, INDUCE or GOTO");
            }
        }

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        m_OnForbidden = saveOnForbidden;
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        boolean saveOnForbidden = m_OnForbidden;
        m_OnForbidden = true;
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        m_OnForbidden = saveOnForbidden;
        return null;
    }

    @Override
    public Void visitSchedulingSignalReaction(OpenPearlParser.SchedulingSignalReactionContext ctx) {

        m_OnForbidden = true;

        //ErrorStack.note(ctx, "Check Level of ON", "level="+m_currentSymbolTable.m_level);
        if (m_currentSymbolTable.m_level != 2) {
            ErrorStack.add(ctx, "illegal level for ON statement", "only allowed in TASK or PROC level");
        }

        visitChildren(ctx);

        if (ctx.signalReaction() == null) {
            ErrorStack.warn(ctx,"deprecated","the usage of error variables is dangerous");
            ASTAttribute attr = m_ast.lookup(m_taskOrProcContext);
            attr.setOnSignalSetOnlyRst();
            int x=11;
        }
        return null;
    }


    @Override
    public Void visitSignalReaction(OpenPearlParser.SignalReactionContext ctx) {
        m_isInSignalReaction = true;
        visitChildren(ctx);
        m_isInSignalReaction = false;
        return null;
    }


    @Override
    public Void visitSignalFinalStatement(OpenPearlParser.SignalFinalStatementContext ctx) {
        if (ctx.task_terminating() != null) {
            // no task name may be provided
            if (ctx.task_terminating().name() != null) {
                // no name may be specified
                ErrorStack.add(ctx.task_terminating().name(),"signal handler","TERMINATE as signal termination statement forbids a name for task");
            }
        }

        if (ctx.returnStatement() != null) {
            // must be in PROC!
            if (m_isInTask) {
                ErrorStack.add(ctx.returnStatement(),"signal handler", "RETURN is not allowed is a signal reaction of a task");
            }
        }
        // nothing to do for INDUCE and GOTO

        return null;
    }

    @Override
    public Void visitInduceStatement(OpenPearlParser.InduceStatementContext ctx) {
        visitChildren(ctx);
        if (!m_isInSignalReaction) {
            if (ctx.name()== null) {
                ErrorStack.add(ctx,"missing name of signal","required outside of signal reactions");
            }
        }

        return null;
    }


}
