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

import org.openpearl.compiler.SymbolTable.TaskEntry;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceLocations {
    private static ArrayList<SourceLocation> m_sourceLocs = new ArrayList<>();
    private static Pattern m_pattern = Pattern.compile("^#[ \t]([0-9]+)[ \t](.+?)[ \t]([0-9])$");
    private static SourceLocation m_loc = null;

    public static void print() {
        for (int i = 0; i < m_sourceLocs.size(); i++) {
            System.out.println(m_sourceLocs.get(i));
        }
    }

    public static void process(int lineNo, String line) {
        int nlineNo = 0;
        String filename = null;
        int flag = 0;

        Matcher matcher = m_pattern.matcher(line);

        if (matcher.find()) {
            nlineNo = Integer.parseInt(matcher.group(1));
            filename = matcher.group(2);
            flag = Integer.parseInt(matcher.group(3));
            switch (flag) {
                case 1:
                    if (m_sourceLocs.size() >= 1) {
                        SourceLocations.closeLoc(lineNo-2);
                    }
                    SourceLocations.openLoc(lineNo, filename, nlineNo, flag);
                    break;
                case 2:
                    SourceLocations.closeLoc(lineNo-1);
                    SourceLocations.openLoc(lineNo,filename,nlineNo,flag);
                    break;
                default:
                    break;
            }
        }
    }

    public static void openLoc(int currentLineNo, String filename, int startingLineNo, int flag) {
        m_sourceLocs.add(new SourceLocation(currentLineNo, startingLineNo, filename, flag));
    }

    public static void closeLoc(int lineNo) {
        SourceLocation loc = m_sourceLocs.get(m_sourceLocs.size()-1);
        loc.setSrcTo(lineNo);
    }

    public static SourceLocation getSourceLoc(int lineNo) {
        if ( m_sourceLocs.size() == 0) {
            return m_loc;
        }

        for (int i = 0; i < m_sourceLocs.size(); i++) {
            SourceLocation loc = m_sourceLocs.get(i);
            if ( lineNo >= loc.srcFrom() && (lineNo <= loc.srcTo() || loc.srcTo() == -1)) {
                return loc;
            }
        }

        return null;
    }

    public static String getTopFileName() {
        if ( m_sourceLocs != null && m_sourceLocs.size() > 0) {
            return m_sourceLocs.get(0).filename();
        } else {
            return null;
        }
    }

    public static void setSourceFileName(String filename) {
        m_loc = new SourceLocation(0,0,filename,0 );
    }
}