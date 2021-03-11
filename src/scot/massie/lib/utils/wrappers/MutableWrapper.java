package scot.massie.lib.utils.wrappers;

/**
 * Wraps an item in an object. The item can be changed.
 * @param <T> The type of the item in the wrapper.
 */
public class MutableWrapper<T> extends Wrapper<T>
{
    /**
     * Wraps the given item in a mutable wrapper instance.
     * @param item The item to be wrapped.
     */
    public MutableWrapper(T item)
    { super(item); }

    /**
     * Replaces the item in this wrapper with the provided item.
     * @param item The item to be put into this wrapper.
     */
    public void set(T item)
    { this.item = item; }
}
