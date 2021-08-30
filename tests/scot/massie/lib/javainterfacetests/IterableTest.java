package scot.massie.lib.javainterfacetests;

import org.junit.jupiter.api.Test;
import scot.massie.lib.utils.wrappers.MutableWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class IterableTest<T, TIterable extends Iterable<T>>
{
    public abstract TIterable getEmptyIterable();

    public abstract TIterable getIterableWithSingleValue();

    public abstract TIterable getIterableWithValues();

    public abstract T getSingleTestValue();

    public abstract List<T> getTestValues();

    public abstract boolean areEqual(T a, T b);

    public Comparator<T> getEquator()
    { return (a, b) -> areEqual(a, b) ? 0 : -1; }

    @Test
    public void foreach_empty()
    {
        MutableWrapper<Integer> iterations = new MutableWrapper<>(0);
        getEmptyIterable().forEach(x -> iterations.set(iterations.get() + 1));
        assertThat(iterations.get()).isEqualTo(0);
    }

    @Test
    public void foreach_single()
    {
        MutableWrapper<Integer> iterations = new MutableWrapper<>(0);

        getIterableWithSingleValue().forEach(x ->
        {
            assertThat(x).isNotNull();
            assertThat(x).usingComparator(getEquator()).isEqualTo(getSingleTestValue());
            iterations.set(iterations.get() + 1);
        });

        assertThat(iterations.get()).isEqualTo(1);
    }

    @Test
    public void foreach_multiple()
    {
        List<T> expectedValues = getTestValues();
        List<T> actualValues = new ArrayList<>();
        getIterableWithValues().forEach(actualValues::add);
        assertThat(actualValues).usingElementComparator(getEquator())
                                .containsExactlyInAnyOrderElementsOf(expectedValues);
    }

    @Test
    public void iterator_empty()
    {
        Iterator<T> iterator = getEmptyIterable().iterator();
        assertThat(iterator.hasNext()).isFalse();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void iterator_single()
    {
        Iterator<T> iterator = getIterableWithSingleValue().iterator();

        assertThat(iterator.hasNext()).isTrue();
        T next = iterator.next();
        assertThat(next).isNotNull();
        assertThat(next).usingComparator(getEquator()).isEqualTo(getSingleTestValue());

        assertThat(iterator.hasNext()).isFalse();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void iterator_multiple()
    {
        List<T> expectedValues = getTestValues();
        List<T> actualValues = new ArrayList<>();

        Iterator<T> iterator = getIterableWithValues().iterator();

        while(iterator.hasNext())
            actualValues.add(iterator.next());

        assertThrows(NoSuchElementException.class, iterator::next);
        assertThat(actualValues).usingElementComparator(getEquator())
                                .containsExactlyInAnyOrderElementsOf(expectedValues);
    }
}
