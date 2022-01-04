package org.openpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;



public class ReferenceInitializer extends Initializer {
    private SymbolTableEntry m_symbolTableEntry;
    private SymbolTable m_symbolTable;
    
    public ReferenceInitializer(ParserRuleContext ctx, SymbolTableEntry entry, SymbolTable table) {
        super(ctx);
        m_symbolTableEntry = entry;
        m_symbolTable = table;
    }
    
    public SymbolTableEntry getSymbolTableEntry() {
        return m_symbolTableEntry;
    }
    public SymbolTable getSymbolTable() {
        return m_symbolTable;
    }

}
