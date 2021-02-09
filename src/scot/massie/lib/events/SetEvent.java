package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.events.convenience.EventListenerCallInfo;

import java.util.*;
import java.util.function.Function;

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
            List<EventListenerCallInfo<?>> listenersWithCallInfo = generateCallInfo(eventArgs);
            listenersWithCallInfo.sort(Events.listenerCallInfoComparator);

            for(EventListenerCallInfo<?> i : listenersWithCallInfo)
            {
                // i.getListener().onEvent(...) is guaranteed to accept the same type as is returned by i.getArgs()
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
                ((Event) dependentEvent.getKey()).invoke(dependentEvent.getValue().apply(eventArgs));
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<EventListenerCallInfo<?>> generateCallInfo(TArgs args)
    {
        List<EventListenerCallInfo<?>> result = new ArrayList<>();

        for(EventListener<TArgs> el : listeners)
            result.add(new EventListenerCallInfo<>(el, args));

        for(Map.Entry<Event<?>, Function<TArgs, ? extends EventArgs>> e : dependentEvents.entrySet())
        {
            // Return type of the function is guaranteed to be the same as the type accepted by event's generateCallInfo method.
            result.addAll(((Event)e.getKey()).generateCallInfo(e.getValue().apply(args)));
        }

        return result;
    }
}
