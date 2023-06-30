package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;

import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.OpenPearlParser.Case_statementContext;

public class CaseNode extends ControlFlowGraphNode {
    
    // m_alternatives index 0 is OUT
    //                index 1 ... is ALT
    // this is treated independent of case version 1 and 2
    
    private ControlFlowGraphNode m_finNode;
    
    public CaseNode(ParserRuleContext ctx) {
        super(ctx);
        m_finNode=null;
   }

    public CaseNode(CaseNode other) {
        super(other.getCtx());
        for (int i=0; i<other.m_alternatives.size(); i++) {
           m_alternatives.add(null);
        }
        m_finNode=null;
    }
    
    public ControlFlowGraphNode getOutNode() {
        return m_alternatives.get(0);
    }

    public void setOutNode(ControlFlowGraphNode outNode) {
        m_alternatives.set(0, outNode);
    }

    /**
     * get the index alternative index starts with 1, as usual in PEARL
     * @param index the desired alternative
     * @return the desired node
     */
    public ControlFlowGraphNode getAltNode(int index) {
        return m_alternatives.get(index); // starts with 1
    }

    /**
     * set the index alternative index starts with 1, as usual in PEARL
     * @param altNode a PseudoNode
     * @param index the desired alternative
     * @return
     */
    public void setAltNode(ControlFlowGraphNode altNode, int index) {
        m_alternatives.set(index, altNode);
    }

    /* add an vector entry for a new alternative
     */
    public void addAlternative() {
        m_alternatives.add(null);
    }
        
    public String printCtx(int length) {
       String result = "CASE ";
       
       Case_statementContext caseCtx = (Case_statementContext)(getCtx()); 
       ParserRuleContext expr = null;
       if (caseCtx.case_statement_selection1() != null) {
            expr = caseCtx.case_statement_selection1().expression();
       }          
       if (caseCtx.case_statement_selection2() != null) {
           expr = caseCtx.case_statement_selection2().expression();
       }
       
       ParserRuleContext prevousCtx = getCtx();
       super.setCtx(expr);
       result += super.printCtx(length-5);
       super.setCtx(prevousCtx);   
       
       return result;
    }

    /* the fin location is the character index of 'FIN' in the source code
     * this allowes to ignore nodes outside the CASE statement like 
     * any END in PROC as follower of an RETURN     
     */
    public void setFinNode(ControlFlowGraphNode n) {
        m_finNode = n;
    }
    
    public ControlFlowGraphNode getFinNode() {
        return m_finNode;
    }


}
