/*
 * [A "BSD license"]
 * *  Copyright (c) 2022 Rainer Müller
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


import java.util.Vector;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.*;
import org.openpearl.compiler.OpenPearlParser.Cpp_inlineContext;
import org.openpearl.compiler.OpenPearlParser.GotoStatementContext;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.*;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.RepeatNode.RepeatType;
import org.openpearl.compiler.SymbolTable.*;

/**
 * generate the control flow graphs
 * 
 */
public class GenerateControlFlowGraph extends OpenPearlBaseVisitor<Void>
        implements OpenPearlVisitor<Void> {

    /* grammar: 
    statement:
        label_statement* ( 
            unlabeled_statement | 
            block_statement |                       done
            cpp_inline )  ;


unlabeled_statement:
      empty_statement                           no job for the cfg
    | realtime_statement                        done
    | interrupt_statement                       done
    | assignment_statement                      done
    | sequential_control_statement              done
    | io_statement                              done
    | callStatement                             done
    | returnStatement
    | gotoStatement
    | loopStatement                             done
    | exitStatement                             done
    | convertStatement                          done
    ;
sequential_control_statement:
      if_statement                              done
    | case_statement                            done
    ;    

*/
    private class BlockLevel {
        public String blockId;
        public ControlFlowGraphNode blockEnd;
        public BlockLevel(String id, ControlFlowGraphNode end) {
            blockId = id;
            blockEnd = end;
        }

    };
    
    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private AST m_ast = null;
    
    private ControlFlowGraph m_currentCfg  = null;
    private ControlFlowGraphNode m_procEndNode = null;
    private ControlFlowGraphNode m_previousNode = null;
    private CaseNode m_currentCaseStatement = null;
    private IfNode m_currentIfStatement = null;
    private Vector<ControlFlowGraphNode> m_gotoNodes = null;
    private Vector<ControlFlowGraphNode> m_labelNodes = null;
    private static int removeMe = 1;

    /**
     * in order to locate the addressed block in EXIT, the nested block levels are stacked 
     * in this vector
     */
    private Vector<BlockLevel> m_blockLevels = new Vector<>();
            
    private Vector<ParserRuleContext> m_functionCalls;
    private Vector<PseudoNode> m_signalReactions;
    
    /*
     * the variable m_previousNode points the the previous node in the cfg
     * this may be null, if the previous statement terminates the control flow like 
     *    EXIT, which jumps to the block end
     *    RETURN, which jump to procEnd
     *    GOTO, which jumps to the given label
     *    TERMINATE without a taskname, which terminates the execution of the task
     *  In these cases the m_previousNode is set to null, to indicate that the next statement
     *  has no predecessor   
     */
    private void updatePreviousNode(ControlFlowGraphNode next) {
        if (m_previousNode != null) {
            m_previousNode.setNext(next); 
        }
        m_previousNode = next;
    }
    
    
    public GenerateControlFlowGraph(String sourceFileName, 
            SymbolTableVisitor symbolTableVisitor,
            AST ast) {

        m_debug = Options.isDebug();
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        Log.debug("    Generate Control Fliow Graphs");
    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Generate CFGs: visitModule");
        }

        org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        
        visitChildren(ctx);
        
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Generate CFGs: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
       
        String name = ctx.nameOfModuleTaskProc().ID().toString();
        ProcedureEntry procedureEntry = (ProcedureEntry)m_currentSymbolTable.lookup(name);
        
      
        PseudoNode proc = new PseudoNode(ctx,PseudoNode.procEntry);
        m_currentCfg = new ControlFlowGraph(proc);
        m_currentCfg.addNode(proc);
        m_previousNode = proc;
        m_functionCalls = null;
        m_gotoNodes = null;
        m_labelNodes = null;
        m_signalReactions = new Vector<PseudoNode>();
        
        
        m_procEndNode = new PseudoNode(ctx.endOfBlockLoopProcOrTask(),PseudoNode.procEnd);
        proc.setEnd(m_procEndNode);
        
        visitChildren(ctx);
        
        
        m_currentCfg.addNode(m_procEndNode);
        
        updatePreviousNode(m_procEndNode);
        
        finalizeGotos();
        removePseudoNodesWithoutPredecessors();
        procedureEntry.setControlFlowGraph(m_currentCfg);
               
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }



    /* ----------------------------------------------------------------------- */
    /* class specify stuff starts here                                         */
    /* ----------------------------------------------------------------------- */
    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Generate CFGs: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        
        String name = ctx.nameOfModuleTaskProc().ID().toString();
        TaskEntry taskEntry = (TaskEntry) m_currentSymbolTable.lookup(name);
        
        PseudoNode taskEntryNode = new PseudoNode(ctx,PseudoNode.taskEntry);
        m_currentCfg = new ControlFlowGraph(taskEntryNode);
        m_currentCfg.addNode(taskEntryNode);
        m_previousNode = taskEntryNode;
        m_functionCalls = null;
        m_gotoNodes = null;
        m_labelNodes = null;
        m_signalReactions = new Vector<PseudoNode>();
        
        visitChildren(ctx);
        
        PseudoNode taskEnd = new PseudoNode(ctx.endOfBlockLoopProcOrTask(),PseudoNode.taskEnd);
        m_currentCfg.addNode(taskEnd);
        taskEntryNode.setEnd(taskEnd);

        if (m_previousNode != null) {
           m_previousNode.setNext(taskEnd);
        }
        
        finalizeGotos();
        removePseudoNodesWithoutPredecessors();
        taskEntry.setControlFlowGraph(m_currentCfg);
        
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
       
    }



    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
       
        if (m_debug) {
            System.out.println("Semantic: Generate CFGs: visitBlock_statement");
        }
        
        String blockLabel = null;
        
        if (ctx.blockId() != null) {
            blockLabel = ctx.blockId().ID().toString();
        }
        
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        
        PseudoNode blockBegin = new PseudoNode(ctx, PseudoNode.blockBegin);
        m_currentCfg.addNode(blockBegin);
        updatePreviousNode(blockBegin);
        PseudoNode blockEnd = new PseudoNode(ctx.endOfBlockLoopProcOrTask(), PseudoNode.blockEnd);
        blockBegin.setEnd(blockEnd);
               
        BlockLevel bl = new BlockLevel(blockLabel, blockEnd);
        m_blockLevels.add(0,bl);  // add as first element
       
        visitChildren(ctx);
        
        updatePreviousNode(blockEnd);
        m_currentCfg.addNode(blockEnd);

                
        m_blockLevels.removeElementAt(0);
        
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitCpp_inline(OpenPearlParser.Cpp_inlineContext ctx) {
        if (m_currentSymbolTable.m_level>1) {
            PseudoNode cpp = new PseudoNode(ctx, PseudoNode.cppInline);
            m_currentCfg.addNode(cpp);
            updatePreviousNode(cpp);
        }
        return null;
    }
    
    @Override
    public Void visitExitStatement(OpenPearlParser.ExitStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Generate CFGs: visitExit_statement");
        }

        
        SequentialNode exitNode = new SequentialNode(ctx);
        m_currentCfg.addNode(exitNode);
        updatePreviousNode(exitNode);
 
        if (ctx.ID() != null) {
            String label = ctx.ID().toString(); 
            // lookup desired block end
            for (BlockLevel bl: m_blockLevels) {
                if (bl.blockId != null && bl.blockId.equals(label) ) {
                    exitNode.setNext(bl.blockEnd);
                }
            }
            
        } else {
            exitNode.setNext(m_blockLevels.get(0).blockEnd);
        }
        
        m_previousNode = null;
        return null;
    }
    
    @Override
    public Void visitLabel_statement  (OpenPearlParser.Label_statementContext ctx) {
        LabelNode label = new LabelNode(ctx);
        m_currentCfg.addNode(label);

        updatePreviousNode(label);
        
        if (m_labelNodes == null) {
            m_labelNodes = new Vector<ControlFlowGraphNode>();
        }
        
        m_labelNodes.add(label);
    
        return null;
    }
    
    @Override
    public Void visitGotoStatement  (OpenPearlParser.GotoStatementContext ctx) {
        
        // note: the statement after a GOTO has no predecessor
        //   thus the 2nd parameter should be true
        //   but we need the created node for the resolution with the labels
        treatSimpleSequentialStatement(ctx, false); 
        if (m_gotoNodes == null) {
            m_gotoNodes = new Vector<ControlFlowGraphNode>();
        }
        m_gotoNodes.add(m_previousNode);
        
        // now fix the workaround from above
        m_previousNode = null;
    
        return null;
    }
    
    @Override
    public Void visitInduceStatement  (OpenPearlParser.InduceStatementContext ctx) {
        
        // note: the statement after an INDUCE has no predecessor
        //   thus the 2nd parameter should be true
        //   but we need the created node for the resolution with the labels
        treatSimpleSequentialStatement(ctx, false); 
//        if (m_gotoNodes == null) {
//            m_gotoNodes = new Vector<ControlFlowGraphNode>();
//        }
//        m_gotoNodes.add(m_previousNode);
//        
        // now fix the workaround from above
        m_previousNode = null;
    
        return null;
    }
    
    
    @Override
    public Void visitAssignment_statement  (OpenPearlParser.Assignment_statementContext ctx) {
       
        treatSimpleSequentialStatement(ctx, false);
             
        return null;
    }

    
    @Override
    public Void visitCallStatement  (OpenPearlParser.CallStatementContext ctx) {
  
        CallNode newNode = new CallNode(ctx);
        m_currentCfg.addNode(newNode);
        
        visitChildren(ctx.name());
        
        if (m_functionCalls != null) {
            FunctionCallsInNextStatement  fc = new FunctionCallsInNextStatement(m_ast, ctx, m_functionCalls);
            m_currentCfg.addNode(fc);
            m_functionCalls = null;
            updatePreviousNode(fc);
        }
        
        updatePreviousNode(newNode);
    
        return null;
    }
    
    
    @Override
    public Void visitReturnStatement  (OpenPearlParser.ReturnStatementContext ctx) {
        
        SequentialNode newNode = new SequentialNode(ctx);
        m_currentCfg.addNode(newNode);
        
        visitChildren(ctx);
        
        if (m_functionCalls != null) {
            FunctionCallsInNextStatement  fc = new FunctionCallsInNextStatement(m_ast, ctx, m_functionCalls);
            m_currentCfg.addNode(fc);
            m_functionCalls = null;
            updatePreviousNode(fc);
        }
        
        updatePreviousNode(newNode);
        newNode.setNext(m_procEndNode); 
        m_previousNode = null;
        
        return null;
    }
        
    
    @Override
    public Void visitName(OpenPearlParser.NameContext ctx) {
        ASTAttribute attr = m_ast.lookup(ctx);
        
        if (attr != null && attr.isFunctionCall()) {
            if (m_functionCalls == null) {
                m_functionCalls = new Vector<ParserRuleContext>();
            }
            m_functionCalls.add(ctx);
        }
        visitChildren(ctx);
        
    
        return null;
    }
    @Override
    public Void visitIf_statement  (OpenPearlParser.If_statementContext ctx) {
        
        if (m_debug) {
            System.out.println("Semantic: Generate CFGs: visitIf_statement");
        }
        
        IfNode ifNode = new IfNode(ctx);
        m_currentCfg.addNode(ifNode);
        IfNode previousIfStatement = m_currentIfStatement;
        m_currentIfStatement = ifNode;
        
//        // locate the end of the statement,
//        // since the grammar is ok, we can take the address of the last child
//        org.antlr.v4.runtime.tree.ParseTree pt = ctx.getChild(ctx.getChildCount()-1);
//        int x=pt.getSourceInterval().a;
//        m_currentIfStatement.setFinLocation(x);
        
 
        visitChildren(ctx.expression());
        if (m_functionCalls != null) {
            FunctionCallsInNextStatement fc = new FunctionCallsInNextStatement(m_ast, ctx, m_functionCalls);
            m_currentCfg.addNode(fc);
            m_functionCalls = null;
            updatePreviousNode(fc);
        }

        
        updatePreviousNode(ifNode);
    
        PseudoNode pseudoFin = new PseudoNode(ctx.fin_if_case() , PseudoNode.ifFin);
       
        ifNode.setFinNode(pseudoFin);

        PseudoNode pseudoThen = new PseudoNode(ctx.then_block(), PseudoNode.ifThen);
        m_currentCfg.addNode(pseudoThen);
        ifNode.setThenNode(pseudoThen);
       
        
        m_previousNode = pseudoThen;
        visit(ctx.then_block());
        
        updatePreviousNode(pseudoFin);
 
       
        if (ctx.else_block()!= null) {
            PseudoNode pseudoElse = new PseudoNode(ctx.else_block(), PseudoNode.ifElse);
            m_currentCfg.addNode(pseudoElse);
            m_previousNode = pseudoElse;
            ifNode.setElseNode(pseudoElse);
            visit(ctx.else_block());
            updatePreviousNode(pseudoFin);
            
        } else {
            ifNode.setElseNode(pseudoFin);
        }
        m_previousNode = pseudoFin;
        m_currentCfg.addNode(pseudoFin);

        m_currentIfStatement = previousIfStatement;
        
        return null;
    }
    
    
    /*
     * remarks about the CFG
     * 
     * there are PseudoNodes for RepeatBegin, RepeatEnd, BodyBegin and BodyEnd
     * and RepeatNodes for FOR and/or WHILE 
     * 
     * Expressions in FROM/BYand TO are evaluated only once
     * Expressions in WHILE are evaluated before each iteration of the loop
     * 
     * The graph will be
     *  RepeatBegin -> [ [ FunctionCallsInNextStatement ->] RepeatNode(For) ] ->
     *                 [ [ FunctionCallsInNextStatement ->]RepeatNode(While) ] ->
     *          BodyBegin -> Nodes -> BodyEnd ->
     *          RepeatNode(While or For) 
     *      and RepeatNode(For) and RepeatNode(While) -> RepeatEnd
     *      
     *  An EXIT will point to the corresponding RepeatEnd
     *  
     *  Loop is missing if there is no TO and WHILE given. 
     *  In this case to exiting path from RepeatNode(FOR/WHILE) are missing
     *  
     */
    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        RepeatNode repeatWhile = null;
        RepeatNode repeatFor = null;
        ControlFlowGraphNode nextIteration = null;
        
        boolean isEndless = false;
        
        if (m_debug) {
            System.out.println("Semantic: Generate CFGs: visitLoopStatement");
        }
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        
        String blockLabel = null;
        
        if (ctx.loopStatement_end().ID() != null) {
            blockLabel = ctx.loopStatement_end().ID().toString();
        }
        
        isEndless = (ctx.loopStatement_to() == null) && (ctx.loopStatement_while() == null);
        
        PseudoNode repeatBegin = new PseudoNode(ctx, PseudoNode.repeatBegin);
        m_currentCfg.addNode(repeatBegin);
        updatePreviousNode(repeatBegin);
        PseudoNode repeatEnd = new PseudoNode(ctx.loopStatement_end(), PseudoNode.repeatEnd);
        if (ctx.loopStatement_for() != null || ctx.loopStatement_from() != null ||
                ctx.loopStatement_by() != null || ctx.loopStatement_to() != null) {
            // we have at least one of FOR/FROM/BY/TO items
            repeatFor = new RepeatNode(ctx,RepeatType.IsFor);
            nextIteration = repeatFor;
            repeatFor.setEndNode(repeatEnd);
            
            m_currentCfg.addNode(repeatFor);
            
            // let's gather all function calls in loop control
            if (ctx.loopStatement_from() != null) visitChildren(ctx.loopStatement_from());
            if (ctx.loopStatement_to() != null) visitChildren(ctx.loopStatement_to());
            if (ctx.loopStatement_by() != null) visitChildren(ctx.loopStatement_by());
            
            if (m_functionCalls != null) {
                FunctionCallsInNextStatement  fc = new FunctionCallsInNextStatement(m_ast, ctx, m_functionCalls);
                m_currentCfg.addNode(fc);
                m_functionCalls = null;
                updatePreviousNode(fc);
            }
            
            updatePreviousNode(repeatFor);
        }
      

        if (ctx.loopStatement_while() != null) {
            visitChildren(ctx.loopStatement_while());
            
            if (m_functionCalls != null) {
                FunctionCallsInNextStatement  fc = new FunctionCallsInNextStatement(m_ast, ctx, m_functionCalls);
                m_currentCfg.addNode(fc);
                m_functionCalls = null;
                updatePreviousNode(fc);
                if (nextIteration == null) {
                    nextIteration = fc;
                }
            }
            repeatWhile = new RepeatNode(ctx.loopStatement_while(),RepeatType.IsWhile);
            repeatWhile.setEndNode(repeatEnd);
            m_currentCfg.addNode(repeatWhile);
            updatePreviousNode(repeatWhile);
            if (nextIteration == null) {
                nextIteration = repeatWhile;
            }
        }
        
    
        BlockLevel bl = new BlockLevel(blockLabel, repeatEnd);
        m_blockLevels.add(0,bl);  // add as first element
        
        PseudoNode body=new PseudoNode(ctx.loopBody(),PseudoNode.repeatBodyBegin);
        m_currentCfg.addNode(body);
        updatePreviousNode(body);
       
        
        visitChildren(ctx.loopBody());
        
        body = new PseudoNode(ctx.loopStatement_end(), PseudoNode.repeatBodyEnd);
        m_currentCfg.addNode(body);
        
        updatePreviousNode(body);
        if (nextIteration != null) {
            updatePreviousNode(nextIteration);
        } else {
            updatePreviousNode(repeatBegin);
        }
     
        m_currentCfg.addNode(repeatEnd);
        m_previousNode = repeatEnd;

        
        m_blockLevels.removeElementAt(0);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

 
    @Override
    public Void visitCase_statement(OpenPearlParser.Case_statementContext ctx) {
        
        if (m_debug) {
            System.out.println("Semantic: Generate CFGs: visitCaseStatement");
        }
        
        if (ctx.case_statement_selection1() != null) {
            visitChildren(ctx.case_statement_selection1().expression());    
        }
        if (ctx.case_statement_selection2() != null) {
            visitChildren(ctx.case_statement_selection2().expression());    
        }
        
        // not checked yet
        if (m_functionCalls != null) {
            FunctionCallsInNextStatement  fc = new FunctionCallsInNextStatement(m_ast, ctx, m_functionCalls);
            m_currentCfg.addNode(fc);
            m_functionCalls = null;
            updatePreviousNode(fc);
        }
        
        CaseNode previousCaseStatement = m_currentCaseStatement;
        m_currentCaseStatement = new CaseNode(ctx);
        m_currentCfg.addNode(m_currentCaseStatement);
        updatePreviousNode(m_currentCaseStatement);
        
//        // locate the end of the statement,
//        // since the grammar is ok, we can take the address of the last child
//        org.antlr.v4.runtime.tree.ParseTree pt = ctx.getChild(ctx.getChildCount()-1);
//        int x=pt.getSourceInterval().a;
//        m_currentCaseStatement.setFinLocation(x);
        

        ControlFlowGraphNode finNode = new PseudoNode(ctx, PseudoNode.caseFin);

        m_currentCaseStatement.setFinNode(finNode);
        
        
        visitChildren(ctx);
        
//        if (m_currentCaseStatement.getOutNode() != null) {
//            m_currentCaseStatement.setNext(null);
//        } else {
//            m_currentCaseStatement.setNext(finNode);
//        }
        m_previousNode = finNode;
        m_currentCfg.addNode(finNode);
        
        m_currentCaseStatement = previousCaseStatement;
        
        
        return null;
    }
    
    
    @Override
    public Void visitCase_statement_selection_out(OpenPearlParser.Case_statement_selection_outContext ctx) {
        ControlFlowGraphNode alt = new PseudoNode(ctx, PseudoNode.caseOut);
        m_currentCfg.addNode(alt);
        m_currentCaseStatement.setOutNode(alt);
        m_previousNode = alt;
        visitChildren(ctx);
        updatePreviousNode(m_currentCaseStatement.getFinNode());  // branch to FIN
        m_previousNode = null; 
        return null;
    }
    
    @Override
    public Void visitCase_statement_selection1_alt(OpenPearlParser.Case_statement_selection1_altContext ctx) {
        ControlFlowGraphNode alt = new PseudoNode(ctx, PseudoNode.caseAlt);
        m_currentCfg.addNode(alt);
        m_currentCaseStatement.addAlternative();
        int currentAltIndex = m_currentCaseStatement.m_alternatives.size()-1;
        m_currentCaseStatement.setAltNode(alt,currentAltIndex);
        m_previousNode = alt;
        
        visitChildren(ctx);
        
        updatePreviousNode(m_currentCaseStatement.getFinNode());  // branch to FIN
        m_previousNode = null;
        return null;
    }

    @Override
    public Void visitCase_statement_selection2_alt(OpenPearlParser.Case_statement_selection2_altContext ctx) {
        ControlFlowGraphNode alt = new PseudoNode(ctx, PseudoNode.caseAlt);
        m_currentCfg.addNode(alt);
        m_currentCaseStatement.addAlternative();
        int currentAltIndex = m_currentCaseStatement.m_alternatives.size()-1;
        m_currentCaseStatement.setAltNode(alt,currentAltIndex);
        m_previousNode = alt;
        visitChildren(ctx);
        updatePreviousNode(m_currentCaseStatement.getFinNode());  // branch to FIN
        m_previousNode = null;
        return null;
    }
    
    @Override
    public Void visitIo_statement(OpenPearlParser.Io_statementContext ctx) {
        
        treatSimpleSequentialStatement(ctx,false);
        
        return null;
    }
    
    @Override
    public Void visitConvertStatement(OpenPearlParser.ConvertStatementContext ctx) {
        
        treatSimpleSequentialStatement(ctx,false);
        return null;
    }
    
    @Override
    public Void visitRealtime_statement(OpenPearlParser.Realtime_statementContext ctx) {
        if (ctx.task_control_statement()!= null &&
                ctx.task_control_statement().task_terminating()!= null) {
                visit(ctx.task_control_statement().task_terminating());
        } else {
           treatSimpleSequentialStatement(ctx,false);
        }
        return null;
    }

    @Override
    public Void visitInterrupt_statement(OpenPearlParser.Interrupt_statementContext ctx) {

        treatSimpleSequentialStatement(ctx,false);

        return null;
    }
    
    
    /*
     * common stuff for most of the statements
     * 
     * exceptions must be done for GOTO, TERMINATE <without taskname>, RETURN, 
     * which have no path to the next statement
     * 
     * @param ctx the current context
     * @param controlFlowStops: false indicates standard operation
     *                        true indicates that the control flow stops here
     */
    private void treatSimpleSequentialStatement(ParserRuleContext ctx, boolean controlFlowStops) {
        
        SequentialNode newNode = new SequentialNode(ctx);
        m_currentCfg.addNode(newNode);
        
        visitChildren(ctx);
        
        if (m_functionCalls != null) {
            FunctionCallsInNextStatement  fc = new FunctionCallsInNextStatement(m_ast, ctx, m_functionCalls);
            m_currentCfg.addNode(fc);
            m_functionCalls = null;
            updatePreviousNode(fc);
        }
        
        updatePreviousNode(newNode);
        
        if (controlFlowStops) {
            m_previousNode = null;
        }
        
    }
    

 
    /**
    task_terminating: 'TERMINATE' name? ';' ;
     */
    @Override
    public Void visitTask_terminating(OpenPearlParser.Task_terminatingContext ctx) {

        if (ctx.name() == null) {
            treatSimpleSequentialStatement(ctx,true);
        } else {
            treatSimpleSequentialStatement(ctx,false);
        }

        return null;
    }

    public Void visitSchedulingSignalReaction(OpenPearlParser.SchedulingSignalReactionContext ctx) {
        if (ctx.signalReaction() != null) {
           PseudoNode s = new PseudoNode(ctx.signalReaction(),PseudoNode.sigReaction);
           m_currentCfg.addNode(s);
           ControlFlowGraphNode  safePreviousNode = m_previousNode;
           m_signalReactions.add(s);
           m_previousNode = s;
           visitChildren(ctx.signalReaction());
           m_previousNode = safePreviousNode;
        }
        return null;
    }
    
    /*
    triggerStatement : 'TRIGGER' ID ';'     ;
     */
    @Override
    public Void visitTriggerStatement(OpenPearlParser.TriggerStatementContext ctx) {
        ErrorStack.warn(ctx, "generate CFG", "TRIGGER not supported");
        return null;
    }

    /*
     * we have a vector with goto nodes and a vector with label nodes
     * 
     * we must find for each goto node the label node with the context of the label
     * CheckGotoExit checked already that each goto label exists and is in the correct level 
     */
    private void finalizeGotos() {
        if (m_gotoNodes != null) {
            // at least one goto found
            for (ControlFlowGraphNode n: m_gotoNodes) {
                GotoStatementContext gto = (GotoStatementContext)(n.getCtx());
                ASTAttribute attr = m_ast.lookup(gto);
                ParserRuleContext labelCtx = attr.getSymbolTableEntry().getCtx();
                
                for ( ControlFlowGraphNode l: m_labelNodes) {
                    if (l.getCtx().equals(labelCtx)) {
                        n.setNext(l);
                        break;
                    }
                }
            }
        }
    }

    /**
     * remove pseudo nodes without predecessors like FIN, END which are never reached
     * in the CFG
     */
    private void removePseudoNodesWithoutPredecessors() {
        for (ControlFlowGraphNode n: m_currentCfg.getNodeList()) {
            if (n instanceof PseudoNode) {
                int nodeType = ((PseudoNode)n).getNodeType();
                
                if (nodeType == PseudoNode.blockEnd ||
                        nodeType == PseudoNode.caseFin ||
                        nodeType == PseudoNode.ifFin ||
                        nodeType == PseudoNode.repeatEnd ||
                        nodeType == PseudoNode.repeatBodyEnd) {
                    Vector<ControlFlowGraphNode> predecessors = m_currentCfg.getPredecessors(n);
                    if (predecessors.size() == 0) {
                        n.setFlag(removeMe);
                    }
                }
            }
        }
        
        for (int i=m_currentCfg.getNodeList().size()-1; i>=0; i--) {
            if (m_currentCfg.getNodeList().elementAt(i).isSet(removeMe)) {
                m_currentCfg.getNodeList().remove(i);
            }
        }
    }

}
