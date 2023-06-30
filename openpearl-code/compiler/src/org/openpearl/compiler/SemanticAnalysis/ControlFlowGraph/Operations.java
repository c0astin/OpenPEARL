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

//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public interface Operations extends DeepCloneable {

    boolean hasNoValue();
    default Operations noOp() {return this;}

    //monadic operators
    default Operations negate() {throw new NotImplementedException();}
    default Operations not() {throw new NotImplementedException();}
    default Operations abs() {throw new NotImplementedException();}
    default Operations sign() {throw new NotImplementedException();}

    default Operations toFixed() {throw new NotImplementedException();}
    default Operations toFloat() {throw new NotImplementedException();}
    default Operations toBit() {throw new NotImplementedException();}
    default Operations toChar() {throw new NotImplementedException();}
    default Operations entier() {throw new NotImplementedException();}
    default Operations round() {throw new NotImplementedException();}
    default Operations cont() {throw new NotImplementedException();}

    default Operations sqrt() {throw new NotImplementedException();}
    default Operations sin() {throw new NotImplementedException();}
    default Operations cos() {throw new NotImplementedException();}
    default Operations e_exp() {throw new NotImplementedException();}
    default Operations ln() {throw new NotImplementedException();}
    default Operations tan() {throw new NotImplementedException();}
    default Operations atan() {throw new NotImplementedException();}
    default Operations tanh() {throw new NotImplementedException();}

    default Operations lwb() {throw new NotImplementedException();}
    default Operations upb() {throw new NotImplementedException();}
    default Operations sizeof() {throw new NotImplementedException();}
    default Operations sizeofMax() {throw new NotImplementedException();}
    default Operations sizeofLength() {throw new NotImplementedException();}
    default Operations tryRequest() {throw new NotImplementedException();}

    //dyadic operators
    default Operations add(Operations other) {throw new NotImplementedException();}
    default Operations sub(Operations other) {throw new NotImplementedException();}
    default Operations mul(Operations other) {throw new NotImplementedException();}
    default Operations div(Operations other) {throw new NotImplementedException();}
    default Operations intDiv(Operations other) {throw new NotImplementedException();}
    default Operations rem(Operations other) {throw new NotImplementedException();}
    default Operations exp(Operations other) {throw new NotImplementedException();}
    default Operations fit(Operations other) {throw new NotImplementedException();}

    default Operations less(Operations other) {throw new NotImplementedException();}
    default Operations greater(Operations other) {throw new NotImplementedException();}
    default Operations lessEqual(Operations other) {throw new NotImplementedException();}
    default Operations greaterEqual(Operations other) {throw new NotImplementedException();}
    default Operations equal(Operations other) {throw new NotImplementedException();}
    default Operations notEqual(Operations other) {throw new NotImplementedException();}

    default Operations is(Operations other) {throw new NotImplementedException();}
    default Operations isnt(Operations other) {throw new NotImplementedException();}

    default Operations and(Operations other) {throw new NotImplementedException();}
    default Operations or(Operations other) {throw new NotImplementedException();}
    default Operations exor(Operations other) {throw new NotImplementedException();}
    default Operations cShift(Operations other) {throw new NotImplementedException();}
    default Operations shift(Operations other) {throw new NotImplementedException();}

    default Operations concat(Operations other) {throw new NotImplementedException();}

    default Operations lwb(Operations other) {throw new NotImplementedException();}
    default Operations upb(Operations other) {throw new NotImplementedException();}

    //helpers
    default Operations h_and(Operations other) {throw new NotImplementedException();}
    default Operations h_or(Operations other) {throw new NotImplementedException();}
    default Operations h_xor(Operations other) {throw new NotImplementedException();}

    default Operations h_less() {throw new NotImplementedException();}
    default Operations h_greater() {throw new NotImplementedException();}
    default Operations h_lessEqual() {throw new NotImplementedException();}
    default Operations h_greaterEqual() {throw new NotImplementedException();}
    default Operations h_notEqual() {throw new NotImplementedException();}
}
