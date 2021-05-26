package scot.massie.lib.maths;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.function.Executable;
import scot.massie.lib.collections.tree.Tree;
import scot.massie.lib.maths.EquationEvaluator.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

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

    Builder newBuilderWithInfixOp(boolean shouldBeLeftAssociative)
    {
        Builder result = new Builder("2+2", false).withVariable("a", 5)
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
        assertEquals(tokens.subList(lowerBound, upperBound + 1), opRun.tokens);
        assertEquals(lowerBound, opRun.startIndexInSource);
        assertEquals(upperBound, opRun.endIndexInSource);
        assertEquals(at - opRun.startIndexInSource, opRun.indexOfPivotInRun);
        assertEquals(tokens.subList(lowerBound, at), opRun.tokensBeforePivot);
        assertEquals(tokens.subList(at + 1, upperBound + 1), opRun.tokensAfterPivot);
    }

    //region verifyTokenisationBrackets
    @Test
    void verifyTokenisationBrackets_noBrackets()
    {
        assertDoesNotThrow(() ->
        {
            TokenList tl = newTokenList("a", "b", "c");
            new Builder("2+2").verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_brackets()
    {
        assertDoesNotThrow(() ->
        {
            TokenList tl = newTokenList("a", Token.OPEN_BRACKET, "b", Token.CLOSE_BRACKET, "c");
            new Builder("2+2").verifyTokenisationBrackets(tl);
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
            new Builder("2+2").verifyTokenisationBrackets(tl);
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
            new Builder("2+2").verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_singleOpenBracket()
    {
        assertThrows(Builder.UnmatchedOpenBracketException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.OPEN_BRACKET);
            new Builder("2+2").verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_singleCloseBracket()
    {
        assertThrows(Builder.UnexpectedCloseBracketException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.CLOSE_BRACKET);
            new Builder("2+2").verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_bracketsWithExtraOpen()
    {
        assertThrows(Builder.UnmatchedOpenBracketException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.OPEN_BRACKET, Token.OPEN_BRACKET, "b", Token.CLOSE_BRACKET);
            new Builder("2+2").verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_bracketsWithExtraClose()
    {
        assertThrows(Builder.UnexpectedCloseBracketException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.OPEN_BRACKET, "b", Token.CLOSE_BRACKET, Token.CLOSE_BRACKET);
            new Builder("2+2").verifyTokenisationBrackets(tl);
        });
    }

    @Test
    void verifyTokenisationBrackets_unmatchedCloseFollowedByUnmatchedOpen()
    {
        assertThrows(Builder.BracketMismatchException.class, () ->
        {
            TokenList tl = newTokenList("a", Token.CLOSE_BRACKET, Token.OPEN_BRACKET, "b");
            new Builder("2+2").verifyTokenisationBrackets(tl);
        });
    }
    //endregion
    
    //region startsWithNonPrefixOperator
    @Test
    void startsWithNonPrefixOperator_startsWithPrefixOperator()
    { assertFalse(new Builder("2+2").startsWithNonPrefixOperator(newTokenList(new Token("-"), "a"))); }

    @Test
    void startsWithNonPrefixOperator_startsWithInfixOperator()
    { assertTrue(new Builder("2+2").startsWithNonPrefixOperator(newTokenList(new Token("/"), "a"))); }

    @Test
    void startsWithNonPrefixOperator_startsWithNonOperator()
    { assertFalse(new Builder("2+2").startsWithNonPrefixOperator(newTokenList("a", "b"))); }
    //endregion

    //region endsWithNonPostfixOperator
    @Test
    void endsWithNonPostfixOperator_endsWithPostfixOperator()
    { assertFalse(new Builder("2+2").endsWithNonPostfixOperator(newTokenList("a", new Token("%")))); }

    @Test
    void endsWithNonPostfixOperator_endsWithInfixOperator()
    { assertTrue(new Builder("2+2").endsWithNonPostfixOperator(newTokenList("a", new Token("/")))); }

    @Test
    void endsWithNonPostfixOperator_endsWithNonOperator()
    { assertFalse(new Builder("2+2").endsWithNonPostfixOperator(newTokenList("a", "b"))); }
    //endregion

    //region getOpRun

    // Note that Builder.getOpRun(...) assumes that the index it's given is a valid operator character and does not
    // check whether or not this is true.

    @Test
    void getOpRun_tokenRunIn()
    {
        List<Token> ts = asListOfTokens("a", new Token("+"), new Token("-"), new Token("/"), "b");
        Builder.OperatorTokenRun opRun = new Builder("2+2").getOpRun(ts, 2);
        assertOpRun(opRun, ts, 1, 2, 3);
    }

    @Test
    void getOpRun_tokenRunAtStart()
    {
        List<Token> ts = asListOfTokens(new Token("+"), new Token("-"), new Token("/"), "b");
        Builder.OperatorTokenRun opRun = new Builder("2+2").getOpRun(ts, 1);
        assertOpRun(opRun, ts, 0, 1, 2);
    }

    @Test
    void getOpRun_tokenRunAtEnd()
    {
        List<Token> ts = asListOfTokens("a", new Token("+"), new Token("-"), new Token("/"));
        Builder.OperatorTokenRun opRun = new Builder("2+2").getOpRun(ts, 2);
        assertOpRun(opRun, ts, 1, 2, 3);
    }

    @Test
    void getOpRun_tokenRunIsEntire()
    {
        List<Token> ts = asListOfTokens(new Token("+"), new Token("-"), new Token("/"));
        Builder.OperatorTokenRun opRun = new Builder("2+2").getOpRun(ts, 1);
        assertOpRun(opRun, ts, 0, 1, 2);
    }

    @Test
    void getOpRun_tokenRunIsOneToken()
    {
        List<Token> ts = asListOfTokens("a", new Token("+"), "b");
        Builder.OperatorTokenRun opRun = new Builder("2+2").getOpRun(ts, 1);
        assertOpRun(opRun, ts, 1, 1, 1);
    }

    @Test
    void getOpRun_tokenRunIsOneTokenAtStart()
    {
        List<Token> ts = asListOfTokens(new Token("+"), "b");
        Builder.OperatorTokenRun opRun = new Builder("2+2").getOpRun(ts, 0);
        assertOpRun(opRun, ts, 0, 0, 0);
    }

    @Test
    void getOpRun_tokenRunIsOneTokenAtEnd()
    {
        List<Token> ts = asListOfTokens("a", new Token("+"));
        Builder.OperatorTokenRun opRun = new Builder("2+2").getOpRun(ts, 1);
        assertOpRun(opRun, ts, 1, 1, 1);
    }

    @Test
    void getOpRun_tokenRunIsOneTokenThatIsEntire()
    {
        List<Token> ts = asListOfTokens(new Token("+"));
        Builder.OperatorTokenRun opRun = new Builder("2+2").getOpRun(ts, 0);
        assertOpRun(opRun, ts, 0, 0, 0);
    }
    //endregion

    //region canBeInfixOperatorToken
    @Test
    void canBeInfixOperatorToken_singleToken()
    {
        List<Token> tl = asListOfTokens("a", new Token("+"), "b");
        assertTrue(new Builder("2+2").canBeInfixOperatorToken(tl, 1));
    }

    @Test
    void canBeInfixOperatorToken_followedByPrefix()
    {
        List<Token> tl = asListOfTokens("a", new Token("+"), new Token("-"), "b");
        assertTrue(new Builder("2+2").canBeInfixOperatorToken(tl, 1));
    }

    @Test
    void canBeInfixOperatorToken_followingPostfix()
    {
        List<Token> tl = asListOfTokens("a", new Token("%"), new Token("+"), "b");
        assertTrue(new Builder("2+2").canBeInfixOperatorToken(tl, 2));
    }

    @Test
    void canBeInfixOperatorToken_runAtStart()
    {
        List<Token> tl = asListOfTokens(new Token("+"), "b");
        assertFalse(new Builder("2+2").canBeInfixOperatorToken(tl, 0));
    }

    @Test
    void canBeInfixOperatorToken_runAtEnd()
    {
        List<Token> tl = asListOfTokens("a", new Token("+"));
        assertFalse(new Builder("2+2").canBeInfixOperatorToken(tl, 1));
    }

    @Test
    void canBeInfixOperatorToken_runIsEntire()
    {
        List<Token> tl = asListOfTokens(new Token("+"));
        assertFalse(new Builder("2+2").canBeInfixOperatorToken(tl, 0));
    }

    @Test
    void canBeInfixOperatorToken_followedByNonPrefixOperatorTokens()
    {
        List<Token> tl = asListOfTokens("a", new Token("+"), new Token("%"), "b");
        assertFalse(new Builder("2+2").canBeInfixOperatorToken(tl, 1));
    }

    @Test
    void canBeInfixOperatorToken_followsNonPostfixOperatorTokens()
    {
        List<Token> tl = asListOfTokens("a", new Token("/"), new Token("+"), "b");
        assertFalse(new Builder("2+2").canBeInfixOperatorToken(tl, 2));
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
        Builder b = new Builder("2+2", false);
        b.buildOperatorGroups();
        assertThat(b.operatorGroups).isEmpty();
        assertThat(b.operatorGroupsInOrder).isEmpty();
    }

    @Test
    void buildOperatorGroups_oneOperator()
    {
        Builder b = new Builder("2+2", false);
        BinaryOperator op = new BinaryOperator(new Token("+"), true, 1.3, (l, r) -> l + r);
        b.addOperator(op);
        b.buildOperatorGroups();

        assertThat(b.operatorGroups).hasSize(1);
        assertThat(b.operatorGroups).containsKey(1.3);
        assertOpGroupHasOpsExactly(b.operatorGroups.get(1.3), op);

        assertThat(b.operatorGroupsInOrder).hasSize(1);
        assertSame(b.operatorGroups.get(1.3), b.operatorGroupsInOrder.get(0));
    }

    @Test
    void buildOperatorGroups_twoOperators_differentPriorities()
    {
        Builder b = new Builder("2+2", false);
        BinaryOperator op1 = new BinaryOperator(new Token("+"), true, 1.3, (l, r) -> l + r);
        BinaryOperator op2 = new BinaryOperator(new Token("-"), true, 3.4, (l, r) -> l - r);
        b.addOperator(op1);
        b.addOperator(op2);
        b.buildOperatorGroups();

        assertThat(b.operatorGroups).hasSize(2);
        assertThat(b.operatorGroups).containsKeys(1.3, 3.4);
        assertOpGroupHasOpsExactly(b.operatorGroups.get(1.3), op1);
        assertOpGroupHasOpsExactly(b.operatorGroups.get(3.4), op2);

        assertThat(b.operatorGroupsInOrder).hasSize(2);
        assertSame(b.operatorGroups.get(1.3), b.operatorGroupsInOrder.get(0));
        assertSame(b.operatorGroups.get(3.4), b.operatorGroupsInOrder.get(1));
    }

    @Test
    void buildOperatorGroups_twoOperators_samePriorityDifferentAssociativity()
    {
        Builder b = new Builder("2+2", false);
        BinaryOperator op1 = new BinaryOperator(new Token("+"), true, 1.3, (l, r) -> l + r);
        BinaryOperator op2 = new BinaryOperator(new Token("-"), false, 1.3, (l, r) -> l - r);
        b.addOperator(op1);
        b.addOperator(op2);
        b.buildOperatorGroups();

        assertThat(b.operatorGroups).hasSize(1);
        assertThat(b.operatorGroups).containsKeys(1.3);
        assertOpGroupHasOpsExactly(b.operatorGroups.get(1.3), op1, op2);

        assertThat(b.operatorGroupsInOrder).hasSize(1);
        assertSame(b.operatorGroups.get(1.3), b.operatorGroupsInOrder.get(0));
    }

    @Test
    void buildOperatorGroups_twoOperators_samePrioritySameAssociativity()
    {
        Builder b = new Builder("2+2", false);
        BinaryOperator op1 = new BinaryOperator(new Token("+"), true, 1.3, (l, r) -> l + r);
        BinaryOperator op2 = new BinaryOperator(new Token("-"), true, 1.3, (l, r) -> l - r);
        b.addOperator(op1);
        b.addOperator(op2);
        b.buildOperatorGroups();

        assertThat(b.operatorGroups).hasSize(1);
        assertThat(b.operatorGroups).containsKeys(1.3);
        assertOpGroupHasOpsExactly(b.operatorGroups.get(1.3), op1, op2);

        assertThat(b.operatorGroupsInOrder).hasSize(1);
        assertSame(b.operatorGroups.get(1.3), b.operatorGroupsInOrder.get(0));
    }
    //endregion

    //region tryParseVariable
    @Test
    void tryParseVariable_notAVariable()
    { assertNull(new Builder("2+2").tryParseVariable(newTokenList("doot"))); }

    @Test
    void tryParseVariable_isAVariable()
    {
        VariableReference v = new Builder("2+2").withVariable("doot", 5).tryParseVariable(newTokenList("doot"));

        assertNotNull(v);
        assertEquals("doot", v.name);
        assertEquals(5, v.evaluate());
    }

    @Test
    void tryParseVariable_isAVariableWithSpaces()
    {
        VariableReference v = new Builder("2+2").withVariable("a doot", 5).tryParseVariable(newTokenList("a doot"));

        assertNotNull(v);
        assertEquals("a doot", v.name);
        assertEquals(5, v.evaluate());
    }

    @Test
    void tryParseVariable_isAVariableWithTokenCharacters()
    {
        VariableReference v = new Builder("2+2").withVariable("a  doot", 5).tryParseVariable(newTokenList("a", "doot"));

        assertNotNull(v);
        assertEquals("a  doot", v.name);
        assertEquals(5, v.evaluate());
    }
    //endregion

    //region tryParseFunctionCall
    @Test
    void tryParseFunctionCall_notAFunctionCall()
    { assertNull(new Builder("2+2").tryParseFunctionCall(newTokenList("doot"))); }

    @Test
    void tryParseFunctionCall_functionCallWithNoArgs()
    {
        Builder b = new Builder("2+2").withFunction("doot", x -> 3);
        b.buildOperatorGroups();
        FunctionCall f = b.tryParseFunctionCall(newTokenList("doot", Token.OPEN_BRACKET, Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.arguments).isEmpty();
        assertEquals("doot", f.functionName);
        assertEquals(3, f.evaluate());
    }

    @Test
    void tryParseFunctionCall_functionCallWithSpaceWithNoArgs()
    {
        FunctionCall f = new Builder("2+2")
                                 .withFunction("a doot", x -> 3)
                                 .tryParseFunctionCall(newTokenList("a doot",
                                                                    Token.OPEN_BRACKET,
                                                                    Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.arguments).isEmpty();
        assertEquals("a doot", f.functionName);
        assertEquals(3, f.evaluate());
    }

    @Test
    void tryParseFunctionCall_functionCallWithOpTokensWithArgs()
    {
        FunctionCall f = new Builder("2+2")
                                 .withFunction("doot  +   dat", x -> 3)
                                 .tryParseFunctionCall(newTokenList("doot",
                                                                    new Token("+"),
                                                                    "dat",
                                                                    Token.OPEN_BRACKET,
                                                                    Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.arguments).isEmpty();
        assertEquals("doot  +   dat", f.functionName);
        assertEquals(3, f.evaluate());
    }

    @Test
    void tryParseFunctionCall_functionCallWithOneArgument()
    {
        FunctionCall f = new Builder("2+2")
                                 .withFunction("doot", x -> 3)
                                 .withVariable("x", 5)
                                 .tryParseFunctionCall(newTokenList("doot",
                                                                    Token.OPEN_BRACKET,
                                                                    "x",
                                                                    Token.CLOSE_BRACKET));

        assertNotNull(f);
        assertThat(f.arguments).hasSize(1);
        assertThat(f.arguments[0]).isInstanceOf(VariableReference.class);
        assertEquals(5, f.arguments[0].evaluate());
        assertEquals("doot", f.functionName);
        assertEquals(3, f.evaluate());
    }

    @Test
    void tryParseFunctionCall_functionCallWithThreeArgs()
    {
        Builder b = new Builder("2+2").withFunction("doot", x -> 3).withVariable("x", 5);
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
        assertThat(f.arguments).hasSize(3);
        assertThat(f.arguments[0]).isInstanceOf(VariableReference.class);
        assertThat(f.arguments[1]).isInstanceOf(VariableReference.class);
        assertThat(f.arguments[2]).isInstanceOf(VariableReference.class);
        assertEquals(5, f.arguments[0].evaluate());
        assertEquals(5, f.arguments[1].evaluate());
        assertEquals(5, f.arguments[2].evaluate());
        assertEquals("doot", f.functionName);
        assertEquals(3, f.evaluate());
    }

    @Test
    void tryParseFunctionCall_functionCallWithThreeArgsWhereOneIsFunctionCall()
    {
        Builder b = new Builder("2+2").withFunction("doot", x -> 3).withVariable("x", 5);
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
        assertThat(f.arguments).hasSize(3);
        assertThat(f.arguments[0]).isInstanceOf(VariableReference.class);
        assertThat(f.arguments[1]).isInstanceOf(FunctionCall.class);
        assertThat(f.arguments[2]).isInstanceOf(VariableReference.class);
        assertEquals(5, f.arguments[0].evaluate());
        assertEquals(3, f.arguments[1].evaluate());
        assertEquals(5, f.arguments[2].evaluate());
        assertEquals("doot", f.functionName);
        assertEquals(3, f.evaluate());
    }

    @Test
    void tryParseFunctionCall_functionCallWithThreeArgsWhereOneIsInBrackets()
    {
        Builder b = new Builder("2+2").withFunction("doot", x -> 3).withVariable("x", 5);
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
        assertThat(f.arguments).hasSize(3);
        assertThat(f.arguments[0]).isInstanceOf(VariableReference.class);
        assertThat(f.arguments[1]).isInstanceOf(VariableReference.class);
        assertThat(f.arguments[2]).isInstanceOf(VariableReference.class);
        assertEquals(5, f.arguments[0].evaluate());
        assertEquals(5, f.arguments[1].evaluate());
        assertEquals(5, f.arguments[2].evaluate());
        assertEquals("doot", f.functionName);
        assertEquals(3, f.evaluate());
    }

    @Test
    void tryParseFunctionCall_unrecognisedFunction()
    {
        assertThrows(Builder.UnrecognisedFunctionException.class,
                     () -> new Builder("2+2").withFunction("doot", x -> 3)
                                             .tryParseFunctionCall(newTokenList("dat",
                                                                                Token.OPEN_BRACKET,
                                                                                Token.CLOSE_BRACKET)));
    }

    //endregion

    //region tryParseNumber
    @Test
    void tryParseNumber_notANumber()
    { assertNull(new Builder("2+2").tryParseNumber(newTokenList("doot"))); }

    @Test
    void tryParseNumber_isANumber()
    {
        LiteralNumber ln = new Builder("2+2").tryParseNumber(newTokenList("7.3"));
        assertNotNull(ln);
        assertEquals(7.3, ln.evaluate());
    }

    @Test
    void tryParseNumber_isNumberMadeUpOfMultipleTokens()
    {
        LiteralNumber ln = new Builder("2+2").tryParseNumber(new TokenList("7.3",
                                                                           Arrays.asList(new UntokenisedString("7"),
                                                                                         new Token("."),
                                                                                         new UntokenisedString("3")),
                                                                           Arrays.asList(0, 0, 0, 0)));
        assertNotNull(ln);
        assertEquals(7.3, ln.evaluate());
    }
    //endregion

    //region tryParseInfixOperation_rightAssociative
    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperation()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("^"), "b", new Token("*"), "c", new Token("&"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperationButHasFirstFewTokensOfIt()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("&"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperationBecauseOneOfTheTokensCantBe()
    {
        Builder b = newBuilderWithInfixOp(false).withOperator("+", (l, r) -> l + r);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("+"), new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_rightAssociative_notAnInfixOperationBecauseOneOfTheTokensIsInBrackets()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("£"), Token.OPEN_BRACKET, "b", new Token("€"), "c",
                                    Token.CLOSE_BRACKET, new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_rightAssociative_isAnInfixOperation()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.rightAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.action);
        assertThat(op.components).hasSize(4);
        assertThat(op.components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)op.components.get(0)).name);
        assertEquals("b", ((VariableReference)op.components.get(1)).name);
        assertEquals("c", ((VariableReference)op.components.get(2)).name);
        assertEquals("d", ((VariableReference)op.components.get(3)).name);
        assertSame(b.variables, ((VariableReference)op.components.get(0)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(2)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(3)).variableValues);
        assertEquals(114.0, op.evaluate());
    }

    @Test
    void tryParseInfixOperation_rightAssociative_isAnInfixOperationFollowedBySameOne()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("¢"),
                                    "d", new Token("£"), "c", new Token("€"), "b", new Token("¢"), "a");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.rightAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.action);
        assertThat(op.components).hasSize(4);
        assertThat(op.components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(3)).isInstanceOf(Operation.class);
        assertEquals("a", ((VariableReference)op.components.get(0)).name);
        assertEquals("b", ((VariableReference)op.components.get(1)).name);
        assertEquals("c", ((VariableReference)op.components.get(2)).name);
        assertSame(b.variables, ((VariableReference)op.components.get(0)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(2)).variableValues);

        Operation innerOp = (Operation)op.components.get(3);
        assertEquals(expectedA, innerOp.action);
        assertThat(innerOp.components).hasSize(4);
        assertThat(innerOp.components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("d", ((VariableReference)innerOp.components.get(0)).name);
        assertEquals("c", ((VariableReference)innerOp.components.get(1)).name);
        assertEquals("b", ((VariableReference)innerOp.components.get(2)).name);
        assertEquals("a", ((VariableReference)innerOp.components.get(3)).name);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(0)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(2)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(3)).variableValues);

        assertEquals(94.0, innerOp.evaluate());
        assertEquals(716.0, op.evaluate());
    }

    @Test
    void tryParseInfixOperation_rightAssociative_isAnInfixOperationContainingSameOneAsAnOperand()
    {
        Builder b = newBuilderWithInfixOp(false);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a",
                                    new Token("£"),
                                    "b",
                                    new Token("€"),
                                    "c", new Token("£"), "d", new Token("€"), "c", new Token("¢"), "b",
                                    new Token("¢"),
                                    "a");

        Operation op = b.tryParseInfixOperation_rightAssociative(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.rightAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.action);
        assertThat(op.components).hasSize(4);
        assertThat(op.components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(2)).isInstanceOf(Operation.class);
        assertThat(op.components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)op.components.get(0)).name);
        assertEquals("b", ((VariableReference)op.components.get(1)).name);
        assertEquals("a", ((VariableReference)op.components.get(3)).name);
        assertSame(b.variables, ((VariableReference)op.components.get(0)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(3)).variableValues);

        Operation innerOp = (Operation)op.components.get(2);
        assertEquals(expectedA, innerOp.action);
        assertThat(innerOp.components).hasSize(4);
        assertThat(innerOp.components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("c", ((VariableReference)innerOp.components.get(0)).name);
        assertEquals("d", ((VariableReference)innerOp.components.get(1)).name);
        assertEquals("c", ((VariableReference)innerOp.components.get(2)).name);
        assertEquals("b", ((VariableReference)innerOp.components.get(3)).name);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(0)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(2)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(3)).variableValues);

        assertEquals(108.0, innerOp.evaluate());
        assertEquals(598.0, op.evaluate());
    }
    //endregion

    //region tryParseInfixOperation_leftAssociative
    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperation()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("^"), "b", new Token("*"), "c", new Token("&"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperationButHasLastFewTokensOfIt()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("^"), "b", new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperationBecauseOneOfTheTokensCantBe()
    {
        Builder b = newBuilderWithInfixOp(true).withOperator("+", (l, r) -> l + r);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("+"), new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_leftAssociative_notAnInfixOperationBecauseOneOfTheTokensIsInBrackets()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("£"), Token.OPEN_BRACKET, "b", new Token("€"), "c",
                                    Token.CLOSE_BRACKET, new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParseInfixOperation_leftAssociative_isAnInfixOperation()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("¢"), "d");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.leftAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.action);
        assertThat(op.components).hasSize(4);
        assertThat(op.components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)op.components.get(0)).name);
        assertEquals("b", ((VariableReference)op.components.get(1)).name);
        assertEquals("c", ((VariableReference)op.components.get(2)).name);
        assertEquals("d", ((VariableReference)op.components.get(3)).name);
        assertSame(b.variables, ((VariableReference)op.components.get(0)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(2)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(3)).variableValues);
        assertEquals(114.0, op.evaluate());
    }

    @Test
    void tryParseInfixOperation_leftAssociative_isAnInfixOperationFollowingSameOne()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a", new Token("£"), "b", new Token("€"), "c", new Token("¢"),
                                    "d", new Token("£"), "c", new Token("€"), "b", new Token("¢"), "a");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.leftAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.action);
        assertThat(op.components).hasSize(4);
        assertThat(op.components.get(0)).isInstanceOf(Operation.class);
        assertThat(op.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("c", ((VariableReference)op.components.get(1)).name);
        assertEquals("b", ((VariableReference)op.components.get(2)).name);
        assertEquals("a", ((VariableReference)op.components.get(3)).name);
        assertSame(b.variables, ((VariableReference)op.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(2)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(3)).variableValues);

        Operation innerOp = (Operation)op.components.get(0);
        assertEquals(expectedA, innerOp.action);
        assertThat(innerOp.components).hasSize(4);
        assertThat(innerOp.components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)innerOp.components.get(0)).name);
        assertEquals("b", ((VariableReference)innerOp.components.get(1)).name);
        assertEquals("c", ((VariableReference)innerOp.components.get(2)).name);
        assertEquals("d", ((VariableReference)innerOp.components.get(3)).name);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(0)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(2)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(3)).variableValues);

        assertEquals(114.0, innerOp.evaluate());
        assertEquals(200.0, op.evaluate());
    }

    @Test
    void tryParseInfixOperation_leftAssociative_isAnInfixOperationContainingSameOneAsAnOperand()
    {
        Builder b = newBuilderWithInfixOp(true);
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("a",
                                    new Token("£"),
                                    "b",
                                    new Token("€"),
                                    "c", new Token("£"), "d", new Token("€"), "c", new Token("¢"), "b",
                                    new Token("¢"),
                                    "a");

        Operation op = b.tryParseInfixOperation_leftAssociative(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.leftAssociativeInfixOperators.getItems().stream().findFirst().get().action;
        assertEquals(expectedA, op.action);
        assertThat(op.components).hasSize(4);
        assertThat(op.components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(op.components.get(2)).isInstanceOf(Operation.class);
        assertThat(op.components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("a", ((VariableReference)op.components.get(0)).name);
        assertEquals("b", ((VariableReference)op.components.get(1)).name);
        assertEquals("a", ((VariableReference)op.components.get(3)).name);
        assertSame(b.variables, ((VariableReference)op.components.get(0)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)op.components.get(3)).variableValues);

        Operation innerOp = (Operation)op.components.get(2);
        assertEquals(expectedA, innerOp.action);
        assertThat(innerOp.components).hasSize(4);
        assertThat(innerOp.components.get(0)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(1)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(2)).isInstanceOf(VariableReference.class);
        assertThat(innerOp.components.get(3)).isInstanceOf(VariableReference.class);
        assertEquals("c", ((VariableReference)innerOp.components.get(0)).name);
        assertEquals("d", ((VariableReference)innerOp.components.get(1)).name);
        assertEquals("c", ((VariableReference)innerOp.components.get(2)).name);
        assertEquals("b", ((VariableReference)innerOp.components.get(3)).name);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(0)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(1)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(2)).variableValues);
        assertSame(b.variables, ((VariableReference)innerOp.components.get(3)).variableValues);

        assertEquals(108.0, innerOp.evaluate());
        assertEquals(598.0, op.evaluate());
    }
    //endregion
    
    //region tryParsePrefixOperation
    @Test
    void tryParsePrefixOperation_notAPrefixOperation()
    {
        Builder b = new Builder("2+2", false).withPrefixOperator("£", o -> o * 3);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("x", new Token("£"), new NumberToken("7", 7.0));

        Operation op = b.tryParsePrefixOperation(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParsePrefixOperation_isAPrefixOperation()
    {
        Builder b = new Builder("2+2", false).withPrefixOperator("£", o -> o * 3);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList(new Token("£"), new NumberToken("7", 7.0));

        Operation op = b.tryParsePrefixOperation(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.prefixOperators.values().stream().findFirst().get().action;
        assertEquals(expectedA, op.action);
        assertThat(op.components).hasSize(1);
        EquationComponent operand = op.components.get(0);
        assertThat(operand).isInstanceOf(LiteralNumber.class);
        assertEquals(7.0, ((LiteralNumber)operand).value);
        assertEquals(21.0, op.evaluate());
    }
    //endregion

    //region tryParsePostfixOperation
    @Test
    void tryParsePostfixOperation_notAPostfixOperation()
    {
        Builder b = new Builder("2+2", false).withPostfixOperator("£", o -> o * 3);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList("x", new Token("£"), new NumberToken("7", 7.0));

        Operation op = b.tryParsePrefixOperation(tl, opGroup);
        assertNull(op);
    }

    @Test
    void tryParsePostfixOperation_isAPostfixOperation()
    {
        Builder b = new Builder("2+2", false).withPostfixOperator("£", o -> o * 3);
        b.buildOperatorGroups();
        Builder.OperatorPriorityGroup opGroup = b.operatorGroupsInOrder.get(0);
        TokenList tl = newTokenList(new NumberToken("7", 7.0), new Token("£"));

        Operation op = b.tryParsePostfixOperation(tl, opGroup);
        assertNotNull(op);
        OperatorAction expectedA = opGroup.postfixOperators.values().stream().findFirst().get().action;
        assertEquals(expectedA, op.action);
        assertThat(op.components).hasSize(1);
        EquationComponent operand = op.components.get(0);
        assertThat(operand).isInstanceOf(LiteralNumber.class);
        assertEquals(7.0, ((LiteralNumber)operand).value);
        assertEquals(21.0, op.evaluate());
    }
    //endregion
}
