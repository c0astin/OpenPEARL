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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SourceLocation {
    private String m_filename;
    private int m_srcFromLineNo;
    private int m_srcToLineNo;
    private int m_dstLineNo;
    private int m_flag;

    SourceLocation(int srcLineNo, int dstLineNo, String filename, int flag) {
        m_srcFromLineNo = srcLineNo;
        m_srcToLineNo = -1;
        m_dstLineNo = dstLineNo;
        m_filename = filename;
        m_flag = flag;
    }

    public String toString() {
        return "LOC:" + this.m_srcFromLineNo + ":" + this.m_srcToLineNo + ":" + this.m_filename;
    }

    public int srcFrom() {
        return this.m_srcFromLineNo;
    }

    public void setSrcFrom(int lineNo) {
        m_srcFromLineNo = lineNo;
    }

    public int srcTo() {
        return this.m_srcToLineNo;
    }

    public void setSrcTo(int lineNo) {
        m_srcToLineNo = lineNo;
    }

    public String filename() {
        return this.m_filename;
    }

    public int getLineNo(int lineNo) {
        return lineNo - m_srcFromLineNo + m_dstLineNo;
    }
}

