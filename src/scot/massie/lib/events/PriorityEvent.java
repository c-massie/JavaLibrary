package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;

/**
 * Generic event for calling arbitrary listeners in an order as defined by listener priority.
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
}
