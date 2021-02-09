package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;
import scot.massie.lib.events.convenience.EventListenerPriorityPair;
import scot.massie.lib.events.convenience.EventWithArgsConverter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderedEvent<TArgs extends EventArgs> implements PriorityEvent<TArgs>
{
    /**
     * Creates a new OrderedEvent with no listeners or dependent events.
     */
    public OrderedEvent()
    {}

    protected final Set<EventListener<TArgs>> listenersWithoutPriority = new HashSet<>();
    protected final List<EventListenerPriorityPair<TArgs>> listenersWithPriority = new ArrayList<>();
    protected final Map<Event<?>, EventWithArgsConverter<TArgs, ?>> dependentEvents = new HashMap<>();

    // Synchronized on listenersWithoutPriority's lock, even where listenersWithPriority or dependentEvents is concerned

    @Override
    public void invoke(TArgs eventArgs)
    {
        boolean listenerOrderMatters = false;
        Stream<EventListenerCallInfo<?>> listenerStream;

        synchronized(listenersWithoutPriority)
        {
            if(!listenersWithPriority.isEmpty())
                listenerOrderMatters = true;
            else
                for(Event<?> e : dependentEvents.keySet())
                    if(e.listenerOrderMatters())
                        listenerOrderMatters = true;

            listenerStream = generateCallInfoAsStream_unthreadsafe(eventArgs);
        }

        if(listenerOrderMatters)
        {
            listenerStream.sorted(Events.listenerCallInfoComparator)
                          .forEachOrdered(EventListenerCallInfo::callListener);
        }
        else
            listenerStream.forEach(EventListenerCallInfo::callListener);
    }

    @Override
    public void register(EventListener<TArgs> listener)
    { synchronized(listenersWithoutPriority) { listenersWithoutPriority.add(listener); } }

    @Override
    public void register(EventListener<TArgs> listener, double priority)
    {
        EventListenerPriorityPair<TArgs> elpp = new EventListenerPriorityPair<>(listener, priority);

        synchronized(listenersWithoutPriority)
        { listenersWithPriority.add(Collections.binarySearch(listenersWithPriority, elpp), elpp); }
    }

    @Override
    public void register(Event<TArgs> dependentEvent)
    {
        EventWithArgsConverter<TArgs, TArgs> ewac = new EventWithArgsConverter<>(dependentEvent, Function.identity());

        synchronized(listenersWithoutPriority)
        { dependentEvents.put(dependentEvent, ewac); }
    }

    @Override
    public <TDependentArgs extends EventArgs> void register(Event<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
    {
        EventWithArgsConverter<TArgs, TDependentArgs> ewac = new EventWithArgsConverter<>(dependentEvent, argWrapper);

        synchronized(listenersWithoutPriority)
        { dependentEvents.put(dependentEvent, ewac); }
    }

    @Override
    public void deregister(EventListener<TArgs> listener)
    {
        synchronized(listenersWithoutPriority)
        {
            listenersWithoutPriority.remove(listener);
            listenersWithPriority.removeIf(x -> x.getListener() == listener);
        }
    }

    @Override
    public void deregister(Event<?> event)
    { synchronized(listenersWithoutPriority) { dependentEvents.remove(event); } }

    @Override
    public void clearListeners()
    {
        synchronized(listenersWithoutPriority)
        {
            listenersWithoutPriority.clear();
            listenersWithPriority.clear();
        }
    }

    @Override
    public void clearDependentEvents()
    { synchronized(listenersWithoutPriority) { dependentEvents.clear(); } }

    @Override
    public void clear()
    {
        synchronized(listenersWithoutPriority)
        {
            listenersWithoutPriority.clear();
            listenersWithPriority.clear();
            dependentEvents.clear();
        }
    }

    @Override
    public boolean listenerOrderMatters()
    {
        synchronized(listenersWithoutPriority)
        {
            if(!listenersWithPriority.isEmpty())
                return true;

            for(Event<?> e : dependentEvents.keySet())
                if(e.listenerOrderMatters())
                    return true;
        }

        return false;
    }

    @Override
    public Collection<EventListener<TArgs>> getListeners()
    {
        synchronized(listenersWithoutPriority)
        {
            return Stream.concat(listenersWithoutPriority.stream(),
                                 listenersWithPriority.stream().map(EventListenerPriorityPair::getListener))
                         .collect(Collectors.toList());
        }
    }

    @Override
    public List<EventListenerPriorityPair<TArgs>> getListenersWithPriorities()
    {
        synchronized(listenersWithoutPriority)
        {
            return Stream.concat(listenersWithoutPriority.stream().map(EventListenerPriorityPair::new),
                                 listenersWithPriority.stream())
                         .collect(Collectors.toList());
        }
    }

    @Override
    public Collection<Event<?>> getDependentEvents()
    { synchronized(listenersWithoutPriority) { return dependentEvents.keySet(); } }

    @Override
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    { return generateCallInfoAsStream(args).collect(Collectors.toList()); }

    private Stream<EventListenerCallInfo<?>> generateCallInfoAsStream_unthreadsafe(TArgs args)
    {
        return Stream.of(listenersWithoutPriority.stream().map(x -> new EventListenerCallInfo<>(x, args)),
                         listenersWithPriority.stream().map(x -> x.toCallInfo(args)),
                         dependentEvents.values().stream().flatMap(x -> x.generateCallInfoAsStream(args)))
                     .flatMap(Function.identity());
    }

    @Override
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args)
    { synchronized(listenersWithoutPriority) { return generateCallInfoAsStream_unthreadsafe(args); } }
}
