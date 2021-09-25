package scot.massie.lib.collections.maps;

import scot.massie.lib.closuredropins.SupplierFunction;
import scot.massie.lib.closuredropins.ValueSupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static scot.massie.lib.utils.ControlFlowUtils.*;

/**
 * A map that provides a default value for any particular key when queried where no value has been explicitly set,
 * rather than null.
 * @param <K> The type of the keys of the map.
 * @param <V> The type of the values of the map.
 */
public class DefaultingMap<K, V> extends WrapperMap<K, V>
{
    /**
     * The function providing default values for the map.
     */
    protected final Function<? super K, ? extends V> defaultValueFunction;

    /**
     * Creates a new DefaultingMap.
     * @param wrappedMap The map this map is backed by.
     * @param defaultValueFunction The function that derives a default value from a key.
     */
    public DefaultingMap(Map<K, V> wrappedMap, Function<? super K, ? extends V> defaultValueFunction)
    {
        super(wrappedMap);
        this.defaultValueFunction = defaultValueFunction;
    }

    /**
     * Creates a new DefaultingMap.
     * @param wrappedMap The map this map is backed by.
     * @param defaultValueSupplier The supplier that provides default values.
     */
    public DefaultingMap(Map<K, V> wrappedMap, Supplier<? extends V> defaultValueSupplier)
    { this(wrappedMap, new SupplierFunction<>(defaultValueSupplier)); }

    /**
     * Creates a new DefaultingMap.
     * @param wrappedMap The map this map is backed by.
     * @param defaultValue The default value in this map.
     */
    public DefaultingMap(Map<K, V> wrappedMap, V defaultValue)
    { this(wrappedMap, new ValueSupplier<>(defaultValue).asFunction()); }

    /**
     * Creates a new DefaultingMap, where values default to null.
     * @param wrappedMap The map this map is backed by.
     */
    public DefaultingMap(Map<K, V> wrappedMap)
    { this(wrappedMap, new ValueSupplier<V>(null).asFunction()); }

    /**
     * Creates a new DefaultingMap backed by a {@link HashMap}.
     * @param defaultValueFunction The function that derives a default value from a key.
     */
    public DefaultingMap(Function<? super K, ? extends V> defaultValueFunction)
    { this(new HashMap<>(), defaultValueFunction); }

    /**
     * Creates a new DefaultingMap backed by a {@link HashMap}.
     * @param defaultValueSupplier The supplier that provides default values.
     */
    public DefaultingMap(Supplier<? extends V> defaultValueSupplier)
    { this(new HashMap<>(), defaultValueSupplier); }

    /**
     * Creates a new DefaultingMap backed by a {@link HashMap}.
     * @param defaultValue The default value in this map.
     */
    public DefaultingMap(V defaultValue)
    { this(new HashMap<>(), new ValueSupplier<>(defaultValue).asFunction()); }

    /**
     * Creates a new DefaultingMap backed by a {@link HashMap}, where values default to null.
     */
    public DefaultingMap()
    { this(new HashMap<>(), new ValueSupplier<V>(null).asFunction()); }

    /**
     * Gets the value mapped to the given key in this map, or a default value otherwise.
     * @param key The key to get the mapped value of.
     * @return The value mapped to the given key in this map, or if there is none, a default value.
     * @see Map#get(Object)
     */
    @Override
    public V get(Object key)
    {
        //noinspection unchecked .get should be passed an object of type K.
        return getFromIf(internal, x -> x.containsKey((K)key), x  -> x.get(key),
                                                               () -> defaultValueFunction.apply((K)key));
    }

    /**
     * Sets the value associated to the given key in this map.
     * @param key The key to associate value with.
     * @param value The value to associate with the given key in this map.
     * @return The previous value associated with the key, or a default value if there was none.
     * @see Map#put(Object, Object)
     */
    @Override
    public V put(K key, V value)
    {
        if(internal.containsKey(key))
            return internal.put(key, value);
        else
        {
            internal.put(key, value);
            return defaultValueFunction.apply(key);
        }
    }

    /**
     * Removes an explicitly set key-value association from this map. After calling this, getting the value for the
     * given key will produce a default value.
     * @param key The key to remove any explicitly set associations of.
     * @return The previous value associated with the given key, or a default value is there was none explicitly set.
     * @see Map#remove(Object)
     */
    @Override
    public V remove(Object key)
    {
        @SuppressWarnings("unchecked") // .remove should be passed an object of type K.
        K kKey = (K)key;

        return internal.containsKey(kKey) ? internal.remove(key) : defaultValueFunction.apply(kKey);
    }

    /**
     * Associates a given value with a given key only if there is no current value associated with the given key.
     * @param key The key to associate a value with.
     * @param value The value to associate with a key.
     * @return The value already associated with the given key, or a default value if there was none.
     * @see Map#putIfAbsent(Object, Object)
     */
    @Override
    public V putIfAbsent(K key, V value)
    {
        if(internal.containsKey(key))
            return internal.get(key);
        else
        {
            internal.put(key, value);
            return defaultValueFunction.apply(key);
        }
    }

    /**
     * Associates a given value with a given key only if there is a currently value associated with that key.
     * @param key The key to associate a value with.
     * @param value The value to associate with a key.
     * @return The value previously associated with the given key, or a default value if there was none.
     * @see Map#replace(Object, Object)
     */
    @Override
    public V replace(K key, V value)
    { return internal.containsKey(key) ? internal.replace(key, value) : defaultValueFunction.apply(key); }

    /**
     * Computes a new value to be associated with a given key, using that key, only if there is no current value
     * associated with that key.
     * @param key The key to have a new value associated with it.
     * @param mappingFunction The function to produce a new value to be associated with the given key. If this function
     *                        returns null, no new value is associated with that key and attempting to access it only
     *                        results in default values.
     * @return The new value associated the given key after this function call, or a default value if the given function
     *         did not associate a new value with the given key.
     * @see Map#computeIfAbsent(Object, Function)
     */
    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        if(internal.containsKey(key))
            return internal.get(key);
        else
        {
            V newValue = mappingFunction.apply(key);

            if(newValue == null)
                return defaultValueFunction.apply(key);

            internal.put(key, newValue);
            return newValue;
        }
    }

    /**
     * Computes a new value to be associated with a given key, using that key and the previous value associated with
     * that key, only if there is a current value associated with that key.
     * @param key The key to have a new value associated with it.
     * @param remappingFunction The function to produce a new value to be associated with the given key. If this
     *                          function returns null, there will no longer be any value explicitly associated with the
     *                          given key, and attempting to access it will only result in default values.
     * @return The new value associated with the given key after this function call, or a default value is there is no
     *         longer a value explicitly associated with the given value after the function call.
     * @see Map#computeIfPresent(Object, BiFunction)
     */
    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        if(internal.containsKey(key))
        {
            V newValue = remappingFunction.apply(key, internal.get(key));

            if(newValue == null)
            {
                internal.remove(key);
                return defaultValueFunction.apply(key);
            }

            internal.put(key, newValue);
            return newValue;
        }
        else
            return defaultValueFunction.apply(key);
    }

    /**
     * Computes a new value to be associated with a given key, using that key and the previous value associated with
     * that key if there was one, or a default value if there was not.
     * @param key The key to have a new value associated with it.
     * @param remappingFunction The function to produce a new value to be associated with the given key. This accepts
     *                          the key and the previous value associated with the key. If there was no previous value
     *                          explicitly associated with the key, it instead accepts a default value. If this function
     *                          returns null, this function call will result in no value being explicitly associated
     *                          with the given key, and attempting to access it will only result in default values.
     * @return The new value associated with the given key after this function call, or a default value if there is no
     *         explicitly associated value as a result of this function call.
     * @see Map#compute(Object, BiFunction)
     */
    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        boolean containedKey = internal.containsKey(key);
        V existingValue = containedKey ? internal.get(key) : defaultValueFunction.apply(key);
        V newValue = remappingFunction.apply(key, existingValue);

        if(newValue == null)
            return containedKey ? defaultValueFunction.apply(key) : existingValue;

        internal.put(key, newValue);
        return newValue;
    }

    /**
     * Computes a new value to be associated with a given key, using that key, the previous value associated with that
     * key, (or a default value if no value is explicitly associated with that key) and a given second value.
     * @param key The key to have a new value associated with it.
     * @param value The additional value to feed into the remapping function.
     * @param remappingFunction The function or produce a new value to be associated with the given key. This accepts
     *                          the key, the previous value associated with the key, and a given value. It there was no
     *                          previous value explicitly associated with the key, it instead accepts a default value.
     *                          If this function returns null, this function call will result in no value being
     *                          explicitly associated with the given key, and attempting to access it will only result
     *                          in default values.
     * @return The new value associated with the given key after this function call, or a default value if there is no
     *         explicitly associated value as a result of this function call.
     * @see Map#merge(Object, Object, BiFunction)
     */
    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        boolean containedKey = internal.containsKey(key);
        V existingValue = containedKey ? internal.get(key) : defaultValueFunction.apply(key);
        V newValue = remappingFunction.apply(existingValue, value);

        if(newValue == null)
            return containedKey ? defaultValueFunction.apply(key) : existingValue;

        internal.put(key, newValue);
        return newValue;
    }
}
