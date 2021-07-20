package scot.massie.lib.functionalinterfaces.functions;

/**
 * Functional interface accepting three arguments and returning a value.
 * @param <T1> The type of the first argument.
 * @param <T2> The type of the second argument.
 * @param <T3> The type of the third argument.
 * @param <R> The type of the return value.
 */
@FunctionalInterface
public interface TriFunction<T1, T2, T3, R>
{
    /**
     * Applies this function to the given arguments and provides a result.
     * @param arg1 The first argument.
     * @param arg2 The second argument.
     * @param arg3 The third argument.
     * @return The result.
     */
    R apply(T1 arg1, T2 arg2, T3 arg3);
}
