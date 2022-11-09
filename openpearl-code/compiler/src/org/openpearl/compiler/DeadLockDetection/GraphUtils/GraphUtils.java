// hfujk

package org.openpearl.compiler.DeadLockDetection.GraphUtils;

public class GraphUtils
{
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
}
