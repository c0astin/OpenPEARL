package org.smallpearl.compiler.SymbolTable;

import org.smallpearl.compiler.SmallPearlParser;

/****
 *
 * LabelEntry extends SymbolTableEntry by adding a useCount attribute
 * 
 * the useCount is used to detect unused labels
 *
 */
public class LabelEntry extends SymbolTableEntry {

    private int m_useCount;
    
    /**
     * Construct this with null data fields.
     */
    public LabelEntry() {
      super();
    }

    /**
     * Construct this with the given data field values.
     */
    public LabelEntry(String name, SmallPearlParser.Label_statementContext ctx) {
        super(name);
        super.m_ctx = ctx;
        m_useCount=0;
    }

    /**
     * Return the string rep of this, which consists of the return value of
     * super.toString, plus the values of this.isRef and this.memoryLocation.
     */
    public String toString(int level) {
        return indentString(level) + super.toString(level) + "label ";
    }
    

}