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
