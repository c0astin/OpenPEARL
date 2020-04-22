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

import org.smallpearl.compiler.*;
import org.smallpearl.compiler.Exception.InternalCompilerErrorException;
import org.smallpearl.compiler.Exception.TypeMismatchException;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;

public class CheckAssignment extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

  private int m_verbose;
  private boolean m_debug;
  private String m_sourceFileName;
  private ExpressionTypeVisitor m_expressionTypeVisitor;
  private SymbolTableVisitor m_symbolTableVisitor;
  private SymbolTable m_symboltable;
  private SymbolTable m_currentSymbolTable;
  private ModuleEntry m_module;
  private AST m_ast = null;

  public CheckAssignment(String sourceFileName,
      int verbose,
      boolean debug,
      SymbolTableVisitor symbolTableVisitor,
      ExpressionTypeVisitor expressionTypeVisitor,
      AST ast) {

    m_debug = debug;
    m_verbose = verbose;
    m_sourceFileName = sourceFileName;
    m_symbolTableVisitor = symbolTableVisitor;
    m_expressionTypeVisitor = expressionTypeVisitor;
    m_symboltable = symbolTableVisitor.symbolTable;
    m_currentSymbolTable = m_symboltable;
    m_ast = ast;

    Log.info("Semantic Check: Check assignment statements");    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // The type of the variable given to the left of the assignment sign has to match the type of the  value of the
  // expression, with the following exceptions:
  //  (1) The value of a FIXED variable or an integer, resp., may be assigned to a FLOAT variable.
  //  (2) The precision of a numeric variable to the left of an assignment sign may be greater than the precision of
  //      the value of the expression.
  //  (3) A bit or character string, resp., to the left may have a greater length than the value to be assigned; if
  //      needed, the latter is extended by zeros or spaces, resp., on the right.
  //  (4) A variable (no expression) may be assigned to a REF. In this case the type must match exactly
  //  (5) A reference may be assigned to a variable, if the reference is in a struct or array
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  @Override
  public Void visitAssignment_statement(SmallPearlParser.Assignment_statementContext ctx) {
    Log.debug("CheckAssignment:visitAssignment_statement:ctx" + CommonUtils.printContext(ctx));
    String id = null;
    
    ErrorStack.enter(ctx,"assignment");
    ASTAttribute lhsAttr = null; 
    ConstantSelection selection = null;
    TypeDefinition lhsType = null;

    SmallPearlParser.NameContext ctxName = null;
    if ( ctx.stringSelection() != null ) {
      lhsAttr = m_ast.lookup(ctx.stringSelection()); 
      selection = lhsAttr.getConstantSelection();
      if ( ctx.stringSelection().charSelection() != null ) {
        ctxName = ctx.stringSelection().charSelection().name();

      }
      else  if (ctx.stringSelection().bitSelection() != null) {
        ctxName = ctx.stringSelection().bitSelection().name();
      } else {
        ErrorStack.addInternal("CheckAssignment: missing alternative");
        ErrorStack.leave();
        return null; 
      }
    } else {
      // no selection; is  ('CONT')? name  
      lhsAttr = m_ast.lookup(ctx.name()); 
      ctxName = ctx.name();
    }

    lhsType = lhsAttr.getType();
    id = ctxName.ID().getText();
    SymbolTableEntry lhs = m_currentSymbolTable.lookup(id);
    VariableEntry lhsVariable = null;
    if (lhs != null && lhs instanceof VariableEntry) {
       lhsVariable = (VariableEntry)lhs;
    } else {
      ErrorStack.addInternal(id+" not in symbol table or is no variable");
    }

    Log.debug("CheckAssignment:visitAssignment_statement:ctx.expression" + CommonUtils.printContext(ctx.expression()));

    if (!(lhsType instanceof TypeStructure ||
        lhsType instanceof TypeReference ||
        lhsType instanceof TypeVariableChar ||
        isSimpleType(lhsType))) {
      ErrorStack.add(lhsAttr.getType().toString() +" not allowed on lhs"); 
    } else { 
    
    SmallPearlParser.ExpressionContext expr = ctx.expression();
 String s = ctx.getText();
    TypeDefinition rhsType = m_ast.lookupType(ctx.expression());
    ASTAttribute rhsAttr = m_ast.lookup(ctx.expression());
    VariableEntry rhsVariable = rhsAttr.getVariable();
    
    if (lhsType instanceof TypeReference && ctx.dereference() == null &&
         rhsType instanceof TypeReference) {
      // pointer assignment refVar1 := refVar2;
      // base types must be identical
      lhsType = ((TypeReference)lhsType).getBaseType();
      rhsType = ((TypeReference)rhsType).getBaseType();
      checkTypes(lhsType, lhsAttr, rhsType, rhsAttr, true); // match exactly
    } else if (lhsType instanceof TypeReference && ctx.dereference() != null &&   
              !(rhsType instanceof TypeReference)) {
      // CONT refVar = expr
      // types must match relaxed
      lhsType = ((TypeReference)lhsType).getBaseType();
      checkTypes(lhsType, lhsAttr, rhsType, rhsAttr, false); // lhs may be larger
    } else if (lhsType instanceof TypeReference &&
        !(rhsType instanceof TypeReference)) {
      // refVar = var; no expression allowed on rhs!
      // need variable or TASK,SEMA,...INTERRUPT
      if  (isReferableType(rhsType) ||
           rhsAttr.getVariable() != null) {
        lhsType = ((TypeReference)lhsType).getBaseType();
        checkTypes(lhsType, lhsAttr, rhsType, rhsAttr, true); // match exactly
      } else {
        ErrorStack.add("reference must point to a variable or TASK,SEMA,BOLT,INTERRUPT or SIGNAL");
      } 
    } else if (!(lhsType instanceof TypeReference) &&
        !(rhsType instanceof TypeReference)) {
      // simple assignment var:= expr
      checkTypes(lhsType, lhsAttr, rhsType, rhsAttr, false); // lhs may be larger
    } else if (!(lhsType instanceof TypeReference) &&
        rhsType instanceof TypeReference) {
      // assignment var:= refVar
      // rhsVariable must be array or struct
      if (rhsVariable.getType() instanceof TypeStructure ||
          rhsVariable.getType() instanceof TypeArray ) {
        // ok auto derefenece
        rhsType = ((TypeReference)rhsType).getBaseType();
      }  
      
      checkTypes(lhsType, lhsAttr, rhsType, rhsAttr, false); // lhs may be larger
    } else {
      ErrorStack.add("type mismatch: "+lhsAttr.getType().toString()+":="+rhsAttr.getType().toString());
    }

// old code - should be removed after 2020-05-01 :-))    
//    if ( lhs instanceof VariableEntry) {
//      VariableEntry variable = (VariableEntry) lhs;
//      if (variable.getAssigmentProtection()) {
//        ErrorStack.add("left hand side is INV");
//      }
//      if ( lhs == null ) {
//        throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//      }
//
//      if ( variable.getLoopControlVariable()) {
//        ErrorStack.add("left hand side is loop variable");
//      }
//
//      if ( rhsType == null ) {
//        throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//      }
//
//      boolean implicitDereference = false;
//      // check for implicit dereferenciation on array-elements and struct-components
//      // on rhs; if an array element with typeReferenvce is on lhs, we must set the pointer
//      // according the rhs
//
//      if (!(lhsType instanceof TypeReference) && rhsType instanceof TypeReference) {
//        VariableEntry ve = attrRhs.getVariable(); 
//        if (ve!=null) {
//          if (ve.getType() instanceof TypeArray || ve.getType() instanceof TypeStructure) {
//            if (!(lhsType instanceof TypeReference)) {
//              // let's dereference the rhsType here
//              rhsType = ((TypeReference)rhsType).getBaseType();
//              implicitDereference=true;
//            }
//          }
//        }
//      }
//      
//      if ( variable.getType() instanceof TypeFloat ) {
//        TypeFloat lhs_type = (TypeFloat) variable.getType();
//
//        if ( !(rhsType instanceof  TypeFloat || rhsType instanceof TypeFixed) ) {
//          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//        }
//
//        if ( rhsType instanceof TypeFloat ) {
//          TypeFloat rhs_type = (TypeFloat)rhsType;
//
//          if ( rhs_type.getPrecision() >  lhs_type.getPrecision() ) {
//            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//          }
//        }
//        else if ( rhsType instanceof TypeFixed ) {
//          TypeFixed rhs_type = (TypeFixed) rhsType;
//        }
//      }
//      else if ( variable.getType() instanceof TypeFixed ) {
//        TypeFixed lhs_type = (TypeFixed) variable.getType();
//
///*        if ( rhsType instanceof TypeReference &&
//            ((TypeReference)rhsType).getBaseType() instanceof TypeFixed) {
//          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//        } else*/
//        if ( !(rhsType instanceof TypeFixed) ) {
//          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//        } else { 
//
//          int rhs_precision = 0;
///*          if (rhsType instanceof TypeReference) {
//            TypeFixed typ = (TypeFixed) ((TypeReference) rhsType).getBaseType();
//            rhs_precision = typ.getPrecision();
//          }
//          else { */
//            rhs_precision = ((TypeFixed) rhsType).getPrecision();
////          }
//
//          if ( ((TypeFixed) variable.getType()).getPrecision() < rhs_precision ) {
//            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//          }
//        }
//      }
//      else if ( variable.getType() instanceof TypeClock ) {
//        if ( !(rhsType instanceof TypeClock) ) {
//          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//        }
//      }
//      else if ( variable.getType() instanceof TypeDuration ) {
//        if ( !(rhsType instanceof TypeDuration) ) {
//          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//        }
//      }
//      else if ( variable.getType() instanceof TypeBit ) {
//        TypeBit lhs_type = (TypeBit) variable.getType();
//
//        if (!(rhsType instanceof TypeBit)) {
//          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//        }
//
//        TypeBit rhs_type = (TypeBit) rhsType;
//
//        if (rhs_type.getPrecision() > lhs_type.getPrecision()) {
//          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//        }
//      }
//      else if (variable.getType() instanceof TypeChar) {
//        if (!(rhsType instanceof TypeChar || rhsType instanceof TypeVariableChar)) {
//          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().getName());
//        }
//        TypeChar lhs_type = (TypeChar) variable.getType();
//        if (rhsType instanceof TypeChar) {
//          TypeChar rhs_type = (TypeChar)rhsType;
//          if (selection == null) {
//            if (rhs_type.getPrecision() > lhs_type.getPrecision()) {
//              ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//            }
//          } else {
//            if (rhs_type.getPrecision() > selection.getNoOfElements()) {
//              ErrorStack.add("type mismatch: CHAR("+selection.getNoOfElements()+") := "+attrRhs.getType().toString());
//            }
//          }
//        }
//      }
//      else if ( variable.getType() instanceof TypeReference ) {
//        TypeReference lhs_type = (TypeReference) variable.getType();
//        TypeDefinition rhs_type;
//
//        if ( ctx.dereference() == null ) {
//          if ( (attrRhs.getVariable() == null) && ( !(attrRhs.getType() instanceof TypeTask))) {
//            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//          }
//
//          TypeDefinition lt = lhs_type.getBaseType();
//
//          if ( rhsType instanceof TypeReference) {
//            rhs_type = ((TypeReference) rhsType).getBaseType();
//          }
//          else {
//            rhs_type = rhsType;
//          }
//
//          if ( !(lt.equals(rhs_type))) {
//            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//          }
//        }
//        else {
//          TypeDefinition lt = lhs_type.getBaseType();
//          if ( !(lt.equals(rhsType))) {
//            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+attrRhs.getType().toString());
//          }
//        }
//      }
//      else if ( variable.getType() instanceof TypeTask ) {
//        System.out.println("Semantic: visitAssignment_statement: TASK");
//      }
//    }
//    else {
//      throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
//    }
    }
    
    ErrorStack.leave();
    return null;
  }
  
  
  private void checkTypes(TypeDefinition lhsType, ASTAttribute lhsAttr,
      TypeDefinition rhsType, ASTAttribute rhsAttr,
      boolean matchExact) {
    
    boolean typeMismatch = false;


    
    if (matchExact) {
      if (!lhsType.equals(rhsType)) {
        ErrorStack.add("type mismatch: " +lhsAttr.getType().toString() +" := " + rhsAttr.getType().toString());
        typeMismatch = true; 
      }
    } else {
      // 1) assignment to float must accept also fixed on the rhs
      // 2) TypeVariableChar on lhs or rhs without ConstantSelection fit to char of any length
      //    the check is done during runtime
      if (getTypeWithoutPrecision(lhsType) instanceof TypeFloat) {
        if (getTypeWithoutPrecision(rhsType) instanceof TypeFloat ||
            getTypeWithoutPrecision(rhsType) instanceof TypeFixed) {
          if (lhsType.getPrecision() < rhsType.getPrecision()) {
            ErrorStack.add("type mismatch: " +lhsAttr.getType().toString() +" := " + rhsAttr.getType().toString());
            typeMismatch = true; 
          }          
        } else {
          typeMismatch = true;
        }
      } else if (lhsType instanceof TypeVariableChar) {
        if (!(rhsType instanceof TypeVariableChar || rhsType instanceof TypeChar)) {
          ErrorStack.add("type mismatch: " +lhsAttr.getType().toString() +" := " + rhsAttr.getType().toString());
          typeMismatch = true; 
        }
      } else if (rhsType instanceof TypeVariableChar) {
        if (!(lhsType instanceof TypeVariableChar || lhsType instanceof TypeChar)) {
          ErrorStack.add("type mismatch: " +lhsAttr.getType().toString() +" := " + rhsAttr.getType().toString());
          typeMismatch = true; 
        }
      } else if (!getTypeWithoutPrecision(lhsType).equals(getTypeWithoutPrecision(rhsType))) {
          typeMismatch = true; 
      } else if (lhsType.getPrecision() < rhsType.getPrecision()) {
        typeMismatch = true; 
      }
    
    }
    if (typeMismatch) {
      ErrorStack.add("type mismatch: " +lhsAttr.getType().toString() +" := " + rhsAttr.getType().toString());
    }
    return;
  }
  
  private TypeDefinition getTypeWithoutPrecision(TypeDefinition t) {
    if (t instanceof TypeFixed) {
      return new TypeFixed();
    }
    if (t instanceof TypeFloat) {
      return new TypeFloat();
    }
    if (t instanceof TypeChar) {
      return new TypeChar();
    }
    if (t instanceof TypeBit) {
      return new TypeBit();
    }
    return t;
  }
  
  private boolean isSimpleType(TypeDefinition type) {
    boolean result = false;

    if (type instanceof TypeFixed    ||
        type instanceof TypeFloat    ||
        type instanceof TypeBit      ||
        type instanceof TypeChar     ||
        type instanceof TypeDuration ||
        type instanceof TypeClock ) {
      result = true;
    }
    return result;
  }
  
  // naming not good, since simple types are also referable
  private boolean isReferableType(TypeDefinition rhsType) {
    boolean result = false;

    if (rhsType instanceof TypeTask      ||
        rhsType instanceof TypeSemaphore ||
        rhsType instanceof TypeBolt      ||
        rhsType instanceof TypeSignal    ||
        rhsType instanceof TypeInterrupt) {
      result = true;
    }
    return result;
  }

  @Override
  public Void visitModule(SmallPearlParser.ModuleContext ctx) {
    org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry = m_currentSymbolTable.lookupLocal(ctx.ID().getText());
    m_currentSymbolTable = ((org.smallpearl.compiler.SymbolTable.ModuleEntry)symbolTableEntry).scope;
    visitChildren(ctx);
    m_currentSymbolTable = m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    m_currentSymbolTable = m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
    return null;
  }
}