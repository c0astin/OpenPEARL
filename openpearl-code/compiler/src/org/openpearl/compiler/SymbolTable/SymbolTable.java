/*
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


package org.openpearl.compiler.SymbolTable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.openpearl.compiler.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Organize a hierarchically SymbolTable with individual tables for each 
 * scope.
 * 
 * Special treatment of block and loops
 * <ul>
 * <li>blocks and loops may have a name. 
 * <li> the name may not be redefined in an inner block or loop level
 * <li>blocks and loops may be defined also without a name
 * <li>the HashMap of the SymbolTable stores only the last added 
 *    element of a given key. Thus loops and blocks without a name 
 *    are not reliable in the Hashmap  
 * <li>to get the context for the continuation point for EXIT we need access
 *    to the Loop/Block entry with the identical context
 * <li>since the context is unique, we may store loop- and block-entries
 *     in a list (Vector) for the complete module
 * </ul>
 * 
 * Special treatment of names in system part.
 * All names in the system part are stored in a static vector.
 * Attributes may differ, if a name is user define (was seen in the system part left of the colon (':')
 * <br>
 *
 * Supplied Methods:
 * <ul>
 * <li>enterSystemPartName
 * <li>lookupSystemPartName
 * </ul> 
 */
public class SymbolTable {

    /**
     * Allocate a new symtab of the given size.  The size is the number of
     * table m_entries (not bytes).  All m_entries are initialized to null, the
     * parent is initialized to null, and m_level to 0.  Parent and m_level are
     * only set to non-null/non-zero values when a SymbolTable is constructed
     * with the newLevel method.
     * 
     */
    public SymbolTable() {
        m_entries = new HashMap<String, SymbolTableEntry>();
        m_level = 0;
        m_usesSystemElements = false;

    }

    /**
     * Allocate a new symtab and add it as a new m_level to this symtab.  The new
     * m_level is linked into the existing symtab via the scope field of the
     * given function entry, and the parent entry of this, as illustrated in
     * the class documentation.  The m_level field of the the new symtab is set
     * to this.m_level+1.  The return value is a reference to the new m_level.
     */
    public SymbolTable newLevel(ModuleEntry moduleEntry) {
        SymbolTable newst;

        enter(moduleEntry);
        newst = moduleEntry.scope = new SymbolTable();
        newst.parent = this;
        newst.m_level = m_level + 1;

        return newst;

    }

    public SymbolTable newLevel(ProcedureEntry procedureEntry) {
        SymbolTable newst;

        enter(procedureEntry);
        newst = procedureEntry.scope = new SymbolTable();
        newst.parent = this;
        newst.m_level = m_level + 1;

        return newst;

    }

    public SymbolTable newLevel(BlockEntry blockEntry) {
        SymbolTable newst;
        m_loopsAndBlocks.add(blockEntry);

        enter(blockEntry);
        newst = blockEntry.scope = new SymbolTable();
        newst.parent = this;
        newst.m_level = m_level + 1;

        return newst;

    }

    public SymbolTable newLevel(TaskEntry taskEntry) {
        SymbolTable newst;

        enter(taskEntry);
        newst = taskEntry.scope = new SymbolTable();
        newst.parent = this;
        newst.m_level = m_level + 1;

        return newst;

    }

    public SymbolTable newLevel(LoopEntry loopEntry) {
        SymbolTable newst;
        m_loopsAndBlocks.add(loopEntry);

        enter(loopEntry);
        newst = loopEntry.scope = new SymbolTable();
        newst.parent = this;
        newst.m_level = m_level + 1;
        return newst;
    }

    public SymbolTableEntry lookup(String name) {
        int i;
        SymbolTable st;
        SymbolTableEntry se;
        Log.debug("SymbolTable:lookup: name=" + name);

        for (st = this; st != null; st = st.parent) {
            if ((se = (SymbolTableEntry) st.m_entries.get(name)) != null) {
                return se;
            }
        }

        return null;
    }

    public SymbolTableEntry lookupLoopBlock(ParserRuleContext ctx) {
        SymbolTable st;
        SymbolTableEntry se;

        for (int i = 0; i < m_loopsAndBlocks.size(); i++) {
            if (m_loopsAndBlocks.get(i).getCtx().equals(ctx)) {
                return m_loopsAndBlocks.get(i);
            }
        }
        return null;
    }

    public SymbolTableEntry lookupLocal(String name) {
        Log.debug("SymbolTable:lookupLocal: name=" + name);
        return m_entries.get(name);
    }

    /**
     * 
     * @param se
     * @return false, if the symbol was already in the symbol table
     * <br> true, if the symbol was added
     */
    public boolean enter(SymbolTableEntry se) {

        if (lookupLocal(se.getName()) != null) {
            return false;
        }

        se.setLevel(m_level);
        m_entries.put(se.getName(), se);
        return true;
    }

    public boolean enterOrReplace(SymbolTableEntry se) {
        if (lookupLocal(se.getName()) != null) {
            m_entries.remove(se.getName());
        }
        se.setLevel(m_level);
        m_entries.put(se.getName(), se);
        return true;
    }

    public SymbolTable ascend() {
        return parent != null ? parent : this;
    }

    // obsolete - never used
    // class comaprison does not work
//    public SymbolTable descend(String name) {
//        SymbolTableEntry se = lookupLocal(name);
//       
//        try {
//            if (se == null) {
//                return this;
//            }
//            
//            if (se.getClass() != Class.forName("ModuleEntry")) {
//               return ((ModuleEntry) se).scope;
//            }
//
//            if (se.getClass() != Class.forName("ProcedureEntry")) {
//                return ((ProcedureEntry) se).scope;
//            }
//
//            if (se.getClass() != Class.forName("TaskEntry")) {
//                return ((TaskEntry) se).scope;
//            }
//
//            if (se.getClass() != Class.forName("BlockEntry")) {
//                return ((BlockEntry) se).scope;
//            }
//
//            return this;
//        } catch (Exception e) {
//            System.out.println(e);
//            e.printStackTrace();
//            return null;
//        }
//    }

    public SymbolTable getModuleTable() {
       
        if  (m_level>0) {
         ErrorStack.addInternal("SymboleTable.getModuleTable must be called from top layer table");
        }
        
        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof ModuleEntry) {
                return ((ModuleEntry) symbolTableEntry).scope;
            }
        }
        return null;
    }
    
    public void dump() {
        System.out.println();
        System.out.println("Symboltable:");
        System.out.println(toString(m_systemPartNames));

        System.out.println("PROBLEM part:");
        System.out.println(toString() + "\n");
    }

    public String toString() {
        return toString(this.m_level);
    }

    public String toString(int level) {
        String output = "";
        int nextLevel = level + 1;

        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            String s = it.next().toString(nextLevel) + (it.hasNext() ? "\n" : "");
            //System.out.println("level="+level+" s="+s+"len(s)="+s.length()+"len(output)="+output.length());
            output += s;
            //System.out.println("-->"+output.length());
        }
        

        return output;
    }
  
    public LinkedList<TaskEntry> getTaskDeclarationsAndSpecifications() {
        LinkedList<TaskEntry> listOfTaskEntries = new LinkedList<TaskEntry>();

        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof TaskEntry) {
                TaskEntry taskEntry = (TaskEntry) symbolTableEntry;
                listOfTaskEntries.add(taskEntry);
            }
        }
         
        return listOfTaskEntries;
    }
    
    public LinkedList<ProcedureEntry> getProcedureDeclarationsAndSpecifications() {
        LinkedList<ProcedureEntry> listOfProcedureEntries = new LinkedList<ProcedureEntry>();

        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof ProcedureEntry) {
                ProcedureEntry entry = (ProcedureEntry) symbolTableEntry;
                listOfProcedureEntries.add(entry);
            }
        }
         
        return listOfProcedureEntries;
    }

    private  LinkedList<VariableEntry> getAllVariableDeclarations(SymbolTable scope) {
        LinkedList<VariableEntry> listOfVariableDeclarationsEntries =
                new LinkedList<VariableEntry>();
        
        for (Iterator<SymbolTableEntry> it = scope.m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry ste = it.next();
            if (ste instanceof VariableEntry) {
                listOfVariableDeclarationsEntries.add((VariableEntry)ste);
            } else if (ste instanceof TaskEntry) {
                // ignore task specifications
                if (((TaskEntry)ste).scope != null) {
                   listOfVariableDeclarationsEntries.addAll(getAllVariableDeclarations(((TaskEntry)ste).scope));
                }
            } else if (ste instanceof LoopEntry) {
                listOfVariableDeclarationsEntries.addAll(getAllVariableDeclarations(((LoopEntry)ste).scope));
            } else if (ste instanceof BlockEntry) {
                listOfVariableDeclarationsEntries.addAll(getAllVariableDeclarations(((BlockEntry)ste).scope));
            } else if (ste instanceof ProcedureEntry) {
                // ignore procedure specifications
                if (((ProcedureEntry)ste).scope != null) {
                   listOfVariableDeclarationsEntries.addAll(getAllVariableDeclarations(((ProcedureEntry)ste).scope));
                }
            } 
            
        }

        return listOfVariableDeclarationsEntries;
    }
  
    public LinkedList<VariableEntry> getAllVariableDeclarations() {
        SymbolTable saveSymbolTable = m_currentSymbolTable;
        m_currentSymbolTable = this;
        LinkedList<VariableEntry> listOfVariableDeclarationsEntries =
                new LinkedList<VariableEntry>();
        // hopefully we are on top level ....
        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry ste = it.next();
            if (ste instanceof ModuleEntry) {
                m_currentSymbolTable = ((ModuleEntry)ste).scope;
                listOfVariableDeclarationsEntries.addAll(getAllVariableDeclarations(m_currentSymbolTable));
                m_currentSymbolTable = parent;
            }
        }
        m_currentSymbolTable = saveSymbolTable;
        Collections.sort( listOfVariableDeclarationsEntries, new SortByContext());        
        return listOfVariableDeclarationsEntries;
    }
    
    class SortByContext implements Comparator<VariableEntry> {
        @Override
        public int compare(VariableEntry arg0, VariableEntry arg1) {
            int result =0;
            if (arg0.getCtx() == null || arg1.getCtx() == null) {
                ErrorStack.addInternal(null, "SymbolTable::SortbyContext","context is null");
                return 0;
            }
            result = arg0.getCtx().start.getLine() - arg1.getCtx().start.getLine();
            if (result == 0) {
                result = arg0.getCtx().start.getCharPositionInLine() - arg1.getCtx().start.getCharPositionInLine();
            }
            return result;
        }
        
    }
    public LinkedList<VariableEntry> getVariableDeclarations() {
        LinkedList<VariableEntry> listOfVariableDeclarationsEntries =
                new LinkedList<VariableEntry>();
        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof VariableEntry) {
                VariableEntry variableEntry = (VariableEntry) symbolTableEntry;
                listOfVariableDeclarationsEntries.add(variableEntry);
            }
        }
        Collections.sort( listOfVariableDeclarationsEntries, new SortByContext()); 
        return listOfVariableDeclarationsEntries;
    }

    
    public LinkedList<VariableEntry> getDationSpcAndDcl() {
        LinkedList<VariableEntry> listOfVariableDeclarationsEntries =
                new LinkedList<VariableEntry>();
        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof VariableEntry) {
                VariableEntry variableEntry = (VariableEntry) symbolTableEntry;
                if (variableEntry.getType() instanceof TypeDation) {
                   listOfVariableDeclarationsEntries.add(variableEntry);
                }
            }
        }
        Collections.sort( listOfVariableDeclarationsEntries, new SortByContext()); 
        return listOfVariableDeclarationsEntries;
    }

    
   // not used (2022-04-01 rm)
//    public LinkedList<VariableEntry> getAllArrayDeclarations(SymbolTable symbolTable) {
//        LinkedList<VariableEntry> listOfArrayDeclarations = new LinkedList<VariableEntry>();
//
//        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
//            SymbolTableEntry entry = it.next();
//        }
//
//        return listOfArrayDeclarations;
//    }

    public LinkedList<VariableEntry> getSemaphoreDeclarations() {
        LinkedList<VariableEntry> listOfSemaEntries = new LinkedList<VariableEntry>();
   
        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof VariableEntry) {
                VariableEntry ve = (VariableEntry)symbolTableEntry;
                if (ve.getType()  instanceof TypeSemaphore) {
                    listOfSemaEntries.add(ve);
                }
            }
        }

        return listOfSemaEntries;
    }

    
    public LinkedList<VariableEntry> getBoltDeclarations() {
        LinkedList<VariableEntry> listOfBoltEntries = new LinkedList<VariableEntry>();
   
        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof VariableEntry) {
                VariableEntry ve = (VariableEntry)symbolTableEntry;
                if (ve.getType()  instanceof TypeBolt) {
                    listOfBoltEntries.add(ve);
                }
            }
        }

        return listOfBoltEntries;
    }
    
    public LinkedList<InterruptEntry> getInterruptSpecifications() {
        LinkedList<InterruptEntry> listOfVariableDeclarationsEntries =
                new LinkedList<InterruptEntry>();
   
        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof InterruptEntry) {
                InterruptEntry variableEntry = (InterruptEntry) symbolTableEntry;
                listOfVariableDeclarationsEntries.add(variableEntry);
            }
        }

        return listOfVariableDeclarationsEntries;
    }
    
    
    public LinkedList<SignalEntry> getSignalSpecifications() {
        LinkedList<SignalEntry> list =
                new LinkedList<SignalEntry>();
   
        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof SignalEntry) {
                SignalEntry variableEntry = (SignalEntry) symbolTableEntry;
                list.add(variableEntry);
            }
        }

        return list;
    }
    public LinkedList<ProcedureEntry> getProcedureSpecificationsAndDeclarations() {
        LinkedList<ProcedureEntry> listOfVariableDeclarationsEntries =
                new LinkedList<ProcedureEntry>();
    
        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof ProcedureEntry) {
                ProcedureEntry variableEntry = (ProcedureEntry) symbolTableEntry;
                listOfVariableDeclarationsEntries.add(variableEntry);
            }
        }

        return listOfVariableDeclarationsEntries;
    }
    
      
    public LinkedList<ModuleEntry> getModules() {
        LinkedList<ModuleEntry> listOfModules = new LinkedList<ModuleEntry>();

        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof ModuleEntry) {
                ModuleEntry moduleEntry = (ModuleEntry) symbolTableEntry;
                listOfModules.add(moduleEntry);
            }
        }

        return listOfModules;
    }

    public HashMap<String, TypeStructure> getStructureDeclarations() {
        HashMap<String, TypeStructure> structures = new HashMap<>();
        SymbolTableEntry e;

        for (Iterator<SymbolTableEntry> it = m_entries.values().iterator(); it.hasNext();) {
            SymbolTableEntry symbolTableEntry = it.next();
            if (symbolTableEntry instanceof ModuleEntry) {
                getStructureDeclarationsForSymboltable(((ModuleEntry) symbolTableEntry).scope,
                        structures);
            }
        }

        return structures;
    }

    private void getStructureDeclarationsForSymboltable(SymbolTable symbolTable,
            HashMap<String, TypeStructure> structures) {
        SymbolTableEntry e;

        for (Iterator<SymbolTableEntry> it = symbolTable.m_entries.values().iterator(); it
                .hasNext();) {
            SymbolTableEntry symbolTableEntry = (SymbolTableEntry) it.next();

            if (symbolTableEntry instanceof VariableEntry) {
                VariableEntry entry = (VariableEntry) symbolTableEntry;
                if (entry.getType() instanceof TypeStructure) {
                    TypeStructure struct = (TypeStructure) entry.getType();
                    getStructureDeclarationsForStructure(struct.getStructureName(), struct,
                            structures);
                } else if (entry.getType() instanceof TypeArrayDeclaration) {
                    TypeArrayDeclaration array = (TypeArrayDeclaration) entry.getType();
                    if (array.getBaseType() instanceof TypeStructure) {
                        TypeStructure struct = (TypeStructure) array.getBaseType();
                        getStructureDeclarationsForStructure(struct.getStructureName(), struct,
                                structures);
                    } else if (array.getBaseType() instanceof UserDefinedTypeStructure ) {
                        TypeStructure struct = (TypeStructure) (((UserDefinedTypeStructure)(array.getBaseType())).getStructuredType());
                        getStructureDeclarationsForStructure(struct.getStructureName(), struct,
                                structures);
                    }
                } else if (entry.getType() instanceof UserDefinedTypeStructure) {
                    TypeStructure struct = (TypeStructure) (((UserDefinedTypeStructure)(entry.getType())).getStructuredType());
                    getStructureDeclarationsForStructure(struct.getStructureName(), struct,
                            structures);
                }
            } else if (symbolTableEntry instanceof FormalParameter) {
                FormalParameter entry = (FormalParameter) symbolTableEntry;
                if (entry.getType() instanceof TypeStructure) {
                    TypeStructure struct = (TypeStructure) entry.getType();
                    getStructureDeclarationsForStructure(struct.getStructureName(), struct,
                            structures);
                } else if (entry.getType() instanceof TypeArrayDeclaration) {
                    TypeArrayDeclaration array = (TypeArrayDeclaration) entry.getType();
                    if (array.getBaseType() instanceof TypeStructure) {
                        TypeStructure struct = (TypeStructure) array.getBaseType();
                        getStructureDeclarationsForStructure(struct.getStructureName(), struct,
                                structures);
                    } else if (array.getBaseType() instanceof UserDefinedTypeStructure ) {
                        TypeStructure struct = (TypeStructure) (((UserDefinedTypeStructure)(array.getBaseType())).getStructuredType());
                        getStructureDeclarationsForStructure(struct.getStructureName(), struct,
                                structures);
                    }
                }
            } else if (symbolTableEntry instanceof UserDefinedType) {
                UserDefinedType entry = (UserDefinedType) symbolTableEntry;
                if (entry.getType() instanceof UserDefinedTypeStructure) {
                    TypeStructure struct = (TypeStructure) ((UserDefinedTypeStructure)(entry.getType())).getStructuredType();
                    getStructureDeclarationsForStructure(struct.getStructureName(), struct,
                            structures);
                }
            } else if (symbolTableEntry instanceof ProcedureEntry) {
                ProcedureEntry procedureEntry = (ProcedureEntry) symbolTableEntry;

                if (procedureEntry.getResultType() != null) {
                    if (procedureEntry.getResultType() instanceof TypeStructure) {
                        TypeStructure result = (TypeStructure) procedureEntry.getResultType();
                        structures.put(result.getStructureName(), result);
                    } else if (procedureEntry.getResultType() instanceof UserDefinedTypeStructure) {
                        TypeStructure result = (TypeStructure) (((UserDefinedTypeStructure)(procedureEntry.getResultType())).getStructuredType());
                        structures.put(result.getStructureName(), result);
                    }
                }
                if (((ProcedureEntry)symbolTableEntry).scope != null ) {
                    getStructureDeclarationsForSymboltable(((ProcedureEntry) symbolTableEntry).scope,
                            structures);
                }
            } else if (symbolTableEntry instanceof TaskEntry) {
                if (((TaskEntry)symbolTableEntry).scope != null ) {
                    getStructureDeclarationsForSymboltable(((TaskEntry) symbolTableEntry).scope,
                            structures);
                }
            } else if (symbolTableEntry instanceof BlockEntry) {
                getStructureDeclarationsForSymboltable(((BlockEntry) symbolTableEntry).scope,
                        structures);
            } else if (symbolTableEntry instanceof LoopEntry) {
                getStructureDeclarationsForSymboltable(((LoopEntry) symbolTableEntry).scope,
                        structures);
            } else if (symbolTableEntry instanceof LabelEntry ||
                       symbolTableEntry instanceof InterruptEntry ||
                       symbolTableEntry instanceof LengthEntry ||
                       symbolTableEntry instanceof SignalEntry){
                // nothing to do
            } else {
                ErrorStack.add(symbolTableEntry.getCtx(),"SymbolTable@560", "missing alternative");
            }
        }
    }

    private void getStructureDeclarationsForStructure(String name, TypeStructure structure,
            HashMap<String, TypeStructure> structures) {
        
        structures.put(name, structure);

        for (Iterator<StructureComponent> it = structure.m_listOfComponents.iterator(); it
                .hasNext();) {
            StructureComponent structureComponent = (StructureComponent) it.next();

            if (structureComponent.m_type instanceof TypeStructure) {
                TypeStructure struct = (TypeStructure) structureComponent.m_type;
                getStructureDeclarationsForStructure(struct.getStructureName(), struct, structures);
            } else if (structureComponent.m_type instanceof TypeArrayDeclaration) {
                TypeArrayDeclaration array = (TypeArrayDeclaration) structureComponent.m_type;

                if (array.getBaseType() instanceof TypeStructure) {
                    TypeStructure struct = (TypeStructure) array.getBaseType();
                    getStructureDeclarationsForStructure(struct.getStructureName(), struct,
                            structures);
                }
            } else if (structureComponent.m_type instanceof UserDefinedTypeStructure) {
                UserDefinedTypeStructure uts = (UserDefinedTypeStructure)(structureComponent.m_type);
                TypeStructure struct = (TypeStructure)(uts.getStructuredType());
                getStructureDeclarationsForStructure(struct.getStructureName(), struct, structures);
            }
        }
    }

    public int lookupDefaultFixedLength() {
        SymbolTableEntry entry = this.lookup("~LENGTH_FIXED~");

        if (entry != null) {
            if (entry instanceof LengthEntry) {
                LengthEntry e = (LengthEntry) entry;
                if (e.getType() instanceof TypeFixed) {
                    TypeFixed typ = (TypeFixed) e.getType();
                    return typ.getPrecision();
                }
            }
        } else {
            return Defaults.FIXED_LENGTH;
        }

        return -1;
    }

    public int lookupDefaultFloatLength() {
        SymbolTableEntry entry = this.lookup("~LENGTH_FLOAT~");

        if (entry != null) {
            if (entry instanceof LengthEntry) {
                LengthEntry e = (LengthEntry) entry;
                if (e.getType() instanceof TypeFloat) {
                    TypeFloat typ = (TypeFloat) e.getType();
                    return typ.getPrecision();
                }
            }
        } else {
            return Defaults.FLOAT_PRECISION;
        }

        return -1;
    }

    public int lookupDefaultCharLength() {
        SymbolTableEntry entry = this.lookup("~LENGTH_CHAR~");

        if (entry != null) {
            if (entry instanceof LengthEntry) {
                LengthEntry e = (LengthEntry) entry;
                if (e.getType() instanceof TypeChar) {
                    TypeChar typ = (TypeChar) e.getType();
                    return typ.getSize();
                }
            }
        } else {
            return Defaults.CHARACTER_LENGTH;
        }

        return -1;
    }

    public int lookupDefaultBitLength() {
        SymbolTableEntry entry = this.lookup("~LENGTH_BIT~");

        if (entry != null) {
            if (entry instanceof LengthEntry) {
                LengthEntry e = (LengthEntry) entry;
                if (e.getType() instanceof TypeBit) {
                    TypeBit typ = (TypeBit) e.getType();
                    return typ.getPrecision();
                }
            }
        } else {
            return Defaults.BIT_LENGTH;
        }

        return -1;
    }

    public int getNumberOfComponents(TypeStructure typ) {
        int numberOfComponents = 0;

        for (int i = 0; i < typ.m_listOfComponents.size(); i++) {
            TypeDefinition componentType = typ.m_listOfComponents.get(i).m_type;

            if (componentType instanceof TypeFixed) {
                numberOfComponents++;
            } else {
                numberOfComponents += getNumberOfComponents(componentType);
            }
        }

        return numberOfComponents;
    }

    public int getNumberOfComponents(TypeArrayDeclaration typ) {
        return typ.getTotalNoOfElements() * getNumberOfComponents(typ.getBaseType());
    }

    public int getNumberOfComponents(TypeDefinition typ) {
        if (typ instanceof TypeArrayDeclaration) {
            return getNumberOfComponents((TypeArrayDeclaration) typ);
        } else if (typ instanceof TypeStructure) {
            return getNumberOfComponents((TypeStructure) typ);
        } else {
            return 1;
        }
    }

    public void enterSystemPartName(SymbolTableEntry se) {
        m_systemPartNames.add(se);
    }

    public SymbolTableEntry lookupSystemPartName(String name) {
        SymbolTableEntry result = null;
        for (int i = 0; i < m_systemPartNames.size(); i++) {
            if (name.equals(m_systemPartNames.elementAt(i).getName())) {
                return m_systemPartNames.elementAt(i);
            }
        }
        return result;
    }

    private String toString(Vector<SymbolTableEntry> v) {
        String result = "SYSTEM part:\n";
        for (int i = 0; i < m_systemPartNames.size(); i++) {
            SystemPartName s = (SystemPartName) (m_systemPartNames.elementAt(i));
            result += s.toString(2) + "\n";
        }
        return result;
    }

    public void setUsesSystemElements() {
        m_usesSystemElements = true;
    }

    public boolean usesSystemElements() {
        return m_usesSystemElements;
    }

    public SymbolTable parent;
    private SymbolTable m_currentSymbolTable; // needed for traversion the complete symbol table
    protected HashMap<String, SymbolTableEntry> m_entries;
    public int m_level;
    private boolean m_usesSystemElements;
    private static Vector<SymbolTableEntry> m_loopsAndBlocks = new Vector<SymbolTableEntry>();
    private static Vector<SymbolTableEntry> m_systemPartNames = new Vector<SymbolTableEntry>();

}
