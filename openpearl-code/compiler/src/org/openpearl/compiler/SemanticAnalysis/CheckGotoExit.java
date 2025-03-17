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

package org.openpearl.compiler.SemanticAnalysis;

import org.antlr.v4.runtime.RuleContext;
import org.openpearl.compiler.*;
import org.openpearl.compiler.SymbolTable.BlockEntry;
import org.openpearl.compiler.SymbolTable.LabelEntry;
import org.openpearl.compiler.SymbolTable.LoopEntry;
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;

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
 * <li>
 *  <ul>
 *  <li>each block and loop statement is represented by an symbol table entry
 *  <li>the symbol table entry is checked for the presence of an 'name'
 *  <li>if no 'name' is specified, we creat a new 'autoLabelXX' and set an
 *      ASTAttribute for the context of the block or loop with the 
 *      flag isInternal
 *  <li>the CppCodeGenerator may access the symboltable entry for the name
 *      and the ASTAttribute to enable/disable the 'user_label'-decoration       
 *  </ul>
 * </li>
 * <li> ascend the AST until we reach PROC or TASK
 *   <ul>
 *   <li>if we found nether loop nor block, the EXIT is misplaced
 *   <li>if an ID is specified, we must find a loop or block with the same name
 *   <li>when we found a fitting block or loop we obtain the corresponding
 *       symbol table entry via a lookup function with the loop or block context
 *   </ul>
 *  </ol>
 *
 */
public class CheckGotoExit extends OpenPearlBaseVisitor<Void> implements OpenPearlVisitor<Void> {

    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private AST m_ast = null;
    private int m_autoLabelNumber;

    public CheckGotoExit(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_debug = debug;
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;
        m_autoLabelNumber = 0;

        Log.debug("    Check GOTO and EXIT");
    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Goto/Exit: visitModule");
        }

        org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Goto/Exit: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Goto/Exit: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Goto/Exit: visitBlock_statement");
        }
        BlockEntry be = (BlockEntry) (m_currentSymbolTable.lookupLoopBlock(ctx));
        ASTAttribute attr = m_ast.lookup(ctx);
        if (be.getName() == null) {
            be.setName(nextAutoBlockOrLoopName());
            if (attr == null) {
                attr = new ASTAttribute(null);
                m_ast.put(ctx, attr);
            }
            attr.setIsInternal(true);
        }
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    private String nextAutoBlockOrLoopName() {
        String autoName = "automaticLabel" + m_autoLabelNumber;
        m_autoLabelNumber++;
        return autoName;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Goto/Exit: visitLoopStatement");
        }
        LoopEntry le = (LoopEntry) (m_currentSymbolTable.lookupLoopBlock(ctx));
        ASTAttribute attr = m_ast.lookup(ctx);
        if (le.getName() == null) {
            le.setName(nextAutoBlockOrLoopName());
            if (attr == null) {
                attr = new ASTAttribute(null);
                m_ast.put(ctx, attr);
            }
            attr.setIsInternal(true);
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }


    @Override
    public Void visitExitStatement(OpenPearlParser.ExitStatementContext ctx) {
        String name = null;
        SymbolTableEntry se = null;
        if (ctx.ID() != null) {
            name = ctx.ID().getText();
            //     System.out.println("EXIT "+name);
        } else {
            //      System.out.println("EXIT *anon*");
        }

        if (!(name != null)) {
            // exit after first loop or block
            RuleContext c = ctx;
            while (c != null && se == null) {
                c = c.parent;
                if (c instanceof OpenPearlParser.LoopStatementContext) {
                    OpenPearlParser.LoopStatementContext lctx = (OpenPearlParser.LoopStatementContext) c;
                    se = m_currentSymbolTable.lookupLoopBlock(lctx);
                }
                if (c instanceof OpenPearlParser.Block_statementContext) {
                    OpenPearlParser.Block_statementContext bctx = (OpenPearlParser.Block_statementContext) c;
                    se = m_currentSymbolTable.lookupLoopBlock(bctx);
                }
            }
        } else {
            // we must verify the correct loop/block via the ID
            // let's walk the ctx up until we find the block or loop
            // with the requested name or we read the end of the AST
            RuleContext c = ctx;

            while (c != null && se == null) {
                String id = "";
                c = c.parent;
                if (c instanceof OpenPearlParser.LoopStatementContext) {
                    OpenPearlParser.LoopStatementContext lctx = (OpenPearlParser.LoopStatementContext) c;
                    if (lctx.loopStatement_end().ID() != null) {
                        id = lctx.loopStatement_end().ID().getText();
                    }
                    if (id.equals(name)) {
                        se = m_currentSymbolTable.lookupLoopBlock(lctx);
                    }
                }
                if (c instanceof OpenPearlParser.Block_statementContext) {
                    OpenPearlParser.Block_statementContext bctx = (OpenPearlParser.Block_statementContext) c;
                    if (bctx.blockId() != null) {
                        id = bctx.blockId().ID().getText();
                    }

                    if (id.equals(name)) {
                        se = m_currentSymbolTable.lookupLoopBlock(bctx);
                    }
                }
            }

        }

        if (se == null) {
            ErrorStack.add(ctx, "EXIT", "no block or loop with name '" + name + "' found");
        } else {
            // we have a surrounding loop/block found 
            // set an ASTAttribute for the CppCodeGeneratorVisitor
            ASTAttribute attr = new ASTAttribute(null, se);
            m_ast.put(ctx, attr);
            se.setIsUsed(true);
        }
        return null;
    }

    @Override
    public Void visitGotoStatement(OpenPearlParser.GotoStatementContext ctx) {
        String name = ctx.ID().getText();
        SymbolTableEntry se = m_currentSymbolTable.lookup(name);
        if (se == null) {
            ErrorStack.add(ctx, "GOTO", "label '" + name + "' not found");
        } else {
            if (!(se instanceof LabelEntry)) {
                ErrorStack.add(ctx, "GOTO",
                        "'" + name + "' is not a label --- " + se.getClass().getName());
            } else {
                se.setIsUsed(true);
                // add the label entry as to the ASTAttributes.
                // this simplifies the generation of the control flow graph
                ASTAttribute attr = new ASTAttribute(null, se);
                m_ast.put(ctx, attr);
            }
        }
        return null;
    }

}
