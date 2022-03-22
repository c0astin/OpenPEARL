package org.openpearl.compiler;

public class UserDefinedSimpleType extends TypeDefinition {

    private TypeDefinition m_simpleType;
    
    public UserDefinedSimpleType(TypeDefinition simpleType) {
        m_simpleType = simpleType;
    }
    
    public String toString() {
        return toString4IMC(true);
    }
    
    @Override
    public String toString4IMC(boolean isInStructure) {
        if (m_simpleType != null) {
            return m_simpleType.toString4IMC(isInStructure);
        }
        return null;
    }

    public TypeDefinition getSimpleType() {
        return m_simpleType;
    }

    public void setSimpleType(TypeDefinition simpleType) {
        this.m_simpleType = simpleType;
    }

}
