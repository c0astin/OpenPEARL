package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;


import java.util.Vector;
import java.io.FileWriter;
import java.io.IOException;
import org.openpearl.compiler.ErrorStack;
import org.openpearl.compiler.Options;
/**
 * Control Flow Graph (CFG)
 * 
 * <ul>
 * <li> a CFG becomes created for each TASK and PROC
 * <li> it consists of node for each executable statement
 * <li> variable declarations and specifications are omitted
 * <li> PseudoNodes are used for PROC/TASK/BLOCK entry and end
 * <li> all statements with sequential execution characteristic are treated as SequentialNode
 * <li> IF, CASE, REPEAT statements are treated by IfNode, CaseNode and RepeatNode 
 * <li> each Node consists of a reference to the parser rule context and a flag storage for usage by semantic runs
 * <li> the cfg becomes stored in the ProcedureEntry or TaskEntry in the symbol table   
 * </ul> 
 */
public class ControlFlowGraph {
   private ControlFlowGraphNode m_entry;
   private Vector<ControlFlowGraphNode> m_nodes;
   private static final int lengthOfContextString=20; 
   
   public ControlFlowGraph(ControlFlowGraphNode entry) {
       m_entry=entry;
       m_nodes=new Vector<ControlFlowGraphNode>();
   }
   
   public ControlFlowGraph(ControlFlowGraph other) {
      
       m_nodes = new Vector<ControlFlowGraphNode>();
       for (int i=0; i< other.m_nodes.size();i++) {
           ControlFlowGraphNode n = other.getNodeList().get(i);
           if (n instanceof CallNode) {
               m_nodes.add(new CallNode((CallNode)n));
           } else if (n instanceof CaseNode) {
               m_nodes.add(new CaseNode((CaseNode)n));
           } else if (n instanceof FunctionCallsInNextStatement) {
               m_nodes.add(new FunctionCallsInNextStatement((FunctionCallsInNextStatement)n));
           } else if (n instanceof IfNode) {
               m_nodes.add(new IfNode((IfNode)n));
           } else if (n instanceof LabelNode) {
               m_nodes.add(new LabelNode((LabelNode)n));
           } else if (n instanceof RepeatNode) {
               m_nodes.add(new RepeatNode((RepeatNode)n));
           } else if (n instanceof PseudoNode) {
               m_nodes.add(new PseudoNode((PseudoNode)n));
           } else if (n instanceof SequentialNode) {
                   m_nodes.add(new SequentialNode((SequentialNode)n));
           } else {
               ErrorStack.addInternal("ControlFlowGraph:45: missing alternative");
           }
       }
       
       int index= other.getNodeList().indexOf(other.m_entry);
       m_entry = getNodeList().get(index);
       
       for (int i = 0; i< other.m_nodes.size();i++) {
           for (int j = 0; j< other.m_nodes.get(i).m_alternatives.size(); j++) {
               ControlFlowGraphNode alt = other.m_nodes.get(i).m_alternatives.get(j);
               index = other.getNodeList().indexOf(alt);
               if (index >= 0) {
                  m_nodes.get(i).m_alternatives.set(j, m_nodes.get(index));
               } else {
                   m_nodes.get(i).m_alternatives.set(j, null);
               }
           }
           if (other.m_nodes.elementAt(i) instanceof IfNode) {
               IfNode ifNode = (IfNode)(other.m_nodes.elementAt(i));
               index = other.getNodeList().indexOf(ifNode.getFinNode());
               ((IfNode)(m_nodes.get(i))).setFinNode(m_nodes.get(index));
           }
           if (other.m_nodes.elementAt(i) instanceof CaseNode) {
               CaseNode caseNode = (CaseNode)(other.m_nodes.elementAt(i));
               index = other.getNodeList().indexOf(caseNode.getFinNode());
               ((CaseNode)(m_nodes.get(i))).setFinNode(m_nodes.get(index));
           }
       }
   }
   
   public void clearAllFlagsInGraph() {
       for (ControlFlowGraphNode cfgn: m_nodes) {
           cfgn.resetFlags();
       }
   }
   
   public ControlFlowGraphNode getFirstEntry() {
       return m_nodes.elementAt(0);
   }
   
   public void addNode (ControlFlowGraphNode newNode) {
       m_nodes.add(newNode);
   }
   
   public Vector<ControlFlowGraphNode> getNodeList() {
       return m_nodes;
   }

   public void output(String nameOfProcOrTask) {
       String outputFileName = Options.getInputFiles().get(0); 
       outputFileName = outputFileName.substring(0, outputFileName.length()-4);
       outputFileName += "_cfg_"+nameOfProcOrTask+".dot";

       if (Options.getVerbose() > 0) {
           System.out.println("Generating ControlFlowGraph file " + outputFileName);
       }
       
       try
       {
           FileWriter writer = new FileWriter(outputFileName);
           writer.write("digraph G {\n");
           writer.write("   subgraph procOrTask {\n");
           writer.write("      label=\""+nameOfProcOrTask+"\"\n");
           for (int i=0; i<m_nodes.size(); i++) {
               String color = "";
               if (m_nodes.get(i).isSet(2)) {
                   color=" style=\"filled\", fillcolor=\"yellow\",";
               }
               if (m_nodes.get(i) instanceof PseudoNode) {
                   writer.write("      node"+i+" [shape=ellipse,"+color+" label=\""+m_nodes.get(i).printCtx(lengthOfContextString)+"\" ]\n");
               } else {
                  writer.write("      node"+i+" [shape=box,"+color+" label=\""+m_nodes.get(i).printCtx(lengthOfContextString)+"\" ]\n");
               }
           }

           // set edges
           //for (ControlFlowGraphNode node = m_entry; node !=null; node = node.m_next) {
           for (int i=0; i<m_nodes.size(); i++) {
               ControlFlowGraphNode node = m_nodes.get(i);
               int startIndex = m_nodes.indexOf(node);
               int endIndex = -1;

               if (node.m_alternatives != null) {
                   for (int j=0; j< node.m_alternatives.size(); j++) {
                       endIndex = m_nodes.indexOf(node.m_alternatives.get(j));
                       if (endIndex>=0) {
                           writer.write("      node"+startIndex+ "-> node"+endIndex+"\n");
                       }

                   }
               }
           }

           writer.write("   }\n");
           writer.write("}\n");

           writer.close();
       } catch (IOException e) {
           System.err.println("could not create file");
       }
   }
}
