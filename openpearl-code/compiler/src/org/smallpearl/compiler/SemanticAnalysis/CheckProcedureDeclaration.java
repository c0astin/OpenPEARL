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

package org.smallpearl.compiler.SemanticAnalysis;

import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.SmallPearlParser.ExpressionContext;
import org.smallpearl.compiler.SmallPearlParser.FormatPositionContext;
import org.smallpearl.compiler.SmallPearlParser.ProcedureBodyContext;
import org.smallpearl.compiler.SmallPearlParser.ProcedureDeclarationContext;
import org.smallpearl.compiler.SmallPearlParser.ResultAttributeContext;
import org.smallpearl.compiler.SmallPearlParser.TaskDeclarationContext;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SymbolTable.FormalParameter;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.ProcedureEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;


/**
 *
 * checks whether:
 * <ul>
 * <li>the formal parameters are passed correctly ---
 *    arrays, dations and realtime elements by IDENT,
 * <li> the result type is supported (currently no TYPE and no REF)
 * <li>the RETURN statement uses a proper type of expression, 
 *    but an implicit RETURN at the end of a PROc with RETURNS is not 
 *    part of this check 
 * <li>the procedure call uses compatible parameters
 * </ul>
 * 
 * <p>
 * Missing stuff:
 * <ul>
 *   <li>parameters of function calls are not treated, yet
 *   <li>REF is not treated 
 *   <li>TYPE is not treated
 * </ul>
 * 
 */
public class CheckProcedureDeclaration extends SmallPearlBaseVisitor<Void>
        implements SmallPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private AST m_ast = null;
    private TypeDefinition m_typeOfReturns;
    //private TypeDefinition m_typeOfReturnExpression;

    public CheckProcedureDeclaration(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_debug = debug;
        m_verbose = verbose;
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        Log.debug("    Check DationDeclaration");
    }

    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: CheckProcedureDeclaration: visitModule");
        }

        org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    /* @Override
    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: CheckProcedureDeclaration: visitProcedureDeclaration");
        }
    
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }
    */

    @Override
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: CheckProcedureDeclaration: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: CheckProcedureDeclaration: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: CheckProcedureDeclaration: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        Log.debug("Semantic: CheckProcedureDeclaration: visitProcedureDeclaration");

        ErrorStack.enter(ctx, "PROC");

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        SymbolTableEntry entry =
                this.m_currentSymbolTable.lookup(ctx.nameOfModuleTaskProc().ID().toString());
        if (entry == null) {
            throw new InternalCompilerErrorException(
                    "PROC " + ctx.nameOfModuleTaskProc().ID().toString() + " not found",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        ProcedureEntry procedureEntry = (ProcedureEntry) entry;

        if (procedureEntry.getFormalParameters() != null
                && procedureEntry.getFormalParameters().size() > 0) {
            /* check formal parameters of this procedure */

            for (FormalParameter formalParameter : procedureEntry.getFormalParameters()) {
                checkFormalParameter(formalParameter);
            }
        }

        // reset the attribute before visitChildren()
        // m_typeOfReturnExpression contains the type of the last RETURN statement
        // in the procedure body
        m_typeOfReturns = procedureEntry.getResultType();

        visitChildren(ctx);

        if (m_typeOfReturns != null) {
            // check last statement of function to be RETURN
            // this is easier to implement as to enshure that all paths of control
            // meet a RETURN(..) statement
            ProcedureBodyContext b = ctx.procedureBody();
            int last = b.statement().size();
            if (last == 0) {
                ErrorStack.add("must end with RETURN (" + m_typeOfReturns.toString() + ")");
            } else {
                SmallPearlParser.StatementContext lastStmnt = b.statement(last - 1);
                if (lastStmnt.unlabeled_statement() != null) {
                    if (lastStmnt.unlabeled_statement().returnStatement() == null) {
                        ErrorStack.add("must end with RETURN (" + m_typeOfReturns.toString() + ")");

                    }
                }
            }
        }
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        ErrorStack.leave();
        return null;
    }


    @Override
    public Void visitCallStatement(SmallPearlParser.CallStatementContext ctx) {
        Log.debug("Semantic: CheckProcedureDeclarations: visitCallStatement");

        ErrorStack.enter(ctx, "CALL");
        String procName = ctx.ID().getText();
        ProcedureEntry proc = null;

        SymbolTableEntry entry = m_currentSymbolTable.lookup(procName);

        if (entry != null) {
            if (entry instanceof ProcedureEntry) {
                Log.debug("Semantic: Check ProcedureCall: found call in expression");
                proc = (ProcedureEntry) entry;
                TypeDefinition resultType = proc.getResultType();

                if (resultType != null) {
                    ErrorStack.add("result discarded");
                }
            } else {
                ErrorStack.add("'" + procName + "' is not of type PROC  -- is of type "
                        + CommonUtils.getTypeOf(entry));
            }
        } else {
            ErrorStack.add("'" + procName + "' is not defined");
        }

        if (proc != null && ctx.listOfActualParameters() != null) {

            int nbrActualParameters = ctx.listOfActualParameters().expression().size();
            int nbrFormatParameters = proc.getFormalParameters().size();

            if (nbrActualParameters != nbrFormatParameters) {
                ErrorStack.add("number of parameters mismatch: expected " + nbrFormatParameters
                        + " -- got " + nbrActualParameters);
            } else {
                // check each parameter type
                for (int i = 0; i < nbrActualParameters; i++) {
                    ErrorStack.enter(ctx.listOfActualParameters().expression(i), "param");
                    checkParameter(proc, ctx.listOfActualParameters().expression(i),
                            proc.getFormalParameters().get(i));
                    ErrorStack.leave();
                }
            }

            // really necessary?
            //      visitListOfActualParameters(ctx.listOfActualParameters());
        }

        ErrorStack.leave();

        return null;
    }


    @Override
    public Void visitReturnStatement(SmallPearlParser.ReturnStatementContext ctx) {
        ErrorStack.enter(ctx, "RETURN");


        if (m_typeOfReturns == null && ctx.expression() != null) {
            ErrorStack.add("illegal without RETURNS in declaration");
        } else if (ctx.expression() != null) {
            // we have an expression at RETURN
            TypeDefinition exprType = m_ast.lookupType(ctx.expression());

            // can be removed if all possible types are detected
            Boolean typeIsCompatible = true;

            TypeDefinition tmpTypeOfResult = m_typeOfReturns;
            TypeDefinition tmpExprType = exprType;

            if (m_typeOfReturns != null) {
                // check for implicit dereference /reference possibilities
                // --> base types must be compatible
                if (tmpTypeOfResult instanceof TypeReference) {
                    tmpTypeOfResult = ((TypeReference) tmpTypeOfResult).getBaseType();
                }
                if (tmpExprType instanceof TypeReference) {
                    tmpExprType = ((TypeReference) tmpExprType).getBaseType();
                }

                // check compatibility of baseTypes
                if (tmpTypeOfResult instanceof TypeFixed && tmpExprType instanceof TypeFixed) {
                    if (((TypeFixed) tmpTypeOfResult).getPrecision() < ((TypeFixed) tmpExprType)
                            .getPrecision()) {
                        typeIsCompatible = false;
                    }
                } else if (tmpTypeOfResult instanceof TypeFloat
                        && tmpExprType instanceof TypeFloat) {
                    if (((TypeFloat) tmpTypeOfResult).getPrecision() < ((TypeFloat) tmpExprType)
                            .getPrecision()) {
                        typeIsCompatible = false;
                    }
                } else if (tmpTypeOfResult instanceof TypeBit && tmpExprType instanceof TypeBit) {
                    if (((TypeBit) tmpTypeOfResult).getPrecision() < ((TypeBit) tmpExprType)
                            .getPrecision()) {
                        typeIsCompatible = false;
                    }
                } else if (tmpTypeOfResult instanceof TypeChar && tmpExprType instanceof TypeChar) {
                    if (((TypeChar) tmpTypeOfResult).getSize() < ((TypeChar) tmpExprType)
                            .getSize()) {
                        typeIsCompatible = false;
                    }
                } else if (tmpTypeOfResult instanceof TypeChar
                        && tmpExprType instanceof TypeVariableChar) {
                    // this must be checked during runtime
                } else {
                    if (!tmpTypeOfResult.equals(tmpExprType)) {
                        typeIsCompatible = false;
                    }
                }

                if (!typeIsCompatible) {
                    ErrorStack.add("expression does not fit to RETURN type: expected "
                            + m_typeOfReturns.toString() + " -- got " + exprType.toString());
                }

                if (m_typeOfReturns instanceof TypeReference) {
                    // check lifeCyle required
                    ASTAttribute attrRhs = m_ast.lookup(ctx.expression());
                    if (attrRhs.getVariable() != null) {
                        int level = attrRhs.getVariable().getLevel();
                        if (level > 1) {
                            ErrorStack.add("life cycle of '" + attrRhs.getVariable().getName()
                                    + "' is too short");
                        }
                    } else if (attrRhs.getType() instanceof TypeProcedure) {
                        // ok - we have a procedure name
                    }
                }

            }
        }

        ErrorStack.leave();
        return null;
    }

    private void checkParameter(ProcedureEntry proc, ExpressionContext expression,
            FormalParameter formalParameter) {
        // check types - must be equal if IDENT is set
        //               formalParameter may be larger if IDENT ist NOT SET
        // 
        // check INV  on actual parameter enforces INV on formal parameter
        // check IDENT must be set for array

        // analyse expression
        TypeDefinition actualType = null;
        boolean actualIsInv = false;
        boolean actualIsArray = false;
        int actualArrayDimensions = 0;
        TypeDefinition formalBaseType = null;
        TypeDefinition actualBaseType = null;

        VariableEntry actualVariableEntry = null;
        ASTAttribute attr = m_ast.lookup(expression);

        if (attr != null) {
            actualType = attr.getType();
            actualIsInv = attr.isReadOnly();

            actualVariableEntry = attr.getVariable();

            if (actualType instanceof TypeArray) {
                actualArrayDimensions = ((TypeArray) actualType).getNoOfDimensions();
                actualBaseType = ((TypeArray) actualType).getBaseType();
                actualIsArray = true;
            }
        }

        if (formalParameter.passIdentical) {
            // actual parameter must be LValue
            if (actualVariableEntry == null) {
                ErrorStack.add("only variables may be passed by IDENT");
                return; // do no further checks on this parameter
            } else {
                if (actualIsInv && !formalParameter.getAssigmentProtection()) {
                    ErrorStack.add("pass INV data as non INV parameter");
                }
            }
        }

        if (formalParameter.getType() instanceof TypeArray) {
            formalBaseType = ((TypeArray) formalParameter.getType()).getBaseType();
        } else {
            formalBaseType = formalParameter.getType();
        }

        // compare array parameters
        if (actualIsArray) {
            if (formalParameter.getType() instanceof TypeArray) {
                // both are arrays --> nbr of dimensions and baseTypes must fit 
                TypeArray ta = ((TypeArray) formalParameter.getType());
                if (actualArrayDimensions != ta.getNoOfDimensions()) {
                    ErrorStack.add("dimension mismatch: expect " + ta.getNoOfDimensions()
                            + " -- got " + actualArrayDimensions);
                } else if (!actualBaseType.equals(ta.getBaseType())) {
                    ErrorStack.add("type mismatch: expect ARRAY of "
                            + ((TypeArray) formalParameter.getType()).getBaseType()
                            + " -- got ARRAY of " + actualType.toString());
                }
            } else {
                ErrorStack.add("expected scalar type");
            }
        } else {
            if (formalParameter.getType() instanceof TypeArray) {
                ErrorStack.add("expected array type");
            } else {
                // treat both scalar types

                // easy stuff first -- check base types
                // if they fit we must check length for FIXED,FLOAT,CHAR,BIT
                if (formalBaseType instanceof TypeChar && actualType instanceof TypeVariableChar) {
                    // this is ok -- mark the actual parameter in the AST Attribute
                    // this will be used by the CppCodeGeneratorVisitor to instanciate
                    // a temporary variable
                    ((TypeVariableChar) attr.getType()).setBaseType(formalBaseType);
                } else if (formalBaseType instanceof TypeFloat && actualType instanceof TypeFixed) {
                    // check if an implicit cast is allowed:
                    if(actualType.getPrecision() > formalBaseType.getPrecision()) {
                        ErrorStack.add("actual argument does not fit into formal parameter: expected " + formalBaseType.toString()
                                + "  -- got " + actualType.toString());
                    }
                } else if (!formalBaseType.getName().equals(actualType.getName())) {
                    ErrorStack.add("type mismatch: expected " + formalBaseType.toString()
                            + "  -- got " + actualType.toString());
                } else if (formalParameter.getType() instanceof TypeFixed) {

                    TypeFixed fp = (TypeFixed) formalParameter.getType();
                    TypeFixed ap = (TypeFixed) actualType;

                    if (formalParameter.passIdentical == false
                            && fp.getPrecision().intValue() < ap.getPrecision().intValue()) {
                        ErrorStack.add("type mismatch: expected (not larger than) "
                                + formalParameter.toString() + " -- got " + actualType.toString());
                    }
                    if (formalParameter.passIdentical
                            && fp.getPrecision().intValue() != ap.getPrecision().intValue()) {
                        ErrorStack.add("type mismatch: expected " + formalParameter.toString()
                                + " -- got " + actualType.toString());
                    }
                } else if (formalParameter.getType() instanceof TypeFloat
                        && actualType instanceof TypeFixed) {
                    // implicit FIXED->FLOAT conversion is ok if NOT IDENT
                    TypeFloat fp = (TypeFloat) formalParameter.getType();
                    TypeFixed ap = (TypeFixed) actualType;

                    if (formalParameter.passIdentical == false
                            && fp.getPrecision().intValue() < ap.getPrecision().intValue()) {
                        ErrorStack.add("type mismatch: expected (not larger than) "
                                + formalParameter.toString() + " -- got " + actualType.toString());
                    }
                    if (formalParameter.passIdentical) {
                        ErrorStack.add("type mismatch: expected " + formalParameter.toString()
                                + " -- got " + actualType.toString());
                    }
                } else if (formalParameter.getType() instanceof TypeBit) {

                    TypeBit fp = (TypeBit) formalParameter.getType();
                    TypeBit ap = (TypeBit) actualType;

                    if (formalParameter.passIdentical == false
                            && fp.getPrecision().intValue() < ap.getPrecision().intValue()) {
                        ErrorStack.add("type mismatch: expected (not larger than) "
                                + formalParameter.toString() + " -- got " + actualType.toString());
                    }
                    if (formalParameter.passIdentical
                            && fp.getPrecision().intValue() != ap.getPrecision().intValue()) {
                        ErrorStack.add("type mismatch: expected " + formalParameter.toString()
                                + " -- got " + actualType.toString());
                    }
                } else if (formalParameter.getType() instanceof TypeChar) {

                    TypeChar fp = (TypeChar) formalParameter.getType();
                    TypeChar ap = (TypeChar) actualType;

                    if (formalParameter.passIdentical == false && fp.getSize() < ap.getSize()) {
                        ErrorStack.add("type mismatch: expected (not larger than) "
                                + formalParameter.toString() + " -- got " + actualType.toString());
                    }
                    if (formalParameter.passIdentical && fp.getSize() != ap.getSize()) {
                        ErrorStack.add("type mismatch: expected " + formalParameter.toString()
                                + " -- got " + actualType.toString());
                    }
                }
            }
        }
    }


    @Override
    public Void visitPrimaryExpression(SmallPearlParser.PrimaryExpressionContext ctx) {

        if (ctx.name() != null && ctx.name().ID() != null) {
            SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.name().ID().getText());

            if (entry instanceof org.smallpearl.compiler.SymbolTable.ProcedureEntry) {
                org.smallpearl.compiler.SymbolTable.ProcedureEntry proc =
                        (org.smallpearl.compiler.SymbolTable.ProcedureEntry) (entry);

                int nbrActualParameters = 0;
                int nbrFormalParameters = 0;
                if (ctx.name().listOfExpression() != null
                        && ctx.name().listOfExpression().expression().size() > 0) {
                    nbrActualParameters = ctx.name().listOfExpression().expression().size();
                }
                if (proc.getFormalParameters() != null) {
                    nbrFormalParameters = proc.getFormalParameters().size();
                }
                /*
                 * not useful for ref proc assignments
                if (nbrActualParameters != nbrFormalParameters) {
                	ErrorStack.enter(ctx);
                	ErrorStack.add("number of parameters mismatch: given "+nbrActualParameters+" expected: "+nbrFormalParameters);
                	ErrorStack.leave();
                }
                */
                if (ctx.name().listOfExpression() != null
                        && ctx.name().listOfExpression().expression().size() > 0) {
                    int min = Math.min(nbrActualParameters, nbrFormalParameters);
                    for (int i = 0; i < min; i++) {
                        ErrorStack.enter(ctx.name().listOfExpression().expression(i), "param");
                        checkParameter(proc, ctx.name().listOfExpression().expression(i),
                                proc.getFormalParameters().get(i));
                        ErrorStack.leave();
                    }
                }

            }
        }
        return null;
    }


    private void checkFormalParameter(FormalParameter formalParameter) {
        TypeDefinition type;
        Boolean passIdentical = formalParameter.passIdentical;

        type = formalParameter.getType();
        ParserRuleContext ctx = formalParameter.getCtx();

        if ((!passIdentical) && type instanceof TypeArray) {
            ErrorStack.enter(ctx, "param");
            ErrorStack.add("arrays must passed by IDENT");
            ErrorStack.leave();
        }
        if ((!passIdentical) && type instanceof TypeSemaphore) {
            ErrorStack.enter(ctx, "param");
            ErrorStack.add("SEMA must passed by IDENT");
            ErrorStack.leave();
        }
        if ((!passIdentical) && type instanceof TypeBolt) {
            ErrorStack.enter(ctx, "param");
            ErrorStack.add("BOLT must passed by IDENT");
            ErrorStack.leave();
        }
        if ((!passIdentical) && type instanceof TypeDation) {
            ErrorStack.enter(ctx, "param");
            ErrorStack.add("DATION must passed by IDENT");
            ErrorStack.leave();
        }
        if ((!passIdentical) && type instanceof TypeInterrupt) {
            ErrorStack.enter(ctx, "param");
            ErrorStack.add("INTERRUPT must passed by IDENT");
            ErrorStack.leave();
        }
        if ((!passIdentical) && type instanceof TypeSignal) {
            ErrorStack.enter(ctx, "param");
            ErrorStack.add("SIGNAL must passed by IDENT");
            ErrorStack.leave();
        }

    }

}
