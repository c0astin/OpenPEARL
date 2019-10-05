/*
 * [The "BSD license"]
 *  Copyright (c) 2012-2017 Marcel Schaible
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

import org.antlr.v4.runtime.ParserRuleContext;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;

public class CheckIOFormats extends SmallPearlBaseVisitor<Void> implements
		SmallPearlVisitor<Void> {

	private int m_verbose;
	private boolean m_debug;
	private String m_sourceFileName;
	private ExpressionTypeVisitor m_expressionTypeVisitor;
	private SymbolTableVisitor m_symbolTableVisitor;
	private SymbolTable m_symboltable;
	private SymbolTable m_currentSymbolTable;
	private ModuleEntry m_module;
	private AST m_ast = null;

	public CheckIOFormats(String sourceFileName, int verbose, boolean debug,
			SymbolTableVisitor symbolTableVisitor,
			ExpressionTypeVisitor expressionTypeVisitor, AST ast) {

		m_debug = debug;
		m_verbose = verbose;
		m_sourceFileName = sourceFileName;
		m_symbolTableVisitor = symbolTableVisitor;
		m_expressionTypeVisitor = expressionTypeVisitor;
		m_symboltable = symbolTableVisitor.symbolTable;
		m_currentSymbolTable = m_symboltable;
		m_ast = ast;

		if (m_verbose > 0) {
			System.out.println("    Check IOFormats");
		}
	}

	@Override
	public Void visitModule(SmallPearlParser.ModuleContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitModule");
		}

		org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry = m_currentSymbolTable
				.lookupLocal(ctx.ID().getText());
		m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
		visitChildren(ctx);
		m_currentSymbolTable = m_currentSymbolTable.ascend();
		return null;
	}

	@Override
	public Void visitProcedureDeclaration(
			SmallPearlParser.ProcedureDeclarationContext ctx) {
		if (m_debug) {
			System.out
					.println("Semantic: Check IOFormats: visitProcedureDeclaration");
		}

		this.m_currentSymbolTable = m_symbolTableVisitor
				.getSymbolTablePerContext(ctx);
		visitChildren(ctx);
		this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
		return null;
	}

	@Override
	public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
		if (m_debug) {
			System.out
					.println("Semantic: Check IOFormats: visitTaskDeclaration");
		}

		this.m_currentSymbolTable = m_symbolTableVisitor
				.getSymbolTablePerContext(ctx);
		visitChildren(ctx);
		m_currentSymbolTable = m_currentSymbolTable.ascend();
		return null;
	}

	@Override
	public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
		if (m_debug) {
			System.out
					.println("Semantic: Check IOFormats: visitBlock_statement");
		}

		this.m_currentSymbolTable = m_symbolTableVisitor
				.getSymbolTablePerContext(ctx);
		visitChildren(ctx);
		this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
		return null;
	}

	@Override
	public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitLoopStatement");
		}

		this.m_currentSymbolTable = m_symbolTableVisitor
				.getSymbolTablePerContext(ctx);
		visitChildren(ctx);
		this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
		return null;
	}

	/* ------------------------------------------------ */
	/* start of check specific code */
	/* ------------------------------------------------ */

	@Override
	public Void visitFieldWidth(SmallPearlParser.FieldWidthContext ctx) {

		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitFieldWidth");
		}
		/*
		System.out.println("ctx is: " + ctx);
		
		System.out.println("ctx.expression is: " + ctx.expression());
		if (ctx.expression() instanceof SmallPearlParser.BaseExpressionContext) {
			System.out.println("BaseExpressionContext");
		} else if (ctx.expression() instanceof SmallPearlParser.SubtractiveExpressionContext) {
			System.out.println("SubstractiveExpressionContext");
		} else if (ctx.expression() instanceof SmallPearlParser.UnarySubtractiveExpressionContext) {
			System.out.println("UnarySubstractionExpressionContext");
		} else if (ctx.expression() instanceof SmallPearlParser.UnaryAdditiveExpressionContext) {
			System.out.println("UnaryAdditiveExpressionContext");
		} else {
			System.out.println("nix");
		}
		*/
		
		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "fieldwidth");
		ErrorStack.enter(eEnv);

		// check, if we have ASTattributes for this node
		TypeDefinition type = m_ast.lookupType(ctx.expression());

		if (!(type instanceof TypeFixed)) {
			ErrorStack.add("type must be FIXED");
		} else {
			ASTAttribute attr = m_ast.lookup(ctx.expression());
			// width is mandatory, which is definied in the grammar
			ConstantFixedValue cfv = getConstantValue(attr);	
			if (cfv != null) 
			{
				long value = cfv.getValue();
				if (value <= 0) {
					ErrorStack.add("must be >0 ");
				}
			}
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitDecimalPositions(
			SmallPearlParser.DecimalPositionsContext ctx) {
		if (m_debug) {
			System.out
					.println("Semantic: Check IOFormats: visitDecimalPositions");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "decimal positions");
		ErrorStack.enter(eEnv);

		TypeDefinition type = m_ast.lookupType(ctx.expression());
		if (!(type instanceof TypeFixed)) {
			ErrorStack.add("type must be FIXED");
		} else {
			if (ctx.expression() != null) {
				// we have the attribute given
				ASTAttribute attr = m_ast.lookup(ctx.expression());
				ConstantFixedValue cfv = getConstantValue(attr);
				if (cfv != null) {
					long value = cfv.getValue();
					if (value < 0) {
						ErrorStack.add("must be >=0 ");
					}
				}
			}
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitSignificance(SmallPearlParser.SignificanceContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitSignificance");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "significance");
		ErrorStack.enter(eEnv);

		TypeDefinition type = m_ast.lookupType(ctx.expression());
		if (!(type instanceof TypeFixed)) {
			ErrorStack.add("type must be FIXED");
			/*
			 * throw new TypeMismatchException(ctx.getText(),
			 * ctx.start.getLine(), ctx.start.getCharPositionInLine(),
			 * "type of significance must be integer");
			 */
		} else {
			if (ctx.expression() != null) {
				// we have the attribute given
				ASTAttribute attr = m_ast.lookup(ctx.expression());
				ConstantFixedValue cfv = getConstantValue(attr);
				if (cfv != null) {
					long value = cfv.getValue();
					if (value <= 0) {
						ErrorStack.add("must be >0 ");
					}
				}
			}
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitFloatFormatE(SmallPearlParser.FloatFormatEContext ctx) {
		boolean checkWidth = false;
		boolean checkDecimalPositions = false;
		boolean checkSignificance = false;
		long width = 0;
		long decimalPositions = 0;
		long significance = 0;

		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitFloatFormatE");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "E-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given

			ASTAttribute attr = m_ast.lookup(ctx.fieldWidth().expression());
			// width is mandatory, which is definied in the grammar
			ConstantFixedValue cfv = getConstantValue(attr);
			if (cfv != null) {
				width = cfv.getValue();
				checkWidth = true;
			}

			if (ctx.decimalPositions() != null) {
				// we have the attribute given
				attr = m_ast.lookup(ctx.decimalPositions().expression());
				cfv = getConstantValue(attr);
				if (cfv != null) {
					// is of type ConstantFixedValue
					// --> get value and use it for checks
					decimalPositions = cfv.getValue();
					checkDecimalPositions = true;
				}
			} else {
				// decimalPositions not given --> used default value
				decimalPositions = 0;
				checkDecimalPositions = true;
			}

			if (ctx.significance() != null) {
				// we have the attribute given
				attr = m_ast.lookup(ctx.significance().expression());
				cfv = getConstantValue(attr);
				if (cfv != null) {
					// is of type ConstantFixedValue
					// --> get value and use it for checks
					significance = cfv.getValue();
					checkSignificance = true;
				}
			} else {
				// significance not given --> used default value
				// which need the dicimalPositions to be kwnown
				if (checkDecimalPositions) {
					significance = decimalPositions + 1;
					checkSignificance = true;
				}
			}

			// analysis complete --> let's apply the checks
			/*
			System.out.println("width=" + width + " (check=" + checkWidth + ")"
					+ "decimalPositions=" + decimalPositions + " (check="
					+ checkDecimalPositions + ")" + "significance="
					+ significance + " (check=" + checkSignificance + ")");
  			
			if (checkWidth) {
				if (width < 0) {
					ErrorStack.add("width must be non negative");
				}
			}

			if (checkDecimalPositions) {
				if (decimalPositions < 0) {
					ErrorStack.add("decimal positions must be non negative");
				}
			}

			if (checkSignificance) {
				if (significance < 0) {
					ErrorStack.add("significance must be positive");
				}
			}
			*/
			
			if (checkWidth && checkDecimalPositions && checkSignificance) {
				if (significance <= decimalPositions) {
					ErrorStack
							.add("significance must be larger than decimal positions");
				}

				// add 5 to significance due to decimal point and "E+xx"
				// if the output value is <0, there may still occur a
				// problem during run time, since the sign is not mandatory
				// add 6 to decimal positions due to
				// leading digit, decimal point and "E+xx"
				// if the output value is <0, there may still occur a
				// problem during run time, since the sign is not mandatory
				if ((width < significance + 5)
						|| (width < decimalPositions + 6)) {
					ErrorStack.add("field width too small (at least "
							+ Math.max((significance + 5),
									(decimalPositions + 6)) + " required)");
				}
			} else if (!checkWidth && checkDecimalPositions
					&& checkSignificance) {
				if (significance <= decimalPositions) {
					ErrorStack
							.add("significance must be larger than decimal positions");
				}
			} else if (checkWidth && checkDecimalPositions) {
				// add 6 to decimal positions due to
				// leading digit, decimal point and "E+xx"
				// if the output value is <0, there may still occur a
				if (width < decimalPositions + 6) {
					ErrorStack.add("field width too small (at least "
							+ (decimalPositions + 6) + " required)");
				}
			} else if (checkWidth && checkSignificance) {

				// add 5 to significance due to decimal point and "E+xx"
				// if the output value is <0, there may still occur a
				// problem during run time, since the sign is not mandatory
				// problem during run time, since the sign is not mandatory
				if (width < significance + 5) {
					ErrorStack.add("field width too small (at least "
							+ (significance + 5) + " required)");
				}
			}
		}
		ErrorStack.leave();
		return null;
	}

	/*
	 * @Override public Void
	 * visitFloatFormatE3(SmallPearlParser.FloatFormatE3Context ctx) { if
	 * (m_debug) { System.out.println(
	 * "Semantic: Check IOFormats: visitFloatFormatE3"); }
	 * 
	 * // CheckPrecision(ctx.ID().getText(), ctx); return null; }
	 */

	private ConstantFixedValue getConstantValue(ASTAttribute formatAttribute) {
		//System.out.println("formatAttribute=" + formatAttribute);
		if (formatAttribute.isReadOnly()) {
			if (formatAttribute.getType() instanceof TypeFixed) {
				ConstantFixedValue cfv = (ConstantFixedValue) formatAttribute
						.getConstant();
				cfv = (ConstantFixedValue) formatAttribute
						.getConstantFixedValue();
				/*
				if (cfv != null) {
					System.out.println("width=" + cfv.getValue());
					System.out.println("precision=" + cfv.getPrecision());
				}
				*/
				return cfv;
			}
		} else {
			// entry is in symbol table! --> is a variable!
			// we have no constant
		}
		return null;
	}
}
