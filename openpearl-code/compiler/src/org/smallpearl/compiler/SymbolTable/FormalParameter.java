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

public class FormalParameter extends VariableEntry {

    public  Boolean			passIdentical;
    
    public FormalParameter() {
    }

    public FormalParameter(String name, TypeDefinition type, Boolean assignmentProtection, Boolean passIdentical, SmallPearlParser.FormalParameterContext ctx) {
    	super(name, type, assignmentProtection, ctx);
    	this.passIdentical = passIdentical;
        
    }

    public String toString() {
        return (super.getAssigmentProtection() ? " INV " : " " ) + super.getType() +(passIdentical ? " IDENT" : "");
    }

    public String toString4IMC(boolean isInStructure) {
        return (super.getAssigmentProtection() ? " INV " : " " ) + super.getType().toString4IMC(isInStructure) +(passIdentical ? " IDENT" : "");
    }
    
    /* other methods inherited from parent class */
   

}