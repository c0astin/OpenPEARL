/*
 * [The "BSD license"]
 *  Copyright (c) 2012-2017 Marcel Schaible
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
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.Exception.InternalCompilerErrorException;
import org.smallpearl.compiler.Exception.TypeMismatchException;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;

public class CheckRST extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private ExpressionTypeVisitor m_expressionTypeVisitor;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ModuleEntry m_module;
    private AST m_ast = null;

    public CheckRST(String sourceFileName,
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

        Log.debug( "    Check RST");
    }

    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: visitModule");
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
            System.out.println( "Semantic: Check RST: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitOpen_parameter_rst(SmallPearlParser.Open_parameter_rstContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: visitOpen_parameter_rst");
        }

        CheckPrecision(ctx.ID().getText(), ctx);
        return null;
    }

    @Override
    public Void visitClose_parameter_rst(SmallPearlParser.Close_parameter_rstContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: visitClose_parameter_rst");
        }

        CheckPrecision(ctx.ID().getText(), ctx);
        return null;
    }

    @Override
    public Void visitPositionRST(SmallPearlParser.PositionRSTContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: visitPositionRST");
        }

        CheckPrecision(ctx.ID().getText(), ctx);
        return null;
    }

    @Override
    public Void visitReadRSTPosition(SmallPearlParser.ReadRSTPositionContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: ReadRSTPosition");
        }

        CheckPrecision(ctx.ID().getText(), ctx);
        return null;
    }

    @Override
    public Void visitWriteRSTPosition(SmallPearlParser.WriteRSTPositionContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: WriteRSTPosition");
        }

        CheckPrecision(ctx.ID().getText(), ctx);
        return null;
    }

    @Override
    public Void visitTake_send_rst_s_ctrl_format_RST(SmallPearlParser.Take_send_rst_s_ctrl_format_RSTContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: visitTake_send_rst_s_ctrl_format_RST");
        }

        CheckPrecision(ctx.ID().getText(), ctx);
        return null;
    }

    @Override
    public Void visitPositionConvertRST(SmallPearlParser.PositionConvertRSTContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check RST: visitPositionConvertRST");
        }

        return null;
    }

    private Void CheckPrecision(String id, ParserRuleContext ctx) {

        SymbolTableEntry entry = m_currentSymbolTable.lookup(id);
        VariableEntry var = null;

        if (entry != null && entry instanceof VariableEntry) {
            var = (VariableEntry) entry;

            if ( var.getType() instanceof TypeFixed) {
                TypeFixed type = (TypeFixed) var.getType();
                if (type.getPrecision() < 15) {
                    throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            } else {
                throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }
}