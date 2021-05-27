package scot.massie.lib.maths;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import scot.massie.lib.maths.Equation.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

public class Equation_TokeniserTest
{
    /*

    Things to test:
        .countSpacesAtStart
            With empty string
            With string that just has spaces
            With string that has spaces at start
            With string that has spaces in middle
            With string that has spaces in middle and start
            With string that has spaces at end
            With string that has spaces at end and start
        .countSpacesAtEnd
            With empty string
            With string that just has spaces
            With string that has spaces at end
            With string that has spaces in middle
            With string that has spaces in middle and end
            With string that has spaces at start
            With string that has spaces at start and end
        .tokeniseNumbers
            With empty tokenlist
            With token list with no numbers
            With token list with just numbers
            With token list of a mix of numbers and non-numbers
            With token list containing tokens that start with numbers, but are not numbers
        .tokeniseStringWithSingleToken
            With empty string
            With string with just spaces
            With string with only non-tokens
            With string with only tokens
            With string with mix of tokens and non-tokens
        .tokenise
            With empty string
            With empty string that just has spaces
            With string just containing valid tokens
            With string containing no valid tokens
            With string containing mix of valid and invalid tokens
            With string containing what should be number tokens

     */

    Tokeniser tokeniser = new Tokeniser(
            Arrays.asList(Token.ARGUMENT_SEPARATOR, Token.OPEN_BRACKET, Token.CLOSE_BRACKET));

    void assertTokenList(TokenList t, String asString, List<Token> tokens, List<Integer> spacings)
    {
        assertEquals(asString, t.equationAsString);

        assertThat(t.tokens).containsExactlyElementsOf(tokens);

        assertThat(t.spacings).usingElementComparator(Integer::compareTo)
                              .containsExactlyElementsOf(spacings);
    }

    List<Token> dummyTokens = new ArrayList<>();
    {
        dummyTokens.add(Token.ARGUMENT_SEPARATOR);
        dummyTokens.add(new UntokenisedString("doot"));
        dummyTokens.add(Token.OPEN_BRACKET);
        dummyTokens.add(Token.CLOSE_BRACKET);
    }

    //region countSpacesAtStart
    @Test
    void countSpacesAtStart_empty()
    { assertEquals(0, Tokeniser.countSpacesAtStart("")); }

    @Test
    void countSpacesAtStart_justSpaces()
    { assertEquals(5, Tokeniser.countSpacesAtStart("     ")); }

    @Test
    void countSpacesAtStart_spacesAtStart()
    { assertEquals(5, Tokeniser.countSpacesAtStart("     doot")); }

    @Test
    void countSpacesAtStart_spacesInMiddle()
    { assertEquals(0, Tokeniser.countSpacesAtStart("do   ot")); }

    @Test
    void countSpacesAtStart_spacesAtStartAndInMiddle()
    { assertEquals(5, Tokeniser.countSpacesAtStart("     do   ot")); }

    @Test
    void countSpacesAtStart_spacesAtEnd()
    { assertEquals(0, Tokeniser.countSpacesAtStart("doot    ")); }

    @Test
    void countSpacesAtStart_spacesAtStartAndEnd()
    { assertEquals(5, Tokeniser.countSpacesAtStart("     doot    ")); }
    //endregion

    //region countSpacesAtEnd
    @Test
    void countSpacesAtEnd_empty()
    { assertEquals(0, Tokeniser.countSpacesAtEnd("")); }

    @Test
    void countSpacesAtEnd_justSpaces()
    { assertEquals(5, Tokeniser.countSpacesAtEnd("     ")); }

    @Test
    void countSpacesAtEnd_spacesAtEnd()
    { assertEquals(5, Tokeniser.countSpacesAtEnd("doot     ")); }

    @Test
    void countSpacesAtEnd_spacesInMiddle()
    { assertEquals(0, Tokeniser.countSpacesAtEnd("do   ot")); }

    @Test
    void countSpacesAtEnd_spacesAtEndAndInMiddle()
    { assertEquals(5, Tokeniser.countSpacesAtEnd("do   ot     ")); }

    @Test
    void countSpacesAtEnd_spacesAtStart()
    { assertEquals(0, Tokeniser.countSpacesAtEnd("    doot")); }

    @Test
    void countSpacesAtEnd_spacesAtEndAndStart()
    { assertEquals(5, Tokeniser.countSpacesAtEnd("    doot     ")); }
    //endregion

    //region tokeniseNumbers

    @Test
    void tokeniseNumbers_empty()
    {
        LinkedList<Token> tl = new LinkedList<>();
        Tokeniser.tokeniseNumbers(tl);
        assertThat(tl).isEmpty();
    }

    @Test
    void tokeniseNumbers_noNumbers()
    {
        LinkedList<Token> tl = new LinkedList<>(dummyTokens);
        Tokeniser.tokeniseNumbers(tl);
        assertThat(tl).containsExactlyElementsOf(dummyTokens);
    }

    @Test
    void tokeniseNumber_justNumbers()
    {
        List<Token> exampleNumbers = Arrays.asList(new UntokenisedString("5"),
                                                   new UntokenisedString("3.1"),
                                                   new UntokenisedString("-7"));

        LinkedList<Token> tl = new LinkedList<>(exampleNumbers);
        Tokeniser.tokeniseNumbers(tl);
        assertThat(tl).containsExactlyElementsOf(Arrays.asList(new NumberToken("5", 5d),
                                                               new NumberToken("3.1", 3.1),
                                                               new NumberToken("-7", -7d)));
    }

    @Test
    void tokeniseNumbers_mixOfNumbersAndNonNumbers()
    {
        List<Token> exampleTokens = new ArrayList<>();
        exampleTokens.add(Token.ARGUMENT_SEPARATOR);
        exampleTokens.add(new UntokenisedString("doot"));
        exampleTokens.add(Token.OPEN_BRACKET);
        exampleTokens.add(Token.CLOSE_BRACKET);
        exampleTokens.add(new UntokenisedString("5"));
        exampleTokens.add(new UntokenisedString("3.1"));
        exampleTokens.add(new UntokenisedString("-7"));
        exampleTokens.add(Token.ARGUMENT_SEPARATOR);

        LinkedList<Token> tl = new LinkedList<>(exampleTokens);

        Tokeniser.tokeniseNumbers(tl);
        exampleTokens.set(4, new NumberToken("5", 5d));
        exampleTokens.set(5, new NumberToken("3.1", 3.1d));
        exampleTokens.set(6, new NumberToken("-7", -7d));

        assertThat(tl).containsExactlyElementsOf(exampleTokens);
    }

    @Test
    void tokeniseNumbers_almostNumbers()
    {
        List<Token> exampleTokens = new ArrayList<>();
        exampleTokens.add(new UntokenisedString("452g"));
        exampleTokens.add(new UntokenisedString("g847"));
        exampleTokens.add(new UntokenisedString("3.5.4"));
        exampleTokens.add(new UntokenisedString("35 43"));
        LinkedList<Token> tl = new LinkedList<>(exampleTokens);
        Tokeniser.tokeniseNumbers(tl);
        assertThat(tl).containsExactlyElementsOf(exampleTokens);
    }

    //endregion

    //region tokeniseStringWithSingleToken
    @Test
    void tokeniseStringWithSingleToken_empty()
    {
        assertTokenList(Tokeniser.tokeniseStringWithSingleToken("", Token.ARGUMENT_SEPARATOR),
                        "",
                        Collections.emptyList(),
                        Collections.singletonList(0));
    }

    @Test
    void tokeniseStringWithSingleToken_justSpaces()
    {
        assertTokenList(Tokeniser.tokeniseStringWithSingleToken("   ", Token.ARGUMENT_SEPARATOR),
                        "   ",
                        Collections.emptyList(),
                        Collections.singletonList(3));
    }

    @Test
    void tokeniseStringWithSingleToken_onlyNonTokens()
    {
        assertTokenList(Tokeniser.tokeniseStringWithSingleToken("  enanrg   ", Token.ARGUMENT_SEPARATOR),
                        "  enanrg   ",
                        Collections.singletonList(new UntokenisedString("enanrg")),
                        Arrays.asList(2, 3));
    }

    @Test
    void tokeniseStringWithSingleToken_onlyTokens()
    {
        assertTokenList(Tokeniser.tokeniseStringWithSingleToken("  ,, ,,,  ,   ", Token.ARGUMENT_SEPARATOR),
                        "  ,, ,,,  ,   ",
                        Arrays.asList(Token.ARGUMENT_SEPARATOR,
                                      Token.ARGUMENT_SEPARATOR,
                                      Token.ARGUMENT_SEPARATOR,
                                      Token.ARGUMENT_SEPARATOR,
                                      Token.ARGUMENT_SEPARATOR,
                                      Token.ARGUMENT_SEPARATOR),
                        Arrays.asList(2, 0, 1, 0, 0, 2, 3));
    }

    @Test
    void tokeniseStringWithSingleToken_mixOfTokensAndNonTokens()
    {
        assertTokenList(Tokeniser.tokeniseStringWithSingleToken("  first, second third ,, fourth,   ",
                                                                Token.ARGUMENT_SEPARATOR),
                        "  first, second third ,, fourth,   ",
                        Arrays.asList(new UntokenisedString("first"),
                                      Token.ARGUMENT_SEPARATOR,
                                      new UntokenisedString("second third"),
                                      Token.ARGUMENT_SEPARATOR,
                                      Token.ARGUMENT_SEPARATOR,
                                      new UntokenisedString("fourth"),
                                      Token.ARGUMENT_SEPARATOR),
                        Arrays.asList(2, 0, 1, 1, 0, 1, 0, 3));
    }
    //endregion

    //region tokenise
    @Test
    void tokenise_emptyString()
    {
        TokenList t = tokeniser.tokenise("");
        assertTokenList(t, "", Collections.emptyList(), Arrays.asList(0));
    }

    @Test
    void tokenise_emptyStringWithSpaces()
    {
        TokenList t = tokeniser.tokenise("     ");
        assertTokenList(t, "     ", Collections.emptyList(), Arrays.asList(5));
    }

    @Test
    void tokenise_onlyValidTokens()
    {
        assertTokenList(tokeniser.tokenise("   ,  ( )    "),
                        "   ,  ( )    ",
                        Arrays.asList(Token.ARGUMENT_SEPARATOR, Token.OPEN_BRACKET, Token.CLOSE_BRACKET),
                        Arrays.asList(3, 2, 1, 4));
    }

    @Test
    void tokenise_onlyInvalidTokens()
    {
        assertTokenList(tokeniser.tokenise("  doot§  doot   "),
                        "  doot§  doot   ",
                        Collections.singletonList(new UntokenisedString("doot§  doot")),
                        Arrays.asList(2, 3));
    }

    @Test
    void tokenise_mixOfValidAndInvalidTokens()
    {
        assertTokenList(tokeniser.tokenise(" ,  okay( but this )(, ))  "),
                        " ,  okay( but this )(, ))  ",
                        Arrays.asList(Token.ARGUMENT_SEPARATOR,
                                      new UntokenisedString("okay"),
                                      Token.OPEN_BRACKET,
                                      new UntokenisedString("but this"),
                                      Token.CLOSE_BRACKET,
                                      Token.OPEN_BRACKET,
                                      Token.ARGUMENT_SEPARATOR,
                                      Token.CLOSE_BRACKET,
                                      Token.CLOSE_BRACKET),
                        Arrays.asList(1, 2, 0, 1, 1, 0, 0, 1, 0, 2));
    }

    @Test
    void tokenise_numbers()
    {
        assertTokenList(tokeniser.tokenise("5  , 3.1 (-7 )"),
                        "5  , 3.1 (-7 )",
                        Arrays.asList(new NumberToken("5", 5d),
                                      Token.ARGUMENT_SEPARATOR,
                                      new NumberToken("3.1", 3.1),
                                      Token.OPEN_BRACKET,
                                      new NumberToken("-7", -7d),
                                      Token.CLOSE_BRACKET),
                        Arrays.asList(0, 2, 1, 1, 0, 1, 0));
    }

    //endregion
}
