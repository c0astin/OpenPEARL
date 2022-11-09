package static_analyzer;

import java.util.HashMap;
import java.util.Map;

public class CounterMap<T>
{
    private final Map<T, Integer> values = new HashMap<>();

    public void add(T object)
    {
        add(object, 1);
    }

    public void add(T object, int value)
    {
        Integer currentValue = values.get(object);

        if (currentValue == null)
        {
            currentValue = 0;
        }

        values.put(object, currentValue + value);
    }

    public int get(T object)
    {
        if (values.containsKey(object))
        {
            return values.get(object);
        }

        return 0;
    }

    public boolean isEmpty()
    {
        return values.isEmpty();
    }
}
