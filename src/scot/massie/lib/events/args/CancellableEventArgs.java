package scot.massie.lib.events.args;

/**
 * Base type for all sets of event arguments, where the event is or may be cancellable by listeners.
 */
public interface CancellableEventArgs extends EventArgs
{
    /**
     * Gets whether or not the corresponding event has been marked as being cancelled by an earlier listener.
     * @return True if the event is to be cancelled. Otherwise, false.
     */
    boolean isCancelled();

    /**
     * Sets whether or not the corresponding event should be marked as being cancelled.
     * @param cancellationState True to mark the event as being cancelled, false to mark the event as being not
     *                          cancelled.
     */
    void setCancelled(boolean cancellationState);

    /**
     * Marks the event as being cancelled.
     */
    default void cancel()
    { setCancelled(true); }
}
