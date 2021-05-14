package scot.massie.lib.maths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EquationEvaluator_BuilderTest
{
    // These tests assume that the methods for registering operators, functions, and variables are working as intended.

    /*

    Things to test:
        .verifyTokenisationBrackets
            - Contains no brackets
            - Contains one pair of brackets
            - Contains nested brackets
            - Contains two separate sets of brackets
            - Contains one open bracket
            - Contains one close bracket
            - Contains pair of brackets with extraneous open bracket
            - Contains pair of brackets with extraneous close bracket
            - Contains an unmatched close bracket followed by an unmatched open bracket
        .startsWithNonPrefixOperator
            Assumes that the given tokenlist is not empty.
            - Starts with a prefix operator
            - Starts with an infix operator
            - Starts with a non-operator
        .endsWithNonPostfixOperator
            Assumes that the given tokenlist is not empty.
            - Ends with a postfix operator
            - Ends with an infix operator
            - Ends with a non-operator
        .getOpRun
            - Operator token run in tokenlist
            - Operator token run at start of tokenlist
            - Operator token run at end of tokenlist
            - Operator token run is entire tokenlist
            - Operator token run is one token
            - Operator token run is one token at start of tokenlist
            - Operator token run is one token at end of tokenlist
            - Operator token run is one token which is entire tokenlist
        .canBeInfixOperatorToken
            Note that .canBeInfixOperatorToken assumes that the index it is given is of a token used as an infix
            operator, and doesn't itself check if that is true.
            - Can be, is single token
            - Can be, followed by prefix operator tokens
            - Can be, follows postfix operator tokens
            - Cannot be, operator run is at start of tokenlist
            - Cannot be, operator run is at end of tokenlist
            - Cannot be, operator run is entire tokenlist
            - Cannot be, provided index is followed by operator tokens that cannot be prefix operator tokens
            - Cannot be, provided index follows operator tokens that cannot be postfix operator tokens

        .buildOperatorGroups
            Assumes behaviour across infix/prefix/postfix operators to be the same. Only testing for infix operators.
            - No operators
            - One operator
            - Two operators at different priorities
            - Two operators at the same priority and different associativities
            - Two operators at the same priority and same associativity


        .tryParse
            As this only routes to other methods, this is assumed to be working correctly if all other methods it
            routes to are working correctly.

        .tryParseVariable
            - Not a variable
            - Is a variable
            - Is a variable with spaces
            - Is a variable with token characters
        .tryParseFunctionCall
            - Not a function call
            - Is a function call with no arguments
            - Is a function call (whose name has spaces) with no arguments
            - Is a function call (whose name has token characters) with no arguments
            - Is a function call with one argument
            - Is a function call with three arguments
            - Is a function call with three arguments, one of which is another function call
            - Is a function call with three arguments, one of which is in brackets
        .tryParseNumber
            - Is not a number
            - Is a number
            - Is a number made up of multiple tokens

        .tryParseOperation
            As this only routes to other methods, this is assumed to be working correctly if all other methods it
            routes to are working correctly.

        .tryParseInfixOperation_rightAssociative
            - Not an infix operation
            - Not an infix operation, but contains the first few operator tokens of it
            - Not an infix operation, but contains the operator tokens of it. One of the operator tokens cannot be
              an infix operator token as dictated by .canBeInfixOperatorToken.
            - Not an infix operation, but contains the operator tokens of it. One of the operator tokens is in brackets
            - Is an infix operation
            - Is an infix operation followed by the same one
        .tryParseInfixOperation_leftAssociative
            - Not an infix operation
            - Not an infix operation, but contains the last few operator tokens of it
            - Not an infix operation, but contains the operator tokens of it. One of the operator tokens cannot be
              an infix operator token as dictated by .canBeInfixOperatorToken.
            - Not an infix operation, but contains the operator tokens of it. One of the operator tokens is in brackets
            - Is an infix operation
            - Is an infix operation followed by the same one
        .tryParsePrefixOperation
            - Not a prefix operation
            - Is a prefix operation
        .tryParsePostfixOperation
            - Not a postfix operation
            - Is a postfix operation

     */
}
