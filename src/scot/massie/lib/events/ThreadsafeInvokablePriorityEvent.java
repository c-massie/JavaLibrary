package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerPriorityPair;

import java.util.List;

/**
 * Wrapper for an event with support for listener priorities to produce a threadsafe variant.
 *
 * Listeners are called outwith the synchronisation lock, although accessing the information needed to invoke the event
 * is still done synchronously.
 *
 * Note that this won't prevent the original event being invoked independently of this wrapper.
 *
 * Note that the standard basic priority event implementations, {@link OrderedEvent}, is independently threadsafe and
 * doesn't need to be wrapped in this.
 * @param <TArgs> The type of the EventArgs objects passed to invocations of this event.
 */
public class ThreadsafeInvokablePriorityEvent<TArgs extends EventArgs>
        extends ThreadsafeInvokableEvent<TArgs> implements InvokablePriorityEvent<TArgs>
{
    /**
     * The event being wrapped to make it threadsafe. This shadows the original field of the same name in
     * ThreadsafeInvokableEvent, containing the same object, but providing access to additional functions.
     */
    protected final InvokablePriorityEvent<TArgs> wrappedEvent;

    /**
     * Creates a new ThreadsafeInvokablePriorityEvent by wrapping a (presumably unthreadsafe) invokable priority event
     * object.
     * @param wrappedEvent The event to wrap.
     */
    public ThreadsafeInvokablePriorityEvent(InvokablePriorityEvent<TArgs> wrappedEvent)
    {
        super(wrappedEvent);
        this.wrappedEvent = wrappedEvent;
    }

    /**
     * Creates a new ThreadsafeInvokablePriorityEvent by wrapping an {@link OrderedEvent}. Note that OrderedEvent is
     * already threadsafe, so this does nothing but provide a version that can be stored in a variable of this type.
     */
    public ThreadsafeInvokablePriorityEvent()
    { this(new OrderedEvent<>()); }

    @Override
    public List<EventListenerPriorityPair<TArgs>> getListenersWithPriorities()
    { synchronized(wrappedEvent) { return wrappedEvent.getListenersWithPriorities(); } }

    @Override
    public void register(EventListener<TArgs> listener, double priority)
    { synchronized(wrappedEvent) { wrappedEvent.register(listener, priority); } }
}
