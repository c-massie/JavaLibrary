package scot.massie.lib.events.args.predefined;

import scot.massie.lib.events.args.EventArgs;

/**
 * Generic event args for an item being added to something.
 * @param <T> The type of the item being added.
 */
public class ItemAddedEventArgs<T> implements EventArgs
{
    /**
     * Creates a new event args for an item being added to something.
     * @param itemAdded The item being added.
     */
    public ItemAddedEventArgs(T itemAdded)
    { this.itemAdded = itemAdded; }

    /**
     * The item being added.
     */
    protected T itemAdded;

    /**
     * Gets the item being added.
     * @return The item being added.
     */
    public T getItemAdded()
    { return itemAdded; }
}
