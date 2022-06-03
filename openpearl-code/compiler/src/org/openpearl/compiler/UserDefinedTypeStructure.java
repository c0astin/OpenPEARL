package org.openpearl.compiler;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

/**
 * stores user defined type of kind simpleType like in:
 * TYPE MyInt STRUCT [ (a,b) FIXED(7) ];
 * 
 * The idea is that the error messages in type mismatches are 
 * printed with the user defined type and its base type similar 
 * to the behavior if the g++
 */
public class UserDefinedTypeStructure extends TypeDefinition {
    private TypeDefinition m_structuredType;
    
    public String toString() {
        return getName() + " {aka: "+ m_structuredType.toString() +"}" ;
    }
    
    public UserDefinedTypeStructure(String name) {
        super(name);
        m_structuredType = null;
    }
    

    public String toString4IMC(boolean isInStructure) {
        if (m_structuredType != null) {
            return ((TypeStructure)m_structuredType).toString4IMC(true);
        }
        return null;
    }

 
    public TypeDefinition getStructuredType() {
        return m_structuredType;
    }

    public void setStructuredType(TypeDefinition structuredType) {
        this.m_structuredType = structuredType;
    }
    
    public ST toST(STGroup group) {
        ST st= m_structuredType.toST(group);
        if (hasAssignmentProtection()) {
            ST inv = group.getInstanceOf("const_type");
            inv.add("type", st);
            st = inv;
        }
        return st;
     }

    public String toErrorString() {
        String s = getName() + " {aka: ";
        if (m_structuredType != null) {
            s += ((TypeStructure)m_structuredType).toErrorString();
        }
        s += "}";
        return s;
    }
    
    public boolean equals(Object other) {
        UserDefinedTypeStructure uts=null;
        if (other instanceof UserDefinedTypeStructure) {
            uts = (UserDefinedTypeStructure)other;
        }
        if (other instanceof TypeSameStructure) {
            uts = (UserDefinedTypeStructure)((TypeSameStructure)other).getContainerStructure();
        }
        if (uts == null) return false;
        return getName().equals(uts.getName());
    }

}
