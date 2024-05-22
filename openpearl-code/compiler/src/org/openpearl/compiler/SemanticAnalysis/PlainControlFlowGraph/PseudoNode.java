package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

public class PseudoNode extends ControlFlowGraphNode {
    
    private int m_typeOfNode;
    
    public static final int procEntry = 0, procEnd=1, taskEntry=2, taskEnd=3, blockBegin=4, blockEnd=5,
           ifThen=6, ifElse=7, ifFin=8, repeatBegin = 9, repeatWhile = 10, repeatEnd=11, 
           caseFin = 12, caseAlt=13, caseOut = 14, cppInline = 15,
           repeatBodyBegin = 16, repeatBodyEnd=17, sigReaction = 18,
           lastType = 18;

    private static final String typeAsString[] = {"PROC ENTRY", "PROC END", "TASK ENTRY", "TASK END",
            "BLOCK BEGIN", "BLOCK END", "THEN", "ELSE", "FIN", "REPEAT BEGIN", "REPEAT WHILE", "REPEAT END",
            "CASE FIN", "ALT", "OUT" ,"__cpp__",
            "REPEAT BODY BEGIN","REPEAT BODY END","SIG REACTION"
            };
    
    public PseudoNode(ParserRuleContext ctx, int type) {
        super(ctx);
        m_typeOfNode = type;
    }
    
    public PseudoNode( PseudoNode other) {
        super(other.getCtx());
        m_typeOfNode = other.getNodeType();
    }

    public int getNodeType() {
        return m_typeOfNode;
    }
    
    public String printCtx(int length) {
 
        String readable = "--??--";
        if (m_typeOfNode >=0 && m_typeOfNode <= lastType) {
            readable = typeAsString[m_typeOfNode];
        }
        
        if (readable.length() > length) {
           readable = readable.substring(0, length) + "...";
        }
                
        return readable;
    }
    
}
