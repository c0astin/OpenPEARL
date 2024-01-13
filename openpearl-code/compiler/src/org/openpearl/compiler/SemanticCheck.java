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

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SemanticAnalysis.*;
//import org.openpearl.compiler.SemanticAnalysis.ControlFlowGraph.ControlFlowGraph;
//import org.openpearl.compiler.SemanticAnalysis.ControlFlowGraph.ControlFlowGraphNode;
//import org.openpearl.compiler.SemanticAnalysis.ControlFlowGraph.NotImplementedException;

public class SemanticCheck {

    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private ExpressionTypeVisitor m_expressionTypeVisitor;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ModuleEntry m_module;
    private ParserRuleContext m_parseTree;
    private AST m_ast = null;
//    ControlFlowGraphGenerate cfgGenerate = null;

    public SemanticCheck(String sourceFileName,
                         int verbose,
                         boolean debug,
                         ParserRuleContext tree,
                         SymbolTableVisitor symbolTableVisitor,
                         ExpressionTypeVisitor expressionTypeVisitor,
                         AST ast) {
        m_debug = debug;
        m_verbose = verbose;
        m_sourceFileName = sourceFileName;
        m_symbolTableVisitor = symbolTableVisitor;
        m_expressionTypeVisitor = expressionTypeVisitor;
        m_ast = ast;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_parseTree = tree;

        if (m_verbose > 0) {
            System.out.println( "Performing semantic check");
        }

        //new CheckVariableDeclaration(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
        new CheckVariableDeclaration(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast);

        //     new CheckDationDeclaration(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
        new CheckProcedureDeclaration(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
        new CheckAssignment(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
      
       
        new CheckSwitchCase(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
        new CheckGotoExit(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);        
        new CheckRealTimeStatements(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
        new CheckDeclarationScope(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
        new CheckArrayDeclarationAccess(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
        new CheckIOStatements(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
        if (ErrorStack.getTotalErrorCount()<=0) {
           new GenerateControlFlowGraph(sourceFileName, symbolTableVisitor, ast).visit(tree);
        }
        if (ErrorStack.getTotalErrorCount()<=0) {
            new CheckUnreachableStatements2(sourceFileName, verbose, debug, symbolTableVisitor, expressionTypeVisitor, ast).visit(m_parseTree);
        }
//        cfgGenerate = new ControlFlowGraphGenerate(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast);
//        cfgGenerate.visit(tree);
//        
//        List<ControlFlowGraph> cfgs = cfgGenerate.getControlFlowGraphs();
        if(ErrorStack.getTotalErrorCount() <= 0) {

//            // this does not work properly yet for all testcases in testsuite/build
//            if (Options.isCheckControlFlowGraphExpressions()) {
//              
//                // Creating List of all procedures
//                Map<String, ParserRuleContext> procedureMap = new HashMap<>();
//                procedureMap.put("NOW", null); // add predefined procedures
//                procedureMap.put("DATE", null);
//                cfgs.forEach(cfg -> {
//              
//                    ControlFlowGraphNode node = cfg.getEntryNode();
//                    if(node.getCtx() instanceof OpenPearlParser.ProcedureDeclarationContext) {
//                        OpenPearlParser.ProcedureDeclarationContext procCtx = (OpenPearlParser.ProcedureDeclarationContext) node.getCtx();
//                        procedureMap.put(procCtx.nameOfModuleTaskProc().ID().toString(), procCtx);
//                    }
//
//                });
//
//                // Finding the ControlFlowGraph of the Module
//                ControlFlowGraph moduleGraph = null;
//                for(ControlFlowGraph cfg : cfgs) {
//                    if(cfg.getName().contains("Module: ")) {
//                        moduleGraph = cfg;
//                        break;
//                    }
//                }
//                // Generating a Stack for the Module
//                if(moduleGraph != null) {
//                    moduleGraph.createVariableStack(procedureMap, null);
//                }
//
//                // The Variable Stack of the Module is passed to all other ControlFlowGraphs,
//                // since the Variables can be accessed from Procedures and Tasks
//                ControlFlowGraph finalModuleGraph = moduleGraph;
//                cfgs.forEach(cfg -> {
//                    try {
//                    if(cfg != finalModuleGraph) {
//                        if(finalModuleGraph != null)
//                            cfg.createVariableStack(procedureMap, finalModuleGraph.getEndNode().getOutputNodes().iterator().next().getVariableStack());
//                        else
//                            cfg.createVariableStack(procedureMap, null);
//                    }
//                    } catch (NotImplementedException e) {
//                        System.err.println("CFD creation aborted");
//                    }
//                });
//            }
//            // outputs a .dot file, when compiler option is turned on, which can be turned into a picture of the ControlFlowGraph with GraphViz
//            Log.info( "debugControlGraph" + Options.isDebugControlFlowGraph());
//            if(Options.isDebugControlFlowGraph()) {
//                outputCFG(cfgs);
//            }

            // Checks the ControlFlowGraphs for unreachable nodes
//            new CheckUnreachableStatements(cfgs).check();
        }
        // CheckUnusedElements must run after CheckGotoExit, maybe later other
        // dependencies may be added
        new CheckUnusedElements(m_sourceFileName, m_verbose, m_debug, m_symbolTableVisitor, m_expressionTypeVisitor, m_ast).visit(m_parseTree);
    }
    
//    public List<ControlFlowGraph> getControlFlowGraphs() {
//        return cfgGenerate.getControlFlowGraphs();
//    }
//    
//    public  void outputCFG(List<ControlFlowGraph> cfgs) {
//        int uniqueId = 0;
//        Map<ControlFlowGraphNode, Integer> nodeIdMap = new HashMap<>();
//        String outputFilePath = m_sourceFileName.substring(0, m_sourceFileName.lastIndexOf(".")) + "_cfg.dot";
//        if (Options.getVerbose()>0) {
//            System.out.println("SemanticCheck: Generating ControlGraph file " + outputFilePath);
//        }
//        try {
//            FileWriter writer = new FileWriter(outputFilePath);
//            writer.write("digraph G {\n");
//            for (ControlFlowGraph cfg : cfgs) {
//                writer.write("\tsubgraph cluster" + (uniqueId++) + " {\n");
//                writer.write("\t\tlabel = \"" + cfg.getName() + "\"\n");
//                for(ControlFlowGraphNode controlFlowGraphNode : cfg.getGraph()) {
//                    nodeIdMap.put(controlFlowGraphNode, uniqueId);
//                    //controlFlowGraphNode.setVariableStack(null);
//                    if(controlFlowGraphNode.getVariableStack() != null)
//                        writer.write("\t\tnode" + (uniqueId++) + " [ label=\"" + controlFlowGraphNode.getStatement() + "\nStack:\n" + controlFlowGraphNode.getVariableStack().toString() + "\" ]\n");
//                    else
//                        writer.write("\t\tnode" + (uniqueId++) + " [ label=\"" + controlFlowGraphNode.getStatement() + "\" ]\n");
//                }
//                for(ControlFlowGraphNode controlFlowGraphNode : cfg.getGraph()) {
//                    for(ControlFlowGraphNode inputControlFlowGraphNode : controlFlowGraphNode.getInputNodes()) {
//                        writer.write("\t\tnode" + nodeIdMap.get(controlFlowGraphNode) + " -> node" +  nodeIdMap.get(inputControlFlowGraphNode) + "\n");
//                    }
//                }
//                writer.write("\t}\n");
//            }
//            writer.write("}");
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
