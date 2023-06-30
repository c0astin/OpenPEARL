package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;

import java.util.Vector;
import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.AST;
import org.openpearl.compiler.ASTAttribute;
import org.openpearl.compiler.TypeReference;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;

public class FunctionCallsInNextStatement extends ControlFlowGraphNode {
    private AST m_ast = null;
    private Vector<ParserRuleContext> m_calls;
    
    public FunctionCallsInNextStatement(AST ast, ParserRuleContext ctx, Vector<ParserRuleContext> calls) {
        super(ctx);
        m_calls = calls;
        m_ast = ast;
    }
    
    public FunctionCallsInNextStatement(FunctionCallsInNextStatement other) {
        super(other.getCtx());
        m_calls = other.m_calls;
        m_ast =other.m_ast;
    }
    
    public String printCtx(int length) {
        
        String readable = "function calls in next stmt:";
        
        for (int i=0; i<m_calls.size(); i++) {
            
            ASTAttribute attr = m_ast.lookup(m_calls.get(i));
            String name="";
            SymbolTableEntry se = attr.getSymbolTableEntry();
            if (se instanceof VariableEntry) {
                if ( ((VariableEntry)se).getType() instanceof TypeReference) {
                   name="CONT ";
                }
               
            }
            name += attr.getSymbolTableEntry().getName();
            readable += "\n"+ name;
        }
        
        return readable;
    }
    
    public Vector<ParserRuleContext> getCalls() {
        // returns a vector of NameContext
        return m_calls;
    }

}
