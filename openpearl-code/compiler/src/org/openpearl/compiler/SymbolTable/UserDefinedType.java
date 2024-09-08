package org.openpearl.compiler.SymbolTable;

import org.openpearl.compiler.OpenPearlParser;
import org.openpearl.compiler.TypeDefinition;
import org.openpearl.compiler.UserDefinedSimpleType;
import org.openpearl.compiler.UserDefinedTypeStructure;

public class UserDefinedType extends SymbolTableEntry {
    private TypeDefinition m_type;
    
    public UserDefinedType(OpenPearlParser.IdentifierContext ctx) {
        super(ctx.ID().toString());
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
        String s = indentString(level) + super.toString(level) + "TYPE " ;
    
        if (m_type instanceof UserDefinedSimpleType) {
           s += ((UserDefinedSimpleType)m_type).getSimpleType();
        } else {
            s += ((UserDefinedTypeStructure)m_type).toErrorString();
        }
        return s;
    }

}
