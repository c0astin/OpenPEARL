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


import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.ArrayList;
import java.util.LinkedList;

public class TypeStructure extends TypeDefinition {
    public LinkedList<StructureComponent> m_listOfComponents;
    
    // used in getfirstElement() and getNextElement()
    private int m_currentComponentForIteration =0;
    private int m_nbrOfRemainingElementsInArray = 0;
    private StructureComponent m_currentStructureComponent=null;
    
    TypeStructure() {
        super("STRUCT");
        m_listOfComponents = new LinkedList<StructureComponent>();
    }

    public String getName() {
        return this.getStructureName();
    }

    public String toString() {
        String line = super.toString() + " [ ";

        for (int i = 0; i < m_listOfComponents.size(); i++) {
            String prefix = " ";
            if ( i > 0 ) {
                prefix = ",";
            }

            line += prefix + m_listOfComponents.get(i).toString();
        }

        return line + " ] ";
    }
    
    public String toString4IMC(boolean isInStructure) {
        String line = super.getName() + " [ ";

        for (int i = 0; i < m_listOfComponents.size(); i++) {
            String prefix = " ";
            if ( i > 0 ) {
                prefix = ",";
            }

            line += prefix + m_listOfComponents.get(i).toString4IMC(true);
        }

        return line + " ] ";
    }


    public int getTotalNoOfElements() {
        int nbr = 0;
        for (int i=0; i<m_listOfComponents.size(); i++) {
            StructureComponent sc = m_listOfComponents.get(i);
            if (sc.m_type instanceof TypeStructure) {
                nbr += ((TypeStructure)(sc.m_type)).getTotalNoOfElements();
            } else if (sc.m_type instanceof TypeArray) {
                TypeArray ta = (TypeArray)(sc.m_type);
                if (ta.getBaseType() instanceof TypeStructure) {
                  nbr += ta.getTotalNoOfElements()*((TypeStructure)(ta.getBaseType())).getTotalNoOfElements();
                } else {
                    nbr += ta.getTotalNoOfElements();
                }
            } else {
                nbr ++;
            }
        }
        return nbr;
    }
    public int noOfEntries() {
        return this.m_listOfComponents.size();
    }

    public boolean add(StructureComponent component) {
        m_listOfComponents.add(component);
        component.m_index = m_listOfComponents.size() - 1;
        component.m_alias = "m"+component.m_index;
        return true;
    }

    public boolean add(TypeStructure typeStructure) {
        StructureComponent component = new StructureComponent();
        component.m_type = typeStructure;
        m_listOfComponents.add(component);
        component.m_index = m_listOfComponents.size() - 1;
        component.m_alias = "m"+component.m_index;
        return true;
    }

    /*
            Datatype      letter   REF
            --------------------------
            FIXED         A        a
            FLOAT         B        b
            BIT           C        c
            CHARACTER     D        d
            CLOCK         E        e
            DURATION      F        f
            TASK                   g
            PROC                   h
            SEMA          I        i
            BOLT          J        j
            INTERRUT      K        k
            STRUCT        S        s
 */

    private String getDataTypeEncoding(TypeDefinition type) {
        if ( type instanceof TypeFixed)           return "A" + type.getPrecision().toString();
        if ( type instanceof TypeFloat)           return "B" + type.getPrecision().toString();
        if ( type instanceof TypeBit)             return "C" + type.getPrecision().toString();
        if ( type instanceof TypeChar)            return "D" + type.getPrecision().toString();
        if ( type instanceof TypeClock)           return "E";
        if ( type instanceof TypeDuration)        return "F";
        if ( type instanceof TypeTask)            return "G";
        if ( type instanceof TypeProcedure)       return "H";
        if ( type instanceof TypeSemaphore)       return "I";
        if ( type instanceof TypeBolt)            return "J";
        if ( type instanceof TypeInterrupt)       return "K";
        if ( type instanceof TypeStructure)       return "S";

        if ( type instanceof TypeArray ) {
            TypeArray typeArray = (TypeArray) type;
            String encoding =  Integer.toString(typeArray.getNoOfDimensions());
            ArrayList<ArrayDimension> dimensionList = typeArray.getDimensions();

            for ( int i = 0; i <  dimensionList.size(); i++ ) {
                encoding += "_" + dimensionList.get(i).getLowerBoundary() + "_" + dimensionList.get(i).getUpperBoundary();
            }

            if (typeArray.getBaseType() instanceof TypeStructure) {
                return typeArray.getBaseType().getName() + "_" + encoding;    
            } else {
                return getDataTypeEncoding(typeArray.getBaseType()) + "_" + encoding;
            }
            
        }

        if ( type instanceof TypeReference) {
            TypeReference reftype = (TypeReference) type;
            String s = getDataTypeEncoding(reftype.getBaseType());
            s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
            return s;
//            if ( reftype.getBaseType() instanceof TypeFixed)           return "a" + type.getPrecision().toString();
//            if ( reftype.getBaseType() instanceof TypeFloat)           return "b" + type.getPrecision().toString();
//            if ( reftype.getBaseType() instanceof TypeBit)             return "c" + type.getPrecision().toString();
//            if ( reftype.getBaseType() instanceof TypeChar)            return "d" + type.getPrecision().toString();
//            if ( reftype.getBaseType() instanceof TypeClock)           return "e";
//            if ( reftype.getBaseType() instanceof TypeDuration)        return "f";
//            if ( reftype.getBaseType() instanceof TypeTask)            return "g";
//            if ( reftype.getBaseType() instanceof TypeProcedure)       return "h";
//            if ( reftype.getBaseType() instanceof TypeSemaphore)       return "i";
//            if ( reftype.getBaseType() instanceof TypeBolt)            return "j";
//            if ( reftype.getBaseType() instanceof TypeInterrupt)       return "k";
//            if ( reftype.getBaseType() instanceof TypeStructure)       return "s" + reftype.getBaseType().getName();
        }

        return "~?~";
    }

    public String getStructureName() {
        String sname = "";

        for (int i = 0; i < m_listOfComponents.size(); i++ ) {
            TypeDefinition typ = m_listOfComponents.get(i).m_type;
            sname += getComponentName(typ);
        }

        return "S" + sname.length() + sname;
    }

    private String getComponentName(TypeDefinition type) {
        String componentName = "";

        if ( type instanceof TypeStructure) {
            TypeStructure typeStructure = (TypeStructure) type;
            String components = "";
            for (int i = 0; i < typeStructure.m_listOfComponents.size(); i++ ) {
                TypeDefinition typ = typeStructure.m_listOfComponents.get(i).m_type;
                components += getComponentName(typ);
            }
            componentName += "S" + components.length() + components;
        }
        else {
            componentName = getDataTypeEncoding(type);
        }

        return componentName;
    }

    /**
     * Search the given identifier in the list of structure elements.
     *
     * @param id Identifier to look for
     * @return StructureComponent of the identifier
     *         null if not found
     */
    public StructureComponent lookup(String id) {
        for (int i = 0; i < m_listOfComponents.size(); i++ ) {
            if ( m_listOfComponents.get(i).m_id.equals(id) ) {
                return m_listOfComponents.get(i);
            }
        }

        return null;
    }

    /**
     * Get a structure component by its index-.
     *
     * @param index of the component starting from 0
     * @return StructureComponent of the identifier
     */
    public StructureComponent getStructureComponentByIndex(int index) {
        return m_listOfComponents.get(index);
    }

    public ST toST(STGroup group) {
        ST st = group.getInstanceOf("StructureType");
        st.add("type", getStructureName());
        return st;
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TypeStructure)) {
            return false;
        }

        TypeStructure that = (TypeStructure) other;

        // Custom equality check here.
        return this.getStructureName().equals(that.getStructureName());
    }

    
    /**
     * iterator over STRUCT components
     * 
     * getFirstElement() returns the first data element of a nested TypeStructure
     * getNextElement() returns the next data element of a nested TypeStructure
     * 
     * Components of TypeStructure are rolled out and arrays of TypeStructure 
     * are rolled out repeatedly. 
     * All other types are returned step-by-step for each call of getNextElement() 

     * @return the selected StructureComponent, or
     *         null, if the end of the structure was reached
     */
    public StructureComponent getFirstElement() {
        m_currentComponentForIteration =-1;
        return getNextElement();
       
    }
    
    public StructureComponent getNextElement() {
        StructureComponent sc=null;
        if (m_currentStructureComponent != null) {
            if (m_currentStructureComponent.m_type instanceof TypeArray) {
                // m_currentStructureComponent is only != null, if we have a
                //  component of TypeStructure, or of array of TypeStructure. 
                sc = ((TypeStructure )(((TypeArray)m_currentStructureComponent.m_type)).getBaseType()).getNextElement();
                if (sc != null) {
                    return sc;
                }
                // end of TypeStructure reached!
                m_nbrOfRemainingElementsInArray --;
                if (m_nbrOfRemainingElementsInArray > 0) {
                    return ((TypeStructure )(((TypeArray)m_currentStructureComponent.m_type)).getBaseType()).getFirstElement();
                 } else {
                     m_currentStructureComponent = null;
                 }
            } else {
               sc = m_currentStructureComponent.getNextElement();
               if (sc != null) {
                  return sc;
              }
            }
        }
        m_currentComponentForIteration ++;
        if (m_currentComponentForIteration >= m_listOfComponents.size()) {
            return null;
        }
        StructureComponent comp = getStructureComponentByIndex(m_currentComponentForIteration);
        TypeDefinition td = comp.m_type;
        
        if (td instanceof TypeStructure) {
            m_currentStructureComponent = comp;
            m_nbrOfRemainingElementsInArray=0;
            return comp.getFirstElement();
        }
        if (td instanceof TypeArray && ((TypeArray)td).getBaseType() instanceof TypeStructure) {
            m_nbrOfRemainingElementsInArray = ((TypeArray)td).getTotalNoOfElements();
            m_currentStructureComponent = comp;
            return ((TypeStructure )(((TypeArray)comp.m_type)).getBaseType()).getFirstElement();
        }
        return comp;
    }
}