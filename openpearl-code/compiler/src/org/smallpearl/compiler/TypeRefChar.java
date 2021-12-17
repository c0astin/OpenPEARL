package org.smallpearl.compiler;

public class TypeRefChar extends TypeDefinition {
    TypeRefChar() {
        /* note
         * the grammar selects 'REF' (... | typeRefChar | ...)
         * thus this type is the baseType of a TypeReference object
         */
        super("CHAR()");
    }
    
    public String toString() {
        return getName();
    }
    
    public String toString4IMC(boolean isInStructure) {
        return getName();
    }
    
}
