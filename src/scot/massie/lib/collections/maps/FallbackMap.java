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
 *
 * <p>Note that this may have functionality normally unexpected of a map when removing or computing values. If normal
 * map functionality is desired, this map can be flattened into a normal HashMap with {@link #flatten()}.</p>
 * @param <K> The type of the keys of this map, and of the maps referenced.
 * @param <V> The type of the values of this map, and of the maps referenced.
 * @see java.util.Map
 */
public class FallbackMap<K, V> implements Map<K, V>
{
    /**
     * A set view of the keys of this map.
     */
    class KeySet implements Set<K>
    {
        @Override
        public int size()
        { return FallbackMap.this.size(); }

        @Override
        public boolean isEmpty()
        { return FallbackMap.this.isEmpty(); }

        @Override
        public boolean contains(Object o)
        { return FallbackMap.this.containsKey(o); }

        @Override
        public Iterator<K> iterator()
        { return toFixedSet().iterator(); }

        @Override
        public Object[] toArray()
        { return toFixedSet().toArray(); }

        @Override
        public <T> T[] toArray(T[] a)
        {
            //noinspection SuspiciousToArrayCall
            return toFixedSet().toArray(a);
        }

        /**
         * Creates a new set populated with the contents of this keyset. (rather than being a view of the map.)
         * @return A new set, containing the contents of this keyset.
         */
        public Set<K> toFixedSet()
        {
            Set<K> result = new HashSet<>();

            for(Map<K, V> map : chain)
                for(Map.Entry<K, V> e : map.entrySet())
                    result.add(e.getKey());

            return result;
        }

        @Override
        public boolean add(K k)
        { throw new UnsupportedOperationException("KeySets do not implement .add(...)."); }

        @Override
        public boolean remove(Object o)
        {
            boolean result = FallbackMap.this.containsKey(o);
            FallbackMap.this.remove(o);
            return result;
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            for(Object o : c)
                if(!FallbackMap.this.containsKey(o))
                    return false;

            return true;
        }

        @Override
        public boolean addAll(Collection<? extends K> c)
        { throw new UnsupportedOperationException("KeySets do not implement .addAll(...)"); }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            boolean result = false;

            for(Map<K, V> map : chain)
                result = map.keySet().retainAll(c) || result;

            return result;
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            boolean result = false;

            for(Object o : c)
                result = remove(o) || result;

            return result;
        }

        @Override
        public void clear()
        { FallbackMap.this.clear(); }
    }

    /**
     * A collection view of the values of this map.
     */
    class ValueCollection implements Collection<V>
    {
        @Override
        public int size()
        { return FallbackMap.this.size(); }

        @Override
        public boolean isEmpty()
        { return FallbackMap.this.isEmpty(); }

        @Override
        public boolean contains(Object o)
        { return FallbackMap.this.containsValue(o); }

        @Override
        public Iterator<V> iterator()
        { return toFixedCollection().iterator(); }

        @Override
        public Object[] toArray()
        { return toFixedCollection().toArray(); }

        @Override
        public <T> T[] toArray(T[] a)
        {
            //noinspection SuspiciousToArrayCall
            return toFixedCollection().toArray(a);
        }

        /**
         * Creates a new collection populated with the contents of this value collection. (rather than being a view.)
         * @return A new collection, containing the contents of this ValueCollection.
         */
        public Collection<V> toFixedCollection()
        {
            Collection<K> keysChecked = new HashSet<>();
            Collection<V> result = new ArrayList<>();

            for(Map<K, V> map : chain)
                for(Map.Entry<K, V> e : map.entrySet())
                    if(keysChecked.add(e.getKey()))
                        result.add(e.getValue());

            return result;
        }

        @Override
        public boolean add(V v)
        { throw new UnsupportedOperationException("Value collections do not implement .add(...)."); }

        @Override
        public boolean remove(Object o)
        {
            boolean foundKeyToRemove = false;
            K keyToRemove = null;

            for(Map.Entry<K, V> e : FallbackMap.this.entrySet)
                if((o == null) ? (e.getValue() == null) : (o.equals(e.getValue())))
                {
                    keyToRemove = e.getKey();
                    foundKeyToRemove = true;
                    break;
                }

            if(!foundKeyToRemove)
                return false;

            @SuppressWarnings("unchecked") // .remove should be passed an object of type V
            V vO = (V)o;

            for(Map<K, V> map : chain)
                if(map.remove(keyToRemove, vO))
                    return true;

            throw new RuntimeException("This exception should not be reachable.");
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            for(Object o : c)
                if(!FallbackMap.this.containsValue(o))
                    return false;

            return true;
        }

        @Override
        public boolean addAll(Collection<? extends V> c)
        { throw new UnsupportedOperationException("Value collections do not implement .addAll(...)."); }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            boolean result = false;

            for(Map<K, V> map : chain)
                result = map.values().removeAll(c) || result;

            return result;
        }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            boolean result = false;

            for(Map<K, V> map : chain)
                result = map.values().retainAll(c) || result;

            return result;
        }

        @Override
        public void clear()
        { FallbackMap.this.clear(); }
    }

    /**
     * A set view of the entries of this map.
     */
    class EntrySet implements Set<Map.Entry<K, V>>
    {
        @Override
        public int size()
        { return FallbackMap.this.size(); }

        @Override
        public boolean isEmpty()
        { return FallbackMap.this.isEmpty(); }

        @Override
        public boolean contains(Object o)
        {
            if(!(o instanceof Map.Entry))
                return false;

            @SuppressWarnings("unchecked") // .contains should be passed an object of type Map.Entry<K, V>
            Map.Entry<K, V> e = (Map.Entry<K, V>)o;
            Wrapper<V> vInWrapper = FallbackMap.this.getInWrapper(e.getKey());

            if(vInWrapper == null)
                return false;

            if(e.getValue() == null)
                return vInWrapper.get() == null;

            return e.getValue().equals(vInWrapper.get());
        }

        @Override
        public Iterator<Entry<K, V>> iterator()
        { return toFixedSet().iterator(); }

        @Override
        public Object[] toArray()
        { return toFixedSet().toArray(); }

        @Override
        public <T> T[] toArray(T[] a)
        {
            //noinspection SuspiciousToArrayCall
            return toFixedSet().toArray(a);
        }

        /**
         * Creates a new set populated with the contents of this EntrySet. (rather than being a view of the map)
         * @return A new set containing the contents of this EntrySet.
         */
        public Set<Map.Entry<K, V>> toFixedSet()
        { return FallbackMap.this.flatten().entrySet(); }

        @Override
        public boolean add(Entry<K, V> kvEntry)
        { throw new UnsupportedOperationException("EntrySets do not implement .add(...)."); }

        @Override
        public boolean remove(Object o)
        {
            boolean result = false;

            for(Map<K, V> map : chain)
                result = map.entrySet().remove(o) || result;

            return result;
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            @SuppressWarnings({"unchecked", "TypeMayBeWeakened"})
            // .containsAll should be passed a Collection<Map.Entry<K, V>>
            // KvC is just c with generic args, doesn't make sense to weaken without also weakening the parameter type.
            Collection<Map.Entry<K, V>> kvC = (Collection<Map.Entry<K, V>>)c;

            for(Map.Entry<K, V> o : kvC)
            {
                Wrapper<V> vInWrapper = FallbackMap.this.getInWrapper(o.getKey());

                if((o.getValue() == null) && (vInWrapper.get() == null))
                    continue;

                if(o.getValue() == null)
                {
                    if(vInWrapper.get() == null)
                        continue;
                    else
                        return false;
                }

                if(!o.getValue().equals(vInWrapper.get()))
                    return false;
            }

            return true;
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> c)
        { throw new UnsupportedOperationException("EntrySets do not implement .addAll(...)"); }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            boolean result = false;

            for(Map<K, V> map : chain)
                result = map.entrySet().retainAll(c) || result;

            return result;
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            boolean result = false;

            for(Object o : c)
                result = remove(o) || result;

            return result;
        }

        @Override
        public void clear()
        { FallbackMap.this.clear(); }
    }

    /**
     * The list of maps that calls to this go through.
     */
    protected List<Map<K, V>> chain;

    /**
     * A {@link Set} view of the entries of this map.
     */
    protected final EntrySet entrySet = new EntrySet();

    /**
     * A {@link Set} view of the keys of this map.
     */
    protected final KeySet keySet = new KeySet();

    /**
     * A {@link Collection} view of the values of this map.
     */
    protected final ValueCollection valueCollection = new ValueCollection();

    /**
     * Creates a new FallbackMap, which references the given maps in order until it finds a value.
     * @param maps The maps this should be backed by.
     */
    @SafeVarargs
    public FallbackMap(Map<K, V>... maps)
    { chain = new ArrayList<>(Arrays.asList(maps)); }

    /**
     * Converts this FallbackMap into a flat {@link HashMap}, by copying all top values for each key. Not that the
     * resulting map is not reflective of changes made to the referenced maps, and changes to it are not made to the
     * referenced maps.
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

    /**
     * <p>Gets the number of accessible key-value pairs on this map.</p>
     *
     * <p>Note that in order to determine this, the map is flattened first - this may be an expensive operation.</p>
     * @return The number of accessible key-value pairs on this map.
     */
    @Override
    public int size()
    { return flatten().size(); }

    /**
     * Gets whether or not all maps this map references are empty.
     * @return True if all reference maps are empty. Otherwise, false.
     */
    @Override
    public boolean isEmpty()
    {
        for(Map<K, V> map : chain)
            if(!map.isEmpty())
                return false;

        return true;
    }

    /**
     * Gets whether or not any map referenced contained a mapping for the given key.
     * @param key The key whose presence in this map is to be tested.
     * @return True if any referenced map contains a mapping for the given key. Otherwise, false.
     */
    @Override
    public boolean containsKey(Object key)
    {
        for(Map<K, V> map : chain)
            if(map.containsKey(key))
                return true;

        return false;
    }

    /**
     * Gets if the given value is accessible in a key-value pairing accessible from this map.
     * @param value The value whose presence in this map is to be tested.
     * @return True if any referenced map contains a mapping for the given value *and* that mapping is the first one in
     *         the chain for the given key. Otherwise, false.
     */
    @Override
    public boolean containsValue(Object value)
    {
        Collection<K> keysChecked = new HashSet<>();

        for(Map<K, V> map : chain)
        {
            for(Map.Entry<K, V> e : map.entrySet())
            {
                if(keysChecked.contains(e.getKey()))
                    continue;

                if((value == null) ? (e.getValue() == null) : (value.equals(e.getValue())))
                    return true;

                keysChecked.add(e.getKey());
            }
        }

        return false;
    }

    /**
     * <p>Gets the first value mapped against the given key in a key-value pair in any of the referenced maps, or null
     * if no mapping is found.</p>
     *
     * <p>Note that this method returning null may be because no such mapping was found, *or* because the first mapping
     * found assigned null as the value to the matching key-value pairing.</p>
     * @param key The key whose associated value is to be tested.
     * @return The first value mapped against the given in a key-value pair in any of the referenced maps, or null if no
     *         such mapping is found.
     */
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

    /**
     * Associates the given value with the key key in the first map in the chain. If the first map in the chain
     * previously contained a mapping for the key, the old value is replaced by the specified value.
     * @param key The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @return The previous first mapping for the given key in any referenced map in the chain, or null if there was
     *         no mapping for the given key across any referenced map in the chain.
     */
    @Override
    public V put(K key, V value)
    {
        V oldValue = get(key);
        chain.get(0).put(key, value);
        return oldValue;
    }

    /**
     * <p>Removes all mappings for the given key across all referenced maps.</p>
     * @param key The key to remove any matching key-value pairs of.
     * @return The value of the first matching key-value pair found, or null if no such mapping was found.
     */
    @Override
    public V remove(Object key)
    {
        @SuppressWarnings("unchecked") // .remove should be passed an instance of K
        K kKey = (K)key;

        boolean valueFound = false;
        V value = null;

        for(Map<K, V> map : chain)
        {
            if(!valueFound && map.containsKey(kKey))
            {
                valueFound = true;
                value = map.remove(key);
            }
            else
                map.remove(key);
        }

        return value;
    }

    /**
     * Adds all key-value pairings from the given map to the first map in the chain. This may overwrite some existing
     * pairings.
     * @param m The map from which to draw pairings.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    { chain.get(0).putAll(m); }

    /**
     * Removes all key-values pairs from all referenced maps in the chain.
     */
    @Override
    public void clear()
    {
        for(Map<K, V> map : chain)
            map.clear();
    }

    /**
     * Gets a live view of this map's keys as a set.
     * @return A live view of this map's keys as a set.
     */
    @Override
    public KeySet keySet()
    { return keySet; }

    /**
     * <p>Gets a live view of this map's values as a collection.</p>
     *
     * <p>Keep in mind that, as with this map, some behaviour may be normally unexpected of map value collection views
     * when removing values.</p>
     * @return A live view of this map's values as a collection.
     */
    @Override
    public ValueCollection values()
    { return valueCollection; }

    /**
     * <p>Gets a live view of this map's entries as a set.</p>
     *
     * <p>Keep in mind that, as with this map, some behaviour may be normally unexpected of map entry sets when removing
     * their values.</p>
     * @return A live view of this map's entries as a set.
     */
    @Override
    public EntrySet entrySet()
    { return entrySet; }

    /**
     * Gets the first value mapping in the chain for the given key, or the given default value is no mapping is found.
     * @param key The key whose associated value is to be returned.
     * @param defaultValue The value to return if there is no mapping for the given key.
     * @return The first value mapping in the chain for the given key, or the given default value if no such mapping
     *         exists.
     */
    @Override
    public V getOrDefault(Object key, V defaultValue)
    {
        @SuppressWarnings("unchecked") // .getOrDefault should be passed an object of type K
        K kKey = (K)key;

        for(Map<K, V> map : chain)
            if(map.containsKey(kKey))
                return map.get(kKey);

        return defaultValue;
    }

    /**
     * Performs the given action for each first entry for a given key in this map until all entries have been processed
     * or the action throws an exception.
     * @param action The action to be performed for each entry.
     */
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action)
    {
        Collection<K> keysDone = new HashSet<>();

        for(Map<K, V> map : chain)
            for(Map.Entry<K, V> e : map.entrySet())
                if(!keysDone.contains(e.getKey()))
                {
                    keysDone.add(e.getKey());
                    action.accept(e.getKey(), e.getValue());
                }
    }

    /**
     * Replaces each entry's value across all maps (including for entries not accessible) with the result of invoking
     * the given function on that entry until all entries have been processed or the function throws an exception.
     * @param function The function to apply to each entry.
     */
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
    {
        for(Map<K, V> map : chain)
            map.replaceAll(function);
    }

    /**
     * Associates the given value with the given key in the first map in the chain if there is currently no mapping for
     * the given key.
     * @param key The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @return The previous value associated with the specified key, or null if there was no mapping for the key. Note
     *         that this can return null if the previous value associated with the specified key was null.
     */
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

    /**
     * Replaces the values of all instances of the given key/value pair across all references maps in the chain with the
     * given value.
     * @param key The key with which the specified value is associated.
     * @param oldValue The value expected to be associated with the specified key.
     * @param newValue The value to be associated with the specified key.
     * @return True if the value was replaced in any of the referenced maps.
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue)
    {
        boolean result = false;

        for(Map<K, V> map : chain)
            result = result || map.replace(key, oldValue, newValue);

        return result;
    }

    /**
     * Replaces all key-value pairs for the given key in the referenced maps (including ones hidden by earlier key-value
     * pairs) with the given key-value pair.
     * @param key The key with which the specified value is associated.
     * @param value The value to be associated with the specified key.
     * @return The previous value associated with the specified key, or null if there was no mapping for the key. Note
     *         that this may return null if the first value associated with the given key in the chain was null.
     */
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
