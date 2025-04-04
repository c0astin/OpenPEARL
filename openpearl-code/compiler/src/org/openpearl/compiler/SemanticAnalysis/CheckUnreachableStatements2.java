package org.openpearl.compiler.SemanticAnalysis;

import java.util.LinkedList;
import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.AST;
import org.openpearl.compiler.ASTAttribute;
import org.openpearl.compiler.ErrorStack;
import org.openpearl.compiler.ExpressionTypeVisitor;
import org.openpearl.compiler.OpenPearlBaseVisitor;
import org.openpearl.compiler.OpenPearlParser;
import org.openpearl.compiler.OpenPearlParser.StatementContext;
import org.openpearl.compiler.OpenPearlVisitor;
import org.openpearl.compiler.Options;
import org.openpearl.compiler.SymbolTableVisitor;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.ControlFlowGraph;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.ControlFlowGraphNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.PseudoNode;
import org.openpearl.compiler.SymbolTable.*;


public class CheckUnreachableStatements2  extends OpenPearlBaseVisitor<Void>
implements OpenPearlVisitor<Void> {
    private SymbolTable m_currentSymbolTable = null;
   
    private SymbolTable m_symboltable;
    private AST m_ast = null;
    
    private boolean m_debug;
        
    public CheckUnreachableStatements2(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {
        m_debug = Options.isDebug();
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;
    }
    
    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
         org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        
        LinkedList<ProcedureEntry> listOfProcedures = m_currentSymbolTable.getProcedureDeclarationsAndSpecifications();
        
        for (ProcedureEntry pe: listOfProcedures) {
            if (m_debug) {
                System.out.println("Semantic: Check unreachable code: check PROC "+ pe.getName());
            }
            if (!(pe.isSpecified() || pe.isPredefined())) {
                checkUnreachableStatements(pe.getControlFlowGraph());
            }
        }
        
        LinkedList<TaskEntry> listOfTasks = m_currentSymbolTable.getTaskDeclarationsAndSpecifications();
        
        for (TaskEntry te: listOfTasks) {
            if (m_debug) {
                System.out.println("Semantic: Check unreachable code: check TASK "+ te.getName());
            }
            if (!(te.isSpecified() || te.isPredefined())) {
                checkUnreachableStatements(te.getControlFlowGraph());
            }
        }
        return null;
        
    }
    
    private void checkUnreachableStatements(ControlFlowGraph cfg) {
        boolean warningEmitted = false;
        cfg.clearAllFlagsInGraph();
        
        // mark SIGNAL reactions as reachable
        for (int i=0; i<cfg.getNodeList().size(); i++) {
            ControlFlowGraphNode n = cfg.getNodeList().get(i);
            if (n instanceof PseudoNode) {
                if (((PseudoNode)n).getNodeType() == PseudoNode.sigReaction) {
                   markReachableNodes(n);
                }
            }
        }
        markReachableNodes(cfg.getFirstEntry());
        for (int i=0; i<cfg.getNodeList().size(); i++) {
            ControlFlowGraphNode n = cfg.getNodeList().get(i);
            if (n.isSet(ControlFlowGraph.flag_is_reached)) {
                warningEmitted = false;
            } else {
                markStatementInAstAsUnreachable(n);
                if (warningEmitted) continue;
                warningEmitted = true;
                boolean noWarn = false;
                if (n instanceof PseudoNode) {
                    PseudoNode pseudo = (PseudoNode)n;
                    // END of PROC is normally not reached if the PROC has RETURN
                    // END of REPEAT is not reached if the loop is ended by EXIT, GOTO, RETURN, TERMINATE
                    if (pseudo.getNodeType() == PseudoNode.procEnd ||
                            pseudo.getNodeType() == PseudoNode.repeatBodyEnd ||
                            pseudo.getNodeType() == PseudoNode.blockEnd) {
                        noWarn=true;
                    }
                    // if the END/FIN of the loop, IF or  CASE is no reached, warn with the next statement,
                    // as long as the next statement is not PROC/TASK END
                    if (pseudo.getNodeType() == PseudoNode.repeatEnd || 
                            pseudo.getNodeType() == PseudoNode.ifFin ||
                            pseudo.getNodeType() == PseudoNode.caseFin) {
                        ControlFlowGraphNode next = pseudo.getNext();
                       if (next instanceof PseudoNode) {
                           int nodeType = ((PseudoNode)next).getNodeType();
                           if (nodeType != PseudoNode.procEnd && nodeType != PseudoNode.taskEnd) {
                             n = next;
                        }
                       } else {
                           n = next;
                       }
                    }
                }
                if (!noWarn) { 
                    ErrorStack.warn(n.getCtx(), "unreachable code", "no path to this statement");
                }
            }
        }
        
        //dumpUnreachedNodes(cfg);
    }
    
    private void markReachableNodes(ControlFlowGraphNode n) {
        if (n != null) {
            if (!n.isSet(ControlFlowGraph.flag_is_reached)) {
                n.setFlag(ControlFlowGraph.flag_is_reached);
             //   System.out.println("reached: " + n.getCtx().start.getLine() + ": "+n.printCtx(10));
                for (int node=0; node < n.m_alternatives.size(); node++) {
                    markReachableNodes(n.m_alternatives.get(node));
                }
            }
        }
    }
    
    private void markStatementInAstAsUnreachable(ControlFlowGraphNode n) {
        // set the AST attribute in the statement context
        //statement:
        //    label_statement* ( unlabeled_statement | block_statement | cpp_inline )  ;
        ParserRuleContext c = n.getCtx();
        if (n instanceof PseudoNode) {
            int pseudoNodeType = ((PseudoNode)n).getNodeType();
            if (pseudoNodeType !=PseudoNode.blockBegin && pseudoNodeType != PseudoNode.cppInline) {
               return;
            }
        }
        // mark the superior statementContext 
        //if (!(n instanceof PseudoNode)) {
            while (!(c instanceof StatementContext)) {
                c = c.getParent();
            }

            ASTAttribute attr = m_ast.lookup(c);
            if (attr == null) {
                attr = new ASTAttribute(null);
                m_ast.put(c, attr);
            }
            attr.setIsUnreachableStatement(true);
        //}
    }

   
}
