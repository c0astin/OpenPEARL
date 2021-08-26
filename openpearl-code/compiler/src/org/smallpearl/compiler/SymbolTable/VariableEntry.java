package org.smallpearl.compiler.SymbolTable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.smallpearl.compiler.*;

public class VariableEntry extends SymbolTableEntry {

    private TypeDefinition    m_type;
    private Boolean           m_hasAssigmentProtection;
    private Boolean           m_loopControlVariable;
    private Initializer  m_initializer;   // just memorize the location of the initializer in AST 
   
    public VariableEntry()
    {
        super("");
        //this.m_ctx = null;
        this.m_type = null;
        this.m_hasAssigmentProtection = false;
        this.m_loopControlVariable = false;
        this.m_initializer = null;
    }

    public VariableEntry(String name)
    {
        super(name);
        //this.m_ctx = null;
        this.m_type = null;
        this.m_hasAssigmentProtection = false;
        this.m_loopControlVariable = false;
        this.m_initializer = null;
    }

    public VariableEntry(String name, TypeDefinition type,
            org.antlr.v4.runtime.ParserRuleContext ctx) {
        super(name);
        this.m_ctx = ctx;
        this.m_type = type;
        this.m_hasAssigmentProtection = false;
        this.m_loopControlVariable = false;
        this.m_initializer = null;


    }
    public VariableEntry(String name, TypeDefinition type, Boolean hasAssignmentProtection, ParserRuleContext ctx)
    {
        super(name);
        this.m_ctx = ctx;
        this.m_type = type;
        this.m_hasAssigmentProtection = hasAssignmentProtection;
        this.m_loopControlVariable = false;
        this.m_initializer = null;
    }

    
    public VariableEntry(String name, TypeDefinition type, Boolean hasAssigmentProtection, ParserRuleContext ctx, Initializer init)
    {
        super(name);
        this.m_ctx = ctx;
        this.m_type = type;
        this.m_hasAssigmentProtection = hasAssigmentProtection;
        this.m_loopControlVariable = false;
        this.m_initializer = init;
    

    }

    public String toString(int level) {
        String assigmenProtection = this.m_hasAssigmentProtection ? "INV" : "";
        
        return indentString(level) +
                super.toString(level) +
                "var " +
                m_type + " " +
                assigmenProtection +
                (this.m_loopControlVariable ? " LC" :"") +
                (this.m_initializer != null ? "  INIT-Index(" + m_initializer + ")" : "");
    }


	public TypeDefinition getType() { return m_type; }

	// TODO: add array, struct, ref
	public TypeDefinition getBaseType() {
        return m_type;
    }

    public Boolean getAssigmentProtection() { return m_hasAssigmentProtection; }

    public Void setLoopControlVariable() {
        m_loopControlVariable = true;
        return null;
    }

    public Boolean getLoopControlVariable() { return m_loopControlVariable; }

    public Initializer getInitializer() { return m_initializer; }

}