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
     * This event's listeners.
     * @implNote Synchronisation is on this, the listeners set, even when working with dependent events.
     */
    protected final Set<EventListener<TArgs>> listeners = new HashSet<>();

    /**
     * This event's dependants, with converters for instances of this event's eventargs objects to their eventargs
     * objects.
     */
    protected final Map<InvokableEvent<?>, EventWithArgsConverter<TArgs, ?>> dependentEvents = new HashMap<>();

    /**
     * The synchronisation lock for this object.
     */
    protected final Object syncLock = new Object();

    /**
     * Creates a new SetEvent with no listeners or dependent events.
     */
    public SetEvent()
    {}

    @Override
    public void invoke(TArgs eventArgs)
    {
        boolean listenerOrderMatters = false;
        Stream<EventListenerCallInfo<?>> listenerStream;

        synchronized(syncLock)
        {
            for(InvokableEvent<?> e : dependentEvents.keySet())
                if(e.listenerOrderMatters())
                    listenerOrderMatters = true;

            listenerStream = generateCallInfoAsStream(eventArgs);
        }

        if(listenerOrderMatters)
            listenerStream.sorted(Events.listenerCallInfoComparator)
                          .forEachOrdered(EventListenerCallInfo::callListener);
        else
            listenerStream.forEach(EventListenerCallInfo::callListener);
    }

    @Override
    public void register(EventListener<TArgs> listener)
    { synchronized(syncLock) { listeners.add(listener); } }

    @Override
    public void register(InvokableEvent<TArgs> dependentEvent)
    {
        EventWithArgsConverter<TArgs, TArgs> ewac = new EventWithArgsConverter<>(dependentEvent, Function.identity());

        synchronized(syncLock)
        {
            if(dependentEvent.hasDependentEventRecursively(this))
                throw new IllegalArgumentException("Events may not be registered to events that are registered to "
                                                   + "them.");

            dependentEvents.put(dependentEvent, ewac);
        }
    }

    @Override
    public <TDependentArgs extends EventArgs> void register(InvokableEvent<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
    {
        EventWithArgsConverter<TArgs, TDependentArgs> ewac = new EventWithArgsConverter<>(dependentEvent, argWrapper);

        synchronized(syncLock)
        { dependentEvents.put(dependentEvent, ewac); }
    }

    @Override
    public void deregister(EventListener<TArgs> listener)
    { synchronized(syncLock) { listeners.remove(listener); } }

    @Override
    public void deregister(InvokableEvent<?> event)
    { synchronized(syncLock) { dependentEvents.remove(event); } }

    @Override
    public void clearListeners()
    { synchronized(syncLock) { listeners.clear(); } }

    @Override
    public void clearDependentEvents()
    { synchronized(syncLock) { dependentEvents.clear(); } }

    @Override
    public void clear()
    {
        synchronized(syncLock)
        {
            listeners.clear();
            dependentEvents.clear();
        }
    }

    @Override
    public boolean listenerOrderMatters()
    {
        synchronized(syncLock)
        {
            for(InvokableEvent<?> e : dependentEvents.keySet())
                if(e.listenerOrderMatters())
                    return true;
        }

        return false;
    }

    @Override
    public Collection<EventListener<TArgs>> getListeners()
    { synchronized(syncLock) { return new HashSet<>(listeners); } }

    @Override
    public Collection<InvokableEvent<?>> getDependentEvents()
    { synchronized(syncLock) { return new HashSet<>(dependentEvents.keySet()); } }

    @Override
    public boolean hasDependentEventRecursively(InvokableEvent<?> other)
    {
        synchronized(syncLock)
        {
            for(InvokableEvent<?> e : dependentEvents.keySet())
                if(e == other || e.hasDependentEventRecursively(other))
                    return true;
        }

        return false;
    }

    @Override
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    { return generateCallInfoAsStream(args).collect(Collectors.toList()); }

    @Override
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args)
    {
        synchronized(syncLock)
        {
            return Stream.concat(listeners.stream().map(x -> new EventListenerCallInfo<>(x, args)),
                                 dependentEvents.values().stream().flatMap(x -> x.generateCallInfoAsStream(args)));
        }
    }
}
