package scot.massie.lib.closuredropins;

import java.util.function.Supplier;

public class ValueSupplier<T> implements Supplier<T>
{
    T valueToSupply;

    public ValueSupplier(T valueToSupply)
    { this.valueToSupply = valueToSupply; }

    @Override
    public T get()
    { return valueToSupply; }
}
