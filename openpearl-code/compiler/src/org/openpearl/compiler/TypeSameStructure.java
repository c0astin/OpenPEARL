package org.openpearl.compiler;

/**
 * placeholder for a structure component which refers the top level structure
 * e.g. TYPE listElement STRUCT [ next REF listElement, .... ];
 * 
 * during parsing the type definition the top level structure changes with each detected structure component .
 * After the complete parsing we can derive the type of the top level structure
 */
public class TypeSameStructure extends TypeDefinition {

    private UserDefinedTypeStructure m_containerStructure=null;
    
    public TypeSameStructure(UserDefinedTypeStructure containerStructure) {
        m_containerStructure = containerStructure;
    }
    
    public String toString() {
       // String s = m_containerStructure.toString();
       return m_containerStructure.toString() + 
               " (aka:" +((TypeStructure)(m_containerStructure.getStructuredType())).getStructureName()+")";
    }
    
    @Override
    public String toString4IMC(boolean isInStructure) {
        // this shoul never be used, since the export to the IMC should be
        // done with the real types
        return "~selfReference~";
    }

    public UserDefinedTypeStructure getContainerStructure() {
        return m_containerStructure;
    }
}
