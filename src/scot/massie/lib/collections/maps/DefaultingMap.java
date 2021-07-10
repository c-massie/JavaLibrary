package scot.massie.lib.collections.maps;

import scot.massie.lib.closuredropins.ValueSupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultingMap<K, V> extends WrapperMap<K, V>
{
    Supplier<V> defaultValueSupplier;

    public DefaultingMap(Map<K, V> wrappedMap, Supplier<V> defaultValueSupplier)
    {
        super(wrappedMap);
        this.defaultValueSupplier = defaultValueSupplier;
    }

    public DefaultingMap(Map<K, V> wrappedMap, V defaultValue)
    { this(wrappedMap, new ValueSupplier<>(defaultValue)); }

    public DefaultingMap(Map<K, V> wrappedMap)
    { this(wrappedMap, new ValueSupplier<>(null)); }

    public DefaultingMap(Supplier<V> defaultValueSupplier)
    { this(new HashMap<>(), defaultValueSupplier); }

    public DefaultingMap(V defaultValue)
    { this(new HashMap<>(), new ValueSupplier<>(defaultValue)); }

    public DefaultingMap()
    { this(new HashMap<>(), new ValueSupplier<>(null)); }

    @Override
    public V get(Object key)
    {
        if(!internal.containsKey(key))
            return defaultValueSupplier.get();

        return internal.get(key);
    }

    @Override
    public V put(K key, V value)
    {
        if(internal.containsKey(key))
            return internal.put(key, value);
        else
        {
            internal.put(key, value);
            return defaultValueSupplier.get();
        }
    }

    @Override
    public V remove(Object key)
    { return internal.containsKey(key) ? internal.remove(key) : defaultValueSupplier.get(); }

    @Override
    public V putIfAbsent(K key, V value)
    { return !internal.containsKey(key) ? internal.put(key, value) : defaultValueSupplier.get(); }

    @Override
    public V replace(K key, V value)
    {
        if(internal.containsKey(key))
            return internal.replace(key, value);
        else
        {
            internal.replace(key, value);
            return defaultValueSupplier.get();
        }
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        if(internal.containsKey(key))
            return internal.get(key);
        else
        {
            V newValue = mappingFunction.apply(key);

            if(newValue == null)
                return defaultValueSupplier.get();

            internal.put(key, newValue);
            return newValue;
        }
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        if(internal.containsKey(key))
        {
            V newValue = remappingFunction.apply(key, internal.get(key));

            if(newValue == null)
            {
                internal.remove(key);
                return defaultValueSupplier.get();
            }

            internal.put(key, newValue);
            return newValue;
        }
        else
            return defaultValueSupplier.get();
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        boolean containedKey = internal.containsKey(key);
        V existingValue = containedKey ? internal.get(key) : defaultValueSupplier.get();
        V newValue = remappingFunction.apply(key, existingValue);

        if(newValue == null)
            return containedKey ? defaultValueSupplier.get() : existingValue;

        internal.put(key, newValue);
        return newValue;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        boolean containedKey = internal.containsKey(key);
        V existingValue = containedKey ? internal.get(key) : defaultValueSupplier.get();
        V newValue = remappingFunction.apply(existingValue, value);

        if(newValue == null)
            return containedKey ? defaultValueSupplier.get() : existingValue;

        internal.put(key, newValue);
        return newValue;
    }
}
