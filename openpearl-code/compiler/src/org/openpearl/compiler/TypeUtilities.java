package org.openpearl.compiler;

import org.openpearl.compiler.OpenPearlParser.ExpressionContext;
import org.openpearl.compiler.SymbolTable.ProcedureEntry;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
import org.openpearl.compiler.SymbolTable.VariableEntry;

/**
 * Class with utilities for type checks, which are used at more than one location
 *
 */
public class TypeUtilities {
    
    /**
     * Check whether there exist a rule to assign rhs to lhs with considerations of 
     * <ul>
     * <li>implicit dereference of REF elements
     * <li>invokation of procedures, if the result type fits 
     * </ul>
     * 
     * The rules are 
     * <ul>
     * 
     * </ul>
     *
     * @param lhsType
     * @param lhsVariable
     * @param expression
     * @param m_ast
     * @return
     */
    public static  boolean mayBeAssignedTo(TypeDefinition lhsType, VariableEntry lhsVariable,
            ExpressionContext expression, AST m_ast) {

        TypeDefinition rhsType = m_ast.lookupType(expression);
        String rhsOriginalType = rhsType.toString4IMC(true);


        ASTAttribute rhsAttr = m_ast.lookup(expression);
        VariableEntry rhsVariable = rhsAttr.getVariable();
        SymbolTableEntry rhsSymbol = rhsAttr.getSymbolTableEntry();
        TypeDefinition resultType = null;

        // check if we may have a function call (rhsType REF PROC RETURNS(...))
        if (rhsType instanceof TypeReference && (((TypeReference)rhsType).getBaseType() instanceof TypeProcedure)) {
            TypeProcedure tp= (TypeProcedure)(((TypeReference)rhsType).getBaseType());
            resultType = tp.getResultType();

        }
        if (rhsType instanceof TypeProcedure) {
            resultType = ((TypeProcedure)rhsType).getResultType();
            rhsAttr.setIsFunctionCall(true);
        }
        // resultType is != null, if it may be a procedure call

        // there are a lot of possible cases
        // let's check valid assignments start with easy cases
        // and continue with more complicated cases as long we have not found
        // a valid assignment
        boolean searchValidAssignment = true;

        //-- easiest case; simple variable assignment
        if (searchValidAssignment && simpleTypeMayBeAssignedTo(lhsType, rhsType)) {
            searchValidAssignment=false;
        }

        // Part 1: lhsType is NOT TypeReference
        //-- lhs is not TypeReference; rhs is TypeReference with compatible baseType
        if (searchValidAssignment && !(lhsType instanceof TypeReference) && (rhsType instanceof TypeReference) &&
                TypeUtilities.simpleTypeMayBeAssignedTo(lhsType, ((TypeReference)rhsType).getBaseType())) {
            // implicit dereference required
            rhsAttr.setNeedImplicitDereferencing(true);
            searchValidAssignment=false;
        }

        //-- lhs is not TypeReference; rhs may be PROC or REF PROC --> create function call
        if (searchValidAssignment && !(lhsType instanceof TypeReference) && (rhsType instanceof TypeProcedure) &&
                (TypeUtilities.simpleTypeMayBeAssignedTo(lhsType, resultType) ||
                        lhsType.equals(resultType)) ){
            // function call on rhs
            rhsAttr.setIsFunctionCall(true);
            searchValidAssignment=false;
        }
        if (searchValidAssignment && !(lhsType instanceof TypeReference) && (rhsType instanceof TypeProcedure) &&
                (resultType instanceof TypeReference && lhsType.equals(((TypeReference)resultType).getBaseType()))) {

            rhsAttr.setNeedImplicitDereferencing(true);
            rhsAttr.setIsFunctionCall(true);
            searchValidAssignment=false;
        }

        if (searchValidAssignment && !(lhsType instanceof TypeReference) && (rhsType instanceof TypeReference) &&
                ((TypeReference)rhsType).getBaseType() instanceof TypeProcedure && 
                TypeUtilities.simpleTypeMayBeAssignedTo(lhsType, resultType)) {
            // deref and function call on rhs
            rhsAttr.setIsFunctionCall(true);
            rhsAttr.setNeedImplicitDereferencing(true);
            searchValidAssignment=false;
        }

        // Part 2: lhsType IS TypeReference
        // -- lhs is reference; rhs is variable of the same type and INV setting
        // -- lhs is reference; rhs PROC returning basetype of lhs with same INV-setting
        if (searchValidAssignment && lhsType instanceof TypeReference && 
                (rhsSymbol != null) && rhsSymbol instanceof VariableEntry &&  
                ((TypeReference)lhsType).getBaseType().equals(rhsType) &&
                ((TypeReference)lhsType).getBaseType().hasAssignmentProtection() == rhsType.hasAssignmentProtection()) { 
            searchValidAssignment=false;   
            checkLifeCycle(lhsVariable, rhsVariable);
        }

        // -- lhs is reference to PROC; rhs is PROC of same type
        if (searchValidAssignment && 
                lhsType instanceof TypeReference && ((TypeReference)lhsType).getBaseType() instanceof TypeProcedure &&
                rhsSymbol != null && rhsSymbol instanceof ProcedureEntry) {
            TypeDefinition lhsBase = ((TypeReference)lhsType).getBaseType();      
            if (lhsBase.equals(rhsAttr.getType())) {
                searchValidAssignment=false;   
            }
        }

        // -- lhs is reference; rhs PROC returning basetype of lhs with same INV-setting
        if (searchValidAssignment && lhsType instanceof TypeReference && 
                (rhsSymbol != null) && rhsSymbol instanceof ProcedureEntry &&  
                resultType!= null &&
                resultType.hasAssignmentProtection()==lhsType.hasAssignmentProtection()) {
            TypeDefinition lhsBase = ((TypeReference)lhsType).getBaseType();      
            if (lhsBase.equals(resultType)) {
                searchValidAssignment=false;  
                //  checkLifeCycle(lhsVariable, rhsVariable);
                // not required, since PROC RETURNS(REF ...) may only return
                // objects at module level
            }
        }
        // -- lhs is reference; rhs is a symbol; 
        //
        if (searchValidAssignment && lhsType instanceof TypeReference && (rhsSymbol != null) &&
                !(rhsSymbol instanceof ProcedureEntry) &&
                rhsType.hasAssignmentProtection()==lhsType.hasAssignmentProtection()) {
            TypeDefinition lhsBase = ((TypeReference)lhsType).getBaseType();      
            if (lhsBase.equals(rhsAttr.getType())) {
                // pointer assignment from SymbolTableEntry
                searchValidAssignment=false;   
                checkLifeCycle(lhsVariable, rhsVariable);
            }
        }

        // NIL assignment
        if (searchValidAssignment && lhsType instanceof TypeReference && 
                rhsType instanceof TypeReference && ((TypeReference)rhsType).getBaseType() == null ) {
            // pointer assignment from pointer expression
            searchValidAssignment=false;   
        }

        // -- lhs is REF TASK; rhs is TASK ; special treatment since we have no symbol table entry here 
        if (searchValidAssignment && lhsType instanceof TypeReference && ((TypeReference)lhsType).getBaseType() instanceof TypeTask &&
                rhsType instanceof TypeTask) {
            searchValidAssignment=false;
        }

        // -- lhs is reference; rhs is real expression resulting in lValue 
        if (searchValidAssignment && lhsType instanceof TypeReference && (rhsAttr.isLValue())) {
            if (!(lhsType.equals(rhsType))) {
                // emit special error message
                ErrorStack.add("type mismatch: "+lhsType.toString4IMC(false)+" := "+ rhsType.toString4IMC(false));
            }
            checkLifeCycle(lhsVariable, rhsVariable);
            searchValidAssignment=false;
        }

        // -- lhs is reference; rhs is procedure returning REF
        if (searchValidAssignment && lhsType instanceof TypeReference && rhsType instanceof TypeProcedure) {
            if (!(lhsType.equals(resultType))) {
                // emit special error message
                ErrorStack.add("type mismatch: "+lhsType.toString4IMC(false)+" := expression of type "+ rhsType.toString4IMC(false));
            }
            rhsAttr.setIsFunctionCall(true);
            searchValidAssignment=false;
        }

        // -- lhs is reference; rhs is real expression resulting in lValue
        if (searchValidAssignment && lhsType instanceof TypeReference && (rhsAttr.isLValue())) {
            TypeDefinition lhsBase = ((TypeReference)lhsType).getBaseType();      
            if (lhsBase.equals(rhsAttr.getType())) {
                // pointer assignment from SymbolTableEntry
                searchValidAssignment=false;  

            }
        }


        // maybe some cases are missing

        if (searchValidAssignment == true) {
            if (rhsSymbol instanceof ProcedureEntry) {
                ErrorStack.add("type mismatch: "+lhsType.toString4IMC(false)+" := "+ ((ProcedureEntry)rhsSymbol).getType());//  rhsType.toString4IMC(false));
            } else {
                ErrorStack.add("type mismatch: "+lhsType.toString4IMC(false)+" := "+ rhsOriginalType);//  rhsType.toString4IMC(false));
            }
        }
        return !searchValidAssignment;
    }

    private static void checkLifeCycle(VariableEntry lhsVariable, VariableEntry rhsVariable) {
        if (rhsVariable != null) {
            if (lhsVariable.getLevel() < rhsVariable.getLevel()) {
                ErrorStack.add("life cycle of '" + rhsVariable.getName() + "' is shorter than '"
                        + lhsVariable.getName() + "'");
            }
        } else {
            // rhs is NIL, TASK or PROC
        }
    }


    public static boolean isSimpleType(TypeDefinition type) {
        boolean result = false;

        if (type instanceof TypeFixed || type instanceof TypeFloat || type instanceof TypeBit
                || type instanceof TypeChar || type instanceof TypeDuration
                || type instanceof TypeClock) {
            result = true;
        }
        return result;
    }

    public static boolean simpleTypeMayBeAssignedTo(TypeDefinition lhs, TypeDefinition rhs) {
        boolean result = false;

        if (lhs.equals(rhs)) { 
            result=true;
        } else if (lhs instanceof TypeFixed) {
            int lhsPrecision = ((TypeFixed)lhs).getPrecision(); 
            if (rhs instanceof TypeFixed) {
                int rhsPrecision = ((TypeFixed)rhs).getPrecision();
                if (rhsPrecision<= lhsPrecision) {
                    result=true;
                }
            }
        } else if (lhs instanceof TypeFloat) {
            int lhsPrecision = ((TypeFloat)lhs).getPrecision(); 
            if (rhs instanceof TypeFixed) {
                int rhsPrecision = ((TypeFixed)rhs).getPrecision();
                if (rhsPrecision<= lhsPrecision) {
                    result=true;
                }
            }
            if (rhs instanceof TypeFloat) {
                int rhsPrecision = ((TypeFloat)rhs).getPrecision();
                if (rhsPrecision<= lhsPrecision) {
                    result=true;
                }
            }
        } else if (lhs instanceof TypeChar) {
            int lhsPrecision = ((TypeChar)lhs).getPrecision(); 
            if (rhs instanceof TypeChar) {
                int rhsPrecision = ((TypeChar)rhs).getPrecision();
                if (rhsPrecision<= lhsPrecision) {
                    result=true;
                }
            }
            if (rhs instanceof TypeVariableChar) {
                // the check for correct size must be done at runtime
                result=true;
            }

        } else if (lhs instanceof TypeBit) {
            int lhsPrecision = ((TypeBit)lhs).getPrecision(); 
            if (rhs instanceof TypeBit) {
                int rhsPrecision = ((TypeBit)rhs).getPrecision();
                if (rhsPrecision<= lhsPrecision) {
                    result=true;
                }
            }
        }    
        return result;
    }

    public static TypeDefinition performImplicitDereferenceAndFunctioncall(ASTAttribute attr) {
        TypeDefinition type = attr.getType();
        if (type instanceof TypeReference) {
            type = ((TypeReference)type).getBaseType();
            if (type instanceof TypeProcedure || type instanceof TypeArray) {
                attr.setArrayOrProcNeedsImplicitDereferencing(true);
            } else {
               attr.setNeedImplicitDereferencing(true);
            }
        }
        if (type instanceof TypeProcedure) {
            type = ((TypeProcedure)type).getResultType();
            attr.setIsFunctionCall(true);
            if (type instanceof TypeReference) {
                attr.setIsLValue(true);
            }
        }
        if (type instanceof TypeReference) {
            type = ((TypeReference)type).getBaseType();
            attr.setNeedImplicitDereferencing(true);
        }
        return type;
    }

}
