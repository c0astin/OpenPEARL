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
import org.antlr.v4.runtime.tree.ParseTree;
import org.openpearl.compiler.*;
import org.openpearl.compiler.SymbolTable.ModuleEntry;
import org.openpearl.compiler.SymbolTable.SemaphoreEntry;
import org.openpearl.compiler.SymbolTable.SymbolTable;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;
import java.util.ArrayList;
import java.util.List;

/**
 
 * @author Marcel Schaible
 * @version 1
 *   <p><b>Description</b>
 * current state
 * <ol>
 * <li>symbol table contains all defined and specified symbols
 * <li>SymbolTableVisitor checks only initializers for type FIXED
 * <li>all initializers are in the symbol table
 * <li>for each expression we have an ASTAttribute 
 * <li>initializers of type identifier must be INV, if the variable is not of type REF  
 * </ol>
 * 
 * Check initialisers for
 * <ol>
 * <li> fitting type
 * </ol>
 * 
 * principle of operation
 * <ul>
 * <li>retrieve list of all defined variables from the SymbolTable
 * <li>check the types of given initializers
 * </ul>
 *    
 * Problems:
 *  <ol>
 *  <li>for TypeStructure we need an iterator over all component elements, even with nested structures
 *  <li>we should not extend the initializer to the size of the variable to safe space in the C++-code - 
 *     especially if CHAR-Variables become initialized like x CHAR(10) INIT (' '); 
 *  </ol>
 */


public class CheckVariableDeclaration extends OpenPearlBaseVisitor<Void>
        implements OpenPearlVisitor<Void> {

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
            
            if (v.getInitializer() != null) {
                checkInitializer(v);
            }
        }
    }
    
    /* simple implementation without nested structure components and no arrays as structure components */
    private int m_componentIndex;
    private TypeStructure m_typeStructure;
    
    private TypeDefinition getFirstStructureComponentElement (TypeStructure ts){
        m_componentIndex = 0;
        m_typeStructure = ts;
        TypeDefinition t = m_typeStructure.getStructureComponentByIndex(m_componentIndex).m_type;
        m_componentIndex ++;
        return t;
    }
    
    private TypeDefinition getNextStructureComponentElement (){
        if (m_componentIndex < m_typeStructure.getTotalNoOfElements()) {
            TypeDefinition t = m_typeStructure.getStructureComponentByIndex(m_componentIndex).m_type;
            m_componentIndex ++;
            return t;
        }
        return null;
    }
    
    private void checkInitializer(VariableEntry v) {
        if (v.getType() instanceof TypeArray) {
            TypeArray ta = (TypeArray)v.getType();
            if (ta.getBaseType() instanceof TypeStructure) {
                ErrorStack.addInternal(v.getCtx(), "CheckVariableDeclaration@137", "STRUCT initializer not implemented yet");

            } else if (isScalarType(ta.getBaseType())) {
                TypeDefinition baseType = ta.getBaseType();    
                ArrayOrStructureInitializer asi = (ArrayOrStructureInitializer)(v.getInitializer());
                for (int i=0; i<asi.getInitElementList().size(); i++) {
                    SimpleInitializer init = (SimpleInitializer)(asi.getInitElementList().get(i));
                    checkTypes(baseType, init.getConstant(), init.getContext());
                }
            }
        } else if (isScalarType(v.getType())) {
            if (v.getType() instanceof TypeReference) {
                // baseType must match the type of the initializer
                // except it is possible to initialize an REF INV xxx with type xxx to reduce
                // access possibilities
                TypeDefinition baseTypeOfVariable = ((TypeReference)(v.getType())).getBaseType();
                ASTAttribute attrInit=null;
                ParserRuleContext ctx=null;
                if (v.getInitializer() instanceof ReferenceInitializer) {
                   ReferenceInitializer ri =(ReferenceInitializer)(v.getInitializer());
                   ctx = ri.getContext();
                   attrInit = m_ast.lookup(ri.getContext());
                } else if (v.getInitializer() instanceof SimpleInitializer) {
                    SimpleInitializer si =(SimpleInitializer)(v.getInitializer());
                    ctx = si.getContext();
                    attrInit = m_ast.lookup(si.getContext());
                }
                if (attrInit == null) {
                    ErrorStack.addInternal(ctx, "CheckVariableDeclaration@155", "no ASTAttribute found");
                    return ;
                }
                if (baseTypeOfVariable instanceof TypeArraySpecification ) {
                    TypeArraySpecification tas = (TypeArraySpecification)baseTypeOfVariable;
                    boolean ok = true;
                    TypeDefinition typeOfInitializer = null;
                    if (attrInit.m_type instanceof TypeArray) {
                        typeOfInitializer = ((TypeArray)(attrInit.m_type)).getBaseType();
                        if (tas.getNoOfDimensions() != ((TypeArray)(attrInit.m_type)).getNoOfDimensions()) {
                            ok = false;
                        }
                    } else {
                        ok = false;
                    }
                    if (ok && typeOfInitializer != null) {
                           
                    
                            if (!typeOfInitializer.getName().equals(tas.getBaseType().getName()) || 
                                    typeOfInitializer.getPrecision() != tas.getBaseType().getPrecision()) {
                                ok = false;
                            }
                    }
                    if (ok) {
                        if (tas.getBaseType().hasAssignmentProtection() == false && 
                                typeOfInitializer.hasAssignmentProtection()) {
                            ok = false;
                        }
                    }
                    if (!ok) {
                        ErrorStack.add(ctx, "type mismatch in REF INIT",
                             "REF "+baseTypeOfVariable.toString4IMC(true)  +" := " + attrInit.getType().toString4IMC(true));    
                    } 
                    
                } else {
                    boolean ok= true;
                    if (baseTypeOfVariable instanceof TypeRefChar) {
                        if (! (attrInit.getType() instanceof TypeChar)) {
                            ok = false;
                        }
                    } else if (!attrInit.getType().getName().equals(baseTypeOfVariable.getName()) || 
                            attrInit.getType().getPrecision() != baseTypeOfVariable.getPrecision()) {
                        ok = false;
                    }
                    if (ok && baseTypeOfVariable.hasAssignmentProtection()==false &&
                            attrInit.isConstant()) {
                        ok = false;
                    }
                    if (!ok) {
                        ErrorStack.add(ctx,"type mismatch in REF INIT",
                                "REF "+baseTypeOfVariable.toString4IMC(true)  +" := " + attrInit.getType().toString4IMC(true));
                    }
                }
            } else {
               SimpleInitializer si = (SimpleInitializer )(v.getInitializer());
               checkTypes(v.getType(), si.getConstant(),si.getContext());
            }

        } else if (v.getType() instanceof TypeStructure){
            TypeDefinition t = getFirstStructureComponentElement((TypeStructure)(v.getType()));
            int nextInitializer = 0;
            ArrayOrStructureInitializer asi = (ArrayOrStructureInitializer)(v.getInitializer());
            do {
                SimpleInitializer init = (SimpleInitializer)(asi.getInitElementList().get(nextInitializer));
                nextInitializer++;
                checkTypes(t, init.getConstant(), init.getContext());
                t=getNextStructureComponentElement();
            } while (t!= null);


        } else {
            ErrorStack.addInternal(v.getCtx(),"CheckVariableDeclaration","missing alternative@137 for "+v.getType());
        }

    }
    
    /**
     * 
     * @param initializer is ether a simple initializer or a reference initializer 
     * @param baseType the base type of the variable
     */
    private void checkTypes(TypeDefinition typeOfVariable, ConstantValue initializer, ParserRuleContext ctx) {

        if (isSimpleType(initializer.getType()) ) {
             checkTypeCompatibility(typeOfVariable, initializer.getType(),ctx);
        }
        if (typeOfVariable instanceof TypeSemaphore) {
            long value = ((ConstantFixedValue)initializer).getValue();
            if ( value < 0) {
                ErrorStack.add(ctx,"PRESET","must be not negative");
            }
        }
            
//        } else {
//            ErrorStack.add(ctx, "CheckVariableDeclaration", "untreated type of initializer");
//        }
    }

    private boolean checkTypeCompatibility(TypeDefinition typeOfVariable, TypeDefinition typeOfInitializer, ParserRuleContext ctxOfInitElement) {
        
        if (typeOfVariable instanceof TypeFixed && typeOfInitializer instanceof TypeFixed) {
            if (((TypeFixed)typeOfVariable).getPrecision() < ((TypeFixed)typeOfInitializer).getPrecision()) {
                ErrorStack.add(ctxOfInitElement,"type mismatch in INIT",typeOfVariable +" := " + typeOfInitializer);
            }
        } else if (typeOfVariable instanceof TypeFloat && typeOfInitializer instanceof TypeFloat) {
                if (((TypeFloat)typeOfVariable).getPrecision() < ((TypeFloat)typeOfInitializer).getPrecision()) {
                    ErrorStack.add(ctxOfInitElement,"type mismatch in INIT",typeOfVariable +" := " + typeOfInitializer);
                }
        } else if (typeOfVariable instanceof TypeFloat && typeOfInitializer instanceof TypeFixed) {
            if (((TypeFloat)typeOfVariable).getPrecision() < ((TypeFixed)typeOfInitializer).getPrecision()) {
                ErrorStack.add(ctxOfInitElement,"type mismatch in INIT",typeOfVariable +" := " + typeOfInitializer);
            }
        } else if (typeOfVariable instanceof TypeChar && typeOfInitializer instanceof TypeChar) {
            if (((TypeChar)typeOfVariable).getPrecision() < ((TypeChar)typeOfInitializer).getPrecision()) {
                ErrorStack.add(ctxOfInitElement,"type mismatch in INIT",typeOfVariable +" := " + typeOfInitializer);
            }
        } else if (typeOfVariable instanceof TypeBit && typeOfInitializer instanceof TypeBit) {
            if (((TypeBit)typeOfVariable).getPrecision() < ((TypeBit)typeOfInitializer).getPrecision()) {
                ErrorStack.add(ctxOfInitElement,"type mismatch in INIT",typeOfVariable +" := " + typeOfInitializer);
            }
        } else if (typeOfVariable instanceof TypeSemaphore) {
           if (!(typeOfInitializer instanceof TypeFixed)) {  
               ErrorStack.add(ctxOfInitElement,"type mismatch in PRESET",typeOfVariable +" with " + typeOfInitializer);
           }
        } else if (!typeOfVariable.equals(typeOfInitializer) ) {
            ErrorStack.add(ctxOfInitElement,"type mismatch in INIT",typeOfVariable +" := " + typeOfInitializer);
        } 
        
        return false;
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



}
