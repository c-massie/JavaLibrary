package scot.massie.lib.collections.maps;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

public final class MapUtils
{
    private MapUtils()
    {}

    /**
     * Produces a map where the item assigned to each key is the result of the provided operation being performed
     * iteratively on the value assigned to that key in each of the passed maps.
     * @param op The operation to perform on values of matching keys.
     * @param maps The maps from which to source keys and values.
     * @param <K> The type of the keys of the maps.
     * @param <V> The value of the keys of the maps.
     * @return A new map object where the item assigned to each key is the result of performing the provided operation
     */
    public static <K, V> Map<K, V> accumulateValues(BinaryOperator<V> op, Map<K, V>... maps)
    {
        Map<K, V> result = new HashMap<>();

        for(Map<K, V> m : maps)
            for(Map.Entry<K, V> e : m.entrySet())
                result.compute(e.getKey(), (k, v) -> v == null ? e.getValue() : op.apply(v, e.getValue()));

        return result;
    }

    /**
     * Produces a map where the item assigned to each key is the result of the provided operation being performed
     * iteratively on the value assigned to that key in each of the passed maps.
     * @param op The operation to perform on values of matching keys.
     * @param maps The maps from which to source keys and values.
     * @param <K> The type of the keys of the maps.
     * @param <V> The value of the keys of the maps.
     * @return A new map object where the item assigned to each key is the result of performing the provided operation
     */
    public static <K, V> Map<K, V> accumulateValues(BinaryOperator<V> op, Iterable<Map<K, V>> maps)
    {
        Map<K, V> result = new HashMap<>();

        for(Map<K, V> m : maps)
            for(Map.Entry<K, V> e : m.entrySet())
                result.compute(e.getKey(), (k, v) -> v == null ? e.getValue() : op.apply(v, e.getValue()));

        return result;
    }

    /**
     * Produces a map where the item assigned to each key is comparatively the lowest assigned to that key across all
     * maps that were passed.
     * @param maps The maps to key the lowest values of.
     * @param <K> The key type of the maps.
     * @param <V> The value type of the maps.
     * @return A new map where all values are the comparative minimum across all passed maps for that key.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> minValues(Map<K, V>... maps)
    { return accumulateValues((a, b) -> a.compareTo(b) <= 0 ? a : b, maps); }

    /**
     * Produces a map where the item assigned to each key is comparatively the lowest assigned to that key across all
     * maps that were passed.
     * @param maps The maps to key the lowest values of.
     * @param <K> The key type of the maps.
     * @param <V> The value type of the maps.
     * @return A new map where all values are the comparative minimum across all passed maps for that key.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> minValues(Iterable<Map<K, V>> maps)
    { return accumulateValues((a, b) -> a.compareTo(b) <= 0 ? a : b, maps); }

    /**
     * Produces a map where the item assigned to each key is comparatively the highest assigned to that key across all
     * maps that were passed.
     * @param maps The maps to key the highest values of.
     * @param <K> The key type of the maps.
     * @param <V> The value type of the maps.
     * @return A new map where all values are the comparative maximum across all passed maps for that key.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> maxValues(Map<K, V>... maps)
    { return accumulateValues((a, b) -> a.compareTo(b) <= 0 ? a : b, maps); }

    /**
     * Produces a map where the item assigned to each key is comparatively the highest assigned to that key across all
     * maps that were passed.
     * @param maps The maps to key the highest values of.
     * @param <K> The key type of the maps.
     * @param <V> The value type of the maps.
     * @return A new map where all values are the comparative maximum across all passed maps for that key.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> maxValues(Iterable<Map<K, V>> maps)
    { return accumulateValues((a, b) -> a.compareTo(b) <= 0 ? a : b, maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Short> sumMatchingShortValues(Map<K, Short>... maps)
    { return accumulateValues((a, b) -> (short)(a + b), maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Short> sumMatchingShortValues(Iterable<Map<K, Short>> maps)
    { return accumulateValues((a, b) -> (short)(a + b), maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Integer> sumMatchingIntValues(Map<K, Integer>... maps)
    { return accumulateValues(Integer::sum, maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Integer> sumMatchingIntValues(Iterable<Map<K, Integer>> maps)
    { return accumulateValues(Integer::sum, maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Long> sumMatchingLongValues(Map<K, Long>... maps)
    { return accumulateValues(Long::sum, maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Long> sumMatchingLongValues(Iterable<Map<K, Long>> maps)
    { return accumulateValues(Long::sum, maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Float> sumMatchingFloatValues(Map<K, Float>... maps)
    { return accumulateValues(Float::sum, maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Float> sumMatchingFloatValues(Iterable<Map<K, Float>> maps)
    { return accumulateValues(Float::sum, maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Double> sumMatchingDoubleValues(Map<K, Double>... maps)
    { return accumulateValues(Double::sum, maps); }

    /**
     * Produces a map where the item assigned to each key is a sum of the items added to the same key in every passed
     * map, where applicable.
     * @param maps The maps to sum the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the sum of the values associated with the same
     *         key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Double> sumMatchingDoubleValues(Iterable<Map<K, Double>> maps)
    { return accumulateValues(Double::sum, maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Short> multiplyMatchingShortValues(Map<K, Short>... maps)
    { return accumulateValues((a, b) -> (short)(a + b), maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Short> multiplyMatchingShortValues(Iterable<Map<K, Short>> maps)
    { return accumulateValues((a, b) -> (short)(a + b), maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Integer> multiplyMatchingIntValues(Map<K, Integer>... maps)
    { return accumulateValues((a, b) -> a * b, maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Integer> multiplyMatchingIntValues(Iterable<Map<K, Integer>> maps)
    { return accumulateValues((a, b) -> a * b, maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Long> multiplyMatchingLongValues(Map<K, Long>... maps)
    { return accumulateValues((a, b) -> a * b, maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Long> multiplyMatchingLongValues(Iterable<Map<K, Long>> maps)
    { return accumulateValues((a, b) -> a * b, maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Float> multiplyMatchingFloatValues(Map<K, Float>... maps)
    { return accumulateValues((a, b) -> a * b, maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Float> multiplyMatchingFloatValues(Iterable<Map<K, Float>> maps)
    { return accumulateValues((a, b) -> a * b, maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Double> multiplyMatchingDoubleValues(Map<K, Double>... maps)
    { return accumulateValues((a, b) -> a * b, maps); }

    /**
     * Produces a map where the item assigned to each key is the result of multiplying the values assigned to the same
     * key in the different passed maps together.
     * @param maps The maps to multiply the values from.
     * @param <K> The type of the keys of the maps.
     * @return A new map object where the value assigned to each key is the result of multiplying together all of the
     * values associated with the same key in all of the passed maps where there is a value associated with that key.
     */
    public static <K> Map<K, Double> multiplyMatchingDoubleValues(Iterable<Map<K, Double>> maps)
    { return accumulateValues((a, b) -> a * b, maps); }
}
