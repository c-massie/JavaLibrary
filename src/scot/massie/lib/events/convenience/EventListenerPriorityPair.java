package scot.massie.lib.events.convenience;

import scot.massie.lib.events.EventListener;
import scot.massie.lib.events.args.EventArgs;

/**
 * A pairing of an event listener and its priority in an event.
 *
 * Exists because of Java's lack of support for named tuples.
 */
public final class EventListenerPriorityPair<TArgs extends EventArgs>
        implements Comparable<EventListenerPriorityPair<TArgs>>
{
    /**
     * The event listener.
     */
    private final EventListener<TArgs> listener;

    /**
     * The priority.
     */
    private final double priority;

    /**
     * Whether or not this pairing has a priority.
     */
    private final boolean hasPriority;

    /**
     * Creates a new event listener priority pair, with priority.
     * @param listener The event listener.
     * @param priority The event listener's priority.
     */
    public EventListenerPriorityPair(EventListener<TArgs> listener, double priority)
    {
        this.listener = listener;
        this.priority = priority;
        this.hasPriority = true;
    }

    /**
     * Creates a new event listener priority pair, with no priority.
     * @param listener The event listener.
     */
    public EventListenerPriorityPair(EventListener<TArgs> listener)
    {
        this.listener = listener;
        this.priority = Double.NEGATIVE_INFINITY;
        this.hasPriority = false;
    }

    /**
     * Gets the listener.
     * @return The listener/
     */
    public EventListener<TArgs> getListener()
    { return listener; }

    /**
     * Gets the listener's priority.
     * @return The listener's priority.
     */
    public double getPriority()
    { return priority; }

    /**
     * Creates a new EventListenerCallInfo instance using the listener and priority in this pairing, along with a given
     * matching eventargs object to be passed into the listener.
     * @param args The eventargs object to be passed into the listener upon invocation.
     * @return A new EventListenerCallInfo instance, with this pairing's listener and priority, and the given eventargs
     *         object.
     */
    public EventListenerCallInfo<TArgs> toCallInfo(TArgs args)
    {
        return hasPriority ? new EventListenerCallInfo<>(listener, priority, args)
                           : new EventListenerCallInfo<>(listener, args);
    }

    /**
     * Listener priority pairs are comparable by their priorities.
     * @param o The other pair to compare.
     * @return The result of comparing this pair's priority to the other pair's priority.
     */
    @Override
    public int compareTo(EventListenerPriorityPair<TArgs> o)
    { return Double.compare(this.priority, o.priority); }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof EventListenerPriorityPair))
            return false;

        @SuppressWarnings("rawtypes") // Can't tell type parameter - at this point, it doesn't matter.
        EventListenerPriorityPair other = (EventListenerPriorityPair)obj;

        //noinspection unchecked
        return (this.listener == other.listener) && (compareTo(other) == 0);
    }
}
