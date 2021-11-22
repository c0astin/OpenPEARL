package org.smallpearl.compiler;

public class TypeRefChar extends TypeDefinition {
    TypeRefChar() {
        /* note
         * the grammar selects 'REF' (... | typeRefChar | ...)
         * thus this type is the baseType of a TypeReference object
         */
        super("CHAR()");
    }
}
