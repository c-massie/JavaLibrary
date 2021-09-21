package scot.massie.lib.utils;

import scot.massie.lib.functionalinterfaces.Procedure;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utils and util functions for controlling the flow of code.
 */
public final class ControlFlowUtils
{
    private ControlFlowUtils()
    {}

    /**
     * Returns the first non-null argument, in a short-circuiting manner. Arguments are passed in suppliers, and
     * suppliers are only invoked if all previous suppliers have produced null.
     * @param operands The suppliers producing arguments that may or may not be null.
     * @param <T> The type of the arguments being coalesced.
     * @return The first passed argument in a supplier no equal to null, or null if all arguments evaluate to null.
     */
    @SafeVarargs
    public static <T> T nullCoalesce(Supplier<T>... operands)
    {
        for(Supplier<T> operand : operands)
        {
            T result = operand.get();

            if(result != null)
                return result;
        }

        return null;
    }

    public static <T, R> R getFromIf(T from,
                                     Predicate<T> ifStatement,
                                     Function<T, R> trueResultGetter,
                                     Function<T, R> falseResultGetter)
    { return ifStatement.test(from) ? trueResultGetter.apply(from) : falseResultGetter.apply(from); }

    public static <T, R> R getFromIf(T from,
                                     Predicate<T> ifStatement,
                                     Function<T, R> trueResultGetter,
                                     Supplier<R> falseResultGetter)
    { return ifStatement.test(from) ? trueResultGetter.apply(from) : falseResultGetter.get(); }

    public static <T, R> R getFromIf(T from,
                                     Predicate<T> ifStatement,
                                     Supplier<R> trueResultGetter,
                                     Function<T, R> falseResultGetter)
    { return ifStatement.test(from) ? trueResultGetter.get() : falseResultGetter.apply(from); }

    public static <T, R> R getFromIf(T from,
                                     Predicate<T> ifStatement,
                                     Supplier<R> trueResultGetter,
                                     Supplier<R> falseResultGetter)
    { return ifStatement.test(from) ? trueResultGetter.get() : falseResultGetter.get(); }

    public static <T, R> R getFromUnlessNull(T from, Function<T, R> notNullResultGetter, Supplier<R> nullResultGetter)
    { return from != null ? notNullResultGetter.apply(from) : nullResultGetter.get(); }

    public static <T, R> R getFromUnlessNull(T from, Function<T, R> notNullResultGetter)
    { return from != null ? notNullResultGetter.apply(from) : null; }

    public static <T, R> R getFromUnlessNull(T from, Supplier<R> notNullResultGetter, Supplier<R> nullResultGetter)
    { return from != null ? notNullResultGetter.get() : nullResultGetter.get(); }

    public static <T, R> R getFromUnlessNull(T from, Supplier<R> notNullResultGetter)
    { return from != null ? notNullResultGetter.get() : null; }

    public static <T> void ifVal(T val, Predicate<T> ifStatement, Consumer<T> trueAction, Consumer<T> falseAction)
    {
        if(ifStatement.test(val))
            trueAction.accept(val);
        else
            falseAction.accept(val);
    }

    public static <T> void ifVal(T val, Predicate<T> ifStatement, Consumer<T> trueAction, Procedure falseAction)
    {
        if(ifStatement.test(val))
            trueAction.accept(val);
        else
            falseAction.run();
    }

    public static <T> void ifVal(T val, Predicate<T> ifStatement, Consumer<T> trueAction)
    {
        if(ifStatement.test(val))
            trueAction.accept(val);
    }

    public static <T> void ifVal(T val, Predicate<T> ifStatement, Procedure trueAction, Consumer<T> falseAction)
    {
        if(ifStatement.test(val))
            trueAction.run();
        else
            falseAction.accept(val);
    }

    public static <T> void ifVal(T val, Predicate<T> ifStatement, Procedure trueAction, Procedure falseAction)
    {
        if(ifStatement.test(val))
            trueAction.run();
        else
            falseAction.run();
    }

    public static <T> void ifVal(T val, Predicate<T> ifStatement, Procedure trueAction)
    {
        if(ifStatement.test(val))
            trueAction.run();
    }

    public static <T> void ifNotNull(T val, Consumer<T> trueAction, Consumer<T> falseAction)
    {
        if(val != null)
            trueAction.accept(val);
        else
            falseAction.accept(val);
    }

    public static <T> void ifNotNull(T val, Consumer<T> trueAction, Procedure falseAction)
    {
        if(val != null)
            trueAction.accept(val);
        else
            falseAction.run();
    }

    public static <T> void ifNotNull(T val, Consumer<T> trueAction)
    {
        if(val != null)
            trueAction.accept(val);
    }

    public static <T> void ifNotNull(T val, Procedure trueAction, Consumer<T> falseAction)
    {
        if(val != null)
            trueAction.run();
        else
            falseAction.accept(val);
    }

    public static <T> void ifNotNull(T val, Procedure trueAction, Procedure falseAction)
    {
        if(val != null)
            trueAction.run();
        else
            falseAction.run();
    }

    public static <T> void ifNotNull(T val, Procedure trueAction)
    {
        if(val != null)
            trueAction.run();
    }
}
