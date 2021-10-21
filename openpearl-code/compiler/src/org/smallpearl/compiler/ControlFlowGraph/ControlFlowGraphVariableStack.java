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

import org.smallpearl.compiler.*;

import java.util.*;

public class ControlFlowGraphVariableStack {
    private Map<String, Map<Integer, Operations>> variables;

    public ControlFlowGraphVariableStack() {
        variables = new HashMap<>();
    }

    public ControlFlowGraphVariableStack(ControlFlowGraphVariableStack lastStack, int untilDepth) {
        variables = createCopy(lastStack.variables, untilDepth);
    }

    public void combine(ControlFlowGraphVariableStack with, int untilDepth) {
        combine(variables, with.variables, untilDepth);
    }
    public Map<String, Map<Integer, Operations>> getVariables() { return variables; }
    public Map<Integer, Operations> getVariableByName(String name) { return variables.get(name); }

    public Operations getVariableByNameAndDepth(String name, int depth) {
        Map<Integer, Operations> variableMap = getVariableByName(name);
        if(variableMap == null) return null;
        Operations variable = variableMap.get(depth);
        if(variable == null) return null;
        return variable;
    }

    private <T> Integer getMaxDepthOfVariable(String name) {
        Map<Integer, Operations> found = variables.get(name);
        if(found == null) return null;
        if(found.keySet().size() == 0) return null;
        return Collections.max(found.keySet());
    }

    public Operations getDeepestVariableByName(String name) {
        Integer depth = getMaxDepthOfVariable(name);
        if(depth == null) return null;
        return variables.get(name).get(depth);
    }

    public <T extends Operations> void addVariable(String name, int depth, T value) {
        Map<Integer, Operations> valueMap = variables.getOrDefault(name, new HashMap<>());
        valueMap.put(depth, value);
        variables.put(name, valueMap);
    }

    public <T extends Operations> void setVariable(String variable, T value) {
        Map<Integer, Operations> valueMap = variables.get(variable);
        if(valueMap != null && !valueMap.isEmpty()) {
            Integer deepest = Collections.max(valueMap.keySet());
            if(deepest == null)
                return;
            valueMap.put(deepest, value);
        }
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();
        variables.forEach((name, values) -> {
            if(values.isEmpty())
                return;
            Operations deepest = getDeepestVariableByName(name);
            if(deepest == null) return;
            result.append(name + deepest.toString());

            result.append("\n");
        });
        return result.toString();
    }

    private Map<String, Map<Integer, Operations>> createCopy(Map<String, Map<Integer, Operations>> variableMap, int untilDepth) {
        Map<String, Map<Integer, Operations>> result = new HashMap<>();
        variableMap.forEach((name, map) -> {
            Map<Integer, Operations> newMap = new HashMap<>();
            map.forEach((depth, values) -> {
                if(depth > untilDepth)
                    return;
                newMap.put(depth, (Operations) values.deepClone());
            });
            result.put(name, newMap);
        });
        return result;
    }

    private void combine(Map<String, Map<Integer, Operations>> a, Map<String, Map<Integer, Operations>> with, int untilDepth) {
        Map<String, Map<Integer, Operations>> copy = createCopy(with, untilDepth);
        copy.forEach((variable, values) -> {
            values.forEach((depth, value) -> {
                Operations other = a.get(variable).get(depth);
                if(other instanceof FixedVariableValue) {
                    FixedVariableValue otherC = (FixedVariableValue) other;
                    FixedVariableValue valueC = (FixedVariableValue) value;

                    if(!valueC.isKnown() || !otherC.isKnown()) otherC.setKnown(false);
                    otherC.getValues().addAll(valueC.getValues());
                    otherC.setValues(removeFixedDuplicate(otherC.values));
                }
                else if(other instanceof FloatVariableValue) {
                    FloatVariableValue otherC = (FloatVariableValue) other;
                    FloatVariableValue valueC = (FloatVariableValue) value;

                    if(!valueC.isKnown() || !otherC.isKnown()) otherC.setKnown(false);
                    otherC.getValues().addAll(valueC.getValues());
                    otherC.setValues(removeDuplicate(otherC.values));
                }
                else if(other instanceof CharVariableValue) {
                    CharVariableValue otherC = (CharVariableValue) other;
                    CharVariableValue valueC = (CharVariableValue) value;

                    if(!valueC.isKnown() || !otherC.isKnown()) otherC.setKnown(false);
                    otherC.getValues().addAll(valueC.getValues());
                    otherC.setValues(removeDuplicate(otherC.values));
                }
                else if(other instanceof BitVariableValue) {
                    BitVariableValue otherC = (BitVariableValue) other;
                    BitVariableValue valueC = (BitVariableValue) value;

                    if(!valueC.isKnown() || !otherC.isKnown()) otherC.setKnown(false);
                    otherC.getValues().addAll(valueC.getValues());
                    otherC.setValues(removeDuplicate(otherC.values));
                }
                else if(other instanceof DurationVariableValue) {
                    DurationVariableValue otherC = (DurationVariableValue) other;
                    DurationVariableValue valueC = (DurationVariableValue) value;

                    if(!valueC.isKnown() || !otherC.isKnown()) otherC.setKnown(false);
                    otherC.getValues().addAll(valueC.getValues());
                    otherC.setValues(removeDuplicate(otherC.values));
                }
                else if(other instanceof ClockVariableValue) {
                    ClockVariableValue otherC = (ClockVariableValue) other;
                    ClockVariableValue valueC = (ClockVariableValue) value;

                    if(!valueC.isKnown() || !otherC.isKnown()) otherC.setKnown(false);
                    otherC.getValues().addAll(valueC.getValues());
                    otherC.setValues(removeDuplicate(otherC.values));
                }
            });
        });
    }

    private Set<VariableValueRange<ConstantFixedValue>> removeFixedDuplicate(Set<VariableValueRange<ConstantFixedValue>> values) {
        if(values.size() == 0) return values;
        List<VariableValueRange<ConstantFixedValue>> newList = new ArrayList<>(values);
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        newList.sort(Comparator.comparing(VariableValueRange::getFrom));

        Iterator<VariableValueRange<ConstantFixedValue>> iterator = newList.iterator();
        VariableValueRange<ConstantFixedValue> current = iterator.next();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantFixedValue> next = iterator.next();
            if(current.getTo().getValue() >= next.getFrom().getValue()) {
                ConstantFixedValue max = current.getTo().compareTo(next.getTo()) > 0 ? current.getTo() : next.getTo();
                current.setTo(max);
            }
            else {
                result.add(current);
                current = next;
            }
        }
        result.add(current);
        return result;
    }

    private <T extends Comparable<T>> Set<VariableValueRange<T>> removeDuplicate(Set<VariableValueRange<T>> values) {
        if(values.size() == 0) return values;
        List<VariableValueRange<T>> newList = new ArrayList<>(values);
        Set<VariableValueRange<T>> result = new HashSet<>();
        newList.sort(Comparator.comparing(VariableValueRange::getFrom));

        Iterator<VariableValueRange<T>> iterator = newList.iterator();
        VariableValueRange<T> current = iterator.next();
        while (iterator.hasNext()) {
            VariableValueRange<T> next = iterator.next();
            if(current.getTo().compareTo(next.getFrom()) >= 0) {
                T max = current.getTo().compareTo(next.getTo()) > 0 ? current.getTo() : next.getTo();
                current.setTo(max);
            }
            else {
                result.add(current);
                current = next;
            }
        }
        result.add(current);
        return result;
    }
}
