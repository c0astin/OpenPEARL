/*
 * [The "BSD license"]
 * *  Copyright (c) 2012-2021 Rainer Mueller & Marcel Schaible
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
 *     derived from this software without specific prior written permissision.
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

package org.openpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.openpearl.compiler.Exception.*;
import org.openpearl.compiler.SymbolTable.*;
import org.openpearl.compiler.OpenPearlParser.*;

import java.util.LinkedList;

/*
 * notes about implicit dereferencing:
 *  1) there are many cases of implicit dereferencing
 *     the dereferencing inside of the rule name
 *      eq x STRUCT [ a REF STRUCT [ b FIXED,
 *                                   c REF FIXED,
 *                                   d REF PROC(FIXED) RETURNS(FIXED),
 *                                   e REF PROC RETURNS(FIXED),
 *                                   f REF PROC RETURNS(REF FIXED),
 *                                   g REF PROC ]];
 *      x.a.b need implicit dereferencing on lhs and rhs
 *            and is of type FIXED
 *      x.a.c is REF FIXED
 *  2) 1) hold also for intermediated array elements like
 *     x STRUCT [ a(10) REF STRUCT [ b FIXED, .. ]
 *       x.a(2).b
 *  3) x.a.d(3) is a function call with implicit derefenciation
 *  4) x.a.e may be TypeProcedure or TypeFixed. This is decided by the usage in assignment
 *     or procedure parameter
 *  5) x.a.f may be on lhs and rhs!
 *     on lhs: function call with result REF FIXED with may be used with CONT
 *     on rhs: is TypProcedure or function call depending on the usage
 *  6) x.a.g depends on usage: ether REF PROC or procedure call
 *  7) REFs should not become dereferenced if
 *     rhs of assignments if lhs is a REF
 *     as procedure parameters is the formal parameter is a REF
 *     in expressions with IS or ISNT
 *  8) REFs must be dereferences if expressions are used with operators, 
 *     except in CONT, IS and ISNT
 *
 * If an EXPRESSION needs an implicit dereference, the ASTAttribute becomes 
 * modified with the flag 'needImplicitDereferencing'. This helps the code 
 * generator. Explicit dereferencing is not noted in the ASTAttributes.
 * 
 * Implicit dereferencing of STRUCT components are noted in the ASTAttribute 
 * of the name of the structure.
 * 
 * Names of variables are marked as lValue; expression results are marked 
 * as non lValue, which is the default setting 
 *  
 */
/**
 * @author Marcel Schaible
 * @version 1
 *     <p><b>Description</b> The ExpressionTypeVisitor (ETV) determines for all expressions
 *     including expressions inside initializers their resulting type. The result type is attached
 *     to the context and stored in the AST by calling the method {@link
 *     org.openpearl.compiler.AST#put}. The type can be later be retrieved by calling {@link
 *     org.openpearl.compiler.AST#lookup}.
 *     <p>The following checks are performed during AST traversal:
 *     <ol>  
 *       <li>The precision of FLOAT must be either {@link
 *           org.openpearl.compiler.Defaults#FLOAT_SHORT_PRECISION} or
 *           {@link org.smallpearl.compiler.Defaults#FLOAT_LONG_PRECISION}</li>
 *       <li>Checks the type of expression in all monadic and dyadic operators for allowed types.
 *         <ul>
 *           <li> if a dereference of a REF   or the invocation of a PROC is required to obtain a valid type
 *           the action is marked in the {@link ASTAttribute} for usage in the code generator and the 
 *           type becomes updated in the ASTAttribute accordingly</li>
 *           <li>if the types do not match the requirements of the operator of formal procedure parameter,
 *            an error messages becomes emitted</li>
 *         </ul> 
 *       </li>
 *       <li>for arrays the list of indices are checked to be compatible with type FIXED</li>
 *       <li>for procedure parameters, they are checked to be compatible with the formal parameters</li>
 *     </ol>
 *     
 *     Other type checks are performed in the semantic analysis
 *     <table border=2>
 *     <tr><td>SWITCH,IF</td><td>{@link org.smallpearl.compiler.SemanticAnalysis.CheckSwitchCase}</td></tr>
 *     <tr><td>assignment</td><td>{@link org.smallpearl.compiler.SemanticAnalysis.CheckAssignment}</td></tr>
 *     <tr><td>i/o statements</td><td>{@link org.smallpearl.compiler.SemanticAnalysis.CheckIOStatements}</td></tr>
 *     <tr><td>real-time statements</td><td>{@link org.smallpearl.compiler.SemanticAnalysis.CheckRealTimeStatements}</td></tr>

 *     </table>  
 *     <ul>
 *     <p>For an overview of the execution of the various visitors see {@link
 *     org.openpearl.compiler.SymbolTableVisitor}.
 */
public class ExpressionTypeVisitor extends OpenPearlBaseVisitor<Void>
implements OpenPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private org.openpearl.compiler.SymbolTable.SymbolTable m_symboltable;
    private org.openpearl.compiler.SymbolTable.SymbolTable m_currentSymbolTable;
    private org.openpearl.compiler.SymbolTable.ModuleEntry m_module;
    private ConstantPool m_constantPool;
    private Integer m_currFixedLength = null;
    private boolean m_calculateRealFixedLength;
    private org.openpearl.compiler.AST m_ast;
    private TypeDefinition m_type = null;


    public ExpressionTypeVisitor(
            int verbose,
            boolean debug,
            SymbolTableVisitor symbolTableVisitor,
            ConstantPool constantPool,
            org.openpearl.compiler.AST ast) {

        m_verbose = verbose;
        m_debug = debug;

        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        this.m_constantPool = constantPool;
        m_ast = ast;
        m_type = null;

        Log.info("Semantic Check: Attributing parse tree with expression type information");

        LinkedList<ModuleEntry> listOfModules = this.m_symboltable.getModules();

        if (listOfModules.size() > 1) {
            throw new NotYetImplementedException("Multiple modules", 0, 0);
        }

        m_module = listOfModules.get(0);
        m_currentSymbolTable = m_module.scope;
        m_calculateRealFixedLength = false;
    }


    @Override
    public Void visitBaseExpression(OpenPearlParser.BaseExpressionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitBaseExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.primaryExpression() != null) {
            visitPrimaryExpression(ctx.primaryExpression());
            ASTAttribute expressionResult = m_ast.lookup(ctx.primaryExpression());
            if (expressionResult != null) {
                m_ast.put(ctx, expressionResult);

                Log.debug(
                        "ExpressionTypeVisitor: visitBaseExpression: exp="
                                + ctx.primaryExpression().getText());
                Log.debug(
                        "ExpressionTypeVisitor: visitBaseExpression: res=("
                                + expressionResult
                                + ")");
            }
        }

        return null;
    }

    @Override
    public Void visitPrimaryExpression(OpenPearlParser.PrimaryExpressionContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitPrimaryExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.constant() != null) {
            visitConstant(ctx.constant());
            ASTAttribute expressionResult = m_ast.lookup(ctx.constant());
            if (expressionResult != null) {
                m_ast.put(ctx, expressionResult);
            } else {
                ErrorStack.addInternal(ctx, "literal", "no AST attribute found");
            }
        } else if (ctx.name() != null) {
            visitName(ctx.name());
            // visitName returns in m_type the type of the complete name -element
            // the attribute is set already in visitName for the given context
            //  or error messages were emitted
            // now: just copy the result type
            ASTAttribute expressionResult = m_ast.lookup(ctx.name());
            m_ast.put(ctx, expressionResult);

        } else if (ctx.semaTry() != null) {
            visit(ctx.semaTry());
            ASTAttribute expressionResult = m_ast.lookup(ctx.semaTry());
            if (expressionResult != null) {
                m_ast.put(ctx, expressionResult);
            } else {
                ErrorStack.addInternal(ctx, "TRY", "no AST attribute found");
            }

        } else if (ctx.stringSelection() != null) {
            visitStringSelection(ctx.stringSelection());
            ASTAttribute expressionResult = m_ast.lookup(ctx.stringSelection());
            if (expressionResult != null) {
                m_ast.put(ctx, expressionResult);
            }
        } else if (ctx.expression() != null) {
            if (ctx.expression() != null) {
                visit(ctx.expression());

                ASTAttribute expressionResult = m_ast.lookup(ctx.expression());
                if (expressionResult != null) {
                    m_ast.put(ctx, expressionResult);
                }
            }
        }

        return null;
    }

    @Override
    public Void visitTaskFunction(OpenPearlParser.TaskFunctionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitTaskFunction");
        Log.debug("ExpressionTypeVisitor:visitTaskFunction:ctx" + CommonUtils.printContext(ctx));
        SymbolTableEntry se = null;
        visitChildren(ctx);
        TypeTask type = new TypeTask();

        if (ctx.expression() != null) {
            ASTAttribute tsk = m_ast.lookup(ctx.expression());
            TypeDefinition t = tsk.getType();
            se = tsk.getSymbolTableEntry();
            if (t instanceof TypeReference) {
                t = ((TypeReference)t).getBaseType();
            }
            if (!(t instanceof TypeTask)) {
                ErrorStack.add(
                        ctx.expression(),
                        "TASK",
                        "need TASK -- got " + tsk.getType().toString4IMC(false));
            }
        }
        ASTAttribute expressionResult = new ASTAttribute(type, se);
        m_ast.put(ctx, expressionResult);

        return null;
    }

    @Override
    public Void visitPrioFunction(OpenPearlParser.PrioFunctionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitPrioFunction");
        Log.debug("ExpressionTypeVisitor:visitPrioFunction:ctx" + CommonUtils.printContext(ctx));
        SymbolTableEntry se = null;
        visitChildren(ctx);
        TypeFixed type = new TypeFixed(15);

        if (ctx.expression() != null) {
            ASTAttribute tsk = m_ast.lookup(ctx.expression());
            TypeDefinition t = tsk.getType();
            if (t instanceof TypeReference) {
                t = ((TypeReference)t).getBaseType();
                tsk.setType(t);
            }
            se = tsk.getSymbolTableEntry();
            if (!(t instanceof TypeTask)) {
                ErrorStack.add(
                        ctx.expression(),
                        "PRIO",
                        "requires TASK -- got " + tsk.getType().toString4IMC(false));
            }
        }
        ASTAttribute expressionResult = new ASTAttribute(type, se);
        m_ast.put(ctx, expressionResult);

        return null;
    }

    private ASTAttribute treatFixedFloatDyadic(
            ParserRuleContext ctx1,
            ParserRuleContext ctx2,
            TypeDefinition type1,
            TypeDefinition type2,
            boolean resIsFloat,
            Boolean isConstant,
            ASTAttribute op1,
            ASTAttribute op2,
            String operator) {

        Log.debug("ExpressionTypeVisitor:treatFixedFloatDyadic");

        ASTAttribute res = null;

        TypeDefinition initialType1 = null;
        TypeDefinition initialType2 = null;

        if (type1 instanceof UserDefinedSimpleType) {
            initialType1 = type1;
            type1 = ((UserDefinedSimpleType)type1).getSimpleType();
        }

        if (type2 instanceof UserDefinedSimpleType) {
            initialType2 = type2;
            type2 = ((UserDefinedSimpleType)type2).getSimpleType();
        }

        if (isVoidProc(ctx1, ctx2)) {
            if (type1 == null && type2 != null) {
                res = op2;
            } else if (type1 != null && type2 == null) {
                res = op1;
            } else {
                res = op1;
            }
            return res;
        }


        int precision = Math.max(type1.getPrecision(), type2.getPrecision());
        boolean type1IsLarger = type1.getPrecision() > type2.getPrecision();

        // If the result type is FLOAT, the precision must be FLOAT_SHORT_PRECISION or
        // FLOAT_LONG_PRECISION
        if (resIsFloat) {
            if (precision <= Defaults.FLOAT_SHORT_PRECISION) {
                precision = Defaults.FLOAT_SHORT_PRECISION;
            } else if (precision <= Defaults.FLOAT_LONG_PRECISION) {
                precision = Defaults.FLOAT_LONG_PRECISION;
            } else {
                ErrorStack.add(
                        "type mismatch: "
                                + op1.getType().toString()
                                + operator
                                + op2.getType()
                                + " not possible");
            }
        }

        if (type1 instanceof TypeFixed && type2 instanceof TypeFixed) {
            Log.debug("ExpressionTypeVisitor: AdditiveExpression: rule#1");
            if (resIsFloat) res = new ASTAttribute(new TypeFloat(precision), isConstant);
            else {
                if (type1IsLarger && initialType1 != null) {
                    res = new ASTAttribute(initialType1, isConstant);
                } else if (initialType2 != null && !type1IsLarger) {
                    res = new ASTAttribute(initialType2, isConstant);
                } else {
                    res = new ASTAttribute(new TypeFixed(precision), isConstant);
                }
            }
        } else if (type1 instanceof TypeFixed && type2 instanceof TypeFloat) {
            Log.debug("ExpressionTypeVisitor: AdditiveExpression: rule#2");
            if (initialType2 != null && precision == type2.getPrecision()) {
                res = new ASTAttribute(initialType2, isConstant);
            } else {
                res = new ASTAttribute(new TypeFloat(precision), isConstant);
            }
        } else if (type1 instanceof TypeFloat && type2 instanceof TypeFixed) {
            Log.debug("ExpressionTypeVisitor: AdditiveExpression: rule#3");
            res = new ASTAttribute(new TypeFloat(precision), isConstant);
            if (initialType1 != null && precision == type1.getPrecision()) {
                res = new ASTAttribute(initialType1, isConstant);
            } else {
                res = new ASTAttribute(new TypeFloat(precision), isConstant);
            }
        } else if (type1 instanceof TypeFloat && type2 instanceof TypeFloat) {
            Log.debug("ExpressionTypeVisitor: AdditiveExpression: rule#4");
            if (initialType1 != null && precision == type1.getPrecision()) {
                res = new ASTAttribute(initialType1, isConstant);
            } else if (initialType2 != null && precision == type2.getPrecision()) {
                res = new ASTAttribute(initialType2, isConstant);
            } else {
                res = new ASTAttribute(new TypeFloat(precision), isConstant);
            }
            res = new ASTAttribute(new TypeFloat(precision), isConstant);
        } else {
            ErrorStack.add(
                    "type mismatch: "
                            + op1.getType().toErrorString()+" "
                            + operator+" "
                            + op2.getType().toErrorString()
                            + " not possible");
            // simulate result type as type of lhs? or return null
            // res = op1; -- let's return null
        }

        return res;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //
    // -----------+-----------+-----------+-------------+---------------------------------
    // Expression | Type of   | Type of   | Result type | Meaning of operation
    //            | operand 1 | operand 2 |             |
    // -----------+-----------+-----------+-------------+---------------------------------
    // op1 + op2  | FIXED(g1) | FIXED(g2) | FIXED(g3)   | addition of the values of
    //            | FIXED(g1) | FLOAT(g2) | FLOAT(g3)   | the operands op1 and op2
    //            | FLOAT(g1) | FIXED(g2) | FLOAT(g3)   |
    //            | FLOAT(g1) | FLOAT(g2) | FLOAT(g3)   | g3 = max (g1, g2)
    //            | DURATION  | DURATION  | DURATION    |
    //            | DURATION  | CLOCK     | CLOCK       |
    //            | CLOCK     | DURATION  | CLOCK       |

    @Override
    public Void visitAdditiveExpression(OpenPearlParser.AdditiveExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res = null;

        Log.debug("ExpressionTypeVisitor:visitAdditiveExpression");
        Log.debug(
                "ExpressionTypeVisitor:visitAdditiveExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));


        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "add",
                        "no AST attribute found for lhs of operation +");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "add",
                        "no AST attribute found for rhs of operation +");

        if (op1 != null && op2 != null) {
            TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            Boolean isConstant = op1.isConstant() && op2.isConstant();

            ErrorStack.enter(ctx,"+");

            if (type1 instanceof TypeDuration && type2 instanceof TypeDuration) {
                res = new ASTAttribute(new TypeDuration(), isConstant);

                Log.debug("ExpressionTypeVisitor: AdditiveExpression: rule#5");
            } else if (type1 instanceof TypeDuration && type2 instanceof TypeClock) {
                res = new ASTAttribute(new TypeClock(), isConstant);

                Log.debug("ExpressionTypeVisitor: AdditiveExpression: rule#6");
            } else if (type1 instanceof TypeClock && type2 instanceof TypeDuration) {
                res = new ASTAttribute(new TypeClock(), isConstant);

                Log.debug("ExpressionTypeVisitor: AdditiveExpression: rule#7");
            } else {
                res = treatFixedFloatDyadic(ctx.expression(0), ctx.expression(1), type1, type2, false, isConstant, op1, op2, "+");
            }
            ErrorStack.leave();
        }
        m_ast.put(ctx, res);

        return null;
    }
    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //
    // -----------+-----------+-----------+-------------+---------------------------------
    // Expression | Type of   | Type of   | Result type | Meaning of operation
    //            | operand 1 | operand 2 |             |
    // -----------+-----------+-----------+-------------+---------------------------------
    // op1 - op2  | FIXED(g1) | FIXED(g2) | FIXED(g3)   | subtraction of the values of
    //            | FIXED(g1) | FLOAT(g2) | FLOAT(g3)   | the operands op1 and op2
    //            | FLOAT(g1) | FIXED(g2) | FLOAT(g3)   |
    //            | FLOAT(g1) | FLOAT(g2) | FLOAT(g3)   | g3 = max (g1, g2)
    //            | DURATION  | DURATION  | DURATION    |
    //            | CLOCK     | DURATION  | CLOCK       |
    //            | CLOCK     | CLOCK     | DURATION    |



    @Override
    public Void visitSubtractiveExpression(OpenPearlParser.SubtractiveExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res = null;

        Log.debug("ExpressionTypeVisitor:visitSubtractiveExpression");
        Log.debug(
                "ExpressionTypeVisitor:visitSubtractiveExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "sub",
                        "no AST attribute found for lhs of operation -");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "sub",
                        "no AST attribute found for rhs of operation -");

        if (op1 != null && op2 != null) {
            TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            Boolean isConstant = op1.isConstant() && op2.isConstant();

            ErrorStack.enter(ctx,"-");

            if (type1 instanceof TypeDuration && type2 instanceof TypeDuration) {
                res = new ASTAttribute(new TypeDuration(), isConstant);

                Log.debug("ExpressionTypeVisitor: SubtractiveExpression: rule#5");
            } else if (type1 instanceof TypeClock && type2 instanceof TypeDuration) {
                res = new ASTAttribute(new TypeClock(), isConstant);

                Log.debug("ExpressionTypeVisitor: SubtractiveExpression: rule#6");
            } else if (type1 instanceof TypeClock && type2 instanceof TypeClock) {
                res = new ASTAttribute(new TypeDuration(), isConstant);

                Log.debug("ExpressionTypeVisitor: SubtractiveExpression: rule#7");
            } else {
                res = treatFixedFloatDyadic(ctx.expression(0), ctx.expression(1), type1, type2, false, isConstant, op1, op2, "-");
            }
            ErrorStack.leave();
        }
        m_ast.put(ctx, res);

        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Monadic operators for numerical, temporal
    //            and bit values
    // -----------+-----------+-----------+-------------+---------------------------------
    // Expression | Type of   |           | Result type | Meaning of operation
    //            | operand   |           |             |
    // -----------+-----------+-----------+-------------+---------------------------------
    // + op       | FIXED(g)  |           | FIXED(g)    | identity
    //            | FLOAT(g)  |           | FLOAT(g)    |
    //            | DURATION  |           | DURATION    |

    @Override
    public Void visitUnaryAdditiveExpression(OpenPearlParser.UnaryAdditiveExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitUnaryAdditiveExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression());

        ErrorStack.enter(ctx, "unary +");
        op = m_ast.lookup(ctx.expression());
        TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
        res = enshureFixedFloatDuration(type1, ctx.expression(), "UnaryAdditive");
        m_ast.put(ctx, res);

        ErrorStack.leave();

        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Monadic operators for numerical, temporal
    //            and bit values
    // -----------+-----------+-----------+-------------+---------------------------------
    // Expression | Type of   |           | Result type | Meaning of operation
    //            | operand   |           |             |
    // -----------+-----------+-----------+-------------+---------------------------------
    // - op       | FIXED(g)  |           | FIXED(g)    | changing the sign of op
    //            | FLOAT(g)  |           | FLOAT(g)    |
    //            | DURATION  |           | DURATION    |

    @Override
    public Void visitUnarySubtractiveExpression(
            OpenPearlParser.UnarySubtractiveExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitUnarySubtractiveExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression());

        ErrorStack.enter(ctx, "unary -");
        op = m_ast.lookup(ctx.expression());
        TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
        res = enshureFixedFloatDuration(type1, ctx.expression(), "UnarySubstractive");
        m_ast.put(ctx, res);

        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Monadic operators for numerical, temporal
    //           and bit values
    // -----------+-----------+-----------+-------------+---------------------------------
    // Expression | Type of   |           | Result type | Meaning of operation
    //            | operand   |           |             |
    // -----------+-----------+-----------+-------------+---------------------------------
    // NOT op     | BIT(lg)   |           | BIT(lg)     | inverting all bit positions of op

    @Override
    public Void visitNotExpression(OpenPearlParser.NotExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitNotExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx, "NOT");

        if (op == null) {
            // throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
            // ctx.start.getCharPositionInLine());
            ErrorStack.addInternal("no AST attribute found");
        } else {
            TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op);

            if (type1 instanceof TypeBit) {
                res =
                        new ASTAttribute(
                                new TypeBit(((TypeBit) op.getType()).getPrecision()),
                                op.isConstant());
                m_ast.put(ctx, res);

                if (m_debug) System.out.println("ExpressionTypeVisitor: NotExpression: rule#1");
            } else {
                if (isVoidProc(ctx.expression())) {
                    // do nothing, error message already emitted
                } else {
                    ErrorStack.add("expected type BIT -- got type " + op.getType().toString());
                }
                // set default result type for easy continuation
                res = new ASTAttribute( new TypeBit(1), op.isConstant());
                m_ast.put(ctx, res);

            }
        }

        ErrorStack.leave();

        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Monadic operators for numerical, temporal
    //           and bit values
    // -----------+-----------+-----------+-------------+---------------------------------
    // Expression | Type of   |           | Result type | Meaning of operation
    //            | operand   |           |             |
    // -----------+-----------+-----------+-------------+---------------------------------
    // ABS op     | FIXED(g)  |           | FIXED(g)    | absolute value of op
    //            | FLOAT(g)  |           | FLOAT(g)    |
    //            | DURATION  |           | DURATION    |

    @Override
    public Void visitAbsExpression(OpenPearlParser.AbsExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitAbsExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());

        ErrorStack.enter(ctx, "ABS");

        op = m_ast.lookup(ctx.expression());
        TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
        res = enshureFixedFloatDuration(type1, ctx.expression(), "Abs");
        m_ast.put(ctx, res);
        ErrorStack.leave();
        return null;
    }

    /** check if the operand is a proc returning nothing
     * emit an error message if the operand is an pure PROC returning nothing 
     */
    private boolean isVoidProc(ParserRuleContext ctx) {
        ASTAttribute attr = m_ast.lookup(ctx);
        if (attr == null) {
            ErrorStack.addInternal(ctx, null, "no AST attribute found");
            return false;
        } else {
            if (attr.m_type == null && attr.getSymbolTableEntry() != null) {
                CommonErrorMessages.noVoidProcAllowed(ctx);
                return true;
            }
        }
        return false;
    }

    private boolean isVoidProc(ParserRuleContext ctx1, ParserRuleContext ctx2) {
        boolean voidUsed = isVoidProc(ctx1);
        voidUsed |= isVoidProc(ctx2);
        return voidUsed;
    }

    /**
     * check types for operations monadic +, moadic -, ABS and SIGN
     *
     * @param op the ASTAttribute of the current operand
     * @param operation string with the operation like 'UnaryAdditive' or 'Abs' for the error
     *     messages
     * @return an ASTAttribute with the same type if the type in op is FIXED,FLOAT or DURATION or
     *     null, if a different type is detected
     */
    private ASTAttribute enshureFixedFloatDuration(TypeDefinition type, ParserRuleContext ctx, String operation) {
        ASTAttribute res=null;

        ASTAttribute op = m_ast.lookup(ctx);
        if (op == null) {
            ErrorStack.addInternal("no AST attribute found for " + operation);
        }
        if (isVoidProc(ctx)) return res;

        if (type instanceof TypeFixed) {
            res =
                    new ASTAttribute(
                            new TypeFixed(((TypeFixed) op.getType()).getPrecision()),
                            op.isConstant());

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: " + operation + "Expression: rule#1");
        } else if (op.getType() instanceof TypeFloat) {
            res =
                    new ASTAttribute(
                            new TypeFloat(((TypeFloat) op.getType()).getPrecision()),
                            op.isConstant());

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: " + operation + "Expression: rule#2");
        } else if (op.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration());

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: " + operation + "Expression: rule#3");
        } else {

            ErrorStack.add("type '" + op.getType().getName() + "' not allowed");
            res = null;
        }

        return res;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Monadic operators for numerical, temporal
    //           and bit values
    // -----------+-----------+-----------+-------------+---------------------------------
    // Expression | Type of   |           | Result type | Meaning of operation
    //            | operand   |           |             |
    // -----------+-----------+-----------+-------------+---------------------------------
    // SIGN op    | FIXED(g)  |           | FIXED(1)    | determining the sign of op
    //            | FLOAT(g)  |           |             |  1 for op > 0
    //            | DURATION  |           |             |  0 for op = 0
    //            |           |           |             | -1 for op < 0

    @Override
    public Void visitSignExpression(OpenPearlParser.SignExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitSignsExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());

        op = m_ast.lookup(ctx.expression());
        TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
        ErrorStack.enter(ctx, "SIGN");

        // let's use the checking of enshureFixedFloatDuration and replace
        // the result if the type was ok
        res = enshureFixedFloatDuration(type1, ctx.expression(), "Sign");
        if (res != null) {
            // replace with FIXED(1)
            res = new ASTAttribute(new TypeFixed(1));
        }
        m_ast.put(ctx, res);
        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //
    // -----------+-----------+-----------+-------------+---------------------------------
    // Expression | Type of   | Type of   | Result type | Meaning of operation
    //            | operand 1 | operand 2 |             |
    // -----------+-----------+-----------+-------------+---------------------------------
    // op1 * op2  | FIXED(g1) | FIXED(g2) | FIXED(g3)   | multiplication of the values of
    //            | FIXED(g1) | FLOAT(g2) | FLOAT(g3)   | the operands op1 and op2
    //            | FLOAT(g1) | FIXED(g2) | FLOAT(g3)   |
    //            | FLOAT(g1) | FLOAT(g2) | FLOAT(g3)   | g3 = max (g1, g2)
    //            | FIXED(g1) | DURATION  | DURATION    |
    //            | DURATION  | FIXED(g2) | DURATION    |
    //            | FLOAT(g1) | DURATION  | DURATION    |
    //            | DURATION  | FLOAT(g2) | DURATION    |
    // -----------+-----------+-----------+-------------+---------------------------------

    @Override
    public Void visitMultiplicativeExpression(
            OpenPearlParser.MultiplicativeExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitMultiplicativeExpression:ctx"
                        + CommonUtils.printContext(ctx));
        Log.debug(
                "ExpressionTypeVisitor:visitMultiplicativeExpression:ctx.expression(0)"
                        + CommonUtils.printContext(ctx.expression(0)));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "mult",
                        "no AST attribute found for lhs of operation *");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "mult",
                        "no AST attribute found for rhs of operation *");

        ErrorStack.enter(ctx,"*");

        if (op1 != null && op2 != null) {
            Boolean isConstant = op1.isConstant() && op2.isConstant();

            // implicit dereferences
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);


            if (type1 instanceof TypeFixed && type2 instanceof TypeDuration) {
                res = new ASTAttribute(new TypeDuration(), isConstant);
                Log.debug("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#6");
            } else if (type1 instanceof TypeDuration && type2 instanceof TypeFixed) {
                res = new ASTAttribute(new TypeDuration(), isConstant);
                Log.debug("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#6");
            } else if (type1 instanceof TypeFloat && type2 instanceof TypeDuration) {
                res = new ASTAttribute(new TypeDuration(), isConstant);
                Log.debug("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#7");
            } else if (type1 instanceof TypeDuration && type2 instanceof TypeFloat) {
                res = new ASTAttribute(new TypeDuration(), isConstant);
            } else {
                res =
                        treatFixedFloatDyadic(ctx.expression(0), ctx.expression(1), 
                                type1, op2.getType(), false, isConstant, op1, op2, "*");
            }

            m_ast.put(ctx, res);
        }
        ErrorStack.leave();

        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //
    // -------------+-----------+-----------+-------------+---------------------------------
    // Expression   | Type of   | Type of   | Result type | Meaning of operation
    //              | operand 1 | operand 2 |             |
    // -------------+-----------+-----------+-------------+---------------------------------
    // op1 / op2    | FIXED(g1) | FIXED(g2) | FLOAT(g3)   | division of the values of
    //              | FLOAT(g1) | FIXED(g2) | FLOAT(g3)   | the operands op1 and op2,
    //              | FIXED(g1) | FLOAT(g2) | FLOAT(g3)   | if op2 <> 0
    //              | FLOAT(g1) | FLOAT(g2) | FLOAT(g3)   |
    //              | DURATION  | FIXED(g2) | DURATION    | g3 = max (g1, g2)
    //              | DURATION  | FLOAT(g2) | DURATION    | g4 = 31
    //              | DURATION  | DURATION  | FLOAT(g4)   | (dependent on implementation)

    @Override
    public Void visitDivideExpression(OpenPearlParser.DivideExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitDivideExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "/",
                        "no AST attribute found for lhs of operation /");

        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "/",
                        "no AST attribute found for rhs of operation /");

        ErrorStack.enter(ctx,"/");
        if (op1 != null && op2 != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            Boolean isConstant = op1.isConstant() && op2.isConstant();

            if (type1 instanceof TypeFloat && type2 instanceof TypeDuration) {
                res = new ASTAttribute(new TypeDuration(), isConstant);

                Log.debug("ExpressionTypeVisitor: DivideExpression: rule#6");
            } else if (type1 instanceof TypeDuration && type2 instanceof TypeFloat) {
                res = new ASTAttribute(new TypeDuration(), isConstant);

                Log.debug("ExpressionTypeVisitor: DivideExpression: rule#7");
            } else if (type1 instanceof TypeDuration && type2 instanceof TypeFixed) {
                res = new ASTAttribute(new TypeDuration(), isConstant);

                Log.debug("ExpressionTypeVisitor: DivideExpression: rule#6");
            } else if (type1 instanceof TypeDuration && type2 instanceof TypeDuration) {
                res = new ASTAttribute(new TypeFloat(23), isConstant);

                Log.debug("ExpressionTypeVisitor: DivideExpression: rule#7");
            } else {
                res =
                        treatFixedFloatDyadic(ctx.expression(0), ctx.expression(1), 
                                op1.getType(), op2.getType(), true, isConstant, op1, op2, "/");
            }

            m_ast.put(ctx, res);
        }

        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //
    // -------------+-----------+-----------+-------------+---------------------------------
    // Expression   | Type of   | Type of   | Result type | Meaning of operation
    //              | operand 1 | operand 2 |             |
    // -------------+-----------+-----------+-------------+---------------------------------
    // op1 // op2   | FIXED(g1) | FIXED(g2) | FIXED(g3)   | integer division of the values of
    //              |           |           |             | the operands op1 and op2
    //              |           |           |             | g3 = max (g1, g2)

    @Override
    public Void visitDivideIntegerExpression(OpenPearlParser.DivideIntegerExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitDivideIntegerExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        ErrorStack.enter(ctx, "//");

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "//",
                        "no AST attribute found for lhs of operation //");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "//",
                        "no AST attribute found for rhs of operation //");

        if (op1 != null && op2 != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            Boolean isConstant = op1.isConstant() && op2.isConstant();

            if (type1 instanceof TypeFixed && type2 instanceof TypeFixed) {
                Integer precision = Math.max(type1.getPrecision(), type2.getPrecision());
                res = new ASTAttribute(new TypeFixed(precision), isConstant);
                m_ast.put(ctx, res);

                Log.debug("ExpressionTypeVisitor: DivideIntegerExpression: rule#1");
            } else if ( isVoidProc(ctx.expression(0),ctx.expression(1))) {
                if (type1 == null && type2 != null) {
                    res = op2;
                } else if (type1 != null && type2 == null) {
                    res = op1;
                } else {
                    res = op1;
                }
                m_ast.put(ctx, res);
            } else {
                ErrorStack.add(
                        "type mismatch: expected FIXED // FIXED -- got "
                                + op1.getType().toString()
                                + "//"
                                + op2.getType().toString());
            }

        }

        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //
    // -------------+-----------+-----------+-------------+---------------------------------
    // Expression   | Type of   | Type of   | Result type | Meaning of operation
    //              | operand 1 | operand 2 |             |
    // -------------+-----------+-----------+-------------+---------------------------------
    // op1 REM op2  | FIXED(g1) | FIXED(g2) | FIXED(g2)   | remainder of the integer division
    //              | FLOAT(g1) | FLOAT(g2) | FLOAT(g2)   | of the values of the operands op1
    //              |           |           |             | and op2
    // -------------+-----------+-----------+-------------+---------------------------------

    @Override
    public Void visitRemainderExpression(OpenPearlParser.RemainderExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitRemainderExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        ErrorStack.enter(ctx, "REM");

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "REM",
                        "no AST attribute found for lhs of operation REM");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "REM",
                        "no AST attribute found for rhs of operation REM");

        if (op1 != null && op2 != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            Boolean isConstant = op1.isConstant() && op2.isConstant();

            if (type1 instanceof TypeFixed && type2 instanceof TypeFixed) {
                Integer precision = Math.max(type1.getPrecision(), type2.getPrecision());
                res = new ASTAttribute(new TypeFixed(precision), isConstant);
                m_ast.put(ctx, res);

                Log.debug("ExpressionTypeVisitor: visitRemainderExpression: rule#1");
            } else {
                if ( isVoidProc(ctx.expression(0), ctx.expression(1))) {
                    if (type1 == null && type2 != null) {
                        res = op2;
                    } else if (type1 != null && type2 == null) {
                        res = op1;
                    } else {
                        res = op1;
                    }
                    m_ast.put(ctx, res);
                } else {
                    ErrorStack.add(

                            "type mismatch: expected FIXED REM FIXED -- got "
                                    + op1.getType().toString()
                                    + "//"
                                    + op2.getType().toString());
                }
            }
        }
        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //
    // -------------+-----------+-----------+-------------+---------------------------------
    // Expression   | Type of   | Type of   | Result type | Meaning of operation
    //              | operand 1 | operand 2 |             |
    // -------------+-----------+-----------+-------------+---------------------------------
    // op1 ** op2   | FIXED(g1) | FIXED(g2) | FIXED(g2)   | exponentiation of the values
    //              | FLOAT(g1) | FLOAT(g2) | FLOAT(g2)   | of the operands op1 and op2
    // -------------+-----------+-----------+-------------+---------------------------------

    @Override
    public Void visitExponentiationExpression(
            OpenPearlParser.ExponentiationExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitExponentiationExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        ErrorStack.enter(ctx, "**");

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "**",
                        "no AST attribute found for lhs of operation **");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "**",
                        "no AST attribute found for rhs of operation **");

        if (op1 != null && op2 != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            Boolean isConstant = op1.isConstant() && op2.isConstant();

            if (type1 instanceof TypeFixed && type2 instanceof TypeFixed) {
                Integer precision = type1.getPrecision();
                res = new ASTAttribute(new TypeFixed(precision), isConstant);
                m_ast.put(ctx, res);

                Log.debug("ExpressionTypeVisitor: ExponentiationExpression: rule#1");
            } else if (type1 instanceof TypeFloat && type2 instanceof TypeFixed) {
                res =
                        new ASTAttribute(
                                new TypeFloat(((TypeFloat) op1.getType()).getPrecision()),
                                isConstant);
                m_ast.put(ctx, res);

                Log.debug("ExpressionTypeVisitor: ExponentiationExpression: rule#1");
            } else if ( isVoidProc(ctx.expression(0), ctx.expression(1)) ) {

                if (type1 == null && type2 != null) {
                    res = op2;
                } else if (type1 != null && type2 == null) {
                    res = op1;
                } else {
                    res = op1;
                }
                m_ast.put(ctx, res);
            } else {
                ErrorStack.add(
                        "type mismatch: expected FIXED ** FIXED or FLOAT ** FLOAT -- got "
                                + op1.getType().toString()
                                + "//"
                                + op2.getType().toString());
            }
        }
        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //
    // -------------+-----------+-----------+-------------+---------------------------------
    // Expression   | Type of   | Type of   | Result type | Meaning of operation
    //              | operand 1 | operand 2 |             |
    // -------------+-----------+-----------+-------------+---------------------------------
    // op1 FIT op2  | FIXED(g1) | FIXED(g2) | FIXED(g2)   | changing the precision of
    //              | FLOAT(g1) | FLOAT(g2) | FLOAT(g2)   | operand op1 into the precision
    //              |           |           |             | of operand op2
    // -------------+-----------+-----------+-------------+---------------------------------

    @Override
    public Void visitFitExpression(OpenPearlParser.FitExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitFitExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        ErrorStack.enter(ctx, "FIT");

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "FIT",
                        "no AST attribute found for lhs of operation FIT");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "FIT",
                        "no AST attribute found for rhs of operation FIT");

        if (op1 != null && op2 != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            Boolean isConstant = op1.isConstant() && op2.isConstant();


            if (type1 instanceof TypeFixed && type2 instanceof TypeFixed) {
                Integer precision = ((TypeFixed) op2.getType()).getPrecision();
                res = new ASTAttribute(new TypeFixed(precision), isConstant);
                m_ast.put(ctx, res);

                Log.debug("ExpressionTypeVisitor: FitExpression: rule#1");
            } else if (type1 instanceof TypeFloat && type2 instanceof TypeFloat) {
                Integer precision = ((TypeFloat) op2.getType()).getPrecision();
                res = new ASTAttribute(new TypeFloat(precision), isConstant);
                m_ast.put(ctx, res);

                Log.debug("ExpressionTypeVisitor: FitExpression: rule#2");
            } else  if (isVoidProc(ctx.expression(0), ctx.expression(1))) {
                if (type1 == null && type2 != null) {
                    res = op2;
                } else if (type1 != null && type2 == null) {
                    res = op1;
                } else {
                    res = new ASTAttribute(new TypeFixed(Defaults.FIXED_LENGTH));
                }
                m_ast.put(ctx, res);

            } else {
                ErrorStack.add(
                        "type mismatch: expected FIXED FIT FIXED or FLOAT FIT FLOAT -- got "
                                + op1.getType().toString()
                                + " FIT "
                                + op2.getType().toString());
            }
        }
        ErrorStack.leave();

        return null;
    }

    // FIXED(g) is also possible for the monadic arithmetic operators
    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //            Table 6.3: Monadic arithmetical operators
    // -----------+-------------+-----------+-------------+---------------------------------
    // Expression | Type of     |           | Result type | Meaning of operation
    //            | operand     |           |             |
    // -----------+-------------+-----------+-------------+---------------------------------
    // SQRT op    | FLOAT(g)    |           | FLOAT(g)    | square root of operand
    // SIN op     | FIXED(g)    |           |             | sine of operand
    // COS op     |             |           |             | cosine of operand
    // EXP op     |             |           |             | e^op with e=2.718281828459
    // LN op      |             |           |             | natural loarithm of operand
    // TAN op     |             |           |             | tangent of operand
    // ATAN op    |             |           |             | arcus tangent of operand
    // TANH op    |             |           |             | tangent hyperbolicus of operand
    // -----------+-------------+-----------+-------------+---------------------------------

    @Override
    public Void visitSqrtExpression(OpenPearlParser.SqrtExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitSqrtExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = saveGetAttribute(ctx.expression(), ctx, "SQRT", "no AST attribute found for SQRT");

        setResultAttributeForMonadicArithmeticOperators(ctx.expression(), ctx, op, "SQRT");

        return null;
    }

    @Override
    public Void visitSinExpression(OpenPearlParser.SinExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitSinExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = saveGetAttribute(ctx.expression(), ctx, "SIN", "no AST attribute found for SIN");

        setResultAttributeForMonadicArithmeticOperators(ctx.expression(), ctx, op, "SIN");

        return null;
    }



    @Override
    public Void visitCosExpression(OpenPearlParser.CosExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitCosExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = saveGetAttribute(ctx.expression(), ctx, "COS", "no AST attribute found for COS");

        setResultAttributeForMonadicArithmeticOperators(ctx.expression(), ctx, op, "COS");

        return null;
    }

    @Override
    public Void visitExpExpression(OpenPearlParser.ExpExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitExpExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = saveGetAttribute(ctx.expression(), ctx, "EXP", "no AST attribute found for EXP");

        setResultAttributeForMonadicArithmeticOperators(ctx.expression(), ctx, op, "EXP");

        return null;
    }

    @Override
    public Void visitLnExpression(OpenPearlParser.LnExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitLnExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = saveGetAttribute(ctx.expression(), ctx, "LN", "no AST attribute found for LN");

        setResultAttributeForMonadicArithmeticOperators(ctx.expression(), ctx, op, "LN");

        return null;
    }

    @Override
    public Void visitTanExpression(OpenPearlParser.TanExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitTanExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = saveGetAttribute(ctx.expression(), ctx, "TAN", "no AST attribute found for TAN");

        setResultAttributeForMonadicArithmeticOperators(ctx.expression(), ctx, op, "TAN");

        return null;
    }

    @Override
    public Void visitAtanExpression(OpenPearlParser.AtanExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitAtanExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = saveGetAttribute(ctx.expression(), ctx, "ATAN", "no AST attribute found for ATAN");

        setResultAttributeForMonadicArithmeticOperators(ctx.expression(), ctx, op, "ATAN");

        return null;
    }

    @Override
    public Void visitTanhExpression(OpenPearlParser.TanhExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitTanhExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = saveGetAttribute(ctx.expression(), ctx, "TANH", "no AST attribute found for TANH");

        setResultAttributeForMonadicArithmeticOperators(ctx.expression(), ctx, op, "TANH");

        return null;
    }


    private Void setResultAttributeForMonadicArithmeticOperators(
            ExpressionContext ctxOperand, ParserRuleContext ctxResult, ASTAttribute op, String operator) {
        ASTAttribute res;

        ErrorStack.enter(ctxOperand, operator);

        if (op == null) {
            ErrorStack.addInternal("no AST attribute found for " + operator);
        } else {
            TypeDefinition type =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
            Boolean isConstant = op.isConstant();

            if (type instanceof TypeFloat) {
                res = new ASTAttribute(new TypeFloat(type.getPrecision()), isConstant);
                m_ast.put(ctxResult, res);
                Log.debug("ExpressionTypeVisitor: " + operator + "Expression: rule#2");
            } else if (type instanceof TypeFixed) {
                int precision = type.getPrecision();
                if (precision > Defaults.FLOAT_SHORT_PRECISION) {
                    precision = Defaults.FLOAT_LONG_PRECISION; // ???  FIXED_MAX_LENGTH;
                } else {
                    precision = Defaults.FLOAT_SHORT_PRECISION;
                }
                res = new ASTAttribute(new TypeFloat(precision), isConstant);
                m_ast.put(ctxResult, res);
            } else {
                if (isVoidProc(ctxOperand)) {

                } else {
                    ErrorStack.add(
                            "only FIXED and FLOAT are allowed -- got " + op.getType().toString());
                }
                res = new ASTAttribute(new TypeFloat(Defaults.FLOAT_PRECISION), isConstant);
                m_ast.put(ctxResult, res);
            }
        }
        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //            Table 6.2: Monadic operators for explicit type conversions
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      |           | Result type  | Meaning of operation
    //            | operand      |           |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    // TOFIXED op | CHARACTER(1) |           | FIXED(7)     | ASCII code for operand character
    //            | BIT(lg)      |           | FIXED(g)     | interpreting the bit pattern of the
    //            |              |           |              | operand as an integer, with g = lg
    // TOFLOAT op | FIXED(g)     |           | FLOAT(g)     | converting the operand into a
    //            |              |           |              | floating point number
    // TOBIT op   | FIXED(g)     |           | BIT(lg)      | interpreting the operand as bit
    //            |              |           |              | pattern, with lg = g
    // TOCHAR op  | FIXED        |           | CHARRATER(1) | character for the ASCII code of the
    //            |              |           |              | operand
    // ENTIER op  | FLOAT(g)     |           | FIXED(g)     | greatest integer less or equal than
    //            |              |           |              | the operand
    // ROUND op   | FLOAT(g)     |           | FIXED(g)     | next integer according to DIN

    @Override
    public Void visitTOFIXEDExpression(OpenPearlParser.TOFIXEDExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitTOFIXEDExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());

        ErrorStack.enter(ctx, "TOFIXED");

        op =
                saveGetAttribute(
                        ctx.expression(), ctx, "ToFIXED", "no AST attribute found for TOFIXED");

        if (op != null) {
            Boolean isConstant = op.isConstant();
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op);

            if (type1 instanceof TypeBit) {
                int prec = ((TypeBit) op.getType()).getPrecision();
                if ( prec > Defaults.FIXED_MAX_LENGTH) {
                    ErrorStack.add("result would by FIXED("+ prec +")");
                    prec = Defaults.FIXED_MAX_LENGTH; // easy solution for further type checks
                }
                res = new ASTAttribute(new TypeFixed(prec), isConstant);
                m_ast.put(ctx, res);

                Log.debug("ExpressionTypeVisitor: TOFIXED: rule#1");

            } else if (type1 instanceof TypeChar) {
                TypeChar typeChar;

                Log.debug("ExpressionTypeVisitor: TOFIXED: rule#2");

                typeChar = (TypeChar) type1;

                if (typeChar.getPrecision() != 1) {
                    ErrorStack.add("only single CHAR allowed");
                }

                res = new ASTAttribute(new TypeFixed(7));  
                m_ast.put(ctx, res);
            } else if (isVoidProc(ctx.expression())){
                // smooth continuation of compilation
                res = new ASTAttribute(new TypeFixed(1));  
                m_ast.put(ctx, res);
            } else {
                ErrorStack.add("only BIT and CHAR are allowed -- got " + op.getType().toString());
            }
        }

        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitTOFLOATExpression(OpenPearlParser.TOFLOATExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitTOFLOATExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());

        ErrorStack.enter(ctx, "TOFLOAT");
        op =
                saveGetAttribute(
                        ctx.expression(), ctx, "TOFLOAT", "no AST attribute found for TOFLOAT");

        if (op != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
            Boolean isConstant = op.isConstant();

            if (type1 instanceof TypeFixed) {
                TypeFixed fixedValue = (TypeFixed) type1;
                int precision = 0;

                if (fixedValue.getPrecision() <= Defaults.FLOAT_SHORT_PRECISION) {
                    precision = Defaults.FLOAT_SHORT_PRECISION;
                } else {
                    precision = Defaults.FLOAT_LONG_PRECISION;
                }

                res = new ASTAttribute(new TypeFloat(precision));
                m_ast.put(ctx, res);

                Log.debug("ExpressionTypeVisitor: TOFLOAT: rule#1");
            } else if (isVoidProc(ctx.expression())){
                // smooth continuation of compilation
                res = new ASTAttribute(new TypeFloat(Defaults.FLOAT_PRECISION));  
                m_ast.put(ctx, res);
            } else {
                ErrorStack.add("only type FIXED allowed -- got " + op.getType().toString());
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitTOBITExpression(OpenPearlParser.TOBITExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitTOBITExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());

        ErrorStack.enter(ctx, "TOBIT");
        op = saveGetAttribute(ctx.expression(), ctx, "TOBIT", "no AST attribute found for TOBIT");

        if (op != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
            Boolean isConstant = op.isConstant();

            if (type1 instanceof TypeFixed) {
                int prec = type1.getPrecision();
                if (type1.getPrecision() < Defaults.BIT_MIN_LENGTH) {
                    ErrorStack.add("result would be BIT("+prec+")");
                    // enshure at least BIT(1) for further checks
                    prec = Defaults.BIT_MIN_LENGTH;
                }
                res = new ASTAttribute(  new TypeBit(prec), isConstant);
                m_ast.put(ctx, res);
                Log.debug("ExpressionTypeVisitor: TOBIT: rule#1");
            } else if (isVoidProc(ctx.expression())){
                // smooth continuation of compilation
                res = new ASTAttribute(new TypeBit(Defaults.BIT_LENGTH));  
                m_ast.put(ctx, res);
            } else {
                ErrorStack.add("only type FIXED allowed -- got " + op.getType().toString());
            }
        }
        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //            Table 6.2: Monadic operators for explicit type conversions
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      |           | Result type  | Meaning of operation
    //            | operand      |           |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    // TOCHAR op  | FIXED        |           | CHARRATER(1) | character for the ASCII code of the
    //            |              |           |              | operand

    @Override
    public Void visitTOCHARExpression(OpenPearlParser.TOCHARExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitTOCHARExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = saveGetAttribute(ctx.expression(), ctx, "TOCHAR", "no AST attribute found for TOCHAR");
        ErrorStack.enter(ctx, "TOCHAR");
        if (op != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
            Boolean isConstant = op.isConstant();

            if (type1 instanceof TypeFixed) {
                res = new ASTAttribute(new TypeChar(1));
                m_ast.put(ctx, res);
            } else {
                if (isVoidProc(ctx.expression())) {
                    // smooth continuation of compilation
                    res = new ASTAttribute(new TypeChar(Defaults.CHARACTER_LENGTH));  
                    m_ast.put(ctx, res); 
                } else {
                    ErrorStack.add("only type FIXED allowed -- got " + op.getType().toString());
                }
                // throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(),
                // ctx.start.getCharPositionInLine());
            }
        }
        ErrorStack.leave();

        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //            Table 6.2: Monadic operators for explicit type conversions
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      |           | Result type  | Meaning of operation
    //            | operand      |           |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    // ENTIER op  | FLOAT(g)     |           | FIXED(g)     | greatest integer less or equal than

    @Override
    public Void visitEntierExpression(OpenPearlParser.EntierExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitEntierExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        ErrorStack.enter(ctx, "ENTIER");
        op = saveGetAttribute(ctx.expression(), ctx, "ENTIER", "no AST attribute found for ENTIER");

        if (op != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
            Boolean isConstant = op.isConstant();

            if (type1 instanceof TypeFloat) {
                res = new ASTAttribute(new TypeFixed(type1.getPrecision()), isConstant);
                m_ast.put(ctx, res);
                if (m_debug) System.out.println("ExpressionTypeVisitor: ENTIER: rule#1");
            } else {
                if (isVoidProc(ctx.expression())) {
                    // smooth continuation of compilation
                    res = new ASTAttribute(new TypeChar(Defaults.FIXED_LENGTH));  
                    m_ast.put(ctx, res); 
                } else {
                    ErrorStack.add("only type FLOAT allowed -- got " + op.getType().toString());
                }
            }
        }
        ErrorStack.leave();

        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //            Table 6.2: Monadic operators for explicit type conversions
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      |           | Result type  | Meaning of operation
    //            | operand      |           |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    // ROUND op   | FLOAT(g)     |           | FIXED(g)     | next integer according to DIN

    @Override
    public Void visitRoundExpression(OpenPearlParser.RoundExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitRoundExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        ErrorStack.enter(ctx, "ROUND");
        op = saveGetAttribute(ctx.expression(), ctx, "ROUND", "no AST attribute found for ROUND");

        if (op != null) {
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
            Boolean isConstant = op.isConstant();


            if (type1 instanceof TypeFloat) {
                res = new ASTAttribute(new TypeFixed(type1.getPrecision()), isConstant);
                m_ast.put(ctx, res);
                Log.debug("ExpressionTypeVisitor: ROUND: rule#1");
            } else {
                if (isVoidProc(ctx.expression())) {
                    // smooth continuation of compilation
                    res = new ASTAttribute(new TypeChar(Defaults.FIXED_LENGTH));  
                    m_ast.put(ctx, res); 
                } else {
                    ErrorStack.add("only type FLOAT allowed -- got " + op.getType().toString());
                }
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitUnaryExpression(OpenPearlParser.UnaryExpressionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitUnaryExpression:ctx" + CommonUtils.printContext(ctx));
        ErrorStack.addInternal(ctx, "unary expr", "not implemented!");
        return null;
        // throw new NotYetImplementedException("ExpressionTypeVisitor:visitUnaryExpression",
        // ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    //
    // Reference: OpenPEARL Language Report
    //            9.3.1 Semaphore Variables (SEMA) and Statements (REQUEST, RELEASE, TRY)
    // -------------+--------------+-----------+--------------+---------------------------------
    // Expression   |              |           | Result type  | Meaning of operation
    //              |              |           |              |
    // -------------+------------- +-----------+--------------+---------------------------------
    // TRY sema     | SEMAPHORE    |           | BIT(1)       | Obtains the state of a semaphore
    //              |              |           |              | variable

    @Override
    public Void visitSemaTry(OpenPearlParser.SemaTryContext ctx) {
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitSemaTry:ctx" + CommonUtils.printContext(ctx));


        // set AST attributes for the names. This makes is easier in the CppCodeGenerator
        visitChildren(ctx);

        for (int i=0; i<ctx.listOfNames().name().size(); i++) {
            ASTAttribute op = m_ast.lookup(ctx.listOfNames().name(i));
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
        }

        res = new ASTAttribute(new TypeBit(1));
        m_ast.put(ctx, res);



        if (m_debug) System.out.println("ExpressionTypeVisitor: TRY: rule#1");

        return null;
    }


    private ConstantCharacterValue getConstantStringLiteral(TerminalNode terminalNode) {
        String s = terminalNode.toString();

        ConstantCharacterValue result = new ConstantCharacterValue(s);

        return result;
    }


    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitModule:ctx" + CommonUtils.printContext(ctx));

        //        this.m_currentSymbolTable = this.symbolTable.newLevel(moduleEntry);
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitTaskDeclaration:ctx" + CommonUtils.printContext(ctx));

        SymbolTableEntry entry =
                this.m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());

        if (entry != null) {
            if (entry instanceof TaskEntry) {
                m_currentSymbolTable = ((TaskEntry) entry).scope;
                visitChildren(ctx);
                this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
            } else {
                ErrorStack.addInternal(
                        ctx,
                        "TASK",
                        "'" + ctx.nameOfModuleTaskProc().ID().getText() + "' is not of type TASK");
            }
        } else {
            ErrorStack.addInternal(
                    ctx,
                    "TASK",
                    "'" + ctx.nameOfModuleTaskProc().ID().getText() + "' is not in symbol table");
        }

        return null;
    }

    @Override
    public Void visitPositionSKIP(OpenPearlParser.PositionSKIPContext ctx) {
        if (ctx.expression() != null) {
            visit(ctx.expression());
            ASTAttribute a = m_ast.lookup(ctx.expression());
            TypeDefinition type1 =  TypeUtilities.performImplicitDereferenceAndFunctioncall(a);
        }
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitProcedureDeclaration:ctx"
                        + CommonUtils.printContext(ctx));

        SymbolTableEntry entry =
                this.m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());

        if (entry != null) {
            if (entry instanceof ProcedureEntry) {
                m_currentSymbolTable = ((ProcedureEntry) entry).scope;

                visitChildren(ctx);
                this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
            } else {
                ErrorStack.addInternal(
                        ctx,
                        "PROC",
                        "'" + ctx.nameOfModuleTaskProc().ID().getText() + "' is not of type PROC");
            }
        } else {
            ErrorStack.addInternal(
                    ctx,
                    "PROC",
                    "'" + ctx.nameOfModuleTaskProc().ID().getText() + "' is not in symbol table");
        }

        return null;
    }


    // obsolete: the result type is stored already in the symbol table    
    //    // set an AST attribute if we have a CHAR-type as result attribute
    //    // we need for all result types as AST Attribute
    //    @Override
    //    public Void visitResultAttribute(OpenPearlParser.ResultAttributeContext ctx) {
    //        if (ctx.resultType().simpleType() != null) {
    //            if (ctx.resultType().simpleType().typeCharacterString() != null) {
    //                int len =
    //                        Integer.parseInt(
    //                                ctx.resultType()
    //                                        .simpleType()
    //                                        .typeCharacterString()
    //                                        .length()
    //                                        .getText());
    //                m_ast.put(ctx, new ASTAttribute(new TypeChar(len)));
    //            }
    //        }
    //        return null;
    //    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitBlock_statement:ctx" + CommonUtils.printContext(ctx));

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitLoopStatement:ctx" + CommonUtils.printContext(ctx));

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);


        int precisionFor = m_currentSymbolTable.lookupDefaultFixedLength(); 

        if (ctx.loopStatement_from() != null) {
            visit(ctx.loopStatement_from());
            ASTAttribute attr = m_ast.lookup(ctx.loopStatement_from().expression());
            TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
            if (!(attr.getType() instanceof TypeFixed)) {
                ErrorStack.add(
                        ctx.loopStatement_from().expression(),
                        "FOR",
                        "type must be FIXED - but is " + attr.getType().toString());
            }
            precisionFor = Math.max(precisionFor, attr.getType().getPrecision());
        }
        if (ctx.loopStatement_to() != null) {
            visit(ctx.loopStatement_to());
            ASTAttribute attr = m_ast.lookup(ctx.loopStatement_to().expression());
            TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
            if (!(attr.getType() instanceof TypeFixed)) {
                ErrorStack.add(
                        ctx.loopStatement_to().expression(),
                        "TO",
                        "type must be FIXED - but is " + attr.getType().toString());
            }
            precisionFor = Math.max(precisionFor, attr.getType().getPrecision());
        } 

        if (ctx.loopStatement_by() != null) {
            visit(ctx.loopStatement_by());
            ASTAttribute attr = m_ast.lookup(ctx.loopStatement_by().expression());
            TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
            if (!(attr.getType() instanceof TypeFixed)) {
                ErrorStack.add(
                        ctx.loopStatement_by().expression(),
                        "BY",
                        "type must be FIXED - but is " + attr.getType().toString());
            }
            precisionFor = Math.max(precisionFor, attr.getType().getPrecision());
        }
        if (ctx.loopStatement_while() != null) {
            visit(ctx.loopStatement_while());
            ASTAttribute attr = m_ast.lookup(ctx.loopStatement_while().expression());
            TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
            if (!(attr.getType() instanceof TypeBit) || attr.getType().getPrecision() != 1) {
                ErrorStack.add(
                        ctx.loopStatement_while().expression(),
                        "WHILE",
                        "type must be BIT(1) - but is " + attr.getType().toString());
            }
        }

        if (ctx.loopStatement_for() != null) {
            // adjust the precision of the loop control variable in the symbol table
            SymbolTableEntry seFor =
                    m_currentSymbolTable.lookup(ctx.loopStatement_for().ID().toString());
            if (seFor != null) {
                VariableEntry ve = (VariableEntry) seFor;
                ((TypeFixed) (ve.getType())).setPrecision(precisionFor);
            }
        }

        // now treat the loop body
        visit(ctx.loopBody());

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement_from(OpenPearlParser.LoopStatement_fromContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitLoopStatement_from:ctx"
                        + CommonUtils.printContext(ctx));

        m_calculateRealFixedLength = true;
        visitChildren(ctx);
        m_calculateRealFixedLength = false;

        return null;
    }

    @Override
    public Void visitLoopStatement_to(OpenPearlParser.LoopStatement_toContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitLoopStatement_to:ctx" + CommonUtils.printContext(ctx));

        m_calculateRealFixedLength = true;
        visitChildren(ctx);
        m_calculateRealFixedLength = false;

        return null;
    }

    @Override
    public Void visitLoopStatement_by(OpenPearlParser.LoopStatement_byContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitLoopStatement_by:ctx" + CommonUtils.printContext(ctx));

        m_calculateRealFixedLength = true;
        visitChildren(ctx);
        m_calculateRealFixedLength = false;

        return null;
    }


    @Override
    public Void visitAssignment_statement(OpenPearlParser.Assignment_statementContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitAssignment_statement_by:ctx"
                        + CommonUtils.printContext(ctx));
        Log.debug("ExpressionTypeVisitor:visitAssignment_statement:" + ctx.getText());
        String s = ctx.getText();
        ASTAttribute attrName = null;
        ASTAttribute selection = null;


        Log.debug(
                "ExpressionTypeVisitor:visitAssignment_statement:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        ASTAttribute a = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx, "assignment");

        visit(ctx.name());

        attrName = m_ast.lookup(ctx.name());
        if (attrName == null) {
            // lhs not found --> error already issued --> just leave check
            ErrorStack.leave();
            return null;
        }
        if (ctx.dereference() != null) {
            if (!(attrName.getType() instanceof TypeReference) ) {
                ErrorStack.add("CONT needs type reference -- got " + attrName.getType().toErrorString());
                ErrorStack.leave();   // abort checks
                return null;
            } 
        }

        if (ctx.charSelection() != null) {
            //  visit(ctx.charSelectionSlice());
            makeExpressionToType(ctx.name(), new TypeChar());
            m_type = attrName.getType();
            visit(ctx.charSelection());
            selection = m_ast.lookup(ctx.charSelection());
            //            if (attrName.getType() instanceof TypeReference &&
            //                    ((TypeReference)attrName.getType()).getBaseType() instanceof TypeChar) {
            //                    attrName.setType(((TypeReference)attrName.getType()).getBaseType());
            //            }
            //            if (!(attrName.getType() instanceof TypeChar)) {
            //                ErrorStack.add(
            //                        ".CHAR must be applied on variable of type CHAR -- used with "
            //                                + attrName.getType());
            //            }
        } else if (ctx.bitSelection() != null) {
            makeExpressionToType(ctx.name(), new TypeBit());
            m_type = attrName.getType();
            visit(ctx.bitSelection());
            selection = m_ast.lookup(ctx.bitSelection());
            //            // visit(ctx.bitSelectionSlice());
            //            selection = m_ast.lookup(ctx.bitSelectionSlice());
            //            if (attrName.getType() instanceof TypeReference &&
            //                ((TypeReference)attrName.getType()).getBaseType() instanceof TypeBit) {
            //                attrName.setType(((TypeReference)attrName.getType()).getBaseType());
            //            }
            //            if (!(attrName.getType() instanceof TypeBit)) {
            //                ErrorStack.add(
            //                        ".BIT must be applied on variable of type BIT -- used with "
            //                                + attrName.getType());
            //            }
        }
        if (selection != null && selection.getConstantSelection() != null) {
            long lower = selection.getConstantSelection().getLowerBoundary().getValue();
            long upper = selection.getConstantSelection().getUpperBoundary().getValue();
            if (lower < 1
                    || upper < 1
                    || attrName.getType().getPrecision() < lower
                    || attrName.getType().getPrecision() < upper) {
                ErrorStack.add("selection beyond variable size");
            }
        }

        if (ctx.dereference() != null) {
            if ( attrName.getType() instanceof TypeReference) {
                attrName.setType(((TypeReference)(attrName.getType())).getBaseType());
            } else {
                ErrorStack.add("need type reference -- got " + attrName.getType().toString());
            }
        }



        ErrorStack.leave();

        return null;
    }

    //    @Override
    //    public Void visitNowFunction(OpenPearlParser.NowFunctionContext ctx) {
    //        Log.debug("ExpressionTypeVisitor:visitNowFunction:ctx" + CommonUtils.printContext(ctx));
    //
    //        TypeClock type = new TypeClock();
    //        ASTAttribute expressionResult = new ASTAttribute(type);
    //        m_ast.put(ctx, expressionResult);
    //
    //        return null;
    //    }

    @Override
    public Void visitSizeofExpression(OpenPearlParser.SizeofExpressionContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitSizeofExpression:ctx" + CommonUtils.printContext(ctx));
        boolean isRefChar = false;
        ErrorStack.enter(ctx,"SIZEOF"); 
        TypeFixed type = new TypeFixed(Defaults.FIXED_LENGTH);
        ASTAttribute expressionResult = new ASTAttribute(type);
        expressionResult.setIsConstant(true);
        m_ast.put(ctx, expressionResult);

        if (ctx.name() != null) {
            visitName(ctx.name());
        }

        if (ctx.refCharSizeofAttribute() != null ) {

            ASTAttribute attrName = m_ast.lookup(ctx.name());
            if (attrName.getType()  instanceof TypeReference && 
                    (((TypeReference)(attrName.getType())).getBaseType() instanceof TypeRefChar)) {
                isRefChar = true;
            }

            if (! isRefChar) {
                ErrorStack.add(ctx.refCharSizeofAttribute().getText()+" must be used on type REF CHAR() --- got "
                        +attrName.m_type);
            }

        } else {
            if (ctx.name() != null) {
                if (ctx.name().listOfExpression() != null || ctx.name().name() != null) {
                    ErrorStack.add("only identifier allowed");
                } else {
                    ASTAttribute attrName = m_ast.lookup(ctx.name());
                    if (attrName.getVariable() != null) {
                        if (attrName.getType() instanceof TypeDation ||
                                attrName.getType() instanceof TypeSemaphore ||
                                attrName.getType() instanceof TypeBolt ||
                                attrName.getType() instanceof TypeInterrupt ||
                                attrName.getType() instanceof TypeSignal) {
                            ErrorStack.add(ctx.name(), null,"may not be applied on type "+attrName.getType().getName());
                        }
                    } else {
                        ErrorStack.add(ctx.name(), null, "may not be applied on type "+attrName.getType().getName());
                    }
                }
            } else if (ctx.simpleType() != null) {
                //                SymbolTableEntry entry = this.m_currentSymbolTable.lookup(ctx.name().ID().getText());
                //                if (entry == null) {
                //                    ErrorStack.add("'" + ctx.name().ID().getText() + "' is not defined");
                //                } else if (entry instanceof VariableEntry) {
                //                    VariableEntry ve = (VariableEntry)entry;
                //                    ASTAttribute nameAttr = new ASTAttribute(ve.getType());
                //                    m_ast.put(ctx.name(), nameAttr);
                //                } else {
                //                    ErrorStack.addInternal(ctx.name(),"ExpressionTypeVisitor@2004","missing alternative");
                //                }
            } 
        }

        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report
    //            Table 6.6: Dyadic comparison operators
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      | Type of   | Result type  | Meaning of operation
    //            | operand 1    | operand 2 |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    // op1 == op2 | FIXED(g1)    | FIXED(g2) | BIT(1)       | “equal”
    //    or      | FIXED(g1)    | FLOAT(g2) |              | If op1 is equal op2,
    // op1 EQ op2 | FLOAT(g1)    | FIXED(g1) |              | the result has value ’1’B,
    //            | FLOAT(g1)    | FLOAT(g2) |              | otherwise ’0’B.
    //            | CLOCK        | CLOCK     |              | If lg2 = lg1, the shorter
    //            | DURATION     | DURATION  |              | character or bit string, resp.,
    //            | CHAR(lg1)    | CHAR(lg2) |              | is padded with blanks or zeros,
    //            | BIT(lg1)     | BIT(lg2)  |              | resp., on the right side to match

    @Override
    public Void visitEqRelationalExpression(OpenPearlParser.EqRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug(
                "ExpressionTypeVisitor:visitEqRelationalExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "EQ",
                        "no AST attribute found for lhs of operation EQ");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "EQ",
                        "no AST attribute found for rhs of operation EQ");

        checkUnOrderedCompare(op1, op2, "EQ or ==", ctx,ctx.expression(0), ctx.expression(1));

        return null;
    }

    //
    // Reference: OpenPEARL Language Report
    //            Table 6.6: Dyadic comparison operators
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      | Type of   | Result type  | Meaning of operation
    //            | operand 1    | operand 2 |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    // op1 /= op2 | FIXED(g1)    | FIXED(g2) | BIT(1)       | “not equal”
    //    or      | FIXED(g1)    | FLOAT(g2) |              | If op1 is not equal op2,
    // op1 NE op2 | FLOAT(g1)    | FIXED(g1) |              | the result has value ’1’B,
    //            | FLOAT(g1)    | FLOAT(g2) |              | otherwise ’0’B.
    //            | CLOCK        | CLOCK     |              | If lg2 = lg1, the shorter
    //            | DURATION     | DURATION  |              | character or bit string, resp.,
    //            | CHAR(lg1)    | CHAR(lg2) |              | is padded with blanks or zeros,
    //            | BIT(lg1)     | BIT(lg2)  |              | resp., on the right side to match

    @Override
    public Void visitNeRelationalExpression(OpenPearlParser.NeRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug(
                "ExpressionTypeVisitor:visitNeRelationalExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "NE",
                        "no AST attribute found for lhs of operation NE");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "NE",
                        "no AST attribute found for rhs of operation NE");

        checkUnOrderedCompare(op1, op2, "NE or /=", ctx,ctx.expression(0),ctx.expression(1));

        return null;
    }

    private Void checkUnOrderedCompare(
            ASTAttribute op1,
            ASTAttribute op2,
            String relation,
            OpenPearlParser.ExpressionContext ctx,
            OpenPearlParser.ExpressionContext ctx1,
            OpenPearlParser.ExpressionContext ctx2) {
        ASTAttribute res;
        ErrorStack.enter(ctx,relation);

        if (op1 != null && op2 != null) {
            TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);
            Boolean isConstant = op1.isConstant() && op2.isConstant();

            if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
                res = new ASTAttribute(new TypeBit(1), isConstant);
                m_ast.put(ctx, res);

                if (m_debug)
                    Log.debug(
                            "ExpressionTypeVisitor: visit"
                                    + relation
                                    + "RelationalExpression: rule#7");
            } else {
                ErrorStack.leave();  // checkOrdered compare performs ErrorStack.enter(ctx,relation)!
                checkOrderedCompare(op1, op2, relation, ctx, ctx1, ctx2);
                ErrorStack.enter(ctx,relation);
            }
        }
        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report
    //            Table 6.6: Dyadic comparison operators
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      | Type of   | Result type  | Meaning of operation
    //            | operand 1    | operand 2 |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    //  op1 < op2 | FIXED(g1)    | FIXED(g2) | BIT(1)       | “less than”
    //    or      | FIXED(g1)    | FLOAT(g2) |              | If op1 is less than op2,
    // op1 LT op2 | FLOAT(g1)    | FIXED(g1) |              | the result has value ’1’B,
    //            | FLOAT(g1)    | FLOAT(g2) |              | otherwise ’0’B.
    //            | CLOCK        | CLOCK     |              |
    //            | DURATION     | DURATION  |              |
    //            |              |           |              |
    //            | CHAR(lg1)    | CHAR(lg2) |              | character string comparison
    //            |              |           |              | if lg1 <> lg2 the shorter
    //            |              |           |              | character string is padded with
    //            |              |           |              | spaces on the right side to
    //            |              |           |              | match the length. Then the
    //            |              |           |              | internal represenations are
    //            |              |           |              | compared character by character
    //            |              |           |              | from left to right

    @Override
    public Void visitLtRelationalExpression(OpenPearlParser.LtRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug(
                "ExpressionTypeVisitor:visitLtRelationalExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "LT",
                        "no AST attribute found for lhs of operation LT");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "LT",
                        "no AST attribute found for rhs of operation LT");

        checkOrderedCompare(op1, op2, "LT or <", ctx,ctx.expression(0),ctx.expression(1));

        return null;
    }

    //
    // Reference: OpenPEARL Language Report
    //            Table 6.6: Dyadic comparison operators
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      | Type of   | Result type  | Meaning of operation
    //            | operand 1    | operand 2 |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    // op1 <= op2 | FIXED(g1)    | FIXED(g2) | BIT(1)       | “less or equal”
    //    or      | FIXED(g1)    | FLOAT(g2) |              | If op1 is less or equal op2,
    // op1 LE op2 | FLOAT(g1)    | FIXED(g1) |              | the result has value ’1’B,
    //            | FLOAT(g1)    | FLOAT(g2) |              | otherwise ’0’B.
    //            | CLOCK        | CLOCK     |              |
    //            | DURATION     | DURATION  |              |
    //            |              |           |              |
    //            | CHAR(lg1)    | CHAR(lg2) |              | character string comparison
    //            |              |           |              | if lg1 <> lg2 the shorter
    //            |              |           |              | character string is padded with
    //            |              |           |              | spaces on the right side to
    //            |              |           |              | match the length. Then the
    //            |              |           |              | internal represenations are
    //            |              |           |              | compared character by character
    //            |              |           |              | from left to right

    @Override
    public Void visitLeRelationalExpression(OpenPearlParser.LeRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug(
                "ExpressionTypeVisitor:visitLeRelationalExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "LE",
                        "no AST attribute found for lhs of operation LE");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "LE",
                        "no AST attribute found for rhs of operation LE");

        checkOrderedCompare(op1, op2, "LE or <=", ctx,ctx.expression(0), ctx.expression(1));

        return null;
    }

    //
    // Reference: OpenPEARL Language Report
    //            Table 6.6: Dyadic comparison operators
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      | Type of   | Result type  | Meaning of operation
    //            | operand 1    | operand 2 |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    // op1 > op2  | FIXED(g1)    | FIXED(g2) | BIT(1)       | “greater”
    //    or      | FIXED(g1)    | FLOAT(g2) |              | If op1 is greater op2,
    // op1 GT op2 | FLOAT(g1)    | FIXED(g1) |              | the result has value ’1’B,
    //            | FLOAT(g1)    | FLOAT(g2) |              | otherwise ’0’B.
    //            | CLOCK        | CLOCK     |              |
    //            | DURATION     | DURATION  |              |
    //            |              |           |              |
    //            | CHAR(lg1)    | CHAR(lg2) |              | character string comparison
    //            |              |           |              | if lg1 <> lg2 the shorter
    //            |              |           |              | character string is padded with
    //            |              |           |              | spaces on the right side to
    //            |              |           |              | match the length. Then the
    //            |              |           |              | internal represenations are
    //            |              |           |              | compared character by character
    //            |              |           |              | from left to right

    @Override
    public Void visitGtRelationalExpression(OpenPearlParser.GtRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug(
                "ExpressionTypeVisitor:visitGtRelationalExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "GT",
                        "no AST attribute found for lhs of operation GT");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "GT",
                        "no AST attribute found for rhs of operation GT");

        checkOrderedCompare(op1, op2, "GT or >", ctx,ctx.expression(0),ctx.expression(1));

        return null;
    }

    //
    // Reference: OpenPEARL Language Report
    //            Table 6.6: Dyadic comparison operators
    // -----------+--------------+-----------+--------------+---------------------------------
    // Expression | Type of      | Type of   | Result type  | Meaning of operation
    //            | operand 1    | operand 2 |              |
    // -----------+------------- +-----------+--------------+---------------------------------
    // op1 >= op2 | FIXED(g1)    | FIXED(g2) | BIT(1)       | “greater or equal”
    //    or      | FIXED(g1)    | FLOAT(g2) |              | If op1 is greater or equal op2,
    // op1 GE op2 | FLOAT(g1)    | FIXED(g1) |              | the result has value ’1’B,
    //            | FLOAT(g1)    | FLOAT(g2) |              | otherwise ’0’B.
    //            | CLOCK        | CLOCK     |              |
    //            | DURATION     | DURATION  |              |
    //            |              |           |              |
    //            | CHAR(lg1)    | CHAR(lg2) |              | character string comparison
    //            |              |           |              | if lg1 <> lg2 the shorter
    //            |              |           |              | character string is padded with
    //            |              |           |              | spaces on the right side to
    //            |              |           |              | match the length. Then the
    //            |              |           |              | internal represenations are
    //            |              |           |              | compared character by character
    //            |              |           |              | from left to right

    @Override
    public Void visitGeRelationalExpression(OpenPearlParser.GeRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug(
                "ExpressionTypeVisitor:visitGeRelationalExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "GE",
                        "no AST attribute found for lhs of operation GE");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "GE",
                        "no AST attribute found for rhs of operation GE");

        checkOrderedCompare(op1, op2, "GE or >=", ctx,ctx.expression(0),ctx.expression(1));

        return null;
    }

    private Void checkOrderedCompare(
            ASTAttribute op1,
            ASTAttribute op2,
            String relation,
            OpenPearlParser.ExpressionContext ctx,
            OpenPearlParser.ExpressionContext ctx1,
            OpenPearlParser.ExpressionContext ctx2) {
        ASTAttribute res = null;


        ErrorStack.enter(ctx, relation);

        if (op1 != null && op2 != null) {
            TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);
            Boolean isConstant = op1.isConstant() && op2.isConstant();


            if (type1 instanceof TypeFixed && type2 instanceof TypeFixed) {
                res = new ASTAttribute(new TypeBit(1), isConstant);

                if (m_debug)
                    Log.debug(
                            "ExpressionTypeVisitor: visit"
                                    + relation
                                    + "RelationalExpression: rule#1");
            } else if (type1 instanceof TypeFixed && type2 instanceof TypeFloat) {
                res = new ASTAttribute(new TypeBit(1), isConstant);


                if (m_debug)
                    Log.debug(
                            "ExpressionTypeVisitor: visit"
                                    + relation
                                    + "RelationalExpression: rule#2");
            } else if (type1 instanceof TypeFloat && type2 instanceof TypeFixed) {
                res = new ASTAttribute(new TypeBit(1), isConstant);

                if (m_debug)
                    System.out.println(
                            "ExpressionTypeVisitor: visit"
                                    + relation
                                    + "RelationalExpression: rule#3");
            } else if (type1 instanceof TypeFloat && type2 instanceof TypeFloat) {
                res = new ASTAttribute(new TypeBit(1), isConstant);

                if (m_debug)
                    Log.debug(
                            "ExpressionTypeVisitor: visit"
                                    + relation
                                    + "RelationalExpression: rule#4");
            } else if (type1 instanceof TypeClock && type2 instanceof TypeClock) {
                res = new ASTAttribute(new TypeBit(1), isConstant);

                if (m_debug)
                    Log.debug(
                            "ExpressionTypeVisitor: visit"
                                    + relation
                                    + "RelationalExpression: rule#5");
            } else if (type1 instanceof TypeDuration && type2 instanceof TypeDuration) {
                res = new ASTAttribute(new TypeBit(1), isConstant);

                if (m_debug)
                    Log.debug(
                            "ExpressionTypeVisitor: visit"
                                    + relation
                                    + "RelationalExpression: rule#6");
            } else if ((type1 instanceof TypeChar || type1 instanceof TypeVariableChar)
                    && (type2 instanceof TypeChar || type2 instanceof TypeVariableChar)) {
                res = new ASTAttribute(new TypeBit(1), isConstant);

                if (m_debug)
                    Log.debug(
                            "ExpressionTypeVisitor: visit\"+relation+\"RelationalExpression: rule#7");

            } else if (type1 instanceof TypeRefChar || type2 instanceof TypeRefChar) {
                res = new ASTAttribute(new TypeBit(1), isConstant);

                ErrorStack.add(
                        "REF CHAR() not supported in expressions, yet");

            } else if (isVoidProc(ctx1,ctx2)) {
                res = new ASTAttribute(new TypeBit(1), isConstant);
            } else {
                ErrorStack.add(
                        "type mismatch: '"
                                + op1.getType().toString()
                                + "' cannot be compared with '"
                                + op2.getType().getName()
                                + "'");

            }
        }
        if (res != null) {
            m_ast.put(ctx, res);
        }

        ErrorStack.leave();
        return null;
    }

    public Void visitIsRelationalExpression(OpenPearlParser.IsRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug(
                "ExpressionTypeVisitor:visitIsRelationalExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "IS",
                        "no AST attribute found for lhs of operation IS");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "IS",
                        "no AST attribute found for rhs of operation IS");

        checkIsIsntCompare(op1, op2, "IS", ctx);

        return null;
    }

    public Void visitIsntRelationalExpression(
            OpenPearlParser.IsntRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug(
                "ExpressionTypeVisitor:visitIsntRelationalExpression:ctx"
                        + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "ISNT",
                        "no AST attribute found for lhs of operation ISNT");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "ISNT",
                        "no AST attribute found for rhs of operation ISNT");

        checkIsIsntCompare(op1, op2, "ISNT", ctx);

        return null;
    }

    private Void checkIsIsntCompare(
            ASTAttribute op1,
            ASTAttribute op2,
            String relation,
            OpenPearlParser.ExpressionContext ctx) {
        ASTAttribute res;

        ErrorStack.enter(ctx);

        if (m_debug)
            Log.debug("ExpressionTypeVisitor: visit" + relation + "RelationalExpression: rule#1");
        if (op1 != null && op2 != null) {
            Boolean typeMismatch = true;

            // at least one must be references with the same base type
            // but NIL does not count here as reference
            TypeDefinition type1 = op1.getType();
            TypeDefinition type2 = op2.getType();

            int nbrOfReferences = 0;

            if (type1 instanceof TypeReference || type2 instanceof TypeReference) {
                if (type1 instanceof TypeReference) {
                    type1 = ((TypeReference) type1).getBaseType();
                    if (type1 != null) {
                        nbrOfReferences++;
                    }
                }
                if (type2 instanceof TypeReference) {
                    type2 = ((TypeReference) type2).getBaseType();
                    if (type2 != null) {
                        nbrOfReferences++;
                    }
                }

                if (nbrOfReferences > 0) {
                    // typeX == null means is 'NIL'
                    if (type1 == null || type2 == null || type1.equals(type2)) {
                        typeMismatch = false;
                    } else if ((type1 instanceof TypeArray
                            && type2 instanceof TypeArray)) {
                        if (((TypeArray) type1)
                                .getBaseType()
                                .equals(((TypeArray) type2).getBaseType())) {
                            typeMismatch = false;
                        }
                    }
                    if (typeMismatch) {
                        if ((type2 instanceof TypeArray && type1 instanceof TypeArray)) {
                            if (((TypeArray) type2)
                                    .getBaseType()
                                    .equals(((TypeArray) type1).getBaseType())) {
                                typeMismatch = false;
                            }
                        } else 
                            if ((type1 == null && type2 instanceof TypeArray)
                                    || (type2 == null && type1 instanceof TypeArray)
                                    || type1.equals(type2)) {
                                typeMismatch = false;

                            } else if ((type1 == null && type2 instanceof TypeTask)
                                    || (type2 == null && type1 instanceof TypeTask)
                                    || type1.equals(type2)) {
                                typeMismatch = false;
                            } else if ((type1 == null && type2 instanceof TypeDation)
                                    || (type2 == null && type1 instanceof TypeDation)
                                    || type1.equals(type2)) {
                                typeMismatch = false;
                            }
                    }
                    // check REF CHAR()
                    if (typeMismatch) {
                        if (type1 instanceof TypeRefChar) {
                            if (type2 instanceof TypeChar || type2 instanceof TypeRefChar) {
                                typeMismatch = false;
                            }
                        } else if (type2 instanceof TypeRefChar) {
                            if (type1 instanceof TypeChar) {
                                typeMismatch = false;
                            }
                        }
                    }
                }
            }

            if (typeMismatch) {
                ErrorStack.add(
                        "type mismatch: '"
                                + op1.getType().toString()
                                + "' cannot be compared as "
                                + relation
                                + " with '"
                                + op2.getType().getName()
                                + "'");
            } else {
                res = new ASTAttribute(new TypeBit(1));
                m_ast.put(ctx, res);
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitStringSelection(OpenPearlParser.StringSelectionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitStringSelection:ctx" + CommonUtils.printContext(ctx));

        ASTAttribute attr = null;

        visit(ctx.name());


        if (ctx.bitSelection() != null) {
            //attr = m_ast.lookup(ctx.bitSelection().name());
            makeExpressionToType(ctx.name(), new TypeBit());
            m_type = m_ast.lookup(ctx.name()).getType();
            visit(ctx.bitSelection());
            // TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);

            attr = m_ast.lookup(ctx.bitSelection());
            //          attrName = m_ast.lookup(ctx.bitSelection().name());
            //m_ast.put(ctx, attr);
        } else if (ctx.charSelection() != null) {
            makeExpressionToType(ctx.name(), new TypeChar());
            m_type = m_ast.lookup(ctx.name()).getType();
            visit(ctx.charSelection());

            //            attr = m_ast.lookup(ctx.name());
            //            TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);

            attr = m_ast.lookup(ctx.charSelection());
            //          attrName = m_ast.lookup(ctx.charSelection().name());
            m_ast.put(ctx, attr);
        }
        m_ast.put(ctx, attr);
        return null;
    }

    @Override
    public Void visitCshiftExpression(OpenPearlParser.CshiftExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug(
                "ExpressionTypeVisitor:visitCshiftExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "CSHIFT",
                        "no AST attribute found for lhs of operation CSHIFT");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "CSHIFT",
                        "no AST attribute found for rhs of operation CSHIFT");

        treatShiftCshift(ctx, op1, op2, "CSHIFT or <>",ctx.expression(0), ctx.expression(1));

        return null;
    }

    @Override
    public Void visitShiftExpression(OpenPearlParser.ShiftExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug("ExpressionTypeVisitor:visitShiftExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "SHIFT",
                        "no AST attribute found for lhs of operation SHIFT");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "SHIFT",
                        "no AST attribute found for rhs of operation SHIFT");

        treatShiftCshift(ctx, op1, op2, "SHIFT", ctx.expression(0), ctx.expression(1));

        return null;
    }

    private void treatShiftCshift(
            ParserRuleContext ctx, ASTAttribute op1, ASTAttribute op2, String operation,
            ParserRuleContext ctx1,ParserRuleContext ctx2) {
        ErrorStack.enter(ctx, operation);
        if (op1 != null && op2 != null) {
            // implicit dereferences

            TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            if (type1 instanceof TypeBit && type2 instanceof TypeFixed) {
                TypeBit type = new TypeBit(type1.getPrecision());
                ASTAttribute expressionResult = new ASTAttribute(type);
                m_ast.put(ctx, expressionResult);

                Log.debug("ExpressionTypeVisitor: Dyadic Boolean and shift operators");
            } else if (isVoidProc(ctx1, ctx2)) { 
                if (type1 == null && type2 != null) {
                    TypeBit type = new TypeBit(Defaults.BIT_LENGTH);
                    ASTAttribute expressionResult = new ASTAttribute(type);
                    m_ast.put(ctx, expressionResult);
                } else if (type1 != null && type2 == null) {
                    m_ast.put(ctx, op1);
                } else {
                    m_ast.put(ctx, op1);
                }

            } else {
                ErrorStack.add(
                        "type mismatch: expected BIT "
                                + operation
                                + " FIXED -- got: "
                                + op1.getType().toString()
                                + " "
                                + operation
                                + " "
                                + op2.getType().toString());
            }
        }

        ErrorStack.leave();
    }

    @Override
    public Void visitCatExpression(OpenPearlParser.CatExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug("ExpressionTypeVisitor:visitCatExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "CAT",
                        "no AST attribute found for lhs of operation CAT");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "CAT",
                        "no AST attribute found for rhs of operation CAT");

        ErrorStack.enter(ctx, "CAT or ><");
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 != null && op2 != null) {
            // implicit dereferences
            TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            if (type1 instanceof TypeBit && type2 instanceof TypeBit) {
                TypeBit type = new TypeBit(type1.getPrecision() + type2.getPrecision());
                ASTAttribute expressionResult = new ASTAttribute(type);
                m_ast.put(ctx, expressionResult);
            } else if (type1 instanceof TypeChar && type2 instanceof TypeChar) {
                TypeChar type = new TypeChar(type1.getPrecision() + type2.getPrecision());
                ASTAttribute expressionResult = new ASTAttribute(type);
                m_ast.put(ctx, expressionResult);
            } else if (type1 instanceof TypeRefChar || type2 instanceof TypeRefChar) {
                ErrorStack.add(
                        "REF CHAR() not supported in expressions, yet");
            } else if (isVoidProc(ctx.expression(0), ctx.expression(1))) {

                ASTAttribute expressionResult = null;
                if (type1 == null && type2 != null) {
                    expressionResult = new ASTAttribute(type2);
                } else if (type1 != null && type2 == null) {
                    expressionResult = new ASTAttribute(type1);
                } else {
                    expressionResult = op1;
                }
                m_ast.put(ctx, expressionResult);
            } else {
                ErrorStack.add(
                        "type mismatch: expected BIT CAT BIT or CHAR CAT CHAR -- got "
                                + op1.getType().toString()
                                + " CAT "
                                + op2.getType().toString());
            }
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitAndExpression(OpenPearlParser.AndExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug("ExpressionTypeVisitor:visitAndExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "AND",
                        "no AST attribute found for lhs of operation AND");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "AND",
                        "no AST attribute found for rhs of operation AND");

        treatAndOrExor(ctx, op1, op2, "AND",ctx.expression(0), ctx.expression(1));

        return null;
    }

    @Override
    public Void visitOrExpression(OpenPearlParser.OrExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug("ExpressionTypeVisitor:visitOrExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "OR",
                        "no AST attribute found for lhs of operation OR");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "OR",
                        "no AST attribute found for rhs of operation OR");

        treatAndOrExor(ctx, op1, op2, "OR",ctx.expression(0), ctx.expression(1));

        return null;
    }

    @Override
    public Void visitExorExpression(OpenPearlParser.ExorExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug("ExpressionTypeVisitor:visitExorExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        visit(ctx.expression(1));

        op1 =
                saveGetAttribute(
                        ctx.expression(0),
                        ctx,
                        "EXOR",
                        "no AST attribute found for lhs of operation EXOR");
        op2 =
                saveGetAttribute(
                        ctx.expression(1),
                        ctx,
                        "EXOR",
                        "no AST attribute found for rhs of operation EXOR");

        treatAndOrExor(ctx, op1, op2, "EXOR",ctx.expression(0), ctx.expression(1));

        return null;
    }

    private void treatAndOrExor(
            ParserRuleContext ctx, ASTAttribute op1, ASTAttribute op2, String operation,
            ParserRuleContext ctx1, ParserRuleContext ctx2) {
        ErrorStack.enter(ctx, operation);

        if (op1 != null && op2 != null) {

            // implicit dereferences
            TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);
            TypeDefinition type2 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

            if (type1 instanceof TypeBit && type2 instanceof TypeBit) {
                TypeBit type = new TypeBit(Math.max(type1.getPrecision(), type2.getPrecision()));
                ASTAttribute expressionResult = new ASTAttribute(type);
                m_ast.put(ctx, expressionResult);
            } else if (isVoidProc(ctx1, ctx2)) {
                ASTAttribute expressionResult = null;
                if (type1 == null && type2 != null) {
                    expressionResult = new ASTAttribute(type2);
                } else if (type1 != null && type2 == null) {
                    expressionResult = new ASTAttribute(type1);
                } else {
                    expressionResult = op1;
                }
                m_ast.put(ctx, expressionResult);
            } else {
                ErrorStack.add(
                        "type mismatch: expected BIT "
                                + operation
                                + " BIT -- got"
                                + op1.getType().toString()
                                + operation
                                + op2.getType().toString());
            }
        }
        ErrorStack.leave();
    }

    @Override
    public Void visitCONTExpression(OpenPearlParser.CONTExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitCONTExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());

        op = saveGetAttribute(ctx.expression(), ctx, "CONT", "no AST attribute found for CONT");

        ErrorStack.enter(ctx, "CONT");

        if (op != null) {

            if (op.getType() instanceof TypeReference) {
                ASTAttribute expressionResult =
                        new ASTAttribute(((TypeReference) (op.getType())).getBaseType());
                m_ast.put(ctx, expressionResult);
                Log.debug("ExpressionTypeVisitor: CONT: rule#1");
            } else {
                ErrorStack.add("need type reference -- got " + op.getType().toString());
            }
        }
        ErrorStack.leave();

        return null;
    }


    @Override
    public Void visitConstantFixedExpression(OpenPearlParser.ConstantFixedExpressionContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitConstantFixedExpression:ctx"
                        + CommonUtils.printContext(ctx));

        ConstantFixedExpressionEvaluator evaluator =
                new ConstantFixedExpressionEvaluator(
                        m_verbose, m_debug, m_currentSymbolTable, null, null);

        ConstantFixedValue c = evaluator.visit(ctx);

        ASTAttribute attr = new ASTAttribute(new TypeFixed(c.getPrecision()));
        attr.setConstant(c);
        m_ast.put(ctx, attr);

        return null;
    }

    @Override
    public Void visitConstantFixedExpressionFit(
            OpenPearlParser.ConstantFixedExpressionFitContext ctx) {


        Log.debug(
                "ExpressionTypeVisitor:visitConstantFixedExpressionFit:ctx"
                        + CommonUtils.printContext(ctx));

        ErrorStack.addInternal(ctx, "FIT", "code for constant fixed expression missing");

        return null;
    }

    @Override
    public Void visitIdentifier(OpenPearlParser.IdentifierContext ctx) {
        String s = ctx.getText();

        SymbolTableEntry se = m_currentSymbolTable.lookup(s);
        if (se == null) {
            // we need no attributes for procedure specification parameters
            // check if ctx is a formal parameter in a procedure specification or REF PROC 
            ParserRuleContext c = ctx;
            while (c!= null && ! (c  instanceof FormalParameterContext) ) {
                c = c.getParent();
            }
            if (c!= null) {
                // we have a formal parameter
                // let's if we are in a procedureDenotation or in a typeReference
                while (c!= null && !( c instanceof ProcedureDenotationContext || c instanceof TypeReferenceContext)) {
                    c = c.getParent();
                }
                if (c != null) {
                    return null;
                }
            }
        }
        if (se == null) {
            ErrorStack.addInternal(ctx, "ExpressionTypeVisitor", "no entry found for " + s);
            return null;
        }
        TypeDefinition type = null;
        if (se instanceof VariableEntry) {
            type = ((VariableEntry) se).getType();
            ASTAttribute attr = new ASTAttribute(type);

            if (type.hasAssignmentProtection()) {
                attr.setIsConstant(true);
                Initializer i = ((VariableEntry) se).getInitializer();
                if (i instanceof SimpleInitializer) {
                    attr.setConstant(((SimpleInitializer) i).getConstant());
                }
            }
            m_ast.put(ctx, attr);
        } else if (se instanceof InterruptEntry) {
            ASTAttribute attr = new ASTAttribute(new TypeInterrupt());
            m_ast.put(ctx, attr);
        } else if (se instanceof ProcedureEntry) {
            type = ((ProcedureEntry) se).getType();
            ASTAttribute attr = new ASTAttribute(type);
            m_ast.put(ctx, attr);
        } else if (se instanceof TaskEntry) {

            ASTAttribute attr = new ASTAttribute(new TypeTask());
            m_ast.put(ctx, attr);            
        } else if (se instanceof UserDefinedType) {
            // we are in a type definition
            // --> do nothing
        } else if (se instanceof SignalEntry) {
            ASTAttribute attr = new ASTAttribute(new TypeSignal());
            m_ast.put(ctx, attr);   
        } else {
            ErrorStack.addInternal(
                    ctx, "ExpressionTypeVisitor:visitIdentifer", "missing alternative@2979");
        }
        return null;
    }

    @Override
    public Void visitInitElement(OpenPearlParser.InitElementContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitInitElement:ctx" + CommonUtils.printContext(ctx));
        // may be ID, constant or constantExpression
        visitChildren(ctx);
        ASTAttribute attr = null;
        if (ctx.constant() != null) {
            attr = m_ast.lookup(ctx.constant());
        } else if (ctx.constantExpression() != null) {
            attr = m_ast.lookup(ctx.constantExpression());
        } else if (ctx.identifier() != null) {
            // System.out.println("ID as initelement: "+ctx.identifier().getText());
            attr = m_ast.lookup(ctx.identifier());
        } else if (ctx.name() != null) {
            attr = m_ast.lookup(ctx.name());
        }
        m_ast.put(ctx, attr);
        return null;
    }

    @Override
    public Void visitConstant(OpenPearlParser.ConstantContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitConstant:ctx" + CommonUtils.printContext(ctx));
        int sign = 1;
        ASTAttribute expressionResult = null;

        if (ctx.sign() != null) {
            if (ctx.sign().getText().equals("-")) sign = -1;
        }
        if (ctx.durationConstant() != null) {
            expressionResult = new ASTAttribute(new TypeDuration(), true);
            ConstantDurationValue c =
                    (ConstantDurationValue)
                    (CommonUtils.getConstantDurationValue(ctx.durationConstant(), sign));
            expressionResult.setConstant(c);
            m_constantPool.add(c);
        } else if (ctx.floatingPointConstant() != null) {
            // ConstantFloatValue c = CommonUtils.getConstantFloatValue(ctx,sign));
            try {
                double value =
                        CommonUtils.getFloatingPointConstantValue(ctx.floatingPointConstant())
                        * sign;
                int precision =
                        CommonUtils.getFloatingPointConstantPrecision(
                                ctx.floatingPointConstant(),
                                m_currentSymbolTable.lookupDefaultFloatLength());

                ConstantFloatValue c = new ConstantFloatValue(value, precision);
                expressionResult = new ASTAttribute(new TypeFloat(precision), true);
                expressionResult.setConstant(c);
                m_constantPool.add(c);
            } catch (NumberFormatException ex) {
                //        throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(),
                // ctx.start.getCharPositionInLine());
                ErrorStack.add(ctx, "floating point constant", "illegal number");
            }

        } else if (ctx.timeConstant() != null) {
            expressionResult = new ASTAttribute(new TypeClock(), true);
            ConstantClockValue c = CommonUtils.getConstantClockValue(ctx.timeConstant());
            c = (ConstantClockValue) (m_constantPool.add(c));
            expressionResult.setConstant(c);
            m_ast.put(ctx, expressionResult);
        } else if (ctx.stringConstant() != null) {
            ConstantCharacterValue ccv =
                    getConstantStringLiteral(ctx.stringConstant().StringLiteral());
            int length = ccv.getLength();
            if (length == 0) {
                ErrorStack.add(ctx, "char literal", "need at least 1 character");
            }
            // generate AST Attribute for further analysis
            expressionResult = new ASTAttribute(new TypeChar(ccv.getLength()), true);
            ConstantValue cv =
                    m_constantPool.add(ccv); // add to constant pool; maybe we have it already
            expressionResult.setConstant(cv);
            m_ast.put(ctx, expressionResult);

        } else if (ctx.bitStringConstant() != null) {
            ConstantBitValue c = CommonUtils.getConstantBitValue(ctx.bitStringConstant());
            expressionResult = new ASTAttribute(new TypeBit(c.getLength()), true);
            m_ast.put(ctx, expressionResult);
            c = (ConstantBitValue) m_constantPool.add(c);
            expressionResult.setConstant(c);
        } else if (ctx.fixedConstant() != null) {
            long value = 0;
            int precision;
            try {
                precision = m_currentSymbolTable.lookupDefaultFixedLength();

                if (m_currFixedLength != null) {
                    precision = m_currFixedLength;
                }

                m_calculateRealFixedLength = true;
                if (m_calculateRealFixedLength) {
                    value = Long.parseLong(ctx.fixedConstant().IntegerConstant().getText());

                    precision = Long.toBinaryString(Math.abs(value)).length();
                    if (value < 0) {
                        precision++;
                    }
                }

                m_calculateRealFixedLength = false;

                if (ctx.fixedConstant().fixedNumberPrecision() != null) {
                    precision =
                            Integer.parseInt(
                                    ctx.fixedConstant()
                                    .fixedNumberPrecision()
                                    .IntegerConstant()
                                    .toString());
                }
                if (precision < Defaults.FIXED_MIN_LENGTH
                        || precision > Defaults.FIXED_MAX_LENGTH) {
                    CommonErrorMessages.wrongFixedPrecission(ctx.fixedConstant());
                }

                expressionResult = new ASTAttribute(new TypeFixed(precision), true);
                ConstantFixedValue cfv = new ConstantFixedValue(value * sign, precision);
                ConstantValue cv =
                        m_constantPool.add(cfv); // add to constant pool; maybe we have it already
                cv = m_constantPool.add(cv);
                expressionResult.setConstant(cv);
            } catch (NumberFormatException ex) {
                ErrorStack.add(ctx, "integer literal", "illegal number");
            }
        } else if (ctx.referenceConstant() != null) {
            // NIL fits to any type; thus we have NO basetype
            expressionResult = new ASTAttribute(new TypeReference());
            ConstantNILReference cnr = new ConstantNILReference();
            ConstantValue cv =
                    m_constantPool.add(cnr); // add to constant pool; maybe we have it already
            expressionResult.setConstant(cv);
        }
        m_ast.put(ctx, expressionResult);
        return null;
    }

    @Override
    public Void visitConstantExpression(OpenPearlParser.ConstantExpressionContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitConstantExpression:ctx"
                        + CommonUtils.printContext(ctx));
        ASTAttribute attr = null;

        visitChildren(ctx);

        if (ctx.constantFixedExpression() != null) {
            attr = m_ast.lookup(ctx.constantFixedExpression());
        } else if (ctx.floatingPointConstant() != null) {
            attr = m_ast.lookup(ctx.floatingPointConstant());
        } else if (ctx.durationConstant() != null) {
            attr = m_ast.lookup(ctx.durationConstant());
        } else {
            ErrorStack.addInternal(
                    ctx,
                    "ExpressionTypeVisitor:visitConstantExpression",
                    "untreated alternative@3134");
        }
        m_ast.put(ctx, attr);
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.9 Other dyadic operators
    //
    // -------------+-----------+-----------+-------------+---------------------------------
    // Expression   | Type of   | Type of   | Result type | Meaning of operation
    //              | operand 1 | operand 2 |             |
    // -------------+-----------+-----------+-------------+---------------------------------
    // op1 LWB op2  | FIXED(g)  | array     | FIXED(31)   | lower boundary of the dimension
    //              |           |           |             | (given by op1) of the array
    //              |           |           |             | (determined by op2), if existing
    // -------------+-----------+-----------+-------------+---------------------------------
    // op1 UPB op2  | FIXED(g)  | array     | FIXED(31)   | upper boundary of the dimension
    //              |           |           |             | (given by op1) of the array
    //              |           |           |             | (determined by op2), if existing
    // -------------+-----------+-----------+-------------+---------------------------------

    @Override
    public Void visitLwbDyadicExpression(OpenPearlParser.LwbDyadicExpressionContext ctx) {

        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitLwbDyadicExpression:ctx"
                        + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx,"LWB");
        res = new ASTAttribute(new TypeFixed(31), false);
        m_ast.put(ctx, res);
        visitChildren(ctx);
        ASTAttribute op1 = m_ast.lookup(ctx.expression(0));
        TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);

        ASTAttribute op2 = m_ast.lookup(ctx.expression(1));
        TypeDefinition type2 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

        if (isVoidProc(ctx.expression(0), ctx.expression(1))) {
            // result type already set
        } else {
            if (!(type1 instanceof TypeFixed)) {
                ErrorStack.warn("index selector must be FIXED -- got "+type1.toString4IMC(false));
            }

            if (!(type2 instanceof TypeArray)) {
                ErrorStack.warn("must be applied on array -- got "+type2.toString4IMC(false));
            }
        }
        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitUpbDyadicExpression(OpenPearlParser.UpbDyadicExpressionContext ctx) {
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitUpbDyadicExpression:ctx"
                        + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx,"UPB");
        res = new ASTAttribute(new TypeFixed(31), false);
        m_ast.put(ctx, res);
        visitChildren(ctx);
        ASTAttribute op1 = m_ast.lookup(ctx.expression(0));
        TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op1);

        ASTAttribute op2 = m_ast.lookup(ctx.expression(1));
        TypeDefinition type2 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op2);

        if (isVoidProc(ctx.expression(0)) || isVoidProc(ctx.expression(1))) {

        } else {
            if (!(type1 instanceof TypeFixed)) {

                ErrorStack.warn("index selector must be FIXED -- got "+type1.toString4IMC(false));
            }

            if (!(type2 instanceof TypeArray)) {
                ErrorStack.warn("must be applied on array -- got "+type2.toString4IMC(false));
            }
        }
        ErrorStack.leave();
        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.4 Other monadic perators
    //
    // -------------+---------------+-------------+---------------------------------
    // Expression   | Type of       | Result type | Meaning of operation
    //              | operand       |             |
    // -------------+---------------+-------------+---------------------------------
    // LWB a        | array         | FIXED(31)   | lower boundary of the first
    //              |               |             | dimension of the operand array
    // -------------+---------------+-------------+---------------------------------
    // UPB a        | array         | FIXED(31)   | upper boundary of the first
    //              |               |             | dimension of the operand array
    //              +---------------+-------------+---------------------------------
    //              | CHARACTER(lg) | FIXED(15)   | result := lg
    // -------------+---------------+-------------+---------------------------------

    @Override
    public Void visitLwbMonadicExpression(OpenPearlParser.LwbMonadicExpressionContext ctx) {
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitLwbMonadicExpression:ctx"
                        + CommonUtils.printContext(ctx));
        ErrorStack.enter(ctx, "LWB");

        res = new ASTAttribute(new TypeFixed(31), false);
        m_ast.put(ctx, res);
        visitChildren(ctx);
        ASTAttribute op = m_ast.lookup(ctx.expression());
        TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
        if (isVoidProc(ctx.expression())) {
            // do nothing
        } else  if (!(type1 instanceof TypeArray)) {
            ErrorStack.warn(ctx, "LWB","must be applied on array -- got "+type1.toString4IMC(false));
        }
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitUpbMonadicExpression(OpenPearlParser.UpbMonadicExpressionContext ctx) {
        ASTAttribute res;

        Log.debug(
                "ExpressionTypeVisitor:visitUpbMonadicExpression:ctx"
                        + CommonUtils.printContext(ctx));
        ErrorStack.enter(ctx, "UPB");
        res = new ASTAttribute(new TypeFixed(31), false);
        m_ast.put(ctx, res);
        visitChildren(ctx);
        ASTAttribute op = m_ast.lookup(ctx.expression());
        TypeDefinition type1 = TypeUtilities.performImplicitDereferenceAndFunctioncall(op);
        if (isVoidProc(ctx.expression())) {
            // do nothing
        } else if (!(type1 instanceof TypeArray)) {
            ErrorStack.warn(ctx, "UPB","must be applied on array -- got "+type1.toString4IMC(false));
        }
        ErrorStack.leave();
        return null;
    }


    //    @Override
    //    public Void visitCharSelection(OpenPearlParser.CharSelectionContext ctx) {
    //        Log.debug("ExpressionTypeVisitor:visitCharSelection:ctx" + CommonUtils.printContext(ctx));
    //        visitName(ctx.name());
    //        visitCharSelectionSlice(ctx.charSelectionSlice());
    //
    //        ASTAttribute attrSelection = m_ast.lookup(ctx.charSelectionSlice());
    //        if (attrSelection.getType() instanceof TypeVariableChar) {
    //            // in the code generation we need the size of the char variable
    //            ASTAttribute attrName = m_ast.lookup(ctx.name());
    //            ((TypeVariableChar) attrSelection.getType()).setBaseType(attrName.getType());
    //        }
    //
    //        m_ast.put(ctx.charSelectionSlice(), attrSelection);
    //
    //        return null;
    //    }

    //    @Override
    //    public Void visitBitSelection(OpenPearlParser.BitSelectionContext ctx) {
    //        Log.debug("ExpressionTypeVisitor:visitBitSelection:ctx" + CommonUtils.printContext(ctx));
    //        visitChildren(ctx);
    //        ASTAttribute attrName = m_ast.lookup(ctx.name());
    //        
    //        //makeExpressionToType(ctx.name(), new TypeBit());
    //        //m_type = attrName.getType();           hier fehlt noch was!!!!
    ////        if (attrName.getType() instanceof TypeReference) {
    ////            attrName.setType(((TypeReference)attrName.getType()).getBaseType());
    ////        }
    //
    //        ASTAttribute attrSelection = m_ast.lookup(ctx.bitSelectionSlice());
    //
    //
    //        m_ast.put(ctx.bitSelectionSlice(), attrSelection);
    //
    //        return null;
    //    }
    /*
     bitSelectionSlice:
    '.' 'BIT' '('
    (
         expression
       | constantFixedExpression ':' constantFixedExpression
       | expression ':' expression '+' IntegerConstant
    )
    ')'
    ;
     */
    @Override
    public Void visitBitSelection(OpenPearlParser.BitSelectionContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitBitSelectionSlice:ctx" + CommonUtils.printContext(ctx));

        ASTAttribute attr0 = null;
        ASTAttribute attr1 = null;
        ASTAttribute result = null;
        TypeDefinition typeOfBit = m_type;

        String expr0 = null;
        String expr1 = null;
        int intValue = -1; // impossible value, since the Fixed-const is always >= 0

        visitChildren(ctx);

        ErrorStack.enter(ctx, ".BIT()");

        if (typeOfBit instanceof TypeBit) {
            // we must consider 3 cases for .BIT(expr0:expr1+CONST_FIXED)
            //   expr0 present; expr1 missing (implied CONST_FIXED missing)
            //     --> result: BIT(1);
            //         ConstantSlice will become set if expr1 is constant
            //   expr0 and expr1 present; CONST_FIXED missing
            //     --> both expressions must be constant
            //         --> result is calculated from the constants
            //             ConstantSlice will be set
            //   all 3 present
            //     --> both expressions must be equal
            //        --> result is derived form the CONST_FIXED
            //           ConstantSlice may be set if expr0/1 are constant, but
            //           difficult to calculate

            if (ctx.expression(0) != null) {
                makeExpressionToType(ctx.expression(0), new TypeFixed());
                attr0 = m_ast.lookup(ctx.expression(0));
                expr0 = ctx.expression(0).getText();
                if (! expressionIsFixedWithImplicitDereference(attr0)) {
                    ErrorStack.add(ctx.expression(0),"type mismatch","expected FIXED --- got "+attr0.getType());
                }

            } else {
                ErrorStack.addInternal("visitBitSelectionSlice: missing first expression");
            }

            if (ctx.expression(1) != null) {
                makeExpressionToType(ctx.expression(1), new TypeFixed());
                attr1 = m_ast.lookup(ctx.expression(1));
                expr1 = ctx.expression(1).getText();
                if (! expressionIsFixedWithImplicitDereference(attr1)) {
                    ErrorStack.add(ctx.expression(1),"type mismatch","expected FIXED --- got "+attr0.getType());
                }
            }

            if (ctx.IntegerConstant() != null) {
                intValue = Integer.parseInt(ctx.IntegerConstant().getText());
            }

            if (expr1 == null) {
                result = new ASTAttribute(new TypeBit(1));
                if (attr0.getConstant() != null) {

                    long start = attr0.getConstantFixedValue().getValue();
                    if (start > m_type.getPrecision()) {
                        ErrorStack.add("start index out of range (1.."+typeOfBit.getPrecision()+")");
                    }
                    ConstantSelection slice =
                            new ConstantSelection(
                                    attr0.getConstantFixedValue(), attr0.getConstantFixedValue());
                    result.setConstantSelection(slice);
                }
            } else {
                if (intValue == -1) {
                    // no constant given .BIT(x:x+constant) -> both expressions must be constant
                    if (attr0.getConstant() != null && attr1.getConstant() != null) {
                        long start = attr0.getConstantFixedValue().getValue();
                        long end = attr1.getConstantFixedValue().getValue();
                        long size = end - start + 1;

                        if (start < 1) {
                            ErrorStack.add("start index must be at least 1");
                            size = 1;
                        } else if (start > end) {
                            ErrorStack.add("start index must not be larger than end index");
                            size = 1;
                        }
                        if (end > typeOfBit.getPrecision()) {
                            ErrorStack.add("end index out of range (1.."+typeOfBit.getPrecision()+")");
                        }
                        result = new ASTAttribute(new TypeBit((int) size));

                        ConstantSelection slice =
                                new ConstantSelection(
                                        attr0.getConstantFixedValue(), attr1.getConstantFixedValue());
                        result.setConstantSelection(slice);
                    } else {
                        if (expr0.equals(expr1)) {
                            result = new ASTAttribute(new TypeBit(1));
                        } else {
                            ErrorStack.add("(expr1:expr2+constant) need identical expressions");
                            result =
                                    new ASTAttribute(
                                            new TypeBit(1)); // dummy value for easy method completion
                        }
                    }
                } else {
                    if (!expr0.equals(expr1)) {
                        ErrorStack.add("(expr1:expr2+constant) need identical expressions");
                        result =
                                new ASTAttribute(
                                        new TypeBit(1)); // dummy value for easy method completion
                    } else {
                        if (intValue+1 > typeOfBit.getPrecision()) {
                            ErrorStack.add("selection is larger than BIT("+typeOfBit.getPrecision()+") variable");
                        }
                        result = new ASTAttribute(new TypeBit(intValue + 1));
                    }
                }
            }
            m_ast.put(ctx, result);
        } else {
            ErrorStack.add("must be applied of type BIT -- got "+typeOfBit.toErrorString());
            m_ast.put(ctx, new ASTAttribute(new TypeBit(1))); // easy solution for continuation
        }

        ErrorStack.leave();
        return null;
    }

    private boolean expressionIsFixedWithImplicitDereference(ASTAttribute attr) {

        TypeDefinition savedActualIndexType = attr.getType();

        if (savedActualIndexType instanceof TypeReference) {
            savedActualIndexType = ((TypeReference)savedActualIndexType).getBaseType();
        }
        if (savedActualIndexType instanceof TypeProcedure && ((TypeProcedure)savedActualIndexType).getFormalParameters() == null &&
                ((TypeProcedure)savedActualIndexType).getResultType() != null) {
            savedActualIndexType = ((TypeProcedure)savedActualIndexType).getResultType();
            attr.setIsFunctionCall(true);
        }
        return ((savedActualIndexType instanceof TypeFixed));

    }

    @Override
    public Void visitCharSelection(OpenPearlParser.CharSelectionContext ctx) {

        Log.debug(
                "ExpressionTypeVisitor:visitCharSelectionSlice:ctx"
                        + CommonUtils.printContext(ctx));

        ASTAttribute attr0 = null;
        ASTAttribute attr1 = null;
        String expr0 = null; // we need both expressions to detect .CHAR(x:x+3)
        String expr1 = null;
        int intValue = -1; // impossible value, since the Fixed-const is always >= 0
        TypeDefinition typeOfChar = m_type;
        ErrorStack.enter(ctx, ".CHAR()");

        if (m_type instanceof TypeChar)  {

            visitChildren(ctx);

            if (ctx.IntegerConstant() != null) {
                intValue = Integer.parseInt(ctx.IntegerConstant().getText());
            }

            if (ctx.expression(0) != null) {
                attr0 = m_ast.lookup(ctx.expression(0));
                if (! expressionIsFixedWithImplicitDereference(attr0)) {
                    ErrorStack.add(ctx.expression(0),"type mismatch","expected FIXED --- got "+attr0.getType());
                }
                expr0 = ctx.expression(0).getText();

            } else {
                ErrorStack.addInternal("visitCharSelectionSlice: missing alternative@3521");
            }

            if (ctx.expression(1) != null) {
                attr1 = m_ast.lookup(ctx.expression(1));
                if (! expressionIsFixedWithImplicitDereference(attr1)) {
                    ErrorStack.add(ctx.expression(0),"type mismatch","expected FIXED --- got "+attr1.getType());
                }
                expr1 = ctx.expression(1).getText();
            } else {
                ASTAttribute attr = new ASTAttribute(new TypeChar(1));

                if (attr0.getConstant() != null) {
                    ConstantSelection slice =
                            new ConstantSelection(
                                    attr0.getConstantFixedValue(), attr0.getConstantFixedValue());
                    attr.setConstantSelection(slice);
                }

                m_ast.put(ctx, attr);
            }

            if (attr1 != null) {
                // check if we have 2 constants
                if (attr0.getConstant() != null && attr1.getConstant() != null) {


                    long start = attr0.getConstantFixedValue().getValue();
                    long end = attr1.getConstantFixedValue().getValue();
                    long size = end - start + 1;

                    if (start < 1) {
                        ErrorStack.add("start index must be at least 1");
                        size = 1;
                    } else if (start > end) {
                        ErrorStack.add("start index must not be larger than end index");
                        size = 1;
                    }
                    if (end > typeOfChar.getPrecision()) {
                        ErrorStack.add("end index out of range (1.."+typeOfChar.getPrecision()+")");
                    }

                    ConstantSelection slice =
                            new ConstantSelection(
                                    attr0.getConstantFixedValue(), attr1.getConstantFixedValue());

                    ASTAttribute attr = new ASTAttribute(new TypeChar((int) size));
                    attr.setConstantSelection(slice);
                    m_ast.put(ctx, attr);
                } else {
                    // we have 2 expressions + intValue
                    if (expr0.equals(expr1)) {
                        if (intValue >= 0) {
                            if (intValue + 1 > typeOfChar.getPrecision()) {
                                ErrorStack.add("selection is larger than CHAR("+typeOfChar.getPrecision()+") variable");
                            }
                            m_ast.put(ctx, new ASTAttribute(new TypeChar(intValue + 1)));
                        } else {
                            m_ast.put(ctx, new ASTAttribute(new TypeChar(1)));
                        }
                    } else {
                        TypeVariableChar tvc = new TypeVariableChar();
                        tvc.setBaseType(typeOfChar);
                        m_ast.put(ctx, new ASTAttribute(tvc));
                    }
                }
            }
        } else if (m_type instanceof TypeReference && 
                ((TypeReference)m_type).getBaseType() instanceof TypeRefChar)  {
            ErrorStack.add("REF CHAR() not supported in expressions, yet");
            m_ast.put(ctx, new ASTAttribute(new TypeChar(1))); // easy solution for continuation
        } else {
            ErrorStack.add("must be applied of type CHAR -- got "+typeOfChar.toErrorString());
            m_ast.put(ctx, new ASTAttribute(new TypeChar(1))); // easy solution for continuation
        }
        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitName(OpenPearlParser.NameContext ctx) {
        ASTAttribute attr = null;

        Log.debug("ExpressionTypeVisitor:visitName:ctx=" + CommonUtils.printContext(ctx));
        Log.debug("ExpressionTypeVisitor:visitName:id=" + ctx.ID().toString());
        // System.out.println("name = "+ ctx.getText());

        // ErrorStack.enter(ctx, ctx.ID().toString());

        SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.ID().getText());
        if (entry == null) {
            ErrorStack.add(ctx,null, "'" + ctx.ID().getText()+ "' is not defined");
            //    ErrorStack.leave();
            return null;
        }
        if (entry instanceof ModuleEntry) {
            ErrorStack.add("illegal usage of module name");
            //    ErrorStack.leave();
            return null;
            //        } else if (entry instanceof UserDefinedType) {
            //            ErrorStack.add("illegal usage of type name '"+((UserDefinedType)entry).getName()+"'");
            //            ErrorStack.leave();
            //            return null;
        }

        // seams be be fine, we have a variable or procedure
        // let's get the type of the symbol


        if (treatTaskSemaBoltSignalInterrupt(ctx, entry)) {
            attr = new ASTAttribute(m_type, entry);
        } else if (entry instanceof ProcedureEntry) {
            m_type = ((ProcedureEntry) entry).getType();
            attr = new ASTAttribute(m_type, entry);
        } else if (entry instanceof VariableEntry) {
            VariableEntry var = (VariableEntry) entry;
            m_type = var.getType();
            attr = new ASTAttribute(m_type, entry);
            attr.setVariable(var);
            if (m_type.hasAssignmentProtection()) {
                attr.setIsConstant(true);
            } else {
                attr.setIsLValue(true);
            }
        } else if (entry instanceof SignalEntry) {
            m_type = new TypeSignal();
            attr = new ASTAttribute(m_type, entry);
        } else {
            ErrorStack.addInternal(ctx,"","untreated type of entry "+entry.getName());
            return null;
        }
        m_ast.put(ctx, attr);

        boolean firstLoop = true;

        do {
            if (firstLoop) {
                firstLoop = false;
            } else {
                ctx=ctx.name(); // goto next component
            }
            if (ctx.listOfExpression() != null) {
                if (treatListOfExpressions(ctx) == false) {
                    // failure in listOfExpressions
                    break;
                }
            }
            if (ctx.name() != null) {
                if (getNextStructComponent(ctx) == false) {
                    break;
                }
            }

        } while (ctx.name() != null);

        //System.out.println("resulting type: "+ m_type);
        //  ErrorStack.leave();

        attr.setType(m_type);

        return null;
    }


    // SIGNAL stuff
    // + SchedulingSignalReaction

    @Override
    public Void visitSchedulingSignalReaction(OpenPearlParser.SchedulingSignalReactionContext ctx) {
        // check whether types of RST-variable is FIXED with precision >= 15
        Log.debug("ExpressionTypeVisitor:visitSchedulingSignalReaction");


        visitChildren(ctx);
        ASTAttribute attrName = m_ast.lookup(ctx.name());
        TypeDefinition t = TypeUtilities.performImplicitDereferenceAndFunctioncall(attrName);

        if (!(t instanceof TypeSignal)) {
            ErrorStack.add(ctx.name(),"type mismatch","expected type SIGNAL --- got type "+t);
        }

        if (ctx.signalRST() != null) {
            ASTAttribute attr = m_ast.lookup(ctx.signalRST().name());
            t = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
            //            t = attr.m_type;
            if (t instanceof TypeReference) {
                t = ((TypeReference)t).getBaseType();
            }
            if (t instanceof TypeFixed ) {
                int size = ((TypeFixed)t).getPrecision();
                if (size < 15) {
                    ErrorStack.add(ctx.signalRST().name(),"type mismatch",
                            "expected FIXED(15) or larger --- got " + t) ;
                }
            } else {
                ErrorStack.add(ctx.signalRST().name(),"type mismatch",
                        "expected FIXED(15) or larger --- got " + t) ;
            }

            attr.setType(t);  // update type to basetype

        }
        return null;
    }

    @Override
    public Void visitInduceStatement(OpenPearlParser.InduceStatementContext ctx) {
        // check if name() - if given - is of type SIGNAL
        visitChildren(ctx);

        if (ctx.name() != null) {
            ASTAttribute sig = m_ast.lookup(ctx.name());
            if (sig == null) {
                // error message already emitted
                return null;
            }
                
            TypeDefinition t = sig.getType();

            if (t instanceof TypeReference) {
                t = ((TypeReference)t).getBaseType();
            }
            if (!(t instanceof TypeSignal)) {
                ErrorStack.add(
                        ctx.name(),
                        "INDUCE",
                        "need type SIGNAL -- got " + sig.getType().toString4IMC(false));
            }
            sig.setType(t);  // update type to basetype
        }


        return null;
    }

    // + induceStatement
    private boolean treatListOfExpressions(NameContext ctx) {
        // we have a listOfExpressions
        TypeDefinition safeType = m_type;  // save current type and treat the listOfExpressions 
        visitChildren(ctx.listOfExpression());
        m_type = safeType;                 // restore type

        if (m_type instanceof TypeReference) {

            m_type = ((TypeReference)m_type).getBaseType();

        }
        if (m_type instanceof TypeArray) {
            m_type = ((TypeArray)m_type).getBaseType();
            checkListOfExpressionAsIndices(ctx.listOfExpression());

        } else if (m_type instanceof TypeProcedure) {
            if (((TypeProcedure)m_type).getFormalParameters()== null) {
                ErrorStack.add(ctx.listOfExpression(),"procedure expects no parameters","");
                return false;
            }

            checkListOfExpressionAsParameters(ctx.listOfExpression(),(TypeProcedure)m_type);
            ASTAttribute attr = m_ast.lookup(ctx);
            attr.setIsFunctionCall(true);

            m_type = ((TypeProcedure)m_type).getResultType();
        } else {
            ErrorStack.add("cannot apply indices or parameters on type "+ safeType);
            return false;
        }
        return true;
    }

    /*
     * make the AST-attribute of the context to fixed if this is possible by
     * dereferening or perform function calls
     * if this is not possible, leave the AST-attribute as it was 
     */
    private void makeExpressionToType(ParserRuleContext ctx, TypeDefinition targetType) {
        ASTAttribute attr = m_ast.lookup(ctx);
        TypeDefinition savedActualType = attr.getType();
        Boolean anyThingDone;

        do {
            anyThingDone = false;

            if (savedActualType instanceof TypeReference) {
                savedActualType = ((TypeReference)savedActualType).getBaseType();
                anyThingDone = true;
            }
            if (savedActualType instanceof TypeProcedure && 
                    ((TypeProcedure)savedActualType).getFormalParameters() == null &&
                    ((TypeProcedure)savedActualType).getResultType() != null) {
                savedActualType =  ((TypeProcedure)savedActualType).getResultType() ;
                attr.setIsFunctionCall(true);
                anyThingDone = true;
            }
        } while (anyThingDone);

        if (savedActualType.getName().equals(targetType.getName())) {
            attr.setType(savedActualType);
        }

    }

    private void checkListOfExpressionAsIndices(ListOfExpressionContext ctx) {
        for (int i=0; i<ctx.expression().size(); i++) {
            ASTAttribute attr = m_ast.lookup(ctx.expression(i));
            if (attr == null) {
                // abort treatment of the expression, since there was an error
                // in the expression evaluation
                return;
            }
            TypeDefinition savedActualIndexType = attr.getType();

            makeExpressionToType(ctx.expression(i), new TypeFixed());

            if (savedActualIndexType instanceof TypeFixed) {
                if (((TypeFixed)(savedActualIndexType)).getPrecision() <= 31 ) {
                    continue;
                }
                ErrorStack.add(ctx.expression(i),"type mismatch for index","got "+savedActualIndexType.toErrorString()+" larger than FIXED(31)");
            } else {
                ErrorStack.add(ctx.expression(i),"type mismatch for index","got "+savedActualIndexType.toErrorString()+" expected FIXED");
            }

        }
    }

    private void checkListOfExpressionAsParameters(ListOfExpressionContext ctx, TypeProcedure proc) {
        int nbrOfFormalParameters = proc.getFormalParameters().size();
        int nbrOfActualParameters = ctx.expression().size();
        if (nbrOfFormalParameters != nbrOfActualParameters) {
            ErrorStack.add("number of parameters mismtatch: expected "+nbrOfFormalParameters+" got "+nbrOfActualParameters);
        }


        for (int i=0; i<Math.min(nbrOfActualParameters,nbrOfFormalParameters); i++) {

            ASTAttribute attr = m_ast.lookup(ctx.expression(i));
            if (attr == null) {
                // e.g. variable in expression not defined and error already emitted
                return;
            }
            TypeDefinition savedActualParameterType = attr.getType();
            FormalParameter fp = proc.getFormalParameters().get(i);
            TypeDefinition typeOfFormalParameter = fp.getType();
            TypeDefinition typeOfActualParameter = savedActualParameterType;
            ErrorStack.enter(ctx.expression(i));

            boolean assignable = false;
            if (fp.passIdentical()) {
                // types must be identical or
                // actual parameter must be a lValue if INV is not present
                // if (!attr.isLValue()) {
                //     ErrorStack.add(ctx.expression(i),"not an lvalue",""+attr.m_type+"  "+fp.getType());
                // } else {
                assignable = TypeUtilities.mayBePassedByIdent(typeOfFormalParameter, proc.getFormalParameters().get(i), ctx.expression(i), m_ast);
                //}
            } else {
                assignable = TypeUtilities.mayBeAssignedTo(typeOfFormalParameter, proc.getFormalParameters().get(i), ctx.expression(i), m_ast, false);
            }

            if (assignable) {
                // additional tests for INV and REF violations
                //                if (typeOfActualParameter.hasAssignmentProtection() && !(typeOfFormalParameter.hasAssignmentProtection())) {
                //                    ErrorStack.add("type mismatch: cannot pass INV value to non INV formal parameter");
                //                }

                if (typeOfFormalParameter instanceof TypeReference) {
                    if (!((TypeReference)typeOfFormalParameter).getBaseType().hasAssignmentProtection()) {
                        if (typeOfActualParameter instanceof TypeReference && ((TypeReference)typeOfActualParameter).getBaseType().hasAssignmentProtection()) {
                            ErrorStack.add("type mismatch: cannot pass INV value to non INV formal parameter");
                        }
                    }
                }
            }
            ErrorStack.leave();
        }

    }



    /*
     * check for error situations
     *   * m_type must be a kind of structure (PROC returning (REF?) STRUCT or (REF?) STRUCT
     *   * the component name fits to the structure
     *  if no error: update m_type according and return true
     *  else emit error message and return false   
     */
    private boolean getNextStructComponent(NameContext ctx) {


        if (m_type instanceof TypeReference) {

            m_type = ((TypeReference)m_type).getBaseType();
            if (m_type instanceof TypeSameStructure) {
                m_type =((TypeSameStructure)m_type).getContainerStructure().getStructuredType();
            }
            if (m_type instanceof TypeProcedure) {
                if (((TypeProcedure)m_type).getFormalParameters()!= null) {
                    ErrorStack.add(ctx,"missing parameters for procedure call","");
                    return false;
                }

                m_type = ((TypeProcedure)m_type).getResultType();

            } else  if (m_type instanceof TypeStructure || m_type instanceof UserDefinedTypeStructure) {
                m_type = treatStructComponent(ctx.name());
            }
        } else if (m_type instanceof TypeStructure || m_type instanceof UserDefinedTypeStructure) {
            m_type = treatStructComponent(ctx.name());
        }

        return true;
    }

    private TypeDefinition treatStructComponent(NameContext ctx) {
        String componentName = ctx.ID().getText();
        StructureComponent component = null;
        if (m_type instanceof TypeStructure) {
            component = ((TypeStructure)m_type).lookup(componentName);
        } else {
            component = ((TypeStructure)((UserDefinedTypeStructure)(m_type)).getStructuredType()).lookup(componentName);
        }
        if (component == null) {
            ErrorStack.add("STRUCT/TYPE has no component with name "+componentName);
            m_type = null;
        } else {
            m_type = component.m_type;
        }

        return m_type;
    }

    private boolean treatTaskSemaBoltSignalInterrupt(
            NameContext ctx, SymbolTableEntry entry) {
        TypeDefinition typ = null;

        if (entry instanceof TaskEntry) {
            typ = new TypeTask();
        } else if (entry instanceof SemaphoreEntry) {
            typ = new TypeSemaphore();
        } else if (entry instanceof BoltEntry) {
            typ = new TypeBolt();
        } else if (entry instanceof InterruptEntry) {
            typ = new TypeInterrupt();
        } else if (entry instanceof SignalEntry) {
            typ = new TypeSignal();
        }

        if (typ != null) {
            m_type = typ;
            return true;
        }
        return false;
    }




    @Override
    public Void visitArraySlice(OpenPearlParser.ArraySliceContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitArraySlice:ctx" + CommonUtils.printContext(ctx));

        visitChildren(ctx);

        ErrorStack.enter(ctx, "array slice");

        TypeArraySlice t = new TypeArraySlice();
        ASTAttribute nameAttr = m_ast.lookup(ctx.name());
        if (nameAttr.getType() instanceof TypeArray) {
            t.setBaseType(nameAttr.getType());
        } else {
            ErrorStack.add("must be applied to an array");
        }

        int lastElementInList = ctx.startIndex().listOfExpression().expression().size() - 1;
        ASTAttribute startIndex =
                m_ast.lookup(ctx.startIndex().listOfExpression().expression(lastElementInList));
        ASTAttribute endIndex = m_ast.lookup(ctx.endIndex().expression());
        if (startIndex.getConstant() != null && endIndex.getConstant() != null) {
            t.setStartIndex(startIndex.getConstantFixedValue());
            t.setEndIndex(endIndex.getConstantFixedValue());
            if (t.getTotalNoOfElements() < 1) {
                ErrorStack.add("must select at least 1 element");
            }
        }
        ErrorStack.leave();
        m_ast.put(ctx, new ASTAttribute(t));

        return null;
    }

    /**
     * return the ast-attribute of the expression context
     *
     * <p>if no attribute is found, an error message is emitted and null returned
     *
     * @param expression the desired context
     * @param ctx the current context of the expression
     * @param prefix the error prefix (may be null)
     * @param message the error message
     * @return the AST attribute or null
     */
    private ASTAttribute saveGetAttribute(
            ExpressionContext expression, ParserRuleContext ctx, String prefix, String message) {
        ASTAttribute op = m_ast.lookup(expression);
        if (op == null) {
            ErrorStack.addInternal(ctx, prefix, message);
        }
        return op;
    }


    @Override
    public Void visitListOfExpression(OpenPearlParser.ListOfExpressionContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitListOfExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitOpen_parameter_idf(OpenPearlParser.Open_parameter_idfContext ctx) {
        visitChildren(ctx);
        ASTAttribute result = null;
        if (ctx.name()!= null) {
            result = m_ast.lookup(ctx.name());
        } else if (ctx.stringSelection() != null) {
            result = m_ast.lookup(ctx.stringSelection());
        } else if (ctx.stringConstant() != null) {
            ConstantCharacterValue ccv =
                    getConstantStringLiteral(ctx.stringConstant().StringLiteral());
            int length = ccv.getLength();
            if (length == 0) {
                ErrorStack.add(ctx, "char literal", "need at least 1 character");
            }
            // generate AST Attribute for further analysis
            result = new ASTAttribute(new TypeChar(ccv.getLength()), true);
            ConstantValue cv =
                    m_constantPool.add(ccv); // add to constant pool; maybe we have it already
            result.setConstant(cv);
            m_ast.put(ctx.stringConstant(),result);
        }

        m_ast.put(ctx,result);
        return null;
    }

}
