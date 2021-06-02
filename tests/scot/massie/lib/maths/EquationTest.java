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
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_mixed_ternariesAndUnaries()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    //endregion
    //endregion

    //region custom
    //region unary
    @Test
    void op_custom_unary_prefix()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_custom_unary_postfix()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_custom_mixedUnaries()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region binary
    @Test
    void op_custom_binary()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_custom_mixedBinaries()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region ternary
    @Test
    void op_custom_ternary()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_custom_chainedTernaries_leftAssociative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_custom_chainedTernaries_rightAssociative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_custom_chainedTernaries_mixedAssociativities()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_custom_nestedTernaries_leftAssociative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_custom_nestedTernaries_rightAssociative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void op_custom_nestedTernaries_mixedAssociativities()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
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