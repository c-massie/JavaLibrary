package scot.massie.lib.events.convenience;

import scot.massie.lib.events.EventListener;
import scot.massie.lib.events.args.EventArgs;

/**
 * An event listener paired with all information required to call the listener properly as part of an event invocation.
 *
 * Exists because of Java's lack of support for named tuples.
 */
public final class EventListenerCallInfo<TArgs extends EventArgs>
{
    public EventListenerCallInfo(EventListener<TArgs> listener, double priority, TArgs args)
    {
        this.listener = listener;
        this.priority = priority;
        this.hasPriority = true;
        this.args = args;
    }

    public EventListenerCallInfo(EventListener<TArgs> listener, TArgs args)
    {
        this.listener = listener;
        this.priority = 0;
        this.hasPriority = false;
        this.args = args;
    }

    private final EventListener<TArgs> listener;
    private final double priority;
    private final boolean hasPriority;
    private final TArgs args;

    public EventListener<TArgs> getListener()
    { return listener; }

    public double getPriority()
    { return priority; }

    public boolean hasPriority()
    { return hasPriority; }

    public TArgs getArgs()
    { return args; }
}
