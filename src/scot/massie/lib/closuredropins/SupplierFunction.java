package scot.massie.lib.closuredropins;

import java.util.function.Function;
import java.util.function.Supplier;

public class SupplierFunction<T, R> implements Function<T, R>
{
    final Supplier<R> storedSupplier;

    public SupplierFunction(Supplier<R> supplier)
    { this.storedSupplier = supplier; }

    @Override
    public R apply(T t)
    { return storedSupplier.get(); }
}
