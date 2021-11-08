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
import org.smallpearl.compiler.ConstantFixedValue;
import org.smallpearl.compiler.ConstantFloatValue;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class BitVariableValue extends VariableValue<ConstantBitValue> implements Operations {



    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(" =  [ ");
        Iterator<VariableValueRange<ConstantBitValue>> iterator = values.iterator();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantBitValue> value = iterator.next();
            long from = value.getFrom().getLongValue();
            long to = value.getTo().getLongValue();

            String fromString;
            if(from == Long.MAX_VALUE) fromString = "LONG_MAX";
            else fromString = "" + from;

            String toString;
            if(to == Long.MAX_VALUE) toString = "LONG_MAX";
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

    public static VariableValueRange<ConstantBitValue> defaultValue() {
        return new VariableValueRange<>(
                new ConstantBitValue(0L, 64),
                new ConstantBitValue(Long.MAX_VALUE, 64)
        );
    }

    public static VariableValueRange<ConstantBitValue> defaultBoolValue() {
        return new VariableValueRange<>(
                new ConstantBitValue(0L, 1),
                new ConstantBitValue(1L, 1)
        );
    }

    public BitVariableValue(Set<VariableValueRange<ConstantBitValue>> values) {
        super(values);
    }

    public BitVariableValue(Collection<VariableValueRange<ConstantBitValue>> values) {
        super(values);
    }

    public BitVariableValue(VariableValueRange<ConstantBitValue> value) {
        super(value);
    }

    public BitVariableValue(ConstantBitValue value) {
        super(value);
    }

    public BitVariableValue(Set<VariableValueRange<ConstantBitValue>> values, boolean known) {
        super(values, known);
    }

    @Override
    public Operations toFixed() {
        return new FixedVariableValue(BitOperations.toFixed(values));
    }

    @Override
    public Operations toBit() {
        return this;
    }

    @Override
    public Operations equal(Operations other) {
        if(other instanceof BitVariableValue) {
            BitVariableValue o = (BitVariableValue) other;
            return new BitVariableValue(BitOperations.bitEqual(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations notEqual(Operations other) {
        if(other instanceof BitVariableValue) {
            BitVariableValue o = (BitVariableValue) other;
            return new BitVariableValue(BitOperations.bitNotEqual(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public boolean hasNoValue() {
        return values.size() == 0;
    }

    @Override
    public Operations not() {
        return new BitVariableValue(BitOperations.not(values));
    }

    @Override
    public Operations and(Operations other) {
        if(other instanceof BitVariableValue) {
            BitVariableValue o = (BitVariableValue) other;
            return new BitVariableValue(BitOperations.and(values, o.values));
        }
        throw new NotImplementedException();
    }

    @Override
    public Operations or(Operations other) {
        if(other instanceof BitVariableValue) {
            BitVariableValue o = (BitVariableValue) other;
            return new BitVariableValue(BitOperations.or(values, o.values));
        }
        throw new NotImplementedException();
    }
    @Override
    public Operations exor(Operations other) {
        if(other instanceof BitVariableValue) {
            BitVariableValue o = (BitVariableValue) other;
            return new BitVariableValue(BitOperations.exor(values, o.values));
        }
        throw new NotImplementedException();
    }

    @Override
    public Operations shift(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new BitVariableValue(BitOperations.shift(values, o.getValues()));
        }
        throw new NotImplementedException();
    }

    @Override
    public Operations cShift(Operations other) {
        if(other instanceof FixedVariableValue) {
            FixedVariableValue o = (FixedVariableValue) other;
            return new BitVariableValue(BitOperations.cShift(values, o.getValues()));
        }
        throw new NotImplementedException();
    }

    @Override
    public Operations concat(Operations other) {
        if(other instanceof BitVariableValue) {
            BitVariableValue o = (BitVariableValue) other;
            return new BitVariableValue(BitOperations.concat(values, o.values));
        }
        throw new NotImplementedException();
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
    public Object deepClone() {
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        values.forEach(value -> {
            result.add(new VariableValueRange<>(
                    new ConstantBitValue(value.getFrom().getLongValue(), value.getFrom().getLength()),
                    new ConstantBitValue(value.getTo().getLongValue(), value.getTo().getLength())));
        });
        return new BitVariableValue(result, known);
    }

    @Override
    public Operations h_and(Operations other) {
        if(other instanceof BitVariableValue) {
            BitVariableValue o = (BitVariableValue) other;
            return new BitVariableValue(BitOperations.h_and(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations h_or(Operations other) {
        if(other instanceof BitVariableValue) {
            BitVariableValue o = (BitVariableValue) other;
            return new BitVariableValue(BitOperations.h_or(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations h_xor(Operations other) {
        return new BitVariableValue(BitVariableValue.defaultValue());
    }

    @Override
    public Operations h_notEqual() {
        return new BitVariableValue(BitOperations.h_notEqual(values));
    }

    @Override
    public Operations h_less() {
        return new BitVariableValue(BitOperations.h_less(values));
    }

    @Override
    public Operations h_greater() {
        return new BitVariableValue(BitOperations.h_greater(values));
    }

    @Override
    public Operations h_lessEqual() {
        return new BitVariableValue(BitOperations.h_lessEqual(values));
    }

    @Override
    public Operations h_greaterEqual() {
        return new BitVariableValue(BitOperations.h_greaterEqual(values));
    }

}
