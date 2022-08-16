/*
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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.openpearl.compiler.Exception.*;
import org.openpearl.compiler.SymbolTable.*;
import org.openpearl.compiler.OpenPearlParser.*;

// import com.sun.org.apache.xpath.internal.operations.Bool;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcel Schaible
 * @version 1
 *     <p><b>Description</b> Various visitors must run, after the parser has generated the AST, in
 *     the following predefined order:
 *     <p><img width=200 src="{@docRoot}/OpenPEARL-Visitors.png" alt="alternative directory">
 *     <p><b>Overview</b>
 *     <ul>
 *       <li>SymbolTableVisitor: Collects Module, Variable, Task and Procedure Declarations as well as TYPE definitions
 *       <li>{@link org.openpearl.compiler.ExpressionTypeVisitor}: Determines for all expressions the resulting type
 *       <li>{@link org.openpearl.compiler.ConstantFoldingVisitor}: Evaluates constant expressions
 *       <li>{@link org.openpearl.compiler.ConstantPoolVisitor}: Collects all constants and adds them into the constant pool
 *       <li>{@link org.openpearl.compiler.ConstantExpressionEvaluatorVisitor}: Calculates constant fixed expressions and adds them
 *           to the constant pool
 *       <li>{@link org.openpearl.compiler.FixUpSymbolTableVisitor}: Determines the types and precision of loop indices and patches
 *           the related symboltable entries
 *     </ul>
 *     <p>The SymbolTableVisitor (STV) main purpose is to collect declarations of modules, global
 *     and local variables, loop indices, interrupts, tasks and procedures. STV follows the block
 *     nesting and generates for every block a separate symboltable (ST), which is linked to the
 *     enclosing block. STV creates ST entries for each entity found and adds the belonging
 *     attributes like e.g. <i>INV</i> or <i>GLOBAL</i> to the ST entries.
 *     <p>STV checks for duplicate declarations on the same block level and block identifiers.
 *     <p>If a variable denotations contains an initializer, STV checks the compatibility of the
 *     types and stores the init values in a respective Initializer.
 *     <p>STV relies on the following checks:
 *     <ol>
 *       <li>{@link org.openpearl.compiler.SemanticAnalysis.CheckDeclarationScope}
 *       <li>{@link org.openpearl.compiler.SemanticAnalysis.CheckVariableDeclaration} for fitting initialisers and presets
 *     </ol>
 *     
 *     Treatment of TYPE definitions:
 *     <ol>
 *     <li>redefinition of simpleType or structure without self reference is just an alias
 *     <li>redefinition of a structure with reference to itself  uses the attribute m_currentTypeStructureDefinition
 *     </ol>
 */
public class SymbolTableVisitor extends OpenPearlBaseVisitor<Void>
implements OpenPearlVisitor<Void> {

    private final int m_verbose;
    private final boolean m_debug;

    public SymbolTable symbolTable;
    private SymbolTable m_currentSymbolTable;
    private final LinkedList<ArrayDescriptor> m_listOfArrayDescriptors;
    private TypeDefinition m_type;
    private ParseTreeProperty<SymbolTable> m_symboltablePerContext = null;
    private ConstantPool m_constantPool = null;

    private TypeStructure m_typeStructure = null;
    private boolean m_hasAllocationProtection = false;
    private boolean m_isGlobal = false;
    private String m_globalName;
    private String m_currentModuleName;
    private boolean m_isInSpecification = false;
    private IdentifierDenotationContext m_identifierDenotationContext;
    private LinkedList<FormalParameter> m_formalParameters;
    private UserDefinedType m_currentUserdefinedType = null;

    public SymbolTableVisitor(int verbose, ConstantPool constantPool) {
        m_debug = false;
        m_verbose = verbose;

        Log.debug("SymbolTableVisitor:Building new symbol table");

        this.symbolTable = new org.openpearl.compiler.SymbolTable.SymbolTable();
        new LinkedList<LinkedList<SemaphoreEntry>>();
        new LinkedList<LinkedList<BoltEntry>>();
        this.m_listOfArrayDescriptors = new LinkedList<ArrayDescriptor>();
        this.m_symboltablePerContext = new ParseTreeProperty<SymbolTable>();
        this.m_constantPool = constantPool;
        // TODO: MS REMOVE?:        this.m_currentStructureEntry = null;
        this.m_typeStructure = null;
        m_formalParameters=null;
    }

    @Override
    public Void visitModule(ModuleContext ctx) {
        Log.debug("SymbolTableVisitor:visitModule:ctx" + CommonUtils.printContext(ctx));

        org.openpearl.compiler.SymbolTable.ModuleEntry moduleEntry =
                new org.openpearl.compiler.SymbolTable.ModuleEntry(
                        ctx.nameOfModuleTaskProc().ID().getText(), ctx, null);
        this.m_currentSymbolTable = this.symbolTable.newLevel(moduleEntry);
        this.m_symboltablePerContext.put(ctx, this.m_currentSymbolTable);
        m_currentModuleName = ctx.nameOfModuleTaskProc().ID().getText();
        visitChildren(ctx);

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitSystemElementDefinition(SystemElementDefinitionContext ctx) {
        Log.debug(
                "SymbolTableVisitor:visitSystemElementDefinition:ctx"
                        + CommonUtils.printContext(ctx));

        if (ctx != null) {
            String name = ctx.systemPartName().ID().toString();
            SymbolTableEntry se = m_currentSymbolTable.lookupSystemPartName(name);

            if (se != null) {
                if (se.isSystemName()) {
                    ErrorStack.enter(ctx);
                    ErrorStack.add("'" + name + "' already used as system name");
                    ErrorStack.note(se.getCtx(), "previous usage was here", "");
                    ErrorStack.leave();
                } else if (se.isUserName()) {
                    ErrorStack.enter(ctx);
                    ErrorStack.add("'" + name + "' already defined");
                    ErrorStack.note(se.getCtx(), "previous definition was here", "");
                    ErrorStack.leave();
                } else {
                    se.setIsUsername(true);
                }
            }
            if (se == null) {
                SystemPartName s = new SystemPartName(name, ctx);
                s.setIsUsername(true);
                m_currentSymbolTable.enterSystemPartName(s);
            }
            // we must ensure that the SystemName is not
            // used as Username
            //          String sysName = ctx.systemDescription().systemPartName().ID().toString();
            //
            //          SymbolTableEntry entry1 =
            // m_currentSymbolTable.lookupSystemPartName(sysName);
            //          if (entry1 == null) {
            //            s = new SystemPartName(name,ctx.systemDescription().systemPartName());
            //            m_currentSymbolTable.enterSystemPartName(s);
            //          } else {
            //            s.setIsSystemName(true);
            //          }

            visit(ctx.systemDescription());
        }
        return null;
    }

    @Override
    public Void visitSystemDescription(SystemDescriptionContext ctx) {
        Log.debug(
                "SymbolTableVisitor:visitSystemElementDefinition:ctx"
                        + CommonUtils.printContext(ctx));

        if (ctx != null) {
            String name = ctx.systemPartName().ID().toString();
            // this name must be a name of a system element (e.g.StdOut)
            SymbolTableEntry se = m_currentSymbolTable.lookupSystemPartName(name);
            if (se != null && !se.isSystemName()) {
                ErrorStack.enter(ctx);
                ErrorStack.add("user name '" + name + "' not allowed");
                ErrorStack.note(se.getCtx(), "previous definition was here", "");
                ErrorStack.leave();
            }
            if (se == null) {
                SystemPartName s = new SystemPartName(name, ctx);
                s.setIsSystemName(true);
                m_currentSymbolTable.enterSystemPartName(s);
            }

            // lets treat the associations
            for (int i = 0; i < ctx.association().size(); i++) {
                AssociationContext a = ctx.association(i);
                name = a.systemPartName().ID().getText();
                se = m_currentSymbolTable.lookupSystemPartName(name);
                if (se == null) {
                    se = new SystemPartName(name, a);
                    m_currentSymbolTable.enterSystemPartName(se);
                }
            }
        }
        return null;
    }

    @Override
    public Void visitTypeDefinition(TypeDefinitionContext ctx) {
        Log.debug(
                "SymbolTableVisitor:visitTypeDefinition:ctx"
                        + CommonUtils.printContext(ctx));
        m_currentUserdefinedType = new UserDefinedType(ctx.identifier());
        m_isInSpecification = false;
        m_isGlobal = false;
        
        checkDoubleDefinitionAndEnterToSymbolTable(m_currentUserdefinedType, ctx.identifier());

        if (ctx.simpleType() != null) {
            visit(ctx.simpleType());
            UserDefinedSimpleType udst = new UserDefinedSimpleType(ctx.identifier().ID().toString(),m_type);
            m_currentUserdefinedType.setType(udst);
    
        } else if (ctx.typeStructure()!= null) {
            UserDefinedTypeStructure uts = new UserDefinedTypeStructure(ctx.identifier().ID().toString());
            
            m_currentUserdefinedType.setType(uts);
            visit(ctx.typeStructure());
            uts.setStructuredType(m_type);
        }
        
        m_currentUserdefinedType = null;

        return null;
    }
    
    @Override
    public Void visitIdentifierForType(IdentifierForTypeContext ctx) {
        String name = ctx.ID().toString();
        ErrorStack.enter(ctx);
        m_type = null;
        SymbolTableEntry se = m_currentSymbolTable.lookup(name);
        if (se != null) {
            if (se instanceof UserDefinedType) {
                if (m_currentUserdefinedType != null && 
                        name.equals(m_currentUserdefinedType.getName())) {
                    m_type = new TypeSameStructure((UserDefinedTypeStructure)(m_currentUserdefinedType).getType());
                } else {
                   m_type = ((UserDefinedType)se).getType();
                }
            } else {
                ErrorStack.add(name + " should be user defined type ");
                ErrorStack.note(se.getCtx(),"previous definition of",name);
            }
        } else {
            ErrorStack.add( name + " is no name of a type or an user defined type");
        }
        ErrorStack.leave();
        return null;
    }
    
    @Override
    public Void visitVariableDeclaration(VariableDeclarationContext ctx) {
        m_isInSpecification = false;
        visitChildren(ctx);

        return null;
    }

    @Override
    public Void visitSpecification(SpecificationContext ctx) {
        m_isInSpecification = true;
        visitChildren(ctx);
        m_isInSpecification = false;
        return null;
    }

    @Override
    public Void visitTaskDeclaration(TaskDeclarationContext ctx) {
        Boolean isMain = false;
        PriorityContext priority = null;

        Log.debug("SymbolTableVisitor:visitTaskDeclaration:ctx" + CommonUtils.printContext(ctx));

        String s = ctx.nameOfModuleTaskProc().ID().toString();

        isMain = ctx.task_main() != null;
        if (ctx.priority() != null) {
            priority = ctx.priority();
        }
        
        visitGlobalAttribute(ctx.globalAttribute());
        
        m_isInSpecification = false;

        
        TaskEntry taskEntry =
                new TaskEntry(
                        ctx.nameOfModuleTaskProc().ID().getText(),
                        priority,
                        isMain,
                        ctx,
                        this.m_currentSymbolTable);
        checkDoubleDefinitionAndEnterToSymbolTable(taskEntry, ctx);
        m_type = null; // invalidate m_type to avoid side effects with SIZOF <type>
          
            
        this.m_currentSymbolTable = this.m_currentSymbolTable.newLevel(taskEntry);
        this.m_symboltablePerContext.put(ctx, this.m_currentSymbolTable);

        visitChildren(ctx);

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(ProcedureDeclarationContext ctx) {
        String globalId = null;
        LinkedList<FormalParameter> formalParameters = null;
        ASTAttribute resultType = null;
        m_isGlobal = false;

        Log.debug(
                "SymbolTableVisitor:visitProcedureDeclaration:ctx" + CommonUtils.printContext(ctx));
        
        if (ctx.globalAttribute() != null) {
           visit(ctx.globalAttribute());   
        }

        TypeProcedureContext tpc = ctx.typeProcedure();
        if (tpc.listOfFormalParameters()!= null) {
            formalParameters =
                    getListOfFormalParameters(tpc.listOfFormalParameters());
        }
        if (tpc.resultAttribute() != null) {
            resultType =
                    new ASTAttribute(
                            getResultAttribute(tpc.resultAttribute())); 
        }
       
        
        // create type of the procedure
        if (resultType != null) {
            m_type = new TypeProcedure(formalParameters, resultType.getType());
        } else {
            m_type = new TypeProcedure(formalParameters, null);
        }
        //
        //        if (ctx.globalAttribute() != null) {
        //            globalId = ctx.globalAttribute().ID().getText();
        //        }
        
        ProcedureEntry procedureEntry =
                new ProcedureEntry(
                        ctx.nameOfModuleTaskProc().getText(),m_type,
                        formalParameters,
                        resultType,
                        globalId,
                        ctx,
                        this.m_currentSymbolTable);
        if (m_isGlobal) {
            procedureEntry.setGlobalAttribute(m_currentModuleName);
        }
        
        String s = ctx.nameOfModuleTaskProc().ID().toString();
        SymbolTableEntry entry = this.m_currentSymbolTable.lookupLocal(s);
        if (entry != null) {
            boolean ok = differsOnlyInSpcDcl(procedureEntry, entry);
            if (!ok) {
               CommonErrorMessages.doubleDeclarationError(
                    s, ctx.nameOfModuleTaskProc(), entry.getCtx());
            }
        } else {
            entry = this.m_currentSymbolTable.lookup(s);
            if (entry != null) {
                CommonErrorMessages.doubleDeclarationWarning(
                        "procedure definition ", s, ctx.nameOfModuleTaskProc(), entry.getCtx());
            }
        }
        


        this.m_currentSymbolTable = this.m_currentSymbolTable.newLevel(procedureEntry);

        /* Enter formal parameter into the local symbol table of this procedure */
        if (formalParameters != null && formalParameters.size() > 0) {
            for (FormalParameter formalParameter : formalParameters) {
                // VariableEntry param = new VariableEntry(formalParameter.name,
                // formalParameter.type,
                // formalParameter.assignmentProtection, formalParameter.m_ctx, null);
                // this.m_currentSymbolTable.enter(param);

                entry = this.m_currentSymbolTable.lookup(formalParameter.getName());
                if (entry != null) {
                    if (entry instanceof FormalParameter) {

                        CommonErrorMessages.doubleDeclarationError(
                                formalParameter, entry);
                        //"formal parameter ",
                        //                            formalParameter.getName(),
                        //                            formalParameter.getCtx(),
                        //                            entry.getCtx());
                    } else {
                        CommonErrorMessages.doubleDeclarationWarning("formal parameter ",
                                formalParameter.getName(),
                                formalParameter.getCtx(),
                                entry.getCtx());
                    }
                }
                this.m_currentSymbolTable.enter(formalParameter);
            }
        }

        this.m_symboltablePerContext.put(ctx, this.m_currentSymbolTable);

        visitChildren(ctx);

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    private TypeDefinition getResultAttribute(ResultAttributeContext ctx) {
        Log.debug("SymbolTableVisitor:getResultAttribute:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx.resultType());
        return m_type;
    }

    private LinkedList<FormalParameter> getListOfFormalParameters(
            ListOfFormalParametersContext ctx) {
        boolean warningIssued=false;
        LinkedList<FormalParameter> listOfFormalParameters = new LinkedList<FormalParameter>();

        Log.debug(
                "SymbolTableVisitor:getListOfFormalParameters:ctx" + CommonUtils.printContext(ctx));

        if (ctx != null) {
            for (int i = 0; i < ctx.formalParameter().size(); i++) {
                if (m_isInSpecification && 
                        ctx.formalParameter(i).getChild(0) instanceof TerminalNode &&
                        Compiler.isStdPEARL90() == true) {
                  if (!warningIssued) {
                      ErrorStack.warn(ctx.formalParameter(i),"SPC PROC","no list of identifiers allowed");
                      warningIssued=true;   // report warning only once
                  }
                  
                }
               
                if ((!m_isInSpecification) && ctx.formalParameter(i).identifier().size()== 0) {
                    ErrorStack.add(ctx.formalParameter(i),"PROC declaration","identifier(s) required");
                }
                getFormalParameter(listOfFormalParameters, ctx.formalParameter(i));
            }
        }

        return listOfFormalParameters;
    }

    private Void getFormalParameter(
            LinkedList<FormalParameter> listOfFormalParameters,
            FormalParameterContext ctx) {
        Log.debug("SymbolTableVisitor:getFormalParameter:ctx" + CommonUtils.printContext(ctx));

        if (ctx != null) {
            int nbrOfRepetitions = 1;
            Object o = ctx.identifier();
            if (ctx.identifier().size() > 0) {
                nbrOfRepetitions = ctx.identifier().size();
            }
            for (int i = 0; i < nbrOfRepetitions; i++) {
                int nbrDimensions = 0; // default to scalar value
                String name = "";
                Boolean assignmentProtection = false;
                Boolean passIdentical = false;

                if (ctx.identifier(i) != null) {
                    name = ctx.identifier(i).ID().getText();
                }

                if (ctx.virtualDimensionList() != null) {

                    // get the number of array dimensions
                    // we count the ',' symbols and add 1,since 0 ',' is dimension 1
                    nbrDimensions = 1;
                    if (ctx.virtualDimensionList().commas() != null) {
                        nbrDimensions += ctx.virtualDimensionList().commas().getChildCount();
                    }
                }

                if (ctx.assignmentProtection() != null) {
                    assignmentProtection = true;
                }

                if (ctx.passIdentical() != null) {
                    passIdentical = true;
                }

                getParameterType(ctx.parameterType());

                
                if (nbrDimensions > 0) {
                    TypeArray array = new TypeArray();
                    array.setBaseType(m_type);
                    for (i = 0; i < nbrDimensions; i++) {
                        // the real dimensions limits are passed via array descriptor
                        array.addDimension(new ArrayDimension());
                    }
                    m_type = array;
                }
                m_type.setHasAssignmentProtection(assignmentProtection);
                
                listOfFormalParameters.add(
                        new FormalParameter(
                                name, m_type, //assignmentProtection, 
                                passIdentical, ctx));
            }
        }

        return null;
    }

    private Void getParameterType(ParameterTypeContext ctx) {
        Log.debug("SymbolTableVisitor:getParameterType:ctx" + CommonUtils.printContext(ctx));

        visitChildren(ctx);

        return null;
    }

    @Override
    public Void visitBlock_statement(Block_statementContext ctx) {
        String blockLabel = null;

        Log.debug("SymbolTableVisitor:visitBlock_statement:ctx" + CommonUtils.printContext(ctx));

        if (ctx.blockId() != null) {
            blockLabel = ctx.blockId().ID().toString();

            SymbolTableEntry se = m_currentSymbolTable.lookup(blockLabel);
            if (se != null) {
                ErrorStack.add(
                        ctx.blockId(), "BLOCK", "duplicate name '" + blockLabel + "' in scope");
                if (se instanceof LoopEntry) {
                    ErrorStack.note(
                            ((LoopStatementContext) (se.getCtx())).loopStatement_end(),
                            "previous definion",
                            "");
                }
                if (se instanceof BlockEntry) {
                    ErrorStack.note(se.getCtx(), "superior definition", "");
                }
            }
        }
        BlockEntry blockEntry = new BlockEntry(blockLabel, ctx, m_currentSymbolTable);

        m_currentSymbolTable = m_currentSymbolTable.newLevel(blockEntry);
        this.m_symboltablePerContext.put(ctx, this.m_currentSymbolTable);

        visitChildren(ctx);

        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    /*
     variableDenotation :
    identifierDenotation dimensionAttribute?
        (problemPartDataAttribute | semaDenotation | boltDenotation | dationDenotation)
    ;
     */
    @Override
    public Void visitVariableDenotation(VariableDenotationContext ctx) {

        Log.debug("SymbolTableVisitor:visitVariableDenotation:ctx" + CommonUtils.printContext(ctx));
        // System.out.println(ctx.getText());
        m_type = null;
        m_hasAllocationProtection = false;
        m_identifierDenotationContext = ctx.identifierDenotation();

        if (ctx != null) {
            // check if all variables are of type array

            if (ctx.dimensionAttribute() != null) {
                // TypeDefinition baseType = m_type;
                if (ctx.dimensionAttribute().virtualDimensionList() != null) {
                    m_type = new TypeArraySpecification();
                } else {
                   m_type = new TypeArray();
                }
                //                ((TypeArray) m_type).setBaseType(baseType);
                visitDimensionAttribute(ctx.dimensionAttribute());
                ParserRuleContext c = ((TypeArray)m_type).getDimensions().get(0).getCtx();
                if (c != null && m_isInSpecification) {
                    ErrorStack.add(ctx.dimensionAttribute(), "SPC", "need virtual dimension list");
                }
                if (c == null && ! m_isInSpecification) {
                    ErrorStack.add(ctx.dimensionAttribute(), "DCL", "need real dimension list");
                }
                if ( c!= null) {
                    // do not add virtual dimension lists to the array descriptors
                   addArrayDescriptor(
                        new ArrayDescriptor(
                                ((TypeArray) m_type).getNoOfDimensions(),
                                ((TypeArray) m_type).getDimensions()));
                }
            }

            if (ctx.problemPartDataAttribute() != null) {
                visitProblemPartDataAttribute(ctx.problemPartDataAttribute());
                if (ctx.problemPartDataAttribute().initialisationAttribute() != null) {
                }

            } else if (ctx.semaDenotation() != null) {
                visitSemaDenotation(ctx.semaDenotation());
            } else if (ctx.boltDenotation() != null) {
                visitBoltDenotation(ctx.boltDenotation());
            } else if (ctx.dationDenotation() != null) {
                visitDationDenotation(ctx.dationDenotation());
            } else {
                ErrorStack.addInternal(
                        ctx, "SymbolTableVisitor:variableDenotation", "missing alternative@473");
            }
        }


        return null;
    }

    @Override
    public Void visitProblemPartDataAttribute(ProblemPartDataAttributeContext ctx) {
        List<InitElementContext> initElements = null;
       
        m_isGlobal = false;
        m_globalName = null;
        m_hasAllocationProtection = false;
        
        TypeDefinition safeType = m_type;

        if (ctx.initialisationAttribute() != null) {
            initElements = ctx.initialisationAttribute().initElement();
        }

        visitChildren(ctx);
        
        if (m_type == null) {
            // type could be be resolved
            return null;
        }
        
        m_type.setHasAssignmentProtection(m_hasAllocationProtection);
       

        if (safeType instanceof TypeArray) {
            ((TypeArray) safeType).setBaseType(m_type);
            m_type = safeType;
        }

        if (initElements != null && m_isInSpecification) {
            ErrorStack.add(ctx, "SPC", "no INIT allowed");
        }
        
        // treatment of initialisiers, eg
        // DCL (x,y) FIXED INIT(1,2),
        //     (arrA,arrb)(2) FIXED INIT(1,2,3),
        //     (a,b) STRUCT [(c,d) FIXED] INIT(1,2,3,4);
        // extract for each symbol the required number of initialisers
        // create ArrayOrStructureInitialiser or SimpleInitializer
        Initializer init = null;
        int nbrOfInitializer = 0;
        if (initElements != null) {

            nbrOfInitializer = initElements.size();
            int nbrOfVariables = m_identifierDenotationContext.identifier().size();

            // check number of required and given initializers
            if (m_type instanceof TypeArray) {
                // need ArrayOrStructureInitialiser
                int maxOfInitializers =
                        nbrOfVariables * ((TypeArray) (m_type)).getTotalNoOfElements();
                if (((TypeArray) m_type).getBaseType() instanceof TypeStructure) {
                    maxOfInitializers *=
                            ((TypeStructure) ((TypeArray) m_type).getBaseType())
                            .getTotalNoOfElements();
                    if (nbrOfInitializer< maxOfInitializers) {
                        ErrorStack.add(m_identifierDenotationContext,
                                "INIT",
                                "array of STRUCT "+ (maxOfInitializers-nbrOfInitializer) +" initialisers missing");
                    }
                }
                if (nbrOfInitializer > maxOfInitializers) {
                    ErrorStack.add(
                            initElements.get(maxOfInitializers), "INIT", "too many initializers");
                }
            } else if (m_type instanceof TypeStructure || m_type instanceof UserDefinedTypeStructure) {
                // need ArrayOrStructureInitialiser
                int requiredInitializers;
                if (m_type instanceof TypeStructure) {
                   requiredInitializers =
                        nbrOfVariables * ((TypeStructure) (m_type)).getTotalNoOfElements();
                } else {
                    requiredInitializers = nbrOfVariables *
                            ((TypeStructure)(((UserDefinedTypeStructure)m_type).getStructuredType())).getTotalNoOfElements();
                }
                if (nbrOfInitializer < requiredInitializers) {
                    ErrorStack.add(
                            m_identifierDenotationContext,
                            "INIT",
                            requiredInitializers - nbrOfInitializer + " initializers missing");
                } else if (nbrOfInitializer > requiredInitializers) {
                    ErrorStack.add(
                            initElements.get(requiredInitializers),
                            "INIT",
                            "too many initializers");
                }
            } else if (m_type instanceof TypeReference) {
                // need NameInitializer
                nbrOfInitializer = nbrOfVariables;

                //ErrorStack.addInternal(m_identifierDenotationContext,"","SymbolTableVisitor@708: NameInitializer missing");
            } else {
                // need SimpleInitializer
                if (nbrOfInitializer < nbrOfVariables) {
                    ErrorStack.add(
                            m_identifierDenotationContext.identifier(nbrOfInitializer),
                            "INIT",
                            (nbrOfVariables - nbrOfInitializer) + " initializers missing");
                } else if (nbrOfInitializer > nbrOfVariables) {
                    ErrorStack.add(
                            initElements.get(nbrOfVariables), "INIT", "too many initializers");
                }
            }
        }
        

        int nextInitializerIndex = 0;
        init = null;
        Initializer initializer = null;
        for (int i = 0; i < m_identifierDenotationContext.identifier().size(); i++) {
            String s = m_identifierDenotationContext.identifier(i).getText();
            if (initElements != null) {
                // create initializers

                if (m_type instanceof TypeArray) {
                    // need ArrayOrStructureInitialiser
                    int maxOfInitializers = ((TypeArray) (m_type)).getTotalNoOfElements();
                    if (((TypeArray) m_type).getBaseType() instanceof TypeStructure) {
                        maxOfInitializers *=
                                ((TypeStructure) ((TypeArray) m_type).getBaseType())
                                .getTotalNoOfElements();
                    }
                    int remainingInitializers = nbrOfInitializer - nextInitializerIndex;
                    if (remainingInitializers > maxOfInitializers) {
                        ParserRuleContext startCtx = initElements.get(nextInitializerIndex);
                        ArrayList<Initializer> ali = new ArrayList<Initializer>();
                        
                        for (int j = 0; j < maxOfInitializers; j++) {
                             initializer = getInitializer(initElements.get(nextInitializerIndex));
                            nextInitializerIndex++;
                            ali.add(initializer);
                        }
                        init = new ArrayOrStructureInitializer(startCtx, ali);
                    } else if (remainingInitializers == 0) {
                        // (re-)use last init element
                        ParserRuleContext startCtx = initElements.get(nextInitializerIndex);
                        ArrayList<Initializer> ali = new ArrayList<Initializer>();

                        ali.add(initializer);
                        init = new ArrayOrStructureInitializer(startCtx, ali);
                    } else {
                        ArrayList<Initializer> ali = new ArrayList<Initializer>();
                        ParserRuleContext startCtx = initElements.get(nextInitializerIndex);
                        for (int j = 0; j < remainingInitializers; j++) {
                            initializer = getInitializer(initElements.get(nextInitializerIndex));
                            nextInitializerIndex++;
                            ali.add(initializer);
                        }
                        init = new ArrayOrStructureInitializer(startCtx, ali);
                    }

                } else if (m_type instanceof TypeStructure || m_type instanceof UserDefinedTypeStructure) {
                    // need ArrayOrStructureInitialiser
                    int maxOfInitializers;
                    if (m_type instanceof TypeStructure) {
                        maxOfInitializers = ((TypeStructure) (m_type)).getTotalNoOfElements();
                    } else {
                        maxOfInitializers = ((TypeStructure)(((UserDefinedTypeStructure)(m_type)).getStructuredType())) .getTotalNoOfElements();
                    }
                    int remainingInitializers = nbrOfInitializer - nextInitializerIndex;
                    if (remainingInitializers >= maxOfInitializers) {
                        ParserRuleContext startCtx = initElements.get(nextInitializerIndex);
                        ArrayList<Initializer> ali = new ArrayList<Initializer>();
                        for (int j = 0; j < maxOfInitializers; j++) {
                            initializer = getInitializer(initElements.get(nextInitializerIndex));
                            nextInitializerIndex++;
                            ali.add(initializer);
                        }
                        init = new ArrayOrStructureInitializer(startCtx, ali);
                    }
                } else if (m_type instanceof TypeReference) {
                    // lookup the SymboltableEntry of the identifier
                    // further checks of existence, type and structure components are done 
                    // in CheckVariableDefinition
                    init = getInitializer(initElements.get(nextInitializerIndex));
                 
                } else {
                    // need SimpleInitializer
                    if (nextInitializerIndex < initElements.size()) {
                        init = getInitializer(initElements.get(nextInitializerIndex));
                            nextInitializerIndex++;
                            if (m_hasAllocationProtection && m_type instanceof TypeFixed) { 
                                if (init instanceof SimpleInitializer && ((SimpleInitializer)init).getConstant() != null) {
                                    ConstantValue constant = ((SimpleInitializer)init).getConstant();
                                    if (!( constant instanceof ConstantFixedValue) || m_type.getPrecision() < ((ConstantFixedValue)constant).getPrecision()) {
//                                        
//                                if (constant != null &&(
//                                        !(constant instanceof ConstantFixedValue) || 
//                                        m_type.getPrecision() < ((ConstantFixedValue)constant).getPrecision()) ){
                                    // error in case of initializer is not of type fixed
                                    // or precision of initializer is larger than variable
                                    // if the constant is null --> the error is reported already
                                     
                                    ErrorStack.enter(m_identifierDenotationContext.identifier(i));
                                    CommonErrorMessages.typeMismatchInInit(m_type.toErrorString(), constant.getType().toErrorString(), null);
                                    ErrorStack.leave();
                                }

                            }
                        }
                    }
                }
            }
            if (m_hasAllocationProtection && init == null && m_isInSpecification==false) {
                ErrorStack.add(
                        m_identifierDenotationContext.identifier(i), "DCL", "INV needs INIT");
            }
            VariableEntry ve =
                    new VariableEntry(
                            s,
                            m_type,
                            //m_hasAllocationProtection,
                            m_identifierDenotationContext.identifier(i),
                            init);

            // spc/dcl and global attribute is treated in checkDoubleDefinitionAndEnterToSymbolTable
            checkDoubleDefinitionAndEnterToSymbolTable(
                    ve, m_identifierDenotationContext.identifier(i));
        }
        m_type = null; // invalidate m_type to avoid side effects with SIZOF <type>

        return null;
    }

    private Initializer getInitializer(InitElementContext ctx) {
        Initializer init = null;
        if (ctx.identifier() != null) {
            String identifier = ctx.identifier().getText();
            SymbolTableEntry se = m_currentSymbolTable.lookup(identifier);
            if (se != null) {
               init = new ReferenceInitializer( ctx.identifier(),se,m_currentSymbolTable);
            } else {
                ErrorStack.add(ctx,"DCL","undefined symbol: '"+ identifier+"'");
            }
        } else if (ctx.name() != null) {
            String identifier = ctx.name().ID().getText();
            SymbolTableEntry se = m_currentSymbolTable.lookup(identifier);
            if (se != null) {
               init = new ReferenceInitializer( ctx.name(),se,m_currentSymbolTable);
            } else {
                ErrorStack.add(ctx,"DCL","undefined symbol: '"+ identifier+"'");
            }
        } else if (ctx.constant()!= null) {
            ConstantValue constant =
                    getInitElement(ctx);
             init = new SimpleInitializer(
                            ctx, constant);
        } else if (ctx.constantExpression()!= null) {
            init = new SimpleInitializer(ctx, null); // the constant expression becomes evaluated in ContantFolding!
        }  else {
            ErrorStack.addInternal(ctx,"SymbolTableVisitor@857","missing alternative");
        }
        return init;
    }
    
    @Override
    public Void visitSemaDenotation(SemaDenotationContext ctx) {
        int nbrOfInitializersPerName = 1;
        boolean isArray = false;
        
        m_isGlobal = false;
        m_globalName = null;

        
        List<InitElementContext> initElements = null;
        if (ctx.preset() != null) {
            initElements = ctx.preset().initElement();
        }
        if (m_type != null && m_type instanceof TypeArray) {
            nbrOfInitializersPerName = ((TypeArray)m_type).getTotalNoOfElements();
            ((TypeArray)m_type).setBaseType(new TypeSemaphore());
            isArray = true;
        } else {
           m_type = new TypeSemaphore();
        }
        
        visitChildren(ctx);

        if (initElements != null) {
            if (m_isInSpecification) {
                ErrorStack.add(ctx, "SPC", "no PRESET allowed");
            } else {
                if (m_identifierDenotationContext.identifier().size()*nbrOfInitializersPerName < initElements.size()) {
                    ErrorStack.add(ctx, "DCL", "too many PRESETs");
                }
            }
        }

        SimpleInitializer init = null;
        int nextInitializerIndex = 0;
        ArrayList<Initializer> arrayInit = new ArrayList<Initializer>();
 
        for (int i = 0; i < m_identifierDenotationContext.identifier().size(); i++) {
            if (initElements != null) {
                for (int j=0; j<nbrOfInitializersPerName; j++) {
                    if (i < initElements.size()) {
                        ConstantValue constant =
                                getInitElement(
                                      
                                        initElements.get(nextInitializerIndex));
                        if (constant == null) {
                            ErrorStack.add(m_identifierDenotationContext.identifier(i),
                                    "PRESET","could not evaluate initializer");   
                        }

                        init =
                                new SimpleInitializer(
                                        initElements.get(nextInitializerIndex), constant);
                        nextInitializerIndex++;
                    } else {
                        // use last initializer
                    }
                    if (isArray) {
                        arrayInit.add(init);
                    }
                }

            }
            String s = m_identifierDenotationContext.identifier(i).getText();
            VariableEntry ve = null;
            if (!isArray) {
            ve =
                    new VariableEntry(
                            s,
                            m_type,
                            //m_hasAllocationProtection,
                            m_identifierDenotationContext.identifier(i),
                            init);
            } else {
                ArrayOrStructureInitializer aInit = new ArrayOrStructureInitializer(ctx.preset(), arrayInit);
                
               ve =
                        new VariableEntry(
                                s,
                                m_type,
                                //m_hasAllocationProtection,
                                m_identifierDenotationContext.identifier(i),
                                aInit);
                
            }
            // spc/dcl and global attribute is treated in checkDoubleDefinitionAndEnterToSymbolTable
            checkDoubleDefinitionAndEnterToSymbolTable(
                    ve, m_identifierDenotationContext.identifier(i));
        }
        m_type = null; // invalidate m_type to avoid side effects with SIZOF <type>

        return null;
    }

    public Void visitAllocationProtection(AllocationProtectionContext ctx) {
        m_hasAllocationProtection = true;
        return null;
    }

    public Void visitGlobalAttribute(GlobalAttributeContext ctx) {
        m_isGlobal = false;
        m_globalName = null;
        if (ctx != null) {
            m_isGlobal = true;
            m_globalName = null;
            if (ctx.ID() != null) {
                m_globalName = ctx.ID().getText();
            }
        }
        return null;
    }

      
    @Override
    public Void visitTypeReference(TypeReferenceContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeReference:ctx" + CommonUtils.printContext(ctx));
        boolean hasVistualDimensionList = false;
        boolean hasAssignmentProtection = false;
        int dimensions = 0;
        if (ctx.virtualDimensionList() != null) {
            hasVistualDimensionList = true;
            if (ctx.virtualDimensionList().commas() != null) {
                dimensions = ctx.virtualDimensionList().commas().getChildCount()+1;
            } else {
                dimensions = 1;
            }
        }
        if (ctx.assignmentProtection() != null) {
            hasAssignmentProtection = true;
        }
        visitChildren(ctx);
        if (m_type != null) {
            m_type.setHasAssignmentProtection(hasAssignmentProtection);
            if (hasVistualDimensionList) {
                m_type = new TypeArraySpecification(m_type, dimensions);
            }
            m_type = new TypeReference(m_type);
        }
        return null;
    }

    @Override
    public Void visitTypeRefChar(TypeRefCharContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeRefChar:ctx" + CommonUtils.printContext(ctx));
        m_type = new TypeRefChar();
        return null;
    }
    

    @Override
    public Void visitTypeDation(TypeDationContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeDation:ctx" + CommonUtils.printContext(ctx));

        TypeDation d = new TypeDation();
        //        private void treatTypeDation(TypeDationContext ctx, TypeDation d) {
        if (ctx != null) {
            // let's have a look on the type of the dation
            if (ctx.sourceSinkAttribute() != null) {
                if (ctx.sourceSinkAttribute() instanceof SourceSinkAttributeINContext) {
                    d.setIn(true);
                } else if (ctx.sourceSinkAttribute() instanceof SourceSinkAttributeOUTContext) {
                    d.setOut(true);
                } else if (ctx.sourceSinkAttribute() instanceof SourceSinkAttributeINOUTContext) {
                    d.setIn(true);
                    d.setOut(true);
                } else {
                    ErrorStack.addInternal(
                            ctx.sourceSinkAttribute(),
                            "SymbolTableVisitor",
                            "untreated SourceSinkAttribute");
                }
            }
            if (ctx.classAttribute() != null) {
                if (ctx.classAttribute().systemDation() != null) {
                    d.setSystemDation(true);
                }
                if (ctx.classAttribute().alphicDation() != null) {
                    d.setAlphic(true);
                }
                if (ctx.classAttribute().basicDation() != null) {
                    d.setBasic(true);
                }
                if (ctx.classAttribute().typeOfTransmissionData() != null) {
                    TypeOfTransmissionDataContext c = ctx.classAttribute().typeOfTransmissionData();
                    if (c instanceof TypeOfTransmissionDataALLContext) {
                        // nothing to do here!
                    } else {
                        // ether simpleType or typeStructure possible
                        visitChildren(c);
                        d.setTypeOfTransmission(m_type);
                    }
                    // maybe we need some treatment for STRUCT
                    //d.setTypeOfTransmission(c.getText());
                }
            }
        }

        // in progress treat typologgy
        if (ctx.typology() != null) {
            TypologyContext c = ctx.typology();
            if (c.dimension1() != null) {
                d.setDimension1(treatDimension(c.dimension1().getText()));
            }
            if (c.dimension2() != null) {
                d.setDimension2(treatDimension(c.dimension2().getText()));
            }
            if (c.dimension3() != null) {
                d.setDimension3(treatDimension(c.dimension3().getText()));
            }

            if (c.tfu() != null) {
                d.setTfu(true);
                if (c.tfu().tfuMax() != null) {
                    ErrorStack.enter(c.tfu(), "TFU MAX");
                    ErrorStack.warn("is deprecated");
                    ErrorStack.leave();
                }
            }

        } else {
            // we have a type basic without typology --> DationTS; ALPHIC should not be set
            // lets check this in another stage
        }

        if (ctx.accessAttribute() != null) {
            ErrorStack.enter(ctx.accessAttribute(), "accessAttributes");
            for (ParseTree c1 : ctx.accessAttribute().children) {
                if (c1.getText().equals("DIRECT")) d.setDirect(true);
                else if (c1.getText().equals("FORWARD")) d.setForward(true);
                else if (c1.getText().equals("FORBACK")) {
                    ErrorStack.add("FORBACK is not supported");
                } else if (c1.getText().equals("CYCLIC")) d.setCyclic(true);
                else if (c1.getText().equals("NOCYCL")) d.setCyclic(false);
                else if (c1.getText().equals("STREAM")) d.setStream(true);
                else if (c1.getText().equals("NOSTREAM")) d.setStream(false);
                else {
                    ErrorStack.addInternal(
                            ctx.accessAttribute(),
                            "SymbolTableVisitor",
                            "untreated accessAttribute");
                }
            }
            ErrorStack.leave();
        }
        m_type = d;
        Log.warn("SmybolTableVisitor: treatment of TypeDation must be more elaboarate");
        return null;
    }

    @Override
    public Void visitTypeTask(TypeTaskContext ctx) {
        Log.debug(
                "SymbolTableVisitor:visitTypeTask:ctx"
                        + CommonUtils.printContext(ctx));

        m_type = new TypeTask();
        return null;
    }

    @Override
    public Void visitTypeSema(TypeSemaContext ctx) {
        Log.debug(
                "SymbolTableVisitor:visitTypeSema:ctx"
                        + CommonUtils.printContext(ctx));

        m_type = new TypeSemaphore();
        return null;
    }

    @Override
    public Void visitTypeBolt(TypeBoltContext ctx) {
        Log.debug(
                "SymbolTableVisitor:visitTypeBolt:ctx"
                        + CommonUtils.printContext(ctx));

        m_type = new TypeBolt();
        return null;
    }

    @Override
    public Void visitTypeProcedure(TypeProcedureContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeProcedure:ctx" + CommonUtils.printContext(ctx));
        LinkedList<FormalParameter> formalParameters = null;
        TypeDefinition resultType = null;

       // ASTAttribute resultAttr = null;
        for (ParseTree c : ctx.children) {
            if (c instanceof ResultAttributeContext) {
                resultType = getResultAttribute((ResultAttributeContext) c);
        //        resultAttr = new ASTAttribute(resultType);
            } else if (c instanceof ListOfFormalParametersContext) {
                formalParameters =
                        getListOfFormalParameters(
                                (ListOfFormalParametersContext) c);
            }
        }
        m_formalParameters = formalParameters; // required for SPC PROC
        m_type = new TypeProcedure(formalParameters, resultType);

        return null;
    }

    
    @Override
    public Void visitTypeInterrupt(
            TypeInterruptContext ctx) {
        Log.debug(
                "SymbolTableVisitor:visitTypeInterrupt:ctx"
                        + CommonUtils.printContext(ctx));

        m_type = new TypeInterrupt();
        return null;
    }

    @Override
    public Void visitTypeSignal(TypeSignalContext ctx) {
        Log.debug(
                "SymbolTableVisitor:visitTypeSignal:ctx"
                        + CommonUtils.printContext(ctx));

        m_type = new TypeSignal();
        return null;
    }

    @Override
    public Void visitTypeInteger(TypeIntegerContext ctx) {
        Integer size = m_currentSymbolTable.lookupDefaultFixedLength();

        if (ctx != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof MprecisionContext) {
                    size =
                            Integer.parseInt(
                                    ((MprecisionContext) c)
                                    .integerWithoutPrecision()
                                    .IntegerConstant()
                                    .getText());
                    if (size < Defaults.FIXED_MIN_LENGTH || size > Defaults.FIXED_MAX_LENGTH) {
                        CommonErrorMessages.wrongFixedPrecission(ctx.mprecision());
                    }
                }
            }
        }
        m_type = new TypeFixed(size);

        return null;
    }

    @Override
    public Void visitTypeBitString(TypeBitStringContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeBitString:ctx" + CommonUtils.printContext(ctx));
        int length = m_currentSymbolTable.lookupDefaultBitLength();

        if (ctx.length() != null) {
            length = Integer.parseInt(ctx.length().getText());
            if (length < Defaults.BIT_MIN_LENGTH || length > Defaults.BIT_MAX_LENGTH) {
                CommonErrorMessages.wrongBitLength(ctx.length());
            }
        }

        m_type = new TypeBit(length);

        return null;
    }

    @Override
    public Void visitTypeCharacterString(TypeCharacterStringContext ctx) {
        int length = m_currentSymbolTable.lookupDefaultCharLength();

        if (ctx.length() != null) {
            length = Integer.parseInt(ctx.length().getText());

            if (length < Defaults.CHARACTER_LENGTH || length > Defaults.CHARACTER_MAX_LENGTH) {
                CommonErrorMessages.wrongCharLength(ctx.length());
            }
        }

        m_type = new TypeChar(length);

        return null;
    }

    @Override
    public Void visitTypeFloatingPointNumber(TypeFloatingPointNumberContext ctx) {
        int precision = m_currentSymbolTable.lookupDefaultFloatLength();

        if (ctx.length() != null) {
            precision = Integer.parseInt(ctx.length().getText());

            if (precision != Defaults.FLOAT_SHORT_PRECISION
                    && precision != Defaults.FLOAT_LONG_PRECISION) {
                CommonErrorMessages.wrongFloatPrecission(ctx.length());
            }
        }

        m_type = new TypeFloat(precision);

        return null;
    }

    @Override
    public Void visitTypeDuration(TypeDurationContext ctx) {
        m_type = new TypeDuration();
        return null;
    }

    @Override
    public Void visitTypeClock(TypeClockContext ctx) {
        m_type = new TypeClock();
        return null;
    }


    @Override
    public Void visitVirtualDimensionList(VirtualDimensionListContext ctx) {
        int nbrDimensions;
        if (ctx != null) {

            // get the number of array dimensions
            // we count the ',' symbols and add 1,since 0 ',' is dimension 1
            nbrDimensions = 1;
            if (ctx.commas() != null) {
                nbrDimensions += ctx.commas().getChildCount();
            }
            TypeArray array = new TypeArray();
            //array.setBaseType(m_type);
            for (int i = 0; i < nbrDimensions; i++) {
                // the real dimensions limits are passed via array descriptor
                array.addDimension(new ArrayDimension());
            }
            m_type = array;
        }
        return null;
    }
    
    @Override
    public Void visitBoundaryDenotation(BoundaryDenotationContext ctx) {

        if (ctx.constantFixedExpression().size() == 1) {
            int upb =
                    CommonUtils.getConstantFixedExpression(
                            ctx.constantFixedExpression(0), m_currentSymbolTable);

            ((TypeArray) m_type)
            .addDimension(new ArrayDimension(Defaults.DEFAULT_ARRAY_LWB, upb, ctx));
        } else {
            int lwb =
                    CommonUtils.getConstantFixedExpression(
                            ctx.constantFixedExpression(0), m_currentSymbolTable);
            int upb =
                    CommonUtils.getConstantFixedExpression(
                            ctx.constantFixedExpression(1), m_currentSymbolTable);
            ((TypeArray) m_type).addDimension(new ArrayDimension(lwb, upb, ctx));
        }

        return null;
    }


    @Override
    public Void visitStatement(StatementContext ctx) {
        Log.debug("SymbolTableVisitor:visitStatement:ctx" + CommonUtils.printContext(ctx));

        if (ctx != null) {
            if (ctx.label_statement() != null) {
                for (int i = 0; i < ctx.label_statement().size(); i++) {
                    LabelEntry entry =
                            new LabelEntry(
                                    ctx.label_statement(i).ID().getText(), ctx.label_statement(i));

                    if (!m_currentSymbolTable.enter(entry)) {
                        ErrorStack.add(
                                ctx, "DECLARATION", "duplicate name '" + entry.getName() + "' in scope");
                    }
                }
            }

            visitChildren(ctx);
        }

        return null;
    }

    public LinkedList<ArrayDescriptor> getListOfArrayDescriptors() {
        Log.debug("SymbolTableVisitor:getListOfArrayDescriptors");

        return m_listOfArrayDescriptors;
    }

    @Override
    public Void visitLoopStatement(LoopStatementContext ctx) {
        Log.debug("SymbolTableVisitor:visitLoopStatement:ctx" + CommonUtils.printContext(ctx));

        String label = null;

        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor: visitLoopStatement");
        }

        if (ctx.loopStatement_end().ID() != null) {
            label = ctx.loopStatement_end().ID().getText();

            SymbolTableEntry se = m_currentSymbolTable.lookupLocal(label);
            if (se != null) {
                ErrorStack.add(
                        ctx.loopStatement_end(), "LOOP", "duplicate name '" + label + "' in scope");
                ErrorStack.note(
                        se.getCtx(),
                        "previous definion",
                        "");
                if (se instanceof LoopEntry) {
                    ErrorStack.note(
                            ((LoopStatementContext) (se.getCtx())).loopStatement_end(),
                            "previous definion",
                            "");
                }
                if (se instanceof BlockEntry) {
                    ErrorStack.note(se.getCtx(), "previous definition", "");
                }
            }
        }
        LoopEntry loopEntry = new LoopEntry(label, ctx, m_currentSymbolTable);

        m_currentSymbolTable = m_currentSymbolTable.newLevel(loopEntry);
        this.m_symboltablePerContext.put(ctx, this.m_currentSymbolTable);

        if (ctx.loopStatement_for() != null) {
            // we define the symbol with the default length
            // the ExpressionTypeVisitor checks the LoopStatement with types and precisions
            // and updates the precision of the loop control variable to fit with start-value,
            // increment and end-value as far as they are given
            VariableEntry controlVariable =
                    new VariableEntry(
                            ctx.loopStatement_for().ID().getText(),
                            new TypeFixed(Defaults.FIXED_LENGTH),
                            //true,
                            ctx.loopStatement_for(),
                            null);
            controlVariable.setLoopControlVariable();
            m_currentSymbolTable.enter(controlVariable);
        }
        
        visitChildren(ctx.loopBody());


        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBoltDenotation(BoltDenotationContext ctx) {
        Log.debug("SymbolTableVisitor:visitBoltDenotation:ctx" + CommonUtils.printContext(ctx));

        m_isGlobal = false;
        m_globalName = null;
        
        if (m_type != null && m_type instanceof TypeArray) {
            ((TypeArray)m_type).setBaseType(new TypeBolt());
        } else {
           m_type = new TypeBolt();
        }
        
       
        visitChildren(ctx);

 
        for (int i = 0; i < m_identifierDenotationContext.identifier().size(); i++) {
            String s = m_identifierDenotationContext.identifier(i).getText();
            VariableEntry ve = new VariableEntry(s, m_type, //m_hasAllocationProtection, 
                    m_identifierDenotationContext.identifier(i));

            // spc/dcl and global attribute is treated in checkDoubleDefinitionAndEnterToSymbolTable
            checkDoubleDefinitionAndEnterToSymbolTable(
                    ve, m_identifierDenotationContext.identifier(i));
        }
        
        m_type = null; // invalidate m_type to avoid side effects with SIZOF <type>

        return null;
    }

    public SymbolTable getSymbolTablePerContext(ParseTree ctx) {
        return m_symboltablePerContext.get(ctx);
    }

    @Override
    public Void visitDationDenotation(DationDenotationContext ctx) {
        // the TypeDation may have lot of parameters depending on user/system dation
        // so we just create an object and set the attributes while scanning the context
        m_isGlobal = false;
        m_globalName = null;

        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor: visitDationDenotation");
        }

        if (m_isInSpecification) {
            ErrorStack.enter(ctx, "SPC");
        } else {
            ErrorStack.enter(ctx, "DCL");
        }

        visitTypeDation(ctx.typeDation());
        visitGlobalAttribute(ctx.globalAttribute());

        TypeDation d = (TypeDation) m_type;
        d.setIsDeclaration(!m_isInSpecification);
        // treatIdentifierDenotation(ctx.identifierDenotation(), d);

        treatGlobalAttribute(ctx.globalAttribute(), d);
        if (ctx.typeDation().controlAttribute() != null) {
            if (Compiler.isStdOpenPEARL()) {
                Log.warn("deprecated: CONTROL(ALL)");
            }
        }

        // get CREATED parameter if we are in a declaration, must no given in specification
        if (ctx.ID() != null) {
            /* just add the string of system dations name to the TypeDation
             * and check definition and type in the CheckIOStatement analyses
             */
            d.setCreatedOnAsString(ctx.ID().toString());
        }
        
        ErrorStack.leave();
        
        for (int i = 0; i < m_identifierDenotationContext.identifier().size(); i++) {
            String s = m_identifierDenotationContext.identifier(i).getText();
            VariableEntry ve =
                    new VariableEntry(
                            s,
                            m_type,
                            //m_hasAllocationProtection,
                            m_identifierDenotationContext.identifier(i));

            // spc/dcl and global attribute is treated in checkDoubleDefinitionAndEnterToSymbolTable
            checkDoubleDefinitionAndEnterToSymbolTable(
                    ve, m_identifierDenotationContext.identifier(i));
        }
        m_type = null; // invalidate m_type to avoid side effects with SIZOF <type>


        return null;
    }

    /* -------------------------------------------------------------------  */
    /* START of common for dationDeclaration and dationSpecification 	    */
    /* -------------------------------------------------------------------  */


    int treatDimension(String s) {
        if (s.equals("*")) {
            return -1; // '*'
        } else {
            int nbr = Integer.parseInt(s);
            if (nbr <= 0) {
                ErrorStack.add("dimension range must be > 0");
            }
            return (nbr);
        }
    }

    void treatGlobalAttribute(GlobalAttributeContext ctx, TypeDation d) {
        if (ctx == null) {
            // no GLOBAL given --> default to m_currentModuleName if we are in a specification
            if (m_isInSpecification) {
                d.setGlobal(m_currentModuleName);
            }
        } else {
            if (ctx.ID() != null) {
                d.setGlobal(ctx.ID().getText());
            } else {
                d.setGlobal(m_currentModuleName);
            }
        }
    }
    /* -------------------------------------------------------------------  */
    /* END of common for dationDeclaration and dationSpecification 			*/
    /* -------------------------------------------------------------------  */

    @Override
    public Void visitLengthDefinition(LengthDefinitionContext ctx) {
        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor: visitLengthDefinition");
        }

        if (ctx.lengthDefinitionType()
                instanceof LengthDefinitionFixedTypeContext) {
            TypeFixed typ =
                    new TypeFixed(Integer.valueOf((ctx.length().IntegerConstant().toString())));

            if (typ.getPrecision() < Defaults.FIXED_MIN_LENGTH
                    || typ.getPrecision() > Defaults.FIXED_MAX_LENGTH) {
                CommonErrorMessages.wrongFixedPrecission(ctx.lengthDefinitionType());
            }

            LengthEntry entry = new LengthEntry(typ, ctx);
            m_currentSymbolTable.enterOrReplace(entry);
        } else if (ctx.lengthDefinitionType()
                instanceof LengthDefinitionFloatTypeContext) {
            TypeFloat typ =
                    new TypeFloat(Integer.valueOf((ctx.length().IntegerConstant().toString())));

            if (typ.getPrecision() != Defaults.FLOAT_SHORT_PRECISION
                    && typ.getPrecision() != Defaults.FLOAT_LONG_PRECISION) {
                CommonErrorMessages.wrongFloatPrecission(ctx.length());
            }

            LengthEntry entry = new LengthEntry(typ, ctx);
            m_currentSymbolTable.enterOrReplace(entry);
        } else if (ctx.lengthDefinitionType()
                instanceof LengthDefinitionBitTypeContext) {
            TypeBit typ = new TypeBit(Integer.valueOf((ctx.length().IntegerConstant().toString())));
            LengthEntry entry = new LengthEntry(typ, ctx);
            if (typ.getPrecision() < Defaults.BIT_MIN_LENGTH
                    || typ.getPrecision() > Defaults.BIT_MAX_LENGTH) {
                CommonErrorMessages.wrongBitLength(ctx.length());
            } else {
                m_currentSymbolTable.enterOrReplace(entry);
            }
        } else if (ctx.lengthDefinitionType()
                instanceof LengthDefinitionCharacterTypeContext) {
            TypeChar typ =
                    new TypeChar(Integer.valueOf((ctx.length().IntegerConstant().toString())));
            LengthEntry entry = new LengthEntry(typ, ctx);
            if (typ.getPrecision() < Defaults.CHARACTER_LENGTH
                    || typ.getPrecision() > Defaults.CHARACTER_MAX_LENGTH) {
                CommonErrorMessages.wrongCharLength(ctx.length());
            } else {
                m_currentSymbolTable.enterOrReplace(entry);
            }
        }

        return null;
    }

    private Void addArrayDescriptor(ArrayDescriptor descriptor) {
        boolean found = false;
        for (int i = 0; i < m_listOfArrayDescriptors.size(); i++) {
            if (m_listOfArrayDescriptors.get(i).equals(descriptor)) {
                found = true;
            }
        }

        if (!found) {
            m_listOfArrayDescriptors.add(descriptor);
        }
        return null;
    }

    private ConstantValue getInitElement(InitElementContext ctx) {
        ConstantValue constant = null;

        Log.debug("SymbolTableVisitor:getInitElement:ctx" + CommonUtils.printContext(ctx));

        if (ctx.constantExpression() != null) {
            constant = getConstantExpression(ctx.constantExpression());
        } else if (ctx.constant() != null) {
            constant = getConstant(ctx.constant());
        } else if (ctx.identifier() != null) {
            // check for names constant
            SymbolTableEntry entry = this.m_currentSymbolTable.lookup(ctx.identifier().getText());

            if (entry == null) {
                // may be a forward reference, which is not treated yet
                ErrorStack.add(
                        ctx, "initializer " + ctx.identifier().getText(), "is not defined yet");
                return null;

            } else {
                if (entry instanceof VariableEntry) {

                    VariableEntry var = (VariableEntry) entry;

                    //if (var.getAssigmentProtection()) {
                    if (var.getType().hasAssignmentProtection()) {
                        if (var.getInitializer() instanceof SimpleInitializer) {

                            constant = ((SimpleInitializer) var.getInitializer()).getConstant();
                        } else {
                            ErrorStack.add(ctx, "initializer", "not valid");
                            return null;
                        }
                    } else {
                        ErrorStack.add(ctx, "initializer", "must be INV");
                    }
                } else {
                    ErrorStack.addInternal("SymbolTableVisitor@1699: untreated alternative");
                    return null;
                }
            }
        }
        if (constant != null) {
           constant = m_constantPool.add(constant);
        }
        
        return constant;
    }


    private ConstantValue getConstant(ConstantContext ctx) {
        ConstantValue constant = null;
        int sign = 1;

        if (ctx.sign() != null) {
            if (ctx.sign() instanceof SignMinusContext) {
                sign = -1;
            }
        }

        if (ctx.fixedConstant() != null) {
            // treat sign after calculation of the precision
            long curval = Long.parseLong(ctx.fixedConstant().IntegerConstant().toString());
            int curlen = m_currentSymbolTable.lookupDefaultFixedLength();

            if (ctx.fixedConstant().fixedNumberPrecision() != null) {
                curlen =
                        Integer.parseInt(
                                ctx.fixedConstant()
                                .fixedNumberPrecision()
                                .IntegerConstant()
                                .toString());
            } else {
                //curlen = getPrecisionByType(0, m_type);
                // derive precision from the constant value 2021-11-01 (rm)
                curlen = (int)(Long.toBinaryString(curval).length());
                if (sign < 0) {
                    curlen ++;
                }
            }
            curval *= sign;

            constant = new ConstantFixedValue(curval, curlen);
        } else if (ctx.floatingPointConstant() != null) {
            double curval =
                    sign * CommonUtils.getFloatingPointConstantValue(ctx.floatingPointConstant());
            int curlen = 0;
            curlen =
                    CommonUtils.getFloatingPointConstantPrecision(
                            ctx.floatingPointConstant(),
                            m_currentSymbolTable.lookupDefaultFloatLength());
            constant = new ConstantFloatValue(curval, curlen);
        } else if (ctx.durationConstant() != null) {
            Integer hours = 0;
            Integer minutes = 0;
            Double seconds = 0.0;

            if (ctx.durationConstant().hours() != null) {
                hours =
                        Integer.valueOf(
                                ctx.durationConstant().hours().IntegerConstant().toString());
            }
            if (ctx.durationConstant().minutes() != null) {
                minutes =
                        Integer.valueOf(
                                ctx.durationConstant().minutes().IntegerConstant().toString());
            }
            if (ctx.durationConstant().seconds() != null) {
                if (ctx.durationConstant().seconds().IntegerConstant() != null) {
                    seconds =
                            Double.valueOf(
                                    ctx.durationConstant().seconds().IntegerConstant().toString());
                } else if (ctx.durationConstant().seconds().floatingPointConstant() != null) {
                    seconds =
                            CommonUtils.getFloatingPointConstantValue(
                                    ctx.durationConstant().seconds().floatingPointConstant());
                }
            }

            constant = new ConstantDurationValue(hours, minutes, seconds,sign);

        } else if (ctx.bitStringConstant() != null) {
            long value =
                    CommonUtils.convertBitStringToLong(
                            ctx.bitStringConstant().BitStringLiteral().getText());
            int nb =
                    CommonUtils.getBitStringLength(
                            ctx.bitStringConstant().BitStringLiteral().getText());
            constant = new ConstantBitValue(value, nb);
        } else if (ctx.timeConstant() != null) {
            Integer hours = 0;
            Integer minutes = 0;
            Double seconds = 0.0;

            hours = Integer.valueOf(ctx.timeConstant().IntegerConstant(0).toString());
            minutes = Integer.valueOf(ctx.timeConstant().IntegerConstant(1).toString());

            if (ctx.timeConstant().floatingPointConstant() != null) {
                seconds =
                        CommonUtils.getFloatingPointConstantValue(
                                ctx.timeConstant().floatingPointConstant());
            } else if (ctx.timeConstant().IntegerConstant(2) != null) {
                seconds = Double.valueOf(ctx.timeConstant().IntegerConstant(2).toString());
            } else {
                ErrorStack.addInternal(ctx, "SymbolTableVisitor@1738", "missing alternative");
            }

            constant = new ConstantClockValue(hours, minutes, seconds);
        } else if (ctx.stringConstant() != null) {
            String s = ctx.stringConstant().StringLiteral().toString();
            // s = CommonUtils.removeQuotes(s);
            // s = CommonUtils.compressPearlString(s);
            ConstantCharacterValue ccv = new ConstantCharacterValue(s);
            if (ccv.getLength() == 0) {
                ErrorStack.enter(ctx, "character string constant");
                ErrorStack.add("need at least 1 character");
                ErrorStack.leave();
            }
            // continue with wrong constant for further analysis
            constant = ccv;
        }

        return constant;
    }

    private ConstantValue getConstantExpression(ConstantExpressionContext ctx) {
        Log.debug("SymbolTableVisitor:getConstantExpression:ctx" + CommonUtils.printContext(ctx));

        ConstantValue constant=null;

        ConstantFixedExpressionEvaluator evaluator =
                new ConstantFixedExpressionEvaluator(
                        m_verbose, m_debug, m_currentSymbolTable, null, null);
     
        constant = evaluator.visit(ctx.constantFixedExpression());
        // note: if there was a problem, the method above already emitted an error message
        return constant;
    }

   
    /** add specified interrupts to the symbol table maybe this would be better placed in */
    @Override
    public Void visitInterruptDenotation(InterruptDenotationContext ctx) {
        m_isGlobal = false;
        m_globalName = null;

        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor:visitInterruptDenotaion");
        }

        visitChildren(ctx);

        for (int i = 0; i < ctx.identifierDenotation().identifier().size(); i++) {
            String iName = ctx.identifierDenotation().identifier(i).ID().toString();
            InterruptEntry ie = new InterruptEntry(iName, ctx.identifierDenotation().identifier(i));
            checkDoubleDefinitionAndEnterToSymbolTable(
                    ie, ctx.identifierDenotation().identifier(i));
        }
        m_type = null; // invalidate m_type to avoid side effects with SIZOF <type>

        return null;
    }

    /** add specified interrupts to the symbol table maybe this would be better placed in */
    @Override
    public Void visitTaskDenotation(TaskDenotationContext ctx) {
        m_isGlobal = false;
        m_globalName = null;

        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor:visitTaskDenotation");
        }

        visitChildren(ctx);

        for (int i = 0; i < ctx.identifierDenotation().identifier().size(); i++) {
            String iName = ctx.identifierDenotation().identifier(i).ID().toString();
            TaskEntry ie = new TaskEntry(iName, ctx.identifierDenotation().identifier(i));
            checkDoubleDefinitionAndEnterToSymbolTable(
                    ie, ctx.identifierDenotation().identifier(i));
        }
        m_type = null; // invalidate m_type to avoid side effects with SIZOF <type>

        return null;
    }

    public Void visitProcedureDenotation(ProcedureDenotationContext ctx) {
        m_isGlobal = false;
        m_globalName = null;

        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor:visitProcedureDenotation");
        }
        
        visitChildren(ctx);
        
        ASTAttribute resultType = null;

        if (ctx.typeProcedure().resultAttribute() != null) {
           resultType =     new ASTAttribute(   getResultAttribute(ctx.typeProcedure().resultAttribute()));
           m_type = new TypeProcedure(m_formalParameters, getResultAttribute(ctx.typeProcedure().resultAttribute()));
        } else {
            m_type = new TypeProcedure(m_formalParameters, null);
        }

        
        for (int i = 0; i < ctx.identifierDenotation().identifier().size(); i++) {
            String iName = ctx.identifierDenotation().identifier(i).ID().toString();
            Log.warn("SymbolTableVisitor@1924: visitProcedureDenotation still incomplete");
            
            //set scope to null to inhibit the creation of a new scope
            ProcedureEntry ie = new ProcedureEntry(iName,m_type, m_formalParameters, resultType,m_globalName,ctx,null);
            checkDoubleDefinitionAndEnterToSymbolTable(ie, ctx.identifierDenotation().identifier(i));
        }
        m_type = null; // invalidate m_type to avoid side effects with SIZOF <type>

        return null;
    }
    
   
  

    @Override
    public Void visitTypeStructure(TypeStructureContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeStructure:ctx" + CommonUtils.printContext(ctx));
       
        m_typeStructure = new TypeStructure();
        m_type = m_typeStructure;

        for (int i = 0; i < ctx.structureComponent().size(); i++) {
            visitStructureComponent(ctx.structureComponent(i));
        }
        
        m_type = m_typeStructure;

        return null;
    }

    @Override
    public Void visitStructureComponent(StructureComponentContext ctx) {
        Log.debug("SymbolTableVisitor:visitStructureComponent:ctx" + CommonUtils.printContext(ctx));
        TypeDefinition parentStructure = m_type;

        StructureComponent component = null;

        TypeStructure saved_typeStructure = m_typeStructure;

        // principle of operation
        // * in nested structs we work with recursion
        // * parentStructure is the struct which contains the current component
        // * if the current component is an array

        visitTypeAttributeInStructureComponent(ctx.typeAttributeInStructureComponent());

        if (ctx.dimensionAttribute() != null) {
            Log.debug("SymbolTableVisitor:visitStructureComponent: ARRAY");
            TypeArray ta = new TypeArray();
            ta.setBaseType(m_type);
            m_type = ta;
            visitDimensionAttribute(ctx.dimensionAttribute());
            addArrayDescriptor(
                    new ArrayDescriptor(
                            ((TypeArray) m_type).getNoOfDimensions(),
                            ((TypeArray) m_type).getDimensions()));
        }

        m_typeStructure = saved_typeStructure;

        for (int i = 0; i < ctx.ID().size(); i++) {
            component = new StructureComponent();
            component.m_type = m_type;
            component.m_id = ctx.ID(i).getText();

            saved_typeStructure.add(component);
        }
        m_type = parentStructure;

        return null;
    }

    @Override
    public Void visitTypeAttributeInStructureComponent(
            TypeAttributeInStructureComponentContext ctx) {
        Log.debug(
                "SymbolTableVisitor:visitTypeAttributeInStructureComponent:ctx"
                       + CommonUtils.printContext(ctx));
        String s = ctx.getText();
        if (ctx.simpleType() != null) {
            visitSimpleType(ctx.simpleType());
        } else if (ctx.typeStructure() != null) {
            visitTypeStructure(ctx.typeStructure());
        } else if (ctx.typeReference() != null) {
            visitTypeReference(ctx.typeReference());
        } else if (ctx.identifierForType() != null) {
            visitIdentifierForType(ctx.identifierForType());
        } else {
            ErrorStack.addInternal(
                    ctx,
                    "SymbolTableVisitor::visitTypeAttributeInStructureComponent",
                    "missing alternative");
        }

        return null;
    }


    private void checkDoubleDefinitionAndEnterToSymbolTable(
            SymbolTableEntry newEntry, ParserRuleContext ctx) {
        
        String s = newEntry.getName();
        
        // setup SPC/DCL and GLOBAL information
        if (m_isInSpecification) {
            newEntry.setIsSpecified();
            // check: m_isGlobal may be false if symbol is username in system part
            //        m_globalName my be null, in PEARL90Mode
            //                                 in OpenPEARL: warning defaulted to 'm_currentModuleName'
            if (m_isGlobal == false &&  m_currentSymbolTable.lookupSystemPartName(s) != null) {
                newEntry.setGlobalAttribute(m_currentModuleName);
            } else if (m_isGlobal == true && m_globalName == null) {
                newEntry.setGlobalAttribute(m_currentModuleName);
                SymbolTableEntry isSystemName = m_currentSymbolTable.lookupSystemPartName(s);
                if (Compiler.isStdOpenPEARL() && isSystemName == null) {
                    ErrorStack.warn(ctx,"SPC", "import module name defaulted to '"+m_currentModuleName+"'");
                }
            } else if (m_isGlobal == true && m_globalName != null) {
                newEntry.setGlobalAttribute(m_globalName);
            } else {
                ErrorStack.add(ctx,"SPC","GLOBAL attribute required for import from other module");
            }
        } else {
            if (m_isGlobal) {
                if (m_currentSymbolTable.m_level > 1) {
                    ErrorStack.add(ctx, "GLOBAL", "not allowed in procedures or tasks");
                } else {
                    if (m_globalName != null) {
                        ErrorStack.add(ctx, "GLOBAL", "no nameOfModule allowed in DCL");
                    } else {
                        newEntry.setGlobalAttribute(m_currentModuleName);
                    }
                }
            }
        }
        


        SymbolTableEntry entry = m_currentSymbolTable.lookupLocal(s);
        if (entry != null) {
            boolean ok = differsOnlyInSpcDcl(newEntry, entry);
            if (!ok) CommonErrorMessages.doubleDeclarationError(s, ctx, entry.getCtx());
        } else {
            entry = m_currentSymbolTable.lookup(s);
            if (entry != null && !(entry instanceof ModuleEntry) ) {
                CommonErrorMessages.doubleDeclarationWarning(
                        "redeclaration of ", s, ctx, entry.getCtx());
            }

            m_currentSymbolTable.enter(newEntry);
        }
    }
    
    /* compare two symbol table entries with the same name
     * 
     * it is ok, if they have the same type but differs in SPC and DCL
     * it is ok if one of them is ModuleEntry, or
     * it is ok if only one is a BlockEntry/LabelEntry, UserDefinedType, LoopEntry
     * 
     */
    private boolean differsOnlyInSpcDcl(SymbolTableEntry newEntry, SymbolTableEntry previousEntry) {
       boolean ok=false;
       
       if (previousEntry instanceof ModuleEntry) ok = true;
       
       if (!ok && previousEntry.getClass().equals(newEntry.getClass()) && m_currentSymbolTable.m_level==1) {
           // we have the same type of object; VariableEntry, ProcedureEntry must be checked for proper type
           if (previousEntry instanceof VariableEntry) {
               if (((VariableEntry)previousEntry).getType().equals(((VariableEntry)newEntry).getType())) {
                   ok = true;
               }
           } else if (previousEntry instanceof ProcedureEntry) {
               ProcedureEntry p = ((ProcedureEntry)previousEntry);
               ProcedureEntry n = ((ProcedureEntry)newEntry);
               boolean allTypesOk = true;
               if (p.getFormalParameters().size() == n.getFormalParameters().size()) {
                   
                   for (int i=0; i< p.getFormalParameters().size(); i++) {
                       if (!(p.getFormalParameters().get(i).getType().equals(n.getFormalParameters().get(i).getType()))) {
                           allTypesOk = false;
                       }
                   }
                   if (p.getResultType() != null 
                           && !(p.getResultType().equals(n.getResultType()))) {
                       allTypesOk = false;
                   }
               } else {
                   allTypesOk = false;
               }
               ok = allTypesOk;
           }
     
           if (ok && previousEntry.isSpecified()  !=  newEntry.isSpecified()) {
               // we must remove the specification from the symbol table
               if (previousEntry.isSpecified()) {
                   m_currentSymbolTable.enterOrReplace(newEntry);
               }
               ok=true;
           } else if (ok && previousEntry.isSpecified()  &&  newEntry.isSpecified()) {
               ok = true;
           }
       }
        return ok;
    }
}

