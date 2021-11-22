package org.smallpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
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
}
