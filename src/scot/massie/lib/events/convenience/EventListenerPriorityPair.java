package scot.massie.lib.events.convenience;

import scot.massie.lib.events.EventListener;
import scot.massie.lib.events.args.EventArgs;

/**
 * A pairing of an event listener and its priority in an event.
 *
 * Exists because of Java's lack of support for named tuples.
 */
public final class EventListenerPriorityPair<TArgs extends EventArgs>
{
    public EventListenerPriorityPair(EventListener<TArgs> listener, double priority)
    {
        this.listener = listener;
        this.priority = priority;
    }

    private final EventListener<TArgs> listener;
    private final double priority;

    public EventListener<TArgs> getListener()
    { return listener; }

    public double getPriority()
    { return priority; }
}
