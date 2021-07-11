package scot.massie.lib.collections.maps;

/**
 * A {@link NumberMap} with double-typed values.
 * @param <K> The type of the keys of this map.
 */
public class DoubleMap<K> extends NumberMap<K, Double>
{
    /**
     * Creates a new DoubleMap.
     */
    public DoubleMap()
    { super(0d); }

    @Override
    public Double add(K key, Double numberToAdd)
    {
        double result = get(key) + numberToAdd;
        put(key, result);
        return result;
    }

    @Override
    public Double addReturningOldValue(K key, Double numberToAdd)
    { return put(key, get(key) + numberToAdd); }

    @Override
    public Double subtract(K key, Double numberToSubtract)
    {
        double result = get(key) - numberToSubtract;
        put(key, result);
        return result;
    }

    @Override
    public Double subtractReturningOldValue(K key, Double numberToSubtract)
    { return put(key, get(key) - numberToSubtract); }

    @Override
    public Double multiply(K key, Double numberToMultiplyBy)
    {
        double result = get(key) * numberToMultiplyBy;
        put(key, result);
        return result;
    }

    @Override
    public Double multiplyReturningOldValue(K key, Double numberToMultiplyBy)
    { return put(key, get(key) * numberToMultiplyBy); }

    @Override
    public Double divideBy(K key, Double numberToDivideBy)
    {
        double result = get(key) / numberToDivideBy;
        put(key, result);
        return result;
    }

    @Override
    public Double divideByReturningOldValue(K key, Double numberToDivideBy)
    { return put(key, get(key) / numberToDivideBy); }
}
