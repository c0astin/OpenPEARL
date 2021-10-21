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

import java.util.*;

public class CharOperations {

    public static Set<VariableValueRange<ConstantCharacterValue>> toChar(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        Set<VariableValueRange<ConstantCharacterValue>> result = new HashSet<>();
        ranges.forEach(range -> {
            long from = Math.max(range.getFrom().getValue(), Character.MIN_VALUE);
            long to = Math.min(range.getTo().getValue(), Character.MAX_VALUE);

            result.add(new VariableValueRange<>(
                    new ConstantCharacterValue("" + (char) from),
                    new ConstantCharacterValue("" + (char) to)
            ));
        });
        return result;
    }

    public static Set<VariableValueRange<ConstantFixedValue>> upb(Set<VariableValueRange<ConstantCharacterValue>> ranges) {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        ranges.forEach(range -> {
            String from = range.getFrom().getValue();
            String to = range.getTo().getValue();

            result.add(new VariableValueRange<>(
                    new ConstantFixedValue(from.length(), 63),
                    new ConstantFixedValue(to.length(), 63)
            ));
        });
        return result;
    }

    public static Set<VariableValueRange<ConstantCharacterValue>> and(Set<VariableValueRange<ConstantCharacterValue>> ranges, Set<VariableValueRange<ConstantCharacterValue>> others) {
        Set<VariableValueRange<ConstantCharacterValue>> result = new HashSet<>();
        ranges.forEach(range -> {
            String from = range.getFrom().getValue();
            String to = range.getTo().getValue();

            others.forEach(other -> {
                String otherFrom = other.getFrom().getValue();
                String otherTo = other.getTo().getValue();

                if(from.length() != 1 || to.length() != 1 || otherFrom.length() != 1 || otherTo.length() != 1) {
                    if(from.compareTo(to) != 0
                            || otherFrom.compareTo(otherTo) != 0)
                        throw new RuntimeException("compare stringRanges not implemented");

                    if(from.equals(otherFrom)) {
                        result.add(new VariableValueRange<>(new ConstantCharacterValue(from)));
                    }
                }
                else {
                    char fromC = from.charAt(0);
                    char toC = to.charAt(0);
                    char otherFromC = otherFrom.charAt(0);
                    char otherToC = otherTo.charAt(0);

                    boolean fromIsInside = isTouching(fromC, fromC, otherFromC, otherToC);
                    boolean toIsInside = isTouching(toC, toC, otherFromC, otherToC);
                    boolean otherFromIsInside = isTouching(fromC, toC, otherFromC, otherFromC);
                    boolean otherToIsInside = isTouching(fromC, toC, otherToC, otherToC);

                    Character fromResult = null;
                    Character toResult = null;
                    if (fromIsInside && toIsInside) {
                        fromResult = fromC;
                        toResult = toC;
                    } else if (otherFromIsInside && otherToIsInside) {
                        fromResult = otherFromC;
                        toResult = otherToC;
                    } else if (fromIsInside) {
                        fromResult = fromC;
                        toResult = otherToC;
                    } else if (toIsInside) {
                        fromResult = otherFromC;
                        toResult = toC;
                    }
                    if (fromResult != null) {
                        result.add(new VariableValueRange<>(
                                new ConstantCharacterValue("" + fromResult),
                                new ConstantCharacterValue("" + toResult)
                        ));
                    }
                }
            });
        });
        return removeDuplicate(result);
    }

    public static Set<VariableValueRange<ConstantCharacterValue>> or(Set<VariableValueRange<ConstantCharacterValue>> ranges, Set<VariableValueRange<ConstantCharacterValue>> others) {
        Set<VariableValueRange<ConstantCharacterValue>> result = new HashSet<>();
        result.addAll(ranges);
        result.addAll(others);
        return removeDuplicate(result);
    }

    public static Set<VariableValueRange<ConstantBitValue>> charEqual(Set<VariableValueRange<ConstantCharacterValue>> ranges, Set<VariableValueRange<ConstantCharacterValue>> others) {
        boolean hasTrue = false;
        boolean hasFalse = false;

        for(VariableValueRange<ConstantCharacterValue> range : ranges) {
            String from = range.getFrom().getValue();
            String to = range.getTo().getValue();

            for(VariableValueRange<ConstantCharacterValue> other : others) {
                String otherFrom = other.getFrom().getValue();
                String otherTo = other.getTo().getValue();

                if(from.length() != 1 || to.length() != 1 || otherFrom.length() != 1 || otherTo.length() != 1) {
                    if(from.compareTo(to) != 0
                            || otherFrom.compareTo(otherTo) != 0)
                        throw new RuntimeException("compare stringRanges not implemented");
                    if(from.equals(otherFrom))
                        hasTrue = true;
                    else
                        hasFalse = true;
                }
                else {
                    char fromC = from.charAt(0);
                    char toC = to.charAt(0);
                    char otherFromC = otherFrom.charAt(0);
                    char otherToC = otherTo.charAt(0);

                    boolean isTouching = isTouching(fromC, toC, otherFromC, otherToC);
                    if(isTouching)
                        hasTrue = true;

                    if(fromC != otherFromC || toC != otherToC)
                        hasFalse = true;
                }
            }
        }
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        if(hasTrue)
            result.add(new VariableValueRange<>(new ConstantBitValue(1L, 1)));
        if (hasFalse)
            result.add(new VariableValueRange<>(new ConstantBitValue(0L, 1)));

        return result;
    }

    public static boolean isTouching(char x1, char x2, char y1, char y2) {
        return (x1 >= y1 && x1 <= y2) ||
                (x2 >= y1 && x2 <= y2) ||
                (y1 >= x1 && y1 <= x2) ||
                (y2 >= x1 && y2 <= x2);
    }

    public static Set<VariableValueRange<ConstantBitValue>> charNotEqual(Set<VariableValueRange<ConstantCharacterValue>> ranges, Set<VariableValueRange<ConstantCharacterValue>> others) {
        boolean hasTrue = false;
        boolean hasFalse = false;

        for(VariableValueRange<ConstantCharacterValue> range : ranges) {
            String from = range.getFrom().getValue();
            String to = range.getTo().getValue();

            if(!from.equals(to)) continue;
            for(VariableValueRange<ConstantCharacterValue> other : others) {
                String otherFrom = other.getFrom().getValue();
                String otherTo = other.getTo().getValue();
                if(!otherFrom.equals(otherTo)) continue;

                if(!from.equals(otherFrom))
                    hasTrue = true;
                else
                    hasFalse = true;
            }
        }
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        if(hasTrue)
            result.add(new VariableValueRange<>(new ConstantBitValue(1L, 1)));
        if (hasFalse)
            result.add(new VariableValueRange<>(new ConstantBitValue(0L, 1)));

        return result;
    }

    public static Set<VariableValueRange<ConstantCharacterValue>> charNotEqual(Set<VariableValueRange<ConstantCharacterValue>> ranges) {
        Set<VariableValueRange<ConstantCharacterValue>> result = new HashSet<>();
        List<VariableValueRange<ConstantCharacterValue>> sortedList = new ArrayList<>(removeDuplicate(ranges));
        sortedList.sort(Comparator.comparing(VariableValueRange::getFrom));
        char current = Character.MIN_VALUE;
        for(VariableValueRange<ConstantCharacterValue> range : sortedList) {
            if(range.getFrom().getLength() != 1 || range.getTo().getLength() != 1) return null;
            char from = range.getFrom().getValue().charAt(0);
            char to = range.getTo().getValue().charAt(0);
            if(current == from) {
                current = (char) (to + 1);
                continue;
            };
            result.add(new VariableValueRange<>(
                    new ConstantCharacterValue("" + current),
                    new ConstantCharacterValue("" + (char) (from - 1))
            ));
            if(to == Character.MAX_VALUE) return result;
            current = (char) (to + 1);
        }
        result.add(new VariableValueRange<>(
                new ConstantCharacterValue("" + current),
                new ConstantCharacterValue("" + Character.MAX_VALUE)
        ));
        return result;
    }

    private static Set<VariableValueRange<ConstantCharacterValue>> removeDuplicate(Set<VariableValueRange<ConstantCharacterValue>> ranges) {
        if(ranges.size() == 0) return ranges;
        List<VariableValueRange<ConstantCharacterValue>> newList = new ArrayList<>(ranges);
        Set<VariableValueRange<ConstantCharacterValue>> result = new HashSet<>();
        newList.sort(Comparator.comparing(VariableValueRange::getFrom));

        Iterator<VariableValueRange<ConstantCharacterValue>> iterator = newList.iterator();
        VariableValueRange<ConstantCharacterValue> current = iterator.next();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantCharacterValue> next = iterator.next();
            if(current.getFrom().getLength() != 1 || current.getTo().getLength() != 1
                    || next.getFrom().getLength() != 1 || next.getTo().getLength() != 1) {
                if(current.getFrom().getValue().compareTo(current.getTo().getValue()) != 0
                        || next.getFrom().getValue().compareTo(next.getTo().getValue()) != 0)
                    throw new RuntimeException("removing stringRange duplicates not implemented");
                if(current.getFrom().getValue().compareTo(next.getFrom().getValue()) != 0) {
                    result.add(current);
                    current = next;
                }
            }
            else {
                char from = current.getFrom().getValue().charAt(0);
                char to = current.getTo().getValue().charAt(0);
                char nextFrom = next.getFrom().getValue().charAt(0);
                char nextTo = next.getTo().getValue().charAt(0);
                if(to >= nextFrom) {
                    ConstantCharacterValue max = to > nextTo ? current.getTo() : next.getTo();
                    current.setTo(max);
                }
                else {
                    result.add(current);
                    current = next;
                }
            }
        }
        result.add(current);
        return result;
    }

    public static Set<VariableValueRange<ConstantCharacterValue>> concat(Set<VariableValueRange<ConstantCharacterValue>> ranges, Set<VariableValueRange<ConstantCharacterValue>> others) {
        Set<VariableValueRange<ConstantCharacterValue>> result = new HashSet<>();

        for(VariableValueRange<ConstantCharacterValue> range : ranges) {
            String from = range.getFrom().getValue();
            String to = range.getTo().getValue();

            if(!from.equals(to)) continue;
            for(VariableValueRange<ConstantCharacterValue> other : others) {
                String otherFrom = other.getFrom().getValue();
                String otherTo = other.getTo().getValue();
                if(!otherFrom.equals(otherTo)) continue;

                result.add(new VariableValueRange<>(new ConstantCharacterValue(from + otherFrom)));
            }
        }
        return result;
    }
}
