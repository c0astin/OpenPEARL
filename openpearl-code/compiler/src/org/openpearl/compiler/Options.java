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

import static org.openpearl.compiler.Log.LEVEL_DEBUG;
import java.util.ArrayList;
import java.util.List;

public class Options {
    private static boolean printAST = false;
    private static boolean nosemantic = false;
    private static boolean debugControlFlowGraph = false;
    private static boolean checkControlFlowGraphExpressions = false;
    private static boolean coloured = false;
    private static boolean stdPearl90 = false;
    private static boolean stdOpenPEARL = true;
    
    private static List<String> inputFiles = new ArrayList<String>();

    private static String outputFilename = null;
    private static int verbose = 0;
    private static boolean printSysInfo = false;
    private static boolean dumpDFA = false;
    private static boolean dumpSymbolTable = false;
    private static boolean dumpConstantPool = false;
    private static boolean debug = false;
    private static boolean debugSTG = false;
    private static boolean stacktrace = false;

    private static boolean imc = true;
    private static String imc_output = null;
    private static boolean makeStaticDeadlockDetection = false;
    private static String originalSourceFilename =null;
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void printHelp() {
        System.err.println("java org.openpearl.compiler                             \n"
                + " Options:                                                           \n"
                + "  --help                      Print this help message               \n"
                + "  --version                   Print version information             \n"
                + "  --verbose                   Print more information                \n"
                + "  --quiet                     Be quiet                              \n"
             //   + "  --trace                                                           \n"
                + "  --nosemantic                Disable semantic checker              \n"
             //   + "  --noconstantfolding         Disable constant folding              \n"
                + "  --printAST                  Print Abtract Syntax Tree             \n"
             //   + "  --dumpDFA                   Print DFA                             \n"
                + "  --dumpSymbolTable           Print the SymbolTable                 \n"
                + "  --dumpConstantPool          Print the constant pool               \n"
                + "  --debug                     Generate debug information            \n"
                + "  --stacktrace                Print stacktrace in case of an        \n"
                + "                              exception                             \n"
              //  + "  --warninglevel <m_level>    Set the warning m_level               \n"
              //  + "                              Level   0: no warning                 \n"
              //  + "                              Level 255: all warnings (default)     \n"
                + " --imc                        Enable Inter Module Checker           \n"
                + " --imc-output                 Set the IMC output file               \n"
                + "                              if not specified, derive filename from\n"
                + "                              the '--output' option                 \n"
                + " --sysinfo                    Print system information              \n"
                + " -std=OpenPEARL               use OpenPEARL behavior (default)      \n"
                + " -std=PEARL90                 use PEARL90 behavior                  \n"
                + " --coloured                   mark errors with colour               \n"
                + " --output <filename>          Filename of the generated code        \n"
                + " --checkCfgExpr               check expression in  cfg (experimental)\n"
                + " --debugCfg                   Outputs a .dot File with a cfg        \n"
                + " --SDD                        enable static deadlock analysis       \n"
                // source is required for the static deadlock detection export for 
                // multiple modules. --source must be set bei the prl-script
                + " --source                     original source file name without extension\n"
                + " infile ...                                                         \n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean checkAndProcessArguments(String[] args) {
        int i = 0;

        while (i < args.length) {
            String arg = args[i];
            if(verbose > 0) {
               System.out.println("Options: command line arguments arg("+i+")="+arg);
            }
            i++;
            if (arg.charAt(0) != '-') { // input file name
                inputFiles.add(arg);
                continue;
            }

            if (arg.equals("--help")) {
                printHelp();
                System.exit(0);
            } else if (arg.equals("--printAST")) {
                printAST =true;
//            } else if (arg.equals("--tokens")) {
//                showTokens = true;
//            } else if (arg.equals("--trace")) {
//                trace = true;
//            } else if (arg.equals("--SLL")) {
//                SLL = true;
            } else if (arg.equals("--sysinfo")) {
                printSysInfo = true;
            } else if (arg.equals("--nosemantic")) {
                nosemantic = true;
//            } else if (arg.equals("--noconstantfolding")) {
//                constantfolding = false;
            } else if (arg.equals("-std=OpenPEARL")) {
                stdOpenPEARL=true;
                stdPearl90= false;
            } else if (arg.equals("-std=PEARL90")) {
                stdOpenPEARL = false;
                stdPearl90 =true;

//            } else if (arg.equals("--diagnostics")) {
//                diagnostics = true;
//            } else if (arg.equals("--dumpDFA")) {
                dumpDFA = true;
            } else if (arg.equals("--dumpSymbolTable")) {
                dumpSymbolTable = true;
            } else if (arg.equals("--dumpConstantPool")) {
                dumpConstantPool = true;
            } else if (arg.equals("--debug")) {
                debug = true;
                Log.set(LEVEL_DEBUG);
            } else if (arg.equals("--verbose")) {
                verbose ++;
            } else if (arg.equals("--debugSTG")) {
                debugSTG = true;
            } else if (arg.equals("--stacktrace")) {
                stacktrace = true;
            } else if (arg.equals("--imc")) {
                imc =true;
            } else if (arg.equals("--imc-output")) {
                imc_output = args[i];
                i++;
            } else if (arg.equals("--coloured")) {
                coloured = true;
            } else if (arg.equals("--output")) {
                if (i >= args.length) {
                    System.err.println("missing filename on --output");
                    return false;
                }
                outputFilename = args[i];
                i++;
                continue;
            } else if (arg.equals("--source")) {
                if (i >= args.length) {
                    System.err.println("missing filename on --output");
                    return false;
                }
                originalSourceFilename = args[i];
                if (originalSourceFilename.endsWith(".prl")) {
                    System.err.println("no extension expected for --source <filename>");
                    return false;
                }
                i++;
                continue;
            
//            } else if (arg.equals("-encoding")) {
//                if (i >= args.length) {
//                    System.err.println("missing encoding on -encoding");
//                    return false;
//                }
//                encoding = args[i];
//                i++;
//            } else if (arg.equals("--ps")) {
//                if (i >= args.length) {
//                    System.err.println("missing filename on --ps");
//                    return false;
//                }
//                psFile = args[i];
//                i++;
            } else if (arg.equals("--version")) {
                System.out.println("OpenPEARL compiler version " +Compiler.version);
                i++;
//            } else if (arg.equals("--warninglevel")) {
//                if (i >= args.length) {
//                    System.err.println("missing warning m_level on --warninglevel");
//                    return false;
//                }
//                warninglevel = Integer.parseInt(args[i]);
//                i++;
            } else if (arg.equals("--checkCfgExpr")) {
                checkControlFlowGraphExpressions = true;
            } else if (arg.equals("--debugCfg")) {
                debugControlFlowGraph = true;
            } else if (arg.equals("--SDD")) {
                makeStaticDeadlockDetection = true;
            } else {
                System.out.println("Unknown command line argument:" + arg);
                return false;
            }
        }
        
        if (isMakeStaticDeadlockDetection() && originalSourceFilename == null) {
            System.out.println("--SDD requires --source");
            return false;
        }

        return true;
    }
    
    public static boolean isPrintAST() {
        return printAST;
    }

    public static void setPrintAST(boolean printAST) {
        Options.printAST = printAST;
    }

    public static boolean isNosemantic() {
        return nosemantic;
    }

    
    public static boolean isDebugControlFlowGraph() {
        return debugControlFlowGraph;
    }

    public static boolean isCheckControlFlowGraphExpressions() {
        return checkControlFlowGraphExpressions;
    }

    public static boolean isColoured() {
        return coloured;
    }

   
    public static boolean isStdOpenPEARL() {
        return stdOpenPEARL;
    }
    public static boolean isStdPEARL90() {
        return stdPearl90;
    }

    public static List<String> getInputFiles() {
        return inputFiles;
    }


    public static String getOutputFilename() {
        return outputFilename;
    }

    /**
     * the user starts the compilation with a given source file
     * the preprocessor creates an intermediated file, which is passed to this compilation step
     * the original source file name was passed as a separate option without the extension .prl  
     * 
     * 
     * @return the source file name before the preprocessor invocation
     */
    public static String getOriginalSourceFilename() {
        return originalSourceFilename;
    }

    public static int getVerbose() {
        return verbose;
    }

    public static boolean isDumpDFA() {
        return dumpDFA;
    }


    public static boolean isDumpSymbolTable() {
        return dumpSymbolTable;
    }


    public static boolean isDumpConstantPool() {
        return dumpConstantPool;
    }


    public static boolean isDebug() {
        return debug;
    }


    public static boolean isDebugSTG() {
        return debugSTG;
    }


    public static boolean isStdPearl90() {
        return stdPearl90;
    }

    public static boolean isImc() {
        return imc;
    }


    public static boolean isPrintSysInfo() {
        return printSysInfo;
    }

 
    public static boolean isStacktrace() {
        return stacktrace;
    }


    public static String getImc_output() {
        return imc_output;
    }


    public static boolean isMakeStaticDeadlockDetection() {
        return makeStaticDeadlockDetection;
    }
}
