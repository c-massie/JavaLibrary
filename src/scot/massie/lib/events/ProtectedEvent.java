package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;

import java.util.function.Function;

/**
 * <p>Wraps another event object, denying access to features intended for the implementation of the event rather than the
 * interaction with it.</p>
 *
 * <p>Note that the wrapped event may still be accessible via reflection.</p>
 * @param <TArgs> The type of the event args objects passed to listeners of this event.
 * @see InvokableEvent
 */
public class ProtectedEvent<TArgs extends EventArgs> implements Event<TArgs>
{
    /**
     * Wraps the given event in a ProtectedEvent instance. Calls to the instance will defer to the wrapped event, except
     * for calls made to methods intended for the implementation of an event rather than the use of one, which will be
     * blocked.
     * @param wrappedEvent The event to wrap.
     */
    public ProtectedEvent(InvokableEvent<TArgs> wrappedEvent)
    { this.wrappedEvent = wrappedEvent; }

    protected final InvokableEvent<TArgs> wrappedEvent;

    @Override
    public void register(EventListener<TArgs> listener)
    { wrappedEvent.register(listener); }

    @Override
    public void register(InvokableEvent<TArgs> dependentEvent)
    { wrappedEvent.register(dependentEvent); }

    @Override
    public <TDependentArgs extends EventArgs> void register(InvokableEvent<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
    { wrappedEvent.register(dependentEvent, argWrapper); }

    @Override
    public void deregister(EventListener<TArgs> listener)
    { wrappedEvent.deregister(listener); }

    @Override
    public void deregister(InvokableEvent<?> event)
    { wrappedEvent.deregister(event); }

    @Override
    public boolean listenerOrderMatters()
    { return wrappedEvent.listenerOrderMatters(); }
}
