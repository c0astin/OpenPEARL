package org.openpearl.compiler;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

/**
 * stores user defined type of kind simpleType like in:
 * TYPE MyInt FIXED(7);
 * 
 * The idea is that the error messages in type mismatches are 
 * printed with the user defined type and its base type similar 
 * to the behavior if the g++
 */
public class UserDefinedSimpleType extends TypeDefinition {

    private TypeDefinition m_simpleType;
    
    public UserDefinedSimpleType(String name, TypeDefinition simpleType) {
        super(name);
        m_simpleType = simpleType;
    }
    
    public String toString() {
        return getName() +   " {aka: " +m_simpleType+"}";
    }
    
    @Override
    public String toString4IMC(boolean isInStructure) {
        if (m_simpleType != null) {
            return m_simpleType.toString();
        }
        return null;
    }

    public TypeDefinition getSimpleType() {
        return m_simpleType;
    }

    public void setSimpleType(TypeDefinition simpleType) {
        this.m_simpleType = simpleType;
    }
    
    public ST toST(STGroup group) {
       ST st = m_simpleType.toST(group); 
       if (hasAssignmentProtection()) {
           ST inv = group.getInstanceOf("const_type");
           inv.add("type", st);
           st = inv;
       }
       return st;
    }

}
