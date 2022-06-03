package org.openpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.SymbolTable.FormalParameter;
import org.openpearl.compiler.SymbolTable.SymbolTableEntry;
/**
 * some error messages, which are emitted at different source locations 
 * <ul>
 * <li>duplicate declaration in the level of scope
 * <li>warning if declaration hides other declaration
 * <li>illegal length of types FIXED,FLOAT,BIT and CHAR
 * </ul>
 *
 */
public class CommonErrorMessages {


    public static void doubleDeclarationError(SymbolTableEntry current,
            SymbolTableEntry entry) {
        ErrorStack.add(current.getCtx(), "duplicate declaration", current.getName());
        ErrorStack.note(entry.getCtx(), "previous declaration", "was here");
        
    }
    public static void doubleDeclarationError(String name, ParserRuleContext ctx,
            ParserRuleContext previousCtx) {
        ErrorStack.add(ctx, "duplicate declaration", name);
        ErrorStack.note(previousCtx, "previous declaration", "was here");
    }

    public static void doubleDeclarationWarning(String msg, String name, ParserRuleContext ctx,
            ParserRuleContext previousCtx) {
        ErrorStack.warn(ctx, msg + name, "hides previous declaraction");
        ErrorStack.note(previousCtx, "previous declaration", "was here");
    }

    public static void wrongFixedPrecission(ParserRuleContext ctx) {
        ErrorStack.add(ctx, "illegal precision", "not in range [" + Defaults.FIXED_MIN_LENGTH + ","
                + Defaults.FIXED_MAX_LENGTH + "]");
    }

    public static void wrongFloatPrecission(ParserRuleContext ctx) {
        ErrorStack.add(ctx, "illegal precision", "not in set [" + Defaults.FLOAT_SHORT_PRECISION
                + "," + Defaults.FLOAT_LONG_PRECISION + "]");
    }

    public static void wrongBitLength(ParserRuleContext ctx) {
        ErrorStack.add(ctx, "illegal length",
                "not in range [" + Defaults.BIT_MIN_LENGTH + "," + Defaults.BIT_MAX_LENGTH + "]");
    }

    public static void wrongCharLength(ParserRuleContext ctx) {
        ErrorStack.add(ctx, "illegal length",
                "not in range [" + Defaults.CHARACTER_LENGTH + "," + Defaults.CHARACTER_MAX_LENGTH + "]");
    }
    
    public static void typeMismatchProcedureParameter(String lhsType, String rhsType, ASTAttribute rhsAttr) {
        ErrorStack.add("type mismatch: cannot pass "+ getTypeOfRhs(rhsAttr)+" of type "+ rhsType + 
                " as " + lhsType);
    }
    
    public static void typeMismatchProcedureParameterIdent(String lhsType, String rhsType, ASTAttribute rhsAttr,String hint) {
        ErrorStack.add("type mismatch: cannot pass "+ getTypeOfRhs(rhsAttr)+" of type "+ rhsType + 
                " as " + lhsType+" by IDENT"+ (hint!=null?hint:"") );
    }

    public static void typeMismatchInAssignment(String lhsType, String rhsType, ASTAttribute rhsAttr) {
        ErrorStack.add("type mismatch: cannot assign "+ getTypeOfRhs(rhsAttr)+" of type "+ rhsType + 
                " to " + lhsType);
    }
    
    public static void typeMismatchInInit(String lhsType, String rhsType, ASTAttribute rhsAttr) {
        ErrorStack.add("type mismatch: cannot use "+ getTypeOfRhs(rhsAttr)+" of type "+ rhsType + 
                " as initializer for object of type " + lhsType);
    }
    
    private static String getTypeOfRhs(ASTAttribute attr) {
        if (attr == null || attr.m_constant != null) {
            return "constant";
        } else if (attr.getVariable() != null || attr.getSymbolTableEntry() != null) {
            return "object";
        } else {
            return "expression";
        }
    }
}
