package scot.massie.lib.events.convenience;

import scot.massie.lib.events.EventListener;
import scot.massie.lib.events.args.EventArgs;

/**
 * A pairing of an event listener and its priority in an event.
 *
 * Exists because of Java's lack of support for named tuples.
 */
public final class EventListenerPriorityPair<TArgs extends EventArgs> implements Comparable<EventListenerPriorityPair<TArgs>>
{
    public EventListenerPriorityPair(EventListener<TArgs> listener, double priority)
    {
        this.listener = listener;
        this.priority = priority;
    }

    public EventListenerPriorityPair(EventListener<TArgs> listener)
    {
        this.listener = listener;
        this.priority = Double.NEGATIVE_INFINITY;
    }

    private final EventListener<TArgs> listener;
    private final double priority;

    public EventListener<TArgs> getListener()
    { return listener; }

    public double getPriority()
    { return priority; }

    public EventListenerCallInfo<TArgs> toCallInfo(TArgs args)
    { return new EventListenerCallInfo<>(listener, priority, args); }

    @Override
    public int compareTo(EventListenerPriorityPair<TArgs> o)
    { return Double.compare(this.priority, o.priority); }
}
