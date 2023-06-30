/*
 * [A "BSD license"]
 *  Copyright (c) 2021 Ilja Mascharow
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
package org.openpearl.compiler.SemanticAnalysis.ControlFlowGraph;

import org.antlr.v4.runtime.*;//ParserRuleContext;
import org.openpearl.compiler.*;

import java.util.*;
import java.util.stream.Collectors;

public class ControlFlowGraph {
    private String name;
    private Set<ControlFlowGraphNode> graph;
    private ControlFlowGraphNode entryNode;
    private ControlFlowGraphNode endNode;

    public ControlFlowGraph() {
        graph = new HashSet<>();
    }

    public ControlFlowGraph(String name, ControlFlowGraphNode entryNode, ControlFlowGraphNode endNode) {
        this.name = name;
        this.graph = new HashSet<>();
        this.entryNode = entryNode;
        this.endNode = endNode;
    }

    public String getName() { return name; }
    public Set<ControlFlowGraphNode> getGraph() { return graph; }
    public void addIfNotExist(ControlFlowGraphNode ControlFlowGraphNode, ControlFlowGraphNode inputControlFlowGraphNode) {
        graph.add(ControlFlowGraphNode);
        ControlFlowGraphNode.addInputNode(inputControlFlowGraphNode);
    }
    public void addIfNotExist(ControlFlowGraphNode ControlFlowGraphNode) {
        graph.add(ControlFlowGraphNode);
    }
    public ControlFlowGraphNode getEntryNode() { return entryNode; }
    public ControlFlowGraphNode getEndNode() { return endNode; }
    public void setEndNode(ControlFlowGraphNode endNode) { this.endNode = endNode; }

    private ControlFlowGraphVariableStack createParameterStack(ControlFlowGraphVariableStack startStack) {
        ControlFlowGraphVariableStack entryStack;
        // creates a copy of the passed module ControlFlowGraph
        if(startStack != null)
            entryStack = new ControlFlowGraphVariableStack(startStack, entryNode.getDepth());
        else
            entryStack = new ControlFlowGraphVariableStack();

        // Sets all the Module Variables to an unknown value
        // TODO If the Module Variable is unchangeable, it is not necessary to set the value to an unknown value
        // TODO will not work if a different Task changes the value of a Module Variable, while this procedure is executed
        entryStack.getVariables().forEach((name, values) -> {
            values.forEach((depth, value) -> {
                if(value instanceof FixedVariableValue) {
                    FixedVariableValue valueC = (FixedVariableValue) value;
                    valueC.getValues().clear();
                    valueC.getValues().add(FixedVariableValue.defaultValue());
                }
                else if(value instanceof FloatVariableValue) {
                    FloatVariableValue valueC = (FloatVariableValue) value;
                    valueC.getValues().clear();
                    valueC.getValues().add(FloatVariableValue.defaultValue());
                }
                else if(value instanceof CharVariableValue) {
                    CharVariableValue valueC = (CharVariableValue) value;
                    valueC.setKnown(false);
                }
                else if(value instanceof BitVariableValue) {
                    BitVariableValue valueC = (BitVariableValue) value;
                    valueC.getValues().clear();
                    valueC.getValues().add(BitVariableValue.defaultValue());
                }
                else if(value instanceof DurationVariableValue) {
                    DurationVariableValue valueC = (DurationVariableValue) value;
                    valueC.getValues().clear();
                    valueC.getValues().add(DurationVariableValue.defaultValue());
                }
                else if(value instanceof ClockVariableValue) {
                    ClockVariableValue valueC = (ClockVariableValue) value;
                    valueC.getValues().clear();
                    valueC.getValues().add(ClockVariableValue.defaultValue());
                }
            });
        });

        // Adds all the passable parameters to the Stack of the ControlFlowGraph
        // TODO this implementation is wrong, when the passed parameter is passed as a Reference, since other Tasks can change the value of the variable, while this function is executed
        ParserRuleContext ctx = entryNode.getCtx();
        if(ctx instanceof OpenPearlParser.ProcedureDeclarationContext) {
            OpenPearlParser.ProcedureDeclarationContext procCtx = (OpenPearlParser.ProcedureDeclarationContext) ctx;
            OpenPearlParser.TypeProcedureContext typeCtx = procCtx.typeProcedure();
            if(typeCtx != null) {
                OpenPearlParser.ListOfFormalParametersContext parametersCtx = typeCtx.listOfFormalParameters();
                if(parametersCtx != null) {
                    List<OpenPearlParser.FormalParameterContext> parameterList = parametersCtx.formalParameter();
                    if(parameterList != null) {
                        parameterList.forEach(parameter -> {
                            parameter.identifier().forEach(identifierContext -> {
                                String variableName = identifierContext.getText();
                                OpenPearlParser.ParameterTypeContext type = parameter.parameterType();
                                if(type != null) {
                                    OpenPearlParser.SimpleTypeContext simpleType = type.simpleType();
                                    if(simpleType != null) {
                                        OpenPearlParser.TypeIntegerContext integer = simpleType.typeInteger();
                                        OpenPearlParser.TypeFloatingPointNumberContext floating = simpleType.typeFloatingPointNumber();
                                        OpenPearlParser.TypeClockContext clock = simpleType.typeClock();
                                        OpenPearlParser.TypeDurationContext duration = simpleType.typeDuration();
                                        OpenPearlParser.TypeCharacterStringContext string = simpleType.typeCharacterString();
                                        OpenPearlParser.TypeBitStringContext bitString = simpleType.typeBitString();

                                        if(integer != null) {
                                            entryStack.addVariable(variableName, entryNode.getDepth(), new FixedVariableValue(FixedVariableValue.defaultValue()));
                                        }
                                        else if(floating != null) {
                                            entryStack.addVariable(variableName, entryNode.getDepth(), new FloatVariableValue(FloatVariableValue.defaultValue()));
                                        }
                                        else if(duration != null) {
                                            entryStack.addVariable(variableName, entryNode.getDepth(), new DurationVariableValue(DurationVariableValue.defaultValue()));
                                        }
                                        else if(string != null) {
                                            entryStack.addVariable(variableName, entryNode.getDepth(), new CharVariableValue());
                                        }
                                        else if(bitString != null) {
                                            entryStack.addVariable(variableName, entryNode.getDepth(), new BitVariableValue(BitVariableValue.defaultValue()));
                                        }
                                        //else if(time != null) {
                                        //    OpenPearlParser.TypeClockContext clock = time.type_clock();
                                        if(clock != null) {
                                            entryStack.addVariable(variableName, entryNode.getDepth(), new ClockVariableValue(ClockVariableValue.defaultValue()));
                                        }
                                        //}
                                    }
                                }
                            });
                        });
                    }
                }
            }
        }
        return entryStack;
    }

    public void createVariableStack(Map<String, ParserRuleContext> procedureMap, ControlFlowGraphVariableStack startStack) {
        // Initialize the Stack with Module Variables and passed arguments, when this ControlFlowGraph is from a Procedure
        entryNode.setVariableStack(createParameterStack(startStack));

        // Goes through the ControlFlowGraph, finds and saves the possible values of variables
        // and removes impossible connections between nodes.
        // Impossible connection are If or Case commands, which only have a single result
        ControlFlowGraphNode nextNode = entryNode.getInputNodes().iterator().next();
        createStackRecursive(nextNode, entryNode, new HashSet<>(), procedureMap, true);
    }

    private void createStackRecursive(ControlFlowGraphNode currentNode,
                                      ControlFlowGraphNode lastNode,
                                      Set<ControlFlowGraphNode> visitedNodes,
                                      Map<String, ParserRuleContext> procedureMap,
                                      boolean removeNodeConnection) {
        if(visitedNodes.contains(currentNode)) return;

        //Check if the current node has multiple ways of being reached
        if(currentNode.getOutputNodes().size() > 1) {
            //TODO not efficient, for each node with more than one outputNode, all nodes before it have to be visited
            for(ControlFlowGraphNode outPutNode : currentNode.getOutputNodes()) {
                // if it is possible, to reach the current output node, without visiting the current node, this function returns.
                // By doing this, it is possible to only continue calculating, when both path have already been visited.
                if(!visitedNodes.contains(outPutNode) && isReachable(entryNode, outPutNode, new HashSet<>(Collections.singletonList(currentNode)), false)) {
                    return;
                }
                //Checking if currentNode is inside a loop
                else if(isReachable(currentNode, outPutNode, null, false)) {
                    //creates the variable stack from the multiple outputNodes by combining them
                    buildFromLast(currentNode, procedureMap);

                    // Loops have to be calculated twice.
                    // This Code is used to do the first calculation
                    // By creating a new Set, which saves, which Nodes have been already visited,
                    // It is possible to visit the same Nodes again later
                    calculate(currentNode, procedureMap, false);
                    currentNode.getInputNodes().forEach(inputNode -> {
                        Set<ControlFlowGraphNode> visitedNodesCopy = new HashSet<>(visitedNodes);
                        visitedNodesCopy.add(currentNode);
                        createStackRecursive(inputNode, currentNode, visitedNodesCopy, procedureMap, false);
                    });
                }
            }
            buildFromLast(currentNode, procedureMap);
        }
        // Copy the variable stack from the last node to the currentNode, variables,
        // which were decelerated in a block, which is deeper then the current block, are not copied.
        else if(currentNode.getVariableStack() != null) currentNode.getVariableStack().combine(lastNode.getVariableStack(), currentNode.getDepth());
        else currentNode.setVariableStack(new ControlFlowGraphVariableStack(lastNode.getVariableStack(), currentNode.getDepth()));

        visitedNodes.add(currentNode);
        //execute the command of the current node
        calculate(currentNode, procedureMap, removeNodeConnection);

        currentNode.getInputNodes().forEach(node -> {
            createStackRecursive(node, currentNode, visitedNodes, procedureMap, removeNodeConnection);
        });
    }

    // executes the command, which is saved in currentNode
    private void calculate(ControlFlowGraphNode currentNode, Map<String, ParserRuleContext> procedureMap, boolean removeNodeConnection) {
        if(currentNode.getCtx() instanceof OpenPearlParser.VariableDeclarationContext) {
            variableDeclaration((OpenPearlParser.VariableDeclarationContext) currentNode.getCtx(), currentNode, procedureMap);
        }
        else if(currentNode.getCtx() instanceof OpenPearlParser.Unlabeled_statementContext) {
            OpenPearlParser.Unlabeled_statementContext ulCtx = (OpenPearlParser.Unlabeled_statementContext) currentNode.getCtx();
            OpenPearlParser.Assignment_statementContext asCtx = ulCtx.assignment_statement();
            OpenPearlParser.Sequential_control_statementContext scCtx = ulCtx.sequential_control_statement();
            if(asCtx != null) {
                assignment(asCtx, currentNode, procedureMap);
            }
            else if(scCtx != null) {
                OpenPearlParser.If_statementContext ifCtx = scCtx.if_statement();
                OpenPearlParser.Case_statementContext caseCtx = scCtx.case_statement();
                if(ifCtx != null) {
                    if (removeNodeConnection)
                        ifCheck(ifCtx, currentNode, procedureMap);
                }
                if(caseCtx != null) {
                    if (removeNodeConnection)
                        caseCheck(caseCtx, currentNode, procedureMap);
                }
            }
        }
        else if(currentNode.getType() == ControlFlowGraphNodeType.THEN_START) {
            OpenPearlParser.Unlabeled_statementContext ulCtx = (OpenPearlParser.Unlabeled_statementContext) currentNode.getOutputNodes().iterator().next().getCtx();
            OpenPearlParser.Sequential_control_statementContext scCtx = ulCtx.sequential_control_statement();
            if(scCtx != null) {
                OpenPearlParser.If_statementContext ifCtx = scCtx.if_statement();
                if(ifCtx != null) {
                    ifSetValues(ifCtx, currentNode, procedureMap, false);
                }
            }
        }
        else if(currentNode.getType() == ControlFlowGraphNodeType.ELSE_START) {
            OpenPearlParser.Unlabeled_statementContext ulCtx = (OpenPearlParser.Unlabeled_statementContext) currentNode.getOutputNodes().iterator().next().getCtx();
            OpenPearlParser.Sequential_control_statementContext scCtx = ulCtx.sequential_control_statement();
            if(scCtx != null) {
                OpenPearlParser.If_statementContext ifCtx = scCtx.if_statement();
                if(ifCtx != null) {
                    ifSetValues(ifCtx, currentNode, procedureMap, true);
                }
            }
        }
        else if(currentNode.getType() == ControlFlowGraphNodeType.ALT_START
                || currentNode.getType() == ControlFlowGraphNodeType.OUT_START) {
            caseSetValues(currentNode);
        }
        else if(currentNode.getType() == ControlFlowGraphNodeType.LOOP_BLOCK_START) {
            ControlFlowGraphNode lastNode = currentNode.getOutputNodes().iterator().next();
            ParserRuleContext ctx = lastNode.getCtx();
            OpenPearlParser.Unlabeled_statementContext ul = (OpenPearlParser.Unlabeled_statementContext) ctx;
            OpenPearlParser.LoopStatementContext loop = ul.loopStatement();
            if(loop != null) {
                loop(loop, currentNode, procedureMap);
            }
        }
    }

    private void caseSetValues(ControlFlowGraphNode currentNode) {
        Operations value = altOrOutNodeValueMap.get(currentNode);
        ControlFlowGraphNode caseNode = currentNode.getOutputNodes().iterator().next();

        OpenPearlParser.Unlabeled_statementContext ulCtx = (OpenPearlParser.Unlabeled_statementContext) caseNode.getCtx();
        if (ulCtx == null) return;
        OpenPearlParser.Sequential_control_statementContext scCtx = ulCtx.sequential_control_statement();
        if(scCtx == null) return;
        OpenPearlParser.Case_statementContext caseCtx = scCtx.case_statement();
        if(caseCtx == null) return;

        OpenPearlParser.Case_statement_selection1Context case1 = caseCtx.case_statement_selection1();
        OpenPearlParser.Case_statement_selection2Context case2 = caseCtx.case_statement_selection2();
        if(case1 == null && case2 == null) return;

        String variableName;
        if(case1 != null)
            variableName = case1.expression().getText();
        else
            variableName = case2.expression().getText();

        variableName = variableName.replaceAll("\\(", "").replaceAll("\\)", "");

        ControlFlowGraphVariableStack stack = currentNode.getVariableStack();

        Operations variable = stack.getDeepestVariableByName(variableName);
        if(variable instanceof FixedVariableValue) {
            FixedVariableValue fixedV = (FixedVariableValue) variable;
            stack.setVariable(variableName, (FixedVariableValue) value.h_and(fixedV));
        }
        else if(variable instanceof CharVariableValue) {
            CharVariableValue charV = (CharVariableValue) variable;
            stack.setVariable(variableName, (CharVariableValue) value.h_and(charV));
        }
    }

    private void ifSetValues(OpenPearlParser.If_statementContext ifCtx, ControlFlowGraphNode currentNode, Map<String, ParserRuleContext> procedureMap, boolean negate) {
        OpenPearlParser.ExpressionContext expression = ifCtx.expression();

        Map<String, Operations> possibleValues = OperationTree.createVariableValueMap(expression.getText(), currentNode.getVariableStack(), procedureMap);
        //Check creating a operationTree of the expression has failed. Happens when the expression uses features, which are not yet supported
        if(possibleValues == null) return;

        ControlFlowGraphVariableStack stack = currentNode.getVariableStack();
        possibleValues.forEach((name, value) -> {
            Operations variable = stack.getDeepestVariableByName(name);
            if(variable instanceof FixedVariableValue) {
                FixedVariableValue fixedV = (FixedVariableValue) variable;
                if(negate)
                    stack.setVariable(name, (FixedVariableValue) value.h_notEqual().h_and(fixedV));
                else
                    stack.setVariable(name, (FixedVariableValue) value.h_and(fixedV));
            }
            else if(variable instanceof BitVariableValue) {
                BitVariableValue fixedV = (BitVariableValue) variable;
                if(negate)
                    stack.setVariable(name, (BitVariableValue) value.h_notEqual().h_and(fixedV));
                else
                    stack.setVariable(name, (BitVariableValue) value.h_and(fixedV));
            }
            if(variable instanceof CharVariableValue) {
                CharVariableValue fixedV = (CharVariableValue) variable;
                if(negate)
                    stack.setVariable(name, (CharVariableValue) value.h_notEqual().h_and(fixedV));
                else
                    stack.setVariable(name, (CharVariableValue) value.h_and(fixedV));
            }

            /*
            FloatVariableValue floatV = stack.getDeepestFloatValueByName(name);
            if(floatV != null) {
                if(negate)
                    stack.setFloatVariableValue(name, (FloatVariableValue) value.h_notEqual().h_and(fixedV));
                else
                    stack.setFloatVariableValue(name, (FloatVariableValue) value.h_and(fixedV));
            }
            DurationVariableValue durationV = stack.getDeepestDurationValueByName(name);
            if(durationV != null) {
                if(negate)
                    stack.setDurationVariableValue(name, (DurationVariableValue) value.h_notEqual().h_and(fixedV));
                else
                    stack.setDurationVariableValue(name, (DurationVariableValue) value.h_and(fixedV));
            }
            ClockVariableValue clockV = stack.getDeepestClockValueByName(name);
            if(clockV != null) {
                if(negate)
                    stack.setClockVariableValue(name, (ClockVariableValue) value.h_notEqual().h_and(fixedV));
                else
                    stack.setClockVariableValue(name, (ClockVariableValue) value.h_and(fixedV));
            }
            */
        });
    }

    private void ifCheck(OpenPearlParser.If_statementContext ifCtx, ControlFlowGraphNode currentNode, Map<String, ParserRuleContext> procedureMap) {
        OpenPearlParser.ExpressionContext expression = ifCtx.expression();
        //Check creating a operationTree of the expression has failed. Happens when the expression uses features, which are not yet supported
        OperationTree.OperationNode tree = OperationTree.buildTree(expression.getText(), currentNode.getVariableStack(), procedureMap);
        if(tree == null) return;
        BitVariableValue result = (BitVariableValue) tree.calculate();

        boolean neverTrue = true;
        boolean neverFalse = true;
        try {
            for(VariableValueRange<ConstantBitValue> value : result.getValues()) {
                if(value.getFrom().getLongValue() > 0L) neverTrue = false;
                else neverFalse = false;
                if(value.getTo().getLongValue() > 0L) neverTrue = false;
                else neverFalse = false;
            }
        }
        catch (Exception ex) {
            int a = 0;
            OperationTree.buildTree(expression.getText(), currentNode.getVariableStack(), procedureMap);
        }
        Set<ControlFlowGraphNode> toRemoveConnection = new HashSet<>();
        for(ControlFlowGraphNode inputNode : currentNode.getInputNodes()) {
            if(inputNode.getType() == ControlFlowGraphNodeType.THEN_START) {
                if(neverTrue)
                    toRemoveConnection.add(inputNode);
                //else if(!neverFalse)
                    //test(expression, currentNode, inputNode, procedureMap, toRemoveConnection);
            }
            else {
                if(neverFalse)
                    toRemoveConnection.add(inputNode);
                //else if(!neverTrue)
                    //test(expression, currentNode, inputNode, procedureMap, toRemoveConnection);
            }
        }
        toRemoveConnection.forEach(currentNode::removeInputNode);
    }

    private Map<ControlFlowGraphNode, Operations> altOrOutNodeValueMap = new HashMap<>();
    private void case1(ControlFlowGraphNode currentNode, Operations variableValue) {
        List<ControlFlowGraphNode> alts = currentNode.getInputNodes().stream()
                .sorted(Comparator.comparing(ControlFlowGraphNode::getId))
                .collect(Collectors.toList());
        long currentValue = 1;
        Operations allCheckedValues = null;
        for(ControlFlowGraphNode alt : alts) {
            if(alt.getType() == ControlFlowGraphNodeType.OUT_START || alt.getType() == ControlFlowGraphNodeType.CASE_END) {
                if(allCheckedValues != null) {
                    Operations reversed = allCheckedValues.h_notEqual();
                    BitVariableValue result = (BitVariableValue) variableValue.equal(reversed);
                    if(neverTrue(result))
                        currentNode.removeInputNode(alt);
                    altOrOutNodeValueMap.put(alt, reversed);
                }
            }
            else if (alt.getType() == ControlFlowGraphNodeType.ALT_START){
                FixedVariableValue toCheckValue = new FixedVariableValue(new VariableValueRange<>(new ConstantFixedValue(currentValue)));
                BitVariableValue result = (BitVariableValue) variableValue.equal(toCheckValue);
                if(neverTrue(result))
                    currentNode.removeInputNode(alt);

                if(allCheckedValues == null)
                    allCheckedValues = toCheckValue;
                else
                    allCheckedValues = allCheckedValues.h_or(toCheckValue);
                currentValue++;
                altOrOutNodeValueMap.put(alt, toCheckValue);
            }
        }
    }

    private void case2(ControlFlowGraphNode currentNode, Operations variableValue) {
        List<ControlFlowGraphNode> alts = currentNode.getInputNodes().stream()
                .sorted(Comparator.comparing(ControlFlowGraphNode::getId))
                .collect(Collectors.toList());
        List<ControlFlowGraphNode> toRemoveNodes = new ArrayList<>();
        Operations allCheckedValues = null;
        for(ControlFlowGraphNode alt : alts) {
            if(alt.getType() == ControlFlowGraphNodeType.OUT_START || alt.getType() == ControlFlowGraphNodeType.CASE_END) {
                if(allCheckedValues != null) {
                    Operations reversed = allCheckedValues.h_notEqual();
                    BitVariableValue result = (BitVariableValue) variableValue.equal(reversed);
                    if(neverTrue(result))
                        currentNode.removeInputNode(alt);
                    altOrOutNodeValueMap.put(alt, reversed);
                }
            }
            else if(alt.getType() == ControlFlowGraphNodeType.ALT_START) {
                OpenPearlParser.Case_statement_selection2_altContext altCtx = (OpenPearlParser.Case_statement_selection2_altContext) alt.getCtx();
                OpenPearlParser.Case_listContext caseList = altCtx.case_list();
                List<OpenPearlParser.Index_sectionContext> index_sectionContexts = caseList.index_section();
                Operations checkValue = null;
                for(OpenPearlParser.Index_sectionContext index_sectionContext: index_sectionContexts) {
                    List<OpenPearlParser.ConstantFixedExpressionContext> fixes = index_sectionContext.constantFixedExpression();
                    List<OpenPearlParser.ConstantCharacterStringContext> strings = index_sectionContext.constantCharacterString();
                    if(fixes != null && fixes.size() != 0) {
                        List<Long> values = new ArrayList<>();
                        fixes.forEach(fixed -> {
                            values.add(Long.valueOf(fixed.getText()));
                        });
                        Collections.sort(values);
                        if(checkValue == null) {
                            checkValue = new FixedVariableValue(new VariableValueRange<>(
                                    new ConstantFixedValue(values.get(0)),
                                    new ConstantFixedValue(values.get(values.size()-1))
                            ));
                        }
                        else {
                            checkValue = checkValue.h_or(new FixedVariableValue(new VariableValueRange<>(
                                    new ConstantFixedValue(values.get(0)),
                                    new ConstantFixedValue(values.get(values.size()-1))
                            )));
                        }
                    }
                    else {
                        List<String> values = new ArrayList<>();
                        strings.forEach(string -> {
                            values.add(string.getText());
                        });
                        Collections.sort(values);
                        if(checkValue == null) {
                            checkValue = new CharVariableValue(new VariableValueRange<>(
                                    new ConstantCharacterValue(values.get(0)),
                                    new ConstantCharacterValue(values.get(values.size()-1))
                            ));
                        }
                        else {
                            checkValue = checkValue.h_or(new CharVariableValue(new VariableValueRange<>(
                                    new ConstantCharacterValue(values.get(0)),
                                    new ConstantCharacterValue(values.get(values.size()-1))
                            )));
                        }
                    }
                    if(allCheckedValues == null)
                        allCheckedValues = checkValue;
                    else
                        allCheckedValues = allCheckedValues.h_or(checkValue);
                }
                if(checkValue != null) {
                    BitVariableValue result = (BitVariableValue) variableValue.equal(checkValue);
                    if(neverTrue(result))
                        toRemoveNodes.add(alt);
                }
                altOrOutNodeValueMap.put(alt, checkValue);
            }
        }
        toRemoveNodes.forEach(currentNode::removeInputNode);
    }

    private boolean neverTrue(BitVariableValue result) {
        for(VariableValueRange<ConstantBitValue> value : result.getValues()) {
            if(value.getFrom().getLongValue() > 0L) return false;
            if(value.getTo().getLongValue() > 0L) return false;
        }
        return true;
    }

    private boolean neverFalse(BitVariableValue result) {
        for(VariableValueRange<ConstantBitValue> value : result.getValues()) {
            if(value.getFrom().getLongValue() == 0L) return false;
            if(value.getTo().getLongValue() == 0L) return false;
        }
        return true;
    }


    private void caseCheck(OpenPearlParser.Case_statementContext caseCtx, ControlFlowGraphNode currentNode, Map<String, ParserRuleContext> procedureMap) {
        OpenPearlParser.Case_statement_selection1Context case1 = caseCtx.case_statement_selection1();
        OpenPearlParser.Case_statement_selection2Context case2 = caseCtx.case_statement_selection2();
        if(case1 == null && case2 == null) return;

        String variableName;
        if(case1 != null)
            variableName = case1.expression().getText();
        else
            variableName = case2.expression().getText();

        variableName = variableName.replaceAll("\\(", "").replaceAll("\\)", "");
        ControlFlowGraphVariableStack stack = currentNode.getVariableStack();
        Operations variableValue = stack.getDeepestVariableByName(variableName);

        if(case1 != null)
            case1(currentNode, variableValue);
        else
            case2(currentNode, variableValue);
    }

    private void loop(OpenPearlParser.LoopStatementContext loop, ControlFlowGraphNode currentNode, Map<String, ParserRuleContext> procedureMap) {
        if (loop.loopStatement_for() != null) {
        OpenPearlParser.LoopStatement_fromContext fromCtx = loop.loopStatement_from();
        OpenPearlParser.LoopStatement_toContext toCtx = loop.loopStatement_to();
        OpenPearlParser.LoopStatement_forContext forCtx = loop.loopStatement_for();

        FixedVariableValue from;
        FixedVariableValue to;

        if(fromCtx == null) {
            from = new FixedVariableValue(new ConstantFixedValue(1, 63));
        }
        else {
            OperationTree.OperationNode tree = OperationTree.buildTree(fromCtx.expression().getText(), currentNode.getVariableStack(), procedureMap);
            if(tree == null) return;
            Operations result = tree.calculate();
            from = (FixedVariableValue) result;
        }
        if(toCtx == null) {
            to = new FixedVariableValue(new ConstantFixedValue(Long.MAX_VALUE, 63));
        }
        else {
            OperationTree.OperationNode tree = OperationTree.buildTree(toCtx.expression().getText(), currentNode.getVariableStack(), procedureMap);
            if(tree == null) return;
            Operations result = tree.calculate();
            to = (FixedVariableValue) result;
        }

        String name = forCtx.ID().getText();

        currentNode.getVariableStack().addVariable(name, currentNode.getDepth(),
                new FixedVariableValue(new VariableValueRange<>(getSmallest(from.getValues()), getLargest(to.getValues()))));
        }
    }

    private static <T extends Comparable<T>> T getSmallest(Collection<VariableValueRange<T>> values) {
        Iterator<VariableValueRange<T>> iterator = values.iterator();
        T smallest = iterator.next().getFrom();
        while (iterator.hasNext()) {
            T next = iterator.next().getFrom();
            if(smallest.compareTo(next) > 0)
                smallest = next;
        }
        return smallest;
    }

    private static <T extends Comparable<T>> T getLargest(Collection<VariableValueRange<T>> values) {
        Iterator<VariableValueRange<T>> iterator = values.iterator();
        T largest = iterator.next().getFrom();
        while (iterator.hasNext()) {
            T next = iterator.next().getFrom();
            if(largest.compareTo(next) < 0)
                largest = next;
        }
        return largest;
    }

    private void buildFromLast(ControlFlowGraphNode currentNode, Map<String, ParserRuleContext> procedureMap) {
        Set<ControlFlowGraphNode> outPutNodes = currentNode.getOutputNodes();
        Iterator<ControlFlowGraphNode> iterator = outPutNodes.iterator();
        ControlFlowGraphNode node;
        if(currentNode.getVariableStack() == null) {
            do {
                node = iterator.next();
            } while (node.getVariableStack() == null || !isReachable(entryNode, node, null, true));

            if(node.getVariableStack() != null && isReachable(entryNode, node, null, true)) {
                currentNode.setVariableStack(new ControlFlowGraphVariableStack(node.getVariableStack(), currentNode.getDepth()));
            }
        }
        while (iterator.hasNext()) {
            node = iterator.next();
            if(node.getVariableStack() != null && isReachable(entryNode, node, null, false)) {
                currentNode.getVariableStack().combine(node.getVariableStack(), currentNode.getDepth());
            }
        }
    }

    private boolean isReachable(ControlFlowGraphNode from, ControlFlowGraphNode to, Set<ControlFlowGraphNode> notAllowedNodes, boolean allowSelf) {
        if(allowSelf && from == to) return true;
        Set<ControlFlowGraphNode> visitedNodes = new HashSet<>();
        Queue<ControlFlowGraphNode> currentNodeQueue = new ArrayDeque<>(from.getInputNodes());
        while (!currentNodeQueue.isEmpty()) {
            ControlFlowGraphNode currentNode = currentNodeQueue.remove();
            if(currentNode == to) return true;

            if(notAllowedNodes != null && notAllowedNodes.contains(currentNode)) continue;
            if(visitedNodes.contains(currentNode)) continue;
            visitedNodes.add(currentNode);

            currentNodeQueue.addAll(currentNode.getInputNodes());
        }
        return false;
    }

    private void variableDeclaration(OpenPearlParser.VariableDeclarationContext ctx,
            ControlFlowGraphNode currentNode,
            Map<String, ParserRuleContext> procedureMap) {
        if (ctx == null) return;

        ctx.variableDenotation().forEach(denotation -> {
            if (denotation.dimensionAttribute() != null) {
                return; // do not treat arrays 
            }
            if (denotation.problemPartDataAttribute() != null) {
                OpenPearlParser.TypeAttributeContext type = denotation.problemPartDataAttribute().typeAttribute();
                OpenPearlParser.SimpleTypeContext simpleTypeContext = type.simpleType();
                OpenPearlParser.TypeReferenceContext typeReferenceContext = type.typeReference();

                String fullSting = denotation.identifierDenotation().getText();
                String[] variableNames = fullSting.replaceAll("\\(", "").replaceAll("\\)", "").split(",");
                OpenPearlParser.ConstantExpressionContext initExpressionCtx  = getConstantExpression(denotation);
                OpenPearlParser.ConstantContext initConstantCtx = getConstant(denotation);

                for (String variableName : variableNames) {
                    if(simpleTypeContext != null) {
                        OpenPearlParser.TypeIntegerContext intctx = simpleTypeContext.typeInteger();
                        OpenPearlParser.TypeFloatingPointNumberContext floatctx = simpleTypeContext.typeFloatingPointNumber();
                        OpenPearlParser.TypeBitStringContext bitctx = simpleTypeContext.typeBitString();
                        OpenPearlParser.TypeCharacterStringContext charctx = simpleTypeContext.typeCharacterString();
                        OpenPearlParser.TypeClockContext clockctx = simpleTypeContext.typeClock();
                        OpenPearlParser.TypeDurationContext durationctx = simpleTypeContext.typeDuration();

                        if(intctx != null) {
                            int precision = getPrecision(intctx);
                            if(initExpressionCtx != null || initConstantCtx != null) {
                                FixedVariableValue result;
                                OperationTree.OperationNode tree;
                                if(initExpressionCtx != null) {
                                    tree = OperationTree.buildTree(initExpressionCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                else {
                                    tree = OperationTree.buildTree(initConstantCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                if(tree == null) return;
                                result = (FixedVariableValue) tree.calculate();
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(), result);
                            }
                            else {
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(),
                                        new FixedVariableValue(FixedVariableValue.defaultValue()));
                            }
                        }
                        else if(floatctx != null) {
                            if(initExpressionCtx != null || initConstantCtx != null) {
                                FloatVariableValue result;
                                if(initExpressionCtx != null) {
                                    OperationTree.OperationNode tree = OperationTree.buildTree(initExpressionCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                    if(tree == null) return;
                                    Operations tmp = tree.calculate();
                                    if(tmp instanceof FixedVariableValue)
                                        result = (FloatVariableValue) tmp.toFloat();
                                    else
                                        result = (FloatVariableValue) tree.calculate();
                                }
                                else {
                                    OperationTree.OperationNode tree = OperationTree.buildTree(initConstantCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                    if(tree == null) return;
                                    Operations tmp = tree.calculate();
                                    if(tmp instanceof FixedVariableValue)
                                        result = (FloatVariableValue) tmp.toFloat();
                                    else
                                        result = (FloatVariableValue) tree.calculate();
                                }

                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(), result);
                            }
                            else {
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(),
                                        new FloatVariableValue(FloatVariableValue.defaultValue()));
                            }
                        }
                        else if(bitctx != null) {
                            if(initExpressionCtx != null || initConstantCtx != null) {
                                BitVariableValue result;
                                OperationTree.OperationNode tree;
                                if(initConstantCtx != null) {
                                    tree = OperationTree.buildTree(initConstantCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                else {
                                    tree = OperationTree.buildTree(initExpressionCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                if(tree == null) return;
                                result = (BitVariableValue) tree.calculate();
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(), result);
                            }
                            else {
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(),
                                        new BitVariableValue(BitVariableValue.defaultValue()));
                            }
                        }
                        else if(charctx != null) {
                            if(initExpressionCtx != null || initConstantCtx != null) {
                                CharVariableValue result;
                                OperationTree.OperationNode tree;
                                if(initConstantCtx != null) {
                                    tree = OperationTree.buildTree(initConstantCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                else {
                                    tree = OperationTree.buildTree(initExpressionCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                if(tree == null) return;
                                result = (CharVariableValue) tree.calculate();
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(), result);
                            }
                            else {
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(),
                                        new CharVariableValue());
                            }
                        }
                        else if(clockctx != null) {
                            //                   OpenPearlParser.TypeClockContext clock = clockctx.typeClock();
                            /////                    OpenPearlParser.Type_durationContext duration = timectx.typeDuration();
                            //                    if(clock != null) {
                            if(initExpressionCtx != null || initConstantCtx != null) {
                                ClockVariableValue result;
                                OperationTree.OperationNode tree;
                                if(initConstantCtx != null) {
                                    tree = OperationTree.buildTree(initConstantCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                else {
                                    tree = OperationTree.buildTree(initExpressionCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                if(tree == null) return;
                                result = (ClockVariableValue) tree.calculate();
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(), result);
                            }
                            else {
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(),
                                        new ClockVariableValue(ClockVariableValue.defaultValue()));
                            }
                        }
                        else if(durationctx != null) {
                            if(initExpressionCtx != null || initConstantCtx != null) {
                                DurationVariableValue result;
                                OperationTree.OperationNode tree;
                                if(initConstantCtx != null) {
                                    tree = OperationTree.buildTree(initConstantCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                else {
                                    tree = OperationTree.buildTree(initExpressionCtx.getText(), currentNode.getVariableStack(), procedureMap);
                                }
                                if(tree == null) return;
                                result = (DurationVariableValue) tree.calculate();
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(), result);
                            }
                            else {
                                currentNode.getVariableStack().addVariable(variableName, currentNode.getDepth(),
                                        new DurationVariableValue(DurationVariableValue.defaultValue()));
                            }
                        }
                    }

                    else if(typeReferenceContext != null) {
                        //TODO add Reference Type
                    }
                }
            }
            else {
                // Type sema. bolt, dation are without interrest here
            }
        }

        );  // end of foreach
    }


    private void assignment(OpenPearlParser.Assignment_statementContext ctx,
                            ControlFlowGraphNode currentNode,
                            Map<String, ParserRuleContext> procedureMap) {
        String variableName = ctx.name().ID().toString();
        OpenPearlParser.ExpressionContext expressionContext = ctx.expression();
        ControlFlowGraphVariableStack stack = currentNode.getVariableStack();
        Operations variable = stack.getDeepestVariableByName(variableName);
        OperationTree.OperationNode operationNode = OperationTree.buildTree(expressionContext.getText(), currentNode.getVariableStack(), procedureMap);
        //Check creating a operationTree of the expression has failed. Happens when the expression uses features, which are not yet supported
        if(operationNode == null) return;
        if(variable instanceof FixedVariableValue) {
            if(isReachable(currentNode, currentNode, null, false) && !operationNode.isConstant()) {
                stack.setVariable(variableName, new FixedVariableValue(FixedVariableValue.defaultValue()));
            }
            else {
                Operations tmp = operationNode.calculate();
                if(tmp instanceof  FloatVariableValue)
                    stack.setVariable(variableName, tmp.entier());
                else
                    stack.setVariable(variableName, tmp);
            }
        }
        else if(variable instanceof FloatVariableValue) {
            if(isReachable(currentNode, currentNode, null, false) && !operationNode.isConstant()) {
                stack.setVariable(variableName, new FloatVariableValue(FloatVariableValue.defaultValue()));
            }
            else {
                Operations tmp = operationNode.calculate();
                if(tmp instanceof FixedVariableValue) {
                    stack.setVariable(variableName, tmp.toFloat());
                }
                else {
                    stack.setVariable(variableName, tmp);
                }
            }
        }
        else if(variable instanceof CharVariableValue) {
            if(isReachable(currentNode, currentNode, null, false) && !operationNode.isConstant()) {
                stack.setVariable(variableName, new CharVariableValue());
            }
            else {
                CharVariableValue result = (CharVariableValue) operationNode.calculate().deepClone();
                stack.setVariable(variableName, result);
            }
        }
        else if(variable instanceof BitVariableValue) {
            if(isReachable(currentNode, currentNode, null, false) && !operationNode.isConstant()) {
                stack.setVariable(variableName, new BitVariableValue(BitVariableValue.defaultValue()));
            }
            else {
                try {
                    BitVariableValue result = (BitVariableValue) operationNode.calculate().deepClone();
                    stack.setVariable(variableName, result);
                }
                catch (Exception ex) {
                    int a = 0;
                }
            }
        }
        else if(variable instanceof DurationVariableValue) {
            if(isReachable(currentNode, currentNode, null, false) && !operationNode.isConstant()) {
                stack.setVariable(variableName, new DurationVariableValue(DurationVariableValue.defaultValue()));
            }
            else {
                DurationVariableValue result = (DurationVariableValue) operationNode.calculate().deepClone();
                stack.setVariable(variableName, result);
            }
        }
        else if(variable instanceof ClockVariableValue) {
            if(isReachable(currentNode, currentNode, null, false) && !operationNode.isConstant()) {
                stack.setVariable(variableName, new ClockVariableValue(ClockVariableValue.defaultValue()));
            }
            else {
                ClockVariableValue result = (ClockVariableValue) operationNode.calculate().deepClone();
                stack.setVariable(variableName, result);
            }
        }
    }

    private String getInitString(OpenPearlParser.InitialisationAttributeContext ctx) {
        if(ctx == null) return null;

        for(OpenPearlParser.InitElementContext init : ctx.initElement()) {
            return init.getText();
        }
        return null;
    }

    private int getPrecision(OpenPearlParser.TypeIntegerContext ctx) {
        OpenPearlParser.MprecisionContext precision = ctx.mprecision();
        if(precision != null)
            return Integer.parseInt(precision.getText());
        else
            return 63;
    }

    private VariableValueRange<ConstantFixedValue> getMinMaxRange(int precision) {
        long result = (long) (Math.pow(2, precision)-1);
        return new VariableValueRange<>(new ConstantFixedValue((result * -1)-1, precision), new ConstantFixedValue(result, precision));
    }

    private OpenPearlParser.ConstantExpressionContext getConstantExpression(OpenPearlParser.VariableDenotationContext denotation) {
        OpenPearlParser.InitialisationAttributeContext initAttrCtx = denotation.problemPartDataAttribute().initialisationAttribute();
        if(initAttrCtx == null) return null;

        List<OpenPearlParser.InitElementContext> initElements =  initAttrCtx.initElement();
        if(initElements == null || initElements.isEmpty()) return null;

        OpenPearlParser.InitElementContext initElement = initElements.get(0);
        return initElement.constantExpression();
    }

    private OpenPearlParser.ConstantContext getConstant(OpenPearlParser.VariableDenotationContext denotation) {
        OpenPearlParser.InitialisationAttributeContext initAttrCtx = denotation.problemPartDataAttribute().initialisationAttribute();
        if(initAttrCtx == null) return null;

        List<OpenPearlParser.InitElementContext> initElements =  initAttrCtx.initElement();
        if(initElements == null || initElements.isEmpty()) return null;

        OpenPearlParser.InitElementContext initElement = initElements.get(0);
        return initElement.constant();
    }
}
