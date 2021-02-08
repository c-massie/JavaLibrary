package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Generic event for calling arbitrary listeners.
 * @param <TArgs> The type of the event args objects passed to listeners of this event.
 */
public interface Event<TArgs extends EventArgs>
{
    /**
     * Calls all listeners to this event.
     * @param eventArgs A fresh event arguments object, possibly with information relating to this invocation.
     */
    void invoke(TArgs eventArgs);

    /**
     * Registers an event listener to this event.
     * @param listener The listener to be called when this event is invoked.
     */
    void register(EventListener<TArgs> listener);

    /**
     * Registers another event to this event as a dependent event.
     * Dependent events will be invoked when this event is invoked.
     * Passes the same EventArgs object to the listeners of the dependent event as to this one.
     * @param dependentEvent The event to register as a dependent event of this one.
     */
    void register(Event<TArgs> dependentEvent);

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
    <TDependentArgs extends EventArgs> void register(Event<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper);

    /**
     * Deregisters an event listener from this event.
     * @param listener The listener to deregister from this event.
     */
    void deregister(EventListener<TArgs> listener);

    /**
     * Deregisters another event as a dependent event from this event.
     * @param event The event to deregister as a dependent event.
     */
    void deregister(Event<?> event);

    /**
     * Removes all listeners to this event.
     */
    void clearListeners();

    /**
     * Gets the listeners registered to this event.
     * @return A collections of the listeners registered to this event.
     */
    Collection<EventListener<TArgs>> getListeners();

    /**
     * Generates the information required to call all listeners of this event and all dependent events appropriately as
     * the result of the event being raised, as represented by the provided EventArgs object.
     * @param args The EventArgs representing this specific event invocation.
     * @return A list of objects pairing each event listener with the information required to call it
     * appropriately for this event invocation. The returned list should be mutable and not used elsewhere.githu
     *
     */
    List<EventListenerCallInfo<?>> generateCallInfo(TArgs args);
}
