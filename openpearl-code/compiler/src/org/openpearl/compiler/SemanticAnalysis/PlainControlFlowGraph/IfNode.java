package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;

import org.antlr.v4.runtime.ParserRuleContext;

public class IfNode extends ControlFlowGraphNode {
    
    // m_alternatives index 0 is THEN
    //                index 1 is ELSE
    
    //                
    ControlFlowGraphNode m_fin;
    
    public IfNode(ParserRuleContext ctx) {
        super(ctx);
        m_alternatives.add(null);   // add else      
       
    }
    
    public IfNode(IfNode other) {
        super(other.getCtx());
        m_alternatives.add(null);   // add else
    }

    public ControlFlowGraphNode getThenNode() {
        return m_alternatives.get(0);
    }

    public void setThenNode(ControlFlowGraphNode thenNode) {
        m_alternatives.set(0, thenNode);
    }

    public ControlFlowGraphNode getElseNode() {
        return m_alternatives.get(1);
    }

    public void setElseNode(ControlFlowGraphNode elseNode) {
        m_alternatives.set(1, elseNode);
    }
    
    public void setFinNode(ControlFlowGraphNode n) {
        //m_alternatives.set(0, n);
        m_fin = n;
    }
    
    public ControlFlowGraphNode getFinNode() {
       // return m_alternatives.get(0);
        return m_fin;
    }


}
