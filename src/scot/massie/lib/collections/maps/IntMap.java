package scot.massie.lib.collections.maps;

public class IntMap<K> extends NumberMap<K, Integer>
{
    public IntMap(Integer zeroValue)
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
}
