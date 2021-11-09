package scot.massie.lib.collections.iterables;

import java.util.ArrayList;
import java.util.List;

public final class IterableUtils
{
    private IterableUtils()
    {}

    /**
     * Converts an iterable into a list.
     * @param iterable The iterable to convert into a list.
     * @param <T> The type of the iterable.
     * @return A list containing the items from the given iterable, in the order given.
     */
    public static <T> List<T> iterableToList(Iterable<? extends T> iterable)
    {
        List<T> result = new ArrayList<>();

        for(T i : iterable)
            result.add(i);

        return result;
    }

    /**
     * Converts an iterable into a list.
     * @param iterable The iterable to convert into a list.
     * @param maxListSize The maximum size of the output list.
     * @param <T> The type of the iterable.
     * @return A list containing the items from the given iterable, in the order given. Any items given by the iterable
     *         after the Nth item where N is the maxListSize are ignored.
     */
    public static <T> List<T> iterableToList(Iterable<? extends T> iterable, int maxListSize)
    {
        List<T> result = new ArrayList<>();
        int count = 0;

        for(T i : iterable)
        {
            result.add(i);

            if(++count == maxListSize)
                break;
        }

        return result;
    }
}
