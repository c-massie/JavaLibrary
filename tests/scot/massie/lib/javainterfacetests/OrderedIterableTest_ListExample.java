package scot.massie.lib.javainterfacetests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OrderedIterableTest_ListExample<T, TIterable extends Iterable<T>>
        extends OrderedIterableTest<String, List<String>>
{
    @Override
    public boolean areEqual(String a, String b)
    { return Objects.equals(a, b); }

    @Override
    public List<String> getEmptyIterable()
    { return new ArrayList<>(); }

    @Override
    public List<String> getIterableWithSingleValue()
    { return new ArrayList<>(Collections.singleton(getSingleTestValue())); }

    @Override
    public List<String> getIterableWithValues()
    { return new ArrayList<>(getTestValues()); }

    @Override
    public String getSingleTestValue()
    { return "onlyvalue"; }

    @Override
    public List<String> getTestValues()
    { return Arrays.asList("first", "second", "third", "fourth", "fifth"); }
}
