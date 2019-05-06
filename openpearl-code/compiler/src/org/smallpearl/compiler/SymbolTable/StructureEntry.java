
/*
 * [The "BSD license"]
 *  Copyright (c) 2012-2018 Marcel Schaible
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.smallpearl.compiler.SymbolTable;

import org.smallpearl.compiler.*;
import org.smallpearl.compiler.Exception.InternalCompilerErrorException;

import java.util.LinkedList;

public class StructureEntry extends SymbolTableEntry {

    public StructureEntry() {
        m_ctx = null;
        m_listOfStructureComponents = new LinkedList<>() ;
    }

    public StructureEntry(String name, SmallPearlParser.StructureDenotationContext ctx, SymbolTable scope) {
        super(name);
        this.m_ctx = ctx;
        this.scope = scope;
    }

    public String toString(int level) {
        return indentString(level) + super.toString(level) + "struct" + scopeString(level) + ":" +getStructureName();
    }


    protected String scopeString(int m_level) {
        return scope == null ? "" : "\n " + indentString(m_level) +
                scope.toString(m_level);
    }

    public int getSourceLineNo() {
        return m_ctx.getStart().getLine();
    }

    public int getCharPositionInLine() {
        return m_ctx.getStart().getCharPositionInLine();
    }

    /*
        Datatype      letter   REF
        --------------------------
        FIXED         A        a
        FLOAT         B        b
        BIT           C        c
        CHARACTER     D        d
        CLOCK         E        e
        DURATION      F        f
        TASK                   g
        PROC                   h
        SEMA          I        i
        BOLT          J        j
        STRUCT        S        s
     */

    private String getDataTypeEncoding(TypeDefinition type) {
        if ( type instanceof TypeFixed)           return "A" + type.getPrecision().toString();
        if ( type instanceof TypeFloat)           return "B" + type.getPrecision().toString();
        if ( type instanceof TypeBit)             return "C" + type.getPrecision().toString();
        if ( type instanceof TypeChar)            return "D" + type.getPrecision().toString();
        if ( type instanceof TypeClock)           return "E" + type.getPrecision().toString();
        if ( type instanceof TypeDuration)        return "F" + type.getPrecision().toString();
        if ( type instanceof TypeSemaphore)       return "I" + type.getPrecision().toString();
        if ( type instanceof TypeBolt)            return "J" + type.getPrecision().toString();
        if ( type instanceof TypeStructure)       return "S" + type.getPrecision().toString();

        if ( type instanceof TypeReference) {
            TypeReference reftype = (TypeReference) type;

            if ( reftype.getBaseType() instanceof TypeFixed)           return "a" + type.getPrecision().toString();
            if ( reftype.getBaseType() instanceof TypeFloat)           return "b" + type.getPrecision().toString();
            if ( reftype.getBaseType() instanceof TypeBit)             return "c" + type.getPrecision().toString();
            if ( reftype.getBaseType() instanceof TypeChar)            return "d" + type.getPrecision().toString();
            if ( reftype.getBaseType() instanceof TypeClock)           return "e" + type.getPrecision().toString();
            if ( reftype.getBaseType() instanceof TypeDuration)        return "f" + type.getPrecision().toString();
            if ( reftype.getBaseType() instanceof TypeTask)            return "g";
            if ( reftype.getBaseType() instanceof TypeProcedure)       return "h";
            if ( reftype.getBaseType() instanceof TypeSemaphore)       return "i";
            if ( reftype.getBaseType() instanceof TypeBolt)            return "j";
            if ( reftype.getBaseType() instanceof TypeStructure)       return "s";
        }

        return "~?~";
    }

    public String getStructureName() {
        String sname = "";
        for (int i = 0; i < m_listOfStructureComponents.size(); i++ ) {
            TypeDefinition typ = m_listOfStructureComponents.get(i).getType();
            sname += getDataTypeEncoding(typ);
        }
        return sname;
    }

    /** Local scope for this function. */
    public SymbolTable scope;

    private SmallPearlParser.StructureDenotationContext m_ctx;
    private LinkedList<StructureComponentEntry> m_listOfStructureComponents;
}
