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
import org.smallpearl.compiler.ConstantCharacterValue;
import org.smallpearl.compiler.ConstantFixedValue;
import org.smallpearl.compiler.ConstantFloatValue;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CharVariableValue extends VariableValue<ConstantCharacterValue> implements Operations {

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if(!isDisplayable(values))
            return " = [ ??? ]";

        result.append(" =  [ ");
        Iterator<VariableValueRange<ConstantCharacterValue>> iterator = values.iterator();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantCharacterValue> value = iterator.next();
            if(value.getFrom().getValue().compareTo(value.getTo().getValue()) == 0)
                result.append(value.getFrom().getValue());
            else
                result.append("(" +  value.getFrom().getValue() + ", " + value.getTo().getValue() + ")");

            if(iterator.hasNext())
                result.append(", ");
        }
        result.append(" ]");
        return result.toString();
    }

    private boolean isDisplayable(Set<VariableValueRange<ConstantCharacterValue>> values) {
        for(VariableValueRange<ConstantCharacterValue> value : values) {
            if(!isDisplayable(value.getFrom().getValue()))
                return false;
            if(!isDisplayable(value.getTo().getValue()))
                return false;
        }
        return true;
    }

    private boolean isDisplayable(String string) {
        for(int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if(c < 32 || c > 126) return false;
        }
        return true;
    }


    public CharVariableValue(Set<VariableValueRange<ConstantCharacterValue>> values) {
        super(values);
    }

    public CharVariableValue(Collection<VariableValueRange<ConstantCharacterValue>> values) {
        super(values);
    }

    public CharVariableValue(VariableValueRange<ConstantCharacterValue> value) {
        super(value);
    }

    public CharVariableValue(ConstantCharacterValue value) {
        super(value);
    }

    public CharVariableValue() {
        super();
    }

    public CharVariableValue(Set<VariableValueRange<ConstantCharacterValue>> values, boolean known) {
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
    public Operations upb() {
        return new FixedVariableValue(CharOperations.upb(values));
    }

    @Override
    public Operations equal(Operations other) {
        if(other instanceof CharVariableValue) {
            CharVariableValue o = (CharVariableValue) other;
            if(!known || !o.known) return new BitVariableValue(new VariableValueRange<>(
                    new ConstantBitValue(0, 64),
                    new ConstantBitValue(1, 64)
            ));
            return new BitVariableValue(CharOperations.charEqual(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations notEqual(Operations other) {
        if(other instanceof CharVariableValue) {
            CharVariableValue o = (CharVariableValue) other;
            if(!known || !o.known) return new BitVariableValue(new VariableValueRange<>(
                    new ConstantBitValue(0, 64),
                    new ConstantBitValue(1, 64)
            ));
            return new BitVariableValue(CharOperations.charNotEqual(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations concat(Operations other) {
        if(other instanceof CharVariableValue) {
            CharVariableValue o = (CharVariableValue) other;
            if(!known || !o.known) return new BitVariableValue(new VariableValueRange<>(
                    new ConstantBitValue(0, 64),
                    new ConstantBitValue(1, 64)
            ));
            return new CharVariableValue(CharOperations.concat(values, o.values));
        }
        else throw new NotImplementedException();
    }


    @Override
    public Object deepClone() {
        Set<VariableValueRange<ConstantCharacterValue>> result = new HashSet<>();
        if(!known) return new CharVariableValue();
        values.forEach(value -> {
            result.add(new VariableValueRange<>(
                    new ConstantCharacterValue(value.getFrom().getValue()),
                    new ConstantCharacterValue(value.getTo().getValue())));
        });
        return new CharVariableValue(result, known);
    }

    @Override
    public Operations h_and(Operations other) {
        if(other instanceof CharVariableValue) {
            CharVariableValue o = (CharVariableValue) other;
            if(!known && o.known) return new CharVariableValue(o.values);
            else if(known && !o.known) return new CharVariableValue(values);
            return new CharVariableValue(CharOperations.and(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations h_or(Operations other) {
        if(other instanceof CharVariableValue) {
            CharVariableValue o = (CharVariableValue) other;
            if(!known || !o.known) return new CharVariableValue();
            return new CharVariableValue(CharOperations.or(values, o.values));
        }
        else throw new NotImplementedException();
    }

    @Override
    public Operations h_notEqual() {
        Set<VariableValueRange<ConstantCharacterValue>> result = CharOperations.charNotEqual(values);
        if(result == null)
            return new CharVariableValue();
        else
            return new CharVariableValue(result);
    }

    @Override
    public Operations h_less() {
        return new CharVariableValue();
    }

    @Override
    public Operations h_greater() {
        return new CharVariableValue();
    }

    @Override
    public Operations h_lessEqual() {
        return new CharVariableValue();
    }

    @Override
    public Operations h_greaterEqual() {
        return new CharVariableValue();
    }

}