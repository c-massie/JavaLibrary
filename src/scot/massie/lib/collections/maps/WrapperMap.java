package scot.massie.lib.collections.maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A map that routes all standard {@link Map} interface calls to it to an internally stored map. This type exists
 * primarily to serve as the base class for other map types that defer to an internally stored map.
 * @param <K> The type of the keys of the map.
 * @param <V> The type of the values of the map.
 */
public class WrapperMap<K, V> implements Map<K, V>
{
    /**
     * The internally stored map to which all {@link Map} calls defer.
     */
    protected Map<K, V> internal;

    /**
     * Creates a new WrapperMap, routing calls to the given map.
     * @param wrappedMap The map to route calls to.
     */
    public WrapperMap(Map<K, V> wrappedMap)
    { this.internal = wrappedMap; }

    /**
     * Creates a new WrapperMap, routing calls to an internally stored hashmap.
     */
    public WrapperMap()
    { this(new HashMap<>()); }

    @Override
    public int size()
    { return internal.size(); }

    @Override
    public boolean isEmpty()
    { return internal.isEmpty(); }

    @Override
    public boolean containsKey(Object key)
    { return internal.containsKey(key); }

    @Override
    public boolean containsValue(Object value)
    { return internal.containsValue(value); }

    @Override
    public V get(Object key)
    { return internal.get(key); }

    @Override
    public V put(K key, V value)
    { return internal.put(key, value); }

    @Override
    public V remove(Object key)
    { return internal.remove(key); }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    { internal.putAll(m); }

    @Override
    public void clear()
    { internal.clear(); }

    @Override
    public Set<K> keySet()
    { return internal.keySet(); }

    @Override
    public Collection<V> values()
    { return internal.values(); }

    @Override
    public Set<Entry<K, V>> entrySet()
    { return internal.entrySet(); }

    @Override
    public V getOrDefault(Object key, V defaultValue)
    { return internal.getOrDefault(key, defaultValue); }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action)
    { internal.forEach(action); }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
    { internal.replaceAll(function); }

    @Override
    public V putIfAbsent(K key, V value)
    { return internal.putIfAbsent(key, value); }

    @Override
    public boolean remove(Object key, Object value)
    { return internal.remove(key, value); }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
    { return internal.replace(key, oldValue, newValue); }

    @Override
    public V replace(K key, V value)
    { return internal.replace(key, value); }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    { return internal.computeIfAbsent(key, mappingFunction); }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    { return internal.computeIfPresent(key, remappingFunction); }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    { return internal.compute(key, remappingFunction); }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    { return internal.merge(key, value, remappingFunction); }
}
