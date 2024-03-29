package scot.massie.lib.maths;

import org.junit.jupiter.api.Test;

import scot.massie.lib.maths.Equation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

public class Equation_BuilderTest
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

    Builder newBuilderWithInfixOp(boolean shouldBeLeftAssociative)
    {
        Builder result = new Builder(false).withVariable("a", 5)
                                           .withVariable("b", 6)
                                           .withVariable("c", 7)
                                           .withVariable("d", 8);

        List<Token> opTokens = Arrays.asList(new Token("£"), new Token("€"), new Token("¢"));
        result.addOperator(new InfixOperator(opTokens,
                                             shouldBeLeftAssociative,
                                             0,
                                             o -> (o[0]) + (o[1] * 3) + (o[2] * 5) + (o[3] * 7)));
        result.buildOperatorGroups();
        return result;
    }

    void assertOpRun(Builder.OperatorTokenRun opRun, List<Token> tokens, int lowerBound, int at, int upperBound)
    {
        assertEquals(tokens.subList(lowerBound, upperBound + 1), opRun.getTokens());
        assertEquals(lowerBound, opRun.getStartIndexInSource());
        assertEquals(upperBound, opRun.getEndIndexInSource());
        assertEquals(at - opRun.getStartIndexInSource(), opRun.getIndexOfPivotInRun());
        assertEquals(tokens.subList(lowerBound, at), opRun.getTokensBeforePivot());
        assertEquals(tokens.subList(at + 1, upperBound + 1), opRun.getTokensAfterPivot());
    }

    Equation newDummyEquation(Equation.Builder builder)
    { return new Equation(builder, null, builder.getVariables(), builder.getFunctions()); }

    //region verifyTokenisationBrackets
    @Test
    void verifyTokenisationBrackets_noBrackets()
    {
        assertDoesNotThrow(() ->
        {
            TokenList tl = newTokenList("a", "b", "c");
            new Builder().verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_brackets()
    {
        assertDoesNotThrow(() ->
        {
            TokenList tl = newTokenList("a", Token.OPEN_BRACKET, "b", Token.CLOSE_BRACKET, "c");
            new Builder().verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_nestedBrackets()
    {
        assertDoesNotThrow(() ->
        {
            TokenList tl = newTokenList("a",
                                        Token.OPEN_BRACKET,
                                        "b",
                                        Token.OPEN_BRACKET,
                                        "c",
                                        Token.CLOSE_BRACKET,
                                        Token.CLOSE_BRACKET,
                                        "d");
            new Builder().verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_consecutiveBrackets()
    {
        assertDoesNotThrow(() ->
        {
            TokenList tl = newTokenList("a",
                                        Token.OPEN_BRACKET,
                                        "b",
                                        Token.CLOSE_BRACKET,
                                        Token.OPEN_BRACKET,
                                        "c",
                                        Token.CLOSE_BRACKET);
            new Builder().verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_singleOpenBracket()
    {
        assertThrows(Builder.UnmatchedOpenBracketException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.OPEN_BRACKET);
            new Builder().verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_singleCloseBracket()
    {
        assertThrows(Builder.UnexpectedCloseBracketException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.CLOSE_BRACKET);
            new Builder().verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_bracketsWithExtraOpen()
    {
        assertThrows(Builder.UnmatchedOpenBracketException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.OPEN_BRACKET, Token.OPEN_BRACKET, "b", Token.CLOSE_BRACKET);
            new Builder().verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_bracketsWithExtraClose()
    {
        assertThrows(Builder.UnexpectedCloseBracketException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.OPEN_BRACKET, "b", Token.CLOSE_BRACKET, Token.CLOSE_BRACKET);
            new Builder().verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_unmatchedCloseFollowedByUnmatchedOpen()
    {
        assertThrows(Builder.BracketMismatchException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.CLOSE_BRACKET, Token.OPEN_BRACKET, "b");
            new Builder().verifyTokenisationBrackets(tl);
        });
    }
    //endregion
    
    //region startsWithNonPrefixOperator
    @Test
    void startsWithNonPrefixOperator_startsWithPrefixOperator()
    { assertFalse(new Builder().startsWithNonPrefixOperator(newTokenList(new Token("-"), "a"))); }

    @Test
    void startsWithNonPrefixOperator_startsWithInfixOperator()
    { assertTrue(new Builder().startsWithNonPrefixOperator(newTokenList(new Token("/"), "a"))); }

    @Test
    void startsWithNonPrefixOperator_startsWithNonOperator()
    { assertFalse(new Builder().startsWithNonPrefixOperator(newTokenList("a", "b"))); }
    //endregion

    //region endsWithNonPostfixOperator
    @Test
    void endsWithNonPostfixOperator_endsWithPostfixOperator()
    { assertFalse(new Builder().endsWithNonPostfixOperator(newTokenList("a", new Token("%")))); }

    @Test
    void endsWithNonPostfixOperator_endsWithInfixOperator()
    { assertTrue(new Builder().endsWithNonPostfixOperator(newTokenList("a", new Token("/")))); }

    @Test
    void endsWithNonPostfixOperator_endsWithNonOperator()
    { assertFalse(new Builder().endsWithNonPostfixOperator(newTokenList("a", "b"))); }
    //endregion

    //region getOpRun

    // Note that Builder.getOpRun(...) assumes that the index it's given is a valid operator character and does not
    // check whether or not this is true.

    @Test
    void getOpRun_tokenRunIn()
    {
        List<Token> ts = asListOfTokens("a", new Token("+"), new Token("-"), new Token("/"), "b");
        Builder.OperatorTokenRun opRun = new Builder().getOpRun(ts, 2);
        assertOpRun(opRun, ts, 1, 2, 3);
    }

    @Test
    void getOpRun_tokenRunAtStart()
    {
        List<Token> ts = asListOfTokens(new Token("+"), new Token("-"), new Token("/"), "b");
        Builder.OperatorTokenRun opRun = new Builder().getOpRun(ts, 1);
        assertOpRun(opRun, ts, 0, 1, 2);
    }

    @Test
    void getOpRun_tokenRunAtEnd()
    {
        List<Token> ts = asListOfTokens("a", new Token("+"), new Token("-"), new Token("/"));
        Builder.OperatorTokenRun opRun = new Builder().getOpRun(ts, 2);
        assertOpRun(opRun, ts, 1, 2, 3);
    }

    @Test
    void getOpRun_tokenRunIsEntire()
    {
        List<Token> ts = asListOfTokens(new Token("+"), new Token("-"), new Token("/"));
        Builder.OperatorTokenRun opRun = new Builder().getOpRun(ts, 1);
        assertOpRun(opRun, ts, 0, 1, 2);
    }

    @Test
    void getOpRun_tokenRunIsOneToken()
    {
        List<Token> ts = asListOfTokens("a", new Token("+"), "b");
        Builder.OperatorTokenRun opRun = new Builder().getOpRun(ts, 1);
        assertOpRun(opRun, ts, 1, 1, 1);
    }

    @Test
    void getOpRun_tokenRunIsOneTokenAtStart()
    {
        List<Token> ts = asListOfTokens(new Token("+"), "b");
        Builder.OperatorTokenRun opRun = new Builder().getOpRun(ts, 0);
        assertOpRun(opRun, ts, 0, 0, 0);
    }

    @Test
    void getOpRun_tokenRunIsOneTokenAtEnd()
    {
        List<Token> ts = asListOfTokens("a", new Token("+"));
        Builder.OperatorTokenRun opRun = new Builder().getOpRun(ts, 1);
        assertOpRun(opRun, ts, 1, 1, 1);
    }

    @Test
    void getOpRun_tokenRunIsOneTokenThatIsEntire()
    {
        List<Token> ts = asListOfTokens(new Token("+"));
        Builder.OperatorTokenRun opRun = new Builder().getOpRun(ts, 0);
        assertOpRun(opRun, ts, 0, 0, 0);
    }
    //endregion

    //region canBeInfixOperatorToken
    @Test
    void canBeInfixOperatorToken_singleToken()
    {
        List<Token> tl = asListOfTokens("a", new Token("+"), "b");
        assertTrue(new Builder().canBeInfixOperatorToken(tl, 1));
    }

    @Test
    void canBeInfixOperatorToken_followedByPrefix()
    {
        List<Token> tl = asListOfTokens("a", new Token("+"), new Token("-"), "b");
        assertTrue(new Builder().canBeInfixOperatorToken(tl, 1));
    }

    @Test
    void canBeInfixOperatorToken_followingPostfix()
    {
        List<Token> tl = asListOfTokens("a", new Token("%"), new Token("+"), "b");
        assertTrue(new Builder().canBeInfixOperatorToken(tl, 2));
    }

    @Test
    void canBeInfixOperatorToken_runAtStart()
    {
        List<Token> tl = asListOfTokens(new Token("+"), "b");
        assertFalse(new Builder().canBeInfixOperatorToken(tl, 0));
    }

    @Test
    void canBeInfixOperatorToken_runAtEnd()
    {
        List<Token> tl = asListOfTokens("a", new Token("+"));
        assertFalse(new Builder().canBeInfixOperatorToken(tl, 1));
    }

    @Test
    void canBeInfixOperatorToken_runIsEntire()
    {
        List<Token> tl = asListOfTokens(new Token("+"));
        assertFalse(new Builder().canBeInfixOperatorToken(tl, 0));
    }

    @Test
    void canBeInfixOperatorToken_followedByNonPrefixOperatorTokens()
    {
        List<Token> tl = asListOfTokens("a", new Token("+"), new Token("%"), "b");
        assertFalse(new Builder().canBeInfixOperatorToken(tl, 1));
    }

    @Test
    void canBeInfixOperatorToken_followsNonPostfixOperatorTokens()
    {
        List<Token> tl = asListOfTokens("a", new Token("/"), new Token("+"), "b");
        assertFalse(new Builder().canBeInfixOperatorToken(tl, 2));
    }
    //endregion

    //region buildOperatorGroups
    void assertOpGroupHasOpsExactly(Builder.OperatorPriorityGroup actual, Operator... ops)
    {
        List<PrefixOperator> prefixOps = Arrays.stream(ops)
                                               .filter(o -> o instanceof PrefixOperator)
                                               .map(operator -> (PrefixOperator)operator)
                                               .collect(Collectors.toList());

        List<PostfixOperator> postfixOps = Arrays.stream(ops)
                                                 .filter(o -> o instanceof PostfixOperator)
                                                 .map(o -> (PostfixOperator)o)
                                                 .collect(Collectors.toList());

        List<InfixOperator> leftOps = Arrays.stream(ops)
                                            .filter(o ->    o instanceof InfixOperator
                                                         && ((InfixOperator)o).isLeftAssociative)
                                            .map(o -> (InfixOperator)o)
                                            .collect(Collectors.toList());

        List<InfixOperator> rightOps = Arrays.stream(ops)
                                             .filter(o ->    o instanceof InfixOperator
                                                          && !((InfixOperator)o).isLeftAssociative)
                                             .map(o -> (InfixOperator)o)
                                             .collect(Collectors.toList());

        assertThat(actual.prefixOperators.values()).hasSameElementsAs(prefixOps);
        assertThat(actual.postfixOperators.values()).hasSameElementsAs(postfixOps);
        assertThat(actual.leftAssociativeInfixOperators.getItems()).hasSameElementsAs(leftOps);
        assertThat(actual.rightAssociativeInfixOperators.getItems()).hasSameElementsAs(rightOps);
    }

    @Test
    void buildOperatorGroups_noOperators()
    {
        Builder b = new Builder(false);
        b.buildOperatorGroups();
        assertThat(b.getOpGroups()).isEmpty();
        assertThat(b.getOpGroupsInOrder()).isEmpty();
    }

    @Test
    void buildOperatorGroups_oneOperator()
    {
        Builder b = new Builder(false);
        BinaryOperator op = new BinaryOperator(new Token("+"), true, 1.3, (l, r) -> l + r);
        b.addOperator(op);
        b.buildOperatorGroups();

        assertThat(b.getOpGroups()).hasSize(1);
        assertThat(b.getOpGroups()).containsKey(1.3);
        assertOpGroupHasOpsExactly(b.getOpGroups().get(1.3), op);

        assertThat(b.getOpGroupsInOrder()).hasSize(1);
        assertSame(b.getOpGroups().get(1.3), b.getOpGroupsInOrder().get(0));
    }

    @Test
    void buildOperatorGroups_twoOperators_differentPriorities()
    {
        Builder b = new Builder(false);
        BinaryOperator op1 = new BinaryOperator(new Token("+"), true, 1.3, (l, r) -> l + r);
        BinaryOperator op2 = new BinaryOperator(new Token("-"), true, 3.4, (l, r) -> l - r);
        b.addOperator(op1);
        b.addOperator(op2);
        b.buildOperatorGroups();

        assertThat(b.getOpGroups()).hasSize(2);
        assertThat(b.getOpGroups()).containsKeys(1.3, 3.4);
        assertOpGroupHasOpsExactly(b.getOpGroups().get(1.3), op1);
        assertOpGroupHasOpsExactly(b.getOpGroups().get(3.4), op2);

        assertThat(b.getOpGroupsInOrder()).hasSize(2);
        assertSame(b.getOpGroups().get(1.3), b.getOpGroupsInOrder().get(0));
        assertSame(b.getOpGroups().get(3.4), b.getOpGroupsInOrder().get(1));
    }

    @Test
    void buildOperatorGroups_twoOperators_samePriorityDifferentAssociativity()
    {
        Builder b = new Builder(false);
        BinaryOperator op1 = new BinaryOperator(new Token("+"), true, 1.3, (l, r) -> l + r);
        BinaryOperator op2 = new BinaryOperator(new Token("-"), false, 1.3, (l, r) -> l - r);
        b.addOperator(op1);
        b.addOperator(op2);
        b.buildOperatorGroups();

        assertThat(b.getOpGroups()).hasSize(1);
        assertThat(b.getOpGroups()).containsKeys(1.3);
        assertOpGroupHasOpsExactly(b.getOpGroups().get(1.3), op1, op2);

        assertThat(b.getOpGroupsInOrder()).hasSize(1);
        assertSame(b.getOpGroups().get(1.3), b.getOpGroupsInOrder().get(0));
    }

    @Test
    void buildOperatorGroups_twoOperators_samePrioritySameAssociativity()
    {
        Builder b = new Builder(false);
        BinaryOperator op1 = new BinaryOperator(new Token("+"), true, 1.3, (l, r) -> l + r);
        BinaryOperator op2 = new BinaryOperator(new Token("-"), true, 1.3, (l, r) -> l - r);
        b.addOperator(op1);
        b.addOperator(op2);
        b.buildOperatorGroups();

        assertThat(b.getOpGroups()).hasSize(1);
        assertThat(b.getOpGroups()).containsKeys(1.3);
        assertOpGroupHasOpsExactly(b.getOpGroups().get(1.3), op1, op2);

        assertThat(b.getOpGroupsInOrder()).hasSize(1);
        assertSame(b.getOpGroups().get(1.3), b.getOpGroupsInOrder().get(0));
    }
    //endregion

    //region tryParseVariable
    @Test
    void tryParseVariable_notAVariable()
    { assertNull(new Builder().tryParseVariable(newTokenList("doot"))); }

    @Test
    void tryParseVariable_isAVariable()
    {
        Builder b = new Builder().withVariable("doot", 5);
        VariableReference v = b.tryParseVariable(newTokenList("doot"));

        assertNotNull(v);
        assertEquals("doot", v.getName());
        assertEquals(5, v.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseVariable_isAVariableWithSpaces()
    {
        Builder b = new Builder().withVariable("a doot", 5);
        VariableReference v = b.tryParseVariable(newTokenList("a doot"));

        assertNotNull(v);
        assertEquals("a doot", v.getName());
        assertEquals(5, v.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseVariable_isAVariableWithTokenCharacters()
    {
        Builder b = new Builder().withVariable("a  doot", 5);
        VariableReference v = b.tryParseVariable(newTokenList("a", "doot"));

        assertNotNull(v);
        assertEquals("a  doot", v.getName());
        assertEquals(5, v.evaluate(newDummyEquation(b)));
    }
    //endregion

    //region tryParseFunctionCall
    @Test
    void tryParseFunctionCall_notAFunctionCall()
    { assertNull(new Builder().tryParseFunctionCall(newTokenList("doot"))); }

    @Test
    void tryParseFunctionCall_functionCallWithNoArgs()
    {
        Builder b = new Builder().withFunction("doot", x -> 3);
        b.buildOperatorGroups();
        FunctionCall f = b.tryParseFunctionCall(newTokenList("doot", Token.OPEN_BRACKET, Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.getArguments()).isEmpty();
        assertEquals("doot", f.getFunctionName());
        assertEquals(3, f.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseFunctionCall_functionCallWithSpaceWithNoArgs()
    {
        Builder b = new Builder().withFunction("a doot", x -> 3);
        FunctionCall f = b.tryParseFunctionCall(newTokenList("a doot", Token.OPEN_BRACKET,  Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.getArguments()).isEmpty();
        assertEquals("a doot", f.getFunctionName());
        assertEquals(3, f.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseFunctionCall_functionCallWithOpTokensWithArgs()
    {
        Builder b = new Builder().withFunction("doot  +   dat", x -> 3);
        FunctionCall f = b.tryParseFunctionCall(newTokenList("doot",
                                                              new Token("+"),
                                                              "dat",
                                                              Token.OPEN_BRACKET,
                                                              Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.getArguments()).isEmpty();
        assertEquals("doot  +   dat", f.getFunctionName());
        assertEquals(3, f.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseFunctionCall_functionCallWithOneArgument()
    {
        Builder b = new Builder().withFunction("doot", x -> 3).withVariable("x", 5);
        FunctionCall f = b.tryParseFunctionCall(newTokenList("doot", Token.OPEN_BRACKET, "x", Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.getArguments()).hasSize(1);
        assertThat(f.getArguments()[0]).isInstanceOf(VariableReference.class);
        assertEquals(5, f.getArguments()[0].evaluate(newDummyEquation(b)));
        assertEquals("doot", f.getFunctionName());
        assertEquals(3, f.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseFunctionCall_functionCallWithThreeArgs()
    {
        Builder b = new Builder().withFunction("doot", x -> 3).withVariable("x", 5);
        Equation dummy = newDummyEquation(b);
        b.buildOperatorGroups();
        FunctionCall f = b.tryParseFunctionCall(newTokenList("doot",
                                                             Token.OPEN_BRACKET,
                                                             "x",
                                                             Token.ARGUMENT_SEPARATOR,
                                                             "x",
                                                             Token.ARGUMENT_SEPARATOR,
                                                             "x",
                                                             Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.getArguments()).hasSize(3);
        assertThat(f.getArguments()[0]).isInstanceOf(VariableReference.class);
        assertThat(f.getArguments()[1]).isInstanceOf(VariableReference.class);
        assertThat(f.getArguments()[2]).isInstanceOf(VariableReference.class);
        assertEquals(5, f.getArguments()[0].evaluate(dummy));
        assertEquals(5, f.getArguments()[1].evaluate(dummy));
        assertEquals(5, f.getArguments()[2].evaluate(dummy));
        assertEquals("doot", f.getFunctionName());
        assertEquals(3, f.evaluate(dummy));
    }

    @Test
    void tryParseFunctionCall_functionCallWithThreeArgsWhereOneIsFunctionCall()
    {
        Builder b = new Builder().withFunction("doot", x -> 3).withVariable("x", 5);
        Equation dummy = newDummyEquation(b);
        b.buildOperatorGroups();
        FunctionCall f = b.tryParseFunctionCall(newTokenList("doot",
                                                             Token.OPEN_BRACKET,
                                                             "x",
                                                             Token.ARGUMENT_SEPARATOR,
                                                             "doot",
                                                             Token.OPEN_BRACKET,
                                                             Token.CLOSE_BRACKET,
                                                             Token.ARGUMENT_SEPARATOR,
                                                             "x",
                                                             Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.getArguments()).hasSize(3);
        assertThat(f.getArguments()[0]).isInstanceOf(VariableReference.class);
        assertThat(f.getArguments()[1]).isInstanceOf(FunctionCall.class);
        assertThat(f.getArguments()[2]).isInstanceOf(VariableReference.class);
        assertEquals(5, f.getArguments()[0].evaluate(dummy));
        assertEquals(3, f.getArguments()[1].evaluate(dummy));
        assertEquals(5, f.getArguments()[2].evaluate(dummy));
        assertEquals("doot", f.getFunctionName());
        assertEquals(3, f.evaluate(dummy));
    }

    @Test
    void tryParseFunctionCall_functionCallWithThreeArgsWhereOneIsInBrackets()
    {
        Builder b = new Builder().withFunction("doot", x -> 3).withVariable("x", 5);
        Equation dummy = newDummyEquation(b);
        b.buildOperatorGroups();
        FunctionCall f = b.tryParseFunctionCall(newTokenList("doot",
                                                             Token.OPEN_BRACKET,
                                                             "x",
                                                             Token.ARGUMENT_SEPARATOR,
                                                             Token.OPEN_BRACKET,
                                                             "x",
                                                             Token.CLOSE_BRACKET,
                                                             Token.ARGUMENT_SEPARATOR,
                                                             "x",
                                                             Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.getArguments()).hasSize(3);
        assertThat(f.getArguments()[0]).isInstanceOf(VariableReference.class);
        assertThat(f.getArguments()[1]).isInstanceOf(VariableReference.class);
        assertThat(f.getArguments()[2]).isInstanceOf(VariableReference.class);
        assertEquals(5, f.getArguments()[0].evaluate(dummy));
        assertEquals(5, f.getArguments()[1].evaluate(dummy));
        assertEquals(5, f.getArguments()[2].evaluate(dummy));
        assertEquals("doot", f.getFunctionName());
        assertEquals(3, f.evaluate(dummy));
    }

    @Test
    void tryParseFunctionCall_emptyArg()
    {
        Builder b = new Builder().withFunction("doot", 3, args -> args[0] + args[1] * 3 + args[2] * 5)
                                 .withVariable("x", 7)
                                 .withVariable("y", 11);

        Equation dummy = newDummyEquation(b);
        b.buildOperatorGroups();

        assertThrows(Builder.EmptyFunctionArgumentException.class,
                     () -> b.tryParseFunctionCall(newTokenList("doot",
                                                               Token.OPEN_BRACKET,
                                                               "x",
                                                               Token.ARGUMENT_SEPARATOR,
                                                               Token.ARGUMENT_SEPARATOR,
                                                               "y",
                                                               Token.CLOSE_BRACKET)));
    }

    @Test
    void tryParseFunctionCall_unrecognisedFunction()
    {
        assertThrows(Builder.UnrecognisedFunctionException.class,
                     () -> new Builder().withFunction("doot", x -> 3)
                                             .tryParseFunctionCall(newTokenList("dat",
                                                                                Token.OPEN_BRACKET,
                                                                                Token.CLOSE_BRACKET)));
    }

    //endregion

    //region tryParseNumber
    @Test
    void tryParseNumber_notANumber()
    { assertNull(new Builder().tryParseNumber(newTokenList("doot"))); }

    @Test
    void tryParseNumber_isANumber()
    {
        Builder b = new Builder();
        LiteralNumber ln = b.tryParseNumber(newTokenList("7.3"));
        assertNotNull(ln);
        assertEquals(7.3, ln.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseNumber_isNumberMadeUpOfMultipleTokens()
    {
        Builder b = new Builder();
        LiteralNumber ln = b.tryParseNumber(new TokenList("7.3",
                                                          Arrays.asList(new UntokenisedString("7"),
                                                                        new Token("."),
                                                                        new UntokenisedString("3")),
                                                          Arrays.asList(0, 0, 0, 0)));
        assertNotNull(ln);
        assertEquals(7.3, ln.evaluate(newDummyEquation(b)));
    }
    //endregion

    //region tryParseInfixOperation_rightAssociative
    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperation()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("^"), "b", new Token("*"), "c", new Token("&"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup, 0);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperationButHasFirstFewTokensOfIt()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("&"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup, 0);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperationBecauseOneOfTheTokensCantBe()
    {
        Builder b = newBuilderWithInfixOp(false).withOperator("+", (l, r) -> l + r);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("+"), new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup, 0);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperationBecauseOneOfTheTokensIsInBrackets()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("£"), Token.OPEN_BRACKET, "b", new Token("€"), "c",
                                    Token.CLOSE_BRACKET, new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup, 0);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_rightAssociative_isAnInfixOperation()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup, 0);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.rightAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.getAction());
        List<EquationComponent> components = op.getComponents();
        assertThat(components).hasSize(4);
        assertThat(components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)components.get(0)).getName());
        assertEquals("b", ((VariableReference)components.get(1)).getName());
        assertEquals("c", ((VariableReference)components.get(2)).getName());
        assertEquals("d", ((VariableReference)components.get(3)).getName());
        assertEquals(114.0, op.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseInfixOperation_rightAssociative_isAnInfixOperationFollowedBySameOne()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("¢"),
                                    "d", new Token("£"), "c", new Token("€"), "b", new Token("¢"), "a");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup, 0);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.rightAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.getAction());
        List<EquationComponent> components = op.getComponents();
        assertThat(components).hasSize(4);
        assertThat(components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(components.get(3)).isInstanceOf(Operation.class);
        assertEquals("a", ((VariableReference)components.get(0)).getName());
        assertEquals("b", ((VariableReference)components.get(1)).getName());
        assertEquals("c", ((VariableReference)components.get(2)).getName());

        Operation innerOp = (Operation)components.get(3);
        List<EquationComponent> innerOpComponents = innerOp.getComponents();
        assertEquals(expectedA, innerOp.getAction());
        assertThat(innerOpComponents).hasSize(4);
        assertThat(innerOpComponents.get(0)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(1)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(2)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("d", ((VariableReference)innerOpComponents.get(0)).getName());
        assertEquals("c", ((VariableReference)innerOpComponents.get(1)).getName());
        assertEquals("b", ((VariableReference)innerOpComponents.get(2)).getName());
        assertEquals("a", ((VariableReference)innerOpComponents.get(3)).getName());

        assertEquals(94.0, innerOp.evaluate(newDummyEquation(b)));
        assertEquals(716.0, op.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseInfixOperation_rightAssociative_isAnInfixOperationContainingSameOneAsAnOperand()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a",
                                    new Token("£"),
                                    "b",
                                    new Token("€"),
                                    "c", new Token("£"), "d", new Token("€"), "c", new Token("¢"), "b",
                                    new Token("¢"),
                                    "a");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup, 0);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.rightAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.getAction());
        List<EquationComponent> components = op.getComponents();
        assertThat(components).hasSize(4);
        assertThat(components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(components.get(2)).isInstanceOf(Operation.class);
        assertThat(components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)components.get(0)).getName());
        assertEquals("b", ((VariableReference)components.get(1)).getName());
        assertEquals("a", ((VariableReference)components.get(3)).getName());

        Operation innerOp = (Operation)components.get(2);
        List<EquationComponent> innerOpComponents = innerOp.getComponents();
        assertEquals(expectedA, innerOp.getAction());
        assertThat(innerOpComponents).hasSize(4);
        assertThat(innerOpComponents.get(0)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(1)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(2)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("c", ((VariableReference)innerOpComponents.get(0)).getName());
        assertEquals("d", ((VariableReference)innerOpComponents.get(1)).getName());
        assertEquals("c", ((VariableReference)innerOpComponents.get(2)).getName());
        assertEquals("b", ((VariableReference)innerOpComponents.get(3)).getName());

        assertEquals(108.0, innerOp.evaluate(newDummyEquation(b)));
        assertEquals(598.0, op.evaluate(newDummyEquation(b)));
    }
    //endregion

    //region tryParseInfixOperation_leftAssociative
    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperation()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("^"), "b", new Token("*"), "c", new Token("&"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup, 0);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperationButHasLastFewTokensOfIt()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("^"), "b", new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup, 0);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperationBecauseOneOfTheTokensCantBe()
    {
        Builder b = newBuilderWithInfixOp(true).withOperator("+", (l, r) -> l + r);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("+"), new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup, 0);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperationBecauseOneOfTheTokensIsInBrackets()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("£"), Token.OPEN_BRACKET, "b", new Token("€"), "c",
                                    Token.CLOSE_BRACKET, new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup, 0);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_leftAssociative_isAnInfixOperation()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup, 0);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.leftAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.getAction());
        List<EquationComponent> components = op.getComponents();
        assertThat(components).hasSize(4);
        assertThat(components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)components.get(0)).getName());
        assertEquals("b", ((VariableReference)components.get(1)).getName());
        assertEquals("c", ((VariableReference)components.get(2)).getName());
        assertEquals("d", ((VariableReference)components.get(3)).getName());
        assertEquals(114.0, op.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseInfixOperation_leftAssociative_isAnInfixOperationFollowingSameOne()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("¢"),
                                    "d", new Token("£"), "c", new Token("€"), "b", new Token("¢"), "a");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup, 0);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.leftAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.getAction());
        List<EquationComponent> components = op.getComponents();
        assertThat(components).hasSize(4);
        assertThat(components.get(0)).isInstanceOf(Operation.class);
        assertThat(components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("c", ((VariableReference)components.get(1)).getName());
        assertEquals("b", ((VariableReference)components.get(2)).getName());
        assertEquals("a", ((VariableReference)components.get(3)).getName());

        Operation innerOp = (Operation)components.get(0);
        List<EquationComponent> innerOpComponents = innerOp.getComponents();
        assertEquals(expectedA, innerOp.getAction());
        assertThat(innerOpComponents).hasSize(4);
        assertThat(innerOpComponents.get(0)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(1)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(2)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)innerOpComponents.get(0)).getName());
        assertEquals("b", ((VariableReference)innerOpComponents.get(1)).getName());
        assertEquals("c", ((VariableReference)innerOpComponents.get(2)).getName());
        assertEquals("d", ((VariableReference)innerOpComponents.get(3)).getName());

        assertEquals(114.0, innerOp.evaluate(newDummyEquation(b)));
        assertEquals(200.0, op.evaluate(newDummyEquation(b)));
    }

    @Test
    void tryParseInfixOperation_leftAssociative_isAnInfixOperationContainingSameOneAsAnOperand()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("a",
                                    new Token("£"),
                                    "b",
                                    new Token("€"),
                                    "c", new Token("£"), "d", new Token("€"), "c", new Token("¢"), "b",
                                    new Token("¢"),
                                    "a");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup, 0);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.leftAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.getAction());
        List<EquationComponent> components = op.getComponents();
        assertThat(components).hasSize(4);
        assertThat(components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(components.get(2)).isInstanceOf(Operation.class);
        assertThat(components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)components.get(0)).getName());
        assertEquals("b", ((VariableReference)components.get(1)).getName());
        assertEquals("a", ((VariableReference)components.get(3)).getName());

        Operation innerOp = (Operation)components.get(2);
        List<EquationComponent> innerOpComponents = innerOp.getComponents();
        assertEquals(expectedA, innerOp.getAction());
        assertThat(innerOpComponents).hasSize(4);
        assertThat(innerOpComponents.get(0)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(1)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(2)).isInstanceOf(VariableReference.class);
        assertThat(innerOpComponents.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("c", ((VariableReference)innerOpComponents.get(0)).getName());
        assertEquals("d", ((VariableReference)innerOpComponents.get(1)).getName());
        assertEquals("c", ((VariableReference)innerOpComponents.get(2)).getName());
        assertEquals("b", ((VariableReference)innerOpComponents.get(3)).getName());

        assertEquals(108.0, innerOp.evaluate(newDummyEquation(b)));
        assertEquals(598.0, op.evaluate(newDummyEquation(b)));
    }
    //endregion
    
    //region tryParsePrefixOperation
    @Test
    void tryParsePrefixOperation_notAPrefixOperation()
    {
        Builder b = new Builder(false).withPrefixOperator("£", o -> o * 3);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("x", new Token("£"), new NumberToken("7", 7.0));

        Operation op = b.tryParsePrefixOperation(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParsePrefixOperation_isAPrefixOperation()
    {
        Builder b = new Builder(false).withPrefixOperator("£", o -> o * 3);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList(new Token("£"), new NumberToken("7", 7.0));

        Operation op = b.tryParsePrefixOperation(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.prefixOperators.values().stream().findFirst().get().action;
        assertEquals(expectedA, op.getAction());
        List<EquationComponent> components = op.getComponents();
        assertThat(components).hasSize(1);
        EquationComponent operand = components.get(0);
        assertThat(operand).isInstanceOf(LiteralNumber.class);
        assertEquals(7.0, ((LiteralNumber)operand).getValue());
        assertEquals(21.0, op.evaluate(newDummyEquation(b)));
    }
    //endregion

    //region tryParsePostfixOperation
    @Test
    void tryParsePostfixOperation_notAPostfixOperation()
    {
        Builder b = new Builder(false).withPostfixOperator("£", o -> o * 3);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList("x", new Token("£"), new NumberToken("7", 7.0));

        Operation op = b.tryParsePrefixOperation(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParsePostfixOperation_isAPostfixOperation()
    {
        Builder b = new Builder(false).withPostfixOperator("£", o -> o * 3);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.getOpGroupsInOrder().get(0);
        TokenList tl = newTokenList(new NumberToken("7", 7.0), new Token("£"));

        Operation op = b.tryParsePostfixOperation(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.postfixOperators.values().stream().findFirst().get().action;
        assertEquals(expectedA, op.getAction());
        List<EquationComponent> components = op.getComponents();
        assertThat(components).hasSize(1);
        EquationComponent operand = components.get(0);
        assertThat(operand).isInstanceOf(LiteralNumber.class);
        assertEquals(7.0, ((LiteralNumber)operand).getValue());
        assertEquals(21.0, op.evaluate(newDummyEquation(b)));
    }
    //endregion
}
