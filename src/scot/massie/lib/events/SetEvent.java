package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;
import scot.massie.lib.events.convenience.EventWithArgsConverter;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Basic Event implementation.
 *
 * Stores listeners in an unordered fashion in a set.
 * @param <TArgs> The type of the EventArgs objects passed to invocations of this event.
 */
public class SetEvent<TArgs extends EventArgs> implements Event<TArgs>
{
    /**
     * Creates a new SetEvent with no listeners or dependent events.
     */
    public SetEvent()
    {}

    protected final Set<EventListener<TArgs>> listeners = new HashSet<>();
    protected final Map<Event<?>, EventWithArgsConverter<TArgs, ?>> dependentEvents = new HashMap<>();

    @Override
    public void invoke(TArgs eventArgs)
    {
        if(listenerOrderMatters())
        {
            generateCallInfoAsStream(eventArgs).sorted(Events.listenerCallInfoComparator)
                                               .forEachOrdered(EventListenerCallInfo::callListener);
        }
        else
        {
            for(EventListener<TArgs> listener : listeners)
                listener.onEvent(eventArgs);

            for(EventWithArgsConverter<TArgs, ?> ewac : dependentEvents.values())
                ewac.invokeEvent(eventArgs);
        }
    }

    @Override
    public void register(EventListener<TArgs> listener)
    { listeners.add(listener); }

    @Override
    public void register(Event<TArgs> dependentEvent)
    { dependentEvents.put(dependentEvent, new EventWithArgsConverter<>(dependentEvent, Function.identity())); }

    @Override
    public <TDependentArgs extends EventArgs> void register(Event<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
    { dependentEvents.put(dependentEvent, new EventWithArgsConverter<>(dependentEvent, argWrapper)); }

    @Override
    public void deregister(EventListener<TArgs> listener)
    { listeners.remove(listener); }

    @Override
    public void deregister(Event<?> event)
    { dependentEvents.remove(event); }

    @Override
    public void clearListeners()
    { listeners.clear(); }

    @Override
    public void clearDependentEvents()
    { dependentEvents.clear(); }

    @Override
    public void clear()
    {
        listeners.clear();
        dependentEvents.clear();
    }

    @Override
    public boolean listenerOrderMatters()
    {
        for(Event<?> e : dependentEvents.keySet())
            if(e.listenerOrderMatters())
                return true;

        return false;
    }

    @Override
    public Collection<EventListener<TArgs>> getListeners()
    { return new HashSet<>(listeners); }

    @Override
    public Collection<Event<?>> getDependentEvents()
    { return dependentEvents.values().stream().map(EventWithArgsConverter::getEvent).collect(Collectors.toList()); }

    @Override
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    { return generateCallInfoAsStream(args).collect(Collectors.toList()); }

    @Override
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args)
    {
        return Stream.concat(listeners.stream().map(x -> new EventListenerCallInfo<>(x, args)),
                             dependentEvents.values().stream().flatMap(x -> x.generateCallInfoAsStream(args)));
    }
}
