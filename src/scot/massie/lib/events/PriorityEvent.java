package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;

/**
 * Listenable generic event supporting the ability associate priorities to listeners, for registering listeners to be
 * run in order (from lowest to highest) when the event happens.
 * @param <TArgs> The type of the event args objects passed to listeners of this event.
 */
public interface PriorityEvent<TArgs extends EventArgs> extends Event<TArgs>
{
    /**
     * Registers an event listener to this event with the given priority.
     * @param listener The listener to be called when this event is invoked.
     * @param priority The priority of the listener. Listeners are called in order from lowest to highest priority.
     */
    void register(EventListener<TArgs> listener, double priority);

    /**
     * Registers an event listener to this event.
     *
     * Listeners registered without a priority can be considered to have a priority of negative infinity.
     * @param listener The listener to be called when this event is invoked.
     */
    @Override
    void register(EventListener<TArgs> listener);

    /**
     * Registers an event listener to this event with the given priority.
     * @param listener The listener to be called when this event is invoked.
     * @param priority The priority of the listener. Listeners are called in order from lowest to highest priority.
     */
    default void register(ArglessEventListener listener, double priority)
    { register(x -> listener.onEvent(), priority); }

    /**
     * Registers an event listener to this event.
     *
     * Listeners registered without a priority can be considered to have a priority of negative infinity.
     * @param listener The listener to be called when this event is invoked.
     */
    @Override
    default void register(ArglessEventListener listener)
    { register(x -> listener.onEvent()); }
}
