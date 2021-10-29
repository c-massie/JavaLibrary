package scot.massie.lib.events.convenience;

import scot.massie.lib.events.EventListener;
import scot.massie.lib.events.args.EventArgs;

/**
 * An event listener paired with all information required to call the listener properly as part of an event invocation.
 */
public final class EventListenerCallInfo<TArgs extends EventArgs>
{
    /**
     * The listener being invoked.
     */
    private final EventListener<TArgs> listener;

    /**
     * The listener's priority, if one is explicitly given.
     */
    private final double priority;

    /**
     * Whether or not the listener has a priority.
     */
    private final boolean hasPriority;

    /**
     * The eventargs object to be passed to the listener when invoked.
     */
    private final TArgs args;

    /**
     * Creates a new EventListenerCallInfo object with priority.
     * @param listener The event listener being invoked.
     * @param priority The event listener's priority.
     * @param args The eventargs object being passed to the event listener.
     */
    public EventListenerCallInfo(EventListener<TArgs> listener, double priority, TArgs args)
    {
        this.listener = listener;
        this.priority = priority;
        this.hasPriority = true;
        this.args = args;
    }

    /**
     * Creates a new EventListenerCallInfo object without priority.
     * @param listener The event listener being invoked.
     * @param args The eventargs object being passed to the event listener.
     */
    public EventListenerCallInfo(EventListener<TArgs> listener, TArgs args)
    {
        this.listener = listener;
        this.priority = 0;
        this.hasPriority = false;
        this.args = args;
    }

    /**
     * Gets the listener being invoked.
     * @return The listener being invoked.
     */
    public EventListener<TArgs> getListener()
    { return listener; }

    /**
     * Gets the listener's priority.
     * @return The listener's priority, or 0 if the listener has no priority.
     */
    public double getPriority()
    { return priority; }

    /**
     * Whether or not the listener has a priority.
     * @return True if the listener has a priority, otherwise false.
     */
    public boolean hasPriority()
    { return hasPriority; }

    /**
     * Gets the eventargs object to be passed to the listener upon invocation.
     * @return The eventargs object.
     */
    public TArgs getArgs()
    { return args; }

    /**
     * Invokes the event listener, passing in the eventargs object
     */
    public void callListener()
    { listener.onEvent(args); }
}
