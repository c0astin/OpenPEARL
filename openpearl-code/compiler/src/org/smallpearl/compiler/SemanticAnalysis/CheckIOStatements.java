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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.SmallPearlParser.*;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;
import org.stringtemplate.v4.ST;



/**
check types the io-statatements
with respect to the dation declaration
and constant values of format statements

Errors are forwarded to the ErrorStack class

 */

public class CheckIOStatements extends SmallPearlBaseVisitor<Void> implements
SmallPearlVisitor<Void> {

	/**
	 * @author mueller
	 *
	 * check if all referenced system dations are specified 
	 * verify compatibility of system dation attributes with user dation attributes
	 * create entry in symbol table with the dations attributes
	 * 
	 */
	public class CheckDationDeclaration extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {
	
	    private int m_verbose;
	    private boolean m_debug;
	    private String m_sourceFileName;
	    private ExpressionTypeVisitor m_expressionTypeVisitor;
	    private SymbolTableVisitor m_symbolTableVisitor;
	    private SymbolTable m_symboltable;
	    private SymbolTable m_currentSymbolTable;
	    private ModuleEntry m_module;
	    private AST m_ast = null;
	
	    public CheckDationDeclaration(String sourceFileName,
	                    int verbose,
	                    boolean debug,
	                    SymbolTableVisitor symbolTableVisitor,
	                    ExpressionTypeVisitor expressionTypeVisitor,
	                    AST ast) {
	
	        m_debug = debug;
	        m_verbose = verbose;
	        m_sourceFileName = sourceFileName;
	        m_symbolTableVisitor = symbolTableVisitor;
	        m_expressionTypeVisitor = expressionTypeVisitor;
	        m_symboltable = symbolTableVisitor.symbolTable;
	        m_currentSymbolTable = m_symboltable;
	        m_ast = ast;
	
	        Log.debug( "    Check DationDeclaration");
	    }
	
	    @Override
	    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
	        if (m_debug) {
	            System.out.println( "Semantic: Check RST: visitModule");
	        }
	
	        org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry = m_currentSymbolTable.lookupLocal(ctx.ID().getText());
	        m_currentSymbolTable = ((ModuleEntry)symbolTableEntry).scope;
	        visitChildren(ctx);
	        m_currentSymbolTable = m_currentSymbolTable.ascend();
	        return null;
	    }
	
	    @Override
	    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
	        if (m_debug) {
	            System.out.println( "Semantic: Check RST: visitProcedureDeclaration");
	        }
	
	        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
	        visitChildren(ctx);
	        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
	        return null;
	    }
	
	    @Override
	    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
	        if (m_debug) {
	            System.out.println( "Semantic: Check RST: visitTaskDeclaration");
	        }
	
	        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
	        visitChildren(ctx);
	        m_currentSymbolTable = m_currentSymbolTable.ascend();
	        return null;
	    }
	
	    @Override
	    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
	        if (m_debug) {
	            System.out.println( "Semantic: Check RST: visitBlock_statement");
	        }
	
	        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
	        visitChildren(ctx);
	        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
	        return null;
	    }
	
	    @Override
	    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
	        if (m_debug) {
	            System.out.println( "Semantic: Check RST: visitLoopStatement");
	        }
	
	        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
	        visitChildren(ctx);
	        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
	        return null;
	    }
	
	    @Override
	    public Void visitDationDeclaration(SmallPearlParser.DationDeclarationContext ctx) {
	        if (m_debug) {
	            System.out.println( "Semantic: visitDationDeclaration");
	        }
	        for (int i = 0; i<ctx.identifierDenotation().ID().size(); i++) {
	        	String dationName = ctx.identifierDenotation().ID(i).toString();
	            //System.out.println("DationName: "+ dationName);
	        
	            ErrorStack.enter(ctx, "DationDCL");
	            
	
	    	    SymbolTableEntry entry1 = this.m_currentSymbolTable.lookup(dationName);
	
	            if (entry1 == null) {
	        	   throw new InternalCompilerErrorException("Symbol table does not contain:"+dationName);
	            }
	            SymbolTableEntry se = this.m_currentSymbolTable.lookup(dationName);
	            if (se != null &&
	            	! (se instanceof VariableEntry)	&&
	            	!(((VariableEntry)se).getType() instanceof TypeDation)
	            		) {
	            	throw new InternalCompilerErrorException("symbol "+dationName+" not found/or no dation");
	            }
	        	TypeDation d = (TypeDation)(((VariableEntry)se).getType());
	        	
	        	// userdation must be 
	        	// of type ALPHIC					   -> DationPG
	        	// or type BASIC + typeOfTransmission  -> DationTS
	        	// or type of       typeOfTransmission -> DationRW
	        	// this is all enforced by the grammar
	        	
	        	if (d.isSystemDation()) {
	        		ErrorStack.add("SYSTEM dations may not be declared");
	        	}
	        	// dimension settings must be >0 or '*' if given
	        	// only the last dimension may be '*' - the not 0 check is not in the grammar
	        	switch (d.getNumberOfDimensions()) {
	        	case  3:
	         		if (d.getDimension3()<=0 || d.getDimension2()<=0) {
	        			// '*' not allowed
	        			ErrorStack.add("only first dimension may be '*'");
	        		}
	         		break;
	        	case  2:
	        		if (d.getDimension2()==0) {
	        			// '*' not allowed
	        			ErrorStack.add("only first dimension may be '*'");
	        		}
	        		break;
	        	case  1:
	          		break;
	          	}
	
	        	if (d.hasTfu() && d.getNumberOfDimensions() == 1 && d.getDimension1() == 0) {
	     	       ErrorStack.add("TFU requires limited record length");
	        	}
	
	        	
	            SymbolTableEntry sys = this.m_currentSymbolTable.lookup(d.getCreatedOn());;
	            if (sys == null) {
	            	ErrorStack.add(d.getCreatedOn()+" is not defined");
	            	// TFU does not agree with last dimension unlimited  !
	            } else if ( (! (sys instanceof VariableEntry))  ||
	                 (! (((VariableEntry)sys).getType() instanceof TypeDation))) {
	            	ErrorStack.add(d.getCreatedOn()+" is not of type DATION");
	            } else {	
	               TypeDation sd = (TypeDation)(((VariableEntry)sys).getType());
	               
	               // check compatibility
	               // (1) ALPHIC need !BASIS of the system dation
	               if (d.isAlphic() && sd.isBasic()) {
	            	   ErrorStack.add("attempt to create PUT/GET dation upon BASIC system dation");
	               }
	               if (!d.isBasic() && sd.isBasic()) {
	            	   ErrorStack.add("attempt to create a READ/WRITE dation upon a BASIC sytem dation");
	               }
	               if (d.isBasic() && !sd.isBasic()) {
	            	   ErrorStack.add("attempt to create a BASIC dation upon an non BASIC system dation");
	               }
	
	               // (2) direction must fit (sourceSinkAttribute
	               if (d.isIn() && ! sd.isIn()) {
	            	   ErrorStack.add("system dation does not provide direction IN");
	               }
	               if (d.isOut() && ! sd.isOut()) {
	            	   ErrorStack.add("system dation does not provide direction OUT");
	               }
	               
	               // (3) TFU must be on userdation if this is set on system dation
	               if (!d.hasTfu() && sd.hasTfu()) {
	            	   ErrorStack.add("system dation requires TFU for user dation");
	               	   if (d.getNumberOfDimensions() == 1 && d.getDimension1() == 0) {
	            		   ErrorStack.warn("TFU would require limited record length for user dation");
	               	   }
	               }
	               
	               // (4) type of transmission must fit, if not set to ALL in system dation
	               if (sd.getTypeOfTransmission() == null) {
	            	   if (!sd.isAlphic()) {
	            	      // the system dation misses some data -- this should be detected by the
	            	      // imc in all compilations
	            	      throw new InternalCompilerErrorException(sd+" has no typeOfTransmission");
	            	   }
	               } else if (!sd.getTypeOfTransmission().equals("ALL")) {
	            	   if (!sd.getTypeOfTransmission().equals(d.getTypeOfTransmission())) {
	            		   ErrorStack.add("type of transmission mismatch (system:"+sd.getTypeOfTransmission()+
	            				   " user: "+d.getTypeOfTransmission()+")");
	            	   }
	               }
	               
	               // (5) if the system dation specifies a TFU-size; the record length
	               //     of the user must not exceed. This is checked during runtime,
	               //     since we have no access to the system dations description files
	               //     at compile time
	
	            }
	            
	        	// TFU does not agree with last dimension unlimited  !
	        	if (d.hasTfu() && d.getNumberOfDimensions() == 1 && d.getDimension1() == 0) {
	        		if (sys == null) {
	        	       ErrorStack.add("TFU requires limited record length");
	        		}
	        	}
	        
	        	
	            ErrorStack.leave();
	        }
	                
	        return null;
	    }
	
	    
	
	}

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
			System.out.println("Semantic: Check IOStatements: visitModule");
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
			.println("Semantic: Check IOStatements: visitProcedureDeclaration");
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
			.println("Semantic: Check IOStatements: visitTaskDeclaration");
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
			.println("Semantic: Check IOStatements: visitBlock_statement");
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
			System.out.println("Semantic: Check IOStatements: visitLoopStatement");
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

	@Override public Void visitOpen_statement(SmallPearlParser.Open_statementContext ctx) {
		ErrorStack.enter(ctx, "OPEN");
		boolean hasRST = false;
		boolean hasIDF = false;
		boolean hasOLD = false;
		boolean hasNEW = false;
		boolean hasANY = false;
		boolean hasCAN = false;
		boolean hasPRM = false;


		lookupDation(ctx.ID().toString());

		if (ctx.open_parameterlist() != null && ctx.open_parameterlist().open_parameter() != null) {

			for (int i=0; i<ctx.open_parameterlist().open_parameter().size(); i++) {
				//System.out.println("openParam: "+ ctx.open_parameterlist().open_parameter(i).getText());
				if (ctx.open_parameterlist().open_parameter(i).open_parameter_idf() != null ) {
					if (hasIDF) ErrorStack.add("multiple IDF attributes");
					hasIDF = true;
					// let's check the type of the IDF-variable. It must be of type CHAR
					if (ctx.open_parameterlist().open_parameter(i).open_parameter_idf().ID() != null) {
						String name = ctx.open_parameterlist().open_parameter(i).open_parameter_idf().ID().toString();
						SymbolTableEntry se = m_currentSymbolTable.lookup(name);
						if (se == null) {
							ErrorStack.add("'" + name+"' is not defined");
						} else if (  (se instanceof VariableEntry) &&
								! ((((VariableEntry)se).getType() instanceof TypeChar))) {
							ErrorStack.add("'"+ name + "' must be of type CHAR -- has type "+ (((VariableEntry)se).getType().toString()));
						}
					}
					/*
					 it's ok for the moment
					 if there is a StringLiteral, this should be in the ConstantPool 
					 if not, it must be a character variable
					 both information should be places as ASTAttributes
					 */
				}
				if (ctx.open_parameterlist().open_parameter(i).open_close_RST() != null) {
					Open_close_RSTContext c = (Open_close_RSTContext)(ctx.open_parameterlist().open_parameter(i).open_close_RST() );
					if (hasRST) ErrorStack.warn("multiple RST attributes");
					hasRST=true;
					CheckPrecision(c.ID().toString(), c);
				}

				if (ctx.open_parameterlist().open_parameter(i).open_parameter_old_new_any() != null ) {
					Open_parameter_old_new_anyContext c = (Open_parameter_old_new_anyContext)(ctx.open_parameterlist().open_parameter(i).open_parameter_old_new_any() );

					if (c.getText().equals("OLD")) {
						if (hasOLD) ErrorStack.warn("multiple OLD attributes");
						hasOLD=true;
					}


					if (c.getText().equals("NEW")) {
						if (hasNEW) ErrorStack.warn("multiple NEW attributes");
						hasNEW=true;
					}

					if (c.getText().equals("ANY")) {
						if (hasANY) ErrorStack.warn("multiple ANY attributes");
						hasANY=true;
					}
				}

				if (ctx.open_parameterlist().open_parameter(i).open_close_parameter_can_prm() != null ) {
					Open_close_parameter_can_prmContext c = (Open_close_parameter_can_prmContext)(ctx.open_parameterlist().open_parameter(i).open_close_parameter_can_prm() );
					if (c.getText().equals("CAN")) {
						if (hasCAN) ErrorStack.add("multiple CAN attributes");
						hasCAN=true;
					}


					if (c.getText().equals("PRM")) {
						if (hasPRM) ErrorStack.add("multiple PRM attributes");
						hasPRM=true;
					}
				}


			}
			if (hasCAN && hasPRM) {
				ErrorStack.add("ether CAN or PRM allowed");
			}

			int nbrOfPreviousStati = 0;
			if (hasOLD) nbrOfPreviousStati ++;
			if (hasNEW) nbrOfPreviousStati ++;
			if (hasANY) nbrOfPreviousStati ++;
			if (nbrOfPreviousStati > 1) ErrorStack.add("only one of OLD/NEW/ANY allowed");
		}

		ErrorStack.leave();
		return null;
	}

	@Override public Void visitClose_statement(SmallPearlParser.Close_statementContext ctx) {

		ErrorStack.enter(ctx, "CLOSE");

		boolean hasRST = false;
		boolean hasCAN = false;
		boolean hasPRM = false;


		lookupDation(ctx.ID().toString());
		if (ctx.close_parameterlist() != null && ctx.close_parameterlist().close_parameter() != null) {
			for (int i=0; i<ctx.close_parameterlist().close_parameter().size(); i++) {

				if (ctx.close_parameterlist().close_parameter(i).open_close_RST() != null) {
					Open_close_RSTContext c = (Open_close_RSTContext)(ctx.close_parameterlist().close_parameter(i).open_close_RST());
					if (hasRST) ErrorStack.warn("multiple RST attributes");
					hasRST=true;
					CheckPrecision(c.ID().toString(), c);
				}


				if (ctx.close_parameterlist().close_parameter(i).open_close_parameter_can_prm() != null ) {
					Open_close_parameter_can_prmContext c = (Open_close_parameter_can_prmContext)(ctx.close_parameterlist().close_parameter(i).open_close_parameter_can_prm() );
					if (c.getText().equals("CAN")) {
						if (hasCAN) ErrorStack.add("multiple CAN attributes");
						hasCAN=true;
					}


					if (c.getText().equals("PRM")) {
						if (hasPRM) ErrorStack.add("multiple PRM attributes");
						hasPRM=true;
					}
				}


			}
			if (hasCAN && hasPRM) {
				ErrorStack.add("ether CAN or PRM allowed");
			}
		}

		ErrorStack.leave();
		return null;
	}


	@Override
	public Void visitPutStatement(SmallPearlParser.PutStatementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check IOStatements: visitPositionPUT");
		}

		// enshure that the dation id of type ALPHIC
		ErrorStack.enter(ctx, "PUT");

		TypeDation d = lookupDation(ctx.dationName());

		if (!d.isAlphic()) {
			ErrorStack.enter(ctx.dationName());
			ErrorStack.add("need ALPHIC dation");
			ErrorStack.leave();
		}


		if (ctx.expression().size() > 0) {
			// count number of format(without positions) 
			int nbr = 0;
			for (int i = 0; i<ctx.formatPosition().size(); i++) {
				if (ctx.formatPosition(i) instanceof SmallPearlParser.FactorFormatContext) {
					nbr ++;
				}
			}
			if (nbr == 0) {
				ErrorStack.add("need at least 1 format element");
			}
		}

		visitChildren(ctx);
		ErrorStack.leave();
		return null;
	}

	/**
	 * check if the ID-list is not INV
	 * check type of dation
	 * no positioning after last format element in position/format list
	 */
	@Override
	public Void visitGetStatement(SmallPearlParser.GetStatementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check IOStatements: visitPositionGET");
		}

		// enshure that the dation id of type ALPHIC
		ErrorStack.enter(ctx, "GET");

		TypeDation d = lookupDation(ctx.dationName());


		if (!d.isAlphic()) {
			ErrorStack.enter(ctx.dationName());
			ErrorStack.add("need ALPHIC dation");
			ErrorStack.leave();
		}

		for (int i=0; i<ctx.ID().size(); i++) {
			enshureNotInvVariableForInput(ctx.ID(i).toString());
		}

		if (ctx.ID().size() > 0) {
			if (ctx.formatPosition() == null) {
				ErrorStack.add("need format/position");
			} else {
				if (ctx.formatPosition(ctx.formatPosition().size()-1) instanceof FactorPositionContext) {
					ErrorStack.add("format/position list must end with format");
				}
			}
		}

		if (ctx.ID().size() > 0) {
			// count number of format(without positions) 
			int nbr = 0;
			for (int i = 0; i<ctx.formatPosition().size(); i++) {
				if (ctx.formatPosition(i) instanceof SmallPearlParser.FactorFormatContext) {
					nbr ++;
				} 
			}

			if (nbr != ctx.ID().size()) {
				ErrorStack.add("number of format elements differs from data elements");
			}
		}


		visitChildren(ctx);
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitConvertToStatement(SmallPearlParser.ConvertToStatementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check IOStatements: visitConvertTo");
		}

		// enshure that the dation id of type ALPHIC
		ErrorStack.enter(ctx, "CONVERT");


		if (ctx.expression().size() > 0) {
			// count number of format(without positions) 
			int nbr = 0;
			for (int i = 0; i<ctx.formatPosition().size(); i++) {
				if (ctx.formatPosition(i) instanceof SmallPearlParser.FactorFormatContext) {
					nbr ++;
				}
			}
			if (nbr == 0) {
				ErrorStack.add("need at least 1 format element");
			}
		}

		// check TO parameter
		String toName = ctx.idCharacterString().ID().toString();

		SymbolTableEntry se = m_currentSymbolTable.lookup(toName);

		TypeChar c = enshureCharacterString(toName);
		if (c != null) {
			// INV check is missing
			if (((VariableEntry)se).getAssigmentProtection()) {
				ErrorStack.add(toName+ " is INV");
			}
		}


		visitChildren(ctx);
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitConvertFromStatement(SmallPearlParser.ConvertFromStatementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check IOStatements: visitConvertFrom");
		}

		// enshure that the dation id of type ALPHIC
		ErrorStack.enter(ctx, "CONVERT");


		if (ctx.ID().size() > 0) {
			// count number of format(without positions) 
			int nbr = 0;
			for (int i = 0; i<ctx.formatPosition().size(); i++) {
				if (ctx.formatPosition(i) instanceof SmallPearlParser.FactorFormatContext) {
					nbr ++;
				}
			}
			if (nbr == 0) {
				ErrorStack.add("need at least 1 format element");
			}
		}

		// check FROM parameter
		String fromName = ctx.idCharacterString().ID().toString();

		enshureCharacterString(fromName);

		for (int i=0; i<ctx.ID().size(); i++) {
			enshureNotInvVariableForInput(ctx.ID(i).toString());
		}

		visitChildren(ctx);
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitReadStatement(SmallPearlParser.ReadStatementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check IOStatements: visitPositionREAD");
		}

		// enshure that the dation id of type 'type'
		ErrorStack.enter(ctx, "READ");

		TypeDation d = lookupDation(ctx.dationName());

		if (d.isAlphic() || d.isBasic()) {
			ErrorStack.enter(ctx.dationName());
			ErrorStack.add("need 'type' dation");
			ErrorStack.leave();
		}

		for (int i=0; i<ctx.ID().size(); i++) {
			enshureNotInvVariableForInput(ctx.ID(i).toString());
		}

		// check if absolute positions follow relative positions
		boolean foundAbsolutePosition= false;
		boolean foundRelativePosition = false;
		for (int i = 0; i<ctx.position().size(); i++) {
			ParseTree child = ctx.position(i).getChild(0); 

			if (child instanceof SmallPearlParser.AbsolutePositionContext && foundRelativePosition) {
				ErrorStack.warn("relative positioning before absolute positioning is void");
				foundRelativePosition = false;
			}
			if (child instanceof SmallPearlParser.RelativePositionContext) {
				foundRelativePosition = true;
			}
		}

		visitChildren(ctx);
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitWriteStatement(SmallPearlParser.WriteStatementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check IOStatements: visitPositionWRITE");
		}

		// enshure that the dation id of type ALPHIC
		ErrorStack.enter(ctx, "WRITE");

		TypeDation d = lookupDation(ctx.dationName());

		if (d.isAlphic() || d.isBasic()) {
			ErrorStack.enter(ctx.dationName());
			ErrorStack.add("need 'type' dation");
			ErrorStack.leave();
		}

		// check if absolute positions follow relative positions

		boolean foundAbsolutePosition= false;
		boolean foundRelativePosition = false;
		for (int i = 0; i<ctx.position().size(); i++) {
			ParseTree child = ctx.position(i).getChild(0); 

			if (child instanceof SmallPearlParser.AbsolutePositionContext) {
				ErrorStack.enter(ctx.position(i));
				if (foundRelativePosition) {
					ErrorStack.warn("relative positioning before absolute positioning is void");
					foundRelativePosition = false;
				}
				if (foundAbsolutePosition) {
					ErrorStack.warn("previous absolute positioning is void");
				}
				ErrorStack.leave();
				foundAbsolutePosition = true;
			}
			if (child instanceof SmallPearlParser.RelativePositionContext) {
				foundRelativePosition = true;
			}
		}

		visitChildren(ctx);
		ErrorStack.leave();
		return null;
	}
	@Override
	public Void visitTakeStatement(SmallPearlParser.TakeStatementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check IOStatements: visitPositionTAKE");
		}

		// enshure that the dation id of type BASIC
		ErrorStack.enter(ctx, "TAKE");

		TypeDation d = lookupDation(ctx.dationName());
		if (!d.isBasic()) {
			ErrorStack.enter(ctx.dationName());
			ErrorStack.add("need BASIC dation");
			ErrorStack.leave();
		}

		if(ctx.ID() == null) {
			ErrorStack.add("OpenPEARL needs one variable");
		} else {
			SymbolTableEntry se = m_currentSymbolTable.lookup(ctx.ID().getText());
			if (se == null) {
				ErrorStack.add("'" + ctx.ID().getText()+"' not defined");
			} else {
				if (se instanceof VariableEntry) {
					if (d.getTypeOfTransmission() != null &&
							!d.getTypeOfTransmission().equals("ALL") ) {

						if (!((VariableEntry)se).getType().toString().equals(d.getTypeOfTransmission())) {
							ErrorStack.add("type mismatch: expected "+d.getTypeOfTransmission()+ " -- got "+
									((VariableEntry)se).getType().toString());
						}
					}
				}
			}
			enshureNotInvVariableForInput(ctx.ID().toString());
		}
		if (ctx.take_send_rst() != null) {
			ErrorStack.enter(ctx.take_send_rst(), "RST");
			CheckPrecision(ctx.take_send_rst().ID().getText(), ctx.take_send_rst());
			ErrorStack.leave();
		}
		visitChildren(ctx);
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitSendStatement(SmallPearlParser.SendStatementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check IOStatements: visitPositionSEND");
		}

		// enshure that the dation id of type BASIC
		ErrorStack.enter(ctx, "SEND");

		TypeDation d = lookupDation(ctx.dationName());

		if (!d.isBasic()) {
			ErrorStack.add("need BASIC dation");
		}	   

		if(ctx.expression() == null) {
			ErrorStack.add("OpenPEARL needs one expression");
		} else {
			String typeOfExpression = lookupTypeOfExpressionOrConstant(ctx.expression());
			if (d.getTypeOfTransmission() != null &&
					!d.getTypeOfTransmission().equals(typeOfExpression) ) {
				ErrorStack.add("type mismatch: expected "+d.getTypeOfTransmission()+ " -- got "+
						typeOfExpression);



			}
		}

		if (ctx.take_send_rst() != null) {
			ErrorStack.enter(ctx.take_send_rst(), "RST");
			CheckPrecision(ctx.take_send_rst().ID().getText(), ctx.take_send_rst());
			ErrorStack.leave();
		}
		visitChildren(ctx);
		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitPositionRST(SmallPearlParser.PositionRSTContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check IOStatements: visitPositionRST");
		}

		CheckPrecision(ctx.ID().getText(), ctx);
		return null;
	}

	@Override
	public Void visitFieldWidth(SmallPearlParser.FieldWidthContext ctx) {

		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitFieldWidth");
		}


		ErrorStack.enter(ctx, "fieldwidth");

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
			System.out.println("Semantic: Check IOStatements: visitNumberOfCharacters");
		}

		ErrorStack.enter(ctx, "numberOfCharacters");

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
			.println("Semantic: Check IOStatements: visitDecimalPositions");
		}

		ErrorStack.enter(ctx, "decimal positions");

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
			System.out.println("Semantic: Check IOStatements: visitSignificance");
		}

		ErrorStack.enter(ctx, "significance");

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
			System.out.println("Semantic: Check IOStatements: visitFloatFormatE");
		}

		ErrorStack.enter(ctx, "E-format");

		enshureAlphicDation(ctx);

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
			System.out.println("Semantic: Check IOStatements: visitFloatFormatE3");
		}

		ErrorStack.enter(ctx, "E3-format");

		enshureAlphicDation(ctx);

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
			System.out.println("Semantic: Check IOStatements: visitFixedFormat");
		}

		ErrorStack.enter(ctx, "F-format");

		enshureAlphicDation(ctx);

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
			System.out.println("Semantic: Check IOStatements: visitDurationFormat");
		}

		ErrorStack.enter(ctx, "D-format");

		enshureAlphicDation(ctx);

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
			System.out.println("Semantic: Check IOStatements: visitTimeFormat");
		}

		ErrorStack.enter(ctx, "T-format");

		enshureAlphicDation(ctx);

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
			System.out.println("Semantic: Check IOStatements: visitCharacterStringFormat");
		}

		ErrorStack.enter(ctx, "A-format");

		enshureAlphicDation(ctx);

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
	public Void visitCharacterStringFormatS(SmallPearlParser.CharacterStringFormatSContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitCharacterStringFormatS");
		}

		ErrorStack.enter(ctx, "S-format");

		enshureAlphicDation(ctx);

		ErrorStack.add("S-format not sopported yet");
		/*
		// check the types of all children
		visitChildren(ctx);
		if (ErrorStack.getLocalCount() == 0) {

			// check of the parameters is possible if they are
			// of type ConstantFixedValue
			// or not given
			// check of the value oof width is already done
		}
		 */
		ErrorStack.leave();
		return null;
	}
	@Override
	public Void visitBitFormat1(SmallPearlParser.BitFormat1Context ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitBitFormat1");
		}

		ErrorStack.enter(ctx, "B/B1-format");

		enshureAlphicDation(ctx);

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
			System.out.println("Semantic: Check IOStatements: visitBitFormat2");
		}

		ErrorStack.enter(ctx, "B2-format");

		enshureAlphicDation(ctx);

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
	public Void visitBitFormat3(SmallPearlParser.BitFormat3Context ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitBitFormat3");
		}

		ErrorStack.enter(ctx, "B3-format");

		enshureAlphicDation(ctx);

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
	public Void visitBitFormat4(SmallPearlParser.BitFormat4Context ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitBitFormat4");
		}

		ErrorStack.enter(ctx, "B4-format");

		enshureAlphicDation(ctx);

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

	/* --------------------------------------------------- */
	// positions
	@Override
	public Void visitPositionX(SmallPearlParser.PositionXContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitPositionX");
		}

		ErrorStack.enter(ctx, "X-format");

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
			System.out.println("Semantic: Check IOStatements: visitPositionSkip");
		}

		ErrorStack.enter(ctx, "SKIP-format");

		// check the types of all children
		// check, if we have ASTattributes for this node
		if (ctx.expression() != null) {
			enshureTypeFixed(ctx.expression());
		}

		enshureDimensionsFit(ctx,2);

		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitPositionPAGE(SmallPearlParser.PositionPAGEContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitPositionPAGE");
		}

		ErrorStack.enter(ctx, "PAGE-format");

		// check the types of all children
		// check, if we have ASTattributes for this node
		if (ctx.expression() != null) {
			enshureTypeFixed(ctx.expression());
		}
		enshureDimensionsFit(ctx,3);

		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitPositionADV(SmallPearlParser.PositionADVContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitPositionADV");
		}

		ErrorStack.enter(ctx, "ADV-format");

		// check the types of all children
		// check, if we have ASTattributes for this node
		for (int i=0; i< ctx.expression().size(); i++) {
			enshureTypeFixed(ctx.expression(i));
		}

		enshureDimensionsFit(ctx,ctx.expression().size());

		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitPositionPOS(SmallPearlParser.PositionPOSContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitPositionPOS");
		}

		ErrorStack.enter(ctx, "POS-format");

		enshureDirectDation(ctx);

		// check the types of all children
		// check, if we have ASTattributes for this node
		for (int i=0; i< ctx.expression().size(); i++) {
			enshureTypeFixed(ctx.expression(i));
			enshureGreaterZero(ctx.expression(i));
		}
		enshureDimensionsFit(ctx,ctx.expression().size());

		ErrorStack.leave();
		return null;
	}

	@Override
	public Void visitPositionSOP(SmallPearlParser.PositionSOPContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitPositionSOP");
		}

		ErrorStack.enter(ctx, "SOP-format");

		enshureDirectDation(ctx);

		// check the types of all children
		// check, if we have ASTattributes for this node
		for (int i=0; i< ctx.ID().size(); i++) {
			CheckFixedVariable(ctx.ID(i).getText(), ctx);
		}
		enshureDimensionsFit(ctx,ctx.ID().size());

		ErrorStack.leave();
		return null;
	}
	@Override
	public Void visitPositionCOL(SmallPearlParser.PositionCOLContext ctx) {
		if (m_debug) {
			System.out.println("Semantic: Check IOStatements: visitPositionCOL");
		}

		ErrorStack.enter(ctx, "COL-format");

		enshureDirectDation(ctx);

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
			System.out.println("Semantic: Check IOStatements: visitPositionLINE");
		}

		ErrorStack.enter(ctx, "LINE-format");

		enshureDirectDation(ctx);
		enshureDimensionsFit(ctx,2);

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

		ErrorStack.enter(ctx, "expression");
		if (!(type instanceof TypeFixed)) {
			ErrorStack.add("must be FIXED");
		}
		ErrorStack.leave();		
		return null;
	}

	private Void enshureGreaterZero(SmallPearlParser.ExpressionContext ctx){
		ErrorStack.enter(ctx, "expression");
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


		ErrorStack.enter(ctx, "RST");

		if (entry != null && entry instanceof VariableEntry) {
			// it would be got to set a new error environment to the 
			// context of the 'entry'
			var = (VariableEntry) entry;

			if ( var.getType() instanceof TypeFixed) {
				TypeFixed type = (TypeFixed) var.getType();
				if (type.getPrecision() < 15) {
					ErrorStack.add("must be at least FIXED(15) -- got FIXED("+type.getPrecision()+")");
				}
			} else {
				ErrorStack.add("variable must be FIXED");
			}
		} else {
			ErrorStack.add(id + " is not defined"); 
		}

		ErrorStack.leave();

		return null;
	}

	private Void CheckFixedVariable(String id, ParserRuleContext ctx) {

		SymbolTableEntry entry = m_currentSymbolTable.lookup(id);
		VariableEntry var = null;

		ErrorStack.enter(ctx, "variable");

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

	private void enshureAlphicDation(RuleContext ctx) {
		TypeDation d = lookupDation(ctx);
		if (!d.isAlphic()) {
			ErrorStack.add("applies only on ALPHIC dations");
		}
	}

	private void enshureDirectDation(RuleContext ctx) {
		TypeDation d = lookupDation(ctx);
		if (!d.isDirect()) {
			ErrorStack.add("format applies only on DIRECT dations");
		}
	}

	private void allowBackwardPositioning(RuleContext ctx) {
		TypeDation d = lookupDation(ctx);
		if (!d.isDirect() && !d.isForback()){
			ErrorStack.add("backward positioning need DIRECT or FORBACK");
		}
	}

	private void enshureDimensionsFit(RuleContext ctx, int nbr) {
		TypeDation d = lookupDation(ctx);

		if (d.getNumberOfDimensions() < nbr) {
			ErrorStack.add("too many dimensions used (dation provides "+d.getNumberOfDimensions()+")"); 
		}
	}



	private void enshureNotInvVariableForInput(String var) {
		SymbolTableEntry se = m_currentSymbolTable.lookup(var);
		if (se == null) {
			ErrorStack.add("'" + var+"' is not defined");
		} else if ( (se instanceof VariableEntry) ) {
			if (((VariableEntry)se).getAssigmentProtection()) {
				ErrorStack.add("'" + var + "' is INV");
			}
		}
	}

	/**
	 * check if the given name is defined and of type CHAR
	 * return the element from thesymbol table
	 * or return null
	 * 
	 * @param convertString
	 * @return null, if no symbol was found, or symbol is not of typeChar<br>
	 *    reference to the TypeChar for further analysis
	 */
	private TypeChar  enshureCharacterString(String convertString) {
		SymbolTableEntry se = m_currentSymbolTable.lookup(convertString);
		if (se == null) {
			ErrorStack.add("'" + convertString+"' is not defined");
		} else if ( (se instanceof VariableEntry) ) {
			if (((VariableEntry)se).getType() instanceof TypeChar) {
				// maybe we need further details about the variable 
				// like INV
				TypeChar c = (TypeChar)((VariableEntry)se).getType();
				return c;
			}
		} else {
			ErrorStack.add("'"+ convertString + "' must be CHAR -- has type "+ (((VariableEntry)se).getType().toString()));
		}
		return null;
	}

	private TypeDation lookupDation(String userDation) {
		SymbolTableEntry se = m_currentSymbolTable.lookup(userDation);
		if (se == null) {
			ErrorStack.add("'" + userDation+"' is not defined");
		} else if ( (se instanceof VariableEntry) ) {
			if (((VariableEntry)se).getType() instanceof TypeDation) {
				TypeDation sd = (TypeDation)(((VariableEntry)se).getType());
				if (sd.isSystemDation()) {
					ErrorStack.add("need user dation");
				}
				return sd;
			} else if (((VariableEntry)se).getType() instanceof TypeChar) {
				// lets simulate user dation for CONVERT
				TypeChar c = (TypeChar)((VariableEntry)se).getType();
				TypeDation sd = new TypeDation();
				sd.setDimension1(c.getSize());
				sd.setAlphic(true);
				sd.setDirect(true);
				return sd;
			}
		} else {
			ErrorStack.add("'"+ userDation + "' must be DATION -- has type "+ (((VariableEntry)se).getType().toString()));
		}
		return null;
	}


	private TypeDation lookupDation(RuleContext ctx) {
		String userDation = "";
		boolean isConvert = false;

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
			if (p instanceof SmallPearlParser.ConvertToStatementContext) {
				SmallPearlParser.ConvertToStatementContext stmnt = (SmallPearlParser.ConvertToStatementContext) p;
				userDation = stmnt.idCharacterString().ID().toString();  // name of the string
				isConvert = true;
				p=null;
			}
			if (p instanceof SmallPearlParser.ConvertFromStatementContext) {
				SmallPearlParser.ConvertFromStatementContext stmnt = (SmallPearlParser.ConvertFromStatementContext) p;
				userDation = stmnt.idCharacterString().ID().toString();  // name of the string
				isConvert = true;
				p=null;
			}
			if (p instanceof SmallPearlParser.ReadStatementContext) {
				SmallPearlParser.ReadStatementContext stmnt = (SmallPearlParser.ReadStatementContext) p;
				userDation = stmnt.dationName().ID().toString();
				p=null;
			}
			if (p instanceof SmallPearlParser.WriteStatementContext) {
				SmallPearlParser.WriteStatementContext stmnt = (SmallPearlParser.WriteStatementContext) p;
				userDation = stmnt.dationName().ID().toString();
				p=null;
			}
			if (p instanceof SmallPearlParser.TakeStatementContext) {
				SmallPearlParser.TakeStatementContext stmnt = (SmallPearlParser.TakeStatementContext) p;
				userDation = stmnt.dationName().ID().toString();
				p=null;
			}
			if (p instanceof SmallPearlParser.SendStatementContext) {
				SmallPearlParser.SendStatementContext stmnt = (SmallPearlParser.SendStatementContext) p;
				userDation = stmnt.dationName().ID().toString();
				p=null;
			}
		}

		if (userDation == null) {
			ErrorStack.add("no userdation found");
		} else {
			TypeDation sd = lookupDation(userDation);
			return sd;
		}
		return null;
	}

	private String lookupTypeOfExpressionOrConstant(ExpressionContext ctx) {

		ASTAttribute attr = m_ast.lookup(ctx);
		boolean isArray = false;

		if (attr != null) {
			String s = attr.getType().toString();

			if (attr.isReadOnly()) {
			} else {
				VariableEntry ve = attr.getVariable();

				if (ve != null && ve.getType() instanceof TypeArray) {
					isArray = true;
					s = ve.getType().toString();
				}
			}
			return s;
		}
		return null;
	}


}
