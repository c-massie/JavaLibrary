package scot.massie.lib.utils.wrappers;

public class Wrapper<T>
{
    public Wrapper(T item)
    { this.item = item; }

    protected T item;

    /**
     * Gets the item contained in this wrapper.
     * @return The item contained in this wrapper.
     */
    public T get()
    { return item; }
}
