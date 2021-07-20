package scot.massie.lib.functionalinterfaces.consumers;

/**
 * Functional interface accepting four arguments and returning no value.
 * @param <T1> The type of the first argument.
 * @param <T2> The type of the second argument.
 * @param <T3> The type of the third argument.
 * @param <T4> The type of the fourth argument.
 */
@FunctionalInterface
public interface QuadConsumer<T1, T2, T3, T4>
{
    /**
     * Runs this consumer with the given arguments.
     * @param arg1 The first argument.
     * @param arg2 The second argument.
     * @param arg3 The third argument.
     * @param arg4 The fourth argument.
     */
    void accept(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
}
