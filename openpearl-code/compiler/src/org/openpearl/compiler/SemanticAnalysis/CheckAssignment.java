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
import org.openpearl.compiler.OpenPearlParser.ExpressionContext;
import org.openpearl.compiler.SymbolTable.ProcedureEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;

public class CheckAssignment extends OpenPearlBaseVisitor<Void>
implements OpenPearlVisitor<Void> {

    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private AST m_ast = null;

    public CheckAssignment(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        Log.info("Semantic Check: Check assignment statements");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The type of the variable given to the left of the assignment sign has to match the type of the  value of the
    // expression, with the following exceptions:
    //  (1) The value of a FIXED variable or an integer, resp., may be assigned to a FLOAT variable.
    //  (2) The precision of a numeric variable to the left of an assignment sign may be greater than the precision of
    //      the value of the expression.
    //  (3) A bit or character string, resp., to the left may have a greater length than the value to be assigned; if
    //      needed, the latter is extended by zeros or spaces, resp., on the right.
    //  (4) A variable (no expression) may be assigned to a REF. In this case the type must match exactly
    //  (5) A reference may be assigned to a variable, implicit dereferencing occurs
    //  (6) special for assignments to references: rhs must live longer or equal as lhs
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Void visitAssignment_statement(OpenPearlParser.Assignment_statementContext ctx) {
        Log.debug("CheckAssignment:visitAssignment_statement:ctx" + CommonUtils.printContext(ctx));
        String id = null;
        // System.out.println(ctx.getText());

        ErrorStack.enter(ctx, "assignment");
        ASTAttribute lhsAttr = null;
        TypeDefinition lhsType = null;

        OpenPearlParser.NameContext ctxName = ctx.name();
        lhsAttr = m_ast.lookup(ctx.name());
        ctxName = ctx.name();

        lhsType = lhsAttr.getType(); //  CommonUtils.getBaseTypeForName(ctxName, m_currentSymbolTable);

        id = ctxName.ID().getText();

        if (lhsType == null) {
            ErrorStack.addInternal(id + " not in symbol table or is no variable");
        }

        SymbolTableEntry lhs = m_currentSymbolTable.lookup(id);
        VariableEntry lhsVariable = null;

        if (lhs != null && lhs instanceof VariableEntry) {
            lhsVariable = (VariableEntry) lhs;
        } else {
            ErrorStack.addInternal(id + " not in symbol table or is no variable");
        }

        Log.debug("CheckAssignment:visitAssignment_statement:ctx.expression"
                + CommonUtils.printContext(ctx.expression()));

        if(lhsVariable.getLoopControlVariable()==true) {
            ErrorStack.add("loop variable not allowed on lhs");
            //} else if(lhsVariable.getAssigmentProtection()==true) {
        } else if(lhsType.hasAssignmentProtection()==true) {    
            ErrorStack.add(lhsAttr.getType().toString() + " variable not allowed on lhs");
        }


        if (!(lhsType instanceof TypeStructure || lhsType instanceof TypeReference
                || lhsType instanceof TypeVariableChar || TypeUtilities.isSimpleType(lhsType))) {
            ErrorStack.add(lhsAttr.getType().toString4IMC(true) + " not allowed on lhs");
        }

        if (ErrorStack.getLocalCount()>0) {
            ErrorStack.leave();
            return null;
        }

      
        /* boolean assignable = */
        ASTAttribute rhsAttr = m_ast.lookup(ctx.expression());
        //TypeDefinition t = TypeUtilities.performImplicitDereferenceAndFunctioncallForTargetType(rhsAttr, lhsType);
        TypeUtilities.mayBeAssignedTo(lhsType, lhsVariable, ctx.expression(),m_ast);
//        if (t != null) {
//            // target type found
//            if (lhsType.getPrecision() < t.getPrecision()) {
//                CommonErrorMessages.typeMismatch(lhsType, t);
//            }
//        }
        ErrorStack.leave();
        return null;
    }


    
    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable =
                ((org.openpearl.compiler.SymbolTable.ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }
}
