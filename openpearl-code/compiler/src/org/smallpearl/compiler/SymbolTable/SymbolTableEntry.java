/*
 * [The "BSD license"]
 *  Copyright (c) 2012-2016 Marcel Schaible
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

package org.smallpearl.compiler.SymbolTable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.smallpearl.compiler.SmallPearlParser;

/**
 * base class for all symbol table entries
 * 
 * stores the 
 * <ul>
 * <li> name of the symbol
 * <li> visibility scope level 
 * <li> context of the definition
 * <li> some flags
 * <ul>
 * 
 * List of flags:
 * <ul>
 * <li>isUsed is set of the symbol is used - 
 *     currently (feb 2021) used for labels in the context of GOTO and EXIT
 * </ul>
 */
public abstract class SymbolTableEntry  implements Comparable<SymbolTableEntry> {

    private String m_name;
    private int m_level;
    private int m_flags;
    protected ParserRuleContext m_ctx;
    private static final int const_isUsed = 0x01;
    
    // the symbol is defined in the system part
    private static final int const_isUserName = 0x02;
    
    // the symbol is name of a SystemName (
    private static final int const_isSystemName = 0x04;
    
    SymbolTableEntry() {
        m_name = null;
        m_ctx=null;
        m_level=0;
        m_flags=0;
    }

    SymbolTableEntry(String name) {
        m_name = name;
    }

    SymbolTableEntry(ParserRuleContext ctx, String name) {
      m_name = name;
      m_ctx = ctx;
  }

    public String getName() {
        return m_name;
    }

    public Void setName(String name ) {
        m_name = name;
        return null;
    }

    public String toString(int level) {
        if (m_name != null) {
            return indentString(level) + Integer.toString(level) + ": " + m_name + " ";
        }
        else {
            return indentString(level) + Integer.toString(level) + ": ";
        }
    }
    public int getSourceLineNo() {
      return m_ctx.getStart().getLine();
  }

  public int getCharPositionInLine() {
      return m_ctx.getStart().getCharPositionInLine();
  }

    protected String indentString(int level) {
        String indent = "";

        for (int i = 1; i < level; i++) {
            indent += "  ";
        }

        //return "";
        return indent;
    }

    @Override
    public int compareTo(SymbolTableEntry o) {
        return this.m_name.compareTo(o.m_name);
    }

    public int getLevel() {
      return m_level;
    }

    public void setLevel(int level) {
      this.m_level = level;
    }

    public ParserRuleContext getCtx() {
      return m_ctx;
    }
    
    public void setIsUsed(boolean used) {
      if (used) {
        m_flags |= const_isUsed;
      } else {
        m_flags &= ~ const_isUsed;
      }
    }
    
    public boolean isUsed() {
      return ((m_flags & const_isUsed) != 0);
    }

    public void setIsUsername(boolean used) {
      if (used) {
        m_flags |= const_isUserName;
      } else {
        m_flags &= ~ const_isUserName;
      }
    }
    
    public boolean isUserName() {
      return ((m_flags & const_isUserName) != 0);
    }

    public void setIsSystemName(boolean used) {
      if (used) {
        m_flags |= const_isSystemName;
      } else {
        m_flags &= ~ const_isSystemName;
      }
    }
    
    public boolean isSystemName() {
      return ((m_flags & const_isSystemName) != 0);
    }

}
