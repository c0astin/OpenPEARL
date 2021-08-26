package org.smallpearl.compiler.SymbolTable;

import org.smallpearl.compiler.SmallPearlParser;

public class SemaphoreEntry extends SymbolTableEntry {
    public SemaphoreEntry() {}

    public SemaphoreEntry(String name, SmallPearlParser.SemaDenotationContext ctx) {
        super(name);
        this.m_ctx = ctx;
    }

    public String toString(int level) {
        return indentString(level) + super.toString(level) + "var " + "SEMA";
    }



}
