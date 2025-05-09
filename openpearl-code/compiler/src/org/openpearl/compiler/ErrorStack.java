/*
 * [A "BSD license"]
 *  Copyright (c) 2019 Rainer Mueller
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

import java.io.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.Exception.*;

/**
 * helper class for error treatment
 * 
 * The detection of semantic errors occurs in different layers. Eg. A
 * PUT-statement has a list of values, a list of formats. Each format may have
 * some parameters like E(fieldWidth, decimalPositions,significance). These
 * parameters may be expressions of type FIXED.Ich they are constants we could
 * check their validity at compile time.
 * 
 * Thus the analysis will enter each part and visit all sub-parts.
 * 
 * If there was a error detected, we could abort the checks at a suitable point.
 * It is also possible to continue with other checks.
 * 
 * For each part of analysis we enter a new level with an descriptive text and
 * the parser context of the part.
 */
public class ErrorStack {
    private static final int maxStackSize = 100;
    private static ErrorEnvironment m_stack[] = new ErrorEnvironment[maxStackSize];
    private static int m_sp = -1;
    private static int m_totalErrorCount = 0;
    private static String errorOn = "";
    private static String errorOff = "";

    /**
     * enter a new sub-part
     * 
     * @param ctx ParserRuleContext of the element
     * @param errorPrefix  tag for the error message
     */
    public static Void enter(ParserRuleContext ctx, String errorPrefix) {
        //System.out.println("ErrorStack.enter: " + env.getErrorPrefix()
        //		+ "  level=" + m_sp);
        ErrorEnvironment env = new ErrorEnvironment(ctx, errorPrefix);

        m_sp++;
        if (m_sp >= maxStackSize) {
            String prefix = "";
            for (int i = 0; i <= m_sp; i++) {
                if (m_stack[i].getErrorPrefix() != null) {
                    prefix += m_stack[i].getErrorPrefix() + ":";
                }
            }
            throw new InternalCompilerErrorException("ErrorStack overflow (" + prefix + ")");
        }

        m_stack[m_sp] = env;
        env.resetLocalCount();
        return null;
    }

    /**
     * enter a new sub-part
     * 
     * @param ctx ParserRuleContext of the element
     *           
     */
    public static Void enter(ParserRuleContext ctx) {
        enter(ctx, null);
        return null;
    }

    /**
     * read the number of detected errors in this part
     * 
     * @returns the number of errors in this part
     */
    public static long getLocalCount() {
        return m_stack[m_sp].getLocalCount();
    }

    /**
     * read the total number of detected errors
     * 
     * @returns the number of errors
     */
    public static long getTotalErrorCount() {
        return m_totalErrorCount;
    }

    /**
     * leave the current part
     */
    public static Void leave() {
        m_sp--;
        if (m_sp < -1) {
            throw new InternalCompilerErrorException("ErrorStack underflow");
        }
        return null;
    }

    /**
     * add a new error
     * 
     * emits an error message to System.err with
     * <ul>
     * <li>the corresponding source code
     * <li>colored region for the error
     * <li>complete hierarchy of the error stack and the error message<br>
     * like PUT:E-format:fieldWidth: must be positive
     * </ul>
     * 
     * The error counters are incremented
     * 
     * @param msg the concrete error message
     */
    public static Void add(String msg) {
        m_stack[m_sp].incLocalCount();
        m_totalErrorCount++;
        printMessage(msg, "error");
        return null;
    }

    /**
     * add a new error in a temporary new error context
     * 
     * emits an error message to System.err with
     * 
     * <ul>
     * <li>the corresponding source code
     * <li>colored region for the error
     * <li>complete hierarchy of the error stack and the error message<br>
     * like PUT:E-format:fieldWidth: must be positive
     * </ul>
     * 
     * The error counters are incremented
     * @param ctx the current ParserRuleContext
     * @param prefix the error prefix
     * @param msg the concrete error message
     */
    public static Void add(ParserRuleContext ctx, String prefix, String msg) {
        enter(ctx, prefix);
        m_stack[m_sp].incLocalCount();
        m_totalErrorCount++;
        printMessage(msg, "error");
        leave();
        return null;
    }

    /**
     * add a new NOTE in a temporary new error context
     * 
     * emits an 'note' message to System.err with
     * 
     * <ul>
     * <li>the corresponding source code
     * <li>colored region for the error
     * <li>complete hierarchy of the error stack and the error message<br>
     * like PUT:E-format:fieldWidth: must be positive
     * </ul>
     * 
     * The error counters are NOT incremented
     * @param ctx the current ParserRuleContext
     * @param prefix the error prefix
     * @param msg the concrete error message
     */
    public static Void note(ParserRuleContext ctx, String prefix, String msg) {
        enter(ctx, prefix);
        printMessage(msg, "note");
        leave();
        return null;
    }

    public static Void note(String msg) {
        printMessage(msg, "note");
        return null;
    }

    public static Void warn(ParserRuleContext ctx, String prefix, String msg) {
        enter(ctx, prefix);
        printMessage(msg, "warning");
        leave();
        return null;
    }

    /**
     * add a new internal compiler error
     * 
     * emits an internal compiler error message to System.err with
     * <ul>
     * <li>the corresponding source code
     * <li>colored region for the error
     * <li>complete hierarchy of the error stack and the error message<br>
     * like PUT:E-format:fieldWidth: must be positive
     * </ul>
     * 
     * The error counters are incremented
     * 
     * @param msg the concrete error message
     */
    public static Void addInternal(String msg) {
        m_stack[m_sp].incLocalCount();
        m_totalErrorCount++;
        printMessage(msg + "\n\tplease send a bug report", "internal compiler error");
        return null;
    }

    /**
     * add a new internal compiler error
     * 
     * emits an internal compiler error message to System.err with
     * <ul>
     * <li>the corresponding source code
     * <li>colored region for the error
     * <li>complete hierarchy of the error stack and the error message<br>
     * like PUT:E-format:fieldWidth: must be positive
     * </ul>
     * 
     * The error counters are incremented
     * 
     * @param msg the concrete error message
     */
    public static Void addInternal(ParserRuleContext ctx, String prefix, String msg) {
        if (ctx != null) {
           enter(ctx, prefix);
           m_stack[m_sp].incLocalCount();
           m_totalErrorCount++;
           printMessage(msg + "\n\tplease send a bug report", "internal compiler error");
           leave();
        } else {
            System.err.println("internal compiler error: "+prefix+": "+msg + "\n\tplease send a bug report" );
        }
        return null;
    }


    /**
     * add a new warning
     * 
     * emits an warning message to System.err with
     * <ul>
     * <li>the corresponding source code
     * <li>colored region for the error
     * <li>complete hierarchy of the error stack and the error message<br>
     * like PUT:E-format:fieldWidth: must be positive
     * </ul>
     * 
     * The error counters are NOT incremented
     * 
     * @param msg the concrete warning message
     */
    public static Void warn(String msg) {
        printMessage(msg, "warning");
        return null;
    }

    private static Void printMessage(String msg, String typeOfMessage) {
        int startLineNumber;
        int stopLineNumber;
        int expandedStartLineNumber;
        int expandedStopLineNumber;
        int startColNumber;
        int stopColNumber;
        String sourceLine;
        String filename;

        expandedStartLineNumber = m_stack[m_sp].getCtx().start.getLine();
        expandedStopLineNumber = m_stack[m_sp].getCtx().stop.getLine();

        SourceLocation startLoc = SourceLocations.getSourceLoc(expandedStartLineNumber);
        SourceLocation stopLoc = SourceLocations.getSourceLoc(expandedStopLineNumber);

        if (startLoc == null || stopLoc == null ) {
            System.err.println("internal compiler error: "+msg + "\n\tplease send a bug report" );
        }

        filename = startLoc.filename();

        startLineNumber = startLoc.getLineNo(m_stack[m_sp].getCtx().start.getLine());
        startColNumber = m_stack[m_sp].getCtx().start.getCharPositionInLine();
        stopLineNumber = stopLoc.getLineNo(m_stack[m_sp].getCtx().stop.getLine());
        stopColNumber = m_stack[m_sp].getCtx().stop.getCharPositionInLine();

        /* print error message */
        String prefix = "";
        for (int i = 0; i <= m_sp; i++) {
            if (m_stack[i].getErrorPrefix() != null) {
                prefix += m_stack[i].getErrorPrefix() + ": ";
            }
        }

        /* print source line */
        sourceLine = getLineFromSourceFile(Compiler.getSourceFilename(), expandedStartLineNumber);

        String errorLine = "";

        errorLine = sourceLine.substring(0, startColNumber);
        int errorPos = calculateErrorPosition(errorLine);

        //errorLine = spaces(errorPos) + errorOn;
        // let's see the beginning of the line with the error 
        errorLine += errorOn;

        System.err.println(filename + ":" + startLineNumber + ":"
                + (errorPos + 1) + ": " + typeOfMessage + ": " + 
                (prefix.length()>0 ? prefix : "")  +   // if prefix is empty suppress the second space 
                msg);

        if (startLineNumber == stopLineNumber) {
            errorLine += sourceLine.substring(startColNumber, stopColNumber + 1);
            errorLine += errorOff;
            errorLine += sourceLine.substring(stopColNumber + 1);
            System.err.println(errorLine);
        } else {
            errorLine += sourceLine.substring(startColNumber);//, stopColNumber + 1);
            errorLine += errorOff;
            System.err.println(errorLine);
//            // token covers multiple lines
//            // print initial part
//            errorLine += sourceLine.substring(startColNumber);
//            System.err.println(errorLine);
//            System.err.println("------1 "+expandedStartLineNumber+":"+startColNumber+" - "+expandedStopLineNumber+":"+stopColNumber);
//            // print complete lines of the token
//            for (int i =expandedStartLineNumber + 1; i < expandedStopLineNumber; i++) {
//                System.err.println("------2a");
//                sourceLine = expandTabs(getLineFromSourceFile(filename, i));
//                System.err.println("------2b");
//                System.err.println(sourceLine);
//                System.err.println("------2");
//            }
//            
//            // print last line
//            System.err.println("------3a");
//            sourceLine = getLineFromSourceFile(filename, expandedStopLineNumber);
//            System.err.println("------3b "+stopColNumber+ " "+sourceLine.length());
//            errorLine = expandTabs(sourceLine.substring(0, stopColNumber + 1)) + errorOff
//                    + sourceLine.substring(stopColNumber + 1);
//            System.err.println("------3c");
//            System.err.println(errorLine);
//            System.err.println("------3");

        }

        /* print circumflex */
        System.err.println(spaces(errorPos) + errorOn + "^" + errorOff);

        return null;
    }

    static String spaces(int nbr) {
        StringBuilder sb = new StringBuilder(nbr);
        for (int i = 0; i < nbr; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /** calculate the error posintion accordinmg the following rules
     * <ul>
     * <li>\t goes to the next tab pos 
     * <li>tabsize = 8
     * <li>non ascii characters are counted as 1 character
     * <li>first position in line is 1
     * </ul>
     */
    private static int calculateErrorPosition(String errorLine) {
        int errorPos = 0;
        for (int i = 0; i < errorLine.length(); i++) {
            char ch = errorLine.charAt(i);
            if (ch == '\t') {
                errorPos += 8 - (errorPos % 8);
            } else {
                errorPos++;
            }
        }
        return errorPos;
    }

    private static String expandTabs(String s) {
        String result = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '\t') {
                result += s.charAt(i);
            } else {
                for (int j = 0; j < (8 - i % 8); j++) {
                    result += ' ';
                }
            }
        }

        return result;
    }

    private static String getLineFromSourceFile(String filename, int lineNbr) {
        String text = "";
        int lineNumber = 0;
        try {
            FileReader readfile = new FileReader(filename);
            BufferedReader readbuffer = new BufferedReader(readfile);
            while (lineNumber < lineNbr) {
                text = readbuffer.readLine();
                lineNumber++;
            }
            readbuffer.close();
            readfile.close();
        } catch (IOException e) {
            
            e.printStackTrace();
        }

        return text;
    }

    public static Void useColors(boolean coloured) {
        String ANSI_RESET = "\u001B[0m";
        //String ANSI_BLACK = "\u001B[30m";
        //String ANSI_RED = "\u001B[31m";
        //String ANSI_GREEN = "\u001B[32m";
        //String ANSI_YELLOW = "\u001B[33m";
        String ANSI_BLUE = "\u001B[34m";
        //String ANSI_PURPLE = "\u001B[35m";
        //String ANSI_CYAN = "\u001B[36m";
        //String ANSI_WHITE = "\u001B[37m";

        if (coloured) {
            errorOn = ANSI_BLUE;
            errorOff = ANSI_RESET;
        } else {
            errorOn = "";
            errorOff = "";
        }
        return null;
    }



}
