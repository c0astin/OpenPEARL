package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;

import org.antlr.v4.runtime.ParserRuleContext;

public class SequentialNode extends ControlFlowGraphNode {
    
    public SequentialNode(ParserRuleContext ctx) {
        super(ctx);
    }
    
    public SequentialNode(SequentialNode  other) {
        super(other.getCtx());
    }


}
