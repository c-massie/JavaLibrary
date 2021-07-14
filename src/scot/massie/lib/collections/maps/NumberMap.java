package scot.massie.lib.collections.maps;

import java.util.Map;

/**
 * A numerically-valued map with additional arithmetic methods, where all values default to 0.
 * @param <K> The type of the keys of this map.
 * @param <V> The numeric type of the values of this map.
 */
public abstract class NumberMap<K, V extends Number> extends DefaultingMap<K, V>
{
    /**
     * Creates a new NumberMap.
     * @param zeroValue The value that represents 0 in a particular value type.
     */
    public NumberMap(V zeroValue)
    { super(zeroValue); }

    /**
     * Adds a given number to the number already associated with the given key.
     * @param key The key to modify the associated value of.
     * @param numberToAdd The number to add to the value already associated with the given key.
     * @return The new value associated with the given key.
     */
    public abstract V add(K key, V numberToAdd);

    /**
     * Adds a given number to the number already associated with the given key.
     * @param key The key to modify the associated value of.
     * @param numberToAdd The number to add to the value already associated with the given key.
     * @return The previous value associated with the given key.
     */
    public abstract V addReturningOldValue(K key, V numberToAdd);

    /**
     * Subtracts a given number from the number already associated with the given key.
     * @param key The key to modify the associated value of.
     * @param numberToSubtract The number to subtract from the value already associated with the given key.
     * @return The new value associated with the given key.
     */
    public abstract V subtract(K key, V numberToSubtract);

    /**
     * Subtracts a given number from the number already associated with the given key.
     * @param key The key to modify the associated value of.
     * @param numberToSubtract The number to subtract from the value already associated with the given key.
     * @return The previous value associated with the given key.
     */
    public abstract V subtractReturningOldValue(K key, V numberToSubtract);

    /**
     * Multiplies the number already associated with a given key by a given number.
     * @param key The key to modify the associated value of.
     * @param numberToMultiplyBy The number to multiply the value already associated with the given key by.
     * @return The new value associated with the given key.
     */
    public abstract V multiply(K key, V numberToMultiplyBy);

    /**
     * Multiplies the number already associated with a given key by a given number.
     * @param key The key to modify the associated value of.
     * @param numberToMultiplyBy The number to multiply the value already associated with the given key by.
     * @return The previous value associated with the given key.
     */
    public abstract V multiplyReturningOldValue(K key, V numberToMultiplyBy);

    /**
     * Divides the number already associated with a given key by a given number.
     * @param key The key to modify the associated value of.
     * @param numberToDivideBy The number to divide the value already associated with the given key by.
     * @return The new value associated with the given key.
     */
    public abstract V divideBy(K key, V numberToDivideBy);

    /**
     * Divides the number already associated with a given key by a given number.
     * @param key The key to modify the associated value of.
     * @param numberToDivideBy The number to divide the value already associated with the given key by.
     * @return The previous value associated with the given key.
     */
    public abstract V divideByReturningOldValue(K key, V numberToDivideBy);


    /**
     * Adds all values in the given map to the values at the matching keys in this map.
     * @param other The map with values to add to the values of this map.
     */
    public abstract void addMap(Map<? extends K, ? extends V> other);

    /**
     * Subtracts all values in the given map from the values at the matching keys in this map.
     * @param other The map with values to subtract from the values of this map.
     */
    public abstract void subtractMap(Map<? extends K, ? extends V> other);

    /**
     * Multiplies all values in the given map by the values at the matching keys in this map.
     * @param other The map with values to multiply the values of this map by.
     */
    public abstract void multiplyMap(Map<? extends K, ? extends V> other);

    /**
     * Divides values in this map where an existing explicitly set value exists in the given map at the same key, by the
     * values at the same key in the given map.
     * @param other The map with values to divide the values of this map by.
     */
    public abstract void divideByMap(Map<? extends K, ? extends V> other);
}
