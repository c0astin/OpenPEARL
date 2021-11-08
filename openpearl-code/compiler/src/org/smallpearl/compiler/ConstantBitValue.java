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

package org.smallpearl.compiler;

public class ConstantBitValue extends ConstantValue 
       implements Comparable<ConstantBitValue> {
    private long m_value;
    private int m_noOfBits;

    public ConstantBitValue(long value, int noOfBits) {
        m_noOfBits = noOfBits;
        m_value = value;
    }

    public int getLength() { return m_noOfBits; }
    public long getLongValue() {
        return m_value;
    }
    public String getValue() {
        return formatBitStringConstant(m_value,m_noOfBits);
    }
    public String getBaseType() {
        return "BitString";
    }

    public String toString() {
        String name = "CONST_BITSTRING_" +  m_noOfBits + "_" + canonicalize(Long.toHexString(m_value));
        return name;
    }

    public String canonicalize(String str) {
        String res = "";

        if( str.startsWith("'")) {
            str = str.substring(1, str.length());
        }

        if( str.endsWith("'")) {
            str = str.substring(0, str.length() - 1);
        }

        for ( int i = 0; i < str.length(); i++) {
            Character ch = str.charAt(i);

            if ( !(( ch >= 'a' && ch <= 'z') || ( ch >= 'A' && ch <= 'Z' ) || ( ch >= '0' && ch <= '9'))) {
                ch = '_';
            }

            res += String.valueOf(ch);

        }

        return res;
    }

    public String formatBitStringConstant(Long l, int numberOfBits) {
        String bitStringConstant;

        String b = Long.toBinaryString(l);
        String bres = "";

        int l1 = b.length();

        if (numberOfBits < b.length())

        {
            bres = "";
            for (int i = 0; i < numberOfBits; i++) {
                bres = bres + b.charAt(i);
            }
            Long r = Long.parseLong(bres, 2);
            if (Long.toBinaryString(Math.abs(r)).length() < 15) {
                bitStringConstant = "0x" + Long.toHexString(r);
            } else if (Long.toBinaryString(Math.abs(r)).length() < 31) {
                bitStringConstant = "0x" + Long.toHexString(r) + "UL";
            } else {
                bitStringConstant = "0x" + Long.toHexString(r) + "ULL";
            }
        } else if (numberOfBits > b.length()) {
            bres = b;
            Long r = Long.parseLong(bres, 2);
            if (Long.toBinaryString(Math.abs(r)).length() < 15) {
                bitStringConstant = "0x" + Long.toHexString(r);
            } else if (Long.toBinaryString(Math.abs(r)).length() < 31) {
                bitStringConstant = "0x" + Long.toHexString(r) + "UL";
            } else {
                bitStringConstant = "0x" + Long.toHexString(r) + "ULL";
            }
        } else
        {
            if (Long.toBinaryString(Math.abs(l)).length() < 15) {
                bitStringConstant = "0x" + Long.toHexString(l);
            } else if (Long.toBinaryString(Math.abs(l)).length() < 31) {
                bitStringConstant = "0x" + Long.toHexString(l) + "UL";
            } else {
                bitStringConstant = "0x" + Long.toHexString(l) + "ULL";
            }
        }

        return bitStringConstant;
    }

    @Override
    public int compareTo(ConstantBitValue other) {
        return Long.compare(m_value, other.m_value);
    }

    @Override
    public TypeDefinition getType() {
        return new TypeBit(m_noOfBits);
    }

}
