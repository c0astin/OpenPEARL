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
