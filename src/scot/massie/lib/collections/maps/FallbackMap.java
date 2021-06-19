package scot.massie.lib.collections.maps;

import scot.massie.lib.utils.wrappers.Wrapper;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>A chain of {@link Map map objects}, where accessing the contents routes to the first map in the chain with the
 * relevant entry.</p>
 *
 * <p>May contain/reference nulls. Null is treated as a possible value and isn't skipped over, even if a different
 * non-null value is in a later map in the chain.</p>
 *
 * <p>If using in a threadsafe manner, accessing a FallbackMap should be done within the same lock as the maps it's
 * backed by.</p>
 * @param <K> The type of the keys of this map, and of the maps referenced.
 * @param <V> The type of the values of this map, and of the maps referenced.
 * @see java.util.Map
 */
public class FallbackMap<K, V> implements Map<K, V>
{
    /**
     * The list of maps that calls to this go through.
     */
    List<Map<K, V>> chain;

    /**
     * Creates a new FallbackMap, which references the given maps in order until it finds a value.
     * @param maps The maps this should be backed by.
     */
    public FallbackMap(Map<K, V>... maps)
    { chain = new ArrayList<>(Arrays.asList(maps)); }

    /**
     * Converts this FallbackMap into a flat {@link HashMap}, by copying all top values for each key.
     * @return A new {@link HashMap} that is a shallow copy of the contents of this FallbackMap.
     */
    public HashMap<K, V> flatten()
    {
        HashMap<K, V> result = new HashMap<>();

        for(Map<K, V> map : chain)
            for(Map.Entry<K, V> e : map.entrySet())
                result.putIfAbsent(e.getKey(), e.getValue());

        return result;
    }

    @Override
    public int size()
    { return flatten().size(); }

    @Override
    public boolean isEmpty()
    {
        for(Map<K, V> map : chain)
            if(!map.isEmpty())
                return false;

        return true;
    }

    @Override
    public boolean containsKey(Object key)
    {
        for(Map<K, V> map : chain)
            if(map.containsKey(key))
                return true;

        return false;
    }

    @Override
    public boolean containsValue(Object value)
    {
        for(Map<K, V> map : chain)
            if(map.containsValue(value))
                return true;

        return false;
    }

    @Override
    public V get(Object key)
    { return getOrDefault(key, null); }

    /**
     * <p>Returns the value to which the specified key is mapped, wrapped in an instance of {@link Wrapper}, or null
     * (not in an instance of {@link Wrapper}) if this map contains no mapping for this key.</p>
     *
     * <p>More formally, if this map contains a mapping from a key k to a value v such that
     * (key==null ? k==null : key.equals(k)), then this method returns new Wrapper&lt;V&gt;(v); otherwise it returns null. (There
     * can be at most one such mapping.) </p>
     *
     * <p>This helps differentiate between a map containing null mapped to a given key and a map containing not mapping
     * for a given key, where the former will return new Wrapper&lt;V&gt;(null) and the latter will return null.</p>
     * @param key The key whose associated value is to be returned.
     * @return The value to which the specified key is mapped, wrapped in an instance of {@link Wrapper}, or null if
     *         this map contains no mapping for the key.
     */
    public Wrapper<V> getInWrapper(K key)
    {
        for(Map<K, V> map : chain)
            if(map.containsKey(key))
                return new Wrapper<>(map.get(key));

        return null;
    }

    @Override
    public V put(K key, V value)
    {
        V oldValue = get(key);
        chain.get(0).put(key, value);
        return oldValue;
    }

    @Override
    public V remove(Object key)
    {
        boolean valueFound = false;
        V value = null;

        for(Map<K, V> map : chain)
        {
            if(!valueFound && map.containsKey(key))
            {
                valueFound = true;
                value = map.remove(key);
            }
            else
                map.remove(key);
        }

        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    { chain.get(0).putAll(m); }

    @Override
    public void clear()
    {
        for(Map<K, V> map : chain)
            map.clear();
    }

    @Override
    public Set<K> keySet()
    { return flatten().keySet(); }

    @Override
    public Collection<V> values()
    { return flatten().values(); }

    @Override
    public Set<Entry<K, V>> entrySet()
    { return flatten().entrySet(); }

    @Override
    public V getOrDefault(Object key, V defaultValue)
    {
        for(Map<K, V> map : chain)
            if(map.containsKey(key))
                return map.get(key);

        return defaultValue;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action)
    {
        Set<K> keysDone = new HashSet<>();

        for(Map<K, V> map : chain)
            for(Map.Entry<K, V> e : map.entrySet())
                if(!keysDone.contains(e.getKey()))
                {
                    keysDone.add(e.getKey());
                    action.accept(e.getKey(), e.getValue());
                }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
    {
        for(Map<K, V> map : chain)
            map.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value)
    {
        Wrapper<V> containedValue = getInWrapper(key);

        if(containedValue != null)
            return containedValue.get();

        chain.get(0).put(key, value);
        return null;
    }

    /**
     * <p>Removes all matching key/value pairs. Removes the given key-value pair from each of the backing maps, only
     * where the individual backing map has that key-value pair.</p>
     * @apiNote This may result in the value mapped to the given key in this map changing rather than being removed, as
     *          it may fall through to another backing map's mapping for the given key.
     * @param key The key with which the specified value is associated.
     * @param value The value expected to be associated with the specified key.
     * @return True if the value was removed from any of the backing maps. Otherwise, false.
     */
    @Override
    public boolean remove(Object key, Object value)
    {
        // NOTE: This may case the value mapped to a key in an instance of FallbackMap to change but not be removed,
        // where this results in the removal of one key/value pair, but not all for the given key.

        boolean result = false;

        for(Map<K, V> map : chain)
            result = result || map.remove(key, value);

        return result;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
    {
        boolean result = false;

        for(Map<K, V> map : chain)
            result = result || map.replace(key, oldValue, newValue);

        return result;
    }

    @Override
    public V replace(K key, V value)
    {
        boolean oldValueFound = false;
        V oldValue = null;

        for(Map<K, V> map : chain)
        {
            if(!oldValueFound)
            {
                if(map.containsKey(key))
                {
                    oldValueFound = true;
                    oldValue = map.replace(key, value);
                }
            }
            else
                map.replace(key, value);
        }

        return oldValue;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        Wrapper<V> oldValue = getInWrapper(key);

        if(oldValue != null)
            return oldValue.get();

        V computedValue = mappingFunction.apply(key);

        if(computedValue != null)
            chain.get(0).put(key, mappingFunction.apply(key));

        return computedValue;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        boolean newValueEstablished = false;
        V newValue = null;

        for(Map<K, V> map : chain)
        {
            if(!newValueEstablished)
            {
                if(map.containsKey(key))
                {
                    V existingValue = map.get(key);

                    if(existingValue != null)
                    {
                        if((newValue = map.computeIfPresent(key, remappingFunction)) != null)
                            newValueEstablished = true;
                    }
                }
            }
            else
                map.computeIfPresent(key, remappingFunction);
        }

        return newValue;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        V newValue = null;

        for(Map<K, V> map : chain)
        {
            if(newValue == null)
                newValue = map.compute(key, remappingFunction);
            else
                map.compute(key, remappingFunction);
        }

        return newValue;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        V newValue = null;

        for(Map<K, V> map : chain)
        {
            if(newValue == null)
                newValue = map.merge(key, value, remappingFunction);
            else
                map.merge(key, value, remappingFunction);
        }

        return newValue;
    }
}
