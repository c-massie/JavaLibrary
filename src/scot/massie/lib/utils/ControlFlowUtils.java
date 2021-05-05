package scot.massie.lib.utils;

import java.util.function.Supplier;

public class ControlFlowUtils
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
    public static <T> T nullCoalesce(Supplier<T>... operands)
    {
        for(int i = 0; i < operands.length; i++)
        {
            T result = operands[i].get();

            if(result != null)
                return result;
        }

        return null;
    }
}
