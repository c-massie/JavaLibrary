package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;

/**
 * <p>Wraps another priority event object, denying access to features intended for the implementation of the event
 * rather than the interaction with it.</p>
 *
 * <p>Note that the wrapped event may still be accessible via reflection.</p>
 * @param <TArgs> The type of the event args objects passed to listeners of this event.
 * @see InvokablePriorityEvent
 */
public class ProtectedPriorityEvent<TArgs extends EventArgs> extends ProtectedEvent<TArgs> implements PriorityEvent<TArgs>
{
    /**
     * The event this wraps, preventing direct invocation.
     */
    protected final InvokablePriorityEvent<TArgs> wrappedEvent;

    /**
     * Wraps the given priority event in a ProtectedPriorityEvent instance. Calls to the instance will defer to the
     * wrapped event, except for calls made to methods intended for the implementation of an event rather than the use
     * of one, which will be blocked.
     * @param wrappedEvent The event to wrap.
     */
    public ProtectedPriorityEvent(InvokablePriorityEvent<TArgs> wrappedEvent)
    {
        super(wrappedEvent);
        this.wrappedEvent = wrappedEvent;
    }

    @Override
    public void register(EventListener<TArgs> listener, double priority)
    { wrappedEvent.register(listener, priority); }
}
