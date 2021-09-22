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

    /**
     * Derives a value from a given value where a predicate is satisfied.
     * @param from The value to derive a value from.
     * @param ifStatement The predicate to satisfy.
     * @param trueResultGetter The function to derive a value from the given value if the predicate is satisfied.
     * @param falseResultGetter The function to derive a value from the given value if the predicate is not satisfied.
     * @param <T> The type of the value a result is being derived from.
     * @param <R> The type of the resulting value.
     * @return The result of the appropriate getter.
     */
    public static <T, R> R getFromIf(T from,
                                     Predicate<T> ifStatement,
                                     Function<T, R> trueResultGetter,
                                     Function<T, R> falseResultGetter)
    { return ifStatement.test(from) ? trueResultGetter.apply(from) : falseResultGetter.apply(from); }

    /**
     * Derives a value from a given value where a predicate is satisfied.
     * @param from The value to derive a value from.
     * @param ifStatement The predicate to satisfy.
     * @param trueResultGetter The function to derive a value from the given value if the predicate is satisfied.
     * @param falseResultGetter The function to produce a value if the predicate is not satisfied.
     * @param <T> The type of the value a result is being derived from.
     * @param <R> The type of the resulting value.
     * @return The result of the appropriate getter.
     */
    public static <T, R> R getFromIf(T from,
                                     Predicate<T> ifStatement,
                                     Function<T, R> trueResultGetter,
                                     Supplier<R> falseResultGetter)
    { return ifStatement.test(from) ? trueResultGetter.apply(from) : falseResultGetter.get(); }

    /**
     * Derives a value from a given value where a predicate is satisfied.
     * @param from The value to derive a value from.
     * @param ifStatement The predicate to satisfy.
     * @param trueResultGetter The function to produce a value if the predicate is satisfied.
     * @param falseResultGetter The function to derive a value from the given value if the predicate is not satisfied.
     * @param <T> The type of the value a result is being derived from.
     * @param <R> The type of the resulting value.
     * @return The result of the appropriate getter.
     */
    public static <T, R> R getFromIf(T from,
                                     Predicate<T> ifStatement,
                                     Supplier<R> trueResultGetter,
                                     Function<T, R> falseResultGetter)
    { return ifStatement.test(from) ? trueResultGetter.get() : falseResultGetter.apply(from); }

    /**
     * Derives a value from a given value where a predicate is satisfied.
     * @param from The value to derive a value from.
     * @param ifStatement The predicate to satisfy.
     * @param trueResultGetter The function to produce a value if the predicate is satisfied.
     * @param falseResultGetter The function to produce a value if the predicate is not satisfied.
     * @param <T> The type of the value a result is being derived from.
     * @param <R> The type of the resulting value.
     * @return The result of the appropriate getter.
     */
    public static <T, R> R getFromIf(T from,
                                     Predicate<T> ifStatement,
                                     Supplier<R> trueResultGetter,
                                     Supplier<R> falseResultGetter)
    { return ifStatement.test(from) ? trueResultGetter.get() : falseResultGetter.get(); }

    /**
     * Derives a value from a given unless where it's not null.
     * @param from The value to derive a value from if it's not null.
     * @param notNullResultGetter The function to derive a value from the given value if it's not null.
     * @param nullResultGetter The function to produce a value if the given value is null.
     * @param <T> The type of the value a result is being derived from.
     * @param <R> The type of the resulting value.
     * @return The result of the appropriate getter.
     */
    public static <T, R> R getFromUnlessNull(T from, Function<T, R> notNullResultGetter, Supplier<R> nullResultGetter)
    { return from != null ? notNullResultGetter.apply(from) : nullResultGetter.get(); }

    /**
     * Derives a value from a given unless where it's not null.
     * @param from The value to derive a value from if it's not null.
     * @param notNullResultGetter The function to derive a value from the given value if it's not null.
     * @param <T> The type of the value a result is being derived from.
     * @param <R> The type of the resulting value.
     * @return The result of the given getter if the given value is not null, or null if it is.
     */
    public static <T, R> R getFromUnlessNull(T from, Function<T, R> notNullResultGetter)
    { return from != null ? notNullResultGetter.apply(from) : null; }

    /**
     * Derives a value from a given unless where it's not null.
     * @param from The value to derive a value from if it's not null.
     * @param notNullResultGetter The function to produce a value if the given value is not null.
     * @param nullResultGetter The function to produce a value if the given value is null.
     * @param <T> The type of the value being checked.
     * @param <R> The type of the resulting value.
     * @return The result of the appropriate getter.
     */
    public static <T, R> R getFromUnlessNull(T from, Supplier<R> notNullResultGetter, Supplier<R> nullResultGetter)
    { return from != null ? notNullResultGetter.get() : nullResultGetter.get(); }

    /**
     * Derives a value from a given unless where it's not null.
     * @param from The value to derive a value from if it's not null.
     * @param notNullResultGetter The function to produce a value if the given value is not null.
     * @param <T> The type of the value being checked.
     * @param <R> The type of the resulting value.
     * @return The result of the given getter if the given value is not null, or null if it is.
     */
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
