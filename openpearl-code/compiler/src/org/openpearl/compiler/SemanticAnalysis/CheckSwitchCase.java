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
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;

import java.util.ArrayList;
import java.util.Stack;

/**
 * check the CASE statement 
 * 
 * Checks:
 * <ul>
 * <li>type of expression must be for type 1 of type FIXED 
 * <li>type of expression must be for type 2 of type FIXED or CHAR(1)
 * <li>unique selectors
 * <li>the type of the selectors must be equal to the type of expression 
 * <li>the value of the selectors must not exceed the range of the expression type 
 * </ul>
 *
 * 
 * Principle of operation
 * <ol>
 * <li> a local class CurrentCaseData contains information about
 *    <ul>
 *    <li>type of the expression
 *    <li>listOfAlternatives
 *    </ul>
 * <li> selectors of type CHAR() are mapped to the according fixed values
 * <li> each CASE creates a new set of CurrentCaseData, 
 *    pushing the previous set to a stack 
 * <li> each visitCase_statement_selection2_alt adds the selections 
 *      to the list and checks if the new selections are already in the list
 *      and report an error if the selection is duplicate
 * <li> at the end of the CASE statement, the previous list of alternatives
 *       pops from the stack
 * </ol>
 * 
 * Notes:
 * <ul>
 * <li>CASE statements of type 2 may be used also with CHAR(1). This 
 *     is currently not supported by the code generator 
 *</ul> 
 */
public class CheckSwitchCase extends OpenPearlBaseVisitor<Void>
        implements OpenPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private Stack<CaseDataSet> m_caseStack;
    private CaseDataSet m_currentCaseDataSet = null;
    private AST m_ast = null;

    /**
     * simple data structure with presets for the treatment of a CASE
     * statement
     */
    private class CaseDataSet {
        public ArrayList<FixedRange> m_listOfAlternatives = null;
        public TypeDefinition m_typeOfExpression;


        public CaseDataSet(TypeDefinition typeOfExpression) {
            m_listOfAlternatives = new ArrayList<FixedRange>();
            m_typeOfExpression = typeOfExpression;
        }
    }

    public CheckSwitchCase(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_debug = debug;
        m_verbose = verbose;
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        m_caseStack = new Stack<CaseDataSet>();

        Log.debug("    Check Case");
    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Case: visitModule");
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
            System.out.println("Semantic: Check Case: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Case: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Case: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Case: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitCase_statement(OpenPearlParser.Case_statementContext ctx) {
        m_caseStack.push(m_currentCaseDataSet);

        //m_listOfAlternativesStack.push(m_listOfAlternatives);
        if (ctx.case_statement_selection1() != null) {
            ASTAttribute attr = m_ast.lookup(ctx.case_statement_selection1().expression());
            if (attr == null) {
                ErrorStack.addInternal(ctx, "no AST attributes found for expression", "");
                return null; // abort further checks
            }
            if (attr.m_type instanceof TypeFixed) {
                m_currentCaseDataSet = new CaseDataSet(attr.m_type);
            } else {
                ErrorStack.add(ctx.case_statement_selection1().expression(), "CASE",
                        "expression must be of type FIXED -- but is " + attr.m_type);
                return null; // abort further checks
            }

        }
        if (ctx.case_statement_selection2() != null) {
            ASTAttribute attr = m_ast.lookup(ctx.case_statement_selection2().expression());
            if (attr == null) {
                ErrorStack.addInternal(ctx, "no AST attributes found for expression", "");
                return null; // abort further checks
            }
            if (attr.m_type instanceof TypeFixed) {
                m_currentCaseDataSet = new CaseDataSet(attr.m_type);
            } else if (attr.m_type instanceof TypeChar) {
                if (attr.m_type.getPrecision() > 1) {
                    ErrorStack.add(ctx.case_statement_selection2().expression(), "CASE",
                            "must be CHAR(1) -- but is " + attr.m_type);
                    return null; // abort further checks
                }
                m_currentCaseDataSet = new CaseDataSet(attr.m_type);

            } else {
                ErrorStack.add(ctx.case_statement_selection2().expression(), "illegal type",
                        "" + attr.m_type);
                return null; // abort further checks
            }
        }
        visitChildren(ctx);
        m_currentCaseDataSet = m_caseStack.pop();
        return null;
    }

    @Override
    public Void visitCase_statement_selection2(
            OpenPearlParser.Case_statement_selection2Context ctx) {
        visitChildren(ctx);
        return null;
    }


    @Override
    public Void visitCase_statement_selection2_alt(
            OpenPearlParser.Case_statement_selection2_altContext ctx) {
        long lowerBoundary;
        long upperBoundary;


        if (m_currentCaseDataSet.m_typeOfExpression instanceof TypeFixed) {
            long max = Long.MAX_VALUE >> (Long.SIZE
                    - m_currentCaseDataSet.m_typeOfExpression.getPrecision() - 1);
            long min = -max - 1;

            ConstantFixedExpressionEvaluator evaluator = new ConstantFixedExpressionEvaluator(
                    m_verbose, m_debug, m_currentSymbolTable, null, null);

            for (int i = 0; i < ctx.case_list().index_section().size(); i++) {
                boolean isOk = true;
                OpenPearlParser.Index_sectionContext index = ctx.case_list().index_section(i);
                if (index.constantFixedExpression().size() == 0) {
                    String s = index.constantCharacterString(0).getText();
                    s = CommonUtils.removeQuotes(s);
                    int len = CommonUtils.getStringLength(s);
                    ASTAttribute attr = m_ast.lookup(index.constantCharacterString(0));
                    ErrorStack.add(index, "CASE",
                            "type mismatch need type " + m_currentCaseDataSet.m_typeOfExpression
                                    + " --- got CHAR(" + len + ")");

                    isOk = false;
                } else if (index.constantFixedExpression().size() == 1) {

                    ConstantFixedValue alternativ =
                            evaluator.visit(index.constantFixedExpression(0));

                    lowerBoundary = alternativ.getValue();
                    upperBoundary = lowerBoundary;

                    if (alternativ.getPrecision() > m_currentCaseDataSet.m_typeOfExpression
                            .getPrecision()) {
                        ErrorStack.warn(index.constantFixedExpression(0), "CASE",
                                "alternative too large: is of type FIXED("
                                        + alternativ.getPrecision()
                                        + ") -- expression is of type FIXED("
                                        + m_currentCaseDataSet.m_typeOfExpression.getPrecision()
                                        + ")");
                        isOk = false;
                    }

                    for (int j = 0; j < m_currentCaseDataSet.m_listOfAlternatives.size(); j++) {
                        if (m_currentCaseDataSet.m_listOfAlternatives.get(j)
                                .isContained(lowerBoundary)) {
                            ErrorStack.add(index, "CASE", "duplicate alternative " + lowerBoundary);
                            ErrorStack.note(
                                    m_currentCaseDataSet.m_listOfAlternatives.get(j).getCtx(),
                                    "previous definition", "");
                            isOk = false;
                        }
                    }
                    if (isOk) {
                        m_currentCaseDataSet.m_listOfAlternatives
                                .add(new FixedRange(index, lowerBoundary, upperBoundary));
                    }
                } else if (index.constantFixedExpression().size() == 2) {
                    ConstantFixedValue alternativ =
                            evaluator.visit(index.constantFixedExpression(0));

                    lowerBoundary = alternativ.getValue();
                    if (alternativ.getPrecision() > m_currentCaseDataSet.m_typeOfExpression
                            .getPrecision()) {
                        ErrorStack.warn(index.constantFixedExpression(0), "CASE",
                                "alternative too large: is of type FIXED("
                                        + alternativ.getPrecision()
                                        + ") -- expression is of type FIXED("
                                        + m_currentCaseDataSet.m_typeOfExpression.getPrecision()
                                        + ")");
                        isOk = false;
                    }

                    alternativ = evaluator.visit(index.constantFixedExpression(1));
                    upperBoundary = alternativ.getValue();
                    if (alternativ.getPrecision() > m_currentCaseDataSet.m_typeOfExpression
                            .getPrecision()) {
                        ErrorStack.warn(index.constantFixedExpression(1), "CASE",
                                "alternative too large: is of type FIXED("
                                        + alternativ.getPrecision()
                                        + ") -- expression is of type FIXED("
                                        + m_currentCaseDataSet.m_typeOfExpression.getPrecision()
                                        + ")");
                        isOk = false;
                    }

                    if (lowerBoundary > upperBoundary) {
                        isOk = false;
                        ErrorStack.add(index, "CASE", "illegal range");
                    } else {

                        for (long alt = lowerBoundary; alt <= upperBoundary; alt++) {
                            for (int j = 0; j < m_currentCaseDataSet.m_listOfAlternatives
                                    .size(); j++) {

                                if (m_currentCaseDataSet.m_listOfAlternatives.get(j)
                                        .isContained(alt)) {
                                    isOk = false;
                                    ErrorStack.add(index, "CASE", "duplicate alternative " + alt);
                                    ErrorStack.note(m_currentCaseDataSet.m_listOfAlternatives.get(j)
                                            .getCtx(), "previous definition", "");
                                    //throw new DuplicateAltValueException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                                }
                            }
                        }

                        if (isOk) {
                            m_currentCaseDataSet.m_listOfAlternatives
                                    .add(new FixedRange(index, lowerBoundary, upperBoundary));
                        }
                    }
                }
            }
        }
        if (m_currentCaseDataSet.m_typeOfExpression instanceof TypeChar) {
            for (int i = 0; i < ctx.case_list().index_section().size(); i++) {
                boolean isOk = true;
                OpenPearlParser.Index_sectionContext index = ctx.case_list().index_section(i);
                if (index.constantCharacterString().size() == 0) {
                    String s = index.constantFixedExpression(i).getText();
                    s = CommonUtils.removeQuotes(s);
                    int len = CommonUtils.getStringLength(s);
                    ASTAttribute attr = m_ast.lookup(index.constantCharacterString(0));
                    ErrorStack.add(index, "CASE",
                            "type mismatch need type " + m_currentCaseDataSet.m_typeOfExpression
                                    + " --- got CHAR(" + len + ")");

                    isOk = false;
                } else if (index.constantCharacterString().size() == 1) {
                    String s = index.constantCharacterString(0).getText();
                    s = CommonUtils.removeQuotes(s);
                    int len = CommonUtils.getStringLength(s);
                    if (len > 1) {
                        ErrorStack.add(index, "CASE",
                                "type mismatch need type " + m_currentCaseDataSet.m_typeOfExpression
                                        + " --- got CHAR(" + len + ")");

                        isOk = false;
                    }

                    lowerBoundary = s.charAt(0);
                    upperBoundary = lowerBoundary;


                    for (int j = 0; j < m_currentCaseDataSet.m_listOfAlternatives.size(); j++) {
                        if (m_currentCaseDataSet.m_listOfAlternatives.get(j)
                                .isContained(lowerBoundary)) {
                            ErrorStack.add(index, "CASE", "duplicate alternative " + s);
                            ErrorStack.note(
                                    m_currentCaseDataSet.m_listOfAlternatives.get(j).getCtx(),
                                    "previous definition", "");
                            isOk = false;
                        }
                    }
                    if (isOk) {
                        m_currentCaseDataSet.m_listOfAlternatives
                                .add(new FixedRange(index, lowerBoundary, upperBoundary));
                    }
                } else if (index.constantCharacterString().size() == 2) {
                    String sLow = index.constantCharacterString(0).getText();
                    sLow = CommonUtils.removeQuotes(sLow);
                    int len = CommonUtils.getStringLength(sLow);
                    if (len > 1) {
                        ErrorStack.add(index, "CASE",
                                "type mismatch need type " + m_currentCaseDataSet.m_typeOfExpression
                                        + " --- got CHAR(" + len + ")");

                        isOk = false;
                    }

                    lowerBoundary = sLow.charAt(0);

                    String sUp = index.constantCharacterString(1).getText();
                    sUp = CommonUtils.removeQuotes(sUp);
                    len = CommonUtils.getStringLength(sUp);
                    if (len > 1) {
                        ErrorStack.add(index, "CASE",
                                "type mismatch need type " + m_currentCaseDataSet.m_typeOfExpression
                                        + " --- got CHAR(" + len + ")");

                        isOk = false;
                    }

                    upperBoundary = sUp.charAt(0);

                    if (lowerBoundary > upperBoundary) {
                        isOk = false;
                        ErrorStack.add(index, "CASE", "illegal range");
                    } else {

                        for (long alt = lowerBoundary; alt <= upperBoundary; alt++) {
                            for (int j = 0; j < m_currentCaseDataSet.m_listOfAlternatives
                                    .size(); j++) {

                                if (m_currentCaseDataSet.m_listOfAlternatives.get(j)
                                        .isContained(alt)) {
                                    isOk = false;
                                    String sAlt = Character.toString((char) alt);
                                    ErrorStack.add(index, "CASE", "duplicate alternative " + sAlt);
                                    ErrorStack.note(m_currentCaseDataSet.m_listOfAlternatives.get(j)
                                            .getCtx(), "previous definition", "");
                                    //throw new DuplicateAltValueException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                                }
                            }
                        }

                        if (isOk) {
                            m_currentCaseDataSet.m_listOfAlternatives
                                    .add(new FixedRange(index, lowerBoundary, upperBoundary));
                        }
                    }
                }
            }

        }

        return null;
    }
}
