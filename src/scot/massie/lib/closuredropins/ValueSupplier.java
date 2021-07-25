package scot.massie.lib.closuredropins;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A supplier that only provides a specific given value.
 * @param <T> The type of the value to supply.
 */
public class ValueSupplier<T> implements Supplier<T>
{
    /**
     * A view of this ValueSupplier as a function.
     * @param <T2> The type of value accepted (and ignored) by this function.
     */
    public class ValueFunction<T2> implements Function<T2, T>
    {
        @Override
        public T apply(T2 t2)
        { return valueToSupply; }
    }

    /**
     * The value this supplier supplies.
     */
    protected final T valueToSupply;

    /**
     * Creates a new ValueSupplier.
     * @param valueToSupply The value this ValueSupplier should supply.
     */
    public ValueSupplier(T valueToSupply)
    { this.valueToSupply = valueToSupply; }

    @Override
    public T get()
    { return valueToSupply; }

    /**
     * Gets this supplier as a {@link Function} that returns this supplier's value.
     * @param <T2> The type of argument accepted by the function.
     * @return This supplier as a function.
     */
    public <T2> ValueFunction<T2> asFunction()
    { return new ValueFunction<>(); }
}
