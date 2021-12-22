package scot.massie.lib.collections.iterables;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Repository for static utility methods pertaining to lists.
 */
public final class ListUtils
{
    private ListUtils()
    {}

    /**
     * Gets the first index of a list where the corresponding item satisfies the given predicate.
     * @param list The list to look through.
     * @param predicate The predicate to be satisfied.
     * @param <T> The type argument of the given list.
     * @return The lowest index of any item in the list satisfying the given predicate, or -1 if no items in the list
     *         satisfy the given predicate.
     */
    @SuppressWarnings("TypeMayBeWeakened") /* May not be weakened, the index of the list is relevant. */
    public static <T> int firstIndexWhere(List<T> list, Predicate<? super T> predicate)
    {
        int i = 0;

        for(T item : list)
        {
            if(predicate.test(item))
                return i;

            i++;
        }

        return -1;
    }

    /**
     * Gets the first index of a list where it and its corresponding item satisfies the given predicate.
     * @param list The list to look through.
     * @param predicate The predicate to be satisfied.
     * @param <T> The type argument of the given list.
     * @return The lowest index of any item in the list that, together with the index, satisfies the given predicate, or
     *         -1 if no items in the list satisfy the given predicate.
     */
    @SuppressWarnings("TypeMayBeWeakened") /* May not be weakened, the index of the list is relevant. */
    public static <T> int firstIndexWhere(List<T> list, BiPredicate<? super Integer, ? super T> predicate)
    {
        int i = 0;

        for(T item : list)
        {
            if(predicate.test(i, item))
                return i;
        }

        return -1;
    }

    /**
     * Gets the last index of a list where the corresponding item satisfies the given predicate.
     * @param list The list to look through.
     * @param predicate The predicate to be satisfied.
     * @param <T> The type argument of the given list.
     * @return The highest index of any item in the list satisfying the given predicate, or the maximum index of the
     *         list, * -1 then - 1, if no items in the list satisfy the given predicate.
     */
    public static <T> int lastIndexWhere(List<T> list, Predicate<? super T> predicate)
    {
        for(int i = list.size() - 1; i >= 0; i--)
            if(predicate.test(list.get(i)))
                return i;

        return -(list.size());
    }

    /**
     * Gets the last index of a list where the corresponding item satisfies the given predicate.
     * @param list The list to look through.
     * @param predicate The predicate to be satisfied.
     * @param <T> The type argument of the given list.
     * @return The highest index of any item in the list satisfying the given predicate, or the maximum index of the
     *         list, * -1 then - 1, if no items in the list satisfy the given predicate.
     */
    @SuppressWarnings("TypeMayBeWeakened") /* May not be weakened, the index of the list is relevant. */
    public static <T> int lastIndexWhere(LinkedList<T> list, Predicate<? super T> predicate)
    {
        if(list.isEmpty())
            return -1;

        int i = list.size() - 1;
        Iterator<T> iter = list.descendingIterator();

        for(T item = iter.next(); iter.hasNext(); i--, item = iter.next())
            if(predicate.test(item))
                return i;

        return -(list.size());
    }

    /**
     * Gets the last index of a list where it and its corresponding item satisfies the given predicate.
     * @param list The list to look through.
     * @param predicate The predicate to be satisfied.
     * @param <T> The type argument of the given list.
     * @return The highest index of any item in the list that, together with the index, satisfies the given predicate,
     *         or the maximum index of the list, * -1 then - 1, if no items in the list satisfy the given predicate.
     */
    public static <T> int lastIndexWhere(List<T> list, BiPredicate<? super Integer, ? super T> predicate)
    {
        for(int i = list.size() - 1; i >= 0; i--)
            if(predicate.test(i, list.get(i)))
                return i;

        return -(list.size());
    }

    /**
     * Gets the last index of a list where it and its corresponding item satisfies the given predicate.
     * @param list The list to look through.
     * @param predicate The predicate to be satisfied.
     * @param <T> The type argument of the given list.
     * @return The highest index of any item in the list that, together with the index, satisfies the given predicate,
     *         or the maximum index of the list, * -1 then - 1, if no items in the list satisfy the given predicate.
     */
    @SuppressWarnings("TypeMayBeWeakened") /* May not be weakened, the index of the list is relevant. */
    public static <T> int lastIndexWhere(LinkedList<T> list, BiPredicate<? super Integer, ? super T> predicate)
    {
        if(list.isEmpty())
            return -1;

        int i = list.size() - 1;
        Iterator<T> iter = list.descendingIterator();

        for(T item = iter.next(); iter.hasNext(); i--, item = iter.next())
            if(predicate.test(i, item))
                return i;

        return -(list.size());
    }
}
