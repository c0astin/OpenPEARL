/*
 * [The "BSD license"]
 * *  Copyright (c) 2012-2022 Marcel Schaible
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
import org.openpearl.compiler.ControlFlowGraph.ControlFlowGraph;
import org.openpearl.compiler.ControlFlowGraph.ControlFlowGraphGenerate;
import org.openpearl.compiler.ControlFlowGraph.ControlFlowGraphNode;
import org.openpearl.compiler.ControlFlowGraph.NotImplementedException;
import org.openpearl.compiler.SemanticAnalysis.CheckUnreachableStatements;
import org.openpearl.compiler.SymbolTable.FormalParameter;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.VariableEntry;
import org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities.DeadlockControlFlowGraphProcedure;
import org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities.ProcedureCall;
import org.openpearl.compiler.DeadLockDetection.DeadlockControlFlowGraph;
import org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities.DeadlockOperation;
import org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities.DeadlockResourceDeclaration;

import org.stringtemplate.v4.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import static org.openpearl.compiler.Log.*;

public class Compiler {
    static String version = "v0.9.10";
    // static String grammarName;// 2022-11-04 unused
    //static String startRuleName; // 2022-11-04 unused
    static List<String> inputFiles = new ArrayList<String>();
    static String m_sourceFilename = "";
    static boolean printAST = false;
    //static String psFile = null;  // 2022-11-04 unused
    static String outputFilename = null;
   // static boolean showTokens = false;  // 2022-11-04 unused
    //static boolean trace = true;      // 2022-11-04 unused
   // static boolean diagnostics = false; // 2022-11-04 unused
   // static String encoding = null;  // 2022-11-04 unused
    //static boolean SLL = false; // 2022-11-04 unused
    static boolean nosemantic = false;
    //static boolean constantfolding = true; // 2022-11-04 unused
    static int verbose = 0;
    static String groupFile = "OpenPearlCpp.stg";
    //static boolean lineSeparatorHasToBeModified = true; // 2022-11-04 unused
    static boolean dumpDFA = false;
    static boolean dumpSymbolTable = false;
    static boolean dumpConstantPool = false;
    static boolean debug = false;
    static boolean debugSTG = false;
    static boolean debugControlFlowGraph = false;
    static boolean checkControlFlowGraphExpressions = false;
    static boolean stacktrace = false;
    static boolean imc = true;
    static String imc_output = null;
    private static boolean printSysInfo = false;
    private static int noOfErrors = 0;
    // private static int noOfWarnings = 0; // 2022-11-04 unused
    //static int warninglevel = 255;  // 2022-11-04 unused
    static int lineWidth = 80;
    static boolean coloured = false;
    static boolean stdPearl90 = false;
    static boolean stdOpenPEARL = true;
    static SourceLocations m_sourcelocations = new SourceLocations();
    private static boolean makeStaticDeadlockDetection = false;
    // public static boolean enableDetailedCompile = false; // 2022-11-04 unused
    public static Set<String> taskIdentifiers = new TreeSet<>();
    public static List<DeadlockResourceDeclaration> deadlockResourceDeclarations = new ArrayList<>();
    public static List<DeadlockOperation> deadlockOperations = new ArrayList<>();
    public static List<DeadlockControlFlowGraphProcedure> procedureDeclarations = new ArrayList<>();
    public static ArrayList<ProcedureCall> procedureCalls = new ArrayList<>();
     

    public static void main(String[] args) {
        boolean containsRefTaskProcSemaBolt = false;
        int i;

        if (args.length < 1) {
            printHelp();
            return;
        }

        long startTime = System.nanoTime();

        ConstantPool constantPool = new ConstantPool();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor(verbose, constantPool);

        if (!checkAndProcessArguments(args)) {
            return;
        }

        ErrorStack.useColors(coloured);

        // Setup logger
        Log.Logger logger = new Log.Logger();
        Log.setLogger(logger);
        //Log.set(LEVEL_INFO);

        for (i = 0; i < inputFiles.size(); i++) {
            OpenPearlLexer lexer = null;
            AST ast = new AST();

            m_sourceFilename = inputFiles.get(i);

            SourceLocations.setSourceFileName(m_sourceFilename);

            logger.setLogFilename(getBaseName(m_sourceFilename) + ".log");
            Log.info("OpenPEARL compiler version " + version);
            Log.info("Start compiling of:" + m_sourceFilename);
            Log.debug("Performing syntax check");

            try { 
                /* MS: Replacement for deprecated ANTLRFileStream:
                CharStream codePointCharStream = CharStreams.fromFileName("myfile.testlang");
                TESTLANGLexer lexer = new TESTLANGLexer(codePointCharStream);
                TESTLANGParser parser = new TESTLANGParser(new CommonTokenStream(lexer));
                parser.addParseListener(new TESTLANGEventListener());
                // Start parsing
                parser.testlangFile();
                 */

                lexer = new OpenPearlLexer(new ANTLRFileStream(m_sourceFilename));
                //lexer = new SmallPearlLexer(CharStreams.fromFileName(m_sourceFilename));
            } catch (IOException ex) {
                System.out.println("Error:" + ex.getMessage());
                System.exit(-2);
            }

            lexer.removeErrorListeners();
            lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            OpenPearlParser parser = new OpenPearlParser(tokens);

            parser.removeErrorListeners();
            parser.addErrorListener(DescriptiveErrorListener.INSTANCE);

            parser.setBuildParseTree(true);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Start Analysis
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            ParserRuleContext tree = parser.program();

            if (printAST) {
                System.out.println("Parse tree:");
                System.out.println(tree.toStringTree(parser));
            }

            if (dumpDFA) {
                parser.dumpDFA();
            }

            try {
                if (parser.getNumberOfSyntaxErrors() <= 0) {
                    symbolTableVisitor.visit(tree);

                    if (dumpSymbolTable) {
                        symbolTableVisitor.symbolTable.dump();
                    }
                } else {
                    // hot fix - if the parser exited with an error - no further steps are 
                    // useful - we need an non-ok result value for the scripts
                    System.out.println("compilation aborted with errors");
                    System.exit(1);
                }

                ExpressionTypeVisitor expressionTypeVisitor = new ExpressionTypeVisitor(verbose,
                        debug, symbolTableVisitor, constantPool, ast);
                if (ErrorStack.getTotalErrorCount() <= 0) {
                    expressionTypeVisitor.visit(tree);
                }

                if (ErrorStack.getTotalErrorCount() <= 0) {
                    ConstantFoldingVisitor constantFoldingVisitor =
                            new ConstantFoldingVisitor(symbolTableVisitor, ast);
                    constantFoldingVisitor.visit(tree);
                }

                ConstantPoolVisitor constantPoolVisitor =
                        new ConstantPoolVisitor(lexer.getSourceName(), verbose, debug,
                                symbolTableVisitor, constantPool, expressionTypeVisitor, ast);

                if (ErrorStack.getTotalErrorCount() <= 0) {
                    constantPoolVisitor.visit(tree);
                }

                ConstantExpressionEvaluatorVisitor constantExpressionVisitor =
                        new ConstantExpressionEvaluatorVisitor(verbose, debug, symbolTableVisitor,
                                constantPoolVisitor);
                if (ErrorStack.getTotalErrorCount() <= 0) {
                    constantExpressionVisitor.visit(tree);
                }

                if (ErrorStack.getTotalErrorCount() <= 0) {
                    FixUpSymbolTableVisitor fixUpSymbolTableVisitor =
                            new FixUpSymbolTableVisitor(verbose, debug, symbolTableVisitor,
                                    expressionTypeVisitor, constantPoolVisitor, ast);
                    fixUpSymbolTableVisitor.visit(tree);
                }

                if (dumpConstantPool) {
                    ConstantPool.dump();
                }

                if (ErrorStack.getTotalErrorCount() <= 0 && !nosemantic) {
                    /* SemanticCheck semanticCheck = */ 
                    new SemanticCheck(lexer.getSourceName(), verbose,
                            debug, tree, symbolTableVisitor, expressionTypeVisitor, ast);
                }

                // Generating a ControlFlowGraph for every module, procedure, task
                ControlFlowGraphGenerate cfgGenerate = new ControlFlowGraphGenerate(lexer.getSourceName(), verbose, debug, symbolTableVisitor, expressionTypeVisitor, ast);
                cfgGenerate.visit(tree);
                List<ControlFlowGraph> cfgs = cfgGenerate.getControlFlowGraphs();
                
                if(ErrorStack.getTotalErrorCount() <= 0) {

                    if (checkControlFlowGraphExpressions) {
                        // this does not work properly yet for all testcases in testsuite/build
                        // Creating List of all procedures
                        Map<String, ParserRuleContext> procedureMap = new HashMap<>();
                        procedureMap.put("NOW", null); // add predefined procedures
                        procedureMap.put("DATE", null);
                        cfgs.forEach(cfg -> {
                      
                            ControlFlowGraphNode node = cfg.getEntryNode();
                            if(node.getCtx() instanceof OpenPearlParser.ProcedureDeclarationContext) {
                                OpenPearlParser.ProcedureDeclarationContext procCtx = (OpenPearlParser.ProcedureDeclarationContext) node.getCtx();
                                procedureMap.put(procCtx.nameOfModuleTaskProc().ID().toString(), procCtx);
                            }

                        });

                        // Finding the ControlFlowGraph of the Module
                        ControlFlowGraph moduleGraph = null;
                        for(ControlFlowGraph cfg : cfgs) {
                            if(cfg.getName().contains("Module: ")) {
                                moduleGraph = cfg;
                                break;
                            }
                        }
                        // Generating a Stack for the Module
                        if(moduleGraph != null) {
                            moduleGraph.createVariableStack(procedureMap, null);
                        }

                        // The Variable Stack of the Module is passed to all other ControlFlowGraphs,
                        // since the Variables can be accessed from Procedures and Tasks
                        ControlFlowGraph finalModuleGraph = moduleGraph;
                        cfgs.forEach(cfg -> {
                            try {
                            if(cfg != finalModuleGraph) {
                                if(finalModuleGraph != null)
                                    cfg.createVariableStack(procedureMap, finalModuleGraph.getEndNode().getOutputNodes().iterator().next().getVariableStack());
                                else
                                    cfg.createVariableStack(procedureMap, null);
                            }
                            } catch (NotImplementedException e) {
                                System.err.println("CFD creation aborted");
                            }
                        });
                    }
                    // outputs a .dot file, when compiler option is turned on, which can be turned into a picture of the ControlFlowGraph with GraphViz
                    Log.info( "debugControlGraph" + debugControlFlowGraph);
                    if(debugControlFlowGraph) {
                        System.out.println("save control flow graphs");
                        outputCFG(cfgs);
                    }

                    // Checks the ControlFlowGraphs for unreachable nodes
                    new CheckUnreachableStatements(cfgs).check();
                }
                
               
                // check if deadlock detection is possible
                    containsRefTaskProcSemaBolt = false;
                    // check if there are REFs used on TASK,PROC,SEMA or BOLT
                    SymbolTable symbolTable = symbolTableVisitor.symbolTable;
                    LinkedList<VariableEntry> vars= symbolTable.getAllVariableDeclarations();
                    for (VariableEntry v:vars) {
                        if (v instanceof FormalParameter ) {
                           if ( ((FormalParameter)v).passIdentical() ) {
                               if (v.getType() instanceof TypeSemaphore || v.getType() instanceof TypeBolt ) {
                                   ErrorStack.warn(v.getCtx(), "deadlock detection disabled", "not possible with parameter "+v.getType().toErrorString()+" IDENT");
                                   containsRefTaskProcSemaBolt = true;
                                   break;
                                   
                               }
                           }
                        }
                        TypeDefinition td = v.getType();
                        if (td instanceof TypeReference) {
                            td = ((TypeReference) td).getBaseType();
                            if (td instanceof TypeTask ||
                                    td instanceof TypeProcedure ||
                                    td instanceof TypeSemaphore ||
                                    td instanceof TypeBolt) {
                                ErrorStack.warn(v.getCtx(), "deadlock detection disabled", "not possible with REF "+td.toErrorString());
                                containsRefTaskProcSemaBolt = true;
                                break;
                            } else if (td instanceof TypeStructure || td instanceof UserDefinedTypeStructure) {
                                    if (containsIllegalRefForDD(td)) {
                                        ErrorStack.warn(v.getCtx(), "deadlock detection disabled", "not possible with REF "+td.toErrorString());
                                        containsRefTaskProcSemaBolt = true;
                                    }
                            }
                        } else if (td instanceof TypeStructure || td instanceof UserDefinedTypeStructure) {
                            if (containsIllegalRefForDD(td)) {
                                ErrorStack.warn(v.getCtx(), "deadlock detection disabled", "not possible with REF "+td.toErrorString());
                                containsRefTaskProcSemaBolt = true;
                            }
                        }
                    }
              
                
                if (ErrorStack.getTotalErrorCount() <= 0) {
                    CppGenerate(SourceLocations.getTopFileName(), tree, symbolTableVisitor,
                            expressionTypeVisitor, constantExpressionVisitor, ast, stdOpenPEARL, containsRefTaskProcSemaBolt);
                }

                if (ErrorStack.getTotalErrorCount() <= 0 && imc) {
                    String filename = SourceLocations.getTopFileName();
                    if ( filename == null ) {
                        filename = lexer.getSourceName();
                    }
                    SystemPartExport(filename, tree, symbolTableVisitor, ast, stdOpenPEARL);
                }
                
               
               if (makeStaticDeadlockDetection && !containsRefTaskProcSemaBolt) {
                   String filePath = m_sourceFilename.substring(0, m_sourceFilename.lastIndexOf(".")) + "_d_cfg.dot";
                   DeadlockControlFlowGraph.generate(cfgs, filePath);
                }


            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                
                if (debug) {
                    ex.printStackTrace();
                }

                if (verbose > 0) {
                    // keep System.err clean from debug messages. They
                    // disturb the errorChecker
                    System.out.println("Compilation aborted.");
                }

                if (dumpSymbolTable) {
                    symbolTableVisitor.symbolTable.dump();
                }

                if (dumpConstantPool) {
                    ConstantPool.dump();
                }

                if (stacktrace) {
                    System.err.println(getStackTrace(ex));
                }

                System.exit(-1);
            }

            if (ErrorStack.getTotalErrorCount() > 0) {
                System.out.println("compilation aborted with errors");

                if (verbose > 0) {
                    // keep System.err clean from debug messages. They
                    // disturb the errorChecker
                    System.out.println("Compilation aborted.");
                }

                if (dumpSymbolTable) {
                    symbolTableVisitor.symbolTable.dump();
                }

                if (dumpConstantPool) {
                    ConstantPool.dump();
                }

                System.exit(-1);
            }

            noOfErrors = parser.getNumberOfSyntaxErrors();

            System.out.flush();
            System.out.println();
            System.out.println(
                    "Number of errors in " + SourceLocations.getTopFileName() + " encountered: " + noOfErrors);

            if (printSysInfo) {
                String lines;
                long difference = System.nanoTime() - startTime;

                lines = "System Information:\n";
                lines += "Total execution time: "
                        + String.format("%d.%d sec", TimeUnit.NANOSECONDS.toSeconds(difference),
                                TimeUnit.NANOSECONDS.toMillis(difference)
                                - TimeUnit.NANOSECONDS.toSeconds(difference) * 1000);

                SystemInformation sysinfo = new SystemInformation();
                lines += "\n" + sysinfo.Info();
                Log.info(lines);
                System.out.println(lines);
            }

            if (noOfErrors == 0) {
                System.exit(0);
            } else {
                System.exit(1);
            }
        }
    }

    private static boolean containsIllegalRefForDD(TypeDefinition td) {
        if (td instanceof UserDefinedTypeStructure) {
            td = ((UserDefinedTypeStructure) td).getStructuredType();
        }
        TypeStructure ts = (TypeStructure)td;
        StructureComponent sc = ts.getFirstElement();
        while (sc != null) {
            if (sc.m_type instanceof TypeReference) {
                TypeDefinition t = ((TypeReference)sc.m_type).getBaseType();
                if (t instanceof TypeTask ||
                        t instanceof TypeProcedure ||
                        t instanceof TypeSemaphore ||
                        t instanceof TypeBolt) {
                    return true;
                }
            }
            sc = ts.getNextElement();
        }
        return false;
    }

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
                + " infile ...                                                         \n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean checkAndProcessArguments(String[] args) {
        int i = 0;

        while (i < args.length) {
            String arg = args[i];
            if(verbose > 0) {
               System.out.println("command line arguments arg("+i+")="+arg);
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
                printAST = true;
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
                stdOpenPEARL = true;
                stdPearl90 = false;
            } else if (arg.equals("-std=PEARL90")) {
                stdOpenPEARL = false;
                stdPearl90 = true;
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
                imc = true;
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
                System.out.println("OpenPEARL compiler version " + version);
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

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Void CppGenerate(String sourceFileName, ParserRuleContext tree,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            ConstantExpressionEvaluatorVisitor constantExpressionEvaluatorVisitor, AST ast,
            boolean useNameSpaceForGlobals, boolean suppressDeadlockDetection) {

        CppCodeGeneratorVisitor cppCodeGenerator = new CppCodeGeneratorVisitor(sourceFileName,
                groupFile, verbose, debug, symbolTableVisitor, expressionTypeVisitor,
                constantExpressionEvaluatorVisitor, ast, useNameSpaceForGlobals, suppressDeadlockDetection);

        ST code = cppCodeGenerator.visit(tree);

        if (debugSTG) {
            System.out.println("Press a key to continue");
            code.inspect();
            try {
                int ch = System.in.read();
            } catch (IOException ex) {
                ;
            }
        }

        if (outputFilename != null) {
            try {
                if (outputFilename.lastIndexOf(".") == -1) {
                    outputFilename += ".cc";
                }

                if (verbose > 0) {
                    System.out.println("Generating output file " + outputFilename);
                }

                PrintWriter writer = new PrintWriter(outputFilename, "UTF-8");
                writer.println(code.render(lineWidth));
                writer.close();
            } catch (IOException e) {
                System.err.println("ERROR: Cannot write file " + outputFilename);
                System.exit(-1);
            }
        } else {
            if (verbose > 0) {
                System.out.println("Generated output:");
            }

            System.out.println(code.render(lineWidth));
        }

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Void SystemPartExport(String sourceFileName, ParserRuleContext tree,
            SymbolTableVisitor symbolTableVisitor, AST ast, Boolean useNamespaceForGlobals) {
        String outputFileName = sourceFileName;

        SystemPartExporter systemPartExporter =
                new SystemPartExporter(sourceFileName, verbose, debug, symbolTableVisitor, ast, useNamespaceForGlobals);
        ST systemPart = systemPartExporter.visit(tree);

        if (debugSTG) {
            System.out.println("Press a key to continue");
            systemPart.inspect();
            try {
                int ch = System.in.read();
            } catch (IOException ex) {
                ;
            }
        }

        outputFileName = getBaseName(outputFileName).concat(".xml");
        try {
            if (verbose > 0) {
                System.out.println("Generating IMC file " + outputFileName);
            }

            PrintWriter writer = new PrintWriter(outputFileName, "UTF-8");
            writer.println(systemPart.render(lineWidth));
            writer.close();
        } catch (IOException e) {
            System.err.println("ERROR: Cannot write IMC file " + outputFileName);
            System.exit(-1);
        }

        return null;
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

    public static String getSourceFilename() {
        return m_sourceFilename;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void outputCFG(List<ControlFlowGraph> cfgs) {
        int uniqueId = 0;
        Map<ControlFlowGraphNode, Integer> nodeIdMap = new HashMap<>();
        String outputFilePath = m_sourceFilename.substring(0, m_sourceFilename.lastIndexOf(".")) + "_cfg.dot";
        System.out.println("Generating ControlGraph file " + outputFilePath);
        try {
            FileWriter writer = new FileWriter(outputFilePath);
            writer.write("digraph G {\n");
            for (ControlFlowGraph cfg : cfgs) {
                writer.write("\tsubgraph cluster" + (uniqueId++) + " {\n");
                writer.write("\t\tlabel = \"" + cfg.getName() + "\"\n");
                for(ControlFlowGraphNode controlFlowGraphNode : cfg.getGraph()) {
                    nodeIdMap.put(controlFlowGraphNode, uniqueId);
                    //controlFlowGraphNode.setVariableStack(null);
                    if(controlFlowGraphNode.getVariableStack() != null)
                        writer.write("\t\tnode" + (uniqueId++) + " [ label=\"" + controlFlowGraphNode.getStatement() + "\nStack:\n" + controlFlowGraphNode.getVariableStack().toString() + "\" ]\n");
                    else
                        writer.write("\t\tnode" + (uniqueId++) + " [ label=\"" + controlFlowGraphNode.getStatement() + "\" ]\n");
                }
                for(ControlFlowGraphNode controlFlowGraphNode : cfg.getGraph()) {
                    for(ControlFlowGraphNode inputControlFlowGraphNode : controlFlowGraphNode.getInputNodes()) {
                        writer.write("\t\tnode" + nodeIdMap.get(controlFlowGraphNode) + " -> node" +  nodeIdMap.get(inputControlFlowGraphNode) + "\n");
                    }
                }
                writer.write("\t}\n");
            }
            writer.write("}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isStdOpenPEARL() {
        return stdOpenPEARL;
    }
    public static boolean isStdPEARL90() {
        return stdPearl90;
    }
    
}
