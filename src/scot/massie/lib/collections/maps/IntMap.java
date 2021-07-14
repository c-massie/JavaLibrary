package scot.massie.lib.collections.maps;

import java.util.Map;

/**
 * A {@link NumberMap} with integer-typed values.
 * @param <K> The type of the keys of this map.
 */
public class IntMap<K> extends NumberMap<K, Integer>
{
    /**
     * Creates a new IntMap.
     */
    public IntMap()
    { super(0); }

    @Override
    public Integer add(K key, Integer numberToAdd)
    {
        int result = get(key) + numberToAdd;
        put(key, result);
        return result;
    }

    @Override
    public Integer addReturningOldValue(K key, Integer numberToAdd)
    { return put(key, get(key) + numberToAdd); }

    @Override
    public Integer subtract(K key, Integer numberToSubtract)
    {
        int result = get(key) - numberToSubtract;
        put(key, result);
        return result;
    }

    @Override
    public Integer subtractReturningOldValue(K key, Integer numberToSubtract)
    { return put(key, get(key) - numberToSubtract); }

    @Override
    public Integer multiply(K key, Integer numberToMultiplyBy)
    {
        int result = get(key) * numberToMultiplyBy;
        put(key, result);
        return result;
    }

    @Override
    public Integer multiplyReturningOldValue(K key, Integer numberToMultiplyBy)
    { return put(key, get(key) * numberToMultiplyBy); }

    @Override
    public Integer divideBy(K key, Integer numberToDivideBy)
    {
        int result = get(key) / numberToDivideBy;
        put(key, result);
        return result;
    }

    @Override
    public Integer divideByReturningOldValue(K key, Integer numberToDivideBy)
    { return put(key, get(key) / numberToDivideBy); }

    @Override
    public void addMap(Map<? extends K, ? extends Integer> other)
    {
        for(Map.Entry<? extends K, ? extends Integer> e : other.entrySet())
            put(e.getKey(), get(e.getKey()) + e.getValue());
    }

    @Override
    public void subtractMap(Map<? extends K, ? extends Integer> other)
    {
        for(Map.Entry<? extends K, ? extends Integer> e : other.entrySet())
            put(e.getKey(), get(e.getKey()) - e.getValue());
    }

    @Override
    public void multiplyMap(Map<? extends K, ? extends Integer> other)
    {
        for(Map.Entry<? extends K, ? extends Integer> e : other.entrySet())
            put(e.getKey(), get(e.getKey()) * e.getValue());
    }

    @Override
    public void divideByMap(Map<? extends K, ? extends Integer> other)
    {
        for(Map.Entry<? extends K, ? extends Integer> e : other.entrySet())
            put(e.getKey(), get(e.getKey()) / e.getValue());
    }
}
