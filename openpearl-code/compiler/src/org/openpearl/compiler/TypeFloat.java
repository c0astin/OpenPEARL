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

package org.openpearl.compiler;


import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

public class TypeFloat extends TypeDefinition {
    private int m_precision;

    public TypeFloat() {
        super("FLOAT");
        this.m_precision = Defaults.FLOAT_PRECISION;
    }

    public TypeFloat(int precision) {
        super("FLOAT");
        this.m_precision = precision;
    }

    public Integer getPrecision() {
        return m_precision;
    }

    public String toString() {
        return super.toString() + "(" + this.m_precision + ")";
    }
    
    public String toString4IMC(boolean isInStructure) {
        return toString();
    }

    public ST toST(STGroup group) {
        ST st = group.getInstanceOf("float_type");
        st.add("size", m_precision);
        if (hasAssignmentProtection()) {
            ST inv = group.getInstanceOf("const_type");
            inv.add("type", st);
            st = inv;
        }
        return st;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TypeFloat)) {
            return false;
        }

        TypeFloat that = (TypeFloat) other;

        // Custom equality check here.
        return this.m_precision == that.m_precision;
    }

    public int getNoOfBytes() {
        return m_precision / 8 + 1;
    }
    
    public Integer getSize() {
      // we have only precisions of 24 and 53, which are mapped to float and double, resp.
      if (m_precision<30) {
        return 4;
      } else {
        return 8;
      }
    }
}
