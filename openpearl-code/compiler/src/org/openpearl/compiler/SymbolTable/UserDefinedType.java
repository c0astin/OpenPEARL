package org.openpearl.compiler.SymbolTable;

import org.openpearl.compiler.OpenPearlParser;
import org.openpearl.compiler.TypeDefinition;

public class UserDefinedType extends SymbolTableEntry {
    private TypeDefinition m_type;
    
    public UserDefinedType(String nameOfType,OpenPearlParser.IdentifierContext ctx) {
        super(nameOfType);
        this.m_ctx = ctx;
        m_type = null;
    }
      
    public TypeDefinition getType() {
        return m_type;
    }
    public void setType(TypeDefinition type) {
        this.m_type = type;
    }
    
    public String toString(int level) {
        
        return  indentString(level) + super.toString(level) + "TYPE (aka: " + m_type+" )";
    }

}
