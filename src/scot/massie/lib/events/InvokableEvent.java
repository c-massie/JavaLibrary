package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * <p>Generic event for calling arbitrary listeners.</p>
 *
 * <p>Events may be exposed as fields. Events should generally always be final, as each Event object represents
 * something happening to that object and isn't expected to be reässigned. Note that exposing implementations of
 * InvokableEvent will allow it to be invoked from code accessing the object as part of the public interface.
 * Public-facing events should be non-invokable implementations of Event. (such as {@link ProtectedEvent} or
 * {@link ProtectedPriorityEvent})</p>
 *
 * <p>Example: {@code private final InvokableEvent<ThingHappenedEventArgs> thingHappened_internal = new SetEvent<>();}</p>
 * <p>Example: {@code public final Event<ThingHappenedEventArgs> thingHappened = new ProtectedEvent(thingHappened_internal)}</p>
 * @param <TArgs> The type of the event args objects passed to listeners of this event.
 */
public interface InvokableEvent<TArgs extends EventArgs> extends Event<TArgs>
{
    /**
     * Calls all listeners to this event.
     * @param eventArgs A fresh event arguments object, possibly with information relating to this invocation.
     */
    void invoke(TArgs eventArgs);

    /**
     * Removes all listeners to this event.
     */
    void clearListeners();

    /**
     * Removes all dependent events from this event.
     */
    void clearDependentEvents();

    /**
     * Removes all listeners to this event and all dependent events from this event.
     */
    void clear();

    /**
     * Gets the listeners registered to this event.
     * @return A collections of the listeners registered to this event.
     */
    Collection<EventListener<TArgs>> getListeners();

    /**
     * Gets the events dependent on this one.
     * @return A collection of all the events that will be invoked when this event is invoked.
     */
    Collection<InvokableEvent<?>> getDependentEvents();

    /**
     * Gets whether another event is dependent on this one, either directly or indirectly.
     * @return True if this event's dependent events, or any of their dependent events recursively, contains the given
     *         event. Otherwise, false.
     */
    default boolean hasDependentEventRecursively(InvokableEvent<?> other)
    {
        for(InvokableEvent<?> e : getDependentEvents())
            if(e == other || e.hasDependentEventRecursively(other))
                return true;

        return false;
    }

    /**
     * Generates the information required to call all listeners of this event and all dependent events appropriately as
     * the result of the event being raised, as represented by the provided EventArgs object.
     * @param args The EventArgs representing this specific event invocation.
     * @return A list of objects pairing each event listener with the information required to call it appropriately for
     *         this event invocation. The returned list should be mutable and not used elsewhere.
     */
    List<EventListenerCallInfo<?>> generateCallInfo(TArgs args);

    /**
     * Generates the information required to call all listeners of this event and all dependent events appropriately as
     * the result of the event being raised, as represented by the provided EventArgs object.
     * @param args The EventArgs representing this specific event invocation.
     * @return A stream of objects pairing each event listener with the information required to call it appropriately
     *         for this event invocation. The returned stream should be returned not consumed and should not be used
     *         elsewhere.
     */
    Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args);
}
