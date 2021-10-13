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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.SmallPearlParser.InitElementContext;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SemaphoreEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;
import java.util.ArrayList;
import java.util.List;

/*
 * current state
 * <ol>
 * <li>symbol table contains all defined and specified symbols
 * <li>no initializers are in the symbol table
 * <li>for each expression we have an ASTAttribute
 * <li>initializers of type identifier must be INV
 * </ol>
 * 
 * Check initialisers for
 * <ol>
 * <li> fitting type
 * <li> fitting number for the symbol 
 * </ol>
 * 
 * principle of operation
 * <ul>
 * <li>at each task,proc,block,loop we fetch declarations from the symbol table
 * <li>via the context information in the SymbolTableEntry we can check for initializers
 * <li>via the ASTAttributes from the ExpressionTypeVisitor, the type compatibility becomes checked
 * <li>the initialisiers become added to the SymbolTableEntries
 * </ul>
 *    
 *    Problems:
 *    <ol>
 *    <li>SymbolTableVisitor created fixed constants with wrong length if they are longer than the type<br>
 *    the ASTAttribute of constants should be set by the SymbolTableVisitor and not by the ExpressionTypeVisitor
 *    <li>for TypeStructure we need an iterator over all component elements, even with nested structures
 *    <li>we should not extend the initializer to the size of the variable to safe space in the C++-code - 
 *       especially if CHAR-Variables become initialized like x CHAR(10) INIT (' '); 
 *    </ol>
 */
public class CheckVariableDeclaration extends SmallPearlBaseVisitor<Void>
        implements SmallPearlVisitor<Void> {

    private int m_verbose;
    private boolean m_debug;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private AST m_ast;
    private ArrayList<String> m_identifierDenotationList = null;

    
    public CheckVariableDeclaration(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_debug = debug;
        m_verbose = verbose;
        m_symbolTableVisitor = symbolTableVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        if (m_verbose > 0) {
            System.out.println("    Check Variable Declaration");
        }
        List<VariableEntry> listOfVariables = m_symboltable.getAllVariableDeclarations();
        for (VariableEntry v : listOfVariables) {
            
            //if (v.getInitializer()!= null) {
                System.out.println(v.getName() + " "+ v.getType());
            //    checkInitializer(v);
            //}
        }
        System.out.println("for(int i=0; ...");
        for (int i=0; i<listOfVariables.size(); i++) {
            VariableEntry v = listOfVariables.get(i);
        
            
            //if (v.getInitializer()!= null) {
                System.out.println(v.getName() + " "+ v.getType());
            //    checkInitializer(v);
            //}
        }

    }
    
    private TypeDefinition getFirstStructureComponentElement (TypeStructure ts){
        
        return null;
    }
    private void checkInitializer(VariableEntry v) {
         if (v.getType() instanceof TypeArray) {
             TypeArray ta = (TypeArray)v.getType();
             if (ta.getBaseType() instanceof TypeStructure) {
                 
                
             } else if (isScalarType(ta.getBaseType())) {
                // checkTypes(InitElementContext initElementContext, TypeDefinition tVar,
                //         TypeDefinition m_type) 
                 // all initializers must have a compatible type
              
             }
         } else if (isScalarType(v.getType())) {
            SimpleInitializer si = (SimpleInitializer )(v.getInitializer());
            TypeDefinition typeOfConstant = getType(si.getConstant());
 
            ASTAttribute attr = m_ast.lookup(v.getInitializer().getContext());
            // anstell ASTAttribute besser Constant um getType erweitern -> erleichtert Lookup
            
             checkTypes(v.getInitializer().getContext(), v.getType(), attr.getType() );
             
             checkTypeCompatibility(v.getType(),typeOfConstant,si.getContext());
        }
        
    }
    
    private boolean checkTypeCompatibility(TypeDefinition typeOfVariable, TypeDefinition typeOfInitializer, ParserRuleContext ctxOfInitElement) {
        
        if (typeOfVariable instanceof TypeFixed && typeOfInitializer instanceof TypeFixed) {
            if (((TypeFixed)typeOfVariable).getPrecision() < ((TypeFixed)typeOfInitializer).getPrecision()) {
                ErrorStack.add(ctxOfInitElement,"INIT",typeOfVariable +" := " + typeOfInitializer);
            }
            
        } else {
            ErrorStack.add(ctxOfInitElement,"INIT",typeOfVariable +" := " + typeOfInitializer);  
        }
        
        return false;
    }
    private TypeDefinition getType(ConstantValue c) {
        if (c instanceof ConstantFixedValue) {
            return (new TypeFixed ( ((ConstantFixedValue)c).getPrecision()));
        }
        if (c instanceof ConstantFloatValue) {
            return (new TypeFloat ( ((ConstantFloatValue)c).getPrecision()));
        }
        if (c instanceof ConstantCharacterValue) {
            return (new TypeChar( ((ConstantCharacterValue)c).getLength()));
        }        
        if (c instanceof ConstantBitValue) {
            return (new TypeBit ( ((ConstantBitValue)c).getLength()));
        }
        if (c instanceof ConstantDurationValue) {
            return new TypeDuration();
        }
        if (c instanceof ConstantClockValue) {
            return new TypeClock();
        }
        ErrorStack.addInternal(null, "CheckVariableDeclaration", "untreated type of constant@143");
        return null;
    }
    
    private boolean isSimpleType(TypeDefinition t) {
        if (t instanceof TypeFixed ||
                t instanceof TypeFloat ||
                t instanceof TypeChar ||
                t instanceof TypeBit ||
                t instanceof TypeDuration ||
                t instanceof TypeClock ) {
            return true;
        }
        return false;
    }
    
    private boolean isScalarType(TypeDefinition t) {
        if (t instanceof TypeFixed ||
                t instanceof TypeFloat ||
                t instanceof TypeChar ||
                t instanceof TypeBit ||
                t instanceof TypeDuration ||
                t instanceof TypeClock ||
                t instanceof TypeSemaphore ||
                t instanceof TypeBolt || 
                t instanceof TypeReference) {
            return true;
        }
        return false;
    }

    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Variable Declaration: visitModule");
        }

        org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry =
                m_currentSymbolTable.lookupLocal(ctx.nameOfModuleTaskProc().ID().getText());
        m_currentSymbolTable = ((ModuleEntry) symbolTableEntry).scope;
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Variable Declaration: visitProcedureDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitProcedureBody(SmallPearlParser.ProcedureBodyContext ctx) {
        if (ctx != null && ctx.children != null) {
            for (ParseTree c : ctx.children) {
                if (c instanceof SmallPearlParser.VariableDeclarationContext) {
                    visitVariableDeclaration(
                            (SmallPearlParser.VariableDeclarationContext) c);
                } else if (c instanceof SmallPearlParser.StatementContext) {
                    visitStatement((SmallPearlParser.StatementContext) c);
//                } else if (c instanceof SmallPearlParser.DationDeclarationContext) {
//                    SmallPearlParser.DationDeclarationContext declctx =
//                            (SmallPearlParser.DationDeclarationContext) c;
//                    throw new DationDeclarationNotAllowedHereException(declctx.getText(),
//                            declctx.start.getLine(), declctx.start.getCharPositionInLine());
                }
            }
        }

        return null;
    }

    @Override
    public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Variable Declaration: visitTaskDeclaration");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        m_currentSymbolTable = m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Variable Declaration: visitBlock_statement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
        if (m_debug) {
            System.out.println("Semantic: Check Variable Declaration: visitLoopStatement");
        }

        this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
        visitChildren(ctx);
        this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
        return null;
    }

    @Override
    public Void visitVariableDeclaration(
            SmallPearlParser.VariableDeclarationContext ctx) {
        if (ctx != null) {
            for (int i = 0; i < ctx.variableDenotation().size(); i++) {
                visitVariableDenotation(ctx.variableDenotation().get(i));
            }
        }

        return null;
    }

    @Override
    public Void visitVariableDenotation(SmallPearlParser.VariableDenotationContext ctx) {
        List<InitElementContext> initElements = null;
        m_identifierDenotationList = getIdentifierDenotation(ctx.identifierDenotation());

        // all identifier have the same type --> first element to retrieve the initialiser-list
        if (ctx.semaDenotation() != null) {
            if (ctx.semaDenotation().preset() != null) {
                initElements = ctx.semaDenotation().preset().initElement();
            }
        } else if (ctx.problemPartDataAttribute() != null) {
            if (ctx.problemPartDataAttribute().initialisationAttribute() != null) {
                initElements =
                        ctx.problemPartDataAttribute().initialisationAttribute().initElement();
            }
        }
        if (initElements == null) {
            return null; // no initializer - nothing to do
        }

        // check required number of initializers and their type/value
        int nextInitializer = 0;

        int requiredNumberOfInitializers = 0;
        for (String identifier : m_identifierDenotationList) {
         
            VariableEntry var = (VariableEntry) m_currentSymbolTable.lookup(identifier.toString());
            if (nextInitializer >= initElements.size()) {
                ErrorStack.add(var.getCtx(), "INIT", "no more initialisers");
                return null;
            }

            TypeDefinition tVar = var.getType();
            if (tVar instanceof TypeArray) {
                requiredNumberOfInitializers = ((TypeArray) tVar).getTotalNoOfElements();

                for (int i = 0; i < requiredNumberOfInitializers
                        && nextInitializer < initElements.size(); i++) {
                    ASTAttribute attr = m_ast.lookup(initElements.get(nextInitializer));
                    checkTypes(initElements.get(nextInitializer), ((TypeArray) tVar).getBaseType(),
                            attr.m_type);
                    nextInitializer++;
                }
            } else if (tVar instanceof TypeStructure) {
                TypeStructure ts = (TypeStructure)tVar;
                requiredNumberOfInitializers = ts.getTotalNoOfElements();
                if (requiredNumberOfInitializers > initElements.size()) {
                    ErrorStack.add(ctx, "INIT", "all STRUCT members must have initialiser");
                }
                if (requiredNumberOfInitializers < initElements.size()) {
                    ErrorStack.add(ctx, "INIT", "too many initialisers");
                }
                nextInitializer+=requiredNumberOfInitializers;
            } else if (tVar instanceof TypeReference) {
                //TODO: Check for valid reference
                TypeReference ts = (TypeReference)tVar;
                nextInitializer++;
            } else {
                // scalar type
                requiredNumberOfInitializers = 1;
                ASTAttribute attr = m_ast.lookup(initElements.get(nextInitializer));
                if (!attr.isReadOnly()) {
                    ErrorStack.add(initElements.get(nextInitializer),"INIT","must be INV");
                }
                checkTypes(initElements.get(nextInitializer), tVar, attr.m_type);
                nextInitializer++;
            }
        }

        if (nextInitializer < initElements.size()) {
            ErrorStack.add(ctx, "INIT", "too many initialisiers");
        }

        // check type of initializers --> all identifiers have the same type 
        // => use first identifier

        String firstIdentifier = m_identifierDenotationList.get(0).toString();
        SymbolTableEntry se = m_currentSymbolTable.lookup(firstIdentifier);

        if (se instanceof SemaphoreEntry) {
            for (InitElementContext i : initElements) {
                ASTAttribute attr = m_ast.lookup(i);
                TypeDefinition t = attr.getType();
                if (t instanceof TypeFixed) {
                    if (attr.getConstantFixedValue().getValue() < 0) {
                        ErrorStack.add(i, "SEMA PRESET", "must be >= 0");
                    }
                } else {
                    ErrorStack.add(i, "SEMA PRESET", "must be FIXED");
                }
                //System.out.println(se.getName() + " . " + t);
            }
        } else if (se instanceof VariableEntry) {
            InitElementContext i = initElements.get(0);
            ASTAttribute attr = m_ast.lookup(i);
            if (attr.getConstant() == null) {
                System.out.println("preset with other variable: " + se.getName());
            }

        }


        //        
        //        if (ctx != null) {
        //            for (ParseTree c : ctx.children) {
        //                if (c instanceof SmallPearlParser.IdentifierDenotationContext) {
        //                    
        //                } else if (c instanceof SmallPearlParser.AllocationProtectionContext) {
        //                } else if (c instanceof SmallPearlParser.TypeAttributeContext) {
        //                    visitTypeAttribute((SmallPearlParser.TypeAttributeContext) c);
        //                } else if (c instanceof SmallPearlParser.GlobalAttributeContext) {
        //                } else if (c instanceof SmallPearlParser.InitialisationAttributeContext) {
        //                    getInitialisationAttribute((SmallPearlParser.InitialisationAttributeContext) c);
        //                }
        //            }
        //
        //            if (initElementList != null
        //                    && identifierDenotationList.size() != initElementList.size()) {
        //                throw new NumberOfInitializerMismatchException(ctx.getText(), ctx.start.getLine(),
        //                        ctx.start.getCharPositionInLine());
        //            }
        //
        //            // TODO: Check Type compability!
        //            for (int i = 0; i < identifierDenotationList.size(); i++) {
        //                if (initElementList != null) {
        //                    ConstantValue value = initElementList.get(i);
        //                }
        //            }
        //        }

        return null;
    }

    private void checkTypes(ParserRuleContext initElementContext, TypeDefinition tVar,
            TypeDefinition m_type) {
        String error = "type mismatch in INIT";
      //  System.out.println("check types "+tVar+" <-> "+m_type);
        if (tVar instanceof TypeFixed) {
            if (!(m_type instanceof TypeFixed) ) {
                ErrorStack.add(initElementContext, error,tVar+" := "+m_type);
            } else {
                if (tVar.getPrecision()< m_type.getPrecision()) {
                    ErrorStack.add(initElementContext, error,tVar+" := "+m_type);
                }
            }
        } else if (tVar instanceof TypeFloat) {
            if (m_type instanceof TypeFloat) {
                if (tVar.getPrecision()< m_type.getPrecision()) {
                    ErrorStack.add(initElementContext, error,tVar+" := "+m_type);
                }
            } else if (m_type instanceof TypeFixed) {
                if (tVar.getPrecision()< m_type.getPrecision()) {
                    ErrorStack.add(initElementContext, error,tVar+" := "+m_type);
                }
            }
        } else if (tVar instanceof TypeBit) {
            if (m_type instanceof TypeBit) {
                if (tVar.getPrecision()< m_type.getPrecision()) {
                    ErrorStack.add(initElementContext, error,tVar+" := "+m_type);
                }
            } else {
                ErrorStack.add(initElementContext, error,tVar+" := "+m_type);
            }
        } else if (tVar instanceof TypeChar) {
            if (m_type instanceof TypeChar) {
                if (tVar.getPrecision()< m_type.getPrecision()) {
                    ErrorStack.add(initElementContext, error,tVar+" := "+m_type);
                }
            } else {
                ErrorStack.add(initElementContext, error, tVar+ " := "+m_type);
            }
            
        } else if (tVar instanceof TypeClock) {
            if (!(m_type instanceof TypeClock)) {
                ErrorStack.add(initElementContext, error,tVar+ " := "+m_type);
            }
        } else if (tVar instanceof TypeDuration) {
            if (!(m_type instanceof TypeDuration)) {
                ErrorStack.add(initElementContext, error,tVar+ " := "+m_type);
            }
        } else if (tVar instanceof TypeSemaphore) {
            if (!(m_type instanceof TypeFixed)) {
                ErrorStack.add(initElementContext, error,"SEMA PRESET "+m_type);
            }
        }
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

    private ArrayList<Integer> getPreset(SmallPearlParser.PresetContext ctx) {
        ArrayList<Integer> presetList = new ArrayList<Integer>();

        if (ctx != null) {
            for (int i = 0; i < ctx.initElement().size(); i++) {
                Integer preset = Integer
                        .parseInt(ctx.initElement(i).getText());
                presetList.add(preset);
            }
        }

        return presetList;
    }

    private ArrayList<ConstantValue> getInitialisationAttribute(
            SmallPearlParser.InitialisationAttributeContext ctx) {
        ArrayList<ConstantValue> initList = new ArrayList<ConstantValue>();
        if (ctx != null) {
            for (int i = 0; i < ctx.initElement().size(); i++) {
                // TODO: expression
                //                initList.add(getInitElement(ctx.initElement(i).constant()));
            }
        }

        return null;
    }

    private ConstantValue getInitElement(SmallPearlParser.ConstantContext ctx) {
        if (ctx != null) {
            if (ctx.fixedConstant() != null) {
                Integer value;
                Integer sign = 1;

                value = Integer.parseInt(ctx.fixedConstant().IntegerConstant().getText());

                if (ctx.getChildCount() > 1) {
                    if (ctx.getChild(0).getText().equals("-")) {
                        value = -value;
                    }
                }

                if (Integer.toBinaryString(Math.abs(value)).length() < 31) {
                    value.toString();
                } else {
                    // value.toString() + "LL";
                }
            } else if (ctx.durationConstant() != null) {
                visitDurationConstant(ctx.durationConstant());
            } else if (ctx.timeConstant() != null) {
                visitTimeConstant(ctx.timeConstant());
            } else if (ctx.floatingPointConstant() != null) {
                Double value = 0.0;
                Integer sign = 1;

                value = CommonUtils.getFloatingPointConstantValue(ctx.floatingPointConstant());

                if (ctx.getChildCount() > 1) {
                    if (ctx.getChild(0).getText().equals("-")) {
                        value = -value;
                    }
                }

                return null;
            } else if (ctx.stringConstant() != null) {
                String s = ctx.stringConstant().StringLiteral().toString();

                if (s.startsWith("'")) {
                    s = s.substring(1, s.length());
                }

                if (s.endsWith("'")) {
                    s = s.substring(0, s.length() - 1);
                }
            } else if (ctx.bitStringConstant() != null) {
                // constant.add("BitStringConstant", getBitStringConstant(ctx));
            }
        }

        return null;
    }

//    @Override
//    public Void visitArrayVariableDeclaration(
//            SmallPearlParser.ArrayVariableDeclarationContext ctx) {
//        if (m_verbose > 0) {
//            System.out
//                    .println("Semantic: Check Variable Declaration: visitArrayVariableDeclaration");
//        }
//
//        if (ctx != null) {
//            for (ParseTree c : ctx.children) {
//                if (c instanceof SmallPearlParser.ArrayDenotationContext) {
//                    visitArrayDenotation((SmallPearlParser.ArrayDenotationContext) c);
//                }
//            }
//        }
//
//        return null;
//    }

//    @Override
//    public Void visitArrayDenotation(SmallPearlParser.ArrayDenotationContext ctx) {
//        for (int i = 0; i < ctx.ID().size(); i++) {
//            SymbolTableEntry entry = m_currentSymbolTable.lookup(ctx.ID().get(i).toString());
//
//            if (entry == null || !(entry instanceof VariableEntry)) {
//                throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
//                        ctx.start.getCharPositionInLine());
//            }
//
//            VariableEntry variableEntry = (VariableEntry) entry;
//
//            if (variableEntry.getType() instanceof TypeArray) {
//                ArrayList<ST> initElementList = null;
//
//                if (variableEntry.getType() instanceof TypeArray) {
//                    TypeArray type = (TypeArray) variableEntry.getType();
//                } else {
//                    throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(),
//                            ctx.start.getCharPositionInLine());
//                }
//
//                if (ctx.initialisationAttribute() != null) {
//                }
//
//            }
//        }
//
//        return null;
//    }

}
