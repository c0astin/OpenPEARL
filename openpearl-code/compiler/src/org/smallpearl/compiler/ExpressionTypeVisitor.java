/*
 * [The "BSD license"]
 *  Copyright (c) 2012-2016 Marcel Schaible
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

package org.smallpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SymbolTable.*;

import java.util.LinkedList;

public  class ExpressionTypeVisitor extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private org.smallpearl.compiler.SymbolTable.SymbolTable m_symboltable;
    private org.smallpearl.compiler.SymbolTable.SymbolTable m_currentSymbolTable;
    private org.smallpearl.compiler.SymbolTable.ModuleEntry m_module;
    private ParseTreeProperty<ASTAttribute> m_properties = null;
    private Integer m_currFixedLength = null;
    private boolean m_calculateRealFixedLength;

    public ExpressionTypeVisitor(int verbose, boolean debug, SymbolTableVisitor symbolTableVisitor) {

        m_verbose = verbose;
        m_debug = debug;

        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;

        if (m_verbose > 0) {
            System.out.println("Semantic Check: Attributing parse tree with expression type information");
        }

        if (m_debug) {
            System.out.println("Semantic Check: Enabled debugging information");
        }

        LinkedList<ModuleEntry> listOfModules = this.m_symboltable.getModules();

        if (listOfModules.size() > 1) {
            throw new NotYetImplementedException("Multiple modules", 0, 0);
        }

        m_module = listOfModules.get(0);
        m_currentSymbolTable = m_module.scope;
        m_properties = new ParseTreeProperty<ASTAttribute>();
        m_calculateRealFixedLength = false;
    }

    public TypeDefinition lookupType(ParserRuleContext ctx) {
        return m_properties.get(ctx).getType();
    }

    public ASTAttribute lookup(ParserRuleContext ctx) {
        return m_properties.get(ctx);
    }

    @Override
    public Void visitBaseExpression(SmallPearlParser.BaseExpressionContext ctx) {
        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitBaseExpression");

        if (ctx.primaryExpression() != null) {
            visitPrimaryExpression(ctx.primaryExpression());
            ASTAttribute expressionResult = m_properties.get(ctx.primaryExpression());
            if (expressionResult != null) {
                m_properties.put(ctx, expressionResult);

                if (m_debug) {
                    System.out.println("ExpressionTypeVisitor: visitBaseExpression: exp=" + ctx.primaryExpression().getText());
                    System.out.println("ExpressionTypeVisitor: visitBaseExpression: res=(" + expressionResult + ")");
                }
            }
        }
        return null;
    }

    @Override
    public Void visitPrimaryExpression(SmallPearlParser.PrimaryExpressionContext ctx) {
        if (m_debug) {
            System.out.println("ExpressionTypeVisitor: visitPrimaryExpression");
        }

        if (ctx.literal() != null) {
            visitLiteral(ctx.literal());
            ASTAttribute expressionResult = m_properties.get(ctx.literal());
            if (expressionResult != null) {
                m_properties.put(ctx, expressionResult);
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.ID() != null) {
            SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.ID().getText());

            if ( entry == null ) {
                throw  new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }

            if (entry instanceof VariableEntry) {
                ASTAttribute expressionResult;

                VariableEntry variable = (VariableEntry) entry;

                if ( variable.getType() instanceof TypeArray ) {
                    expressionResult = new ASTAttribute(((TypeArray) variable.getType()).getBaseType(), variable.getAssigmentProtection(), variable);
                }
                else {
                    expressionResult = new ASTAttribute(variable.getType(), variable.getAssigmentProtection(), variable);
                }

                m_properties.put(ctx, expressionResult);
            } else if (entry instanceof ProcedureEntry) {
                ProcedureEntry proc = (ProcedureEntry) entry;
                TypeDefinition resultType = proc.getResultType();
                if (resultType != null) {
                    ASTAttribute expressionResult = new ASTAttribute(resultType);
                    m_properties.put(ctx, expressionResult);
                } else {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.semaTry() != null) {
            visit(ctx.semaTry());
            ASTAttribute expressionResult= m_properties.get(ctx.semaTry());
            if (expressionResult != null) {
                m_properties.put(ctx, expressionResult);
            }
        } else if (ctx.stringSlice() != null) {
            visit(ctx.stringSlice());
            ASTAttribute expressionResult= m_properties.get(ctx.stringSlice());
            if (expressionResult != null) {
                m_properties.put(ctx, expressionResult);
            }
        } else if (ctx.expression() != null) {
            if (ctx.expression().size() > 1) {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }

            visit(ctx.expression(0));
            ASTAttribute expressionResult = m_properties.get(ctx.expression(0));
            if (expressionResult != null) {
                m_properties.put(ctx, expressionResult);
            }
        }

        return null;
    }

    @Override
    public Void visitTaskFunction(SmallPearlParser.TaskFunctionContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitTaskFunction");
        }

        TypeTask type = new TypeTask();
        ASTAttribute expressionResult = new ASTAttribute(type);
        m_properties.put(ctx, expressionResult);

        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //
    // -----------+-----------+-----------+-------------+---------------------------------
    // Expression | Type of   | Type of   | Result type | Meaning of operation
    //            | operand 1 | operand 2 |             |
    // -----------+-----------+-----------+-------------+---------------------------------
    // op1 + op2  | FIXED(g1) | FIXED(g2) | FIXED(g3)   | addition of the values of
    //            | FIXED(g1) | FLOAT(g2) | FLOAT(g3)   |the operands op1 and op2
    //            | FLOAT(g1) | FIXED(g2) | FLOAT(g3)   |
    //            | FLOAT(g1) | FLOAT(g2) | FLOAT(g3)   | g3 = max (g1, g2)
    //            | DURATION  | DURATION  | DURATION    |
    //            | DURATION  | CLOCK     | CLOCK       |
    //            | CLOCK     | DURATION  | CLOCK       |

    @Override
    public Void visitAdditiveExpression(SmallPearlParser.AdditiveExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitAdditiveExpression");

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#4");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeClock(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#6");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeClock(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#7");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitSubtractiveExpression(SmallPearlParser.SubtractiveExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitSubtractiveExpression");

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#4");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#5");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeClock(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#6");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#7");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitUnaryAdditiveExpression(SmallPearlParser.UnaryAdditiveExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitUnaryAdditiveExpression");

        visit(ctx.expression());
        op1 = m_properties.get(ctx.expression());

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFixed(((TypeFixed) op1.getType()).getPrecision()));
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnaryAdditiveExpression: rule#1");
        } else if (op1.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op1.getType()).getPrecision()), op1.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnaryAdditiveExpression: rule#2");
        } else if (op1.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnaryAdditiveExpression: rule#3");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitUnarySubtractiveExpression(SmallPearlParser.UnarySubtractiveExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitUnarySubtractiveExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFixed(((TypeFixed) op.getType()).getPrecision()));
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnarySubtractiveExpression: rule#1");
        } else if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnarySubtractiveExpression: rule#2");
        } else if (op.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnarySubtractiveExpression: rule#3");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitNotExpression(SmallPearlParser.NotExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitNotExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeBit) {
            res = new ASTAttribute(new TypeBit(((TypeBit) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: NotExpression: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitAbsExpression(SmallPearlParser.AbsExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitAbsExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFixed(((TypeFixed) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AbsExpression: rule#1");
        } else if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AbsExpression: rule#2");
        } else if (op.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AbsExpression: rule#3");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
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
    public Void visitSignExpression(SmallPearlParser.SignExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitSignExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFixed(1), op.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SignExpression: rule#1");
        } else if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFixed(1), op.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SignExpression: rule#2");
        } else if (op.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeFixed(1), op.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SignExpression: rule#3");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitMultiplicativeExpression(SmallPearlParser.MultiplicativeExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression");

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#4");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#5");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#6");

        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#7");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#8");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitDivideExpression(SmallPearlParser.DivideExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitDivideExpression");

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#4");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#5");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#6");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeDuration(), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#7");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeFloat(24), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#7");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitDivideIntegerExpression(SmallPearlParser.DivideIntegerExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitDivideIntegerExpression");

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideIntegerExpression: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitRemainderExpression(SmallPearlParser.RemainderExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitRemainderExpression");

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitRemainderExpression: rule#1");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitRemainderExpression: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitExponentiationExpression(SmallPearlParser.ExponentiationExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitExponentiationExpression");

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: ExponentiationExpression: rule#1");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op1.getType()).getPrecision()), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: ExponentiationExpression: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitFitExpression(SmallPearlParser.FitExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitFitExpression");

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));

        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = ((TypeFixed) op2.getType()).getPrecision();
            res = new ASTAttribute(new TypeFixed(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: FitExpression: rule#1");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = ((TypeFloat) op2.getType()).getPrecision();
            res = new ASTAttribute(new TypeFloat(precision), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: FitExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    //
    // Reference: OpenPEARL Language Report 6.1 Expressions
    //            Table 6.3: Monadic arithmetical operators
    // -----------+-------------+-----------+-------------+---------------------------------
    // Expression | Type of     |           | Result type | Meaning of operation
    //            | operand     |           |             |
    // -----------+-------------+-----------+-------------+---------------------------------
    // SQRT op    | FLOAT(g)    |           | FLOAT(g)    | square root of operand
    // SIN op     |             |           |             | sine of operand
    // COS op     |             |           |             | cosine of operand
    // EXP op     |             |           |             | e^op with e=2.718281828459
    // LN op      |             |           |             | natural logarithm of operand
    // TAN op     |             |           |             | tangent of operand
    // ATAN op    |             |           |             | arcus tangent of operand
    // TANH op    |             |           |             | tangent hyperbolicus of operand
    // -----------+-------------+-----------+-------------+---------------------------------

    @Override
    public Void visitSqrtExpression(SmallPearlParser.SqrtExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitSqrtExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SqrtExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitSinExpression(SmallPearlParser.SinExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitSinExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SinExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitCosExpression(SmallPearlParser.CosExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitCosExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: CosExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitExpExpression(SmallPearlParser.ExpExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitExpExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: ExpExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitLnExpression(SmallPearlParser.LnExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitLnExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: LnExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitTanExpression(SmallPearlParser.TanExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitTanExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: TanExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitAtanExpression(SmallPearlParser.AtanExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitATanExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: ATanExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitTanhExpression(SmallPearlParser.TanhExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitTanhExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: TanhExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitTOFIXEDExpression(SmallPearlParser.TOFIXEDExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitTOFIXEDExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeBit) {
            res = new ASTAttribute(new TypeFixed(((TypeBit) op.getType()).getPrecision() - 1), op.isConstant());
            m_properties.put(ctx, res);

            if (m_debug) {
                System.out.println("ExpressionTypeVisitor: TOFIXED: rule#1");
            }
        } else if (op.getType() instanceof TypeChar) {
            TypeChar typeChar;

            if (m_debug) {
                System.out.println("ExpressionTypeVisitor: TOFIXED: rule#2");
            }

            typeChar = (TypeChar) op.getType();

            if ( typeChar.getSize() != 1 ) {
                throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }

            res = new ASTAttribute(new TypeFixed(1));
            m_properties.put(ctx, res);
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitTOFLOATExpression(SmallPearlParser.TOFLOATExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitTOFLOATExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            TypeFixed fixedValue = (TypeFixed) op.getType();
            int       precision = 0;

            if ( fixedValue.getPrecision() <= Defaults.FLOAT_SHORT_PRECISION) {
                precision = Defaults.FLOAT_SHORT_PRECISION;
            }
            else {
                precision = Defaults.FLOAT_LONG_PRECISION;
            }

            res = new ASTAttribute(new TypeFloat(precision));
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: TOFLOAT: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitTOBITExpression(SmallPearlParser.TOBITExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitTOBITExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(((TypeFixed) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: TOBIT: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitTOCHARExpression(SmallPearlParser.TOCHARExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitTOCHARExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeChar(1));
            m_properties.put(ctx, res);
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitEntierExpression(SmallPearlParser.EntierExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitEntierExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFixed(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: ENTIER: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitRoundExpression(SmallPearlParser.RoundExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitRoundExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFixed(((TypeFloat) op.getType()).getPrecision()), op.isConstant());
            m_properties.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: ROUND: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitUnaryExpression(SmallPearlParser.UnaryExpressionContext ctx) {
        // TODO:
        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitUnaryExpression");
        throw new NotYetImplementedException("ExpressionTypeVisitor:visitUnaryExpression", ctx.start.getLine(), ctx.start.getCharPositionInLine());

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
    public Void visitSemaTry(SmallPearlParser.SemaTryContext ctx) {
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitSemaTry");

        if (ctx.ID() == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        res = new ASTAttribute(new TypeBit(1));
        m_properties.put(ctx, res);

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: TRY: rule#1");

        return null;
    }

    @Override
    public Void visitLiteral(SmallPearlParser.LiteralContext ctx) {
        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitLiteral");

        if (ctx.durationConstant() != null) {
            ASTAttribute expressionResult = new ASTAttribute(new TypeDuration(), true);
            m_properties.put(ctx, expressionResult);
        } else if (ctx.floatingPointConstant() != null) {
            try {
                double value = Double.parseDouble(ctx.floatingPointConstant().FloatingPointNumberWithoutPrecision().toString());
                Integer precision = 24;
                ASTAttribute expressionResult = new ASTAttribute( new TypeFloat(precision),true);
                m_properties.put(ctx, expressionResult);
            } catch (NumberFormatException ex) {
                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.timeConstant() != null) {
            ASTAttribute expressionResult = new ASTAttribute( new TypeClock(getTime(ctx.timeConstant())),true);
            m_properties.put(ctx, expressionResult);
        } else if (ctx.StringLiteral() != null) {
            ASTAttribute expressionResult = new ASTAttribute(new TypeChar(), true);
            m_properties.put(ctx, expressionResult);
        } else if (ctx.BitStringLiteral() != null) {
            ASTAttribute expressionResult = new ASTAttribute(  new TypeBit(CommonUtils.getBitStringLength(ctx.BitStringLiteral().getText())), true);
            m_properties.put(ctx, expressionResult);
        } else if (ctx.fixedConstant() != null) {
            try {
                int precision = m_currentSymbolTable.lookupDefaultFixedLength();

                if ( ctx.fixedConstant().fixedNumberPrecision() != null) {
                    precision = Integer.parseInt(ctx.fixedConstant().fixedNumberPrecision().IntegerConstant().toString());
                }

                if (m_currFixedLength != null ) {
                    precision = m_currFixedLength;
                }

                m_calculateRealFixedLength = true;
                if ( m_calculateRealFixedLength) {
                    long value = Long.parseLong(ctx.fixedConstant().IntegerConstant().getText());

                    precision = Long.toBinaryString(Math.abs(value)).length();
                    if ( value <  0) {
                        precision++;
                    }
                }

                m_calculateRealFixedLength = false;

//                System.out.println( "ExpressionTypeVisitor:visitLiteral:Fixed: ctx="+ctx.toStringTree()+" precision="+precision);
                ASTAttribute expressionResult = new ASTAttribute(new TypeFixed(precision), true);
                m_properties.put(ctx, expressionResult);
            } catch (NumberFormatException ex) {
                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        }
        return null;
    }

    private Double getTime(SmallPearlParser.TimeConstantContext ctx) {
        Integer hours = 0;
        Integer minutes = 0;
        Double seconds = 0.0;

        hours = (Integer.valueOf(ctx.IntegerConstant(0).toString()) % 24);
        minutes = Integer.valueOf(ctx.IntegerConstant(1).toString());

        if (ctx.IntegerConstant().size() == 3) {
            seconds = Double.valueOf(ctx.IntegerConstant(2).toString());
        }

        if ( ctx.floatingPointConstant() != null ) {
            seconds = Double.valueOf(ctx.floatingPointConstant().FloatingPointNumberWithoutPrecision().toString());
        }

        if (hours < 0 || minutes < 0 || minutes > 59) {
            throw new NotSupportedTypeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return hours * 3600 + minutes * 60 + seconds;
    }


    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor: visitModule");
        }

//        this.m_currentSymbolTable = this.symbolTable.newLevel(moduleEntry);
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }


    @Override
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: vistTaskDeclaration");
        }

        SymbolTableEntry entry = this.m_currentSymbolTable.lookupLocal(ctx.ID().getText());

        if (entry != null) {
            if (entry instanceof TaskEntry) {
                m_currentSymbolTable = ((TaskEntry) entry).scope;
                visitChildren(ctx);
                this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitProcedureDeclaration");
        }

        SymbolTableEntry entry = this.m_currentSymbolTable.lookupLocal(ctx.ID().getText());

        if (entry != null) {
            if (entry instanceof ProcedureEntry) {
                m_currentSymbolTable = ((ProcedureEntry) entry).scope;

                visitChildren(ctx);
                this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        visitChildren(ctx);

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement_from(SmallPearlParser.LoopStatement_fromContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitLoopStatement_From");
        }

        m_calculateRealFixedLength = true;
        visitChildren(ctx);
        m_calculateRealFixedLength = false;

        return null;
    }

    @Override
    public Void visitLoopStatement_to(SmallPearlParser.LoopStatement_toContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitLoopStatement_to");
        }

        m_calculateRealFixedLength = true;
        visitChildren(ctx);
        m_calculateRealFixedLength = false;

        return null;
    }

    @Override
    public Void visitLoopStatement_by(SmallPearlParser.LoopStatement_byContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitLoopStatement_By");
        }

        m_calculateRealFixedLength = true;
        visitChildren(ctx);
        m_calculateRealFixedLength = false;

        return null;
    }

    @Override
    public Void visitAssignment_statement(SmallPearlParser.Assignment_statementContext ctx) {
        String id = null;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitAssignment_statement");
        }
        if ( ctx.stringSelection() != null ) {
            if (ctx.stringSelection().charSelection() != null) {
                id = ctx.stringSelection().charSelection().ID().getText();
            }
            else  if (ctx.stringSelection().bitSelection() != null) {
                id = ctx.stringSelection().bitSelection().ID().getText();
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else {
            id = ctx.ID().getText();
        }

        SymbolTableEntry entry = m_currentSymbolTable.lookup(id);
        if (!(entry instanceof VariableEntry)) {
            throw  new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        VariableEntry var = (VariableEntry)entry;
        if ( var.getType() instanceof TypeFixed) {
            TypeFixed typ = (TypeFixed)(var.getType());
            m_currFixedLength = typ.getPrecision();
        }

        SmallPearlParser.ExpressionContext expr = ctx.expression();
        visitChildren(ctx);

        m_currFixedLength = null;

        return null;
    }

    @Override
    public Void visitNowFunction(SmallPearlParser.NowFunctionContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitNowFunction");
        }

        TypeClock type = new TypeClock();
        ASTAttribute expressionResult = new ASTAttribute(type);
        m_properties.put(ctx, expressionResult);

        return null;
    }

    @Override
    public Void visitSizeofExpression(SmallPearlParser.SizeofExpressionContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitSizeofExpression");
        }

        TypeFixed type = new TypeFixed(Defaults.FIXED_LENGTH);
        ASTAttribute expressionResult = new ASTAttribute(type);
        m_properties.put(ctx, expressionResult);

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
    public Void visitEqRelationalExpression(SmallPearlParser.EqRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#7");
        } else if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#7");

        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitLtRelationalExpression(SmallPearlParser.LtRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#7");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitNeRelationalExpression(SmallPearlParser.NeRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#7");
        } else if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#7");

        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitLeRelationalExpression(SmallPearlParser.LeRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#7");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitGtRelationalExpression(SmallPearlParser.GtRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#7");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitGeRelationalExpression(SmallPearlParser.GeRelationalExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isConstant() && op2.isConstant());
            m_properties.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#7");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitStringSelection(SmallPearlParser.StringSelectionContext ctx) {
        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitStringSelection");
        }

        return null;
//        throw new NotSupportedFeatureException("visitStringSelection", ctx.start.getLine(), ctx.start.getCharPositionInLine(), "???");
    }


    @Override
    public Void visitCshiftExpression(SmallPearlParser.CshiftExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitCshiftExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeFixed) {
            TypeBit type = new TypeBit(((TypeBit)op1.getType()).getPrecision());
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_properties.put(ctx, expressionResult);

            if (m_debug) {
                System.out.println("ExpressionTypeVisitor: Dyadic Boolean and shift operators");
            }
        } else {
            throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }


    @Override
    public Void visitShiftExpression(SmallPearlParser.ShiftExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitShiftExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeFixed) {
            TypeBit type = new TypeBit(((TypeBit)op1.getType()).getPrecision());
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_properties.put(ctx, expressionResult);

            if (m_debug) {
                System.out.println("ExpressionTypeVisitor: Dyadic Boolean and shift operators");
            }
        } else {
            throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitCatExpression(SmallPearlParser.CatExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitCatExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            TypeBit type = new TypeBit(((TypeBit) op1.getType()).getPrecision() + ((TypeBit) op2.getType()).getPrecision());
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_properties.put(ctx, expressionResult);
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            TypeChar type = new TypeChar(((TypeChar)op1.getType()).getSize() + ((TypeChar)op2.getType()).getSize());
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_properties.put(ctx, expressionResult);
        } else {
            throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitAndExpression(SmallPearlParser.AndExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitAndExpression");
        }
        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            TypeBit type = new TypeBit( Math.max( ((TypeBit)op1.getType()).getPrecision(),((TypeBit)op2.getType()).getPrecision()));
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_properties.put(ctx, expressionResult);
        }
        else {
            throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitOrExpression(SmallPearlParser.OrExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitOrExpression");
        }
        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            TypeBit type = new TypeBit( Math.max( ((TypeBit)op1.getType()).getPrecision(),((TypeBit)op2.getType()).getPrecision()));
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_properties.put(ctx, expressionResult);
        }
        else {
            throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitExorExpression(SmallPearlParser.ExorExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitExorExpression");
        }

        visit(ctx.expression(0));
        op1 = m_properties.get(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_properties.get(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            TypeBit type = new TypeBit( Math.max( ((TypeBit)op1.getType()).getPrecision(),((TypeBit)op2.getType()).getPrecision()));
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_properties.put(ctx, expressionResult);
        }
        else {
            throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitCONTExpression(SmallPearlParser.CONTExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitCONTExpression");

        visit(ctx.expression());
        op = m_properties.get(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeReference) {
            ASTAttribute expressionResult = new ASTAttribute(op.getType());
            m_properties.put(ctx, expressionResult);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: CONT: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitStringSlice(SmallPearlParser.StringSliceContext ctx) {
        ASTAttribute res;

        if (m_debug) {
            System.out.println("ExpressionTypeVisitor: visitStringSlice");
        }

        if ( ctx.bitSlice() != null ) {
            int bits = 0;
            if ( ctx.bitSlice() instanceof SmallPearlParser.Case1BitSliceContext) {
                bits = 1;
            } else if ( ctx.bitSlice() instanceof SmallPearlParser.Case2BitSliceContext) {
                SmallPearlParser.Case2BitSliceContext ctx1 = (SmallPearlParser.Case2BitSliceContext)ctx.bitSlice();
                long lowerBoundary;
                long upperBoundary;
                ConstantFixedExpressionEvaluator evaluator = new ConstantFixedExpressionEvaluator(m_verbose, m_debug, m_currentSymbolTable,null, null);

                ConstantValue lower = evaluator.visit(ctx1.constantFixedExpression(0));
                ConstantValue upper = evaluator.visit(ctx1.constantFixedExpression(1));

                if ( !(lower instanceof ConstantFixedValue)) {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }

                if ( !(upper instanceof ConstantFixedValue)) {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }

                lowerBoundary = ((ConstantFixedValue) lower).getValue();
                upperBoundary = ((ConstantFixedValue) upper).getValue();


                bits = (int)upperBoundary - (int)lowerBoundary + 1;
            }

           res = new ASTAttribute(new TypeBit(bits));
        }
        else if ( ctx.charSlice() != null ) {
            res = new ASTAttribute(new TypeChar(1));
        }
        else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        m_properties.put(ctx, res);

        return null;
    }

    @Override
    public Void visitConstantFixedExpressionFit(SmallPearlParser.ConstantFixedExpressionFitContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        if (m_verbose > 0) {
            System.out.println("ExpressionTypeVisitor: visitConstantFixedExpressionFit");
        }

//        visit(ctx.expression(0));
//        op1 = m_properties.get(ctx.expression(0));
//
//        if (op1 == null) {
//            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//        }
//
//        visit(ctx.expression(1));
//        op2 = m_properties.get(ctx.expression(1));
//
//        if (op2 == null) {
//            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//        }
//
//        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
//            TypeBit type = new TypeBit( Math.max( ((TypeBit)op1.getType()).getPrecision(),((TypeBit)op2.getType()).getPrecision()));
//            ASTAttribute expressionResult = new ASTAttribute(type);
//            m_properties.put(ctx, expressionResult);
//        }
//        else {
//            throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//        }

        return null;
    }

    @Override
    public Void visitInitElement(SmallPearlParser.InitElementContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitConstantExpression(SmallPearlParser.ConstantExpressionContext ctx) {
        visitChildren(ctx);
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
    public Void visitLwbDyadicExpression(SmallPearlParser.LwbDyadicExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitLwbDyadicExpression");

        res = new ASTAttribute(new TypeFixed(31), false);
        m_properties.put(ctx, res);

        return null;
    }

    @Override
    public Void visitUpbDyadicExpression(SmallPearlParser.UpbDyadicExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitUpbDyadicExpression");

        res = new ASTAttribute(new TypeFixed(31), false);
        m_properties.put(ctx, res);

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
    public Void visitLwbMonadicExpression(SmallPearlParser.LwbMonadicExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitLwbMonadicExpression");

        res = new ASTAttribute(new TypeFixed(31), false);
        m_properties.put(ctx, res);

        return null;
    }

    @Override
    public Void visitUpbMonadicExpression(SmallPearlParser.UpbMonadicExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: visitUpbMonadicExpression");

        res = new ASTAttribute(new TypeFixed(31), false);
        m_properties.put(ctx, res);

        return null;
    }

    private ConstantValue getConstantExpression(SmallPearlParser.ConstantExpressionContext ctx) {
        ConstantFixedExpressionEvaluator evaluator = new ConstantFixedExpressionEvaluator(m_verbose, m_debug, m_currentSymbolTable,null, null);
        ConstantValue constant = evaluator.visit(ctx.constantFixedExpression());

        return constant;
    }

}