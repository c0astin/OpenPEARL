/*
 * [The "BSD license"]
 * *  Copyright (c) 2012-2021 Marcel Schaible
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

package org.openpearl.compiler;

public class StructureComponent {
    public String          m_id;
    public TypeDefinition  m_type;
    public int             m_index;
    public String          m_alias;
    
    private int m_nbrOfRemainingElementsInCurrentComponent = 0;
    
    public String toString() {
        return m_id + " " +  m_type + " " + m_index + " " + m_alias;
    }
    
    public String toString4IMC(boolean isInStruct) {
        return m_type.toString4IMC(isInStruct);
    }
    
    public StructureComponent getFirstElement() {
       
        
        if (m_type instanceof TypeStructure) {
            return ((TypeStructure) m_type).getFirstElement();
        }
        return this;
// TypeArray not treated yet
   
        
    }

    public StructureComponent getNextElement() {
        if (m_type instanceof TypeStructure) {
            return ((TypeStructure) m_type).getNextElement();
        }
        return null;

        
    }

}