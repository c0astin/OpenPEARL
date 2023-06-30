package org.openpearl.compiler.SemanticAnalysis.PlainControlFlowGraph;


import java.util.Vector;
import org.openpearl.compiler.ContextUtilities;
import org.antlr.v4.runtime.ParserRuleContext;


/**
 * base class for cfg nodes
 */
public class ControlFlowGraphNode {
    /**
     * flags for semantic checks, like statement is reachable via mark and sweep
     */
    private int m_flag; 
    
  /**
   * the context of the statement
   */
  private ParserRuleContext m_ctx; 
  
  /*
   * index 0: next statement
   * index 1...: depends on specialized nodes
   */
  public Vector<ControlFlowGraphNode> m_alternatives = null;
  
  public ControlFlowGraphNode(ParserRuleContext ctx) {
      m_alternatives = new Vector<ControlFlowGraphNode>();
      m_alternatives.add(null);  // create index 0
      m_ctx=ctx; 
      m_flag=0;  
  }
  
  public ControlFlowGraphNode(ControlFlowGraphNode other) {
      m_alternatives = new Vector<ControlFlowGraphNode>();
      m_alternatives.add(null);  // create index 0
      m_ctx=other.getCtx(); 
      m_flag=0; 
  }
  
  public ParserRuleContext getCtx( ) {
      return m_ctx;
  }

  public void setCtx( ParserRuleContext ctx) {
      m_ctx = ctx;
  }

  public void setNext(ControlFlowGraphNode next) {
      m_alternatives.set(0, next);
      
  }
 
  
  public ControlFlowGraphNode getNext() {
      return m_alternatives.get(0);
      
  }
  // get a readable version of a context entry
  // stop at \n
  // add ... if string is longer than length
  public String printCtx(int length) {
      return ContextUtilities.printCtx(m_ctx,length);
//      String readable = null;
//      int a = m_ctx.start.getStartIndex();
//      int b = m_ctx.stop.getStopIndex();
//      Interval i = new Interval(a, b);
//      readable = m_ctx.start.getInputStream().getText(i);
//      
//      // clip at \n
//      int newline = readable.indexOf('\n');
//      if (newline > 0) {
//          readable = readable.substring(0, newline-1);
//      }
//      
//      if (readable.length() > length) {
//         readable = readable.substring(0, length) + "...";
//      }
//              
//      return readable;
  }
  
  
 
  
  public void resetFlags() {
      m_flag = 0;
  }
  
  public void setFlag(int flag) {
      m_flag |= flag;
  }
  
  public void clrFlag(int flag) {
      m_flag &= ~ flag;
  }
  
  public boolean isSet(int flag) {
      return ( (m_flag & flag) != 0);
  }
}
