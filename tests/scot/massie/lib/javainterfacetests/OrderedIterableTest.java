package scot.massie.lib.javainterfacetests;

import org.junit.jupiter.api.Test;
import scot.massie.lib.utils.wrappers.MutableWrapper;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

public abstract class OrderedIterableTest<T, TIterable extends Iterable<T>> extends IterableTest<T, TIterable>
{
    @Test
    public void foreach_multiple()
    {
        List<T> expectedValues = getTestValues();
        MutableWrapper<Integer> i = new MutableWrapper<>(0);

        getIterableWithValues().forEach(x ->
        {
            assertThat(i.get()).isLessThan(expectedValues.size());
            assertThat(x).usingComparator(getEquator())
                         .describedAs("i = " + i.get())
                         .isEqualTo(expectedValues.get(i.get()));

            i.set(i.get() + 1);
        });

        assertThat(i.get()).isEqualTo(expectedValues.size());
    }

    @Test
    public void iterator_multiple()
    {
        List<T> expectedValues = getTestValues();
        Iterator<T> iterator = getIterableWithValues().iterator();
        int i = 0;

        while(i < expectedValues.size())
        {
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).usingComparator(getEquator()).isEqualTo(expectedValues.get(i));

            i++;
        }

        assertThat(iterator.hasNext()).isFalse();
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}
