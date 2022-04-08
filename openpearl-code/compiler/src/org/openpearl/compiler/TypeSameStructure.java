package org.openpearl.compiler;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

/**
 * placeholder for a structure component which refers the top level structure
 * e.g. TYPE listElement STRUCT [ next REF listElement, .... ];
 * 
 * during parsing the type definition the top level structure changes with each detected structure component .
 * After the complete parsing we can derive the type of the top level structure
 * 
 * Without this class we would get endless recursions in toString()
 */
public class TypeSameStructure extends TypeDefinition {

    private UserDefinedTypeStructure m_containerStructure=null;
    
    public TypeSameStructure(UserDefinedTypeStructure containerStructure) {
        m_containerStructure = containerStructure;
    }
    
    public String toString() {
      // String s = m_containerStructure.toString();
       //return m_containerStructure.toString() + 
       //        " (aka:" +((TypeStructure)(m_containerStructure.getStructuredType())).getStructureName()+")";
        return m_containerStructure.getName();
    }
    
    @Override
    public String toString4IMC(boolean isInStructure) {
        // this should never be used, since the export to the IMC should be
        // done with the real types
        // but this method is also used for error messages
        return m_containerStructure.getName();
    }
    
    public ST toST(STGroup group) {
        return m_containerStructure.toST(group);
    }

    public UserDefinedTypeStructure getContainerStructure() {
        return m_containerStructure;
    }
    
    public boolean equals(Object other) {
       TypeStructure ts=null;
        if (other instanceof UserDefinedTypeStructure) {
            ts = (TypeStructure)(((UserDefinedTypeStructure)other).getStructuredType());
        }
        if (other instanceof TypeSameStructure) {
            ts = (TypeStructure)((TypeSameStructure)other).getContainerStructure().getStructuredType();
        }

        if (ts == null) return false;
        return m_containerStructure.getStructuredType().equals(ts);
    }
}
