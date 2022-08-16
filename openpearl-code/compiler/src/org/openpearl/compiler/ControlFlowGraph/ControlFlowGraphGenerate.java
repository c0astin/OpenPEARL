/*
 * [A "BSD license"]
 *  Copyright (c) 2021 Ilja Mascharow
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
package org.openpearl.compiler.ControlFlowGraph;

import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.*;
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;

import java.util.*;

public class ControlFlowGraphGenerate extends OpenPearlBaseVisitor<Void> implements OpenPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private ExpressionTypeVisitor m_expressionTypeVisitor;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ModuleEntry m_module;
    private AST m_ast = null;

    private int currentDepth = 0;

    private List<ControlFlowGraph> cfgs = new ArrayList<>();
    private ControlFlowGraph cfg;
    private ControlFlowGraphNode entryControlFlowGraphNode;
    private ControlFlowGraphNode endControlFlowGraphNode;
    private ControlFlowGraphNode lastControlFlowGraphNode;
    private ControlFlowGraphNode currentBlock;
    private Map<ControlFlowGraphNode, List<ControlFlowGraphNode>> blockChildren;
    private Map<ControlFlowGraphNode, List<ControlFlowGraphNode>> exitedAtBlock;
    private boolean blockNewlyEnded;
    private int lineNumberCounter;
    private Map<String, List<ControlFlowGraphNode>> gotoNodes;
    private Map<String, ControlFlowGraphNode> gotoLabels;
    private String currentTaskName;

    private ControlFlowGraph lastCfg;
    private ControlFlowGraphNode lastEntryControlFlowGraphNode;
    private ControlFlowGraphNode lastEndControlFlowGraphNode;
    private ControlFlowGraphNode lastLastControlFlowGraphNode;
    private ControlFlowGraphNode lastCurrentBlock;
    private Map<ControlFlowGraphNode, List<ControlFlowGraphNode>> lastBlockChildren;
    private Map<ControlFlowGraphNode, List<ControlFlowGraphNode>> lastExitedAtBlock;
    private boolean lastBlockNewlyEnded;
    private int lastLineNumberCounter;
    private Map<String, List<ControlFlowGraphNode>> lastGotoNodes;
    private Map<String, ControlFlowGraphNode> lastGotoLabels;
    private String lastCurrentTaskName;

    public ControlFlowGraphGenerate(String sourceFileName,
                                    int verbose,
                                    boolean debug,
                                    SymbolTableVisitor symbolTableVisitor,
                                    ExpressionTypeVisitor expressionTypeVisitor,
                                    AST ast) {

        m_debug = debug;
        m_verbose = verbose;
        m_sourceFileName = sourceFileName;
        m_symbolTableVisitor = symbolTableVisitor;
        m_expressionTypeVisitor = expressionTypeVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        Log.info("Semantic Check: Generate ControlFlowGraph");
    }

    public List<ControlFlowGraph> getControlFlowGraphs() {
        return cfgs;
    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Generate ControlFlowGraph: visitModule");
        }

        lineNumberCounter = 1;
        entryControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.ENTRY, ctx, lineNumberCounter++, currentDepth);
        endControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.END, null, 0, currentDepth);
        cfg = new ControlFlowGraph("Module: " + ctx.nameOfModuleTaskProc().ID().toString(), entryControlFlowGraphNode, endControlFlowGraphNode);
        lastControlFlowGraphNode = entryControlFlowGraphNode;
        currentBlock = entryControlFlowGraphNode;
        exitedAtBlock = new HashMap<>();
        blockNewlyEnded = false;
        blockChildren = new HashMap<>();
        blockChildren.put(currentBlock, new ArrayList<>());
        exitedAtBlock.put(currentBlock, new ArrayList<>());
        gotoLabels = new HashMap<>();
        gotoNodes = new HashMap<>();
        currentTaskName = null;

        currentDepth++;
        visitChildren(ctx);
        currentDepth--;

        endControlFlowGraphNode.setId(lineNumberCounter++);
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, endControlFlowGraphNode);
        }
        cfg.addIfNotExist(endControlFlowGraphNode);
        cfgs.add(cfg);

        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Generate ControlFlowGraph: visitProcedureDeclaration");
        }

        lastCfg = cfg;
        lastEntryControlFlowGraphNode = entryControlFlowGraphNode;
        lastEndControlFlowGraphNode = endControlFlowGraphNode;
        lastLastControlFlowGraphNode = lastControlFlowGraphNode;
        lastCurrentBlock = currentBlock;
        lastBlockChildren = blockChildren;
        lastExitedAtBlock = exitedAtBlock;
        lastBlockNewlyEnded = blockNewlyEnded;
        lastLineNumberCounter = lineNumberCounter;
        lastGotoNodes = gotoNodes;
        lastGotoLabels = gotoLabels;
        lastCurrentTaskName = currentTaskName;

        lineNumberCounter = 1;
        entryControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.ENTRY, ctx, lineNumberCounter++, currentDepth);
        endControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.END, null, 0, currentDepth);
        cfg = new ControlFlowGraph("Procedure: " + ctx.nameOfModuleTaskProc().ID().toString(), entryControlFlowGraphNode, endControlFlowGraphNode);
        lastControlFlowGraphNode = entryControlFlowGraphNode;
        currentBlock = entryControlFlowGraphNode;
        exitedAtBlock = new HashMap<>();
        blockNewlyEnded = false;
        blockChildren = new HashMap<>();
        blockChildren.put(currentBlock, new ArrayList<>());
        exitedAtBlock.put(currentBlock, new ArrayList<>());
        gotoLabels = new HashMap<>();
        gotoNodes = new HashMap<>();
        currentTaskName = null;

        currentDepth++;
        visitChildren(ctx);
        currentDepth--;

        endControlFlowGraphNode.setId(lineNumberCounter++);
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, endControlFlowGraphNode);
        }
        cfg.addIfNotExist(endControlFlowGraphNode);
        cfgs.add(cfg);

        cfg = lastCfg;
        entryControlFlowGraphNode = lastEntryControlFlowGraphNode;
        endControlFlowGraphNode = lastEndControlFlowGraphNode;
        lastControlFlowGraphNode = lastLastControlFlowGraphNode;
        currentBlock = lastCurrentBlock;
        blockChildren = lastBlockChildren;
        exitedAtBlock = lastExitedAtBlock;
        blockNewlyEnded = lastBlockNewlyEnded;
        lineNumberCounter = lastLineNumberCounter;
        gotoNodes = lastGotoNodes;
        gotoLabels = lastGotoLabels;
        currentTaskName = lastCurrentTaskName;
        return null;
    }

    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Generate ControlFlowGraph: visitTaskDeclaration");
        }

        lastCfg = cfg;
        lastEntryControlFlowGraphNode = entryControlFlowGraphNode;
        lastEndControlFlowGraphNode = endControlFlowGraphNode;
        lastLastControlFlowGraphNode = lastControlFlowGraphNode;
        lastCurrentBlock = currentBlock;
        lastBlockChildren = blockChildren;
        lastExitedAtBlock = exitedAtBlock;
        lastBlockNewlyEnded = blockNewlyEnded;
        lastLineNumberCounter = lineNumberCounter;
        lastGotoNodes = gotoNodes;
        lastGotoLabels = gotoLabels;
        lastCurrentTaskName = currentTaskName;

        lineNumberCounter = 1;
        entryControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.ENTRY, ctx, lineNumberCounter++, currentDepth);
        endControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.END, null, 0, currentDepth);
        cfg = new ControlFlowGraph("Task: " + ctx.nameOfModuleTaskProc().ID().toString(), entryControlFlowGraphNode, endControlFlowGraphNode);
        lastControlFlowGraphNode = entryControlFlowGraphNode;
        currentBlock = entryControlFlowGraphNode;
        exitedAtBlock = new HashMap<>();
        blockNewlyEnded = false;
        blockChildren = new HashMap<>();
        blockChildren.put(currentBlock, new ArrayList<>());
        exitedAtBlock.put(currentBlock, new ArrayList<>());
        gotoLabels = new HashMap<>();
        gotoNodes = new HashMap<>();
        currentTaskName = ctx.nameOfModuleTaskProc().ID().getText();

        currentDepth++;
        visitChildren(ctx);
        currentDepth--;

        endControlFlowGraphNode.setId(lineNumberCounter++);
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, endControlFlowGraphNode);
        }
        cfg.addIfNotExist(endControlFlowGraphNode);
        cfgs.add(cfg);

        cfg = lastCfg;
        entryControlFlowGraphNode = lastEntryControlFlowGraphNode;
        endControlFlowGraphNode = lastEndControlFlowGraphNode;
        lastControlFlowGraphNode = lastLastControlFlowGraphNode;
        currentBlock = lastCurrentBlock;
        blockChildren = lastBlockChildren;
        exitedAtBlock = lastExitedAtBlock;
        blockNewlyEnded = lastBlockNewlyEnded;
        lineNumberCounter = lastLineNumberCounter;
        gotoNodes = lastGotoNodes;
        gotoLabels = lastGotoLabels;
        currentTaskName = lastCurrentTaskName;

        return null;
    }


    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Generate ControlFlowGraph: visitBlockStatement");
        }
        ControlFlowGraphNode lastBlock = currentBlock;
        String blockName = getBlockName(ctx);
        currentBlock = new ControlFlowGraphNode(ControlFlowGraphNodeType.BLOCK_START, ctx, lineNumberCounter++, currentDepth);
        ControlFlowGraphNode blockEndControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.BLOCK_END, null, 0, currentDepth);

        blockChildren.get(lastBlock).add(currentBlock);
        blockChildren.put(currentBlock, new ArrayList<>());
        exitedAtBlock.put(currentBlock, new ArrayList<>());

        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, currentBlock);
        }
        else blockNewlyEnded = false;
        lastControlFlowGraphNode = currentBlock;

        currentDepth++;
        visitChildren(ctx);
        currentDepth--;

        blockEndControlFlowGraphNode.setId(lineNumberCounter++);

        if(exitedAtBlock.get(currentBlock) != null) {
            exitedAtBlock.get(currentBlock).forEach(exitControlFlowGraphNode -> {
                OpenPearlParser.ExitStatementContext exitNodeCtx = ((OpenPearlParser.Unlabeled_statementContext) exitControlFlowGraphNode.getCtx()).exitStatement();
                if (exitNodeCtx.ID() == null || exitNodeCtx.ID().getText().equals(blockName))
                    cfg.addIfNotExist(exitControlFlowGraphNode, blockEndControlFlowGraphNode);
            });
        }

        Queue<ControlFlowGraphNode> childBlockQueue = new ArrayDeque<>(blockChildren.get(currentBlock));
        List<ControlFlowGraphNode> allChildBlocks = new ArrayList<>();
        while (!childBlockQueue.isEmpty()) {
            ControlFlowGraphNode block = childBlockQueue.remove();
            allChildBlocks.add(block);
            childBlockQueue.addAll(blockChildren.get(block));
        }
        allChildBlocks.forEach(block -> {
            exitedAtBlock.get(block).forEach(exitControlFlowGraphNode -> {
                OpenPearlParser.ExitStatementContext exitNodeCtx = ((OpenPearlParser.Unlabeled_statementContext) exitControlFlowGraphNode.getCtx()).exitStatement();
                if (exitNodeCtx.ID() != null) {
                    if( blockName != null && blockName.equals(exitNodeCtx.ID().getText())) {
                        cfg.addIfNotExist(exitControlFlowGraphNode, blockEndControlFlowGraphNode);
                    }
                }
            });
        });

        if(!blockNewlyEnded)
            cfg.addIfNotExist(lastControlFlowGraphNode, blockEndControlFlowGraphNode);
        else
            blockNewlyEnded = false;


        lastControlFlowGraphNode = blockEndControlFlowGraphNode;
        currentBlock = lastBlock;

        return null;
    }

    private String getBlockName(OpenPearlParser.Block_statementContext ctx) {
        String id = null;
        if (ctx.blockId() != null) {
            id = ctx.blockId().ID().getText();
        }
        return id;
    }

    private void loopStatement(ControlFlowGraphNode controlFlowGraphNode) {
        ControlFlowGraphNode lastBlock = currentBlock;
        currentBlock = controlFlowGraphNode;
        blockChildren.get(lastBlock).add(currentBlock);
        blockChildren.put(currentBlock, new ArrayList<>());
        exitedAtBlock.put(currentBlock, new ArrayList<>());
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, currentBlock);
        }
        else blockNewlyEnded = false;
        lastControlFlowGraphNode = currentBlock;

        currentDepth++;
        ControlFlowGraphNode blockStart = new ControlFlowGraphNode(ControlFlowGraphNodeType.LOOP_BLOCK_START, null, lineNumberCounter++, currentDepth);
        lastControlFlowGraphNode = blockStart;
        cfg.addIfNotExist(controlFlowGraphNode, blockStart);
        visitChildren(controlFlowGraphNode.getCtx());
        ControlFlowGraphNode blockRepeatControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.LOOP_REPEAT, null, lineNumberCounter++, currentDepth);
        currentDepth--;
        ControlFlowGraphNode blockEndControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.LOOP_END, null, lineNumberCounter++, currentDepth);
        if(exitedAtBlock.get(currentBlock) != null)
            exitedAtBlock.get(currentBlock).forEach(exitControlFlowGraphNode -> {
                OpenPearlParser.ExitStatementContext ctx = ((OpenPearlParser.Unlabeled_statementContext) exitControlFlowGraphNode.getCtx()).exitStatement();
                if (ctx.ID() == null)
                    cfg.addIfNotExist(exitControlFlowGraphNode, blockEndControlFlowGraphNode);
            });
        if(!blockNewlyEnded)
            cfg.addIfNotExist(lastControlFlowGraphNode, blockRepeatControlFlowGraphNode);
        cfg.addIfNotExist(blockRepeatControlFlowGraphNode, controlFlowGraphNode);
        cfg.addIfNotExist(controlFlowGraphNode, blockEndControlFlowGraphNode);

        blockNewlyEnded = false;
        lastControlFlowGraphNode = blockEndControlFlowGraphNode;

        currentBlock = lastBlock;
    }

    private void ifStatement(ControlFlowGraphNode if_ControlFlowGraph_node) {
        OpenPearlParser.If_statementContext if_ctx = ((OpenPearlParser.Unlabeled_statementContext) if_ControlFlowGraph_node.getCtx()).sequential_control_statement().if_statement();
        OpenPearlParser.Then_blockContext then_ctx = if_ctx.then_block();
        OpenPearlParser.Else_blockContext else_ctx = if_ctx.else_block();
        ControlFlowGraphNode ifEnd_ControlFlowGraph_node = new ControlFlowGraphNode(ControlFlowGraphNodeType.IF_END, null, 0, currentDepth);
        currentDepth++;
        ControlFlowGraphNode then_ControlFlowGraph_node = new ControlFlowGraphNode(ControlFlowGraphNodeType.THEN_START, then_ctx, lineNumberCounter++, currentDepth);

        //Node lastBlock = currentBlock;
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, if_ControlFlowGraph_node);
        }
        else blockNewlyEnded = false;

        //currentBlock = then_node;
        lastControlFlowGraphNode = then_ControlFlowGraph_node;

        currentDepth++;
        visitChildren(then_ctx);
        currentDepth--;

        ControlFlowGraphNode thenEnd_ControlFlowGraph_node = new ControlFlowGraphNode(ControlFlowGraphNodeType.THEN_END, null, lineNumberCounter++, currentDepth);
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, thenEnd_ControlFlowGraph_node);
        }
        else blockNewlyEnded = false;

        cfg.addIfNotExist(thenEnd_ControlFlowGraph_node, ifEnd_ControlFlowGraph_node);

        cfg.addIfNotExist(if_ControlFlowGraph_node, then_ControlFlowGraph_node);
        if(else_ctx != null) {
            ControlFlowGraphNode else_ControlFlowGraph_node = new ControlFlowGraphNode(ControlFlowGraphNodeType.ELSE_START, else_ctx, lineNumberCounter++, currentDepth);
            cfg.addIfNotExist(if_ControlFlowGraph_node, else_ControlFlowGraph_node);

            lastControlFlowGraphNode = else_ControlFlowGraph_node;

            currentDepth++;
            visitChildren(else_ctx);
            currentDepth--;

            ControlFlowGraphNode elseEnd_ControlFlowGraph_node = new ControlFlowGraphNode(ControlFlowGraphNodeType.ELSE_END, null, lineNumberCounter++, currentDepth);
            if(!blockNewlyEnded) {
                cfg.addIfNotExist(lastControlFlowGraphNode, elseEnd_ControlFlowGraph_node);
            }
            else blockNewlyEnded = false;

            cfg.addIfNotExist(elseEnd_ControlFlowGraph_node, ifEnd_ControlFlowGraph_node);
        }
        else {
            cfg.addIfNotExist(if_ControlFlowGraph_node, ifEnd_ControlFlowGraph_node);
        }
        ifEnd_ControlFlowGraph_node.setId(lineNumberCounter++);

        lastControlFlowGraphNode = ifEnd_ControlFlowGraph_node;
    }

    private void caseStatement(ControlFlowGraphNode caseControlFlowGraphNode) {
        OpenPearlParser.Case_statementContext case_ctx = ((OpenPearlParser.Unlabeled_statementContext) caseControlFlowGraphNode.getCtx()).sequential_control_statement().case_statement();
        OpenPearlParser.Case_statement_selection1Context selection1 = case_ctx.case_statement_selection1();
        OpenPearlParser.Case_statement_selection2Context selection2 = case_ctx.case_statement_selection2();
        OpenPearlParser.Case_statement_selection_outContext out_ctx;
        List<ParserRuleContext> alts_ctxs;
        if(selection1 != null) {
            alts_ctxs = new ArrayList<>(selection1.case_statement_selection1_alt());
            out_ctx = case_ctx.case_statement_selection1().case_statement_selection_out();
        }
        else {
            alts_ctxs = new ArrayList<>(selection2.case_statement_selection2_alt());
            out_ctx = case_ctx.case_statement_selection2().case_statement_selection_out();
        }
        ControlFlowGraphNode caseEndControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.CASE_END, null, 0, currentDepth);
        currentDepth++;

        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, caseControlFlowGraphNode);
        }
        else blockNewlyEnded = false;

        alts_ctxs.forEach(alt_ctx -> {
            ControlFlowGraphNode altStartControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.ALT_START, alt_ctx, lineNumberCounter++, currentDepth);
            lastControlFlowGraphNode = altStartControlFlowGraphNode;
            currentDepth++;
            visitChildren(alt_ctx);
            currentDepth--;
            ControlFlowGraphNode altEndControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.ALT_END, null, lineNumberCounter++, currentDepth);
            if(!blockNewlyEnded) {
                cfg.addIfNotExist(lastControlFlowGraphNode, altEndControlFlowGraphNode);
            }
            else blockNewlyEnded = false;

            cfg.addIfNotExist(altEndControlFlowGraphNode, caseEndControlFlowGraphNode);
            cfg.addIfNotExist(caseControlFlowGraphNode, altStartControlFlowGraphNode);
        });

        if(out_ctx != null) {
            ControlFlowGraphNode outStartControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.OUT_START, out_ctx, lineNumberCounter++, currentDepth);
            lastControlFlowGraphNode = outStartControlFlowGraphNode;
            currentDepth++;
            visitChildren(out_ctx);
            currentDepth--;
            ControlFlowGraphNode outEndControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.OUT_END, null, lineNumberCounter++, currentDepth);
            if(!blockNewlyEnded) {
                cfg.addIfNotExist(lastControlFlowGraphNode, outEndControlFlowGraphNode);
            }
            else blockNewlyEnded = false;

            cfg.addIfNotExist(outEndControlFlowGraphNode, caseEndControlFlowGraphNode);
            cfg.addIfNotExist(caseControlFlowGraphNode, outStartControlFlowGraphNode);
        }
        else {
            cfg.addIfNotExist(caseControlFlowGraphNode, caseEndControlFlowGraphNode);
        }

        caseEndControlFlowGraphNode.setId(lineNumberCounter++);
        lastControlFlowGraphNode = caseEndControlFlowGraphNode;
    }

    private void gotoStatement(ControlFlowGraphNode gotoControlFlowGraphNode) {
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, gotoControlFlowGraphNode);
        }
        else blockNewlyEnded = false;

        OpenPearlParser.GotoStatementContext goto_ctx = ((OpenPearlParser.Unlabeled_statementContext) gotoControlFlowGraphNode.getCtx()).gotoStatement();
        String labelText = goto_ctx.ID().getText();
        ControlFlowGraphNode label = gotoLabels.get(labelText);
        if(label != null) {
            cfg.addIfNotExist(gotoControlFlowGraphNode, label);
        }
        List<ControlFlowGraphNode> labelGotoControlFlowGraphNodes = gotoNodes.get(labelText);
        if(labelGotoControlFlowGraphNodes == null) {
            List<ControlFlowGraphNode> newList = new ArrayList<>();
            newList.add(gotoControlFlowGraphNode);
            gotoNodes.put(labelText, newList);
        }
        else {
            labelGotoControlFlowGraphNodes.add(gotoControlFlowGraphNode);
        }
        lastControlFlowGraphNode = gotoControlFlowGraphNode;
        blockNewlyEnded = true;
    }

    @Override
    public Void visitLabel_statement(OpenPearlParser.Label_statementContext ctx) {
        ControlFlowGraphNode labelControlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.JUMP_LABEL, ctx, lineNumberCounter++, currentDepth);
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, labelControlFlowGraphNode);
        }
        else blockNewlyEnded = false;
        String labelText = ctx.ID().getText();

        List<ControlFlowGraphNode> labelGotoControlFlowGraphNodes = gotoNodes.get(labelText);
        if(labelGotoControlFlowGraphNodes != null) {
            labelGotoControlFlowGraphNodes.forEach(labelGotoControlFlowGraphNode -> {
                cfg.addIfNotExist(labelGotoControlFlowGraphNode, labelControlFlowGraphNode);
            });
        }

        gotoLabels.put(labelText, labelControlFlowGraphNode);

        lastControlFlowGraphNode = labelControlFlowGraphNode;
        return null;
    }

    @Override
    public Void visitUnlabeled_statement(OpenPearlParser.Unlabeled_statementContext ctx) {
        statement(ctx);
        return null;
    }

    @Override
    public Void visitVariableDeclaration(OpenPearlParser.VariableDeclarationContext ctx) {
        statement(ctx);
        return null;
    }

    private void statement(ParserRuleContext ctx) {
        OpenPearlParser.Unlabeled_statementContext ul_ctx = null;
        OpenPearlParser.ReturnStatementContext r_ctx = null;
        OpenPearlParser.ExitStatementContext e_ctx = null;
        OpenPearlParser.LoopStatementContext l_ctx = null;
        OpenPearlParser.Sequential_control_statementContext sc_ctx = null;
        OpenPearlParser.If_statementContext i_ctx = null;
        OpenPearlParser.Case_statementContext c_ctx = null;
        OpenPearlParser.GotoStatementContext goto_ctx = null;
        OpenPearlParser.Realtime_statementContext real_ctx = null;
        OpenPearlParser.Task_control_statementContext control_ctx = null;
        OpenPearlParser.Task_terminatingContext terminate_ctx = null;
        if (ctx instanceof OpenPearlParser.Unlabeled_statementContext)
            ul_ctx = (OpenPearlParser.Unlabeled_statementContext) ctx;
        if(ul_ctx != null) {
            r_ctx = ul_ctx.returnStatement();
            e_ctx = ul_ctx.exitStatement();
            l_ctx = ul_ctx.loopStatement();
            goto_ctx = ul_ctx.gotoStatement();
            sc_ctx = ul_ctx.sequential_control_statement();
            real_ctx = ul_ctx.realtime_statement();
            if(sc_ctx != null) {
                i_ctx = sc_ctx.if_statement();
                c_ctx = sc_ctx.case_statement();
            }
            if(real_ctx != null) {
                control_ctx = real_ctx.task_control_statement();
                if(control_ctx != null) {
                    terminate_ctx = control_ctx.task_terminating();
                }
            }
        }

        ControlFlowGraphNode controlFlowGraphNode = new ControlFlowGraphNode(ControlFlowGraphNodeType.OTHER, ctx, lineNumberCounter++, currentDepth);
        if(l_ctx != null) {
            controlFlowGraphNode.setType(ControlFlowGraphNodeType.LOOP_START);
            loopStatement(controlFlowGraphNode);
        }
        else if(i_ctx != null) {
            controlFlowGraphNode.setType(ControlFlowGraphNodeType.IF_START);
            ifStatement(controlFlowGraphNode);
        }
        else if(c_ctx != null) {
            controlFlowGraphNode.setType(ControlFlowGraphNodeType.CASE_START);
            caseStatement(controlFlowGraphNode);
        }
        else if(goto_ctx != null) {
            controlFlowGraphNode.setType(ControlFlowGraphNodeType.GOTO);
            gotoStatement(controlFlowGraphNode);
        }
        else if(r_ctx != null) {
            controlFlowGraphNode.setType(ControlFlowGraphNodeType.RETURN);
            returnStatement(controlFlowGraphNode);
        }
        else if(e_ctx != null) {
            controlFlowGraphNode.setType(ControlFlowGraphNodeType.EXIT);
            exitStatement(controlFlowGraphNode);
        }
        else if(terminate_ctx != null) {
            controlFlowGraphNode.setType(ControlFlowGraphNodeType.TERMINATE);
            terminateStatement(controlFlowGraphNode);
        }
        else {
            if(!blockNewlyEnded) {
                cfg.addIfNotExist(lastControlFlowGraphNode, controlFlowGraphNode);
            }
            else blockNewlyEnded = false;
            lastControlFlowGraphNode = controlFlowGraphNode;
        }
    }

    private void returnStatement(ControlFlowGraphNode controlFlowGraphNode) {
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, controlFlowGraphNode);
        }
        lastControlFlowGraphNode = controlFlowGraphNode;

        cfg.addIfNotExist(controlFlowGraphNode, endControlFlowGraphNode);
        blockNewlyEnded = true;
    }

    private void exitStatement(ControlFlowGraphNode controlFlowGraphNode) {
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, controlFlowGraphNode);
        }
        lastControlFlowGraphNode = controlFlowGraphNode;

        exitedAtBlock.get(currentBlock).add(controlFlowGraphNode);
        blockNewlyEnded = true;
    }

    private void terminateStatement(ControlFlowGraphNode controlFlowGraphNode) {
        if(!blockNewlyEnded) {
            cfg.addIfNotExist(lastControlFlowGraphNode, controlFlowGraphNode);
        }
        else blockNewlyEnded = false;

        OpenPearlParser.Task_terminatingContext terminate_ctx = ((OpenPearlParser.Unlabeled_statementContext) controlFlowGraphNode.getCtx()).realtime_statement().task_control_statement().task_terminating();
        String taskName = null;
        if(terminate_ctx.name() != null) {
            taskName = terminate_ctx.name().getText();
        }
        if(taskName == null || taskName.equals(currentTaskName)) {
            cfg.addIfNotExist(controlFlowGraphNode, endControlFlowGraphNode);
            blockNewlyEnded = true;
        }
        lastControlFlowGraphNode = controlFlowGraphNode;
    }
}
