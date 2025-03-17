package org.openpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;


public class ContextUtilities {
        public static  String printCtx(ParserRuleContext ctx, int length) {
        String readable = null;
        
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        Interval i = new Interval(a, b);
        readable = ctx.start.getInputStream().getText(i);

        // clip at \n
        int newline = readable.indexOf('\n');
        if (newline > 0) {
            readable = readable.substring(0, newline-1);
        }

        if (readable.length() > length) {
            readable = readable.substring(0, length) + "...";
        }

        return readable;
    }

  
}
