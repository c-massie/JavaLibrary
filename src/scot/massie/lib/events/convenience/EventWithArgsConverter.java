package scot.massie.lib.events.convenience;

import scot.massie.lib.events.Event;
import scot.massie.lib.events.args.EventArgs;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A pairing of an event and a function for converting a different event's EventArgs object into an instant of this
 * event's EventArgs type.
 *
 * Exists because of Java's lack of support for named tuples.
 * @param <TInvokerArgs> The type of the event args that should be able to be converted into an instance of this event's
 *                      EventArgs type.
 * @param <TInvokedArgs> The EventArgs type used by the contained object.
 */
public final class EventWithArgsConverter<TInvokerArgs extends EventArgs, TInvokedArgs extends EventArgs>
{
    public EventWithArgsConverter(Event<TInvokedArgs> event, Function<TInvokerArgs, TInvokedArgs> conversion)
    {
        this.event = event;
        this.conversion = conversion;
    }

    private final Event<TInvokedArgs> event;
    private final Function<TInvokerArgs, TInvokedArgs> conversion;

    public Event<TInvokedArgs> getEvent()
    { return event; }

    public Function<TInvokerArgs, TInvokedArgs> getConversion()
    { return conversion; }

    public TInvokedArgs getConvertedArgs(TInvokerArgs args)
    { return conversion.apply(args); }

    public void invokeEvent(TInvokerArgs args)
    { event.invoke(conversion.apply(args)); }

    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TInvokerArgs args)
    { return event.generateCallInfoAsStream(conversion.apply(args)); }
}
