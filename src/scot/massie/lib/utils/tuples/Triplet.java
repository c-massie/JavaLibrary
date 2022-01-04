package scot.massie.lib.utils.tuples;

import java.util.Objects;

public class Triplet<T1, T2, T3> extends Pair<T1, T2>
{
    protected final T3 third;

    public Triplet(T1 first, T2 second, T3 third)
    {
        super(first, second);
        this.third = third;
    }

    public T3 getThird()
    { return third; }

    @Override
    public String toString()
    { return "(" + first.toString() + ", " + second.toString() + ", " + third.toString() + ")"; }

    @Override
    public boolean equals(Object o)
    { return super.equals(o) && Objects.equals(third, ((Triplet<?, ?, ?>)o).third); }

    @Override
    public int hashCode()
    { return Objects.hash(super.hashCode(), third); }
}
