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

package org.openpearl.compiler;

import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;

/**
 * <p><b>Description</b> 
 * Additional information about a nodes in the AST are stored in objects of this type.
 * The ETV adds type information and required operations about implicit dereference and implicit 
 * procedure calls.
 * 
 * Stored data:
 * <ul>
 *   <li>{@link  org.openpearl.compiler.TypeDefinition} : the effective type of an expression</li>
 *   <li>{@link org.openpearl.compiler.SymbolTable.SymbolTableEntry} the reference to the object in the symbol table<br>
 *     this is null, if no symbol is used</li> 
 *   <li>{@link org.openpearl.compiler.ConstantValue} the value of a constant, may be if any simple type</li>  
 *   <li>{@link org.openpearl.compiler.ConstantSelection} is used for .CHAR(a:b) or .BIT(a:b)</li>
 *   <li>flags for: 
 *      <ul>
 *         <li>isConstant</li>
 *         <li>IsFunctionCall</li>
 *         <li>isInternal: the symbol is created automatically for EXIT statements</li>
 *         <li>needImplicitDereferencing indicates that the resultind data needs an implicit
 *             dereference</lI>
 *         <li>isLValue indicates that the variable is writable</li>
 *         <li>arrayOrProcedure needs implicit dereference</lI>
 *      </ul>
 *      All flags are cleared at creation of an ASTAttribute
 *      the individual flags are defined as static final like bitIsConstant, ..
 *      they are set and cleared via bit operations in the m_flags attribute
 * </ul>
 */
public class ASTAttribute {
    public TypeDefinition  m_type;
    private int m_flags=0;
    private SymbolTableEntry m_entry;
    public ConstantValue m_constant;
    public ConstantSelection m_selection;
    
    private static final int bitIsConstant = 0x01;
    
    /** flag is set if the corresponding symbol table entry 
     * is generated automatically (like unnamed blocks or loops)
     */
    private static final int bitIsInternal = 0x02;
    
 
    /**
     * flag is set if the expression is a lValue
     */
    private static final int isLValue = 0x04;
   
    /**
     * flag is set if the expression requires a function call
     * 
     * this flag is necessary for simple detection of function calls in expressions
     * for the ControlGraphGenerator
     * 
     */
    private static final int bitIsFunctionCall = 0x08;
    
    /**
     * flag is set in CheckUnreachableCode, if the statement is not reachable
     * 
     */
    private static final int statementIsUnreachable = 0x010;
    
    
    public ASTAttribute(TypeDefinition type) {
        m_type = type;
        m_flags = 0;
        m_entry = null;
        m_constant = null;
        m_selection    = null;
    }

    ASTAttribute(ConstantSelection slice) {
        m_type     = null;
        m_flags = 0;
        m_entry=null;
        m_constant = null;
        m_selection    = slice;
    }

    ASTAttribute(TypeDefinition type, boolean constant) {
        m_type = type;
        setIsConstant(constant);
        m_entry = null;
        m_selection    = null;
    }

    ASTAttribute(TypeDefinition type, boolean constant, VariableEntry variable ) {
        m_type = type;
        m_entry=variable;
        m_constant = null;
        m_selection    = null;

        if ( variable.getLoopControlVariable()) {
          setIsConstant(false);
        }
        else {
          setIsConstant(constant);
        }
    }
    public ASTAttribute(TypeDefinition type, SymbolTableEntry entry ) {
      m_type = type;
      m_entry=entry;
      m_constant = null;
      m_selection    = null;

      if (getVariable()== null || getVariable().getLoopControlVariable()) {
        setIsConstant(false);
      }
   
  }

    /**
     * indicate whether the element is a constant or an expression of constants
     * or a constant variable (INV)
     * 
     * @return
     */
    public boolean isConstant() {
      return getFlag(bitIsConstant);
    }
    
    public void setIsConstant(boolean newValue) {
      setFlag(bitIsConstant, newValue);
    }

    public boolean isLoopControlVariable() {
      //  return ( m_variable != null && m_variable.getLoopControlVariable());
      return m_entry != null && getVariable() != null && getVariable().getLoopControlVariable();
    }


    public boolean isWritable() { return !this.isConstant(); }

    public TypeDefinition getType() { return this.m_type; }
    
    public VariableEntry getVariable() {

      if (m_entry instanceof VariableEntry) {
        return (VariableEntry)m_entry;
      }
      return null;
    }
    
    public SymbolTableEntry getSymbolTableEntry() {
      return m_entry;
    }

    public void setSymbolTableEntry(SymbolTableEntry e) {
        m_entry = e;
    }
    
    public ConstantValue getConstant() { return this.m_constant; }

    public Void setConstant(ConstantValue val) {
        m_constant = val;
        //m_readonly = true;
        setIsConstant(true);
        return null;
    }

    public Void setConstantFixedValue(ConstantFixedValue val) {
        m_constant = val;
        //m_readonly = true;
        setIsConstant(true);
        return null;
    }

    public ConstantFixedValue getConstantFixedValue() { 
        if (m_constant instanceof ConstantFixedValue) {
            return (ConstantFixedValue) this.m_constant;
        } else {
            return null;
        }
    }

    public Void setConstantFloatValue(ConstantFloatValue val) {
        m_constant = val;
        setIsConstant(true);
        return null;
    }

    public ConstantFloatValue getConstantFloatValue() {
        if (m_constant instanceof ConstantFloatValue) {
            return (ConstantFloatValue) this.m_constant;
        } else {
            return null;
        }
    }

    public Void setConstantDurationValue(ConstantDurationValue val) {
        m_constant = val;
        setIsConstant(true);
        return null;
    }

    public ConstantDurationValue getConstantDurationValue() {
        if (m_constant instanceof ConstantDurationValue) {
            return (ConstantDurationValue) this.m_constant;
        } else {
            return null;
        }
    }


    public void setConstantSelection(ConstantSelection m_slice) {
      this.m_selection = m_slice;
    }
    
    public ConstantSelection getConstantSelection() {
        return this.m_selection;
    }

    public String toString() {
//        return "(" + this.m_type + " " + this.isReadOnly() + " " + this.m_variable + " " + this.m_constant + " " + this.m_selection + ")";
      return "(" + this.m_type + " " + this.isConstant() + " " + this.m_entry + " " + this.m_constant + " " + this.m_selection + ")";
    }

    public void setVariable(VariableEntry ve) {
      //this.m_variable = ve;
      m_entry = ve;
    }
    
    public void setType(TypeDefinition type) {
        //this.m_variable = ve;
        m_type = type;
      }

    
    public void setIsInternal(boolean newValue) {
      setFlag(bitIsInternal, newValue);
    }
    
    public boolean isInternal() {
      return getFlag(bitIsInternal);
    }

    /**
     * check if there is a function call 
     * 
     * @return
     */
    public boolean isFunctionCall() {
      return getFlag(bitIsFunctionCall);
    }
    
    /**
     * indicate that there is a function call 
     */
    public void setIsFunctionCall(boolean newValue) {
      setFlag(bitIsFunctionCall, newValue);
    }
    
  
    public boolean isLValue() {
        return (getFlag(isLValue));
    }
    
    public void setIsLValue(boolean set ) {
        setFlag(isLValue,set);
    }
    
    public boolean isUnreachableStatement() {
        return (getFlag(statementIsUnreachable));
    }
    
    public void setIsUnreachableStatement(boolean set ) {
        setFlag(statementIsUnreachable,set);
    }
    
    
    
    private boolean getFlag(int whichFlag) {
      return ((m_flags & whichFlag) == whichFlag);

    }

    private void setFlag(int whichFlag, boolean set) {
      if (set) {
        m_flags = m_flags | whichFlag;
      } else {
        m_flags = m_flags & ~whichFlag;
      }
    }

//    /**
//     * indicate whether the attribute is scalar or not.
//     *
//     * @return true if scalar, otherwise false
//     */
//    public boolean isScalarType() {
//        return this.m_type instanceof TypeBit ||
//               this.m_type instanceof TypeChar ||
//                this.m_type instanceof TypeDuration ||
//                this.m_type instanceof TypeClock ||
//                this.m_type instanceof TypeFixed ||
//                this.m_type instanceof TypeFloat ||
//                this.m_type instanceof TypeClock ||
//                this.m_type instanceof TypeVariableChar;
//    }


}
