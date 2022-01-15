package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;

import java.util.function.Function;

/**
 * <p>Listenable generic event, for registering listeners to to be run when the event happens.</p>
 *
 * <p>Events may be exposed as fields. Events should generally always be final, as each Event object represents
 * something happening to that object and isn't expected to be reässigned.</p>
 * @param <TArgs> The type of the event args objects passed to listeners of this event.
 */
public interface Event<TArgs extends EventArgs>
{
    /**
     * Registers an event listener to this event.
     * @param listener The listener to be called when this event is invoked.
     */
    void register(EventListener<TArgs> listener);

    /**
     * Registers an event listener to this event.
     * @param listener The listener to be called when this event is invoked.
     */
    default void register(ArglessEventListener listener)
    { register(x -> listener.onEvent()); }

    /**
     * Registers another event to this event as a dependent event.
     * Dependent events will be invoked when this event is invoked.
     * Passes the same EventArgs object to the listeners of the dependent event as to this one.
     * @param dependentEvent The event to register as a dependent event of this one.
     */
    @SuppressWarnings("ClassReferencesSubclass")
    // Events should not be able to invoke dependent events that aren't invokable.
    void register(InvokableEvent<TArgs> dependentEvent);

    /**
     * Registers another event to this event as a dependent event.
     * Dependent events will be invoked when this event is invoked.
     * Passes an EventArgs object to the listeners of the dependent event derived from passing the one passed by this
     * event to its listeners through the provided function.
     * @param dependentEvent The event to register as a dependent event of this one.
     * @param argWrapper A function to convert EventArgs objects of the dominant event to EventArgs objects of the
     *                   dependent event. This should generally wrap the dominant event's EventArgs objects such that
     *                   accesses and mutations are translated back to the original EventArgs object where applicable.
     * @param <TDependentArgs> The EventArgs type of the event to be registered as a dependent event.
     */
    @SuppressWarnings("ClassReferencesSubclass")
    // Events should not be able to invoke dependent events that aren't invokable.
    <TDependentArgs extends EventArgs> void register(InvokableEvent<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper);

    /**
     * Deregisters an event listener from this event.
     * @param listener The listener to deregister from this event.
     */
    void deregister(EventListener<TArgs> listener);

    /**
     * Deregisters another event as a dependent event from this event.
     * @param event The event to deregister as a dependent event.
     */
    @SuppressWarnings("ClassReferencesSubclass")
    // Events should not be able to invoke dependent events that aren't invokable.
    void deregister(InvokableEvent<?> event);

    /**
     * Gets whether or not listeners to this event are called in any particular order. e.g. if it supports event
     * listener priority.
     * @return True if listener call order matters. Otherwise, false.
     */
    boolean listenerOrderMatters();
}
