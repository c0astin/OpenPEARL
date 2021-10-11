/*
 * [The "BSD license"]
 * *  Copyright (c) 2012-2021 Marcel Schaible
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
 *     derived from this software without specific prior written permissision.
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

import org.smallpearl.compiler.SmallPearlParser;
import org.smallpearl.compiler.TypeDefinition;
import org.smallpearl.compiler.TypeFixed;
import org.smallpearl.compiler.TypeFloat;
import org.smallpearl.compiler.TypeBit;
import org.smallpearl.compiler.TypeChar;

public class LengthEntry extends SymbolTableEntry {
    private TypeDefinition m_type;
    private SmallPearlParser.LengthDefinitionContext m_ctx;

    public LengthEntry() {
    }

    public LengthEntry(TypeDefinition type, SmallPearlParser.LengthDefinitionContext ctx) {
        this.m_ctx = ctx;
        this.m_type = type;
        this.setName("~LENGTH_" + type.getName() + "~");
    }

    public String toString(int level) {
        String s = indentString(level) + super.toString(level) + this.m_type.getName();
        if ( this.m_type instanceof TypeFixed) {
            s += "(" + ((TypeFixed)this.m_type).getPrecision() +")";
        }
        else if ( this.m_type instanceof TypeFloat) {
            s += "(" + ((TypeFloat)this.m_type).getPrecision() +")";
        }
        else if ( this.m_type instanceof TypeBit) {
            s += "(" + ((TypeBit)this.m_type).getPrecision() +")";
        }
        else if ( this.m_type instanceof TypeChar) {
            s += "(" + ((TypeChar)this.m_type).getPrecision() +")";
        }

        return s;
    }

 // deprecated. is now in SymboleTableEntry       
//    public int getSourceLineNo() {
//        return m_ctx.getStart().getLine();
//    }
//
//    public int getCharPositionInLine() {
//        return m_ctx.getStart().getCharPositionInLine();
//    }

    public TypeDefinition getType() { return this.m_type; }
}
