package org.openpearl.compiler;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

public class TypeRefChar extends TypeDefinition {
    TypeRefChar() {
        /* note
         * the grammar selects 'REF' (... | typeRefChar | ...)
         * thus this type is the baseType of a TypeReference object
         */
        super("CHAR()");
    }
    
//    public String toString() {
//        return getName();
//    }
    @Override

    
    public String toString4IMC(boolean isInStructure) {
        return toString();
    }
    
    public boolean equals(Object other) {
        if (!(other instanceof TypeRefChar)) {
            return false;
        }

        return true;
    }
    
    public ST toST(STGroup group) {
        ST st = group.getInstanceOf("refChar_type");
        return st;
    }
    
}
