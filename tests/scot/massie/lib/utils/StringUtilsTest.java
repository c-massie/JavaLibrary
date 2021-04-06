package scot.massie.lib.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class StringUtilsTest
{
    @Test
    void parseCSVLine_empty()
    { assertThat(StringUtils.parseCSVLine(" ")).containsExactly(""); }

    @Test
    void parseCSVLine_unquoted()
    { assertThat(StringUtils.parseCSVLine("first, second ,third ")).containsExactly("first", "second", "third"); }

    @Test
    void parseCSVLine_unquoted_emptyField()
    { assertThat(StringUtils.parseCSVLine("first, ,third ")).containsExactly("first", "", "third"); }

    @Test
    void parseCSVLine_unquoted_containsQuote()
    { assertThat(StringUtils.parseCSVLine("first, sec\"ond ,third ")).containsExactly("first", "sec\"ond", "third"); }

    @Test
    void parseCSVLine_unquoted_containsDoubleQuote()
    { assertThat(StringUtils.parseCSVLine("first, sec\"\"ond ,third ")).containsExactly("first", "sec\"\"ond", "third"); }

    @Test
    void parseCSVLine_unquoted_startsWithQuote()
    { assertThat(StringUtils.parseCSVLine("first, \"second ,third ")).containsExactly("first", "\"second", "third"); }

    @Test
    void parseCSVLine_unquoted_startsWithDoubleQuote()
    { assertThat(StringUtils.parseCSVLine("first, \"\"second ,third ")).containsExactly("first", "\"\"second", "third"); }

    @Test
    void parseCSVLine_unquoted_endsWithQuote()
    { assertThat(StringUtils.parseCSVLine("first, second\" ,third ")).containsExactly("first", "second\"", "third"); }

    @Test
    void parseCSVLine_unquoted_endsWithDoubleQuote()
    { assertThat(StringUtils.parseCSVLine("first, second\"\" ,third ")).containsExactly("first", "second\"\"", "third"); }

    @Test
    void parseCSVLine_unquoted_startsWithAndContainsQuote()
    { assertThat(StringUtils.parseCSVLine("first, \"sec\"ond ,third ")).containsExactly("first", "\"sec\"ond", "third"); }

    @Test
    void parseCSVLine_unquoted_startsWithQuoteContainsDoubleQuote()
    { assertThat(StringUtils.parseCSVLine("first, \"sec\"\"ond ,third ")).containsExactly("first", "\"sec\"\"ond", "third"); }

    @Test
    void parseCSVLine_unquoted_endsWithAndContainsQuote()
    { assertThat(StringUtils.parseCSVLine("first, sec\"ond\" ,third ")).containsExactly("first", "sec\"ond\"", "third"); }

    @Test
    void parseCSVLine_unquoted_endsWithQuoteContainsDoubleQuote()
    { assertThat(StringUtils.parseCSVLine("first, sec\"\"ond\" ,third ")).containsExactly("first", "sec\"\"ond\"", "third"); }

    @Test
    void parseCSVLine_unquoted_containsNewLine()
    {
        // Note: As .parseCSVLine() is expecting to work on a single line and not a multi-row chunk of CSV data, it
        // should make no attempt to handle newlines different.
        assertThat(StringUtils.parseCSVLine("first, sec\nond ,third ")).containsExactly("first", "sec\nond", "third");
    }

    @Test
    void parseCSVLine_quoted()
    { assertThat(StringUtils.parseCSVLine("first, \"second\" ,third ")).containsExactly("first", "second", "third"); }

    @Test
    void parseCSVLine_quoted_emptyField()
    { assertThat(StringUtils.parseCSVLine("first, \"\" ,third ")).containsExactly("first", "", "third"); }

    @Test
    void parseCSVLine_quoted_fieldWithJustASpace()
    { assertThat(StringUtils.parseCSVLine("first, \" \" ,third ")).containsExactly("first", " ", "third"); }

    @Test
    void parseCSVLine_quoted_withSpace()
    { assertThat(StringUtils.parseCSVLine("first, \" second \" ,third ")).containsExactly("first", " second ", "third"); }

    @Test
    void parseCSVLine_quoted_withComma()
    { assertThat(StringUtils.parseCSVLine("first, \"sec, ond\" ,third ")).containsExactly("first", "sec, ond", "third"); }

    @Test
    void parseCSVLine_quoted_withEscapedQuote()
    { assertThat(StringUtils.parseCSVLine("first, \"sec\"\"ond\" ,third ")).containsExactly("first", "sec\"ond", "third"); }

    @Test
    void parseCSVLine_quoted_withMultipleEscapedQuotes()
    { assertThat(StringUtils.parseCSVLine("first, \"sec\"\"\"\"ond\" ,third ")).containsExactly("first", "sec\"\"ond", "third"); }

    @Test
    void parseCSVLine_quoted_withNewLine()
    { assertThat(StringUtils.parseCSVLine("first, \"sec\nond\" ,third ")).containsExactly("first", "sec\nond", "third"); }

    @Test
    void parseCSV_empty()
    { assertThat(StringUtils.parseCSV(" ")).isEmpty(); }

    @Test
    void parseCSV_empty_multipleLines()
    { assertThat(StringUtils.parseCSV(" \n\n \n \n")).isEmpty(); }

    @Test
    void parseCSV_unquoted()
    { assertThat(StringUtils.parseCSV("first, second ,third ")).containsExactly(Arrays.asList("first", "second", "third")); }

    @Test
    void parseCSV_unquoted_emptyField()
    { assertThat(StringUtils.parseCSV("first, ,third ")).containsExactly(Arrays.asList("first", "", "third")); }

    @Test
    void parseCSV_unquoted_containsQuote()
    { assertThat(StringUtils.parseCSV("first, sec\"ond ,third ")).containsExactly(Arrays.asList("first", "sec\"ond", "third")); }

    @Test
    void parseCSV_unquoted_containsDoubleQuote()
    { assertThat(StringUtils.parseCSV("first, sec\"\"ond ,third ")).containsExactly(Arrays.asList("first", "sec\"\"ond", "third")); }

    @Test
    void parseCSV_unquoted_startsWithQuote()
    { assertThat(StringUtils.parseCSV("first, \"second ,third ")).containsExactly(Arrays.asList("first", "\"second", "third")); }

    @Test
    void parseCSV_unquoted_startsWithDoubleQuote()
    { assertThat(StringUtils.parseCSV("first, \"\"second ,third ")).containsExactly(Arrays.asList("first", "\"\"second", "third")); }

    @Test
    void parseCSV_unquoted_endsWithQuote()
    { assertThat(StringUtils.parseCSV("first, second\" ,third ")).containsExactly(Arrays.asList("first", "second\"", "third")); }

    @Test
    void parseCSV_unquoted_endsWithDoubleQuote()
    { assertThat(StringUtils.parseCSV("first, second\"\" ,third ")).containsExactly(Arrays.asList("first", "second\"\"", "third")); }

    @Test
    void parseCSV_unquoted_startsWithAndContainsQuote()
    { assertThat(StringUtils.parseCSV("first, \"sec\"ond ,third ")).containsExactly(Arrays.asList("first", "\"sec\"ond", "third")); }

    @Test
    void parseCSV_unquoted_startsWithQuoteContainsDoubleQuote()
    { assertThat(StringUtils.parseCSV("first, \"sec\"\"ond ,third ")).containsExactly(Arrays.asList("first", "\"sec\"\"ond", "third")); }

    @Test
    void parseCSV_unquoted_endsWithAndContainsQuote()
    { assertThat(StringUtils.parseCSV("first, sec\"ond\" ,third ")).containsExactly(Arrays.asList("first", "sec\"ond\"", "third")); }

    @Test
    void parseCSV_unquoted_endsWithQuoteContainsDoubleQuote()
    { assertThat(StringUtils.parseCSV("first, sec\"\"ond\" ,third ")).containsExactly(Arrays.asList("first", "sec\"\"ond\"", "third")); }

    @Test
    void parseCSV_quoted()
    { assertThat(StringUtils.parseCSV("first, \"second\" ,third ")).containsExactly(Arrays.asList("first", "second", "third")); }

    @Test
    void parseCSV_quoted_emptyField()
    { assertThat(StringUtils.parseCSV("first, \"\" ,third ")).containsExactly(Arrays.asList("first", "", "third")); }

    @Test
    void parseCSV_quoted_fieldWithJustASpace()
    { assertThat(StringUtils.parseCSV("first, \" \" ,third ")).containsExactly(Arrays.asList("first", " ", "third")); }

    @Test
    void parseCSV_quoted_withSpace()
    { assertThat(StringUtils.parseCSV("first, \" second \" ,third ")).containsExactly(Arrays.asList("first", " second ", "third")); }

    @Test
    void parseCSV_quoted_withComma()
    { assertThat(StringUtils.parseCSV("first, \"sec, ond\" ,third ")).containsExactly(Arrays.asList("first", "sec, ond", "third")); }

    @Test
    void parseCSV_quoted_withEscapedQuote()
    { assertThat(StringUtils.parseCSV("first, \"sec\"\"ond\" ,third ")).containsExactly(Arrays.asList("first", "sec\"ond", "third")); }

    @Test
    void parseCSV_quoted_withMultipleEscapedQuotes()
    { assertThat(StringUtils.parseCSV("first, \"sec\"\"\"\"ond\" ,third ")).containsExactly(Arrays.asList("first", "sec\"\"ond", "third")); }

    @Test
    void parseCSV_quoted_withNewLine()
    { assertThat(StringUtils.parseCSV("first, \"sec\nond\" ,third ")).containsExactly(Arrays.asList("first", "sec\nond", "third")); }

    @Test
    void parseCSV_multipleLines()
    {
        assertThat(StringUtils.parseCSV("first, second, third\nfourth, fifth, sixth, seventh"))
                .containsExactly(Arrays.asList("first", "second", "third"),
                                 Arrays.asList("fourth", "fifth", "sixth", "seventh"));
    }

    @Test
    void parseCSV_multipleLines_newLineSurroundedByQuotesInFields()
    {
        assertThat(StringUtils.parseCSV("first, second, th\"ird\nfo\"urth, fifth, sixth, seventh"))
                .containsExactly(Arrays.asList("first", "second", "th\"ird"),
                                 Arrays.asList("fo\"urth", "fifth", "sixth", "seventh"));
    }
}