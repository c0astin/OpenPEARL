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
package org.smallpearl.compiler.ControlFlowGraph;

import org.antlr.v4.runtime.ParserRuleContext;
import org.smallpearl.compiler.*;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OperationTree {

    private static final HashMap<String, Integer> operationOrder = new HashMap<>();
    static {
        operationOrder.put("(", -1);

        operationOrder.put("**", 0);
        operationOrder.put("FIT", 0);
        operationOrder.put("LWB", 0);
        operationOrder.put("UPB", 0);

        operationOrder.put("*", 1);
        operationOrder.put("/", 1);
        operationOrder.put("><", 1);
        operationOrder.put("//", 1);
        operationOrder.put("REM", 1);

        operationOrder.put("+", 2);
        operationOrder.put("-", 2);
        operationOrder.put("<>", 2);
        operationOrder.put("SHIFT", 2);
        operationOrder.put("CSHIFT", 2);

        operationOrder.put("<", 3); operationOrder.put("LT", 3);
        operationOrder.put(">", 3); operationOrder.put("GT", 3);
        operationOrder.put("<=", 3); operationOrder.put("LE", 3);
        operationOrder.put(">=", 3); operationOrder.put("GE", 3);

        operationOrder.put("==", 4); operationOrder.put("EQ", 4);
        operationOrder.put("/=", 4); operationOrder.put("NE", 4);
        operationOrder.put("IS", 4);
        operationOrder.put("ISNT", 4);

        operationOrder.put("AND", 5);

        operationOrder.put("OR", 6);
        operationOrder.put("EXOR", 6);
    }

    // Returns an object, with which it is possible to get all the possible results
    public static OperationNode buildTree(String expression, ControlFlowGraphVariableStack variables, Map<String, ParserRuleContext> procedureMap) {
        Parser.Expression expressionResult = new Parser(expression).parseExpression(procedureMap, variables, true);
        Parser.Node node = createNodeFromExpression(expressionResult);

        return buildOperationNodeTree(node, variables, procedureMap, new HashMap<>());
    }

    // Returns a map, which all the possible Values, If the check returns true
    public static Map<String, Operations> createVariableValueMap(String expression, ControlFlowGraphVariableStack variables, Map<String, ParserRuleContext> procedureMap) {
        Parser.Expression expressionResult = new Parser(expression).parseExpression(procedureMap, variables, true);
        Parser.Node node = createNodeFromExpression(expressionResult);

        Map<String, Operations> currentVariableValues = new HashMap<>();
        buildOperationNodeTree(node, variables, procedureMap, currentVariableValues);
        return currentVariableValues;
    }

    // Creates a Node Tree, which is sorted,
    // so that all the commands in an expression can be executed in the right order
    private static Parser.Node createNodeFromExpression(Parser.Expression expression) {
        Queue<Parser.Node> outPutQueue = new LinkedList<>();
        Stack<Parser.Node> operators = new Stack<>();


        Parser.Expression current = expression;
        while (current != null) {
            String string = current.getValue().getValue();
            Parser.Type type = current.getValue().getType();

            if(type == Parser.Type.FIXED || type == Parser.Type.FLOAT
                    || type == Parser.Type.CHAR || type == Parser.Type.BIT
                    || type == Parser.Type.CLOCK || type == Parser.Type.DURATION
                    || type == Parser.Type.IDENTIFIER || type == Parser.Type.PROCEDURE) {
                outPutQueue.add(new Parser.Node(string, type));
            }
            else if(type == Parser.Type.MONADIC) {
                operators.push(new Parser.Node(string, type));
            }
            else if(type == Parser.Type.DYADIC || type == Parser.Type.OPENBRACKET) {
                while ((!operators.isEmpty() && operators.peek().getType() == Parser.Type.MONADIC )|| ((!operators.isEmpty())
                        && ((operationOrder.get(operators.peek().getName()) < operationOrder.get(string)) ||
                            (operationOrder.get(operators.peek().getName()).equals(operationOrder.get(string)) && operationOrder.get(string) != 0))
                        && (operators.peek().getType() != Parser.Type.OPENBRACKET))) {
                    outPutQueue.add(operators.pop());
                }
                operators.push(new Parser.Node(string, type));
            }
            else if(type == Parser.Type.CLOSEDBRACKED) {
                while (operators.peek().getType() != Parser.Type.OPENBRACKET) {
                    outPutQueue.add(operators.pop());
                }
                if(operators.peek().getType() == Parser.Type.OPENBRACKET) {
                    operators.pop();
                }
            }

            current = current.getNextExpression();
        }
        while (!operators.isEmpty()) {
            outPutQueue.add(operators.pop());
        }
        return recursiveCreateNodeTree(outPutQueue, new Stack<>());
    }

    public static Parser.Node recursiveCreateNodeTree(Queue<Parser.Node> outPutQueue, Stack<Parser.Node> valueHistory) {
        if(outPutQueue.isEmpty()) return valueHistory.pop();

        Parser.Node currentNode = outPutQueue.remove();
        if(currentNode.getType() == Parser.Type.MONADIC) {
            currentNode.pushChild(valueHistory.pop());
            valueHistory.push(currentNode);
            currentNode = recursiveCreateNodeTree(outPutQueue, valueHistory);
        }
        else if(currentNode.getType() == Parser.Type.DYADIC) {
            Parser.Node tmp = valueHistory.pop();
            currentNode.pushChild(valueHistory.pop());
            currentNode.pushChild(tmp);
            valueHistory.push(currentNode);
            currentNode = recursiveCreateNodeTree(outPutQueue, valueHistory);
        }
        else {
            valueHistory.push(currentNode);
            currentNode = recursiveCreateNodeTree(outPutQueue, valueHistory);
        }
        return currentNode;
    }

    public static Map<String, Operations> createVariableValueMap(Parser.Node node, ControlFlowGraphVariableStack variables, Map<String, ParserRuleContext> procedureMap) {
        List<Parser.Node> comparisonNodes = getComparisonNodes(node);
        Map<Parser.Node, Map<String, Operations>> comparisonToValueMap = new HashMap<>();
        comparisonNodes.forEach(comparisonNode -> {
            Map<String, Operations> variableValues = getVariableValueFromComparison(comparisonNode, variables, procedureMap, new HashMap<>());
            comparisonToValueMap.put(comparisonNode, variableValues);
        });

        return getValuesFromComparisonToValueMap(node, comparisonToValueMap);
    }


    private static Map<String, Operations> getValuesFromComparisonToValueMap(Parser.Node node, Map<Parser.Node, Map<String, Operations>> comparisonToValueMap) {
        return getValuesFromComparisonToValueMapRecursive(node, comparisonToValueMap);
    }

    private static Map<String, Operations> getValuesFromComparisonToValueMapRecursive(Parser.Node node, Map<Parser.Node, Map<String, Operations>> comparisonToValueMap) {
        if(comparisonOperatorsMap.containsKey(node.getName())) {
            return comparisonToValueMap.get(node);
        }
        if(bitConnectionOperatorsMap.containsKey(node.getName())) {
            Iterator<Parser.Node> iterator = node.getChildren().iterator();
            Parser.Node leftChild = iterator.next();
            Parser.Node rightChild = iterator.next();

            Map<String, Operations> leftVariables = getValuesFromComparisonToValueMapRecursive(leftChild, comparisonToValueMap);
            comparisonToValueMap.put(leftChild, leftVariables);

            Map<String, Operations> rightVariables = getValuesFromComparisonToValueMapRecursive(rightChild, comparisonToValueMap);
            comparisonToValueMap.put(rightChild, rightVariables);

            List<String> allVariablesNames = getAllIdentifiers(node);
            Map<String, Operations> values = new HashMap<>();
            allVariablesNames.forEach(variableName -> {
                Operations leftVariable = leftVariables.get(variableName);
                Operations rightVariable = rightVariables.get(variableName);
                if(leftVariable != null && rightVariable != null) {
                    Operations value = bitConnectionOperatorsMap.get(node.getName()).apply(leftVariable, rightVariable);
                    values.put(variableName, value);
                }
                else if(leftVariable != null) {
                    values.put(variableName, leftVariable);
                }
                else if(rightVariable != null) {
                    values.put(variableName, rightVariable);
                }
            });
            return values;
        }
        else {
            node.getChildren().forEach(child -> getValuesFromComparisonToValueMapRecursive(child, comparisonToValueMap));
        }
        return null;
    }

    private static Map<String, Operations> getVariableValueFromComparison(Parser.Node node, ControlFlowGraphVariableStack variables, Map<String, ParserRuleContext> procedureMap, Map<String, Operations> variableValues) {
        Iterator<Parser.Node> iterator = node.getChildren().iterator();
        Parser.Node defaultLeftSide = iterator.next();
        Parser.Node defaultRightSide = iterator.next();

        Map<String, Operations> allLeftValues = new HashMap<>();
        Map<String, Operations> allRightValues = new HashMap<>();

        Parser.Node leftSide = (Parser.Node) defaultLeftSide.deepClone();
        Parser.Node rightSide = (Parser.Node) defaultRightSide.deepClone();

        reverseComparisonForIdentifierRecursive(leftSide, rightSide, variables, procedureMap, variableValues, allLeftValues, allRightValues);

        leftSide = (Parser.Node) defaultLeftSide.deepClone();
        rightSide = (Parser.Node) defaultRightSide.deepClone();

        reverseComparisonForIdentifierRecursive(rightSide, leftSide, variables, procedureMap, variableValues, allRightValues, allLeftValues);

        List<String> identifiers = getAllIdentifiers(node);

        Map<String, Operations> result = new HashMap<>();
        identifiers.forEach(identifier -> {
            Operations value = variableValues.get(identifier);
            if(value == null)
                value = variables.getDeepestVariableByName(identifier);

            Operations leftCheckValue = allLeftValues.get(identifier);
            if(leftCheckValue != null) {
                if(value instanceof FixedVariableValue && leftCheckValue instanceof FloatVariableValue)
                    leftCheckValue = leftCheckValue.entier();
                result.put(identifier, value.h_and(comparisonOperatorsMap.get(node.getName()).apply(leftCheckValue)));
            }

            Operations rightCheckValue = allRightValues.get(identifier);
            if(rightCheckValue != null) {
                if(value instanceof FixedVariableValue && rightCheckValue instanceof FloatVariableValue)
                    rightCheckValue = rightCheckValue.entier();
                if(result.containsKey(identifier)) {
                    Operations oldValue = result.get(identifier);
                    result.put(identifier, oldValue.h_and(reverseComparisonOperatorsMap.get(node.getName()).apply(rightCheckValue)));
                }
                else {
                    result.put(identifier, value.h_and(reverseComparisonOperatorsMap.get(node.getName()).apply(rightCheckValue)));
                }
            }

            if(!result.containsKey(identifier)) {
                result.put(identifier, value);
            }
        });

        return result;
    }

    private static void reverseComparisonForIdentifierRecursive(Parser.Node toReverse, Parser.Node otherSide, ControlFlowGraphVariableStack variables, Map<String, ParserRuleContext> procedureMap, Map<String, Operations> variableValues, Map<String, Operations> result, Map<String, Operations> otherSideResult) {
        if(toReverse.getType() == Parser.Type.DYADIC) {
            String reverseDyadic = reverseDyadicOperationMap.get(toReverse.getName());
            if(reverseDyadic != null) {
                Iterator<Parser.Node> iterator = toReverse.getChildren().iterator();
                Parser.Node child = iterator.next();
                Parser.Node otherChild = iterator.next();

                for(int i = 0; i < 2; i++) {
                    Parser.Node newOtherSide = new Parser.Node(reverseDyadic, Parser.Type.DYADIC);
                    newOtherSide.pushChild(otherSide);
                    newOtherSide.pushChild(otherChild);

                    if(toReverse.getName().equals("/") && i == 1) {
                        if(child.getType() == Parser.Type.IDENTIFIER) {
                            if(!otherSideResult.containsKey(child.getName())) {
                                OperationNode resultCalculation = buildOperationNodeTree(newOtherSide, variables, procedureMap, variableValues);
                                if(!getAllIdentifiers(newOtherSide).contains(child.getName())) {
                                    otherSideResult.put(child.getName(), resultCalculation.calculate());
                                }
                            }
                        }
                        else reverseComparisonForIdentifierRecursive(child, newOtherSide, variables, procedureMap, variableValues, otherSideResult, result);
                    }
                    else {
                        if(child.getType() == Parser.Type.IDENTIFIER) {
                            if(!result.containsKey(child.getName())) {
                                OperationNode resultCalculation = buildOperationNodeTree(newOtherSide, variables, procedureMap, variableValues);
                                if(!getAllIdentifiers(newOtherSide).contains(child.getName())) {
                                    result.put(child.getName(), resultCalculation.calculate());
                                }
                            }
                        }
                        else reverseComparisonForIdentifierRecursive(child, newOtherSide, variables, procedureMap, variableValues, result, otherSideResult);
                    }

                    Parser.Node tmp = child;
                    child = otherChild;
                    otherChild = tmp;
                }

            }
        }
        else if(toReverse.getType() == Parser.Type.IDENTIFIER) {
            if(!result.containsKey(toReverse.getName())) {
                OperationNode resultCalculation = buildOperationNodeTree(otherSide, variables, procedureMap, variableValues);
                if(!getAllIdentifiers(otherSide).contains(toReverse.getName())) {
                    result.put(toReverse.getName(), resultCalculation.calculate());
                }
            }
        }
    }
    private static List<String> getAllIdentifiers(Parser.Node node) {
        Set<String> result = new HashSet<>();
        getAllIdentifiersRecursive(node, result);
        return new ArrayList<>(result);
    }

    private static void getAllIdentifiersRecursive(Parser.Node node, Set<String> result) {
        if(node.getType() == Parser.Type.IDENTIFIER) {
            result.add(node.getName());
        }
        else {
            node.getChildren().forEach(child -> getAllIdentifiersRecursive(child, result));
        }
    }

    private static List<Parser.Node> getComparisonNodes(Parser.Node node) {
        List<Parser.Node> result = new ArrayList<>();
        getComparisonNodesRecursive(node, result);
        return result;
    }

    private static void getComparisonNodesRecursive(Parser.Node currentNode, List<Parser.Node> result) {
        if(comparisonOperatorsMap.containsKey(currentNode.getName()))
            result.add(currentNode);
        else
            currentNode.getChildren().forEach(node -> getComparisonNodesRecursive(node, result));
    }

    private static final HashMap<String, BiFunction<Operations, Operations, Operations>> bitConnectionOperatorsMap = new HashMap<>();
    private static final HashMap<String, Function<Operations, Operations>> comparisonOperatorsMap = new HashMap<>();
    private static final HashMap<String, Function<Operations, Operations>> reverseComparisonOperatorsMap = new HashMap<>();
    static {
        bitConnectionOperatorsMap.put("AND", Operations::h_and);
        bitConnectionOperatorsMap.put("OR", Operations::h_or);
        bitConnectionOperatorsMap.put("XOR", Operations::h_xor);

        comparisonOperatorsMap.put("<", Operations::h_less);
        comparisonOperatorsMap.put("LT", Operations::h_lessEqual);
        comparisonOperatorsMap.put(">", Operations::h_greater);
        comparisonOperatorsMap.put("GT", Operations::h_greater);
        comparisonOperatorsMap.put("<=", Operations::h_lessEqual);
        comparisonOperatorsMap.put("LE", Operations::h_lessEqual);
        comparisonOperatorsMap.put(">=", Operations::h_greaterEqual);
        comparisonOperatorsMap.put("GE", Operations::h_greaterEqual);
        comparisonOperatorsMap.put("==", Operations::noOp);
        comparisonOperatorsMap.put("EQ", Operations::noOp);
        comparisonOperatorsMap.put("/=", Operations::h_notEqual);
        comparisonOperatorsMap.put("NE", Operations::h_notEqual);

        reverseComparisonOperatorsMap.put("<", Operations::h_greater);
        reverseComparisonOperatorsMap.put("LT", Operations::h_greater);
        reverseComparisonOperatorsMap.put(">", Operations::h_less);
        reverseComparisonOperatorsMap.put("GT", Operations::h_less);
        reverseComparisonOperatorsMap.put("<=", Operations::h_greaterEqual);
        reverseComparisonOperatorsMap.put("LE", Operations::h_greaterEqual);
        reverseComparisonOperatorsMap.put(">=", Operations::h_lessEqual);
        reverseComparisonOperatorsMap.put("GE", Operations::h_lessEqual);
        reverseComparisonOperatorsMap.put("==", Operations::noOp);
        reverseComparisonOperatorsMap.put("EQ", Operations::noOp);
        reverseComparisonOperatorsMap.put("/=", Operations::h_notEqual);
        reverseComparisonOperatorsMap.put("NE", Operations::h_notEqual);
    }

    private static final HashMap<String, BiFunction<Operations, Operations, Operations>> dyadicOperationMap = new HashMap<>();
    private static final HashMap<String, Function<Operations, Operations>> monadicOperationMap = new HashMap<>();
    private static final HashMap<String, String> reverseDyadicOperationMap = new HashMap<>();
    static {
        monadicOperationMap.put("+", Operations::noOp);
        monadicOperationMap.put("-", Operations::negate);
        monadicOperationMap.put("NOT", Operations::not);
        monadicOperationMap.put("ABS", Operations::abs);
        monadicOperationMap.put("SIGN", Operations::sign);
        monadicOperationMap.put("TOFIXED", Operations::toFixed);
        monadicOperationMap.put("TOFLOAT", Operations::toFloat);
        monadicOperationMap.put("TOBIT", Operations::toBit);
        monadicOperationMap.put("TOCHAR", Operations::toChar);
        monadicOperationMap.put("ENTIER", Operations::entier);
        monadicOperationMap.put("ROUND", Operations::round);
        monadicOperationMap.put("CONT", Operations::cont);
        monadicOperationMap.put("SQRT", Operations::sqrt);
        monadicOperationMap.put("SIN", Operations::sin);
        monadicOperationMap.put("COS", Operations::cos);
        monadicOperationMap.put("EXP", Operations::e_exp);
        monadicOperationMap.put("LN", Operations::ln);
        monadicOperationMap.put("TAN", Operations::tan);
        monadicOperationMap.put("ATAN", Operations::atan);
        monadicOperationMap.put("TANH", Operations::tanh);
        monadicOperationMap.put("LWB", Operations::lwb);
        monadicOperationMap.put("UPB", Operations::upb);
        monadicOperationMap.put("SIZEOF", Operations::sizeof);
        monadicOperationMap.put("SIZEOF MAX", Operations::sizeofMax);
        monadicOperationMap.put("SIZEOF LENGTH", Operations::sizeofLength);
        monadicOperationMap.put("TRY", Operations::noOp);

        dyadicOperationMap.put("+", Operations::add);
        dyadicOperationMap.put("-", Operations::sub);
        dyadicOperationMap.put("*", Operations::mul);
        dyadicOperationMap.put("/", Operations::div);
        dyadicOperationMap.put("//", Operations::intDiv);
        dyadicOperationMap.put("REM", Operations::rem);
        dyadicOperationMap.put("**", Operations::exp);
        dyadicOperationMap.put("<", Operations::less);
        dyadicOperationMap.put("LT", Operations::lessEqual);
        dyadicOperationMap.put(">", Operations::greater);
        dyadicOperationMap.put("GT", Operations::greater);
        dyadicOperationMap.put("<=", Operations::lessEqual);
        dyadicOperationMap.put("LE", Operations::lessEqual);
        dyadicOperationMap.put(">=", Operations::greaterEqual);
        dyadicOperationMap.put("GE", Operations::greaterEqual);
        dyadicOperationMap.put("==", Operations::equal);
        dyadicOperationMap.put("EQ", Operations::equal);
        dyadicOperationMap.put("/=", Operations::notEqual);
        dyadicOperationMap.put("NE", Operations::notEqual);
        dyadicOperationMap.put("IS", Operations::is);
        dyadicOperationMap.put("ISNT", Operations::isnt);
        dyadicOperationMap.put("AND", Operations::and);
        dyadicOperationMap.put("OR", Operations::or);
        dyadicOperationMap.put("EXOR", Operations::exor);
        dyadicOperationMap.put("<>", Operations::cShift);
        dyadicOperationMap.put("CSHIFT", Operations::cShift);
        dyadicOperationMap.put("><", Operations::concat);
        dyadicOperationMap.put("CAT", Operations::concat);
        dyadicOperationMap.put("LWB", Operations::lwb);
        dyadicOperationMap.put("UPB", Operations::upb);

        reverseDyadicOperationMap.put("+", "-");
        reverseDyadicOperationMap.put("-", "+");
        reverseDyadicOperationMap.put("*", "/");
        reverseDyadicOperationMap.put("/", "*");
    }

    // returns an object which allows one to calculate an expression. the possible values of all variables are returned with currentVariableValues
    private static OperationNode buildOperationNodeTree(Parser.Node node, ControlFlowGraphVariableStack variables, Map<String, ParserRuleContext> procedureMap, Map<String, Operations> currentVariableValues) {
        if(node.getType() == Parser.Type.FIXED) {
            return new OperationNode(new FixedVariableValue(new ConstantFixedValue(Long.parseLong(node.getName()))));
        }
        else if(node.getType() == Parser.Type.FLOAT) {
            return new OperationNode(new FloatVariableValue(new ConstantFloatValue(Double.parseDouble(node.getName()), 52)));
        }
        else if(node.getType() == Parser.Type.BIT) {
            String bitString = node.getName().substring(0, node.getName().lastIndexOf(" "));
            String type = node.getName().substring(node.getName().lastIndexOf(" ")+1);
            if(type.equals("B1")) {
                long value = new BigInteger(bitString, 2).longValue();
                int length = bitString.length();
                return new OperationNode(new BitVariableValue(new ConstantBitValue(value, length)));
            }
            else if(type.equals("B2")) {
                long value = new BigInteger(bitString, 4).longValue();
                int length = bitString.length() * 2;
                return new OperationNode(new BitVariableValue(new ConstantBitValue(value, length)));
            }
            else if(type.equals("B3")) {
                long value = new BigInteger(bitString, 8).longValue();
                int length = bitString.length() * 4;
                return new OperationNode(new BitVariableValue(new ConstantBitValue(value, length)));
            }
            else {
                long value = new BigInteger(bitString, 16).longValue();
                int length = bitString.length() * 8;
                return new OperationNode(new BitVariableValue(new ConstantBitValue(value, length)));
            }
        }
        else if(node.getType() == Parser.Type.CHAR) {
            return new OperationNode(new CharVariableValue(new ConstantCharacterValue(node.getName())));
        }
        else if(node.getType() == Parser.Type.CLOCK) {
            String currentExpression = node.getName();
            List<Double> values = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
                int index = currentExpression.indexOf(":");
                if(index == -1) {
                    values.add(Double.parseDouble(currentExpression));
                }
                else {
                    values.add(Double.parseDouble(currentExpression.substring(0, index)));
                    currentExpression = currentExpression.substring(index+1);
                }
            }
            return new OperationNode(new ClockVariableValue(new ConstantClockValue(values.get(0).intValue(), values.get(1).intValue(), values.get(2))));
        }
        else if(node.getType() == Parser.Type.DURATION) {
            String currentExpression = node.getName();
            List<Double> values = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
                int index = currentExpression.indexOf(":");
                if(index == -1) {
                    values.add(Double.parseDouble(currentExpression));
                }
                else {
                    values.add(Double.parseDouble(currentExpression.substring(0, index)));
                    currentExpression = currentExpression.substring(index+1);
                }
            }
            return new OperationNode(new DurationVariableValue(new ConstantDurationValue(values.get(0).longValue(), values.get(1).intValue(), values.get(2))));
        }
        else if(node.getType() == Parser.Type.IDENTIFIER) {
            if(currentVariableValues.containsKey(node.getName())) {
                return new OperationNode(currentVariableValues.get(node.getName()));
            }
            Operations value = variables.getDeepestVariableByName(node.getName());

            if(value != null && !currentVariableValues.containsKey(node.getName()))
                currentVariableValues.put(node.getName(), value);

            return new OperationNode(value);
        }
        else if(node.getType() == Parser.Type.PROCEDURE) {
            return procedure(node, procedureMap);
        }
        else if(node.getType() == Parser.Type.BRACKET) {
            Parser.Node innerNode = node.getChildren().iterator().next();
            return buildOperationNodeTree(innerNode, variables, procedureMap, currentVariableValues);
        }
        else if(node.getType() == Parser.Type.MONADIC) {
            Parser.Node innerNode = node.getChildren().iterator().next();
            OperationNode value = buildOperationNodeTree(innerNode, variables, procedureMap, currentVariableValues);
            return new OperationNode(value, monadicOperationMap.get(node.getName()));
        }

        else if(node.getType() == Parser.Type.DYADIC) {
            Parser.Node leftNode = node.getChildren().get(0);
            Parser.Node rightNode = node.getChildren().get(1);
            Map<String, Operations> currentVariableValuesCopy = new HashMap<>(currentVariableValues);
            OperationNode leftValue = buildOperationNodeTree(leftNode, variables, procedureMap, currentVariableValues);
            OperationNode rightValue;
            if(node.getName().equals("OR") || node.getName().equals("XOR")) {
                List<String> identifiers = getAllIdentifiers(leftNode);
                Map<String, Operations> newValues = createVariableValueMap(leftNode, variables, procedureMap);
                Map<String, Operations> correctValues = new HashMap<>();
                for(String identifier : identifiers) {
                    Operations value = newValues.get(identifier);
                    if(currentVariableValuesCopy.get(identifier) != null) {
                        currentVariableValues.put(identifier, value.h_notEqual().h_and(currentVariableValuesCopy.get(identifier)));
                        correctValues.put(identifier, value.h_and(currentVariableValuesCopy.get(identifier)));
                        currentVariableValuesCopy.put(identifier, value.h_and(currentVariableValuesCopy.get(identifier)));
                    }
                    else {
                        Operations oldValue = variables.getDeepestVariableByName(identifier);
                        if(oldValue != null){
                            currentVariableValues.put(identifier, value.h_notEqual().h_and(oldValue));
                            correctValues.put(identifier, value.h_and(oldValue));
                            currentVariableValuesCopy.put(identifier, value.h_and(oldValue));
                        }
                    }
                }
                rightValue = buildOperationNodeTree(rightNode, variables, procedureMap, currentVariableValues);

                Map<String, Operations> afterValues = new HashMap<>();
                currentVariableValues.forEach((identifier, value) -> {
                    afterValues.put(identifier, correctValues.getOrDefault(identifier, value));
                });

                boolean leftSideHasNoValue = false;
                boolean rightSideHasNoValue = false;
                for(Operations value : currentVariableValuesCopy.values()) {
                    if(value.hasNoValue()) {
                        leftSideHasNoValue = true;
                        break;
                    }
                }
                for(Operations value : currentVariableValues.values()) {
                    if(value.hasNoValue()) {
                        rightSideHasNoValue = true;
                        break;
                    }
                }
                if(leftSideHasNoValue && rightSideHasNoValue) {
                    currentVariableValuesCopy.forEach((name, value) -> {
                        if(value.hasNoValue())
                            currentVariableValues.put(name, value);
                    });
                }
                else if(rightSideHasNoValue) {
                    currentVariableValues.clear();
                    currentVariableValues.putAll(currentVariableValuesCopy);
                }
                else if(!leftSideHasNoValue){
                    currentVariableValues.forEach((name, value) -> {
                        Operations other = currentVariableValuesCopy.get(name);
                        if(other != null)
                            currentVariableValues.put(name, currentVariableValues.get(name).h_or(other));
                        else {
                            currentVariableValues.put(name, currentVariableValues.get(name).h_or(variables.getDeepestVariableByName(name)));
                        }
                    });
                }

            }
            else if(node.getName().equals("AND")) {
                rightValue = buildOperationNodeTree(rightNode, variables, procedureMap, currentVariableValues);
            }
            else if(comparisonOperatorsMap.containsKey(node.getName())) {
                rightValue = buildOperationNodeTree(rightNode, variables, procedureMap, currentVariableValues);
                Map<String, Operations> newValues = getVariableValueFromComparison(node, variables, procedureMap, currentVariableValues);
                newValues.forEach((name, value) -> {
                    currentVariableValues.put(name, currentVariableValues.get(name).h_and(value));
                });
            }
            else {
                rightValue = buildOperationNodeTree(rightNode, variables, procedureMap, currentVariableValues);
            }

            return new OperationNode(leftValue, rightValue, dyadicOperationMap.get(node.getName()));
        }
        return null;
    }

    private static OperationNode procedure(Parser.Node node, Map<String, ParserRuleContext> procedureMap) {
        ParserRuleContext procCtx = procedureMap.get(node.getName());
        if(procCtx != null) {
            SmallPearlParser.ProcedureDeclarationContext procDecCtx = (SmallPearlParser.ProcedureDeclarationContext) procCtx;
            SmallPearlParser.TypeProcedureContext type = procDecCtx.typeProcedure();
            if (type != null) {
                SmallPearlParser.ResultAttributeContext resultAttr = type.resultAttribute();
                if (resultAttr != null) {
                    SmallPearlParser.ResultTypeContext resultType = resultAttr.resultType();
                    if (resultType != null) {
                        String typeString = resultType.getText();
                        if (typeString.equals("FIXED")) {
                            return new OperationNode(new FixedVariableValue(new VariableValueRange<>(
                                    new ConstantFixedValue(Long.MIN_VALUE),
                                    new ConstantFixedValue(Long.MAX_VALUE)
                            )));
                        } else if (typeString.equals("FLOAT")) {
                            return new OperationNode(new FloatVariableValue(new VariableValueRange<>(
                                    new ConstantFloatValue(Float.MIN_VALUE, 52),
                                    new ConstantFloatValue(Float.MAX_VALUE, 52)
                            )));
                        } else if (typeString.equals("BIT")) {
                            return new OperationNode(new BitVariableValue(new VariableValueRange<>(
                                    new ConstantBitValue(Long.MIN_VALUE, 63),
                                    new ConstantBitValue(Long.MAX_VALUE, 63)
                            )));
                        } else if (typeString.equals("DURATION")) {
                            return new OperationNode(new DurationVariableValue(new VariableValueRange<>(
                                    new ConstantDurationValue(0L, 0, 0.),
                                    new ConstantDurationValue(3600, 60, 60.0)
                            )));
                        } else if (typeString.equals("CLOCK")) {
                            return new OperationNode(new ClockVariableValue(new VariableValueRange<>(
                                    new ConstantClockValue(0, 0, 0.),
                                    new ConstantClockValue(Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE)
                            )));
                        } else if (typeString.equals("CHAR")) {
                            return new OperationNode(new CharVariableValue());
                        }
                    }
                }
            }
        } else {
            // predefined procedure
            if (node.getName().equals("NOW")) {
                return new OperationNode(new ClockVariableValue(new VariableValueRange<>(
                        new ConstantClockValue(0, 0, 0.),
                        new ConstantClockValue(Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE)
                )));
            } else if  (node.getName().equals("NOW")) {
                return new OperationNode(new CharVariableValue());
            }
        }
        return null;
    }


    // Used to execute a complex calculation with multiple commands
    public static class OperationNode {
        private OperationNode node1;
        private OperationNode node2;

        private Operations value;
        private String identifier;

        private Function<Operations, Operations> monadicFunc;
        private BiFunction<Operations, Operations, Operations> dyadicFunc;

        public OperationNode(OperationNode node1, Function<Operations, Operations> monadicFunc) {
            this.node1 = node1;
            this.node2 = null;
            this.value = null;
            this.identifier = null;
            this.monadicFunc = monadicFunc;
            this.dyadicFunc = null;
        }

        public OperationNode(OperationNode node1, OperationNode node2, BiFunction<Operations, Operations, Operations> dyadicFunc) {
            this.node1 = node1;
            this.node2 = node2;
            this.value = null;
            this.identifier = null;
            this.monadicFunc = null;
            this.dyadicFunc = dyadicFunc;
        }

        public OperationNode(Operations value) {
            this.node1 = null;
            this.node2 = null;
            this.value = value;
            this.identifier = null;
            this.monadicFunc = null;
            this.dyadicFunc = null;
        }

        public OperationNode(Operations value, String identifier) {
            this.node1 = null;
            this.node2 = null;
            this.value = value;
            this.identifier = identifier;
            this.monadicFunc = null;
            this.dyadicFunc = null;
        }

        public boolean isConstant() {
            return value != null;
        }

        public Operations calculate() {
            if(value != null) return value;
            else if(node1 != null && node2 != null) {
                return dyadicFunc.apply(node1.calculate(), node2.calculate());
            }
            else if(node1 != null){
                return monadicFunc.apply(node1.calculate());
            }
            else return null;
        }
    }
}
