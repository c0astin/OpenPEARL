/*
 *  Copyright (c) 2012-2020 Marcel Schaible
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

package org.smallpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SmallPearlParser.*;
import org.smallpearl.compiler.SymbolTable.*;

//import com.sun.org.apache.xpath.internal.operations.Bool;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * run through the complete AST and creates symbols for definition of elements like
 * <ul>
 * <li>MODULE,TASK, PROC
 * <li>variables of all types
 * <li>formal parameters of procedures
 * <li>set and check initializer for scalar INV FIXED-types
 * </ul>
 *
 * Performed checks are:
 * <ul>
 *   <li>duplicate definitions in the same level
 *   <li>duplicate block or loop names in the hierarchy ???
 * </ul>
 *
 * rely on further checks:
 * <ol>
 * <li>CheckDeclarationScope
 * <li>CheckVariableDeclaration for fitting initialisers and presets
  * </ol>
 
 */
public class SymbolTableVisitor extends SmallPearlBaseVisitor<Void>
        implements SmallPearlVisitor<Void> {

    private final int m_verbose;
    private final boolean m_debug;

    public SymbolTable symbolTable;
    private SymbolTableEntry m_currentEntry;
    private SymbolTable m_currentSymbolTable;
    private final TypeStructure m_currentTypeStructure;
    private final LinkedList<LinkedList<SemaphoreEntry>> m_listOfTemporarySemaphoreArrays;
    private final LinkedList<LinkedList<BoltEntry>> m_listOfTemporaryBoltArrays;
    private final LinkedList<ArrayDescriptor> m_listOfArrayDescriptors;
    private LinkedList<TypeStructure> m_listOfStructureDeclarations;

    private TypeDefinition m_type;

    private ParseTreeProperty<SymbolTable> m_symboltablePerContext = null;
    private ConstantPool m_constantPool = null;

    private TypeStructure m_typeStructure = null;
    private final StructureComponent m_structureComponent = null;
    private boolean m_hasAllocationProtection = false;
    private boolean m_isGlobal = false;

    public SymbolTableVisitor(int verbose, ConstantPool constantPool) {
        m_debug = false;
        m_verbose = verbose;

        Log.debug("SymbolTableVisitor:Building new symbol table");

        this.symbolTable = new org.smallpearl.compiler.SymbolTable.SymbolTable();
        this.m_listOfTemporarySemaphoreArrays = new LinkedList<LinkedList<SemaphoreEntry>>();
        this.m_listOfTemporaryBoltArrays = new LinkedList<LinkedList<BoltEntry>>();
        this.m_listOfArrayDescriptors = new LinkedList<ArrayDescriptor>();
        this.m_symboltablePerContext = new ParseTreeProperty<SymbolTable>();
        this.m_constantPool = constantPool;
        this.m_currentTypeStructure = null;
        // TODO: MS REMOVE?:        this.m_currentStructureEntry = null;
        this.m_typeStructure = null;
    }

    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        Log.debug("SymbolTableVisitor:visitModule:ctx" + CommonUtils.printContext(ctx));

        org.smallpearl.compiler.SymbolTable.ModuleEntry moduleEntry =
                new org.smallpearl.compiler.SymbolTable.ModuleEntry(
                        ctx.nameOfModuleTaskProc().ID().getText(), ctx, null);
        this.m_currentSymbolTable = this.symbolTable.newLevel(moduleEntry);
        this.m_symboltablePerContext.put(ctx, this.m_currentSymbolTable);

        visitChildren(ctx);

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitSystemElementDefinition(SmallPearlParser.SystemElementDefinitionContext ctx) {
        Log.debug("SymbolTableVisitor:visitSystemElementDefinition:ctx"
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
    public Void visitSystemDescription(SmallPearlParser.SystemDescriptionContext ctx) {
        Log.debug("SymbolTableVisitor:visitSystemElementDefinition:ctx"
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
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        Boolean isMain = false;
        Boolean isGlobal = false;
        SmallPearlParser.PriorityContext priority = null;

        Log.debug("SymbolTableVisitor:visitTaskDeclaration:ctx" + CommonUtils.printContext(ctx));

        String s = ctx.nameOfModuleTaskProc().ID().toString();
        SymbolTableEntry entry = this.m_currentSymbolTable.lookupLocal(s);
        if (entry != null) {
            CommonErrorMessages.doubleDeclarationError(s, ctx.nameOfModuleTaskProc(),
                    entry.getCtx());
        } else {
            entry = this.m_currentSymbolTable.lookup(s);
            if (entry != null) {
                CommonErrorMessages.doubleDeclarationWarning("task definition ", s,
                        ctx.nameOfModuleTaskProc(), entry.getCtx());
            }
        }


        isMain = ctx.task_main() != null;
        if (ctx.priority() != null) {
            priority = ctx.priority();
        }

        TaskEntry taskEntry = new TaskEntry(ctx.nameOfModuleTaskProc().ID().getText(), priority,
                isMain, isGlobal, ctx, this.m_currentSymbolTable);
        this.m_currentSymbolTable = this.m_currentSymbolTable.newLevel(taskEntry);
        this.m_symboltablePerContext.put(ctx, this.m_currentSymbolTable);

        visitChildren(ctx);

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }



    @Override
    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        String globalId = null;
        LinkedList<FormalParameter> formalParameters = null;
        ASTAttribute resultType = null;

        Log.debug(
                "SymbolTableVisitor:visitProcedureDeclaration:ctx" + CommonUtils.printContext(ctx));
        TypeProcedureContext tpc = ctx.typeProcedure();
        for (ParseTree c : tpc.children) {
            if (c instanceof SmallPearlParser.ResultAttributeContext) {
                resultType = new ASTAttribute(
                        getResultAttribute((SmallPearlParser.ResultAttributeContext) c));
            } else if (c instanceof SmallPearlParser.GlobalAttributeContext) {
                globalId = ctx.nameOfModuleTaskProc().getText();
            } else if (c instanceof SmallPearlParser.ListOfFormalParametersContext) {
                formalParameters = getListOfFormalParameters(
                        (SmallPearlParser.ListOfFormalParametersContext) c);
            }
        }

        if (ctx.globalAttribute() != null) {
            globalId = ctx.globalAttribute().ID().getText();
        }

        String s = ctx.nameOfModuleTaskProc().ID().toString();
        SymbolTableEntry entry = this.m_currentSymbolTable.lookupLocal(s);
        if (entry != null) {
            CommonErrorMessages.doubleDeclarationError(s, ctx.nameOfModuleTaskProc(),
                    entry.getCtx());
        } else {
            entry = this.m_currentSymbolTable.lookup(s);
            if (entry != null) {
                CommonErrorMessages.doubleDeclarationWarning("procedure definition ", s,
                        ctx.nameOfModuleTaskProc(), entry.getCtx());
            }
        }

        ProcedureEntry procedureEntry = new ProcedureEntry(ctx.nameOfModuleTaskProc().getText(),
                formalParameters, resultType, globalId, ctx, this.m_currentSymbolTable);
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
                    CommonErrorMessages.doubleDeclarationWarning("formal parameter ",
                            formalParameter.getName(), formalParameter.getCtx(), entry.getCtx());
                }
                this.m_currentSymbolTable.enter(formalParameter);

            }
        }

        this.m_symboltablePerContext.put(ctx, this.m_currentSymbolTable);

        visitChildren(ctx);

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    private TypeDefinition getResultAttribute(SmallPearlParser.ResultAttributeContext ctx) {
        Log.debug("SymbolTableVisitor:getResultAttribute:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx.resultType());
        return m_type;
    }

    private LinkedList<FormalParameter> getListOfFormalParameters(
            SmallPearlParser.ListOfFormalParametersContext ctx) {
        LinkedList<FormalParameter> listOfFormalParameters = new LinkedList<FormalParameter>();

        Log.debug(
                "SymbolTableVisitor:getListOfFormalParameters:ctx" + CommonUtils.printContext(ctx));

        if (ctx != null) {
            for (int i = 0; i < ctx.formalParameter().size(); i++) {
                getFormalParameter(listOfFormalParameters, ctx.formalParameter(i));
            }
        }

        return listOfFormalParameters;
    }

    private Void getFormalParameter(LinkedList<FormalParameter> listOfFormalParameters,
            SmallPearlParser.FormalParameterContext ctx) {
        Log.debug("SymbolTableVisitor:getFormalParameter:ctx" + CommonUtils.printContext(ctx));

        if (ctx != null) {
            for (int i = 0; i < ctx.identifier().size(); i++) {
                int nbrDimensions = 0; // default to scalar value
                String name = null;
                Boolean assignmentProtection = false;
                Boolean passIdentical = false;

                name = ctx.identifier(i).ID().getText();

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

                listOfFormalParameters.add(new FormalParameter(name, m_type, assignmentProtection,
                        passIdentical, ctx));
            }
        }

        return null;
    }

    private Void getParameterType(SmallPearlParser.ParameterTypeContext ctx) {
        Log.debug("SymbolTableVisitor:getParameterType:ctx" + CommonUtils.printContext(ctx));

        visitChildren(ctx);

        // if (ctx.simpleType() != null) {
        //     visitSimpleType(ctx.simpleType());
        // } else if (ctx.typeStructure() != null) {
        //     visitTypeStructure(ctx.typeStructure());
        // }

        return null;
    }

    @Override
    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        String blockLabel = null;

        Log.debug("SymbolTableVisitor:visitBlock_statement:ctx" + CommonUtils.printContext(ctx));

        if (ctx.blockId() != null) {
            blockLabel = ctx.blockId().ID().toString();

            SymbolTableEntry se = m_currentSymbolTable.lookup(blockLabel);
            if (se != null) {
                ErrorStack.add(ctx.blockId(), "BLOCK",
                        "duplicate name '" + blockLabel + "' in scope");
                if (se instanceof LoopEntry) {
                    ErrorStack.note(((LoopStatementContext) (se.getCtx())).loopStatement_end(),
                            "previous definion", "");
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
    public Void visitVariableDenotation(SmallPearlParser.VariableDenotationContext ctx) {
        boolean hasAllocationProtection = false;
        List<InitElementContext> initElements = null;
        ArrayList<String> identifierDenotationList = null;


        Log.debug("SymbolTableVisitor:visitVariableDenotation:ctx" + CommonUtils.printContext(ctx));

        m_type = null;
        m_hasAllocationProtection = false;
        if (ctx != null) {
            // visit dimension attribute at last item

            if (ctx.problemPartDataAttribute() != null) {
                visitProblemPartDataAttribute(ctx.problemPartDataAttribute());
                if (ctx.problemPartDataAttribute().initialisationAttribute() != null) {
                    initElements =
                            ctx.problemPartDataAttribute().initialisationAttribute().initElement();
                }
            } else if (ctx.semaDenotation() != null) {
                visitSemaDenotation(ctx.semaDenotation());
                if (ctx.semaDenotation().preset() != null) {
                    initElements = ctx.semaDenotation().preset().initElement();
                }
            } else if (ctx.boltDenotation() != null) {
                visitBoltDenotation(ctx.boltDenotation());
            } else if (ctx.dationDenotation() != null) {
                visitDationDenotation(ctx.dationDenotation());
            } else {
                ErrorStack.addInternal(ctx, "SymbolTableVisitor:variableDenotation",
                        "missing alternative");
            }

        }



        identifierDenotationList = getIdentifierDenotation(ctx.identifierDenotation());


        if (ctx.dimensionAttribute() != null) {
            visitDimensionAttribute(ctx.dimensionAttribute());
            if (identifierDenotationList.size() > 1) {
                ErrorStack.add(ctx.identifierDenotation(), "array declaration", "no list allowed");
            }


            if (m_type instanceof TypeArray) {
                addArrayDescriptor(new ArrayDescriptor(((TypeArray) m_type).getNoOfDimensions(),
                        ((TypeArray) m_type).getDimensions()));
            } else {
                ErrorStack.addInternal(ctx, "SymbolTableVisitor:variableDenotation",
                        "missing alternative@500");
            }
        }

        if (ctx.problemPartDataAttribute() != null) {
            m_type.setHasAssignmentProtection(m_hasAllocationProtection);
            if (m_type instanceof TypeReference) {
                // check allowed REF types; only types which may be used as arrays are allowed
                // REF () is not allowed on TASK, INTERRUPT, SIGNAL, PROCEDURE, FORMAT
                //         in OpenPEARL additionally not for DATION
                TypeDefinition base = ((TypeReference) m_type).getBaseType();
                if (base instanceof TypeArraySpecification) {
                    TypeDefinition arraySpec = ((TypeArraySpecification) base).getBaseType();
                    if (arraySpec instanceof TypeTask || arraySpec instanceof TypeInterrupt
                            || arraySpec instanceof TypeSignal
                            || arraySpec instanceof TypeProcedure) {
                        ErrorStack.add(ctx.problemPartDataAttribute().typeAttribute(), null,
                                " REF () not allowed on " + arraySpec);
                    } else if (arraySpec instanceof TypeDation) {
                        ErrorStack.add(ctx.problemPartDataAttribute().typeAttribute(), null,
                                " REF () DATION is not supported");
                    }
                }

            }
        }



        // check and set initialiser for scalar inv fixed types

        if (m_hasAllocationProtection && ctx.dimensionAttribute() == null
                && m_type instanceof TypeFixed) {
            if (initElements == null) {
                ErrorStack.add(ctx, "DCL", "INV needs INIT");
            } else {
                if (initElements.size() != identifierDenotationList.size()) {
                    ErrorStack.enter(ctx.identifierDenotation());
                    ErrorStack.add("wrong number of initialisers: found " + initElements.size()
                            + " -- required " + identifierDenotationList.size());
                    ErrorStack.leave();
                } else {
                    // numbers look fine; target type is m_type!
                    for (int i = 0; i < initElements.size(); i++) {
                        ConstantValue cv = getInitElement(i, initElements.get(i));
                        TypeDefinition type = null;
                        InitElementContext initElement = ctx.problemPartDataAttribute()
                                .initialisationAttribute().initElement(i);
                        if (initElement.identifier() != null) {
                            SymbolTableEntry se =
                                    m_currentSymbolTable.lookup(initElement.identifier().getText());
                            if (se == null) {
                                ErrorStack.add(initElement.identifier(), "SymbolTableVisitor", "id "
                                        + initElement.identifier().getText() + " not defined yet");
                            } else {
                                if (se instanceof VariableEntry) {
                                    VariableEntry ve = (VariableEntry) se;
                                    if (!ve.getAssigmentProtection()) {
                                        ErrorStack.add(initElement, "INIT", "must be INV");
                                    }
                                    if (ve.getType() instanceof TypeFixed) {
                                        if (ve.getType().getPrecision() > m_type.getPrecision()) {
                                            ErrorStack.add(initElement, "typemismatch in INIT",
                                                    m_type + " := " + ve.getType());
                                        } else {

                                            VariableEntry variableEntry = new VariableEntry(
                                                    identifierDenotationList.get(i), m_type,
                                                    m_hasAllocationProtection,
                                                    ctx.identifierDenotation().identifier(i),
                                                    ve.getInitializer());
                                            String s = identifierDenotationList.get(i).toString();
                                            checkDoubleDefinitionAndEnterToSymbolTable(
                                                    variableEntry,
                                                    ctx.identifierDenotation().identifier(i), s);
                                        }

                                    }

                                } else {
                                    ErrorStack.addInternal(ctx, "SymbolTableVisitor",
                                            "missing alternative@1");
                                }
                            }
                        }
                        if (initElement.constant() != null
                                && initElement.constant().fixedConstant() != null) {
                            int precision = 0;
                            long value = 0;
                            if (initElement.constant().fixedConstant() != null) {
                                value = Long.parseLong(initElement.constant().fixedConstant()
                                        .IntegerConstant().getText());

                                precision = Long.toBinaryString(Math.abs(value)).length();
                                if (value < 0) {
                                    precision++;
                                }
                            }

                            if (initElement.constant().fixedConstant()
                                    .fixedNumberPrecision() != null) {
                                precision = Integer.parseInt(initElement.constant().fixedConstant()
                                        .fixedNumberPrecision().IntegerConstant().toString());
                            }


                            ConstantFixedValue cfv = new ConstantFixedValue(value, precision);
                            m_constantPool.add(cfv); // add to constant pool; maybe we have it alreadyConstantFixedValue c = evaluator.visit(initElement.constant().fixedConstant() );

                            if (cfv != null && m_type.getPrecision() < cfv.getPrecision()) {
                                ErrorStack.add(initElement, "type mismatch in INIT",
                                        m_type + " := FIXED(" + cfv.getPrecision() + ")");
                            } else {
                                SimpleInitializer initializer =
                                        new SimpleInitializer(initElement, cfv);
                                VariableEntry variableEntry = new VariableEntry(
                                        identifierDenotationList.get(i), m_type,
                                        m_hasAllocationProtection,
                                        ctx.identifierDenotation().identifier(i), initializer);
                                String s = identifierDenotationList.get(i).toString();
                                checkDoubleDefinitionAndEnterToSymbolTable(variableEntry,
                                        ctx.identifierDenotation().identifier(i), s);

                            }
                        }



                    }

                }
            }
        } else {

            for (int i = 0; i < identifierDenotationList.size(); i++) {
                VariableEntry variableEntry = new VariableEntry(identifierDenotationList.get(i),
                        m_type, hasAllocationProtection, ctx.identifierDenotation().identifier(i),
                        null);

                String s = identifierDenotationList.get(i).toString();
                checkDoubleDefinitionAndEnterToSymbolTable(variableEntry,
                        ctx.identifierDenotation().identifier(i), s);
            }
        }



        return null;
    }

    public Void visitAllocationProtection(SmallPearlParser.AllocationProtectionContext ctx) {
        m_hasAllocationProtection = true;
        return null;
    }

    public Void visitGlobalAttribute(SmallPearlParser.GlobalAttributeContext ctx) {
        m_isGlobal = true;
        return null;
    }

    /**
     * Check the compability of the variable type and the initializer
     * 
     * missing stuff: treatment of identifier for REF variables
     *
     * @param type of the variable
     * @param initializer
     */
    private void checkTypeCompability(TypeDefinition type, SimpleInitializer initializer) {
        Log.debug("SymbolTableVisitor:checkTypeCompability:type" + type);


        if ((type instanceof TypeFixed
                && !(initializer.getConstant() instanceof ConstantFixedValue))
                || (type instanceof TypeFloat
                        && (!(initializer.getConstant() instanceof ConstantFloatValue)
                                && !(initializer.getConstant() instanceof ConstantFixedValue)))
                || (type instanceof TypeDuration
                        && !(initializer.getConstant() instanceof ConstantDurationValue))
                || (type instanceof TypeClock
                        && (!(initializer.getConstant() instanceof ConstantDurationValue)
                                && !(initializer.getConstant() instanceof ConstantClockValue)))
                || (type instanceof TypeChar
                        && !(initializer.getConstant() instanceof ConstantCharacterValue))
                || (type instanceof TypeBit
                        && !(initializer.getConstant() instanceof ConstantBitValue))) {
            ErrorStack.add(initializer.m_context, null,
                    " init type does not match type of variable");
        }
    }

    private void fixPrecision(ParserRuleContext ctx, TypeDefinition type,
            SimpleInitializer initializer) {
        Log.debug("SymbolTableVisitor:fixPrecision:ctx" + CommonUtils.printContext(ctx));

        if (type instanceof TypeFixed) {
            TypeFixed typ = (TypeFixed) type;

            if (initializer.getConstant() instanceof ConstantFixedValue) {
                ConstantFixedValue value = (ConstantFixedValue) initializer.getConstant();
                value.setPrecision(typ.getPrecision());
            }
        } else if (type instanceof TypeFloat) {
            TypeFloat typ = (TypeFloat) type;

            if (initializer.getConstant() instanceof ConstantFloatValue) {
                ConstantFloatValue value = (ConstantFloatValue) initializer.getConstant();
                value.setPrecision(typ.getPrecision());
            } else if (initializer.getConstant() instanceof ConstantFixedValue) {
                ConstantFixedValue fixedValue = (ConstantFixedValue) initializer.getConstant();
                ConstantFloatValue floatValue =
                        new ConstantFloatValue((double) fixedValue.getValue(), typ.getPrecision());
                m_constantPool.add(floatValue);
                initializer.setConstant(floatValue);
            }
        }
    }

    //    @Override
    //    public Void visitTypeAttribute(SmallPearlParser.TypeAttributeContext ctx) {
    //        Log.debug("SymbolTableVisitor:visitTypeAttribute:ctx" + CommonUtils.printContext(ctx));
    //        
    //        if (ctx.simpleType() != null) {
    //            visitSimpleType(ctx.simpleType());
    //        } else if (ctx.typeReference() != null) {
    //            visitTypeReference(ctx.typeReference());
    //        }
    //        return null;
    //    }

    //    @Override
    //    public Void visitSimpleType(SmallPearlParser.SimpleTypeContext ctx) {
    //        Log.debug("SymbolTableVisitor:visitSimpleType:ctx" + CommonUtils.printContext(ctx));
    //
    //        if (ctx != null) {
    //            if (ctx.typeInteger() != null) {
    //                visitTypeInteger(ctx.typeInteger());
    //            } else if (ctx.typeDuration() != null) {
    //                visitTypeDuration(ctx.typeDuration());
    //            } else if (ctx.typeBitString() != null) {
    //                visitTypeBitString(ctx.typeBitString());
    //            } else if (ctx.typeFloatingPointNumber() != null) {
    //                visitTypeFloatingPointNumber(ctx.typeFloatingPointNumber());
    //            } else if (ctx.typeClock() != null) {
    //                visitTypeClock(ctx.typeClock());
    //            } else if (ctx.typeCharacterString() != null) {
    //                visitTypeCharacterString(ctx.typeCharacterString());
    //            }
    //        }
    //
    //        return null;
    //    }

    @Override
    public Void visitTypeReference(SmallPearlParser.TypeReferenceContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeReference:ctx" + CommonUtils.printContext(ctx));
        boolean hasVistualDimensionList = false;
        boolean hasAssignmentProtection = false;
        int dimensions = 0;
        if (ctx.virtualDimensionList() != null) {
            hasVistualDimensionList = true;
            if (ctx.virtualDimensionList().commas() != null) {
                dimensions = ctx.virtualDimensionList().commas().getChildCount();
            } else {
                dimensions = 1;
            }
        }
        if (ctx.assignmentProtection() != null) {
            hasAssignmentProtection = true;
        }
        visitChildren(ctx);
        m_type.setHasAssignmentProtection(hasAssignmentProtection);
        if (hasVistualDimensionList) {
            m_type = new TypeArraySpecification(m_type, dimensions);
        }
        m_type = new TypeReference(m_type);

        return null;
    }

    @Override
    public Void visitTypeRefChar(SmallPearlParser.TypeRefCharContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeRefChar:ctx" + CommonUtils.printContext(ctx));
        m_type = new TypeChar();
        return null;
    }

    @Override
    public Void visitTypeDation(SmallPearlParser.TypeDationContext ctx) {
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
                    ErrorStack.addInternal(ctx.sourceSinkAttribute(), "SymbolTableVisitor",
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
                    d.setTypeOfTransmission(c.getText());
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
                if (c1.getText().equals("DIRECT"))
                    d.setDirect(true);
                else if (c1.getText().equals("FORWARD"))
                    d.setForward(true);
                else if (c1.getText().equals("FORBACK")) {
                    ErrorStack.add("FORBACK is not supported");
                } else if (c1.getText().equals("CYCLIC"))
                    d.setCyclic(true);
                else if (c1.getText().equals("NOCYCL"))
                    d.setCyclic(false);
                else if (c1.getText().equals("STREAM"))
                    d.setStream(true);
                else if (c1.getText().equals("NOSTREAM"))
                    d.setStream(false);
                else {
                    ErrorStack.addInternal(ctx.accessAttribute(), "SymbolTableVisitor",
                            "untreated accessAttribute");
                }
            }
            ErrorStack.leave();
        }
        m_type = d;
        Log.warn("SybolTableVisitor: treatment of TypeDation must be more elaboarate");
        return null;
    }

    @Override
    public Void visitTypeReferenceTaskType(SmallPearlParser.TypeReferenceTaskTypeContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeReferenceTaskType:ctx"
                + CommonUtils.printContext(ctx));

        m_type = new TypeTask();
        return null;
    }

    @Override
    public Void visitTypeReferenceSemaType(SmallPearlParser.TypeReferenceSemaTypeContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeReferenceSemaType:ctx"
                + CommonUtils.printContext(ctx));

        m_type = new TypeSemaphore();
        return null;
    }

    @Override
    public Void visitTypeReferenceBoltType(SmallPearlParser.TypeReferenceBoltTypeContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeReferenceBoltType:ctx"
                + CommonUtils.printContext(ctx));

        m_type = new TypeBolt();
        return null;
    }

    @Override
    public Void visitTypeProcedure(SmallPearlParser.TypeProcedureContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeProcedure:ctx" + CommonUtils.printContext(ctx));
        LinkedList<FormalParameter> formalParameters = null;
        TypeDefinition resultType = null;

        ASTAttribute resultAttr = null;
        for (ParseTree c : ctx.children) {
            if (c instanceof SmallPearlParser.ResultAttributeContext) {
                resultType = getResultAttribute((SmallPearlParser.ResultAttributeContext) c);
                resultAttr = new ASTAttribute(resultType);
            } else if (c instanceof SmallPearlParser.ListOfFormalParametersContext) {
                formalParameters = getListOfFormalParameters(
                        (SmallPearlParser.ListOfFormalParametersContext) c);
            }
        }
        m_type = new TypeProcedure(formalParameters, resultType);

        return null;
    }

    @Override
    public Void visitTypeReferenceProcedureType(
            SmallPearlParser.TypeReferenceProcedureTypeContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeReferenceProcedureType:ctx"
                + CommonUtils.printContext(ctx));

        m_type = new TypeProcedure();
        return null;
    }

    @Override
    public Void visitTypeReferenceInterruptType(
            SmallPearlParser.TypeReferenceInterruptTypeContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeReferenceInterruptType:ctx"
                + CommonUtils.printContext(ctx));

        m_type = new TypeInterrupt();
        return null;
    }

    @Override
    public Void visitTypeReferenceSignalType(SmallPearlParser.TypeReferenceSignalTypeContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeReferenceSignalType:ctx"
                + CommonUtils.printContext(ctx));


        m_type = new TypeSignal();
        return null;
    }

    @Override
    public Void visitTypeInteger(SmallPearlParser.TypeIntegerContext ctx) {
        Integer size = m_currentSymbolTable.lookupDefaultFixedLength();

        if (ctx != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.MprecisionContext) {
                    size = Integer.parseInt(((SmallPearlParser.MprecisionContext) c)
                            .integerWithoutPrecision().IntegerConstant().getText());
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
    public Void visitTypeBitString(SmallPearlParser.TypeBitStringContext ctx) {
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
    public Void visitTypeCharacterString(SmallPearlParser.TypeCharacterStringContext ctx) {
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
    public Void visitTypeFloatingPointNumber(SmallPearlParser.TypeFloatingPointNumberContext ctx) {
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
    public Void visitTypeDuration(SmallPearlParser.TypeDurationContext ctx) {
        if (m_type instanceof TypeArray) {
            ((TypeArray) m_type).setBaseType(new TypeDuration());
        } else {
            m_type = new TypeDuration();
        }
        return null;
    }

    @Override
    public Void visitTypeClock(SmallPearlParser.TypeClockContext ctx) {
        if (m_type instanceof TypeArray) {
            ((TypeArray) m_type).setBaseType(new TypeClock());
        } else {
            m_type = new TypeClock();
        }
        return null;
    }

    private ArrayList<String> getIdentifierDenotation(
            SmallPearlParser.IdentifierDenotationContext ctx) {
        ArrayList<String> identifierDenotationList = new ArrayList<String>();

        if (ctx != null) {
            for (int i = 0; i < ctx.identifier().size(); i++) {
                identifierDenotationList.add(ctx.identifier(i).ID().toString());
            }
        }

        return identifierDenotationList;
    }


    //    @Override
    //    public Void visitArrayVariableDeclaration(
    //            SmallPearlParser.ArrayVariableDeclarationContext ctx) {
    //        if (m_verbose > 0) {
    //            System.out.println("SymbolTableVisitor: visitArrayVariableDeclaration");
    //        }
    //
    //        visitChildren(ctx);
    //
    //        return null;
    //    }

    @Override
    public Void visitArrayDenotation(SmallPearlParser.ArrayDenotationContext ctx) {
        Log.debug("SymbolTableVisitor:visitArrayDenotation:ctx" + CommonUtils.printContext(ctx));

        boolean hasGlobalAttribute = false;
        boolean hasAssigmentProtection = false;
        ArrayList<String> identifierDenotationList = new ArrayList<String>();
        ArrayOrStructureInitializer arrayOrStructureInitializer = null;

        m_type = new TypeArray();

        if (ctx != null) {
            for (int i = 0; i < ctx.ID().size(); i++) {
                identifierDenotationList.add(ctx.ID().get(i).toString());
            }
        }

        if (ctx != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.DimensionAttributeContext) {
                    visitDimensionAttribute((SmallPearlParser.DimensionAttributeContext) c);
                } else if (c instanceof SmallPearlParser.AssignmentProtectionContext) {
                    hasAssigmentProtection = true;
                } else if (c instanceof SmallPearlParser.TypeAttributeForArrayContext) {
                    visitTypeAttributeForArray((SmallPearlParser.TypeAttributeForArrayContext) c);
                } else if (c instanceof SmallPearlParser.GlobalAttributeContext) {
                    hasGlobalAttribute = true;
                }
            }

            if (ctx.initialisationAttribute() != null) {
                ArrayList<Initializer> initElementList = null;

                initElementList = getInitialisationAttribute(ctx.initialisationAttribute());

                if (initElementList != null && initElementList.size() > 0) {
                    arrayOrStructureInitializer =
                            new ArrayOrStructureInitializer(ctx, initElementList);
                } else {
                    arrayOrStructureInitializer = null;
                }
            }

            addArrayDescriptor(new ArrayDescriptor(((TypeArray) m_type).getNoOfDimensions(),
                    ((TypeArray) m_type).getDimensions()));

            for (int i = 0; i < identifierDenotationList.size(); i++) {
                VariableEntry variableEntry = new VariableEntry(identifierDenotationList.get(i),
                        m_type, hasAssigmentProtection, ctx, arrayOrStructureInitializer);

                String s = identifierDenotationList.get(i).toString();
                checkDoubleDefinitionAndEnterToSymbolTable(variableEntry, ctx, s);
            }
        }

        return null;
    }

    //    @Override
    //    public Void visitDimensionAttribute(SmallPearlParser.DimensionAttributeContext ctx) {
    //        Log.debug("SymbolTableVisitor:visitDimensionAttribute:ctx" + CommonUtils.printContext(ctx));
    //        visitChildren(ctx);
    //        return null;
    //    }

    @Override
    public Void visitBoundaryDenotation(SmallPearlParser.BoundaryDenotationContext ctx) {

        TypeArray ta = new TypeArray();
        ta.setBaseType(m_type);

        if (ctx.constantFixedExpression().size() == 1) {
            int upb = CommonUtils.getConstantFixedExpression(ctx.constantFixedExpression(0),
                    m_currentSymbolTable);

            ta.addDimension(new ArrayDimension(Defaults.DEFAULT_ARRAY_LWB, upb, ctx));
        } else {
            int lwb = CommonUtils.getConstantFixedExpression(ctx.constantFixedExpression(0),
                    m_currentSymbolTable);
            int upb = CommonUtils.getConstantFixedExpression(ctx.constantFixedExpression(1),
                    m_currentSymbolTable);
            ta.addDimension(new ArrayDimension(lwb, upb, ctx));
        }
        m_type = ta;
        return null;
    }

    @Override
    public Void visitTypeAttributeForArray(SmallPearlParser.TypeAttributeForArrayContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeAttributeForArray:ctx"
                + CommonUtils.printContext(ctx));

        TypeDefinition tempType = m_type;
        visitChildren(ctx);

        ((TypeArray) tempType).setBaseType(m_type);
        m_type = tempType;

        return null;
    }


    @Override
    public Void visitSemaDenotation(SmallPearlParser.SemaDenotationContext ctx) {
        Log.debug("SymbolTableVisitor:visitSemaDenotation:ctx" + CommonUtils.printContext(ctx));

        m_type = new TypeSemaphore();

        visitChildren(ctx);

        return null;
    }

    @Override
    public Void visitStatement(SmallPearlParser.StatementContext ctx) {
        Log.debug("SymbolTableVisitor:visitStatement:ctx" + CommonUtils.printContext(ctx));

        if (ctx != null) {
            if (ctx.label_statement() != null) {
                for (int i = 0; i < ctx.label_statement().size(); i++) {
                    LabelEntry entry = new LabelEntry(ctx.label_statement(i).ID().getText(),
                            ctx.label_statement(i));

                    if (!m_currentSymbolTable.enter(entry)) {
                        System.out.println("ERR: Double definition of "
                                + ctx.label_statement(i).ID().getText());
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
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        Log.debug("SymbolTableVisitor:visitLoopStatement:ctx" + CommonUtils.printContext(ctx));

        String label = null;

        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor: visitLoopStatement");
        }

        if (ctx.loopStatement_end().ID() != null) {
            label = ctx.loopStatement_end().ID().getText();

            SymbolTableEntry se = m_currentSymbolTable.lookup(label);
            if (se != null) {
                ErrorStack.add(ctx.loopStatement_end(), "LOOP",
                        "duplicate name '" + label + "' in scope");

                if (se instanceof LoopEntry) {
                    ErrorStack.note(((LoopStatementContext) (se.getCtx())).loopStatement_end(),
                            "previous definion", "");
                }
                if (se instanceof BlockEntry) {
                    ErrorStack.note(se.getCtx(), "previous definion", "");
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
                    new VariableEntry(ctx.loopStatement_for().ID().getText(),
                            new TypeFixed(Defaults.FIXED_LENGTH), true, null, null);
            controlVariable.setLoopControlVariable();
            m_currentSymbolTable.enter(controlVariable);
        }

        for (int i = 0; i < ctx.loopBody().variableDeclaration().size(); i++) {
            visitVariableDeclaration(ctx.loopBody().variableDeclaration(i));
        }

        //        for (int i = 0; i < ctx.loopBody().arrayVariableDeclaration().size(); i++) {
        //            visitArrayVariableDeclaration(ctx.loopBody().arrayVariableDeclaration(i));
        //        }
        //
        //        for (int i = 0; i < ctx.loopBody().structVariableDeclaration().size(); i++) {
        //            visitStructVariableDeclaration(ctx.loopBody().structVariableDeclaration(i));
        //        }

        for (int i = 0; i < ctx.loopBody().statement().size(); i++) {
            SmallPearlParser.StatementContext stmt = ctx.loopBody().statement(i);
            visit(ctx.loopBody().statement(i));
            //            if (stmt.block_statement() != null) {
            //                visitBlock_statement(stmt.block_statement());
            //            } else if (stmt.unlabeled_statement() != null) {
            //                visitUnlabeled_statement(stmt.unlabeled_statement());
            //            }
        }

        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBoltDenotation(SmallPearlParser.BoltDenotationContext ctx) {
        Log.debug("SymbolTableVisitor:visitBoltDenotation:ctx" + CommonUtils.printContext(ctx));
        visitChildren(ctx);

        m_type = new TypeBolt();

        //        for (int i = 0; i < identifierDenotationList.size(); i++) {
        //            String s = identifierDenotationList.get(i);
        //            VariableEntry variableEntry = new BoltEntry(s, ctx);
        //            checkDoubleDefinitionAndEnterToSymbolTable(variableEntry, ctx, s);
        //
        //        }

        return null;
    }


    public SymbolTable getSymbolTablePerContext(ParseTree ctx) {
        return m_symboltablePerContext.get(ctx);
    }

    @Override
    public Void visitDationDenotation(SmallPearlParser.DationDenotationContext ctx) {
        // the TypeDation may have lot of parameters depending on user/system dation
        // so we just create an object and set the attributes while scanning the context

        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor: visitDationDenotation");
        }

        ErrorStack.enter(ctx, "DationDCL");

        visitTypeDation(ctx.typeDation());

        TypeDation d = (TypeDation) m_type;
        d.setIsDeclaration(true);
        //treatIdentifierDenotation(ctx.identifierDenotation(), d);

        if (ctx.globalAttribute() != null) {
            treatGlobalAttribute(ctx.globalAttribute(), d);
        }

        // get CREATED parameter
        d.setCreatedOn(ctx.ID().getText());

        //
        ErrorStack.leave();
        return null;
    }

    /* -------------------------------------------------------------------  */
    /* START of common for dationDeclaration and dationSpecification 	    */
    /* -------------------------------------------------------------------  */
    private void treatIdentifierDenotation(IdentifierDenotationContext ctx, TypeDation d) {
        for (int i = 0; i < ctx.identifier().size(); i++) {
            String dationName = ctx.identifier(i).ID().toString();
            // System.out.println("DationName: "+ dationName);

            VariableEntry variableEntry = new VariableEntry(dationName, d, ctx);
            checkDoubleDefinitionAndEnterToSymbolTable(variableEntry, ctx, dationName);

        }
    }

    int treatDimension(String s) {
        if (s.equals("*")) {
            return 0; // '*'
        } else {
            int nbr = Integer.parseInt(s);
            if (nbr <= 0) {
                ErrorStack.add("dimension range must be > 0");
            }
            return (nbr);
        }
    }

    void treatGlobalAttribute(GlobalAttributeContext ctx, TypeDation d) {
        if (ctx.ID() != null) {
            d.setGlobal(ctx.ID().getText());
        }
    }
    /* -------------------------------------------------------------------  */
    /* END of common for dationDeclaration and dationSpecification 			*/
    /* -------------------------------------------------------------------  */

    @Override
    public Void visitLengthDefinition(SmallPearlParser.LengthDefinitionContext ctx) {
        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor: visitLengthDefinition");
        }

        if (ctx.lengthDefinitionType() instanceof SmallPearlParser.LengthDefinitionFixedTypeContext) {
            TypeFixed typ =
                    new TypeFixed(Integer.valueOf((ctx.length().IntegerConstant().toString())));

            if (typ.getPrecision() < Defaults.FIXED_MIN_LENGTH
                    || typ.getPrecision() > Defaults.FIXED_MAX_LENGTH) {
                CommonErrorMessages.wrongFixedPrecission(ctx.lengthDefinitionType());
            }

            LengthEntry entry = new LengthEntry(typ, ctx);
            m_currentSymbolTable.enterOrReplace(entry);
        } else if (ctx
                .lengthDefinitionType() instanceof SmallPearlParser.LengthDefinitionFloatTypeContext) {
            TypeFloat typ =
                    new TypeFloat(Integer.valueOf((ctx.length().IntegerConstant().toString())));

            if (typ.getPrecision() != Defaults.FLOAT_SHORT_PRECISION
                    && typ.getPrecision() != Defaults.FLOAT_LONG_PRECISION) {
                CommonErrorMessages.wrongFloatPrecission(ctx.length());
            }

            LengthEntry entry = new LengthEntry(typ, ctx);
            m_currentSymbolTable.enterOrReplace(entry);
        } else if (ctx
                .lengthDefinitionType() instanceof SmallPearlParser.LengthDefinitionBitTypeContext) {
            TypeBit typ = new TypeBit(Integer.valueOf((ctx.length().IntegerConstant().toString())));
            LengthEntry entry = new LengthEntry(typ, ctx);
            if (typ.getPrecision() < Defaults.BIT_MIN_LENGTH
                    || typ.getPrecision() > Defaults.BIT_MAX_LENGTH) {
                CommonErrorMessages.wrongBitLength(ctx.length());
            } else {
                m_currentSymbolTable.enterOrReplace(entry);
            }
        } else if (ctx
                .lengthDefinitionType() instanceof SmallPearlParser.LengthDefinitionCharacterTypeContext) {
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

    private ArrayList<Initializer> getInitialisationAttribute(
            SmallPearlParser.InitialisationAttributeContext ctx) {
        Log.debug("SymbolTableVisitor:getInitialisationAttribute:ctx"
                + CommonUtils.printContext(ctx));

        ArrayList<Initializer> initElementList = new ArrayList<>();

        if (ctx != null) {
            for (int i = 0; i < ctx.initElement().size(); i++) {
                ConstantValue constant = getInitElement(i, ctx.initElement(i));

                SimpleInitializer initializer = new SimpleInitializer(ctx.initElement(i), constant);
                initElementList.add(initializer);
                // TODO: MERGE                Initializer initializer = new
                // Initializer(ctx.initElement(i),
                // constant);
                //                initElementList.add(initializer);
            }
        }

        if (initElementList.size() > 0) {
            return initElementList;
        } else {
            return null;
        }
    }

    private ConstantValue getInitElement(int index, SmallPearlParser.InitElementContext ctx) {
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
                ErrorStack.add(ctx, "initializer " + ctx.identifier().getText(),
                        "is not defined yet");
                //                throw new UnknownIdentifierException(
                //                        ctx.getText(),
                //                        ctx.start.getLine(),
                //                        ctx.start.getCharPositionInLine(),
                //                        (ctx.ID().toString()));
            } else {
                if (entry instanceof VariableEntry) {

                    VariableEntry var = (VariableEntry) entry;

                    if (var.getAssigmentProtection()) {
                        if (var.getInitializer() instanceof SimpleInitializer) {
                            constant = ((SimpleInitializer) var.getInitializer()).getConstant();
                        } else {
                            throw new InternalCompilerErrorException(ctx.getText(),
                                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
                        }
                    } else {
                        ErrorStack.add(ctx, "initializer", "must be INV");
                        //                    throw new TypeMismatchException(
                        //                            ctx.getText(),
                        //                            ctx.start.getLine(),
                        //                            ctx.start.getCharPositionInLine(),
                        //                            (ctx.ID().toString()));
                    }
                } else {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                            ctx.start.getCharPositionInLine());
                }
            }
        }

        constant = m_constantPool.add(constant);
        return constant;
    }

    private int getPrecisionByType(int index, TypeDefinition type) {
        int precision = 0;

        if (type instanceof TypeFixed) {
            precision = type.getPrecision();
        } else if (type instanceof TypeFloat) {
            precision = type.getPrecision();
        } else if (type instanceof TypeBit) {
            precision = type.getPrecision();
        } else if (type instanceof TypeClock) {
            precision = 0;
        } else if (type instanceof TypeArray) {
            TypeArray arrType = (TypeArray) type;
            precision = getPrecisionByType(index, arrType.getBaseType());
        } else if (type instanceof TypeStructure) {
            TypeStructure structType = (TypeStructure) type;
            precision = getPrecisionByType(index,
                    structType.getStructureComponentByIndex(index).m_type);
        } else {
            throw new InternalCompilerErrorException("Cannot determine lenght of type");
        }

        return precision;
    }

    private ConstantValue getConstant(SmallPearlParser.ConstantContext ctx) {
        ConstantValue constant = null;
        int sign = 1;

        if (ctx.sign() != null) {
            if (ctx.sign() instanceof SmallPearlParser.SignMinusContext) {
                sign = -1;
            }
        }

        //        if ( ctx.stringConstant() != null) {
        //            constant = new
        // ConstantCharacterValue(ctx.stringConstant().StringLiteral().toString());
        //        }
        //        else
        if (ctx.fixedConstant() != null) {
            long curval = sign * Long.parseLong(ctx.fixedConstant().IntegerConstant().toString());
            int curlen = m_currentSymbolTable.lookupDefaultFixedLength();

            if (ctx.fixedConstant().fixedNumberPrecision() != null) {
                curlen = Integer.parseInt(
                        ctx.fixedConstant().fixedNumberPrecision().IntegerConstant().toString());
            } else {
                curlen = getPrecisionByType(0, m_type);
            }

            constant = new ConstantFixedValue(curval, curlen);
        } else if (ctx.floatingPointConstant() != null) {
            double curval =
                    sign * CommonUtils.getFloatingPointConstantValue(ctx.floatingPointConstant());
            int curlen = 0;

            if (m_type instanceof TypeArray) {
                curlen = ((TypeArray) m_type).getBaseType().getPrecision();
            } else {
                curlen = m_type.getPrecision();
            }

            constant = new ConstantFloatValue(curval, curlen);
        } else if (ctx.durationConstant() != null) {
            Integer hours = 0;
            Integer minutes = 0;
            Double seconds = 0.0;

            if (ctx.durationConstant().hours() != null) {
                hours = Integer
                        .valueOf(ctx.durationConstant().hours().IntegerConstant().toString());
            }
            if (ctx.durationConstant().minutes() != null) {
                minutes = Integer
                        .valueOf(ctx.durationConstant().minutes().IntegerConstant().toString());
            }
            if (ctx.durationConstant().seconds() != null) {
                if (ctx.durationConstant().seconds().IntegerConstant() != null) {
                    seconds = Double
                            .valueOf(ctx.durationConstant().seconds().IntegerConstant().toString());
                } else if (ctx.durationConstant().seconds().floatingPointConstant() != null) {
                    seconds = CommonUtils.getFloatingPointConstantValue(
                            ctx.durationConstant().seconds().floatingPointConstant());
                }
            }

            constant = new ConstantDurationValue(hours, minutes, seconds);

        } else if (ctx.bitStringConstant() != null) {
            long value = CommonUtils
                    .convertBitStringToLong(ctx.bitStringConstant().BitStringLiteral().getText());
            int nb = CommonUtils
                    .getBitStringLength(ctx.bitStringConstant().BitStringLiteral().getText());
            constant = new ConstantBitValue(value, nb);
        } else if (ctx.timeConstant() != null) {
            Integer hours = 0;
            Integer minutes = 0;
            Double seconds = 0.0;

            hours = Integer.valueOf(ctx.timeConstant().IntegerConstant(0).toString());
            minutes = Integer.valueOf(ctx.timeConstant().IntegerConstant(1).toString());

            if (ctx.timeConstant().floatingPointConstant() != null) {
                seconds = CommonUtils
                        .getFloatingPointConstantValue(ctx.timeConstant().floatingPointConstant());
            } else if (ctx.timeConstant().IntegerConstant(2) != null) {
                seconds = Double.valueOf(ctx.timeConstant().IntegerConstant(2).toString());
            } else {
                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
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

    private ConstantValue getConstantExpression(SmallPearlParser.ConstantExpressionContext ctx) {
        Log.debug("SymbolTableVisitor:getConstantExpression:ctx" + CommonUtils.printContext(ctx));

        ConstantFixedExpressionEvaluator evaluator = new ConstantFixedExpressionEvaluator(m_verbose,
                m_debug, m_currentSymbolTable, null, null);
        ConstantValue constant = evaluator.visit(ctx.constantFixedExpression());

        return constant;
    }


    @Override
    public Void visitDationSpecification(SmallPearlParser.DationSpecificationContext ctx) {
        LinkedList<ModuleEntry> listOfModules = this.symbolTable.getModules();

        Log.debug(
                "SymbolTableVisitor:visitDationSpecification:ctx" + CommonUtils.printContext(ctx));

        if (listOfModules.size() > 1) {
            throw new NotYetImplementedException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        ModuleEntry moduleEntry = listOfModules.get(0);
        SymbolTable symbolTable = moduleEntry.scope;

        /* ---------------- */

        if (m_verbose > 0) {
            System.out.println("SymbolTableVisitor: visitDationSpecification");
        }

        ErrorStack.enter(ctx, "DationSPC");

        visitTypeDation(ctx.typeDation());
        TypeDation d = (TypeDation) m_type;
        d.setIsDeclaration(false);

        treatIdentifierDenotation(ctx.identifierDenotation(), d);

        if (ctx.globalAttribute() != null) {
            treatGlobalAttribute(ctx.globalAttribute(), d);
        }

        //
        ErrorStack.leave();

        /* ---------------- */
        return null;
    }

    /**
     * add specified interrupts to the symbol table maybe this would be better placed in
     * SymbolTableVisitor
     */
    @Override
    public Void visitInterruptSpecification(SmallPearlParser.InterruptSpecificationContext ctx) {

        boolean isGlobal = false;

        if (m_verbose > 0) {
            System.out.println("Semantic: Check RT-statements: visitInterruptSpecification");
        }

        if (ctx.globalAttribute() != null) {
            isGlobal = true;
        }

        for (int i = 0; i < ctx.identifierDenotation().identifier().size(); i++) {
            String iName = ctx.identifierDenotation().identifier(i).ID().toString();
            InterruptEntry ie = new InterruptEntry(iName, isGlobal, ctx);
            checkDoubleDefinitionAndEnterToSymbolTable(ie, ctx, iName);
        }
        return null;
    }


    //    @Override
    //    public Void visitStructureDenotation(SmallPearlParser.StructureDenotationContext ctx) {
    //        Log.debug(
    //                "SymbolTableVisitor:visitStructureDenotation:ctx" + CommonUtils.printContext(ctx));
    //        boolean hasAssignmentProtection = false;
    //
    //        ArrayList<Initializer> initElementList = null;
    //
    //
    //        m_typeStructure = null;
    //        visitTypeStructure(ctx.typeStructure());
    //    
    //        m_type = m_typeStructure;
    //            
    //
    ////        if (ctx.initialisationAttribute() != null) {
    ////            initElementList = getInitialisationAttribute(ctx.initialisationAttribute());
    ////        }
    ////
    ////        ArrayOrStructureInitializer arrayOrStructureInitializer = null;
    ////
    ////        if (initElementList != null && initElementList.size() > 0) {
    ////            arrayOrStructureInitializer = new ArrayOrStructureInitializer(ctx, initElementList);
    ////        } else {
    ////            arrayOrStructureInitializer = null;
    ////        }
    //
    //     //   int no = symbolTable.getNumberOfComponents(m_type);
    //   //     hasAssignmentProtection = ctx.assignmentProtection() != null;
    ////
    ////        VariableEntry variableEntry = new VariableEntry(null, m_type,
    ////                hasAssignmentProtection, ctx);
    //
    ////        String s = ctx.ID().toString();
    ////        checkDoubleDefinitionAndEnterToSymbolTable(variableEntry, ctx, s);
    //
    //        return null;
    //    }

    @Override
    public Void visitTypeStructure(SmallPearlParser.TypeStructureContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeStructure:ctx" + CommonUtils.printContext(ctx));

        m_typeStructure = new TypeStructure();


        for (int i = 0; i < ctx.structureComponent().size(); i++) {
            visitStructureComponent(ctx.structureComponent(i));
        }

        m_type = m_typeStructure;


        return null;
    }

    @Override
    public Void visitStructureComponent(SmallPearlParser.StructureComponentContext ctx) {
        Log.debug("SymbolTableVisitor:visitStructureComponent:ctx" + CommonUtils.printContext(ctx));
        TypeDefinition type_saved = m_type;
        TypeArray array = null;

        StructureComponent component = null;

        TypeStructure saved_typeStructure = m_typeStructure;

        //        if (ctx.dimensionAttribute() != null) {
        //            Log.debug("SymbolTableVisitor:visitStructureComponent: ARRAY");
        //            array = new TypeArray();
        //            m_type = array;
        //            visitDimensionAttribute(ctx.dimensionAttribute());
        //            addArrayDescriptor(
        //                    new ArrayDescriptor(array.getNoOfDimensions(), array.getDimensions()));
        //        }

        if (ctx.typeAttributeInStructureComponent() != null) {
            type_saved = m_type;
            visitTypeAttributeInStructureComponent(ctx.typeAttributeInStructureComponent());
            m_typeStructure = saved_typeStructure;

            for (int i = 0; i < ctx.ID().size(); i++) {
                component = new StructureComponent();

                if (ctx.dimensionAttribute() != null) {
                    visitDimensionAttribute(ctx.dimensionAttribute());
                }
                component.m_type = m_type;


                component.m_id = ctx.ID(i).getText();
                saved_typeStructure.add(component);
            }
            m_type = m_typeStructure;
        }

        return null;
    }

    @Override
    public Void visitTypeAttributeInStructureComponent(
            SmallPearlParser.TypeAttributeInStructureComponentContext ctx) {
        Log.debug("SymbolTableVisitor:visitTypeAttributeInStructureComponent:ctx"
                + CommonUtils.printContext(ctx));

        if (ctx.simpleType() != null) {
            visitSimpleType(ctx.simpleType());
        } else if (ctx.structuredType() != null) {
            visitStructuredType(ctx.structuredType());
        } else if (ctx.typeReference() != null) {
            visitTypeReference(ctx.typeReference());
        } else {
            ErrorStack.addInternal(ctx,
                    "SymbolTableVisitor::visitTypeAttributeInStructureComponent",
                    "missing alternative");
        }

        return null;
    }

    //    @Override
    //    public Void visitStructuredType(SmallPearlParser.StructuredTypeContext ctx) {
    //        Log.debug("SymbolTableVisitor:visitStructuredType:ctx" + CommonUtils.printContext(ctx));
    //        visitChildren(ctx);
    //        return null;
    //    }

    private void checkDoubleDefinitionAndEnterToSymbolTable(SymbolTableEntry newEntry,
            ParserRuleContext ctx, String msg) {
        String s = newEntry.getName();
        SymbolTableEntry entry = m_currentSymbolTable.lookupLocal(s);
        if (entry != null) {
            CommonErrorMessages.doubleDeclarationError(s, ctx, entry.getCtx());
        } else {
            entry = m_currentSymbolTable.lookup(s);
            if (entry != null) {
                CommonErrorMessages.doubleDeclarationWarning("double declaration of ", s, ctx,
                        entry.getCtx());
            }
            m_currentSymbolTable.enter(newEntry);
        }
    }

}
