package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * <p>Wraps another event object, denying access to features intended for the implementation of the event rather than the
 * interaction with it.</p>
 *
 * <p>Note that the wrapped event may still be accessible via reflection.</p>
 * @param <TArgs> The type of the event args objects passed to listeners of this event.
 * @see Event
 */
public class ProtectedEvent<TArgs extends EventArgs> implements Event<TArgs>
{
    /**
     * Wraps the given event in a ProtectedEvent instance. Calls to the instance will defer to the wrapped event, except
     * for calls made to methods intended for the implementation of an event rather than the use of one, which will be
     * blocked.
     * @param wrappedEvent The event to wrap.
     */
    public ProtectedEvent(Event<TArgs> wrappedEvent)
    { this.wrappedEvent = wrappedEvent; }

    protected final Event<TArgs> wrappedEvent;

    @Override
    public void invoke(TArgs eventArgs)
    { throw new UnsupportedOperationException("You may not invoke this event yourself."); }

    @Override
    public void register(EventListener<TArgs> listener)
    { wrappedEvent.register(listener); }

    @Override
    public void register(Event<TArgs> dependentEvent)
    { wrappedEvent.register(dependentEvent); }

    @Override
    public <TDependentArgs extends EventArgs> void register(Event<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
    { wrappedEvent.register(dependentEvent, argWrapper); }

    @Override
    public void deregister(EventListener<TArgs> listener)
    { wrappedEvent.deregister(listener); }

    @Override
    public void deregister(Event<?> event)
    { wrappedEvent.deregister(event); }

    @Override
    public void clearListeners()
    { throw new UnsupportedOperationException("You may not clear the listeners of this event."); }

    @Override
    public void clearDependentEvents()
    { throw new UnsupportedOperationException("You may not clear the dependent events of this event."); }

    @Override
    public void clear()
    { throw new UnsupportedOperationException("You may not clear this event."); }

    @Override
    public boolean listenerOrderMatters()
    { return wrappedEvent.listenerOrderMatters(); }

    @Override
    public Collection<EventListener<TArgs>> getListeners()
    { throw new UnsupportedOperationException("You may not access all listeners of this event."); }

    @Override
    public Collection<Event<?>> getDependentEvents()
    { throw new UnsupportedOperationException("You may not access all dependent events of this event."); }

    @Override
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    {
        throw new UnsupportedOperationException("You may not attempt to generate the information required to invoke "
                                                + "this event.");
    }

    @Override
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args)
    {
        throw new UnsupportedOperationException("You may not attempt to generate the information required to invoke "
                                                + "this event.");
    }
}
