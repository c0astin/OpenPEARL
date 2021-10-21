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

import org.smallpearl.compiler.ConstantBitValue;
import org.smallpearl.compiler.ConstantDurationValue;
import org.smallpearl.compiler.ConstantFixedValue;
import org.smallpearl.compiler.ConstantFloatValue;

import java.util.*;
import java.util.stream.Collectors;

public class FloatVariableValue extends VariableValue<ConstantFloatValue> implements Operations {

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(" =  [ ");
        Iterator<VariableValueRange<ConstantFloatValue>> iterator = values.iterator();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantFloatValue> value = iterator.next();
            double from = value.getFrom().getValue();
            double to = value.getTo().getValue();

            String fromString;
            if(from == Double.MIN_VALUE) fromString = "DOUBLE_MIN";
            else if(from == Double.MAX_VALUE) fromString = "DOUBLE_MAX";
            else if(from == Float.MIN_VALUE) fromString = "FLOAT_MIN";
            else if(from == Float.MAX_VALUE) fromString = "FLOAT_MAX";
            else fromString = "" + from;

            String toString;
            if(to == Double.MIN_VALUE) toString = "DOUBLE_MIN";
            else if(to == Double.MAX_VALUE) toString = "DOUBLE_MAX";
            else if(to == Float.MIN_VALUE) toString = "FLOAT_MIN";
            else if(to == Float.MAX_VALUE) toString = "FLOAT_MAX";
            else toString = "" + to;

            if(from == to)
                result.append(fromString);
            else
                result.append("(" +  fromString + ", " + toString + ")");

            if(iterator.hasNext())
                result.append(", ");
        }
        result.append(" ]");
        return result.toString();
    }

    public static VariableValueRange<ConstantFloatValue> defaultValue() {
        return new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        );
    }

    public FloatVariableValue(Set<VariableValueRange<ConstantFloatValue>> values) {
        super(values);
    }

    public FloatVariableValue(Collection<VariableValueRange<ConstantFloatValue>> values) {
        super(values);
    }

    public FloatVariableValue(VariableValueRange<ConstantFloatValue> value) {
        super(value);
    }

    public FloatVariableValue(ConstantFloatValue value) {
        super(value);
    }

    public FloatVariableValue(Set<VariableValueRange<ConstantFloatValue>> values, boolean known) {
        super(values, known);
    }

    @Override
    public boolean hasNoValue() {
        return values.size() == 0;
    }

    @Override
    public Operations negate() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations abs() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations sign() {
        return new FixedVariableValue(Arrays.asList(
                new VariableValueRange<>(new ConstantFixedValue(0L, 63)),
                new VariableValueRange<>(new ConstantFixedValue(1L, 63)),
                new VariableValueRange<>(new ConstantFixedValue(-1L, 63))
        ));
    }

    @Override
    public Operations entier() {
        return new FixedVariableValue(values.stream()
                .map(range -> {
                    double from = range.getFrom().getValue();
                    double to = range.getTo().getValue();
                    long longFrom = from <= Long.MIN_VALUE ? Long.MIN_VALUE : (long) from;
                    long longTo = to >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) to;
                    return new VariableValueRange<>(
                            new ConstantFixedValue(longFrom, 63),
                            new ConstantFixedValue(longTo, 63));
                })
                .collect(Collectors.toSet()));
    }

    @Override
    public Operations toFixed() {
        return new FixedVariableValue(values.stream()
                .map(range -> {
                    double from = range.getFrom().getValue();
                    double to = range.getTo().getValue();
                    long longFrom = from <= Long.MIN_VALUE ? Long.MIN_VALUE : (long) from;
                    long longTo = to >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) to;
                    return new VariableValueRange<>(
                            new ConstantFixedValue(longFrom, 63),
                            new ConstantFixedValue(longTo, 63));
                })
                .collect(Collectors.toSet()));
    }

    @Override
    public Operations toFloat() {
        return this;
    }

    @Override
    public Operations round() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations sqrt() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations sin() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations cos() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations e_exp() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations ln() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations tan() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations atan() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations tanh() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations sizeof() {
        return new FixedVariableValue(new VariableValueRange<>(
                new ConstantFixedValue(Long.MIN_VALUE, 63),
                new ConstantFixedValue(Long.MAX_VALUE, 63)
        ));
    }

    @Override
    public Operations sizeofMax() {
        return new FixedVariableValue(new VariableValueRange<>(
                new ConstantFixedValue(Long.MIN_VALUE, 63),
                new ConstantFixedValue(Long.MAX_VALUE, 63)
        ));
    }

    @Override
    public Operations sizeofLength() {
        return new FixedVariableValue(new VariableValueRange<>(
                new ConstantFixedValue(Long.MIN_VALUE, 63),
                new ConstantFixedValue(Long.MAX_VALUE, 63)
        ));
    }

    @Override
    public Operations add(Operations other) {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations sub(Operations other) {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations mul(Operations other) {
        if(other instanceof DurationVariableValue) {
            DurationVariableValue o = (DurationVariableValue) other;
            return new DurationVariableValue(new VariableValueRange<>(
                    new ConstantDurationValue(0L, 0, 0.),
                    new ConstantDurationValue(3600, 60, 60.0)
            ));
        }
        else {
            return new FloatVariableValue(new VariableValueRange<>(
                    new ConstantFloatValue(-Double.MAX_VALUE, 52),
                    new ConstantFloatValue(Double.MAX_VALUE, 52)
            ));
        }
    }

    @Override
    public Operations div(Operations other) {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations exp(Operations other) {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations fit(Operations other) {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MIN_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations less(Operations other) {
        return new BitVariableValue(
                Arrays.asList(
                        new VariableValueRange<>(new ConstantBitValue(0L, 1)),
                        new VariableValueRange<>(new ConstantBitValue(1L, 1))
                ));
    }

    @Override
    public Operations greater(Operations other) {
        return new BitVariableValue(
                Arrays.asList(
                        new VariableValueRange<>(new ConstantBitValue(0L, 1)),
                        new VariableValueRange<>(new ConstantBitValue(1L, 1))
                ));
    }

    @Override
    public Operations lessEqual(Operations other) {
        return new BitVariableValue(
                Arrays.asList(
                        new VariableValueRange<>(new ConstantBitValue(0L, 1)),
                        new VariableValueRange<>(new ConstantBitValue(1L, 1))
                ));
    }

    @Override
    public Operations greaterEqual(Operations other) {
        return new BitVariableValue(
                Arrays.asList(
                        new VariableValueRange<>(new ConstantBitValue(0L, 1)),
                        new VariableValueRange<>(new ConstantBitValue(1L, 1))
                ));
    }

    @Override
    public Operations equal(Operations other) {
        return new BitVariableValue(
                Arrays.asList(
                        new VariableValueRange<>(new ConstantBitValue(0L, 1)),
                        new VariableValueRange<>(new ConstantBitValue(1L, 1))
                ));
    }

    @Override
    public Operations notEqual(Operations other) {
        return new BitVariableValue(
                Arrays.asList(
                        new VariableValueRange<>(new ConstantBitValue(0L, 1)),
                        new VariableValueRange<>(new ConstantBitValue(1L, 1))
                ));
    }

    @Override
    public Operations h_less() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations h_greater() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations h_lessEqual() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations h_greaterEqual() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations h_notEqual() {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations h_and(Operations other) {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Operations h_or(Operations other) {
        return new FloatVariableValue(new VariableValueRange<>(
                new ConstantFloatValue(-Double.MAX_VALUE, 52),
                new ConstantFloatValue(Double.MAX_VALUE, 52)
        ));
    }

    @Override
    public Object deepClone() {
        Set<VariableValueRange<ConstantFloatValue>> result = new HashSet<>();
        values.forEach(value -> {
            result.add(new VariableValueRange<>(
                    new ConstantFloatValue(value.getFrom().getValue(), value.getFrom().getPrecision()),
                    new ConstantFloatValue(value.getTo().getValue(), value.getTo().getPrecision())));
        });
        return new FloatVariableValue(result, known);
    }
}
