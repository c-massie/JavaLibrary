package scot.massie.lib.events.args.predefined;

import scot.massie.lib.events.args.EventArgs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Generic event args for any number of items being added to something.
 * @param <T> The type of the items being added.
 */
public class ItemsAddedEventArgs<T> implements EventArgs
{
    /**
     * Creates a new event args for items being added to something.
     * @param itemsAdded The items being added.
     */
    public ItemsAddedEventArgs(Collection<T> itemsAdded)
    {
        this.itemsAdded = itemsAdded;
        this.itemsAddedAsReadOnly = Collections.unmodifiableCollection(itemsAdded);
    }

    /**
     * Creates a new event args for items being added to something.
     * @param itemsAdded The items being added.
     */
    @SafeVarargs
    public ItemsAddedEventArgs(T... itemsAdded)
    { this(Arrays.asList(itemsAdded)); }

    /**
     * The items being added.
     */
    protected Collection<T> itemsAdded;

    /**
     * A read-only view of the items being added.
     */
    protected Collection<T> itemsAddedAsReadOnly;

    /**
     * Gets the items being added, as a read-only collection.
     * @return A read-only collection of the items being added.
     */
    public Collection<T> getItemsAdded()
    { return itemsAddedAsReadOnly; }
}
