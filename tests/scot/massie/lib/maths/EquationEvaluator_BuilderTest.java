package scot.massie.lib.maths;

import org.junit.jupiter.api.Test;

import scot.massie.lib.maths.EquationEvaluator.*;

import java.util.ArrayList;
import java.util.List;

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

    StringBuilder appendSpaces(StringBuilder sb, int numberOfSpaces)
    {
        for(int i = 0; i < numberOfSpaces; i++)
            sb.append(' ');

        return sb;
    }

    String getSpaces(int numberOfSpaces)
    { return appendSpaces(new StringBuilder(), numberOfSpaces).toString(); }

    Token asToken(Object o)
    {
        if(o instanceof Token)
            return (Token)o;
        else if(o instanceof Number)
            return new NumberToken(o.toString(), ((Number)o).doubleValue());
        else if(o instanceof String)
            return new UntokenisedString((String)o);
        else
            throw new UnsupportedOperationException("A token was of type: " + o.getClass().getName());
    }

    List<Token> asListOfTokens(Object... tokens)
    {
        List<Token> result = new ArrayList<>(tokens.length);

        for(Object o : tokens)
            result.add(asToken(o));

        return result;
    }

    TokenList newTokenList(Object... tokens)
    {
        StringBuilder sb = new StringBuilder();
        List<Token> tokenList = asListOfTokens(tokens);
        List<Integer> spacings = new ArrayList<>(tokens.length + 1);

        for(int a = 1, b = 2, i = 0; i <= tokens.length; i++, b += a, a = b - a)
            spacings.add(a);

        for(int i = 0; i < tokenList.size(); i++)
            appendSpaces(sb, spacings.get(i)).append(tokenList.get(i).text);

        appendSpaces(sb, spacings.get(spacings.size() - 1));
        return new TokenList(sb.toString(), tokenList, spacings);
    }

    //region verifyTokenisationBrackets

    @Test
    void verifyTokenisationBrackets_noBrackets()
    { assertDoesNotThrow(() -> new Builder("2+2").verifyTokenisationBrackets(newTokenList("a", "b", "c"))); }

    @Test
    void verifyTokenisationBrackets_brackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void verifyTokenisationBrackets_nestedBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void verifyTokenisationBrackets_consecutiveBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void verifyTokenisationBrackets_singleOpenBracket()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void verifyTokenisationBrackets_singleCloseBracket()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void verifyTokenisationBrackets_bracketsWithExtraOpen()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void verifyTokenisationBrackets_bracketsWithExtraClose()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void verifyTokenisationBrackets_unmatchedCloseFollowedByUnmatchedOpen()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion
    
    //region startsWithNonPrefixOperator
    @Test
    void startsWithNonPrefixOperator_startsWithPrefixOperator()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void startsWithNonPrefixOperator_startsWithInfixOperator()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void startsWithNonPrefixOperator_startsWithNonOperator()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region endsWithNonPostfixOperator
    @Test
    void endsWithNonPostfixOperator_endsWithPostfixOperator()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void endsWithNonPostfixOperator_endsWithInfixOperator()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void endsWithNonPostfixOperator_endsWithNonOperator()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region getOpRun
    @Test
    void getOpRun_tokenRunIn()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getOpRun_tokenRunAtStart()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getOpRun_tokenRunAtEnd()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getOpRun_tokenRunIsEntire()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getOpRun_tokenRunIsOneToken()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getOpRun_tokenRunIsOneTokenAtStart()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getOpRun_tokenRunIsOneTokenAtEnd()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getOpRun_tokenRunIsOneTokenThatIsEntire()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region canBeInfixOperatorToken

    @Test
    void canBeInfixOperatorToken_singleToken()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void canBeInfixOperatorToken_followedByPrefix()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void canBeInfixOperatorToken_followingPostfix()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void canBeInfixOperatorToken_runAtStart()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void canBeInfixOperatorToken_runAtEnd()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void canBeInfixOperatorToken_runIsEntire()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void canBeInfixOperatorToken_followedByNonPrefixOperatorTokens()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void canBeInfixOperatorToken_followsNonPostfixOperatorTokens()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region buildOperatorGroups
    @Test
    void buildOperatorGroups_noOperators()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void buildOperatorGroups_oneOperator()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void buildOperatorGroups_twoOperators_differentPriorities()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void buildOperatorGroups_twoOperators_samePriorityDifferentAssociativity()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void buildOperatorGroups_twoOperators_samePrioritySameAssociativity()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region tryParseVariable
    @Test
    void tryParseVariable_notAVariable()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseVariable_isAVariable()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseVariable_isAVariableWithSpaces()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseVariable_isAVariableWithTokenCharacters()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region tryParseFunctionCall
    @Test
    void tryParseFunctionCall_notAFunctionCall()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseFunctionCall_functionCallWithNoArgs()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseFunctionCall_functionCallWithSpaceWithNoArgs()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseFunctionCall_functionCallWithOpTokensWithArgs()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseFunctionCall_functionCallWithOneArgument()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseFunctionCall_functionCallWithThreeArgs()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseFunctionCall_functionCallWithThreeArgsWhereOneIsFunctionCall()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseFunctionCall_functionCallWithThreeArgsWhereOneIsInBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region tryParseNumber
    @Test
    void tryParseNumber_notANumber()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseNumber_isANumber()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseNumber_isNumberMadeUpOfMultipleTokens()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region tryParseInfixOperation_rightAssociative
    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperation()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperationButHasFirstFewTokensOfIt()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperationBecauseOneOfTheTokensCantBe()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperationBecauseOneOfTheTokensIsInBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_rightAssociative_isAnInfixOperation()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_rightAssociative_isAnInfixOperationFollowedBySameOne()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region tryParseInfixOperation_leftAssociative
    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperation()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperationButHasLastFewTokensOfIt()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperationBecauseOneOfTheTokensCantBe()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperationBecauseOneOfTheTokensIsInBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_leftAssociative_isAnInfixOperation()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParseInfixOperation_leftAssociative_isAnInfixOperationFollowedBySameOne()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion
    
    //region tryParsePrefixOperation
    @Test
    void tryParsePrefixOperation_notAPrefixOperation()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParsePrefixOperation_isAPrefixOperation()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region tryParsePostfixOperation
    @Test
    void tryParsePostfixOperation_notAPostfixOperation()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void tryParsePostfixOperation_isAPostfixOperation()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion
}
