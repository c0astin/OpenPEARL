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
    private SymbolTable m_symboltable;
    private AST m_ast;


    
    public CheckVariableDeclaration(String sourceFileName, int verbose, boolean debug,
            SymbolTableVisitor symbolTableVisitor, ExpressionTypeVisitor expressionTypeVisitor,
            AST ast) {

        m_verbose = verbose;
        m_symboltable = symbolTableVisitor.symbolTable;
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
    
      
    private void checkInitializer(VariableEntry v) {
        if (v.getType() instanceof TypeArrayDeclaration) {
            TypeArrayDeclaration ta = (TypeArrayDeclaration)v.getType();
            int nbrOfArrayElements = ta.getTotalNoOfElements();
            if (ta.getBaseType() instanceof TypeStructure) {
                int indexOfInitializer = 0;
                for (int arrayIndex=0; arrayIndex<nbrOfArrayElements; arrayIndex++) {
                    StructureComponent comp = ((TypeStructure)(ta.getBaseType())).getFirstElement();
                    ArrayOrStructureInitializer asi = (ArrayOrStructureInitializer)(v.getInitializer());
                    
                    do {
                        TypeDefinition typeOfElement = comp.m_type;
                        if (typeOfElement instanceof TypeArrayDeclaration) {
                            int localArraySize = ((TypeArrayDeclaration)typeOfElement).getTotalNoOfElements();
                            TypeDefinition baseType = ((TypeArrayDeclaration)typeOfElement).getBaseType();
                            for (int j=0; j<localArraySize; j++) {
                                indexOfInitializer = Math.min(indexOfInitializer, asi.getInitElementList().size()-1);
                                Initializer init = asi.getInitElementList().get(indexOfInitializer++);
                                if (init instanceof SimpleInitializer) {
                                    checkTypes(baseType, ((SimpleInitializer)init));
                                }else {
                                    ErrorStack.addInternal("missing alternativ: CheckVariabladeclaration@166");
                                } 
                            }
                        } else {
                            indexOfInitializer = Math.min(indexOfInitializer, asi.getInitElementList().size()-1);
                            Initializer init = asi.getInitElementList().get(indexOfInitializer++);
                            if (init instanceof SimpleInitializer) {
                                checkTypes(typeOfElement, ((SimpleInitializer)init));
                            } else {
                                ErrorStack.addInternal("missing alternativ: CheckVariabladeclaration@158");
                            }
                        }
                        comp = ((TypeStructure)(ta.getBaseType())).getNextElement();
                    } while (comp!= null);
                }
            } else if (isScalarType(ta.getBaseType())) {
                TypeDefinition baseType = ta.getBaseType();    
                ArrayOrStructureInitializer asi = (ArrayOrStructureInitializer)(v.getInitializer());
                for (int i=0; i<asi.getInitElementList().size(); i++) {
                    if (asi.getInitElementList().get(i) instanceof SimpleInitializer) {
                       SimpleInitializer init = (SimpleInitializer)(asi.getInitElementList().get(i));
                       checkTypes(baseType, init);
                    } else {
                        checkRefInitializer(baseType, asi.getInitElementList().get(i));
                    }
                }
            }
        } else if (isScalarType(v.getType()) || v.getType() instanceof UserDefinedSimpleType) {
            if (v.getType() instanceof TypeReference) {
                checkRefInitializer(v.getType(),v.getInitializer());
                
            } else {
               SimpleInitializer si = (SimpleInitializer )(v.getInitializer());
               checkTypes(v.getType(), si);
            }

        } else if (v.getType() instanceof TypeStructure || v.getType() instanceof UserDefinedTypeStructure){
            StructureComponent comp;
            if (v.getType() instanceof TypeStructure) {
              comp = ((TypeStructure)(v.getType())).getFirstElement();
            } else {
                comp = ((TypeStructure)((UserDefinedTypeStructure)(v.getType())).getStructuredType()).getFirstElement(); 
            }
            int nextInitializer = 0;
            ArrayOrStructureInitializer asi = (ArrayOrStructureInitializer)(v.getInitializer());
            do {
                TypeDefinition t = comp.m_type;
                Initializer init = asi.getInitElementList().get(nextInitializer);
                nextInitializer++;
                if (init instanceof SimpleInitializer) {
                    SimpleInitializer si = (SimpleInitializer)init;
                    checkTypes(t, si);
                } else {
                    //  is Reference Initializer
                    checkRefInitializer(t, init);
                }
                if (v.getType() instanceof TypeStructure) {
                    comp = ((TypeStructure)(v.getType())).getNextElement();
                  } else {
                      comp = ((TypeStructure)((UserDefinedTypeStructure)(v.getType())).getStructuredType()).getNextElement(); 
                  }

            } while (comp!= null);
        } else {
            ErrorStack.addInternal(v.getCtx(),"CheckVariableDeclaration","missing alternative@137 for "+v.getType());
        }

    }

    private void checkRefInitializer(TypeDefinition typeOfVariableOrComponent, Initializer init) {
        // typeOfVariableOrComponent must match the type of the initializer
        // except it is possible to initialize an REF INV xxx with type xxx to reduce
        // access possibilities
        ParserRuleContext ctx = init.getContext();
        ASTAttribute attrInit=null;
        TypeDefinition baseTypeOfVariableOrComponent = null;
        if (typeOfVariableOrComponent instanceof TypeReference) {
            baseTypeOfVariableOrComponent = ((TypeReference)typeOfVariableOrComponent).getBaseType();
        } else {
            ErrorStack.addInternal(ctx, "CheckVariableDefinition", "missing alternative@210");
        }
       

        if (init instanceof ReferenceInitializer) {
            ReferenceInitializer ri =(ReferenceInitializer)(init);
            ctx = ri.getContext();
            attrInit = m_ast.lookup(ri.getContext());
            //attrInit.setVariable((VariableEntry)(ri.getSymbolTableEntry()));
            attrInit.setSymbolTableEntry((ri.getSymbolTableEntry()));
        } else if (init instanceof SimpleInitializer) {
            SimpleInitializer si =(SimpleInitializer)(init);
            ctx = si.getContext();
            attrInit = m_ast.lookup(si.getContext());
        }
        if (attrInit == null) {
            ErrorStack.addInternal(ctx, "CheckVariableDeclaration@155", "no ASTAttribute found");
            return ;
        }
        if (baseTypeOfVariableOrComponent instanceof TypeArray ) {
            TypeArray tas = (TypeArray)baseTypeOfVariableOrComponent;
            boolean ok = true;
            TypeDefinition typeOfInitializer = null;
            if (attrInit.m_type instanceof TypeArrayDeclaration) {
                typeOfInitializer = ((TypeArrayDeclaration)(attrInit.m_type)).getBaseType();
                if (tas.getNoOfDimensions() != ((TypeArrayDeclaration)(attrInit.m_type)).getNoOfDimensions()) {
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
                ErrorStack.enter(ctx);
                CommonErrorMessages.typeMismatchInInit(typeOfVariableOrComponent.toErrorString(), attrInit.getType().toErrorString(), attrInit);
//                ErrorStack.add(ctx, "type mismatch in REF INIT",
//                        "REF "+baseTypeOfVariableOrComponent.toString4IMC(true)  +" := " + attrInit.getType().toString4IMC(true));
                ErrorStack.leave();
            } 

        } else {
            boolean ok= false;
            if (baseTypeOfVariableOrComponent instanceof TypeRefChar) {
                if ( (attrInit.getType() instanceof TypeChar)) {
                    ok = true;
                }
            } else if (attrInit.getType().getName().equals(baseTypeOfVariableOrComponent.getName()) && 
                    attrInit.getType().getPrecision() == baseTypeOfVariableOrComponent.getPrecision()) {
                ok = true;
            }  else if (attrInit.getType().getName().equals(typeOfVariableOrComponent.getName()) &&
                    attrInit.getType().getPrecision() == typeOfVariableOrComponent.getPrecision()) {
                ok = true;
            }
            if (ok && baseTypeOfVariableOrComponent.hasAssignmentProtection()==false &&
                    !(attrInit.getType() instanceof TypeReference) && attrInit.getType().hasAssignmentProtection()) {
                ok = false;
            }
            if (!ok) {
//                ErrorStack.add(ctx,"type mismatch in REF INIT",
//                        "REF "+baseTypeOfVariableOrComponent.toString4IMC(true)  +" := " + attrInit.getType().toString4IMC(true));
                ErrorStack.enter(ctx);
                CommonErrorMessages.typeMismatchInInit(typeOfVariableOrComponent.toErrorString(), attrInit.getType().toErrorString(), attrInit);
                ErrorStack.leave();
            }
        }
    }
    
    /**
     * 
     * @param initializer is either a simple initializer or a reference initializer 
     * @param baseType the base type of the variable
     */
    private void checkTypes(TypeDefinition typeOfVariable, SimpleInitializer simpleInitializer) {
        
        ParserRuleContext ctx = simpleInitializer.getContext();
        
        if (simpleInitializer.getConstant() == null) {
            ASTAttribute attr = m_ast.lookup(ctx);
            simpleInitializer.setConstant(attr.getConstant());
        }
        
        
        if (isSimpleType(simpleInitializer.getConstant().getType()) ) {
             checkTypeCompatibility(typeOfVariable, simpleInitializer.getConstant().getType(),ctx);
        }
        if (typeOfVariable instanceof TypeSemaphore) {
            if (simpleInitializer.getConstant().getType() instanceof TypeFixed) {
                long value = ((ConstantFixedValue)(simpleInitializer.getConstant())).getValue();
                if ( value < 0) {
                    ErrorStack.add(ctx,"SEMA PRESET","must be not negative");
                }
            }
        }
    }

    private boolean checkTypeCompatibility(TypeDefinition typeOfVariable, TypeDefinition typeOfInitializer, ParserRuleContext ctxOfInitElement) {
        TypeDefinition effectiveTypeOfvariable = typeOfVariable;
        
        ASTAttribute attrInit = m_ast.lookup(ctxOfInitElement);
        
        ErrorStack.enter(ctxOfInitElement);
        
        if (typeOfVariable instanceof UserDefinedSimpleType) {
            effectiveTypeOfvariable = ((UserDefinedSimpleType)typeOfVariable).getSimpleType();
        }
        
        if (effectiveTypeOfvariable instanceof TypeFixed && typeOfInitializer instanceof TypeFixed) {
            if (((TypeFixed)effectiveTypeOfvariable).getPrecision() < ((TypeFixed)typeOfInitializer).getPrecision()) {
                CommonErrorMessages.typeMismatchInInit(typeOfVariable.toErrorString(), typeOfInitializer.toErrorString(),attrInit);
            }
        } else if (effectiveTypeOfvariable instanceof TypeFloat && typeOfInitializer instanceof TypeFloat) {
                if (((TypeFloat)effectiveTypeOfvariable).getPrecision() < ((TypeFloat)typeOfInitializer).getPrecision()) {
                    CommonErrorMessages.typeMismatchInInit(typeOfVariable.toErrorString(), typeOfInitializer.toErrorString(),attrInit);
                }
        } else if (effectiveTypeOfvariable instanceof TypeFloat && typeOfInitializer instanceof TypeFixed) {
            if (((TypeFloat)effectiveTypeOfvariable).getPrecision() < ((TypeFixed)typeOfInitializer).getPrecision()) {
                CommonErrorMessages.typeMismatchInInit(typeOfVariable.toErrorString(), typeOfInitializer.toErrorString(),attrInit);
            }
        } else if (effectiveTypeOfvariable instanceof TypeChar && typeOfInitializer instanceof TypeChar) {
            if (((TypeChar)effectiveTypeOfvariable).getPrecision() < ((TypeChar)typeOfInitializer).getPrecision()) {
                CommonErrorMessages.typeMismatchInInit(typeOfVariable.toErrorString(), typeOfInitializer.toErrorString(),attrInit);
            }
        } else if (effectiveTypeOfvariable instanceof TypeBit && typeOfInitializer instanceof TypeBit) {
            if (((TypeBit)effectiveTypeOfvariable).getPrecision() < ((TypeBit)typeOfInitializer).getPrecision()) {
                CommonErrorMessages.typeMismatchInInit(typeOfVariable.toErrorString(), typeOfInitializer.toErrorString(),attrInit);
            }
        } else if (effectiveTypeOfvariable instanceof TypeSemaphore) {
           if (!(typeOfInitializer instanceof TypeFixed)) {  
              CommonErrorMessages.typeMismatchInInit(typeOfVariable.toErrorString(), typeOfInitializer.toErrorString(),attrInit);
           }
        } else if (!effectiveTypeOfvariable.equals(typeOfInitializer) ) {
           CommonErrorMessages.typeMismatchInInit(typeOfVariable.toErrorString(), typeOfInitializer.toErrorString(),attrInit);
        } 
        
        ErrorStack.leave();
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
