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

    /**
     * Replaces the item in this wrapper with the provided item, and returns the new item.
     * @param item The item to be put into this wrapper.
     * @return The item in this wrapper as a result of this method call.
     */
    public T setThenGet(T item)
    { return this.item = item; }

    /**
     * Replaces the item in this wrapper with the provided item, and returns the previous item.
     * @param item The item to be put into this wrapper.
     * @return The item previously in the wrapper before calling this method.
     */
    public T getThenSet(T item)
    {
        T old = this.item;
        this.item = item;
        return old;
    }
}
