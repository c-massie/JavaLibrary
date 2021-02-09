package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;

import java.util.*;
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
    protected final Map<Event<?>, Function<TArgs, ? extends EventArgs>> dependentEvents = new HashMap<>();

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void invoke(TArgs eventArgs)
    {
        if(listenerOrderMatters())
        {
            Iterator<EventListenerCallInfo<?>> listenerIterator = generateCallInfoAsStream(eventArgs)
                                                                          .sorted(Events.listenerCallInfoComparator)
                                                                          .iterator();

            while(listenerIterator.hasNext())
            {
                EventListenerCallInfo<?> i = listenerIterator.next();
                ((EventListener)i.getListener()).onEvent(i.getArgs());
            }
        }
        else
        {
            for(EventListener<TArgs> listener : listeners)
                listener.onEvent(eventArgs);

            for(Map.Entry<Event<?>, Function<TArgs, ? extends EventArgs>> dependentEvent : dependentEvents.entrySet())
            {
                // Return type of the function is guaranteed to be the same as the type accepted by event's invoke method.
                ((Event)dependentEvent.getKey()).invoke(dependentEvent.getValue().apply(eventArgs));
            }
        }
    }

    @Override
    public void register(EventListener<TArgs> listener)
    { listeners.add(listener); }

    @Override
    public void register(Event<TArgs> dependentEvent)
    { dependentEvents.put(dependentEvent, Function.identity()); }

    @Override
    public <TDependentArgs extends EventArgs> void register(Event<TDependentArgs> dependentEvent, Function<TArgs, TDependentArgs> argWrapper)
    { dependentEvents.put(dependentEvent, argWrapper); }

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
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    { return generateCallInfoAsStream(args).collect(Collectors.toList()); }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Stream<EventListenerCallInfo<?>> generateCallInfoAsStream(TArgs args)
    {
        Stream<EventListenerCallInfo<?>> result = listeners.stream().map(x -> new EventListenerCallInfo<>(x, args));

        for(Map.Entry<Event<?>, Function<TArgs, ? extends EventArgs>> e : dependentEvents.entrySet())
        {
            // Return type of the function is guaranteed to be the same as the type accepted by event's generateCallInfo
            // method.

            result = Stream.concat(result, ((Event)e.getKey()).generateCallInfoAsStream(e.getValue().apply(args)));
        }

        return result;
    }
}
