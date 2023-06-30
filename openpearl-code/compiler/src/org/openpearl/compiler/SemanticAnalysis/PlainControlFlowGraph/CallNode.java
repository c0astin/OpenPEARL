package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;

import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.ContextUtilities;


public class CallNode extends ControlFlowGraphNode {
    
       
    public CallNode(ParserRuleContext ctx) {
        super(ctx);
    }
    
    public CallNode(CallNode other) {
        super(other.getCtx());
    }

  public String printCtx(int length) {
        
        String stmt = ContextUtilities.printCtx(getCtx(), length);
        if (!(stmt.startsWith("CALL "))) {
            stmt = "CALL "+ ContextUtilities.printCtx(getCtx(), length-5);
        }
       
        return stmt;
    }

}
