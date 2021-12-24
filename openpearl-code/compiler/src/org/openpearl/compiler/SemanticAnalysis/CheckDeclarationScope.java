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

package org.openpearl.compiler.SemanticAnalysis;

import org.openpearl.compiler.*;
import org.openpearl.compiler.Exception.DeclarationScopeException;
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;

/**
 * @author Marcel Schaible
 * @version 1
 *     <p><b>Description</b>
 */

public class CheckDeclarationScope extends OpenPearlBaseVisitor<Void>
        implements OpenPearlVisitor<Void> {

    private enum Scope {
        UNKNOWN, MODULE, TASK, PROCEDURE, BLOCK, LOOP
    }

    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private ExpressionTypeVisitor m_expressionTypeVisitor;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ModuleEntry m_module;
    private Scope m_currentScope;
    private AST m_ast = null;

    public CheckDeclarationScope(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_debug = debug;
        m_verbose = verbose;
        m_sourceFileName = sourceFileName;
        m_symbolTableVisitor = symbolTableVisitor;
        m_expressionTypeVisitor = expressionTypeVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        m_currentScope = Scope.UNKNOWN;

        Log.debug("    Check Declaration Scope");
    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Declaration Scope: visitModule");
        }

        Scope lastScope = m_currentScope;

        if (m_currentScope != Scope.UNKNOWN) {
            throw new DeclarationScopeException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        m_currentScope = Scope.MODULE;
        visitChildren(ctx);
        m_currentScope = lastScope;
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Declaration Scope: visitProcedureDeclaration");
        }

        Scope lastScope = m_currentScope;

        if (m_currentScope != Scope.MODULE) {
            throw new DeclarationScopeException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        this.m_currentScope = Scope.PROCEDURE;

        visitChildren(ctx);

        this.m_currentScope = lastScope;
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Declaration Scope: visitTaskDeclaration");
        }

        Scope lastScope = m_currentScope;

        if (m_currentScope != Scope.MODULE) {
            throw new DeclarationScopeException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        this.m_currentScope = Scope.TASK;

        visitChildren(ctx);

        this.m_currentScope = lastScope;
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Declaration Scope: visitBlock_statement");
        }

        Scope lastScope = m_currentScope;

        if (m_currentScope != Scope.PROCEDURE && m_currentScope != Scope.TASK
                && m_currentScope != Scope.LOOP && m_currentScope != Scope.BLOCK) {
            throw new DeclarationScopeException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        this.m_currentScope = Scope.BLOCK;

        visitChildren(ctx);

        this.m_currentScope = lastScope;
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Declaration Scope: visitLoopStatement");
        }

        Scope lastScope = m_currentScope;

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        this.m_currentScope = Scope.LOOP;

        visitChildren(ctx);

        this.m_currentScope = lastScope;
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitDationDenotation(OpenPearlParser.DationDenotationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Declaration Scope: visitDationDeclaration");
        }

        Scope lastScope = m_currentScope;

        if (m_currentScope != Scope.MODULE) {
            throw new DeclarationScopeException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        m_currentScope = lastScope;
        return null;
    }

    @Override
    public Void visitLabel_statement(OpenPearlParser.Label_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Declaration Scope: visitLabel_statement");
        }

        Scope lastScope = m_currentScope;

        if (m_currentScope != Scope.PROCEDURE && m_currentScope != Scope.TASK
                && m_currentScope != Scope.LOOP && m_currentScope != Scope.BLOCK) {
            throw new DeclarationScopeException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        m_currentScope = lastScope;
        return null;
    }

    @Override
    public Void visitBoltDenotation(OpenPearlParser.BoltDenotationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Declaration Scope: visitBoltDeclaration");
        }

        Scope lastScope = m_currentScope;

        if (m_currentScope != Scope.MODULE) {
            throw new DeclarationScopeException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        m_currentScope = lastScope;
        return null;
    }

    @Override
    public Void visitSemaDenotation(OpenPearlParser.SemaDenotationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Declaration Scope: visitSemaDeclaration");
        }

        Scope lastScope = m_currentScope;

        if (m_currentScope != Scope.MODULE) {
            throw new DeclarationScopeException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        m_currentScope = lastScope;
        return null;
    }

}
