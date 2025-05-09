package imc.types;
/*
 * [A "BSD license"] Copyright (c) 2016 Rainer Mueller All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. 3. The name of the author may not be used
 * to endorse or promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import imc.main.ReadXml;
import imc.utilities.*;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * read xml definition for module import/export and provide specific operations, like search for
 * names as well as required and valid parameters
 * 
 * The exported and imported elements are added to lists like dictionaris to get an easy lookup of
 * the elements
 * 
 * Lists:
 * <ul>
 * <li>ModuleEntrySystemPart contain the elements from the <bf>SYSTEM</bd> section of the PEARL code
 * <li>SpcDclProblemPart contains the (GLOBAL) definitions and imports from the <bf>PROBLEM</bf>
 * part of the PEARL code
 * </ul>
 * 
 * 
 */

public class Module {
    String indent = "";
    Document moduleXML;
    boolean verbose;
    private String sourceFileName;
    ReadXml wrappedDomTree;
    int line;
    int col;
    String moduleName;
    boolean useNameSpace; 

    List<ModuleEntrySystemPart> systemElements;
    List<SpcProblemPart> spcProblemPart;
    List<DclProblemPart> dclProblemPart;
    List<UserDationTfuInformation> userDationTfuInformations;



    /**
     * create an object for the query operations for the target platform elements, like SIGNAL, DATION
     * and INTERRUPT in one source file
     * 
     * Several Module objects with the same moduleName may exist
     * 
     * @param fileName platform definition file name
     * @param verbose flag for verbose output; if true lot of messages are sent to System.out
     */
    public Module(String fileName, boolean verbose, boolean useNameSpace) {
        Log.info("Module: parse " + fileName);
        this.useNameSpace = useNameSpace;
        this.verbose = verbose;
        Log.setLocation(fileName, -1, -1);

        wrappedDomTree = new ReadXml(fileName, verbose, null);

        moduleXML = wrappedDomTree.getDocument(); // readXMLDocumentFromFile(fileName);
        if (moduleXML == null) {
            Log.internalError("error reading module definition file (" + fileName + ")");
            System.exit(1);
            return;
        }

        if (verbose) {
            wrappedDomTree.dumpDomTree();
        }

        Node m = NodeUtils.getChildByName(moduleXML, "module");
        sourceFileName = m.getAttributes().getNamedItem("file").getTextContent();
        moduleName = m.getAttributes().getNamedItem("name").getTextContent();

        Node std = m.getAttributes().getNamedItem("standard");
        if (std != null) {
            String standard= std.getTextContent();

            if (useNameSpace && !standard.equals("-std=OpenPEARL")) { 
                Log.setLocation(sourceFileName, line,  col);
                Log.error("file "+sourceFileName + " compiled not with -std=OpenPEARL");
                return;
            }
            if ((!useNameSpace) && !standard.equals("-std=PEARL90")) { 
            }
        } else {
            Log.setLocation(sourceFileName, line,  col);
            Log.error("file "+sourceFileName + " missing attribute 'standard' in module-tag");
            return;

        }

        systemElements = new ArrayList<ModuleEntrySystemPart>();
        readSystemPart();

        spcProblemPart = new ArrayList<SpcProblemPart>();
        dclProblemPart = new ArrayList<DclProblemPart>();
        userDationTfuInformations = new ArrayList<UserDationTfuInformation>();
        readProblemPart();
    }

    /**
     * check existence and type of used system names, associations and configuration elements on the
     * target platform *
     * <p>
     * <b>Note:</b> the method emits error messages in case of problems. The check is aborted by the
     * main program, if errors are detected in one stage of translation
     */

    private void readSystemPart() {
        if (verbose) {
            // wrappedDomTree.dumpDomTree();
        }

        NodeList se = moduleXML.getElementsByTagName("system");
        if (se == null) {
            return;
        }

        // get all defined system elements
        try {
            se = se.item(0).getChildNodes();
        } catch (NullPointerException e) {
            return;
        }

        if (se.getLength() > 0) {
            for (int i = 0; i < se.getLength(); i++) {
                Node n = se.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {

                    if (n.getNodeName().equals("username")) {
                        line = Integer.parseInt(n.getAttributes().getNamedItem("line").getTextContent());
                        col = Integer.parseInt(n.getAttributes().getNamedItem("col").getTextContent());
                        String userName = n.getAttributes().getNamedItem("name").getTextContent();
                        ModuleEntrySystemPart un = new ModuleEntrySystemPart(userName, line, col, n);
                        systemElements.add(un);
                        Log.setLocation(sourceFileName, line, col);
                        Log.info("module " + moduleName + " defines system element: " + userName);
                    } else if (n.getNodeName().equals(Platform.CONFIGURATION.toLowerCase())) {
                        line = Integer.parseInt(n.getAttributes().getNamedItem("line").getTextContent());
                        col = Integer.parseInt(n.getAttributes().getNamedItem("col").getTextContent());
                        ConfigurationSystemPart cnf = new ConfigurationSystemPart(line, col, n);
                        systemElements.add(cnf);
                        Log.setLocation(sourceFileName, line, col);
                        Log.info("module " + moduleName + " defines a configuration");
                    } else {
                        continue;
                    }
                }
            }
        }
    }



    /**
     * read the names, types of the problem part
     */
    private void readProblemPart() {
        NodeList problemElements = moduleXML.getElementsByTagName("problem");

        if (problemElements == null) {
            // no problem part
            return;
        }
        // get all defined system elements
        try {
            problemElements = problemElements.item(0).getChildNodes();
        } catch (NullPointerException e) {
            // no problem part
            return;
        }
        if (problemElements.getLength() > 0) {
            // search desired signal in the text section of concrete
            // signal-entry
            for (int i = 0; i < problemElements.getLength(); i++) {
                Node n = problemElements.item(i); // .getChildNodes();
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    boolean isDcl;

                    if (n.getNodeName().equals("spc")) {
                        isDcl = false;
                    } else if (n.getNodeName().equals("dcl")) {
                        isDcl = true;
                    } else if (n.getNodeName().equals("tfuusage")) {
                        treatTfuUsage(n);
                        continue;
                    } else {
                        System.out.println("node name >" + n.getNodeName() + "< unexpected and discarded");
                        continue; // ignore entry
                    }
                    String userName = n.getAttributes().getNamedItem("name").getTextContent();
                    line = Integer.parseInt(n.getAttributes().getNamedItem("line").getTextContent());
                    col = Integer.parseInt(n.getAttributes().getNamedItem("col").getTextContent());
                    String type = n.getAttributes().getNamedItem("type").getTextContent().toUpperCase();

                    // remove spaces at begin and end and replace multiple whitespaces by one space
                    type = type.trim().replaceAll("\\s+", " ");

                    String global = null; // moduleName;
                    

                   if (n.getAttributes().getNamedItem("global") != null) {
                        global = n.getAttributes().getNamedItem("global").getTextContent();
                   }

                    Log.setLocation(sourceFileName, line, col);

                    String dationData = null, dationAttributes = null, procParameters = null,
                            procReturns = null, taskAttribute=null;

                    if (type.equals(Platform.DATION)) {
                        // parse type and attributes of dation and set them in the dictionary
                        
                        for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                            Node m = n.getChildNodes().item(j);
                            if (m.getNodeType() != Node.ELEMENT_NODE)
                                continue;

                            if (m.getNodeName().equals("data")) {
                                //dationData = m.getNodeValue();
                                dationData = m.getTextContent();
                                dationData = dationData.replaceAll("\\s","");
                            }
                            if (m.getNodeName().equals("attributes")) {
                                //dationAttributes = m.getNodeValue();
                                dationAttributes = m.getTextContent();
                                dationAttributes = dationAttributes.replaceAll("\\s","");
                            }

                        }
                    } else if (type.equals("PROC")) {
                        // parse special elements of PROC
                        // parameters and returns
                        for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                            Node m = n.getChildNodes().item(j);
                            if (m.getNodeType() != Node.ELEMENT_NODE)
                                continue;
                            if (m.getNodeName().equals("parameters")) {
                                procParameters = m.getTextContent();
                            }
                            if (m.getNodeName().equals("returns")) {
                                procReturns = m.getTextContent();
                            }

                        }
                    } else if (type.equals("TASK") && isDcl) {
                        // parse special elements of TASK: here "MAIN"
                    	Node attr = n.getAttributes().getNamedItem("attr");
                    	if (attr != null) {
                    		taskAttribute = attr.getTextContent().toUpperCase();
                    	}
                                           	
                    }

                    if (isDcl) {
                        DclProblemPart dcl = new DclProblemPart(userName, line, col, type, n, this, global);
                        dclProblemPart.add(dcl);
                        if (dationAttributes!=null) dcl.setAttributes(dationAttributes);
                        if (taskAttribute!=null) dcl.setAttributes(taskAttribute);
                        dcl.setDationType(dationData);
                       
                        dcl.setProcParameters(procParameters);
                        dcl.setProcReturns(procReturns);
                        Log.info("DCL found in module '" + moduleName + "' type= '" + type + "' '" + userName
                                + "' GLOBAL('" + global + "')");
                    } else {
                        SpcProblemPart spc = new SpcProblemPart(userName, line, col, type, n, this, global);
                        spcProblemPart.add(spc);
                        spc.setAttributes(dationAttributes);
                       
                        spc.setDationType(dationData);
                        spc.setProcParameters(procParameters);
                        spc.setProcReturns(procReturns);
                        Log.info("SPC found in module '" + moduleName + "' type= '" + type + "' '" + userName
                                + "' GLOBAL('" + global + "')");
                    }


                }
            }

        }

    }

    private void treatTfuUsage(Node n) {
        for (int i = 0; i < n.getChildNodes().getLength(); i++) {
            Node m = n.getChildNodes().item(i);
            if (m.getNodeType() == Node.ELEMENT_NODE) {
                if (m.getNodeName().equals("userdation")) {
                    String username = NodeUtils.getAttributeByName(m, "name");
                    Node t = NodeUtils.getChildByName(m, "usedTFU");
                    int tfuSize = -1;
                    if (t != null) {
                        String tfu = NodeUtils.getAttributeByName(t, "size");
                        if (tfu != null) {
                            tfuSize = Integer.parseInt(tfu);
                        }
                    }

                    int line = Integer.parseInt(m.getAttributes().getNamedItem("line").getTextContent());
                    int col = Integer.parseInt(m.getAttributes().getNamedItem("col").getTextContent());

                    userDationTfuInformations
                    .add(new UserDationTfuInformation(username, line, col, m, this, "", tfuSize));
                    Log.setLocation(sourceFileName, line, col);
                    Log.info("module " + moduleName + " defines a user dation '" + username
                            + "' with tfuSize " + tfuSize);
                }
            }
        }
        // TODO Auto-generated method stub

    }

    public List<ModuleEntrySystemPart> getSystemElements() {
        return systemElements;
    }

    public List<SpcProblemPart> getSpcProblemPart() {
        return spcProblemPart;
    }

    public List<DclProblemPart> getDclProblemPart() {
        return dclProblemPart;
    }

    public List<UserDationTfuInformation> getUserDationTfuInformations() {
        return userDationTfuInformations;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }


}
