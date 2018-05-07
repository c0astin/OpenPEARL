package org.smallpearl.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.smallpearl.compiler.SmallPearlParser.Unlabeled_statementContext;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;

import java.util.LinkedList;

public class Initializer  {
	private ParserRuleContext m_context;
	private ConstantValue     m_constant;

	    public Initializer(ParserRuleContext ctx, ConstantValue constant) {
	    	m_context = ctx;
	    	m_constant = constant;
	    }

	public ParserRuleContext getContext() { return m_context; }
	public ConstantValue getConstant() { return m_constant; }
    public void setConstant(ConstantValue constant) { m_constant = constant; }

	public String toString() {
	 	if ( m_constant instanceof ConstantFixedValue) {
	 		return m_constant.toString();
		}
		else if ( m_constant instanceof ConstantFloatValue) {
            return m_constant.toString();
        }
        else if ( m_constant instanceof ConstantDurationValue) {
            return m_constant.toString();
        }
        else if ( m_constant instanceof ConstantClockValue) {
            return m_constant.toString();
        }
        else if ( m_constant instanceof ConstantCharacterValue) {
            return m_constant.toString();
        }
        else if ( m_constant instanceof ConstantBitValue) {
            return m_constant.toString();
        }
        else {
			return m_constant.toString();
		}

//		return "???";
	}
}
