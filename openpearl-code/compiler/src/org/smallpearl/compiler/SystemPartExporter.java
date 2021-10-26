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

import org.smallpearl.compiler.SymbolTable.InterruptEntry;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import java.util.LinkedList;

/**
 * export module information as xml-file
 * <ul>
 * <li> system dation specifications
 * <li> user dation declarations
 * <li> tfu usage by user dations with reference to system dation
 * </ul>
 * 
 * if global becomes supported, this must become extended by
 * <ul>
 * <li>specifications of problem part elements from other modules
 * <li>declarations of global problem part elements of this module
 * </ul>
 * 
 * The system part elements are generated from the AST.<br>
 * The problem part elements are generated from the symbol table 
 */

public class SystemPartExporter extends SmallPearlBaseVisitor<ST> implements SmallPearlVisitor<ST> {

    private static final String IMC_EXPORT_STG = "IMC.stg";

    private STGroup group;
    private int m_verbose;
    private String m_sourceFileName;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ST module = null;
    private ST systemPart = null;
    private ST problemPart = null;
    private ST tfuUsage = null;
    private boolean m_useNamespaceForGlobals;
    private String m_thisNamespace;


    public SystemPartExporter(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, AST ast, Boolean useNamespaceForGlobals) {

        m_verbose = verbose;
        m_sourceFileName = sourceFileName;
        m_useNamespaceForGlobals = useNamespaceForGlobals;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        if (m_verbose > 1) {
            System.out.println("Generating InterModuleChecker definitions");
        }

        this.ReadTemplate(IMC_EXPORT_STG);
    }

    private Void ReadTemplate(String filename) {
        if (m_verbose > 1) {
            System.out.println("Read StringTemplate Group File: " + filename);
        }

        this.group = new STGroupFile(filename);

        return null;
    }

    @Override
    public ST visitModule(SmallPearlParser.ModuleContext ctx) {
        module = group.getInstanceOf("Module");

        module.add("sourcefile", m_sourceFileName);
        module.add("name", ctx.nameOfModuleTaskProc().ID().getText());
        if (m_useNamespaceForGlobals) {
            m_thisNamespace = ctx.nameOfModuleTaskProc().ID().getText();
        } else {
            m_thisNamespace = "pearlApp";
        }
        org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;


        if (ctx != null) {
            visitChildren(ctx);
        }
        module.add("SystemPart", systemPart);
        problemPart = group.getInstanceOf("ProblemPart");
        tfuUsage = group.getInstanceOf("TfuUsage");
        problemPart.add("decls", tfuUsage);

        exportInterruptSpecifications();
        exportProblemPartSpcAndDcl();
        module.add("ProblemPart", problemPart);

        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return module;
    }


    @Override
    public ST visitSystem_part(SmallPearlParser.System_partContext ctx) {
        systemPart = group.getInstanceOf("SystemPart");

        ErrorStack.enter(ctx, "system part");
        if (ctx != null) {
            visitChildren(ctx);
        }
        ErrorStack.leave();

        return null;
    }


    @Override
    public ST visitSystemElementDefinition(SmallPearlParser.SystemElementDefinitionContext ctx) {
        ST decl = group.getInstanceOf("SystemElementDefinition");
        
        decl.add("username", ctx.systemPartName().getText());
        decl.add("lineno", ctx.start.getLine());
        decl.add("col", ctx.start.getCharPositionInLine() + 1);
        decl.add("decl", visit(ctx.systemDescription()));

        systemPart.add("decls", decl);
        return null;
    }

    @Override
    public ST visitConfigurationElement(SmallPearlParser.ConfigurationElementContext ctx) {
        ST decl = group.getInstanceOf("ConfigurationElement");

        decl.add("lineno", ctx.start.getLine());
        decl.add("col", ctx.start.getCharPositionInLine() + 1);
        decl.add("decl", visit(ctx.systemDescription()));

        systemPart.add("decls", decl);
        return null;
    }


    @Override
    public ST visitSystemDescription(SmallPearlParser.SystemDescriptionContext ctx) {
        ST decl = group.getInstanceOf("SystemDescription");

        // we may have
        SymbolTableEntry se =
                m_currentSymbolTable.lookupSystemPartName(ctx.systemPartName().ID().toString());
        // the symbol table visitor grants that this entry is a system name
        decl.add("sysname", se.getName()); ///ctx.systemPartName().ID().toString());

        if (ctx.systemElementParameters() != null) {
            decl.add("parameters", visitSystemElementParameters(ctx.systemElementParameters()));
        }
        if (ctx.association().size() > 0) {
            ST previousSt = decl;
            for (int i = 0; i < ctx.association().size(); i++) {
                ST association = group.getInstanceOf("Association");
                previousSt.add("association", association);
                String name = ctx.association(i).systemPartName().ID().toString();
                se = m_currentSymbolTable.lookupSystemPartName(name);

                association.add("name", ctx.association(i).systemPartName().ID());


                if (ctx.association(i).systemElementParameters() != null) {
                    if (se.isUserName()) {
                        // we have an user defined name as system element name
                        ErrorStack.add(ctx.association(i).systemPartName(),
                                "no parameters allowed for user names", "");
                        return null;
                    }
                    association.add("parameters", visitSystemElementParameters(
                            ctx.association(i).systemElementParameters()));
                }
                previousSt = association;

            }
        }

        return decl;
    }


    @Override
    public ST visitSystemElementParameters(SmallPearlParser.SystemElementParametersContext ctx) {
        ST parameters = group.getInstanceOf("SystemElementParameters");

        for (int i = 0; i < ctx.constant().size(); i++) {
            ST parameter = group.getInstanceOf("Parameter");
            String param = ctx.constant(i).getText();
            //            param = param.replaceAll("^'","");
            //            param = param.replaceAll("'$","");

            if (ctx.constant(i).stringConstant() != null) {
                ST type = group.getInstanceOf("Type_Char");
                type.add("name", param);
                parameter.add("type", type);
            } else if (ctx.constant(i).bitStringConstant() != null) {
                ST type = group.getInstanceOf("Type_Bit");
                type.add("name", param);
                parameter.add("type", type);
            } else if (ctx.constant(i).fixedConstant() != null) {
                ST type = group.getInstanceOf("Type_Fixed");
                type.add("name", param);
                parameter.add("type", type);
            }

            parameters.add("params", parameter);
        }

        return parameters;
    }

    private void exportInterruptSpecifications() {
        // export interrupt specifications

        LinkedList<InterruptEntry> l = m_currentSymbolTable.getInterruptSpecifications();
        for (InterruptEntry v : l) {

            ST interruptSpecification = group.getInstanceOf("Specification");
            interruptSpecification.add("type", "INTERRUPT");
            interruptSpecification.add("lineno", v.getCtx().start.getLine());
            interruptSpecification.add("col", v.getCtx().start.getCharPositionInLine() + 1);
            interruptSpecification.add("name", v.getName());
            if (m_useNamespaceForGlobals) {
                interruptSpecification.add("fromNamespace", v.getGlobalAttribute());
            }
            problemPart.add("decls", interruptSpecification);
        }
    }

    private void exportProblemPartSpcAndDcl() {
        // export specifications and declaractions of GLOBAL symbols

        LinkedList<VariableEntry> l = m_currentSymbolTable.getVariableDeclarations();

        for (VariableEntry v : l) {
            if (v.getType() instanceof TypeDation) {
                ST dation = null;
                TypeDation t = (TypeDation) v.getType();
                if (!t.isSystemDation()) {
                    // user dation
                    treatTfuStuff(v);
                    if (t.getGlobal()!=null) {
                      dation = treatDationSpcDclCommonStuff(v);
                    }
                 
                } else {
                    // system dation
                    dation = treatDationSpcDclCommonStuff(v);
                }
                problemPart.add("decls", dation);
            } else {
                // other types than dation: DCL or SPC
                ST st = null;
                if (v.isSpecified()) {
                    st = group.getInstanceOf("Specification");
                } else {
                    if (v.getGlobalAttribute()!= null) {
                        //Declaration(lineno,col,name,type,global) ::= <<
                        st = group.getInstanceOf("Declaration");
                    } 
                }
                
                // st is null, if DCL without global -> skip this for the IMC
                if (st == null) continue;
                st.add("lineno", v.getSourceLineNo());
                st.add("col",v.getCharPositionInLine());
                st.add("name", v.getName());
                st.add("type", v.getType());
                if (m_useNamespaceForGlobals && v.getGlobalAttribute() != null) {
                    st.add("fromNamespace", v.getGlobalAttribute());
                }
                problemPart.add("decls",  st);



            }
        }

        return;
    }

    private ST treatDationSpcDclCommonStuff(VariableEntry v) {
        // system dation
        
        ST dation = null;
        if (v.isSpecified()) {
            dation = group.getInstanceOf("DationSpecification");
        } else {
            dation = group.getInstanceOf("DationDeclaration");
        }

        dation.add("lineno", v.getCtx().start.getLine());
        dation.add("col", v.getCtx().start.getCharPositionInLine() + 1);
        dation.add("name", v.getName());
        if (m_useNamespaceForGlobals ) {
            dation.add("fromNamespace", v.getGlobalAttribute());
        }

        TypeDation td = (TypeDation) (v.getType());
        dation.add("isBasic", td.isBasic());
        ST datalist = group.getInstanceOf("DataList");
        ST attributes = group.getInstanceOf("Attributes");
        

        ST data = group.getInstanceOf("Data");
        data.add("name", td.getDataAsString());
        datalist.add("data", data);

        dation.add("datalist", datalist);

        // add attributes: td.getTypeAsString delivers attributes as space separated list
        String attr = td.getTypeAsString();
        String attrs[] = attr.split(" ");
        for (String a : attrs) {
            ST attribute = group.getInstanceOf("Attribute");
            attribute.add("name", a);
            attributes.add("attributes", attribute);
        }
        dation.add("attributes", attributes);
        return dation;
    }
    
    private void treatTfuStuff(VariableEntry v) {
        TypeDation d = (TypeDation) v.getType();

        ST tfuInUserDation = group.getInstanceOf("TfuInUserDation");
        tfuInUserDation.add("lineno", v.getSourceLineNo());
        tfuInUserDation.add("col", v.getCharPositionInLine() + 1);

        tfuInUserDation.add("userdation", v.getName());
        
        if (d.hasTfu() ) {
            if (d.getCreatedOn() != null) {
               tfuInUserDation.add("systemdation", d.getCreatedOn().getName());
            }
            // already checked, that the last dimension is const > 0
            // get last defined dimension
            int recordLength = d.getDimension3();
            if (recordLength < 0)
                recordLength = d.getDimension2();
            if (recordLength < 0)
                recordLength = d.getDimension1();

            // detect element size if not ALPHIC or ALL
            if (d.getTypeOfTransmission() != null) {
                if (!d.getTypeOfTransmission().contentEquals("ALL")) {
                    recordLength *= d.getTypeOfTransmissionAsType().getSize();
                }
                // getSize returns -1, if the size is unknown to the compiler
                // the test od sufficient record size must be done at runtime
                // indicate this which recordLength=0
                if (recordLength < 0) {
                    recordLength = 0;
                }
            }
            tfuInUserDation.add("tfusize", recordLength);
            tfuUsage.add("decls", tfuInUserDation);
        }
    }

}
