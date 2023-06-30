package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;

import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.ContextUtilities;


public class LabelNode extends ControlFlowGraphNode {
    
    
  public LabelNode(ParserRuleContext ctx) {
        super(ctx);
  }
  
  public LabelNode(LabelNode other) {
      super(other.getCtx());
  }

  public String printCtx(int length) {
        
        String stmt = ContextUtilities.printCtx(getCtx(), length-1);
        if (!stmt.endsWith(":")) {
            stmt += ':';        
        }
        return stmt;
    }

}
