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

package org.openpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.openpearl.compiler.Exception.InternalCompilerErrorException;
import org.openpearl.compiler.Exception.NumberOutOfRangeException;
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;

/**
 * @author Marcel Schaible
 * @version 1
 *     <p><b>Description</b>
 */

public class ConstantPoolVisitor extends OpenPearlBaseVisitor<Void>
        implements OpenPearlVisitor<Void> {
    private int m_verbose;
    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ConstantPool m_constantPool = null;
    private Integer m_currFixedLength = null;
    private AST m_ast = null;

    public ConstantPoolVisitor(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ConstantPool constantPool,
            ExpressionTypeVisitor expressionTypeVisitor, AST ast) {


        m_debug = debug;
        m_verbose = verbose;
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_ast = ast;
        m_currentSymbolTable = m_symboltable;
        m_constantPool = constantPool;
        new ParseTreeProperty<TypeDefinition>();
        if (m_verbose > 0) {
            System.out.println("Start ConstantPoolVisitor");
        }

        // Add commonly used constants:
        m_constantPool.add(new ConstantFixedValue(0, Defaults.FIXED_LENGTH));
        m_constantPool.add(new ConstantFixedValue(1, Defaults.FIXED_LENGTH));
        m_constantPool.add(new ConstantFixedValue(-1, Defaults.FIXED_LENGTH));
    }

    public Void add(ConstantValue value) {
        int i;
        int constantBitNo = 0;

        boolean found = false;

        for (i = 0; i < ConstantPool.constantPool.size(); i++) {
            if (value instanceof ConstantFixedValue) {
                if (ConstantPool.constantPool.get(i) instanceof ConstantFixedValue) {
                    ConstantFixedValue a = (ConstantFixedValue) (value);
                    ConstantFixedValue b = (ConstantFixedValue) (ConstantPool.constantPool.get(i));

                    if (a.getValue() == b.getValue() && a.getPrecision() == b.getPrecision()) {
                        found = true;
                        break;
                    }
                }
            } else if (value instanceof ConstantFloatValue) {
                if (ConstantPool.constantPool.get(i) instanceof ConstantFloatValue) {
                    if (Double.compare(((ConstantFloatValue) (value)).getValue(),
                            ((ConstantFloatValue) (ConstantPool.constantPool.get(i)))
                                    .getValue()) == 0) {
                        found = true;
                        break;
                    }
                }
            } else if (value instanceof ConstantCharacterValue) {
                if (ConstantPool.constantPool.get(i) instanceof ConstantCharacterValue) {
                    String s1 = ((ConstantCharacterValue) (value)).getValue();
                    String s2 = ((ConstantCharacterValue) (ConstantPool.constantPool.get(i)))
                            .getValue();

                    if (s1.equals(s2)) {
                        found = true;
                        break;
                    }
                }
            } else if (value instanceof ConstantBitValue) {
                if (ConstantPool.constantPool.get(i) instanceof ConstantBitValue) {
                    constantBitNo = constantBitNo + 1;
                    String s1 = ((ConstantBitValue) (value)).getValue();
                    String s2 = ((ConstantBitValue) (ConstantPool.constantPool.get(i))).getValue();

                    if (s1.equals(s2)) {
                        found = true;
                        break;
                    }
                }
            } else if (value instanceof ConstantDurationValue) {
                if (ConstantPool.constantPool.get(i) instanceof ConstantDurationValue) {
                    ConstantDurationValue other = (ConstantDurationValue) value;
                    ConstantDurationValue constant =
                            ((ConstantDurationValue) (ConstantPool.constantPool.get(i)));

                    if (constant.equals(other)) {
                        found = true;
                        break;
                    }
                }
            }
        }

        if (!found) {
            m_constantPool.add(value);
        }

        return null;
    }

    @Override
    public Void visitBaseExpression(OpenPearlParser.BaseExpressionContext ctx) {
        Log.debug("ConstantPoolVisitor:visitBaseExpression:ctx" + CommonUtils.printContext(ctx));

        if (ctx.primaryExpression() != null) {
            visitPrimaryExpression(ctx.primaryExpression());
        }

        return null;
    }


//    @Override
//    public Void visitLiteral(OpenPearlParser.LiteralContext ctx) {
//        Log.debug("ConstantPoolVisitor: visitLiteral");
//
//        ASTAttribute attr = m_ast.lookup(ctx);
//        String ss = ctx.getText();
//
//        // we should have for all literals an ASTAttribute from the ExpressionTypeVisitor
//        if (attr == null) {
//            ErrorStack.enter(ctx, "ConstantPoolVisitor::visitLiteral");
//            ErrorStack.addInternal("no AST attribute found " + ctx.getText());
//            ErrorStack.leave();
//        }
//
//        if (ctx.durationConstant() != null) {
//            m_constantPool.add(CommonUtils.getConstantDurationValue(ctx.durationConstant(), 1));
//        } else if (ctx.timeConstant() != null) {
//            int hours = 0;
//            int minutes = 0;
//            double seconds = 0.0;
//            ErrorStack.enter(ctx.timeConstant(), "CLOCK value");
//            hours = Integer.valueOf(ctx.timeConstant().IntegerConstant(0).toString());
//            hours %= 24;
//
//            minutes = Integer.valueOf(ctx.timeConstant().IntegerConstant(1).toString());
//            if (minutes < 0 || minutes > 59) {
//                ErrorStack.add("minutes must be in range 0..59");
//            }
//
//            if (ctx.timeConstant().IntegerConstant(2) != null) {
//                seconds = Double.valueOf(ctx.timeConstant().IntegerConstant(2).toString());
//            } else if (ctx.timeConstant().floatingPointConstant() != null) {
//                seconds = CommonUtils
//                        .getFloatingPointConstantValue(ctx.timeConstant().floatingPointConstant());
//            }
//            if (seconds < 0.0 || seconds >= 60.0) {
//                ErrorStack.add("seconds must be in range 0..59");
//            }
//            ErrorStack.leave();
//            m_constantPool.add(new ConstantClockValue(hours, minutes, seconds));
//        } else if (ctx.BitStringLiteral() != null) {
//            long value = CommonUtils.convertBitStringToLong(ctx.BitStringLiteral().getText());
//            int nb = CommonUtils.getBitStringLength(ctx.BitStringLiteral().getText());
//            ConstantBitValue cbv = new ConstantBitValue(value, nb);
//            m_constantPool.add(cbv);
//            attr.setConstant(cbv);
//        } else if (ctx.floatingPointConstant() != null) {
//            try {
//                double value = 0.0;
//                int precision =
//                        CommonUtils.getFloatingPointConstantPrecision(ctx.floatingPointConstant(),
//                                m_currentSymbolTable.lookupDefaultFloatLength());
//
//                value = CommonUtils.getFloatingPointConstantValue(ctx.floatingPointConstant());
//
//                ConstantFloatValue cfv = new ConstantFloatValue(value, precision);
//                m_constantPool.add(cfv);
//                attr.setConstant(cfv);
//            } catch (NumberFormatException ex) {
//                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(),
//                        ctx.start.getCharPositionInLine());
//            }
//
//        } else if (ctx.StringLiteral() != null) {
//            if (attr.m_constant != null) {
//                m_constantPool.add(attr.m_constant);
//            } else {
//                String s = ctx.StringLiteral().toString();
//                //s = CommonUtils.removeQuotes(s);
//                // s = CommonUtils.compressPearlString(s);
//                // s = CommonUtils.unescapePearlString(CommonUtils.removeQuotes(s));
//                m_constantPool.add(new ConstantCharacterValue(s));
//                System.out.println("ConstPoolVisitor: should never be called: " + s);
//            }
//        } else if (ctx.fixedConstant() != null) {
//            try {
//                long value;
//                int precision;
//
//                if (m_currFixedLength != null) {
//                    precision = m_currFixedLength;
//                }
//
//                value = Long.parseLong(ctx.fixedConstant().IntegerConstant().toString());
//                int precisionOfLiteral = Long.toBinaryString(Math.abs(value)).length();
//                if (value < 0) {
//                    precisionOfLiteral++;
//                }
//
//                // calculate the precision
//                // the language report states:
//                //  the precision is ether explicitly given  
//                //  or the precision is derived from the length statement
//
//                // the constant fixed expression evaluator is not affected by this
//                // treatment. ConstantFixedExpressions are calculated only upon the
//                // current values
//
//                precision = precisionOfLiteral; // = m_currentSymbolTable.lookupDefaultFixedLength();
//
//                if (ctx.fixedConstant().fixedNumberPrecision() != null) {
//                    precision = Integer.parseInt(ctx.fixedConstant().fixedNumberPrecision()
//                            .IntegerConstant().toString());
//                }
//
//                if (precisionOfLiteral > precision) {
//                    ErrorStack.enter(ctx, "constant");
//                    ErrorStack.add("value too large for precision " + precision);
//                    ErrorStack.leave();
//                }
//
//                ConstantFixedValue cfv = new ConstantFixedValue(value, precision);
//                m_constantPool.add(cfv);
//
//                attr.setConstant(cfv);
//            } catch (NumberFormatException ex) {
//                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(),
//                        ctx.start.getCharPositionInLine());
//            }
//        }
//
//        return null;
//    }

    @Override
    public Void visitUnarySubtractiveExpression(
            OpenPearlParser.UnarySubtractiveExpressionContext ctx) {
        if (m_debug)
            System.out.println("ConstantPoolVisitor: visitUnarySubtractiveExpression");

        if (ctx.getChild(1) instanceof OpenPearlParser.BaseExpressionContext) {
            OpenPearlParser.BaseExpressionContext base_ctx =
                    (OpenPearlParser.BaseExpressionContext) (ctx.getChild(1));

            if (base_ctx.primaryExpression() != null) {
                OpenPearlParser.PrimaryExpressionContext primary_ctx =
                        base_ctx.primaryExpression();

                if (primary_ctx.getChild(0) instanceof OpenPearlParser.ConstantContext) {
                    OpenPearlParser.ConstantContext literal_ctx =
                            (OpenPearlParser.ConstantContext) (primary_ctx.getChild(0));

                    if (literal_ctx.floatingPointConstant() != null) {
                        try {
                            double value = -1 * CommonUtils.getFloatingPointConstantValue(
                                    literal_ctx.floatingPointConstant());
                            int precision = CommonUtils.getFloatingPointConstantPrecision(
                                    literal_ctx.floatingPointConstant(),
                                    m_currentSymbolTable.lookupDefaultFloatLength());
                            add(new ConstantFloatValue(value, precision));
                        } catch (NumberFormatException ex) {
                            throw new NumberOutOfRangeException(ctx.getText(),
                                    literal_ctx.start.getLine(),
                                    literal_ctx.start.getCharPositionInLine());
                        }
                    } else if (literal_ctx.fixedConstant() != null) {
                        try {
                            Integer value = null;
                            int precision = m_currentSymbolTable.lookupDefaultFixedLength();

                            if (literal_ctx.fixedConstant().fixedNumberPrecision() != null) {
                                precision = Integer.parseInt(literal_ctx.fixedConstant()
                                        .fixedNumberPrecision().IntegerConstant().toString());
                            }

                            value = -1 * Integer.parseInt(
                                    literal_ctx.fixedConstant().IntegerConstant().toString());

                            add(new ConstantFixedValue(value, precision));
                        } catch (NumberFormatException ex) {
                            throw new NumberOutOfRangeException(ctx.getText(),
                                    literal_ctx.start.getLine(),
                                    literal_ctx.start.getCharPositionInLine());
                        }
                    } else if (literal_ctx.stringConstant() != null) {
                        System.out
                                .println("string:(" + literal_ctx.stringConstant().toString() + ")");
                    } else if (literal_ctx.floatingPointConstant() != null) {
                        try {
                            double value = -1 * CommonUtils.getFloatingPointConstantValue(
                                    literal_ctx.floatingPointConstant());
                            int precision = CommonUtils.getFloatingPointConstantPrecision(
                                    literal_ctx.floatingPointConstant(),
                                    m_currentSymbolTable.lookupDefaultFloatLength());
                            add(new ConstantFloatValue(value, precision));
                        } catch (NumberFormatException ex) {
                            throw new NumberOutOfRangeException(ctx.getText(),
                                    literal_ctx.start.getLine(),
                                    literal_ctx.start.getCharPositionInLine());
                        }
                    } else if (literal_ctx.timeConstant() != null) {
                        System.out.println("time:(" + ")");
                    } else if (literal_ctx.durationConstant() != null) {
                        add(CommonUtils.getConstantDurationValue(literal_ctx.durationConstant(),
                                -1));
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Void visitAdditiveExpression(OpenPearlParser.AdditiveExpressionContext ctx) {
        Log.debug(
                "ConstantPoolVisitor:visitAdditiveExpression:ctx" + CommonUtils.printContext(ctx));

        if (!addFixedConstant(ctx)) {
            visitChildren(ctx);
        }

        return null;
    }

    @Override
    public Void visitSubtractiveExpression(OpenPearlParser.SubtractiveExpressionContext ctx) {
        Log.debug("ConstantPoolVisitor:visitSubtractiveExpression:ctx"
                + CommonUtils.printContext(ctx));

        if (!addFixedConstant(ctx)) {
            visitChildren(ctx);
        }

        return null;
    }

    @Override
    public Void visitDivideExpression(OpenPearlParser.DivideExpressionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitDivideIntegerExpression(OpenPearlParser.DivideIntegerExpressionContext ctx) {
        Log.debug("ConstantPoolVisitor:visitDivideIntegerExpression:ctx"
                + CommonUtils.printContext(ctx));

        if (!addFixedConstant(ctx)) {
            visitChildren(ctx);
        }

        return null;
    }

    @Override
    public Void visitConstant(OpenPearlParser.ConstantContext ctx) {
        Log.debug("ConstantPoolVisitor:visitConstant:ctx" + CommonUtils.printContext(ctx));

        int sign = 1;

        if (ctx.sign() != null) {
            if (ctx.sign() instanceof OpenPearlParser.SignMinusContext) {
                sign = -1;
            }
        }

        if (ctx.fixedConstant() != null) {
            try {
                long value;
                int precision = m_currentSymbolTable.lookupDefaultFixedLength();

                value = sign * Long.parseLong(ctx.fixedConstant().IntegerConstant().toString());
                value = sign * Long.parseLong(ctx.fixedConstant().IntegerConstant().toString());
                m_constantPool.add(new ConstantFixedValue(value));
            } catch (NumberFormatException ex) {
                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
            }
        } else if (ctx.stringConstant() != null) {
            String s = ctx.stringConstant().StringLiteral().toString();
            //s = CommonUtils.removeQuotes(s);
            //s = CommonUtils.compressPearlString(s);
            m_constantPool.add(new ConstantCharacterValue(s));
        } else if (ctx.durationConstant() != null) {
            Integer hours = 0;
            Integer minutes = 0;
            Double seconds = 0.0;

            if (ctx.durationConstant().hours() != null) {
                hours = Integer
                        .valueOf(ctx.durationConstant().hours().IntegerConstant().toString());
            }
            if (ctx.durationConstant().minutes() != null) {
                minutes = Integer
                        .valueOf(ctx.durationConstant().minutes().IntegerConstant().toString());
            }
            if (ctx.durationConstant().seconds() != null) {
                if (ctx.durationConstant().seconds().IntegerConstant() != null) {
                    seconds = Double
                            .valueOf(ctx.durationConstant().seconds().IntegerConstant().toString());
                } else if (ctx.durationConstant().seconds().floatingPointConstant() != null) {
                    seconds = CommonUtils.getFloatingPointConstantValue(
                            ctx.durationConstant().seconds().floatingPointConstant());
                }
            }

            ConstantDurationValue durationConst =
                    new ConstantDurationValue(hours, minutes, seconds, sign);
            m_constantPool.add(durationConst);
        } else if (ctx.timeConstant() != null) {
            Integer hours = 0;
            Integer minutes = 0;
            Double seconds = 0.0;

            hours = Integer.valueOf(ctx.timeConstant().IntegerConstant(0).toString());
            minutes = Integer.valueOf(ctx.timeConstant().IntegerConstant(1).toString());

            if (ctx.timeConstant().floatingPointConstant() != null) {
                seconds = CommonUtils
                        .getFloatingPointConstantValue(ctx.timeConstant().floatingPointConstant());
            } else if (ctx.timeConstant().IntegerConstant(2) != null) {
                seconds = Double.valueOf(ctx.timeConstant().IntegerConstant(2).toString());
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
            }

            ConstantClockValue clockConst = new ConstantClockValue(hours, minutes, seconds);
            m_constantPool.add(clockConst);
        } else if (ctx.floatingPointConstant() != null) {
            try {
                double value = 0.0;
                int precision = m_currentSymbolTable.lookupDefaultFloatLength();

                /***
                 // walk up the AST and get VariableDenotationContext:
                 ParserRuleContext  sctx =  ctx.getParent();
                 while ( sctx != null && !(sctx instanceof OpenPearlParser.VariableDenotationContext)) {
                 sctx = sctx.getParent();
                 }
                
                 if (sctx != null) {
                 OpenPearlParser.TypeAttributeContext typeAttributeContext = ((OpenPearlParser.VariableDenotationContext)sctx).typeAttribute();
                 if ( typeAttributeContext.simpleType() != null ) {
                 OpenPearlParser.SimpleTypeContext simpleTypeContext = typeAttributeContext.simpleType();
                
                 if( simpleTypeContext.typeFloatingPointNumber() != null ) {
                 OpenPearlParser.TypeFloatingPointNumberContext typeFloatingPointNumberContext = simpleTypeContext.typeFloatingPointNumber();
                
                 if ( typeFloatingPointNumberContext.IntegerConstant() != null) {
                 precision = Integer.valueOf(typeFloatingPointNumberContext.IntegerConstant().toString());
                 }
                 }
                 }
                
                 }
                 else {
                 throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                 }
                 ***/
                value = sign
                        * CommonUtils.getFloatingPointConstantValue(ctx.floatingPointConstant());
                m_constantPool.add(new ConstantFloatValue(value, precision));
            } catch (NumberFormatException ex) {
                throw new NumberOutOfRangeException(ctx.getText(), ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
            }
        } else if (ctx.bitStringConstant() != null) {
            long value = CommonUtils
                    .convertBitStringToLong(ctx.bitStringConstant().BitStringLiteral().getText());
            int nb = CommonUtils
                    .getBitStringLength(ctx.bitStringConstant().BitStringLiteral().getText());
            ConstantBitValue cbv = new ConstantBitValue(value, nb);
            m_constantPool.add(cbv);
        }

        return null;
    }


    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        Log.debug("ConstantPoolVisitor:visitModule:ctx" + CommonUtils.printContext(ctx));

        org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        Log.debug("ConstantPoolVisitor:visitProcedureDeclaration:ctx"
                + CommonUtils.printContext(ctx));
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        Log.debug("ConstantPoolVisitor:visitTaskDeclaration:ctx" + CommonUtils.printContext(ctx));
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        Log.debug("ConstantPoolVisitor:visitBlock_statement:ctx" + CommonUtils.printContext(ctx));
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        Log.debug("ConstantPoolVisitor:visitLoopStatement:ctx" + CommonUtils.printContext(ctx));
        int precision;

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        if (ctx.loopStatement_for() == null) {
            visitChildren(ctx);
        } else {
            SymbolTableEntry entry =
                    m_currentSymbolTable.lookup(ctx.loopStatement_for().ID().getText());
            VariableEntry var = null;

            if (entry != null && entry instanceof VariableEntry) {
                var = (VariableEntry) entry;
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
            }
            TypeFixed fromType = null;
            TypeFixed toType = null;

            ASTAttribute fromRes = null;
            ASTAttribute toRes = null;
            if (ctx.loopStatement_from() != null) {
                visit(ctx.loopStatement_from().expression());
                fromRes = m_ast.lookup(ctx.loopStatement_from().expression());
                TypeDefinition typ = var.getType();
                if (typ instanceof TypeFixed) {
                    fromType = (TypeFixed) typ;
                } else {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                            ctx.start.getCharPositionInLine());
                }
            }

            if (ctx.loopStatement_to() != null) {
                toRes = m_ast.lookup(ctx.loopStatement_to().expression());
                TypeDefinition typ = var.getType();

                visit(ctx.loopStatement_to().expression());

                if (typ instanceof TypeFixed) {
                    toType = (TypeFixed) typ; //TODO stimmt nicht!!!!
                } else {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                            ctx.start.getCharPositionInLine());
                }
            }

            precision = m_currentSymbolTable.lookupDefaultFixedLength();

            if (fromType != null) {
                if (toType != null) {
                    precision = Math.max(((TypeFixed) fromRes.getType()).getPrecision(),
                            ((TypeFixed) toRes.getType()).getPrecision());
                } else {
                    precision = ((TypeFixed) fromRes.getType()).getPrecision();
                }
            } else if (toType != null) {
                precision = ((TypeFixed) toRes.getType()).getPrecision();
            }
        }

        for (int i = 0; i < ctx.loopBody().statement().size(); i++) {
            visit(ctx.loopBody().statement(i));
        }

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitAssignment_statement(OpenPearlParser.Assignment_statementContext ctx) {
        Log.debug("ConstantPoolVisitor:visitAssignment_statement:ctx"
                + CommonUtils.printContext(ctx));


        visitChildren(ctx);

        m_currFixedLength = null;
        return null;
    }



    @Override
    public Void visitMultiplicativeExpression(
            OpenPearlParser.MultiplicativeExpressionContext ctx) {
        Log.debug("ConstantPoolVisitor:visitMultiplicativeExpression:ctx"
                + CommonUtils.printContext(ctx));

        if (!addFixedConstant(ctx)) {
            visitChildren(ctx);
        }

        return null;
    }



    @Override
    public Void visitInitElement(OpenPearlParser.InitElementContext ctx) {
        // MS: No need to add constant in INIT to the constant pool, because
        // the SymbolTableVisitor takes care of them.
        // visitChildren(ctx);
        return null;
    }

    private boolean addFixedConstant(ParserRuleContext ctx) {
        ASTAttribute attr = null;

        Log.debug("ConstantPoolVisitor:addFixedConstant:ctx" + CommonUtils.printContext(ctx));

        attr = m_ast.lookup(ctx);

        if (attr != null && attr.isReadOnly() && attr.getType() instanceof TypeFixed) {
            ConstantFixedValue value = attr.getConstantFixedValue();

            if (value != null) {
                m_constantPool.add(value);
                Log.debug("ConstantPoolVisitor:addFixedConstant:added value=" + value);
                return true;
            }
        }

        return false;
    }
}
