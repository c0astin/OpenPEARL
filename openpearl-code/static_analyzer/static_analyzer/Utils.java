/*
 * [A "BSD license"]
 *  Copyright (c) 2022 Jan Knoblauch
 *  
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

package static_analyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils
{
    public static String trim(String content, String trim)
    {
        int beginIndex = 0;
        int endIndex = content.length();

        while (content.substring(beginIndex, endIndex).startsWith(trim))
        {
            beginIndex += trim.length();
        }

        while (content.substring(beginIndex, endIndex).endsWith(trim))
        {
            endIndex -= trim.length();
        }

        return content.substring(beginIndex, endIndex);
    }

    public static boolean equalStrings(String a, String b)
    {
        return (toDef(a).equals(toDef(b)));
    }

    public static String toDef(String value)
    {
        if (value == null)
        {
            value = "";
        }

        return value;
    }

    public static boolean matchesPatternInList(List<String> patternList, String value, String patternSymbol)
    {
        for (String currentPattern : patternList)
        {
            if (matchesPattern(currentPattern, value, patternSymbol))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean matchesPattern(String pattern, String value, String patternSymbol)
    {
        String searchedString = pattern.replace(patternSymbol, "");

        if ((pattern.startsWith(patternSymbol)) && (pattern.endsWith(patternSymbol)))
        {
            return value.contains(searchedString);
        }
        else if (pattern.startsWith(patternSymbol))
        {
            return value.endsWith(searchedString);
        }
        else if (pattern.endsWith(patternSymbol))
        {
            return value.startsWith(searchedString);
        }
        else
        {
            return value.equals(searchedString);
        }
    }

    public static String intListToString(List<Integer> integerList, String separator)
    {
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < integerList.size(); i++)
        {
            content.append(integerList.get(i));

            if (i < integerList.size() - 1)
            {
                content.append(separator);
            }
        }

        return content.toString();
    }

    public static String listToString(List<String> stringList, String separator)
    {
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < stringList.size(); i++)
        {
            content.append(stringList.get(i));

            if (i < stringList.size() - 1)
            {
                content.append(separator);
            }
        }

        return content.toString();
    }

    public static <T> List<T> getDuplicates(List<T> list)
    {
        List<T> duplicates = new ArrayList<>();
        Set<T> uniques = new HashSet<>();

        for (T current : list)
        {
            if (!uniques.add(current))
            {
                duplicates.add(current);
            }
        }

        return duplicates;
    }
}
