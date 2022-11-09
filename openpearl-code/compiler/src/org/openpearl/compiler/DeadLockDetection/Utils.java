// hfujk

package org.openpearl.compiler.DeadLockDetection;

//import org.apache.commons.lang3.builder.ToStringBuilder;
//import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils
{
    static Random randomInstance = null;
    static List<String> randomIdentifiers = new ArrayList<>();

    public static String getRandomIdentifier(int length)
    {
        if (randomInstance == null)
        {
            randomInstance = new Random();
        }

        String randomIdentifier = getUniqueRandomIdentifier(length);

        while (randomIdentifiers.contains(randomIdentifier))
        {
            randomIdentifier = getUniqueRandomIdentifier(length);
        }

        return randomIdentifier;
    }

    private static String getUniqueRandomIdentifier(int length)
    {
        int leftLimit = 97;
        int rightLimit = 122;

        return randomInstance.ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

//    public static String dumpObject(Object object)
//    {
//        if (object == null)
//        {
//            return "[null]";
//        }
//
//        return ToStringBuilder.reflectionToString(object, ToStringStyle.JSON_STYLE);
//    }

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
}
