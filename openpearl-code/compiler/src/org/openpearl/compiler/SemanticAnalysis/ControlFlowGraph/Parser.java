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

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;

// used to Parse a String into a Data structure, which is easier to work with
public class Parser {
    private String program;
    private int position;

    public Parser(String program) {
        this.program = program;
        this.position = 0;
    }

    public Expression parseExpression(Map<String, ParserRuleContext> procedureMap, ControlFlowGraphVariableStack variables, boolean advanceClosedBracket) {
        Expression openBracket = parseOpenBracket();
        if(openBracket != null) {
            Expression expression = parseExpression(procedureMap, variables, advanceClosedBracket);
            if(expression != null) {
                openBracket.setNextExpression(expression);
                expression.setLastExpression(openBracket);
                Expression dyadic = parseDyadic();
                if(dyadic != null) {
                    Expression other = parseExpression(procedureMap, variables, advanceClosedBracket);
                    if(other != null) {
                        Expression expressionEnd = expression;
                        while (expressionEnd.nextExpression != null)
                            expressionEnd = expressionEnd.nextExpression;
                        expressionEnd.setNextExpression(dyadic);
                        dyadic.setLastExpression(expressionEnd);
                        dyadic.setNextExpression(other);
                        other.setLastExpression(dyadic);
                        return openBracket;
                    }
                    else return null;
                }
                else return openBracket;
            }
            else return null;
        }
        else {
            Expression monadic = parseMonadic();
            if(monadic != null) {
                Expression expression = parseExpression(procedureMap, variables, advanceClosedBracket);
                if(expression != null) {
                    Expression suffix = parseMonadicSuffix();
                    if(suffix != null) {
                        monadic.value.value = monadic.value.value + " " + suffix.value.value;
                    }
                    monadic.setNextExpression(expression);
                    expression.setLastExpression(monadic);
                    Expression expressionEnd = expression;
                    while (expressionEnd.nextExpression != null)
                        expressionEnd = expressionEnd.nextExpression;

                    Expression dyadic = parseDyadic();
                    if (dyadic != null) {
                        expressionEnd.setNextExpression(dyadic);
                        dyadic.setLastExpression(expressionEnd);
                        Expression other = parseExpression(procedureMap, variables, advanceClosedBracket);
                        if (other != null) {
                            dyadic.setNextExpression(other);
                            other.setLastExpression(dyadic);
                            return monadic;
                        }
                        else return null;
                    }
                    else return monadic;
                }
                else return null;
            }
            else {
                Expression value = null;
                if(value == null)
                    value = parseClock();
                if(value == null)
                    value = parseDuration();
                if(value == null)
                    value = parseBit();
                if(value == null)
                    value = parseChar();
                if(value == null)
                    value = parseFixed();
                if(value == null)
                    value = parseFloat();
                if(value == null)
                    value = parseIdentifier(procedureMap, variables);

                if(value != null) {
                    Expression multiplicationOpenBracket = parseOpenBracket();
                    if(multiplicationOpenBracket != null) {
                        Expression multiplication = new Expression(new Expression.Value("*", Type.DYADIC));
                        value.setNextExpression(multiplication);
                        multiplication.setLastExpression(value);
                        multiplication.setNextExpression(multiplicationOpenBracket);
                        multiplicationOpenBracket.setLastExpression(multiplication);

                        Expression expression = parseExpression(procedureMap, variables, false);
                        multiplicationOpenBracket.setNextExpression(expression);
                        expression.setLastExpression(multiplicationOpenBracket);

                        Expression closedBracket = parseClosedBracket(true);
                        if(closedBracket != null) {
                            expression.setNextExpression(closedBracket);
                            closedBracket.setLastExpression(expression);
                            return value;
                        }
                        return null;
                    }
                    Expression closedBracket = parseClosedBracket(advanceClosedBracket);
                    if(closedBracket != null) {
                        value.setNextExpression(closedBracket);
                        closedBracket.setLastExpression(value);
                        return value;
                    }

                    Expression dyadic = parseDyadic();
                    if (dyadic != null) {
                        value.setNextExpression(dyadic);
                        dyadic.setLastExpression(value);
                        Expression other = parseExpression(procedureMap, variables, advanceClosedBracket);
                        if (other != null) {
                            dyadic.setNextExpression(other);
                            other.setLastExpression(dyadic);
                            return value;
                        }
                        else return null;
                    }
                    else return value;
                }
                else return null;
            }
        }
    }

    private void skipWhiteSpace() {
        for(; position < program.length(); position++) {
            if(!Character.isWhitespace(program.charAt(position))) return;
        }
    }


    private Expression parseIdentifier(Map<String, ParserRuleContext> procedureMap, ControlFlowGraphVariableStack variables) {
        skipWhiteSpace();
        StringBuilder result = new StringBuilder();
        StringBuilder lastResult = null;
        int i = 0;
        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9' || c =='.' || c == '_')) {
                result.append(c);
                if(procedureMap != null && (procedureMap.containsKey(result.toString())
                        || variables.getVariableByName(result.toString()) != null)) {
                    lastResult = new StringBuilder();
                    lastResult.append(result.toString());
                }
            }
            else break;
        }
        if(procedureMap != null && (!procedureMap.containsKey(result.toString()) &&
                variables.getVariableByName(result.toString()) == null)) {
            result = lastResult;
        }

        if(result == null || result.length() == 0) return null;
        position += result.length();
        if(procedureMap != null && procedureMap.containsKey(result.toString())) {
            if(parseOpenBracket() != null) {
                Expression outerNode = new Expression(new Expression.Value(result.toString(), Type.PROCEDURE));
                    /*
                    while (true) {
                        Expression innerNode = parseExpression(procedureMap, false);
                        if(innerNode != null) {
                            outerNode.setNextExpression(innerNode);
                            testInputAndAdvance(",", true);
                            if(parseClosedBracket(true) != null)
                                break;
                        }
                    }
                     */
                while (true) {
                    Expression innerNode = parseExpression(procedureMap, variables, false);
                    if(innerNode != null) {
                        testInputAndAdvance(",", true);
                        if(parseClosedBracket(true) != null) {
                            return outerNode;
                        }
                    }
                }
            }
            else {
                return new Expression(new Expression.Value(result.toString(), Type.PROCEDURE));
            }
        }
        else if (variables.getDeepestVariableByName(result.toString()) != null)
            return new Expression(new Expression.Value(result.toString(), Type.IDENTIFIER));
        else return null;
    }

    private Expression parseFixed() {
        skipWhiteSpace();
        StringBuilder result = new StringBuilder();
        int i = 0;
        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if(c >= '0' && c <= '9')
                result.append(c);
            else if(c == 'E' || c == '.')
                return null;
            else break;
        }
        if(result.length() == 0) return null;
        position += i;
        return new Expression(new Expression.Value(result.toString(), Type.FIXED));
    }

    private Expression parseClock() {
        skipWhiteSpace();
        StringBuilder hours = new StringBuilder();
        StringBuilder minutes = new StringBuilder();
        StringBuilder seconds = new StringBuilder();
        int breakCount = 0;
        int i = 0;
        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if(c >= '0' && c <= '9')
                hours.append(c);
            else if(c == ':') {
                breakCount++;
                break;
            }
            else return null;
        }
        i++;
        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if(c >= '0' && c <= '9')
                minutes.append(c);
            else if(c == ':') {
                breakCount++;
                break;
            }
            else return null;
        }
        i++;
        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if((c >= '0' && c <= '9') || c == '.')
                seconds.append(c);
            else return null;
        }
        if(breakCount != 2) return null;
        position += i;
        int hoursV = Integer.parseInt(hours.toString()) % 24;
        return new Expression(new Expression.Value(hoursV + ":" + minutes.toString() + ":" + seconds.toString(), Type.CLOCK));
    }

    private Expression parseDuration() {
        skipWhiteSpace();
        StringBuilder type = new StringBuilder();
        StringBuilder value = new StringBuilder();
        Long hours = null;
        Long minutes = null;
        Double seconds = null;
        int i = 0;
        boolean keepDoing = true;
        while (keepDoing) {
            for(; position+i < program.length(); i++) {
                char c = program.charAt(position+i);
                if((c >= '0' && c <= '9') || c == '.')
                    value.append(c);
                else break;
            }
            if(value.length() == 0) break;
            for(; position+i < program.length(); i++) {
                char c = program.charAt(position+i);
                if(c >= 'A' && c <= 'Z') {
                    type.append(c);
                    if(type.toString().equals("HRS")) {
                        hours = Long.parseLong(value.toString()) % 24;
                        value = new StringBuilder();
                        type = new StringBuilder();
                    }
                    else if(type.toString().equals("MIN")) {
                        minutes = Long.parseLong(value.toString());
                        value = new StringBuilder();
                        type = new StringBuilder();
                    }
                    else if(type.toString().equals("SEC")) {
                        seconds = Double.parseDouble(value.toString());
                        value = new StringBuilder();
                        type = new StringBuilder();
                    }
                }
                else if((c >= '0' && c <= '9') || c == '.')
                    break;
                else {
                    keepDoing = false;
                    break;
                }
            }
            if(position+i == program.length()) break;
        }
        if(hours == null && minutes == null && seconds == null)
            return null;
        if(hours != null || minutes != null || seconds != null) {
            if(hours == null)
                hours = 0L;
            if(minutes == null)
                minutes = 0L;
            if(seconds == null)
                seconds = 0.;
        }
        else return null;

        position += i;
        return new Expression(new Expression.Value(hours + ":" + minutes + ":" + seconds, Type.DURATION));
    }

    private Expression parseBit() {
        skipWhiteSpace();
        StringBuilder result = new StringBuilder();
        int i = 0;
        if(position == program.length()) return null;
        if(program.charAt(position) != '\'') return null;
        i++;

        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F'))
                result.append(c);
            else if(c == '\'') break;
            else return null;
        }
        i++;
        if(position+i >= program.length()) return null;
        if(program.charAt(position+i) != 'B') return null;
        i++;
        result.append(" B");
        if(position+i >= program.length()) {
            result.append('1');
        }
        else {
            char c = program.charAt(position+i);
            if((c >= '1' && c <= '4'))
                result.append(c);
            else
                result.append('1');
            position += i;
        }
        return new Expression(new Expression.Value(result.toString(), Type.BIT));
    }

    private Expression parseChar() {
        skipWhiteSpace();
        StringBuilder result = new StringBuilder();
        int i = 0;
        if(position == program.length()) return null;
        if(program.charAt(position) != '\'') return null;
        i++;

        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if(c == '\'') {
                if(position+i+1 >= program.length()) break;
                char nextC = program.charAt(position+i+1);
                if(nextC == '\'') {
                    i++;
                    result.append(nextC);
                }
                else if(nextC == '\\') {
                    i++;
                    result.append(c).append(nextC);
                }
                else break;
            }
            else if(c == '\\') {
                if(position+i+1 >= program.length()) break;
                char nextC = program.charAt(position+i+1);
                if(nextC == '\'') {
                    i++;
                    result.append(nextC);
                }
                else break;
            }
            else result.append(c);
        }
        position += i;
        return new Expression(new Expression.Value(result.toString(), Type.CHAR));
    }

    private Expression parseFloat() {
        skipWhiteSpace();
        StringBuilder result = new StringBuilder();
        int i = 0;
        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if((c >= '0' && c <= '9') || c == '.')
                result.append(c);
            else if(c == 'E') {
                char nextC = program.charAt(position+i+1);
                if(nextC == '+' || nextC == '-') {
                    result.append(c).append(nextC);
                    i++;
                }
            }
            else break;
        }
        if(result.length() == 0) return null;
        position += i;
        return new Expression(new Expression.Value(result.toString(), Type.FLOAT));
    }

    private static final Set<String> dyadicFunctions = new HashSet<>(Arrays.asList("+", "-", "*", "/",
            "//", "REM", "**", "<", "LT", ">", "GT", "<=", "LE", ">=", "GE", "==", "EQ", "/=", "NE",
            "IS", "ISNT", "AND", "OR", "EXOR", "<>", "CSHIFT", "SHIFT", "><", "CAT", "LWB", "UPB"));
    private Expression parseDyadic() {
        skipWhiteSpace();
        StringBuilder result = new StringBuilder();
        StringBuilder lastResult = null;
        int lastI = 0;
        int i = 0;
        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if((c >= 'A' && c <= 'Z')
                    || c == '+' || c == '-' || c == '*' || c == '/'
                    || c == '<' || c == '>' || c == '=') {
                result.append(c);
                if(dyadicFunctions.contains(result.toString())) {
                    lastI = i+1;
                    lastResult = new StringBuilder();
                    lastResult.append(result.toString());
                }
            }
            else break;
        }
        if(!dyadicFunctions.contains(result.toString()) && lastResult != null) {
            result = new StringBuilder();
            result.append(lastResult.toString());
            i = lastI;
        }
        if(result.length() == 0) return null;
        if(dyadicFunctions.contains(result.toString())) {
            position += i;
            return new Expression(new Expression.Value(result.toString(), Type.DYADIC));
        }
        return null;
    }

    private static final Set<String> monadicSuffix = new HashSet<>(Arrays.asList("MAX", "LENGTH"));
    private Expression parseMonadicSuffix() {
        skipWhiteSpace();
        int i = 0;
        StringBuilder result = new StringBuilder();
        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if(c >= 'A' && c <= 'Z') {
                result.append(c);
                if(monadicSuffix.contains(result.toString())) {
                    i++;
                    break;
                }
            }
            else break;
        }
        if(result.length() == 0) return null;
        if(monadicSuffix.contains(result.toString())) {
            position += i;
            return new Expression(new Expression.Value(result.toString(), Type.MONADIC));
        }
        return null;
    }

    private static final Set<String> monadicFunctions = new HashSet<>(Arrays.asList("NOT", "ABS", "SIGN", "TOFIXED",
            "TOFLOAT", "TOBIT", "TOCHAR", "ENTIER", "ROUND", "CONT", "SQRT", "SIN", "COS", "EXP", "LN", "TAN", "ATAN", "TANH",
            "LWB", "UPB", "SIZEOF", "TRY"));
    private Expression parseMonadic() {
        skipWhiteSpace();

        if(position == program.length()) return null;
        int i = 0;
        char firstC = program.charAt(position+i);
        if(firstC == '+' || firstC == '-') {
            position += 1;
            return new Expression(new Expression.Value("" + firstC, Type.MONADIC));
        }

        StringBuilder result = new StringBuilder();
        StringBuilder lastResult = null;
        for(; position+i < program.length(); i++) {
            char c = program.charAt(position+i);
            if(c >= 'A' && c <= 'Z') {
                result.append(c);
                if(monadicFunctions.contains(result.toString())) {
                    lastResult = new StringBuilder(result.toString());
                }
            }
            else break;
        }
        if(!monadicFunctions.contains(result.toString())) {
            result = lastResult;
        }
        if(result == null || result.length() == 0) return null;
        if(monadicFunctions.contains(result.toString())) {
            position += result.length();
            return new Expression(new Expression.Value(result.toString(), Type.MONADIC));
        }
        return null;
    }

    private Expression parseOpenBracket() {
        skipWhiteSpace();
        if(testInputAndAdvance("(", true))
            return new Expression(new Expression.Value("(", Type.OPENBRACKET));
        else return null;
    }

    private Expression parseClosedBracket(boolean advanceClosedBracket) {
        skipWhiteSpace();
        if(testInputAndAdvance(")", advanceClosedBracket))
            return new Expression(new Expression.Value(")", Type.CLOSEDBRACKED));
        else return null;
    }

    private boolean testInputAndAdvance(String test, boolean advance) {
        skipWhiteSpace();
        for(int i = 0; i < test.length(); i++) {
            if(position+i >= program.length()) return false;
            if(program.charAt(position+i) != test.charAt(i))
                return false;
        }
        if(advance)
            position += test.length();
        return true;
    }

    public enum Type {
        FIXED, FLOAT, CHAR, BIT, DURATION, CLOCK, MONADIC, DYADIC, OPENBRACKET, CLOSEDBRACKED, BRACKET, IDENTIFIER, PROCEDURE
    }

    public static class Expression {
        private Value value;
        private Expression nextExpression;

        public void setValue(Value value) {
            this.value = value;
        }

        private Expression lastExpression;

        public static class Value {
            private String value;
            private Type type;

            public Value(String value, Type type) {
                this.value = value;
                this.type = type;
            }

            public String getValue() {
                return value;
            }

            public Type getType() {
                return type;
            }
        }

        public Expression() {
        }

        public Expression(Value value) {
            this.value = value;
        }

        public Value getValue() {
            return value;
        }

        public Expression getNextExpression() {
            return nextExpression;
        }
        public void setNextExpression(Expression nextExpression) {
            this.nextExpression = nextExpression;
        }
        public Expression getLastExpression() {return lastExpression;}
        public void setLastExpression(Expression lastExpression) {this.lastExpression = lastExpression;}
    }

    public static class Node implements DeepCloneable {
        private String name;
        private Type type;
        private List<Node> children;

        public Node(String name, Type type) {
            this.name = name;
            this.type = type;
            this.children = new ArrayList<>();
        }

        @Override
        public Object deepClone() {
            Node node = new Node(name, type);
            children.stream().map(child -> (Node) child.deepClone()).forEach(node::pushChild);
            return node;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public List<Node> getChildren() {
            return children;
        }

        public void pushChild(Node node) {
            children.add(node);
        }
    }
}
