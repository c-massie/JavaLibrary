package scot.massie.lib.closuredropins;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wrapper that allows a supplier to be used in place of a function, ignoring the value passed in.
 * @param <T> The type of the value passed in and ignored.
 * @param <R> The type of the value being supplied.
 */
public class SupplierFunction<T, R> implements Function<T, R>
{
    /**
     * The supplier used to provide results.
     */
    final Supplier<R> storedSupplier;

    /**
     * Creates a new SupplierFunction, using the given supplier as a source of results when {@link #apply(Object)} is
     * called.
     * @param supplier The supplier to provide values.
     */
    public SupplierFunction(Supplier<R> supplier)
    { this.storedSupplier = supplier; }

    @Override
    public R apply(T t)
    { return storedSupplier.get(); }
}
