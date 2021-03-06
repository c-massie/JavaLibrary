package scot.massie.lib.events.convenience;

import scot.massie.lib.events.InvokableEvent;
import scot.massie.lib.events.args.EventArgs;

import java.util.List;
import java.util.stream.Stream;

/**
 * A pairing of an event and an event args object to pass into an invocation of the event.
 *
 * Exists because of Java's lack of support for named tuples.
 */
public final class EventAndArgsPair<TArgs extends EventArgs>
{
    public EventAndArgsPair(InvokableEvent<TArgs> event, TArgs args)
    {
        this.event = event;
        this.args = args;
    }

    private final InvokableEvent<TArgs> event;
    private final TArgs args;

    public InvokableEvent<TArgs> getEvent()
    { return event; }

    public TArgs getArgs()
    { return args; }

    public List<EventListenerCallInfo<?>> generateCallInfo()
    { return event.generateCallInfo(args); }

    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream()
    { return event.generateCallInfoAsStream(args); }
}
