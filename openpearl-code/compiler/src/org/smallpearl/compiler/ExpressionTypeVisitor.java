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

import org.smallpearl.compiler.SmallPearlParser.ExpressionContext;
import org.smallpearl.compiler.SmallPearlParser.ListOfExpressionContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SymbolTable.*;
import org.stringtemplate.v4.ST;

import java.util.LinkedList;

public  class ExpressionTypeVisitor extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private org.smallpearl.compiler.SymbolTable.SymbolTable m_symboltable;
    private org.smallpearl.compiler.SymbolTable.SymbolTable m_currentSymbolTable;
    private org.smallpearl.compiler.SymbolTable.ModuleEntry m_module;
    private ConstantPool m_constantPool;
    private Integer m_currFixedLength = null;
    private boolean m_calculateRealFixedLength;
    private org.smallpearl.compiler.AST m_ast;
    private SymbolTableEntry m_name = null;
    private StructureEntry m_struct = null;
    private TypeDefinition m_type = null;
    private int m_nameDepth = 0;

    public ExpressionTypeVisitor(int verbose, boolean debug, SymbolTableVisitor symbolTableVisitor, 
        ConstantPool constantPool, org.smallpearl.compiler.AST ast) {

        m_verbose = verbose;
        m_debug = debug;

        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        this.m_constantPool = constantPool;
        m_ast = ast;
        m_name = null;
        m_type = null;
        m_nameDepth = 0;

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
    public Void visitBaseExpression(SmallPearlParser.BaseExpressionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitBaseExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.primaryExpression() != null) {
            visitPrimaryExpression(ctx.primaryExpression());
            ASTAttribute expressionResult = m_ast.lookup(ctx.primaryExpression());
            if (expressionResult != null) {
                m_ast.put(ctx, expressionResult);

                Log.debug("ExpressionTypeVisitor: visitBaseExpression: exp=" + ctx.primaryExpression().getText());
                Log.debug("ExpressionTypeVisitor: visitBaseExpression: res=(" + expressionResult + ")");
            }
        }

        return null;
    }

    @Override
    public Void visitPrimaryExpression(SmallPearlParser.PrimaryExpressionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitPrimaryExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.literal() != null) {
            visitLiteral(ctx.literal());
            ASTAttribute expressionResult = m_ast.lookup(ctx.literal());
            if (expressionResult != null) {
                m_ast.put(ctx, expressionResult);
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.name() != null) {
            visitName(ctx.name());
            SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.name().ID().getText());

            String s = ctx.toStringTree();
            if ( entry == null ) {
                throw  new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
            if (ctx.name().listOfExpression() != null && ctx.name().listOfExpression().expression()!= null) {
            	// if we have array indices or function parameters -- we must visit them to get the ASTAttributes
            	visitChildren(ctx.name().listOfExpression());
            }
            if (entry instanceof VariableEntry) {
                ASTAttribute expressionResult;

                VariableEntry variable = (VariableEntry) entry;

                if ( variable.getType() instanceof TypeArray ) {
                	// expressionResult should be TypeArray if no indices are given
                  ParserRuleContext c = ctx.name();
                  ListOfExpressionContext cl = ctx.name().listOfExpression();
                 // List<SmallPearlParser.ExpressionContext> cc = ctx.name().listOfExpression().expression();
                  
                	if (ctx.name().listOfExpression()==null) {
                	   TypeArray ta = (TypeArray) variable.getType();
                	   
                	   expressionResult = new ASTAttribute(ta);
                	   expressionResult.m_readonly = variable.getAssigmentProtection();
                	   expressionResult.m_variable=variable;
                	   
                	} else {
                	   expressionResult = new ASTAttribute(((TypeArray) variable.getType()).getBaseType(), variable.getAssigmentProtection(), variable);
                	}
                }
//                else if ( variable.getType() instanceof TypeFormalParameterArray ) {
//                    expressionResult = new ASTAttribute(((TypeFormalParameterArray) variable.getType()).getBaseType(), variable.getAssigmentProtection(), variable);
//                }
                else {
                    expressionResult = new ASTAttribute(variable.getType(), variable.getAssigmentProtection(), variable);
                }

                m_ast.put(ctx, expressionResult);
            } else if (entry instanceof ProcedureEntry) {
                ProcedureEntry proc = (ProcedureEntry) entry;
                TypeDefinition resultType = proc.getResultType();
                if (resultType != null) {
                    ASTAttribute expressionResult = new ASTAttribute(resultType);
                    m_ast.put(ctx, expressionResult);
                } else {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if(ctx.name() != null) {
            visit(ctx.name());
            ASTAttribute expressionResult= m_ast.lookup(ctx.name());
            if (expressionResult != null) {
                m_ast.put(ctx, expressionResult);
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }

        } else if (ctx.semaTry() != null) {
            visit(ctx.semaTry());
            ASTAttribute expressionResult= m_ast.lookup(ctx.semaTry());
            if (expressionResult != null) {
                m_ast.put(ctx, expressionResult);
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.stringSlice() != null) {
            visit(ctx.stringSlice());
            ASTAttribute expressionResult= m_ast.lookup(ctx.stringSlice());
            if (expressionResult != null) {
                m_ast.put(ctx, expressionResult);
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.name() != null) {
            Log.debug("ExpressionTypeVisitor: visitPrimaryExpression: ctx.name=" + ctx.name().getText());
            m_name = m_currentSymbolTable.lookup(ctx.name().ID().getText());

            if (!(m_name instanceof VariableEntry)) {
                throw  new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }

            visit(ctx.name());
            m_name = null;
        } else if (ctx.expression() != null) {
            if ( ctx.expression() !=  null) {
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
    public Void visitTaskFunction(SmallPearlParser.TaskFunctionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitTaskFunction");
        Log.debug("ExpressionTypeVisitor:visitTaskFunction:ctx" + CommonUtils.printContext(ctx));

        TypeTask type = new TypeTask();
        ASTAttribute expressionResult = new ASTAttribute(type);
        m_ast.put(ctx, expressionResult);

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

        Log.debug("ExpressionTypeVisitor:visitAdditiveExpression");
        Log.debug("ExpressionTypeVisitor:visitAdditiveExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#4");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeClock(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AdditiveExpression: rule#6");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeClock(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitSubtractiveExpression");
        Log.debug("ExpressionTypeVisitor:visitSubtractiveExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#4");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#5");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeClock(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SubtractiveExpression: rule#6");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitUnaryAdditiveExpression");
        Log.debug("ExpressionTypeVisitor:visitUnaryAdditiveExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op1 = m_ast.lookup(ctx.expression());

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFixed(((TypeFixed) op1.getType()).getPrecision()));
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnaryAdditiveExpression: rule#1");
        } else if (op1.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op1.getType()).getPrecision()), op1.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnaryAdditiveExpression: rule#2");
        } else if (op1.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitUnarySubtractiveExpression");
        Log.debug("ExpressionTypeVisitor:visitUnarySubtractiveExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFixed(((TypeFixed) op.getType()).getPrecision()));
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnarySubtractiveExpression: rule#1");
        } else if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: UnarySubtractiveExpression: rule#2");
        } else if (op.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitNotExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeBit) {
            res = new ASTAttribute(new TypeBit(((TypeBit) op.getType()).getPrecision()), op.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitAbsExpression:ctx" + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx,"ABS");
        
        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFixed(((TypeFixed) op.getType()).getPrecision()), op.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AbsExpression: rule#1");
        } else if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AbsExpression: rule#2");
        } else if (op.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: AbsExpression: rule#3");
        } else {
          	ErrorStack.enter(ctx.expression());
        	ErrorStack.add("type '" + op.getType().getName()+ "' not allowed");
        	ErrorStack.leave();
//            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
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
    // SIGN op    | FIXED(g)  |           | FIXED(1)    | determining the sign of op
    //            | FLOAT(g)  |           |             |  1 for op > 0
    //            | DURATION  |           |             |  0 for op = 0
    //            |           |           |             | -1 for op < 0

    @Override
    public Void visitSignExpression(SmallPearlParser.SignExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;
        
        ErrorStack.enter(ctx,"SIGN");
        
        Log.debug("ExpressionTypeVisitor:visitSignExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFixed(1), op.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SignExpression: rule#1");
        } else if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFixed(1), op.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SignExpression: rule#2");
        } else if (op.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeFixed(1), op.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: SignExpression: rule#3");
        } else {
           // throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        	ErrorStack.enter(ctx.expression());
        	ErrorStack.add("type '"+op.getType().getName()+"' not allowed");
            ErrorStack.leave();
        }
        
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
    public Void visitMultiplicativeExpression(SmallPearlParser.MultiplicativeExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitMultiplicativeExpression:ctx" + CommonUtils.printContext(ctx));
        Log.debug("ExpressionTypeVisitor:visitMultiplicativeExpression:ctx.expression(0)" + CommonUtils.printContext(ctx.expression(0)));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#4");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#5");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#6");

        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitMultiplicativeExpression: rule#7");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitDivideExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#4");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#5");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#6");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeDuration(), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: DivideExpression: rule#7");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeFloat(23), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitDivideIntegerExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitRemainderExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitRemainderExpression: rule#1");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = Math.max(((TypeFloat) op1.getType()).getPrecision(), ((TypeFloat) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitExponentiationExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = Math.max(((TypeFixed) op1.getType()).getPrecision(), ((TypeFixed) op2.getType()).getPrecision());
            res = new ASTAttribute(new TypeFixed(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: ExponentiationExpression: rule#1");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeFloat(((TypeFloat) op1.getType()).getPrecision()), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitFitExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));

        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            Integer precision = ((TypeFixed) op2.getType()).getPrecision();
            res = new ASTAttribute(new TypeFixed(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: FitExpression: rule#1");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            Integer precision = ((TypeFloat) op2.getType()).getPrecision();
            res = new ASTAttribute(new TypeFloat(precision), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: FitExpression: rule#2");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

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
    public Void visitSqrtExpression(SmallPearlParser.SqrtExpressionContext ctx) {
        ASTAttribute op;
        
        Log.debug("ExpressionTypeVisitor:visitSqrtExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx.expression(), "SQRT");
        
        treatFixedFloatParameterForMonadicArithmeticOperators((ExpressionContext)ctx, op);

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitSinExpression(SmallPearlParser.SinExpressionContext ctx) {
        ASTAttribute op;
        
        Log.debug("ExpressionTypeVisitor:visitSinExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx.expression(), "SIN");
        
        treatFixedFloatParameterForMonadicArithmeticOperators((ExpressionContext)ctx, op);

        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitCosExpression(SmallPearlParser.CosExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitCosExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx.expression(), "COS");
        
        treatFixedFloatParameterForMonadicArithmeticOperators((ExpressionContext)ctx, op);

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitExpExpression(SmallPearlParser.ExpExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitExpExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx.expression(), "EXP");
        
        treatFixedFloatParameterForMonadicArithmeticOperators((ExpressionContext)ctx, op);

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitLnExpression(SmallPearlParser.LnExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitLnExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx.expression(), "LN");
        
        treatFixedFloatParameterForMonadicArithmeticOperators((ExpressionContext)ctx, op);

        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitTanExpression(SmallPearlParser.TanExpressionContext ctx) {
        ASTAttribute op;

        Log.debug("ExpressionTypeVisitor:visitTanExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());
        ErrorStack.enter(ctx.expression(), "TAN");
        
        treatFixedFloatParameterForMonadicArithmeticOperators((ExpressionContext)ctx, op);

        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitAtanExpression(SmallPearlParser.AtanExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitAtanExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx.expression(), "ATAN");
        
        treatFixedFloatParameterForMonadicArithmeticOperators((ExpressionContext)ctx, op);

        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitTanhExpression(SmallPearlParser.TanhExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitTanhExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx.expression(), "TANH");
        
        treatFixedFloatParameterForMonadicArithmeticOperators((ExpressionContext)ctx, op);

        ErrorStack.leave();

        return null;
    }

    private Void treatFixedFloatParameterForMonadicArithmeticOperators(ExpressionContext ctx, ASTAttribute op) {
        ASTAttribute res;
        
        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

    	if (op.getType() instanceof TypeFloat) {
    		res = new ASTAttribute(new TypeFloat(((TypeFloat) op.getType()).getPrecision()), op.isReadOnly());
    		m_ast.put(ctx, res);
    		if (m_debug)
    			System.out.println("ExpressionTypeVisitor: SqrtExpression: rule#2");
    	} else if (op.getType() instanceof TypeFixed) {
    		int precision = ((TypeFixed) op.getType()).getPrecision();
    		if (precision > Defaults.FLOAT_SHORT_PRECISION) {
    			precision = Defaults.FIXED_MAX_LENGTH;
    		} else {
    			precision = Defaults.FLOAT_SHORT_PRECISION;
    		}
    		res = new ASTAttribute(new TypeFloat(precision), op.isReadOnly());
    		m_ast.put(ctx, res);
    	} else {
    		ErrorStack.add("only FIXED and FLOAT are allowed -- got "+op.getType().toString());
    	}
    	return null;
    }
       // throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());

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

        Log.debug("ExpressionTypeVisitor:visitTOFIXEDExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeBit) {
            res = new ASTAttribute(new TypeFixed(((TypeBit) op.getType()).getPrecision() - 1), op.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug) {
                System.out.println("ExpressionTypeVisitor: TOFIXED: rule#1");
            }
        } else if (op.getType() instanceof TypeChar) {
            TypeChar typeChar;

            if (m_debug) {
                System.out.println("ExpressionTypeVisitor: TOFIXED: rule#2");
            }

            typeChar = (TypeChar) op.getType();

            if ( typeChar.getPrecision() != 1 ) {
                throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }

            res = new ASTAttribute(new TypeFixed(1));
            m_ast.put(ctx, res);
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitTOFLOATExpression(SmallPearlParser.TOFLOATExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitTOFLOATExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

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
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitTOBITExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(((TypeFixed) op.getType()).getPrecision()), op.isReadOnly());
            m_ast.put(ctx, res);
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

        Log.debug("ExpressionTypeVisitor:visitTOCHARExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeChar(1));
            m_ast.put(ctx, res);
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

        Log.debug("ExpressionTypeVisitor:visitEntierExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFixed(((TypeFloat) op.getType()).getPrecision()), op.isReadOnly());
            m_ast.put(ctx, res);
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

        Log.debug("ExpressionTypeVisitor:visitRoundExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeFixed(((TypeFloat) op.getType()).getPrecision()), op.isReadOnly());
            m_ast.put(ctx, res);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: ROUND: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitUnaryExpression(SmallPearlParser.UnaryExpressionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitUnaryExpression:ctx" + CommonUtils.printContext(ctx));
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

        Log.debug("ExpressionTypeVisitor:visitSemaTry:ctx" + CommonUtils.printContext(ctx));

        if (ctx.ID() == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        res = new ASTAttribute(new TypeBit(1));
        m_ast.put(ctx, res);

        if (m_debug)
            System.out.println("ExpressionTypeVisitor: TRY: rule#1");

        return null;
    }

    @Override
    public Void visitLiteral(SmallPearlParser.LiteralContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitLiteral:ctx" + CommonUtils.printContext(ctx));

        if (ctx.durationConstant() != null) {
            ASTAttribute expressionResult = new ASTAttribute(new TypeDuration(), true);
            expressionResult.setConstant(getConstantDurationValue(ctx.durationConstant()));
            m_ast.put(ctx, expressionResult);
        } else if (ctx.floatingPointConstant() != null) {
            try {
                double value = Double.parseDouble(ctx.floatingPointConstant().FloatingPointNumberWithoutPrecision().toString());
                Integer precision = 23;
                ASTAttribute expressionResult = new ASTAttribute( new TypeFloat(precision),true);
                m_ast.put(ctx, expressionResult);
            } catch (NumberFormatException ex) {
                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.timeConstant() != null) {
            ASTAttribute expressionResult = new ASTAttribute( new TypeClock(),true);
            expressionResult.setConstant(getConstantClockValue(ctx.timeConstant()));
            m_ast.put(ctx, expressionResult);
        } else if (ctx.StringLiteral() != null) {
            ConstantCharacterValue ccv = getConstantStringLiteral(ctx.StringLiteral());
            int length = ccv.getLength();
            if (length == 0) {
               ErrorStack.enter(ctx,"string constant");
               ErrorStack.add("need at least 1 character");
               ErrorStack.leave();
            } 
            // generate AST Attribute for further analysis
        	ASTAttribute expressionResult = new ASTAttribute(new TypeChar(ccv.getLength()), true);
        	ConstantValue cv = m_constantPool.add(ccv);   // add to constant pool; maybe we have it already
        	expressionResult.setConstant(cv);
            m_ast.put(ctx, expressionResult);
            
        } else if (ctx.BitStringLiteral() != null) {
            ASTAttribute expressionResult = new ASTAttribute(  new TypeBit(CommonUtils.getBitStringLength(ctx.BitStringLiteral().getText())), true);
            m_ast.put(ctx, expressionResult);
        } else if (ctx.fixedConstant() != null) {
            try {
                int precision = m_currentSymbolTable.lookupDefaultFixedLength();
                
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

                if ( ctx.fixedConstant().fixedNumberPrecision() != null) {
                    precision = Integer.parseInt(ctx.fixedConstant().fixedNumberPrecision().IntegerConstant().toString());
                }


                ASTAttribute expressionResult = new ASTAttribute(new TypeFixed(precision), true);
                m_ast.put(ctx, expressionResult);
            } catch (NumberFormatException ex) {
                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        }
        return null;
    }

    private ConstantCharacterValue getConstantStringLiteral( TerminalNode terminalNode) {
      String s = terminalNode.toString();
    
      ConstantCharacterValue result = new ConstantCharacterValue(s);
      
      return result;
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

    private ConstantClockValue getConstantClockValue(SmallPearlParser.TimeConstantContext ctx) {
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

        return new ConstantClockValue(hours,minutes,seconds);
    }

    private ConstantDurationValue getConstantDurationValue(SmallPearlParser.DurationConstantContext ctx) {
        Integer hours = 0;
        Integer minutes = 0;
        Double seconds = 0.0;

        if (ctx.hours() !=  null) {
           hours = (Integer.valueOf(ctx.hours().IntegerConstant().toString()) % 24);
        }
        
        if (ctx.minutes()!= null) {
           minutes = Integer.valueOf(ctx.minutes().IntegerConstant().toString());
        }
        
        if (ctx.seconds() != null) {
          String s = "";
          if (ctx.seconds().floatingPointConstant() != null) {
        	 s = ctx.seconds().floatingPointConstant().FloatingPointNumberWithoutPrecision().toString();
          } else if (ctx.seconds() != null && ctx.seconds().IntegerConstant() != null) {
            s = ctx.seconds().IntegerConstant().toString();
          } else {
            throw new InternalCompilerErrorException("ConstantDurationValue: how did you reach this point?");
          }
            seconds = Double.valueOf(s);
        }

        if (hours < 0 || minutes < 0 || minutes > 59) {
            throw new NotSupportedTypeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return new ConstantDurationValue(hours,minutes,seconds);
    }

    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitModule:ctx" + CommonUtils.printContext(ctx));

//        this.m_currentSymbolTable = this.symbolTable.newLevel(moduleEntry);
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }


    @Override
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitTaskDeclaration:ctx" + CommonUtils.printContext(ctx));

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
        Log.debug("ExpressionTypeVisitor:visitProcedureDeclaration:ctx" + CommonUtils.printContext(ctx));

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
        Log.debug("ExpressionTypeVisitor:visitBlock_statement:ctx" + CommonUtils.printContext(ctx));

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitLoopStatement:ctx" + CommonUtils.printContext(ctx));

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        visitChildren(ctx);

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement_from(SmallPearlParser.LoopStatement_fromContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitLoopStatement_from:ctx" + CommonUtils.printContext(ctx));

        m_calculateRealFixedLength = true;
        visitChildren(ctx);
        m_calculateRealFixedLength = false;

        return null;
    }

    @Override
    public Void visitLoopStatement_to(SmallPearlParser.LoopStatement_toContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitLoopStatement_to:ctx" + CommonUtils.printContext(ctx));

        m_calculateRealFixedLength = true;
        visitChildren(ctx);
        m_calculateRealFixedLength = false;

        return null;
    }

    @Override
    public Void visitLoopStatement_by(SmallPearlParser.LoopStatement_byContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitLoopStatement_by:ctx" + CommonUtils.printContext(ctx));

        m_calculateRealFixedLength = true;
        visitChildren(ctx);
        m_calculateRealFixedLength = false;

        return null;
    }

    @Override
    public Void visitAssignment_statement(SmallPearlParser.Assignment_statementContext ctx) {
        String id = null;

        Log.debug("ExpressionTypeVisitor:visitAssignment_statement:ctx" + CommonUtils.printContext(ctx));
        ErrorStack.enter(ctx,"assigment");
        
        if ( ctx.stringSelection() != null ) {
            if (ctx.stringSelection().charSelection() != null) {
                id = ctx.stringSelection().charSelection().ID().getText();
                visitStringSelection(ctx.stringSelection());
            }
            else  if (ctx.stringSelection().bitSelection() != null) {
                id = ctx.stringSelection().bitSelection().ID().getText();
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.selector() != null ) {
            Log.debug("ExpressionTypeVisitor:visitAssignment_statement:selector:ctx" + CommonUtils.printContext(ctx.selector()));
            visitSelector(ctx.selector());
            id = ctx.selector().ID().getText();
        } else {
            id = ctx.ID().getText();
        }

        SymbolTableEntry entry = m_currentSymbolTable.lookup(id);
        if (!(entry instanceof VariableEntry)) {
        	//  throw  new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        	ErrorStack.add("'" + id+ "' not defined");
        } else {

        	VariableEntry var = (VariableEntry)entry;
        	if ( var.getType() instanceof TypeFixed) {
        		TypeFixed typ = (TypeFixed)(var.getType());
        		m_currFixedLength = typ.getPrecision();
        	}

        	SmallPearlParser.ExpressionContext expr = ctx.expression();
        
        }
    	visitChildren(ctx);
        m_currFixedLength = null;
        
        ErrorStack.leave();
        
        return null;
    }

    @Override
    public Void visitNowFunction(SmallPearlParser.NowFunctionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitNowFunction:ctx" + CommonUtils.printContext(ctx));

        TypeClock type = new TypeClock();
        ASTAttribute expressionResult = new ASTAttribute(type);
        m_ast.put(ctx, expressionResult);

        return null;
    }

    @Override
    public Void visitSizeofExpression(SmallPearlParser.SizeofExpressionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitSizeofExpression:ctx" + CommonUtils.printContext(ctx));

        TypeFixed type = new TypeFixed(Defaults.FIXED_LENGTH);
        ASTAttribute expressionResult = new ASTAttribute(type);
        m_ast.put(ctx, expressionResult);
        if (ctx.expression() != null) {
           visit(ctx.expression());
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

        Log.debug("ExpressionTypeVisitor:visitEqRelationalExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitEqRelationalExpression: rule#7");
        } else if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitLtRelationalExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLtRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitNeRelationalExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitNeRelationalExpression: rule#7");
        } else if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitLeRelationalExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitLeRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitGtRelationalExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGtRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitGeRelationalExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#1");
        } else if (op1.getType() instanceof TypeFixed && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#2");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFixed) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#3");
        } else if (op1.getType() instanceof TypeFloat && op2.getType() instanceof TypeFloat) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#4");
        } else if (op1.getType() instanceof TypeClock && op2.getType() instanceof TypeClock) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#5");
        } else if (op1.getType() instanceof TypeDuration && op2.getType() instanceof TypeDuration) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#6");
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            res = new ASTAttribute(new TypeBit(1), op1.isReadOnly() && op2.isReadOnly());
            m_ast.put(ctx, res);

            if (m_debug)
                System.out.println("ExpressionTypeVisitor: visitGeRelationalExpression: rule#7");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitStringSelection(SmallPearlParser.StringSelectionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitStringSelection:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }


    @Override
    public Void visitCshiftExpression(SmallPearlParser.CshiftExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug("ExpressionTypeVisitor:visitCshiftExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeFixed) {
            TypeBit type = new TypeBit(((TypeBit)op1.getType()).getPrecision());
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_ast.put(ctx, expressionResult);

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

        Log.debug("ExpressionTypeVisitor:visitShiftExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeFixed) {
            TypeBit type = new TypeBit(((TypeBit)op1.getType()).getPrecision());
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_ast.put(ctx, expressionResult);

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

        Log.debug("ExpressionTypeVisitor:visitCatExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            TypeBit type = new TypeBit(((TypeBit) op1.getType()).getPrecision() + ((TypeBit) op2.getType()).getPrecision());
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_ast.put(ctx, expressionResult);
        } else if (op1.getType() instanceof TypeChar && op2.getType() instanceof TypeChar) {
            TypeChar type = new TypeChar(((TypeChar)op1.getType()).getPrecision() + ((TypeChar)op2.getType()).getPrecision());
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_ast.put(ctx, expressionResult);
        } else {
            throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitAndExpression(SmallPearlParser.AndExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug("ExpressionTypeVisitor:visitAndExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            TypeBit type = new TypeBit( Math.max( ((TypeBit)op1.getType()).getPrecision(),((TypeBit)op2.getType()).getPrecision()));
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_ast.put(ctx, expressionResult);
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

        Log.debug("ExpressionTypeVisitor:visitOrExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            TypeBit type = new TypeBit( Math.max( ((TypeBit)op1.getType()).getPrecision(),((TypeBit)op2.getType()).getPrecision()));
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_ast.put(ctx, expressionResult);
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

        Log.debug("ExpressionTypeVisitor:visitExorExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression(0));
        op1 = m_ast.lookup(ctx.expression(0));

        if (op1 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        visit(ctx.expression(1));
        op2 = m_ast.lookup(ctx.expression(1));

        if (op2 == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
            TypeBit type = new TypeBit( Math.max( ((TypeBit)op1.getType()).getPrecision(),((TypeBit)op2.getType()).getPrecision()));
            ASTAttribute expressionResult = new ASTAttribute(type);
            m_ast.put(ctx, expressionResult);
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

        Log.debug("ExpressionTypeVisitor:visitCONTExpression:ctx" + CommonUtils.printContext(ctx));

        visit(ctx.expression());
        op = m_ast.lookup(ctx.expression());

        if (op == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op.getType() instanceof TypeReference) {
            ASTAttribute expressionResult = new ASTAttribute(op.getType());
            m_ast.put(ctx, expressionResult);
            if (m_debug)
                System.out.println("ExpressionTypeVisitor: CONT: rule#1");
        } else {
            throw new IllegalExpressionException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitStringSlice(SmallPearlParser.StringSliceContext ctx) {
        ASTAttribute res = null;

        Log.debug("ExpressionTypeVisitor:visitStringSlice:ctx" + CommonUtils.printContext(ctx));

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

            if ( ctx.charSlice() instanceof SmallPearlParser.Case1CharSliceContext) {
                 visitCase1CharSlice((SmallPearlParser.Case1CharSliceContext)ctx.charSlice());
                 ASTAttribute expressionResult = m_ast.lookup(ctx.charSlice());
                if (expressionResult != null) {
                    m_ast.put(ctx, expressionResult);
                } else {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            }
            else if ( ctx.charSlice() instanceof SmallPearlParser.Case2CharSliceContext) {
                visitCase2CharSlice((SmallPearlParser.Case2CharSliceContext) ctx.charSlice());
                ASTAttribute expressionResult = m_ast.lookup(ctx.charSlice());
                if (expressionResult != null) {
                    m_ast.put(ctx, expressionResult);
                } else {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            }
            else if ( ctx.charSlice() instanceof SmallPearlParser.Case3CharSliceContext) {
                visitCase3CharSlice((SmallPearlParser.Case3CharSliceContext) ctx.charSlice());
            }
            else if ( ctx.charSlice() instanceof SmallPearlParser.Case4CharSliceContext) {
                visitCase4CharSlice((SmallPearlParser.Case4CharSliceContext) ctx.charSlice());
            }
//            HIER FEHLT NOCH ETWAS!!!!
        }
        else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        m_ast.put(ctx, res);

        return null;
    }

    @Override
    public Void visitConstantFixedExpressionFit(SmallPearlParser.ConstantFixedExpressionFitContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;

        Log.debug("ExpressionTypeVisitor:visitConstantFixedExpressionFit:ctx" + CommonUtils.printContext(ctx));

//        visit(ctx.expression(0));
//        op1 = m_ast.lookup(ctx.expression(0));
//
//        if (op1 == null) {
//            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//        }
//
//        visit(ctx.expression(1));
//        op2 = m_ast.lookup(ctx.expression(1));
//
//        if (op2 == null) {
//            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//        }
//
//        if (op1.getType() instanceof TypeBit && op2.getType() instanceof TypeBit) {
//            TypeBit type = new TypeBit( Math.max( ((TypeBit)op1.getType()).getPrecision(),((TypeBit)op2.getType()).getPrecision()));
//            ASTAttribute expressionResult = new ASTAttribute(type);
//            m_ast.put(ctx, expressionResult);
//        }
//        else {
//            throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//        }

        return null;
    }

    @Override
    public Void visitInitElement(SmallPearlParser.InitElementContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitInitElement:ctx" + CommonUtils.printContext(ctx));

        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitConstantExpression(SmallPearlParser.ConstantExpressionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitConstantExpression:ctx" + CommonUtils.printContext(ctx));

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

        Log.debug("ExpressionTypeVisitor:visitLwbDyadicExpression:ctx" + CommonUtils.printContext(ctx));

        res = new ASTAttribute(new TypeFixed(31), false);
        m_ast.put(ctx, res);

        return null;
    }

    @Override
    public Void visitUpbDyadicExpression(SmallPearlParser.UpbDyadicExpressionContext ctx) {
        ASTAttribute op1;
        ASTAttribute op2;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitUpbDyadicExpression:ctx" + CommonUtils.printContext(ctx));

        res = new ASTAttribute(new TypeFixed(31), false);
        m_ast.put(ctx, res);

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

        Log.debug("ExpressionTypeVisitor:visitLwbMonadicExpression:ctx" + CommonUtils.printContext(ctx));

        res = new ASTAttribute(new TypeFixed(31), false);
        m_ast.put(ctx, res);

        return null;
    }

    @Override
    public Void visitUpbMonadicExpression(SmallPearlParser.UpbMonadicExpressionContext ctx) {
        ASTAttribute op;
        ASTAttribute res;

        Log.debug("ExpressionTypeVisitor:visitUpbMonadicExpression:ctx" + CommonUtils.printContext(ctx));

        res = new ASTAttribute(new TypeFixed(31), false);
        m_ast.put(ctx, res);

        return null;
    }

    @Override
    public Void visitCase1CharSlice(SmallPearlParser.Case1CharSliceContext ctx) {
        ASTAttribute op;
        ASTAttribute res;
        ASTAttribute attr = m_ast.lookup(ctx);

        Log.debug("ExpressionTypeVisitor:visitCase1CharSlice:ctx" + CommonUtils.printContext(ctx));
        Log.debug("ExpressionTypeVisitor:visitCase1CharSlice:id=" + ctx.ID().getText());

        SymbolTableEntry entry = this.m_currentSymbolTable.lookup(ctx.ID().getText());

        visit(ctx.expression());

        ASTAttribute expressionResult = m_ast.lookup(ctx.expression());
        if (expressionResult != null) {
            if ( expressionResult.isReadOnly()) {
                if (expressionResult.getType() instanceof TypeFixed ) {
                    m_ast.put(ctx, new ASTAttribute(new TypeChar(1)));
                }
                else {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            }
            else {
                if ( entry instanceof VariableEntry) {
                    VariableEntry var = (VariableEntry) entry;
                    if ( var.getType() instanceof TypeChar) {
                        m_ast.put(ctx, new ASTAttribute(new TypeChar(1)));
                    }
                    else {
                        throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                    }
                } else {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            }
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitCase2CharSlice(SmallPearlParser.Case2CharSliceContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitCase3CharSlice:ctx" + CommonUtils.printContext(ctx));
        SymbolTableEntry entry = this.m_currentSymbolTable.lookup(ctx.ID().getText());

        visitChildren(ctx);

        ASTAttribute lwb = m_ast.lookup(ctx.expression(0));
        ASTAttribute uwb = m_ast.lookup(ctx.expression(0));

        if ( lwb.isReadOnly() && uwb.isReadOnly()) {
            Log.debug("ExpressionTypeVisitor:visitCase3CharSlice:ctx" + CommonUtils.printContext(ctx));
        }

        if ( entry instanceof VariableEntry) {
            VariableEntry var = (VariableEntry) entry;
            if ( var.getType() instanceof TypeChar) {
                m_ast.put(ctx, new ASTAttribute(new TypeChar(((TypeChar) var.getType()).getPrecision())));
            }
            else {
                throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return null;
    }

    @Override
    public Void visitCase3CharSlice(SmallPearlParser.Case3CharSliceContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitCase3CharSlice:ctx" + CommonUtils.printContext(ctx));
        throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public Void visitCase4CharSlice(SmallPearlParser.Case4CharSliceContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitCase4CharSlice:ctx" + CommonUtils.printContext(ctx));
        throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }


    @Override
    public Void visitCharSelection(SmallPearlParser.CharSelectionContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitCharSelection:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitCharSelectionSlice(SmallPearlParser.CharSelectionSliceContext ctx) {

        Log.debug("ExpressionTypeVisitor:visitCharSelectionSlice:ctx" + CommonUtils.printContext(ctx));

        visitChildren(ctx);

        return null;
    }

    /*
    selector
    : ID indices? ( '.' ID indices? )*
    ;

    indices
    : '(' expression ( ',' expression )* ')'
    ;
     */
    @Override
    public Void visitSelector(SmallPearlParser.SelectorContext ctx) {
        String id = null;

        id = ctx.ID().toString();
        if ( ctx.indices() != null ) {
            visitIndices(ctx.indices());
        }

        SymbolTableEntry entry = m_currentSymbolTable.lookup(id);
        if (!(entry instanceof VariableEntry)) {
            throw  new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        VariableEntry var = (VariableEntry)entry;

        for (int i = 0; i < ctx.selectors().size(); i++) {
            visitSelectors(ctx.selectors(i));
        }

        return null;
    }

    @Override
    public Void visitIndices(SmallPearlParser.IndicesContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitSelectors(SmallPearlParser.SelectorsContext ctx) {
        return visitChildren(ctx);
    }

    private ConstantValue getConstantExpression(SmallPearlParser.ConstantExpressionContext ctx) {
        ConstantFixedExpressionEvaluator evaluator = new ConstantFixedExpressionEvaluator(m_verbose, m_debug, m_currentSymbolTable,null, null);
        ConstantValue constant = evaluator.visit(ctx.constantFixedExpression());

        return constant;
    }

    @Override
    public Void visitName(SmallPearlParser.NameContext ctx) {
        Log.debug("ExpressionTypeVisitor:visitName:ctx=" + CommonUtils.printContext(ctx));
        Log.debug("ExpressionTypeVisitor:visitName:id=" + ctx.ID().toString());

        if ( m_nameDepth == 0 ) {
            SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.ID().getText());

            if ( entry != null ) {
                if (entry instanceof VariableEntry) {
                    VariableEntry var = (VariableEntry) entry;
                    TypeDefinition typ = var.getType();

                    if ( typ instanceof TypeArray) {
                        m_type = ((TypeArray) typ).getBaseType();
                    } else {
                        m_type = typ;
                    }

                    if ( ctx.name() != null) {
                        m_nameDepth++;
                        visitName(ctx.name());
                        m_nameDepth--;
                    }

                    m_ast.put(ctx, new ASTAttribute(m_type));
                }
            } else {
                throw new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine(),(ctx.ID().toString()));
            }
        } else {
            if ( m_type instanceof TypeArray) {
                m_type = ((TypeArray)m_type).getBaseType();
                m_nameDepth++;
                visitName(ctx.name());
                m_nameDepth--;
            } else if (m_type instanceof TypeStructure) {
                TypeStructure typ = (TypeStructure)m_type;
                StructureComponent component = typ.lookup(ctx.ID().getText());

                if (component != null) {
                    if ( component.m_type instanceof TypeArray) {
                        m_type = ((TypeArray) component.m_type).getBaseType();
                        m_nameDepth++;
                        visitName(ctx.name());
                        m_nameDepth--;
                    } else if ( component.m_type instanceof TypeStructure) {
                        m_type = component.m_type;
                        m_nameDepth++;
                        visitName(ctx.name());
                        m_nameDepth--;
                    } else {
                        m_type = component.m_type;
                    }
                } else {
                    throw new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine(),(ctx.ID().toString()));
                }
            }
        }

        return null;
    }

    /**
     * Get the type of name
     *
     * @param entry Symboltable entry of the variable
     * @return TypeDefinition of the name
     */
    private TypeDefinition getTypeDefintion(SymbolTableEntry entry) {
        if ( entry != null ) {
            if (entry instanceof VariableEntry) {
                VariableEntry var = (VariableEntry)entry;

                if (var.getType() instanceof TypeStructure ) {
                }
                else if (var.getType() instanceof TypeArray ) {
                    TypeArray typeArray = (TypeArray)var.getType();
                }
            }
        }

        return null;
    }

}

