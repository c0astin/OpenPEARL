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

public class ConstantFixedValue extends ConstantValue 
       implements Comparable<ConstantFixedValue> {
    private long m_value;
    private int m_precision;

    public ConstantFixedValue(Integer value) {
        m_value = value.longValue();
        m_precision = calcPrecision();
    }

    public ConstantFixedValue(Integer value, Integer precision) {
        m_value = value.longValue();
        m_precision = precision;
    }

    public ConstantFixedValue(Integer value, int precision) {
        m_value = value.longValue();
        m_precision = precision;
    }

    public ConstantFixedValue(Long value) {
        m_value = value;
        m_precision = calcPrecision();
    }

    public ConstantFixedValue(Long value, Integer precision) {
        m_value = value;
        m_precision = precision;
    }

    public ConstantFixedValue(Long value, int precision) {
        m_value = value;
        m_precision = precision;
    }

    public ConstantFixedValue(long value, int precision) {
        m_value = value;
        m_precision = precision;
    }

    public long getValue() {
        return m_value;
    }

    public int getPrecision() {
        return m_precision;
    }

    public void setPrecision(int precision) {
        m_precision = precision;
    }

    private int calcPrecision() {
        int precision = Long.toBinaryString(Math.abs(m_value)).length();
        if (m_value < 0) {
            precision++;
        }

        return precision;
    }

    public String getBaseType() {
        return "Fixed";
    }

    public String toString() {
        String name = "CONST_" + getBaseType().toUpperCase();
        long value = Math.abs(m_value);

        if (m_value < 0) {
            name += "_N";
        } else if (m_value >= 0) {
            name += "_P";
        }

        name += "_" + value + "_" + m_precision;
        return name;
    }

    @Override
    public int compareTo(ConstantFixedValue other) {
        return Long.compare(m_value, other.m_value);
    }

    @Override
    public TypeDefinition getType() {
        return new TypeFixed(m_precision);
    }

}
