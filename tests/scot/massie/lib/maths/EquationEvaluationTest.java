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
    public void addition()
    { assertEquals(5.0, new EquationEvaluation("2 + 3").evaluate()); }

    @Test
    public void subtraction()
    { assertEquals(5.0, new EquationEvaluation("9 - 4").evaluate()); }

    @Test
    public void multiplication()
    { assertEquals(12.0, new EquationEvaluation("3 * 4").evaluate()); }

    @Test
    public void multiplication_alt1()
    { assertEquals(12.0, new EquationEvaluation("3 × 4").evaluate()); }

    @Test
    public void division()
    { assertEquals(5.0, new EquationEvaluation("65 / 13").evaluate()); }

    @Test
    public void division_alt1()
    { assertEquals(5.0, new EquationEvaluation("65 ÷ 13").evaluate()); }

    @Test
    public void power()
    { assertEquals(216.0, new EquationEvaluation("6^3").evaluate()); }

    @Test
    public void root()
    { assertEquals(5.0, new EquationEvaluation("3√125").evaluate(), Math.ulp(5.0)); }

    @Test
    public void negation()
    { assertEquals(-5.0, new EquationEvaluation("-5").evaluate()); }

    @Test
    public void squareRoot()
    { assertEquals(5.0, new EquationEvaluation("√25").evaluate(), Math.ulp(5.0)); }

    @Test
    public void brackets()
    { assertEquals(5.0, new EquationEvaluation("(5)").evaluate()); }

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