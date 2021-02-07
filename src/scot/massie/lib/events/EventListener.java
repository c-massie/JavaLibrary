package scot.massie.lib.events;

import scot.massie.lib.events.args.EventArgs;

/**
 * Event listener that can be registered to an event, and is called when that event is invoked.
 * @param <TArgs> The type of the event args objects passed to this listener from any registered event.
 */
@FunctionalInterface
public interface EventListener<TArgs extends EventArgs>
{
    /**
     * The method called when the event this listener is registered to is invoked.
     * @param args The event args for the corresponding event invocation.
     */
    void onEvent(TArgs args);
}
