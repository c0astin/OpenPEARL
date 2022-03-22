package org.openpearl.compiler;

public class UserDefinedTypeStructure extends TypeDefinition {
    private TypeDefinition m_structuredType;
    
    public String toString() {
        return "(UserDefinedStruct) "+getName();
    }
    
    public UserDefinedTypeStructure(String name) {
        super(name);
        m_structuredType = null;
    }
    
    @Override
    public String toString4IMC(boolean isInStructure) {
        if (m_structuredType != null) {
            return m_structuredType.toString4IMC(isInStructure);
        }
        return null;
    }

    public TypeDefinition getStructuredType() {
        return m_structuredType;
    }

    public void setStructuredType(TypeDefinition structuredType) {
        this.m_structuredType = structuredType;
    }

}
