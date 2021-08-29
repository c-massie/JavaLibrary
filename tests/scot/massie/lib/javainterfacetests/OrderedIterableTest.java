package scot.massie.lib.javainterfacetests;

import org.junit.jupiter.api.Test;
import scot.massie.lib.utils.wrappers.MutableWrapper;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

public abstract class OrderedIterableTest<T, TIterable extends Iterable<T>>
{
    abstract TIterable getEmptyIterable();

    abstract TIterable getIterableWithSingleValue();

    abstract TIterable getIterableWithValues();

    abstract T getSingleTestValue();

    abstract List<T> getTestValues();

    @Test
    void foreach_empty()
    {
        MutableWrapper<Integer> iterations = new MutableWrapper<>(0);
        getEmptyIterable().forEach(x -> iterations.set(iterations.get() + 1));
        assertThat(iterations.get()).isEqualTo(0);
    }

    @Test
    void foreach_single()
    {
        MutableWrapper<Integer> iterations = new MutableWrapper<>(0);

        getIterableWithSingleValue().forEach(x ->
        {
            assertThat(x).isEqualTo(getSingleTestValue());
            iterations.set(iterations.get() + 1);
        });

        assertThat(iterations.get()).isEqualTo(1);
    }

    @Test
    void foreach_multiple()
    {
        List<T> expectedValues = getTestValues();
        MutableWrapper<Integer> i = new MutableWrapper<>(0);

        getIterableWithValues().forEach(x ->
        {
            assertThat(i.get()).isLessThan(expectedValues.size());
            assertThat(x).isEqualTo(expectedValues.get(i.get()));

            i.set(i.get() + 1);
        });

        assertThat(i.get()).isEqualTo(expectedValues.size());
    }

    @Test
    void iterator_empty()
    {
        Iterator<T> iterator = getEmptyIterable().iterator();
        assertThat(iterator.hasNext()).isFalse();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iterator_single()
    {
        Iterator<T> iterator = getIterableWithSingleValue().iterator();

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(getSingleTestValue());

        assertThat(iterator.hasNext()).isFalse();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iterator_multiple()
    {
        List<T> expectedValues = getTestValues();
        Iterator<T> iterator = getIterableWithValues().iterator();
        int i = 0;

        while(i < expectedValues.size())
        {
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo(expectedValues.get(i));

            i++;
        }

        assertThat(iterator.hasNext()).isFalse();
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}
