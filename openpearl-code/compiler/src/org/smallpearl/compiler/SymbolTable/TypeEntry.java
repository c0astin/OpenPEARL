package org.smallpearl.compiler.SymbolTable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.smallpearl.compiler.TypeDefinition;
import org.smallpearl.compiler.TypeStructure;

public class TypeEntry extends SymbolTableEntry {

    private TypeStructure    m_structure;

    public TypeEntry() {
    }

    public TypeEntry(String name, TypeStructure struct, ParserRuleContext ctx)
    {
        super(name);
        this.m_ctx = ctx;
        this.m_structure = struct;
    }

    public String toString(int level) {
        return indentString(level) +
                super.toString(level) +
                "type " + m_structure;
    }



    public TypeDefinition getType() { return m_structure; }
}