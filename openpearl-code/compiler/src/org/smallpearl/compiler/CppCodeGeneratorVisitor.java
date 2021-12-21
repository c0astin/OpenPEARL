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

package org.smallpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SmallPearlParser.*;
import org.smallpearl.compiler.SymbolTable.*;
import org.smallpearl.compiler.Graph.Graph;
import org.smallpearl.compiler.Graph.Node;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.*;

/**
 * @author Marcel Schaible
 * @version 1
 *     <p><b>Description</b>
 */

public class CppCodeGeneratorVisitor extends SmallPearlBaseVisitor<ST>
implements SmallPearlVisitor<ST> {

    private STGroup m_group;
    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private ConstantExpressionEvaluatorVisitor m_constantExpressionEvaluatorVisitor;
    private SymbolTableVisitor m_symbolTableVisitor;
    private boolean m_map_to_const = true;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ModuleEntry m_module;
    private int m_sign = 1;
    private AST m_ast = null;
    private boolean m_isNonStatic; // flag to check if an format list contains non static elements
    private TypeDefinition m_resultType; // result type of a PROC; required if a variable character string is returned
    private Vector<ST> m_tempVariableList; // variable character values must be assigned
    private Vector<Integer> m_tempVariableNbr; // to a temporary variable if use as proc parameter
    private LinkedList<LinkedList<String>> m_listOfSemaphoreArrays;
    private ST constantSemaphoreArrays;
    private LinkedList<LinkedList<String>> m_listOfBoltArrays;
    private ST constantBoltArrays;
    private ST m_dationDeclarations;
    private ST m_dationDeclarationInitializers;
    private ST m_dationSpecificationInitializers;
    private boolean m_useNamespaceForGlobals;
    private String m_thisNamespace;
    public enum Type {
        BIT, CHAR, FIXED
    }
    private LinkedList<FormalParameter> m_formalParameters;

    public CppCodeGeneratorVisitor(String sourceFileName, String filename, int verbose,
            boolean debug, SymbolTableVisitor symbolTableVisitor,
            ExpressionTypeVisitor expressionTypeVisitor,
            ConstantExpressionEvaluatorVisitor constantExpressionEvaluatorVisitor, AST ast, 
            boolean useNamespaceForGlobals) {

        
        m_debug = debug;
        m_verbose = verbose;
        m_useNamespaceForGlobals = useNamespaceForGlobals;
        m_sourceFileName = sourceFileName;
        m_symbolTableVisitor = symbolTableVisitor;
        m_constantExpressionEvaluatorVisitor = constantExpressionEvaluatorVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_listOfSemaphoreArrays = new LinkedList<LinkedList<String>>();
        m_listOfBoltArrays = new LinkedList<LinkedList<String>>();
        m_ast = ast;
        m_tempVariableList = new Vector<ST>();
        m_tempVariableNbr = new Vector<Integer>();


        LinkedList<ModuleEntry> listOfModules = this.m_currentSymbolTable.getModules();

        if (listOfModules.size() > 1) {
            throw new NotYetImplementedException("Multiple modules", 0, 0);
        }

        m_module = listOfModules.get(0);

        if (m_verbose > 0) {
            System.out.println("Generating Cpp code");
        }

        this.ReadTemplate(filename);
        constantSemaphoreArrays = m_group.getInstanceOf("SemaphoreArrays");
        constantBoltArrays = m_group.getInstanceOf("BoltArrays");
        m_dationDeclarationInitializers = m_group.getInstanceOf("DationDeclarationInitialiers");
        m_dationSpecificationInitializers =
                m_group.getInstanceOf("DationSpecificationInitialisiers");
        m_dationDeclarations = m_group.getInstanceOf("DationDeclarations");

        // generateProlog is invoked via visitModule!!
        generatePrologue();
    }

    private Void ReadTemplate(String filename) {
        if (m_verbose > 0) {
            System.out.println("Read StringTemplate Group File: " + filename);
        }

        this.m_group = new STGroupFile(filename);

        return null;
    }

    private ST generatePrologue() {
        ST prologue = m_group.getInstanceOf("Prologue");

        prologue.add("src", this.m_sourceFileName);
        if (m_useNamespaceForGlobals) {
           prologue.add("name",this.m_module.getName());
        } else {
            prologue.add("name","pearlApp");
        }

        ST taskspec = m_group.getInstanceOf("TaskSpecifier");

        // generate task forward specifications
        LinkedList<TaskEntry> taskEntries = this.m_module.scope.getTaskDeclarationsAndSpecifications();
        
        
        ArrayList<String> listOfTaskNames = new ArrayList<String>();

        for (int i = 0; i < taskEntries.size(); i++) {
            ST task = m_group.getInstanceOf("TaskSpecifierEntry");
            TaskEntry te = taskEntries.get(i);
            task.add("taskname", te.getName());
            ST scope = null;
            // we need 'extern' if we are in the same namespace
            // and 
             scope = m_group.getInstanceOf("externVariable");
             if (te.getGlobalAttribute()!= null && !te.getGlobalAttribute().equals(m_module.getName())) {
                 // namespace switch only required of namespace changes to another module
                 scope.add("fromNs", te.getGlobalAttribute());
                 if (m_useNamespaceForGlobals) {
                    scope.add("currentNs",m_module.getName());
                 } else {
                     scope.add("currentNs","pearlApp");
                 }
            }
            
            scope.add("variable", task);
            taskspec.add("taskname", scope);

//            listOfTaskNames.add(taskEntries.get(i).getName());
        }

   //     taskspec.add("taskname", listOfTaskNames);
        prologue.add("taskSpecifierList", taskspec);
        prologue.add("ConstantPoolList", generateConstantPool());

        if (m_module.scope.usesSystemElements()) {
            prologue.add("useSystemElements", true);
        }

        prologue.add("StructureForwardDeclarationList", generateStructureForwardDeclarationList());
        prologue.add("StructureDeclarationList", generateStructureDeclarationList());

        return prologue;
    }

    private ST generateConstantPool() {
        Log.debug("CppCodeGeneratorVisitor:generateConstantPool:");

        ST pool = m_group.getInstanceOf("ConstantPoolList");

        for (int i = 0; i < ConstantPool.constantPool.size(); i++) {
            if (ConstantPool.constantPool.get(i) instanceof ConstantFixedValue) {
                ST entry = m_group.getInstanceOf("ConstantPoolEntry");
                entry.add("name", ConstantPool.constantPool.get(i).toString());
                entry.add("type",
                        ((ConstantFixedValue) ConstantPool.constantPool.get(i)).getBaseType());
                entry.add("precision",
                        ((ConstantFixedValue) ConstantPool.constantPool.get(i)).getPrecision());
                entry.add("value",
                        ((ConstantFixedValue) ConstantPool.constantPool.get(i)).getValue());
                pool.add("constants", entry);
            }
        }

        for (int i = 0; i < ConstantPool.constantPool.size(); i++) {
            if (ConstantPool.constantPool.get(i) instanceof ConstantFloatValue) {
                ST entry = m_group.getInstanceOf("ConstantPoolEntry");
                entry.add("name", ConstantPool.constantPool.get(i).toString());
                entry.add("type",
                        ((ConstantFloatValue) ConstantPool.constantPool.get(i)).getBaseType());
                entry.add("precision",
                        ((ConstantFloatValue) ConstantPool.constantPool.get(i)).getPrecision());
                entry.add("value",
                        ((ConstantFloatValue) ConstantPool.constantPool.get(i)).getValue());
                pool.add("constants", entry);
            }
        }

        for (int i = 0; i < ConstantPool.constantPool.size(); i++) {
            if (ConstantPool.constantPool.get(i) instanceof ConstantCharacterValue) {
                ConstantCharacterValue value =
                        (ConstantCharacterValue) ConstantPool.constantPool.get(i);
                ST entry = m_group.getInstanceOf("ConstantPoolCharacterEntry");
                entry.add("name", value.toString());
                entry.add("type", value.getBaseType());
                entry.add("length", value.getLength());

                // we must quote the backslash and the double quote
                String s = value.getValue();
                s = CommonUtils.convertPearl2CString(s);
                entry.add("value", s);

                pool.add("constants", entry);
            }
        }

        for (int i = 0; i < ConstantPool.constantPool.size(); i++) {
            if (ConstantPool.constantPool.get(i) instanceof ConstantBitValue) {
                ConstantBitValue value = (ConstantBitValue) ConstantPool.constantPool.get(i);
                ST entry = m_group.getInstanceOf("ConstantPoolBitEntry");
                entry.add("name", value.toString());
                entry.add("type", value.getBaseType());
                entry.add("length", value.getLength());
                entry.add("value", value.getValue());
                pool.add("constants", entry);
            }
        }

        for (int i = 0; i < ConstantPool.constantPool.size(); i++) {
            if (ConstantPool.constantPool.get(i) instanceof ConstantDurationValue) {
                ConstantDurationValue value =
                        (ConstantDurationValue) ConstantPool.constantPool.get(i);
                ST entry = m_group.getInstanceOf("ConstantPoolDurationEntry");
                entry.add("name", value.toString());
                entry.add("type", value.getBaseType());
                entry.add("valueSec", value.getValueSec());
                entry.add("valueUsec", value.getValueUsec());
                entry.add("valueSign", value.getSign());
                pool.add("constants", entry);
            }
        }

        for (int i = 0; i < ConstantPool.constantPool.size(); i++) {
            if (ConstantPool.constantPool.get(i) instanceof ConstantClockValue) {
                ConstantClockValue value = (ConstantClockValue) ConstantPool.constantPool.get(i);
                ST entry = m_group.getInstanceOf("ConstantPoolClockEntry");
                entry.add("name", value.toString());
                entry.add("type", value.getBaseType());
                entry.add("value", value.getValue());

                pool.add("constants", entry);
            }
        }
        for (int i = 0; i < ConstantPool.constantPool.size(); i++) {
            if (ConstantPool.constantPool.get(i) instanceof ConstantNILReference) {
                ConstantNILReference value =
                        (ConstantNILReference) ConstantPool.constantPool.get(i);
                ST entry = m_group.getInstanceOf("ConstantPoolNILReferenceEntry");
                entry.add("name", value.toString());
                pool.add("constants", entry);
            }
        }

        return pool;
    }


    private ST generateStructureForwardDeclarationList() {
        Log.debug("CppCodeGeneratorVisitor:generateStructureForwardDeclarationList:");

        ST decls = m_group.getInstanceOf("StructureForwardDeclarationList");

        HashMap<String, TypeStructure> structureDecls = m_symboltable.getStructureDeclarations();

        for (String name : structureDecls.keySet()) {
            ST decl = m_group.getInstanceOf("StructureForwardDeclaration");
            decl.add("name", name);
            decls.add("declarations", decl);
        }

        return decls;
    }

    private ST generateStructureDeclarationList() {
        Log.debug("CppCodeGeneratorVisitor:generateStructureDeclarationList:");
        ST decls = m_group.getInstanceOf("StructureDeclarationList");

        HashMap<String, TypeStructure> structureDecls = m_symboltable.getStructureDeclarations();

        // First create a dependency graph for the structure definitions:
        Graph<String> graph = new Graph<>();

        for (String name : structureDecls.keySet()) {
            TypeStructure struct = structureDecls.get(name);
            graph.addDependency(name, null);

            for (int i = 0; i < struct.m_listOfComponents.size(); i++) {
                StructureComponent component = struct.m_listOfComponents.get(i);
                if (component.m_type instanceof TypeStructure) {
                    TypeStructure innerStruct = (TypeStructure) component.m_type;
                    graph.addDependency(name, innerStruct.getStructureName());
                } else if (component.m_type instanceof TypeArray) {
                    TypeArray array = (TypeArray) component.m_type;

                    if (array.getBaseType() instanceof TypeStructure) {
                        TypeStructure innerStruct = (TypeStructure) array.getBaseType();
                        graph.addDependency(name, innerStruct.getStructureName());
                    }
                }
            }
        }

        // Calculate the dependencies:
        List<Node<String>> nodeList = graph.generateDependencies();

        if (nodeList != null) {
            for (int i = 0; i < nodeList.size(); i++) {
                Node<String> node = nodeList.get(i);

                if (node.m_value != null) {
                    TypeStructure struct = structureDecls.get(node.m_value);
                    ST decl = m_group.getInstanceOf("StructureDefinition");
                    decl.add("name", struct.getStructureName());

                    for (int j = 0; j < struct.m_listOfComponents.size(); j++) {
                        ST stComponent = m_group.getInstanceOf("StructComponentDeclaration");

                        StructureComponent component = struct.m_listOfComponents.get(j);

                        if (component.m_type instanceof TypeStructure) {
                            stComponent.add("name", component.m_alias);
                            stComponent.add("TypeAttribute",
                                    ((TypeStructure) component.m_type).getStructureName());
                            decl.add("components", stComponent);
                        } else if (component.m_type instanceof TypeArray) {
                            TypeArray array = (TypeArray) component.m_type;

                            if (array.getBaseType() instanceof TypeStructure) {
                                TypeStructure arraystruct = (TypeStructure) array.getBaseType();
                                ST declaration =
                                        m_group.getInstanceOf("StructureArrayComponentDeclaration");

                                declaration.add("name", component.m_alias);
                                declaration.add("type", arraystruct.getStructureName());
                                declaration.add("totalNoOfElements", array.getTotalNoOfElements());

                                stComponent.add("TypeAttribute", declaration);
                                decl.add("components", stComponent);
                            } else {
                                // simple type:
                                TypeDefinition type = array.getBaseType();
                                ST declaration =
                                        m_group.getInstanceOf("StructureArrayComponentDeclaration");
                                ST typeST = null;

                                // TODO: (MS) Are here the other types like e.g. CLOCK are missing???
                                if (type instanceof TypeFixed) {
                                    typeST = m_group.getInstanceOf("fixed_type");
                                    typeST.add("size", type.getPrecision());
                                } else if (type instanceof TypeFloat) {
                                    typeST = m_group.getInstanceOf("float_type");
                                    typeST.add("size", type.getPrecision());
                                } else if (type instanceof TypeChar) {
                                    typeST = m_group.getInstanceOf("char_type");
                                    typeST.add("size", type.getSize());
                                }

                                declaration.add("name", component.m_alias);
                                declaration.add("type", typeST);
                                declaration.add("totalNoOfElements", array.getTotalNoOfElements());

                                stComponent.add("TypeAttribute", declaration);
                                decl.add("components", stComponent);
                            }
                        } else {
                            stComponent.add("name", component.m_alias);
                            stComponent.add("TypeAttribute", component.m_type.toST(m_group));
                            decl.add("components", stComponent);
                        }

                    }

                    decls.add("declarations", decl);
                }
            }
        }

        return decls;
    }


 
    /*
     * Time/Clock example: 11:30:00 means 11.30 15:45:3.5 means 15.45 and 3.5
     * seconds 25:00:00 means 1.00
     */
    private Double getTime(SmallPearlParser.TimeConstantContext ctx) {
        Integer hours = 0;
        Integer minutes = 0;
        Double seconds = 0.0;

        hours = (Integer.valueOf(ctx.IntegerConstant(0).toString()) % 24);
        minutes = Integer.valueOf(ctx.IntegerConstant(1).toString());

        if (ctx.IntegerConstant().size() == 3) {
            seconds = Double.valueOf(ctx.IntegerConstant(2).toString());
        }

        if (ctx.floatingPointConstant() != null) {
            seconds = CommonUtils.getFloatingPointConstantValue(ctx.floatingPointConstant());
        }

        if (hours < 0 || minutes < 0 || minutes > 59) {
            throw new NotSupportedTypeException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        return hours * 3600 + minutes * 60 + seconds;
    }

    @Override
    public ST visitModule(SmallPearlParser.ModuleContext ctx) {
        ST module = m_group.getInstanceOf("module");

        module.add("src", this.m_sourceFileName);
        if (m_useNamespaceForGlobals) {
            m_thisNamespace = ctx.nameOfModuleTaskProc().ID().getText();
        } else {
            m_thisNamespace = "pearlApp";
        }

        module.add("name", m_thisNamespace);

        module.add("prologue", generatePrologue());

        org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable =
                ((org.smallpearl.compiler.SymbolTable.ModuleEntry) symbolTableEntry).scope;

        if (ctx != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.System_partContext) {
                    module.add("SystemPart",
                            visitSystem_part((SmallPearlParser.System_partContext) c));
                } else if (c instanceof SmallPearlParser.Problem_partContext) {
                    module.add("ProblemPart",
                            visitProblem_part((SmallPearlParser.Problem_partContext) c));
                } else if (c instanceof SmallPearlParser.Cpp_inlineContext) {
                    ST decl = m_group.getInstanceOf("cpp_inline");
                    module.add("cpp_inlines",
                            visitCpp_inline((SmallPearlParser.Cpp_inlineContext) c));
                }
            }
        }

        m_currentSymbolTable = m_currentSymbolTable.ascend();

        return module;
    }

    @Override
    public ST visitSystem_part(SmallPearlParser.System_partContext ctx) {
        ST st = m_group.getInstanceOf("SystemPart");

        visitChildren(ctx);
        if (ctx != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.Cpp_inlineContext) {
                    ST decl = m_group.getInstanceOf("cpp_inline");
                    st.add("cpp_inlines", visitCpp_inline((SmallPearlParser.Cpp_inlineContext) c));
                   
                }
            }
        }

        return st;
    }

    @Override
    public ST visitTypeReference(SmallPearlParser.TypeReferenceContext ctx) {
        // must become more sophisticated! (2020-04-07 rm)
System.out.println("CppCg@487 called");
//System.exit(-1);
        ST st;
        ST baseType = visitChildren(ctx);
        if (ctx.virtualDimensionList() == null) {
            st = m_group.getInstanceOf("TypeReferenceSimpleType");
            st.add("BaseType", baseType);
        } else {
            st = m_group.getInstanceOf("TypeReferenceArray");
            st.add("basetype", baseType);
        }
        if (ctx.assignmentProtection() != null) {
            ErrorStack.addInternal(ctx, "INV", "visitTypeReference: not treated yet");
        }


        return st;
    }

// not longer in use (rm 202-20-24) 
//    @Override
//    public ST visitVariableDeclaration(VariableDeclarationContext ctx) {
//        ST scalarVariableDeclaration = m_group.getInstanceOf("ScalarVariableDeclaration");
//        if (ctx != null) {
//            for (int i = 0; i < ctx.variableDenotation().size(); i++) {
//                scalarVariableDeclaration.add("variable_denotations",
//                        visitVariableDenotation(ctx.variableDenotation().get(i)));
//            }
//
//            //            if (ctx.cpp_inline() != null) {
//            //                scalarVariableDeclaration.add("cpp", visitCpp_inline(ctx.cpp_inline()));
//            //            }
//        }
//        return scalarVariableDeclaration;
//
//    }
 
    private ST generateSpecification(InterruptEntry ve) {
        ST st = m_group.getInstanceOf("InterruptSpecifications");
        ST spec = m_group.getInstanceOf("InterruptSpecification");
        ST scope = getScope(ve);
        
        spec.add("id", ve.getName());
        scope.add("variable", spec);
        st.add("specs", scope);
        return st;
     
    }
    
    private ST generateSpecification(ProcedureEntry ve) {
        ST st = m_group.getInstanceOf("ProcedureSpecifications");
        ST spec = m_group.getInstanceOf("ProcedureSpecification");
        ST scope = getScope(ve);
        /*
         * ProcedureSpecification(id,listOfFormalParameters,body,resultAttribute,globalAttribute) ::= <<
         */

        spec.add("id", ve.getName());

        if (ve.getResultType() != null) {
            spec.add("resultAttribute", visitTypeAttribute(ve.getResultType()));
        }
        if (ve.getFormalParameters() != null) {
            ST formalParams = m_group.getInstanceOf("ListOfFormalParameters");
            for (int i=0; i<ve.getFormalParameters().size(); i++) {
                FormalParameter f = ve.getFormalParameters().get(i);
                ST fp = m_group.getInstanceOf("FormalParameter");
                //FormalParameter(id,type,assignmentProtection,passIdentical,isArray) ::= <%
                TypeDefinition td = f.getType();
                fp.add("type", visitTypeAttribute(f.getType()));
                fp.add("assignmentProtection", f.getAssigmentProtection());
                fp.add("passIdentical", f.passIdentical);
                //fp.add("isArray",f.
                formalParams.add("FormalParameters",fp);
            }
            if (ve.getFormalParameters().size() > 0) {
               spec.add("listOfFormalParameters",  formalParams);
            }
        }
        scope.add("variable", spec);
        st.add("specs", scope);
        return st;
     
    }

    private ST generateSpecification(VariableEntry ve) {
        ST dationSpecifications = m_group.getInstanceOf("DationSpecifications");
        //System.out.println("Spec: "+ ve.getName()+" "+ve.getType());
        TypeDefinition t = ve.getType();

        ST scope = getScope(ve);
        if (ve.getType() instanceof TypeArray) {
            //ErrorStack.addInternal(ve.getCtx(), "SPC","arrays not supported, yet");    
            TypeDefinition baseType = ((TypeArray)(ve.getType())).getBaseType();
            ST st = m_group.getInstanceOf("ArrayVariableSpecification");
            st.add("name", ve.getName());
            st.add("type",visitTypeAttribute(baseType));//(((TypeArray)(ve.getType())).getBaseType()));
            scope.add("variable", st);
            dationSpecifications.add("decl", scope);
        } else if (ve.getType() instanceof TypeStructure) {
            ST st = m_group.getInstanceOf("variable_denotation");
            st.add("name", getUserVariableWithoutNamespace(ve.getName()));
            st.add("type",
                    visitTypeAttribute(ve.getType()));
            st.add("inv", ve.getAssigmentProtection());
            scope.add("variable", st);
            dationSpecifications.add("decl", scope);
      
        } else if (ve.getType() instanceof TypeSemaphore ||
                   ve.getType() instanceof TypeBolt) {
            // scopeXXX adds the extern/static/ ...
            ST specifyVariable=m_group.getInstanceOf("variable_denotation"); 
            specifyVariable.add("name",getUserVariableWithoutNamespace(ve.getName()));
            specifyVariable.add("type",getSTforType(t));
            scope.add("variable", specifyVariable);
            dationSpecifications.add("decl",  scope);
        } else if (t instanceof TypeDation) {
            ST specifyDation = null;
            ST initializer = null;
            TypeDation td = (TypeDation)t;
            if (td.isSystemDation() && td.isBasic()) {
                specifyDation = m_group.getInstanceOf("SpecificationSystemDation");
                specifyDation.add("name", ve.getName());
                specifyDation.add("TypeDation", "SystemDationB");
             
                
               // initializer = m_group.getInstanceOf("InitPointerToSpcSystemDation");
               // initializer.add("name", getUserVariable(ve));
               // initializer.add("TypeDation", "SystemDationB");
            } else if (td.isSystemDation() && !td.isBasic()) {
                specifyDation = m_group.getInstanceOf("SpecificationSystemDation");
                specifyDation.add("name", ve.getName());
                specifyDation.add("TypeDation", "SystemDationNB");
                
                //initializer = m_group.getInstanceOf("InitPointerToSpcSystemDation");
                //initializer.add("name", ve.getName());
                // initializer.add("TypeDation", "SystemDationNB");
              
            } else if (td.isBasic()) {
                specifyDation = m_group.getInstanceOf("SpecificationUserDation");
                specifyDation.add("name", getUserVariableWithoutNamespace(ve.getName()));
                specifyDation.add("TypeDation", "DationTS");
            } else if (td.isAlphic()) {
                specifyDation = m_group.getInstanceOf("SpecificationUserDation");
                specifyDation.add("name", getUserVariableWithoutNamespace(ve.getName()));
                specifyDation.add("TypeDation", "DationPG");
            
            } else if (td.getTypeOfTransmissionAsType() != null) {
                specifyDation = m_group.getInstanceOf("SpecificationUserDation");
                specifyDation.add("name", getUserVariableWithoutNamespace(ve.getName()));
                specifyDation.add("TypeDation", "DationRW");
            }
            
            // initializer are only required for locally defined dations
            // imported dations are initialized in the defining module 
            if (ve.isSpecified() && !ve.getGlobalAttribute().equals(m_module.getName())) {
                initializer = null;
            }

            scope.add("variable", specifyDation);
            dationSpecifications.add("decl", scope);

        } else if (isSimpleType(t)) {
            // scopeXXX adds the extern/static/ ...
            ST specifyVariable=m_group.getInstanceOf("variable_denotation"); 
            specifyVariable.add("name",getUserVariableWithoutNamespace(ve.getName()));
            specifyVariable.add("type",getSTforType(t));
            scope.add("variable", specifyVariable);
            dationSpecifications.add("decl",  scope);
        } else {
            ErrorStack.add(ve.getCtx(),"CppCodeGenerator:generateSpecification @865","missing alternative");
        }

        return dationSpecifications;

    }
    
    private ST getScope(SymbolTableEntry ve) {
        ST scope = null;
        if (ve.getLevel() == 1) {
            if (ve.getGlobalAttribute() != null) {
                scope = m_group.getInstanceOf("globalVariable");
            } else {
                scope = m_group.getInstanceOf("staticVariable");
            }
            if (ve.isSpecified()) {
               scope = m_group.getInstanceOf("externVariable");
               if (!ve.getGlobalAttribute().equals(m_module.getName())) {
                   // namespace switch only required of namespace changes to another module
                   scope.add("fromNs", ve.getGlobalAttribute());
                   if (m_useNamespaceForGlobals) {
                      scope.add("currentNs",m_module.getName());
                   } else {
                       scope.add("currentNs", "pearlApp");
                   }
               }
            }
        }
        if (ve.getLevel() > 1) {
            scope = m_group.getInstanceOf("localVariable");
        }
        return scope;
    }
    
    private ST generateVariableDeclaration(VariableEntry ve) {

        ST variableDeclaration = m_group.getInstanceOf("variable_denotation");
        ST semaDeclarations = m_group.getInstanceOf("SemaDeclaraction");
        ST scalarVariableDeclaration = m_group.getInstanceOf("ScalarVariableDeclaration");
        ST DationDeclarations = m_group.getInstanceOf("DationDeclarations");
        ST st = null;


        if (ve instanceof FormalParameter) {
            // formal parameters are in the same scope, but they are defined in the 
            // procedure definition
            return null;
        }
        //  System.out.println("genVarDecl:"+ ve.getName()+" "+ ve.getType());
        
        // derive the scope form the SymbolTableEntry
        ST scope = getScope(ve);


        if (ve.getType() instanceof TypeArray) {
            TypeDefinition t = ((TypeArray)(ve.getType())).getBaseType();
            ST storage = m_group.getInstanceOf("ArrayStorageDeclaration");
            storage.add("name", ve.getName());
            storage.add("type",visitTypeAttribute(t));
            storage.add("assignmentProtection", ve.getAssigmentProtection());
            storage.add("totalNoOfElements", ((TypeArray) ve.getType()).getTotalNoOfElements());
            if (t instanceof TypeBolt) {
                storage.add("initElements", boltArrayInitializer(ve));   
            } else {
                storage.add("initElements", getStructOrArrayInitializer(ve));
            }    
            
            if (ve.getLevel() == 1) {
                scope = m_group.getInstanceOf("staticVariable");
            } else {
                scope = m_group.getInstanceOf("localVariable"); 
            }
           

            scope.add("variable", storage);
            scalarVariableDeclaration.add("variable_denotations", scope);
            
            st = m_group.getInstanceOf("ArrayVariableDeclaration");
            st.add("name", ve.getName());
            st.add("type",visitTypeAttribute(t));
            st.add("descriptor", getArrayDescriptor(ve));
            st.add("storage", "data_"+ve.getName());

            scope = getScope(ve);
            scope.add("variable", st);
            scalarVariableDeclaration.add("variable_denotations", scope);

        } else if (ve.getType() instanceof TypeStructure) {
            TypeDefinition td= ve.getType();
            st = m_group.getInstanceOf("variable_denotation");
            st.add("name", getUserVariableWithoutNamespace(ve.getName()));
            st.add("type",
                    visitTypeAttribute(ve.getType()));
            
            if (ve.getInitializer()!= null) {
                st.add("init", getStructOrArrayInitializer(ve));
                // Log.warn("CppCodeGenerator@889: STRUCT+INIT incomplete");
            }

            st.add("inv", ve.getAssigmentProtection());
            scope.add("variable", st);
            scalarVariableDeclaration.add("variable_denotations", scope);
          
        } else if (isSimpleType(ve)) {

            st = m_group.getInstanceOf("variable_denotation");
            st.add("name", getUserVariableWithoutNamespace(ve.getName()));
            st.add("type",
                    visitTypeAttribute(ve.getType()));//ctx.problemPartDataAttribute().typeAttribute()));
            if (ve.getInitializer() != null) {
                st.add("init", getInitialiser(ve.getInitializer()));
            }

            st.add("inv", ve.getAssigmentProtection());
            scope.add("variable", st);
            scalarVariableDeclaration.add("variable_denotations", scope);
            

        } else if (ve.getType() instanceof TypeSemaphore) {
            ST sema_decl = m_group.getInstanceOf("sema_declaration");
            sema_decl.add("name", getUserVariableWithoutNamespace(ve.getName()));
            if (ve.getInitializer() != null) {
                sema_decl.add("preset", getInitialiserForSema(ve.getInitializer()));
            }

            scope.add("variable", sema_decl);

            scalarVariableDeclaration.add("variable_denotations", scope);


        } else if (ve.getType() instanceof TypeBolt) {

            ST bolt_decl = m_group.getInstanceOf("bolt_declaration");
            bolt_decl.add("name",getUserVariableWithoutNamespace(ve.getName()));

            scope.add("variable", bolt_decl);

            scalarVariableDeclaration.add("variable_denotations", scope);


        } else if (ve.getType() instanceof TypeDation) {

            TypeDation td = (TypeDation)(ve.getType());

            ST dation_decl = visitDationDenotation(ve); //ctx.dationDenotation());
            //st.add("decl", entry.getName());
            m_dationDeclarationInitializers.add("decl",dation_decl);
            //dationDeclarations.add("decl",  dation_decl);
        } else if (ve.getType() instanceof TypeReference) {
            st = m_group.getInstanceOf("variable_denotation");
            st.add("name", getUserVariableWithoutNamespace(ve.getName()));


            TypeReference tr = ((TypeReference)ve.getType());
            ST ref = null;
            if (tr.getBaseType() instanceof TypeArray) {
                TypeDefinition basetype = ((TypeReference)ve.getType()).getBaseType();
                ref = m_group.getInstanceOf("TypeReferenceArray");
                ref.add("basetype", basetype);
            } else {
                ref = m_group.getInstanceOf("TypeReferenceSimpleType");
                ref.add("BaseType", visitTypeAttribute(tr.getBaseType()));
            }
            st.add("type",ref);

            if (ve.getInitializer() != null) {
                st.add("init", getInitialiser(ve.getInitializer()));
            }

            st.add("inv", ve.getAssigmentProtection());
            scalarVariableDeclaration.add("variable_denotations", st);



        } else {
            ErrorStack.addInternal(ve.getCtx(), "CppCodeGen:visitVariableDenotation",
                    "missing alternative@744 " + ve.getName());

        }

       
        return scalarVariableDeclaration;
    }




    private boolean isSimpleType(VariableEntry ve) {
        return isSimpleType(ve.getType());
    }

    private boolean isSimpleType(TypeDefinition t) {
        boolean result=false;
        if (t instanceof TypeFixed ||
                t instanceof TypeFloat ||
                t instanceof TypeBit ||
                t instanceof TypeChar ||
                t instanceof TypeClock ||
                t instanceof TypeDuration) {
            result = true;
        }

        return result;
    }


    private long getInitialiserForSema(Initializer init) {
        long result;
        if (init != null) {
            ASTAttribute attr = m_ast.lookup(init.m_context);
            result = attr.getConstantFixedValue().getValue();
            
        } else {
            result = 0; // no preset --> init with 0
        }

        return result;

    }

    private ST getInitialiser(Initializer initializer) {
        ST st = m_group.getInstanceOf("variable_init");
        ASTAttribute attr = m_ast.lookup(initializer.m_context);
        ST stValue = m_group.getInstanceOf("expression");
        stValue.add("code", attr.m_constant);
        st.add("value", stValue); 
        return st;
    }

    private ST getInitialiser(int i, VariableEntry ve) {
        ST st = m_group.getInstanceOf("variable_init");
        if (ve.getInitializer() != null) {
            ASTAttribute attr = m_ast.lookup(ve.getInitializer().m_context);
            ST stValue = m_group.getInstanceOf("expression");
            stValue.add("code", attr.m_constant);
            st.add("value", stValue); //stValue);
        } else {
            VariableDenotationContext v = (VariableDenotationContext) ve.getCtx().parent.parent;

            InitElementContext initElement = null;
            if (v.problemPartDataAttribute() != null
                    && v.problemPartDataAttribute().initialisationAttribute() != null) {
                initElement = v.problemPartDataAttribute().initialisationAttribute().initElement(i);
            } else if (v.semaDenotation() != null && v.semaDenotation().preset() != null) {
                initElement = v.semaDenotation().preset().initElement(i);
            }
            if (initElement != null) {
                ASTAttribute attr = m_ast.lookup(initElement);
                ST stValue = m_group.getInstanceOf("expression");

                stValue.add("code", attr.m_constant);
                st.add("value", stValue);
            } else {
                st = null;
            }
        }

        return st;
    }

    /*
     * Bolts should be initializes with their name and array-index
     */
    private ST boltArrayInitializer(VariableEntry variableEntry) {
        ArrayList<ST> initElementList = new ArrayList<ST>();
        ST st = m_group.getInstanceOf("structOrArrayInit");

        TypeArray ta = (TypeArray)(variableEntry.getType());
        for (int i=0; i<ta.getTotalNoOfElements(); i++) {
            ST stValue = m_group.getInstanceOf("bolt_init");
            stValue.add("name", variableEntry.getName());
            stValue.add("idx", i+1);
            initElementList.add(stValue);
        }
        st.add("initElements", initElementList);

        return st;
    }

    private ST getStructOrArrayInitializer(VariableEntry variableEntry) {
        boolean fillWithLast = false;
        TypeDefinition baseType=null;
        ST lastInitValue = null;
        ArrayList<ST> initElementList = new ArrayList<ST>();
        ST st = null;

        if (variableEntry.getInitializer() != null) { 
            st =  m_group.getInstanceOf("structOrArrayInit");

            if (variableEntry.getType() instanceof TypeArray) {
                fillWithLast = true;
                baseType = ((TypeArray)variableEntry.getType()).getBaseType();
            }

            ArrayOrStructureInitializer init = (ArrayOrStructureInitializer)(variableEntry.getInitializer());
            ArrayList<Initializer> initList = init.getInitElementList();
            for (int i=0; i<initList.size(); i++) {
                // simple types have constants as initializers
                // other types may differ
                ST stValue = null;
                if (baseType instanceof TypeSemaphore) {
                    stValue = m_group.getInstanceOf("sema_init");
                    stValue.add("name", variableEntry.getName());
                    stValue.add("idx", i+1);
                    long preset = ((ConstantFixedValue)(((SimpleInitializer)(initList.get(i))).getConstant())).getValue();
                    stValue.add("preset", preset);
                } else {
                   stValue = m_group.getInstanceOf("expression");
                   stValue.add("code", ((SimpleInitializer)(initList.get(i))).getConstant() );
                }
                lastInitValue = stValue;
                initElementList.add(stValue);
                
            }
            if (fillWithLast) {
                // arrays may require repetition of the last init-value
                int remainingInitializers = ((TypeArray)(variableEntry.getType())).getTotalNoOfElements();
                remainingInitializers -= initList.size();
                for (int i=0; i< remainingInitializers; i++) {
                    initElementList.add(lastInitValue);
                }
            }

            st.add("initElements", initElementList);
        }
        return st;
    }



    @Override
    public ST visitDurationConstant(SmallPearlParser.DurationConstantContext ctx) {
        ST st = m_group.getInstanceOf("DurationConstant");

        ConstantDurationValue value = CommonUtils.getConstantDurationValue(ctx, m_sign);

        if (value == null) {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        st.add("value", value);
        return st;
    }


  
    private ST visitTypeAttribute(TypeDefinition type) {
        ST st = m_group.getInstanceOf("TypeAttribute");
        String s = type.toString();

        return type.toST(m_group);
    }

    @Override
    public ST visitTypeAttribute(SmallPearlParser.TypeAttributeContext ctx) {
        ST type = m_group.getInstanceOf("TypeAttribute");

        if (ctx.simpleType() != null) {
            type.add("Type", visitSimpleType(ctx.simpleType()));
        } else if (ctx.typeReference() != null) {
            type.add("Type", visitTypeReference(ctx.typeReference()));
        } else if (ctx.typeStructure()!= null) {
            type.add("Type", visitTypeStructure(ctx.typeStructure()));
        } else {
            ErrorStack.addInternal(ctx, "CppCodeGenerator", "missing alternative"+ctx.typeStructure().getText());
        }

        return type;
    }

    @Override
    public ST visitSimpleType(SmallPearlParser.SimpleTypeContext ctx) {
        ST simpleType = m_group.getInstanceOf("SimpleType");

        if (ctx != null) {
            if (ctx.typeInteger() != null) {
                simpleType.add("TypeInteger", visitTypeInteger(ctx.typeInteger()));
            } else if (ctx.typeDuration() != null) {
                simpleType.add("TypeDuration", visitTypeDuration(ctx.typeDuration()));
            } else if (ctx.typeBitString() != null) {
                simpleType.add("TypeBitString", visitTypeBitString(ctx.typeBitString()));
            } else if (ctx.typeFloatingPointNumber() != null) {
                simpleType.add("TypeFloatingPointNumber",
                        visitTypeFloatingPointNumber(ctx.typeFloatingPointNumber()));
            } else if (ctx.typeClock() != null) {
                simpleType.add("TypeTime", visitTypeClock(ctx.typeClock()));
            } else if (ctx.typeCharacterString() != null) {
                simpleType.add("TypeCharacterString",
                        visitTypeCharacterString(ctx.typeCharacterString()));
            }
        }

        return simpleType;
    }

    @Override
    public ST visitTypeDuration(SmallPearlParser.TypeDurationContext ctx) {
        ST st = m_group.getInstanceOf("TypeDuration");

        if (ctx != null) {
            st.add("code", 1);
        }

        return st;
    }

    @Override
    public ST visitTypeClock(SmallPearlParser.TypeClockContext ctx) {
        ST st = m_group.getInstanceOf("TypeClock");

        if (ctx != null) {
            st.add("code", 1);
        }

        return st;
    }


    @Override
    public ST visitTypeInteger(SmallPearlParser.TypeIntegerContext ctx) {
        ST st = m_group.getInstanceOf("TypeInteger");
        int size = m_currentSymbolTable.lookupDefaultFixedLength();

        if (ctx != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.MprecisionContext) {
                    size = Integer.parseInt(((SmallPearlParser.MprecisionContext) c).getText());
                }
            }
        }

        st.add("size", size);

        return st;
    }

    @Override
    public ST visitTypeBitString(SmallPearlParser.TypeBitStringContext ctx) {
        ST st = m_group.getInstanceOf("TypeBitString");

        int length = m_currentSymbolTable.lookupDefaultBitLength();

        if (ctx.length() != null) {
            length = Integer.parseInt(ctx.length().getText());
        }

        st.add("length", length);

        return st;
    }

    @Override
    public ST visitTypeCharacterString(SmallPearlParser.TypeCharacterStringContext ctx) {
        ST st = m_group.getInstanceOf("TypeCharacterString");
        Integer size = Defaults.CHARACTER_LENGTH;

        if (ctx.length() != null) {
            size = Integer.parseInt(ctx.length().getText());

        }

        st.add("size", size);

        return st;
    }

    @Override
    public ST visitTypeFloatingPointNumber(SmallPearlParser.TypeFloatingPointNumberContext ctx) {
        ST st = m_group.getInstanceOf("TypeFloatingPointNumber");
        int precision = Defaults.FLOAT_PRECISION;

        if (ctx.length() != null) {
            precision = Integer.parseInt(ctx.length().getText());
        }

        st.add("precision", precision);

        return st;
    }

    private String getSTName(VariableEntry v) {
        String result=null;
        // sequence of check matters!
        if (isSimpleType(v.getType()) && v.isSpecified()==false) {
            result = "ScalarVariableDeclaraction";
        } else if (isSimpleType(v.getType()) && v.isSpecified()==true) {
            result = "VariableSpecification";
        } else if (v.getType() instanceof TypeSemaphore && v.isSpecified() == false) {
            result="SemaDeclaration";
        }
        /* ProblemPart contains:
          ScalarVariableDeclarations,
            SemaDeclarations,
            BoltDeclarations,
            semaphoreArrays,boltArrays,
            ArrayDescriptors, ArrayVariableDeclarations,TaskDeclarations,
            DationSpecifications,DationDeclarations,
            SystemDationInitializer,
            StructVariableDeclarations,
            ProcedureSpecifications,ProcedureDeclarations,
            InterruptSpecifications,
         */

        
        return result;
    }
    @Override
    public ST visitProblem_part(SmallPearlParser.Problem_partContext ctx) {
        ST problem_part = m_group.getInstanceOf("ProblemPart");

        // get variable entries from SymbolTable and create code for their definition
        LinkedList<VariableEntry> entries = m_currentSymbolTable.getVariableDeclarations();

        for (int i=0; i<entries.size(); i++) {
            VariableEntry ve = (VariableEntry)(entries.get(i));
            // System.out.println(ve.getName()+": " + ve.getType()+" "+ve.isSpecified());
            if (ve.isSpecified()) {
                problem_part.add("DationSpecifications", generateSpecification(ve));
            } else {
                problem_part.add("ScalarVariableDeclarations", generateVariableDeclaration(ve));
            }
        }
        
        // get variable entries from SymbolTable and create code for their definition
        LinkedList<InterruptEntry> intEntries = m_currentSymbolTable.getInterruptSpecifications();

        for (int i=0; i<intEntries.size(); i++) {
            InterruptEntry ve = intEntries.get(i);

            problem_part.add("InterruptSpecifications", generateSpecification(ve));

        }
        
        // get variable entries from SymbolTable and create code for their definition
        LinkedList<ProcedureEntry> procEntries = m_currentSymbolTable.getProcedureSpecificationsAndDeclarations();

        for (int i=0; i<procEntries.size(); i++) {
            ProcedureEntry ve = procEntries.get(i);

            problem_part.add("ProcedureSpecifications", generateSpecification(ve));

        }
                


        if (ctx != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.VariableDeclarationContext) {
                   
                } else if (c instanceof SmallPearlParser.TaskDeclarationContext) {
                    problem_part.add("TaskDeclarations",
                            visitTaskDeclaration((SmallPearlParser.TaskDeclarationContext) c));
                   
                } else if (c instanceof SmallPearlParser.Cpp_inlineContext) {
                    problem_part.add("cpp_inlines",
                            visitCpp_inline((SmallPearlParser.Cpp_inlineContext) c));
                   
                } else if (c instanceof SmallPearlParser.ProcedureDeclarationContext) {
                    ST procedureDeclaration = visitProcedureDeclaration(
                            (SmallPearlParser.ProcedureDeclarationContext) c);

                    problem_part.add("ProcedureDeclarations", procedureDeclaration);

                } else if (c instanceof SmallPearlParser.InterruptDenotationContext) {
                    //problem_part.add("InterruptSpecifications", visitInterruptDenotation(
                    //        (SmallPearlParser.InterruptDenotationContext) c));
                }
            }
        }

        ST semaphoreArrays = m_group.getInstanceOf("SemaphoreArrays");
        ST boltArrays = m_group.getInstanceOf("BoltArrays");

        //problem_part.add("semaphoreArrays", semaphoreArrays);
        problem_part.add("semaphoreArrays", constantSemaphoreArrays);
        problem_part.add("boltArrays", constantBoltArrays);

        ST arrayDescriptors = m_group.getInstanceOf("ArrayDescriptors");
        LinkedList<ArrayDescriptor> listOfArrayDescriptors =
                m_symbolTableVisitor.getListOfArrayDescriptors();

        for (int i = 0; i < listOfArrayDescriptors.size(); i++) {
            ST stArrayDescriptor = m_group.getInstanceOf("ArrayDescriptor");

            ArrayDescriptor arrayDescriptor = listOfArrayDescriptors.get(i);

            stArrayDescriptor.add("name", arrayDescriptor.getName());
            arrayDescriptors.add("descriptors", stArrayDescriptor);

            ArrayList<ArrayDimension> listOfArrayDimensions = arrayDescriptor.getDimensions();

            ST stArrayLimits = m_group.getInstanceOf("ArrayLimits");

            for (int j = 0; j < listOfArrayDimensions.size(); j++) {
                ST stArrayLimit = m_group.getInstanceOf("ArrayLimit");

                stArrayLimit.add("lowerBoundary",
                        Integer.toString(listOfArrayDimensions.get(j).getLowerBoundary()));
                stArrayLimit.add("upperBoundary",
                        Integer.toString(listOfArrayDimensions.get(j).getUpperBoundary()));

                int noOfElemenstOnNextSubArray = 0;
                for (int k = j + 1; k < listOfArrayDimensions.size(); k++) {
                    noOfElemenstOnNextSubArray += listOfArrayDimensions.get(k).getNoOfElements();
                }

                if (noOfElemenstOnNextSubArray == 0) {
                    noOfElemenstOnNextSubArray = 1;
                }

                stArrayLimit.add("noOfElemenstOnNextSubArray", noOfElemenstOnNextSubArray);
                stArrayLimits.add("limits", stArrayLimit);
            }

            stArrayDescriptor.add("limits", stArrayLimits);
            stArrayDescriptor.add("dimensions", Integer.toString(listOfArrayDimensions.size()));
        }

        problem_part.add("ArrayDescriptors", arrayDescriptors);

        // add dation declarations
        problem_part.add("DationDeclarations", m_dationDeclarations);
        // add system dation initializer
        ST dationInitializer = m_group.getInstanceOf("DationInitialiser");
        ST initSpecs = m_group.getInstanceOf("DationInitialisierSpecs");
        ST initDecls = m_group.getInstanceOf("DationInitialisierDecls");
        dationInitializer.add("specs", m_dationSpecificationInitializers);
        dationInitializer.add("decl", m_dationDeclarationInitializers);

        // add dation initializer only if at least one dation is used
        if (m_dationDeclarationInitializers.render().length() > 0
                || m_dationSpecificationInitializers.render().length() > 0) {
            problem_part.add("SystemDationInitializer", dationInitializer);
        }

        return problem_part;
    }

   

    @Override
    public ST visitBoltReserve(SmallPearlParser.BoltReserveContext ctx) {
        ST operation = m_group.getInstanceOf("BoltReserve");
        ST newSemaOrBoltArray = m_group.getInstanceOf("BoltArray");

        treatListOfSemaOrBoltNames(m_listOfBoltArrays, constantBoltArrays, operation,
                newSemaOrBoltArray, ctx.listOfNames());
        return operation;
    }

    @Override
    public ST visitBoltFree(SmallPearlParser.BoltFreeContext ctx) {
        ST operation = m_group.getInstanceOf("BoltFree");
        ST newSemaOrBoltArray = m_group.getInstanceOf("BoltArray");

        treatListOfSemaOrBoltNames(m_listOfBoltArrays, constantBoltArrays, operation,
                newSemaOrBoltArray, ctx.listOfNames());
        return operation;
    }


    @Override
    public ST visitBoltEnter(SmallPearlParser.BoltEnterContext ctx) {
        ST operation = m_group.getInstanceOf("BoltEnter");
        ST newSemaOrBoltArray = m_group.getInstanceOf("BoltArray");

        treatListOfSemaOrBoltNames(m_listOfBoltArrays, constantBoltArrays, operation,
                newSemaOrBoltArray, ctx.listOfNames());
        return operation;
    }


    @Override
    public ST visitBoltLeave(SmallPearlParser.BoltLeaveContext ctx) {
        ST operation = m_group.getInstanceOf("BoltLeave");
        ST newSemaOrBoltArray = m_group.getInstanceOf("BoltArray");

        treatListOfSemaOrBoltNames(m_listOfBoltArrays, constantBoltArrays, operation,
                newSemaOrBoltArray, ctx.listOfNames());
        return operation;
    }


    @Override
    public ST visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        ST taskdecl = m_group.getInstanceOf("task_declaration");
        ST priority = m_group.getInstanceOf("expression");
        Integer main = 0;

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        if (ctx.priority() != null) {
            priority = visitAndDereference(ctx.priority().expression());
        } else {
            priority.add("code", Defaults.DEFAULT_TASK_PRIORITY);
        }

        if (ctx.task_main() != null) {
            main = 1;
        }

        taskdecl.add("name", ctx.nameOfModuleTaskProc().ID());
        taskdecl.add("priority", priority);
        taskdecl.add("main", main);


        if (ctx != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.TaskBodyContext) {
                    taskdecl.add("body", visitTaskBody((SmallPearlParser.TaskBodyContext) c));
                } else if (c instanceof SmallPearlParser.Cpp_inlineContext) {
                    taskdecl.add("cpp", visitCpp_inline((SmallPearlParser.Cpp_inlineContext) c));
                }
            }
        }

        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return taskdecl;
    }

    @Override
    public ST visitTaskBody(SmallPearlParser.TaskBodyContext ctx) {
        ST taskbody = m_group.getInstanceOf("Body");
        addVariableDeclarationsToST(taskbody);

        if (ctx != null && ctx.children != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.VariableDeclarationContext) {
                   
                } else if (c instanceof SmallPearlParser.StatementContext) {
                    taskbody.add("statements",
                            visitStatement((SmallPearlParser.StatementContext) c));
                    
                }
            }
        }

        return taskbody;
    }

    @Override
    public ST visitTimeConstant(SmallPearlParser.TimeConstantContext ctx) {
        ST st = m_group.getInstanceOf("TimeConstant");
        st.add("value", getTime(ctx));
        return st;
    }


    private ST getReferenceExpression(SmallPearlParser.ExpressionContext ctx) {
        ST st = m_group.getInstanceOf("ReferenceExpression");

        if (ctx != null) {
            if (ctx instanceof SmallPearlParser.BaseExpressionContext) {
                st.add("code", visitBaseExpression(((SmallPearlParser.BaseExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.AdditiveExpressionContext) {
                st.add("code", visitAdditiveExpression(
                        ((SmallPearlParser.AdditiveExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.SubtractiveExpressionContext) {
                st.add("code", visitSubtractiveExpression(
                        ((SmallPearlParser.SubtractiveExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.MultiplicativeExpressionContext) {
                st.add("code", visitMultiplicativeExpression(
                        (SmallPearlParser.MultiplicativeExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.DivideExpressionContext) {
                st.add("code",
                        visitDivideExpression((SmallPearlParser.DivideExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.DivideIntegerExpressionContext) {
                st.add("code", visitDivideIntegerExpression(
                        (SmallPearlParser.DivideIntegerExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.UnaryAdditiveExpressionContext) {
                st.add("code", visitUnaryAdditiveExpression(
                        (SmallPearlParser.UnaryAdditiveExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.UnarySubtractiveExpressionContext) {
                st.add("code", visitUnarySubtractiveExpression(
                        (SmallPearlParser.UnarySubtractiveExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.ExponentiationExpressionContext) {
                st.add("code", visitExponentiationExpression(
                        (SmallPearlParser.ExponentiationExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.LtRelationalExpressionContext) {
                st.add("code", visitLtRelationalExpression(
                        (SmallPearlParser.LtRelationalExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.GeRelationalExpressionContext) {
                st.add("code", visitGeRelationalExpression(
                        (SmallPearlParser.GeRelationalExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.NeRelationalExpressionContext) {
                st.add("code", visitNeRelationalExpression(
                        (SmallPearlParser.NeRelationalExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.EqRelationalExpressionContext) {
                st.add("code", visitEqRelationalExpression(
                        (SmallPearlParser.EqRelationalExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.GtRelationalExpressionContext) {
                st.add("code", visitGtRelationalExpression(
                        (SmallPearlParser.GtRelationalExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.LeRelationalExpressionContext) {
                st.add("code", visitLeRelationalExpression(
                        (SmallPearlParser.LeRelationalExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.IsRelationalExpressionContext) {
                st.add("code", visitIsRelationalExpression(
                        (SmallPearlParser.IsRelationalExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.IsntRelationalExpressionContext) {
                st.add("code", visitIsntRelationalExpression(
                        (SmallPearlParser.IsntRelationalExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.AtanExpressionContext) {
                st.add("code", visitAtanExpression((SmallPearlParser.AtanExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.CosExpressionContext) {
                st.add("code", visitCosExpression((SmallPearlParser.CosExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.ExpExpressionContext) {
                st.add("code", visitExpExpression((SmallPearlParser.ExpExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.LnExpressionContext) {
                st.add("code", visitLnExpression((SmallPearlParser.LnExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.SinExpressionContext) {
                st.add("code", visitSinExpression((SmallPearlParser.SinExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.SqrtExpressionContext) {
                st.add("code", visitSqrtExpression((SmallPearlParser.SqrtExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.TanExpressionContext) {
                st.add("code", visitTanExpression((SmallPearlParser.TanExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.TanhExpressionContext) {
                st.add("code", visitTanhExpression((SmallPearlParser.TanhExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.FitExpressionContext) {
                st.add("code", visitFitExpression((SmallPearlParser.FitExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.ExponentiationExpressionContext) {
                st.add("code", visitExponentiationExpression(
                        (SmallPearlParser.ExponentiationExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.AbsExpressionContext) {
                st.add("code", visitAbsExpression((SmallPearlParser.AbsExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.SizeofExpressionContext) {
                st.add("code",
                        visitSizeofExpression((SmallPearlParser.SizeofExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.EntierExpressionContext) {
                st.add("code",
                        visitEntierExpression((SmallPearlParser.EntierExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.RoundExpressionContext) {
                st.add("code", visitRoundExpression((SmallPearlParser.RoundExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.SignExpressionContext) {
                st.add("code", visitSignExpression((SmallPearlParser.SignExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.RemainderExpressionContext) {
                st.add("code", visitRemainderExpression(
                        (SmallPearlParser.RemainderExpressionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.NowFunctionContext) {
                st.add("code", visitNowFunction((SmallPearlParser.NowFunctionContext) ctx));
            } else if (ctx instanceof SmallPearlParser.AndExpressionContext) {
                st.add("code", visitAndExpression(((SmallPearlParser.AndExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.OrExpressionContext) {
                st.add("code", visitOrExpression(((SmallPearlParser.OrExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.ExorExpressionContext) {
                st.add("code", visitExorExpression(((SmallPearlParser.ExorExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.CshiftExpressionContext) {
                st.add("code",
                        visitCshiftExpression(((SmallPearlParser.CshiftExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.ShiftExpressionContext) {
                st.add("code",
                        visitShiftExpression(((SmallPearlParser.ShiftExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.CatExpressionContext) {
                st.add("code", visitCatExpression(((SmallPearlParser.CatExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.NotExpressionContext) {
                st.add("code", visitNotExpression(((SmallPearlParser.NotExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.TOFIXEDExpressionContext) {
                st.add("code",
                        visitTOFIXEDExpression(((SmallPearlParser.TOFIXEDExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.TOFLOATExpressionContext) {
                st.add("code",
                        visitTOFLOATExpression(((SmallPearlParser.TOFLOATExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.TOBITExpressionContext) {
                st.add("code",
                        visitTOBITExpression(((SmallPearlParser.TOBITExpressionContext) ctx)));
            } else if (ctx instanceof SmallPearlParser.TaskFunctionContext) {
                st.add("code", visitTaskFunction(((SmallPearlParser.TaskFunctionContext) ctx)));
            }
        }

        return st;
    }

    @Override
    public ST visitTaskFunction(SmallPearlParser.TaskFunctionContext ctx) {
        ST stmt = m_group.getInstanceOf("taskAddress");
        ASTAttribute attr = m_ast.lookup(ctx);
        SymbolTableEntry se = attr.getSymbolTableEntry();
        if (se != null) {
           
            stmt.add("taskName", getUserVariableWithoutNamespace(se.getName()));
        }
        return stmt;
    }

    /*
     *     private void setTaskNameToST(ST st, NameContext ctx) {
        if (ctx != null) {
            ASTAttribute attr = m_ast.lookup(ctx);
            ST tsk = visitAndDereference(ctx);
            if (attr.getSymbolTableEntry() instanceof FormalParameter) {
                st.add("task", tsk);
            } else {
                ST taskName = m_group.getInstanceOf("TaskName");
                taskName.add("name",tsk);
                st.add("task", taskName);
            }
        }
    }
    
     */
    @Override
    public ST visitPrioFunction(SmallPearlParser.PrioFunctionContext ctx) {
        ST stmt = m_group.getInstanceOf("taskPrio");
        ASTAttribute attr = m_ast.lookup(ctx);
        SymbolTableEntry se = attr.getSymbolTableEntry();
        if (se instanceof TaskEntry) {
            ST tsk = m_group.getInstanceOf("TaskName");
            ST un = m_group.getInstanceOf("user_variable");
            un.add("name", se.getName());
            tsk.add("name",  un);
            stmt.add("taskName",tsk);
        } else {
            stmt.add("taskName", visitAndDereference(ctx.expression()));
        }
        return stmt;
    }

    @Override
    public ST visitStatement(SmallPearlParser.StatementContext ctx) {
        ST stmt = m_group.getInstanceOf("statement");
        //String s = ctx.getText();

        stmt.add("srcFilename", m_sourceFileName);
        stmt.add("srcLine", ctx.start.getLine());
        stmt.add("srcColumn", ctx.start.getCharPositionInLine());
        m_tempVariableList.add(m_group.getInstanceOf("TempVariableList"));
        m_tempVariableNbr.add(Integer.valueOf(0));

        if (ctx != null) {
            if (ctx.label_statement() != null) {
                for (int i = 0; i < ctx.label_statement().size(); i++) {
                    stmt.add("label", visitLabel_statement(ctx.label_statement(i)));
                }
            }

            if (ctx.children != null) {
                for (ParseTree c : ctx.children) {
                    if (c instanceof SmallPearlParser.Unlabeled_statementContext) {
                        stmt.add("code", visitUnlabeled_statement(
                                (SmallPearlParser.Unlabeled_statementContext) c));
                    } else if (c instanceof SmallPearlParser.Block_statementContext) {
                        stmt.add("code",
                                visitBlock_statement((SmallPearlParser.Block_statementContext) c));
                    } else if (c instanceof SmallPearlParser.Cpp_inlineContext) {
                        stmt.add("cpp", visitCpp_inline((SmallPearlParser.Cpp_inlineContext) c));
                    }
                }
            }
        }
        if (m_tempVariableNbr.lastElement() > 0) {
            stmt.add("tempVariableList", m_tempVariableList.lastElement());
        }
        m_tempVariableList.remove(m_tempVariableList.size() - 1);
        m_tempVariableNbr.remove(m_tempVariableNbr.size() - 1);
        return stmt;
    }

    @Override
    public ST visitLabel_statement(SmallPearlParser.Label_statementContext ctx) {
        ST st = m_group.getInstanceOf("label_statement");
        st.add("label", ctx.ID().getText());
        return st;
    }

    @Override
    public ST visitGotoStatement(SmallPearlParser.GotoStatementContext ctx) {
        ST st = m_group.getInstanceOf("goto_statement");
        st.add("label", ctx.ID().getText());
        return st;
    }

    @Override
    public ST visitUnlabeled_statement(SmallPearlParser.Unlabeled_statementContext ctx) {
        ST statement = m_group.getInstanceOf("statement");

        if (ctx.empty_statement() != null) {
            statement.add("code", visitEmpty_statement(ctx.empty_statement()));
        } else if (ctx.assignment_statement() != null) {
            statement.add("code", visitAssignment_statement(ctx.assignment_statement()));
        } else if (ctx.sequential_control_statement() != null) {
            statement.add("code",
                    visitSequential_control_statement(ctx.sequential_control_statement()));
        } else if (ctx.realtime_statement() != null) {
            statement.add("code", visitRealtime_statement(ctx.realtime_statement()));
        } else if (ctx.interrupt_statement() != null) {
            statement.add("code", visitInterrupt_statement(ctx.interrupt_statement()));
        } else if (ctx.io_statement() != null) {
            statement.add("code", visitIo_statement(ctx.io_statement()));
        } else if (ctx.callStatement() != null) {
            statement.add("code", visitCallStatement(ctx.callStatement()));
        } else if (ctx.returnStatement() != null) {
            statement.add("code", visitReturnStatement(ctx.returnStatement()));
        } else if (ctx.loopStatement() != null) {
            statement.add("code", visitLoopStatement(ctx.loopStatement()));
        } else if (ctx.exitStatement() != null) {
            statement.add("code", visitExitStatement(ctx.exitStatement()));
        } else if (ctx.gotoStatement() != null) {
            statement.add("code", visitGotoStatement(ctx.gotoStatement()));
        } else if (ctx.convertStatement() != null) {
            statement.add("code", visitConvertStatement(ctx.convertStatement()));
        }

        return statement;
    }

    @Override
    public ST visitSequential_control_statement(
            SmallPearlParser.Sequential_control_statementContext ctx) {
        ST statement = m_group.getInstanceOf("statement");

        if (ctx.if_statement() != null) {
            statement.add("code", visitIf_statement(ctx.if_statement()));
        } else if (ctx.case_statement() != null) {
            statement.add("code", visitCase_statement(ctx.case_statement()));
        }

        return statement;
    }

    @Override
    public ST visitIf_statement(SmallPearlParser.If_statementContext ctx) {
        ST stmt = m_group.getInstanceOf("if_statement");

        ST cast = m_group.getInstanceOf("CastBitToBoolean");
        cast.add("name", visitAndDereference(ctx.expression()));
        stmt.add("rhs", cast);

        if (ctx.then_block() != null) {
            stmt.add("then_block", visitThen_block(ctx.then_block()));
        }

        if (ctx.else_block() != null) {
            stmt.add("else_block", visitElse_block(ctx.else_block()));
        }

        return stmt;
    }


    @Override
    public ST visitElse_block(SmallPearlParser.Else_blockContext ctx) {
        ST statement = m_group.getInstanceOf("statement");

        int i;
        for (i = 0; i < ctx.statement().size(); i++) {
            statement.add("code", visitStatement(ctx.statement(i)));
        }

        return statement;
    }

    @Override
    public ST visitThen_block(SmallPearlParser.Then_blockContext ctx) {
        ST statement = m_group.getInstanceOf("statement");

        int i;
        for (i = 0; i < ctx.statement().size(); i++) {
            statement.add("code", visitStatement(ctx.statement(i)));
        }

        return statement;
    }

    @Override
    public ST visitCase_statement(SmallPearlParser.Case_statementContext ctx) {
        ST st = m_group.getInstanceOf("CaseStatement");

        if (ctx.case_statement_selection1() != null) {
            st.add("casestatement1",
                    visitCase_statement_selection1(ctx.case_statement_selection1()));
        } else if (ctx.case_statement_selection2() != null) {
            st.add("casestatement2",
                    visitCase_statement_selection2(ctx.case_statement_selection2()));
        }

        return st;
    }

    @Override
    public ST visitCase_statement_selection1(
            SmallPearlParser.Case_statement_selection1Context ctx) {
        ST st = m_group.getInstanceOf("CaseStatement1");
        ST st_alt = m_group.getInstanceOf("CaseAlternatives");

        st.add("expression", visitAndDereference(ctx.expression()));

        for (int i = 0; i < ctx.case_statement_selection1_alt().size(); i++) {
            SmallPearlParser.Case_statement_selection1_altContext alt =
                    ctx.case_statement_selection1_alt(i);

            ST cur_alt = visitCase_statement_selection1_alt(alt);
            cur_alt.add("alt", i + 1);
            st_alt.add("Alternatives", cur_alt);
        }

        st.add("alternatives", st_alt);

        if (ctx.case_statement_selection_out() != null) {
            st.add("out", visitCase_statement_selection_out(ctx.case_statement_selection_out()));
        }

        return st;
    }

    @Override
    public ST visitCase_statement_selection1_alt(
            SmallPearlParser.Case_statement_selection1_altContext ctx) {
        ST st = m_group.getInstanceOf("CaseAlternative");

        for (int i = 0; i < ctx.statement().size(); i++) {
            st.add("statements", visitStatement(ctx.statement(i)));
        }

        return st;
    }

    @Override
    public ST visitCase_statement_selection_out(
            SmallPearlParser.Case_statement_selection_outContext ctx) {
        ST st = m_group.getInstanceOf("CaseOut");

        for (int i = 0; i < ctx.statement().size(); i++) {
            st.add("statements", visitStatement(ctx.statement(i)));
        }

        return st;
    }

    @Override
    public ST visitCase_statement_selection2(
            SmallPearlParser.Case_statement_selection2Context ctx) {
        ST st = m_group.getInstanceOf("CaseStatement2");
        ST st_alt = m_group.getInstanceOf("CaseAlternatives");

        ASTAttribute attr = m_ast.lookup(ctx.expression());
        st.add("expression", visitAndDereference(ctx.expression()));
        if (attr.m_type instanceof TypeChar) {
            st.add("isChar", 1);
        }

        for (int i = 0; i < ctx.case_statement_selection2_alt().size(); i++) {
            SmallPearlParser.Case_statement_selection2_altContext alt =
                    ctx.case_statement_selection2_alt(i);
            ST cur_alt = visitCase_statement_selection2_alt(alt);
            st_alt.add("Alternatives", cur_alt);
        }

        st.add("alternatives", st_alt);

        if (ctx.case_statement_selection_out() != null) {
            st.add("out", visitCase_statement_selection_out(ctx.case_statement_selection_out()));
        }

        return st;
    }

    @Override
    public ST visitCase_statement_selection2_alt(
            SmallPearlParser.Case_statement_selection2_altContext ctx) {
        ST st = m_group.getInstanceOf("CaseAlternative2");

        st.add("alts", visitCase_list(ctx.case_list()));

        for (int i = 0; i < ctx.statement().size(); i++) {
            st.add("statements", visitStatement(ctx.statement(i)));
        }

        return st;
    }

    @Override
    public ST visitCase_list(SmallPearlParser.Case_listContext ctx) {
        ST st = m_group.getInstanceOf("CaseIndexList");

        for (int i = 0; i < ctx.index_section().size(); i++) {
            SmallPearlParser.Index_sectionContext index = ctx.index_section(i);

            if (index.constantFixedExpression().size() == 1) {
                // not found proper
                // solution yet :-(
                ST st_index = m_group.getInstanceOf("CaseIndex");

                ConstantValue value = m_constantExpressionEvaluatorVisitor
                        .lookup(index.constantFixedExpression(0));

                if (value == null || !(value instanceof ConstantFixedValue)) {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                            ctx.start.getCharPositionInLine());
                }

                st_index.add("index", ((ConstantFixedValue) value).getValue());
                st.add("indices", st_index);
            } else if (index.constantFixedExpression().size() == 2) {
                boolean old_map_to_const = m_map_to_const; // very ugly, but did
                // not found proper
                // solution yet :-(

                ST st_range = m_group.getInstanceOf("CaseRange");

                m_map_to_const = false;

                ConstantValue from = m_constantExpressionEvaluatorVisitor
                        .lookup(index.constantFixedExpression(0));

                if (from == null || !(from instanceof ConstantFixedValue)) {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                            ctx.start.getCharPositionInLine());
                }

                ConstantValue to = m_constantExpressionEvaluatorVisitor
                        .lookup(index.constantFixedExpression(1));

                if (to == null || !(to instanceof ConstantFixedValue)) {
                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                            ctx.start.getCharPositionInLine());
                }

                st_range.add("from", ((ConstantFixedValue) from).getValue());
                st_range.add("to", ((ConstantFixedValue) to).getValue());

                m_map_to_const = old_map_to_const;

                st.add("indices", st_range);
            }
            if (index.constantCharacterString().size() == 1) {
                ST st_index = m_group.getInstanceOf("CaseIndex");

                String s = index.constantCharacterString(0).getText();
                s = CommonUtils.removeQuotes(s);

                st_index.add("index", (int) s.charAt(0));
                st.add("indices", st_index);

            } else if (index.constantCharacterString().size() == 2) {
                ST st_range = m_group.getInstanceOf("CaseRange");

                String sLow = index.constantCharacterString(0).getText();
                sLow = CommonUtils.removeQuotes(sLow);
                st_range.add("from", (int) sLow.charAt(0));

                String sUp = index.constantCharacterString(1).getText();
                sUp = CommonUtils.removeQuotes(sUp);
                st_range.add("to", (int) sUp.charAt(0));
                st.add("indices", st_range);
            }
        }

        return st;
    }

    @Override
    public ST visitEmpty_statement(SmallPearlParser.Empty_statementContext ctx) {
        ST statement = m_group.getInstanceOf("empty_statement");
        return statement;
    }

    private ST lhs4BitSelection(SmallPearlParser.Assignment_statementContext ctx,
            Boolean derefLhs) {
        ST st = m_group.getInstanceOf("AssignmentStatementBitSlice");

        if (derefLhs) {
            ST deref = m_group.getInstanceOf("CONT");
            deref.add("operand", visitName(ctx.name()));
            st.add("id", deref);
        } else {
            st.add("id", visitName(ctx.name()));
        }

        SmallPearlParser.BitSelectionSliceContext c = ctx.bitSelectionSlice();
        ASTAttribute attr = m_ast.lookup(ctx.bitSelectionSlice());
        if (attr.getConstantSelection() != null) {
            st.add("lwb", attr.getConstantSelection().getLowerBoundary());
            st.add("upb", attr.getConstantSelection().getUpperBoundary());
        } else {
            st.add("lwb", visitAndDereference(c.expression(0)));
            if (c.expression(1) == null) {
                st.add("upb", st.getAttribute("lwb"));
            } else {
                ST upb = m_group.getInstanceOf("expression");
                upb.add("code", visitAndDereference(c.expression(1)));
                TerminalNode intConst = ctx.bitSelectionSlice().IntegerConstant();

                if (intConst != null) {
                    upb.add("code", "+");
                    upb.add("code", "(pearlrt::Fixed<15>)" + intConst.getText());
                }
                st.add("upb", upb);
            }

        }
        return st;
    }

    private ST lhs4CharSelection(SmallPearlParser.Assignment_statementContext ctx,
            Boolean derefLhs) {
        ST st = m_group.getInstanceOf("Assign2CharSelection");

        if (derefLhs) {
            ST deref = m_group.getInstanceOf("CONT");
            deref.add("operand", visitName(ctx.name()));
            st.add("id", deref);
        } else {
            st.add("id", visitName(ctx.name()));
        }

        ASTAttribute attr = m_ast.lookup(ctx.charSelectionSlice());

        SmallPearlParser.CharSelectionSliceContext c = ctx.charSelectionSlice();
        ST slice = m_group.getInstanceOf("GetCharSelection");
        if (attr.getConstantSelection() != null) {
            slice.add("lwb", attr.getConstantSelection().getLowerBoundary());
            slice.add("upb", attr.getConstantSelection().getUpperBoundary());
        } else {
            slice.add("lwb", visitAndDereference(c.expression(0)));
            if (c.expression(1) != null) {
                slice.add("upb", visitAndDereference(c.expression(1)));
            } else {
                slice.add("upb", slice.getAttribute("lwb"));
            }
            if (c.IntegerConstant() != null) {
                slice.add("offset", c.IntegerConstant().getText());
            }

        }
        st.add("lhs", slice);

        ST setCharSelection = m_group.getInstanceOf("SetCharSelection");

        setCharSelection.add("expr", visitAndDereference(ctx.expression()));
        st.add("lhs", setCharSelection);
        return st;
    }

    @Override
    public ST visitAssignment_statement(SmallPearlParser.Assignment_statementContext ctx) {
        ST stmt = null;
        String s = ctx.getText();
        ST lhs = visit(ctx.name());
        Boolean derefLhs = false;
        ASTAttribute attrLhs = m_ast.lookup(ctx.name());
        ASTAttribute attrRhs = m_ast.lookup(ctx.expression());

        ErrorStack.enter(ctx, ":=");

        stmt = m_group.getInstanceOf("assignment_statement");

        if (ctx.dereference() != null) {
            derefLhs = true;
        }

        // check if we have a deref and/or a type Reference
        if (ctx.bitSelectionSlice() != null) {
            ST bitSel = lhs4BitSelection(ctx, derefLhs);
            bitSel.add("rhs", visitAndDereference(ctx.expression()));
            stmt.add("lhs", bitSel);
        } else if (ctx.charSelectionSlice() != null) {
            stmt.add("lhs", lhs4CharSelection(ctx, derefLhs));
            // rhs is already treated
        } else if (attrLhs.getType() instanceof TypeReference && derefLhs == false) {
            // we have a reference assignment; rhs may only be a name
            stmt.add("lhs", lhs);
            stmt.add("rhs", visit(ctx.expression()));
        } else if (attrLhs.getType() instanceof TypeChar
                && attrRhs.getType() instanceof TypeVariableChar) {
            stmt = m_group.getInstanceOf("AssignCharSelection2Char");
            // assign a TypeVariableChar to a Char<S> is very special
            // we must convert the Char<s> into a CharSlice and set the rhs-slice
            // via .setSlice -> CharSlice(lhs).setSlice(<rhs>);
            stmt.add("id", visit(ctx.name()));
            stmt.add("expr", visitAndDereference(ctx.expression()));
        } else if (attrLhs.getType() instanceof TypeStructure) {
            ST st = m_group.getInstanceOf("assignment_statement");
            Log.debug("Structure on LHS");
            //            st.add("lhs", traverseNameForStruct(ctx.name(), attrLhs.getType()));
            st.add("lhs", generateLHS(ctx.name(), m_currentSymbolTable));
            st.add("rhs", visitAndDereference(ctx.expression()));
            stmt = st;
        } else {
            if (derefLhs) {
                ST deref = m_group.getInstanceOf("CONT");
                deref.add("operand", lhs);
                stmt.add("lhs", deref);
            } else {
                stmt.add("lhs", lhs);
            }
            stmt.add("rhs", visitAndDereference(ctx.expression()));
        }
        ErrorStack.leave();
        return stmt;
    }

    @Override
    public ST visitListOfExpression(SmallPearlParser.ListOfExpressionContext ctx) {
        ST indices = m_group.getInstanceOf("ArrayIndices");

        for (int i = 0; i < ctx.expression().size(); i++) {
            ST stIndex = m_group.getInstanceOf("ArrayIndex");
            stIndex.add("index", visitAndDereference(ctx.expression(i)));
            indices.add("indices", stIndex);
        }

        return indices;
    }

    @Override
    public ST visitRealtime_statement(SmallPearlParser.Realtime_statementContext ctx) {
        ST statement = m_group.getInstanceOf("statement");

        if (ctx.task_control_statement() != null) {
            statement.add("code", visitTask_control_statement(ctx.task_control_statement()));
        } else if (ctx.task_coordination_statement() != null) {
            statement.add("code",
                    visitTask_coordination_statement(ctx.task_coordination_statement()));
        }

        return statement;
    }

    @Override
    public ST visitBaseExpression(SmallPearlParser.BaseExpressionContext ctx) {
        ST expression = m_group.getInstanceOf("expression");

        if (ctx.primaryExpression() != null) {
            expression.add("code", visitPrimaryExpression(ctx.primaryExpression()));
        }

        return expression;
    }

    private String getBitStringLiteral(String literal) {
        return CommonUtils.convertBitStringToLong(literal).toString();
    }

    @Override
    public ST visitPrimaryExpression(SmallPearlParser.PrimaryExpressionContext ctx) {
        ST expression = m_group.getInstanceOf("expression");

        if (ctx.constant() != null) {
            expression.add("code", visitConstant(ctx.constant()));
        } else if (ctx.name() != null) {
            //ASTAttribute attr = m_ast.lookup(ctx.name());
            expression.add("code", visitName(ctx.name()));
        } else if (ctx.semaTry() != null) {
            expression.add("code", visitSemaTry(ctx.semaTry()));
        } else if (ctx.stringSelection() != null) {
            expression.add("code", visitStringSelection(ctx.stringSelection()));
        } else if (ctx.expression() != null) {
            expression.add("code", "(");
            // or ctx.expression(0) ???
            expression.add("code", visitAndDereference(ctx.expression()));
            expression.add("code", ")");
            //        } else if (ctx.stringSlice() != null) {
            //            expression.add("code", visitStringSlice(ctx.stringSlice()));
        }
        return expression;
    }

    @Override
    public ST visitName(SmallPearlParser.NameContext ctx) {
        String fromModule = null;
        
        ST expression = m_group.getInstanceOf("expression");
        ST stOfName = null;
        ASTAttribute attr = m_ast.lookup(ctx);
        SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.ID().getText());
        if (entry.isSpecified()) {
            fromModule = entry.getGlobalAttribute();
            if (fromModule.equals(m_module.getName())) {
                fromModule=null;
            }
        }
        
        if (entry instanceof org.smallpearl.compiler.SymbolTable.ProcedureEntry) {
            stOfName = m_group.getInstanceOf("FunctionCall");
            stOfName.add("fromns",fromModule);

           
            
            if (attr.isFunctionCall() || (ctx.listOfExpression() != null
                    && ctx.listOfExpression().expression().size() > 0)) {
 
                m_formalParameters = ((ProcedureEntry)(attr.getSymbolTableEntry())).getFormalParameters(); 
                stOfName.add("callee", getUserVariableWithoutNamespace(ctx.ID().getText()));
 

                if (ctx.listOfExpression() != null
                        && ctx.listOfExpression().expression().size() > 0) {

                    stOfName.add("ListOfActualParameters",
                            getActualParameters(ctx.listOfExpression().expression()));
                }
              //  expression.add("functionCall", functionCall);
            } else {
                // only name of a procedure
                stOfName =  getUserVariableWithoutNamespace(ctx.ID().getText());
                //expression.add("id", getUserVariableWithoutNamespace(ctx.ID().getText()));
            }
        } else if (entry instanceof VariableEntry) {
            VariableEntry variable = (VariableEntry) entry;
            // note
            // ST stNameSpace=null; if same namespace; else "fromNamespace"
            // inside the alternative set ST stOfExpression = null;
            if (attr.isFunctionCall()) {
                // code for dereference an function call missing!
                // code: bit15 = refFunctionWithBitResult;
                ErrorStack.addInternal("typeXX := refFunctionReturningTypeXX not implemented yet");
            } else if (variable.getType() instanceof TypeArray && ctx.listOfExpression() != null) {
                stOfName = generateLHS(ctx, m_currentSymbolTable);
               // expression.add("id", generateLHS(ctx, m_currentSymbolTable));
            } else if (variable.getType() instanceof TypeStructure) {
                stOfName = generateLHS(ctx, m_currentSymbolTable);
//                if (variable.isSpecified()) {
//                    String fromModule = variable.getGlobalAttribute();
//                    if (!fromModule.equals(m_module.getName())) {
//                        ST fromns=m_group.getInstanceOf("fromNamespace");
//                        fromns.add("fromNs", fromModule);
//                        fromns.add("name",temp);
//                        expression.add("id", fromns);
//                    }
//                }    else {    
//                   expression.add("id", temp);
//                }
            } else {
                stOfName =  getUserVariable(variable);
                //expression.add("id", getUserVariable(variable)); //getUserVariable(ctx.ID().getText()));
            }
            
            // add namespace here for all alternatives ??????
        } else {
            stOfName =  getUserVariableWithoutNamespace(ctx.ID().getText());
           // expression.add("id", getUserVariableWithoutNamespace(ctx.ID().getText()));
        }
       
        expression.add("id", stOfName);
       
        return expression;
    }

    @Override
    public ST visitCharSelection(SmallPearlParser.CharSelectionContext ctx) {
        ASTAttribute attr = m_ast.lookup(ctx);

        if (m_verbose > 0) {
            System.out.println("CppCodeGeneratorVisitor: visitCharSelection");
        }

        ST st = null;
        if (attr.getType() instanceof TypeChar) {
            st = m_group.getInstanceOf("MakeCharacterFromStringSelection");

            //            st.add("id", getUserVariable(ctx.name().ID().getText()));
            st.add("id", visitName(ctx.name()));
            st.add("size", attr.getType().getPrecision());

            if (attr.getConstantSelection() != null) {
                st.add("lwb", attr.getConstantSelection().getLowerBoundary());
                st.add("upb", attr.getConstantSelection().getUpperBoundary());
            } else {
                st.add("lwb", visitAndDereference(ctx.charSelectionSlice().expression(0)));
                if (ctx.charSelectionSlice().expression(1) != null) {
                    st.add("upb", visitAndDereference(ctx.charSelectionSlice().expression(1)));
                } else {
                    st.add("upb", visitAndDereference(ctx.charSelectionSlice().expression(0)));
                }
                if (ctx.charSelectionSlice().IntegerConstant() != null) {
                    st.add("offset", ctx.charSelectionSlice().IntegerConstant().getText());
                }
            }
        } else if (attr.getType() instanceof TypeVariableChar) {
            st = m_group.getInstanceOf("RhsCharSelection");

            // st.add("id", getUserVariable(ctx.name().ID().getText()));
            st.add("id", visitName(ctx.name()));

            //expr.add("id", s1);
            st.add("lwb", visitAndDereference(ctx.charSelectionSlice().expression(0)));
            if (ctx.charSelectionSlice().expression(1) != null) {
                st.add("upb", visitAndDereference(ctx.charSelectionSlice().expression(1)));
            } else {
                st.add("upb", visitAndDereference(ctx.charSelectionSlice().expression(0)));
            }
            if (ctx.charSelectionSlice().IntegerConstant() != null) {
                st.add("offset", ctx.charSelectionSlice().IntegerConstant().getText());
            }
        }
        return st;
    }

    @Override
    public ST visitBitSelection(SmallPearlParser.BitSelectionContext ctx) {
        ASTAttribute attr = m_ast.lookup(ctx);

        if (m_verbose > 0) {
            System.out.println("CppCodeGeneratorVisitor: visitCharSelection");
        }

        // we have different situations
        //    for BIT(1) we may use  BitString.getBit
        //    for BIT(2..) we may use BitString.getSlice
        if (attr.getType().getPrecision() == 1) {
            ST st = m_group.getInstanceOf("MakeBitString1FromStringSelection");
            //st.add("id", getUserVariable(ctx.name().ID().getText()));
            st.add("id", visitName(ctx.name()));
            if (attr.getConstantSelection() != null) {
                ASTAttribute a = m_ast.lookup(ctx.bitSelectionSlice().expression(0));
                st.add("lwb", a.getConstant()); //attr.getConstantSelection().getLowerBoundary());
            } else {
                st.add("lwb", visitAndDereference(ctx.bitSelectionSlice().expression(0)));
            }
            return st;
        } else {
            ST st = m_group.getInstanceOf("MakeBitStringNFromStringSelection");

            //st.add("id", getUserVariable(ctx.name().ID().getText()));
            st.add("id", visitName(ctx.name()));

            st.add("size", attr.getType().getPrecision());

            if (attr.getConstantSelection() != null) {
                ASTAttribute a = m_ast.lookup(ctx.bitSelectionSlice().expression(0));
                st.add("lwb", a.getConstant());
            } else {
                st.add("lwb", visitAndDereference(ctx.bitSelectionSlice().expression(0)));
            }
            return st;
        }
    }


    @Override
    public ST visitAdditiveExpression(SmallPearlParser.AdditiveExpressionContext ctx) {
        ST expr = m_group.getInstanceOf("expression");

        ASTAttribute attr = m_ast.lookup(ctx);

        if (attr.isReadOnly() && attr.getConstantFixedValue() != null) {
            expr.add("code", attr.getConstantFixedValue());
        } else {
            expr.add("code", visitAndDereference(ctx.expression(0)));
            expr.add("code", ctx.op.getText());
            expr.add("code", visitAndDereference(ctx.expression(1)));
        }
        return expr;
    }

    @Override
    public ST visitSubtractiveExpression(SmallPearlParser.SubtractiveExpressionContext ctx) {
        ST expr = m_group.getInstanceOf("expression");

        ASTAttribute attr = m_ast.lookup(ctx);

        if (attr.isReadOnly() && attr.getConstantFixedValue() != null) {
            expr.add("code", attr.getConstantFixedValue());
        } else {
            expr.add("code", visitAndDereference(ctx.expression(0)));
            expr.add("code", ctx.op.getText());
            expr.add("code", visitAndDereference(ctx.expression(1)));
        }
        return expr;
    }

    @Override
    public ST visitMultiplicativeExpression(SmallPearlParser.MultiplicativeExpressionContext ctx) {
        ST expr = m_group.getInstanceOf("expression");

        ASTAttribute attr = m_ast.lookup(ctx);

        if (attr.isReadOnly() && attr.getConstantFixedValue() != null) {
            expr.add("code", attr.getConstantFixedValue());
        } else {
            expr.add("code", visitAndDereference(ctx.expression(0)));
            expr.add("code", ctx.op.getText());
            expr.add("code", visitAndDereference(ctx.expression(1)));
        }

        return expr;
    }

    @Override
    public ST visitDivideExpression(SmallPearlParser.DivideExpressionContext ctx) {

        ST expr = m_group.getInstanceOf("DivisionExpression");

        expr.add("lhs", visitAndDereference(ctx.expression(0)));
        addFixedFloatConversion(expr, ctx.expression(0), 0);
        expr.add("rhs", visitAndDereference(ctx.expression(1)));
        addFixedFloatConversion(expr, ctx.expression(1), 1);

        return expr;
    }

    @Override
    public ST visitDivideIntegerExpression(SmallPearlParser.DivideIntegerExpressionContext ctx) {
        ST expr = null;

        ASTAttribute attr = m_ast.lookup(ctx);

        if (attr.isReadOnly() && attr.getConstantFixedValue() != null) {
            expr = m_group.getInstanceOf("IntegerConstant");
            expr.add("value", attr.getConstantFixedValue());
        } else {
            expr = m_group.getInstanceOf("FixedDivisionExpression");
            expr.add("lhs", visitAndDereference(ctx.expression(0)));
            expr.add("rhs", visitAndDereference(ctx.expression(1)));
        }

        return expr;
    }

    @Override
    public ST visitUnaryExpression(SmallPearlParser.UnaryExpressionContext ctx) {
        ST st = m_group.getInstanceOf("expression");

        // expr.add( "code", visitAndDereference(ctx.expression(0)));
        // expr.add( "code", ctx.op.getText());
        // expr.add( "code", visitAndDereference(ctx.expression(1)));

        return st;
    }

    @Override
    public ST visitUnaryAdditiveExpression(SmallPearlParser.UnaryAdditiveExpressionContext ctx) {
        ST expr = m_group.getInstanceOf("expression");
        expr.add("code", visitAndDereference(ctx.expression()));
        return expr;
    }

    @Override
    public ST visitUnarySubtractiveExpression(
            SmallPearlParser.UnarySubtractiveExpressionContext ctx) {
        ST expr = m_group.getInstanceOf("expression");

        int last_sign = m_sign;
        m_sign = -1;

        if (ctx.getChild(1) instanceof SmallPearlParser.BaseExpressionContext) {
            SmallPearlParser.BaseExpressionContext base_ctx =
                    (SmallPearlParser.BaseExpressionContext) (ctx.getChild(1));

            if (base_ctx.primaryExpression() != null) {
                SmallPearlParser.PrimaryExpressionContext primary_ctx =
                        base_ctx.primaryExpression();

                ASTAttribute attr = m_ast.lookup(ctx);
                if (attr.getType() instanceof TypeFloat) {
                    if (attr.isReadOnly()) {
                        expr = m_group.getInstanceOf("FloatConstant");
                        expr.add("value", attr.getConstantFloatValue());
                    } else {
                        expr.add("code", ctx.op.getText());
                        expr.add("code", visitAndDereference(ctx.expression()));
                    }
                } else if (attr.getType() instanceof TypeFixed) {
                    if (attr.isReadOnly()) {
                        expr = m_group.getInstanceOf("FixedConstant");
                        expr.add("value", attr.getConstantFixedValue());
                    } else {
                        expr.add("code", ctx.op.getText());
                        expr.add("code", visitAndDereference(ctx.expression()));
                    }
                } else if (attr.getType() instanceof TypeDuration) {
                    if (attr.isReadOnly()) {
                        expr = m_group.getInstanceOf("DurationConstant");
                        expr.add("value", attr.getConstantDurationValue());
                    } else {
                        expr.add("code", ctx.op.getText());
                        expr.add("code", visitAndDereference(ctx.expression()));
                    }
                } else if (attr.getType() instanceof TypeClock) {
                    ErrorStack.addInternal(primary_ctx, "CppCodeGenerator:visitPrimaryExpression",
                            "-CLOCK not treated");
                } else {
                    ErrorStack.addInternal(primary_ctx, "CppCodeGenerator:visitPrimaryExpression",
                            "untreated alternative@2651");
                }
            } else {
                expr.add("code", ctx.op.getText());
                expr.add("code", visitAndDereference(ctx.expression()));
            }

        } else {
            expr.add("code", ctx.op.getText());
            expr.add("code", visitAndDereference(ctx.expression()));
        }

        m_sign = last_sign;
        return expr;
    }

    @Override
    public ST visitExponentiationExpression(SmallPearlParser.ExponentiationExpressionContext ctx) {
        ST expr = m_group.getInstanceOf("Exponentiation");

        expr.add("lhs", visitAndDereference(ctx.expression(0)));
        expr.add("rhs", visitAndDereference(ctx.expression(1)));

        return expr;
    }

    @Override
    public ST visitNotExpression(SmallPearlParser.NotExpressionContext ctx) {
        TypeDefinition typ = m_ast.lookupType(ctx);
        ST expr = null;

        // TODO: bitwise
        if (typ instanceof TypeBit) {
            expr = m_group.getInstanceOf("NotBitwiseExpression");
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        expr.add("rhs", visitAndDereference(ctx.expression()));

        return expr;
    }

    @Override
    public ST visitAndExpression(SmallPearlParser.AndExpressionContext ctx) {
        TypeDefinition typ = m_ast.lookupType(ctx);
        ST expr = null;

        if (typ instanceof TypeBit) {
            expr = m_group.getInstanceOf("AndBitwiseExpression");
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        expr.add("lhs", visitAndDereference(ctx.expression(0)));
        expr.add("rhs", visitAndDereference(ctx.expression(1)));

        return expr;
    }

    @Override
    public ST visitOrExpression(SmallPearlParser.OrExpressionContext ctx) {
        TypeDefinition typ = m_ast.lookupType(ctx);
        ST expr = null;

        if (typ instanceof TypeBit) {
            expr = m_group.getInstanceOf("OrBitwiseExpression");
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        expr.add("lhs", visitAndDereference(ctx.expression(0)));
        expr.add("rhs", visitAndDereference(ctx.expression(1)));

        return expr;
    }

    @Override
    public ST visitExorExpression(SmallPearlParser.ExorExpressionContext ctx) {
        TypeDefinition typ = m_ast.lookupType(ctx);
        ST expr = null;

        if (typ instanceof TypeBit) {
            expr = m_group.getInstanceOf("ExorBitwiseExpression");
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        expr.add("lhs", visitAndDereference(ctx.expression(0)));
        expr.add("rhs", visitAndDereference(ctx.expression(1)));

        return expr;
    }

    @Override
    public ST visitCshiftExpression(SmallPearlParser.CshiftExpressionContext ctx) {
        ST expr = m_group.getInstanceOf("CshiftExpression");

        expr.add("lhs", visitAndDereference(ctx.expression(0)));
        expr.add("rhs", visitAndDereference(ctx.expression(1)));

        return expr;
    }

    @Override
    public ST visitShiftExpression(SmallPearlParser.ShiftExpressionContext ctx) {
        ST expr = m_group.getInstanceOf("ShiftExpression");

        expr.add("lhs", visitAndDereference(ctx.expression(0)));
        expr.add("rhs", visitAndDereference(ctx.expression(1)));

        return expr;
    }

    @Override
    public ST visitCatExpression(SmallPearlParser.CatExpressionContext ctx) {
        ST st;

        TypeDefinition resultType = m_ast.lookupType(ctx);

        if (resultType instanceof TypeChar) {
            st = m_group.getInstanceOf("CharCatExpression");

        } else if (resultType instanceof TypeBit) {
            st = m_group.getInstanceOf("BitCatExpression");
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        st.add("op1", visitAndDereference(ctx.expression(0)));
        st.add("op2", visitAndDereference(ctx.expression(1)));

        return st;
    }

    @Override
    public ST visitConstant(SmallPearlParser.ConstantContext ctx) {
        ST literal = m_group.getInstanceOf("literal");
        ASTAttribute attr = m_ast.lookup(ctx);

        if (attr == null || attr.m_constant == null) {

            ErrorStack.addInternal(ctx, "CppCodeGenerator::visitConstant",
                    "AST attribute for constant missing type=" + attr.m_type.toString());

            return null;
        }

        if (ctx.durationConstant() != null) {
            literal.add("duration", attr.m_constant);
        } else if (ctx.floatingPointConstant() != null) {
            literal.add("float", attr.getConstantFloatValue());
        } else if (ctx.timeConstant() != null) {
            literal.add("time", attr.getConstant());
        } else if (ctx.stringConstant() != null) {
            literal.add("string", attr.m_constant);
        } else if (ctx.bitStringConstant() != null) {
            literal.add("bitstring", attr.m_constant);
        } else if (ctx.fixedConstant() != null) {
            literal.add("integer", attr.m_constant);
        } else if (ctx.referenceConstant() != null) {
            ConstantNILReference nilRef = ConstantPool.lookupConstantNILReference();

            if (nilRef != null) {
                literal.add("nil", nilRef);
            } else {
                ErrorStack.addInternal(ctx, "literal", "no NIL symbol in constant pool");
            }

        }

        return literal;
    }
    

    @Override
    public ST visitLwbDyadicExpression(SmallPearlParser.LwbDyadicExpressionContext ctx) {
        ST st = null;
        
        ASTAttribute attr1 = m_ast.lookup(ctx.expression(1));
        VariableEntry entry = attr1.getVariable();
        
        if (entry.getType() instanceof TypeArray) {
            st = m_group.getInstanceOf("ArrayLWB");
            st.add("name", entry.getName());
            st.add("index", visitAndDereference(ctx.expression(0)).render());
        } else if (entry.getType() instanceof TypeStructure) {
            st = m_group.getInstanceOf("ArrayInStructLWB");
            st.add("descriptor", getArrayDescriptor((TypeArray)(attr1.getType())));
            st.add("index", visitAndDereference(ctx.expression(0)).render());
        } else {
            ErrorStack.addInternal(ctx, "CppCodeGenerator LWB Dyadic", "unexpected type "+attr1.getVariable().getType() );
        }

        return st;
    }

    @Override
    public ST visitLwbMonadicExpression(SmallPearlParser.LwbMonadicExpressionContext ctx) {
        ST st = null;
        
        ASTAttribute attr1 = m_ast.lookup(ctx.expression());
        VariableEntry entry = attr1.getVariable();
        
        if (entry.getType() instanceof TypeArray) {
            st = m_group.getInstanceOf("ArrayLWB");
            st.add("name", entry.getName());
            st.add("index", 1);
        } else if (entry.getType() instanceof TypeStructure) {
            st = m_group.getInstanceOf("ArrayInStructLWB");
            st.add("descriptor", getArrayDescriptor((TypeArray)(attr1.getType())));
            st.add("index", 1);
        } else {
            ErrorStack.addInternal(ctx, "CppCodeGenerator LWB monadic", "unexpected type "+attr1.getVariable().getType() );
        }

        return st;
    }

    @Override
    public ST visitUpbDyadicExpression(SmallPearlParser.UpbDyadicExpressionContext ctx) {
        ST st = null;
        
        ASTAttribute attr1 = m_ast.lookup(ctx.expression(1));
        VariableEntry entry = attr1.getVariable();
        
        if (entry.getType() instanceof TypeArray) {
            st = m_group.getInstanceOf("ArrayUPB");
            st.add("name", entry.getName());
            st.add("index", visitAndDereference(ctx.expression(0)).render());
        } else if (entry.getType() instanceof TypeStructure) {
            st = m_group.getInstanceOf("ArrayInStructUPB");
            st.add("descriptor", getArrayDescriptor((TypeArray)(attr1.getType())));
            st.add("index", visitAndDereference(ctx.expression(0)).render());
        } else {
            ErrorStack.addInternal(ctx, "CppCodeGenerator UPB Dyadic", "unexpected type "+attr1.getVariable().getType() );
        }

        return st;
    }

    @Override
    public ST visitUpbMonadicExpression(SmallPearlParser.UpbMonadicExpressionContext ctx) {
        ST st = null;
        
        ASTAttribute attr1 = m_ast.lookup(ctx.expression());
        VariableEntry entry = attr1.getVariable();
        
        if (entry.getType() instanceof TypeArray) {
            st = m_group.getInstanceOf("ArrayUPB");
            st.add("name", entry.getName());
            st.add("index", 1);
        } else if (entry.getType() instanceof TypeStructure) {
            st = m_group.getInstanceOf("ArrayInStructUPB");
            st.add("descriptor", getArrayDescriptor((TypeArray)(attr1.getType())));
            st.add("index", 1);
        } else {
            ErrorStack.addInternal(ctx, "CppCodeGenerator UPB Dyadic", "unexpected type "+attr1.getVariable().getType() );
        }
       
        return st;
    }

    @Override
    public ST visitReturnStatement(SmallPearlParser.ReturnStatementContext ctx) {
        ST stmt = m_group.getInstanceOf("return_statement");

        if (ctx.expression() != null) {
            ASTAttribute attr = m_ast.lookup(ctx.expression());

            if (attr.getType() instanceof TypeVariableChar) {
                stmt.add("char_size", m_resultType.getPrecision());
            }

            if (attr.getType() instanceof TypeReference) {
                if (m_resultType instanceof TypeReference) {
                    stmt.add("expression", visit(ctx.expression()));
                } else {
                    stmt.add("expression", visitAndDereference(ctx.expression()));
                }
            } else {
                if (m_resultType instanceof TypeReference) {
                    ST ref = m_group.getInstanceOf("referenceOf");
                    ref.add("obj", visitAndDereference(ctx.expression()));
                    stmt.add("expression", ref);
                } else {
                    stmt.add("expression", visitAndDereference(ctx.expression()));
                }
            }
        }
        return stmt;
    }

    @Override
    public ST visitTask_control_statement(SmallPearlParser.Task_control_statementContext ctx) {
        ST stmt = m_group.getInstanceOf("task_control_statement");

        if (ctx.taskStart() != null) {
            stmt.add("code", visitTaskStart(ctx.taskStart()));
        } else if (ctx.task_terminating() != null) {
            stmt.add("code", visitTask_terminating(ctx.task_terminating()));
        } else if (ctx.task_suspending() != null) {
            stmt.add("code", visitTask_suspending(ctx.task_suspending()));
        } else if (ctx.taskContinuation() != null) {
            stmt.add("code", visitTaskContinuation(ctx.taskContinuation()));
        } else if (ctx.taskResume() != null) {
            stmt.add("code", visitTaskResume(ctx.taskResume()));
        } else if (ctx.task_preventing() != null) {
            stmt.add("code", visitTask_preventing(ctx.task_preventing()));
        }

        return stmt;
    }

    @Override
    public ST visitTaskStart(SmallPearlParser.TaskStartContext ctx) {
        ST st = m_group.getInstanceOf("task_start");

        setTaskNameToST(st,ctx.name());

        startConditionToST(st, ctx.startCondition());

        if (ctx.frequency() != null) {
            SmallPearlParser.FrequencyContext c = ctx.frequency();
            st.add("Condition", "ALL");
            st.add("all", visitAndDereference(c.expression(0)));

            for (int i = 0; i < c.getChildCount(); i++) {
                if (c.getChild(i) instanceof TerminalNodeImpl) {
                    if (((TerminalNodeImpl) c.getChild(i)).getSymbol().getText().equals("UNTIL")) {
                        st.add("Condition", "UNTIL");
                        st.add("until", visitAndDereference(c.expression(1)));
                    } else if (((TerminalNodeImpl) c.getChild(i)).getSymbol().getText()
                            .equals("DURING")) {
                        st.add("Condition", "DURING");
                        st.add("during", visitAndDereference(c.expression(1)));
                    }
                }
            }
        }

        if (ctx.priority() != null) {
            st.add("Condition", "PRIO");
            st.add("priority", visitAndDereference(ctx.priority().expression()));
        }

        return st;
    }

    @Override
    public ST visitTask_terminating(SmallPearlParser.Task_terminatingContext ctx) {
        ST stmt = m_group.getInstanceOf("task_terminate");

        if (ctx.name() != null) {
            setTaskNameToST(stmt,ctx.name());
        }

        return stmt;
    }

    @Override
    public ST visitTask_suspending(SmallPearlParser.Task_suspendingContext ctx) {
        ST stmt = m_group.getInstanceOf("task_suspend");

        if (ctx.name() != null) {
            setTaskNameToST(stmt,ctx.name());
        }

        return stmt;
    }

    @Override
    public ST visitTaskContinuation(SmallPearlParser.TaskContinuationContext ctx) {
        ST stmt = m_group.getInstanceOf("TaskContinuation");

        
        if (ctx.name() != null) {
            setTaskNameToST(stmt,ctx.name());
        }

        startConditionToST(stmt, ctx.startCondition());


        if (ctx.priority() != null) {
            stmt.add("Condition", "PRIO");
            stmt.add("priority", visitAndDereference(ctx.priority().expression()));
        }

        return stmt;
    }

    @Override
    public ST visitTaskResume(SmallPearlParser.TaskResumeContext ctx) {
        ST st = m_group.getInstanceOf("TaskResume");

        startConditionToST(st, ctx.startCondition());

        return st;
    }

    private void setTaskNameToST(ST st, NameContext ctx) {
        if (ctx != null) {
            ASTAttribute attr = m_ast.lookup(ctx);
            ST tsk = visitAndDereference(ctx);
            if (attr.getSymbolTableEntry() instanceof FormalParameter) {
                st.add("task", tsk);
            } else {
                ST taskName = m_group.getInstanceOf("TaskName");
                taskName.add("name",tsk);
                st.add("task", taskName);
            }
        }
    }
    
    private void startConditionToST(ST st, StartConditionContext ctx) {
        if (ctx != null) {

            if (ctx.startConditionAT() != null) {
                st.add("Condition", "AT");
                st.add("at", visitAndDereference(ctx.startConditionAT().expression()));
            }
            if (ctx.startConditionAFTER() != null) {
                st.add("Condition", "AFTER");
                st.add("after", visitAndDereference(ctx.startConditionAFTER().expression()));
            }
            if (ctx.startConditionWHEN() != null) {
                st.add("Condition", "WHEN");
                st.add("when", visitAndDereference(ctx.startConditionWHEN().name()));
                // check for AFTER
                if (ctx.startConditionWHEN().expression() != null) {
                    st.add("Condition", "AFTER");
                    st.add("after", visitAndDereference(ctx.startConditionWHEN().expression()));

                }
            }
        }
    }

    @Override
    public ST visitTask_preventing(SmallPearlParser.Task_preventingContext ctx) {
        ST stmt = m_group.getInstanceOf("task_prevent");

        if (ctx.name() != null) {
            setTaskNameToST(stmt,ctx.name());
        }

        return stmt;
    }

    @Override
    public ST visitTask_coordination_statement(
            SmallPearlParser.Task_coordination_statementContext ctx) {
        ST stmt = m_group.getInstanceOf("TaskCoordinationStatement");

        if (ctx.semaRelease() != null) {
            stmt.add("statement", visitSemaRelease(ctx.semaRelease()));
        } else if (ctx.semaRequest() != null) {
            stmt.add("statement", visitSemaRequest(ctx.semaRequest()));
        } else if (ctx.boltEnter() != null) {
            stmt.add("statement", visitBoltEnter(ctx.boltEnter()));
        } else if (ctx.boltReserve() != null) {
            stmt.add("statement", visitBoltReserve(ctx.boltReserve()));
        } else if (ctx.boltFree() != null) {
            stmt.add("statement", visitBoltFree(ctx.boltFree()));
        } else if (ctx.boltLeave() != null) {
            stmt.add("statement", visitBoltLeave(ctx.boltLeave()));
        }
        return stmt;
    }

    @Override
    public ST visitSemaTry(SmallPearlParser.SemaTryContext ctx) {
        ST operation = m_group.getInstanceOf("SemaTry");
        ST newSemaOrBoltArray = m_group.getInstanceOf("SemaphoreArray");

        treatListOfSemaOrBoltNames(m_listOfSemaphoreArrays, constantSemaphoreArrays, operation,
                newSemaOrBoltArray, ctx.listOfNames());

        return operation;
    }

    @Override
    public ST visitSemaRelease(SmallPearlParser.SemaReleaseContext ctx) {
        ST operation = m_group.getInstanceOf("SemaRelease");
        ST newSemaOrBoltArray = m_group.getInstanceOf("SemaphoreArray");

        treatListOfSemaOrBoltNames(m_listOfSemaphoreArrays, constantSemaphoreArrays, operation,
                newSemaOrBoltArray, ctx.listOfNames());


        return operation;
    }

    @Override
    public ST visitSemaRequest(SmallPearlParser.SemaRequestContext ctx) {
        ST operation = m_group.getInstanceOf("SemaRequest");
        ST newSemaOrBoltArray = m_group.getInstanceOf("SemaphoreArray");

        treatListOfSemaOrBoltNames(m_listOfSemaphoreArrays, constantSemaphoreArrays, operation,
                newSemaOrBoltArray, ctx.listOfNames());
        return operation;
    }

    /*
     * if there is a REF, REF() or indexed array element in the list of names 
     * we must create a local array of names
     * else we can create the arrays at system build
     */
    private void treatListOfSemaOrBoltNames(LinkedList<LinkedList<String>> listOfSemaOrBoltArrays,
            ST stOfSemaOrBoltArrays, ST currentOperation, ST newSemaOrBoltArray,
            ListOfNamesContext ctx) {
        LinkedList<String> listOfNames = new LinkedList<String>();
        String nameOfArray = "";

        boolean listIsConstant = true;
        
        for (int i = 0; i < ctx.name().size(); i++) {
            String name = ctx.name(i).getText();

            ST sem = visitAndDereference(ctx.name(i));

            ASTAttribute attr = m_ast.lookup(ctx.name(i));
            if (attr.getSymbolTableEntry() instanceof FormalParameter) {
                listIsConstant = false;
            }
            if (attr.getVariable().isSpecified()) {
                listIsConstant = false;
            }
            if (attr.getVariable().getType() instanceof TypeArray) {
                // check array index
                name=attr.getVariable().getName();
                
                TypeArray ta = (TypeArray)(attr.getVariable().getType());
                int dimensions = ta.getNoOfDimensions();
                for (int dim=0; dim<dimensions; dim++) {
                    ASTAttribute indexAttr = m_ast.lookup(ctx.name(i).listOfExpression().expression(dim));
                    if (indexAttr.getVariable() != null) {
                        listIsConstant = false;
                    } else {
                        name += "_" + indexAttr.getConstantFixedValue().getValue();
                    }
                }
                if (listIsConstant) {
                    
                }
            }
            if (attr.getType() instanceof TypeReference) {
                listIsConstant = false;
            }
            newSemaOrBoltArray.add("element", sem);

            //listOfNames.add(ctx.name(i).getText());
            listOfNames.add(name);
        }


        Collections.sort(listOfNames);

        currentOperation.add("nbrOfElements", ctx.name().size());
        if (listIsConstant) {
            for (int i = 0; i < listOfNames.size(); i++) {
                currentOperation.add("names", listOfNames.get(i));
                nameOfArray += "_" + listOfNames.get(i);
            }
            addToListOfConstantSemaOrBoltArrays(listOfSemaOrBoltArrays, stOfSemaOrBoltArrays,
                    listOfNames, newSemaOrBoltArray);
            newSemaOrBoltArray.add("isConstant", 1);
            newSemaOrBoltArray.add("nameOfArray", nameOfArray);
        } else {
            // create a temporary variable for the statement
            // this produces automatically a block and the variable instanciation
            String tempVarName = nextTempVarName();
            newSemaOrBoltArray.add("nameOfArray", tempVarName);
            currentOperation.add("array", newSemaOrBoltArray);
            currentOperation.add("localArrayname", tempVarName);
            m_tempVariableList.lastElement().add("variable", newSemaOrBoltArray);
        }
    }


    private Void addToListOfConstantSemaOrBoltArrays(
            LinkedList<LinkedList<String>> listOfSemaOrBoltArrays, ST stOfSemaOrBoltArrays,
            LinkedList<String> listOfNames, ST semaOrBoltArray) {
        Boolean found = false;
        for (int i = 0; i < listOfSemaOrBoltArrays.size(); i++) {
            LinkedList<String> names = listOfSemaOrBoltArrays.get(i);
            if (names.size() == listOfNames.size()) {
                int j = 0;
                for (j = 0; j < names.size(); j++) {
                    if (names.get(j).compareTo(listOfNames.get(j)) != 0) {
                        break;
                    }
                }

                if (j == names.size()) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            // update list of combinations
            listOfSemaOrBoltArrays.add(listOfNames);
            // and add to the array container
            stOfSemaOrBoltArrays.add("array", semaOrBoltArray);
        }

        return null;
    }


    @Override
    public ST visitIo_statement(SmallPearlParser.Io_statementContext ctx) {
        ST stmt = m_group.getInstanceOf("io_statement");

        if (ctx.close_statement() != null) {
            stmt.add("code", visitClose_statement(ctx.close_statement()));
        } else if (ctx.open_statement() != null) {
            stmt.add("code", visitOpen_statement(ctx.open_statement()));
        } else if (ctx.readStatement() != null) {
            stmt.add("code", visitReadStatement(ctx.readStatement()));
        } else if (ctx.sendStatement() != null) {
            stmt.add("code", visitSendStatement(ctx.sendStatement()));
        } else if (ctx.takeStatement() != null) {
            stmt.add("code", visitTakeStatement(ctx.takeStatement()));
        } else if (ctx.writeStatement() != null) {
            stmt.add("code", visitWriteStatement(ctx.writeStatement()));
        } else if (ctx.getStatement() != null) {
            stmt.add("code", visitGetStatement(ctx.getStatement()));
        } else if (ctx.putStatement() != null) {
            stmt.add("code", visitPutStatement(ctx.putStatement()));
        }

        return stmt;
    }

    @Override
    public ST visitClose_statement(SmallPearlParser.Close_statementContext ctx) {
        ST stmt = m_group.getInstanceOf("close_statement");
        stmt.add("id", visitAndDereference(ctx.dationName().name()));

        if (ctx.close_parameterlist() != null) {
            stmt.add("paramlist", visitClose_parameterlist(ctx.close_parameterlist()));
        } else {
            ST st = m_group.getInstanceOf("close_parameterlist");
            st.add("parameter", m_group.getInstanceOf("close_parameter_none"));
            stmt.add("paramlist", st);
            stmt.add("rst_var", m_group.getInstanceOf("close_parameter_no_rst"));
        }

        return stmt;
    }

    @Override
    public ST visitOpen_statement(SmallPearlParser.Open_statementContext ctx) {
        ST stmt = m_group.getInstanceOf("open_statement");

        stmt.add("id", visitAndDereference(ctx.dationName().name()));

        if (ctx.open_parameterlist() != null) {
            stmt.add("paramlist", visitOpen_parameterlist(ctx.open_parameterlist()));

            Open_parameter_idfContext idfName = null;
            OpenClosePositionRSTContext rstVar = null;

            if (ctx.open_parameterlist() != null) {
                for (int i = 0; i < ctx.open_parameterlist().open_parameter().size(); i++) {
                    if (ctx.open_parameterlist().open_parameter(i).open_parameter_idf() != null) {
                        idfName = ctx.open_parameterlist().open_parameter(i).open_parameter_idf();
                    }


                    if (ctx.open_parameterlist().open_parameter(i).openClosePositionRST() != null) {
                        rstVar = ctx.open_parameterlist().open_parameter(i).openClosePositionRST();
                    }
                }

            }
            if (idfName != null) {
                String fname = null;
                ST declFilename = m_group.getInstanceOf("declare_idf_filename");
                ST refFilename = m_group.getInstanceOf("reference_idf_filename");

                if (idfName.ID() != null) {
                    fname = idfName.ID().toString();
                    SymbolTableEntry entry = m_currentSymbolTable.lookup(fname);

                    if (entry instanceof VariableEntry) {
                        declFilename.add("variable", fname);
                        refFilename.add("variable", fname);
                    } else {
                        declFilename.add("stringConstant", fname);
                        declFilename.add("lengthOfStringConstant", fname.length());
                        refFilename.add("stringConstant", fname);

                    }
                }
                if (idfName.StringLiteral() != null) {
                    fname = idfName.StringLiteral().toString();
                    fname = fname.substring(1, fname.length() - 1);
                    declFilename.add("stringConstant", fname);
                    declFilename.add("lengthOfStringConstant", fname.length());
                    refFilename.add("stringConstant", fname);
                }
                stmt.add("declFileName", declFilename);
                stmt.add("refFileName", refFilename);

            }
            if (rstVar != null) {
                stmt.add("rst_var", visitAndDereference(rstVar.name()));
            }
        }

        return stmt;
    }


    @Override
    public ST visitOpen_parameterlist(SmallPearlParser.Open_parameterlistContext ctx) {
        ST st = m_group.getInstanceOf("open_parameterlist");

        if (ctx.open_parameter() != null) {
            for (int i = 0; i < ctx.open_parameter().size(); i++) {
                if (ctx.open_parameter(i).open_parameter_old_new_any() != null) {
                    Open_parameter_old_new_anyContext ctxTmp =
                            (Open_parameter_old_new_anyContext) ctx.open_parameter(i)
                            .open_parameter_old_new_any();
                    st.add("parameter", visitOpen_parameter_old_new_any(ctxTmp));
                }

                if (ctx.open_parameter(i).open_close_parameter_can_prm() != null) {
                    Open_close_parameter_can_prmContext ctxTmp =
                            (Open_close_parameter_can_prmContext) ctx.open_parameter(i)
                            .open_close_parameter_can_prm();
                    st.add("parameter", visitOpen_close_parameter_can_prm(ctxTmp));
                }
                if (ctx.open_parameter(i).open_parameter_idf() != null) {
                    Open_parameter_idfContext ctxTemp = (Open_parameter_idfContext) (ctx
                            .open_parameter(i).open_parameter_idf());
                    st.add("parameter", visitOpen_parameter_idf(ctxTemp));
                }

                if (ctx.open_parameter(i).openClosePositionRST() != null) {
                    ST rst = m_group.getInstanceOf("open_close_parameter_rst");
                    rst.add("id", visitAndDereference(
                            ctx.open_parameter(i).openClosePositionRST().name()));
                    st.add("parameter", rst);
                }
            }
        }
        return st;
    }

    @Override
    public ST visitOpen_parameter_idf(SmallPearlParser.Open_parameter_idfContext ctx) {
        ST st = m_group.getInstanceOf("open_parameter_idf");

        if (ctx.ID() != null) {
            st.add("id", ctx.ID().getText());
        } else if (ctx.StringLiteral() != null) {
            st.add("string", ctx.StringLiteral().getText());
        }
        return st;
    }

    @Override
    public ST visitOpen_parameter_old_new_any(Open_parameter_old_new_anyContext ctxTmp) {
        if (ctxTmp.getText().equals("OLD")) {
            ST st = m_group.getInstanceOf("open_parameter_old");
            st.add("attribute", 1);
            return st;
        } else if (ctxTmp.getText().equals("NEW")) {
            ST st = m_group.getInstanceOf("open_parameter_new");
            st.add("attribute", 1);
            return st;
        } else if (ctxTmp.getText().equals("ANY")) {
            ST st = m_group.getInstanceOf("open_parameter_any");
            st.add("attribute", 1);
            return st;
        }

        return null;
    }

    @Override
    public ST visitOpenClosePositionRST(SmallPearlParser.OpenClosePositionRSTContext ctx) {
        ST st = m_group.getInstanceOf("open_close_parameter_rst");
        st.add("id", visitAndDereference(ctx.name()));
        return st;
    }

    @Override
    public ST visitClose_parameterlist(SmallPearlParser.Close_parameterlistContext ctx) {
        ST st = m_group.getInstanceOf("close_parameterlist");

        if (ctx.close_parameter() != null) {
            for (int i = 0; i < ctx.close_parameter().size(); i++) {
                if (ctx.close_parameter(i).openClosePositionRST() != null) {
                    ST rst = m_group.getInstanceOf("close_parameter_rst");
                    rst.add("id", visitAndDereference(
                            ctx.close_parameter(i).openClosePositionRST().name()));
                    st.add("parameter", rst);
                }
                if (ctx.close_parameter(i).open_close_parameter_can_prm() != null) {
                    Open_close_parameter_can_prmContext c =
                            (Open_close_parameter_can_prmContext) ctx.close_parameter(i)
                            .open_close_parameter_can_prm();
                    if (c.getText().equals("CAN")) {
                        ST can = m_group.getInstanceOf("close_parameter_can");
                        can.add("attribute", 1);
                        st.add("parameter", can);
                    }
                    if (c.getText().equals("PRM")) {
                        ST prm = m_group.getInstanceOf("close_parameter_prm");
                        prm.add("attribute", 1);
                        st.add("parameter", prm);
                    }
                }
            }
        }
        return st;
    }

    @Override
    public ST visitGetStatement(SmallPearlParser.GetStatementContext ctx) {
        ST stmt = m_group.getInstanceOf("iojob_io_statement");
        stmt.add("command", "get");
        ErrorStack.enter(ctx, "GET");

        stmt.add("dation", visitAndDereference(ctx.dationName().name()));

        addDataAndFormatListToST(stmt, ctx.listOfFormatPositions(), ctx.ioDataList());

        ErrorStack.leave();
        return stmt;

    }

    @Override
    public ST visitPutStatement(SmallPearlParser.PutStatementContext ctx) {
        //ST stmt = m_group.getInstanceOf("put_statement");
        ST stmt = m_group.getInstanceOf("iojob_io_statement");
        ErrorStack.enter(ctx, "PUT");

        stmt.add("command", "put");
        stmt.add("dation", visitAndDereference(ctx.dationName().name()));

        addDataAndFormatListToST(stmt, ctx.listOfFormatPositions(), ctx.ioDataList());

        ErrorStack.leave();

        return stmt;
    }

    private void addDataAndFormatListToST(ST stmt, ListOfFormatPositionsContext fmtCtx,
            IoDataListContext dataCtx) {
        // this flag is set to true if at least one non static format parameter is detected
        m_isNonStatic = false;

        boolean hasFormats = false;

        // this should never occur, since this is checked in the semantic analysis
        // in CheckIOStatements
        ST formatList = m_group.getInstanceOf("iojob_formatlist");

        // for PUT and GET set format LIST, if the format list is empty
        String command = stmt.getAttribute("command").toString();
        if (fmtCtx == null && (command.equals("put") || command.equals("get"))) {
            ST fmt = m_group.getInstanceOf("iojob_list_format");
            formatList.add("formats", fmt);
            hasFormats = true;
        } else {
            if (fmtCtx != null) {
                for (int i = 0; i < fmtCtx.formatPosition().size(); i++) {
                    addFormatPositionToFormatList(formatList, fmtCtx.formatPosition(i));
                    hasFormats = true;
                }
            }
        }

        if (hasFormats) {
            stmt.add("formatlist", formatList);
            if (!m_isNonStatic) {
                stmt.add("format_list_is_static", "1");
            }
        }

        // create list of datas
        ST dataList = getIojobDataList(dataCtx);

        stmt.add("datalist", dataList);

    }

    private ST getIojobDataList(IoDataListContext ctx) {

        if (ctx != null) {
            ST dataList = m_group.getInstanceOf("iojob_datalist");
            for (int i = 0; i < ctx.ioListElement().size(); i++) {
                //System.out.println("type_ "+m_ast.lookupType(ctx.expression(i)));
                if (ctx.ioListElement(i).expression() != null) {
                    ASTAttribute attr = m_ast.lookup(ctx.ioListElement(i).expression());
                    if (attr.m_type instanceof TypeArray) {
                        TypeArray ta = (TypeArray) attr.m_type;
                        ST data = getIojobDataItem(ta.getBaseType());
                        data.add("isArray", 1);
                        VariableEntry ve = attr.getVariable();
                        data.add("variable", visitAndDereference(ctx.ioListElement(i).expression()));
                        //data.add("nbr_of_elements", ta.getTotalNoOfElements());

                        dataList.add("dataelement", data);
                        //} else if (attr.m_type instanceof TypeStruct) {
                        // Note: STRUCT not treated yet -- must roll out all compartments
                    } else {
                        // scalar value; may be:
                        // constant / variable / expression with scalar type (incl. CharSelection)
                        // regard: BitSelections are treated as type Bit at this point
                        // CharSelections need special treatment
                        //   for character selections we must superseed for known result sizes
                        //       the 'type' and 'size' field with CHARSLICE and the size of the base type
                        //       the selection range must be passed in param2
                        //   TypeVariableChar need 'lwb' and 'upb'
                        ST data = getIojobDataItem(attr.m_type);
                        ExpressionContext e = ctx.ioListElement(i).expression();

                        if (attr.getType() instanceof TypeVariableChar) {
                            CharSelectionContext ssc =
                                    (CharSelectionContext) (e.getChild(0).getChild(0).getChild(0));
                            data.add("variable", visitName(ssc.name()));
                            data.add("nbr_of_elements", "1");
                            data.add("lwb",
                                    visitAndDereference(ssc.charSelectionSlice().expression(0)));
                            data.add("upb",
                                    visitAndDereference(ssc.charSelectionSlice().expression(1)));

                        } else if (attr.isReadOnly() || attr.getVariable() != null) {
                            // constant or  variable with simple type
                            //                            data.add("variable", getExpression(ctx.ioListElement(i).expression()));
                            data.add("variable",
                                    visitAndDereference(ctx.ioListElement(i).expression()));
                            data.add("nbr_of_elements", "1");
                        } else {
                            // it is an expression
                            // System.out.println("need temp variable for expression: "+ ctx.expression(i).getText());

                            // get ST according base type
                            ST t = attr.m_type.toST(m_group);
                            if (t == null) {
                                System.out.println("untreated type " + attr.m_type);
                            }

                            // let's see if the expression is of type CharSelectionContext
                            CharSelectionContext ssc = null;
                            try {
                                ssc = (CharSelectionContext) (e.getChild(0).getChild(0)
                                        .getChild(0));
                            } catch (ClassCastException ex) {
                            } catch (NullPointerException ex) {
                            } ;

                            if (ssc != null) {
                                // fixed size char selection
                                data.remove("type");
                                data.add("type", "CHARSLICE");
                                data.remove("size");
                                ASTAttribute attribName = m_ast.lookup(ssc.name());
                                data.add("size", attribName.getType().getPrecision());
                                data.add("nbr_of_elements", "1");
                                data.add("variable", visitName(ssc.name()));
                                if (attr.getConstantSelection() != null) {
                                    data.add("lwb", attr.getConstantSelection().getLowerBoundary());
                                    data.add("upb", attr.getConstantSelection().getUpperBoundary());
                                } else {
                                    // must be of type .char(x:x+4) of type .char(x); with x is non constant expression
                                    data.add("lwb", visitAndDereference(
                                            ssc.charSelectionSlice().expression(0)));
                                    ST upb = m_group.getInstanceOf("expression");
                                    upb.add("code", data.getAttribute("lwb"));
                                    if (ssc.charSelectionSlice().IntegerConstant() != null) {
                                        upb.add("code", "+");
                                        upb.add("code", "(pearlrt::Fixed<15>)("
                                                + (attr.getType().getPrecision() - 1) + ")");
                                    }
                                    data.add("upb", upb);
                                }

                            } else {
                                // expression, which needs a temporary variable to store the result
                                // since we pass a pointer to the formatting routines
                                ST variable_declaration =
                                        m_group.getInstanceOf("variable_denotation");
                                variable_declaration.add("name", "tempVar" + i);
                                variable_declaration.add("type", t);
                            
                                ST stValue = m_group.getInstanceOf("expression");
                                stValue.add("code", visitAndDereference(ctx.ioListElement(i).expression()));
                                ST stInit = m_group.getInstanceOf("variable_init");
                                stInit.add("value",stValue);
                                variable_declaration.add("init",stInit);
                                        //visitAndDereference(ctx.ioListElement(i).expression()));
                                //variable_declaration.add("no_decoration", 1);
                                dataList.add("data_variable", variable_declaration);
                                dataList.add("data_index", i);

                                data.add("variable", "tempVar" + i);
                                data.add("nbr_of_elements", "1");
                            }
                        }

                        //   addVariableConstantOrExpressionToDatalist(dataList,data,attr,ctx.expression(i),i);
                        dataList.add("dataelement", data);
                    }
                } else if (ctx.ioListElement(i).arraySlice() != null) {
                 
                    ArraySliceContext slice = ctx.ioListElement(i).arraySlice();
                    ASTAttribute attr = m_ast.lookup(slice);
                    // we need
                    //  + the base type of the array
                    //  + the address of the name(startIndex)
                    //  + number of elements
                    ASTAttribute a = m_ast.lookup(slice.name());

                    TypeArraySlice tas = (TypeArraySlice) (attr.getType());
                    TypeArray ta = (TypeArray) (tas.getBaseType());
                    TypeDefinition t = ta.getBaseType();

                    

                    
                    ST data = getIojobDataItem(t);

                    ST firstElement = m_group.getInstanceOf("ArrayLHS");
                    firstElement.add("name", a.getVariable().getName());

                    firstElement.add("indices",
                            getIndices(slice.startIndex().listOfExpression().expression()));

                    data.add("variable", firstElement);

                    int lastElementInList =
                            slice.startIndex().listOfExpression().expression().size() - 1;
                    if (tas.hasConstantSize()) {
                        // both limits are constant -- we know the nbr_of_elements
                        data.add("nbr_of_elements", tas.getTotalNoOfElements());
                    } else {
                        ST nbr = m_group.getInstanceOf("expression");
                        nbr.add("code", "(");
                        nbr.add("code", visitAndDereference(slice.endIndex().expression()));
                        nbr.add("code", "-");

                        nbr.add("code", visitAndDereference(slice.startIndex().listOfExpression()
                                .expression(lastElementInList)));
                        nbr.add("code", ").get()+1");

                        data.add("nbr_of_elements", nbr);
                    }
                    dataList.add("dataelement", data);
                    
                    // test access for last slice element
                    // note first element is automatically checked when accessing the 
                    // data
                    String tempVarName = nextTempVarName();
                    ST temp = m_group.getInstanceOf("variable_denotation");

                    ST lastElement = m_group.getInstanceOf("ArrayLHS");
                    lastElement.add("name", a.getVariable().getName());

                    // get complete index of last element in array slice
                    // use the first size-1 element from startindex and add the end index
                    ST indices = m_group.getInstanceOf("ArrayIndices");
                    for (int indexOfExpression = 0; indexOfExpression < lastElementInList; indexOfExpression++) {
                        ST stIndex = m_group.getInstanceOf("ArrayIndex");
                        stIndex.add("index", visitAndDereference(slice.startIndex().listOfExpression()
                               .expression(indexOfExpression)));
                        indices.add("indices", stIndex);
                    }
                    ST stIndex = m_group.getInstanceOf("ArrayIndex");
                    stIndex.add("index", visitAndDereference(slice.endIndex().expression()));
                    indices.add("indices", stIndex);
                    lastElement.add("indices", indices);

                    temp.add("name",  tempVarName);
                    temp.add("type", visitTypeAttribute(t));
                    temp.add("init",lastElement);
                    m_tempVariableList.lastElement().add("variable", temp);

                    // -- end of test
                    
                }
            }
            return dataList;
        }
        return null; // no dataList
    }

    private ST getIojobDataItem(TypeDefinition type) {
        ST data = m_group.getInstanceOf("iojob_data_item");

        if (type instanceof TypeReference) {
            type = ((TypeReference) type).getBaseType();
        }

        if (type instanceof TypeFixed) {
            data.add("type", "FIXED");
            data.add("size", ((TypeFixed) (type)).getPrecision());
        } else if (type instanceof TypeFloat) {
            data.add("type", "FLOAT");
            data.add("size", ((TypeFloat) (type)).getPrecision());
        } else if (type instanceof TypeChar) {
            data.add("type", "CHAR");
            data.add("size", ((TypeChar) (type)).getSize());
        } else if (type instanceof TypeVariableChar) {
            data.add("type", "CHARSLICE");
            data.add("size", ((TypeVariableChar) (type)).getBaseType().getPrecision());
        } else if (type instanceof TypeBit) {
            data.add("type", "BIT");
            data.add("size", ((TypeBit) (type)).getPrecision());
        } else if (type instanceof TypeClock) {
            data.add("type", "CLOCK");
        } else if (type instanceof TypeDuration) {
            data.add("type", "DURATION");
        } else {
            ErrorStack.addInternal("getIoJobDataItem: untreated type " + type.getName());
        }
        return data;
    }


    private void setIsNonStatic() {
        m_isNonStatic = true;
    }

    private void updateIsNonStatic(ParserRuleContext ctx) {
        ASTAttribute attr;
        attr = m_ast.lookup(ctx);
        if (attr == null) {
            // we do not know details
            System.out.println("no ASTAttributes on " + ctx.getText());
            m_isNonStatic = true;
        } else {
            if (attr.m_constant == null && !attr.isReadOnly()) {
                m_isNonStatic = true; // no constant
            }
        }
    }

    private int addFormatPositionToFormatList(ST formatList, FormatPositionContext ctx) {
        int nbrOfFormats = 0;

        // we have to deal with
        //   FactorFormatContext   like 3F(6)
        //   FactorPositionContext like 3X(4)
        //   FactorFormatPositionContext  like (3)(A,F(6),SKIP)
        //   factor is always aptional!
        if (ctx instanceof SmallPearlParser.FactorFormatContext) {
            FactorFormatContext c = (FactorFormatContext) (ctx);
            if (c.factor() != null) {
                addFactorToFormatList(formatList, c.factor());
                nbrOfFormats++;
            }

            // add format
            if (c.format() instanceof SmallPearlParser.FormatContext) {
                FormatContext c1 = (FormatContext) (c.format());
                if (c1.fixedFormat() != null) {
                    FixedFormatContext ffc = c1.fixedFormat();
                    ST fmt = m_group.getInstanceOf("iojob_fixed_format");
                    updateIsNonStatic(ffc.fieldWidth().expression());
                    fmt.add("fieldwidth", visitAndDereference(ffc.fieldWidth().expression()));
                    if (c1.fixedFormat().decimalPositions() != null) {
                        updateIsNonStatic(ffc.decimalPositions().expression());
                        fmt.add("decimalPositions",
                                visitAndDereference(ffc.decimalPositions().expression()));
                    }
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (c1.floatFormat() != null) {
                    ST fmt = m_group.getInstanceOf("iojob_e_format");
                    if (c1.floatFormat() instanceof FloatFormatEContext) {
                        FloatFormatEContext c2 = (FloatFormatEContext) c1.floatFormat();
                        updateIsNonStatic(c2.fieldWidth().expression());
                        fmt.add("fieldwidth", visitAndDereference(c2.fieldWidth().expression()));
                        fmt.add("exp23", 2);
                        if (c2.decimalPositions() != null) {
                            updateIsNonStatic(c2.decimalPositions().expression());
                            fmt.add("decimalPositions",
                                    visitAndDereference(c2.decimalPositions().expression()));
                        }
                        if (c2.significance() != null) {
                            updateIsNonStatic(c2.significance().expression());
                            fmt.add("significance",
                                    visitAndDereference(c2.significance().expression()));
                        }
                    } else if (c1.floatFormat() instanceof FloatFormatE3Context) {
                        FloatFormatE3Context c2 = (FloatFormatE3Context) c1.floatFormat();
                        updateIsNonStatic(c2.fieldWidth().expression());
                        fmt.add("fieldwidth", visitAndDereference(c2.fieldWidth().expression()));
                        fmt.add("exp23", 3);
                        if (c2.decimalPositions() != null) {
                            updateIsNonStatic(c2.decimalPositions().expression());
                            fmt.add("decimalPositions",
                                    visitAndDereference(c2.decimalPositions().expression()));
                        }
                        if (c2.significance() != null) {
                            updateIsNonStatic(c2.significance().expression());
                            fmt.add("significance",
                                    visitAndDereference(c2.significance().expression()));
                        }
                    }
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (c1.characterStringFormat() != null) {
                    if (c1.characterStringFormat() instanceof CharacterStringFormatAContext) {
                        CharacterStringFormatAContext c2 =
                                (CharacterStringFormatAContext) c1.characterStringFormat();
                        ST fmt = m_group.getInstanceOf("iojob_character_string_format");
                        if (c2.fieldWidth() != null) {
                            updateIsNonStatic(c2.fieldWidth().expression());
                            fmt.add("fieldwidth",
                                    visitAndDereference(c2.fieldWidth().expression()));
                        }
                        formatList.add("formats", fmt);
                        nbrOfFormats++;
                    }
                } else if (c1.bitFormat() != null) {
                    ST fmt = m_group.getInstanceOf("iojob_bit_format");
                    if (c1.bitFormat() instanceof BitFormat1Context) {
                        BitFormat1Context c2 = (BitFormat1Context) (c1.bitFormat());
                        fmt.add("base", 1);
                        if (c2.numberOfCharacters() != null) {
                            updateIsNonStatic(c2.numberOfCharacters().expression());
                            fmt.add("fieldwidth",
                                    visitAndDereference(c2.numberOfCharacters().expression()));
                        }
                    } else if (c1.bitFormat() instanceof BitFormat2Context) {
                        BitFormat2Context c2 = (BitFormat2Context) (c1.bitFormat());
                        fmt.add("base", 2);
                        if (c2.numberOfCharacters() != null) {
                            updateIsNonStatic(c2.numberOfCharacters().expression());
                            fmt.add("fieldwidth",
                                    visitAndDereference(c2.numberOfCharacters().expression()));
                        }
                    } else if (c1.bitFormat() instanceof BitFormat3Context) {
                        BitFormat3Context c2 = (BitFormat3Context) (c1.bitFormat());
                        fmt.add("base", 3);
                        if (c2.numberOfCharacters() != null) {
                            updateIsNonStatic(c2.numberOfCharacters().expression());
                            fmt.add("fieldwidth",
                                    visitAndDereference(c2.numberOfCharacters().expression()));
                        }
                    } else if (c1.bitFormat() instanceof BitFormat4Context) {
                        BitFormat4Context c2 = (BitFormat4Context) (c1.bitFormat());
                        fmt.add("base", 4);
                        if (c2.numberOfCharacters() != null) {
                            updateIsNonStatic(c2.numberOfCharacters().expression());
                            fmt.add("fieldwidth",
                                    visitAndDereference(c2.numberOfCharacters().expression()));
                        }
                    }
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (c1.timeFormat() != null) {
                    TimeFormatContext ffc = c1.timeFormat();
                    ST fmt = m_group.getInstanceOf("iojob_time_format");
                    updateIsNonStatic(ffc.fieldWidth().expression());
                    fmt.add("fieldwidth", visitAndDereference(ffc.fieldWidth().expression()));
                    if (ffc.decimalPositions() != null) {
                        updateIsNonStatic(ffc.decimalPositions().expression());
                        fmt.add("decimalPositions",
                                visitAndDereference(ffc.decimalPositions().expression()));
                    }
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (c1.durationFormat() != null) {
                    DurationFormatContext ffc = c1.durationFormat();
                    ST fmt = m_group.getInstanceOf("iojob_duration_format");
                    fmt.add("fieldwidth", visitAndDereference(ffc.fieldWidth().expression()));
                    updateIsNonStatic(ffc.fieldWidth().expression());
                    if (ffc.decimalPositions() != null) {
                        updateIsNonStatic(ffc.decimalPositions().expression());
                        fmt.add("decimalPositions",
                                visitAndDereference(ffc.decimalPositions().expression()));
                    }
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (c1.listFormat() != null) {
                    ST fmt = m_group.getInstanceOf("iojob_list_format");
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else {
                    System.out.println("untreated format " + c.format().getText());
                }

            }
        }
        if (ctx instanceof SmallPearlParser.FactorPositionContext) {
            FactorPositionContext c = (FactorPositionContext) (ctx);
            if (c.factor() != null) {
                updateIsNonStatic(c.factor());
                addFactorToFormatList(formatList, c.factor());
                nbrOfFormats++;
            }
            // treat position
            if (c.position().openClosePositionRST() != null) {
                ST fmt = m_group.getInstanceOf("iojob_rst");
                fmt.add("element", visitAndDereference(c.position().openClosePositionRST().name()));
                TypeFixed tf =
                        (TypeFixed) (m_ast.lookupType(c.position().openClosePositionRST().name()));
                fmt.add("size", tf.getPrecision());


                formatList.add("formats", fmt);
                setIsNonStatic();
                nbrOfFormats++;
            } else if (c.position().relativePosition() != null) {
                RelativePositionContext rp = c.position().relativePosition();

                if (rp.positionSKIP() != null) {
                    ExpressionContext e = rp.positionSKIP().expression();
                    ST fmt = m_group.getInstanceOf("iojob_position_skip");
                    if (e != null) {
                        updateIsNonStatic(e);
                        fmt.add("element", visitAndDereference(e));
                    }
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (rp.positionX() != null) {
                    ExpressionContext e = rp.positionX().expression();
                    ST fmt = m_group.getInstanceOf("iojob_position_x");
                    if (e != null) {
                        updateIsNonStatic(e);
                        fmt.add("element", visitAndDereference(e));
                    }
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (rp.positionPAGE() != null) {
                    ExpressionContext e = rp.positionPAGE().expression();
                    ST fmt = m_group.getInstanceOf("iojob_position_page");
                    if (e != null) {
                        updateIsNonStatic(e);
                        fmt.add("element", visitAndDereference(e));
                    }
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (rp.positionADV() != null) {
                    ST fmt = m_group.getInstanceOf("iojob_position_adv");
                    for (int i = 0; i < rp.positionADV().expression().size(); i++) {
                        ExpressionContext e = rp.positionADV().expression(i);
                        updateIsNonStatic(e);
                        fmt.add("expression" + (i + 1), visitAndDereference(e));
                    }
                    fmt.add("dimensions", rp.positionADV().expression().size());
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (rp.positionEOF() != null) {
                    ST fmt = m_group.getInstanceOf("iojob_position_eof");
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else {
                    System.out.println("untreated positioning: " + c.position().getText());
                }
            } else if (c.position().absolutePosition() != null) {
                AbsolutePositionContext ap = c.position().absolutePosition();
                if (ap.positionCOL() != null) {
                    ExpressionContext e = ap.positionCOL().expression();
                    ST fmt = m_group.getInstanceOf("iojob_position_col");
                    updateIsNonStatic(e);
                    fmt.add("element", visitAndDereference(e));
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (ap.positionLINE() != null) {
                    ExpressionContext e = ap.positionLINE().expression();
                    ST fmt = m_group.getInstanceOf("iojob_position_line");
                    updateIsNonStatic(e);
                    fmt.add("element", visitAndDereference(e));
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (ap.positionPOS() != null) {
                    ST fmt = m_group.getInstanceOf("iojob_position_pos");
                    for (int i = 0; i < ap.positionPOS().expression().size(); i++) {
                        ExpressionContext e = ap.positionPOS().expression(i);
                        updateIsNonStatic(e);
                        fmt.add("expression" + (i + 1), visitAndDereference(e));
                    }
                    fmt.add("dimensions", ap.positionPOS().expression().size());
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else if (ap.positionSOP() != null) {
                    ST fmt = m_group.getInstanceOf("iojob_position_sop");
                    for (int i = 0; i < ap.positionSOP().ID().size(); i++) {
                        fmt.add("expression" + (i + 1),
                                getUserVariableWithoutNamespace(ap.positionSOP().ID(i).getText()));
                        SymbolTableEntry se =
                                m_currentSymbolTable.lookup(ap.positionSOP().ID(i).getText());
                        if (se instanceof VariableEntry) {
                            TypeFixed tf = (TypeFixed) (((VariableEntry) se).getType());
                            fmt.add("size" + (i + 1), tf.getPrecision());
                        }
                        setIsNonStatic();
                    }
                    fmt.add("dimensions", ap.positionSOP().ID().size());
                    formatList.add("formats", fmt);
                    nbrOfFormats++;
                } else {
                    System.out.println("untreated positioning: " + c.position().getText());
                }
            }
        }

        if (ctx instanceof SmallPearlParser.FactorFormatPositionContext) {
            // System.out.println("(x)( , , , )  treatment");
            FactorFormatPositionContext c = (FactorFormatPositionContext) (ctx);
            //System.out.println("FactorFormatPosition"+c.getText());
            ST loop = m_group.getInstanceOf("iojob_format_loopstart");

            if (c.factor() != null) {
                if (c.factor().expression() != null) {
                    updateIsNonStatic(c.factor().expression());
                    loop.add("repetitions", visitAndDereference(c.factor().expression()));
                    formatList.add("formats", loop);
                    nbrOfFormats++;
                } else if (c.factor().integerWithoutPrecision() != null) {
                    String s = c.factor().integerWithoutPrecision().IntegerConstant().getText();
                    loop.add("repetitions", s);
                }
            }
            nbrOfFormats += addFormatPositionToFormatList(formatList, c.listOfFormatPositions());

            // decrement the number of format element, since the start_loop element
            // does not count, but all containing start_loop elements count
            loop.add("elements", nbrOfFormats - 1);
        }
        return nbrOfFormats;

    }

    private int addFormatPositionToFormatList(ST formatList, ListOfFormatPositionsContext list) {
        int length = 0;
        for (int i = 0; i < list.formatPosition().size(); i++) {
            length += addFormatPositionToFormatList(formatList, list.formatPosition(i));
        }
        return length;
    }

    private void addFactorToFormatList(ST formatList, FactorContext ctx) {
        ST loopStart = m_group.getInstanceOf("iojob_format_loopstart");

        if (ctx.expression() != null) {
            updateIsNonStatic(ctx.expression());
            System.out.println("FactorPosition  (" + ctx.expression().getText() + ")");
            loopStart.add("repetitions", visitAndDereference(ctx.expression()));
        }
        if (ctx.integerWithoutPrecision() != null) {
            loopStart.add("repetitions", ctx.integerWithoutPrecision().IntegerConstant().getText());
        }
        loopStart.add("elements", 1);
        formatList.add("formats", loopStart);
    }

    private ST getUserVariableWithoutNamespace(String user_variable) {
        ST st = m_group.getInstanceOf("user_variable");
        st.add("name", user_variable);
        return st;
    }

    /*
     * get username with namespace if the symbol comes from another module
     */
    private ST getUserVariable(SymbolTableEntry user_variable) {
        ST st = m_group.getInstanceOf("user_variable");
        st.add("name", user_variable.getName());
        if (user_variable.isSpecified()) {
            String fromModule = user_variable.getGlobalAttribute();
            if (!fromModule.equals(m_module.getName())) {
                ST fromns = m_group.getInstanceOf("fromNamespace");
                fromns.add("fromNs", fromModule);
                fromns.add("name",st);
                return fromns;
            }
        }
        
        return st;
    }

    @Override
    public ST visitSendStatement(SmallPearlParser.SendStatementContext ctx) {
        ST st = m_group.getInstanceOf("iojob_io_statement");
        ErrorStack.enter(ctx, "SEND");
        st.add("command", "send");

        st.add("dation", visitAndDereference(ctx.dationName().name()));

        addDataAndFormatListToST(st, ctx.listOfFormatPositions(), ctx.ioDataList());
        ErrorStack.leave();

        return st;
    }

    @Override
    public ST visitTakeStatement(SmallPearlParser.TakeStatementContext ctx) {
        ST st = m_group.getInstanceOf("iojob_io_statement");
        st.add("command", "take");
        ErrorStack.enter(ctx, "TAKE");

        st.add("dation", visitAndDereference(ctx.dationName().name()));

        addDataAndFormatListToST(st, ctx.listOfFormatPositions(), ctx.ioDataList());
        ErrorStack.leave();

        return st;
    }

    @Override
    public ST visitReadStatement(SmallPearlParser.ReadStatementContext ctx) {
        ST st = m_group.getInstanceOf("iojob_io_statement");
        st.add("command", "read");

        ErrorStack.enter(ctx, "READ");

        st.add("dation", visitAndDereference(ctx.dationName().name()));

        addDataAndFormatListToST(st, ctx.listOfFormatPositions(), ctx.ioDataList());

        ErrorStack.leave();

        return st;
    }

    @Override
    public ST visitWriteStatement(SmallPearlParser.WriteStatementContext ctx) {
        ST st = m_group.getInstanceOf("iojob_io_statement");
        st.add("command", "write");

        ErrorStack.enter(ctx, "WRITE");

        st.add("dation", visitAndDereference(ctx.dationName().name()));

        addDataAndFormatListToST(st, ctx.listOfFormatPositions(), ctx.ioDataList());

        ErrorStack.leave();

        return st;
    }


    @Override
    public ST visitCallStatement(SmallPearlParser.CallStatementContext ctx) {
        ST stmt = m_group.getInstanceOf("CallStatement");

        SymbolTableEntry se = m_currentSymbolTable.lookup(ctx.ID().toString());
        m_formalParameters = ((ProcedureEntry)se).getFormalParameters();
        
        String fromns = se.getGlobalAttribute();
        stmt.add("callee", ctx.ID());
        if (fromns != null && fromns.equals(m_module.getName())) {
            fromns = null;
        }
        stmt.add("fromns",fromns);
        if (ctx.listOfActualParameters() != null) {
            stmt.add("ListOfActualParameters",
                    visitListOfActualParameters(ctx.listOfActualParameters()));
        }

        return stmt;
    }

    @Override
    public ST visitListOfActualParameters(SmallPearlParser.ListOfActualParametersContext ctx) {
        ST stmt = m_group.getInstanceOf("ActualParameters");

        // let's see if we must pass an array
        if (ctx.expression() != null) {
            for (int i = 0; i < ctx.expression().size(); i++) {
                addParameter2StOfProcParamList(stmt, ctx.expression(i),m_formalParameters.get(i));
            }
        }
        return stmt;
    }

    /**
     * add an expression result to the actual parameter list for
     * a functionCall or a procedureCall
     * <p>
     * In case of the given expression is an array inside a struct, we must create
     * a local array-object with descriptor and array data
     * Segments/slices of arrays are not mentioned in the language report as valid
     * procedure actual arguments  
     *
     * @param stmt       the ST context which holds all parameters
     * @param expression the current parameter
     */
    private void addParameter2StOfProcParamList(ST stmt, ExpressionContext expression, FormalParameter formalParameter) {
        boolean treatArray = false;
        SymbolTableEntry se = null;
        ASTAttribute attr = null;

        attr = m_ast.lookup(expression);
        se = attr.getSymbolTableEntry();
//        if (attr != null) {
//            if (attr.getType() instanceof TypeArray) {
//                treatArray = true;
//            }
//            if (attr.getVariable() != null) {
//                String var = attr.getVariable().getName();
//                se = m_currentSymbolTable.lookup(var);
//            }
//        }


        if (attr.getType() instanceof TypeArray ) {
            if (((VariableEntry)se).getType() instanceof TypeArray) {
                ST param = m_group.getInstanceOf("ActualParameters");
                param.add("ActualParameter", visitAndDereference(expression));
                stmt.add("ActualParameter", param);
            } else  if (((VariableEntry)se).getType() instanceof TypeStructure) {
              // need temporary Array-object
              // ArrayVariableDeclaration(name,type,descriptor) 
                String tempVarName = nextTempVarName();
                
                ST temp = m_group.getInstanceOf("ArrayVariableDeclaration");
                temp.add("name",  tempVarName);
                temp.add("type", visitTypeAttribute(((TypeArray)(attr.getType())).getBaseType()));
                temp.add("descriptor", getArrayDescriptor(attr.getType()));
                temp.add("storage",visitAndDereference(expression));
                temp.add("no_decoration", 1);
                m_tempVariableList.lastElement().add("variable", temp);
                
                ST param = m_group.getInstanceOf("ActualParameters");
                param.add("ActualParameter", tempVarName);
                stmt.add("ActualParameter", param);
              
            }
            
        } else {
            // scalar type
            if (attr.getType() instanceof TypeVariableChar) {
                ST temp = m_group.getInstanceOf("TempCharVariable");

                String tempVarName = nextTempVarName();

                TypeVariableChar t = (TypeVariableChar) (attr.getType());
                temp.add("char_size", t.getBaseType().getPrecision());
                temp.add("variable", tempVarName);
                temp.add("expr", visitAndDereference(expression));
                ST param = m_group.getInstanceOf("ActualParameters");
                param.add("ActualParameter", tempVarName);
                stmt.add("ActualParameter", param);

                m_tempVariableList.lastElement().add("variable", temp);
            } else {
                ST param = m_group.getInstanceOf("ActualParameters");
                if (formalParameter.getType() instanceof TypeReference) {
                    param.add("ActualParameter", visit(expression)); 
                } else {
                   param.add("ActualParameter", visitAndDereference(expression));
                }
                stmt.add("ActualParameter", param);
            }
        }
    }

    private ST getActualParameters(List<SmallPearlParser.ExpressionContext> parameters) {
        ST stmt = m_group.getInstanceOf("ActualParameters");

        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                addParameter2StOfProcParamList(stmt, parameters.get(i),m_formalParameters.get(i));
            }
        }

        return stmt;
    }

    private ST getIndices(List<SmallPearlParser.ExpressionContext> indices) {
        ST st = m_group.getInstanceOf("ArrayIndices");

        if (indices != null) {
            for (int i = 0; i < indices.size(); i++) {
                ST stIndex = m_group.getInstanceOf("ArrayIndex");
                stIndex.add("index", visitAndDereference(indices.get(i)));
                st.add("indices", stIndex);
            }
        }

        return st;
    }

    @Override
    public ST visitCpp_inline(SmallPearlParser.Cpp_inlineContext ctx) {
        ST stmt = m_group.getInstanceOf("cpp_inline");

        stmt.add("body", "#warning __cpp__ inline inserted");

        int i;
        for (i = 0; i < ctx.CppStringLiteral().size(); i++) {
            String line = ctx.CppStringLiteral(i).toString();

            line = line.replaceAll("^\"", "");
            line = line.replaceAll("\"$", "");

            line = CommonUtils.unescapeCppString(line);

            stmt.add("body", line);
        }

        return stmt;
    }

    @Override
    public ST visitAtanExpression(SmallPearlParser.AtanExpressionContext ctx) {
        ST st = m_group.getInstanceOf("ATAN");
        st.add("operand", visitAndDereference(ctx.expression()));//getChild(1)));
        // if applied to FIXED type we must to FLOAT before applying the operator
        addFixedFloatConversion(st, ctx.expression());

        return st;
    }

    @Override
    public ST visitCosExpression(SmallPearlParser.CosExpressionContext ctx) {
        ST st = m_group.getInstanceOf("COS");
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));
        // if applied to FIXED type we must to FLOAT before applying the operator
        addFixedFloatConversion(st, ctx.expression());

        return st;
    }

    @Override
    public ST visitExpExpression(SmallPearlParser.ExpExpressionContext ctx) {
        ST st = m_group.getInstanceOf("EXP");
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));
        // if applied to FIXED type we must to FLOAT before applying the operator
        addFixedFloatConversion(st, ctx.expression());

        return st;
    }

    @Override
    public ST visitLnExpression(SmallPearlParser.LnExpressionContext ctx) {
        ST st = m_group.getInstanceOf("LN");
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));

        // if applied to FIXED type we must to FLOAT before applying the operator
        addFixedFloatConversion(st, ctx.expression());

        return st;
    }

    @Override
    public ST visitSinExpression(SmallPearlParser.SinExpressionContext ctx) {
        ST st = m_group.getInstanceOf("SIN");
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));
        // if applied to FIXED type we must to FLOAT before applying the operator
        addFixedFloatConversion(st, ctx.expression());
        return st;
    }

    @Override
    public ST visitSqrtExpression(SmallPearlParser.SqrtExpressionContext ctx) {
        ST st = m_group.getInstanceOf("SQRT");
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));

        // if applied to FIXED type we must to FLOAT before applying the operator
        addFixedFloatConversion(st, ctx.expression());

        return st;
    }

    private Void addFixedFloatConversion(ST st, ExpressionContext ctxExpr) {
        ASTAttribute op = m_ast.lookup(ctxExpr);

        if (op.getType() instanceof TypeFixed) {
            int precision = ((TypeFixed) op.getType()).getPrecision();
            if (precision > Defaults.FLOAT_SHORT_PRECISION) {
                precision = Defaults.FLOAT_LONG_PRECISION;
            } else {
                precision = Defaults.FLOAT_SHORT_PRECISION;
            }
            st.add("convert_to", precision);
        }

        return null;
    }

    private Void addFixedFloatConversion(ST st, SmallPearlParser.ExpressionContext ctx,
            int indexOfExpression) {
        ASTAttribute op = m_ast.lookup(ctx);

        if (op.getType() instanceof TypeFixed) {
            int precision = ((TypeFixed) op.getType()).getPrecision();
            if (precision > Defaults.FLOAT_SHORT_PRECISION) {
                precision = Defaults.FLOAT_LONG_PRECISION;
            } else {
                precision = Defaults.FLOAT_SHORT_PRECISION;
            }
            st.add("convert_to" + indexOfExpression, precision);
        }

        return null;
    }

    @Override
    public ST visitTanExpression(SmallPearlParser.TanExpressionContext ctx) {
        ST st = m_group.getInstanceOf("TAN");
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));

        // if applied to FIXED type we must to FLOAT before applying the operator
        addFixedFloatConversion(st, ctx.expression());

        return st;
    }

    @Override
    public ST visitTanhExpression(SmallPearlParser.TanhExpressionContext ctx) {
        ST st = m_group.getInstanceOf("TANH");
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));

        // if applied to FIXED type we must to FLOAT before applying the operator
        addFixedFloatConversion(st, ctx.expression());

        return st;
    }

    @Override
    public ST visitAbsExpression(SmallPearlParser.AbsExpressionContext ctx) {
        ST st = m_group.getInstanceOf("ABS");
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));
        return st;
    }

    @Override
    public ST visitSignExpression(SmallPearlParser.SignExpressionContext ctx) {
        ST st = m_group.getInstanceOf("SIGN");
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));
        return st;
    }

    @Override
    public ST visitRemainderExpression(SmallPearlParser.RemainderExpressionContext ctx) {
        ST st = m_group.getInstanceOf("REM");
        st.add("lhs", visitAndDereference(ctx.expression(0)));
        st.add("rhs", visitAndDereference(ctx.expression(1)));
        return st;
    }

    @Override
    public ST visitSizeofExpression(SmallPearlParser.SizeofExpressionContext ctx) {
        ST st = m_group.getInstanceOf("SIZEOF");
        ASTAttribute attr = m_ast.lookup(ctx);

        if (ctx.name() != null) {
            st.add("operand", visitName(ctx.name()));
        } else if (ctx.simpleType() != null) {
            String typeName = "";
            if (ctx.simpleType().typeInteger() != null) {
                long length = Defaults.FIXED_LENGTH;
                if (ctx.simpleType().typeInteger().mprecision() != null) {
                    String s = ctx.simpleType().typeInteger().mprecision().integerWithoutPrecision()
                            .getText();
                    length = Integer.parseInt(ctx.simpleType().typeInteger().mprecision()
                            .integerWithoutPrecision().getText());
                }
                typeName = "pearlrt::Fixed<" + length + ">";
            } else if (ctx.simpleType().typeDuration() != null) {
                typeName = "pearlrt::Duration";
            } else if (ctx.simpleType().typeClock() != null) {
                typeName = "pearlrt::Clock";
            } else if (ctx.simpleType().typeFloatingPointNumber() != null) {
                long length = Defaults.FLOAT_PRECISION;
                if (ctx.simpleType().typeFloatingPointNumber().length() != null) {
                    length = Integer.parseInt(
                            ctx.simpleType().typeFloatingPointNumber().length().IntegerConstant().toString());
                }
                typeName = "pearlrt::Float<" + length + ">";
            } else if (ctx.simpleType().typeBitString() != null) {
                long length = Defaults.BIT_LENGTH;
                if (ctx.simpleType().typeBitString().length() != null) {
                    length = Integer.parseInt(ctx.simpleType().typeBitString().length().IntegerConstant().toString());
                }
                typeName = "pearlrt::BitString<" + length + ">";
            } else if (ctx.simpleType().typeCharacterString() != null) {
                long length = Defaults.CHARACTER_LENGTH;
                if (ctx.simpleType().typeCharacterString().length() != null) {
                    length = Integer
                            .parseInt(ctx.simpleType().typeCharacterString().length().IntegerConstant().toString());
                }
                typeName = "pearlrt::Character<" + length + ">";
            } else {
                // emergency -- set compiler internal type --> cause c++ errors
                typeName = ctx.simpleType().getText().toString();
            }

            st.add("operand", typeName);
        }
        //st.add("operand", visitAndDereference(ctx.getChild(1)));
        return st;
    }

    @Override
    public ST visitEntierExpression(SmallPearlParser.EntierExpressionContext ctx) {
        ST st = m_group.getInstanceOf("ENTIER");
        if (m_debug) {
            System.out.println("CppCodeGeneratorVisitor: visitEntierExpression");
        }
        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));
        return st;
    }

    @Override
    public ST visitRoundExpression(SmallPearlParser.RoundExpressionContext ctx) {
        ST st = m_group.getInstanceOf("ROUND");
        if (m_debug) {
            System.out.println("CppCodeGeneratorVisitor: visitRoundExpression");
        }

        st.add("operand", visitAndDereference(ctx.expression())); //getChild(1)));
        return st;
    }

    @Override
    public ST visitEqRelationalExpression(SmallPearlParser.EqRelationalExpressionContext ctx) {
        ST st = m_group.getInstanceOf("EQ");

        st.add("lhs", visitAndDereference(ctx.expression(0)));
        st.add("rhs", visitAndDereference(ctx.expression(1)));

        return st;
    }

    @Override
    public ST visitNeRelationalExpression(SmallPearlParser.NeRelationalExpressionContext ctx) {
        ST st = m_group.getInstanceOf("NE");

        st.add("lhs", visitAndDereference(ctx.expression(0)));
        st.add("rhs", visitAndDereference(ctx.expression(1)));

        return st;
    }

    @Override
    public ST visitLtRelationalExpression(SmallPearlParser.LtRelationalExpressionContext ctx) {
        ST st = m_group.getInstanceOf("LT");

        st.add("lhs", visitAndDereference(ctx.expression(0)));
        st.add("rhs", visitAndDereference(ctx.expression(1)));

        return st;
    }

    @Override
    public ST visitLeRelationalExpression(SmallPearlParser.LeRelationalExpressionContext ctx) {
        ST st = m_group.getInstanceOf("LE");

        st.add("lhs", visitAndDereference(ctx.expression(0)));
        st.add("rhs", visitAndDereference(ctx.expression(1)));

        return st;
    }

    @Override
    public ST visitGtRelationalExpression(SmallPearlParser.GtRelationalExpressionContext ctx) {
        ST st = m_group.getInstanceOf("GT");

        st.add("lhs", visitAndDereference(ctx.expression(0)));
        st.add("rhs", visitAndDereference(ctx.expression(1)));

        return st;
    }

    @Override
    public ST visitGeRelationalExpression(SmallPearlParser.GeRelationalExpressionContext ctx) {
        ST st = m_group.getInstanceOf("GE");

        st.add("lhs", visitAndDereference(ctx.expression(0)));
        st.add("rhs", visitAndDereference(ctx.expression(1)));

        return st;
    }

    @Override
    public ST visitIsRelationalExpression(SmallPearlParser.IsRelationalExpressionContext ctx) {
        ST st = m_group.getInstanceOf("EQ");

        treatIsIsnt(st, ctx.expression(0), ctx.expression(1));
        return st;
    }


    @Override
    public ST visitIsntRelationalExpression(SmallPearlParser.IsntRelationalExpressionContext ctx) {
        ST st = m_group.getInstanceOf("NE");
        treatIsIsnt(st, ctx.expression(0), ctx.expression(1));
        return st;
    }

    /* we have to deal with 
      lhs            rhs         
      REF           type    lhs/rhs=visit(ctx0/1)
      type          REF     lhs/rhs=visit(ctx0/1)
      REF()         REF()   rhs/lhs=visit(ctx0/1)
      REF()         ARRAY   lhs=visit; rhs=arrayReference(...)
      REF()         NIL     rhs/lhs=visit(ctx0/1)
      ARRAY         REF()   lhs=arrayRef(...), rhs=visit
      NIL           REF()   rhs/lhs=visit(ctx0/1)
      NIL           NIL     rhs/lhs=visit(ctx0/1)

     */
    private void treatIsIsnt(ST st, ExpressionContext ctx0, ExpressionContext ctx1) {
        ASTAttribute attrLhs = m_ast.lookup(ctx0);
        ASTAttribute attrRhs = m_ast.lookup(ctx1);

        // lhs is Array
        if (attrLhs.getType() instanceof TypeArray) {
            ST ar = m_group.getInstanceOf("arrayReference");
            TypeDefinition td = ((TypeArray) attrLhs.getType()).getBaseType();
            ar.add("basetype", td.toST(m_group));
            ar.add("descriptor", getArrayDescriptor(attrRhs.getVariable()));
            ar.add("data", "data_" + attrLhs.getVariable().getName());
            st.add("lhs", ar);
        } else {
            st.add("lhs", visit(ctx0));
        }
        // rhs is Array
        if (attrRhs.getType() instanceof TypeArray) {
            ST ar = m_group.getInstanceOf("arrayReference");
            TypeDefinition td = ((TypeArray) attrRhs.getType()).getBaseType();
            ar.add("basetype", td.toST(m_group));
            ar.add("descriptor", getArrayDescriptor(attrRhs.getVariable()));
            ar.add("data", "data_" + attrRhs.getVariable().getName());
            st.add("rhs", ar);
        } else {
            st.add("rhs", visit(ctx1));
        }


        return;
    }

    @Override
    public ST visitFitExpression(SmallPearlParser.FitExpressionContext ctx) {
        ST st = m_group.getInstanceOf("FIT");

        st.add("lhs", visitAndDereference(ctx.expression(0)));
        st.add("rhs", visitAndDereference(ctx.expression(1)));

        return st;
    }

    @Override
    public ST visitNowFunction(SmallPearlParser.NowFunctionContext ctx) {
        ST st = m_group.getInstanceOf("NOW");
        return st;
    }

    private ST visitDationDenotation(VariableEntry ve) {

        String name = ve.getName();
        TypeDation td = (TypeDation)(ve.getType());
        int tfuRecord = -1;
        ST typeDation = m_group.getInstanceOf("TypeDation");

        typeDation =  getTypeDation(td);

        ST typology = m_group.getInstanceOf("Typology");
        ST accessAttributes = m_group.getInstanceOf("AccessAttribute");

        if (td.hasTypology()) {
            typology = getTypology(td);
        }
        if (td.hasTfu()) {
            tfuRecord = td.getDimension3();
            if (tfuRecord < 0) {
                tfuRecord = td.getDimension2();
            }
            if (tfuRecord < 0) {
                tfuRecord = td.getDimension1();
            }
        }
        //
        if (!(td.isBasic() && td.getTypeOfTransmission()!= null)) {
            // not needed for DationTS
            if (td.isDirect()) accessAttributes.add("attribute", "DIRECT");
            if (td.isForward()) accessAttributes.add("attribute", "FORWARD");
            if (td.isCyclic()) accessAttributes.add("attribute", "CYCLIC");
            else accessAttributes.add("attribute", "NOCYCL");
            if (td.isStream()) accessAttributes.add("attribute", "STREAM");
            else accessAttributes.add("attribute", "NOSTREAM");
            typeDation.add("AccessAttribute", accessAttributes);
        }
       

        //dationDeclarations.add("decl",m_identifierDenotationList.get(i));
        ST v = m_group.getInstanceOf("DationDeclaration");
        v.add("name", ve.getName());
        //
        if (td.hasTypology()) {
            typology.add("name", ve.getName());

        }
    
        v.add("Dation", getDationClass(td));

        
        if (td.hasTypology()) {
            typeDation.add("Dim", ve.getName());
        }
        ST scope = getScope(ve);
        scope.add("variable", v);
        m_dationDeclarations.add("decl", scope);

        ST dationInitialiser = m_group.getInstanceOf("DationDeclarationInitialiser");
        // name,Dation,TypeDation,Id,Typology, tfu
        dationInitialiser.add("name",  ve.getName());
        dationInitialiser.add("Dation", getDationClass(td));
        dationInitialiser.add("TypeDation", typeDation);
        dationInitialiser.add("Id", getUserVariable(td.getCreatedOn()));
        dationInitialiser.add("Typology", typology);
        if (tfuRecord > 0) {
            dationInitialiser.add("tfu", tfuRecord);
        }
        m_dationDeclarationInitializers.add("decl", dationInitialiser);


        return null;
    }


    private ST getTypeDation(TypeDation td) { //SmallPearlParser.TypeDationContext ctx, String dationClass) {
        ST st = m_group.getInstanceOf("TypeDation");
        ST sourceSinkAttributte = m_group.getInstanceOf("SourceSinkAttribute");
        sourceSinkAttributte.add("attribute",  td.getDirectionAsString());
        st.add("SourceSinkAttribute", sourceSinkAttributte);

        //      if (dationClass.equals("DationRW") && ctx.classAttribute() != null) {
        st.add("ClassAttribute", getClassAttribute(td));
        //      }

        return st;
    }



    private ST getTypology(TypeDation t) {
        ST st = m_group.getInstanceOf("Typology");
        // let's define the three dimension values with impossible preset
        // 

        int d1 = t.getDimension1();
        int d2 = t.getDimension2();
        int d3 = t.getDimension3();

        if (d1 == -1) {              // '*'
            st.add("DIM1",-1);
            st.add("DIM1Unlimited", 1);
        } else if ( d1 > 0) {
            st.add("DIM1",d1);
        }
        if (d2 > 0) {
            st.add("DIM2",d2);
        }
        if (d3 > 0) {
            st.add("DIM3",d3);
        }


        
        return st;
    }

    // OpenPEARL Language Report: 11.5
    //
    // | BASIC | ALPHIC | ALL / type
    // -------+---------------+----------------+----------------
    // SYSTEM | SystemDationB | SystemDationNB | SystemDationNB
    // | DationTS | DationPG | DationRW
    // -------+---------------+----------------+----------------

    private String getDationClass(TypeDation td)
            throws InternalCompilerErrorException {
        if (td.isSystemDation()) {
            if (td.isBasic()) {
                return "SystemDationB";
            }

            if (td.isAlphic()) {
                return "SystemDationNB";
            }

            return "SystemDationNB";
        }

        if (td.isBasic()) {
            return "DationTS";
        } else if (td.isAlphic()) {
            return "DationPG";
        } else {
            return "DationRW";
        }
    }

    private ST getStepSize(SmallPearlParser.ClassAttributeContext ctx) {
        ST st = m_group.getInstanceOf("StepSize");

        st.add("type", "Fixed");
        st.add("size", "31");

        return st;
    }

    private ST getClassAttribute(TypeDation td) {
        ST st = m_group.getInstanceOf("ClassAttribute");

        if (td.isSystemDation()) {
            st.add("system", "1");
        }

        if (td.isAlphic()) {
            st.add("alphic", "1");
        } else if (td.isBasic()) {
            st.add("basic", "1");
        } else if (td.getTypeOfTransmission() != null) {
            st.add("attribute",getTypeOfTransmissionData(td));
        }

        return st;
    }

    private ST getTypeOfTransmissionData(TypeDation td) {
        ST st = m_group.getInstanceOf("TypeOfTransmissionData");

        if (td.getTypeOfTransmission().equals("ALL")) {
            st.add("all", "1");
            //        } else if (ctx instanceof SmallPearlParser.TypeOfTransmissionDataSimpleTypeContext) {
            //            SmallPearlParser.TypeOfTransmissionDataSimpleTypeContext c =
            //                    (SmallPearlParser.TypeOfTransmissionDataSimpleTypeContext) ctx;
            //            st.add("type", visitSimpleType(c.simpleType()));
        } else {
            st.add("type", getSTforType(td.getTypeOfTransmissionAsType()));
        }

        return st;
    }

    private ST getSTforType(TypeDefinition t) {
        ST st= null;

        if (t instanceof TypeFixed) {
            st =  m_group.getInstanceOf("TypeInteger");
            st.add("size", ((TypeFixed)(t)).getPrecision());
        } else if (t instanceof TypeDuration) {
            st =  m_group.getInstanceOf("TypeDuration");
        } else if (t instanceof TypeBit) {
            st =  m_group.getInstanceOf("TypeBitString");
            st.add("length", ((TypeBit)(t)).getPrecision());
        } else if (t instanceof TypeFloat) {
            st =  m_group.getInstanceOf("TypeFloatingPointNumber");
            st.add("precision", ((TypeFloat)(t)).getPrecision());
        } else if (t instanceof TypeClock) {
            st =  m_group.getInstanceOf("TypeClock");
        } else if (t instanceof  TypeChar) {
            st =  m_group.getInstanceOf("TypeCharacterString");
            st.add("size", ((TypeChar)(t)).getPrecision());
        } else if (t instanceof TypeSemaphore) {
            st = m_group.getInstanceOf("sema_type");
        } else if (t instanceof TypeBolt) {
            st = m_group.getInstanceOf("bolt_type");
        } else {
            System.err.println("CppCodeGen: missing alternative@4591");
        }


        return st;
    }


    @Override
    public ST visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        ST st = m_group.getInstanceOf("block_statement");
        ST blockBody = m_group.getInstanceOf("Body");
        st.add("code", blockBody);
        BlockEntry be = (BlockEntry) (m_currentSymbolTable.lookupLoopBlock(ctx));
        ASTAttribute attr = m_ast.lookup(ctx);
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        addVariableDeclarationsToST(blockBody);

        for (ParseTree c : ctx.children) {
            if (c instanceof SmallPearlParser.VariableDeclarationContext) {
                //   st.add("code",
                //           visitVariableDeclaration((SmallPearlParser.VariableDeclarationContext) c));
            } else if (c instanceof SmallPearlParser.StatementContext) {
                blockBody.add("statements", visitStatement((SmallPearlParser.StatementContext) c));
            }
        }

        if (be.isUsed()) {
            st.add("id", be.getName());
            if (attr == null || !attr.isInternal()) {
                // no ASTAttribut found -> we have a user supplied name
                st.add("isUserLabel", 1);
            }
        }

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();

        return st;
    }

    @Override
    public ST visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        ST st = m_group.getInstanceOf("LoopStatement");
        TypeDefinition fromType = null;
        TypeDefinition toType = null;
        TypeDefinition byType = null;
        Integer rangePrecision = 0;
        Boolean loopCounterNeeded = false;
        LoopEntry le = (LoopEntry) (m_currentSymbolTable.lookupLoopBlock(ctx));
        ASTAttribute attr = m_ast.lookup(ctx);
        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        st.add("srcLine", ctx.start.getLine());

        if (ctx.loopStatement_for() != null) {
            SymbolTableEntry se =
                    m_currentSymbolTable.lookup(ctx.loopStatement_for().ID().toString());
            rangePrecision = ((TypeFixed) (((VariableEntry) se).getType())).getPrecision();

            st.add("variable", ctx.loopStatement_for().ID().toString());
            loopCounterNeeded = true;
        }

        if (ctx.loopStatement_from() != null) {
            boolean old_map_to_const = m_map_to_const;

            fromType = m_ast.lookupType(ctx.loopStatement_from().expression());

            m_map_to_const = true;
            st.add("from", visitAndDereference(ctx.loopStatement_from().expression()));
            m_map_to_const = old_map_to_const;
        }

        if (ctx.loopStatement_to() != null) {
            boolean old_map_to_const = m_map_to_const;

            toType = m_ast.lookupType(ctx.loopStatement_to().expression());

            m_map_to_const = true;
            st.add("to", visitAndDereference(ctx.loopStatement_to().expression()));
            m_map_to_const = old_map_to_const;

            loopCounterNeeded = true;
        }

        if (ctx.loopStatement_by() != null) {
            boolean old_map_to_const = m_map_to_const;
            byType = m_ast.lookupType(ctx.loopStatement_by().expression());
            m_map_to_const = true;
            st.add("by", visitAndDereference(ctx.loopStatement_by().expression()));
            m_map_to_const = old_map_to_const;

            loopCounterNeeded = true;
        }

        if (rangePrecision == 0) {
            // we have no loop control variable!
            // derive the precision from from/to and by

            if (fromType != null && toType != null) {
                rangePrecision = Math.max(((TypeFixed) fromType).getPrecision(),
                        ((TypeFixed) toType).getPrecision());
                //  st.add("fromPrecision", rangePrecision);
                //  st.add("toPrecision", rangePrecision);
                loopCounterNeeded = true;
            } else if (fromType != null && toType == null) {
                rangePrecision = ((TypeFixed) fromType).getPrecision();
                //   st.add("fromPrecision", ((TypeFixed) fromType).getPrecision());
            } else if (fromType == null && toType != null) {
                rangePrecision = ((TypeFixed) toType).getPrecision();
                //  st.add("toPrecision", ((TypeFixed) toType).getPrecision());
                loopCounterNeeded = true;
            }
            if (byType != null) {
                rangePrecision = Math.max(rangePrecision, ((TypeFixed) byType).getPrecision());
            }
        }


        st.add("precision", rangePrecision);

        
        if (ctx.loopStatement_while() != null && ctx.loopStatement_while().expression() != null) {
            ST wc = visitAndDereference(ctx.loopStatement_while().expression());
            //String s = wc.toString();
            if (wc.toString().length() > 0) {
                ST cast = m_group.getInstanceOf("CastBitToBoolean");
                cast.add("name", wc);
                st.add("while_cond", cast);
            }
        }

        ST loopBody = m_group.getInstanceOf("Body");
        st.add("body", loopBody);
        addVariableDeclarationsToST(loopBody);

        for (int i = 0; i < ctx.loopBody().statement().size(); i++) {
            loopBody.add("statements", visitStatement(ctx.loopBody().statement(i)));
        }


        if ((ctx.loopStatement_to() != null) || (ctx.loopStatement_for() != null)
                || (ctx.loopStatement_by() != null)) {
            st.add("countLoopPass", 1);
        }

        if (loopCounterNeeded) {
            st.add("GenerateLoopCounter", 1);
        }

        if (le.isUsed()) {
            st.add("label_end", le.getName());
            if (attr == null || !attr.isInternal()) {
                // no ASTAttribut found -> we have a user supplied name
                st.add("isUserLabel", 1);
            }
        }

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();

        return st;
    }

    @Override
    public ST visitExitStatement(SmallPearlParser.ExitStatementContext ctx) {
        ST st = m_group.getInstanceOf("ExitStatement");
        ASTAttribute attr = m_ast.lookup(ctx);

        SymbolTableEntry se = attr.getSymbolTableEntry();
        st.add("label", se.getName());
        attr = m_ast.lookup(se.getCtx());
        if (attr == null || !(attr.isInternal())) {
            st.add("isUserLabel", 1);
        }

        return st;
    }


    @Override
    public ST visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        ST st = m_group.getInstanceOf("ProcedureDeclaration");
        st.add("id", ctx.nameOfModuleTaskProc().ID().getText());

        SymbolTableEntry se =
                m_currentSymbolTable.lookup(ctx.nameOfModuleTaskProc().ID().toString());

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        LinkedList<VariableEntry> entries = m_currentSymbolTable.getVariableDeclarations();


        for (ParseTree c : ctx.typeProcedure().children) {
            //if (c instanceof SmallPearlParser.ProcedureBodyContext) {
            //    st.add("body",
            //            visitProcedureBody((SmallPearlParser.ProcedureBodyContext) c));
            //} else
            if (c instanceof SmallPearlParser.ResultAttributeContext) {
                st.add("resultAttribute", getResultAttributte((ProcedureEntry) se));

            } else if (c instanceof SmallPearlParser.ListOfFormalParametersContext) {
                st.add("listOfFormalParameters", visitListOfFormalParameters(
                        (SmallPearlParser.ListOfFormalParametersContext) c));
            }
        }

        if (ctx.globalAttribute() != null) {
            visit(ctx.globalAttribute());
        }

        st.add("body", visit(ctx.procedureBody()));

        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        
        ST scope = getScope(se);
        scope.add("variable", st);
        return scope;
    }

    private ST getResultAttributte(ProcedureEntry pe) {
        ST st = m_group.getInstanceOf("ResultAttribute");
        st.add("type", pe.getResultType().toST(m_group));
        m_resultType = pe.getResultType();
        return st;
    }


    @Override
    public ST visitListOfFormalParameters(SmallPearlParser.ListOfFormalParametersContext ctx) {
        ST st = m_group.getInstanceOf("ListOfFormalParameters");

        if (ctx != null) {
            for (int i = 0; i < ctx.formalParameter().size(); i++) {
                st.add("FormalParameters", visitFormalParameter(ctx.formalParameter(i)));
            }
        }

        return st;
    }

    @Override
    public ST visitFormalParameter(SmallPearlParser.FormalParameterContext ctx) {
        ST st = m_group.getInstanceOf("FormalParameters");

        if (ctx != null) {
            for (int i = 0; i < ctx.identifier().size(); i++) {
                boolean treatArray = false;
                boolean treatStructure = false;
                String typeName = "";

                ST param = m_group.getInstanceOf("FormalParameter");

                // test if we have an parameter of type array
                SymbolTableEntry se =
                        m_currentSymbolTable.lookup(ctx.identifier(i).ID().toString());

                if (se instanceof VariableEntry) {
                    VariableEntry ve = (VariableEntry) se;

                    if (ve.getType() instanceof TypeArray) {
                        treatArray = true;

                    } else if (ve.getType() instanceof TypeStructure) {
                        treatStructure = true;
                        typeName = ((TypeStructure) ve.getType()).getStructureName();

                    }
                }

                if (treatArray) {
                  param.add("isArray", "");
                }
                param.add("id", ctx.identifier(i).ID());

                if (treatStructure) {
                    param.add("type", typeName);
                } else {
                    param.add("type", visitParameterType(ctx.parameterType()));
                }
                if (ctx.assignmentProtection() != null) {
                    param.add("assignmentProtection", "");
                }

                if (ctx.passIdentical() != null) {
                    param.add("passIdentical", "");
                }

                st.add("FormalParameter", param);

            }
        }

        return st;
    }

    @Override
    public ST visitParameterType(SmallPearlParser.ParameterTypeContext ctx) {
        ST st = m_group.getInstanceOf("ParameterType");

        for (ParseTree c : ctx.children) {
            if (c instanceof SmallPearlParser.SimpleTypeContext) {
                st.add("type", visitSimpleType(ctx.simpleType()));
            } else if (c instanceof SmallPearlParser.TypeReferenceContext) {
                st.add("type", visitTypeReference(ctx.typeReference()));
            } else if (c instanceof SmallPearlParser.TypeStructureContext) {
                st.add("type", visitTypeStructure(ctx.typeStructure()));
            } else if (c instanceof SmallPearlParser.TypeRealTimeObjectContext) {
                TypeRealTimeObjectContext rto = (TypeRealTimeObjectContext)c;
                if (rto.typeBolt() != null) {
                    st.add("type", visitTypeBolt(rto.typeBolt()));
                } else if (rto.typeSema() != null) {
                   st.add("type", visitTypeSema(rto.typeSema()));
                } else if (rto.typeInterrupt() != null) {
                   st.add("type", visitTypeInterrupt(rto.typeInterrupt()));
// remove until SIGNAL implementation starts                
//            } else if (c instanceof SmallPearlParser.TypeSignalContext) {
//                st.add("type", visitTypeSignal((TypeSignalContext)c));
                }
            } else {
                System.err.println("CppCodeGen:visitParameterType: untreated type "
                        + c.getClass().getCanonicalName());
            }
        }

        return st;
    }
   
    @Override
    public ST visitTypeTask(SmallPearlParser.TypeTaskContext ctx) {
        ST st = m_group.getInstanceOf("task_type");
        return st;
    }
    
    @Override
    public ST visitTypeBolt(SmallPearlParser.TypeBoltContext ctx) {
        ST st = m_group.getInstanceOf("bolt_type");
        return st;
    }

    @Override
    public ST visitTypeSema(SmallPearlParser.TypeSemaContext ctx) {
        ST st = m_group.getInstanceOf("sema_type");
        return st;
    }
    
    @Override
    public ST visitTypeInterrupt(SmallPearlParser.TypeInterruptContext ctx) {
        ST st = m_group.getInstanceOf("interrupt_type");
        return st;
    }

    @Override
    public ST visitTypeSignal(SmallPearlParser.TypeSignalContext ctx) {
        ST st = m_group.getInstanceOf("signal_type");
        return st;
    }
    @Override
    public ST visitProcedureBody(SmallPearlParser.ProcedureBodyContext ctx) {
        ST st = m_group.getInstanceOf("Body");
        //this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);

        addVariableDeclarationsToST(st);

        if (ctx != null && ctx.children != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.VariableDeclarationContext) {
                   
                } else if (c instanceof SmallPearlParser.StatementContext) {
                    st.add("statements", visitStatement((SmallPearlParser.StatementContext) c));
                   
                }
            }
        }

        return st;
    }

    private void addVariableDeclarationsToST(ST st) {
        // get variable entries from SymbolTable and create code for their definition
        // ignore loop control variables

        LinkedList<VariableEntry> entries = m_currentSymbolTable.getVariableDeclarations();

        for (int i=0; i<entries.size(); i++) {
            VariableEntry ve = (VariableEntry)(entries.get(i));
            if (ve.getLoopControlVariable()) continue;  
            //System.out.println("BODY: "+ve.getName()+": " + ve.getType()+" "+ve.isSpecified());
            if (ve.isSpecified()) {
                // no specifications allowed - identifications are no treated yet
            } else {
                st.add("declarations", generateVariableDeclaration(ve));
            }

        }

    }

 
    @Override
    public ST visitResultAttribute(SmallPearlParser.ResultAttributeContext ctx) {
        ST st = m_group.getInstanceOf("ResultAttribute");
        st.add("resultType", visitResultType(ctx.resultType()));
        ASTAttribute attr = m_ast.lookup(ctx);
        if (attr != null) {
            m_resultType = attr.getType();
        }
        return st;
    }

    @Override
    public ST visitResultType(SmallPearlParser.ResultTypeContext ctx) {
        ST st = m_group.getInstanceOf("ResultType");

        for (ParseTree c : ctx.children) {
            if (c instanceof SmallPearlParser.SimpleTypeContext) {
                st.add("type", visitSimpleType(ctx.simpleType()));
            } else if (c instanceof SmallPearlParser.TypeReferenceContext) {
                st.add("type", visitTypeReference(ctx.typeReference()));
            } else if (c instanceof SmallPearlParser.TypeStructureContext) {
                st.add("type", visitTypeStructure(ctx.typeStructure()));
            }
        }

        return st;
    }


    @Override
    public ST visitTOFIXEDExpression(SmallPearlParser.TOFIXEDExpressionContext ctx) {
        TypeDefinition op = m_ast.lookupType(ctx.expression());

        if (op instanceof TypeBit) {
            ST st = m_group.getInstanceOf("BITSTOFIXED");
            st.add("operand", visitAndDereference(ctx.expression()));
            return st;
        } else if (op instanceof TypeChar) {
            ST st = m_group.getInstanceOf("CHARACTERSTOFIXED");
            st.add("operand", visitAndDereference(ctx.expression()));
            return st;
        }

        return null;
    }

    @Override
    public ST visitTOFLOATExpression(SmallPearlParser.TOFLOATExpressionContext ctx) {
        ST st = m_group.getInstanceOf("TOFLOAT");
        TypeDefinition op = m_ast.lookupType(ctx.expression());

        st.add("operand", visitAndDereference(ctx.expression()));

        if (op instanceof TypeFixed) {
            TypeFixed fixedValue = (TypeFixed) op;
            int precision = 0;

            if (fixedValue.getPrecision() <= Defaults.FLOAT_SHORT_PRECISION) {
                precision = Defaults.FLOAT_SHORT_PRECISION;
            } else {
                precision = Defaults.FLOAT_LONG_PRECISION;
            }
            st.add("precision", (precision));
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        return st;
    }

    @Override
    public ST visitTOBITExpression(SmallPearlParser.TOBITExpressionContext ctx) {
        TypeDefinition op = m_ast.lookupType(ctx.expression());

        ST st = m_group.getInstanceOf("TOBIT");
        st.add("operand", visitAndDereference(ctx.expression()));

        if (op instanceof TypeFixed) {
            st.add("noOfBits", ((TypeFixed) op).getPrecision() + 1);
        } else {
            throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        return st;
    }

    @Override
    public ST visitTOCHARExpression(SmallPearlParser.TOCHARExpressionContext ctx) {
        TypeDefinition op = m_ast.lookupType(ctx.expression());

        if (op instanceof TypeFixed) {
            ST st = m_group.getInstanceOf("FIXEDTOCHARACTER");
            st.add("operand", visitAndDereference(ctx.expression()));
            return st;
        }

        return null;
    }

    @Override
    public ST visitCONTExpression(SmallPearlParser.CONTExpressionContext ctx) {
        TypeDefinition op = m_ast.lookupType(ctx.expression());

        // the dereferencing occurs implicit in getExpression
        ST st = m_group.getInstanceOf("CONT");
        st.add("operand", visit(ctx.expression()));


        return st;
    }

  

  
    /**
     * create code for CONVERT .. TO
     * we can use lot of the PUT-stuff.
     * We must remove the "dation" tag from the string template,
     * since the code template creates an own element for the conversions
     */
    @Override
    public ST visitConvertToStatement(SmallPearlParser.ConvertToStatementContext ctx) {
        //ST st = m_group.getInstanceOf("ConvertToStatement");
        //String convertToString = "";

        if (m_verbose > 0) {
            System.out.println("CppCodeGeneratorVisitor: visitConvertToStatement");
        }

        ErrorStack.enter(ctx, "CONVERT .. TO");
        //String s = ctx.name().getText();

        ST stmt = m_group.getInstanceOf("iojob_convertTo_statement");
        stmt.add("char_string", getUserVariableWithoutNamespace(ctx.name().getText()));


        // this flag is set to true if at least one non static format parameter is detected
        m_isNonStatic = false;

        // this should never occur, since this is checked in the semantic analysis
        // in CheckIOStatements
        ST formatList = m_group.getInstanceOf("iojob_formatlist");
        if (ctx.listOfFormatPositions() == null) {
            ST fmt = m_group.getInstanceOf("iojob_list_format");
            formatList.add("formats", fmt);
        } else {

            for (int i = 0; i < ctx.listOfFormatPositions().formatPosition().size(); i++) {
                addFormatPositionToFormatList(formatList,
                        ctx.listOfFormatPositions().formatPosition(i));
            }
        }

        stmt.add("formatlist", formatList);
        if (!m_isNonStatic) {
            stmt.add("format_list_is_static", "1");
        }

        // create list of data elements
        ST dataList = getIojobDataList(ctx.ioDataList());

        stmt.add("datalist", dataList);
        ErrorStack.leave();

        return stmt;
    }


    @Override
    public ST visitConvertFromStatement(SmallPearlParser.ConvertFromStatementContext ctx) {

        ST stmt = m_group.getInstanceOf("iojob_convertFrom_statement");
        stmt.add("char_string", visitAndDereference(ctx.expression()));

        ErrorStack.enter(ctx, "CONVERT .. FROM");

        // this flag is set to true if at least one non static format parameter is detected
        m_isNonStatic = false;

        // this should never occur, since this is checked in the semantic analysis
        // in CheckIOStatements
        ST formatList = m_group.getInstanceOf("iojob_formatlist");
        if (ctx.listOfFormatPositions() == null) {
            ST fmt = m_group.getInstanceOf("iojob_list_format");
            formatList.add("formats", fmt);
        } else {

            for (int i = 0; i < ctx.listOfFormatPositions().formatPosition().size(); i++) {
                addFormatPositionToFormatList(formatList,
                        ctx.listOfFormatPositions().formatPosition(i));
            }
        }

        stmt.add("formatlist", formatList);
        if (!m_isNonStatic) {
            stmt.add("format_list_is_static", "1");
        }

        // create list of data elements
        ST dataList = getIojobDataList(ctx.ioDataList());

        stmt.add("datalist", dataList);
        ErrorStack.leave();

        return stmt;
    }

 
    @Override
    public ST visitInterrupt_statement(SmallPearlParser.Interrupt_statementContext ctx) {
        if (m_verbose > 0) {
            System.out.println("CppCodeGeneratorVisitor: visitInterrupt_statement");
        }

        return visitChildren(ctx);
    }

    @Override
    public ST visitEnableStatement(SmallPearlParser.EnableStatementContext ctx) {
        ST st = m_group.getInstanceOf("EnableStatement");

        if (m_verbose > 0) {
            System.out.println("CppCodeGeneratorVisitor: visitEnableStatement");
        }

        st.add("id", visitAndDereference(ctx.name()));
        return st;
    }

    @Override
    public ST visitDisableStatement(SmallPearlParser.DisableStatementContext ctx) {
        ST st = m_group.getInstanceOf("DisableStatement");

        if (m_verbose > 0) {
            System.out.println("CppCodeGeneratorVisitor: visitDisableStatement");
        }

        st.add("id", visitAndDereference(ctx.name()));
        return st;
    }

    @Override
    public ST visitTriggerStatement(SmallPearlParser.TriggerStatementContext ctx) {
        ST st = m_group.getInstanceOf("TriggerStatement");

        if (m_verbose > 0) {
            System.out.println("CppCodeGeneratorVisitor: visitTriggerStatement");
        }

        st.add("id", visitAndDereference(ctx.name()));
        return st;
    }

    /**
     * create an array descriptor according the definition of the variable 'array'
     * <p>
     * if the variable is a formal parameter, we create an anyonymous array descrptor
     * according the formal parameter's name
     * x: PROC( abc() FIXED --> data is storedat FIXED* data_abc
     * array descriptor is named 'Array* ad_abc'
     * <p>
     * if the variable is a real array, the array descriptor is named according the
     * arrays dimension
     *
     * @param array
     * @return
     * @note: array slices are not treated
     */
    private String getArrayDescriptor(VariableEntry var) {
        ParserRuleContext c = var.getCtx();
        String s = null;
        if (c instanceof FormalParameterContext) {
            s = "ad_" + var.getName();
        } else {
            TypeArray type = (TypeArray) var.getType();
            ArrayDescriptor array_descriptor =
                    new ArrayDescriptor(type.getNoOfDimensions(), type.getDimensions());
            s = array_descriptor.getName();
        }
        return s;
    }
    
    private String getArrayDescriptor(TypeDefinition t) {
       String s = null;
       if (t instanceof TypeArray) {
           TypeArray type = (TypeArray) t;
           ArrayDescriptor array_descriptor =
                   new ArrayDescriptor(type.getNoOfDimensions(), type.getDimensions());
           s = array_descriptor.getName();
       }
       return s;
    }

    private ST visitStructVariableDenotation(
            SmallPearlParser.VariableDenotationContext ctx) {
        Log.debug("CppCodeGeneratorVisitor:visitStructVariableDeclaration:ctx"
                + CommonUtils.printContext(ctx));
        ST st = m_group.getInstanceOf("StructureVariableDeclaration");

        for (int i = 0; i < ctx.identifierDenotation().identifier().size(); i++) {
            ASTAttribute attr = m_ast.lookup(ctx.identifierDenotation().identifier(i));
            String id = ctx.identifierDenotation().identifier(i).getText();

            SymbolTableEntry symbolTableEntry =
                    m_currentSymbolTable.lookupLocal(id);

            if (symbolTableEntry != null && symbolTableEntry instanceof VariableEntry) {
                VariableEntry variable = (VariableEntry) symbolTableEntry;

                if (variable.getType() instanceof TypeStructure) {
                    TypeStructure typ = (TypeStructure) variable.getType();
                    st.add("name", id);
                    st.add("type", typ.getStructureName());
                } else if (variable.getType() instanceof TypeArray) {
                    TypeArray array = (TypeArray) variable.getType();

                    if (array.getBaseType() instanceof TypeStructure) {
                        TypeStructure typ = (TypeStructure) array.getBaseType();
                        st.add("name", id);
                        st.add("type", typ.getStructureName());
                    }
                }
            }
        }

        return st;
    }

    private ST traverseNameForStruct(SmallPearlParser.NameContext ctx, TypeDefinition type) {
        ST st = m_group.getInstanceOf("Name");
        st.add("id", ctx.ID().getText());

        if (ctx.name() != null) {
            reVisitName(ctx.name(), type, st);
        }

        return st;
    }

    /**
     * iterate over name recursion levels
     */
    private Void reVisitName(SmallPearlParser.NameContext ctx, TypeDefinition type, ST st) {
        Log.debug("CppCodeGeneratorVisitor:reVisitName:ctx" + CommonUtils.printContext(ctx));

        if (type instanceof TypeStructure) {
            TypeStructure struct = (TypeStructure) type;
            StructureComponent component = struct.lookup(ctx.ID().getText());
            st.add("name", component.m_alias);

            if (ctx.name() != null) {
                if (component.m_type instanceof TypeStructure) {
                    TypeStructure subStruct = (TypeStructure) component.m_type;
                    reVisitName(ctx.name(), subStruct, st);
                }
            }
        }

        return null;
    }


    /*
     * this method obtains the ST of the given context
     * if the context is of type TypeReference an implicit CONT is added
     * if the variable is a reference to an array and indices are given,
     *    the effective data element is returned
     *
     */
    private ST visitAndDereference(ParserRuleContext ctx) {
        String s = ctx.getText();
        ST st = visit(ctx);
        ASTAttribute attr = m_ast.lookup(ctx);
        VariableEntry ve = attr.getVariable();
        if (ve != null) {
            if (ve.getType() instanceof TypeReference) {
                if (((TypeReference) (ve.getType()))
                        .getBaseType() instanceof TypeArraySpecification) {
                    if (ctx instanceof BaseExpressionContext) {
                        BaseExpressionContext bc = (BaseExpressionContext) ctx;
                        if (bc.primaryExpression() != null
                                && bc.primaryExpression().name() != null) {
                            NameContext n = bc.primaryExpression().name();
                            if (n.listOfExpression() != null) {
                                ST arrayRef = m_group.getInstanceOf("RefArrayReadWrite");
                                arrayRef.add("name", ve.getName());
                                arrayRef.add("indices",
                                        visitListOfExpression(n.listOfExpression()));
                                st = arrayRef;
                            }
                        }
                    }
                } else if (((TypeReference) (ve.getType()))
                        .getBaseType() instanceof TypeProcedure) {
                    BaseExpressionContext bc = (BaseExpressionContext) ctx;
                    if (bc.primaryExpression() != null && bc.primaryExpression().name() != null) {
                        NameContext n = bc.primaryExpression().name();
                        if (n.listOfExpression() != null) {
                            // FunctionCall(callee,ListOfActualParameters)
                            ST functionCall = m_group.getInstanceOf("FunctionCall");
                            ST deref = m_group.getInstanceOf("CONT");
                            deref.add("operand", getUserVariable(ve));//getUserVariable(ve.getName()));
                            functionCall.add("callee", deref);
                            functionCall.add("ListOfActualParameters",
                                    visitListOfExpression(n.listOfExpression()));
                            st = functionCall;
                        }
                    }
                }
            }
        }
        TypeDefinition t = attr.getType();
        if (t instanceof TypeReference) {
            ST deref = m_group.getInstanceOf("CONT");
            deref.add("operand", st);
            st = deref;
        }
        return st;
    }

    private String nextTempVarName() {
        int index = m_tempVariableNbr.lastElement();

        m_tempVariableNbr.remove(m_tempVariableNbr.size() - 1);
        m_tempVariableNbr.add(Integer.valueOf(index + 1));
        return "tmp_" + index;
    }

    /*
     * This method traverses a name context and generates the possibly nested
     * array and structures.
     *
     * @param ctx NameContext
     * @param symbolTable Symboltable of the current scope
     * @return TypeDefinition of a given name
     */
    private ST generateLHS(SmallPearlParser.NameContext ctx, SymbolTable symbolTable) {
        TypeDefinition type = null;
        TypeStructure struct = null;
        ST lhs = m_group.getInstanceOf("LHS");
        String fromns = null;

        SymbolTableEntry symbolTableEntry = symbolTable.lookup(ctx.ID().toString());
        
        if (symbolTableEntry.isSpecified()) {
            fromns = symbolTableEntry.getGlobalAttribute();
            if (fromns.equals(m_module.getName())) {
                fromns = null;
            }
        }

        if (symbolTableEntry instanceof VariableEntry) {
            SmallPearlParser.NameContext lctx = ctx.name();
            VariableEntry var = (VariableEntry) symbolTableEntry;
            type = var.getType();

            if (type instanceof TypeArray) {
                ST arrayLHS = m_group.getInstanceOf("ArrayLHS");
                TypeArray arrayType = (TypeArray) type;
                
                arrayLHS.add("name", var.getName());
                arrayLHS.add("fromns", fromns);

                // if no indices are given, the complete array is accessed
                if (ctx.listOfExpression() != null) {
                    arrayLHS.add("indices", getIndices(ctx.listOfExpression().expression()));
                }

                lhs.add("expr", arrayLHS);
                type = ((TypeArray) type).getBaseType();

                if (type instanceof TypeStructure) {
                    struct = (TypeStructure) type;
                }
            } else if (type instanceof TypeReference) {
                type = ((TypeReference) type).getBaseType();
            } else if (type instanceof TypeStructure) {
                struct = (TypeStructure) type;
                lhs.add("expr", generateStructLHS(ctx, var, fromns, struct));
            }

            lctx = ctx.name();

            while (lctx != null) {
                StructureComponent structureComponent = struct.lookup(lctx.ID().toString());

                // Note: This should be handled by the new error reporting mechanism.
                if (structureComponent == null) {
                    ErrorStack.addInternal(ctx, "CPP", "generateLHS: error. please report.");
                }

                type = structureComponent.m_type;

                if (type instanceof TypeArray) {
                    ST structLHS = m_group.getInstanceOf("StructureComponent");
                    structLHS.add("component", structureComponent.m_alias);
                    lhs.add("expr", structLHS);

                    ST arrayLHS = m_group.getInstanceOf("ArrayLHSInStructure");
                    TypeArray arrayType = (TypeArray) type;
                    ArrayDescriptor array_descriptor = new ArrayDescriptor(
                            arrayType.getNoOfDimensions(), arrayType.getDimensions());

                    arrayLHS.add("descriptor", array_descriptor.getName());
                    arrayLHS.add("name", lhs);

                    // if no indices are given, the complete array is accessed
                    if (lctx.listOfExpression() != null) {
                        arrayLHS.add("indices", getIndices(lctx.listOfExpression().expression()));
                    }

                    lhs = m_group.getInstanceOf("LHS");
                    lhs.add("expr", arrayLHS);
                    type = ((TypeArray) type).getBaseType();
                    if (type instanceof TypeStructure) {
                        struct = (TypeStructure) type;
                    }
                } else if (type instanceof TypeReference) {
                    type = ((TypeReference) type).getBaseType();
                    if (!(type instanceof TypeStructure)) {
                      lhs.add("expr", structureComponent.m_alias);
                    } else {
                        ErrorStack.addInternal(lctx, "CppCodeGen@5654","auto rereference of ref to struct not implemented");
                    }
                } else if (type instanceof TypeStructure) {
                    struct = (TypeStructure) type;
                    ST structLHS = m_group.getInstanceOf("StructureComponent");
                    structLHS.add("component", structureComponent.m_alias);
                    lhs.add("expr", structLHS);
                } else {
                    lhs.add("expr", structureComponent.m_alias);
                }

                lctx = lctx.name();
            }
        }

        return lhs;
    }

    /*
     * This method generates a given name context a ArrayLHS ST
     *
     * @param ctx NameContext
     * @param var VariableEntry
     * @parama type Type of Array
     * @return ArrayLHS
     */
    private ST generateArrayLHS(SmallPearlParser.NameContext ctx, VariableEntry var,
            TypeArray type) {
        ST arrayLHS = m_group.getInstanceOf("ArrayLHS");

        ArrayDescriptor array_descriptor =
                new ArrayDescriptor(type.getNoOfDimensions(), type.getDimensions());

        arrayLHS.add("descriptor", array_descriptor.getName());
        arrayLHS.add("name", var.getName());

        // if no indices are given, the complete array is accessed
        if (ctx.listOfExpression() != null) {
            arrayLHS.add("indices", getIndices(ctx.listOfExpression().expression()));
        }

        return arrayLHS;
    }

    /*
     * This method generates a given name context a StructLHS ST
     *
     * @param ctx NameContext
     * @param var VariableEntry
     * @param type Type of Structure
     * @return StructLHS
     */
    private ST generateStructLHS(SmallPearlParser.NameContext ctx, VariableEntry var,
            String fromns, TypeStructure type) {
        ST structLHS = m_group.getInstanceOf("StructureLHS");
        TypeStructure struct = type;

        structLHS.add("name", ctx.ID().getText());
        structLHS.add("fromns", fromns);

        return structLHS;
    }

}
