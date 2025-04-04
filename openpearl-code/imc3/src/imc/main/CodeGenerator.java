package imc.main;
/*
 [A "BSD license"]
 Copyright (c) 2016 Rainer Mueller
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import imc.types.Association;
import imc.types.Module;
import imc.types.ModuleEntrySystemPart;
import imc.types.Platform;
import imc.types.PlatformSystemElement;
import imc.utilities.Log;
import imc.utilities.NodeUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.w3c.dom.Node;




/**
 * create C++ code for the detected elements
 * 
 * The order of definitions in the source code does not strictly depend on the sequence in the PEARL
 * code. The order is dominated by the sequence of definition, which is overwritten if forward usage
 * is detected.
 * 
 * @author mueller
 * 
 */

/**
 * The code generator class creates the C++-file with the system information
 * 
 * OpenPEARL make an intensive use of static objects. The sequence instantiation of these objects in
 * not defined across compilation units. User dation on module level are static objects, which need
 * an already instantiated system device. To ensure the instantiation of these system device, they
 * must be defined static in an simple function with is called to initialize a simple variable in
 * the user module like:
 * 
 * <pre>
 * int createSystemElements();
 * </pre>
 * 
 * The file with the system part consists of the following elements:
 * <ol>
 * <li>the definition of the exported identifiers to the problem parts
 *     with generic types and decorated names
 * <li>simple object definitions and pointer to system devices
 * <li>the function 'createSystemElements()', which
 *    <ul>
 *    <li>creates static objects with the required parameters
 *    <li>sets the exported pointers to the created objects
 *    </ul> 
 * 
 * part 2 and 4 are created in strings and printed, when the system information is processed
 * 
 * @author mueller
 * 
 */
public class CodeGenerator {
    private static StringBuilder prototypes = new StringBuilder();
    private static StringBuilder functionBody = new StringBuilder();
    private static Module module;
    private static boolean useNamespace;
    private static int autoNumber = 0;

    /**
     * create to output file with to complete content
     * 
     * if errors like loops in definition are detected, an error message occurs
     */
    public static void create(String outputFile, List<Module> modules, boolean useNamespace) {
        CodeGenerator.useNamespace = useNamespace;
        PrintWriter file;

        try {
            file = new PrintWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            System.err.println("could not create output file");

            e.printStackTrace();
            return;
        }
        file.println("// [automatic generated by intermodule checker (imc v3) -- do not change ]");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        file.println("// " + dateFormat.format(date));
        file.println("// use of namespace is: " + useNamespace + "\n");
        file.println("#include \"PearlIncludes.h\" \n");

        for (int i = 1; i < 256; i++) {
            instantiate(modules, i);
        }



        file.println();
        file.println(prototypes);
        file.println("\n\nnamespace pearlrt {\n   int createSystemElements() {");
        file.println("\tint nbrOfFails = 0;\n");
        file.println("\ttry {");
        file.println(functionBody);
        file.println("\t} catch (pearlrt::Signal &s) {");
        file.println("\t  nbrOfFails ++;\n\t}\n");
        file.println("\treturn nbrOfFails;");
        file.println("   }\n}");
        file.close();

    }

    private static String addNsPrefix(String namespace) {
        return "ns_"+namespace;
    }
    
    private static void instantiate(List<Module> modules, int priority) {
        boolean nameSpaceSet = false;

        for (Module m : modules) {
            String nameSpace;

            module = m;
            if (useNamespace) {
                nameSpace = module.getModuleName();
            } else {
                nameSpace = "pearlApp";
            }


            for (ModuleEntrySystemPart se : m.getSystemElements()) {
                String sysname = se.getNameOfSystemelement();

                PlatformSystemElement n = Platform.getInstance().getSystemElement(sysname);
                if (n.getPriority() == priority) {
                    if (!nameSpaceSet) {
                        prototypes.append("namespace " + addNsPrefix(nameSpace) + " {\n");
                        nameSpaceSet = true;
                    }
                    doAllPrerequisites(se, 10); // max depth of recursion is 10
                }
            }
            if (nameSpaceSet) {
                prototypes.append("}\n\n");
                nameSpaceSet = false;
            }
        }
    }

    private static void doAllPrerequisites(ModuleEntrySystemPart se, int remainingDepth) {
        Log.setLocation(module.getSourceFileName(), se.getLine(), se.getCol());
        Log.info("treat " + se.getNameOfSystemelement());
        if (se.isCodeGenerated()) {
            return;
        }
        if (remainingDepth >= 0) {
            Association a = se.getAssociation();
            if (a == null) {
                se.setCodeGenerated(true);
                addToCodeParts(se);

            }

            if (a != null) {
                ModuleEntrySystemPart mse = a.getUsername();
                if (!mse.isCodeGenerated()) {
                    doAllPrerequisites(mse, remainingDepth - 1);
                    mse.setCodeGenerated(true);
                }
                addToCodeParts(se);
                se.setCodeGenerated(true);
            }

        } else {
            Log.error("too long chain of associations");
        }
    }


    private static void addToCodeParts(ModuleEntrySystemPart se) {


        String userName;
        if (se.getUserName() != null) {
            userName = se.getPrefix() + se.getUserName();
        } else {
            userName = "configuration_" + autoNumber++;
        }

        String s = se.getNameOfSystemelement();
        PlatformSystemElement pse = Platform.getInstance().getSystemElement(s);
        String nameSpacePrefix = "";
        if (useNamespace) {
            nameSpacePrefix = module.getModuleName() + "::";
        } else {
            nameSpacePrefix = "pearlApp::";
        }

        if (pse.getType().equals(Platform.DATION)) {
            Node nodeSe = Platform.getInstance().getNodeOfSystemname(s);
            Node attribute = NodeUtils.getChildByName(nodeSe, "attributes");
            String attributes = attribute.getTextContent();
            if (attributes.contains("BASIC")) {
                prototypes.append("\tpearlrt::SystemDationB * " + userName + ";" + locationComment(se));
            } else {
                prototypes.append("\tpearlrt::SystemDationNB *  " + userName + ";" + locationComment(se));
            }
            

            functionBody.append("\t  // " + module.getSourceFileName() + ":" + se.getLine() + "\n");
            functionBody.append("\t  static pearlrt::" + se.getNameOfSystemelement() + " "
                    /* + nameSpacePrefix */ + "s" + userName + parameterList(se) + ";\n");
            functionBody.append("\t  " + addNsPrefix(nameSpacePrefix) + userName + "= &" + /* nameSpacePrefix + */ "s"
                    + userName + ";\n\n");

        } else if (pse.getType().equals(Platform.CONNECTION)) {

            functionBody.append("\t  // " + module.getSourceFileName() + ":" + se.getLine() + "\n");
            functionBody.append("\t  static pearlrt::" + se.getNameOfSystemelement() + " "
                    /* + nameSpacePrefix*/ + "s" + userName + parameterList(se) + ";\n");
        } else if (pse.getType().equals(Platform.CONFIGURATION)) {
            functionBody.append("\t  // " + module.getSourceFileName() + ":" + se.getLine() + "\n");
            functionBody.append("\t  static pearlrt::" + se.getNameOfSystemelement() + " "
                    /*+ nameSpacePrefix*/ + userName + parameterList(se) + ";\n");
        } else if (pse.getType().equals(Platform.INTERRUPT)) {
            prototypes.append("\tpearlrt::Interrupt *" + userName + ";" + locationComment(se));

            functionBody.append("\t  // " + module.getSourceFileName() + ":" + se.getLine() + "\n");
            functionBody.append("\t  static pearlrt::" + se.getNameOfSystemelement() + " "
                    /* + nameSpacePrefix */ + "sys" + userName + parameterList(se) + ";\n");
            functionBody.append("\t  " + addNsPrefix(nameSpacePrefix) + userName + "= (pearlrt::Interrupt*) &"
                    /* + nameSpacePrefix */ + "sys" + userName + ";\n");
        } else if (pse.getType().equals(Platform.SIGNAL)) {
        	prototypes.append("\tpearlrt::Signal * "+ " " /* + nameSpacePrefix*/
                  + userName + " = & pearlrt::the"+se.getNameOfSystemelement() + ";" + locationComment(se));
//            prototypes.append("\tpearlrt::Signal *" + " " /* + nameSpacePrefix*/
//                    + "generalized" + userName + ";" + locationComment(se));
//            functionBody.append("\t  // " + module.getSourceFileName() + ":" + se.getLine() + "\n");
//            functionBody.append("\t  pearlrt:: " +se.getNameOfSystemelement() + userName + ";\n" );
//            functionBody.append("\t  "+ addNsPrefix(nameSpacePrefix) + "generalized" + userName + "= & "
//                    /* + nameSpacePrefix*/ + userName + ";\n\n");
        } else {
            Log.error("unsupported type: " + pse.getType());
        }


    }

    private static String locationComment(ModuleEntrySystemPart se) {
        String s = "\t\t// " + module.getSourceFileName() + ":" + se.getLine() + "\n";
        return s;
    }



    private static String parameterList(ModuleEntrySystemPart se) {
        StringBuilder sb = new StringBuilder();
        Association a = se.getAssociation();
        boolean firstParameter = true;
        if (se.getParameters().size() > 0 || se.getAssociation() != null) {
            sb.append("(");

            if (a != null) {
                ModuleEntrySystemPart mse = a.getUsername();
                sb.append("& s" + mse.getPrefix() + mse.getUserName());
                firstParameter = false;
            }
            for (int i = 0; i < se.getParameters().size(); i++) {
                if (!firstParameter)
                    sb.append(", ");
                sb.append(se.getParameters().get(i).getCppCode());
                firstParameter = false;
            }

            sb.append(")");
            // } else if (se.getAssociation() == null) {
            // sb.append("()");
        }
        return sb.toString();
    }

}
