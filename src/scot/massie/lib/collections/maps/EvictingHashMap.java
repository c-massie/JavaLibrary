package scot.massie.lib.collections.maps;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Variant of {@link LinkedHashMap} that removes the oldest entries to make way for new entries, if a maximum size is
 * specified.
 * @param <K> The type of the key objects of the map.
 * @param <V> The type of the value objects of the map.
 */
public class EvictingHashMap<K, V> extends LinkedHashMap<K, V>
{
    /**
     * The maximum number of entries in the map.
     */
    private final int maxSize;

    /**
     * Creates a new evicting hashmap.
     */
    public EvictingHashMap()
    { maxSize = Integer.MAX_VALUE - 1; }

    /**
     * Creates a new evicting hashmap with a specified maximum size. If an operation would result in this map having
     * more entries than this specified size, the oldest entries will be removed to make way.
     * @param maxSize The maximum number of entries allowed in this map.
     */
    public EvictingHashMap(int maxSize)
    { this.maxSize = maxSize; }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
    { return size() > maxSize; }
}
