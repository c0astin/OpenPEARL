/*
 * [The "BSD license"]
 *  Copyright (c) 2018-2019 Marcel Schaible
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

import org.smallpearl.compiler.Exception.DivisionByZeroException;
import org.smallpearl.compiler.Exception.InternalCompilerErrorException;
import org.smallpearl.compiler.Exception.NumberOutOfRangeException;
import org.smallpearl.compiler.Exception.UnknownIdentifierException;
import org.smallpearl.compiler.SymbolTable.*;

public class ConstantFoldingVisitor extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

    private String m_sourceFileName;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ModuleEntry m_module;
    private int m_value;
    private int m_accumulator;
    private Integer m_currFixedLength = null;
    private boolean m_calculateRealFixedLength;
    private AST m_ast;

    public ConstantFoldingVisitor(SymbolTableVisitor symbolTableVisitor, AST  ast) {
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        Log.info("Constant folding pass");
    }

    @Override
    public Void visitBaseExpression(SmallPearlParser.BaseExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitBaseExpression");
        Log.debug("ConstantFoldingVisitor:visitBaseExpression:ctx" + CommonUtils.printContext(ctx));

        ASTAttribute baseExpr = m_ast.lookup(ctx);

        if (ctx.primaryExpression() != null) {
            visitPrimaryExpression(ctx.primaryExpression());
            ASTAttribute primaryExpr = m_ast.lookup(ctx.primaryExpression());
            baseExpr = primaryExpr;
        }

        return null;
    }

    @Override
    public Void visitPrimaryExpression(SmallPearlParser.PrimaryExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitPrimaryExpression");

        ASTAttribute primaryExpr = m_ast.lookup(ctx);

        if (ctx.literal() != null) {
            visitLiteral(ctx.literal());
            ASTAttribute literal = m_ast.lookup(ctx.literal());
            primaryExpr = literal;
        } else if (ctx.ID() != null) {
            SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.ID().getText());

            if (entry == null) {
                throw new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.semaTry() != null) {
            visit(ctx.semaTry());
        } else if (ctx.stringSlice() != null) {
            visit(ctx.stringSlice());
        } else if (ctx.expression() != null) {
            if (ctx.expression().size() > 1) {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }

            visit(ctx.expression(0));

            ASTAttribute attr = m_ast.lookup(ctx.expression(0));
            primaryExpr = attr;
        }

        return null;
    }

    @Override
    public Void visitLiteral(SmallPearlParser.LiteralContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitLiteral:ctx" + CommonUtils.printContext(ctx));

        if (ctx.durationConstant() != null) {
        } else if (ctx.floatingPointConstant() != null) {
            try {
                double value = Double.parseDouble(ctx.floatingPointConstant().FloatingPointNumberWithoutPrecision().toString());
                Integer precision = 24;
                ASTAttribute expressionResult = new ASTAttribute( new TypeFloat(precision),true);

                ASTAttribute attr = m_ast.lookup(ctx);

                if ( attr == null ) {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }

                attr.setConstantFloatValue( new ConstantFloatValue(value,precision));
                m_ast.put(ctx, expressionResult);
            } catch (NumberFormatException ex) {
                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        } else if (ctx.timeConstant() != null) {
        } else if (ctx.StringLiteral() != null) {
        } else if (ctx.BitStringLiteral() != null) {
        } else if (ctx.fixedConstant() != null) {
            try {
                int precision = m_currentSymbolTable.lookupDefaultFixedLength();

                if ( ctx.fixedConstant().fixedNumberPrecision() != null) {
                    precision = Integer.parseInt(ctx.fixedConstant().fixedNumberPrecision().IntegerConstant().toString());
                }

                if (m_currFixedLength != null ) {
                    precision = m_currFixedLength;
                }

                long value = Long.parseLong(ctx.fixedConstant().IntegerConstant().getText());

                precision = Long.toBinaryString(Math.abs(value)).length();

                if ( value <  0) {
                    precision++;
                }

                Log.debug("ConstantFoldingVisitor:visitLiteral:precision="+precision);
                ASTAttribute attr = m_ast.lookup(ctx);

                if ( attr == null ) {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }

                attr.setConstantFixedValue( new ConstantFixedValue(value));
            } catch (NumberFormatException ex) {
                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        }

        return null;
    }

    @Override
    public Void visitAdditiveExpression(SmallPearlParser.AdditiveExpressionContext ctx) {
        ASTAttribute op1 = null;
        ASTAttribute op2 = null;
        ASTAttribute res = null;

        Log.debug("ConstantFoldingVisitor:visitAdditiveExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.expression(0) != null) {
            visit(ctx.expression(0));
            op1 = m_ast.lookup(ctx.expression(0));
            Log.debug("ConstantFoldingVisitor:visitAdditiveExpression:op1=" + op1);
        }

        if (ctx.expression(1) != null) {
            visit(ctx.expression(1));
            op2 = m_ast.lookup(ctx.expression(1));
            Log.debug("ConstantFoldingVisitor:visitAdditiveExpression:op2=" + op2);
        }

        res = m_ast.lookup(ctx);

        if ( res == null )
        {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if ( op1 != null && op2 != null) {
            if ( op1.isReadOnly() && op2.isReadOnly()) {
                ConstantFixedValue op1Value = op1.getConstantFixedValue();
                ConstantFixedValue op2Value = op1.getConstantFixedValue();

                if (op1Value != null && op2Value != null) {
                    ConstantFixedValue value = new ConstantFixedValue(op1.getConstantFixedValue().getValue() + op2.getConstantFixedValue().getValue());
                    res.setConstantFixedValue(value);
                }
            }
        }

        return null;
    }


    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitModule:ctx" + CommonUtils.printContext(ctx));

        org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry = m_currentSymbolTable.lookupLocal(ctx.ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitProcedureDeclaration:ctx" + CommonUtils.printContext(ctx));

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitTaskDeclaration:ctx" + CommonUtils.printContext(ctx));

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitBlock_statement:ctx" + CommonUtils.printContext(ctx));

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitLoopStatement:ctx" + CommonUtils.printContext(ctx));

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLwbDyadicExpression(SmallPearlParser.LwbDyadicExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitLwbDyadicExpression:ctx" + CommonUtils.printContext(ctx));
        return null;
    }

    @Override
    public Void visitUpbDyadicExpression(SmallPearlParser.UpbDyadicExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitUpbDyadicExpression:ctx" + CommonUtils.printContext(ctx));
        return null;
    }

    @Override
    public Void visitLwbMonadicExpression(SmallPearlParser.LwbMonadicExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitLwbMonadicExpression:ctx" + CommonUtils.printContext(ctx));
        return null;
    }

    @Override
    public Void visitUpbMonadicExpression(SmallPearlParser.UpbMonadicExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitUpbMonadicExpression:ctx" + CommonUtils.printContext(ctx));
        return null;
    }

    @Override
    public Void visitUnaryAdditiveExpression(SmallPearlParser.UnaryAdditiveExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitUnaryAdditiveExpression:ctx" + CommonUtils.printContext(ctx));
        return null;
    }

    @Override
    public Void visitUnarySubtractiveExpression(SmallPearlParser.UnarySubtractiveExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitUnarySubtractiveExpression:ctx" + CommonUtils.printContext(ctx));
        return null;
    }

    @Override
    public Void visitSubtractiveExpression(SmallPearlParser.SubtractiveExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitSubtractiveExpression:ctx" + CommonUtils.printContext(ctx));
        ASTAttribute op1 = null;
        ASTAttribute op2 = null;
        ASTAttribute res = null;

        Log.debug("ConstantFoldingVisitor:visitSubtractiveExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.expression(0) != null) {
            visit(ctx.expression(0));
            op1 = m_ast.lookup(ctx.expression(0));
            Log.debug("ConstantFoldingVisitor:visitSubtractiveExpression:op1=" + op1);
        }

        if (ctx.expression(1) != null) {
            visit(ctx.expression(1));
            op2 = m_ast.lookup(ctx.expression(1));
            Log.debug("ConstantFoldingVisitor:visitSubtractiveExpression:op2=" + op2);
        }

        res = m_ast.lookup(ctx);

        if (res == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if (op1 != null && op2 != null) {
            if (op1.isReadOnly() && op2.isReadOnly()) {
                ConstantFixedValue op1Value = op1.getConstantFixedValue();
                ConstantFixedValue op2Value = op1.getConstantFixedValue();

                if (op1Value != null && op2Value != null) {
                    ConstantFixedValue value = new ConstantFixedValue(op1.getConstantFixedValue().getValue() - op2.getConstantFixedValue().getValue());
                    res.setConstantFixedValue(value);
                }
            }
        }

        return null;
    }

    @Override
    public Void visitNotExpression(SmallPearlParser.NotExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitNotExpression:ctx" + CommonUtils.printContext(ctx));
        return null;
    }

    @Override
    public Void visitAbsExpression(SmallPearlParser.AbsExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitAbsExpression:ctx" + CommonUtils.printContext(ctx));
        return null;
    }

    @Override
    public Void visitSignExpression(SmallPearlParser.SignExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitSignExpression:ctx" + CommonUtils.printContext(ctx));
        return null;
    }

    @Override
    public Void visitMultiplicativeExpression(SmallPearlParser.MultiplicativeExpressionContext ctx) {
        ASTAttribute op1 = null;
        ASTAttribute op2 = null;
        ASTAttribute res = null;

        Log.debug("ConstantFoldingVisitor:visitMultiplicativeExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.expression(0) != null) {
            visit(ctx.expression(0));
            op1 = m_ast.lookup(ctx.expression(0));
            Log.debug("ConstantFoldingVisitor:visitMultiplicativeExpression:op1=" + op1);
        }

        if (ctx.expression(1) != null) {
            visit(ctx.expression(1));
            op2 = m_ast.lookup(ctx.expression(1));
            Log.debug("ConstantFoldingVisitor:visitMultiplicativeExpression:op2=" + op2);
        }

        res = m_ast.lookup(ctx);

        if ( res == null )
        {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if ( op1 != null && op2 != null) {
            if ( op1.isReadOnly() && !op1.isLoopControlVariable() && op2.isReadOnly() && !op2.isLoopControlVariable()) {
                ConstantFixedValue op1Value = op1.getConstantFixedValue();
                ConstantFixedValue op2Value = op1.getConstantFixedValue();

                if (op1Value != null && op2Value != null) {
                    ConstantFixedValue value = new ConstantFixedValue(op1.getConstantFixedValue().getValue() * op2.getConstantFixedValue().getValue());
                    res.setConstantFixedValue(value);
                }
            }
        }

        return null;
    }

    @Override
    public Void visitDivideExpression(SmallPearlParser.DivideExpressionContext ctx) {
        ASTAttribute op1 = null;
        ASTAttribute op2 = null;
        ASTAttribute res = null;

        Log.debug("ConstantFoldingVisitor:visitDivideExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.expression(0) != null) {
            visit(ctx.expression(0));
            op1 = m_ast.lookup(ctx.expression(0));
            Log.debug("ConstantFoldingVisitor:visitDivideExpression:op1=" + op1);
        }

        if (ctx.expression(1) != null) {
            visit(ctx.expression(1));
            op2 = m_ast.lookup(ctx.expression(1));
            Log.debug("ConstantFoldingVisitor:visitDivideExpression:op2=" + op2);
        }

        res = m_ast.lookup(ctx);

        if ( res == null )
        {
            throw new DivisionByZeroException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if ( op1 != null && op2 != null) {
            if ( op1.isReadOnly() && op2.isReadOnly()) {
                ConstantFixedValue op1Value = op1.getConstantFixedValue();
                ConstantFixedValue op2Value = op1.getConstantFixedValue();

                if ( op2Value.getValue() == 0 ) {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }

                if (op1Value != null && op2Value != null) {
                    ConstantFixedValue value = new ConstantFixedValue(op1.getConstantFixedValue().getValue() / op2.getConstantFixedValue().getValue());
                    res.setConstantFixedValue(value);
                }
            }
        }

        return null;
    }

    @Override
    public Void visitDivideIntegerExpression(SmallPearlParser.DivideIntegerExpressionContext ctx) {
        ASTAttribute op1 = null;
        ASTAttribute op2 = null;
        ASTAttribute res = null;

        Log.debug("ConstantFoldingVisitor:visitDivideExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.expression(0) != null) {
            visit(ctx.expression(0));
            op1 = m_ast.lookup(ctx.expression(0));
            Log.debug("ConstantFoldingVisitor:visitDivideExpression:op1=" + op1);
        }

        if (ctx.expression(1) != null) {
            visit(ctx.expression(1));
            op2 = m_ast.lookup(ctx.expression(1));
            Log.debug("ConstantFoldingVisitor:visitDivideExpression:op2=" + op2);
        }

        res = m_ast.lookup(ctx);

        if ( res == null )
        {
            throw new DivisionByZeroException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if ( op1 != null && op2 != null) {
            if ( op1.isReadOnly() && op2.isReadOnly()) {
                ConstantFixedValue op1Value = op1.getConstantFixedValue();
                ConstantFixedValue op2Value = op1.getConstantFixedValue();

                if ( op2Value.getValue() == 0 ) {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }

                if (op1Value != null && op2Value != null) {
                    ConstantFixedValue value = new ConstantFixedValue(op1.getConstantFixedValue().getValue() / op2.getConstantFixedValue().getValue());
                    res.setConstantFixedValue(value);
                }
            }
        }

        return null;
    }

    @Override
    public Void visitRemainderExpression(SmallPearlParser.RemainderExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitRemainderExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitExponentiationExpression(SmallPearlParser.ExponentiationExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitExponentiationExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitFitExpression(SmallPearlParser.FitExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitFitExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitSqrtExpression(SmallPearlParser.SqrtExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitSqrtExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitSinExpression(SmallPearlParser.SinExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitSinExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitCosExpression(SmallPearlParser.CosExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitCosExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitExpExpression(SmallPearlParser.ExpExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitExpExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitLnExpression(SmallPearlParser.LnExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitLnExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitTanExpression(SmallPearlParser.TanExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitTanExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitAtanExpression(SmallPearlParser.AtanExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitAtanExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitTanhExpression(SmallPearlParser.TanhExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitTanhExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }


    @Override
    public Void visitTOFIXEDExpression(SmallPearlParser.TOFIXEDExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitTOFIXEDExpression:ctx" + CommonUtils.printContext(ctx));
        ASTAttribute op = null;

        if (ctx.expression() != null) {
            visit(ctx.expression());
            op = m_ast.lookup(ctx.expression());
            Log.debug("ConstantFoldingVisitor:visitTOFIXEDExpression:op=" + op);
        }

        if ( op != null) {
//            if ( op1.isReadOnly() && op2.isReadOnly()) {
//                ConstantFixedValue op1Value = op1.getConstantFixedValue();
//                ConstantFixedValue op2Value = op1.getConstantFixedValue();
//
//                if ( op2Value.getValue() == 0 ) {
//                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//                }
//
//                if (op1Value != null && op2Value != null) {
//                    ConstantFixedValue value = new ConstantFixedValue(op1.getConstantFixedValue().getValue() / op2.getConstantFixedValue().getValue());
//                    res.setConstantFixedValue(value);
//                }
//            }
        }

        return null;
    }

    @Override
    public Void visitTOFLOATExpression(SmallPearlParser.TOFLOATExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitTOFLOATExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitTOBITExpression(SmallPearlParser.TOBITExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitTOBITExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitTOCHARExpression(SmallPearlParser.TOCHARExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitTOCHARExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitEntierExpression(SmallPearlParser.EntierExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitEntierExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitRoundExpression(SmallPearlParser.RoundExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitRoundExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitUnaryExpression(SmallPearlParser.UnaryExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitUnaryExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitSemaTry(SmallPearlParser.SemaTryContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitSemaTry:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitNowFunction(SmallPearlParser.NowFunctionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitNowFunction:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitSizeofExpression(SmallPearlParser.SizeofExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitSizeofExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitEqRelationalExpression(SmallPearlParser.EqRelationalExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitEqRelationalExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitLtRelationalExpression(SmallPearlParser.LtRelationalExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitLtRelationalExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitNeRelationalExpression(SmallPearlParser.NeRelationalExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitNeRelationalExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitLeRelationalExpression(SmallPearlParser.LeRelationalExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitLeRelationalExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitGtRelationalExpression(SmallPearlParser.GtRelationalExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitGtRelationalExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitGeRelationalExpression(SmallPearlParser.GeRelationalExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitGeRelationalExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitStringSelection(SmallPearlParser.StringSelectionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitStringSelection:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitCshiftExpression(SmallPearlParser.CshiftExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitCshiftExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitShiftExpression(SmallPearlParser.ShiftExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitShiftExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitCatExpression(SmallPearlParser.CatExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitCatExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitAndExpression(SmallPearlParser.AndExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitAndExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitOrExpression(SmallPearlParser.OrExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitOrExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitExorExpression(SmallPearlParser.ExorExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitExorExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitCONTExpression(SmallPearlParser.CONTExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitCONTExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitStringSlice(SmallPearlParser.StringSliceContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitStringSlice:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitConstantFixedExpressionFit(SmallPearlParser.ConstantFixedExpressionFitContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitConstantFixedExpressionFit:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitInitElement(SmallPearlParser.InitElementContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitInitElement:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitConstantExpression(SmallPearlParser.ConstantExpressionContext ctx) {
        Log.debug("ConstantFoldingVisitor:visitConstantExpression:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);
        return null;
    }


}
