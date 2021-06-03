package scot.massie.lib.maths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EquationTest
{
    void assertEquation(double expected, String equation)
    { assertEquals(expected, new Equation(equation).evaluate()); }

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

        // The first (commented out) assertion currently fails. In the assertion, the inner operation parses before the
        // outer operation. This doesn't fail for infix n-ary operators (where n >= 3) of the same associativity and
        // priority as in every instance, the outer operation is parsed first regardless.
        //
        // Document that where two infix operators have the same priority but different associativities, the
        // left-associative operator is always treated as having infinitesimally higher priority/stickiness than the
        // right-associative operator.
        //
        // This is not expected to succeed where the operators are of different priorities and the inner one is
        // evaluated first.
        //
        // This may be made to succeed, not just in this example, but in all examples where higher priority infix
        // operator has a lower priority one as an argument, by, when parsing a particular operator group's infix
        // operator tree, checking if the tokenlist being parsed may be parsed with a higher priority infix operator
        // while skipping over this one and its operands, treating it as a single operand.
        //
        // TO DO: This.

        // assertEquals(790.0, eqb.build("3 § 5£7€11 $ 13").evaluate());
        assertEquals(634.0, eqb.build("3 £ 5§7$11 € 13").evaluate());
    }

    //endregion
    //endregion
    //endregion

    @Test
    void brackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void orderOfOperations()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void orderRespectingBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    //region variables
    @Test
    void variables_default()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void variables_custom()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region functions
    @Test
    void functions_premade()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void functions_custom()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion
    
    //region separateFromBuilder
    @Test
    void operatorsSeparateFromBuilder()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void variablesSeparateFromBuilder()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void functionsSeparateFromBuilder()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion
    
    //region invalid
    @Test
    void invalid_nonExistentVariable()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void invalid_trailingInfixOperator()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void invalid_leadingInfixOperator()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void invalid_noInfixOperatorsBetweenOperands()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void invalid_mismatchedBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void invalid_mismatchedFunctionBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion
}