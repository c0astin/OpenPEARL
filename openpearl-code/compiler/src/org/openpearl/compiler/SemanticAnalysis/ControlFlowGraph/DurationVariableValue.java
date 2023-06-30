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

import org.openpearl.compiler.*;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class DurationVariableValue extends VariableValue<ConstantDurationValue> implements Operations {

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(" =  [ ");
        Iterator<VariableValueRange<ConstantDurationValue>> iterator = values.iterator();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantDurationValue> value = iterator.next();
            result.append("(" +  value.getFrom().toString() + ", " + value.getTo().toString() + ")");

            if(iterator.hasNext())
                result.append(", ");
        }
        result.append(" ]");
        return result.toString();
    }

    public static VariableValueRange<ConstantDurationValue> defaultValue() {
        return new VariableValueRange<>(
                new ConstantDurationValue(0L, 0, 0.),
                new ConstantDurationValue(3600, 60, 60.0)
        );
    }

    public DurationVariableValue(Set<VariableValueRange<ConstantDurationValue>> values) {
        super(values);
    }

    public DurationVariableValue(Collection<VariableValueRange<ConstantDurationValue>> values) {
        super(values);
    }

    public DurationVariableValue(VariableValueRange<ConstantDurationValue> value) {
        super(value);
    }

    public DurationVariableValue(ConstantDurationValue value) {
        super(value);
    }

    public DurationVariableValue(Set<VariableValueRange<ConstantDurationValue>> values, boolean known) {
        super(values, known);
    }

    @Override
    public Operations sizeof() {
        return new FixedVariableValue(FixedVariableValue.defaultValue());
    }

    @Override
    public Operations sizeofMax() {
        return new FixedVariableValue(FixedVariableValue.defaultValue());
    }

    @Override
    public Operations sizeofLength() {
        return new FixedVariableValue(FixedVariableValue.defaultValue());
    }


    @Override
    public boolean hasNoValue() {
        return values.size() == 0;
    }

    @Override
    public Operations negate() {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
    }

    @Override
    public Operations abs() {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
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
    public Operations add(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new DurationVariableValue(DurationVariableValue.defaultValue());
        }
        else if(other instanceof ClockVariableValue) {
            return new ClockVariableValue(ClockVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations sub(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new DurationVariableValue(DurationVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations mul(Operations other) {
        if(other instanceof FixedVariableValue) {
            return new DurationVariableValue(DurationVariableValue.defaultValue());
        }
        else if(other instanceof FloatVariableValue) {
            return new DurationVariableValue(DurationVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations div(Operations other) {
        if(other instanceof FixedVariableValue) {
            return new DurationVariableValue(DurationVariableValue.defaultValue());
        }
        else if(other instanceof FloatVariableValue) {
            return new DurationVariableValue(DurationVariableValue.defaultValue());
        }
        else if(other instanceof DurationVariableValue) {
            return new FloatVariableValue(FloatVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations less(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations greater(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations lessEqual(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations greaterEqual(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations equal(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations notEqual(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Object deepClone() {
        Set<VariableValueRange<ConstantDurationValue>> result = new HashSet<>();
        values.forEach(value -> {
            result.add(new VariableValueRange<>(
                    new ConstantDurationValue(value.getFrom().getHours(), value.getFrom().getMinutes(), value.getFrom().getSeconds()),
                    new ConstantDurationValue(value.getTo().getHours(), value.getTo().getMinutes(), value.getTo().getSeconds())));
        });
        return new DurationVariableValue(result, known);
    }

    @Override
    public Operations h_and(Operations other) {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
    }

    @Override
    public Operations h_or(Operations other) {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
    }

    @Override
    public Operations h_xor(Operations other) {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
    }

    @Override
    public Operations h_less() {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
    }

    @Override
    public Operations h_greater() {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
    }

    @Override
    public Operations h_lessEqual() {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
    }

    @Override
    public Operations h_greaterEqual() {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
    }

    @Override
    public Operations h_notEqual() {
        return new DurationVariableValue(DurationVariableValue.defaultValue());
    }
}