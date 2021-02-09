package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventAndArgsPair;
import scot.massie.lib.events.convenience.EventListenerCallInfo;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Static methods pertaining to generic events.
 */
public class Events
{
    private Events()
    {}

    public static final Comparator<EventListenerCallInfo<?>> listenerCallInfoComparator = (a, b) ->
    {
        if(!a.hasPriority())
            return b.hasPriority() ? -1 : 0;
        else if(!b.hasPriority())
            return 1;

        return Double.compare(a.getPriority(), b.getPriority());
    };

    /**
     * Invokes the passed event, with the provided EventArgs object. Will invoke any dependent events concurrently.
     *
     * Gets all event listeners with the information required to call them from the provided event, and calls each of
     * them using the provided information, in the correct order as specified by listener priority, if applicable.
     * @param event The event to invoke.
     * @param args The EventArgs object to pass to listeners of this event.
     * @param <TArgs> The type of the EventArgs object passed to listeners of this event.
     */
    public static <TArgs extends EventArgs> void invokeEvent(Event<TArgs> event, TArgs args)
    {
        event.generateCallInfoAsStream(args)
             .sorted(Events.listenerCallInfoComparator)
             .forEachOrdered(EventListenerCallInfo::callListener);
    }

    /**
     * Invokes the events in the passed Event/EventArgs pairs with the paired EventArgs objects concurrently. Will
     * invoke any dependent events concurrently.
     *
     * Gets all event listeners with the information required to call them from all events passed, and calls each of
     * them using the provided information, in the correct order as specified by listener priority, if applicable.
     * @param eventsAndArgs A collection of events to invoke, paired with the event args objects to pass to the
     *                      listeners of the corresponding event.
     */
    public static void invokeEvents(Collection<EventAndArgsPair<?>> eventsAndArgs)
    {
        eventsAndArgs.stream()
                     .flatMap(EventAndArgsPair::generateCallInfoAsStream)
                     .sorted(listenerCallInfoComparator)
                     .forEachOrdered(EventListenerCallInfo::callListener);
    }

    /**
     * Invokes the events in the passed Event/EventArgs pairs with the paired EventArgs objects concurrently. Will
     * invoke any dependent events concurrently.
     *
     * Gets all event listeners with the information required to call them from all events passed, and calls each of
     * them using the provided information, in the correct order as specified by listener priority, if applicable.
     * @param eventsAndArgs An array of events to invoke, paired with the event args objects to pass to the listeners of
     *                      the corresponding event.
     */
    public static void invokeEvents(EventAndArgsPair<?>... eventsAndArgs)
    {
        Stream.of(eventsAndArgs)
              .flatMap(EventAndArgsPair::generateCallInfoAsStream)
              .sorted(listenerCallInfoComparator)
              .forEachOrdered(EventListenerCallInfo::callListener);
    }
}
