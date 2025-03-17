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

import java.util.ArrayList;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

public class TypeArrayDeclaration extends TypeArray {
    private ArrayList<ArrayDimension> m_dimensions;


    TypeArrayDeclaration() {
        super("ARRAY");
        this.m_dimensions = new ArrayList<ArrayDimension>();
    }

    TypeArrayDeclaration(TypeDefinition type, ArrayList<ArrayDimension> dimensions) {
        super("ARRAY");
        super.setBaseType(type);
        this.m_dimensions = new ArrayList<ArrayDimension>();
    }

    public Void addDimension(ArrayDimension dimension) {
        m_dimensions.add(dimension);
        return null;
    }

    public Void setBaseType(TypeDefinition type) {
        super.setBaseType(type);
        return null;
    }

    public TypeDefinition getBaseType() {
        return super.getBaseType();
    }

    public ArrayList<ArrayDimension> getDimensions() {
        return m_dimensions;
    }

    public int getNoOfDimensions() {
        if ( m_dimensions != null ) {
            return m_dimensions.size();
        }
        else {
            return 0;
        }
    }

    public boolean hasDimensions() {
        if ( m_dimensions != null ) {
            return !(m_dimensions.get(0).getLowerBoundary() == 0 && m_dimensions.get(0).getUpperBoundary() == 0);
        }
        else {
            return false;
        }
    }

    public int getTotalNoOfElements() {
        int totalNoOfElements = 1;
        for (int i = 0; i < m_dimensions.size(); i++) {
            totalNoOfElements *= m_dimensions.get(i).getNoOfElements();
        }


        return totalNoOfElements;
    }

    public String toString() {
        return super.toString() + " " + this.m_dimensions + " " + super.getBaseType();
    }
    
    public String toErrorString() {
        return toString4IMC(true);
    }
    
    public String toString4IMC(boolean isInStructure) {
        String result="(";
        for (int i=0; i< m_dimensions.size(); i++) {
               if (i>0) result += ",";
               result += m_dimensions.get(i).toString4IMC(isInStructure);
        }
        
        result += ") " + super.getBaseType().toString4IMC(isInStructure);
        return result;
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TypeArrayDeclaration)) {
            return false;
        }

        TypeArrayDeclaration that = (TypeArrayDeclaration) other;

        // Custom equality check here.
        return super.getBaseType().equals(that.getBaseType());
    }
    public ST toST(STGroup group) {
//        ST st = group.getInstanceOf("ArrayType");
//        st.add("type",super.getBaseType().toST(group));
//        if (hasAssignmentProtection()) {
//            ST inv = group.getInstanceOf("const_type");
//            inv.add("type", st);
//            st = inv;
//        }
        ST st = super.toST(group);
        return st;
    }
}