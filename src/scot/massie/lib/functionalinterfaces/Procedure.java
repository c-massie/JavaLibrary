package scot.massie.lib.functionalinterfaces;

/**
 * Functional interface for functions accepting no arguments and returning no values. Does not imply the use of threads
 * in the way that {@link Runnable} does.
 */
@FunctionalInterface
public interface Procedure
{
    /**
     * Runs this procedure.
     */
    void run();
}
