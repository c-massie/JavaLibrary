package scot.massie.lib.utils.wrappers;

public class MutableWrapper<T> extends Wrapper<T>
{
    public MutableWrapper(T item)
    { super(item); }

    /**
     * Replaces the item in this wrapper with the provided item.
     * @param item The item to be put into this wrapper.
     */
    public void set(T item)
    { this.item = item; }
}
