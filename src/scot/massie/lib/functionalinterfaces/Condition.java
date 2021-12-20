package scot.massie.lib.functionalinterfaces;

/**
 * Functional interface representing a zero-argument predicate, for grabbing a boolean value from elsewhere.
 */
@FunctionalInterface
public interface Condition
{
    /**
     * Evaluates this condition.
     * @return The result of this condition.
     */
    boolean test();

    /**
     * Returns a composed condition that represents a short-circuiting logical AND of this condition and another. When
     * evaluating the composed condition, if this condition is false, then the other condition is not evaluated.
     * @param other A condition that will be logically-ANDed with this predicate.
     * @return A composed condition that represents the short-circuiting logical AND of this condition and the other
     *         condition.
     */
    default Condition and(Condition other)
    {
        if(other == null)
            throw new NullPointerException("Other condition cannot be null.");

        return () -> test() && other.test();
    }

    /**
     * Returns a composed condition that represents a short-circuiting logical OR of this condition and another. When
     * evaluating the composed condition, if this condition is true, then the other condition is not evaluated.
     * @param other A condition that will be logically-ORed with this condition.
     * @return A composed condition that represents the short-circuiting logical OR of this condition and the other
     *         condition.
     */
    default Condition or(Condition other)
    {
        if(other == null)
            throw new NullPointerException("Other condition cannot be null.");

        return () -> test() || other.test();
    }

    /**
     * Returns a condition that represents the logical negation of this condition.
     * @return A condition that represents the logical negation of this condition.
     */
    default Condition negate()
    { return () -> !test(); }
}
