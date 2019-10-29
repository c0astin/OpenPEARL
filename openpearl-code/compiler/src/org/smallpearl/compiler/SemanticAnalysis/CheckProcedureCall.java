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

import org.smallpearl.compiler.*;
import org.smallpearl.compiler.SmallPearlParser.ExpressionContext;
import org.smallpearl.compiler.Exception.ResultDiscardedException;
import org.smallpearl.compiler.SymbolTable.*;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * check procedure and function parameters for definition and invocation
 *
 * @author mueller
 *
 */
public class CheckProcedureCall extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private ExpressionTypeVisitor m_expressionTypeVisitor;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ModuleEntry m_module;
    private AST m_ast = null;

    public CheckProcedureCall(String sourceFileName,
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

        if (m_verbose > 0) {
            System.out.println( "    Check ProcedureCalls");
        }
    }

    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check ProcedureCalls: visitModule");
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
            System.out.println( "Semantic: Check ProcedureCall: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        
        // check parameter list missing
        
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check ProcedureCall: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check ProcedureCall: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check ProcedureCall: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitCallStatement(SmallPearlParser.CallStatementContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check ProcedureCall: visitCallStatement");
        }
        
        ErrorStack.enter(ctx,"CALL");
        String procName = ctx.ID().getText();
        ProcedureEntry proc = null;
        
        SymbolTableEntry entry = m_currentSymbolTable.lookup(procName);

        if (entry != null) {
        	if (entry instanceof ProcedureEntry) {
        		if (m_debug)
        			System.out.println("Semantic: Check ProcedureCall: found call in expression");
        		proc = (ProcedureEntry) entry;
        		TypeDefinition resultType = proc.getResultType();

        		if ( resultType != null ) {
        			ErrorStack.add("result discarded");
        		}
        	} else {
        		ErrorStack.add("'"+ procName+"' is not of type PROC  -- is of type "+ CommonUtils.getTypeOf(entry));
        	}
        } else {
        	ErrorStack.add("'"+ procName+"' is not defined");
        }
       
        if (proc != null && ctx.listOfActualParameters() != null ) {
        	
        	int nbrActualParameters = ctx.listOfActualParameters().expression().size();
        	int nbrFormatParameters = proc.getFormalParameters().size();
        	
        	if (nbrActualParameters != nbrFormatParameters) {
        		ErrorStack.add("number of parameters mismatch: expected "+nbrFormatParameters + " -- got "+nbrActualParameters);
        	} else {
        		// check each parameter type
        		for (int i=0; i<nbrActualParameters; i++) {
     		       ErrorStack.enter(ctx.listOfActualParameters().expression(i),"param");
        		   checkParameter(proc, ctx.listOfActualParameters().expression(i), proc.getFormalParameters().get(i));
        		   ErrorStack.leave();
        		}
        	}
        
        	// really necessary?
        	//      visitListOfActualParameters(ctx.listOfActualParameters());
        }

        ErrorStack.leave();
        
        return null;
    }

    private void checkParameter(ProcedureEntry proc,
    		ExpressionContext expression, FormalParameter formalParameter) {
    	// check types - must be equal if IDENT is set
    	//               formalParameter may be larger if IDENT ist NOT SET
    	// 
    	// check INV  on actual parameter enforces INV on formal parameter
    	// check IDENT must be set for array

    	// analyse expression
    	TypeDefinition actualType = null;
    	boolean actualIsInv = false;
    	boolean actualIsArray = false;
    	int actualArrayDimensions = 0;
    	boolean passByIdent = false;
    	TypeDefinition formalBaseType = null;
    	TypeDefinition actualBaseType = null;
    	    	
    	VariableEntry   actualVariableEntry = null;
    	ASTAttribute attr = m_ast.lookup(expression);

    	if (attr != null) {
    		actualType = attr.getType();
    		actualIsInv = attr.isReadOnly();

    		actualVariableEntry = attr.getVariable();

    		if (actualType instanceof TypeArray) {
    		    actualArrayDimensions = ((TypeArray) actualType).getNoOfDimensions();
    		    actualBaseType = ((TypeArray) actualType).getBaseType();
    		    actualIsArray = true;	
    		}
    	}

    	if (formalParameter.passIdentical) {
    		// actual parameter must be LValue
    		if (actualVariableEntry == null) {
    			ErrorStack.add("constants may not be passed by IDENT");
    			return;  // do no further checks on this parameter
    		} else {
    			if( actualIsInv && !formalParameter.assignmentProtection) {
    				ErrorStack.add("pass INV data as non INV parameter");
    			}
    		}
    	}
    	if (formalParameter.getType() instanceof TypeArray) {
    		formalBaseType = ((TypeArray)formalParameter.getType()).getBaseType();
    	} else {
    		formalBaseType = formalParameter.type;
    	}

    	// compare array parameters
    	if (actualIsArray) {
    		if (formalParameter.getType() instanceof TypeArray) {
    			// both are arrays --> nbr of dimensions and baseTypes must fit 
    			TypeArray ta = ((TypeArray)formalParameter.getType());
    			if (actualArrayDimensions != ta.getNoOfDimensions()) {
    				ErrorStack.add("dimension mismatch: expect "+ta.getNoOfDimensions()+
    						" -- got "+actualArrayDimensions);
    			} else 	if (!actualBaseType.equals(ta.getBaseType())) {
    				String s = actualType.getName();
    				s = actualType.toString();
    				ErrorStack.add("type mismatch: expect ARRAY of " +((TypeArray)formalParameter.getType()).getBaseType() +
    						" -- got ARRAY of "+actualType.toString());
    			}
    		} else {
    			ErrorStack.add("expected scalar type");
    		}
    	} else {
    		if (formalParameter.getType() instanceof TypeArray) {
    			ErrorStack.add("expected array type");
    		} else {
    			// treat both scalar types

    			// easy stuff first -- check base types
    			// if they fit we must check length for FIXED,FLOAT,CHAR,BIT
    			if (!formalBaseType.getName().equals(actualType.getName())) {
    				ErrorStack.add("type mismatch: expected "+formalBaseType.toString()+
    						"  -- got "+actualType.toString());
    			} else if (formalParameter.getType() instanceof TypeFixed) {

    				TypeFixed fp = (TypeFixed)formalParameter.getType();
    				TypeFixed ap = (TypeFixed)actualType;

    				if (formalParameter.passIdentical == false && fp.getPrecision().intValue() < ap.getPrecision().intValue()) {
    					ErrorStack.add("type mismatch: expected (not larger than) " + formalParameter.toString() + " -- got "+actualType.toString());
    				}
    				if (formalParameter.passIdentical && fp.getPrecision().intValue() != ap.getPrecision().intValue()) {
    					ErrorStack.add("type mismatch: expected " + formalParameter.toString() + " -- got "+actualType.toString());
    				}
    			} else if (formalParameter.getType() instanceof TypeFloat && actualType instanceof TypeFixed) {
    				// implicit FIXED->FLOAT conversion is ok if NOT IDENT
    				TypeFloat fp = (TypeFloat)formalParameter.getType();
    				TypeFixed ap = (TypeFixed)actualType;

    				if (formalParameter.passIdentical == false && fp.getPrecision().intValue() < ap.getPrecision().intValue()) {
    					ErrorStack.add("type mismatch: expected (not larger than) " + formalParameter.toString() + " -- got "+actualType.toString());
    				}
    				if (formalParameter.passIdentical) {
    					ErrorStack.add("type mismatch: expected " + formalParameter.toString() + " -- got "+actualType.toString());
    				}
    			} else if (formalParameter.getType() instanceof TypeBit) {

    				TypeBit fp = (TypeBit)formalParameter.getType();
    				TypeBit ap = (TypeBit)actualType;

    				if (formalParameter.passIdentical == false && fp.getPrecision().intValue() < ap.getPrecision().intValue()) {
    					ErrorStack.add("type mismatch: expected (not larger than) " + formalParameter.toString() + " -- got "+actualType.toString());
    				}
    				if (formalParameter.passIdentical && fp.getPrecision().intValue() != ap.getPrecision().intValue()) {
    					ErrorStack.add("type mismatch: expected " + formalParameter.toString() + " -- got "+actualType.toString());
    				}
    			} else if (formalParameter.getType() instanceof TypeChar) {

    				TypeChar fp = (TypeChar)formalParameter.getType();
    				TypeChar ap = (TypeChar)actualType;

    				if (formalParameter.passIdentical == false && fp.getSize() < ap.getSize()) {
    					ErrorStack.add("type mismatch: expected (not larger than) " + formalParameter.toString() + " -- got "+actualType.toString());
    				}
    				if (formalParameter.passIdentical && fp.getSize() != ap.getSize()) {
    					ErrorStack.add("type mismatch: expected " + formalParameter.toString() + " -- got "+actualType.toString());
    				}
    			}
    		}
    	}	

    }
    
    
	@Override
	public Void visitPrimaryExpression(
			SmallPearlParser.PrimaryExpressionContext ctx) {

		if (ctx.ID() != null) {
		    SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.ID().getText());

			if (entry instanceof org.smallpearl.compiler.SymbolTable.ProcedureEntry) {
				org.smallpearl.compiler.SymbolTable.ProcedureEntry proc = (org.smallpearl.compiler.SymbolTable.ProcedureEntry)(entry);
				
				if (ctx.expression() != null && ctx.expression().size() > 0) {
					for (int i=0; i< ctx.expression().size(); i++) {
			   		   ErrorStack.enter(ctx.expression(i),"param");
				       checkParameter(proc, ctx.expression(i), proc.getFormalParameters().get(i));
				       ErrorStack.leave();
					}
				}
			
			}
		}
		return null;
	}
	

}
