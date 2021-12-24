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

package org.openpearl.compiler.SymbolTable;

import org.openpearl.compiler.OpenPearlParser.Label_statementContext;

/****
 *
 * LabelEntry extends SymbolTableEntry by adding a useCount attribute
 * 
 * the useCount is used to detect unused labels
 *
 */
public class LabelEntry extends SymbolTableEntry {

    private int m_useCount;
    
    /**
     * Construct this with null data fields.
     */
    public LabelEntry() {
      super();
    }

    /**
     * Construct this with the given data field values.
     */
    public LabelEntry(String name, Label_statementContext ctx) {
        super(name);
        super.m_ctx = ctx;
        m_useCount=0;
    }

    /**
     * Return the string rep of this, which consists of the return value of
     * super.toString, plus the values of this.isRef and this.memoryLocation.
     */
    public String toString(int level) {
        return indentString(level) + super.toString(level) + "label ";
    }
    

}