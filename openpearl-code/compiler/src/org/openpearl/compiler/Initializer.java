package org.openpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;

public class Initializer  {
	public ParserRuleContext m_context;

    public Initializer(ParserRuleContext ctx) {
        m_context = ctx;
    }

	public ParserRuleContext getContext() { return m_context; }

	public String toString() {
		return '"' + m_context.getText() + '"';
	}
}
