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
import org.openpearl.compiler.SymbolTable.FormalParameter;
import org.openpearl.compiler.SymbolTable.ProcedureEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.TaskEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;

//import org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities.DeadlockControlFlowGraphProcedure;
//import org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities.ProcedureCall;
//import org.openpearl.compiler.DeadLockDetection.DeadlockControlFlowGraph;
//import org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities.DeadlockOperation;
//import org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities.DeadlockResourceDeclaration;

import org.stringtemplate.v4.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

public class Compiler {
    static String version = "v0.9.10";
    // note: other options moved to Options.java

    // static String grammarName;// 2022-11-04 unused
    //static String startRuleName; // 2022-11-04 unused

    //static String psFile = null;  // 2022-11-04 unused

    // static boolean showTokens = false;  // 2022-11-04 unused
    //static boolean trace = true;      // 2022-11-04 unused
    // static boolean diagnostics = false; // 2022-11-04 unused
    // static String encoding = null;  // 2022-11-04 unused
    //static boolean SLL = false; // 2022-11-04 unused

    //static boolean constantfolding = true; // 2022-11-04 unused

    static String groupFile = "OpenPearlCpp.stg";
    //static boolean lineSeparatorHasToBeModified = true; // 2022-11-04 unused

    private static String m_sourceFilename = "";


    private static int noOfErrors = 0;
    // private static int noOfWarnings = 0; // 2022-11-04 unused
    //static int warninglevel = 255;  // 2022-11-04 unused
    static int lineWidth = 80;
    static SourceLocations m_sourcelocations = new SourceLocations();

    // public static boolean enableDetailedCompile = false; // 2022-11-04 unused
    public static Set<String> taskIdentifiers = new TreeSet<>();
//    public static List<DeadlockResourceDeclaration> deadlockResourceDeclarations = new ArrayList<>();
//    public static List<DeadlockOperation> deadlockOperations = new ArrayList<>();
//    public static List<DeadlockControlFlowGraphProcedure> procedureDeclarations = new ArrayList<>();
//    public static ArrayList<ProcedureCall> procedureCalls = new ArrayList<>();


    public static void main(String[] args) {
        boolean containsRefTaskProcSemaBolt = false;
        SemanticCheck semanticCheck = null;

        int i;

        if (args.length < 1) {
            Options.printHelp();
            return;
        }

        long startTime = System.nanoTime();

        ConstantPool constantPool = new ConstantPool();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor(Options.getVerbose(), constantPool);

        if (!Options.checkAndProcessArguments(args)) {
            return;
        }

        ErrorStack.useColors(Options.isColoured());

        // Setup logger
        Log.Logger logger = new Log.Logger();
        Log.setLogger(logger);
        //Log.set(LEVEL_INFO);

        for (i = 0; i < Options.getInputFiles().size(); i++) {
            OpenPearlLexer lexer = null;
            AST ast = new AST();

            m_sourceFilename =  Options.getInputFiles().get(i);

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

            if (Options.isPrintAST()) {
                System.out.println("Parse tree:");
                System.out.println(tree.toStringTree(parser));
            }

            if (Options.isDumpDFA()) {
                parser.dumpDFA();
            }

            try {
                if (parser.getNumberOfSyntaxErrors() <= 0) {
                    symbolTableVisitor.visit(tree);

                    if (Options.isDumpSymbolTable() ) {
                        symbolTableVisitor.symbolTable.dump();
                    }
                } else {
                    // hot fix - if the parser exited with an error - no further steps are 
                    // useful - we need an non-ok result value for the scripts
                    System.out.println("compilation aborted with errors");
                    System.exit(1);
                }

                ExpressionTypeVisitor expressionTypeVisitor = new ExpressionTypeVisitor(Options.getVerbose(),
                        Options.isDebug(), symbolTableVisitor, constantPool, ast);
                if (ErrorStack.getTotalErrorCount() <= 0) {
                    expressionTypeVisitor.visit(tree);
                }

                if (ErrorStack.getTotalErrorCount() <= 0) {
                    ConstantFoldingVisitor constantFoldingVisitor =
                            new ConstantFoldingVisitor(symbolTableVisitor, ast);
                    constantFoldingVisitor.visit(tree);
                }

                ConstantPoolVisitor constantPoolVisitor =
                        new ConstantPoolVisitor(lexer.getSourceName(), Options.getVerbose(), Options.isDebug(),
                                symbolTableVisitor, constantPool, expressionTypeVisitor, ast);

                if (ErrorStack.getTotalErrorCount() <= 0) {
                    constantPoolVisitor.visit(tree);
                }

                ConstantExpressionEvaluatorVisitor constantExpressionVisitor =
                        new ConstantExpressionEvaluatorVisitor(Options.getVerbose(), Options.isDebug(), symbolTableVisitor,
                                constantPoolVisitor);
                if (ErrorStack.getTotalErrorCount() <= 0) {
                    constantExpressionVisitor.visit(tree);
                }

                if (ErrorStack.getTotalErrorCount() <= 0) {
                    FixUpSymbolTableVisitor fixUpSymbolTableVisitor =
                            new FixUpSymbolTableVisitor(Options.getVerbose(), Options.isDebug(), symbolTableVisitor,
                                    expressionTypeVisitor, constantPoolVisitor, ast);
                    fixUpSymbolTableVisitor.visit(tree);
                }

                if (Options.isDumpConstantPool()) {
                    ConstantPool.dump();
                }

                if (ErrorStack.getTotalErrorCount() <= 0 && !Options.isNosemantic()) {
                    /* SemanticCheck semanticCheck = */ 
                    semanticCheck = new SemanticCheck(lexer.getSourceName(), Options.getVerbose(), Options.isDebug(),
                            tree, symbolTableVisitor, expressionTypeVisitor, ast);
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

                // output control flow graphs
                if (Options.isDebugControlFlowGraph()) {
                    dumpControlFlowGraphs(symbolTableVisitor.symbolTable);
                }
                 
                

                if (ErrorStack.getTotalErrorCount() <= 0) {
                    CppGenerate(SourceLocations.getTopFileName(), tree, symbolTableVisitor,
                            expressionTypeVisitor, constantExpressionVisitor, ast, Options.isStdOpenPEARL(), containsRefTaskProcSemaBolt);
                }

                if (ErrorStack.getTotalErrorCount() <= 0 && Options.isImc()) {
                    String filename = SourceLocations.getTopFileName();
                    if ( filename == null ) {
                        filename = lexer.getSourceName();
                    }
                    SystemPartExport(filename, tree, symbolTableVisitor, ast, Options.isStdOpenPEARL());
                }

                //System.out.println("Options.isMakeStaticDeadlockDetection()" + Options.isMakeStaticDeadlockDetection());
                boolean sddPossible = Options.isMakeStaticDeadlockDetection() && !containsRefTaskProcSemaBolt;
                String filePath = m_sourceFilename.substring(0, m_sourceFilename.lastIndexOf(".")) + "_d_cfg.dot";
//                    DeadlockControlFlowGraph.generate(semanticCheck.getControlFlowGraphs(), filePath);
                Export4StaticDeadlockDetection exp = new Export4StaticDeadlockDetection(ast, symbolTable);
                exp.generate(sddPossible);


            } catch (Exception ex) {
                System.err.println(ex.getMessage());

                if (Options.isDebug()) {
                    ex.printStackTrace();
                }

                if (Options.getVerbose() > 0) {
                    // keep System.err clean from debug messages. They
                    // disturb the errorChecker
                    System.out.println("Compilation aborted.");
                }

                if (Options.isDumpSymbolTable()) {
                    symbolTableVisitor.symbolTable.dump();
                }

                if (Options.isDumpConstantPool()) {
                    ConstantPool.dump();
                }

                if (Options.isStacktrace()) {
                    System.err.println(getStackTrace(ex));
                }

                System.exit(-1);
            }

            if (ErrorStack.getTotalErrorCount() > 0) {
                System.out.println("compilation aborted with errors");

                if (Options.getVerbose() > 0) {
                    // keep System.err clean from debug messages. They
                    // disturb the errorChecker
                    System.out.println("Compilation aborted.");
                }

                if (Options.isDumpSymbolTable()) {
                    symbolTableVisitor.symbolTable.dump();
                }

                if (Options.isDumpConstantPool()) {
                    ConstantPool.dump();
                }

                System.exit(-1);
            }

            noOfErrors = parser.getNumberOfSyntaxErrors();

            System.out.flush();
            System.out.println();
            System.out.println(
                    "Number of errors in " + SourceLocations.getTopFileName() + " encountered: " + noOfErrors);

            if (Options.isPrintSysInfo()) {
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

    private static void dumpControlFlowGraphs(SymbolTable table_level0) {
        SymbolTable st = table_level0.getModuleTable();
               
        LinkedList<ProcedureEntry> procs=st.getProcedureSpecificationsAndDeclarations();
        LinkedList<TaskEntry> tasks=st.getTaskDeclarationsAndSpecifications();
        //System.out.println("proc "+procs.size());
        //System.out.println("tasks "+tasks.size());
    
        for (int i=0; i<procs.size(); i++) {
            ProcedureEntry pe = ((ProcedureEntry)(procs.get(i)));
            if (pe.getControlFlowGraph() != null) {
               pe.getControlFlowGraph().output(pe.getName());
            }
        }
        for (int i=0; i<tasks.size(); i++) {
            TaskEntry te = ((TaskEntry)tasks.get(i));
            if (te.getControlFlowGraph() != null) {
               te.getControlFlowGraph().output(te.getName());
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

    private static Void CppGenerate(String sourceFileName, ParserRuleContext tree,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            ConstantExpressionEvaluatorVisitor constantExpressionEvaluatorVisitor, AST ast,
            boolean useNameSpaceForGlobals, boolean suppressDeadlockDetection) {

        CppCodeGeneratorVisitor cppCodeGenerator = new CppCodeGeneratorVisitor(sourceFileName,
                groupFile, Options.getVerbose(), Options.isDebug(), symbolTableVisitor, expressionTypeVisitor,
                constantExpressionEvaluatorVisitor, ast, useNameSpaceForGlobals, suppressDeadlockDetection);

        ST code = cppCodeGenerator.visit(tree);

        if (Options.isDebugSTG()) {
            System.out.println("Press a key to continue");
            code.inspect();
            try {
                @SuppressWarnings("unused")
                int ch = System.in.read();
            } catch (IOException ex) {
                ;
            }
        }

        if (Options.getOutputFilename() != null) {
            String outputFilename = Options.getOutputFilename();
            try {
                if (outputFilename.lastIndexOf(".") == -1) {
                    outputFilename += ".cc";
                }

                if (Options.getVerbose() > 0) {
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
            if (Options.getVerbose()  > 0) {
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
                new SystemPartExporter(sourceFileName, Options.getVerbose() , Options.isDebug(), symbolTableVisitor, ast, useNamespaceForGlobals);
        ST systemPart = systemPartExporter.visit(tree);

        if (Options.isDebugSTG()) {
            System.out.println("Press a key to continue");
            systemPart.inspect();
            try {
                @SuppressWarnings("unused")
                int ch = System.in.read();
            } catch (IOException ex) {
                ;
            }
        }

        outputFileName = getBaseName(outputFileName).concat(".xml");
        try {
            if (Options.getVerbose() > 0) {
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


}
