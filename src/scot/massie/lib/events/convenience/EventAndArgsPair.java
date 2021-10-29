package scot.massie.lib.events.convenience;

import scot.massie.lib.events.InvokableEvent;
import scot.massie.lib.events.args.EventArgs;

import java.util.List;
import java.util.stream.Stream;

/**
 * <p>A pairing of an event and an event args object to pass into an invocation of the event.</p>
 *
 * <p>Exists because of Java's lack of support for named tuples.</p>
 */
public final class EventAndArgsPair<TArgs extends EventArgs>
{
    /**
     * The event in this pairing.
     */
    private final InvokableEvent<TArgs> event;

    /**
     * The eventargs object in this pairing.
     */
    private final TArgs args;

    /**
     * Creates a new pairing of an invokable event with an instance of its event args.
     * @param event The event.
     * @param args The eventargs object.
     */
    public EventAndArgsPair(InvokableEvent<TArgs> event, TArgs args)
    {
        this.event = event;
        this.args = args;
    }

    /**
     * Gets the event in this pairing.
     * @return The event in this pairing.
     */
    public InvokableEvent<TArgs> getEvent()
    { return event; }

    /**
     * Gets the eventargs object in this pairing.
     * @return The eventargs object in this pairing.
     */
    public TArgs getArgs()
    { return args; }

    /**
     * Generates a list of call infos for this pairing's event's listeners, given this pairing's eventargs.
     * @return A list of call info objects for the listeners of the event.
     */
    public List<EventListenerCallInfo<?>> generateCallInfo()
    { return event.generateCallInfo(args); }

    /**
     * Generates a steam of call infos for this pairing's event's listeners, given this pairing's eventargs.
     * @return A steam of call info objects for the listeners of the event.
     */
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream()
    { return event.generateCallInfoAsStream(args); }
}
