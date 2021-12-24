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

package org.openpearl.compiler.ControlFlowGraph;

import org.openpearl.compiler.ConstantBitValue;
import org.openpearl.compiler.ConstantFixedValue;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class BitOperations {

    private static Set<VariableValueRange<ConstantBitValue>> compare(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others, BiFunction<Integer, Integer, Boolean> func) {
        boolean hasTrue = false;
        boolean hasFalse = false;

        for(VariableValueRange<ConstantFixedValue> range : ranges) {
            for(VariableValueRange<ConstantFixedValue> other : others) {
                boolean val1 = func.apply(range.getFrom().compareTo(other.getFrom()), 0);
                boolean val2 = func.apply(range.getFrom().compareTo(other.getTo()), 0);
                boolean val3 = func.apply(range.getTo().compareTo(other.getFrom()), 0);
                boolean val4 = func.apply(range.getTo().compareTo(other.getTo()), 0);
                if(val1 || val2 || val3 || val4)
                    hasTrue = true;
                if(!val1 || !val2 || !val3 || !val4)
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

    private static boolean less(int a, int b) { return a < b; }
    private static boolean greater(int a, int b) { return a > b; }
    private static boolean lessEqual(int a, int b) { return a <= b; }
    private static boolean greaterEqual(int a, int b) { return a >= b; }

    public static Set<VariableValueRange<ConstantBitValue>> less(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        return compare(ranges, others, BitOperations::less);
    }

    public static Set<VariableValueRange<ConstantBitValue>> greater(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        return compare(ranges, others, BitOperations::greater);
    }

    public static Set<VariableValueRange<ConstantBitValue>> lessEqual(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        return compare(ranges, others, BitOperations::lessEqual);
    }

    public static Set<VariableValueRange<ConstantBitValue>> greaterEqual(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        return compare(ranges, others, BitOperations::greaterEqual);
    }

    public static Set<VariableValueRange<ConstantBitValue>> equal(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        boolean hasTrue = false;
        boolean hasFalse = false;

        for(VariableValueRange<ConstantFixedValue> range : ranges) {
            long from = range.getFrom().getValue();
            long to = range.getTo().getValue();
            for(VariableValueRange<ConstantFixedValue> other : others) {
                long otherFrom = other.getFrom().getValue();
                long otherTo = other.getTo().getValue();

                boolean isTouching = isTouching(from, to, otherFrom, otherTo);
                if(isTouching)
                    hasTrue = true;

                if(from != otherFrom || to != otherTo)
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

    public static Set<VariableValueRange<ConstantBitValue>> notEqual(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        boolean hasTrue = false;
        boolean hasFalse = false;

        for(VariableValueRange<ConstantFixedValue> range : ranges) {
            long from = range.getFrom().getValue();
            long to = range.getTo().getValue();
            for(VariableValueRange<ConstantFixedValue> other : others) {
                long otherFrom = other.getFrom().getValue();
                long otherTo = other.getTo().getValue();

                boolean isTouching = isTouching(from, to, otherFrom, otherTo);

                if(isTouching)
                    hasFalse = true;

                if(from != otherFrom || to != otherTo)
                    hasTrue = true;
            }
        }
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        if(hasTrue)
            result.add(new VariableValueRange<>(new ConstantBitValue(1L, 1)));
        if (hasFalse)
            result.add(new VariableValueRange<>(new ConstantBitValue(0L, 1)));

        return result;
    }

    public static Set<VariableValueRange<ConstantBitValue>> bitEqual(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantBitValue>> others) {
        boolean hasTrue = false;
        boolean hasFalse = false;

        for(VariableValueRange<ConstantBitValue> range : ranges) {
            long from = mask(range.getFrom().getLongValue(), range.getFrom().getLength());
            long to = mask(range.getTo().getLongValue(), range.getTo().getLength());
            for(VariableValueRange<ConstantBitValue> other : others) {
                long otherFrom = mask(other.getFrom().getLongValue(), other.getFrom().getLength());
                long otherTo = mask(other.getTo().getLongValue(), other.getTo().getLength());

                boolean isTouching = isTouching(from, to, otherFrom, otherTo);

                if(isTouching)
                    hasTrue = true;

                if(from != otherFrom || to != otherTo)
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

    public static Set<VariableValueRange<ConstantBitValue>> bitNotEqual(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantBitValue>> others) {
        boolean hasTrue = false;
        boolean hasFalse = false;

        for(VariableValueRange<ConstantBitValue> range : ranges) {
            long from = mask(range.getFrom().getLongValue(), range.getFrom().getLength());
            long to = mask(range.getTo().getLongValue(), range.getTo().getLength());
            for(VariableValueRange<ConstantBitValue> other : others) {
                long otherFrom = mask(other.getFrom().getLongValue(), other.getFrom().getLength());
                long otherTo = mask(other.getTo().getLongValue(), other.getTo().getLength());

                boolean isTouching = isTouching(from, to, otherFrom, otherTo);

                if(isTouching)
                    hasFalse = true;

                if(from != otherFrom || to != otherTo)
                    hasTrue = true;
            }
        }
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        if(hasTrue)
            result.add(new VariableValueRange<>(new ConstantBitValue(1L, 1)));
        if (hasFalse)
            result.add(new VariableValueRange<>(new ConstantBitValue(0L, 1)));

        return result;
    }

    public static Set<VariableValueRange<ConstantBitValue>> and(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantBitValue>> others) {
        HashSet<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        if(hasTrue(ranges) && hasTrue(others))
            result.add(new VariableValueRange<>(new ConstantBitValue(1L, 1)));
        if(hasFalse(ranges) || hasFalse(others))
            result.add(new VariableValueRange<>(new ConstantBitValue(0L, 1)));
        return result;
    }

    public static Set<VariableValueRange<ConstantBitValue>> or(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantBitValue>> others) {
        HashSet<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        if(hasTrue(ranges) || hasTrue(others))
            result.add(new VariableValueRange<>(new ConstantBitValue(1L, 1)));
        if(hasFalse(ranges) && hasFalse(others))
            result.add(new VariableValueRange<>(new ConstantBitValue(0L, 1)));
        return result;
    }

    public static Set<VariableValueRange<ConstantBitValue>> h_or(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantBitValue>> others) {
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        result.addAll(ranges);
        result.addAll(others);
        return removeDuplicate(result);
    }

    public static Set<VariableValueRange<ConstantBitValue>> h_and(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantBitValue>> others) {
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        ranges.forEach(range -> {
            long from = range.getFrom().getLongValue();
            long to = range.getTo().getLongValue();
            others.forEach(other -> {
                long otherFrom = other.getFrom().getLongValue();
                long otherTo = other.getTo().getLongValue();
                boolean fromIsInside = isTouching(from, from, otherFrom, otherTo);
                boolean toIsInside = isTouching(to, to, otherFrom, otherTo);
                boolean otherFromIsInside = isTouching(from, to, otherFrom, otherFrom);
                boolean otherToIsInside = isTouching(from, to, otherTo, otherTo);

                Long fromResult = null;
                Long toResult = null;
                if (fromIsInside && toIsInside) {
                    fromResult = from;
                    toResult = to;
                } else if (otherFromIsInside && otherToIsInside) {
                    fromResult = otherFrom;
                    toResult = otherTo;
                } else if (fromIsInside) {
                    fromResult = from;
                    toResult = otherTo;
                } else if (toIsInside) {
                    fromResult = otherFrom;
                    toResult = to;
                }


                int fromMaxLength = 0;
                if(range.getFrom().getLength() > fromMaxLength) fromMaxLength = range.getFrom().getLength();
                if(range.getTo().getLength() > fromMaxLength) fromMaxLength = range.getTo().getLength();

                int toMaxLength = 0;
                if(other.getFrom().getLength() > toMaxLength) toMaxLength = other.getFrom().getLength();
                if(other.getTo().getLength() > toMaxLength) toMaxLength = other.getTo().getLength();

                if (fromResult != null) {
                    result.add(new VariableValueRange<>(
                            new ConstantBitValue(fromResult, fromMaxLength),
                            new ConstantBitValue(toResult, toMaxLength)
                    ));
                }
            });
        });
        return removeDuplicate(result);
    }

    private static Set<VariableValueRange<ConstantBitValue>> removeDuplicate(Set<VariableValueRange<ConstantBitValue>> ranges) {
        if(ranges.size() == 0) return ranges;
        List<VariableValueRange<ConstantBitValue>> newList = new ArrayList<>(ranges);
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        newList.sort(Comparator.comparing(VariableValueRange::getFrom));

        Iterator<VariableValueRange<ConstantBitValue>> iterator = newList.iterator();
        VariableValueRange<ConstantBitValue> current = iterator.next();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantBitValue> next = iterator.next();
            if(current.getTo().getLongValue() >= next.getFrom().getLongValue()) {
                ConstantBitValue max = current.getTo().getLongValue() > next.getTo().getLongValue() ? current.getTo() : next.getTo();
                current.setTo(max);
            }
            else {
                result.add(current);
                current = next;
            }
        }
        result.add(current);
        return result;
    }

    public static Set<VariableValueRange<ConstantBitValue>> exor(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantBitValue>> others) {
        HashSet<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        if(hasTrue(ranges) && hasTrue(others))
            result.add(new VariableValueRange<>(new ConstantBitValue(0L, 1)));
        if(hasTrue(ranges) || hasTrue(others))
            result.add(new VariableValueRange<>(new ConstantBitValue(1L, 1)));
        if(hasFalse(ranges) || hasFalse(others))
            result.add(new VariableValueRange<>(new ConstantBitValue(0L, 1)));
        return result;
    }

    public static Set<VariableValueRange<ConstantFixedValue>> toFixed(Set<VariableValueRange<ConstantBitValue>> ranges) {
        Set<VariableValueRange<ConstantFixedValue>> results = new HashSet<>();
        ranges.forEach(range -> {
            long from = mask(range.getFrom().getLongValue(), range.getFrom().getLength());
            long to = mask(range.getTo().getLongValue(), range.getTo().getLength());
            results.add(new VariableValueRange<>(
                    new ConstantFixedValue(from),
                    new ConstantFixedValue(to)
            ));
        });
        return results;
    }

    public static Set<VariableValueRange<ConstantBitValue>> toBit(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        Set<VariableValueRange<ConstantBitValue>> results = new HashSet<>();
        ranges.forEach(range -> {
            long from = range.getFrom().getValue();
            long to = range.getTo().getValue();
            results.add(new VariableValueRange<>(
                    new ConstantBitValue(from, 64),
                    new ConstantBitValue(to, 64)
            ));
        });
        return results;
    }

    public static Set<VariableValueRange<ConstantBitValue>> not(Set<VariableValueRange<ConstantBitValue>> ranges) {
        Set<VariableValueRange<ConstantBitValue>> results = new HashSet<>();
        ranges.forEach(range -> {
            long from = range.getFrom().getLongValue();
            int fromLength = range.getFrom().getLength();
            long to = range.getTo().getLongValue();
            int toLength = range.getTo().getLength();

            results.add(new VariableValueRange<>(
                    new ConstantBitValue( mask(~from, fromLength), fromLength),
                    new ConstantBitValue(mask(~to, toLength), toLength)
            ));
        });
        return results;
    }

    public static Set<VariableValueRange<ConstantBitValue>> shift(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> shiftBy) {
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantBitValue(0L, 64),
                new ConstantBitValue(Long.MAX_VALUE, 64)
        )));
    }

    public static Set<VariableValueRange<ConstantBitValue>> cShift(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> shiftBy) {
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantBitValue(0L, 64),
                new ConstantBitValue(Long.MAX_VALUE, 64)
        )));
    }

    public static Set<VariableValueRange<ConstantBitValue>> concat(Set<VariableValueRange<ConstantBitValue>> ranges, Set<VariableValueRange<ConstantBitValue>> others) {
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantBitValue(0L, 64),
                new ConstantBitValue(Long.MAX_VALUE, 64)
        )));
    }

    public static boolean isTouching(long x1, long x2, long y1, long y2) {
        return (x1 >= y1 && x1 <= y2) ||
                (x2 >= y1 && x2 <= y2) ||
                (y1 >= x1 && y1 <= x2) ||
                (y2 >= x1 && y2 <= x2);
    }

    private static long mask(long value, int bits) {
        if(bits == 64) return value;
        long mask = (1L << bits) - 1L;
        return value & mask;
    }

    private static boolean hasTrue(Set<VariableValueRange<ConstantBitValue>> ranges) {
        boolean hasTrue = false;
        for(VariableValueRange<ConstantBitValue> range : ranges) {
            if(range.getFrom().getLongValue() > 0L || range.getTo().getLongValue() > 0L) hasTrue = true;
        }
        return hasTrue;
    }

    private static boolean hasFalse(Set<VariableValueRange<ConstantBitValue>> ranges) {
        boolean hasFalse = false;
        if(ranges.size() == 0) return true;
        for(VariableValueRange<ConstantBitValue> range : ranges) {
            if(range.getFrom().getLongValue() == 0L || range.getTo().getLongValue() == 0L) hasFalse = true;
        }
        return hasFalse;
    }

    public static Set<VariableValueRange<ConstantBitValue>> bitNotEqual(Set<VariableValueRange<ConstantBitValue>> ranges) {
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        List<VariableValueRange<ConstantBitValue>> sortedList = new ArrayList<>(removeDuplicate(ranges));
        sortedList.sort(Comparator.comparing(VariableValueRange::getFrom));
        long current = 0;
        for(VariableValueRange<ConstantBitValue> range : sortedList) {
            long from = range.getFrom().getLongValue();
            long to = range.getTo().getLongValue();
            if(current == from) {
                current = to + 1;
                continue;
            };
            result.add(new VariableValueRange<>(
                    new ConstantBitValue(current, 64),
                    new ConstantBitValue(from - 1, 64)
            ));
            if(to == Long.MAX_VALUE) return result;
            current = to + 1;
        }
        result.add(new VariableValueRange<>(
                new ConstantBitValue(current, 64),
                new ConstantBitValue(Long.MAX_VALUE, 64)
        ));
        return result;
    }

    public static Set<VariableValueRange<ConstantBitValue>> h_less(Set<VariableValueRange<ConstantBitValue>> ranges) {
        Set<Long> values = ranges.stream().map(range -> range.getTo().getLongValue()).collect(Collectors.toSet());
        Set<Integer> amountBit = ranges.stream().map(range -> range.getTo().getLength()).collect(Collectors.toSet());

        long largest = FixedOperations.getLargest(values);
        int largestAmountBit = FixedOperations.getLargest(amountBit);
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantBitValue(0, largestAmountBit),
                new ConstantBitValue(largest-1, largestAmountBit)
        )));
    }

    public static Set<VariableValueRange<ConstantBitValue>> h_greater(Set<VariableValueRange<ConstantBitValue>> ranges) {
        Set<Long> values = ranges.stream().map(range -> range.getFrom().getLongValue()).collect(Collectors.toSet());

        long smallest = FixedOperations.getSmallest(values);
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantBitValue(smallest+1, 64),
                new ConstantBitValue(Long.MAX_VALUE, 64)
        )));
    }

    public static Set<VariableValueRange<ConstantBitValue>> h_lessEqual(Set<VariableValueRange<ConstantBitValue>> ranges) {
        Set<Long> values = ranges.stream().map(range -> range.getTo().getLongValue()).collect(Collectors.toSet());
        Set<Integer> amountBit = ranges.stream().map(range -> range.getTo().getLength()).collect(Collectors.toSet());

        long largest = FixedOperations.getLargest(values);
        int largestAmountBit = FixedOperations.getLargest(amountBit);
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantBitValue(0, largestAmountBit),
                new ConstantBitValue(largest, largestAmountBit)
        )));
    }

    public static Set<VariableValueRange<ConstantBitValue>> h_greaterEqual(Set<VariableValueRange<ConstantBitValue>> ranges) {
        Set<Long> values = ranges.stream().map(range -> range.getFrom().getLongValue()).collect(Collectors.toSet());

        long smallest = FixedOperations.getSmallest(values);
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantBitValue(smallest, 64),
                new ConstantBitValue(Long.MAX_VALUE, 64)
        )));
    }

    public static Set<VariableValueRange<ConstantBitValue>> h_notEqual(Set<VariableValueRange<ConstantBitValue>> ranges) {
        Set<VariableValueRange<ConstantBitValue>> result = new HashSet<>();
        List<VariableValueRange<ConstantBitValue>> sortedList = new ArrayList<>(removeDuplicate(ranges));
        sortedList.sort(Comparator.comparing(VariableValueRange::getFrom));
        long current = 0;
        for(VariableValueRange<ConstantBitValue> range : sortedList) {
            if(current == range.getFrom().getLongValue()) {
                current = range.getTo().getLongValue() + 1;
                continue;
            };
            result.add(new VariableValueRange<>(
                    new ConstantBitValue(current, 64),
                    new ConstantBitValue(range.getFrom().getLongValue() - 1, 64)
            ));
            if(range.getTo().getLongValue() == Long.MAX_VALUE) return result;
            current = range.getTo().getLongValue() + 1;
        }
        result.add(new VariableValueRange<>(
                new ConstantBitValue(current, 64),
                new ConstantBitValue(Long.MAX_VALUE, 64)
        ));
        return result;
    }
}
