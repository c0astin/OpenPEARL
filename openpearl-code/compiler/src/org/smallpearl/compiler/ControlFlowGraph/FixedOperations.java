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

import org.smallpearl.compiler.ConstantFixedValue;
import org.smallpearl.compiler.ConstantFloatValue;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class FixedOperations {

    public static Set<VariableValueRange<ConstantFixedValue>> negate(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        return removeDuplicate(ranges.stream()
                .map(range -> {
                  long from = range.getFrom().getValue();
                  int fromPrec = range.getFrom().getPrecision();
                  long to = range.getTo().getValue();
                  int toPrec = range.getTo().getPrecision();
                  return new VariableValueRange<>(
                          new ConstantFixedValue(from * -1, fromPrec),
                          new ConstantFixedValue(to * -1, toPrec));
                })
                .collect(Collectors.toSet()));
    }

    public static Set<VariableValueRange<ConstantFixedValue>> abs(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        return removeDuplicate(ranges.stream()
                .map(range -> {
                    long from = range.getFrom().getValue();
                    int fromPrec = range.getFrom().getPrecision();
                    long to = range.getTo().getValue();
                    int toPrec = range.getTo().getPrecision();

                    if(from < 0 && to > 0)
                        return new VariableValueRange<>(
                                new ConstantFixedValue(0L, fromPrec),
                                new ConstantFixedValue(to, toPrec));
                    else {
                        long val1 = Math.abs(from);
                        long val2 = Math.abs(to);
                        if(val1 < val2) {
                            return new VariableValueRange<>(
                                    new ConstantFixedValue(val1, fromPrec),
                                    new ConstantFixedValue(val2, toPrec));
                        }
                        else {
                            return new VariableValueRange<>(
                                    new ConstantFixedValue(val2, toPrec),
                                    new ConstantFixedValue(val1, fromPrec));
                        }
                    }
                })
                .collect(Collectors.toSet()));
    }

    public static Set<VariableValueRange<ConstantFixedValue>> sign(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        Set<VariableValueRange<ConstantFixedValue>> resultSet = new HashSet<>();
        ranges.forEach(range -> {
            long from = range.getFrom().getValue();
            long to = range.getTo().getValue();

            if(from < 0)
                resultSet.add(new VariableValueRange<>(new ConstantFixedValue(-1L)));
            if(to > 0)
                resultSet.add(new VariableValueRange<>(new ConstantFixedValue(1L)));
            if(from <= 0 && to >= 0) {
                resultSet.add(new VariableValueRange<>(new ConstantFixedValue(0L)));
            }
        });
        return removeDuplicate(resultSet);
    }

    private static Set<VariableValueRange<ConstantFixedValue>> calculate(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others, BiFunction<BigInteger, BigInteger, Long> func) {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();

        ranges.forEach(range -> {
                    BigInteger from = BigInteger.valueOf(range.getFrom().getValue());
                    int fromPrec = range.getFrom().getPrecision();
                    BigInteger to = BigInteger.valueOf(range.getTo().getValue());
                    int toPrec = range.getTo().getPrecision();

                    others.forEach(other -> {
                        BigInteger otherFrom = BigInteger.valueOf(other.getFrom().getValue());
                        BigInteger otherTo = BigInteger.valueOf(other.getTo().getValue());

                        List<Long> values = Arrays.asList(
                                func.apply(from, otherFrom),
                                func.apply(from, otherTo),
                                func.apply(to, otherFrom),
                                func.apply(to, otherTo)
                        );

                        result.add(new VariableValueRange<>(
                                new ConstantFixedValue(getSmallest(values), fromPrec),
                                new ConstantFixedValue(getLargest(values), toPrec)
                        ));
                    });
                });
        return removeDuplicate(result);
    }

    private static BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
    private static BigInteger minLong = BigInteger.valueOf(Long.MIN_VALUE);

    private static long add(BigInteger a, BigInteger b) {
        BigInteger result = a.add(b);
        if(result.compareTo(maxLong) > 0) result = maxLong;
        else if(result.compareTo(minLong) < 0) result = minLong;
        return result.longValue();
    }
    private static long sub(BigInteger a, BigInteger b) {
        BigInteger result = a.subtract(b);
        if(result.compareTo(maxLong) > 0) result = maxLong;
        else if(result.compareTo(minLong) < 0) result = minLong;
        return result.longValue();
    }
    private static long mul(BigInteger a, BigInteger b) {
        BigInteger result = a.multiply(b);
        if(result.compareTo(maxLong) > 0) result = maxLong;
        else if(result.compareTo(minLong) < 0) result = minLong;
        return result.longValue();
    }

    public static Set<VariableValueRange<ConstantFixedValue>> add(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        return calculate(ranges, others, FixedOperations::add);
    }

    public static Set<VariableValueRange<ConstantFixedValue>> sub(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        return calculate(ranges, others, FixedOperations::sub);
    }

    public static Set<VariableValueRange<ConstantFixedValue>> mul(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        return calculate(ranges, others, FixedOperations::mul);
    }

    public static Set<VariableValueRange<ConstantFloatValue>> div(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        Set<VariableValueRange<ConstantFloatValue>> result = new HashSet<>();

        ranges.forEach(range -> {
            double from = range.getFrom().getValue();
            double to = range.getTo().getValue();

            others.forEach(other -> {
                double otherFrom = other.getFrom().getValue();
                double otherTo = other.getTo().getValue();
                if(otherFrom < 0 && otherTo > 0) {
                    VariableValueRange<Double> range1 = divide(from, to, otherFrom, -1);
                    VariableValueRange<Double> range2 = divide(from, to, 1, otherTo);
                    if(range1 != null) {
                        result.add(new VariableValueRange<>(
                                new ConstantFloatValue(range1.getFrom(), 52),
                                new ConstantFloatValue(range1.getTo(), 52)
                        ));
                    }
                    if(range2 != null) {
                        result.add(new VariableValueRange<>(
                                new ConstantFloatValue(range2.getFrom(), 52),
                                new ConstantFloatValue(range2.getTo(), 52)
                        ));
                    }
                }
                else {
                    VariableValueRange<Double> range1 = divide(from, to, otherFrom, otherTo);
                    if(range1 != null) {
                        result.add(new VariableValueRange<>(
                                new ConstantFloatValue(range1.getFrom(), 52),
                                new ConstantFloatValue(range1.getTo(), 52)
                        ));
                    }
                }
            });
        });

        return removeDuplicate(result);
    }

    private static VariableValueRange<Double> divide(double from, double to, double otherFrom, double otherTo) {
        if(otherFrom == 0 && otherTo == 0) return null;

        if(otherFrom == 0) {
            otherFrom++;
        }
        else if(otherTo == 0) {
            otherTo--;
        }

        List<Double> values = Arrays.asList(
                from / otherFrom ,
                from / otherTo,
                to / otherFrom,
                to / otherTo
        );
        return new VariableValueRange<>(getSmallest(values), getLargest(values));
    }

    public static Set<VariableValueRange<ConstantFixedValue>> intDiv(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        ranges.forEach(range -> {
            others.forEach(other -> {
                VariableValueRange<ConstantFixedValue> val = intDiv(range, other);
                if (val != null)
                    result.add(val);
            });
        });
        if(result.isEmpty())
            result.add(new VariableValueRange<>(
                    new ConstantFixedValue(Long.MIN_VALUE, 63),
                    new ConstantFixedValue(Long.MAX_VALUE, 63)));
        return removeDuplicate(result);
    }

    public static Set<VariableValueRange<ConstantFixedValue>> rem(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        Iterator<VariableValueRange<ConstantFixedValue>> iterator = abs(others).iterator();
        VariableValueRange<ConstantFixedValue> current = iterator.next();
        int precision = current.getTo().getPrecision();
        long largest = current.getTo().getValue();
        while (iterator.hasNext()) {
            VariableValueRange<ConstantFixedValue> next = iterator.next();
            int nextPrecision = next.getTo().getPrecision();
            long nextValue = next.getTo().getValue();
            if(largest < nextValue) {
                largest = nextValue;
                precision = nextPrecision;
            }
        }
        result.add(new VariableValueRange<>(new ConstantFixedValue(0L, precision), new ConstantFixedValue(largest-1, precision)));
        return result;
    }

    public static Set<VariableValueRange<ConstantFixedValue>> exp(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        ranges.forEach(range -> {
            others.forEach(other -> {
                long from = range.getFrom().getValue();
                int fromPrec = range.getFrom().getPrecision();
                long to = range.getTo().getValue();
                int toPrec = range.getTo().getPrecision();

                long otherFrom = other.getFrom().getValue();
                long otherTo = other.getTo().getValue();
                result.add(new VariableValueRange<>(
                        new ConstantFixedValue((long)Math.pow(from, otherFrom), fromPrec),
                        new ConstantFixedValue((long)Math.pow(to, otherTo), toPrec)
                ));
            });
        });
        return removeDuplicate(result);
    }

    public static Set<VariableValueRange<ConstantFixedValue>> fit(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        ranges.forEach(range -> {
            others.forEach(other -> {
                long from = range.getFrom().getValue();
                long to = range.getTo().getValue();

                long otherFrom = other.getFrom().getValue();
                int otherFromPrec = range.getFrom().getPrecision();
                long otherTo = other.getTo().getValue();
                int otherToPrec = range.getTo().getPrecision();
                result.add(new VariableValueRange<>(
                        new ConstantFixedValue(from, otherFromPrec),
                        new ConstantFixedValue(to, otherToPrec)
                ));
            });
        });
        return removeDuplicate(result);
    }

    private static VariableValueRange<ConstantFixedValue> intDiv(VariableValueRange<ConstantFixedValue> range, VariableValueRange<ConstantFixedValue> other) {
        long from = range.getFrom().getValue();
        int fromPrec = range.getFrom().getPrecision();
        long to = range.getTo().getValue();
        int toPrec = range.getTo().getPrecision();

        long otherFrom = other.getFrom().getValue();
        long otherTo = other.getTo().getValue();


        if(otherFrom == otherTo && otherFrom == 0)
            return null;
        otherFrom = otherFrom != 0 ? otherFrom : otherFrom+1;
        otherTo = otherTo != 0 ? otherTo: otherTo-1;

        return new VariableValueRange<>(
                new ConstantFixedValue(from / otherFrom, fromPrec),
                new ConstantFixedValue(to / otherTo, toPrec)
        );
    }


    public static Set<VariableValueRange<ConstantFixedValue>> less(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        Set<Long> values = ranges.stream().map(range -> range.getTo().getValue()).collect(Collectors.toSet());
        long largest = getLargest(values);
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantFixedValue(Long.MIN_VALUE),
                new ConstantFixedValue(largest-1)
        )));
    }

    public static Set<VariableValueRange<ConstantFixedValue>> greater(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        Set<Long> values = ranges.stream().map(range -> range.getFrom().getValue()).collect(Collectors.toSet());
        long smallest = getSmallest(values);
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantFixedValue(smallest+1),
                new ConstantFixedValue(Long.MAX_VALUE)
        )));
    }

    public static Set<VariableValueRange<ConstantFixedValue>> lessEqual(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        Set<Long> values = ranges.stream().map(range -> range.getTo().getValue()).collect(Collectors.toSet());
        long largest = getLargest(values);
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantFixedValue(Long.MIN_VALUE),
                new ConstantFixedValue(largest)
        )));
    }

    public static Set<VariableValueRange<ConstantFixedValue>> greaterEqual(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        Set<Long> values = ranges.stream().map(range -> range.getFrom().getValue()).collect(Collectors.toSet());
        long smallest = getSmallest(values);
        return new HashSet<>(Collections.singletonList(new VariableValueRange<>(
                new ConstantFixedValue(smallest),
                new ConstantFixedValue(Long.MAX_VALUE)
        )));
    }

    public static Set<VariableValueRange<ConstantFixedValue>> notEqual(Set<VariableValueRange<ConstantFixedValue>> ranges) {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        List<VariableValueRange<ConstantFixedValue>> sortedList = new ArrayList<>(removeDuplicate(ranges));
        sortedList.sort(Comparator.comparing(VariableValueRange::getFrom));
        long current = Long.MIN_VALUE;
        for(VariableValueRange<ConstantFixedValue> range : sortedList) {
            if(current == range.getFrom().getValue()) {
                current = range.getTo().getValue() + 1;
                continue;
            };
            result.add(new VariableValueRange<>(
                    new ConstantFixedValue(current),
                    new ConstantFixedValue(range.getFrom().getValue() - 1)
            ));
            if(range.getTo().getValue() == Long.MAX_VALUE) return result;
            current = range.getTo().getValue() + 1;
        }
        result.add(new VariableValueRange<>(
                new ConstantFixedValue(current),
                new ConstantFixedValue(Long.MAX_VALUE)
        ));
        return result;
    }

    public static Set<VariableValueRange<ConstantFixedValue>> or(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        result.addAll(ranges);
        result.addAll(others);
        return removeDuplicate(result);
    }

    public static Set<VariableValueRange<ConstantFixedValue>> xor(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        Set<VariableValueRange<ConstantFixedValue>> andResult = and(ranges, others);

        Set<VariableValueRange<ConstantFixedValue>> orResult = new HashSet<>();
        orResult.addAll(ranges);
        orResult.addAll(others);

        Set<VariableValueRange<ConstantFixedValue>> result = and(notEqual(andResult), orResult);

        return removeDuplicate(result);
    }

    public static Set<VariableValueRange<ConstantFixedValue>> and(Set<VariableValueRange<ConstantFixedValue>> ranges, Set<VariableValueRange<ConstantFixedValue>> others) {
        Set<VariableValueRange<ConstantFixedValue>> result = new HashSet<>();
        ranges.forEach(range -> {
            others.forEach(other -> {
                boolean fromIsInside = isTouching(new VariableValueRange<>(range.getFrom(), range.getFrom()), other);
                boolean toIsInside = isTouching(new VariableValueRange<>(range.getTo(), range.getTo()), other);
                boolean otherFromIsInside = isTouching(range, new VariableValueRange<>(other.getFrom(), other.getFrom()));
                boolean otherToIsInside = isTouching(range, new VariableValueRange<>(other.getTo(), other.getTo()));

                Long fromResult = null;
                Long toResult = null;
                if (fromIsInside && toIsInside) {
                    fromResult = range.getFrom().getValue();
                    toResult = range.getTo().getValue();
                } else if (otherFromIsInside && otherToIsInside) {
                    fromResult = other.getFrom().getValue();
                    toResult = other.getTo().getValue();
                } else if (fromIsInside) {
                    fromResult = range.getFrom().getValue();
                    toResult = other.getTo().getValue();
                } else if (toIsInside) {
                    fromResult = other.getFrom().getValue();
                    toResult = range.getTo().getValue();
                }
                if (fromResult != null) {
                    result.add(new VariableValueRange<>(
                            new ConstantFixedValue(fromResult),
                            new ConstantFixedValue(toResult)
                    ));
                }
            });
        });
        return removeDuplicate(result);
    }

    public static boolean isTouching(VariableValueRange<ConstantFixedValue> range, VariableValueRange<ConstantFixedValue> other) {
        long x1 = range.getFrom().getValue();
        long x2 = range.getTo().getValue();
        long y1 = other.getFrom().getValue();
        long y2 = other.getTo().getValue();
        return (x1 >= y1 && x1 <= y2) ||
                (x2 >= y1 && x2 <= y2) ||
                (y1 >= x1 && y1 <= x2) ||
                (y2 >= x1 && y2 <= x2);
    }

    private static <T extends Comparable<T>> Set<VariableValueRange<T>> removeDuplicate(Set<VariableValueRange<T>> ranges) {
        if(ranges.size() == 0) return ranges;
        List<VariableValueRange<T>> newList = new ArrayList<>(ranges);
        Set<VariableValueRange<T>> result = new HashSet<>();
        newList.sort(Comparator.comparing(VariableValueRange::getFrom));

        Iterator<VariableValueRange<T>> iterator = newList.iterator();
        VariableValueRange<T> current = iterator.next();
        while (iterator.hasNext()) {
            VariableValueRange<T> next = iterator.next();
            if(current.getTo().compareTo(next.getFrom()) >= 0) {
                T max = current.getTo().compareTo(next.getTo()) > 0 ? current.getTo() : next.getTo();
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

    private static <T extends Comparable<T>> T getSmallest(Collection<T> values) {
        Iterator<T> iterator = values.iterator();
        T smallest = iterator.next();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if(smallest.compareTo(next) > 0)
                smallest = next;
        }
        return smallest;
    }

    private static <T extends Comparable<T>> T getLargest(Collection<T> values) {
        Iterator<T> iterator = values.iterator();
        T largest = iterator.next();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if(largest.compareTo(next) < 0)
                largest = next;
        }
        return largest;
    }
}
