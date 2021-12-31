package org.openpearl.preprocessor;

/*
 * [The "BSD license"]
 * *  Copyright (c) 2021-2022 Marcel Schaible
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Preprocessor {
    static String m_version = "v0.1";
    static List<String> m_inputFiles = new ArrayList<String>();
    static String m_sourceFilename = "";
    static String m_outputFilename = null;
    static int m_verbose = 0;
    static boolean m_stacktrace = false;
    static int m_noOfErrors = 0;
    static int m_noOfWarnings = 0;
    static int m_lineWidth = 80;
    static int m_lineno = 1;

    public static void main(String[] args) {
        if (args.length < 1) {
            printHelp();
            return;
        }

        long startTime = System.nanoTime();

        if (!checkAndProcessArguments(args)) {
            return;
        }

        for (int i = 0; i < m_inputFiles.size(); i++) {
            m_sourceFilename = m_inputFiles.get(i);

            processFile(m_sourceFilename);

            System.out.flush();
            System.out.println();
            System.out.println(
                    "Number of errors in " + m_inputFiles.get(i) + " encountered: " + m_noOfErrors);

            if (m_noOfErrors == 0) {
                System.exit(0);
            } else {
                System.exit(1);
            }
        }
    }

    private static void handleInclude(String args) {
        System.out.println("handleInclude: " + args);
        args = stripComment(args);
        System.out.println("      args=: " + args);
        checkForTerminatingSemicolon(args);
        processFile(args);
    }

    private static void handleDefine(String args) {
        System.out.println("handleDefine: " + args);
    }

    private static void handleUndef(String args) {
        System.out.println("handleUndef: " + args);
    }

    private static void handleError(String args) {
        System.out.println("handleError: " + args);
    }

    private static void handleWarn(String args) {
        System.out.println("handleWarn: " + args);
    }

    private static void handleIf(String args) {
        System.out.println("handleIf: " + args);
    }

    private static void handleIfdef(String args) {
        System.out.println("handleIfdef: " + args);
    }

    private static void handleIfudef(String args) {
        System.out.println("handleIfudef: " + args);
    }

    private static void handleFin(String args) {
        System.out.println("handleFin: " + args);
    }

    private static void handleElse(String args) {
        System.out.println("handleElse: " + args);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void printHelp() {
        System.err.println("java org.openpearl.preprocessor                            \n"
                + " Options:                                                           \n"
                + "  --help                      Print this help message               \n"
                + "  --version                   Print version information             \n"
                + "  --verbose                   Print more information                \n"
                + "  [-D<macro> [=defn]]         Define a macro with optional value    \n"
                + "                              1, if value is omitted                \n"
                + "  [-U<macro>]                 Undefine a macro                      \n"
                + "  [-I<dir>...]                Specify include path                  \n"
                + "  --output <filename>         Filename of the generated code        \n"
                + "  --stacktrace                Print stacktrace in case of an        \n"
                + "                              exception                             \n"
                + "  infile                                                            \n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static boolean checkAndProcessArguments(String[] args) {
        int i = 0;

        while (i < args.length) {
            String arg = args[i];
            if(m_verbose > 0) {
                System.out.println("command line arguments arg("+i+")="+arg);
            }
            i++;
            if (arg.charAt(0) != '-') { // input file name
                m_inputFiles.add(arg);
                continue;
            }

            if (arg.equals("--help")) {
                printHelp();
                System.exit(0);
            } else if (arg.equals("--verbose")) {
                m_verbose++;
            } else if (arg.equals("--stacktrace")) {
                m_stacktrace = true;
            } else if (arg.equals("--output")) {
                if (i >= args.length) {
                    System.err.println("missing filename on --output");
                    return false;
                }
                m_outputFilename = args[i];
                i++;
                continue;
            } else if (arg.equals("--version")) {
                System.out.println("OpenPEARL Preprocessor version " + m_version);
                i++;
            } else {
                System.out.println("Unknown command line argument:" + arg);
                return false;
            }
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static String getBaseName(String filename) {
        String basename = "";

        if (filename != null) {
            int posOfExtension = filename.lastIndexOf('.');
            if (posOfExtension == -1) {
                System.err.println("no extension for input file given");
                System.exit(-2);
            }

            basename = filename.substring(0, filename.lastIndexOf('.'));
        }

        return basename;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void processFile(String filePath) {
        InputStream ins = null; // raw byte-stream
        Reader r = null; // cooked reader
        BufferedReader br = null; // buffered for readLine()

        File file = new File(filePath);
        String filename = file.getAbsolutePath();

        Pattern pattern = Pattern.compile("^#(.+?)[ \t]+(.*)$");
        Matcher matcher;

        try {
            String line;

            ins = new FileInputStream(filename);
            r = new InputStreamReader(ins, "UTF-8"); // leave charset out for default
            br = new BufferedReader(r);

            while ((line = br.readLine()) != null) {
                System.out.println("LINE:"+line);
                matcher = pattern.matcher(line);
                while(matcher.find()) {
                    if(matcher.group(1).equals("INCLUDE")) {
                        handleInclude(matcher.group(2));
                    }
                    else if(matcher.group(1).equals("DEFINE")) {
                        handleDefine(matcher.group(2));
                    }
                    else if(matcher.group(1).equals("UNDEF")) {
                        handleUndef(matcher.group(2));
                    }
                    else if(matcher.group(1).equals("ERROR")) {
                        handleError(matcher.group(2));
                    }
                    else if(matcher.group(1).equals("WARN")) {
                        handleWarn(matcher.group(2));
                    }
                    else if(matcher.group(1).equals("IF")) {
                        handleIf(matcher.group(2));
                    }
                    else if(matcher.group(1).equals("IFDEF")) {
                        handleIfdef(matcher.group(2));
                    }
                    else if(matcher.group(1).equals("ELSE")) {
                        handleElse(matcher.group(2));
                    }
                    else if(matcher.group(1).equals("FIN")) {
                        handleFin(matcher.group(2));
                    }
                    else {
                        System.err.println("ERROR: Unknown preprocessor directive: #" + matcher.group(1).toString());
                        System.exit(-1);
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage()); // handle exception
        }
        finally {
            if (br != null) { try { br.close(); } catch(Throwable t) { /* ensure close happens */ } }
            if (r != null) { try { r.close(); } catch(Throwable t) { /* ensure close happens */ } }
            if (ins != null) { try { ins.close(); } catch(Throwable t) { /* ensure close happens */ } }
        }
    }

    private static String stripComment(String line) {
        Pattern pattern = Pattern.compile("^(.*?)!(.*)$");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return line;
        }
    }

    private static void checkForTerminatingSemicolon(String line) {
        Pattern pattern = Pattern.compile("^(.*?);[ \t\n]*$");
        Matcher matcher = pattern.matcher(line);

        if (!matcher.find() ) {
            System.err.println(m_lineno +":"+m_sourceFilename+":ERROR: Missing ';' : " + line);
            System.exit(-1);
        }
    }

    private static void printErrorAndExit(String msg) {
        System.err.println(m_lineno +":"+m_sourceFilename+":ERROR:"+msg);
        System.exit(-1);
    }

    public static String getSourceFilename() {
        return m_sourceFilename;
    }
}
