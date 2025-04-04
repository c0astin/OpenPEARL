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
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Stack;

public class Preprocessor {
    /**
     * The {@link String} instance representing something.
     */
    static String m_version = "v0.1";
    static ArrayList<String> m_inputFiles = new ArrayList<String>();
    static String m_sourceFilename = "";
    static String m_outputFilename = null;
    static int m_verbose = 0;
    static boolean m_quiet = false;
    static boolean m_stacktrace = false;
    static int m_noOfErrors = 0;
    static int m_noOfWarnings = 0;
    static int m_lineWidth = 80;
    static int m_lineno = 1;
    static ArrayList<String> m_includeDirs = new ArrayList<>();
    static HashMap<String,String> m_defines = new HashMap<>();
    static List<String> m_seenIncludes = new ArrayList<>();
    static Stack<SourceFile> m_sourcefiles = new Stack<>();
    static int m_ifdef_level = 0;
    static Pattern m_pattern = Pattern.compile("^#(.+?)([ \t]+)(.*)$");
    static PrintStream m_outputStream = null;
    static PrintStream m_console = System.out;
    static int m_commentNestingLevel = 0;
    static boolean m_insideSingleQuotes = false;
    static boolean m_insideDoubleQuotes = false;

    public enum Statement
    {
        IF, IFDEF, ELSE, FIN, ERROR, WARN, INCLUDE, PRAGMA, DEFINE, UNDEF, MISC, END_COMMENT
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 1) {
            printHelp();
            return;
        }

        long startTime = System.nanoTime();

        if (!checkAndProcessArguments(args)) {
            return;
        }

        if (!m_quiet) {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
        }

        if ( m_outputFilename != null ) {
            m_outputStream = new PrintStream(m_outputFilename);
        } else {
            System.out.println("ERROR: Output file not specified");
            System.exit(-2);
        }

        m_includeDirs.add(".");
        for (int i = 0; i < m_inputFiles.size(); i++) {
            m_sourceFilename = m_inputFiles.get(i);

            outputPPLine(1, m_sourceFilename, "1");
            m_sourcefiles.push(new SourceFile(m_sourceFilename));
            processLines(new HashSet<Statement>(), 0);
            m_sourcefiles.pop();

            if (m_outputFilename != null) {
                m_outputStream.close();
            }

            System.setOut(m_console);

            if (!m_quiet) {
                System.out.flush();
                System.out.println();
                System.out.println(
                        "Number of errors in "
                                + m_inputFiles.get(i)
                                + " encountered: "
                                + m_noOfErrors);
            }

            if (m_noOfErrors == 0) {
                System.exit(0);
            } else {
                System.exit(1);
            }
        }
    }

    private static void outputLine(String line) {
        m_outputStream.println(line);
    }

    private static void outputPPLine(int lineNo, String filename, String flags) {
        m_outputStream.println("# " + lineNo + " " + filename + " " + flags);
    }

    private static void handleInclude(String args) {
        args = skipComment(args.trim());
        String currentFileName = m_sourceFilename;
        m_sourceFilename= findFile(args, m_includeDirs);

        if (m_sourceFilename == null) {
            System.out.println("ERROR: Include file not found:"+args);
            System.exit(-2);
        }

        m_sourcefiles.push(new SourceFile(m_sourceFilename));

        outputPPLine(m_sourcefiles.peek().getLineNo(), m_sourceFilename, "1");

        if (m_seenIncludes.contains(m_sourcefiles.peek().getFileName())) {
            System.out.println("ERROR: Endless Include file:"+m_sourceFilename);
            System.exit(-2);
        }

        m_seenIncludes.add(m_sourcefiles.peek().getFileName());
        processLines(new HashSet<Statement>(),0);
        m_sourcefiles.pop();
        m_sourceFilename = currentFileName;

        outputPPLine(m_sourcefiles.peek().getLineNo(), m_sourceFilename, "2");
    }

    private static void handleDefine(String args) {
        String[] parts = args.split("=");
        String var = parts[0].trim();
        String expr = parts[1].trim();
        m_defines.put(var,expr);
    }

    private static void handleUndef(String args) {
        String var = args.trim();
        m_defines.remove(var);
    }

    private static void handleError(String args) {
        System.out.println("#ERROR:"+args);
        System.exit(-1);
    }

    private static void handleWarn(String args) {
        System.out.println("#WARN:"+args);
    }

    private static void handleIf(String args) {
    }

    private static void handleIfdef(String args) {
        int currentLevel = m_ifdef_level;
        HashSet<Statement> stopSet = new HashSet<>();

        if ( m_defines.get(args) != null ) {
            stopSet.add(Statement.ELSE);
            stopSet.add(Statement.FIN);
            Statement lastStatement = processLines(stopSet,currentLevel);

            if ( lastStatement == Statement.ELSE) {
                stopSet.remove(Statement.ELSE);
                lastStatement = skipLines(stopSet,currentLevel);
            }
        }
        else {
            stopSet.add(Statement.ELSE);
            stopSet.add(Statement.FIN);
            Statement lastStatement = skipLines(stopSet,currentLevel);
            if ( lastStatement == Statement.ELSE) {
                stopSet.remove(Statement.ELSE);
                lastStatement = processLines(stopSet,currentLevel);
            }
        }
    }

    private static void handleIfndef(String args) {
        int currentLevel = m_ifdef_level;
        HashSet<Statement> stopSet = new HashSet<>();

        if ( m_defines.get(args) == null ) {
            stopSet.add(Statement.ELSE);
            stopSet.add(Statement.FIN);
            Statement lastStatement = processLines(stopSet,currentLevel);

            if ( lastStatement == Statement.ELSE) {
                stopSet.remove(Statement.ELSE);
                lastStatement = skipLines(stopSet,currentLevel);
            }
        }
        else {
            stopSet.add(Statement.ELSE);
            stopSet.add(Statement.FIN);
            Statement lastStatement = skipLines(stopSet,currentLevel);
            if ( lastStatement == Statement.ELSE) {
                stopSet.remove(Statement.ELSE);
                lastStatement = processLines(stopSet,currentLevel);
            }
        }
    }

    private static void handleFin(String args) {
    }

    private static void handleElse(String args) {
    }

    private static boolean handlePragma(String args) {
        boolean res = false;
        if (args.equals("ONCE")) {
            res = m_seenIncludes.contains(m_sourcefiles.peek().getFileName());
        } else {
            System.err.println("ERROR: Unknown PRAGMA:"+args);
            System.exit(-3);
        }
        return res;
    }

    private static void printHelp() {
        System.err.println("java org.openpearl.preprocessor                            \n"
                + " Options:                                                           \n"
                + "  --help                      Print this help message               \n"
                + "  --version                   Print version information             \n"
                + "  --verbose                   Print more information                \n"
                + "  --quiet                     Suppress all unnecessary output       \n"
                + "  [-D<macro> [=defn]]         Define a macro with optional value    \n"
                + "                              1, if value is omitted                \n"
                + "  [-U<macro>]                 Undefine a macro                      \n"
                + "  [-I<dir>...]                Specify include path                  \n"
                + "  --output <filename>         Filename of the generated code        \n"
                + "  infile                                                            \n");
    }

    private static boolean checkAndProcessArguments(String[] args) {
        int i = 0;

        while (i < args.length) {
            String arg = args[i];
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
            } else if (arg.equals("--quiet")) {
                m_quiet=true;
            } else if (arg.equals("--stacktrace")) {
                m_stacktrace = true;
            } else if (arg.startsWith("-I")) {
                addIncludeSearchPath(arg.substring(2,arg.length()));
            } else if (arg.startsWith("-D")) {
                addDefine(arg.substring(2,arg.length()));
            } else if (arg.startsWith("-U")) {
                removeDefine(arg.substring(2,arg.length()));
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

    static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

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

    static void addIncludeSearchPath(String path) {
        m_includeDirs.add(path);
    }

    private static void addDefine(String variable) {
        Pattern pattern = Pattern.compile("^(.+?)(?:=(.+))?$");
        Matcher matcher = pattern.matcher(variable);

        String name = null, value = null;

        if (matcher.find()) {
            name = matcher.group(1);
            if (matcher.groupCount() == 2) {
                value = matcher.group(2);
            }

            if( value == null ) {
                value = "1";
            }

            m_defines.put(name,value);
        } else {
            System.err.println("ERROR: -D wrong variable name or value");
            System.exit(-2);
        }
    }

    private static void removeDefine(String variable) {
            m_defines.remove(variable.trim());
    }

    private static Statement processLines(HashSet stopset, int level) {
        boolean directiveFound = false;
        Pattern pattern;
        Matcher matcher;
        try {
            String line;
            SourceFile sourceFile = m_sourcefiles.peek();
            while ((line = sourceFile.getNextLine()) != null) {
                line = skipComment(line);
                if (m_commentNestingLevel == 0) {
                    // Because RTOS-UH does not mandate a ending semicolon, we ignore it here.
                    // checkForTerminatingSemicolon(line);
                    Matcher m_matcher;
                    m_matcher = m_pattern.matcher(line);
                    while (m_matcher.find()) {
                        if (m_matcher.group(1).equals("INCLUDE")) {
                            directiveFound = true;
                            String fname = sourceFile.stripSemicolon(m_matcher.group(3));
                            handleInclude(fname);
                            m_seenIncludes.add(fname);
                        } else if (m_matcher.group(1).equals("DEFINE")) {
                            directiveFound = true;
                            handleDefine(m_matcher.group(2));
                        } else if (m_matcher.group(1).equals("UNDEF")) {
                            directiveFound = true;
                            handleUndef(m_matcher.group(2));
                        } else if (m_matcher.group(1).equals("ERROR")) {
                            directiveFound = true;
                            handleError(m_matcher.group(2));
                        } else if (m_matcher.group(1).equals("WARN")) {
                            directiveFound = true;
                            handleWarn(m_matcher.group(2));
                        } else if (m_matcher.group(1).equals("IF")) {
                            directiveFound = true;
                            handleIf(m_matcher.group(2));
                        } else if (m_matcher.group(1).equals("IFDEF")) {
                            directiveFound = true;
                            String s1, s2, s3;
                            s1 = m_matcher.group(1);
                            s2 = m_matcher.group(2);
                            s3 = m_matcher.group(3);

                            m_ifdef_level++;
                            handleIfdef(m_matcher.group(2).trim());
                        } else if (m_matcher.group(1).equals("IFUDEF")) {
                            directiveFound = true;
                            String s1, s2, s3;
                            s1 = m_matcher.group(1);
                            s2 = m_matcher.group(2);
                            s3 = m_matcher.group(3);

                            m_ifdef_level++;
                            handleIfndef(m_matcher.group(2).trim());
                        } else if (m_matcher.group(1).equals("ELSE")) {
                            directiveFound = true;
                            String ss = m_matcher.group(2);
                            if (stopset.contains(Statement.ELSE) && (level == m_ifdef_level)) {
                                return Statement.ELSE;
                            }
                        } else if (m_matcher.group(1).equals("FIN")) {
                            directiveFound = true;
                            if (stopset.contains(Statement.FIN) && (level == m_ifdef_level)) {
                                m_ifdef_level--;
                                return Statement.FIN;
                            }
                        }
                        /*
                                            else if(m_matcher.group(1).equals("PRAGMA")) {
                                                directiveFound = true;
                                                if ( handlePragma(m_matcher.group(2).trim())) {
                                                    return Statement.MISC;
                                                }
                                            }
                        */
                        else {
                            System.err.println(
                                    "ERROR: Unknown preprocessor directive: #"
                                            + m_matcher.group(1).toString());
                            System.exit(-1);
                        }
                    }
                }

                if (!directiveFound) {
                    line = replaceDefine(line);
                    outputLine(line);
                } else {
                    directiveFound = false;
                }
            }

            if ( m_commentNestingLevel != 0 ) {
                System.err.println(
                        "ERROR: Block comment not closed properly.");
                System.exit(-1);
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage()); // handle exception
            System.exit(-1);
        }

        return Statement.MISC;
    }

    private static Statement skipLines(HashSet stopset, int level) {
        try {
            String line;
            SourceFile sourceFile = m_sourcefiles.peek();
            while ((line = sourceFile.getNextLine()) != null) {
                // Because RTOS-UH does not mandate a ending semicolon, we ignore it here.
                // checkForTerminatingSemicolon(line);
                Matcher m_matcher;
                m_matcher = m_pattern.matcher(line);
                while(m_matcher.find()) {
                    if(m_matcher.group(1).equals("IFDEF")) {
                        m_ifdef_level++;
                    }
                    else if(m_matcher.group(1).equals("IFUDEF")) {
                        m_ifdef_level++;
                    }
                    else if(m_matcher.group(1).equals("ELSE")) {
                        if ( stopset.contains(Statement.ELSE) && (level == m_ifdef_level)) {
                            return Statement.ELSE;
                        }
                    }
                    else if(m_matcher.group(1).equals("FIN")) {
                        if ( stopset.contains(Statement.FIN) ){
                            if (level == m_ifdef_level) {
                                m_ifdef_level--;
                                return Statement.FIN;
                            }
                            m_ifdef_level--;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage()); // handle exception
            System.exit(-1);
        }

        return Statement.MISC;
    }

    private static String skipComment(String line) {
        String res = "";
        String str = "";
        int state = 0;
        boolean done = false;
        int i = 0;
        char lch = ' ';
        char ch = ' ';

        while(!done && i < line.length()) {
            ch = line.charAt(i);

            switch (ch) {
                case '\'':
                    if ( m_commentNestingLevel == 0 ) {
                        if (!m_insideDoubleQuotes && !m_insideDoubleQuotes) {
                            m_insideSingleQuotes = !m_insideSingleQuotes;
                        }
                        res += ch;
                    }
                    break;
                case '\"':
                    if ( m_commentNestingLevel == 0 ) {
                        if (!m_insideSingleQuotes && !m_insideDoubleQuotes) {
                            m_insideDoubleQuotes = !m_insideDoubleQuotes;
                        }
                        res += ch;
                    }
                    break;
                case '!':
                    if (( m_commentNestingLevel == 0 ) && (m_insideSingleQuotes || m_insideDoubleQuotes)) {
                        res += ch;
                    } else {
                        done = m_commentNestingLevel == 0;
                    }
                    break;
                case '/':
                    if (m_insideSingleQuotes || m_insideDoubleQuotes) {
                        res += ch;
                    } else {
                        if (lch == '*') {
                            m_commentNestingLevel--;
                            break;
                        }
                        i++;
                        if (i < line.length()) {
                            lch = ch;
                            ch = line.charAt(i);
                            if ( ch == '*') {
                                m_commentNestingLevel++;
                            }
                            else {
                                if (m_commentNestingLevel == 0) {
                                    res += lch;
                                    res += ch;
                                }
                            }
                        }
                        else {
                            res += ch;
                        }
                    }
                    break;
                default:
                    if (m_commentNestingLevel == 0) {
                        res += ch;
                    }
                    break;
            }

            lch = ch;
            i++;
        }

        // System.out.println("skipComment(" + m_commentNestingLevel + ":" +  m_insideSingleQuotes + ":" + m_insideDoubleQuotes + "):" + line + " -> " + res);
        return res;
    }

    private static String replaceDefine(String line) {
        String str = line;

        for (Map.Entry<String, String> entry : m_defines.entrySet()) {
            str = str.replaceAll(entry.getKey(), entry.getValue());
        }
        return str;
    }

    private static void checkForTerminatingSemicolon(String line) {
        Pattern pattern = Pattern.compile("^(.*?);.*$");
        Matcher matcher = pattern.matcher(line);

        if (line.startsWith("#") && !matcher.find() ) {
            System.err.println(m_sourcefiles.peek().getLineNo() +":"+m_sourcefiles.peek().getFileName()+":ERROR: Missing ';' : " + line);
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

    public static int evalXConstExpr(final String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            int parse() {
                nextChar();
                int x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor

            int parseExpression() {
                int x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            int parseTerm() {
                int x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            int parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                int x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Integer.parseInt(expr.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = expr.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                return x;
            }
        }.parse();
    }

    public static String findFile(String filename, ArrayList<String> searchPaths) {
        for (String path : searchPaths) {
            String absoluteFilename = path + "/" + filename;
            Path fullPath = Paths.get(absoluteFilename);
            boolean b = Files.exists(fullPath, LinkOption.NOFOLLOW_LINKS);
            if ( b) {
                return fullPath.toString();
            }
        }

        return null;
    }
}

class SourceFile {
    public SourceFile(String filename) {
        m_filename = filename;

        m_file = new File(m_filename);

        String filenamehandleDefine = m_file.getAbsolutePath();

        try {
            m_inputStream = new FileInputStream(m_filename);
            m_reader = new InputStreamReader(m_inputStream, "UTF-8");
            m_bufferedReader = new BufferedReader(m_reader);
        } catch (Exception e) {
            System.err.println(e.getMessage()); // handle exception
            System.exit(-1);
        }
    }

    public int getLineNo() {
        return m_lineno;
    }

    public String getFileName() {
        return m_filename;
    }

    public void close() {
        if (m_bufferedReader != null) {
            try {
                m_bufferedReader.close();
            } catch (Throwable t) {
                /* ensure close happens */
            }
        }
        if (m_reader != null) {
            try {
                m_reader.close();
            } catch (Throwable t) {
                /* ensure close happens */
            }
        }
        if (m_inputStream != null) {
            try {
                m_inputStream.close();
            } catch (Throwable t) {
                /* ensure close happens */
            }
        }
    }

    public String stripSemicolon(String s) {
        String res = s.trim();
        if ( res.charAt(res.length()-1) == ';') {
            return res.substring(0,res.length()-1);
        }
        return res;
    }

    public String getNextLine() {
        try {
            readLine();
            return m_curLine;
        } catch (Exception e) {
            System.err.println(e.getMessage()); // handle exception
            System.exit(-1);
        }
        return null;
    }

    public String readLine() {
        try {
            m_lineno++;
            m_curLine = this.m_bufferedReader.readLine();
            m_curIndex = 0;
            return m_curLine;
        } catch (Exception e) {
            System.err.println(e.getMessage()); // handle exception
            System.exit(-1);
        }
        return null;
    }

    public char advance() {
        if (m_curIndex == m_curLine.length()) {
            readLine();
        }
        m_curChar = m_curLine.charAt(m_curIndex++);
        return m_curChar;
    }

    public void getNextChar() {
        int state = 0;
        boolean done = false;

        advance();

        while(!done && m_curChar != -1 ) {
            char lch = ' ';
            switch (state) {
                case 0:
                    switch (m_curChar) {
                        case '"':
                            state = 1;
                            break;
                        case '!':
                            state = 2;
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }
        }

    private String m_filename;
    private InputStream m_inputStream;
    private Reader m_reader;
    private BufferedReader m_bufferedReader;
    private File m_file;
    private int m_lineno = 1;
    private String m_curLine = "";
    private char m_curChar = ' ';
    private int m_curIndex = 0;
}
