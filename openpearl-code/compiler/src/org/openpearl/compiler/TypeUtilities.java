package org.openpearl.compiler;

import org.openpearl.compiler.OpenPearlParser.ExpressionContext;
import org.openpearl.compiler.SymbolTable.FormalParameter;
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
     * <li>invocation of procedures, if the result type fits 
     * </ul>
     * 
     * The rules are 
     * <ol>
     * <li>if lhsType is equal to rhsType: break
     * <li>if lhsType in not TypeReference
     *    <ol>
     *    <li>if rhsType fits and is not larger: break
     *    <li>if rhsType is TypeReference: perform dereference and continue
     *    <li>if rhsType is TypeProcedure: perform function call and continue
     *    </ol> 
     * <li>if lhsType is a TypeReference
     *    <ol>
     *    <li>if rhsType is a Ref with the same basetype: break<br>
     *       nothing to do - already treated by lhsType equals rhsType
     *    <li>if rhsType has the same baseType as lhs and rhsAttr indicates a lValue: break;
     *    <li>if rhsType is procedure returning a ref --> invoke procedure: continue checking in a loop
     *    <li>if rhsType is a Ref to procedure : dereference and continue
     *    </ol>
     * </ul>
     * apply the rules until no rule was applied in a loop 
     *
     * @param lhsType  the target type (lhs in assignment, formalParameter in function calls, input values ini/o statements
     * @param lhsVariable really required??
     * @param expression the expression on the rhs
     * @param m_ast the AST
     * @return
     */
    public static  boolean mayBeAssignedTo( TypeDefinition lhsType, SymbolTableEntry lhs,
            ExpressionContext expression, AST m_ast) {
        

        TypeDefinition rhsType = m_ast.lookupType(expression);
        String rhsOriginalType = rhsType.toString4IMC(true);

        ASTAttribute rhsAttr = m_ast.lookup(expression);
        VariableEntry rhsVariable = rhsAttr.getVariable();
        SymbolTableEntry rhsSymbol = rhsAttr.getSymbolTableEntry();


        // there are a lot of possible cases
        // let's check valid assignments start with easy cases
        // and continue with more complicated cases as long we have not found
        // a valid assignment
        boolean ruleApplied ;
        int loopCounter =0;
        do {
            ruleApplied = false;
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

            //-- easiest case; simple variable assignment
            if (simpleTypeMayBeAssignedTo(lhsType, rhsType)) {
                ruleApplied=true;
                break;
            }

            // Part 1: lhsType is NOT TypeReference

            if (!(lhsType instanceof TypeReference) ) {
                //-- lhs is not TypeReference; rhs is TypeReference with compatible baseType
                if (rhsType instanceof TypeReference &&          
                        TypeUtilities.simpleTypeMayBeAssignedTo(lhsType, ((TypeReference)rhsType).getBaseType())) {
                    // implicit dereference required
                    rhsAttr.setNeedImplicitDereferencing(true);
                    rhsAttr.setType(((TypeReference)rhsType).getBaseType());
                    ruleApplied=true;
                    break;
                }

                //-- lhs is not TypeReference; rhs may be PROC or REF PROC --> create function call
                if ( (rhsType instanceof TypeProcedure) ){
                    // function call on rhs
                    rhsAttr.setIsFunctionCall(true);
                    rhsType = ((TypeProcedure)rhsType).getResultType();
                    rhsAttr.setType(rhsType);
                    ruleApplied=true;
                }
                if ( rhsType instanceof TypeReference) {

                    rhsAttr.setNeedImplicitDereferencing(true);
                    rhsType = ((TypeReference)rhsType).getBaseType();
                    rhsAttr.setType(rhsType);
                 
                    ruleApplied=true;
                }

            } else {
                TypeDefinition lhsBaseType = ((TypeReference)lhsType).getBaseType();
                // Part 2: lhsType IS TypeReference
                // -- lhs is reference; rhs is variable of the same type and INV setting
                // -- lhs is reference; rhs PROC returning basetype of lhs with same INV-setting
                if (    (rhsSymbol != null) && rhsSymbol instanceof VariableEntry &&  
                        (lhsBaseType.equals(rhsType) &&
                        (lhsBaseType.hasAssignmentProtection() == rhsType.hasAssignmentProtection()))) { 
                    checkLifeCycle(lhs, rhsVariable);
                    ruleApplied=true;
                    break;
                }

                // -- lhs is reference to PROC; rhs is PROC of same type
                if (lhsBaseType instanceof TypeProcedure &&
                        rhsSymbol != null && rhsSymbol instanceof ProcedureEntry) {
                    TypeDefinition lhsBase = ((TypeReference)lhsType).getBaseType();      
                    if (lhsBase.equals(rhsAttr.getType())) {
                        ruleApplied=true;
                        break;   
                    }
                }

                // -- lhs is reference; rhs PROC returning basetype of lhs with same INV-setting
                if (  (rhsSymbol != null) && rhsSymbol instanceof ProcedureEntry &&  
                        resultType!= null &&
                        resultType.hasAssignmentProtection()==lhsType.hasAssignmentProtection()) {
                        
                    if (lhsBaseType.equals(resultType)) {
                        //  checkLifeCycle(lhsVariable, rhsVariable);
                        // not required, since PROC RETURNS(REF ...) may only return
                        // objects at module level
                        ruleApplied=true;
                       break;
                    } else {
                        rhsType = resultType;
                        rhsAttr.setType(rhsType);
                        ruleApplied=true;
                    }
                }
                // -- lhs is reference; rhs is a symbol; 
                //
                if ((rhsSymbol != null) &&
                        !(rhsSymbol instanceof ProcedureEntry) &&
                        rhsType.hasAssignmentProtection()==lhsType.hasAssignmentProtection()) {
                     if (lhsBaseType.equals(rhsAttr.getType())) {
                        // pointer assignment from SymbolTableEntry
                        checkLifeCycle(lhs, rhsVariable);
                        ruleApplied=true;
                        break;
                    }
                }

                // NIL assignment
                if (rhsType instanceof TypeReference && ((TypeReference)rhsType).getBaseType() == null ) {
                    // pointer assignment from pointer expression
                    ruleApplied=true;
                    break;   
                }

                // -- lhs is REF TASK; rhs is TASK ; special treatment since we have no symbol table entry here 
                if ( ((TypeReference)lhsType).getBaseType() instanceof TypeTask &&
                        rhsType instanceof TypeTask) {
                    ruleApplied=true;
                    break;
                }

                // -- lhs is reference; rhs is real expression resulting in lValue 
                if ((rhsAttr.isLValue())) {
                    if (!(lhsType.equals(rhsType))) {
                        // emit special error message
                        ErrorStack.add("type mismatch: "+lhsType.toString4IMC(false)+" := "+ rhsType.toString4IMC(false));
                    }
                    checkLifeCycle(lhs, rhsVariable);
                    ruleApplied=true;
                    break;
                }

                // -- lhs is reference; rhs is procedure returning REF
                if (rhsType instanceof TypeProcedure) {
                    if (!(lhsType.equals(resultType))) {
                        // emit special error message
                        ErrorStack.add("type mismatch: "+lhsType.toString4IMC(false)+" := expression of type "+ rhsType.toString4IMC(false));
                    }
                    rhsAttr.setIsFunctionCall(true);
                    rhsType = ((TypeProcedure)rhsType).getResultType();
                    rhsAttr.setType(rhsType);
                    ruleApplied=true;
                }

                // -- lhs is reference; rhs is real expression resulting in lValue
                if ((rhsAttr.isLValue())) {
                    TypeDefinition lhsBase = ((TypeReference)lhsType).getBaseType();      
                    if (lhsBase.equals(rhsAttr.getType())) {
                       // pointer assignment from SymbolTableEntry
                       ruleApplied=true;
                       break;

                    }
                }
            }
            if (loopCounter++>100) {
                ErrorStack.addInternal(expression, "TypeUtils@227", "emergency loop end");
                break;
            }
        } while (ruleApplied);
        // maybe some cases are missing

        if (ruleApplied == false) {
//            String prefix = "type mismatch:";
//            String postfix="";

//                prefix += " formal parameter:";
//                if (((FormalParameter)lhs).passIdentical) {
//                  postfix=" IDENT ";
//                }
//                
//            }
//            CommonErrorMessages.typeMismatch(lhsType, rhsType,"IDENT parameter:");
            if (rhsSymbol instanceof ProcedureEntry) {
                ErrorStack.add("type mismatch: "+lhsType.toString4IMC(true) + " := "+ ((ProcedureEntry)rhsSymbol).getType());//  rhsType.toString4IMC(false));
            } else {
                ErrorStack.add("type mismatch: "+lhsType.toString4IMC(true) + " := "+ rhsOriginalType);//  rhsType.toString4IMC(false));
            }
            
        } else {
            if (lhs instanceof FormalParameter) {
                FormalParameter fp = (FormalParameter)lhs;
                if (fp.passIdentical) {
                    if (fp.getType().hasAssignmentProtection()==false && rhsType.hasAssignmentProtection()==true) {
                        ErrorStack.add("type mismatch: formal parameter "+lhsType.toString4IMC(true) + " IDENT --- got "+ rhsOriginalType);
                    }
                               
                    
                }
            }
        }
        return !ruleApplied;
    }

    private static void checkLifeCycle(SymbolTableEntry lhsVariable, VariableEntry rhsVariable) {
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

    /**
     * check if the expression result may be assigned to the type targetType 
     * 
     * if targetType a of TypeReference, the baseType must match exactly
     * else, targetType may be larger
     * 
     * @param attr ASTAttribute of expression()
     * @param targetType the type of the lhs of an assignment, or the data element in an input statement, or formal parameter
     * @return null, if the type does not match, else the effective type
     */
    public static TypeDefinition performImplicitDereferenceAndFunctioncallForTargetType(ASTAttribute attr, TypeDefinition targetType) {

        TypeDefinition currentType = attr.m_type;
        boolean goon;
        boolean needExactType = false;

        if (targetType instanceof TypeReference) needExactType=true;

        do {
            goon = true;
            if (targetType instanceof TypeReference) {
                if (((TypeReference)targetType).getBaseType().equals(currentType)) {
                    break;
                }
            }
            //            if (currentType instanceof TypeReference)
            currentType = performImplicitDereferenceAndFunctioncall(attr);
            if (currentType == null) goon = false;
            if (currentType.equals(targetType)) goon = false;
            if (targetType instanceof TypeReference && ((TypeReference)targetType).getBaseType().equals(currentType)) goon = false;
            if (simpleTypeMayBeAssignedTo(targetType, currentType) ) goon = false;

        } while (goon);//while (t instanceof TypeProcedure);
        return currentType;

    }

    /**
     * check if we can obtain a TypeFixed (of any size) with dereferencing or procedure calls
     * 
     * @param attr th ASTAttribute of an expression 
     * @return TypeFixed if it was possible<br>
     *         null, else 
     */
    public static TypeDefinition performImplicitDereferenceAndFunctioncallForTargetTypeFixed(ASTAttribute attr) {
        TypeDefinition t = null;
        do {
            t = performImplicitDereferenceAndFunctioncall(attr);

        } while (t instanceof TypeProcedure);
        if (t instanceof TypeFixed) {
            return t;
        }
        return null;
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
            attr.setType(type);
        }
        if (type instanceof TypeProcedure) {
            type = ((TypeProcedure)type).getResultType();
            attr.setIsFunctionCall(true);
            if (type instanceof TypeReference) {
                attr.setIsLValue(true);
            }
            attr.setType(type);
        }
        //        if (type instanceof TypeReference) {
        //            type = ((TypeReference)type).getBaseType();
        //            attr.setNeedImplicitDereferencing(true);
        //            attr.setType(type);
        //        }
        return type;
    }

}
