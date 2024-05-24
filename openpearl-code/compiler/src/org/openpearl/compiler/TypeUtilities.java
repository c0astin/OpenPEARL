package org.openpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
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
     * <li>if the destination is a formal parameter with attribute IDENT, we need
     *     a LValue of the source side with the exact same type, thus  procedure results 
     *     are not allowed  
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
     * <li>if lhsType is TypeRefChar , rhs must be a variable entry of type TypeChar</li>
     * </ul>
     * apply the rules until no rule was applied in a loop 
     *
     * @param lhsType  the target type (lhs in assignment, formalParameter in function calls, input values ini/o statements
     * @param expression the expression on the rhs
     * @param m_ast the AST
     * @param isInAssignment is true we are in an assignment, 
     *                          else we have an actual parameter of a procedure call
     * @return true, if ok, else false
     */
    public static  boolean mayBeAssignedTo( TypeDefinition lhsType, SymbolTableEntry lhs,
            ExpressionContext expression, AST m_ast, boolean isInAssignment) {

        ASTAttribute rhsAttr = m_ast.lookup(expression);
        TypeDefinition rhsType = m_ast.lookupType(expression);

        String rhsOriginalType = rhsType.toString4IMC(true);
        if (rhsType instanceof UserDefinedSimpleType) {
            rhsOriginalType = rhsType.toErrorString();
        }

        if (rhsType instanceof UserDefinedTypeStructure) {
            rhsOriginalType = rhsType.toErrorString();
            // rhsType = ((UserDefinedTypeStructure)rhsType).getStructuredType();
        }

        String lhsOriginalType = lhsType.toString4IMC(true);
        if (lhsType instanceof UserDefinedSimpleType) {
            lhsOriginalType = lhsType.toErrorString();
        }

        if (lhsType instanceof UserDefinedTypeStructure) {
            lhsOriginalType = lhsType.toErrorString();
            //lhsType = ((UserDefinedTypeStructure)lhsType).getStructuredType();
        }


        VariableEntry rhsVariable = rhsAttr.getVariable();
        SymbolTableEntry rhsSymbol = rhsAttr.getSymbolTableEntry();


        // there are a lot of possible cases
        // let's check valid assignments start with easy cases


        // RefChar() assignment this differs from
        // rc = charVariable; ! setup the work zone
        // CONT rc = charExpression ! fill the work zone
        if (lhsType instanceof TypeReference && (((TypeReference)lhsType).getBaseType() instanceof TypeRefChar)) {

            // check for implicit dereference on rhs REF CHAR(x)
            if (rhsVariable != null && rhsType instanceof TypeReference) {
                TypeDefinition td = ((TypeReference)(rhsType)).getBaseType();
                if (!(td instanceof TypeChar  || td instanceof TypeRefChar )) {
                    if (isInAssignment) {
                        CommonErrorMessages.typeMismatchInAssignment(lhsOriginalType, rhsOriginalType, rhsAttr);
                    } else {
                        CommonErrorMessages.typeMismatchProcedureParameterIdent(lhsOriginalType, rhsOriginalType, rhsAttr,null);
                    }
                    return false;
                }
                rhsType = ((TypeReference)(rhsType)).getBaseType();
            }
            if (rhsVariable != null && (rhsType instanceof TypeChar || rhsType instanceof TypeRefChar)) {
                // ok: ref char assignment of work zone

                if (rhsType.hasAssignmentProtection()) {
                    TypeRefChar trc = (TypeRefChar)(((TypeReference)(lhsType)).getBaseType());
                    if(!trc.hasAssignmentProtection()) {
                        if (isInAssignment) {
                            CommonErrorMessages.typeMismatchInAssignment(lhsOriginalType, rhsOriginalType, rhsAttr);
                        } else {
                            CommonErrorMessages.typeMismatchProcedureParameter(lhsOriginalType, rhsOriginalType, rhsAttr);
                        }
                        return false;
                    }
                }

                checkLifeCycle(lhs, rhsVariable);
                return true;
            }
            if (rhsAttr.getType() instanceof TypeChar && rhsAttr.getVariable() != null) {
                return true;
            }
            if (((TypeReference)lhsType).getBaseType().hasAssignmentProtection() && rhsAttr.getConstant()!= null) {
                return true;
            }
        }

        // same type
        if (lhsType.equals(rhsType)) {
                      
            //            if (lhsType instanceof TypeReference) {
            // no lifetime check reasonable, this would cause problems 
            // e.q. sorting a linked list, where local copies of references are required

            //                boolean needCheckLifecycle = true; 
            //                TypeDefinition baseType = ((TypeReference)lhsType).getBaseType();
            //                if (baseType instanceof TypeProcedure || baseType instanceof TypeTask) {
            //                    // no need to check the lifecycle, since PROC, TASK are 
            //                    // always defined on module level
            //                    needCheckLifecycle = false;
            //                } 
            //
            //
            //                if (needCheckLifecycle) {
            //                    checkLifeCycle(lhs, rhsVariable);
            //                }
            //                return true;
            //            }
            return true;
        }


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
                rhsAttr.setIsFunctionCall(true);
                rhsAttr.setType(resultType);
            }
            //            if (rhsType instanceof TypeProcedure &&
            //                    ((TypeProcedure)rhsType).getResultType() != null  ) {
            //                resultType = ((TypeProcedure)rhsType).getResultType();
            //                rhsAttr.setIsFunctionCall(true);
            //                rhsAttr.setType(resultType);
            //            }

            //-- easiest case; simple variable assignment
            if (resultType != null && simpleTypeInclVarCharAndRefCharMayBeAssignedTo(lhsType, resultType)) {
                ruleApplied=true;
                break;
            } else if (simpleTypeInclVarCharAndRefCharMayBeAssignedTo(lhsType, rhsType)) {
                ruleApplied=true;
                break;
            }


            if (!(lhsType instanceof TypeReference) ) {
                // Part 1: lhsType is NOT TypeReference

                //-- lhs is not TypeReference; rhs is TypeReference with compatible baseType
                if (rhsType instanceof TypeReference &&          
                        TypeUtilities.simpleTypeInclVarCharAndRefCharMayBeAssignedTo(lhsType, ((TypeReference)rhsType).getBaseType())) {
                    // implicit dereference required
                    if (! (((TypeReference)rhsType).getBaseType() instanceof TypeRefChar)) {
                        rhsAttr.setType(((TypeReference)rhsType).getBaseType());
                    }
                    ruleApplied=true;
                    break;
                }

                //-- lhs is not TypeReference; rhs may be PROC or REF PROC --> create function call
                if ( (rhsType instanceof TypeProcedure) &&
                        ((TypeProcedure)rhsType).getResultType() != null  ){
                    // function call on rhs
                    rhsType = ((TypeProcedure)rhsType).getResultType();
                    rhsAttr.setType(rhsType);
                    ruleApplied=true;
                    rhsAttr.setIsFunctionCall(true);
                } else if ( rhsType instanceof TypeReference && 
                        simpleTypeInclVarCharAndRefCharMayBeAssignedTo(lhsType, ((TypeReference)rhsType).getBaseType())) {
                    rhsType = ((TypeReference)rhsType).getBaseType();
                    if (rhsType instanceof TypeSameStructure) {
                        rhsType = ((TypeSameStructure)rhsType).getContainerStructure().getStructuredType();
                    }  else {
                        ErrorStack.addInternal(expression, "TypeUtlities@165", "missing alternative");
                    }
                    rhsAttr.setType(rhsType);

                    ruleApplied=true;
                }

            } else {
                // Part 2: lhsType IS TypeReference
                TypeDefinition lhsBaseType = ((TypeReference)lhsType).getBaseType();

                // -- lhs is reference; rhs is TypeReference with compatible base type
                if (rhsType instanceof TypeReference) {
                    TypeDefinition rhsBaseType = ((TypeReference)rhsType).getBaseType();
                    if (lhsBaseType.equals(rhsBaseType)) {
                        System.out.println("checklifecycle is missing");
                        ruleApplied = true;
                        break;
                    }
                }

                // -- lhs is reference; rhs is a symbol; 
                // -- lhs is reference; rhs is variable of the same type and INV setting
                // -- lhs is reference; rhs PROC returning basetype of lhs with same INV-setting
                // REF points to INV type & variable on rhs is INV  & ok
                //       0                &            0            & yes
                //       0                &            1            & no
                //       1                &            0            & yes
                //       1                &            1            & yes

                if (    (rhsSymbol != null) && rhsSymbol instanceof VariableEntry &&  
                        (lhsBaseType.equals(rhsType) )) {
                    if ( (lhsBaseType.hasAssignmentProtection() == rhsType.hasAssignmentProtection()) ||
                            (lhsBaseType.hasAssignmentProtection() &&  rhsType.hasAssignmentProtection()==false)) { 
                        checkLifeCycle(lhs, rhsVariable);
                        ruleApplied=true;
                        break;
                    } else {
                        if (!isInAssignment) {
                            CommonErrorMessages.typeMismatchProcedureParameter(lhsType.toErrorString(), rhsType.toErrorString(), rhsAttr);
                        } else {
                            CommonErrorMessages.typeMismatchInAssignment(lhsType.toErrorString(), rhsType.toErrorString(), rhsAttr);
                        }
                        return  false;
                    }
                }

                // -- lhs is reference to INV ; rhs is constant of base type of lhs
                if (    rhsAttr.getConstant() != null && 
                        (lhsBaseType.equals(rhsType) &&
                                (lhsBaseType.hasAssignmentProtection()))) { 
                    // checkLifeCycle(lhs, ...); not required since constants are defined in module level 
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
                if (  (rhsSymbol != null) && rhsSymbol instanceof ProcedureEntry) {
                    resultType = ((ProcedureEntry)rhsSymbol).getResultType();

                    if (resultType != null &&
                            resultType.hasAssignmentProtection()==lhsType.hasAssignmentProtection()) {

                        if (lhsBaseType.equals(resultType)) {
                            //  checkLifeCycle(lhsVariable, rhsVariable);
                            // not required, since PROC RETURNS(REF ...) may only return
                            // objects at module level
                            rhsAttr.setIsFunctionCall(true);
                            ruleApplied=true;
                            break;
                        }
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

                if ( ((TypeReference)lhsType).getBaseType().equals(rhsType)) {
                    ruleApplied=true;
                    break;
                }

                // -- lhs is reference; rhs is real expression resulting in lValue 
                if ((rhsAttr.isLValue())) {
                    Boolean ok = false;
                    if (!ok && (lhsType.equals(rhsType))) {
                        ok = true;
                    }
                    // test rhs is UserDefineTypeStructure

                    if (!ok && rhsType instanceof UserDefinedTypeStructure) {
                        String lhsTypeString = ((TypeSameStructure)lhsBaseType).getContainerStructure().getStructuredType().getName();
                        String rhsTypeAsString = ((UserDefinedTypeStructure)rhsType).getStructuredType().getName();
                        if (lhsTypeString.equals(rhsTypeAsString)) {
                            ok = true;
                        }
                    }



                    if (!ok) {
                        // emit special error message
                        if (!isInAssignment) {
                            CommonErrorMessages.typeMismatchProcedureParameter(lhsType.toErrorString(), rhsType.toErrorString(), rhsAttr);
                        } else {
                            CommonErrorMessages.typeMismatchInAssignment(lhsType.toErrorString(), rhsType.toErrorString(), rhsAttr);
                        }
                    }
                    checkLifeCycle(lhs, rhsVariable);
                    ruleApplied=true;
                    break;
                }

                // -- lhs is reference; rhs is procedure returning REF
                if (rhsType instanceof TypeProcedure) {
                    if (!(lhsType.equals(resultType))) {
                        // emit special error message
                        if (!isInAssignment) {
                            CommonErrorMessages.typeMismatchProcedureParameter(lhsType.toErrorString(), rhsType.toErrorString(), rhsAttr);
                        } else {
                            CommonErrorMessages.typeMismatchInAssignment(lhsType.toErrorString(), rhsType.toErrorString(), rhsAttr);
                        }
                    }

                    rhsType = ((TypeProcedure)rhsType).getResultType();
                    rhsAttr.setType(rhsType);
                    rhsAttr.setIsFunctionCall(true);
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

            //if (rhsSymbol instanceof ProcedureEntry) {
            if (rhsType instanceof TypeProcedure) {
                if (!isInAssignment) {
                    CommonErrorMessages.typeMismatchProcedureParameter(lhsType.toErrorString(), ((ProcedureEntry)rhsSymbol).getType().toErrorString(), rhsAttr);
                } else {
                    CommonErrorMessages.typeMismatchInAssignment(lhsType.toErrorString(), ((ProcedureEntry)rhsSymbol).getType().toErrorString(), rhsAttr);
                }
                return false;
            } else if (lhsType instanceof TypeReference && ((TypeReference)lhsType).getBaseType() instanceof TypeRefChar &&
                    rhsType instanceof TypeChar && rhsAttr.getConstant() != null) {
                if (!isInAssignment) {
                    CommonErrorMessages.typeMismatchProcedureParameter(lhsType.toErrorString(), rhsOriginalType, rhsAttr);
                } else {
                    CommonErrorMessages.typeMismatchInAssignment(lhsType.toErrorString(), rhsOriginalType, rhsAttr);
                }
                return false;
            } else {
                if (!isInAssignment) {
                    CommonErrorMessages.typeMismatchProcedureParameter(lhsType.toErrorString(), rhsType.toErrorString(), rhsAttr);
                } else {
                    CommonErrorMessages.typeMismatchInAssignment(lhsType.toErrorString(), rhsType.toErrorString(), rhsAttr);
                }
                return false;
            }

        } else {
            if (!isInAssignment) {
                FormalParameter fp = (FormalParameter)lhs;
                if (fp.passIdentical()) {
                    if (fp.getType().hasAssignmentProtection()==false && rhsType.hasAssignmentProtection()==true) {
                        ErrorStack.add("type mismatch: formal parameter "+lhsOriginalType + " IDENT --- got "+ rhsOriginalType);
                        return false;
                    }
                }

            }
        }
        return true;
    }

    private static void checkLifeCycle(SymbolTableEntry lhsVariable, VariableEntry rhsVariable) {
        if (rhsVariable != null) {
            // lhsVariable may be formalParameter, variableEntry,...
            // attention FormalParameter is derived from VariableEntry --> must be checked first!
            if (lhsVariable instanceof FormalParameter) {
                // always ok, since we have no local procedures  
            } else if (lhsVariable instanceof VariableEntry) {
                if (lhsVariable.getLevel() < rhsVariable.getLevel()) {
                    ErrorStack.add("life cycle of '" + rhsVariable.getName() + "' is shorter than '"
                            + lhsVariable.getName() + "'");
                }
            } else {
                ErrorStack.addInternal("untreated alternative @ TypeUtilities:375");
            }
        } else {
            // rhs is NIL, TASK or PROC
        }
    }

    public static boolean isSimpleType(TypeDefinition type) {
        if (type instanceof TypeFixed || type instanceof TypeFloat || type instanceof TypeBit
                || type instanceof TypeChar || type instanceof TypeDuration
                || type instanceof TypeClock ) {
            return true;
        }
        return false;        
    }

    public static boolean isSimpleTypeInclVarCharAndRefChar(TypeDefinition type) {
        boolean result = false;

        if (isSimpleType(type) || type instanceof TypeVariableChar || type instanceof TypeRefChar ||
                type instanceof UserDefinedSimpleType) {
            result = true;
        }
        return result;
    }

    public static boolean isSimpleInclVarCharAndRefCharOrStructureType(TypeDefinition type) {
        boolean result = false;

        if (isSimpleTypeInclVarCharAndRefChar(type) || type instanceof TypeStructure) {
            result = true;
        }
        return result;
    }


    public static boolean simpleTypeInclVarCharAndRefCharMayBeAssignedTo(TypeDefinition lhs, TypeDefinition rhs) {
        boolean result = false;
        TypeDefinition effectiveLhs = lhs;
        TypeDefinition effectiveRhs = rhs;

        if (lhs instanceof UserDefinedSimpleType) {
            effectiveLhs = ((UserDefinedSimpleType)lhs).getSimpleType();
        }
        if (rhs instanceof UserDefinedSimpleType) {
            effectiveRhs = ((UserDefinedSimpleType)rhs).getSimpleType();
        }
        if (lhs instanceof UserDefinedTypeStructure) {
            effectiveLhs = ((UserDefinedTypeStructure)lhs).getStructuredType();
        }
        if (rhs instanceof UserDefinedTypeStructure) {
            effectiveRhs = ((UserDefinedTypeStructure)rhs).getStructuredType();
        }
        if (effectiveLhs.equals(effectiveRhs)) { 
            result=true;
        } else if (effectiveLhs instanceof TypeFixed) {
            int lhsPrecision = ((TypeFixed)effectiveLhs).getPrecision(); 
            if (effectiveRhs instanceof TypeFixed) {
                int rhsPrecision = ((TypeFixed)effectiveRhs).getPrecision();
                if (rhsPrecision<= lhsPrecision) {
                    result=true;
                }
            }
        } else if (effectiveLhs instanceof TypeFloat) {
            int lhsPrecision = ((TypeFloat)effectiveLhs).getPrecision(); 
            if (effectiveRhs instanceof TypeFixed) {
                int rhsPrecision = ((TypeFixed)effectiveRhs).getPrecision();
                if (rhsPrecision<= lhsPrecision) {
                    result=true;
                }
            }
            if (effectiveRhs instanceof TypeFloat) {
                int rhsPrecision = ((TypeFloat)effectiveRhs).getPrecision();
                if (rhsPrecision<= lhsPrecision) {
                    result=true;
                }
            }
        } else if (effectiveLhs instanceof TypeChar) {
            int lhsPrecision = ((TypeChar)effectiveLhs).getPrecision(); 
            if (effectiveRhs instanceof TypeChar) {
                int rhsPrecision = ((TypeChar)effectiveRhs).getPrecision();
                if (rhsPrecision<= lhsPrecision) {
                    result=true;
                }
            } else if (rhs instanceof TypeVariableChar) {
                // the check for correct size must be done at runtime
                result=true;
            } else if (rhs instanceof TypeRefChar) {
                // the check for correct size must be done at runtime
                result = true;
            }

        } else if (effectiveLhs instanceof TypeVariableChar) {
            if (effectiveRhs instanceof TypeChar) {
                // the check for correct size must be done at runtime
                result=true;
            }
        } else if (effectiveLhs instanceof TypeRefChar) {
            if (effectiveRhs instanceof TypeChar ||
                    effectiveRhs instanceof TypeVariableChar) {
                result=true;
            }
        } else if (effectiveLhs instanceof TypeBit) {
            int lhsPrecision = ((TypeBit)effectiveLhs).getPrecision(); 
            if (effectiveRhs instanceof TypeBit) {
                int rhsPrecision = ((TypeBit)effectiveRhs).getPrecision();
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
     * if targetType is of TypeReference, the baseType must match exactly
     * else, targetType may be larger
     * 
     * @param attr ASTAttribute of expression()
     * @param targetType the type of the lhs of an assignment, or the data element in an input statement, or formal parameter
     * @return null, if the type does not match, else the effective type
     */
    public static TypeDefinition performImplicitDereferenceAndFunctioncallForTargetType(ASTAttribute attr, TypeDefinition targetType) {

        TypeDefinition currentType = attr.m_type;
        boolean goon;
        int loopCounter = 0;

        do {
            goon = true;
            if (targetType instanceof TypeReference) {
                if (((TypeReference)targetType).getBaseType().equals(currentType)) {
                    break;
                }
            }
            if (currentType.equals(targetType)) goon = false;
            if (goon) 
                currentType = performImplicitDereferenceAndFunctioncall(attr);
            if (currentType == null) goon = false;

            if (targetType instanceof TypeReference && ((TypeReference)targetType).getBaseType().equals(currentType)) goon = false;
            if (simpleTypeInclVarCharAndRefCharMayBeAssignedTo(targetType, currentType) ) goon = false;

        } while (goon && loopCounter ++ < 100);//while (t instanceof TypeProcedure);
        return currentType;

    }

    //    /**
    //     * check if we can obtain a TypeFixed (of any size) with dereferencing or procedure calls
    //     * 
    //     * @param attr the ASTAttribute of an expression 
    //     * @return TypeFixed if it was possible<br>
    //     *         null, else 
    //     */
    //    public static TypeDefinition performImplicitDereferenceAndFunctioncallForTargetTypeFixed(ASTAttribute attr) {
    //        TypeDefinition t = null;
    //        do {
    //            t = performImplicitDereferenceAndFunctioncall(attr);
    //
    //        } while (t instanceof TypeProcedure);
    //        if (t instanceof TypeFixed) {
    //            return t;
    //        }
    //        return null;
    //    }


    public static TypeDefinition performImplicitDereferenceAndFunctioncall(ASTAttribute attr) {
        TypeDefinition type = attr.getType();
        boolean actionDone = true;
        while (actionDone) {
            actionDone=false;
            if (type instanceof TypeReference) {
                type = ((TypeReference)type).getBaseType();
                attr.setType(type);
                actionDone=true;
            }
            if (type instanceof TypeProcedure) {
                if (((TypeProcedure)type).getFormalParameters() == null ||  ((TypeProcedure)type).getFormalParameters().size()== 0) {
                    // perform implicit function call only if no parameters are required
                    type = ((TypeProcedure)type).getResultType();

                    if (type instanceof TypeReference) {
                        if (!((TypeReference)type).getBaseType().hasAssignmentProtection()) {
                            attr.setIsLValue(true);
                        }
                    }
                    attr.setType(type);
                    attr.setIsFunctionCall(true);
                    actionDone=true;
                }
            }
        }

        return type;
    }

    /**
     * check if given context element may be used as expectedType
     * 
     * assert error message if the element may not be used as the expected type
     * 
     * @param ctx
     * @param targetType
     * @param m_ast
     * @param prefix if true, the ctx must be exactly the target type, else the precision may not exceed the target presicion
     * 
     */
    public static void deliversTypeOrEmitErrorMessage(ParserRuleContext ctx, TypeDefinition targetType, AST m_ast, String prefix) {
        ASTAttribute attr = m_ast.lookup(ctx);
        boolean equal=false;
        boolean exactMatch=false;
        TypeDefinition baseTypeOfTarget=null;
        if (targetType instanceof TypeReference) {
            baseTypeOfTarget = ((TypeReference)targetType).getBaseType();
            exactMatch = true;
        }

        TypeDefinition t = TypeUtilities.performImplicitDereferenceAndFunctioncall(attr); // getEffectiveType(ctx);


        if (exactMatch) {
            equal = t.equals(targetType);
            if (!equal) {
                equal = t.equals(baseTypeOfTarget);
            }
        } else {
            if (isSimpleInclVarCharAndRefCharOrStructureType(t) && isSimpleInclVarCharAndRefCharOrStructureType(targetType)) {
                equal = simpleTypeInclVarCharAndRefCharMayBeAssignedTo(targetType,t);
            } 
        }

        if (!equal) {
            t = TypeUtilities.performImplicitDereferenceAndFunctioncallForTargetType(attr,targetType); 

            if (t == null || !t.equals(targetType)) {
                ErrorStack.add(ctx, prefix, "expected type '" + targetType.toString() + "' --- got '"
                        + attr.getType().toString4IMC(true) + "'");
            }
        }
    }

    /**
     * return true, if given type is a <ul>
     * <li>simpleType pr
     * <li>reference of simple type
     * </ul>
     * return false, else  
     * @return
     */
    public static boolean isScalarType(TypeDefinition type) {
        if (isSimpleType(type)) {
            return true;
        }
        if (type instanceof TypeReference && isSimpleType(((TypeReference)type).getBaseType() )) {
            return true;
        }
        return false;
    }

    /**
     * check type compatibility for pass by IDENT including
     * <ul>
     * <li> check for INV restrictions
     * <li> check if a variable is passed
     * </ul>
     * 
     * @param typeOfFormalParameter
     * @param formalParameter
     * @param expression
     * @param m_ast
     * @return true, if it is possible<br>fails, if it is not possible and an error message became emitted
     */
    public static boolean mayBePassedByIdent(TypeDefinition typeOfFormalParameter,
            FormalParameter formalParameter, ExpressionContext expression, AST m_ast) {
        boolean assignable = false;
        final String breaksINV = " -- would break INV";

        TypeDefinition paramType = m_ast.lookupType(expression);
        String paramOriginalType = paramType.toErrorString();

        if (paramType instanceof UserDefinedSimpleType) {
            paramOriginalType = paramType.toErrorString();
        }

        if (paramType instanceof UserDefinedTypeStructure) {
            paramOriginalType = paramType.toErrorString();
        }

        String formalParamOriginalType = typeOfFormalParameter.toErrorString();
        if (typeOfFormalParameter instanceof UserDefinedSimpleType) {
            formalParamOriginalType = typeOfFormalParameter.toErrorString();
        }

        if (typeOfFormalParameter instanceof UserDefinedTypeStructure) {
            formalParamOriginalType = typeOfFormalParameter.toErrorString();
        }

        ASTAttribute attr = m_ast.lookup(expression);
        if (attr.getVariable()==null && attr.getConstant() == null && 
                (!(attr.m_type instanceof TypeSignal))) {
            CommonErrorMessages.typeMismatchProcedureParameterIdent(formalParamOriginalType, paramOriginalType, attr,"");
            //ErrorStack.add(expression,"IDENT parameter","cannot pass expression result by IDENT");
            return assignable;
        }

        // note: .equals does not look on INV 
        if (typeOfFormalParameter.equals(paramType)) {


            if ((paramType.hasAssignmentProtection() || attr.isConstant()) && 
                    !typeOfFormalParameter.hasAssignmentProtection()) {
                CommonErrorMessages.typeMismatchProcedureParameterIdent(formalParamOriginalType, paramOriginalType, attr, breaksINV);
                //ErrorStack.add(expression,"type mismatch","pass "+paramOriginalType+" to "+formalParamOriginalType+" by IDENT would break INV");
            } else {
                assignable = checkInvOfIdentParameter(typeOfFormalParameter, paramType);
                if (!assignable) {
                    CommonErrorMessages.typeMismatchProcedureParameterIdent(formalParamOriginalType, paramOriginalType, attr, breaksINV);
                }

                //assignable = true;
            }
            //         } else if (paramType instanceof TypeReference) {
            //             // maybe we may dereference the ref parameter
            //             paramType = ((TypeReference)paramType).getBaseType();
            //             if (typeOfFormalParameter.equals(paramType)) {
            //                 if (paramType.hasAssignmentProtection() && !typeOfFormalParameter.hasAssignmentProtection()) {
            //                     CommonErrorMessages.typeMismatchProcedureParameterIdent(formalParamOriginalType, paramOriginalType, attr, breaksINV);
            //                     // ErrorStack.add(expression,"type mismatch","pass "+paramOriginalType+" to "+formalParamOriginalType+" by IDENT would break INV");
            //                 } else {
            //                      assignable = true;
            //                 }
            //             } else {
            //                 CommonErrorMessages.typeMismatchProcedureParameterIdent(formalParamOriginalType, paramOriginalType, attr, null);
            //             }
        } else {
            CommonErrorMessages.typeMismatchProcedureParameterIdent(formalParamOriginalType, paramOriginalType, attr,null);
            // ErrorStack.add(expression,"type mismatch","cannot pass "+paramOriginalType+" to "+formalParamOriginalType+" by IDENT");
        }
        return assignable;
    }

    private static boolean checkInvOfIdentParameter(TypeDefinition formalParameter, TypeDefinition actualParameter) {
        // initial state: both type match, but INV is not checked yet
        // if actualParameterType has INV the formalParameter must have also the INV
        // we must consider: INV REF INV type
        if (actualParameter.hasAssignmentProtection() && !formalParameter.hasAssignmentProtection()) {
            return false;
        }

        while (actualParameter instanceof TypeReference || actualParameter instanceof TypeArray) {
            if (actualParameter instanceof TypeReference) {
                actualParameter = ((TypeReference)actualParameter).getBaseType();
                formalParameter = ((TypeReference)formalParameter).getBaseType();
            } else if (actualParameter instanceof TypeArray) {
                actualParameter = ((TypeArray)actualParameter).getBaseType();
                formalParameter = ((TypeArray)formalParameter).getBaseType();
            }
            if (actualParameter.hasAssignmentProtection() &&
                    !(formalParameter.hasAssignmentProtection())) {
                return false;
            }
        }

        return true;
    }
}
