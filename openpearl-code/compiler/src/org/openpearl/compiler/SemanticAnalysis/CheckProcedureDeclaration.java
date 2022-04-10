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

import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.*;
import org.openpearl.compiler.Exception.*;
import org.openpearl.compiler.SymbolTable.FormalParameter;
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.ProcedureEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;


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
public class CheckProcedureDeclaration extends OpenPearlBaseVisitor<Void>
implements OpenPearlVisitor<Void> {

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
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        Log.debug("    Check DationDeclaration");
    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: CheckProcedureDeclaration: visitModule");
        }

        org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }


    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: CheckProcedureDeclaration: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: CheckProcedureDeclaration: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: CheckProcedureDeclaration: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
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
            TypeReference refChar = new TypeReference(new TypeRefChar());
            
            if (m_typeOfReturns.equals(refChar)) {
                ErrorStack.add(ctx.typeProcedure().resultAttribute().resultType(),"RETURNS", "type "+refChar+" is not allowed as result type");
            }
            
            // check last statement of function to be RETURN
            // this is easier to implement as to enshure that all paths of control
            // meet a RETURN(..) statement
            OpenPearlParser.ProcedureBodyContext b = ctx.procedureBody();
            int last = b.statement().size();
            if (last == 0) {
                ErrorStack.add("must end with RETURN (" + m_typeOfReturns.toString() + ")");
            } else {
                OpenPearlParser.StatementContext lastStmnt = b.statement(last - 1);
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
    public Void visitCallStatement(OpenPearlParser.CallStatementContext ctx) {
        Log.debug("Semantic: CheckProcedureDeclarations: visitCallStatement");
        //System.out.println(ctx.getText());
        ErrorStack.enter(ctx, "CALL");
        ASTAttribute attr = m_ast.lookup(ctx.name());
        TypeDefinition tp=attr.getType();

        SymbolTableEntry entry = attr.getSymbolTableEntry();
        if (entry instanceof ProcedureEntry) {
            tp = ((ProcedureEntry)entry).getType();
        } else if (entry instanceof VariableEntry) {
            tp = ((VariableEntry)entry).getType();
            if (tp instanceof TypeReference && ((TypeReference)tp).getBaseType() instanceof TypeProcedure) {
                tp = ((TypeReference)tp).getBaseType();
            }
            if (!(tp instanceof TypeProcedure)) {
                ErrorStack.add("must be PROCEDURE --- got "+((VariableEntry)entry).getType());
                ErrorStack.leave();
                return null;
            }
        } else {
            ErrorStack.addInternal("CppCodeGen@240: untreated alternative");
            ErrorStack.leave();
            return null;
        }

        if (((TypeProcedure)tp).getFormalParameters() != null && attr.getType() != null) {
            ErrorStack.add(tp.toString4IMC(true)+" requires actual parameters");
            ErrorStack.leave();
            return null;
        }
        if (((TypeProcedure)tp).getResultType() != null) {
            ErrorStack.add(tp.toString4IMC(true)+" --- result discarded");
            ErrorStack.leave();
            return null; 
        }
        attr.setType(null);  // mark procedure call
        ErrorStack.leave();

        return null;
    }


    @Override
    public Void visitReturnStatement(OpenPearlParser.ReturnStatementContext ctx) {
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
            ASTAttribute attr = m_ast.lookup(ctx.expression());

            if (m_typeOfReturns != null) {
                if (TypeUtilities.isSimpleInclVarCharAndRefCharOrStructureType(m_typeOfReturns)) {
                    TypeUtilities.deliversTypeOrEmitErrorMessage(ctx.expression(), m_typeOfReturns,  m_ast, "RETURN"); 
                } else if (m_typeOfReturns instanceof TypeReference) {
                    String typeOfExpression = attr.getType().toString4IMC(true);
                    if (!attr.getType().equals(m_typeOfReturns)) {
                    TypeDefinition t= 
                    TypeUtilities.performImplicitDereferenceAndFunctioncallForTargetType(attr,
                            ((TypeReference)m_typeOfReturns).getBaseType());
                    if (t == null) {
                        ErrorStack.add(ctx.expression(),null, "expected type '" + m_typeOfReturns.toString4IMC(true) + "' --- got '"
                                + typeOfExpression + "'");
                    }
                    int x=0;
                    }
                }
//                // check for implicit dereference /reference possibilities
//                // --> base types must be compatible
//                if (tmpTypeOfResult instanceof TypeReference) {
//                    tmpTypeOfResult = ((TypeReference) tmpTypeOfResult).getBaseType();
//                }
//                if (tmpExprType instanceof TypeReference) {
//                    tmpExprType = ((TypeReference) tmpExprType).getBaseType();
//                }
//
//                // check compatibility of baseTypes
//                if (tmpTypeOfResult instanceof TypeFixed && tmpExprType instanceof TypeFixed) {
//                    if (((TypeFixed) tmpTypeOfResult).getPrecision() < ((TypeFixed) tmpExprType)
//                            .getPrecision()) {
//                        typeIsCompatible = false;
//                    }
//                } else if (tmpTypeOfResult instanceof TypeFloat
//                        && tmpExprType instanceof TypeFloat) {
//                    if (((TypeFloat) tmpTypeOfResult).getPrecision() < ((TypeFloat) tmpExprType)
//                            .getPrecision()) {
//                        typeIsCompatible = false;
//                    }
//                } else if (tmpTypeOfResult instanceof TypeBit && tmpExprType instanceof TypeBit) {
//                    if (((TypeBit) tmpTypeOfResult).getPrecision() < ((TypeBit) tmpExprType)
//                            .getPrecision()) {
//                        typeIsCompatible = false;
//                    }
//                } else if (tmpTypeOfResult instanceof TypeChar && tmpExprType instanceof TypeChar) {
//                    if (((TypeChar) tmpTypeOfResult).getSize() < ((TypeChar) tmpExprType)
//                            .getSize()) {
//                        typeIsCompatible = false;
//                    }
//                } else if (tmpTypeOfResult instanceof TypeChar
//                        && tmpExprType instanceof TypeVariableChar) {
//                    // this must be checked during runtime
//                } else {
//                    if (!tmpTypeOfResult.equals(tmpExprType)) {
//                        typeIsCompatible = false;
//                    }
//                }
//
//                if (!typeIsCompatible) {
//                    ErrorStack.add("expression does not fit to RETURN type: expected "
//                            + m_typeOfReturns.toString() + " -- got " + exprType.toString());
//                }
//
                if (m_typeOfReturns instanceof TypeReference) {
                    // check lifeCyle required
                    ASTAttribute attrRhs = m_ast.lookup(ctx.expression());
                    if (attrRhs.getVariable() != null) {
                        int level = attrRhs.getVariable().getLevel();
                        
                        // level 1: the module statement
                        // level 2: static, global,external objects, as well as formal parameters
                        // level 3: task/proc local top level objects
                        // level 4...: deeper nested blocks/loops
                        
                        // objects, which are related to a formal parameter have static lifetimem,
                        //   since we have no nested procedures/tasks
                        if (level > 2) {
                            ErrorStack.add("life cycle of '" + attrRhs.getVariable().getName()
                                    + "' is too short");
                        }
//                    } else if (attrRhs.getType() instanceof TypeProcedure) {
//                        // ok - we have a procedure name
                    }
                }

            }
        }

        ErrorStack.leave();
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
        
        if (passIdentical && type instanceof TypeReference) {
            TypeDefinition base =  ((TypeReference)type).getBaseType();
            if (base instanceof TypeRefChar) {
                if (! base.hasAssignmentProtection() && !type.hasAssignmentProtection()) {
                    ErrorStack.enter(ctx, "param");
                    ErrorStack.add("REF CHAR() must not be passed by IDENT");
                    ErrorStack.leave();
                }
            }
        }

    }

}
