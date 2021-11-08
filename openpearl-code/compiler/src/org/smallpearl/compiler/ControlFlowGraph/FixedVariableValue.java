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
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class FixedVariableValue extends VariableValue<ConstantFixedValue> implements Operations {

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(" =  [ ");
        Iterator<VariableValueRange<ConstantFixedValue>> iterator = values.iterator();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantFixedValue> value = iterator.next();
            long from = value.getFrom().getValue();
            long to = value.getTo().getValue();

            String fromString;
            if(from == Long.MIN_VALUE) fromString = "LONG_MIN";
            else if(from == Long.MAX_VALUE) fromString = "LONG_MAX";
            else fromString = "" + from;

            String toString;
            if(to == Long.MIN_VALUE) toString = "LONG_MIN";
            else if(to == Long.MAX_VALUE) toString = "LONG_MAX";
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

    public static VariableValueRange<ConstantFixedValue> defaultValue() {
        return new VariableValueRange<>(
                new ConstantFixedValue(Long.MIN_VALUE),
                new ConstantFixedValue(Long.MAX_VALUE)
        );
    }

    public FixedVariableValue(Set<VariableValueRange<ConstantFixedValue>> values) {
        super(values);
    }

    public FixedVariableValue(Collection<VariableValueRange<ConstantFixedValue>> values) {
        super(values);
    }

    public FixedVariableValue(VariableValueRange<ConstantFixedValue> value) {
        super(value);
    }

    public FixedVariableValue(ConstantFixedValue value) {
        super(value);
    }

    public FixedVariableValue(Set<VariableValueRange<ConstantFixedValue>> values, boolean known) {
        super(values, known);
    }

    public FixedVariableValue() {
        super();
    }

    @Override
    public Object deepClone() {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        values.forEach(value -> {
            result.add(new VariableValueRange<>(
                    new ConstantFixedValue(value.getFrom().getValue(), value.getFrom().getPrecision()),
                    new ConstantFixedValue(value.getTo().getValue(), value.getTo().getPrecision())));
        });
        return new FixedVariableValue(result, known);
    }

    @Override
    public boolean hasNoValue() {
        return values.size() == 0;
    }

    @Override
    public Operations negate() {
        return new FixedVariableValue(FixedOperations.negate(values));
    }

    @Override
    public Operations abs() {
        return new FixedVariableValue(FixedOperations.abs(values));
    }

    @Override
    public Operations sign() {
        return new FixedVariableValue(FixedOperations.sign(values));
    }

    @Override
    public Operations toFixed() {
        return this;
    }

    @Override
    public Operations toFloat() {
        return new FloatVariableValue(FloatVariableValue.defaultValue());
    }

    @Override
    public Operations toBit() {
        return new BitVariableValue(BitOperations.toBit(values));
    }

    @Override
    public Operations toChar() {
        return new CharVariableValue(CharOperations.toChar(values));
    }

    @Override
    public Operations sqrt() {
        return new FloatVariableValue(FloatVariableValue.defaultValue());
    }

    @Override
    public Operations sin() {
        return new FloatVariableValue(FloatVariableValue.defaultValue());
    }

    @Override
    public Operations cos() {
        return new FloatVariableValue(FloatVariableValue.defaultValue());
    }

    @Override
    public Operations e_exp() {
        return new FloatVariableValue(FloatVariableValue.defaultValue());
    }

    @Override
    public Operations ln() {
        return new FloatVariableValue(FloatVariableValue.defaultValue());
    }

    @Override
    public Operations tan() {
        return new FloatVariableValue(FloatVariableValue.defaultValue());
    }

    @Override
    public Operations atan() {
        return new FloatVariableValue(FloatVariableValue.defaultValue());
    }

    @Override
    public Operations tanh() {
        return new FloatVariableValue(FloatVariableValue.defaultValue());
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
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.add(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new FloatVariableValue(FloatVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }


    @Override
    public Operations sub(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.sub(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new FloatVariableValue(FloatVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations mul(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.mul(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new FloatVariableValue(FloatVariableValue.defaultValue());
        }
        else if(other instanceof DurationVariableValue) {
            return new DurationVariableValue(DurationVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations div(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FloatVariableValue(FixedOperations.div(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new FloatVariableValue(FloatVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations intDiv(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.intDiv(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations rem(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.rem(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations exp(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.exp(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations fit(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.fit(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations less(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new BitVariableValue(BitOperations.less(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations greater(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new BitVariableValue(BitOperations.greater(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations lessEqual(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new BitVariableValue(BitOperations.lessEqual(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations greaterEqual(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new BitVariableValue(BitOperations.greaterEqual(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations equal(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new BitVariableValue(BitOperations.equal(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations notEqual(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new BitVariableValue(BitOperations.notEqual(values, o.values));
        }
        else if(other instanceof FloatVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultBoolValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations lwb(Operations other) {
        return new FixedVariableValue(FixedVariableValue.defaultValue());
    }

    @Override
    public Operations upb(Operations other) {
        return new FixedVariableValue(FixedVariableValue.defaultValue());
    }

    @Override
    public Operations h_and(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.and(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations h_or(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.or(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations h_xor(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new FixedVariableValue(FixedOperations.xor(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations h_less() {
        return new FixedVariableValue(FixedOperations.less(values));
    }

    @Override
    public Operations h_greater() {
        return new FixedVariableValue(FixedOperations.greater(values));
    }

    @Override
    public Operations h_lessEqual() {
        return new FixedVariableValue(FixedOperations.lessEqual(values));
    }

    @Override
    public Operations h_greaterEqual() {
        return new FixedVariableValue(FixedOperations.greaterEqual(values));
    }

    @Override
    public Operations h_notEqual() {
        return new FixedVariableValue(FixedOperations.notEqual(values));
    }
}
