package scot.massie.lib.events.convenience;

import scot.massie.lib.events.InvokableEvent;
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
    /**
     * The event.
     */
    private final InvokableEvent<TInvokedArgs> event;

    /**
     * The function for converting another event's eventargs instances into an instance of {@link #event}'s eventargs.
     */
    private final Function<TInvokerArgs, TInvokedArgs> conversion;

    /**
     * Pairs an event with the function required to convert an instance of another event's eventargs to an instance of
     * it's own eventargs.
     * @param event The event.
     * @param conversion The function for converting from the other events' eventargs to the given one's eventargs.
     */
    public EventWithArgsConverter(InvokableEvent<TInvokedArgs> event, Function<TInvokerArgs, TInvokedArgs> conversion)
    {
        this.event = event;
        this.conversion = conversion;
    }

    /**
     * Gets the event.
     * @return The event.
     */
    public InvokableEvent<TInvokedArgs> getEvent()
    { return event; }

    /**
     * Gets the function for converting an eventargs instance from another event's eventargs type to the eventargs type
     * for the result of {@link #getEvent()}.
     * @return The converting function.
     */
    public Function<TInvokerArgs, TInvokedArgs> getConversion()
    { return conversion; }

    /**
     * Converts a given eventargs object using the contained converter into an eventargs object for the contained event.
     * @param args The eventargs object to convert.
     * @return An eventargs object suitable for the event contained in this pairing.
     */
    public TInvokedArgs getConvertedArgs(TInvokerArgs args)
    { return conversion.apply(args); }

    /**
     * Invokes the contained event given an eventargs object of another event, which will be converted appropriately
     * using the contained conversion function.
     * @param args Another event's eventargs object which will be passed by the result of {@link #getConversion()}.
     */
    public void invokeEvent(TInvokerArgs args)
    { event.invoke(conversion.apply(args)); }

    /**
     * Generates a steam of call infos from the contained event given another event's eventargs object, which will be
     * passed through the result of {@link #getConversion()} before being used in generating the stream.
     * @param args The other event's eventargs object.
     * @return A stream of event listener call infos, which may be used in invoking the contained event's listeners.
     */
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TInvokerArgs args)
    { return event.generateCallInfoAsStream(conversion.apply(args)); }
}
