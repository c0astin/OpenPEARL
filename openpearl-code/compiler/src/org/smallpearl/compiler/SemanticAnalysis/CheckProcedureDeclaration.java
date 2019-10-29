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

import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.SmallPearlParser.FormatPositionContext;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SymbolTable.FormalParameter;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.ProcedureEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;


/**
 * @author mueller
 *
 * check if all referenced system dations are specified 
 * verify compatibility of system dation attributes with user dation attributes
 * create entry in symbol table with the dations attributes
 * 
 */
public class CheckProcedureDeclaration extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private ExpressionTypeVisitor m_expressionTypeVisitor;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ModuleEntry m_module;
    private AST m_ast = null;
	private TypeDefinition m_type;
	private ParseTreeProperty<SymbolTable> m_symboltablePerContext = null;

    public CheckProcedureDeclaration(String sourceFileName,
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
            System.out.println( "Semantic: CheckProcedureDeclaration: visitModule");
        }

        org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry = m_currentSymbolTable.lookupLocal(ctx.ID().getText());
        m_currentSymbolTable = ((ModuleEntry)symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

   /* @Override
    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: CheckProcedureDeclaration: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }
*/
    @Override
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: CheckProcedureDeclaration: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: CheckProcedureDeclaration: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: CheckProcedureDeclaration: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
	public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
		String globalId = null;
		LinkedList<FormalParameter> formalParameters = null;
		ASTAttribute resultType = null;

		if (m_verbose > 0) {
			System.out.println("SymbolTableVisitor: visitProcedureDeclaration");
		}
		
/*
		for (ParseTree c : ctx.children) {
			if (c instanceof SmallPearlParser.ResultAttributeContext) {
		//		resultType = new ASTAttribute(getResultAttribute((SmallPearlParser.ResultAttributeContext) c));
			} else if (c instanceof SmallPearlParser.GlobalAttributeContext) {
				SmallPearlParser.GlobalAttributeContext globalCtx = (SmallPearlParser.GlobalAttributeContext) c;
				globalId = ctx.ID().getText();
			} else if (c instanceof SmallPearlParser.ListOfFormalParametersContext) {
				SmallPearlParser.ListOfFormalParametersContext listOfFormalParametersContext = (SmallPearlParser.ListOfFormalParametersContext) c;
				getListOfFormalParameters((SmallPearlParser.ListOfFormalParametersContext) c);
			}
		}
*/
	
		SymbolTableEntry entry = this.m_currentSymbolTable.lookup(ctx.ID().toString());
		if (entry == null) {
			throw new InternalCompilerErrorException("PROC "+ctx.ID().toString()+" not found", ctx.start.getLine(), ctx.start.getCharPositionInLine());
		}

		
		ProcedureEntry procedureEntry = (ProcedureEntry)entry;
			
		this.m_currentSymbolTable = this.m_currentSymbolTable.newLevel(procedureEntry);
		
		if ( procedureEntry.getFormalParameters() != null && procedureEntry.getFormalParameters().size() > 0) {
			/* check formal parameters of this procedure */
			
	
			for (FormalParameter formalParameter : procedureEntry.getFormalParameters()) {
				checkFormalParameter(formalParameter);
			}
		}

	//	this.m_symboltablePerContext.put(ctx, this.m_currentSymbolTable);

		visitChildren(ctx);

		this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
		return null;
	}

/*
 	private TypeDefinition getResultAttribute(SmallPearlParser.ResultAttributeContext ctx) {
 		visitChildren(ctx.resultType());
		return m_type;
	}

	private LinkedList<FormalParameter> getListOfFormalParameters(SmallPearlParser.ListOfFormalParametersContext ctx) {
		LinkedList<FormalParameter> listOfFormalParameters = new LinkedList<FormalParameter>();

		if (ctx != null) {
			for (int i = 0; i < ctx.formalParameter().size(); i++) {
				getFormalParameter(listOfFormalParameters, ctx.formalParameter(i));
			}
		}

		return listOfFormalParameters;
	}
*/
	private void checkFormalParameter(FormalParameter formalParameter) {
		
				String name = formalParameter.name;
				Boolean assignmentProtection = formalParameter.assignmentProtection;
				Boolean passIdentical = formalParameter.passIdentical;

				m_type = formalParameter.type;
				ParserRuleContext ctx = formalParameter.m_ctx;
				
				if ((!passIdentical) && m_type instanceof TypeArray) {
					ErrorStack.enter(ctx,"param");
					ErrorStack.add("arrays must passed by IDENT");
					ErrorStack.leave();
				}
				if ((!passIdentical) && m_type instanceof TypeSemaphore) {
					ErrorStack.enter(ctx,"param");
					ErrorStack.add("SEMA must passed by IDENT");
					ErrorStack.leave();
				}
				if ((!passIdentical) && m_type instanceof TypeBolt) {
					ErrorStack.enter(ctx,"param");
					ErrorStack.add("BOLT must passed by IDENT");
					ErrorStack.leave();
				}
				if ((!passIdentical) && m_type instanceof TypeDation) {
					ErrorStack.enter(ctx,"param");
					ErrorStack.add("DATION must passed by IDENT");
					ErrorStack.leave();
				}				
				if ((!passIdentical) && m_type instanceof TypeInterrupt) {
					ErrorStack.enter(ctx,"param");
					ErrorStack.add("INTERRUPT must passed by IDENT");
					ErrorStack.leave();
				}				
				if ((!passIdentical) && m_type instanceof TypeSignal) {
					ErrorStack.enter(ctx,"param");
					ErrorStack.add("SIGNAL must passed by IDENT");
					ErrorStack.leave();
				}

	}

	/*
	private Void getParameterType(SmallPearlParser.ParameterTypeContext ctx) {
		for (ParseTree c : ctx.children) {
			if (c instanceof SmallPearlParser.SimpleTypeContext) {
				visitSimpleType(ctx.simpleType());
			}
		}

		return null;
	}

	@Override
	public Void visitSimpleType(SmallPearlParser.SimpleTypeContext ctx) {
		if (ctx != null) {
			if (ctx.typeInteger() != null) {
				visitTypeInteger(ctx.typeInteger());
			} else if (ctx.typeDuration() != null) {
				visitTypeDuration(ctx.typeDuration());
			} else if (ctx.typeBitString() != null) {
				visitTypeBitString(ctx.typeBitString());
			} else if (ctx.typeFloatingPointNumber() != null) {
				visitTypeFloatingPointNumber(ctx.typeFloatingPointNumber());
			} else if (ctx.typeTime() != null) {
				visitTypeTime(ctx.typeTime());
			} else if (ctx.typeCharacterString() != null) {
				visitTypeCharacterString(ctx.typeCharacterString());
			}
		}

		return null;
	}

    */

}