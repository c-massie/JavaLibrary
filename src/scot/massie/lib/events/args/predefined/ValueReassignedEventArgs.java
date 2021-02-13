package scot.massie.lib.events.args.predefined;

import scot.massie.lib.events.args.EventArgs;

/**
 * Generic event args for a value being changed.
 * @param <T> The type of the value being changed.
 */
public class ValueReassignedEventArgs<T> implements EventArgs
{
    /**
     * Creates a new event args for a value being changed.
     * @param previousValue The previous value.
     * @param newValue The value being changed to.
     */
    public ValueReassignedEventArgs(T previousValue, T newValue)
    {
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    /**
     * The previous value.
     */
    protected final T previousValue;

    /**
     * The value being changed to.
     */
    protected T newValue;

    /**
     * Gets the previous value.
     * @return The previous value.
     */
    public T getPreviousValue()
    { return previousValue; }

    /**
     * Gets the value being changed to.
     * @return The value being changed to.
     */
    public T getNewValue()
    { return newValue; }
}
