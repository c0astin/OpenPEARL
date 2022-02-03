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

package org.openpearl.compiler.SemanticAnalysis;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.openpearl.compiler.*;
import org.openpearl.compiler.OpenPearlParser.Realtime_statementContext;
import org.openpearl.compiler.SymbolTable.*;

/**
 * check all real time statements
 * 
 * <ul>
 * <li> task-dcl + operations
 * <li> semaphore operations 
 * <li> bolt operations 
 * <li> interrupt operation
 * </ul>
 * 
 * 
 * @author mueller
 * 
 * Attention:
 *  visitName() sets the attributes 'm_type'.
 *  This is the reason why visitChildren(..) may only be used, if only 1 'name' is 
 *  possible in the current context. If more than 1 'name' is possible like in
 *    WHEN name§ofInterrupt ACTIVATE name§task;
 *  we must iterate this manually
 */
public class CheckRealTimeStatements extends OpenPearlBaseVisitor<Void>
        implements OpenPearlVisitor<Void> {

    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private AST m_ast = null;
    

    public CheckRealTimeStatements(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_debug = debug;
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        Log.debug("    Check Template");
    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check RT-statements: visitModule");
        }

        org.openpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(OpenPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check RT-statements: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }


    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check RT-statements: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check RT-statements: visitLoopStatement");
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
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check RT-statements: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();


        return null;
    }


    @Override
    public Void visitAssignment_statement(OpenPearlParser.Assignment_statementContext ctx) {
        // do not check lhs
        visitChildren(ctx.expression());
        return null;
    }
    

    private Void checkListOfNames(OpenPearlParser.ListOfNamesContext ctx,
            TypeDefinition expectedType) {
        for (int i = 0; i < ctx.name().size(); i++) {
                checkName(ctx.name(i), expectedType);
        }

        return null;
    }

    @Override
    public Void visitSemaTry(OpenPearlParser.SemaTryContext ctx) {
        Log.debug("CheckRealtimeStatements::visitSemaphoreTry " + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx, "TRY");
        checkListOfNames(ctx.listOfNames(), new TypeSemaphore());

        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitSemaRequest(OpenPearlParser.SemaRequestContext ctx) {
        Log.debug("CheckRealtimeStatements:visitSemaphoreRequest " + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx, "REQUEST");
        checkListOfNames(ctx.listOfNames(), new TypeSemaphore());
        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitSemaRelease(OpenPearlParser.SemaReleaseContext ctx) {
        Log.debug("CheckRealtimeStatements:visitSemaphoreRelease " + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx, "RELEASE");
        checkListOfNames(ctx.listOfNames(), new TypeSemaphore());
        ErrorStack.leave();

        return null;
    }


    @Override
    public Void visitBoltReserve(OpenPearlParser.BoltReserveContext ctx) {
        Log.debug("CheckRealtimeStatements:visitBoltReserve " + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx, "RESERVE");
        checkListOfNames(ctx.listOfNames(), new TypeBolt());
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitBoltFree(OpenPearlParser.BoltFreeContext ctx) {
        Log.debug("CheckRealtimeStatements:visitBoltFree " + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx, "FREE");
        checkListOfNames(ctx.listOfNames(), new TypeBolt());
        ErrorStack.leave();

        return null;
    }

    @Override
    public Void visitBoltEnter(OpenPearlParser.BoltEnterContext ctx) {
        Log.debug("CheckRealtimeStatements:visitBoltEnter " + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx, "ENTER");
        checkListOfNames(ctx.listOfNames(), new TypeBolt());
        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitBoltLeave(OpenPearlParser.BoltLeaveContext ctx) {
        Log.debug("CheckRealtimeStatements:visitBoltLeave " + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx, "LEAVE");
        checkListOfNames(ctx.listOfNames(), new TypeBolt());
        ErrorStack.leave();
        return null;
    }


    /**
    taskStart: startCondition? frequency? 'ACTIVATE' ID  priority? ';'   ;
    
    ID must be of type task
    types in startCondition and frequency must fit
    priority must be of correct type and range
     */
    @Override
    public Void visitTaskStart(OpenPearlParser.TaskStartContext ctx) {
        ErrorStack.enter(ctx, "ACTIVATE");


        checkName(ctx.name(), new TypeTask());


        visitStartCondition(ctx.startCondition());


        if (ctx.frequency() != null) {
            OpenPearlParser.FrequencyContext c = ctx.frequency();
            checkDurationValue(c.expression(0), "ALL");


            for (int i = 0; i < c.getChildCount(); i++) {
                if (c.getChild(i) instanceof TerminalNodeImpl) {
                    if (((TerminalNodeImpl) c.getChild(i)).getSymbol().getText().equals("ALL")) {
                        // ALL is mandatory!
                    } else if (((TerminalNodeImpl) c.getChild(i)).getSymbol().getText()
                            .equals("UNTIL")) {
                        checkClockValue(c.expression(1), "UNTIL");
                    } else if (((TerminalNodeImpl) c.getChild(i)).getSymbol().getText()
                            .equals("DURING")) {
                        checkDurationValue(c.expression(1), "DURING");
                    } else {
                        ErrorStack.addInternal("untreated alternative: "
                                + ((TerminalNodeImpl) c.getChild(i)).getSymbol().getText());
                    }
                }
            }
        }
        visitPriority(ctx.priority());

        ErrorStack.leave();
        return null;
    }

    /**
    task_terminating: 'TERMINATE' name? ';' ;
     */
    @Override
    public Void visitTask_terminating(OpenPearlParser.Task_terminatingContext ctx) {
        ErrorStack.enter(ctx, "TERMINATE");
        if (ctx.name() != null) {
       
           //visitName(ctx.name());
            checkName(ctx.name(), new TypeTask());
        }
        ErrorStack.leave();

        return null;
    }

    /* 
    task_suspending : 'SUSPEND' ID? ';' ;
     */
    @Override
    public Void visitTask_suspending(OpenPearlParser.Task_suspendingContext ctx) {
        ErrorStack.enter(ctx, "SUSPEND");
       
        if (ctx.name() != null) {
            checkName(ctx.name(), new TypeTask());
        }
        ErrorStack.leave();

        return null;
    }

    /* 
    task_preventing: 'PREVENT' ID? ';' ;
     */
    @Override
    public Void visitTask_preventing(OpenPearlParser.Task_preventingContext ctx) {
        ErrorStack.enter(ctx, "PREVENT");
        if (ctx.name() != null) {
            checkName(ctx.name(), new TypeTask());
        }
        ErrorStack.leave();

        return null;
    }

    /* 
    taskResume : startCondition 'RESUME' ';'     ;
     */
    @Override
    public Void visitTaskResume(OpenPearlParser.TaskResumeContext ctx) {
        ErrorStack.enter(ctx, "RESUME");
        visitChildren(ctx);
        ErrorStack.leave();
        return null;
    }

    /* 
    taskContinuation : startCondition? 'CONTINUE' ID? priority? ';' ;
     */
    @Override
    public Void visitTaskContinuation(OpenPearlParser.TaskContinuationContext ctx) {
        ErrorStack.enter(ctx, "CONTINUE");

        if (ctx.name() != null) {
            checkName(ctx.name(), new TypeTask());
        }

        visitStartCondition(ctx.startCondition());
        visitPriority(ctx.priority());
        
        ErrorStack.leave();
        return null;
    }


    @Override
    public Void visitPriority(OpenPearlParser.PriorityContext ctx) {
        if (ctx != null) {
            ErrorStack.enter(ctx, "PRIO");
            checkPriority(ctx.expression());
            ErrorStack.leave();
        }
        return null;
    }
    

    @Override
    public Void visitStartCondition(OpenPearlParser.StartConditionContext ctx) {
        if (ctx != null) {
            visitChildren(ctx);
        }
        return null;
    }



    @Override
    public Void visitStartConditionAT(OpenPearlParser.StartConditionATContext ctx) {
        checkClockValue(ctx.expression(), "AT");

        return null;
    }


    @Override
    public Void visitStartConditionAFTER(OpenPearlParser.StartConditionAFTERContext ctx) {
        checkDurationValue(ctx.expression(), "AFTER");

        return null;
    }

    @Override
    public Void visitStartConditionWHEN(OpenPearlParser.StartConditionWHENContext ctx) {
        checkInterrupt(ctx.name(), "WHEN");
        return null;
    }


    /*
    disableStatement : 'DISABLE' ID ';'     ;
     */
    @Override
    public Void visitDisableStatement(OpenPearlParser.DisableStatementContext ctx) {
        checkInterrupt(ctx.name(), "DISABLE");
        return null;
    }


    /*
    enableStatement : 'ENABLE' ID ';'     ;
     */
    @Override
    public Void visitEnableStatement(OpenPearlParser.EnableStatementContext ctx) {
        checkInterrupt(ctx.name(), "ENABLE");
        return null;
    }

    /*
    triggerStatement : 'TRIGGER' ID ';'     ;
     */
    @Override
    public Void visitTriggerStatement(OpenPearlParser.TriggerStatementContext ctx) {
        checkInterrupt(ctx.name(), "TRIGGER");
        return null;
    }


    private void checkPriority(OpenPearlParser.ExpressionContext ctx) {
        ErrorStack.enter(ctx);
        
        ASTAttribute attr = m_ast.lookup(ctx);
        TypeDefinition t = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr);
        //TypeDefinition t = getEffectiveType(ctx);

        if (t instanceof TypeFixed) {
            if (attr.isConstant()) {
                long p = attr.getConstantFixedValue().getValue();
                if (p < Defaults.BEST_PRIORITY || p > Defaults.LOWEST_PRIORITY) {
                    ErrorStack.add("must be in [" + Defaults.BEST_PRIORITY + ","
                            + Defaults.LOWEST_PRIORITY + "]");
                }
            }
        } else {
            ErrorStack.add("must be of type FIXED");
        }
        ErrorStack.leave();

    }

    private void checkClockValue(OpenPearlParser.ExpressionContext ctx, String prefix) {
        ASTAttribute attr = m_ast.lookup(ctx);
        TypeClock clk = new TypeClock();
        TypeDefinition t = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr); // getEffectiveType(ctx);

        if (!t.equals(clk)) {
            t = TypeUtilities.performImplicitDereferenceAndFunctioncallForTargetType(attr,clk); 

            if (t == null || !t.equals(clk)) {
                ErrorStack.add(ctx, null, "expected type '" + clk.toString() + "' -- got '"
                        + attr.getType().toString4IMC(false) + "'");
            }
        }

    }

    private void checkDurationValue(OpenPearlParser.ExpressionContext ctx, String prefix) {
        ASTAttribute attr = m_ast.lookup(ctx);
        TypeDuration dur = new TypeDuration();
        TypeDefinition t = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr); // getEffectiveType(ctx);

        if (!t.equals(dur)) {
            t = TypeUtilities.performImplicitDereferenceAndFunctioncallForTargetType(attr,dur); 

            if (t == null || !t.equals(dur)) {
                ErrorStack.add(ctx, null, "expected type '" + dur.toString() + "' -- got '"
                        + attr.getType().toString4IMC(false) + "'");
            }
        }
    }

    private void checkInterrupt(OpenPearlParser.NameContext ctx, String prefix) {
        ErrorStack.enter(ctx, prefix);
        
        checkName(ctx, new TypeInterrupt());
        ErrorStack.leave();
    }

    private void checkName(OpenPearlParser.NameContext ctx, TypeDefinition expectedType) {
        ASTAttribute attr = m_ast.lookup(ctx);

        TypeDefinition t = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr); // getEffectiveType(ctx);

        if (!t.equals(expectedType)) {
            t = TypeUtilities.performImplicitDereferenceAndFunctioncallForTargetType(attr,expectedType); 

            if (t == null || !t.equals(expectedType)) {
                ErrorStack.add(ctx, null, "expected type '" + expectedType.toString() + "' -- got '"
                        + attr.getType().toString4IMC(false) + "'");
            }
        }
    }

//    private TypeDefinition getEffectiveType(ParserRuleContext ctx) {
//
//        ASTAttribute attr = m_ast.lookup(ctx);
//        TypeDefinition t = attr.getType();
//        if (t instanceof TypeReference) {
//            t = ((TypeReference)t).getBaseType();
//            attr.setNeedImplicitDereferencing(true);
//        }
//
//        return t;
//    }

}
