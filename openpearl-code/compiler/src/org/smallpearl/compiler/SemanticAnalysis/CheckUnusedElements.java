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
 * Checks:
 * <ul>
 * <li>if all labels are used
 * </ul>
 *
 * Principle of operation:
 * <ul>
 * <li>unused labels
 * <ol>
 * <li> the CheckGotoExit increments for each GOTO a useCount attribute 
 *      of the symbol table element of the label
 * <li>the we find a lable in this check with a useCount of 0, this label is never used
 * <li>emit a warning in this case
 * </ol>     
 * </li>
 * </ul>
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
public class CheckUnusedElements extends SmallPearlBaseVisitor<Void>
        implements SmallPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private ExpressionTypeVisitor m_expressionTypeVisitor;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    
    public CheckUnusedElements(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_debug = debug;
        m_verbose = verbose;
        m_sourceFileName = sourceFileName;
        m_symbolTableVisitor = symbolTableVisitor;
        m_expressionTypeVisitor = expressionTypeVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        Log.debug("    Check GOTO and EXIT");
    }

    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Case: visitModule");
        }

        org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Case: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Case: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Case: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Case: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    public Void visitLabel_statement(SmallPearlParser.Label_statementContext ctx) {
        String id = ctx.ID().getText();

        LabelEntry le = (LabelEntry) (m_currentSymbolTable.lookup(id));
        if (!le.isUsed()) {
            ErrorStack.warn(ctx, "label '" + id + "'", "is never used");
        }

        return null;
    }
}
