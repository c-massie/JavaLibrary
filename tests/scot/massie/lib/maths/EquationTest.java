package scot.massie.lib.maths;

import org.junit.jupiter.api.Test;

import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

class EquationTest
{
    void assertEquation(double expected, String equation)
    { assertEquals(expected, new Equation.Builder().withComparativeOperators().build(equation).evaluate()); }

    void assertEquation(double expected, String equation, double delta)
    { assertEquals(expected, new Equation(equation).evaluate(), delta); }

    @Test
    void literalNumber()
    { assertEquation(5.0, "5"); }

    //region operators
    //region default
    //region unary
    //region prefix

    @Test
    void op_unary_prefix_negation()
    {
        assertEquation(-9.0, "-9");
        assertEquation(9.0, "--9");
    }

    @Test
    void op_unary_prefix_positive()
    { assertEquation(9.0, "+9"); }

    //endregion

    //region postfix

    @Test
    void op_unary_postfix_percent()
    {
        assertEquation(0.5, "50%");
        assertEquation(7.0, "700 %");
    }

    //endregion
    //endregion

    //region binary
    @Test
    void op_binary_add()
    { assertEquation(21.0, "13 + 8"); }

    @Test
    void op_binary_minus()
    { assertEquation(5.0, "13 - 8"); }

    @Test
    void op_binary_multiply()
    {
        assertEquation(104, "13 * 8");
        assertEquation(104, "13 × 8");
    }

    @Test
    void op_binary_divide()
    {
        assertEquation(13.0, "65 / 5");
        assertEquation(13.0, "65 ÷ 5");
    }

    @Test
    void op_binary_mod()
    { assertEquation(5.0, "13 % 8"); }

    @Test
    void op_binary_root()
    { assertEquation(5.0, "3 √ 125", Math.ulp(5.0)); }

    @Test
    void op_binary_power()
    { assertEquation(625.0, "5^4"); }
    //endregion

    //region ternary

    @Test
    void op_ternary_conditional()
    {
        assertEquation(5, "1 ? 5 : 7");
        assertEquation(7, "0 ? 5 : 7");
    }

    //endregion

    //region mixed n-aries

    @Test
    void op_mixed_binariesAndUnaries()
    { assertEquation(4.0, "50% * 8"); }

    @Test
    void op_mixed_ternariesAndUnaries()
    { assertEquation(0.25, "1 ? 25% : 7"); }
    //endregion
    //endregion

    //region custom
    //region unary
    @Test
    void op_custom_unary_prefix()
    { assertEquals(45.0, new Equation.Builder().withPrefixOperator("§", x -> x * 5).build("§9").evaluate()); }

    @Test
    void op_custom_unary_postfix()
    { assertEquals(45.0, new Equation.Builder().withPostfixOperator("§", x -> x * 5).build("9§").evaluate()); }

    @Test
    void op_custom_mixedUnaries()
    {
        // Where prefix and postfix operators have the same priority, prefix operators win.
        // Where prefix and postfix operators of the same priority are used on the same operand, the postfix operator
        // is considered to have higher priority/stickiness. That is, the prefix operator is checked for first.
        assertEquals(38.0, new Equation.Builder(false)
                                 .withPrefixOperator("§", x -> x + 3)
                                 .withPostfixOperator("§", x -> x * 5)
                                 .build("§7§")
                                 .evaluate());
    }
    //endregion

    //region binary
    @Test
    void op_custom_binary_leftAssociative()
    {
        assertEquals(37.0, new Equation.Builder(false)
                                   .withOperator("§", (a, b) -> a + b + b)
                                   .build("5§7§9")
                                   .evaluate());
    }

    @Test
    void op_custom_binary_rightAssociative()
    {
        assertEquals(55.0, new Equation.Builder(false)
                                   .withOperator("§", false, (a, b) -> a + b + b)
                                   .build("5§7§9")
                                   .evaluate());
    }

    @Test
    void op_custom_mixedBinaries()
    {
        // Where two infix operators with the same priority are used together, left-associative operators are considered
        // to have higher priority/stickiness. That is, right-associative operators are checked for first.
        Equation.Builder eb = new Equation.Builder(false).withOperator("§", true, (a, b) -> a + b + b)
                                                         .withOperator("$", false, (a, b) -> a * b);

        assertEquals(153.0, eb.build("3 § 7 $ 9").evaluate());
        assertEquals(75.0, eb.build("3 $ 7 § 9").evaluate());
    }
    //endregion

    //region ternary
    @Test
    void op_custom_ternary()
    {
        Equation.Builder eqb = new Equation.Builder(false).withOperator("§", "$", (a, b, c) -> a * b + c);
        assertEquals(22.0, eqb.build("3§5$7").evaluate()); }

    @Test
    void op_custom_chainedTernaries_leftAssociative()
    {
        Equation.Builder eqb = new Equation.Builder(false).withOperator("§", "$", true, (a, b, c) -> a * b + c);
        assertEquals(255.0, eqb.build("3§5$7 § 11 $ 13").evaluate());
    }

    @Test
    void op_custom_chainedTernaries_rightAssociative()
    {
        Equation.Builder eqb = new Equation.Builder(false).withOperator("§", "$", false, (a, b, c) -> a * b + c);
        assertEquals(105.0, eqb.build("3 § 5 $ 7§11$13").evaluate());
    }

    @Test
    void op_custom_chainedTernaries_mixedAssociativities()
    {
        // Where two infix operators with the same priority are used together, left-associative operators are considered
        // to have higher priority/stickiness. That is, right-associative operators are checked for first.

        Equation.Builder eqb = new Equation.Builder(false)
                                       .withOperator("§", "$", true, (a, b, c) -> a * b + c)
                                       .withOperator("£", "€", false, (a, b, c) -> a * 4 + b * 9 + c * 16);

        assertEquals(395.0, eqb.build("3§5$7 £ 11 € 13").evaluate());
        assertEquals(1497.0, eqb.build("3 £ 5 € 7§11$13").evaluate());
    }

    @Test
    void op_custom_nestedTernaries_leftAssociative()
    {
        Equation.Builder eqb = new Equation.Builder(false).withOperator("§", "$", true, (a, b, c) -> a * b + c);
        assertEquals(151.0, eqb.build("3 § 5§7$11 $ 13").evaluate());
    }

    @Test
    void op_custom_nestedTernaries_rightAssociative()
    {
        Equation.Builder eqb = new Equation.Builder(false).withOperator("§", "$", false, (a, b, c) -> a * b + c);
        assertEquals(151.0, eqb.build("3 § 5§7$11 $ 13").evaluate());
    }

    @Test
    void op_custom_nestedTernaries_mixedAssociativities()
    {
        Equation.Builder eqb = new Equation.Builder(false)
                                       .withOperator("§", "$", true, (a, b, c) -> a * b + c)
                                       .withOperator("£", "€", false, (a, b, c) -> a * 4 + b * 9 + c * 16);

        assertEquals(790.0, eqb.build("3 § 5£7€11 $ 13").evaluate());
        assertEquals(634.0, eqb.build("3 £ 5§7$11 € 13").evaluate());
    }

    //endregion

    //region correct tokenisation

    @Test
    void op_correctTokenisation()
    { assertEquals(35.0 , new Equation.Builder(true).withOperator("+-", (l, r) -> l * r).build("5+-7").evaluate()); }

    //endregion
    //endregion
    //endregion

    @Test
    void brackets()
    {
        assertEquation(5.0, "(5)");
        assertEquation(14.0, "(5 + 9)");
        assertEquation(17.0, "4 + (6 + 7)");
    }

    @Test
    void orderOfOperations()
    { assertEquation(-681.0, "√25 + -7^3 * 2√4", Math.ulp(-681.0)); }

    @Test
    void orderRespectingBrackets()
    { assertEquation(65.0, "5 * (6 + 7)"); }

    //region variables
    @Test
    void variables_default()
    {
        assertEquation(Math.PI, "π");
        assertEquation(Math.E, "e");
        assertEquation((1 + Math.sqrt(5)) / 2, "ϕ");
        assertEquation((1 + Math.sqrt(5)) / 2, "φ");
        assertEquation(Double.POSITIVE_INFINITY, "∞");

        assertEquation(Math.PI, "pi");
        assertEquation((1 + Math.sqrt(5)) / 2, "phi");
        assertEquation(Double.POSITIVE_INFINITY, "inf");
    }

    @Test
    void variables_custom()
    { assertEquals(5.0, new Equation.Builder().withVariable("x", 5).build("x").evaluate()); }
    //endregion

    //region pushing
    //region variables
    @Test
    void push_variable()
    {
        Equation.Builder builder = new Equation.Builder().withVariable("doot", 7);
        Equation eq = builder.build("doot");
        assertEquals(7.0, eq.evaluate());
        builder.pushVariable("doot", 8);
        assertEquals(8.0, eq.evaluate());
    }

    @Test
    void push_variable_overwritten()
    {
        Equation.Builder builder = new Equation.Builder().withVariable("doot", 7);
        Equation eq = builder.build("doot");
        assertEquals(7.0, eq.evaluate());
        eq.setVariable("doot", 8.0);
        assertEquals(8.0, eq.evaluate());
        builder.pushVariable("doot", 9.0);
        assertEquals(8.0, eq.evaluate());
    }
    //endregion
    //region functions

    @Test
    void push_function()
    {
        Equation.Builder builder = new Equation.Builder().withFunction("doot", () -> 7);
        Equation eq = builder.build("doot()");
        assertEquals(7.0, eq.evaluate());
        builder.pushFunction("doot", () -> 8.0);
        assertEquals(8.0, eq.evaluate());
    }

    @Test
    void push_function_overwritten()
    {
        Equation.Builder builder = new Equation.Builder().withFunction("doot", () -> 7);
        Equation eq = builder.build("doot()");
        assertEquals(7.0, eq.evaluate());
        eq.redefineFunction("doot", () -> 8);
        assertEquals(8.0, eq.evaluate());
        builder.pushFunction("doot", () -> 9);
        assertEquals(8.0, eq.evaluate());
    }

    //endregion
    //endregion

    //region functions
    @Test
    void functions_premade()
    { assertEquation(8.25, "avg(5, 8, 9, 11)"); }

    @Test
    void functions_custom()
    {
        assertEquals(10.0, new Equation.Builder().withFunction("double", a -> a[0] * 2).build("double(5)").evaluate());

        assertEquals(120.0, new Equation.Builder().withFunction("multiplytogether", 1, a ->
        {
            double current = a[0];

            for(int i = 1; i < a.length; i++)
                current *= a[i];

            return current;
        }).build("multiplytogether(2, 3, 4, 5)").evaluate());

        assertEquals(5.0, new Equation.Builder().withFunction("get5", a -> 5).build("get5()").evaluate());
    }
    //endregion
    
    //region separateFromBuilder
    @Test
    void operatorsSeparateFromBuilder()
    {
        Equation.Builder eqb = new Equation.Builder(false).withOperator("§", (l, r) -> l * r);
        Equation eq = eqb.build("5 § 7");
        eqb.withOperator("§", (l, r) -> l + r);
        assertEquals(35.0, eq.evaluate());
    }

    @Test
    void variablesSeparateFromBuilder()
    {
        Equation.Builder eqb = new Equation.Builder(false).withVariable("doot", 7);
        Equation eq = eqb.build("doot");
        eqb.withVariable("doot", 5);
        assertEquals(7.0, eq.evaluate());
        eq.setVariable("doot", 8);
        assertEquals(8.0, eq.evaluate());
    }

    @Test
    void functionsSeparateFromBuilder()
    {
        Equation.Builder eqb = new Equation.Builder(false).withFunction("getdoot", x -> 7);
        Equation eq = eqb.build("getdoot()");
        eqb.withFunction("getdoot", x -> 5);
        assertEquals(7.0, eq.evaluate());
        eq.redefineFunction("getdoot", x -> 8);
        assertEquals(8.0, eq.evaluate());
    }
    //endregion
    
    //region invalid
    @Test
    void invalid_nonExistentVariable()
    { assertThrows(Equation.Builder.EquationParseException.class, () -> new Equation("doot")); }

    @Test
    void invalid_trailingInfixOperator()
    { assertThrows(Equation.Builder.TrailingNonPostfixOperatorException.class, () -> new Equation("9*")); }

    @Test
    void invalid_leadingInfixOperator()
    { assertThrows(Equation.Builder.LeadingNonPrefixOperatorException.class, () -> new Equation("*9")); }

    @Test
    void invalid_noInfixOperatorsBetweenOperands()
    {
        assertThrows(Equation.Builder.EquationParseException.class,
                     () -> new Equation.Builder(false)
                                   .withPrefixOperator("§", o -> o * 2)
                                   .build("5§7"));
    }

    @Test
    void invalid_mismatchedBrackets()
    {
        assertThrows(Equation.Builder.UnmatchedOpenBracketException.class, () -> new Equation("(5 + 7"));
        assertThrows(Equation.Builder.UnexpectedCloseBracketException.class, () -> new Equation("5 + 7)"));
        assertThrows(Equation.Builder.BracketMismatchException.class, () -> new Equation("5) + (7"));
    }

    @Test
    void invalid_mismatchedFunctionBrackets()
    {
        Equation.Builder eqb = new Equation.Builder(false).withFunction("doot", value -> 5);

        assertThrows(Equation.Builder.EquationParseException.class, () -> eqb.build("doot("));
        assertThrows(Equation.Builder.EquationParseException.class, () -> eqb.build("doot(9"));
        assertThrows(Equation.Builder.EquationParseException.class, () -> eqb.build("doot(9, 5"));
    }

    @Test
    void invalid_functionWithTrailingComma()
    {
        Equation.Builder eqb = new Equation.Builder(false).withFunction("doot", value -> 5);
        assertThrows(Equation.Builder.DanglingArgumentSeparatorException.class, () -> eqb.build("doot(,)"));
        assertThrows(Equation.Builder.TrailingArgumentSeparatorException.class, () -> eqb.build("doot(5,)"));
    }



    @Test
    void invalid_functionWithLeadingComma()
    {
        Equation.Builder eqb = new Equation.Builder(false).withFunction("doot", value -> 5);
        assertThrows(Equation.Builder.DanglingArgumentSeparatorException.class, () -> eqb.build("doot(,)"));
        assertThrows(Equation.Builder.LeadingArgumentSeparatorException.class, () -> eqb.build("doot(,5)"));
    }

    //endregion

    //region disallowed
    @Test
    void disallowed_newVariableOnEquation()
    { assertFalse(new Equation("5 + 7").setVariable("doot", 9)); }

    @Test
    void disallowed_newFunctionOnEquation()
    { assertFalse(new Equation("5 + 7").redefineFunction("doot", x -> 9)); }

    //endregion


}