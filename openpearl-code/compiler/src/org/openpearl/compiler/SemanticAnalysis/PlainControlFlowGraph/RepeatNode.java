package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;

import org.antlr.v4.runtime.ParserRuleContext;

public class RepeatNode extends ControlFlowGraphNode {
    

    // m_alternatives index 1 is the following statement of the loop (eg. RepeatEnd-PseudoNode)
    //                index 0 is loop body or RepeatNode of WHILE

    private RepeatType m_isForOrWhile;
    public static enum RepeatType {IsFor, IsWhile, IsEndless};

    public RepeatNode(ParserRuleContext ctx, RepeatType isForOrWhile) {
        super(ctx);
        m_alternatives.add(null);  // add loop end
        m_isForOrWhile = isForOrWhile;
    }

    public RepeatNode(RepeatNode other) {
        super(other.getCtx());
        m_isForOrWhile = other.m_isForOrWhile;
        m_alternatives.add(null);  // add loop end
    }

    public ControlFlowGraphNode getBodyNode() {
        return m_alternatives.get(0);
    }

    public void setBodyNode(ControlFlowGraphNode bodyNode) {
        m_alternatives.set(0, bodyNode);
    }
    
    public ControlFlowGraphNode getEndNode() {
        return m_alternatives.get(1);
    }
    
    public void setEndNode(ControlFlowGraphNode endNode) {
        m_alternatives.set(1, endNode);
    }
    
    public RepeatType getRepeatType() {
        return m_isForOrWhile;
    }
  
}
