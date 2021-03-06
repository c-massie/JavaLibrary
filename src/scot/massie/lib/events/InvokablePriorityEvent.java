package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerPriorityPair;

import java.util.List;

/**
 * Generic event for calling arbitrary listeners in an order as defined by listener priority.
 * @param <TArgs> The type of the event args objects passed to listeners of this event.
 */
public interface InvokablePriorityEvent<TArgs extends EventArgs> extends InvokableEvent<TArgs>, PriorityEvent<TArgs>
{
    /**
     * Gets the event listeners registered to this event. Listeners not registered with a priority will be said to have
     * a priority of negative infinity.
     * @return A list of the event listeners registered to this event, paired with the priorities they were registered
     * with, in order of priority from lowest to highest. Where an event listener isn't registered with an event, it
     * will be paired with negative infinity.
     */
    List<EventListenerPriorityPair<TArgs>> getListenersWithPriorities();
}
