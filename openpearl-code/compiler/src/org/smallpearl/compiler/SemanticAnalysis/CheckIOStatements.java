/*
 * [A "BSD license"]
 *  Copyright (c) 2019 Raier Müller
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
import org.antlr.v4.runtime.RuleContext;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;


/**
check types the io-statatements
with respect to the dation declaration
and constant values of format statements

Errors are forwarded to the ErrorStack class

*/

public class CheckIOStatements extends SmallPearlBaseVisitor<Void> implements
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

	public CheckIOStatements(String sourceFileName, int verbose, boolean debug,
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
	    public Void visitPositionRST(SmallPearlParser.PositionRSTContext ctx) {
	        if (m_debug) {
	            System.out.println( "Semantic: Check RST: visitPositionRST");
	        }

	        CheckPrecision(ctx.ID().getText(), ctx);
	        return null;
	    }
	   
	@Override
	public Void visitFieldWidth(SmallPearlParser.FieldWidthContext ctx) {
		String userDation = null;
		
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitFieldWidth");
		}
		
		/*
		// walk grammar up to put_statement
		RuleContext p= (RuleContext)ctx;
		while ( p != null ) {
			p=p.parent;
			if (p instanceof SmallPearlParser.PutStatementContext) {
				SmallPearlParser.PutStatementContext stmnt = (SmallPearlParser.PutStatementContext) p;
				userDation = stmnt.dationName().ID().toString();
				p=null;
			}
			if (p instanceof SmallPearlParser.GetStatementContext) {
				SmallPearlParser.GetStatementContext stmnt = (SmallPearlParser.GetStatementContext) p;
				userDation = stmnt.dationName().ID().toString();
				p=null;
			}
		} 
		if (userDation == null) {
			ErrorStack.add("no userdation found");
		} else {
		   System.out.println("type check for userdation missing: "+ userDation);
		   // name of userdation found -- now we should check the type 
		}
		*/
		
		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "fieldwidth");
		ErrorStack.enter(eEnv);
		
		ASTAttribute attr = m_ast.lookup(ctx.expression());
		// width is mandatory, which is definied in the grammar
		ConstantFixedValue cfv = getConstantValue(attr);	
		if (cfv != null) {
			long value = cfv.getValue();
			if (value <= 0) {
				ErrorStack.add("must be >0 ");
			}
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitNumberOfCharacters(
			SmallPearlParser.NumberOfCharactersContext ctx) {

		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitNumberOfCharacters");
		}
		
		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "numberOfCharacters");
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
			*/
			
			if (checkWidth && checkDecimalPositions && checkSignificance) {
				if (significance <= decimalPositions) {
					ErrorStack
							.add("significance must be larger than decimal positions");
				}

				// add 5 to significance due to decimal point and "E+xx"
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
				// problem during run time, since the sign is not mandatory
				if (width < decimalPositions + 6) {
					ErrorStack.add("field width too small (at least "
							+ (decimalPositions + 6) + " required)");
				}
			} else if (checkWidth && checkSignificance) {

				// add 5 to significance due to decimal point and "E+xx"
				// if the output value is <0, there may still occur a
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

	@Override
	public Void visitFloatFormatE3(SmallPearlParser.FloatFormatE3Context ctx) {
		boolean checkWidth = false;
		boolean checkDecimalPositions = false;
		boolean checkSignificance = false;
		long width = 0;
		long decimalPositions = 0;
		long significance = 0;

		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitFloatFormatE3");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "E3-format");
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
  			
			*/
			
			if (checkWidth && checkDecimalPositions && checkSignificance) {
				if (significance <= decimalPositions) {
					ErrorStack
							.add("significance must be larger than decimal positions");
				}

				// add 6 to significance due to decimal point and "E+xxx"
				// add 7 to decimal positions due to
				// leading digit, decimal point and "E+xxx"
				// if the output value is <0, there may still occur a
				// problem during run time, since the sign is not mandatory
				if ((width < significance + 6)
						|| (width < decimalPositions + 7)) {
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
				// add 7 to decimal positions due to
				// leading digit, decimal point and "E+xxx"
				// if the output value is <0, there may still occur a
				// problem during run time, since the sign is not mandatory
				if (width < decimalPositions + 7) {
					ErrorStack.add("field width too small (at least "
							+ (decimalPositions + 7) + " required)");
				}
			} else if (checkWidth && checkSignificance) {

				// add 6 to significance due to decimal point and "E+xxx"
				// if the output value is <0, there may still occur a
				// problem during run time, since the sign is not mandatory
				if (width < significance + 6) {
					ErrorStack.add("field width too small (at least "
							+ (significance + 6) + " required)");
				}
			}
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitFixedFormat(SmallPearlParser.FixedFormatContext ctx) {
		boolean checkWidth = false;
		boolean checkDecimalPositions = false;
		long width = 0;
		long decimalPositions = 0;

		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitFixedFormat");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "F-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given

			ASTAttribute attr = m_ast.lookup(ctx.fieldWidth().expression());
			// width is mandatory, which is defined in the grammar
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
			}


			// analysis complete --> let's apply the checks
			/*
			System.out.println("width=" + width + " (check=" + checkWidth + ")"
					+ "decimalPositions=" + decimalPositions + " (check="
					+ checkDecimalPositions + ")" + 
					")");
  			
			*/
			
			if (checkWidth && checkDecimalPositions) {
				// add 2 to decimal positions due to
				// leading digit, decimal point
				// if the output value is <0, there may still occur a
				// problem during run time, since the sign is not mandatory
				if (width < decimalPositions + 2) {
					ErrorStack.add("field width too small (at least "
							+ (decimalPositions + 2) + " required)");
				}
			} //else if (checkWidth) {

				// at least 1 digit required
				// if the output value is <0, there may still occur a
				// problem during run time, since the sign is not mandatory
				// this is already checkes in visitFieldWidth!
				//if (width < 1) {
				//	ErrorStack.add("field width too small (at least 1 required)");
				//}
			//}
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitDurationFormat(SmallPearlParser.DurationFormatContext ctx) {
		boolean checkWidth = false;
		boolean checkDecimalPositions = false;
		long width = 0;
		long decimalPositions = 0;

		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitDurationFormat");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "D-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given

			ASTAttribute attr = m_ast.lookup(ctx.fieldWidth().expression());
			// width is mandatory, which is defined in the grammar
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
			}


			// analysis complete --> let's apply the checks
			/*
			System.out.println("width=" + width + " (check=" + checkWidth + ")"
					+ "decimalPositions=" + decimalPositions + " (check="
					+ checkDecimalPositions + ")" + 
					")");
  			
			*/
			
			if (checkWidth && checkDecimalPositions) {
				// add 6 to decimal positions due to
				// leading digit, decimal point ans 'SEC'
				// if the output value is <0 or the value is larger than 1 sec,
				// there may still occur a problem during run time
				if (width < decimalPositions + 6) {
					ErrorStack.add("field width too small (at least "
							+ (decimalPositions + 6) + " required)");
				}
			} else if (checkWidth) {
				// at least 'x_SEC' required
				// if the output value is <0 or > 9, there may still occur a
				// problem during run time, since the sign is not mandatory
				// this is already checkes in visitFieldWidth!
				if (width < 5) {
					ErrorStack.add("field width too small (at least 5 required)");
				}
			}
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitTimeFormat(SmallPearlParser.TimeFormatContext ctx) {
		boolean checkWidth = false;
		boolean checkDecimalPositions = false;
		long width = 0;
		long decimalPositions = 0;

		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitTimeFormat");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "T-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given

			ASTAttribute attr = m_ast.lookup(ctx.fieldWidth().expression());
			// width is mandatory, which is defined in the grammar
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
			}


			// analysis complete --> let's apply the checks
			/*
			System.out.println("width=" + width + " (check=" + checkWidth + ")"
					+ "decimalPositions=" + decimalPositions + " (check="
					+ checkDecimalPositions + ")" + 
					")");
  			
			*/
			
			if (checkWidth && checkDecimalPositions) {
				// add 8 to decimal positions due to
				// x:xx:xx.xxx
				// if the output value is <0 or the value is larger than 9:59:59.99,
				// there may still occur a problem during run time
				if (width < decimalPositions + 8) {
					ErrorStack.add("field width too small (at least "
							+ (decimalPositions + 8) + " required)");
				}
			} else if (checkWidth) {
				// at least 'x:xx:xx' required
				// if the output value is > 9:59:59.999, there may still occur a
				// problem during run time, since the sign is not mandatory
				// this is already checkes in visitFieldWidth!
				if (width < 7) {
					ErrorStack.add("field width too small (at least 7 required)");
				}
			}
		}
		ErrorStack.leave();
		return null;
	}

	
	@Override
	public Void visitCharacterStringFormatA(SmallPearlParser.CharacterStringFormatAContext ctx) {
			if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitCharacterStringFormat");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "A-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given
			// check of the value oof width is already done
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitBitFormat1(SmallPearlParser.BitFormat1Context ctx) {
			if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitBitFormat1");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "B/B1-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given
			// check of the value of width is already done
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitBitFormat2(SmallPearlParser.BitFormat2Context ctx) {
			if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitBitFormat2");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "B2-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given
			// check of the value oof width is already done
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitBitFormat3(SmallPearlParser.BitFormat3Context ctx) {
			if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitBitFormat3");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "B3-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given
			// check of the value oof width is already done
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitBitFormat4(SmallPearlParser.BitFormat4Context ctx) {
			if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitBitFormat4");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "B4-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given
			// check of the value oof width is already done
		}
		ErrorStack.leave();
		return null;
	}

	/* --------------------------------------------------- */
	// positions
	@Override
	public Void visitPositionX(SmallPearlParser.PositionXContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitPositionX");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "X-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		// check, if we have ASTattributes for this node
		if (ctx.expression() != null) {
		   enshureTypeFixed(ctx.expression());
		}
		
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitPositionSKIP(SmallPearlParser.PositionSKIPContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitPositionSkip");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "SKIP-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		// check, if we have ASTattributes for this node
		if (ctx.expression() != null) {
		   enshureTypeFixed(ctx.expression());
		}
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitPositionPAGE(SmallPearlParser.PositionPAGEContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitPositionPAGE");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "PAGE-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		// check, if we have ASTattributes for this node
		if (ctx.expression() != null) {
		   enshureTypeFixed(ctx.expression());
		}
		
		ErrorStack.leave();
		return null;
	}
	
	@Override
	public Void visitPositionADV(SmallPearlParser.PositionADVContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitPositionADV");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "ADV-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		// check, if we have ASTattributes for this node
		for (int i=0; i< ctx.expression().size(); i++) {
		   enshureTypeFixed(ctx.expression(i));
		}
		ErrorStack.leave();
		return null;
	}
	
	@Override
	public Void visitPositionPOS(SmallPearlParser.PositionPOSContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitPositionPOS");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "POS-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		// check, if we have ASTattributes for this node
		for (int i=0; i< ctx.expression().size(); i++) {
		   enshureTypeFixed(ctx.expression(i));
		   enshureGreaterZero(ctx.expression(i));
		}
		ErrorStack.leave();
		return null;
	}
	
	@Override
	public Void visitPositionSOP(SmallPearlParser.PositionSOPContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitPositionSOP");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "SOP-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		// check, if we have ASTattributes for this node
		for (int i=0; i< ctx.ID().size(); i++) {
		       CheckFixedVariable(ctx.ID(i).getText(), ctx);
		}
		ErrorStack.leave();
		return null;
	}
	@Override
	public Void visitPositionCOL(SmallPearlParser.PositionCOLContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitPositionCOL");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "COL-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		// check, if we have ASTattributes for this node
		   enshureTypeFixed(ctx.expression());
		   enshureGreaterZero(ctx.expression());
		ErrorStack.leave();
		return null;
	}
	
	@Override
	public Void visitPositionLINE(SmallPearlParser.PositionLINEContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOFormats: visitPositionLINE");
		}

		ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "LINE-format");
		ErrorStack.enter(eEnv);

		// check the types of all children
		// check, if we have ASTattributes for this node
		   enshureTypeFixed(ctx.expression());
		   enshureGreaterZero(ctx.expression());
		ErrorStack.leave();
		return null;
	}
	
	
	/* ----------------------------------------------------_ */
	
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

	private Void enshureTypeFixed(SmallPearlParser.ExpressionContext ctx){
		   TypeDefinition type = m_ast.lookupType(ctx);
		   ErrorEnvironment eEnvType = new ErrorEnvironment(ctx, "expression");
		   ErrorStack.enter(eEnvType);
		   if (!(type instanceof TypeFixed)) {
			   ErrorStack.add("must be FIXED");
		   }
		   ErrorStack.leave();		
		return null;
	}
	
	private Void enshureGreaterZero(SmallPearlParser.ExpressionContext ctx){
		   ErrorEnvironment eEnvType = new ErrorEnvironment(ctx, "expression");
		   ErrorStack.enter(eEnvType);
		ASTAttribute attr = m_ast.lookup(ctx);
		ConstantFixedValue cfv = getConstantValue(attr);
		if (cfv != null) {
			// is of type ConstantFixedValue
			// --> get value and use it for checks
			if (cfv.getValue() <= 0) {
				   ErrorStack.add("must be > 0");
			}
		}
		  ErrorStack.leave();	
		return null;
	}

	   private Void CheckPrecision(String id, ParserRuleContext ctx) {

	        SymbolTableEntry entry = m_currentSymbolTable.lookup(id);
	        VariableEntry var = null;


			ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "RST");
			ErrorStack.enter(eEnv);
			
	        if (entry != null && entry instanceof VariableEntry) {
	        	// it would be got to set a new error environment to the 
	        	// context of the 'entry'
	    		var = (VariableEntry) entry;

	            if ( var.getType() instanceof TypeFixed) {
	                TypeFixed type = (TypeFixed) var.getType();
	                if (type.getPrecision() < 15) {
	                	ErrorStack.add("RST variable must be at least FIXED(15)");
	                }
	            } else {
	            	ErrorStack.add("RST variable must be FIXED");
	            }
	        } else {
	            ErrorStack.add("RST needs a variable");
	        }

	        ErrorStack.leave();

	        return null;
	    }
	   
	   private Void CheckFixedVariable(String id, ParserRuleContext ctx) {

	        SymbolTableEntry entry = m_currentSymbolTable.lookup(id);
	        VariableEntry var = null;

			ErrorEnvironment eEnv = new ErrorEnvironment(ctx, "variable");
			ErrorStack.enter(eEnv);
			
	        if (entry != null && entry instanceof VariableEntry) {
	        	// it would be got to set a new error environment to the 
	        	// context of the 'entry'
	    		var = (VariableEntry) entry;

	            if ( var.getType() instanceof TypeFixed) {
	                TypeFixed type = (TypeFixed) var.getType();
	                if (type.getPrecision() != 31) {
	                	ErrorStack.add("must be FIXED(31)");
	                }	                
	            } else {
	            	ErrorStack.add("must be of type FIXED");
	            }
	        } else {
	            ErrorStack.add("must be a variable");
	        }

	        ErrorStack.leave();
	        
	        return null;
	    }
}
