package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;
import scot.massie.lib.events.convenience.EventListenerPriorityPair;
import scot.massie.lib.events.convenience.EventWithArgsConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>An event implementation that invokes its listeners in a particular order, as dictated by listener priorities.</p>
 *
 * <p>Listeners without priority are invoked before listeners with priority.</p>
 *
 * <p>Stores listeners without priority in a set, stores listeners with priority in an ordered list.</p>
 * @param <TArgs> The event args type.
 */
public class OrderedEvent<TArgs extends EventArgs> implements InvokablePriorityEvent<TArgs>
{
    /**
     * Creates a new OrderedEvent with no listeners or dependent events.
     */
    public OrderedEvent()
    {}

    /**
     * The listeners to this event without a particular priority.
     */
    protected final Set<EventListener<TArgs>> listenersWithoutPriority = new HashSet<>();

    /**
     * The listeners to this event with a given priority. Invoked after priority-less listeners.
     */
    protected final List<EventListenerPriorityPair<TArgs>> listenersWithPriority = new ArrayList<>();

    /**
     * This event's dependants, along with converters for converting this event's eventargs objects to their own
     * eventargs objects.
     */
    protected final Map<InvokableEvent<?>, EventWithArgsConverter<TArgs, ?>> dependentEvents = new HashMap<>();

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
                for(InvokableEvent<?> e : dependentEvents.keySet())
                    if(e.listenerOrderMatters())
                        listenerOrderMatters = true;

            listenerStream = generateCallInfoAsStream(eventArgs);
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
        {
            int index = Collections.binarySearch(listenersWithPriority, elpp);

            if(index < 0)
                index = -(index + 1);

            listenersWithPriority.add(index, elpp);
        }
    }

    @Override
    public void register(InvokableEvent<TArgs> dependentEvent)
    {
        EventWithArgsConverter<TArgs, TArgs> ewac = new EventWithArgsConverter<>(dependentEvent, Function.identity());

        synchronized(listenersWithoutPriority)
        { dependentEvents.put(dependentEvent, ewac); }
    }

    @Override
    public <TDependentArgs extends EventArgs> void register(InvokableEvent<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
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
    public void deregister(InvokableEvent<?> event)
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

            for(InvokableEvent<?> e : dependentEvents.keySet())
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
    public Collection<InvokableEvent<?>> getDependentEvents()
    { synchronized(listenersWithoutPriority) { return dependentEvents.keySet(); } }

    @Override
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    { return generateCallInfoAsStream(args).collect(Collectors.toList()); }

    @Override
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args)
    {
        synchronized(listenersWithoutPriority)
        {
            return Stream.of(listenersWithoutPriority.stream().map(x -> new EventListenerCallInfo<>(x, args)),
                             listenersWithPriority   .stream().map(x -> x.toCallInfo(args)),
                             dependentEvents.values().stream().flatMap(x -> x.generateCallInfoAsStream(args)))
                         .flatMap(Function.identity());
        }
    }
}
