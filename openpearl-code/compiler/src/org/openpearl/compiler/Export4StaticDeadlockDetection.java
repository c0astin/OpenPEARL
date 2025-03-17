package org.openpearl.compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.OpenPearlParser.*;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.CallNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.CaseNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.CombinedNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.ControlFlowGraph;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.ControlFlowGraphNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.FunctionCallsInNextStatement;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.IfNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.LabelNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.PseudoNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.RepeatNode;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.RepeatNode.RepeatType;
import org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph.SequentialNode;
import org.openpearl.compiler.SymbolTable.ProcedureEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.TaskEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;

/**
 * generate file with control flow graph of the module
 * for the static_analyzer
 * 
 * it contains all 
 *   <ul>
 *   <li> defined (specified) semaphores and bolts
 *   <li> defined procedures
 *   <li> control flow graphs (cfg) of procedures and tasks
 *   </ul>
 *   
 *  the implementation is a dot-file, where the label is json encoded 
 *  
 *  the cfg becomes exported in a simplified version, where
 *   <ul>
 *   <li> unreachable statements are removed from the control flow graph
 *   <li> IF/CASE/REPEAT statements may be removed if they contain nether deadlock relevant operations nor
 *      statements which disrupt the sequential execution (like, GOTO, RETURN, EXIT, CALL)
 *   <li> CASE alternatives with alternatives containing deadlock relevant operations 
 *       will be reduced to these alternative.
 *       <ul>
 *       <li>If an OUT is given and contains deadlock relevant operations then 
 *          the ALT blocks without deadlock operations may be reduced to one CombinedNode
 *       <li>If an OUT is given and contains NO deadlock relevant operations then 
 *          the ALT blocks without deadlock operations may be reduced to nothing
 *       <li>If an OUT is NOT given and 
 *          then the ALT blocks without deadlock operations may be reduced to one CombinedNode
 *       </ul>
 *         
 *   <li> sequences of non deadlock relevant operations may be merged into a single operation
 *   <li> loops without deadlock relevant operations may be reduced to a single CombinedNode 
 *   <li> If a global procedure or task does not contain any deadlock relevant operation this is marked 
 *   in the procedure/task definition with "noDeadlockOperation=true"
 *   <li> Non global tasks without deadlock relevant operations are still exported 
 *   - they could be suppressed in a future step. This would require to remove all tasking statement 
 *   referring these tasks
 *   <li> local procedures without deadlock relevant operations are removed from 
 *      <ul>
 *      <li>the CALL statements and list of function calls in the next statement
 *      <li>the export to the static Analyzer
 *      </ul>
 *   </ul>    
 *     
 *   Principle of operation:
 *   <ol>
 *   <li>In the first step, all PROC/TASKs CFG are treated. 
 *   <li>complete removable block statements are removed by traversing the list of nodes from the end.
 *       This resolves the problems with nested block (IF/CASE/REPEAT/BEGIN), since the node list
 *       is created according the grammar. 
 *   <li>each non-global procedure without deadlock relevant operations becomes removed from 
 *       the list and all function calls in other CFGs are removed 
 *   <li>all CALL nodes and function calls in next statement nodes in all CFGs may be reduced if
 *       the invoked procedures are not deadlock relevant
 *   </ol>     
 *   
 *   Export requirements for multiple module support:
 *   <ul>
 *   <li>the static_analyzer expects unique names for nodes <br>
 *       The name of clusters are not treated by the static_analyzer. Its uses the 'label' tag,
 *       which must be the first line in a cluster to differ between 
 *         <ul>
 *         <li> module information
 *         <li> TASK declaration
 *         <li> PROC declaration
 *         </ul>
 *   <li>the label names are used as strings and compared with others to regenerate the 
 *       graph
 *   <li>in the initial version of the export format worked for a single module with 
 *      node names "node0", "node1", ...
 *   <li>for multiple module operation the node names must become unique.
 *      <ol>
 *      <li>the original source file name is unique
 *      <li>the preprocessor creates an intermediate file usually in /tmp
 *      <li>the compiler got a new option -source=<filename> (without extension)
 *      <li>the node name is generated from the source file name and the input line number 
 *          from the preprocessor output. This creates unique information for each pearl source
 *      <li>DOT allows IDs with a-z,A-Z,0-9 and _ or a quoted string.
 *          In quoted strings DOT requires that double quote is quoted with \ (backslash).
 *          Other layout formatter may require more quotations.
 *      </ol>
 *    <li>quotation rule for the export of the label:
 *      <ul>quotation character in '@'
 *      <li>each '@' in the input in replaced by '@@'
 *      <li>each character outside the valid unquoted IDs is replaced 
 *         by '@' and 2 hex digits with the character code
 *      <li>the encoded label becomes wrapped with double quotes 
 *      </ul>   
 *   </ul>
 */

public class Export4StaticDeadlockDetection {

    /**
     * storage for additional information about the procedures and tasks
     * for the reduction process
     */
    private class ProcTaskInfo {
        public static final int ContainsFunctionCalls = 1, 
                ContainsDeadlockOperations=2, 
                IsSimple = 4, 
                IsAlreadyTreated = 8,
                MyBecomeSimplified  =16;
        public int flags;
        public ControlFlowGraph reducedCfg;

        ProcTaskInfo() {
            flags=0;
            reducedCfg = null;
        }
        
        public ControlFlowGraph getReducedCfg() {
            return reducedCfg;
        }
        public void setReducedCfg(ControlFlowGraph reducedCfg) {
            this.reducedCfg = reducedCfg;
        }

    }

    private class ProcInfo extends ProcTaskInfo {
        public ProcedureEntry pe;
        ProcInfo(ProcedureEntry pe) {
            super();
            this.pe = pe;
        }
    }

    private class TaskInfo extends ProcTaskInfo {
        public TaskEntry te;
        TaskInfo(TaskEntry te) {
            super();
            this.te = te;

        }
    }

    private SymbolTable m_symSymbolTableOfModule;
    private int nextClusterNumber = 0;
    private static final int maxLengthOfStatement = 20;
    private static final int flag_is_deadlock_relevant= 1;
    private static final int flag_is_reduceable = 2;
    private static final int flag_node_reached = 8;
    private static final int flag_remove_me = 16;
    private AST m_ast=null;
    //private LinkedList<ProcedureEntry> procs;
    //private LinkedList<TaskEntry> tasks;
    private int fileCounter=0;
    /**
     * the procedureMap holds the (reduced) deadlock control flow graph for each procedure
     * in the module 
     */
    Vector<ProcInfo> procedureMap;

    /**
     * the taskMap holds the (reduced) deadlock control flow graph for each task
     * in the module 
     */
    Vector<TaskInfo> taskMap;


    public Export4StaticDeadlockDetection(AST ast, SymbolTable symboleTableOfModule) {
        m_symSymbolTableOfModule = symboleTableOfModule.getModuleTable();
        m_ast = ast;

        LinkedList<ProcedureEntry> procs=m_symSymbolTableOfModule.getProcedureSpecificationsAndDeclarations();
        procedureMap = new Vector<ProcInfo>();

        for (int i=procs.size()-1; i>= 0; i--) {
            if (procs.get(i).getControlFlowGraph() != null) {
                procedureMap.add(new ProcInfo(procs.get(i)));
            }
        }

        LinkedList<TaskEntry> tasks=m_symSymbolTableOfModule.getTaskDeclarationsAndSpecifications();
        taskMap = new Vector<TaskInfo>();

        for (int i=tasks.size()-1; i>= 0; i--) {
            if (tasks.get(i).getControlFlowGraph() != null) {
                taskMap.add(new TaskInfo(tasks.get(i)));
            }
        }


    }

    public void generate(boolean sddPossible) {

        if (sddPossible)  {
            // create deadlock CFG for all tasks and procedures in the module
            createAllProcedureAndTaskDCFG();

            // remove local procedures without deadlock relevant operations. 
            // These procedures are named as 'simple procedures'

            removeCallsToSimpleProcedures();
            
            // the staticAnalyzer requires a CFG even for simple procedures
            // let' remain them in the _d_cfg.dot-file
            //removeSimpleNonglobalProceduresFromMap();
        }
        String outputFilePath = Options.getInputFiles().get(0);
        outputFilePath = outputFilePath.substring(0,outputFilePath.length()-4); // remove .prl
        outputFilePath += "_d_cfg.dot";

        if (Options.isDebug()) {
            System.out.println("generate deadlock control graph "+outputFilePath);
        }

        try {
            FileWriter writer = new FileWriter(outputFilePath);

            writer.write("digraph G {\n");

            addDeadlockResources(writer,sddPossible);

            if (sddPossible) {
                addTaskGraphs(writer);
                addProcedureGraphs(writer);
            }
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            ErrorStack.addInternal("failed to write on file "+outputFilePath);
        }
    }

    /**
     * 
     * @return true, if a simplification was applied
     */
    private boolean removeCallsToSimpleProcedures() {
        boolean anyCallRemoved;
        do {
            anyCallRemoved = false;
            boolean callRemoved; 
            do {
                callRemoved=false;
                Vector<ProcedureEntry> removeTheseProcCalls = new Vector<ProcedureEntry>();
                for (ProcInfo pi: procedureMap) {
                    ControlFlowGraph dfg = pi.reducedCfg;
                    // detect simple procedures:
                    // at this point, a simple procedure has a CFG with 2 nodes (ENTRY and END)
                    if (dfg != null && 
                            dfg.getNodeList().size() == 2 && 
                            (pi.flags&ProcTaskInfo.IsAlreadyTreated)==0) {
                        //System.out.println("new simple PROC detected "+pi.pe.getName());
                        removeTheseProcCalls.add(pi.pe);

                        // let's mark the ProcedureEntry as treated right now, since it becomes 
                        // treated in this function
                        pi.flags |= ProcTaskInfo.IsAlreadyTreated;
                    }
                }

                if (removeTheseProcCalls.size()>0) {
                    for (ProcInfo pi: procedureMap) {
                        //System.out.println("remove function calls in "+pi.pe.getName());
                        if (removeCalls(removeTheseProcCalls, pi.reducedCfg)) {
                            pi.flags |= ProcTaskInfo.MyBecomeSimplified;
                            callRemoved = true;
                            anyCallRemoved = true;
                        }
                    }
                    for (TaskInfo ti: taskMap) {
                        //System.out.println("remove function calls in "+ti.te.getName());
                        removeCalls(removeTheseProcCalls, ti.reducedCfg);
                    }
                }
            } while (callRemoved);

            if (anyCallRemoved) {
                // ok, some procedures my become simplified
                for (ProcInfo pi: procedureMap) {
                    if((pi.flags& ProcTaskInfo.MyBecomeSimplified) != 0) {
                        reduceControlFlowGraph(pi.reducedCfg);
                        //pi.flags &= ~ProcTaskInfo.MyBecomeSimplified;
                        pi.flags = 0;
                    }
                }
            }
        } while (anyCallRemoved);
        return (anyCallRemoved);
    }

    private void removeSimpleNonglobalProceduresFromMap() {
        Iterator<ProcInfo> it = procedureMap.iterator();
        while (it.hasNext()) {
            ProcInfo pi = it.next();
            //            System.out.println("removeing...: "+pe.getName()+ 
            //                    " size"+cfg.getNodeList().size());
            if (pi.pe.getGlobalAttribute()==null && pi.reducedCfg.getNodeList().size() == 2) {
                it.remove();
            }
        }
    }

    /**
     * remove function calls to the given list of function, 
     * which are already detected to be not deadlock relevant
     * 
     * @param removeTheseProcCalls Vector<ProcedureEntry> which contain no DL relevant operation  
     * @param controlFlowGraph the ContolFlowGraph , which shoul be treated
     * @return true, if the given ControlFlowGrpah was modified<br>
     *         false, if there was no reduction
     */
    private boolean removeCalls(Vector<ProcedureEntry> removeTheseProcCalls,
            ControlFlowGraph controlFlowGraph) {
        boolean somethingRemoved = false;
        for (ControlFlowGraphNode n: controlFlowGraph.getNodeList()) {
            if (n instanceof CallNode) {
                String name = null;
                if (n.getCtx() instanceof NameContext) {
                    name = ((NameContext)(n.getCtx())).ID().getText();
                } else if (n.getCtx() instanceof CallStatementContext) {
                    name = ((CallStatementContext)n.getCtx()).name().ID().getText();
                } else {
                    ErrorStack.addInternal("Export4StaticAlanyzer@342: missing alternative");
                }
                if (n != null) {
                    for (int i=0; i<removeTheseProcCalls.size(); i++) {
                        if (removeTheseProcCalls.get(i).getName().equals(name)) {
                            //System.out.println("found function call to "+name);
                            Vector<ControlFlowGraphNode> predecessors = getPredecessors(controlFlowGraph, n);
                            for (int j=0; j<predecessors.size(); j++) {
                                // replace (all) alternatives to 'n'
                                replaceAlternatives(predecessors.get(j),n,n.getNext());
                            }
                            n.setFlag(flag_remove_me);
                            somethingRemoved = true;
                        }
                    }

                }
            }
        }
        removeRemoveableNodes(controlFlowGraph);

        return somethingRemoved;
    }

    private void createAllProcedureAndTaskDCFG() {
        for (ProcInfo pi: procedureMap) {
            System.out.println("check "+pi.pe.getName());
            if (pi.pe.getControlFlowGraph() != null) {
                ControlFlowGraph copy = new ControlFlowGraph(pi.pe.getControlFlowGraph());
                /*copy = */reduceControlFlowGraph(copy);
                pi.reducedCfg = copy;
            }
        }

        for (TaskInfo ti: taskMap) {
            if (ti.te.getControlFlowGraph() != null) {
                ControlFlowGraph copy = new ControlFlowGraph(ti.te.getControlFlowGraph());
                /*copy =*/ reduceControlFlowGraph(copy);
                ti.reducedCfg = copy;
            }
        }
    }

    private boolean containsDeadlockRelevantNode(ControlFlowGraph cfg) {
        for (int i=0; i<cfg.getNodeList().size(); i++) {
            if (isDeadlockRelevant(cfg.getNodeList().get(i))) {
                return true;
            }
        }
        return false;
    }


    private boolean containsDeadlockOperation(ControlFlowGraph cfg) {
        for (int i=0; i<cfg.getNodeList().size(); i++) {
            if (isDeadlockRelevant(cfg.getNodeList().get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFunctionCalls(ControlFlowGraph cfg) {
        for (int i=0; i<cfg.getNodeList().size(); i++) {
            ControlFlowGraphNode n = cfg.getNodeList().get(i);
            if (n instanceof CallNode) {
                return true;
            }
            if (n instanceof FunctionCallsInNextStatement) {
                return true;
            }

        }
        return false;
    }



    private void addTaskGraphs(FileWriter writer) {
        for(TaskInfo ti: taskMap) {
            addGraph(writer, "Task: "+ti.te.getName(), ti.reducedCfg);
        }
    }

    private void addProcedureGraphs(FileWriter writer) {
        for(ProcInfo pi: procedureMap) {
//        if (!(pi.pe.getGlobalAttribute() != null && pi.reducedCfg.getNodeList().size() == 2)) {
         // the staticAnalyzer required up to now (2023-05-10) even simple procedure 
         // CFGs --> let'S create them until the staticAnalyzer rreacts on the
         // tag in the procedure definition in the mod-section, if the procedure is global

          if (pi.pe.getGlobalAttribute() != null) {
                addGraph(writer, "Procedure: "+pi.pe.getName(), pi.reducedCfg);
            }
        }
    }

    private ControlFlowGraph reduceControlFlowGraph(ControlFlowGraph copy) {
        int startingNumberOfRealNodes = 0;
        copy.clearAllFlagsInGraph();
        
        // remove unreachable nodes
        markReachableNodes(copy.getFirstEntry());
        for (int i =0; i<copy.getNodeList().size(); i++) {
            //System.out.println(i+" "+copy.getNodeList().get(i).printCtx(20)+" "+copy.getNodeList().get(i).isSet(flag_node_reached));
            if (!(copy.getNodeList().get(i) instanceof PseudoNode) ) {
                startingNumberOfRealNodes++;
            }
            if (!copy.getNodeList().get(i).isSet(flag_node_reached)) {
                copy.getNodeList().get(i).setFlag(flag_remove_me);
                //System.out.println(i+" mark node to remove"+copy.getNodeList().get(i).printCtx(maxLengthOfStatement));
            }
        }
        removeRemoveableNodes(copy);
        copy.clearAllFlagsInGraph();
        

        // mark deadlock relevant operations
        int nbrOfDeadlockRelevantOperations = 0;
        for (int i=0; i< copy.getNodeList().size(); i++) {
            ControlFlowGraphNode n = copy.getNodeList().get(i);

            if (isDeadlockRelevant(n)) {
                n.setFlag(flag_is_deadlock_relevant);
                nbrOfDeadlockRelevantOperations++;
            } else if (n instanceof FunctionCallsInNextStatement) {
                nbrOfDeadlockRelevantOperations++;
                n.setFlag(flag_is_deadlock_relevant);
            } else if (n instanceof CallNode) {
                nbrOfDeadlockRelevantOperations++;
                n.setFlag(flag_is_deadlock_relevant);
            } else if (disruptSequentialExecution(n) || 
                    (n instanceof PseudoNode && ((PseudoNode)n).getNodeType() != PseudoNode.cppInline )||
                    n instanceof IfNode ||
                    n instanceof RepeatNode ||
                    n instanceof LabelNode ||
                    n instanceof CaseNode ) {
                // is not reducible! at this point of operation
            } else {
                n.setFlag(flag_is_reduceable);
            }

            //System.out.println(n.printCtx(10)+" dl "+n.isSet(flag_is_deadlock_relevant)+
            //        " reduc: " + n.isSet(flag_is_reduceable));
        }
        if (nbrOfDeadlockRelevantOperations == 0) {
            // we can reduce all except entry and end
            PseudoNode entry=null;

            int initialSize = copy.getNodeList().size();
            for (int i=0; i< initialSize; i++) {
                ControlFlowGraphNode n = copy.getNodeList().get(i);
                //System.out.println(n.printCtx(20));

                if (n instanceof PseudoNode) {
                    PseudoNode pn = (PseudoNode)n;
                    if (pn.getNodeType() == PseudoNode.taskEntry ||
                            pn.getNodeType() == PseudoNode.procEntry ) {
                        entry=pn;
                    } else if (pn.getNodeType() == PseudoNode.taskEnd ||
                            pn.getNodeType() == PseudoNode.procEnd ) {
                        entry.setNext(n);
                    } else {
                        n.setFlag(flag_remove_me);
                    }
                } else {
                    n.setFlag(flag_remove_me);
                }

            }

            removeRemoveableNodes(copy);

        } else {
            //copy.output("abc"+fileCounter++);

            // traverse graph and mark sequences of irrelevant statements (leave the first unmarked)
            // mark blocks (BEGIN/END,IF,REPEAT, CASE as irrelevant if they contain no relevant operation

            // mark deadlock operations
            for (int i=0; i<copy.getNodeList().size(); i++) {
                if (isDeadlockRelevant(copy.getNodeList().get(i))) {
                    copy.getNodeList().get(i).setFlag(flag_is_deadlock_relevant);
                }
            }

            reduceFineBlockStatements(copy);
            copy.output("abc"+fileCounter++);

                    System.out.println("\n");
                    for (int i=0; i< copy.getNodeList().size(); i++) {
                        ControlFlowGraphNode n = copy.getNodeList().get(i);
                        System.out.println(n.printCtx(10) + " deadlockRelevant:"
                                + n.isSet(flag_is_deadlock_relevant) + " reducable: "
                                + n.isSet(flag_is_reduceable) + " reached: " + n.isSet(flag_node_reached)
                                );
                    }

            //copy.output("abc"+fileCounter++);

            // remove LabelNodes: redirect all predecessors with the labelNode.getNext()
            // remove GOTO nodes: redirect the predecessor node with gotoNode.getNext()
            // mark RETURN statements as reducible for the merging step
            for (int i=0; i< copy.getNodeList().size(); i++) {
                ControlFlowGraphNode n = copy.getNodeList().get(i);
                if (n instanceof LabelNode) {
                    Vector<ControlFlowGraphNode> predecessors = getPredecessors(copy, n);
                    for (int j=0; j<predecessors.size(); j++) {
                        // replace (all) alternatives to 'n'
                        replaceAlternatives(predecessors.get(j),n,n.getNext());
                    }
                    n.setFlag(flag_remove_me);
                } else if (n instanceof SequentialNode && 
                        n.getCtx() instanceof GotoStatementContext) {
                    Vector<ControlFlowGraphNode> predecessors = getPredecessors(copy, n);
                    for (int j=0; j<predecessors.size(); j++) {
                        // replace (all) alternatives to 'n'
                        replaceAlternatives(predecessors.get(j),n,n.getNext());
                    }
                    n.setFlag(flag_remove_me);      
                } else if (n instanceof SequentialNode && 
                        n.getCtx() instanceof ReturnStatementContext) {
                    n.setFlag(flag_is_reduceable);
                }

            }
            removeRemoveableNodes(copy);
            copy.output("abc"+fileCounter++);

            // merge reducible sequences including RETURN

            for (int i=0; i< copy.getNodeList().size(); i++) {
                ControlFlowGraphNode n = copy.getNodeList().get(i);
                CombinedNode combindNode = null;
                if (n.isSet(flag_is_reduceable) && !n.isSet(flag_remove_me)) {

                    ControlFlowGraphNode next = n.getNext();
                    if (!(n instanceof CombinedNode)) {
                        combindNode = new CombinedNode(n.getCtx());
                        copy.addNode(combindNode);
                        n.setFlag(flag_remove_me);
                        Vector<ControlFlowGraphNode> previous = getPredecessors(copy, n);
                        for (int j=0; j<previous.size(); j++) {
                            replaceAlternatives(previous.get(j), n, combindNode);
                        }
                        combindNode.setFlag(flag_is_reduceable);
                        combindNode.setNext(next);

                    } else {
                        combindNode = (CombinedNode)n;
                    }
                    while (/*next != null && */ next.isSet(flag_is_reduceable)) {
                        combindNode.add(next);
                        combindNode.setNext(next.getNext());
                        next.setFlag(flag_remove_me);
                        next = next.getNext();
                    }
                }
            }
            removeRemoveableNodes(copy);
            //copy.output("abc"+fileCounter++);

            // replace FunctionCallInNextStatement by CallNode(s)
            for (int i=0; i<copy.getNodeList().size(); i++) {
                if (copy.getNodeList().get(i) instanceof FunctionCallsInNextStatement) {
                    FunctionCallsInNextStatement fcnCalls = (FunctionCallsInNextStatement) copy.getNodeList().get(i);
                    ControlFlowGraphNode next = copy.getNodeList().get(i).getNext();
                    CallNode callNode = new CallNode(fcnCalls.getCalls().get(0));
                    callNode.setNext(next);
                    copy.getNodeList().set(i, callNode);

                    Vector<ControlFlowGraphNode> predecessors = getPredecessors(copy, fcnCalls);
                    for (int j=0; j<predecessors.size(); j++) {
                        // replace (all) alternatives to 'n'
                        replaceAlternatives(predecessors.get(j),fcnCalls,callNode);
                    }


                    for (int j=1; j< fcnCalls.getCalls().size(); j++) {
                        CallNode cn = new CallNode(fcnCalls.getCalls().get(j));
                        copy.addNode(cn);
                        callNode.setNext(cn);
                        cn.setNext(next);
                        callNode = cn;
                    }

                }
            }

            removeRemoveableNodes(copy);
            //copy.output("abc"+fileCounter++);

            // ?? merge reducible parallel paths 
            // may by difficult! --> not implemented yet 

            // remove PseudoNodes except procBegin/TaskBegin/ProcEnd and TaskEnd
            for (int i=0; i<copy.getNodeList().size(); i++) {
                ControlFlowGraphNode n = copy.getNodeList().get(i);
                if (n instanceof PseudoNode) {
                    int pseudoNodeType = ((PseudoNode)n).getNodeType();
                    if (pseudoNodeType != PseudoNode.taskEntry &&
                            pseudoNodeType != PseudoNode.taskEnd &&
                            pseudoNodeType != PseudoNode.procEntry &&
                            pseudoNodeType != PseudoNode.procEnd) {
                        // remove this node
                        Vector<ControlFlowGraphNode> predecessors = getPredecessors(copy, n);
                        for (int j=0; j<predecessors.size(); j++) {
                            replaceAlternatives(predecessors.get(j), n, n.getNext());
                        }
                        n.setFlag(flag_remove_me);
                    }
                }
            }
            removeRemoveableNodes(copy);
            //copy.output("abc"+fileCounter++);
        }

        int resultingNumberOfNodes = 0;
        for (int i=0; i<copy.getNodeList().size(); i++) {
            if (!(copy.getNodeList().get(i) instanceof PseudoNode) ) {
                resultingNumberOfNodes ++;
            }
        }

        //copy.output("abc"+fileCounter++);
        Log.debug("export4StaticDeadlockDetection: reduction of nodes "+startingNumberOfRealNodes+" -> " + resultingNumberOfNodes);;
        return copy;
    }

    private String getUniqueNodename(ParserRuleContext ctx) {
        String result = "";
        String originalSource = Options.getOriginalSourceFilename();
        for (int i=0; i<originalSource.length(); i++) {
            char ch = originalSource.charAt(i); 
            if ( (ch >='A' && ch <='Z') ||
                    (ch >='a' && ch <='z') ||
                    (ch >='0' && ch <='9') ||
                    (ch == '_') ) {
                result += ch;
            } else if (ch == '@') {
                result += "@@";
            } else {
                int numerical = ch;
                result += "@" + Integer.toHexString(numerical);
            }
        }
        result += ":" + ctx.start.getLine()+":"+ctx.start.getCharPositionInLine();
        result = "\""+result + "\"";
        return result;
    }

    private void addGraph(FileWriter writer, String name, ControlFlowGraph controlFlowGraph) {

        ControlFlowGraph copy = controlFlowGraph; // reduceControlFlowGraph(controlFlowGraph);

        try {
            writer.write("   subgraph cluster_"+nextClusterNumber +"{\n");
            nextClusterNumber++;
            writer.write("       label = \""+name+"\"\n");
            for (ControlFlowGraphNode n: copy.getNodeList()) {
                ControlFlowDataWrapper wrapper = new ControlFlowDataWrapper();
                wrapper.set("codeFilename",getSourceFile(n.getCtx()));
                wrapper.set("codeLineNumber",getSourceLine(n.getCtx()));
                encodeNode(wrapper,n);
                String un = getUniqueNodename(n.getCtx());
                if (wrapper.getItem("label") == null) { 
                    writer.write("      " + un +" [ label=\"" +
                            encode4Dot(wrapper.toJson())+"\" ] \n");
                } else {
                    writer.write("     " + un + " [ label=\""+wrapper.getItem("label") + "\" ] \n");
                }
            }

            // add edges
            for (ControlFlowGraphNode n: copy.getNodeList()) {
                String startNode = getUniqueNodename(n.getCtx());
                for (ControlFlowGraphNode dest :n.m_alternatives) {
                    int idx = copy.getNodeList().indexOf(dest);
                    if (dest!= null && copy.getNodeList().indexOf(dest)>= 0) {
                        String destNode = getUniqueNodename(dest.getCtx());
                        writer.write  ("    " + startNode + " -> " + destNode + "\n");
                    }
                }
            }

            writer.write("   }\n");
        } catch (IOException e) {
            ErrorStack.addInternal("Export4StaticAlalyzer@722: got I/O exception");
        }
    }

    /**
     * helper method for removal of nodes.
     * 
     * each node may contain more than 1 alternative as next node in the graph
     * 
     * Each reference in the previousNode to the pointsTo node is replaced by 
     * newTarget
     * 
     * @param previousNode a node which contains a reference to pointsTo
     * @param pointsTo the node which should be removed from the graph
     *   all references to the node, which should be removed must be replaced 
     *   to the newTarget 
     * @param newTarget the new alternative
     */
    private void replaceAlternatives(ControlFlowGraphNode previousNode,
            ControlFlowGraphNode pointsTo, ControlFlowGraphNode newTarget) {
        for (int i=0; i<previousNode.m_alternatives.size(); i++) {
            if (previousNode.m_alternatives.get(i) != null &&
                    previousNode.m_alternatives.get(i).equals(pointsTo)) {
                previousNode.m_alternatives.set(i, newTarget);
            }
        }
    }

    private void markReachableNodes(ControlFlowGraphNode n) {
        if (n != null) {
            if (!n.isSet(flag_node_reached)) {
                n.setFlag(flag_node_reached);
                for (int node=0; node < n.m_alternatives.size(); node++) {
                    markReachableNodes(n.m_alternatives.get(node));
                }
            }
        }
    }


    /*
     * remove IF/CASE/REPEAT statements without deadlock operations and 
     * unmodified control flow (EXIT,RETURN,CALL)
     */
    private void  reduceFineBlockStatements(ControlFlowGraph cfg) {
        for (int i= cfg.getNodeList().size()-1; i>= 0; i--) {
            if (cfg.getNodeList().get(i) instanceof PseudoNode &&
                    ((PseudoNode)cfg.getNodeList().get(i)).getNodeType() == PseudoNode.blockBegin) {
                reduceBeginStatement(cfg, cfg.getNodeList().get(i));
            }
            if (cfg.getNodeList().get(i) instanceof IfNode) {
                reduceIfStatement(cfg, (IfNode)(cfg.getNodeList().get(i)));
            }
            if (cfg.getNodeList().get(i) instanceof CaseNode) {
                reduceCaseStatement(cfg, (CaseNode)(cfg.getNodeList().get(i)));
            }
            if (cfg.getNodeList().get(i) instanceof PseudoNode &&
                    ((PseudoNode)cfg.getNodeList().get(i)).getNodeType() == PseudoNode.repeatBegin) {
                reduceRepeatStatement(cfg, (PseudoNode)(cfg.getNodeList().get(i)));
            }

        }
        removeRemoveableNodes(cfg);
        cfg.output(null);

    }

    private void removeRemoveableNodes(ControlFlowGraph cfg) {
        for (int i=cfg.getNodeList().size()-1; i>=0; i--) {
            if (cfg.getNodeList().get(i).isSet(flag_remove_me)) {
                cfg.getNodeList().remove(i);
            }
        }
    }
    
//    private void reduceSequentialExecution(ControlFlowGraph cfg, 
//            ControlFlowGraphNode startPseudoNode) {
//        boolean allReducible = true;
//        ControlFlowGraphNode node = startPseudoNode.getNext();
//        boolean goon = true;
//        do {
//            if (!node.isSet(flag_is_reduceable)) {
//                allReducible = false;
//            }
//            node = node.getNext();
//            if (node instanceof PseudoNode) {
//                PseudoNode pn = (PseudoNode) node;
//                int nodeType = pn.getNodeType();
//                if (nodeType == PseudoNode.blockEnd || nodeType == PseudoNode.caseAlt ||
//                        nodeType == PseudoNode.caseFin || nodeType == PseudoNode.caseOut ||
//                        nodeType == PseudoNode.ifElse || nodeType == PseudoNode.ifFin ||
//                        nodeType == PseudoNode.repeatBodyEnd) {
//                    goon = false;
//                }
//            }
//        } while (goon);
//        
//        
//    }

    private void reduceBeginStatement(ControlFlowGraph cfg,
            ControlFlowGraphNode node) {
        String s = node.printCtx(maxLengthOfStatement);
        Log.debug("test reduce "+s);
        ControlFlowGraphNode blockBegin = node;
        ControlFlowGraphNode blockEnd = null;
        boolean allReducible=true;
        node = node.getNext();  // ignore blockBegin

        while (!((node instanceof PseudoNode) &&
                ((PseudoNode)node).getNodeType() ==PseudoNode.blockEnd)) {
            if (!node.isSet(flag_is_reduceable)) {
                allReducible=false;
            }
            node = node.getNext();
        }
        blockEnd = node;
        if (allReducible) {
            CombinedNode combined = new CombinedNode(blockBegin.getCtx());
            cfg.addNode(combined);
            getPredecessor(cfg, blockBegin).setNext(combined);
            combined.setFlag(flag_is_reduceable);
            combined.setNext(blockEnd.getNext());
            for (node=blockBegin; !(node instanceof PseudoNode &&
                    ((PseudoNode)node).getNodeType() ==PseudoNode.blockEnd); 
                    node=node.getNext()) {
                node.setFlag(flag_remove_me);
            }
            node.setFlag(flag_remove_me);
        }
    }

    private void reduceIfStatement(ControlFlowGraph cfg, IfNode ifNode) {
        boolean allAlternativesAreReducable = true; 
        //String s = ifNode.printCtx(maxLengthOfStatement);
        //System.out.println("test reduce "+s);
        for (int i=0; i< ifNode.m_alternatives.size(); i++) {
            ControlFlowGraphNode alt = ifNode.m_alternatives.get(i);

            if (alt != null) {
                // a missing else-path points directly to the FinNode
                if (!alt.equals(ifNode.getFinNode())) {

                    if (allReducable(alt.getNext())) {
                        alt.setFlag(flag_is_reduceable);
                    } else  {
                        allAlternativesAreReducable = false;
                    }
                }
            }
        } 

        if (allAlternativesAreReducable) {
            ifNode.setFlag(flag_is_reduceable);
            ifNode.getFinNode().setFlag(flag_is_reduceable);
            //System.out.println("reduce "+s);
            Vector<ControlFlowGraphNode> previousNodes = new Vector<ControlFlowGraphNode>() ;
            previousNodes = getPredecessors(cfg, ifNode);
            CombinedNode combined = new CombinedNode(ifNode.getCtx());
            combined.setNext(ifNode.getFinNode().getNext());
            cfg.addNode(combined);
            combined.setFlag(flag_is_reduceable);

            if (previousNodes.size() != 1) {
                System.out.println("uups: should be 1 - is "+previousNodes.size());
            } else {
                previousNodes.get(0).setNext(combined);
            }

            // remove nodes from the if-statement from the cfg
            ifNode.setFlag(flag_remove_me);
            for (ControlFlowGraphNode c = ifNode.getThenNode(); !c.equals(ifNode.getFinNode()); c = c.getNext()) {
                c.setFlag(flag_remove_me);
            }
            if (ifNode.getElseNode()!= null) {
                for (ControlFlowGraphNode c = ifNode.getElseNode(); !c.equals(ifNode.getFinNode()); c = c.getNext()) {
                    c.setFlag(flag_remove_me);
                } 
            }
            ifNode.getFinNode().setFlag(flag_remove_me);
        }
    }

    private void reduceCaseStatement(ControlFlowGraph cfg, CaseNode caseNode) {
        boolean allAlternativesAreReducable = true; 
        //String s = caseNode.printCtx(maxLengthOfStatement);
        //System.out.println("test reduce "+s);
        for (int i=0; i< caseNode.m_alternatives.size(); i++) {
            ControlFlowGraphNode alt = caseNode.m_alternatives.get(i);

            if (alt != null) {
                if (allReducable(alt.getNext())) {
                    alt.setFlag(flag_is_reduceable);
                } else  {
                    allAlternativesAreReducable = false;
                }
            }
        } 

        if (allAlternativesAreReducable) {
            caseNode.setFlag(flag_is_reduceable);
            caseNode.getFinNode().setFlag(flag_is_reduceable);
            //System.out.println("reduce "+s);
            Vector<ControlFlowGraphNode> previousNodes = new Vector<ControlFlowGraphNode>() ;
            previousNodes = getPredecessors(cfg, caseNode);
            CombinedNode combined = new CombinedNode(caseNode.getCtx());
            combined.setNext(caseNode.getFinNode().getNext());
            //combined.setLines(caseNode.getCtx().start.getLine(), caseNode.getCtx().stop.getLine());
            cfg.addNode(combined);
            combined.setFlag(flag_is_reduceable);

            if (previousNodes.size() != 1) {
                System.out.println("uups: should be 1 - is "+previousNodes.size());
            } else {
                previousNodes.get(0).setNext(combined);
            }

            // remove nodes from the case-statement from the cfg
            caseNode.setFlag(flag_remove_me);
            for (int i=0; i<caseNode.m_alternatives.size(); i++) {
                if (caseNode.m_alternatives.get(i) != null) {
                    for (ControlFlowGraphNode c = caseNode.m_alternatives.get(i); !c.equals(caseNode.getFinNode()); c = c.getNext()) {
                        c.setFlag(flag_remove_me);
                    }
                }
            }
            caseNode.getFinNode().setFlag(flag_remove_me);
        } else {
            /* <li> CASE alternatives with alternatives containing deadlock relevant operations 
             *       will be reduced to these alternative.
             *       <ul>
             *       <li>If an OUT is given and contains deadlock relevant operations then 
             *          the ALT blocks without deadlock operations may be reduced to one CombinedNode
             *       <li>If an OUT is given and contains NO deadlock relevant operations then 
             *          the ALT blocks without deadlock operations may be reduced to nothing
             *       <li>If an OUT is NOT given and 
             *          then the ALT blocks without deadlock operations may be reduced to one CombinedNode
             *       </ul>
             */        
            if (caseNode.getOutNode() != null && !caseNode.getOutNode().isSet(flag_is_reduceable) ) {
                // combine all reducible ALTs into 1 combined node
                CombinedNode combinedNode=null;

                boolean firstAltFound = false;
                for (int i=1; i<caseNode.m_alternatives.size(); i++) {
                    if (caseNode.m_alternatives.get(i) != null) {
                        if (caseNode.m_alternatives.get(i).isSet(flag_is_reduceable)) {
                            for (ControlFlowGraphNode c = caseNode.m_alternatives.get(i); !c.equals(caseNode.getFinNode()); c = c.getNext()) {
                                c.setFlag(flag_remove_me);
                            }
                            if (!firstAltFound) {
                                firstAltFound = true;
                                combinedNode = new CombinedNode(caseNode.m_alternatives.get(i).getCtx());
                                cfg.addNode(combinedNode);
                                combinedNode.setNext(caseNode.getFinNode());
                                caseNode.m_alternatives.get(i).clrFlag(flag_remove_me);
                                caseNode.m_alternatives.get(i).setNext(combinedNode);
                                combinedNode.setFlag(flag_is_reduceable);
                            } else {
                                combinedNode.add(caseNode.m_alternatives.get(i));
                            }
                        }
                    }
                }

            } else if (caseNode.getOutNode() != null && caseNode.getOutNode().isSet(flag_is_reduceable) ) {
                // reduce OUT and all reducible ALTs into 1 combined node
                CombinedNode combinedNode = new CombinedNode(caseNode.getOutNode().getCtx());
                for (ControlFlowGraphNode c = caseNode.m_alternatives.get(0); !c.equals(caseNode.getFinNode()); c = c.getNext()) {
                    c.setFlag(flag_remove_me);
                }

                // start loop with index 1 to skip OUT, which is treated in the CTOR above!
                for (int i=1; i<caseNode.m_alternatives.size(); i++) {
                    if (caseNode.m_alternatives.get(i) != null) {
                        if (caseNode.m_alternatives.get(i).isSet(flag_is_reduceable)) {
                            combinedNode.add(caseNode.m_alternatives.get(i));
                            for (ControlFlowGraphNode c = caseNode.m_alternatives.get(i); !c.equals(caseNode.getFinNode()); c = c.getNext()) {
                                c.setFlag(flag_remove_me);
                            }
                        }
                    }
                }

                combinedNode.setNext(caseNode.getFinNode());
                combinedNode.setFlag(flag_is_reduceable);
                cfg.addNode(combinedNode);
                caseNode.setOutNode(combinedNode);
            } else if (caseNode.getOutNode() == null) {
                // reduce all reducible ALTs into nothing 
                boolean firstAltFound = false;
                for (int i=1; i<caseNode.m_alternatives.size(); i++) {
                    if (caseNode.m_alternatives.get(i) != null) {
                        if (caseNode.m_alternatives.get(i).isSet(flag_is_reduceable)) {
                            for (ControlFlowGraphNode c = caseNode.m_alternatives.get(i); !c.equals(caseNode.getFinNode()); c = c.getNext()) {
                                c.setFlag(flag_remove_me);
                            }
                            if (!firstAltFound) {
                                firstAltFound = true;
                                caseNode.m_alternatives.get(i).clrFlag(flag_remove_me);
                                caseNode.m_alternatives.get(i).setNext(caseNode.getFinNode());
                            }
                        }
                    }
                }
            }
        }

    }

    /*
     * considerations about reduction of loop statements
     * 
     * a) we may have procedure calls in FOR
     * b) we may have procedure calls and TRY in WHILE
     * c) we may have procedure calls and Deadlock Operations in loop body
     * 
     *  NOTE: if body must remain, at least FOR or WHILE must remain to enshure the structure
     *          if while has function calls or TRY we must keep the FOR-node to execute
     *          the function call/TRY in each iteration
     *  
     *   a      b       c       Action
     * true     false   false   keep for, remove WHILE-node, reduce body
     * true     true    false   keep for, keep WHILE, reduce body
     * false    false   false   remove complete loop
     * false    true    false   keep FOR, keep WHILE, reduce body
     * true     false   true    keep FOR, remove WHILE-node, keep body
     * true     true    true    keep FOR, keep WHILE, keep body
     * false    false   true    keep FOR, remove WHILE-node, keep body
     * false    true    true    keep FOR, keep WHILE, keep body
     * missing  false   false   remove complete loop
     * missing  true    false   keep WHILE-node, reduce body
     * missing  false   true    keep complete loop
     * missing  true    true    keep complete loop 
     * true     missing true    keep complete loop
     * true     missing false   remove complete loop; keep function calls of FOR 
     * false    missing true    keep complete loop
     * false    missing false   remove complete loop  
     * 
     *          
     */
    private void reduceRepeatStatement(ControlFlowGraph cfg, PseudoNode repeatBegin) {
        
        // gather information
        boolean whileHasFunctionCallsOrTry = false;
        ControlFlowGraphNode forNode = null;
        ControlFlowGraphNode whileNode = null;
        ControlFlowGraphNode functionCallsInNextStatement = null;
        ControlFlowGraphNode functionCallsInForStatement = null;
        ControlFlowGraphNode functionCallsInWhileStatement = null;

        // search begin of loop body, there may be some function calls in FOR and WHILE
        // as well as FOR and/or WHILE-nodes, or none of them
        ControlFlowGraphNode bodyBegin = repeatBegin.getNext();

        // lets see what is present
        // + for given/with/without function calls
        // + while given/with/without function calls
        // + body with/without deadlock relevant operations 
        while (!(bodyBegin instanceof PseudoNode && 
                ((PseudoNode)bodyBegin).getNodeType() == PseudoNode.repeatBodyBegin)) {
            if (bodyBegin instanceof FunctionCallsInNextStatement) {
                functionCallsInNextStatement = bodyBegin;
            }
            if (bodyBegin instanceof RepeatNode) {
            }
            if (bodyBegin instanceof RepeatNode && ((RepeatNode)bodyBegin).getRepeatType() == RepeatType.IsFor) {
                forNode = bodyBegin;  // FOR node found

                if (functionCallsInNextStatement != null) {
                    functionCallsInForStatement = functionCallsInNextStatement;
                    functionCallsInNextStatement = null;
                } 
            } 
            if (bodyBegin instanceof RepeatNode && ((RepeatNode)bodyBegin).getRepeatType() == RepeatType.IsWhile) {
                whileNode = bodyBegin;  // WHILE node found
                if (functionCallsInNextStatement != null) {
                    functionCallsInWhileStatement = functionCallsInNextStatement;
                    whileHasFunctionCallsOrTry = true;
                    functionCallsInNextStatement = null;
                } 
            }

            bodyBegin = bodyBegin.getNext();
        }

        // check for complete loop remove
        boolean reductionComplete = false;
        if (whileHasFunctionCallsOrTry == false && allReducable(bodyBegin)) {
           CombinedNode loopNode = reduceLoop(cfg,repeatBegin, forNode, whileNode);
           if (functionCallsInForStatement != null) {
               // if we have function call in FOR, they were marked to remove in reduceLoop()
               // we must keep them as only remaining element of the loop  
               functionCallsInForStatement.clrFlag(flag_remove_me);
               functionCallsInForStatement.setNext(loopNode);
               getPredecessor(cfg, repeatBegin).setNext(functionCallsInForStatement);
           } else {
               getPredecessor(cfg, repeatBegin).setNext(loopNode);
           }
           reductionComplete = true;
        }
        
        // remove WHILE, if FOR is present
        if ( (!reductionComplete) && forNode != null && whileNode != null && whileHasFunctionCallsOrTry == false) {
            whileNode.setFlag(flag_remove_me);
            
            if (functionCallsInWhileStatement != null) {
                functionCallsInWhileStatement.setNext(((RepeatNode)whileNode).getNext());
            } else {
                forNode.setNext(((RepeatNode)whileNode).getNext());
            }
        }
   
        // check for reduce body
        if ( (!reductionComplete) && allReducable(bodyBegin)) {
            CombinedNode loopBody = reduceBody(cfg,bodyBegin);
            // reduce body sets the next of the result node to the next statement of BodyEnd
            getPredecessor(cfg, bodyBegin).setNext(loopBody);
        }

 
    }

    private CombinedNode reduceBody(ControlFlowGraph cfg, ControlFlowGraphNode bodyBegin) {
        CombinedNode combindBody = new CombinedNode(bodyBegin.getCtx());

        bodyBegin.setFlag(flag_is_reduceable);
        ControlFlowGraphNode n; // not defined in for-loop to access 'n' afterwards

        for (n = bodyBegin;
                !(n instanceof PseudoNode && 
                        ((PseudoNode)n).getNodeType() == PseudoNode.repeatBodyEnd);

                n=n.getNext()) {
            n.setFlag(flag_remove_me);
        }
        //n.setFlag(flag_remove_me);
        combindBody.setNext(n);
        bodyBegin.setNext(combindBody);

        combindBody.setFlag(flag_is_reduceable);
        cfg.addNode(combindBody);
        return combindBody;
    }

    private CombinedNode reduceLoop(ControlFlowGraph cfg, PseudoNode repeatBegin, 
            ControlFlowGraphNode forNode, ControlFlowGraphNode whileNode) {
        CombinedNode combinedLoop = new CombinedNode(repeatBegin.getCtx());
        cfg.addNode(combinedLoop);
       // getPredecessor(cfg, repeatBegin).setNext(combinedLoop);
        combinedLoop.setFlag(flag_is_reduceable);
        if (forNode != null) {
            ((RepeatNode)forNode).setFlag(flag_remove_me);
            ((RepeatNode)forNode).getEndNode().setFlag(flag_remove_me);
            combinedLoop.setNext(((RepeatNode)forNode).getEndNode().getNext());
        } else if (whileNode != null) {
            ((RepeatNode)whileNode).setFlag(flag_remove_me);
            ((RepeatNode)whileNode).getEndNode().setFlag(flag_remove_me);
            combinedLoop.setNext(((RepeatNode)whileNode).getEndNode().getNext());
        } else {
            // endless loop
            combinedLoop.setNext(null);
        }

        // remove nodes of the loop
        ControlFlowGraphNode n;  // I need the index afterwards
        for ( n= repeatBegin; 
                !(n instanceof PseudoNode  && ((PseudoNode)n).getNodeType()==PseudoNode.repeatBodyEnd);
                n=n.getNext()) {
            n.setFlag(flag_remove_me);
        }
        // remove reapeatBodyEnd
        n.setFlag(flag_remove_me);
        repeatBegin.setFlag(flag_remove_me);

        return combinedLoop;
    }

    private ControlFlowGraphNode getPredecessor(ControlFlowGraph cfg, ControlFlowGraphNode n) {
        Vector<ControlFlowGraphNode> previousNodes = getPredecessors(cfg, n);
        if (previousNodes.size() != 1) {
            ErrorStack.addInternal(n.getCtx(),"Export4StaticDeadlockDetection@564", 
                    "exactly one predecessor node expected -- got "+
                            previousNodes.size());
            return n;
        }
        return previousNodes.get(0);
    }

    // getPredecessors is since 2024-07-09 a method og ControlFlowGraph
    private Vector<ControlFlowGraphNode> getPredecessors(ControlFlowGraph cfg, ControlFlowGraphNode n) {
        Vector<ControlFlowGraphNode> previousNodes = new Vector<ControlFlowGraphNode>() ;
        for (int i=0; i<cfg.getNodeList().size(); i++) {
            //            System.out.println("aa: "+cfg.getNodeList().get(i).getCtx().getStart().getLine()+":"
            //        + cfg.getNodeList().get(i).printCtx(10));
            for (ControlFlowGraphNode node : cfg.getNodeList().get(i).m_alternatives) {

                if (node != null && node.equals(n)) {
                    previousNodes.add(cfg.getNodeList().get(i)); 
                    //                    System.out.println("xx: "+ cfg.getNodeList().get(i).printCtx(10));
                }
            }
        }
        return previousNodes;
    }

    /*
     * check if all nodes in a block (if/case/repeat/begin) are reducible
     */
    private boolean allReducable(ControlFlowGraphNode n) {

        if (n == null) return true;

        do {
            n.setFlag(flag_node_reached);
            //System.out.println("testing..."+n.printCtx(maxLengthOfStatement));
            if (isDeadlockRelevant(n)) {
                return false;
            } else if (disruptSequentialExecution(n)) {
                return false;
            } else {    
                n = n.getNext();
            }
            if (n instanceof PseudoNode && ((PseudoNode)n).getNodeType() == PseudoNode.ifFin) {
                return true;
            }
            if (n instanceof PseudoNode && ((PseudoNode)n).getNodeType() == PseudoNode.caseFin) {
                return true;
            }
            if (n instanceof PseudoNode && 
                    ((PseudoNode)n).getNodeType() == PseudoNode.repeatBodyEnd)  {
                return true;
            }

        } while (n != null);
        return false;
    }

    /*
     * deadlock relevant operations are
     * - function calls
     * - realtime statements like TERMINATE, ACTIVATE, and all SEMA/BOLT opeations
     * - maybe TRIGGER and WHEN are also relevant
     * - in the first approach also sequential control is relevant
     */

    private boolean isDeadlockRelevant(ControlFlowGraphNode n) {
        /* grammar: 
        statement:
            label_statement* ( 
                unlabeled_statement | 
                block_statement |                       done
                cpp_inline )  ;


    unlabeled_statement:
          empty_statement                           no job for the cfg
        | realtime_statement                        done
        | interrupt_statement                       done
        | assignment_statement                      done
        | sequential_control_statement              done
        | io_statement                              done
        | callStatement                             done
        | returnStatement
        | gotoStatement
        | loopStatement                             done
        | exitStatement                             done
        | convertStatement                          done
        ;
    sequential_control_statement:
          if_statement                              done
        | case_statement                            done
        ; 
         */

        if (n instanceof SequentialNode) {
            ParserRuleContext ctx = n.getCtx();
            if (ctx instanceof Realtime_statementContext) {
                Realtime_statementContext c = (Realtime_statementContext)ctx;
                if (c.task_control_statement()!= null) {
                    Task_control_statementContext tc = c.task_control_statement();
                    if (tc.taskStart()!= null || tc.task_terminating()!= null) {
                        return true;
                    } 
                } else if (c.task_coordination_statement() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean disruptSequentialExecution(ControlFlowGraphNode n) {
        if (n instanceof SequentialNode) {
            ParserRuleContext ctx = n.getCtx();
            if (ctx instanceof ReturnStatementContext) {
                return true;
            }
            if (ctx instanceof GotoStatementContext) {
                return true;
            }            
            if (ctx instanceof ExitStatementContext) {
                return true;
            }
        }
        if (n instanceof CallNode) {
            return true;
        }
        if (n instanceof FunctionCallsInNextStatement) {
            return true;
        }
        return false;
    }



    private void encodeNode(ControlFlowDataWrapper wrapper, ControlFlowGraphNode n) {
        if (n instanceof  CallNode) {
            wrapper.set("class","ProcedureCall");
            if (n.getCtx() instanceof CallStatementContext) {
                CallStatementContext c = ((CallStatementContext)(n.getCtx()));
                wrapper.set("procedure", c.name().ID().getText());
                wrapper.set("statement",n.printCtx(maxLengthOfStatement));
            } else if (n.getCtx() instanceof NameContext) {
                ASTAttribute attr = m_ast.lookup(n.getCtx());

                // we have a function call in next statement
                wrapper.set("procedure", attr.getSymbolTableEntry().getName());
                //wrapper.set("statement",attr.getSymbolTableEntry().getName());
            }
        } else if (n instanceof  CaseNode) {
            wrapper.set("class", "FlowControl");
            wrapper.set("type","CASE");
            Case_statementContext c = (Case_statementContext)n.getCtx();
            String expr = null;
            if (c.case_statement_selection1() != null) {
                expr = c.case_statement_selection1().expression().getText();
            }
            if (c.case_statement_selection2() != null) {
                expr = c.case_statement_selection2().expression().getText();
            }
            wrapper.set("statement", expr);
        } else if (n instanceof  FunctionCallsInNextStatement) {
            // were replaced by a sequence of CallStatements

        } else if (n instanceof  IfNode) {
            wrapper.set("class","FlowControl");
            wrapper.set("type","IF");
            If_statementContext c = ((If_statementContext)(n.getCtx()));
            wrapper.set("statement", c.expression().getText());
        } else if (n instanceof  LabelNode) {
        } else if (n instanceof  PseudoNode) {
            PseudoNode pn = (PseudoNode) n;
            switch (pn.getNodeType()) {
                case PseudoNode.ifThen:
                    wrapper.set("class","FlowControl");
                    wrapper.set("type","THEN");
                    break;
                case PseudoNode.ifElse:
                    wrapper.set("class","FlowControl");
                    wrapper.set("type","ELSE");
                    break;
                case PseudoNode.ifFin:
                    wrapper.set("class","FlowControl");
                    wrapper.set("type","IF END");
                    break;
                case PseudoNode.caseAlt:
                    wrapper.set("class","FlowControl");
                    wrapper.set("type","ALT");
                    break;
                case PseudoNode.caseOut:
                    wrapper.set("class","FlowControl");
                    wrapper.set("type","OUT START");
                    break;
                case PseudoNode.caseFin:
                    wrapper.set("class","FlowControl");
                    wrapper.set("type","CASE END");
                    break;
                case PseudoNode.procEntry:
                    wrapper.set("label","Entry");
                    break;
                case PseudoNode.taskEntry:
                    TaskDeclarationContext td =(TaskDeclarationContext)(  pn.getCtx());
                    wrapper.set("runAtStartup", td.task_main()!=null);
                    wrapper.set("global",td.globalAttribute()!=null);
                    wrapper.set("class","DeadlockControlFlowGraphTask");
                    wrapper.set("taskIdentifier",td.nameOfModuleTaskProc().getText());
                    break;
                case PseudoNode.procEnd:
                case PseudoNode.taskEnd:
                    wrapper.set("label","End");
                    break;

                case PseudoNode.repeatBegin:
                    wrapper.set("statement","repeatBegin");
                    break;
                case PseudoNode.repeatEnd:
                    wrapper.set("statement","repeatEnd");
                    break;
                case PseudoNode.repeatWhile:
                    wrapper.set("statement","repeatWhile");
                    break;
                case PseudoNode.repeatBodyBegin:
                    wrapper.set("statement","repeatBodyBegin");
                    break;
                case PseudoNode.repeatBodyEnd:
                    wrapper.set("statement","repeatBodyEnd");
                    break;
                case PseudoNode.blockBegin:
                    wrapper.set("statement","blockBegin");
                case PseudoNode.blockEnd:
                    wrapper.set("statement","blockEnd");
                    break;
                case PseudoNode.cppInline:
                    wrapper.set("statement", "__cpp__");
                    break;
                default:
                    ErrorStack.addInternal(pn.getCtx(),"Export4StaticDeadlockDetection:190","missing alternative");
                    break;
            }
        } else if (n instanceof  RepeatNode) {
            wrapper.set("class","FlowControl");
            wrapper.set("type","LOOP BEGIN");
            wrapper.set("statement", n.printCtx(maxLengthOfStatement));
        } else if (n instanceof  SequentialNode) {
            if (n.isSet(flag_is_deadlock_relevant)) {
                wrapper.set("statement", n.printCtx(maxLengthOfStatement));
                addDeadlockOperationDetails(wrapper, n);
            } else {
                wrapper.set("class","Unknown");
                wrapper.set("statement", n.printCtx(maxLengthOfStatement)); 
            }
        } else if (n instanceof  CombinedNode) {
            wrapper.set("class","Unknown");
            wrapper.set("statement", n.printCtx(maxLengthOfStatement)); 
        } else {
            ErrorStack.addInternal(n.getCtx(),"Export4StaticDeadlockDetection:136","missing alternative");
        }
    }

    private void addDeadlockOperationDetails(ControlFlowDataWrapper wrapper,
            ControlFlowGraphNode n) {
        ParserRuleContext ctx = n.getCtx();
        if (ctx instanceof Realtime_statementContext) {
            Realtime_statementContext c = (Realtime_statementContext)ctx;
            if (c.task_control_statement()!= null) {
                Task_control_statementContext tc = c.task_control_statement();
                if (tc.taskStart()!= null ) {
                    wrapper.set("class","TaskControlActivate");
                    wrapper.set("taskIdentifier",tc.taskStart().name().getText());                    
                } else if (tc.task_terminating()!= null) {
                    wrapper.set("class","FlowControl");
                    wrapper.set("type","TERMINATE");
                    if (tc.task_terminating().name() != null) {
                        wrapper.set("statement", tc.task_terminating().name().getText());
                    } else {
                        wrapper.set("statement","<self>");  
                    }
                }                    

            } else if (c.task_coordination_statement() != null) {
                wrapper.set("class","DeadlockOperation");
                ListOfNamesContext listOfNames = null;
                if (c.task_coordination_statement().semaRequest()!= null) {
                    wrapper.set("resourcesType","SEMA");
                    wrapper.set("actionType", "REQUEST");
                    listOfNames = c.task_coordination_statement().semaRequest().listOfNames();
                } else if (c.task_coordination_statement().semaRelease()!= null ) {
                    wrapper.set("resourcesType","SEMA");
                    wrapper.set("actionType", "RELEASE");
                    listOfNames = c.task_coordination_statement().semaRelease().listOfNames();
                } else if (c.task_coordination_statement().boltEnter()!= null ) {
                    wrapper.set("resourcesType","BOLT");
                    wrapper.set("actionType", "ENTER"); 
                    listOfNames = c.task_coordination_statement().boltEnter().listOfNames();
                } else if (c.task_coordination_statement().boltLeave()!= null ) {
                    wrapper.set("resourcesType","BOLT");
                    wrapper.set("actionType", "LEAVE"); 
                    listOfNames = c.task_coordination_statement().boltLeave().listOfNames();
                } else if (c.task_coordination_statement().boltReserve()!= null ) {
                    wrapper.set("resourcesType","BOLT");
                    wrapper.set("actionType", "RESERVE"); 
                    listOfNames = c.task_coordination_statement().boltReserve().listOfNames();
                } else if (c.task_coordination_statement().boltFree()!= null ) {
                    wrapper.set("resourcesType","BOLT");
                    wrapper.set("actionType", "FREE"); 
                    listOfNames = c.task_coordination_statement().boltFree().listOfNames();
                }
                if (listOfNames != null) {
                    String names = "";
                    for (int i=0; i<listOfNames.name().size(); i++ ) {
                        if (names != "") { 
                            names +=",";
                        }
                        names += listOfNames.name(i).getText();
                    }

                    String arrayOfNames[] = names.split(",");
                    wrapper.set("resourceIdentifiers",arrayOfNames);
                }

            }
        }

    }

    private void addDeadlockResources(FileWriter writer, boolean sddPossible) {
        int nodeNumber=0;
        try {
            writer.write("   subgraph cluster"+nextClusterNumber +"{\n");
            nextClusterNumber++;
            writer.write("       label = \"Module: "+"mod\"\n");
            writer.write("       edge[style=\"invis\"]\n");

            {
                ControlFlowDataWrapper wrapper= new ControlFlowDataWrapper();
                wrapper.set("sddPossible", sddPossible);
                wrapper.set("codeFilename", Options.getOriginalSourceFilename()+".prl");
                writer.write("        node" + (nodeNumber++) + " [ label=\"" + encode4Dot(wrapper.toJson()) + "\" ]\n");
            }

            LinkedList<VariableEntry> semas =  m_symSymbolTableOfModule.getSemaphoreDeclarations();
            for (int i=0;  i<semas.size(); i++) {
                ControlFlowDataWrapper wrapper= new ControlFlowDataWrapper();
                VariableEntry se = semas.get(i);
                // export only declarations
                if (se.isSpecified()) continue;

                long init=0;  // sema without preset is locked
                if (se.getInitializer() != null) {
                    init = ((ConstantFixedValue)(((SimpleInitializer)se.getInitializer()).getConstant())).getValue();
                }
                wrapper.set("class", "DeadlockResourceDeclaration")
                .set("codeFilename", getSourceFile(se.getCtx()))
                .set("codeLineNumber", getSourceLine(se.getCtx()))
                .set("resourceType", "SEMA")
                .set("resourceIdentifier", se.getName())
                .set("presetValue", init)
                .set("global", se.getGlobalAttribute()!=null?true:false);
                writer.write("        node" + (nodeNumber++) + " [ label=\"" + encode4Dot( wrapper.toJson() ) + "\" ]\n");
                //writer.write("        "+getUniqueNodename(se.getCtx()) + " [ label=\"" + encode4Dot( wrapper.toJson() ) + "\" ]\n");
            }

            writer.write("\n");

            LinkedList<VariableEntry> bolts = m_symSymbolTableOfModule.getBoltDeclarations();
            for (int i=0;  i<bolts.size(); i++) {
                ControlFlowDataWrapper wrapper= new ControlFlowDataWrapper();
                VariableEntry se = bolts.get(i);
                // export only declarations
                if (se.isSpecified()) continue;

                wrapper.set("class", "DeadlockResourceDeclaration")
                .set("codeFilename", getSourceFile(se.getCtx()))
                .set("codeLineNumber", getSourceLine(se.getCtx()))
                .set("resourceType", "BOLT")
                .set("resourceIdentifier", se.getName())
                .set("global", se.getGlobalAttribute()!=null?true:false);
                writer.write("        node" + (nodeNumber++) + " [ label=\"" + encode4Dot( wrapper.toJson() ) + "\" ]\n");
                // writer.write("        "+getUniqueNodename(se.getCtx()) + " [ label=\"" + encode4Dot( wrapper.toJson() ) + "\" ]\n");
            }
            writer.write("\n");
            for(int i=0; i< procedureMap.size(); i++) {
                ProcInfo pi = procedureMap.get(i);
                if (pi.pe.isSpecified()) continue;

                ControlFlowDataWrapper wrapper= new ControlFlowDataWrapper();

                if (pi.reducedCfg != null) {          
                    wrapper.set("class", "DeadlockControlFlowGraphProcedure")
                    .set("codeFilename", getSourceFile(pi.pe.getCtx()))
                    .set("codeLineNumber", getSourceLine(pi.pe.getCtx()))
                    .set("procedureIdentifier", pi.pe.getName())
                    .set("global", pi.pe.getGlobalAttribute()!=null?true:false);
                    if (pi.pe.getGlobalAttribute() != null && pi.reducedCfg.getNodeList().size() == 2) {
                        wrapper.set("noDeadlockOperation",true);
                    }
                    writer.write("        node" + (nodeNumber++) + " [ label=\"" + encode4Dot(wrapper.toJson()) + "\" ]\n");
                }
            }

            // link nodes for vertical alignment in dot (invisible)
            writer.write("\n        // dummy edges for vertical alignment\n");
            for (int i=0; i<nodeNumber-1; i++) {
                writer.write("        node"+i+"->node"+(i+1)+"\n");
            }
            writer.write("   }\n");
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }



    private String getSourceFile(ParserRuleContext ctx) {
        SourceLocation sl = SourceLocations.getSourceLoc(ctx.start.getLine());
        return sl.filename();
    }

    private int getSourceLine(ParserRuleContext ctx) {
        SourceLocation sl = SourceLocations.getSourceLoc(ctx.start.getLine());
        return sl.getLineNo(ctx.start.getLine());
    }

    private String encode4Dot(String json) {
        String encoded = "";
        for (int i=0; i<json.length(); i++) {
            if (json.charAt(i) == '"' && i>0 && json.charAt(i-1) != '\\') {
                encoded += '\\';
                encoded += '"';
            } else {
                encoded += json.charAt(i);
            }
        }
        return encoded;
    }

    private class ControlFlowDataWrapper
    {
        private final Map<String, Object> values = new HashMap<>();

        public ControlFlowDataWrapper set(String key, Object value)
        {
            values.put(key, value);

            return this;
        }

        public Object getItem(String key) {
            return values.get(key);
        }

        //        public void reset()
        //        {
        //            values.clear();
        //        }


        /*
         * we have only a simple structure of data.
         * + a list of key:value pairs, where the values may have only the types String, String[], Integer, Long Boolean
         * --> it makes no sense to use a full featured json library for the export 
         */
        public String toJson()
        {
            String result = "{";
            Boolean firstElement = true;

            for (Map.Entry<String, Object> entry : values.entrySet())
            {
                String key = escapeValue(entry.getKey());
                if (!firstElement) {
                    result += ",";
                }
                firstElement = false;

                result += '"' + key + '"';

                Object value = entry.getValue();

                if (value instanceof String)
                {
                    value = escapeValue((String) value);
                    result += ":\""+ value + '"';
                } else if (value instanceof String[]) {
                    result += ":[";
                    String strings[] = (String[])(entry.getValue());
                    for (int i=0; i<strings.length; i++) {
                        if (i != 0) {
                            result += ",";
                        }
                        result += '"' + escapeValue(strings[i]) + '"';
                    }
                    result += "]";
                } else if (value instanceof Integer){
                    result += ":"+(Integer)(entry.getValue());
                } else if (value instanceof Long){
                    result += ":"+(Long)(entry.getValue());
                } else if (value instanceof Boolean) {
                    result += ":"+(Boolean)(entry.getValue() );
                } else {
                    System.err.println("missing alternative: Export4StaticDeadlockDetection@1122");
                }
            }
            result += '}';
            return result;    
        }

        private  String escapeValue(String value) {
            String result = "";
            for (int i=0; i<value.length(); i++) {
                switch (value.charAt(i)) {
                    default:
                        result += value.charAt(i);
                        break;
                    case '"':
                        result += "\\\"";
                        break;
                    case '\\': 
                        result += "\\\\";
                        break;
                }
            }
            return result;
        }

    }   

}
