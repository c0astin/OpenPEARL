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

package org.smallpearl.compiler;

import java.io.*;
import org.antlr.v4.runtime.*;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.Exception.*;

public class ErrorStack {
    private static final int maxStackSize = 20;
    private static ErrorEnvironment m_stack[] = new ErrorEnvironment[maxStackSize];
    private static int m_sp = 0;  
    private static int m_totalErrorCount = 0;

/*
    public static ErrorStack() {
       m_sp = 0;
       m_totalErrorCount = 0;
    }
*/

    public static Void enter(ErrorEnvironment env) {
       m_sp ++;
       if (m_sp >= maxStackSize) {
           throw new InternalCompilerErrorException("",0,0,"ErrorStack overflow");
       }

       m_stack[m_sp] = env;
       env.resetLocalCount();
       return null;
    }


    public static Void leave() {
       m_sp --;
       if (m_sp < 0 ) {
           throw new InternalCompilerErrorException("",0,0,"ErrorStack underflow");
       }
       return null;
    }

    public static Void add(String msg) {
       String ANSI_RESET = "\u001B[0m";
	String ANSI_BLACK = "\u001B[30m";
	String ANSI_RED = "\u001B[31m";
	String ANSI_GREEN = "\u001B[32m";
	String ANSI_YELLOW = "\u001B[33m";
	String ANSI_BLUE = "\u001B[34m";
	String ANSI_PURPLE = "\u001B[35m";
	String ANSI_CYAN = "\u001B[36m";
	String ANSI_WHITE = "\u001B[37m";

       int startLineNumber;
       int startColNumber;
       String sourceLine;

       startLineNumber = m_stack[m_sp].getCtx().start.getLine();
       startColNumber  = m_stack[m_sp].getCtx().start.getCharPositionInLine();
       int stopLineNumber = m_stack[m_sp].getCtx().stop.getLine();
       int stopColNumber  = m_stack[m_sp].getCtx().stop.getCharPositionInLine();

System.err.println("start: "+ startLineNumber+":"+startColNumber+
                   "  stop: "+ stopLineNumber+" : " + stopColNumber);

       sourceLine = getErrorTextLine(Compiler.getSourceFilename(),
                                    startLineNumber);
       
       String errorLine = sourceLine.substring(0,startColNumber)+ ANSI_BLUE;
       if (startLineNumber == stopLineNumber) {
           errorLine += sourceLine.substring(startColNumber,stopColNumber+1);
           errorLine += ANSI_RESET;
           errorLine += sourceLine.substring(stopColNumber+1);
           System.err.println(errorLine);
       } else {
         // token covers multiple lines
         // print initial part
           errorLine += sourceLine.substring(startColNumber);
           System.err.println(errorLine);

           // print complete lines of the token
           for (int i=startLineNumber+1; i<stopLineNumber; i++) { 
             sourceLine = getErrorTextLine(Compiler.getSourceFilename(),i);
             System.err.println(sourceLine);
           }
           // print last line
           sourceLine = getErrorTextLine(Compiler.getSourceFilename(),stopLineNumber);
           errorLine = sourceLine.substring(0,stopColNumber+1) +
                       ANSI_RESET +
                       sourceLine.substring(stopColNumber+1);
           System.err.println(errorLine);
       }
       

	StringBuilder sb = new StringBuilder(startColNumber+10);
	for (int i=0; i < startColNumber; i++){
  	   sb.append(" ");
	}
        sb.append("^"+ANSI_RESET);

       System.err.println(sb.toString());
        System.err.println(
       //       m_stack[m_sp].getCtx().getText()+":"+
              Compiler.getSourceFilename() + ":" + 
              startLineNumber + ":" +
              startColNumber + ": "+
              m_stack[m_sp].getErrorPrefix()+" : "+msg);

        m_stack[m_sp].incLocalCount();
        m_totalErrorCount ++;
        return null;
    }

    private static String getErrorTextLine(String filename, int lineNbr) {
       String text="";
       int lineNumber = 0;
       try {
         FileReader readfile = new FileReader(filename);
         BufferedReader readbuffer = new BufferedReader(readfile);
         while (lineNumber < lineNbr) {
             text = readbuffer.readLine();
             lineNumber ++;
         }
       } catch (IOException e) {
         e.printStackTrace();
       }
       return text;
   }

}
