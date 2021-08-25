package scot.massie.lib.events.args.predefined;

import scot.massie.lib.events.args.EventArgs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Generic event args for any number of items being removed from something.
 * @param <T> The type of the items being removed.
 */
public class ItemsRemovedEventArgs<T> extends ContentsChangedEventArgs
{
    /**
     * Creates a new event args for items being removed from something.
     * @param itemsRemoved The items being removed.
     */
    public ItemsRemovedEventArgs(Collection<T> itemsRemoved)
    {
        this.itemsRemoved = itemsRemoved;
        this.itemsRemovedAsReadOnly = Collections.unmodifiableCollection(itemsRemoved);
    }

    /**
     * Creates a new event args for items being removed from something.
     * @param itemsRemoved The items being removed.
     */
    @SafeVarargs
    public ItemsRemovedEventArgs(T... itemsRemoved)
    { this(Arrays.asList(itemsRemoved)); }

    /**
     * The items being removed.
     */
    protected Collection<T> itemsRemoved;

    /**
     * A read-only view of the items being added.
     */
    protected Collection<T> itemsRemovedAsReadOnly;

    /**
     * Gets the items being removed, as a read-only collection.
     * @return A read-only collection of the items being removed.
     */
    public Collection<T> getItemsRemoved()
    { return itemsRemovedAsReadOnly; }
}
