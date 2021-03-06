package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;
import scot.massie.lib.events.convenience.EventWithArgsConverter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Basic Event implementation.
 *
 * Stores listeners in an unordered fashion in a set.
 * @param <TArgs> The type of the EventArgs objects passed to invocations of this event.
 */
public class SetEvent<TArgs extends EventArgs> implements InvokableEvent<TArgs>
{
    /**
     * Creates a new SetEvent with no listeners or dependent events.
     */
    public SetEvent()
    {}

    protected final Set<EventListener<TArgs>> listeners = new HashSet<>();
    protected final Map<InvokableEvent<?>, EventWithArgsConverter<TArgs, ?>> dependentEvents = new HashMap<>();

    // Synchronised on the listeners set, even where working with dependentEvents

    @Override
    public void invoke(TArgs eventArgs)
    {
        boolean listenerOrderMatters = false;
        Stream<EventListenerCallInfo<?>> listenerStream;

        synchronized(listeners)
        {
            for(InvokableEvent<?> e : dependentEvents.keySet())
                if(e.listenerOrderMatters())
                    listenerOrderMatters = true;

            listenerStream = generateCallInfoAsStream_unthreadsafe(eventArgs);
        }

        if(listenerOrderMatters)
            listenerStream.sorted(Events.listenerCallInfoComparator)
                          .forEachOrdered(EventListenerCallInfo::callListener);
        else
            listenerStream.forEach(EventListenerCallInfo::callListener);
    }

    @Override
    public void register(EventListener<TArgs> listener)
    { synchronized(listeners) { listeners.add(listener); } }

    @Override
    public void register(InvokableEvent<TArgs> dependentEvent)
    {
        EventWithArgsConverter<TArgs, TArgs> ewac = new EventWithArgsConverter<>(dependentEvent, Function.identity());

        synchronized(listeners)
        { dependentEvents.put(dependentEvent, ewac); }
    }

    @Override
    public <TDependentArgs extends EventArgs> void register(InvokableEvent<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
    {
        EventWithArgsConverter<TArgs, TDependentArgs> ewac = new EventWithArgsConverter<>(dependentEvent, argWrapper);

        synchronized(listeners)
        { dependentEvents.put(dependentEvent, ewac); }
    }

    @Override
    public void deregister(EventListener<TArgs> listener)
    { synchronized(listeners) { listeners.remove(listener); } }

    @Override
    public void deregister(InvokableEvent<?> event)
    { synchronized(listeners) { dependentEvents.remove(event); } }

    @Override
    public void clearListeners()
    { synchronized(listeners) { listeners.clear(); } }

    @Override
    public void clearDependentEvents()
    { synchronized(listeners) { dependentEvents.clear(); } }

    @Override
    public void clear()
    {
        synchronized(listeners)
        {
            listeners.clear();
            dependentEvents.clear();
        }
    }

    @Override
    public boolean listenerOrderMatters()
    {
        synchronized(listeners)
        {
            for(InvokableEvent<?> e : dependentEvents.keySet())
                if(e.listenerOrderMatters())
                    return true;
        }

        return false;
    }

    @Override
    public Collection<EventListener<TArgs>> getListeners()
    { synchronized(listeners) { return new HashSet<>(listeners); } }

    @Override
    public Collection<InvokableEvent<?>> getDependentEvents()
    {
        synchronized(listeners)
        { return dependentEvents.values().stream().map(EventWithArgsConverter::getEvent).collect(Collectors.toList()); }
    }

    @Override
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    { return generateCallInfoAsStream(args).collect(Collectors.toList()); }

    private Stream<EventListenerCallInfo<?>> generateCallInfoAsStream_unthreadsafe(TArgs args)
    {
        return Stream.concat(listeners.stream().map(x -> new EventListenerCallInfo<>(x, args)),
                             dependentEvents.values().stream().flatMap(x -> x.generateCallInfoAsStream(args)));
    }

    @Override
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args)
    { synchronized(listeners) { return generateCallInfoAsStream_unthreadsafe(args); } }
}
