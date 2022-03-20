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

import org.antlr.v4.runtime.*;


public class DescriptiveErrorListener extends BaseErrorListener {
    public static DescriptiveErrorListener INSTANCE = new DescriptiveErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int lineNo,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        String sourceName = recognizer.getInputStream().getSourceName();

        SourceLocation loc = SourceLocations.getSourceLoc(lineNo);
        if ( loc != null ) {
            System.err.println(loc.filename() + ":" + loc.getLineNo(lineNo) + ":" + (charPositionInLine +1 )+ ": ERROR : Syntax error :" + msg);
            underlineError(recognizer,(Token)offendingSymbol, lineNo, charPositionInLine);
        } else {
            System.err.println(sourceName + ":" + lineNo + ":" + (charPositionInLine +1 )+ ": ERROR : Syntax error :" + msg);
            underlineError(recognizer,(Token)offendingSymbol, lineNo, charPositionInLine);
        }
    }

    protected void underlineError(Recognizer<?,?> recognizer,
                                  Token offendingToken, int lineNo,
                                  int charPositionInLine) {
        CommonTokenStream tokens =
                (CommonTokenStream)recognizer.getInputStream();
        String input = tokens.getTokenSource().getInputStream().toString();
        String[] lines = input.split("\n");
        if (lineNo > lines.length) {
            lineNo = lines.length;
        }
        String errorLine = lines[lineNo - 1];
        System.err.println(errorLine);
        for (int i=0; i<charPositionInLine; i++) System.err.print(" ");
        int start = offendingToken.getStartIndex();
        int stop = offendingToken.getStopIndex();
        System.err.print("^");
        if ( start>=0 && stop>=0 ) {
            for (int i=start; i<stop; i++) System.err.print("~");
        }
        System.err.println();
    }
}


