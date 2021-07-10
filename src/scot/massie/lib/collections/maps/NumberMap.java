package scot.massie.lib.collections.maps;

public abstract class NumberMap<K, V extends Number> extends DefaultingMap<K, V>
{
    public NumberMap(V zeroValue)
    { super(zeroValue); }

    public abstract V add(K key, V numberToAdd);

    public abstract V addReturningOldValue(K key, V numberToAdd);

    public abstract V subtract(K key, V numberToSubtract);

    public abstract V subtractReturningOldValue(K key, V numberToSubtract);

    public abstract V multiply(K key, V numberToMultiplyBy);

    public abstract V multiplyReturningOldValue(K key, V numberToMultiplyBy);

    public abstract V divideBy(K key, V numberToDivideBy);

    public abstract V divideByReturningOldValue(K key, V numberToDivideBy);
}
