package scot.massie.lib.maths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

class EquationEvaluationTest
{
    @Test
    public void literalNumber()
    { assertEquals(5.0, new EquationEvaluation("5").evaluate()); }

    @Test
    void op_binary_add()
    { assertEquals(21.0, new EquationEvaluation("13 + 8").evaluate()); }

    @Test
    void op_binary_minus()
    { assertEquals(5.0, new EquationEvaluation("13 - 8").evaluate()); }

    @Test
    void op_binary_divide()
    {
        assertEquals(13.0, new EquationEvaluation("65 / 5").evaluate());
        assertEquals(13.0, new EquationEvaluation("65 ÷ 5").evaluate());
    }

    @Test
    void op_binary_multiply()
    {
        assertEquals(104.0, new EquationEvaluation("13 * 8").evaluate());
        assertEquals(104.0, new EquationEvaluation("13 × 8").evaluate());
    }

    @Test
    void op_binary_mod()
    { assertEquals(5.0, new EquationEvaluation("13 % 8").evaluate()); }

    @Test
    void op_binary_root()
    { assertEquals(5.0, new EquationEvaluation("3 √ 125").evaluate(), Math.ulp(5.0)); }

    @Test
    void op_binary_power()
    { assertEquals(625.0, new EquationEvaluation("5^4").evaluate()); }

    @Test
    void op_unary_prefix_negation()
    { assertEquals(-9.0, new EquationEvaluation("-9").evaluate()); }

    @Test
    void op_unary_prefix_positive()
    { assertEquals(9.0, new EquationEvaluation("+ 9").evaluate()); }

    @Test
    void op_unary_postfix_percent()
    {
        assertEquals(0.5, new EquationEvaluation("50%").evaluate());
        assertEquals(7.0, new EquationEvaluation("700 %").evaluate());
    }

    @Test
    void op_custom_binary_leftAssociative()
    {
        assertEquals(37.0, new EquationEvaluation("5§7§9")
                                   .withOperator('§', (a, b) -> a + b + b)
                                   .evaluate());
    }

    @Test
    void op_custom_binary_rightAssociative()
    {
        assertEquals(55.0, new EquationEvaluation("5§7§9")
                                   .withRightAssociativeOperator('§', (a, b) -> a + b + b)
                                   .evaluate());
    }

    @Test
    void op_custom_unary_prefix()
    {
        assertEquals(45.0, new EquationEvaluation("§9")
                                   .withPrefixOperator('§', x -> x * 5)
                                   .evaluate());
    }

    @Test
    void op_custom_unary_suffix()
    {
        assertEquals(45.0, new EquationEvaluation("9§")
                                   .withSuffixOperator('§', x -> x * 5)
                                   .evaluate());
    }

    @Test
    void op_custom_order_mixedUnaries()
    {
        assertEquals(32.0, new EquationEvaluation("§9§")
                                .withPrefixOperator('§', x -> x * 3)
                                .withSuffixOperator('§', x -> x + 5)
                                .evaluate());

        assertEquals(32.0, new EquationEvaluation("§9§")
                                   .withPrefixOperator('§', 5, x -> x * 3)
                                   .withSuffixOperator('§', 4, x -> x + 5)
                                   .evaluate());

        assertEquals(42.0, new EquationEvaluation("§9§")
                                   .withPrefixOperator('§', 5, x -> x * 3)
                                   .withSuffixOperator('§', 6, x -> x + 5)
                                   .evaluate());
    }

    @Test
    void op_custom_order_mixedAssociativityBinaries()
    {
        assertEquals(108.0, new EquationEvaluation("5 § 7 ◊ 9")
                                    .withOperator                ('§', (a, b) -> a + b)
                                    .withRightAssociativeOperator('◊', (a, b) -> a * b)
                                    .evaluate());

        assertEquals(108.0, new EquationEvaluation("5 § 7 ◊ 9")
                                    .withOperator                ('§', 5, (a, b) -> a + b)
                                    .withRightAssociativeOperator('◊', 4, (a, b) -> a * b)
                                    .evaluate());

        assertEquals(68.0, new EquationEvaluation("5 § 7 ◊ 9")
                                    .withOperator                ('§', 5, (a, b) -> a + b)
                                    .withRightAssociativeOperator('◊', 6, (a, b) -> a * b)
                                    .evaluate());
    }

    @Test
    public void brackets()
    {
        assertEquals(5.0, new EquationEvaluation("(5)").evaluate());
        assertEquals(14.0, new EquationEvaluation("(5 + 9)").evaluate());
        assertEquals(17.0, new EquationEvaluation("4 + (6 + 7)").evaluate());
    }

    @Test
    public void orderOfOperations()
    { assertEquals(-681.0, new EquationEvaluation("√25 + -7^3 * 2√4").evaluate(), Math.ulp(-681.0)); }

    @Test
    public void orderRespectingBrackets()
    { assertEquals(65.0, new EquationEvaluation("5 * (6 + 7)").evaluate()); }

    @Test
    public void correctlyHandlesMixedBinaryAndUnaryOperators()
    {
        assertEquals(-25.0, new EquationEvaluation("5 * - 5").evaluate());
        assertEquals(3.5, new EquationEvaluation("50% * 7").evaluate());
        assertEquals(-24.0, new EquationEvaluation("75% * -32").evaluate());
    }

    @Test
    public void variables_premade()
    {
        assertEquals(Math.PI, new EquationEvaluation("π").evaluate());
        assertEquals(Math.E, new EquationEvaluation("e").evaluate());
        assertEquals((1 + Math.sqrt(5)) / 2, new EquationEvaluation("ϕ").evaluate());
        assertEquals((1 + Math.sqrt(5)) / 2, new EquationEvaluation("φ").evaluate());
        assertEquals(Double.POSITIVE_INFINITY, new EquationEvaluation("∞").evaluate());

        assertEquals(Math.PI, new EquationEvaluation("pi").evaluate());
        assertEquals((1 + Math.sqrt(5)) / 2, new EquationEvaluation("phi").evaluate());
        assertEquals(Double.POSITIVE_INFINITY, new EquationEvaluation("inf").evaluate());
    }

    @Test
    public void variables_custom()
    {
        assertEquals(5.0, new EquationEvaluation("x").withVariable("x", 5).evaluate());
        assertEquals(5.0, new EquationEvaluation("myval").withVariable("myval", 5).evaluate());
        assertEquals(11.0, new EquationEvaluation("val1 + val2")
                                  .withVariable("val1", 5)
                                  .withVariable("val2", 6)
                                  .evaluate());
    }

    @Test
    public void functions_premade()
    { assertEquals(8.25, new EquationEvaluation("avg(5, 8, 9, 11)").evaluate()); }

    @Test
    public void functions_custom()
    {
        assertEquals(10.0, new EquationEvaluation("double(5)").withFunction("double", args -> args[0] * 2).evaluate());

        assertEquals(120.0, new EquationEvaluation("multiplytogether(2, 3, 4, 5)").withFunction("multiplytogether", args ->
        {
            double current = args[0];

            for(int i = 1; i < args.length; i++)
                current *= args[i];

            return current;
        }).evaluate());

        assertEquals(5.0, new EquationEvaluation("get5()").withFunction("get5", args -> 5).evaluate());
    }

    @Test
    void remove_variable_removedBeforeBuild()
    {
        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        { new EquationEvaluation("π + 4").withoutVariable("π").evaluate(); });
    }

    @Test
    void remove_variable_removedAfterBuild()
    {
        assertThrows(EquationEvaluation.UnresolvedArgumentInEquationException.class, () ->
        { new EquationEvaluation("π + 4").build().withoutVariable("π").evaluate(); });
    }

    @Test
    void remove_variable_removedBeforeBuild_different()
    { assertEquals(Math.PI + 4.0, new EquationEvaluation("π + 4").withoutVariable("e").evaluate()); }

    @Test
    void remove_variable_removedAfterBuild_different()
    { assertEquals(Math.PI + 4.0, new EquationEvaluation("π + 4").build().withoutVariable("e").evaluate()); }

    @Test
    void remove_function_removedBeforeBuild()
    {
        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        { new EquationEvaluation("fib(6)").withoutFunction("fib").evaluate(); });
    }

    @Test
    void remove_function_removedAfterBuild()
    {
        assertThrows(EquationEvaluation.MissingFunctionException.class, () ->
        { new EquationEvaluation("fib(6)").build().withoutFunction("fib").evaluate(); });
    }

    @Test
    void remove_function_removedBeforeBuild_different()
    { assertEquals(8.0, new EquationEvaluation("fib(6)").withoutFunction("tan").evaluate()); }

    @Test
    void remove_function_removedAfterBuild_different()
    { assertEquals(8.0, new EquationEvaluation("fib(6)").build().withoutFunction("tan").evaluate()); }

    @Test
    void remove_operator_removedBeforeBuild()
    {
        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        { new EquationEvaluation("4 + 7").withoutBinaryOperator('+').evaluate(); });
    }

    @Test
    void remove_operator_removedAfterBuild()
    {
        // Equation evaluation shouldn't be affected by the removal of an operator until the next time it's built.
        assertEquals(11.0, new EquationEvaluation("4 + 7").build().withoutBinaryOperator('+').evaluate());
    }

    @Test
    void remove_operator_removedBeforeBuild_different()
    { assertEquals(11.0, new EquationEvaluation("4 + 7").withoutBinaryOperator('-').evaluate()); }

    @Test
    void remove_operator_removedAfterBuild_different()
    { assertEquals(11.0, new EquationEvaluation("4 + 7").build().withoutBinaryOperator('-').evaluate()); }

    @Test
    void remove_operator_removedBeforeBuild_sameButDifferentType()
    { assertEquals(11.0, new EquationEvaluation("4 + 7").withoutPrefixOperator('+').evaluate()); }

    @Test
    void remove_operator_removedAfterBuild_sameButDifferentType()
    { assertEquals(11.0, new EquationEvaluation("4 + 7").build().withoutPrefixOperator('+').evaluate()); }

    @Test
    public void invalid_unspecifiedVariableText()
    {
        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        { new EquationEvaluation("doodly doot + 7").evaluate(); });
    }

    @Test
    public void invalid_trailingOperator()
    {
        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        { new EquationEvaluation("3 + 4 + 5 -").evaluate(); });
    }

    @Test
    public void invalid_frontTrailingOperator()
    {
        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        { new EquationEvaluation(" * 6 + 5 + 4").evaluate(); });
    }

    @Test
    public void invalid_onlyUnaryNoBinaryOperators()
    {
        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        {
            new EquationEvaluation("4§◊6")
                    .withSuffixOperator('§', x -> x * 3)
                    .withPrefixOperator('◊', x -> x + 5)
                    .evaluate();
        });
    }

    @Test
    public void invalid_mismatchedBrackets()
    {
        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        { new EquationEvaluation("(5 + 4").evaluate(); });


        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        { new EquationEvaluation("5 + 4)").evaluate(); });
    }

    @Test
    public void invalid_mismatchedFunctionBrackets()
    {
        assertThrows(EquationEvaluation.UnparsableEquationException.class, () ->
        { new EquationEvaluation("avg(5, 9").evaluate(); });
    }
}