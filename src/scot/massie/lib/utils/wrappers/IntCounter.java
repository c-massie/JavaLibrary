package scot.massie.lib.utils.wrappers;

/**
 * An integer wrapper with convenience methods for counting. (e.g. incrementing and decrementing)
 */
public class IntCounter extends MutableWrapper<Integer>
{
    /**
     * Creates a new counter starting at the given number.
     * @param n The number to start at.
     */
    public IntCounter(Integer n)
    { super(n); }

    /**
     * Creates a new counter starting at the 0.
     */
    public IntCounter()
    { this(0); }

    /**
     * Adds 1 to the contained number.
     * @return The value contained within this counter after being incremented.
     */
    public int increment()
    { return this.item = this.item + 1; }

    /**
     * Adds 1 to the contained number.
     * @return The value contained within this counter after being incremented.
     */
    public int incr()
    { return this.item = this.item + 1; }

    /**
     * Subtracts 1 from the contained number.
     * @return The value contained within this counter after being decremented.
     */
    public int decrement()
    { return this.item = this.item - 1; }

    /**
     * Subtracts 1 from the contained number.
     * @return The value contained within this counter after being decremented.
     */
    public int decr()
    { return this.item = this.item - 1; }

    /**
     * Adds the given amount to the contained number.
     * @return The value contained within this counter after being incremented.
     */
    public int plus(int n)
    { return this.item = this.item + n; }

    /**
     * Subtracts the given number from the contained number.
     * @return The value contained within this counter after being decremented.
     */
    public int minus(int n)
    { return this.item = this.item - n; }
}
