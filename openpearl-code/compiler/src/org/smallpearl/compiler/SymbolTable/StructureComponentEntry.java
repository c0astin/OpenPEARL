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

import org.antlr.v4.runtime.ParserRuleContext;
import org.smallpearl.compiler.Initializer;
import org.smallpearl.compiler.SmallPearlParser;
import org.smallpearl.compiler.TypeDefinition;

public class StructureComponentEntry extends SymbolTableEntry {

    private TypeDefinition m_type;
    private Boolean m_hasAssigmentProtection;
    private Boolean m_loopControlVariable;
    private Initializer m_initializer;

    public StructureComponentEntry() {
    }

    public StructureComponentEntry(String name, TypeDefinition type, ParserRuleContext ctx) {
        super(name);
        this.m_ctx = ctx;
        this.m_type = type;
        this.m_hasAssigmentProtection = false;
        this.m_loopControlVariable = false;
        this.m_initializer = null;
    }

    public StructureComponentEntry(String name,
                                   TypeDefinition type,
                                   Boolean hasAssigmentProtection,
                                   SmallPearlParser.VariableDenotationContext ctx,
                                   Initializer initializer) {
        super(name);
        this.m_ctx = ctx;
        this.m_type = type;
        this.m_hasAssigmentProtection = hasAssigmentProtection;
        this.m_loopControlVariable = false;
        this.m_initializer = initializer;
    }

    public String toString(int level) {
        String assigmenProtection = this.m_hasAssigmentProtection ? "INV" : "";
        String constant = null;

        return indentString(level) +
                super.toString(level) +
                "STRUCTCOMPONENT " +
                m_type + " " +
                assigmenProtection +
                (this.m_loopControlVariable ? " LC" : "") +
                (this.m_initializer != null ? "  INIT(" + m_initializer + ")" : "");
    }

 // deprecated. is now in SymboleTableEntry     
//   
//    public int getSourceLineNo() {
//        return m_ctx.getStart().getLine();
//    }
//
//    public int getCharPositionInLine() {
//        return m_ctx.getStart().getCharPositionInLine();
//    }
//
//    private ParserRuleContext m_ctx;

    public TypeDefinition getType() {
        return m_type;
    }

    //   public ParserRuleContext getConstantCtx() { return m_constantCtx; }
    public Boolean getAssigmentProtection() {
        return m_hasAssigmentProtection;
    }

    public Void setLoopControlVariable() {
        m_loopControlVariable = true;
        return null;
    }

    public Boolean getLoopControlVariable() {
        return m_loopControlVariable;
    }

    public Initializer getInitializer() {
        return m_initializer;
    }
}