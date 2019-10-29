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

import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.Exception.InternalCompilerErrorException;
import org.smallpearl.compiler.SmallPearlParser.*;
import org.smallpearl.compiler.SymbolTable.*;
import org.stringtemplate.v4.ST;

/**
 * check all real time statements
 * 
 * <ul>
 * <li> task-dcl + operations
 * <li> semaphore operations (see below)
 * <li> bolt operations (see below)
 * <li> interrupt operation
 * </ul>
 * 
	these rules are already checkes in SymbolTableVisitor


	semaRequest: 'REQUEST' ID ( ',' ID)* ';'
    	;


	semaRelease: 'RELEASE' ID ( ',' ID)* ';'
    	;

	boltEnter: 'ENTER' ID ( ',' ID)* ';'
    	;

	boltReserve: 'RESERVE' ID ( ',' ID)* ';'
    	;
	boltFree: 'FREE' ID ( ',' ID)* ';'
    	;
	boltLeave: 'LEAVE' ID ( ',' ID)* ';'
    	;

 * 
 * @author mueller
 *
 */
public class CheckRealTimeStatements extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

	private int m_verbose;
	private boolean m_debug;
	private String m_sourceFileName;
	private ExpressionTypeVisitor m_expressionTypeVisitor;
	private SymbolTableVisitor m_symbolTableVisitor;
	private SymbolTable m_symboltable;
	private SymbolTable m_currentSymbolTable;
	private ModuleEntry m_module;
	private AST m_ast = null;

	public CheckRealTimeStatements(String sourceFileName,
			int verbose,
			boolean debug,
			SymbolTableVisitor symbolTableVisitor,
			ExpressionTypeVisitor expressionTypeVisitor,
			AST ast){

		m_debug = debug;
		m_verbose = verbose;
		m_sourceFileName = sourceFileName;
		m_symbolTableVisitor = symbolTableVisitor;
		m_expressionTypeVisitor = expressionTypeVisitor;
		m_symboltable = symbolTableVisitor.symbolTable;
		m_currentSymbolTable = m_symboltable;
		m_ast = ast;

		Log.debug( "    Check Template");
	}

	@Override
	public Void visitModule(SmallPearlParser.ModuleContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check RT-statements: visitModule");
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
			System.out.println( "Semantic: Check RT-statements: visitProcedureDeclaration");
		}

		this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
		visitChildren(ctx);
		this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
		return null;
	}


	@Override
	public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check RT-statements: visitBlock_statement");
		}

		this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
		visitChildren(ctx);
		this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
		return null;
	}

	@Override
	public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check RT-statements: visitLoopStatement");
		}

		this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
		visitChildren(ctx);
		this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
		return null;
	}

	/* ----------------------------------------------------------------------- */
	/* class specify stuff starts here                                         */
	/* ----------------------------------------------------------------------- */
	@Override
	public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
		if (m_debug) {
			System.out.println( "Semantic: Check RT-statements: visitTaskDeclaration");
		}

		this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

		// taskDeclaration :
		//ID ':' 'TASK' priority? task_main? ';' taskBody 'END' ';' cpp_inline?
		//	    ;
		// ID is already in the symbol table

		// let's check priority
		if (ctx.priority() != null) {
			checkPriority(ctx.priority().expression());
		}


		visitChildren(ctx);
		m_currentSymbolTable = m_currentSymbolTable.ascend();


		return null;
	}



	/**
     taskStart: startCondition? frequency? 'ACTIVATE' ID  priority? ';'   ;

     ID must be of type task
     types in startCondition and frequency must fit
     priority must be of correct type and range
	 */
	@Override
	public Void visitTaskStart(SmallPearlParser.TaskStartContext ctx) {
		ErrorStack.enter(ctx,"ACTIVATE");

		String taskName = ctx.ID().toString();
		SymbolTableEntry se = m_currentSymbolTable.lookup(taskName);
		if (se == null) {
			ErrorStack.add("'"+taskName+"' is undefined");
		} else if (!(se instanceof TaskEntry)) {
			if (se instanceof VariableEntry) {
				ErrorStack.add(taskName+" must be a TASK -- but is "+
						((VariableEntry)(se)).getType().toString());
			} else if (se instanceof InterruptEntry) {
				ErrorStack.add(taskName+" must be a TASK -- but is INTERRUPT");
			} else {
				ErrorStack.add(taskName+" must be a TASK -- but is "+se.toString());
			}
		}


		if (ctx.startCondition() instanceof SmallPearlParser.StartConditionATContext) {
			SmallPearlParser.StartConditionATContext c = (SmallPearlParser.StartConditionATContext) ctx
					.startCondition();
			checkClockValue(c.expression(),"AT");
		} else 		if (ctx.startCondition() instanceof SmallPearlParser.StartConditionAFTERContext) {
			SmallPearlParser.StartConditionAFTERContext c = (SmallPearlParser.StartConditionAFTERContext) ctx
					.startCondition();
			checkDurationValue(c.expression(),"AFTER");
		} else if (ctx.startCondition() instanceof SmallPearlParser.StartConditionWHENContext) {
			SmallPearlParser.StartConditionWHENContext c = (SmallPearlParser.StartConditionWHENContext) ctx
					.startCondition();
			checkInterrupt(c,"WHEN");
			if (c.expression()!= null) {
				checkDurationValue(c.expression(), "AFTER");
			}
		}

		if (ctx.frequency() != null) {
			SmallPearlParser.FrequencyContext c = ctx.frequency();
			checkDurationValue(c.expression(0),"ALL");
			
			for (int i = 0; i < c.getChildCount(); i++) {
				if (c.getChild(i) instanceof TerminalNodeImpl) {
					if (((TerminalNodeImpl) c.getChild(i)).getSymbol()
							.getText().equals("ALL")) {
						// ALL is mandatory!
					} else if (((TerminalNodeImpl) c.getChild(i)).getSymbol()
							.getText().equals("UNTIL")) {
						checkClockValue(c.expression(1), "UNTIL");
					} else if (((TerminalNodeImpl) c.getChild(i)).getSymbol()
							.getText().equals("DURING")) {
						checkDurationValue(c.expression(1), "DURING");
					} else {
						System.err.println("untreated alternative: "+((TerminalNodeImpl) c.getChild(i)).getSymbol()
							.getText());
					}
				}
			}
		}

		if (ctx.priority()!= null) {
			checkPriority(ctx.priority().expression());
		}

		ErrorStack.leave();
		return null;
	}

	/**
      task_terminating: 'TERMINATE' ID? ';' ;
	 */
	@Override 
	public Void visitTask_terminating(SmallPearlParser.Task_terminatingContext ctx) {
		ErrorStack.enter(ctx,"TERMINATE");
		checkTask(ctx.ID());
		ErrorStack.leave();

		return null;
	}

	/* 
      task_suspending : 'SUSPEND' ID? ';' ;
	 */
	@Override 
	public Void visitTask_suspending(SmallPearlParser.Task_suspendingContext ctx) {
		ErrorStack.enter(ctx,"SUSPEND");
		checkTask(ctx.ID());
		ErrorStack.leave();

		return null;
	}

	/* 
    task_preventing: 'PREVENT' ID? ';' ;
	 */
	@Override 
	public Void visitTask_preventing(SmallPearlParser.Task_preventingContext ctx) {
		ErrorStack.enter(ctx,"PREVENT");
		checkTask(ctx.ID());
		ErrorStack.leave();

		return null;
	}

	/* 
  taskResume : startCondition 'RESUME' ';'     ;
	 */
	@Override 
	public Void visitTaskResume(SmallPearlParser.TaskResumeContext ctx) {
		ErrorStack.enter(ctx,"RESUME");

		// copied from startTask 
		if (ctx.startCondition() instanceof SmallPearlParser.StartConditionATContext) {
			SmallPearlParser.StartConditionATContext c = (SmallPearlParser.StartConditionATContext) ctx
					.startCondition();
			checkClockValue(c.expression(),"AT");
		} else 		if (ctx.startCondition() instanceof SmallPearlParser.StartConditionAFTERContext) {
			SmallPearlParser.StartConditionAFTERContext c = (SmallPearlParser.StartConditionAFTERContext) ctx
					.startCondition();
			checkDurationValue(c.expression(),"AFTER");
		} else if (ctx.startCondition() instanceof SmallPearlParser.StartConditionWHENContext) {
			SmallPearlParser.StartConditionWHENContext c = (SmallPearlParser.StartConditionWHENContext) ctx
					.startCondition();
			checkInterrupt(c,"WHEN");
		}
		ErrorStack.leave();

		return null;
	}

	/* 
    taskContinuation : startCondition? 'CONTINUE' ID? priority? ';' ;
	 */
	@Override 
	public Void visitTaskContinuation(SmallPearlParser.TaskContinuationContext ctx) {
		ErrorStack.enter(ctx,"CONTINUE");


		// copied from startTask 
		if (ctx.startCondition() instanceof SmallPearlParser.StartConditionATContext) {
			SmallPearlParser.StartConditionATContext c = (SmallPearlParser.StartConditionATContext) ctx
					.startCondition();
			checkClockValue(c.expression(),"AT");
		} else 		if (ctx.startCondition() instanceof SmallPearlParser.StartConditionAFTERContext) {
			SmallPearlParser.StartConditionAFTERContext c = (SmallPearlParser.StartConditionAFTERContext) ctx
					.startCondition();
			checkDurationValue(c.expression(),"AFTER");
		} else if (ctx.startCondition() instanceof SmallPearlParser.StartConditionWHENContext) {
			SmallPearlParser.StartConditionWHENContext c = (SmallPearlParser.StartConditionWHENContext) ctx
					.startCondition();
			checkInterrupt(c,"WHEN");
		}

		checkTask(ctx.ID());

		if (ctx.priority()!= null) {
			checkPriority(ctx.priority().expression());
		}

		ErrorStack.leave();

		return null;
	}


	/*
     disableStatement : 'DISABLE' ID ';'     ;
	 */
	@Override
	public Void visitDisableStatement(DisableStatementContext ctx) {
		ErrorStack.enter(ctx,"DISABLE");
		checkInterrupt(ctx.ID());
		ErrorStack.leave();
		return null;
	}


	/*
    enableStatement : 'ENABLE' ID ';'     ;
	 */
	@Override
	public Void visitEnableStatement(EnableStatementContext ctx) {
		ErrorStack.enter(ctx,"ENABLE");
		checkInterrupt(ctx.ID());
		ErrorStack.leave();
		return null;
	}

	/*
   triggerStatement : 'TRIGGER' ID ';'     ;
	 */
	@Override
	public Void visitTriggerStatement(TriggerStatementContext ctx) {
		ErrorStack.enter(ctx,"TRIGGER");
		checkInterrupt(ctx.ID());
		ErrorStack.leave();
		return null;
	}


	private void checkPriority(SmallPearlParser.ExpressionContext ctx) {
		ErrorStack.enter(ctx,"priority");
		ASTAttribute attr = m_ast.lookup(ctx);
		TypeDefinition t = m_ast.lookupType(ctx);

		if (t instanceof TypeFixed) {
			if (attr.isReadOnly()) {
				long p = attr.getConstantFixedValue().getValue();
				if (p< Defaults.BEST_PRIORITY || p > Defaults.LOWEST_PRIORITY) {
					ErrorStack.add("must be in ["+Defaults.BEST_PRIORITY+","+Defaults.LOWEST_PRIORITY+"]");
				}
			}
		} else {
			ErrorStack.add("must be of type FIXED");
		}
		ErrorStack.leave();

	}

	private void checkClockValue(ExpressionContext ctx, String prefix) {
		ASTAttribute attr = m_ast.lookup(ctx);
		TypeDefinition t = m_ast.lookupType(ctx);

		ErrorStack.enter(ctx,prefix);
		if (!(t instanceof TypeClock)) {
			ErrorStack.add("must be of type CLOCK  -- but is of "+t.toString());
		}
		ErrorStack.leave();

	}


	private void checkDurationValue(ExpressionContext ctx, String prefix) {
		ASTAttribute attr = m_ast.lookup(ctx);
		TypeDefinition t = m_ast.lookupType(ctx);

		ErrorStack.enter(ctx,prefix);
		if (!(t instanceof TypeDuration)) {
			ErrorStack.add("must be of type DURATION  -- but is of "+t.toString());
		} else {
			if (attr.isReadOnly()) {
				ConstantDurationValue cd = attr.getConstantDurationValue();
				if (cd.getValue() <= 0) {
					ErrorStack.add("must be > 0");
				}
			}
		}
		ErrorStack.leave();
	}

	private void checkInterrupt(StartConditionWHENContext ctx, String prefix) {

		String iName = ctx.ID().toString();

		ErrorStack.enter(ctx,prefix);
		checkInterrupt(ctx.ID());
		/*
		SymbolTableEntry se = m_currentSymbolTable.lookup(iName);
		if (se == null) {
			ErrorStack.add("'"+iName+"' not defined");
		} else {
			if (!(se instanceof InterruptEntry)) {
				if (se instanceof VariableEntry) {
				    ErrorStack.add("'" + iName + "' must be INTERRUPT -- but is " + ((VariableEntry)se).getType().toString());
				} else {
				    ErrorStack.add("'" + iName + "' must be INTERRUPT -- but is ???" );
				}
			}
		}
		 */
		ErrorStack.leave();
	}

	private void checkTask(TerminalNode tName) {
		if (tName != null) {
			String taskName = tName.toString();
			SymbolTableEntry se = m_currentSymbolTable.lookup(taskName);	
			if (se == null) {
				ErrorStack.add("'"+ taskName+"' is not defined");
			} else {
				if (!(se instanceof TaskEntry)) {
					if (se instanceof VariableEntry) {
						ErrorStack.add("'"+taskName+"' must be TASK -- but is "+ ((VariableEntry)se).getType().toString());
					} else if (se instanceof InterruptEntry) {
						ErrorStack.add("'"+taskName+"' must be TASK -- but is INTERRUPT");
					} else {
						ErrorStack.add("'"+taskName+"' must be TASK -- but is "+ se.getClass().getTypeName());
					}
				}
			}
		}
	}

	private void checkInterrupt(TerminalNode iName) {
		if (iName != null) {
			String intName = iName.toString();
			SymbolTableEntry se = m_currentSymbolTable.lookup(intName);	
			if (se == null) {
				ErrorStack.add("'"+ intName+"' is not defined");
			} else {
				if (!(se instanceof InterruptEntry)) {
					if (se instanceof VariableEntry) {
						ErrorStack.add("'"+intName+"' must be INTERRUPT -- but is "+ ((VariableEntry)se).getType().toString());
					} else if (se instanceof TaskEntry) {
						ErrorStack.add("'"+intName+"' must be INTERRUPT -- but is TASK");
					} else {
						ErrorStack.add("'"+intName+"' must be INTERRUPT -- but is "+ se.getClass().getTypeName());
					}
				}
			}
		}
	}


}