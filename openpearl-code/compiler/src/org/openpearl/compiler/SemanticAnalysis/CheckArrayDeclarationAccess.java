package org.openpearl.compiler.SemanticAnalysis;


import java.util.List;

import org.openpearl.compiler.*;
import org.openpearl.compiler.Exception.*;
import org.openpearl.compiler.SymbolTable.FormalParameter;
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;

/**
 * check for array boundaries: lwb &le; upb
 * check indices
 *   - type to be FIXED with precision &le; 31
 *   - number as required by the array denotation
 *   - values in range if value is constant and array is no formal parameter 
 *   
 *   
 * @author mueller
 *
 */
public class CheckArrayDeclarationAccess extends OpenPearlBaseVisitor<Void>
        implements OpenPearlVisitor<Void> {

    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private AST m_ast = null;

    public CheckArrayDeclarationAccess(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_debug = debug;
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        Log.debug("    Check Array Declaration and Access");
    }

    @Override
    public Void visitModule(OpenPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Array Declaration and Access: visitModule");
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
            System.out.println(
                    "Semantic: Check Array Declaration and Access: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitTaskDeclaration(OpenPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out
                    .println("Semantic: Check Array Declaration and Access: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(OpenPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out
                    .println("Semantic: Check Array Declaration and Access: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(OpenPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Array Declaration and Access: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }
    
    @Override
    public Void  visitUpbMonadicExpression(OpenPearlParser.UpbMonadicExpressionContext ctx) {
        ASTAttribute attr1 = m_ast.lookup(ctx.expression());
                

        if (attr1.getType() instanceof TypeArray) {
            

        } else {
            ErrorStack.add(ctx,"UPB","must be applied on ARRAY");
        }
        return null;
    }
    
    @Override
    public Void  visitUpbDyadicExpression(OpenPearlParser.UpbDyadicExpressionContext ctx) {
        ASTAttribute attr1 = m_ast.lookup(ctx.expression(1));
        ASTAttribute attr0 = m_ast.lookup(ctx.expression(0));
        
        
        if (attr1.getType() instanceof TypeArray) {
            if (!(attr0.getType() instanceof TypeFixed)) {
                ErrorStack.add(ctx,"UPB", 
                        "type mismatch: array index must be of type FIXED -- got "+attr0.getType());
            } else {
                long index = attr0.getConstantFixedValue().getValue();
                int dim = ((TypeArray)(attr1.getType())).getNoOfDimensions();
                if (index < 1 || index > dim) {
                   ErrorStack.add(ctx,"UPB","array index out of range [1,"+dim+"]"); 
                }
            }
        } else {
            ErrorStack.add(ctx,"UPB","must be applied on ARRAY");
        }
        return null;
    }
    
    @Override
    public Void  visitLwbMonadicExpression(OpenPearlParser.LwbMonadicExpressionContext ctx) {
        ASTAttribute attr1 = m_ast.lookup(ctx.expression());
                
        
        if (attr1.getType() instanceof TypeArray) {
            
        } else {
            ErrorStack.add(ctx,"LWB","must be applied on ARRAY");
        }
        return null;
    }
    
    @Override
    public Void  visitLwbDyadicExpression(OpenPearlParser.LwbDyadicExpressionContext ctx) {
        ASTAttribute attr1 = m_ast.lookup(ctx.expression(1));
        ASTAttribute attr0 = m_ast.lookup(ctx.expression(0));
        
 
        if (attr1.getType() instanceof TypeArray) {
            if (!(attr0.getType() instanceof TypeFixed)) {
                ErrorStack.add(ctx,"LWB", 
                        "type mismatch: array index must be of type FIXED -- got "+attr0.getType());
            } else {
                long index = attr0.getConstantFixedValue().getValue();
                int dim = ((TypeArray)(attr1.getType())).getNoOfDimensions();
                if (index < 1 || index > dim) {
                   ErrorStack.add(ctx,"LWB","array index out of range [1,"+dim+"]"); 
                }
            }
        } else {
            ErrorStack.add(ctx,"LWB","must be applied on ARRAY");
        }
        return null;
    }



    @Override
    public Void visitPrimaryExpression(OpenPearlParser.PrimaryExpressionContext ctx) {
        Log.debug(
                "ExpressionTypeVisitor:visitPrimaryExpression:ctx" + CommonUtils.printContext(ctx));

        ErrorStack.enter(ctx, "array index");
        if (ctx.name() != null && ctx.name().ID() != null) {
            SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.name().ID().getText());

            //		String s = ctx.toStringTree();
            //		System.out.println(ctx.ID().toString());
            if (entry == null) {
                throw new UnknownIdentifierException(ctx.getText(), ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
            }

            if (entry instanceof VariableEntry) {

                VariableEntry variable = (VariableEntry) entry;

                if (variable.getType() instanceof TypeArrayDeclaration) {
                    Boolean checkIndexValue = true;
                    if (variable instanceof FormalParameter) {
                        checkIndexValue = false;
                    }
                    // expressionResult should be TypeArray if no indices are given
                    if (ctx.name().listOfExpression() != null
                            && ctx.name().listOfExpression().expression().size() > 0) {
                        TypeArrayDeclaration ta = (TypeArrayDeclaration) variable.getType();
                        checkIndices(ta, ctx.name().listOfExpression().expression(),
                                checkIndexValue);

                    } else {
                        // we got no indices -_> complete array as parameter
                    }
                }

            }
        }


        ErrorStack.leave();


        return null;
    }


    @Override
    public Void visitAssignment_statement(OpenPearlParser.Assignment_statementContext ctx) {
        Log.debug("CheckAssignment:visitAssignment_statement:ctx" + CommonUtils.printContext(ctx));

        String id = null;
        ErrorStack.enter(ctx, "array index");

        //		if ( ctx.stringSelection() != null ) {
        //			if ( ctx.stringSelection().charSelection() != null ) {
        //				id = ctx.stringSelection().charSelection().name().getText();
        //			}
        //			else  if (ctx.stringSelection().bitSelection() != null) {
        //				id = ctx.stringSelection().bitSelection().name().getText();
        //			} else {
        //				throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        //			}
        //		}
        //		else {
        //			id = ctx.name().getText();
        //		}
        id = ctx.name().ID().toString();


        SymbolTableEntry lhs = m_currentSymbolTable.lookup(id);
        if (lhs instanceof VariableEntry) {
            VariableEntry variable = (VariableEntry) lhs;
            if (variable.getType() instanceof TypeArrayDeclaration) {
                Boolean checkIndexValue = true;
                if (variable instanceof FormalParameter) {
                    checkIndexValue = false;
                }
                TypeArrayDeclaration ta = (TypeArrayDeclaration) (variable.getType());

                if (ctx.name().listOfExpression() != null
                        && ctx.name().listOfExpression().expression().size() > 0) {
                    checkIndices(ta, ctx.name().listOfExpression().expression(), checkIndexValue);
                }
            }
        }
        ErrorStack.leave();
        visitChildren(ctx);
        return null;
    }

    private void checkIndices(TypeArrayDeclaration ta, List<OpenPearlParser.ExpressionContext> expression,
            Boolean checkIndexValue) {
        int arrayDimensions = ta.getNoOfDimensions();


        if (arrayDimensions != expression.size()) {
            ErrorStack.add("wrong number of indices: expected " + arrayDimensions + " -- got "
                    + expression.size());
        }
        for (int d = 0; d < ta.getNoOfDimensions(); d++) {
            ErrorStack.enter(expression.get(d));

            if (m_ast.lookupType(expression.get(d)) instanceof TypeFixed) {
                TypeFixed tf = (TypeFixed)(m_ast.lookupType(expression.get(d)));
                int prec = tf.getPrecision().intValue();
                if (prec > 31) {
                    ErrorStack.add("type too large "+tf+" > FIXED(31)");
                }
                if (checkIndexValue) {

                    ASTAttribute attr = m_ast.lookup(expression.get(d));
                    if (attr.isConstant()) {
                        if (attr.getConstantFixedValue() != null) {
                            long val = attr.getConstantFixedValue().getValue();
                            if (val < ta.getDimensions().get(d).getLowerBoundary()
                                    || val > ta.getDimensions().get(d).getUpperBoundary()) {
                                ErrorStack.add("index out of range ["
                                        + ta.getDimensions().get(d).getLowerBoundary() + ":"
                                        + ta.getDimensions().get(d).getUpperBoundary() + "]");
                            }

                        }
                    }
                }

            } else {
                ErrorStack.add("index must be of type FIXED" + " -- got "
                        + m_ast.lookupType(expression.get(d)).toString());
            }
            ErrorStack.leave();
        }

    }

}
