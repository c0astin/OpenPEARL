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
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.HashSet;
import java.util.Set;

public class ControlFlowGraphNode {
    private Integer id;
    private Integer depth;
    private ControlFlowGraphNodeType type;
    private ParserRuleContext ctx;
    private String statement;
    private Set<ControlFlowGraphNode> inputNodes;
    private Set<ControlFlowGraphNode> outputNodes;
    private ControlFlowGraphVariableStack variableStack;

    public ControlFlowGraphNode() {
        id = 0;
        depth = 0;
        inputNodes = new HashSet<>();
        outputNodes = new HashSet<>();
        ctx = null;
        type = null;
        variableStack = null;
    }

    public ControlFlowGraphNode(ControlFlowGraphNodeType type, ParserRuleContext ctx, int id, int depth) {
        this.id = id;
        this.depth = depth;
        this.type = type;
        this.ctx = ctx;
        inputNodes = new HashSet<>();
        outputNodes = new HashSet<>();
        setStatement();
        variableStack = null;
    }

    public ControlFlowGraphNode(ControlFlowGraphNodeType type, ParserRuleContext ctx) {
        id = 0;
        depth = 0;
        this.type = type;
        this.ctx = ctx;
        inputNodes = new HashSet<>();
        outputNodes = new HashSet<>();
        setStatement();
        variableStack = null;
    }

    private void setStatement() {
        switch (type) {
            case ENTRY:
                statement = "Entry";
                break;
            case END:
                statement = "End";
                break;
            case BLOCK_START:
                statement = "Block Start";
                break;
            case BLOCK_END:
                statement = "Block End";
                break;
            case LOOP_START:
                statement = ctx.getText().substring(0, ctx.getText().indexOf("REPEAT"));
                break;
            case LOOP_BLOCK_START:
                statement = "Loop Start";
                break;
            case LOOP_REPEAT:
                statement = "Loop Repeat";
                break;
            case LOOP_END:
                statement = "Loop End";
                break;
            case IF_START:
                statement = ctx.getText().substring(0, ctx.getText().indexOf("THEN"));
                break;
            case IF_END:
                statement = "If End";
                break;
            case THEN_START:
                statement = "Then";
                break;
            case THEN_END:
                statement = "Then End";
                break;
            case ELSE_START:
                statement = "Else";
                break;
            case ELSE_END:
                statement = "Else End";
                break;
            case CASE_START:
                statement = ctx.getText().substring(0, ctx.getText().indexOf("ALT"));
                break;
            case CASE_END:
                statement = "Case End";
                break;
            case ALT_START:
                statement = ctx.getText().substring(0, ctx.getText().indexOf(")")+1);
                break;
            case ALT_END:
                statement = "Alt End";
                break;
            case OUT_START:
                statement = "Out Start";
                break;
            case OUT_END:
                statement = "Out End";
                break;
            case JUMP_LABEL:
                statement = "LABEL " + ctx.getText();
                break;
            case TERMINATE:
                statement = ctx.getText();
                break;
            case GOTO:
                statement = ctx.getText();
                break;
            case RETURN:
                statement = ctx.getText();
                break;
            case EXIT:
                statement = ctx.getText();
                break;
            case OTHER:
                statement = ctx.getText();
                break;
            default:
                statement = "";
        }
    }
 
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ControlFlowGraphNodeType getType() {
        return type;
    }

    public void setType(ControlFlowGraphNodeType type) {
        this.type = type;
        setStatement();
    }

    public ParserRuleContext getCtx() {
        return ctx;
    }

    public String getStatement() {
        return statement;
    }

    public Set<ControlFlowGraphNode> getInputNodes() {
        return inputNodes;
    }

    public Set<ControlFlowGraphNode> getOutputNodes() {
        return outputNodes;
    }

    public ControlFlowGraphVariableStack getVariableStack() {
        return variableStack;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setVariableStack(ControlFlowGraphVariableStack variableStack) {
        this.variableStack = variableStack;
    }

    public void addInputNode(ControlFlowGraphNode controlFlowGraphNode) {
        inputNodes.add(controlFlowGraphNode);
        controlFlowGraphNode.addOutputNode(this);
    }

    private void addOutputNode(ControlFlowGraphNode controlFlowGraphNode) {
        outputNodes.add(controlFlowGraphNode);
    }

    public void removeInputNode(ControlFlowGraphNode controlFlowGraphNode) {
        inputNodes.remove(controlFlowGraphNode);
        controlFlowGraphNode.removeOutPutNode(this);
    }

    private void removeOutPutNode(ControlFlowGraphNode controlFlowGraphNode) {
        outputNodes.remove(controlFlowGraphNode);
    }
}