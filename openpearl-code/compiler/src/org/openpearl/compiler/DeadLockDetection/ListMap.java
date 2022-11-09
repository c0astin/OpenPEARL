// hfujk

package org.openpearl.compiler.DeadLockDetection;

import java.util.*;

public class ListMap<Key, Value>
{
    Map<Key, List<Value>> map = new HashMap<>();

    public void add(Key key, Value value, boolean allowDuplicates)
    {
        if (map.containsKey(key))
        {
            if ((!allowDuplicates) && (map.get(key).contains(value)))
            {
                return;
            }

            map.get(key).add(value);
        }
        else
        {
            List<Value> list = new ArrayList<>();
            list.add(value);
            map.put(key, list);
        }
    }

    public List<Value> get(Key key)
    {
        if (map.containsKey(key))
        {
            return map.get(key);
        }

        return new ArrayList<>();
    }

    public List<Key> getKeysContainingValue(Value value)
    {
        List<Key> keys = new ArrayList<>();

        for (Map.Entry<Key, List<Value>> mapEntry : map.entrySet())
        {
            if (mapEntry.getValue().contains(value))
            {
                keys.add(mapEntry.getKey());
            }
        }

        return keys;
    }
}
