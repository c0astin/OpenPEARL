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
import org.smallpearl.compiler.ConstantClockValue;
import org.smallpearl.compiler.ConstantDurationValue;
import org.smallpearl.compiler.ConstantFixedValue;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class ClockVariableValue extends VariableValue<ConstantClockValue> implements Operations {

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(" =  [ ");
        Iterator<VariableValueRange<ConstantClockValue>> iterator = values.iterator();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantClockValue> value = iterator.next();
            result.append("(" +  value.getFrom().toString() + ", " + value.getTo().toString() + ")");

            if(iterator.hasNext())
                result.append(", ");
        }
        result.append(" ]");
        return result.toString();
    }

    public static VariableValueRange<ConstantClockValue> defaultValue() {
        return new VariableValueRange<>(
                new ConstantClockValue(0, 0, 0.),
                new ConstantClockValue(Integer.MAX_VALUE, Integer.MAX_VALUE , Double.MAX_VALUE)
        );
    }

    public ClockVariableValue(Set<VariableValueRange<ConstantClockValue>> values) {
        super(values);
    }

    public ClockVariableValue(Collection<VariableValueRange<ConstantClockValue>> values) {
        super(values);
    }

    public ClockVariableValue(VariableValueRange<ConstantClockValue> value) {
        super(value);
    }

    public ClockVariableValue(ConstantClockValue value) {
        super(value);
    }

    public ClockVariableValue(Set<VariableValueRange<ConstantClockValue>> values, boolean known) {
        super(values, known);
    }

    @Override
    public boolean hasNoValue() {
        return values.size() == 0;
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
    public Operations add(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new ClockVariableValue(ClockVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations sub(Operations other) {
        if(other instanceof DurationVariableValue) {
            return new ClockVariableValue(ClockVariableValue.defaultValue());
        }
        else if(other instanceof ClockVariableValue) {
            return new DurationVariableValue(DurationVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations less(Operations other) {
        if(other instanceof ClockVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations greater(Operations other) {
        if(other instanceof ClockVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations lessEqual(Operations other) {
        if(other instanceof ClockVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations greaterEqual(Operations other) {
        if(other instanceof ClockVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations equal(Operations other) {
        if(other instanceof ClockVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations notEqual(Operations other) {
        if(other instanceof ClockVariableValue) {
            return new BitVariableValue(BitVariableValue.defaultValue());
        }
        else throw new NotImplementedException();
    }

    @Override
    public Object deepClone() {
        Set<VariableValueRange<ConstantClockValue>> result = new HashSet<>();
        values.forEach(value -> {
            result.add(new VariableValueRange<>(
                    new ConstantClockValue(value.getFrom().getHours(), value.getFrom().getMinutes(), value.getFrom().getSeconds()),
                    new ConstantClockValue(value.getTo().getHours(), value.getTo().getMinutes(), value.getTo().getSeconds())));
        });
        return new ClockVariableValue(result, known);
    }

    @Override
    public Operations h_and(Operations other) {
        return new ClockVariableValue(ClockVariableValue.defaultValue());
    }

    @Override
    public Operations h_or(Operations other) {
        return new ClockVariableValue(ClockVariableValue.defaultValue());
    }

    @Override
    public Operations h_xor(Operations other) {
        return new ClockVariableValue(ClockVariableValue.defaultValue());
    }

    @Override
    public Operations h_less() {
        return new ClockVariableValue(ClockVariableValue.defaultValue());
    }

    @Override
    public Operations h_greater() {
        return new ClockVariableValue(ClockVariableValue.defaultValue());
    }

    @Override
    public Operations h_lessEqual() {
        return new ClockVariableValue(ClockVariableValue.defaultValue());
    }

    @Override
    public Operations h_greaterEqual() {
        return new ClockVariableValue(ClockVariableValue.defaultValue());
    }

    @Override
    public Operations h_notEqual() {
        return new ClockVariableValue(ClockVariableValue.defaultValue());
    }
}
