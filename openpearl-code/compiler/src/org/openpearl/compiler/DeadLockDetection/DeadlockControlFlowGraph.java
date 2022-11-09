// hfujk

package org.openpearl.compiler.DeadLockDetection;

//import com.eclipsesource.json.Json;
//import com.eclipsesource.json.JsonArray;
import javax.json.*;

import org.openpearl.compiler.Compiler;
import org.openpearl.compiler.ControlFlowGraph.ControlFlowGraph;
import org.openpearl.compiler.ControlFlowGraph.ControlFlowGraphNode;
import org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DeadlockControlFlowGraph
{
    public static Map<String, AbstractControlFlowGraphEntity> cfgTokens = new HashMap<>();

    public static void setTokenData(String tokenIdentifier, AbstractControlFlowGraphEntity labelData)
    {
        cfgTokens.put(tokenIdentifier, labelData);
    }

    public static AbstractControlFlowGraphEntity getTokenData(String tokenIdentifier)
    {
        return cfgTokens.get(tokenIdentifier);
    }

    public static void generate(List<ControlFlowGraph> controlFlowGraphList, String outputFilePath)
    {
        int uniqueId = 0;
        Map<ControlFlowGraphNode, Integer> nodeIdMap = new HashMap<>();
        //if (verbose > 0) {
            System.out.println("Generating DeadlockControlGraph file " + outputFilePath);
        //}
        try
        {
            FileWriter writer = new FileWriter(outputFilePath);

            writer.write("digraph G {\n");

            ControlFlowDataWrapper controlFlowDataWrapper = new ControlFlowDataWrapper();

            for (ControlFlowGraph cfg : controlFlowGraphList)
            {
                controlFlowDataWrapper.reset();

                writer.write("    subgraph cluster" + (uniqueId++) + " {\n");

                writer.write("        label = \"" + cfg.getName() + "\"\n");

                if (cfg.getName().startsWith("Module: "))
                {
                    for (DeadlockControlFlowGraphProcedure dcfgProcedure : Compiler.procedureDeclarations)
                    {
                        controlFlowDataWrapper.reset();

                        controlFlowDataWrapper.set("class", "DeadlockControlFlowGraphProcedure")
                                .set("codeFilename", dcfgProcedure.codeFilename)
                                .set("codeLineNumber", dcfgProcedure.codeLineNumber)
                                .set("procedureIdentifier", dcfgProcedure.procedureIdentifier)
                                .set("global", dcfgProcedure.global);

                        writer.write("        node" + (uniqueId++) + " [ label=\"" + controlFlowDataWrapper.get() + "\" ]\n");
                    }

                    for (DeadlockResourceDeclaration deadlockResourceDeclaration : Compiler.deadlockResourceDeclarations)
                    {
                        controlFlowDataWrapper.reset();

                        controlFlowDataWrapper.set("class", "DeadlockResourceDeclaration")
                                .set("codeFilename", deadlockResourceDeclaration.codeFilename)
                                .set("codeLineNumber", deadlockResourceDeclaration.codeLineNumber)
                                .set("resourceType", deadlockResourceDeclaration.resourceType)
                                .set("resourceIdentifier", deadlockResourceDeclaration.resourceIdentifier)
                                .set("presetValue", deadlockResourceDeclaration.presetValue)
                                .set("global", deadlockResourceDeclaration.global);

                        writer.write("        node" + (uniqueId++) + " [ label=\"" + controlFlowDataWrapper.get() + "\" ]\n");
                    }
                }
                else
                {
                    for (ControlFlowGraphNode controlFlowGraphNode : cfg.getGraph())
                    {
                        controlFlowDataWrapper.reset();

                        nodeIdMap.put(controlFlowGraphNode, uniqueId);

                        String statement = controlFlowGraphNode.getStatement();

                        String tokenIdentifier = "";

                        String defaultFilename = Compiler.getSourceFilename();
                        int defaultCodeLineNumber = 0;

                        if ((controlFlowGraphNode.getCtx() != null) && (controlFlowGraphNode.getCtx().getStart() != null))
                        {
                            tokenIdentifier = DeadlockControlFlowGraph.getTokenIdentifier(controlFlowGraphNode.getCtx().getStart());
                            defaultCodeLineNumber = controlFlowGraphNode.getCtx().getStart().getLine();
                        }

                        if (!tokenIdentifier.isEmpty())
                        {
                            AbstractControlFlowGraphEntity graphLabelData = getTokenData(tokenIdentifier);

                            if (graphLabelData != null)
                            {
                                controlFlowDataWrapper
                                    .set("codeFilename", graphLabelData.codeFilename)
                                    .set("codeLineNumber", graphLabelData.codeLineNumber);

                                if (graphLabelData instanceof DeadlockOperation)
                                {
                                    controlFlowDataWrapper.set("class", "DeadlockOperation");

                                    DeadlockOperation deadlockOperation = (DeadlockOperation) graphLabelData;

                                    controlFlowDataWrapper
                                            .set("actionType", deadlockOperation.actionType)
                                            .set("resourcesType", deadlockOperation.resourcesType);

//                                    JsonArray resourceIdentifiers = Json.array();
//                                    for (String resourceIdentifier : deadlockOperation.resourceIdentifiers) {
//                                        resourceIdentifiers.add(Utils.trim(resourceIdentifier, "_"));
//                                    }
                                      JsonArrayBuilder jab = Json.createArrayBuilder();
                                      for (String resourceIdentifier : deadlockOperation.resourceIdentifiers) {
                                          jab.add(Utils.trim(resourceIdentifier, "_"));
                                      }
                                    controlFlowDataWrapper.set("resourceIdentifiers", jab.build());
                                }
                                else if (graphLabelData instanceof DeadlockControlFlowGraphTask)
                                {
                                    controlFlowDataWrapper.set("class", "DeadlockControlFlowGraphTask");

                                    DeadlockControlFlowGraphTask controlFlowGraphTask = (DeadlockControlFlowGraphTask) graphLabelData;

                                    controlFlowDataWrapper
                                        .set("taskIdentifier", controlFlowGraphTask.taskIdentifier)
                                        .set("runAtStartup", controlFlowGraphTask.runAtStartup)
                                        .set("global", controlFlowGraphTask.global);
                                }
                                else if (graphLabelData instanceof ProcedureCall)
                                {
                                    controlFlowDataWrapper.set("class", "ProcedureCall");

                                    ProcedureCall procedureCall = (ProcedureCall) graphLabelData;

                                    controlFlowDataWrapper
                                            .set("procedure", procedureCall.identifier);
                                }

                                statement = controlFlowDataWrapper.get();
                            }
                            else
                            {
                                controlFlowDataWrapper
                                    .set("codeFilename", defaultFilename)
                                    .set("codeLineNumber", defaultCodeLineNumber);

                                boolean insertDefault = true;

                                List<String> keywords = Arrays.asList("Entry", "End");

                                String[] flowControlStatement = getFlowControl(statement);
                                String[] taskControlStatement = getTaskControl(statement);

                                if ((flowControlStatement[0] != null) && (flowControlStatement[1] != null))
                                {
                                    controlFlowDataWrapper
                                            .set("class", flowControlStatement[0])
                                            .set("type", flowControlStatement[1])
                                            .set("statement", flowControlStatement[2]);

                                    statement = controlFlowDataWrapper.get();

                                    insertDefault = false;
                                }
                                else if ((taskControlStatement[0] != null) && (taskControlStatement[1] != null))
                                {
                                    controlFlowDataWrapper
                                            .set("class", taskControlStatement[0])
                                            .set("taskIdentifier", taskControlStatement[1]);

                                    statement = controlFlowDataWrapper.get();

                                    insertDefault = false;
                                }

                                if (insertDefault)
                                {
                                    if (!keywords.contains(statement))
                                    {
                                        controlFlowDataWrapper
                                                .set("class", "Unknown")
                                                .set("statement", statement);

                                        statement = controlFlowDataWrapper.get();
                                    }
                                }
                            }
                        }
                        else
                        {
                            String[] flowControlStatement = getFlowControl(statement);

                            if (flowControlStatement[0] != null)
                            {
                                controlFlowDataWrapper
                                        .set("class", flowControlStatement[0])
                                        .set("type", flowControlStatement[1])
                                        .set("statement", flowControlStatement[2]);

                                statement = controlFlowDataWrapper.get();
                            }
                        }

                        writer.write("        node" + (uniqueId++) + " [ label=\"" + statement + "\" ]\n");
                    }

                    for (ControlFlowGraphNode controlFlowGraphNode : cfg.getGraph())
                    {
                        for (ControlFlowGraphNode inputControlFlowGraphNode : controlFlowGraphNode.getInputNodes())
                        {
                            writer.write("        node" + nodeIdMap.get(controlFlowGraphNode) + " -> node" +  nodeIdMap.get(inputControlFlowGraphNode) + "\n");
                        }
                    }
                }

                writer.write("    }\n");
            }

            writer.write("}");
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static String getTokenIdentifier(org.antlr.v4.runtime.Token token)
    {
        return String.valueOf(token.getTokenIndex());
    }

    private static String getProcedureName(String statement)
    {
        String statementPattern = statement;

        statementPattern = statementPattern.replace("(", "|");
        statementPattern = statementPattern.replace(")", "|");
        statementPattern = statementPattern.replace(";", "|");

        statementPattern = statementPattern.trim();

        String[] statementParts = statementPattern.split("\\|");

        if (statementParts.length > 0)
        {
            return statementParts[0];
        }

        return "";
    }

    private static String[] getTaskControl(String statement)
    {
        String[] result = new String[]{null, null};

        if (statement.contains("ACTIVATE"))
        {
            int keywordIndex = statement.indexOf("ACTIVATE");

            String taskIdentifier = statement.substring(keywordIndex + "ACTIVATE".length()).replace(";", "");

            result[0] = "TaskControlActivate";
            result[1] = taskIdentifier;
        }
        else if (statement.contains("CONTINUE"))
        {
            // Ignored
        }
        else if (statement.contains("RESUME"))
        {
            // Ignored
        }

        if (result[1] != null)
        {
            int keywordPrioIndex = result[1].indexOf("PRIO");

            if (keywordPrioIndex >= 0)
            {
                result[1] = result[1].substring(0, keywordPrioIndex);
            }
        }

        return result;
    }

    private static String[] getFlowControl(String statement)
    {
        String[] result = new String[]{"FlowControl", null, null};

        String statementLowerCase = statement.toLowerCase(Locale.ROOT);

        if (statementLowerCase.equals("if end"))
        {
            result[1] = "IF END";
            result[2] = "";
        }
        else if (statementLowerCase.equals("then end"))
        {
            result[1] = "THEN END";
            result[2] = "";
        }
        else if (statementLowerCase.equals("else end"))
        {
            result[1] = "ELSE END";
            result[2] = "";
        }
        else if (statementLowerCase.startsWith("if"))
        {
            result[1] = "IF";
            result[2] = statement.substring(result[1].length()).trim();
        }
        else if (statementLowerCase.equals("then"))
        {
            result[1] = "THEN";
            result[2] = "";
        }
        else if (statementLowerCase.equals("else"))
        {
            result[1] = "ELSE";
            result[2] = "";
        }
        else if (statementLowerCase.equals("alt end"))
        {
            result[1] = "ALT END";
            result[2] = "";
        }
        else if (statementLowerCase.startsWith("alt"))
        {
            result[1] = "ALT";
            result[2] = statement.substring(result[1].length()).replace("(", "").replace(")", "").trim();
        }
        else if (statementLowerCase.equals("out end"))
        {
            result[1] = "OUT END";
            result[2] = "";
        }
        else if (statementLowerCase.equals("out start"))
        {
            result[1] = "OUT START";
            result[2] = "";
        }
        else if (statementLowerCase.equals("case end"))
        {
            result[1] = "CASE END";
            result[2] = "";
        }
        else if (statementLowerCase.startsWith("case"))
        {
            result[1] = "CASE";
            result[2] = statement.substring(result[1].length()).trim();
        }
        else if (statementLowerCase.startsWith("for"))
        {
            result[1] = "FOR";
            result[2] = statement.substring(result[1].length()).trim();
        }
        else if (statementLowerCase.startsWith("to"))
        {
            result[1] = "TO";
            result[2] = statement.substring(result[1].length()).trim();
        }
        else if (statementLowerCase.equals("loop repeat"))
        {
            result[1] = "LOOP REPEAT";
            result[2] = "";
        }
        else if (statementLowerCase.equals("loop start"))
        {
            result[1] = "LOOP START";
            result[2] = "";
        }
        else if (statementLowerCase.equals("loop end"))
        {
            result[1] = "LOOP END";
            result[2] = "";
        }
        else if (statementLowerCase.startsWith("exit"))
        {
            result[1] = "EXIT";
            result[2] = statement.substring(result[1].length()).trim();
        }
        else if (statementLowerCase.startsWith("goto"))
        {
            result[1] = "GOTO";
            result[2] = statement.substring(result[1].length()).trim();
        }
        else if (statementLowerCase.startsWith("label"))
        {
            result[1] = "LABEL";
            result[2] = statement.substring(result[1].length()).trim();
        }
        else if (statementLowerCase.startsWith("terminate"))
        {
            result[1] = "TERMINATE";
            result[2] = statement.substring(result[1].length()).trim();
        }
        else
        {
            result[0] = null;
        }

        return result;
    }
}
