// hfujk

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
