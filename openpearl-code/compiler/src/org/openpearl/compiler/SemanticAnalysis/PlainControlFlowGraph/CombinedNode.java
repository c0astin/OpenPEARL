package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;

import java.util.Arrays;
import org.antlr.v4.runtime.ParserRuleContext;

public class CombinedNode extends ControlFlowGraphNode {
    private String m_lines="";
    
    public CombinedNode(ParserRuleContext ctx) {
        super(ctx);
        int s= ctx.getStart().getLine();
        int e = ctx.getStop().getLine();
        if (s!=e) {
            m_lines= ""+s+"-"+e;
        } else {
            m_lines= ""+s;
        }
    }
    
    public CombinedNode(CombinedNode  other) {
        super(other.getCtx());
        int s= other.getCtx().getStart().getLine();
        int e = other.getCtx().getStop().getLine();
        if (s!=e) {
            m_lines= ""+s+"-"+e;
        } else {
            m_lines= ""+s;
        }
    }

    public String printCtx(int length) {
        String result="lines: ";
        String parts[] = m_lines.split(",");
        for (int i=0; i<parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        Arrays.sort(parts);
        for (int i=0; i<parts.length; i++) {
            if (i>0) {result += ", ";}
            result += parts[i];
        }
        return result;
    }
    
    public void add(ControlFlowGraphNode n) {
        int s= n.getCtx().getStart().getLine();
        int e = n.getCtx().getStop().getLine();
        if (s!=e) {
            m_lines += ", "+s+"-"+e;
        } else {
            m_lines += ", "+s;
        }
    }
}
