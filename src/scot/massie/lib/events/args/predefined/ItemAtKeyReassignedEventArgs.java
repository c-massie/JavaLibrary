package scot.massie.lib.events.args.predefined;

import scot.massie.lib.events.args.EventArgs;

/**
 * Generic event args for the value assigned to a key being changed or removed.
 * @param <K> The type of the key being assigned to.
 * @param <V> The type of the value being assigned or removed.
 */
public class ItemAtKeyReassignedEventArgs<K, V> implements EventArgs
{
    /**
     * Creates a new event args for the value assigned to a key being changed.
     * @param key The key being assigned to.
     * @param previousItem The item previously assigned to the key.
     * @param newItem The item to be assigned to the key after the event has happened.
     */
    public ItemAtKeyReassignedEventArgs(K key, V previousItem, V newItem)
    { this(key, true, previousItem, true, newItem); }

    /**
     * Creates a new event args for a value being assigned to a key.
     * @param key The key being assigned to.
     * @param newItem The item to be assigned to the key.
     */
    public ItemAtKeyReassignedEventArgs(K key, V newItem)
    { this(key, false, null, true, newItem); }

    /**
     * Creates a new event args for the value assigned to a key being changed.
     * @param key The key being assigned to.
     * @param hadItemBefore Whether or not there was previously an item assigned to the key.
     * @param previousItem The item previously assigned to the key.
     * @param hasItemNow Whether or not there should be an item assigned to the key after the event has happened.
     * @param newItem The item to be assigned to the key after the event has happened.
     */
    protected ItemAtKeyReassignedEventArgs(K key, boolean hadItemBefore, V previousItem, boolean hasItemNow, V newItem)
    {
        this.key = key;
        this.keyHadItemBefore = hadItemBefore;
        this.previousItem = previousItem;
        this.keyHasItemNow = hasItemNow;
        this.newItem = newItem;
    }

    /**
     * Creates a new event args for a key being deassigned an item.
     * @param key The key being deassigned from.
     * @param previousItem The item previously assigned to the key.
     * @param <K> The type of the key being deassigned from.
     * @param <V> The type of the item being deassigned.
     * @return A new event args object representing a key being deassigned a value.
     */
    public static <K, V> ItemAtKeyReassignedEventArgs<K, V> clearingAtKey(K key, V previousItem)
    { return new ItemAtKeyReassignedEventArgs<>(key, true, previousItem, false, null); }

    /**
     * The key being reassigned an item.
     */
    protected final K key;

    /**
     * Whether or not an item was previously assigned to the key.
     */
    protected final boolean keyHadItemBefore;

    /**
     * The item previously assigned to the key, if applicable.
     */
    protected final V previousItem;

    /**
     * Whether or not there should be an item assigned to the key after the event has happened.
     */
    protected boolean keyHasItemNow;

    /**
     * The item to be assigned to the key after the event has happened.
     */
    protected V newItem;

    /**
     * Gets the key being reassigned items.
     * @return The key being reassigned items.
     */
    public K getKey()
    { return key; }

    /**
     * Gets whether or not there was previously an item assigned to the key.
     * @return True if there was an item assigned to the key. Otherwise, false.
     */
    public boolean hadItemBefore()
    { return keyHadItemBefore; }

    /**
     * Gets the item previously assigned to the key.
     * @return The item previously assigned to the key, or null if there was none.
     */
    public V getPreviousItem()
    { return previousItem; }

    /**
     * Gets whether or not there should be an item assigned to the key after the event has happened.
     * @return True if an item should be assigned to the key. Otherwise, false.
     */
    public boolean hasItemNow()
    { return keyHasItemNow; }

    /**
     * Gets the item to be assigned to the key after the event has happened.
     * @return The item to be assigned to the key, or null if there is none.
     */
    public V getNewItem()
    { return newItem; }
}
