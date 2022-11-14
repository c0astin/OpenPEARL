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

package org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities;

//import com.eclipsesource.json.JsonArray;
//import com.eclipsesource.json.JsonObject;
import javax.json.*;


import java.util.HashMap;
import java.util.Map;

public class ControlFlowDataWrapper
{
    private final Map<String, Object> values = new HashMap<>();

    public ControlFlowDataWrapper set(String key, Object value)
    {
        values.put(key, value);

        return this;
    }

    public void reset()
    {
        values.clear();
    }

    public String get()
    {
        //JsonObject jsonObject = new JsonObject();
        JsonObjectBuilder job = Json.createObjectBuilder();

        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            String key = escapeKey(entry.getKey());
            Object value = entry.getValue();

            if (entry.getValue() instanceof String)
            {
                value = escapeValue((String) value);
            }

            //addValue(jsonObject, key, value);
            addValue(job,key,value);
        }

        //return jsonObject.toString().replace("\"", "'");
        //return job.build().toString().replace("\"","'");
        
        // new encoding strategy
        // the jsonbuilder already treats quotes in strings
        // thus only the surrounding quotes of values and keys must be quoted to
        // make dot happy
        // let's look on each double quote which has no leading backslash 
        String json = job.build().toString();
        String encoded = "";
        for (int i=0; i<json.length(); i++) {
            if (json.charAt(i) == '"' && i>0 && json.charAt(i-1) != '\\') {
                encoded += '\\';
                encoded += '"';
            } else {
               encoded += json.charAt(i);
            }
        }
        //json.replaceAll( "\"", "\\\"");
        //json.replaceAll("\\", "\\\\");
        return encoded;
    }

    private static String escapeKey(String key)
    {
        return escapeValue(key);
    }

    private static String escapeValue(String value)
    {
        value= value.replace("\"", "\\\"");
//                .replace("'", "#");
        return value;
    }

   // private void addValue(JsonObject jsonObject, String key, Object value)
    private void addValue(JsonObjectBuilder jsonObject, String key, Object value)
    {
        if (value instanceof String)
        {
            jsonObject.add(key, (String) value);
        }
        else if (value instanceof Integer)
        {
            jsonObject.add(key, (Integer) value);
        }
        else if (value instanceof Float)
        {
            jsonObject.add(key, (Float) value);
        }
        else if (value instanceof Double)
        {
            jsonObject.add(key, (Double) value);
        }
        else if (value instanceof Long)
        {
            jsonObject.add(key, (Long) value);
        }
        else if (value instanceof Boolean)
        {
            jsonObject.add(key, (Boolean) value);
        }
        else if (value instanceof JsonArray)
        {
            jsonObject.add(key, (JsonArray) value);
        }
    }
}
