package scot.massie.lib.utils.wrappers;

/**
 * Wraps an item in an object.
 * @param <T> The type of the object stored in the wrapper.
 */
public class Wrapper<T>
{
    /**
     * Wraps the given item in a wrapper instance.
     * @param item The item to be wrapped.
     */
    public Wrapper(T item)
    { this.item = item; }

    /**
     * The item in the wrapper.
     */
    protected T item;

    /**
     * Gets the item contained in this wrapper.
     * @return The item contained in this wrapper.
     */
    public T get()
    { return item; }
}
