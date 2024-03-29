package scot.massie.lib.utils.tuples;

import java.util.Objects;

public class Pair<T1, T2>
{
    protected final T1 first;
    protected final T2 second;

    public Pair(T1 first, T2 second)
    {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst()
    { return first; }

    public T2 getSecond()
    { return second; }

    @Override
    public String toString()
    { return "(" + first.toString() + ", " + second.toString() + ")"; }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;

        if(o == null || getClass() != o.getClass())
            return false;

        Pair<?, ?> pair = (Pair<?, ?>)o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode()
    { return Objects.hash(first, second); }
}
