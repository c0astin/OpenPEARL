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

import java.util.*;

public abstract class VariableValue<T extends Comparable<T>> {
    protected Set<VariableValueRange<T>> values;
    protected boolean known;

    public VariableValue(Set<VariableValueRange<T>> values) {
        this.known = true;
        this.values = values;
    }

    public VariableValue(Collection<VariableValueRange<T>> values) {
        this.known = true;
        this.values = new HashSet<>(values);
    }

    public VariableValue(VariableValueRange<T> value) {
        this.known = true;
        this.values = new HashSet<>(Collections.singletonList(value));
    }

    public VariableValue(T value) {
        this.known = true;
        this.values = new HashSet<>(Collections.singletonList(new VariableValueRange<>(value)));
    }

    public VariableValue(Set<VariableValueRange<T>> values, boolean known) {
        this.values = values;
        this.known = known;
    }

    public VariableValue(Collection<VariableValueRange<T>> values, boolean known) {
        this.values = new HashSet<>(values);
        this.known = known;
    }

    public VariableValue(VariableValueRange<T> value, boolean known) {
        this.values = new HashSet<>(Collections.singletonList(value));
        this.known = known;
    }

    public VariableValue(T value, boolean known) {
        this.values = new HashSet<>(Collections.singletonList(new VariableValueRange<>(value)));
        this.known = known;
    }

    public VariableValue() {
        this.values = new HashSet<>();
        this.known = false;
    }


    public Set<VariableValueRange<T>> getValues() {
        return values;
    }
    public void setValues(Set<VariableValueRange<T>> values) {
        this.values = values;
    }

    public boolean isKnown() {
        return known;
    }
    public void setKnown(boolean known) {
        this.known = known;
    }
}
