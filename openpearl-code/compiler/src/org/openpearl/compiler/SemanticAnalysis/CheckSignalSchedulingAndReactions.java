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
import java.util.Vector;
import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.*;
import org.openpearl.compiler.OpenPearlParser.StatementContext;
import org.openpearl.compiler.OpenPearlParser.Task_control_statementContext;
import org.openpearl.compiler.OpenPearlParser.Unlabeled_statementContext;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.ControlFlowGraph;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.ControlFlowGraphNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.PseudoNode;
import org.openpearl.compiler.SymbolTable.ProcedureEntry;
import org.openpearl.compiler.SymbolTable.TaskEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;


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
        SymbolTable symbolTable;
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        symbolTable = this.m_currentSymbolTable;
        
        // step up one level to get the symbol table with the procedure declaration
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend(); 
        String name = ctx.nameOfModuleTaskProc().ID().toString();
        SymbolTableEntry entry = this.m_currentSymbolTable.lookup(name);

        
        // switch back to the symbol table of the procedure
        this.m_currentSymbolTable = symbolTable;
                
        m_isInTask = false;
        m_OnForbidden = false;
        ASTAttribute attr = m_ast.lookup(ctx);
        if (attr == null) {
            attr = new ASTAttribute(((ProcedureEntry)entry).getType(), entry );
        }
        
        m_ast.put(ctx, attr);
        m_taskOrProcContext = ctx;        
        visitChildren(ctx);
        
        // check signal handler for proper termination
        ControlFlowGraph cfg = ((ProcedureEntry)entry).getControlFlowGraph();
        checkAllSignalHandlers(cfg);

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }
    
    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        SymbolTable symbolTable;
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        symbolTable = this.m_currentSymbolTable;
        
        // step up one level to get the symbol table with the procedure declaration
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend(); 
        String name = ctx.nameOfModuleTaskProc().ID().toString();
        SymbolTableEntry entry = this.m_currentSymbolTable.lookup(name);
        
        // switch back to the symbol table of the procedure
        this.m_currentSymbolTable = symbolTable;

        m_isInTask = true;
        m_OnForbidden = false;
        ASTAttribute attr = m_ast.lookup(ctx);
        if (attr == null) {
            attr = new ASTAttribute(new TypeTask(),entry);
        }
        m_ast.put(ctx, attr);
        m_taskOrProcContext = ctx;

        visitChildren(ctx);
        
        // check signal handler for proper termination
        ControlFlowGraph cfg = ((TaskEntry)entry).getControlFlowGraph();
        checkAllSignalHandlers(cfg);
        
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    private void checkAllSignalHandlers(ControlFlowGraph cfg) {
    
        Vector<ControlFlowGraphNode> nodes = cfg.getNodeList();
        for (ControlFlowGraphNode n: nodes) {
            if (n instanceof PseudoNode && ((PseudoNode)n).getNodeType() == PseudoNode.sigReaction) {
                checkSignalHandler(cfg, n);
            }
        }
    }
    
    private void checkSignalHandler(ControlFlowGraph cfg, ControlFlowGraphNode signalHandler) {

        ControlFlowGraphNode begin = signalHandler.getNext();
        
        // the grammar makes shure that 
        //   ether a BEGIN/END block exists 
        //   or a signal terminating statement is given
        
        // test if we have a BEGIN/END-block
        if (!(begin instanceof PseudoNode)) return;
        
        // we have a signal handler with a block of statements
        ControlFlowGraphNode end = ((PseudoNode)begin).getEnd();
        Vector<ControlFlowGraphNode> predecessors = cfg.getPredecessors(end);
        if (predecessors.size() == 0) {
            // fine, we do not reach the end
        } else {
            // mark the first real node of predecessors as error
            ControlFlowGraphNode errorNode=predecessors.firstElement();
            ErrorStack.add(errorNode.getCtx(), "signal handler", "must end with RETURN, TERMINATE, INDUCE or GOTO");

//            boolean hasRealStatement = true;
//            // if we have nested IF/SWITCH-contructs, we must iterate backward to the first real statement
//            while (hasRealStatement == true && errorNode instanceof PseudoNode && errorNode != begin) {
//                Vector<ControlFlowGraphNode> p = cfg.getPredecessors(errorNode);
//                if (p.size() == 0) {
//                    hasRealStatement = false;
//                } else {
//                   errorNode = p.firstElement();
//                }
//            }
//            if (hasRealStatement) {
//                if (errorNode == begin) {
//                   // empty handler --> mark END
//                   errorNode = begin.getNext();
//               }
//               ErrorStack.add(errorNode.getCtx(), "signal handler", "requires signal termination statement");
//            }
        }
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
                ErrorStack.add(ctx.returnStatement(),"signal handler", "RETURN is not allowed in a signal reaction of a task");
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

    @Override
    public Void visitReturnStatement(OpenPearlParser.ReturnStatementContext ctx) {
        visitChildren(ctx);
        if (m_isInSignalReaction) {
            if (m_isInTask) {
                ErrorStack.add(ctx,"signal handler", "RETURN is not allowed in a signal reaction of a task");
            }
         }

        return null;
    }

}
