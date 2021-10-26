package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

// TO DO: The same as this class, but ThreadsafeInvokablePriorityEvent

/**
 * Wrapper for an event to produce a threadsafe variant.
 *
 * Listeners are called outwith the synchronisation lock, although accessing the information needed to invoke the event
 * is still done synchronously.
 *
 * Note that this won't prevent the original event being invoked independently of this wrapper.
 *
 * Note that the standard basic event implementations, {@link SetEvent} and {@link OrderedEvent}, are independently
 * threadsafe and don't need to be wrapped in this.
 * @param <TArgs> The type of the EventArgs objects passed to invocations of this event.
 */
public class ThreadsafeInvokableEvent<TArgs extends EventArgs> implements InvokableEvent<TArgs>
{
    /**
     * The event being wrapped to make it threadsafe.
     */
    protected final InvokableEvent<TArgs> wrappedEvent;

    /**
     * Creates a new ThreadsafeInvokableEvent by wrapping a (presumably unthreadsafe) invokable event object.
     * @param wrappedEvent The event to wrap.
     */
    public ThreadsafeInvokableEvent(InvokableEvent<TArgs> wrappedEvent)
    { this.wrappedEvent = wrappedEvent; }

    /**
     * Creates a new ThreadsafeInvokableEvent by wrapping a {@link SetEvent}. Note that SetEvent is already threadsafe,
     * so this does nothing but provide a version that can be stored in a variable of this type.
     */
    public ThreadsafeInvokableEvent()
    { this(new SetEvent<>()); }

    @Override
    public void register(EventListener<TArgs> listener)
    { synchronized(wrappedEvent) { wrappedEvent.register(listener); } }

    @Override
    public void register(InvokableEvent<TArgs> dependentEvent)
    { synchronized(wrappedEvent) { wrappedEvent.register(dependentEvent); } }

    @Override
    public <TDependentArgs extends EventArgs> void register(InvokableEvent<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
    { synchronized(wrappedEvent) { wrappedEvent.register(dependentEvent, argWrapper); } }

    @Override
    public void deregister(EventListener<TArgs> listener)
    { synchronized(wrappedEvent) { wrappedEvent.deregister(listener); } }

    @Override
    public void deregister(InvokableEvent<?> event)
    { synchronized(wrappedEvent) { wrappedEvent.deregister(event); } }

    @Override
    public boolean listenerOrderMatters()
    { synchronized(wrappedEvent) { return wrappedEvent.listenerOrderMatters(); } }

    @Override
    public void invoke(TArgs eventArgs)
    {
        boolean listenerOrderMatters;
        Stream<EventListenerCallInfo<?>> listenerStream;

        synchronized(wrappedEvent)
        {
            listenerOrderMatters = wrappedEvent.listenerOrderMatters();
            listenerStream = wrappedEvent.generateCallInfoAsStream(eventArgs);
        }

        if(listenerOrderMatters)
            listenerStream.sorted(Events.listenerCallInfoComparator)
                          .forEachOrdered(EventListenerCallInfo::callListener);
        else
            listenerStream.forEach(EventListenerCallInfo::callListener);
    }

    @Override
    public void clearListeners()
    { synchronized(wrappedEvent) { wrappedEvent.clearListeners(); } }

    @Override
    public void clearDependentEvents()
    { synchronized(wrappedEvent) { wrappedEvent.clearDependentEvents(); } }

    @Override
    public void clear()
    { synchronized(wrappedEvent) { wrappedEvent.clear(); } }

    @Override
    public Collection<EventListener<TArgs>> getListeners()
    { synchronized(wrappedEvent) { return wrappedEvent.getListeners(); } }

    @Override
    public Collection<InvokableEvent<?>> getDependentEvents()
    { synchronized(wrappedEvent) { return wrappedEvent.getDependentEvents(); } }

    @Override
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    { synchronized(wrappedEvent) { return wrappedEvent.generateCallInfo(args); } }

    @Override
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args)
    { synchronized(wrappedEvent) { return wrappedEvent.generateCallInfoAsStream(args); } }
}
