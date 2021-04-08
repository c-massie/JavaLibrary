package scot.massie.lib.collections.maps;

import java.util.HashMap;
import java.util.Map;

public final class MapUtils
{
    private MapUtils()
    {}

    /**
     * Produces a map where the item assigned to each key is comparatively the lowest assigned to that key across all
     * maps that were passed.
     * @param maps The maps to key the lowest values of.
     * @param <K> The key type of the maps.
     * @param <V> The value type of the maps.
     * @return A new map where all values are the comparative minimum across all passed maps for that key.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> minValues(Map<K, V>... maps)
    {
        Map<K, V> result = new HashMap<>();

        for(Map<K, V> m : maps)
            for(Map.Entry<K, V> e : m.entrySet())
                result.compute(e.getKey(), (k, v) -> (v == null || v.compareTo(e.getValue()) > 0) ? e.getValue() : v);

        return result;
    }

    /**
     * Produces a map where the item assigned to each key is comparatively the highest assigned to that key across all
     * maps that were passed.
     * @param maps The maps to key the highest values of.
     * @param <K> The key type of the maps.
     * @param <V> The value type of the maps.
     * @return A new map where all values are the comparative maximum across all passed maps for that key.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> maxValues(Map<K, V>... maps)
    {
        Map<K, V> result = new HashMap<>();

        for(Map<K, V> m : maps)
            for(Map.Entry<K, V> e : m.entrySet())
                result.compute(e.getKey(), (k, v) -> (v == null || v.compareTo(e.getValue()) < 0) ? e.getValue() : v);

        return result;
    }
}
