package scot.massie.lib.events.args.predefined;

/**
 * Generic event args for an item being added to something.
 * @param <T> The type of the item being added.
 */
public class ItemAddedEventArgs<T> extends ContentsChangedEventArgs
{
    /**
     * The item being added.
     */
    protected T itemAdded;

    /**
     * Creates a new event args for an item being added to something.
     * @param itemAdded The item being added.
     */
    public ItemAddedEventArgs(T itemAdded)
    { this.itemAdded = itemAdded; }

    /**
     * Gets the item being added.
     * @return The item being added.
     */
    public T getItemAdded()
    { return itemAdded; }
}
