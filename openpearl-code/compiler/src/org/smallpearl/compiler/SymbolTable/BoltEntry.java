package org.smallpearl.compiler.SymbolTable;

import org.antlr.v4.runtime.ParserRuleContext;

public class BoltEntry extends SymbolTableEntry {
    public BoltEntry() {
    }

    public BoltEntry(String name, ParserRuleContext ctx)
    {
        super(name);
        this.m_ctx = ctx;
    }

    public String toString(int level) {
        return indentString(level) + super.toString(level) + "var " + "BOLT";
    }
 // deprecated. is now in SymboleTableEntry       
//    public int getSourceLineNo() {
//        return m_ctx.getStart().getLine();
//    }
//    public int getCharPositionInLine() {
//        return m_ctx.getStart().getCharPositionInLine();
//    }
//    private SmallPearlParser.BoltDeclarationContext m_ctx;
}