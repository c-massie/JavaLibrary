package scot.massie.lib.maths;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.function.Executable;
import scot.massie.lib.maths.EquationEvaluator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

public class EquationEvaluator_TokenListTest
{
    /*

    Things to test:
        .isInBrackets
            - Not in brackets
            - in brackets
            - contains brackets, but is not in brackets
            - starts with content in brackets, but is not in brackets
            - ends with content in brackets, but is not in brackets
            - starts and ends with content in brackets, but is not in brackets
        .withoutFirst()
            - tokenlist with tokens
            - tokenlist with one token
            - tokenlist with no tokens
        .withoutFirst(int)
            - tokenlist with more tokens
            - tokenlist with same number of tokens
            - tokenlist with less tokens
            - tokenlist with no tokens
            - tokenlist with tokens, removing none
            - tokenlist with no tokens, removing none
        .withoutLast()
            - tokenlist with tokens
            - tokenlist with one token
            - tokenlist with no tokens
        .withoutLast(int)
            - tokenlist with more tokens
            - tokenlist with same number of tokens
            - tokenlist with less tokens
            - tokenlist with no tokens
            - tokenlist with tokens, removing none
            - tokenlist with no tokens, removing none
        .withoutFirstAndLast()
            - tokenlist with more than 2 tokens
            - tokenlist with 2 tokens
            - tokenlist with 1 token
            - tokenlist with no tokens
        .subList
            - sublist is entire tokenlist
            - sublist is within tokenlist
            - sublist is start of tokenlist
            - sublist is end of tokenlist
            - sublist is one item
            - sublist is empty
            - sublist is bigger than tokenlist
            - tokenlist and sublist are empty
            - sublist sublist has higher min than max
        .splitBy
            - tokenlist contains token
            - tokenlist contains token, but only in brackets
            - tokenlist does not contain token
        .splitBySequence
            - tokenlist does not contain sequence
            - tokenlist contains sequence
            - tokenlist contains sequence, but only in brackets
            - tokenlist contains part of sequence in brackets
            - tokenlist contains sequence, and some tokens are in brackets in addition to not
            - tokenlist contains sequence twice
            - tokenlist contains sequence interwoven with other instance of sequence
        .splitBySequenceInReverse
            - tokenlist does not contain sequence
            - tokenlist contains sequence
            - tokenlist contains sequence, but only in brackets
            - tokenlist contains part of sequence in brackets
            - tokenlist contains sequence, and some tokens are in brackets in addition to not
            - tokenlist contains sequence twice
            - tokenlist contains sequence interwoven with other instance of sequence
        .splitAtPoints
            - tokenlist and points are empty
            - points is empty
            - tokenlist contains points
            - tokenlist does not contain points (points are out of bounds)
            - points are at bounds of tokenlist
            - tokenlist contains points, but points are not in order
            - tokenlist contains points, but multiple points are the same

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

    void assertTokenList(TokenList t, String asString, Object[] tokens, int[] spacings)
    {
        assertEquals(asString, t.equationAsString);
        assertThat(t.tokens).containsExactlyElementsOf(asListOfTokens(tokens));
        assertThat(t.spacings).containsExactly(IntStream.of(spacings).boxed().toArray(Integer[]::new));
    }

    //region isInBrackets()
    @Test
    void isInBrackets_notInBrackets()
    {
        TokenList tl = newTokenList("a", "b", "c");
        assertFalse(tl.isInBrackets());
    }

    @Test
    void isInBrackets_inBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isInBrackets_containsButNotInBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isInBrackets_startsWithButNotInBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isInBrackets_endsWithButNotInBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isInBrackets_startsAndEndsWithButNotInBrackets()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion

    //region withoutFirst()
    @Test
    void withoutFirst_withTokens()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutFirst(),
                        "  b   c     d        ",
                        new Object[]{ "b", "c", "d" },
                        new int[]{ 2, 3, 5, 8 });
    }

    @Test
    void withoutFirst_withSingleToken()
    {
        assertTokenList(newTokenList("a").withoutFirst(),
                        "  ",
                        new Object[]{},
                        new int[]{ 2 });
    }

    @Test
    void withoutFirst_withNoTokens()
    {
        assertTokenList(newTokenList().withoutFirst(),
                        "",
                        new Object[]{},
                        new int[]{ 0 });
    }
    //endregion

    //region withoutFirst(int)
    @Test
    void withoutFirst_int_withMoreTokens()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutFirst(3),
                        "     d        ",
                        new Object[]{ "d" },
                        new int[]{ 5, 8 });
    }

    @Test
    void withoutFirst_int_withSameNumberOfTokens()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutFirst(4),
                        "        ",
                        new Object[]{},
                        new int[]{ 8 });
    }

    @Test
    void withoutFirst_int_withFewerTokens()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutFirst(5),
                        "",
                        new Object[]{},
                        new int[]{ 0 });
    }

    @Test
    void withoutFirst_int_withNoTokens()
    {
        assertTokenList(newTokenList().withoutFirst(1),
                        "",
                        new Object[]{},
                        new int[]{ 0 });
    }

    @Test
    void withoutFirst_int_withTokensRemovingNone()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutFirst(0),
                        " a  b   c     d        ",
                        new Object[]{ "a", "b", "c", "d" },
                        new int[]{ 1, 2, 3, 5, 8 });
    }

    @Test
    void withoutFirst_int_withNoTokensRemovingNone()
    {
        assertTokenList(newTokenList().withoutFirst(0),
                        " ",
                        new Object[]{},
                        new int[]{ 1 });
    }
    //endregion

    //region withoutLast()
    @Test
    void withoutLast_withTokens()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutLast(),
                        " a  b   c     ",
                        new Object[]{ "a", "b", "c" },
                        new int[]{ 1, 2, 3, 5 });
    }

    @Test
    void withoutLast_withSingleToken()
    {
        assertTokenList(newTokenList("a").withoutLast(),
                        " ",
                        new Object[]{},
                        new int[]{ 1 });
    }

    @Test
    void withoutLast_withNoTokens()
    {
        assertTokenList(newTokenList().withoutLast(),
                        "",
                        new Object[]{},
                        new int[]{ 0 });
    }
    //endregion

    //region withoutLast(int)
    @Test
    void withoutLast_int_withMoreTokens()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutLast(3),
                        " a  ",
                        new Object[]{ "a" },
                        new int[]{ 1, 2 });
    }

    @Test
    void withoutLast_int_withSameNumberOfTokens()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutLast(4),
                        " ",
                        new Object[]{},
                        new int[]{ 1 });
    }

    @Test
    void withoutLast_int_withFewerTokens()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutLast(5),
                        "",
                        new Object[]{},
                        new int[]{ 0 });
    }

    @Test
    void withoutLast_int_withNoTokens()
    {
        assertTokenList(newTokenList().withoutLast(1),
                        "",
                        new Object[]{},
                        new int[]{ 0 });
    }

    @Test
    void withoutLast_int_withTokensRemovingNone()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutLast(0),
                        " a  b   c     d        ",
                        new Object[]{ "a", "b", "c", "d" },
                        new int[]{ 1, 2, 3, 5, 8 });
    }

    @Test
    void withoutLast_int_withNoTokensRemovingNone()
    {
        assertTokenList(newTokenList().withoutLast(0),
                        " ",
                        new Object[]{},
                        new int[]{ 1 });
    }

    //endregion

    //region withoutFirstAndLast()
    @Test
    void withoutFirstAndLast_moreThan2Tokens()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").withoutFirstAndLast(),
                        "  b   c     ",
                        new Object[]{ "b", "c" },
                        new int[]{ 2, 3, 5 });
    }

    @Test
    void withoutFirstAndLast_2Tokens()
    {
        assertTokenList(newTokenList("a", "b").withoutFirstAndLast(),
                        "  ",
                        new Object[]{},
                        new int[]{ 2 });
    }

    @Test
    void withoutFirstAndLast_1Token()
    {
        assertTokenList(newTokenList("a").withoutFirstAndLast(),
                        "",
                        new Object[]{},
                        new int[]{ 0 });
    }

    @Test
    void withoutFirstAndLast_noTokens()
    {
        assertTokenList(newTokenList("").withoutFirstAndLast(),
                        "",
                        new Object[]{},
                        new int[]{ 0 });
    }
    //endregion

    //region subList(int, int)

    @Test
    void subList_entire()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").subList(0, 4),
                        " a  b   c     d        ",
                        new Object[]{ "a", "b", "c", "d" },
                        new int[]{ 1, 2, 3, 5, 8 });
    }

    @Test
    void subList_within()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").subList(1, 3),
                        "  b   c     ",
                        new Object[]{ "b", "c" },
                        new int[]{ 2, 3, 5 });
    }

    @Test
    void subList_start()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").subList(0, 3),
                        " a  b   c     ",
                        new Object[]{ "a", "b", "c" },
                        new int[]{ 1, 2, 3, 5 });
    }

    @Test
    void subList_end()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").subList(1, 4),
                        "  b   c     d        ",
                        new Object[]{ "b", "c", "d" },
                        new int[]{ 2, 3, 5, 8 });
    }

    @Test
    void subList_singleItem()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").subList(2, 3),
                        "   c     ",
                        new Object[]{ "c" },
                        new int[]{ 3, 5 });
    }

    @Test
    void subList_empty()
    {
        assertTokenList(newTokenList("a", "b", "c", "d").subList(2, 2),
                        "   ",
                        new Object[]{  },
                        new int[]{ 3 });
    }

    @Test
    void subList_bigger()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> newTokenList("a", "b", "c", "d").subList(-1, 4));

        assertThrows(IllegalArgumentException.class,
                     () -> newTokenList("a", "b", "c", "d").subList(0, 5));
    }

    @Test
    void subList_tokenListAndSublistEmpty()
    {
        assertTokenList(newTokenList().subList(0, 0),
                        " ",
                        new Object[]{},
                        new int[]{ 1 });
    }

    @Test
    void subList_minLargerThanMax()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> newTokenList("a", "b", "c", "d").subList(3, 2));
    }

    //endregion

    //region splitBy(Token)
    @Test
    void splitBy_contains()
    {
        List<TokenList> tls = newTokenList("a", "b", "-", "c", "-", "-").splitBy(new UntokenisedString("-"));

        assertEquals(4, tls.size());

        assertTokenList(tls.get(0),
                        " a  b   ",
                        new Object[] { "a", "b" },
                        new int[] { 1, 2, 3 });

        assertTokenList(tls.get(1),
                        "     c        ",
                        new Object[] { "c" },
                        new int[] { 5, 8 });

        assertTokenList(tls.get(2),
                        "             ",
                        new Object[] {},
                        new int[] { 13 });

        assertTokenList(tls.get(3),
                        "                     ",
                        new Object[] {},
                        new int[] { 21 });
    }

    @Test
    void splitBy_containsInBrackets()
    {
        List<TokenList> tls = newTokenList("a", Token.OPEN_BRACKET, "-", Token.CLOSE_BRACKET, "b")
                                      .splitBy(new UntokenisedString("-"));

        assertEquals(1, tls.size());
        assertTokenList(tls.get(0),
                        " a  (   -     )        b             ",
                        new Object[]{ "a", Token.OPEN_BRACKET, "-", Token.CLOSE_BRACKET, "b" },
                        new int[]{ 1, 2, 3, 5, 8, 13 });
    }

    @Test
    void splitBy_doesNotContain()
    {
        List<TokenList> tls = newTokenList("a", "b", "c", "d", "e").splitBy(new UntokenisedString("-"));

        assertEquals(1, tls.size());
        assertTokenList(tls.get(0),
                        " a  b   c     d        e             ",
                        new Object[]{ "a", "b", "c", "d", "e" },
                        new int[]{ 1, 2, 3, 5, 8, 13 });
    }
    //endregion

    //region splitBySequence(List<Token>)
    @Test
    void splitBySequence_doesNotContain()
    {
        List<TokenList> tls = newTokenList("a", "b", "c", "d", "e")
                                      .splitBySequence(Arrays.asList(new UntokenisedString("1"),
                                                                     new UntokenisedString("2"),
                                                                     new UntokenisedString("3")));

        assertNull(tls);
    }

    @Test
    void splitBySequence_contains()
    {
        List<TokenList> tls = newTokenList("1", "a", "b", "c", "2", "d", "e", "3", "f")
                                      .splitBySequence(Arrays.asList(new UntokenisedString("1"),
                                                                     new UntokenisedString("2"),
                                                                     new UntokenisedString("3")));

        assertEquals(4, tls.size());

        assertTokenList(tls.get(0),
                        " ",
                        new Object[]{},
                        new int[]{ 1 });

        assertTokenList(tls.get(1),
                        "  a   b     c        ",
                        new Object[]{ "a", "b", "c" },
                        new int[]{ 2, 3, 5, 8 });

        assertTokenList(tls.get(2),
                        "             d                     e                                  ",
                        new Object[]{ "d", "e" },
                        new int[]{ 13, 21, 34 });

        assertTokenList(tls.get(3),
                        getSpaces(55) + "f" + getSpaces(89),
                        new Object[]{ "f" },
                        new int[] { 55, 89 });
    }

    @Test
    void splitBySequence_containsInBrackets()
    {
        List<TokenList> tls = newTokenList("a", "b", Token.OPEN_BRACKET, "1", "c", "2", "3", "d", Token.CLOSE_BRACKET)
                                      .splitBySequence(Arrays.asList(new UntokenisedString("1"),
                                                                     new UntokenisedString("2"),
                                                                     new UntokenisedString("3")));

        assertNull(tls);
    }

    @Test
    void splitBySequence_containsButPartInBrackets()
    {
        List<TokenList> tls = newTokenList("a", "1", Token.OPEN_BRACKET, "2", "b", Token.CLOSE_BRACKET, "c", "3")
                                      .splitBySequence(Arrays.asList(new UntokenisedString("1"),
                                                                     new UntokenisedString("2"),
                                                                     new UntokenisedString("3")));

        assertNull(tls);
    }

    @Test
    void splitBySequence_containsAlsoWithTokenInBrackets()
    {
        List<TokenList> tls = newTokenList("a", "1", Token.OPEN_BRACKET, "2", "b", Token.CLOSE_BRACKET, "2", "c", "3")
                                      .splitBySequence(Arrays.asList(new UntokenisedString("1"),
                                                                     new UntokenisedString("2"),
                                                                     new UntokenisedString("3")));

        assertEquals(4, tls.size());

        assertTokenList(tls.get(0),
                        " a  ",
                        new Object[] { "a" },
                        new int[] { 1, 2 });

        assertTokenList(tls.get(1),
                        "   (     2        b             )                     ",
                        new Object[] { Token.OPEN_BRACKET, "2", "b", Token.CLOSE_BRACKET },
                        new int[] { 3, 5, 8, 13, 21 });

        assertTokenList(tls.get(2),
                        getSpaces(34) + "c" + getSpaces(55),
                        new Object[] { "c" },
                        new int[] { 34, 55 });

        assertTokenList(tls.get(3),
                        getSpaces(89),
                        new Object[] {},
                        new int[] { 89 });
    }

    @Test
    void splitBySequence_containsTwiceConsecutively()
    {
        List<TokenList> tls = newTokenList("a", "1", "b", "2", "c", "3", "d", "1", "e", "2", "f", "3", "g")
                                      .splitBySequence(Arrays.asList(new UntokenisedString("1"),
                                                                     new UntokenisedString("2"),
                                                                     new UntokenisedString("3")));

        assertEquals(4, tls.size());

        assertTokenList(tls.get(0),
                        " a  ",
                        new Object[] { "a" },
                        new int[] { 1, 2 });

        assertTokenList(tls.get(1),
                        "   b     ",
                        new Object[] { "b" },
                        new int[] { 3, 5 });

        assertTokenList(tls.get(2),
                        "        c             ",
                        new Object[] { "c" },
                        new int[] { 8, 13 });

        assertTokenList(tls.get(3),
                        getSpaces(21) + "d" + getSpaces(34) + "1" + getSpaces(55) + "e" + getSpaces(89) + "2"
                            + getSpaces(144) + "f" + getSpaces(233) + "3" + getSpaces(377) + "g" + getSpaces(610),
                        new Object[] { "d", "1", "e", "2", "f", "3", "g" },
                        new int[] { 21, 34, 55, 89, 144, 233, 377, 610 });
    }

    @Test
    void splitBySequence_containsTwiceInterwoven()
    {
        List<TokenList> tls = newTokenList("a", "1", "b", "1", "c", "2", "d", "2", "e", "3", "f", "3", "g")
                                      .splitBySequence(Arrays.asList(new UntokenisedString("1"),
                                                                     new UntokenisedString("2"),
                                                                     new UntokenisedString("3")));

        assertEquals(4, tls.size());

        assertTokenList(tls.get(0),
                        " a  ",
                        new Object[] { "a" },
                        new int[] { 1, 2 });

        assertTokenList(tls.get(1),
                        "   b     1        c             ",
                        new Object[] { "b", "1", "c" },
                        new int[] { 3, 5, 8, 13 });

        assertTokenList(tls.get(2),
                        getSpaces(21) + "d" + getSpaces(34) + "2" + getSpaces(55) + "e" + getSpaces(89),
                        new Object[] { "d", "2", "e" },
                        new int[] { 21, 34, 55, 89 });

        assertTokenList(tls.get(3),
                        getSpaces(144) + "f" + getSpaces(233) + "3" + getSpaces(377) + "g" + getSpaces(610),
                        new Object[] { "f", "3", "g" },
                        new int[] { 144, 233, 377, 610 });
    }
    //endregion

    //region splitBySequenceInReverse(List<Token>)
    @Test
    void splitBySequenceInReverse_doesNotContain()
    {
        List<TokenList> tls = newTokenList("a", "b", "c", "d", "e")
                                      .splitBySequenceInReverse(Arrays.asList(new UntokenisedString("1"),
                                                                              new UntokenisedString("2"),
                                                                              new UntokenisedString("3")));

        assertNull(tls);
    }

    @Test
    void splitBySequenceInReverse_contains()
    {
        List<TokenList> tls = newTokenList("1", "a", "b", "c", "2", "d", "e", "3", "f")
                                      .splitBySequenceInReverse(Arrays.asList(new UntokenisedString("1"),
                                                                     new UntokenisedString("2"),
                                                                     new UntokenisedString("3")));

        assertEquals(4, tls.size());

        assertTokenList(tls.get(0),
                        " ",
                        new Object[]{},
                        new int[]{ 1 });

        assertTokenList(tls.get(1),
                        "  a   b     c        ",
                        new Object[]{ "a", "b", "c" },
                        new int[]{ 2, 3, 5, 8 });

        assertTokenList(tls.get(2),
                        "             d                     e                                  ",
                        new Object[]{ "d", "e" },
                        new int[]{ 13, 21, 34 });

        assertTokenList(tls.get(3),
                        getSpaces(55) + "f" + getSpaces(89),
                        new Object[]{ "f" },
                        new int[] { 55, 89 });
    }

    @Test
    void splitBySequenceInReverse_containsInBrackets()
    {
        List<TokenList> tls = newTokenList("a", "b", Token.OPEN_BRACKET, "1", "c", "2", "3", "d", Token.CLOSE_BRACKET)
                                      .splitBySequenceInReverse(Arrays.asList(new UntokenisedString("1"),
                                                                              new UntokenisedString("2"),
                                                                              new UntokenisedString("3")));

        assertNull(tls);
    }

    @Test
    void splitBySequenceInReverse_containsButPartInBrackets()
    {
        List<TokenList> tls = newTokenList("a", "1", Token.OPEN_BRACKET, "2", "b", Token.CLOSE_BRACKET, "c", "3")
                                      .splitBySequenceInReverse(Arrays.asList(new UntokenisedString("1"),
                                                                              new UntokenisedString("2"),
                                                                              new UntokenisedString("3")));

        assertNull(tls);
    }

    @Test
    void splitBySequenceInReverse_containsAlsoWithTokenInBrackets()
    {
        List<TokenList> tls = newTokenList("1", "a", "2", Token.OPEN_BRACKET, "2", "b", Token.CLOSE_BRACKET, "3", "c")
                                      .splitBySequenceInReverse(Arrays.asList(new UntokenisedString("1"),
                                                                              new UntokenisedString("2"),
                                                                              new UntokenisedString("3")));

        assertEquals(4, tls.size());

        assertTokenList(tls.get(0),
                        " ",
                        new Object[] {  },
                        new int[] { 1 });

        assertTokenList(tls.get(1),
                        "  a   ",
                        new Object[] { "a" },
                        new int[] { 2, 3 });

        assertTokenList(tls.get(2),
                        getSpaces(5) + "(" + getSpaces(8) + "2" + getSpaces(13) + "b" + getSpaces(21) + ")" + getSpaces(34),
                        new Object[] { Token.OPEN_BRACKET, "2", "b", Token.CLOSE_BRACKET },
                        new int[] { 5, 8, 13, 21, 34 });

        assertTokenList(tls.get(3),
                        getSpaces(55) + "c" + getSpaces(89),
                        new Object[] { "c" },
                        new int[] { 55, 89 });
    }

    @Test
    void splitBySequenceInReverse_containsTwiceConsecutively()
    {
        List<TokenList> tls = newTokenList("a", "1", "b", "2", "c", "3", "d", "1", "e", "2", "f", "3", "g")
                                      .splitBySequenceInReverse(Arrays.asList(new UntokenisedString("1"),
                                                                              new UntokenisedString("2"),
                                                                              new UntokenisedString("3")));

        assertEquals(4, tls.size());

        assertTokenList(tls.get(0),
                        " a  1   b     2        c             3" + getSpaces(21) + "d" + getSpaces(34),
                        new Object[] { "a", "1", "b", "2", "c", "3", "d" },
                        new int[] { 1, 2, 3, 5, 8, 13, 21, 34 });

        assertTokenList(tls.get(1),
                        getSpaces(55) + "e" + getSpaces(89),
                        new Object[] { "e" },
                        new int[] { 55, 89 });

        assertTokenList(tls.get(2),
                        getSpaces(144) + "f" + getSpaces(233),
                        new Object[] { "f" },
                        new int[] { 144, 233 });

        assertTokenList(tls.get(3),
                        getSpaces(377) + "g" + getSpaces(610),
                        new Object[] { "g" },
                        new int[] { 377, 610 });
    }

    @Test
    void splitBySequenceInReverse_containsTwiceInterwoven()
    {
        List<TokenList> tls = newTokenList("a", "1", "b", "1", "c", "2", "d", "2", "e", "3", "f", "3", "g")
                                      .splitBySequenceInReverse(Arrays.asList(new UntokenisedString("1"),
                                                                              new UntokenisedString("2"),
                                                                              new UntokenisedString("3")));

        assertEquals(4, tls.size());

        assertTokenList(tls.get(0),
                        " a  1   b     ",
                        new Object[] { "a", "1", "b" },
                        new int[] { 1, 2, 3, 5 });

        assertTokenList(tls.get(1),
                        "        c             2" + getSpaces(21) + "d" + getSpaces(34),
                        new Object[] { "c", "2", "d" },
                        new int[] { 8, 13, 21, 34 });

        assertTokenList(tls.get(2),
                        getSpaces(55) + "e" + getSpaces(89) + "3" + getSpaces(144) + "f" + getSpaces(233),
                        new Object[] { "e", "3", "f" },
                        new int[] { 55, 89, 144, 233 });

        assertTokenList(tls.get(3),
                        getSpaces(377) + "g" + getSpaces(610),
                        new Object[] { "g" },
                        new int[] { 377, 610 });
    }
    //endregion

    //region splitAtPoints(List<Integer>)
    @Test
    void splitAtPoints_bothEmpty()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void splitAtPoints_noPoints()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void splitAtPoints_contains()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void splitAtPoints_outOfBounds()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void splitAtPoints_atBounds()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void splitAtPoints_containsButPointsNotOrdered()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void splitAtPoints_containsButPointsDuplicated()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    //endregion
}
