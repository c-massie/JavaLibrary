package scot.massie.lib.javainterfacetests;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
        List<T> actualValues = new ArrayList<>();
        getIterableWithValues().forEach(actualValues::add);
        assertThat(actualValues).usingElementComparator(getEquator())
                                .containsExactlyElementsOf(expectedValues);
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
                                .containsExactlyElementsOf(expectedValues);
    }
}
