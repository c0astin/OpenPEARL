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

package org.smallpearl.compiler;


public class TypeDation extends TypeDefinition {
	private boolean m_in;  ///< is IN
	private boolean m_out;    ///< is OUT; INOUT sets both
	private boolean m_systemDation; // is a SYSTEM dation
	private boolean m_alphic;
	private boolean m_basic;
	private String m_typeOfTransmission;
	private boolean m_direct;
	private boolean m_forward;
	private boolean m_forback;
	private boolean m_cyclic;
	private boolean m_stream;
	private boolean m_isDeclaration;
	private String m_global;
	private int m_dimension1;
	private int m_dimension2;
	private int m_dimension3;
	private boolean m_tfu;
	
	
	// typology not added yet
	
	
    TypeDation() {
        super("DATION");
        m_in = false;
        m_out = false;
        m_systemDation = false;
        m_alphic = false;
        m_basic = false;
        m_typeOfTransmission = null;
        m_direct = false;
        m_forward = false;
        m_forback = false;
        m_cyclic = false;  // default value from language report
        m_stream = true;   // default value from language report
        m_isDeclaration = false;
        m_global = null;
        m_dimension1 = -1;  // not set
        m_dimension2 = -1;  // not set
        m_dimension3 = -1;  // not set
        m_tfu = false;      // default from language report
    }

    public String toString() {
    	String s=this.getName()+" ";
    	if (m_isDeclaration) s+= " (DCL) ";
    	else s += " (SPC) ";
    	if (m_in) s += "IN";
    	if (m_out) s += "OUT";
    	if (m_systemDation) s+= " SYSTEM";
    	else s+= " (userdation)";
    	if (m_alphic) s+=" ALPHIC";
    	if (m_basic) s+=" BASIC";
    	if (m_typeOfTransmission!= null) {
    		s+=" "+m_typeOfTransmission;
    	}
    	if (m_direct) s+= " DIRECT";
    	if (m_forward) s+= " FORWARD";
    	if (m_forback) s+= " FORBACK";
    	if (m_dimension1>=0) s+=" DIM("+m_dimension1;
    	if (m_dimension2>=0) s+= ","+m_dimension2;
    	if (m_dimension3>=0) s+= "," +m_dimension3;
    	if (m_dimension1>= 0) s+= ")";
    	s += " (numberOfDimensions="+getNumberOfDimensions()+")";
    	if (m_tfu) s+= " TFU";
    	if (m_cyclic) s+= " CYCLIC";
    	else s+= " NOCYCL";
    	if (m_stream) s+= " STREAM";
    	else s+=" NOSTREAM";
    		
    	// typology still missing
        return s;
    }
    
	public boolean isIn() {
		return m_in;
	}

	public void setIn(boolean m_in) {
		this.m_in = m_in;
	}
	
	public boolean isOut() {
		return m_out;
	}

	public void setOut(boolean m_out) {
		this.m_out = m_out;
	}

    public boolean isSystemDation() {
		return m_systemDation;
	}

	public void setSystemDation(boolean m_systemDation) {
		this.m_systemDation = m_systemDation;
	}

	public boolean isAlphic() {
		return m_alphic;
	}

	public void setAlphic(boolean m_alphic) {
		this.m_alphic = m_alphic;
	}

	public boolean isBasic() {
		return m_basic;
	}

	public void setBasic(boolean m_basic) {
		this.m_basic = m_basic;
	}

	public String getTypeOfTransmission() {
		return m_typeOfTransmission;
	}

	public void setTypeOfTransmission(String m_typeOfTransmission) {
		this.m_typeOfTransmission = m_typeOfTransmission;
	}

	public boolean isDirect() {
		return m_direct;
	}

	public void setDirect(boolean m_direct) {
		this.m_direct = m_direct;
	}

	public boolean isForward() {
		return m_forward;
	}

	public void setForward(boolean m_forward) {
		this.m_forward = m_forward;
	}

	public boolean isForback() {
		return m_forback;
	}

	public void setForback(boolean m_forback) {
		this.m_forback = m_forback;
	}

	public boolean isCyclic() {
		return m_cyclic;
	}

	public void setCyclic(boolean m_cyclic) {
		this.m_cyclic = m_cyclic;
	}

	public boolean isStream() {
		return m_stream;
	}

	public void setStream(boolean m_stream) {
		this.m_stream = m_stream;
	}

	/* fucking name but consequent */
	public boolean isIsDeclaration() {
		return m_isDeclaration;
	}

	public void setIsDeclaration(boolean m_isDeclaration) {
		this.m_isDeclaration = m_isDeclaration;
	}
	
	
	public boolean isGlobal() {
		return (m_global!=null);
	}

	public String getGlobal() {
		return m_global;
	}
	public void setGlobal(String moduleName) {
	   this.m_global = moduleName;
	}
	
	public int getDimension1() {
		return m_dimension1;
	}

	public void setDimension1(int m_dimension1) {
		this.m_dimension1 = m_dimension1;
	}

	public int getDimension2() {
		return m_dimension2;
	}

	public void setDimension2(int m_dimension2) {
		this.m_dimension2 = m_dimension2;
	}
	public int getDimension3() {
		return m_dimension3;
	}

	public void setDimension3(int m_dimension3) {
		this.m_dimension3 = m_dimension3;
	}
	
	public int getNumberOfDimensions() {
		if (m_dimension1 >= 0) return (3);
		if (m_dimension2 >= 0) return (2);
		if (m_dimension3 >= 0) return (1);
		return 0;
	}
	public void setTfu(boolean tfu) {
		m_tfu = tfu;
	}
	
	public boolean hasTfu() {
		return m_tfu;
	}
	
	@Override
    public boolean equals(Object other) {
        if (!(other instanceof TypeDation)) {
            return false;
        }

        TypeDation that = (TypeDation) other;
        
        // Custom equality check here.
        if (this.m_in != that.m_in) return false;
        if (this.m_out != that.m_out) return false;
        if (this.m_systemDation != that.m_systemDation) return false;
        if (this.m_alphic != that.m_alphic) return false;
        if (this.m_basic != that.m_basic) return false;
        if (this.m_typeOfTransmission.equals(that.m_typeOfTransmission)) return false;
        if (this.m_direct != that.m_direct) return false;
        if (this.m_forward != that.m_forward) return false;
        if (this.m_cyclic != that.m_cyclic) return false;
        if (this.m_stream != that.m_stream) return false;
        if (this.m_isDeclaration != that.m_isDeclaration) return false;
   

        return true;
    }


}
