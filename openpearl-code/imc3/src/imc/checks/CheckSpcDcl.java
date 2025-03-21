package imc.checks;

import imc.types.DclProblemPart;
import imc.types.Module;
import imc.types.ModuleEntrySystemPart;
import imc.types.Platform;
import imc.types.PlatformSystemElement;
import imc.types.SpcProblemPart;
import imc.utilities.EvaluateNamedExpression;
import imc.utilities.Log;
import imc.utilities.NodeUtils;

import java.util.ArrayList;
import java.util.List;


import org.w3c.dom.Node;

/**
 * check for conflicts in global names
 * 
 * The GLOBAL attribute in <b>SPC</b> may specify from which <b>MODULE</b> the identifier should be
 * imported. The default is the current module, which is useful for system part elements.
 * 
 * The GLOBAL attribute in a <b>DCL</b> is defaulted by the compiler to the module name of the
 * definition.
 * 
 * Instead of iterating over the file based checks, we must iterate over all files which have the
 * same module_name attribute.
 * 
 * 
 * @author mueller
 *
 */
public class CheckSpcDcl {
    private List<Module> modules;
    private List<String> moduleNames;
    private Module currentModule;
    private boolean useNameSpace;

    public CheckSpcDcl(List<Module> modules, boolean useNameSpace) {
        this.modules = modules;
        this.useNameSpace = useNameSpace;
        moduleNames = new ArrayList<String>();

        // create a list of module names in the project
        for (Module m : modules) {
            String mn = m.getModuleName();
            if (moduleNames.contains(mn))
                continue;
            moduleNames.add(mn);
        }
    }
    

    /**
     * search if each specification in the problem part matches a declaration in the system or problem
     * part of this or other modules
     */
    public void spcHasOneDcl() {

        Log.info("SpcHasOneDcl started ...");
        moduleNames = new ArrayList<String>();
        for (Module m : modules) {
            currentModule = m;
            String modName = m.getModuleName();
            if (!moduleNames.contains(modName)) {
                moduleNames.add(modName);
            }
            for (SpcProblemPart spc : m.getSpcProblemPart()) {
                String userName = spc.getUserName();
                boolean dclFound = false;
                Log.setLocation(m.getSourceFileName(), spc.getLine(), spc.getCol());
                if (useNameSpace) {
                    Log.info("check SPC " + spc.getGlobal() + ":" + userName);
                } else {
                    Log.info("check SPC " + userName);
                }
                for (Module m1 : modules) {
                    int indexInSystemPart = isDefinedInSystemPart(m1, spc.getGlobal(), userName);
                    if (indexInSystemPart >= 0) {
                        dclFound = true;
                        Log.info(userName + " found in systempart of " + m1.getSourceFileName());
                        m1.getSystemElements().get(indexInSystemPart).setIsUsed();

                        checkType(spc, m1, indexInSystemPart);

                    } else {
                        Log.info(userName + "  is not in system part of " + m1.getSourceFileName());
                    }
                }
                // let's see if we find the name in any problem parts
                for (Module m1 : modules) {
                    // search only in modules with the same name and ignore the current module
                    if (m1 == m)
                        continue;
                    if (!m1.getModuleName().equals(spc.getGlobal()))
                        continue;
                    int indexInProblemPart = isDefinedInProblemPart(m1, spc.getGlobal(), userName);
                    if (indexInProblemPart >= 0) {
                        m1.getDclProblemPart().get(indexInProblemPart).setUsedBySpc();
                        dclFound = true;
                        Log.info(userName + " index in problempart " + indexInProblemPart + " of module "
                                + m1.getModuleName());
                        checkType(spc, m1.getDclProblemPart().get(indexInProblemPart));
                    } else {
                        Log.info(userName + "  is not in problem part of " + m1.getSourceFileName());
                    }
                }
                if (!dclFound) {
                    if (useNameSpace) {
                        Log.error(
                                "SPC of " + spc.getType() + " '" + userName + "' GLOBAL('" + spc.getGlobal() + "') has no declaration");
                    } else {
                        Log.error("SPC of " + spc.getType() + " '" + userName + "' has no declaration");
                    }
                }
            }
        }

    }

    public void duplicateDefinitions() {
        Log.info("duplicateDefinitions check started ...");
        boolean firstNamePrinted;
        // check problem part elements
        for (int i=0; i< modules.size(); i++) {
        	Module m=modules.get(i);
        	// check system names from current module
        	
            List<ModuleEntrySystemPart> mseList = m.getSystemElements();
            for (int sysIndex = 0; sysIndex < mseList.size(); sysIndex++) {
               	firstNamePrinted=false;
          
                if (mseList.get(sysIndex).getUserName() == null)
                    continue; // configurations have no username
                if (mseList.get(sysIndex).isDuplicateDefined()) continue;
                String userName = mseList.get(sysIndex).getUserName();
            	for (int j=i+1; j<modules.size(); j++) {
            		Module n = modules.get(j);
            		
                    // check if userName is defined in other system part
                    int indexInModeleN = isDefinedInSystemPart(n, n.getModuleName(), userName);
            		String inModule="";
    				if (useNameSpace) {
    					inModule = "in module '"+m.getModuleName()+"' ";
    				}

                    if (indexInModeleN >= 0 ) {
                    	if (!firstNamePrinted) {
                    		firstNamePrinted = true;
                    		Log.setLocation(m.getSourceFileName(), mseList.get(sysIndex).getLine(), mseList.get(sysIndex).getCol());
    						Log.error("'" + mseList.get(sysIndex).getUserName() + "' " + inModule + "multiple defined as SYSTEM element");
    						mseList.get(sysIndex).setDuplicateDefined();
                    	}
                		Log.setLocation(n.getSourceFileName(), n.getSystemElements().get(indexInModeleN).getLine(),
                				n.getSystemElements().get(indexInModeleN).getCol());
						Log.note("other definition " + inModule + "as SYSTEM element");
						n.getSystemElements().get(indexInModeleN).setDuplicateDefined();
                    }
                    // check if userName is defined in other problem part
                    List<DclProblemPart> dclList = n.getDclProblemPart();
                    indexInModeleN = isDefinedInProblemPart(n, n.getModuleName(), userName);
                    
                    if (indexInModeleN >= 0 ) {
                    	if (dclList.get(indexInModeleN).isDuplicateDefined()) continue;
                    	if (!firstNamePrinted) {
                    		firstNamePrinted = true;
                    		Log.setLocation(m.getSourceFileName(), mseList.get(sysIndex).getLine(), mseList.get(sysIndex).getCol());
    						Log.error("'" + mseList.get(sysIndex).getUserName() + "' " + inModule + "multiple defined as SYSTEM element");
    						mseList.get(sysIndex).setDuplicateDefined();
                    	}
                		Log.setLocation(n.getSourceFileName(), dclList.get(indexInModeleN).getLine(),
                				dclList.get(indexInModeleN).getCol());
						Log.note("other definition " + inModule + " with type "+ dclList.get(indexInModeleN).getType());
						dclList.get(indexInModeleN).setDuplicateDefined();
						firstNamePrinted = true;                    	
                    }

            	}
            }
            
            for (DclProblemPart dcl : m.getDclProblemPart()) {
               	
               	if (dcl.isDuplicateDefined()) continue;
               	firstNamePrinted=false;
                String userName = dcl.getUserName();
                
            	for (int j=i+1; j<modules.size(); j++) {
            		Module n = modules.get(j);
            		
                    // check if userName is defined in other system part
                    int indexInModeleN = isDefinedInSystemPart(n, n.getModuleName(), userName);
            		String inModule="";
    				if (useNameSpace) {
    					inModule = "in module '"+m.getModuleName()+"' ";
    				}

                    if (indexInModeleN >= 0 ) {
        
                    	if (!firstNamePrinted) {
                    		firstNamePrinted = true;
                    		Log.setLocation(m.getSourceFileName(), dcl.getLine(), dcl.getCol());
    						Log.error("'" + dcl.getUserName() + "' " + inModule + "multiple defined with type "+ dcl.getType());
    						dcl.setDuplicateDefined();
                    	}
                		Log.setLocation(n.getSourceFileName(), n.getSystemElements().get(indexInModeleN).getLine(),
                				n.getSystemElements().get(indexInModeleN).getCol());
                		Log.note("other definition " + inModule + "as SYSTEM element");
                		n.getSystemElements().get(indexInModeleN).setDuplicateDefined();
                    }
                    // check if userName is defined in other problem part
                    List<DclProblemPart> dclList = n.getDclProblemPart();
                    indexInModeleN = isDefinedInProblemPart(n, n.getModuleName(), userName);
                    if (indexInModeleN >= 0 ) {
                    	if (!firstNamePrinted) {
                    		firstNamePrinted = true;
                    		Log.setLocation(m.getSourceFileName(), dcl.getLine(), dcl.getCol());
    						Log.error("'" + dcl.getUserName() + "' " + inModule + "multiple defined with type "+ dcl.getType());
    						dcl.setDuplicateDefined();
                    	}
                		Log.setLocation(n.getSourceFileName(), dclList.get(indexInModeleN).getLine(),
                				dclList.get(indexInModeleN).getCol());
						Log.note("other definition " + inModule + "with type "+ dclList.get(indexInModeleN).getType());
						dclList.get(indexInModeleN).setDuplicateDefined();
						
						firstNamePrinted = true;                    	
                    }

            	}
            }
        }
   
    }

    public void dclHasSpc() {
        Log.info("SpcHasOneDcl started ...");

        // check problem part elements
        for (Module m : modules) {
            for (DclProblemPart dcl : m.getDclProblemPart()) {
                if (dcl.isUsedBySpc() || dcl.getGlobal()==null)
                    continue;
                Log.setLocation(m.getSourceFileName(), dcl.getLine(), dcl.getCol());
                Log.warn(dcl.getType() + " '" + dcl.getUserName() + "' is never used by SPC");
                dcl.setUsedBySpc();  // avoid duplicate error messages
            }
        }

    }
    

    public void warnUnusedDcl() {
        Log.info("warnUnusedDcl started ...");

        for (Module m : modules) {
            for (ModuleEntrySystemPart mse : m.getSystemElements()) {
                if (mse.getUserName() == null)
                    continue; // is configuration
                if (mse.isUsed())
                    continue;
                Log.setLocation(m.getSourceFileName(), mse.getLine(), mse.getCol());
                if (useNameSpace) {
                    Log.warn(
                            "'" + mse.getUserName() + "' in module '" + m.getModuleName() + "' is never used");
                } else {
                    Log.warn("'" + mse.getUserName() + "' is never used");
                }

            }

            for (DclProblemPart dcl : m.getDclProblemPart()) {
                if (dcl.isUsedBySpc() || dcl.getGlobal() == null) {
                	// skip used dation and not global dations
                    continue;
                }
                Log.setLocation(m.getSourceFileName(), dcl.getLine(), dcl.getCol());
                if (useNameSpace) {
                    Log.warn(
                            "'" + dcl.getUserName() + "' in module '" + m.getModuleName() + "' is never used");
                } else {
                    Log.warn("'" + dcl.getUserName() + "' is never used");

                }
            }
        }
    }

    private void checkType(SpcProblemPart spc, DclProblemPart dcl) {
        if (!spc.getType().equals(dcl.getType())) {
            Log.setLocation(spc.getModule().getSourceFileName(), spc.getLine(), spc.getCol());
            

            Log.error("type mismatch: expected in SPC: of '" + spc.getUserName() + "' as type '"
                    +spc.getType() + "'");
            Log.note(dcl.getModule().getSourceFileName(), dcl.getLine(), dcl.getCol(),
                    "found in DCL :'" + dcl.getType() + "'");
            return;
        }
        if (spc.getType().equals(Platform.DATION)) {
//            System.out.println("spc="+spc);
//            System.out.println("dcl="+dcl);
//            System.out.println("spc.getDationType()="+spc.getDationType());
//            System.out.println("dcl.getDationType()="+dcl.getDationType());
            
            if (!spc.getDationType().equals(dcl.getDationType())) {
                Log.error("mismatch in type of transfer data of '" + spc.getUserName()
                + "' expected in SPC: " + spc.getDationType() + "'");
                Log.note(dcl.getModule().getSourceFileName(), dcl.getLine(), dcl.getCol(),
                        "type in DCL: " + dcl.getDationType());
            }
            if (!spc.getAttributes().equals(dcl.getAttributes())) {
                Log.error("mismatch in DATION attributes of '" + spc.getUserName() + "'"
                        + spc.getAttributes());
                Log.note(dcl.getModule().getSourceFileName(), dcl.getLine(), dcl.getCol(),
                        "attributes in DCL " + dcl.getAttributes());
            }
        } else if (spc.getType().equals("PROC")) {
        	if (spc.getProcParameters() == null && dcl.getProcParameters() != null) {
                    Log.error("mismatch in parameter list of PROC '" + spc.getUserName() + "' expected in SPC: "
                            + " - none -" + "'");
                    Log.note(dcl.getModule().getSourceFileName(), dcl.getLine(), dcl.getCol(),
                            "parameter list of PROC in DCL: " + dcl.getProcParameters());
        	} else if (spc.getProcParameters() != null && dcl.getProcParameters() == null) {
                Log.error("mismatch in parameter list of PROC '" + spc.getUserName() + "' expected in SPC: "
                        + spc.getProcParameters()+"'");
                Log.note(dcl.getModule().getSourceFileName(), dcl.getLine(), dcl.getCol(),
                        "parameter list of PROC in DCL: " + " - none -");
        	}
        	
            if (spc.getProcParameters() != null && !spc.getProcParameters().equals(dcl.getProcParameters())) {
                Log.error("mismatch in parameter list of PROC '" + spc.getUserName() + "' expected in SPC: "
                        + spc.getProcParameters() + "'");
                Log.note(dcl.getModule().getSourceFileName(), dcl.getLine(), dcl.getCol(),
                        "parameter list of PROC in DCL: " + dcl.getProcParameters());
            }

            if (spc.getProcReturns() == null && dcl.getProcReturns() != null) {
            	 Log.error("type mismatch in result type of PROC '" + spc.getUserName()
                 + "' does not return a value");
            	 Log.note(dcl.getModule().getSourceFileName(), dcl.getLine(), dcl.getCol(),
                         "result type in declaration of PROC '" + dcl.getUserName() + "' is '"
                                 + dcl.getProcReturns() + "'");
            } else if (spc.getProcReturns() != null && dcl.getProcReturns() == null) {
                Log.error("type mismatch in result type of PROC '" + spc.getUserName() + "' RETURNS '"
                        + spc.getProcReturns() + "'");
                
            } else    if (spc.getProcReturns() != null && !(spc.getProcReturns().equals(dcl.getProcReturns()))) {
                    Log.error("type mismatch in result type of PROC '" + spc.getUserName() + "' RETURNS '"
                            + spc.getProcReturns() + "'");

                    Log.note(dcl.getModule().getSourceFileName(), dcl.getLine(), dcl.getCol(),
                            "declaration of PROC '" + dcl.getUserName() + "' does not return a value");
            }
        }
    }

    private void checkType(SpcProblemPart spc, Module m1, int  indexInSystemPart) {
    	ModuleEntrySystemPart moduleEntrySystemPart = m1.getSystemElements().get(indexInSystemPart);
        Log.setLocation(currentModule.getSourceFileName(), spc.getLine(), spc.getCol());
		
        String inModule="";

		
        PlatformSystemElement pse =
                Platform.getInstance().getSystemElement(moduleEntrySystemPart.getNameOfSystemelement());

        if (!spc.getType().equals(pse.getType())) {
    		if (useNameSpace) {
    			inModule = "in module '"+spc.getGlobal()+"' ";
    		}

            Log.error("'"+spc.getUserName() + "' " + inModule + " specified as " + spc.getType());
            Log.setLocation( m1.getSourceFileName(), moduleEntrySystemPart.getLine(), moduleEntrySystemPart.getCol());
    		if (useNameSpace) {
    			inModule = "in module '"+m1.getModuleName()+"' ";
    		}
            Log.note("definition in SYSTEM part as '" + pse.getType()+"'");
            return;
        }
        if (spc.getType().equals(Platform.DATION)) {
            // Dations must check attributes for compatibility and data for equality
            // data may be an expression in the platform side
            Node inPlatform = pse.getNode();
            Node dationInSpc = spc.getLocationInDomTree();

            Node n = imc.utilities.NodeUtils.getChildByName(inPlatform, "data");
            if (n == null) {
                Log.internalError("no 'data' for dation '" + pse.getSystemName() + "'");
                return;
            } else {
                String dataInPlatform = n.getTextContent();
                EvaluateNamedExpression expr =
                        new EvaluateNamedExpression(moduleEntrySystemPart.getParameters());
                dataInPlatform = expr.evaluateExpression(dataInPlatform);

                n = NodeUtils.getChildByName(dationInSpc, "data");
                String dataInSpc = n.getTextContent();

                boolean dataTypeMatch = false;

                if (dataInPlatform.equals("ALL")) {
                    dataTypeMatch = true;
                }
                if (dataInPlatform.equals("ALPHIC")) {
                    if (dataInSpc.trim().equals("CHAR")) {
                        dataTypeMatch = true;
                    }
                    if (dataInSpc.trim().equals("ALPHIC")) {
                        dataTypeMatch = true;
                    }
                }

                if (dataInPlatform.trim().equals(dataInSpc.trim())) {
                    dataTypeMatch = true;
                }
                if (!dataTypeMatch) {
                    Log.error("conflict in type of transfer data: SPC of '" + spc.getUserName()
                    + "' has type '" + dataInSpc + "'");
                    Log.note(m1.getSourceFileName(), moduleEntrySystemPart.getLine(),
                            moduleEntrySystemPart.getCol(), "declaration in SYSTEM part for '" + spc.getUserName()
                            + "' specifies type '" + dataInPlatform + "'");
                }


            }
            // check attributes

            String attributesInSpc = NodeUtils.getChildByName(dationInSpc, "attributes").getTextContent();
            // System.out.println("SPC attr:" + attributesInSpc);
            // check if SYSTEM is given
            if (! attributesInSpc.contains("SYSTEM")) {
            	Log.error("'" + spc.getUserName()+"' is defined in SYSTEM part; required attribute SYSTEM missing");
            	return;
            }
            String attributesInPlatform =
                    NodeUtils.getChildByName(inPlatform, "attributes").getTextContent();
            // System.out.println("DCL attr:" + attributesInDevice);

            String[] attrListInSpc = attributesInSpc.split(",");

            for (int i = 0; i < attrListInSpc.length; i++) {
                if (!attributesInPlatform.contains(attrListInSpc[i].trim())) {
                    Log.error(
                            "dation attribute '" + attrListInSpc[i].trim() + "' not supported by system device");
                    Log.note(m1.getSourceFileName(), moduleEntrySystemPart.getLine(),
                            moduleEntrySystemPart.getCol(), moduleEntrySystemPart.getNameOfSystemelement()
                            + " supports " + attributesInPlatform.trim());
                }
            }
        }
    }

    /**
     * search the given name in the system part of the specified module
     * 
     * @param m the module in which the search is done
     * @param userName the user name
     * @return the index in the list, or -1 if not in the list
     */
    private int isDefinedInSystemPart(Module m, String globalOfUserName, String userName) {
        List<ModuleEntrySystemPart> mseList = m.getSystemElements();
        for (int i = 0; i < mseList.size(); i++) {
            if (useNameSpace) {
                if (!m.getModuleName().equals(globalOfUserName))
                    continue;
            }
            if (mseList.get(i).getUserName() == null)
                continue; // configurations have no username
            if (mseList.get(i).getUserName().equals(userName))
                return i;
        }
        return -1;
    }

    /**
     * search the given name in the problem part of the specified module
     * 
     * @param m the module in which the search is done
     * @param userName the user name
     * @return the index in the list, or -1 if not in the list
     */
    private int isDefinedInProblemPart(Module m, String globalOfUserName, String userName) {
        List<DclProblemPart> dclList = m.getDclProblemPart();
        for (int i = 0; i < dclList.size(); i++) {
        	String globalOfDclElement = dclList.get(i).getGlobal(); 
            if (globalOfDclElement == null) {
            	continue;  // ignore non global objects
            }
            if (useNameSpace) {
                if (!globalOfDclElement.equals(globalOfUserName)) {
                    continue;	// ignore different name
                }
            }
            if (dclList.get(i).getUserName().equals(userName))
                return i;
        }
        return -1;
    }



}
