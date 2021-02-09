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
    protected final Set<EventListener<TArgs>> listenersWithoutPriority = new HashSet<>();
    protected final List<EventListenerPriorityPair<TArgs>> listenersWithPriority = new ArrayList<>();
    protected final Map<Event<?>, EventWithArgsConverter<TArgs, ?>> dependentEvents = new HashMap<>();

    @Override
    public void invoke(TArgs eventArgs)
    {
        if(listenerOrderMatters())
        {
            if(dependentEvents.isEmpty())
            {
                for(EventListener<TArgs> listener : listenersWithoutPriority)
                    listener.onEvent(eventArgs);

                for(EventListenerPriorityPair<TArgs> elpp : listenersWithPriority)
                    elpp.getListener().onEvent(eventArgs);

                return;
            }

            generateCallInfoAsStream(eventArgs).sorted(Events.listenerCallInfoComparator)
                                               .forEachOrdered(EventListenerCallInfo::callListener);
        }
        else
        {
            for(EventListener<TArgs> listener : listenersWithoutPriority)
                listener.onEvent(eventArgs);

            // If listener order doesn't matter, there are no listeners with priority.

            for(EventWithArgsConverter<TArgs, ?> ewac : dependentEvents.values())
                ewac.invokeEvent(eventArgs);
        }
    }

    @Override
    public void register(EventListener<TArgs> listener)
    { listenersWithoutPriority.add(listener); }

    @Override
    public void register(EventListener<TArgs> listener, double priority)
    {
        EventListenerPriorityPair<TArgs> elpp = new EventListenerPriorityPair<>(listener, priority);
        listenersWithPriority.add(Collections.binarySearch(listenersWithPriority, elpp), elpp);
    }

    @Override
    public void register(Event<TArgs> dependentEvent)
    { dependentEvents.put(dependentEvent, new EventWithArgsConverter<>(dependentEvent, Function.identity())); }

    @Override
    public <TDependentArgs extends EventArgs> void register(Event<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
    { dependentEvents.put(dependentEvent, new EventWithArgsConverter<>(dependentEvent, argWrapper)); }

    @Override
    public void deregister(EventListener<TArgs> listener)
    {
        listenersWithoutPriority.remove(listener);
        listenersWithPriority.removeIf(x -> x.getListener() == listener);
    }

    @Override
    public void deregister(Event<?> event)
    { dependentEvents.remove(event); }

    @Override
    public void clearListeners()
    {
        listenersWithoutPriority.clear();
        listenersWithPriority.clear();
    }

    @Override
    public void clearDependentEvents()
    { dependentEvents.clear(); }

    @Override
    public void clear()
    {
        listenersWithoutPriority.clear();
        listenersWithPriority.clear();
        dependentEvents.clear();
    }

    @Override
    public boolean listenerOrderMatters()
    {
        if(!listenersWithPriority.isEmpty())
            return true;

        for(Event<?> e : dependentEvents.keySet())
            if(e.listenerOrderMatters())
                return true;

        return false;
    }

    @Override
    public Collection<EventListener<TArgs>> getListeners()
    {
        return Stream.concat(listenersWithoutPriority.stream(),
                             listenersWithPriority.stream().map(EventListenerPriorityPair::getListener))
                     .collect(Collectors.toList());
    }

    @Override
    public List<EventListenerPriorityPair<TArgs>> getListenersWithPriorities()
    {
        return Stream.concat(listenersWithoutPriority.stream().map(EventListenerPriorityPair::new),
                             listenersWithPriority.stream())
                     .collect(Collectors.toList());
    }

    @Override
    public Collection<Event<?>> getDependentEvents()
    { return dependentEvents.keySet(); }

    @Override
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    { return generateCallInfoAsStream(args).collect(Collectors.toList()); }

    @Override
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args)
    {
        return Stream.of(listenersWithoutPriority.stream().map(x -> new EventListenerCallInfo<>(x, args)),
                         listenersWithPriority.stream().map(x -> x.toCallInfo(args)),
                         dependentEvents.values().stream().flatMap(x -> x.generateCallInfoAsStream(args)))
                     .flatMap(Function.identity());
    }
}
