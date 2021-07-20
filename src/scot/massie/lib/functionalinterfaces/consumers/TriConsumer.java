package scot.massie.lib.functionalinterfaces.consumers;

/**
 * Functional interface accepting three arguments and returning no value.
 * @param <T1> The type of the first argument.
 * @param <T2> The type of the second argument.
 * @param <T3> The type of the third argument.
 */
@FunctionalInterface
public interface TriConsumer<T1, T2, T3>
{
    /**
     * Runs this consumer with the given arguments.
     * @param arg1 The first argument.
     * @param arg2 The second argument.
     * @param arg3 The third argument.
     */
    void accept(T1 arg1, T2 arg2, T3 arg3);
}
