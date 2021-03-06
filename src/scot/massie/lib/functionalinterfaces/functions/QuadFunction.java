package scot.massie.lib.functionalinterfaces.functions;

/**
 * Functional interface accepting four arguments and returning a value.
 * @param <T1> The type of the first argument.
 * @param <T2> The type of the second argument.
 * @param <T3> The type of the third argument.
 * @param <T4> The type of the fourth argument.
 * @param <R> The type of the return value.
 */
@FunctionalInterface
public interface QuadFunction<T1, T2, T3, T4, R>
{
    /**
     * Applies this function to the given arguments and provides a result.
     * @param arg1 The first argument.
     * @param arg2 The second argument.
     * @param arg3 The third argument.
     * @param arg4 The fourth argument.
     * @return The result.
     */
    R apply(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
}
