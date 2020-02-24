/*
 * [The "BSD license"]
 *  Copyright (c) 2012-2016 Marcel Schaible
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

package org.smallpearl.compiler;

import org.smallpearl.compiler.SymbolTable.VariableEntry;

public class ASTAttribute {
    public TypeDefinition  m_type;
    public boolean m_readonly;
    public VariableEntry m_variable;
    public ConstantValue m_constant;
    public ConstantSlice m_slice;

    public ASTAttribute(TypeDefinition type) {
        m_type = type;
        m_readonly = false;
        m_variable = null;
        m_constant = null;
        m_slice    = null;
    }

    ASTAttribute(ConstantSlice slice) {
        m_type     = null;
        m_readonly = false;
        m_variable = null;
        m_constant = null;
        m_slice    = slice;
    }

    ASTAttribute(TypeDefinition type, boolean constant) {
        m_type = type;
        m_readonly = constant;
        m_variable = null;
        m_slice    = null;
    }

    ASTAttribute(TypeDefinition type, boolean constant, VariableEntry variable ) {
        m_type = type;
        m_variable = variable;
        m_constant = null;
        m_slice    = null;

        if ( variable.getLoopControlVariable()) {
            m_readonly = false;
        }
        else {
            m_readonly = constant;
        }
    }

    public boolean isReadOnly() {
        return this.m_readonly;
    }

    public boolean isLoopControlVariable() {
        return ( m_variable != null && m_variable.getLoopControlVariable());
    }


    public boolean isWritable() { return !this.isReadOnly(); }
    public TypeDefinition getType() { return this.m_type; }
    public VariableEntry getVariable() { return this.m_variable; }

    public ConstantValue getConstant() { return this.m_constant; }

    public Void setConstant(ConstantValue val) {
        m_constant = val;
        m_readonly = true;
        return null;
    }

    public Void setConstantFixedValue(ConstantFixedValue val) {
        m_constant = val;
        m_readonly = true;
        return null;
    }

    public ConstantFixedValue getConstantFixedValue() { 
        if (m_constant instanceof ConstantFixedValue) {
            return (ConstantFixedValue) this.m_constant;
        } else {
            return null;
        }
    }

    public Void setConstantFloatValue(ConstantFloatValue val) {
        m_constant = val;
        m_readonly = true;
        return null;
    }

    public ConstantFloatValue getConstantFloatValue() {
        if (m_constant instanceof ConstantFloatValue) {
            return (ConstantFloatValue) this.m_constant;
        } else {
            return null;
        }
    }

    public Void setConstantDurationValue(ConstantDurationValue val) {
        m_constant = val;
        m_readonly = true;
        return null;
    }

    public ConstantDurationValue getConstantDurationValue() {
        if (m_constant instanceof ConstantDurationValue) {
            return (ConstantDurationValue) this.m_constant;
        } else {
            return null;
        }
    }

    public ConstantSlice getConstantSlice() {
        return this.m_slice;
    }

    public String toString() {
        return "(" + this.m_type + " " + this.isReadOnly() + " " + this.m_variable + " " + this.m_constant + " " + this.m_slice + ")";
    }

    public void setVariable(VariableEntry ve) {
      this.m_variable = ve;
    }
}
