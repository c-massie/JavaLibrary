package scot.massie.lib.events.args.predefined;

import scot.massie.lib.events.args.EventArgs;

/**
 * Generic event args for an item being removed from something.
 * @param <T> The type of the item being removed.
 */
public class ItemRemovedEventArgs<T> implements EventArgs
{
    /**
     * Creates a new event args for an item being removed from something.
     * @param itemRemoved The item being added.
     */
    public ItemRemovedEventArgs(T itemRemoved)
    { this.itemRemoved = itemRemoved; }

    /**
     * The item being removed.
     */
    protected T itemRemoved;

    /**
     * Gets the item being removed.
     * @return The item being removed.
     */
    public T getItemRemoved()
    { return itemRemoved; }
}
