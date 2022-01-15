package scot.massie.lib.events;

/**
 * Event listener that can be registered to an event, and is called when that event is invoked.
 */
public interface ArglessEventListener
{
    /**
     * The method called when the event this listener is registered to is invoked.
     */
    void onEvent();
}
