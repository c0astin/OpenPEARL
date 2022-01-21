/*
 * [The "BSD license"]
 *  Copyright (c) 2017 Marcel Schaible
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

import org.antlr.v4.runtime.tree.ParseTree;
import org.openpearl.compiler.Exception.*;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;

/**
 * @author Marcel Schaible
 * @version 1
 *     <p><b>Description</b>
 */

public  class ConstantFixedExpressionEvaluator extends OpenPearlBaseVisitor<ConstantFixedValue> implements OpenPearlVisitor<ConstantFixedValue> {

    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private SymbolTable m_currentSymbolTable;
    private ConstantExpressionEvaluatorVisitor  m_constantExpressionEvaluatorVisitor;
    private ConstantPoolVisitor m_constantPoolVisitor;

    public ConstantFixedExpressionEvaluator(int verbose,
                                            boolean debug,
                                            SymbolTable symbolTable,
                                            ConstantExpressionEvaluatorVisitor constantExpressionEvaluatorVisitor,
                                            ConstantPoolVisitor constantPoolVisitor) {

        m_debug = debug;
        m_verbose = verbose;
        m_currentSymbolTable = symbolTable;
        m_constantExpressionEvaluatorVisitor = constantExpressionEvaluatorVisitor;
        m_constantPoolVisitor = constantPoolVisitor;

        //m_debug = true;

        if (m_verbose > 0) {
            System.out.println( "    ConstantFixedExpressionEvaluator");
        }
    }

    @Override
    public ConstantFixedValue visitConstantExpression(OpenPearlParser.ConstantExpressionContext ctx) {
        ConstantFixedValue value;

        if (m_debug) {
            System.out.println("ConstantFixedExpressionEvaluator: visitConstantExpression");
        }

        value = visitChildren(ctx);

        if (m_debug) {
           System.out.println("ConstantFixedExpressionEvaluator: value="+value);
        }

        return value;
    }

    @Override
    public ConstantFixedValue visitConstantFixedExpression(OpenPearlParser.ConstantFixedExpressionContext ctx) {
        ConstantFixedValue value = null;

        if (m_debug) {
            System.out.println("ConstantFixedExpressionEvaluator: visitConstantFixedExpression");
        }

        value = visitConstantFixedExpressionTerm(ctx.constantFixedExpressionTerm());

        for (ParseTree c : ctx.children) {
            if (c instanceof OpenPearlParser.AdditiveConstantFixedExpressionTermContext) {
                ConstantFixedValue rhs =  visit(c);
                long res = value.getValue() + rhs.getValue();
                ConstantFixedValue v = new ConstantFixedValue(res);
                value = v;
            }
            else if (c instanceof OpenPearlParser.SubtractiveConstantFixedExpressionTermContext) {
                ConstantFixedValue rhs =  visit(c);
                long res = value.getValue() - rhs.getValue();
                ConstantFixedValue v = new ConstantFixedValue(res);
                value = v;
            }
        }
        
        // adjust m_precision according the result
        int bc ;//= Long.numberOfLeadingZeros(value.getValue());
        long val= value.getValue();

        // this solution is not nice for the cpu, but it works
        bc = Long.toBinaryString(Math.abs(val)).length();
        if (val < 0) {
          bc--;
        }
        value.setPrecision(bc);
        return value;
    }

    @Override public ConstantFixedValue visitAdditiveConstantFixedExpressionTerm(OpenPearlParser.AdditiveConstantFixedExpressionTermContext ctx)
    {
        return visitChildren(ctx);
    }

    @Override
    public ConstantFixedValue visitConstantFixedExpressionTerm(OpenPearlParser.ConstantFixedExpressionTermContext ctx) {
        ConstantFixedValue value = null;

        if (m_debug) {
            System.out.println("ConstantFixedExpressionEvaluator: visitConstantFixedExpressionTerm");
        }

        value = visitConstantFixedExpressionFactor(ctx.constantFixedExpressionFactor());

        for (ParseTree c : ctx.children) {
            if (c instanceof OpenPearlParser.MultiplicationConstantFixedExpressionTermContext) {
                ConstantFixedValue rhs =  visit(c);
                long res = value.getValue() * rhs.getValue();
                ConstantFixedValue v = new ConstantFixedValue(res);
                value = v;
            }
            else if (c instanceof OpenPearlParser.DivisionConstantFixedExpressionTermContext) {
                ConstantFixedValue rhs =  visit(c);

                if ( rhs.getValue() != 0 ) {
                    long res = value.getValue() / rhs.getValue();
                    ConstantFixedValue v = new ConstantFixedValue(res);
                    value = v;
                } else {
                    throw new DivisionByZeroException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            }
            else if (c instanceof OpenPearlParser.RemainderConstantFixedExpressionTermContext) {
                ConstantFixedValue rhs =  visit(c);

                if ( rhs.getValue() != 0 ) {
                    long res = value.getValue() % rhs.getValue();
                    ConstantFixedValue v = new ConstantFixedValue(res);
                    value = v;
                } else {
                    throw new DivisionByZeroException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            }

        }

        return value;
    }

    @Override
    public ConstantFixedValue visitConstantFixedExpressionFactor(OpenPearlParser.ConstantFixedExpressionFactorContext ctx) {
        ConstantFixedValue value = null;
        int sign = 1;

        if (m_debug) {
            System.out.println("ConstantFixedExpressionEvaluator: visitConstantFixedExpressionFactor");
        }

        if ( ctx.sign() != null ) {
            if ( ctx.sign() instanceof OpenPearlParser.SignMinusContext ) {
                sign = -1;
            }
        }

        if ( ctx.fixedConstant() != null) {
          long curval = 0;
          try {
             curval = sign * Long.parseLong(ctx.fixedConstant().IntegerConstant().toString());
          } catch (NumberFormatException e) {
            ErrorStack.add(ctx,"FIXED","too large");
          }
//            int curval = sign * Integer.parseInt(ctx.fixedConstant().IntegerConstant().toString());
            int curlen =   m_currentSymbolTable.lookupDefaultFixedLength();

            if ( ctx.fixedConstant().fixedNumberPrecision() != null ) {
                curlen = Integer.parseInt(ctx.fixedConstant().fixedNumberPrecision().IntegerConstant().toString());
            }

            value = new ConstantFixedValue(curval,curlen);
        }
        else if ( ctx.ID() != null ) {
            SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.ID().toString());

            if ( entry == null ) {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }

            if ( entry instanceof VariableEntry ) {
                VariableEntry variableEntry = (VariableEntry)entry;
                if ( !variableEntry.getType().hasAssignmentProtection() ) {
                  ErrorStack.enter(ctx);
                  ErrorStack.add("constant expected");
                  ErrorStack.leave();
                  return new ConstantFixedValue(1); // let's return for smooth ending of pass
                  //throw new ConstantExpectedException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }

                if ( !(variableEntry.getType() instanceof TypeFixed )) {
                    throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }

                if ( variableEntry.getInitializer() != null ) {

                    if ( variableEntry.getInitializer() instanceof SimpleInitializer) {
                        if ( ((SimpleInitializer)variableEntry.getInitializer()).getConstant() instanceof ConstantFixedValue) {
                            value = (ConstantFixedValue) ((SimpleInitializer)variableEntry.getInitializer()).getConstant();
                        } else {
                            throw new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                        }
                    }
                    else {
                        throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                    }
/*
                    if ( variableEntry.getInitializer().getContext() instanceof OpenPearlParser.ConstantExpressionContext) {
                        OpenPearlParser.ConstantExpressionContext c = (OpenPearlParser.ConstantExpressionContext)variableEntry.getInitializer().getContext();

                        ConstantValue v = m_constantExpressionEvaluatorVisitor.lookup(c);

                        if ( v instanceof ConstantFixedValue) {
                            value = (ConstantFixedValue)v;
                        }
                        else {
                            throw  new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                        }
                    }
                    else if ( variableEntry.getInitializer().getContext()instanceof OpenPearlParser.ConstantContext) {
                        OpenPearlParser.ConstantContext c = (OpenPearlParser.ConstantContext)variableEntry.getInitializer().getContext();

                        if ( c.StringLiteral() != null) {
                            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                        }

                        else if ( c.fixedConstant() != null ) {
                            int v = Integer.parseInt(c.fixedConstant().IntegerConstant().getText());
                            value = new ConstantFixedValue(v);

                        }
                        else if ( c.floatingPointConstant() != null ) {
                            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                        }
                        else if ( c.durationConstant() != null ) {
                            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                        }
                        else if ( c.bitStringConstant() != null ) {
                            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                        }
                        else if ( c.timeConstant() != null ) {
                            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
                        }
                    }
*/

                }
            }
        }
        else if ( ctx.constantFixedExpression() != null ) {
            value = visitConstantFixedExpression(ctx.constantFixedExpression());
        }

        if ( ctx.constantFixedExpressionFit() != null ) {
            ConstantFixedValue length;
            length = visitConstantFixedExpressionFit( ctx.constantFixedExpressionFit());

            int l = length.getPrecision();
            ConstantFixedValue fittedValue = new ConstantFixedValue(value.getValue(),l);
            value = fittedValue;

            if ( m_constantPoolVisitor != null ) {
                m_constantPoolVisitor.add(fittedValue);
            }
        }

        if ( m_debug) {
            System.out.println("ConstantFixedExpressionEvaluatorVisitor: value=" + value);
        }
        
        return value;
    }

    @Override
    public ConstantFixedValue visitConstantFixedExpressionFit(OpenPearlParser.ConstantFixedExpressionFitContext ctx) {
        ConstantFixedValue value = null;
        int sign = 1;

        if (m_debug) {
            System.out.println("ConstantFixedExpressionEvaluator: visitConstantFixedExpressionFit");
        }

        value = visitConstantFixedExpression(ctx.constantFixedExpression());

        if ( m_debug) {
            System.out.println("ConstantFixedExpressionEvaluatorVisitor: value=" + value);
        }

        return value;
    }
}
