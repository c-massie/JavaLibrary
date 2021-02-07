package scot.massie.lib.events.convenience;

import scot.massie.lib.events.Event;
import scot.massie.lib.events.args.EventArgs;

/**
 * A pairing of an event and an event args object to pass into an invocation of the event.
 *
 * Exists because of Java's lack of support for named tuples.
 */
public final class EventAndArgsPair<TArgs extends EventArgs>
{
    public EventAndArgsPair(Event<TArgs> event, TArgs args)
    {
        this.event = event;
        this.args = args;
    }

    private final Event<TArgs> event;
    private final TArgs args;

    public Event<TArgs> getEvent()
    { return event; }

    public TArgs getArgs()
    { return args; }
}
