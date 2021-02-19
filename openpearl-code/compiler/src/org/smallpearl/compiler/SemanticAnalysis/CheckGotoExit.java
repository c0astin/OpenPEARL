/*
 * [A "BSD license"]
 *  Copyright (c) 2021 Rainer Mueller
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

package org.smallpearl.compiler.SemanticAnalysis;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.SmallPearlParser.Block_statementContext;
import org.smallpearl.compiler.SmallPearlParser.LoopStatementContext;
import org.smallpearl.compiler.SmallPearlParser.LoopStatement_byContext;
import org.smallpearl.compiler.SymbolTable.BlockEntry;
import org.smallpearl.compiler.SymbolTable.LabelEntry;
import org.smallpearl.compiler.SymbolTable.LoopEntry;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/**
 * check the GOTO and EXIT statements 
 * 
 * Checks for GOTO:
 * <ul>
 * <li>the label must be defined as a label
 * </ul>
 *
 * Checks for EXIT:
 * <ul>
 * <li>the EXIT must be inside a block or loop
 * <li>the label must be a name of a surrounding block or loop. So we 
 *     run up the context until we find a loop or fitting block
 * </ul>
 * 
 * Principle of operation for GOTO
 * <ol>
 * <li> just lookup the symbol of the label.
 *    <ul>
 *    <li> If it not found we have an error
 *    <li> if the type of symbol is not a label we have an error
 *    </ul>
 * </li>
 * </ol>
 * 
 * Principle of operation for EXIT
 * <ol>
 * <li> ascend the symbol table until we reach PROC or TASK
 *   <ul>
 *   <li>if we found nether loop nor block, the EXIT is misplaced
 *   <li>if an ID is specified, we must find a loop or block with the same name
 *   <li>??? maybe it would be fine to store the adressed ctx as symbol table Entry in an ASTAttribute
 *   </ul>
 *  </ol>
 *
 */
public class CheckGotoExit extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

  private int m_verbose;
  private boolean m_debug;
  private String m_sourceFileName;
  private ExpressionTypeVisitor m_expressionTypeVisitor;
  private SymbolTableVisitor m_symbolTableVisitor;
  private SymbolTable m_symboltable;
  private SymbolTable m_currentSymbolTable;
  private ModuleEntry m_module;

  private AST m_ast = null;

  public CheckGotoExit(String sourceFileName,
      int verbose,
      boolean debug,
      SymbolTableVisitor symbolTableVisitor,
      ExpressionTypeVisitor expressionTypeVisitor,
      AST ast) {

    m_debug = debug;
    m_verbose = verbose;
    m_sourceFileName = sourceFileName;
    m_symbolTableVisitor = symbolTableVisitor;
    m_expressionTypeVisitor = expressionTypeVisitor;
    m_symboltable = symbolTableVisitor.symbolTable;
    m_currentSymbolTable = m_symboltable;
    m_ast = ast;

    Log.debug( "    Check GOTO and EXIT");
  }

  @Override
  public Void visitModule(SmallPearlParser.ModuleContext ctx) {
    if (m_debug) {
      System.out.println( "Semantic: Check Case: visitModule");
    }

    org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry = m_currentSymbolTable.lookupLocal(ctx.ID().getText());
    m_currentSymbolTable = ((ModuleEntry)symbolTableEntry).scope;
    visitChildren(ctx);
    m_currentSymbolTable = m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
    if (m_debug) {
      System.out.println( "Semantic: Check Case: visitProcedureDeclaration");
    }

    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
    if (m_debug) {
      System.out.println( "Semantic: Check Case: visitTaskDeclaration");
    }

    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    m_currentSymbolTable = m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
    if (m_debug) {
      System.out.println( "Semantic: Check Case: visitBlock_statement");
    }

    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
    if (m_debug) {
      System.out.println( "Semantic: Check Case: visitLoopStatement");
    }

    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitExitStatement(SmallPearlParser.ExitStatementContext ctx) {
    SymbolTable st=m_currentSymbolTable;
    String name = null;
    SymbolTableEntry se = null;
    if (ctx.ID()!=null) {
      name = ctx.ID().getText();
 //     System.out.println("EXIT "+name);
    } else {
 //      System.out.println("EXIT *anon*");
    }
    
    if (name == null) {
      // exit after first loop or block
      RuleContext c = ctx;
      while (c!= null && se == null) {        String id="";
        c = c.parent;
        if (c instanceof LoopStatementContext) {
          LoopStatementContext lctx = (LoopStatementContext)c;
          if (lctx.loopStatement_end().ID() != null) {
            id = lctx.loopStatement_end().ID().getText();
          }
          //se=m_currentSymbolTable.lookup(lctx);
        }        
        if (c instanceof Block_statementContext) {
          Block_statementContext bctx = (Block_statementContext)c;
          id = "* anon *";
          if (bctx.ID() != null) {
            id = bctx.ID().getText();
          }
   //       System.out.println("block "+id);
          //se=m_currentSymbolTable.lookup(bctx);
        }
      }
    } else {
      // we must verify the correct loop/block via the ID
      RuleContext c = ctx;

      while (c!= null && se == null) {
        String id="";
        c = c.parent;
        if (c instanceof LoopStatementContext) {
          LoopStatementContext lctx = (LoopStatementContext)c;
          if (lctx.loopStatement_end().ID() != null) {
            id = lctx.loopStatement_end().ID().getText();
          }
          if (id.equals(name)) {
             se = new LoopEntry();
            // System.out.println("loop found: "+id);
             //se=m_currentSymbolTable.lookup(lctx);
          }
        }
        if (c instanceof Block_statementContext) {
          Block_statementContext bctx = (Block_statementContext)c;
          id = "* anon *";
          if (bctx.ID() != null) {
            id = bctx.ID().getText();
          }
          
          if (id.equals(name)) {
            se = new BlockEntry();
           // System.out.println("block found: "+id);
            //se=m_currentSymbolTable.lookup(lctx);
         }
         
        }
      }

    }

    if (se == null) {
      ErrorStack.add(ctx,"EXIT","no block or loop with name '"+name+"' found");
    }
    return null;
  }

  @Override
  public Void visitGotoStatement(SmallPearlParser.GotoStatementContext ctx) {
    String name = ctx.ID().getText(); 
    SymbolTableEntry se = m_currentSymbolTable.lookup(name);
    if (se == null) {
      ErrorStack.add(ctx,"GOTO","label '"+name+"' not found");
    } else {
      if (!(se instanceof LabelEntry)) {
        ErrorStack.add(ctx,"GOTO","'"+name+"' is not a label --- "+se.getClass().getName());
      }
    }
    return null;
  }

}
