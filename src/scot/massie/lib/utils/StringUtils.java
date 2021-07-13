package scot.massie.lib.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Miscellaneous string-related util methods.</p>
 *
 * <p>Generally, methods that would be extension methods if Java supported them.</p>
 */
public final class StringUtils
{
    private StringUtils()
    {}

    /**
     * Indents a given string with four spaces.
     * @param s The string to indent.
     * @return The initially given string, with each line preceded by "    ".
     */
    public static String indent(String s)
    { return indentWith(s, "    ", 1); }

    /**
     * Indents a given string with four spaces a given number of times.
     * @param s The string to indent.
     * @param levels The number of levels to indent. This is how many times four spaces are repeated.
     * @return The initially given string, with each line preceded by "    " a given number of times.
     */
    public static String indent(String s, int levels)
    { return indentWith(s, "    ", levels); }

    /**
     * Indents a given string with another given string.
     * @param s The string to indent.
     * @param with The string to indent with. This is what will precede each line in the resulting string.
     * @return The initially given string, with each line preceded by "with".
     */
    public static String indentWith(String s, String with)
    { return indentWith(s, with, 1); }

    /**
     * Indents a given string with another given string a given number of times.
     * @param s The string to indent.
     * @param with The string to indent with. This is what will precede each line in the resulting string.
     * @param levels The level to indent. This is how many times "with" is repeated.
     * @return The initially given string, with each line preceded by "with" the given number of times.
     */
    public static String indentWith(String s, String with, int levels)
    {
        if(s == null)
            throw new IllegalArgumentException("s cannot null.");

        if(with == null)
            throw new IllegalArgumentException("with cannot be null.");

        if(levels < 0)
            throw new IllegalArgumentException("levels cannot be negative.");

        return s.replaceAll("(?m)^", repeat(with, levels));
    }

    /**
     * Gets a copy of the provided string with preceding whitespace removed.
     * @param s The string to get with preceding whitespace removed.
     * @return A copy of the string provided with whitespace removed from the start.
     */
    public static String trimStart(String s)
    { return s.replaceFirst("^\\s+", ""); }

    /**
     * Gets a copy of the provided string with trailing whitespace removed.
     * @param s The string to get with trailing whitespace removed.
     * @return A copy of the string provided with whitespace removed from the end.
     */
    public static String trimEnd(String s)
    { return s.replaceFirst("\\s+$", ""); }

    /**
     * Gets the given string repeated the given number of times. That is, the given number of copies of the given string
     * concatenated together.
     * @param s The string to repeat.
     * @param numberOfTimes The number of times to repeat the string.
     * @return The given string repeated the given number of times.
     */
    public static String repeat(String s, int numberOfTimes)
    {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < numberOfTimes; i++)
            sb.append(s);

        return sb.toString();
    }

    /**
     * Gets the position of the terminator matching the opener at the given position.
     * @param s The string to look in.
     * @param opener The character to use as an opener.
     * @param terminator The character being used as a terminator.
     * @param openerPosition The position of the opener in the given string. Where the character in the given string
     *                       isn't an opener, it will be treated as though it is.
     * @return The position of the matching terminator in the provided string, or -1 if no matching terminator is found.
     */
    public static int getMatchingTerminatorPosition(String s, char opener, char terminator, int openerPosition)
    {
        int slength = s.length();
        char[] chars = s.toCharArray();
        int depth = 1;

        for(int i = openerPosition + 1; i < slength; i++)
        {
            char ichar = chars[i];

            if(ichar == terminator)
            {
                if(--depth == 0)
                    return i;
            }
            else if(ichar == opener)
            {
                depth++;
            }
        }

        return -1;
    }

    public static int getMatchingBracketPosition(String s, int openingBracketPosition)
    { return getMatchingTerminatorPosition(s, '(', ')', openingBracketPosition); }

    public static int getMatchingSquareBracketPosition(String s, int openingSquareBracketPosition)
    { return getMatchingTerminatorPosition(s, '[', ']', openingSquareBracketPosition); }

    public static int getMatchingCurlyBracketPosition(String s, int openingCurlyBracketPosition)
    { return getMatchingTerminatorPosition(s, '{', '}', openingCurlyBracketPosition); }

    public static int getMatchingPointyBracketPosition(String s, int openingPointyBracketPosition)
    { return getMatchingTerminatorPosition(s, '<', '>', openingPointyBracketPosition); }

    /**
     * Splits a string into a key-value pair based on the position of the first colon in the string.
     * @param s The string to convert into a key-value pair.
     * @return A map entry object where the key is the text before the first colon and the value is the text after the
     *         first colon. Where the provided string contains no colon, the contents of the string is used as the key
     *         and the value is set to null. Keys and values are trimmed.
     */
    public static Map.Entry<String, String> splitColonSeparatedValuePair(String s)
    {
        s = s.trim();

        if(s.isEmpty())
            return new AbstractMap.SimpleImmutableEntry<>("", null);

        String[] split = s.split(":", 2);
        return new AbstractMap.SimpleImmutableEntry<>(split[0].trim(), split.length == 1 ? null : split[1].trim());
    }

    /**
     * Splits a string into a map by splitting it into lines then splitting each line into a key-value pair based on the
     * position of the first colon in each line.
     * @param s The string to convert into a map.
     * @return A map where each key-value pair is a line in the provided string, split into a key-value pair by the
     *         first colon in the line, where all the text before the first colon is the key and all the text after the
     *         first colon is the value. Where a line contains no colon, the entire line is used as the key and the
     *         value is set to null. Keys and values are trimmed.
     */
    public static Map<String, String> splitColonSeparatedValuePairs(String s)
    {
        String[] lines = s.split("\\r?\\n|\\r");
        Map<String, String> result = new HashMap<>();

        for(String line : lines)
        {
            line = line.trim();

            if(line.isEmpty())
                continue;

            String[] split = line.split(":", 2);
            result.put(split[0].trim(), split.length == 1 ? null : split[1].trim());
        }

        return result;
    }

    /**
     * Parses a single row of CSV-formatted text. Follows RFC-4180 with the additions of allowing (and trimming) spaces
     * around fields (that are enclosed in quotes or not) and attempting to produce reasonable results with invalid
     * data. (such as invalid use of quotes) As this only attempts to parse a single row of CSV-formatted text, newline
     * characters should not be treated as special characters and should not terminate the row nor field.
     * @param line The CSV-formatted text to parse into a list of strings.
     * @return A list of strings, where each string is a cell/field.
     */
    public static List<String> parseCSVRow(String line)
    {
        List<String> result = new ArrayList<>();
        int startOfField = 0;

        for(int i = 0; i < line.length(); i++)
        {
            char ichar = line.charAt(i);

            if(ichar == ',')
            {
                result.add(line.substring(startOfField, i).trim());
                startOfField = i + 1;
            }
            else if(i == startOfField)
            {
                if(ichar == ' ')
                    startOfField++;
                else if(ichar == '"')
                {
                    int closingQuotePosition = getClosingUnescapedQuotePositionInCSVText(line, i);

                    if(closingQuotePosition >= 0 && charAtPositionIsEndOfCSVField(line, closingQuotePosition))
                    {
                        String field = line.substring(i + 1, closingQuotePosition); // Get rid of enclosing quotes.
                        field = field.replaceAll("\"\"", "\""); // De-escape quotes.
                        result.add(field);

                        // Jump past quoted field.
                        for(int j = closingQuotePosition + 1;; j++)
                        {
                            if(j == line.length())
                            {
                                startOfField = j;
                                i = j;
                                break;
                            }

                            char jchar = line.charAt(j);

                            if(jchar == ',')
                            {
                                startOfField = j + 1;
                                i = j;
                                break;
                            }
                        }
                    }
                }
            }
        }

        result.add(line.substring(startOfField).trim());
        return result;
    }

    /**
     * Parses a chunk of CSV-formatted text. Follows RFC-4180 with the additions of allowing (and trimming) spaces
     * around fields (that are enclosed in quotes or not) and attempting to produce reasonable results with invalid
     * data. (such as invalid use of quotes)
     * @param csv The CSV-formatted text to parse into a list of lists of strings.
     * @return A list of lists of strings, where each contained list is a row in the CSV table, and each string is a
     *         cell/field.
     */
    public static List<List<String>> parseCSV(String csv)
    {
        csv = csv.replaceAll("\\r\\n?", "\n");

        List<List<String>> result = new ArrayList<>();
        List<String> currentLine = new ArrayList<>();
        int startOfField = 0;

        for(int i = 0; i < csv.length(); i++)
        {
            char ichar = csv.charAt(i);

            if(i == startOfField)
            {
                if(ichar == ' ')
                    startOfField++;
                else if(ichar == ',')
                {
                    currentLine.add(csv.substring(startOfField, i).trim());
                    startOfField = i + 1;
                }
                else if(ichar == '\n')
                {
                    startOfField++;

                    if(!currentLine.isEmpty())
                    {
                        currentLine.add("");
                        result.add(currentLine);
                        currentLine = new ArrayList<>();
                    }
                }
                else if(ichar == '"')
                {
                    int closingQuotePosition = getClosingUnescapedQuotePositionInCSVText(csv, i);

                    if(closingQuotePosition >= 0 && charAtPositionIsEndOfCSVField(csv, closingQuotePosition))
                    {
                        String field = csv.substring(i + 1, closingQuotePosition); // Get rid of enclosing quotes.
                        field = field.replaceAll("\"\"", "\""); // De-escape quotes.
                        currentLine.add(field);

                        // Jump past quoted field
                        for(int j = closingQuotePosition + 1;; j++)
                        {
                            if(j == csv.length())
                            {
                                startOfField = j;
                                i = j;
                                break;
                            }

                            char jchar = csv.charAt(j);

                            if(jchar == ',')
                            {
                                startOfField = j + 1;
                                i = j;
                                break;
                            }
                            else if(jchar == '\n')
                            {
                                startOfField = j + 1;
                                i = j;
                                result.add(currentLine);
                                currentLine = new ArrayList<>();
                                break;
                            }
                        }
                    }
                }
            }
            else
            {
                if(ichar == ',')
                {
                    currentLine.add(csv.substring(startOfField, i).trim());
                    startOfField = i + 1;
                }
                else if(ichar == '\n')
                {
                    currentLine.add(csv.substring(startOfField, i).trim());
                    result.add(currentLine);
                    currentLine = new ArrayList<>();
                    startOfField = i + 1;
                }
            }
        }

        String lastField = csv.substring(startOfField).trim();

        if((!lastField.isEmpty()) || !currentLine.isEmpty())
        {
            currentLine.add(lastField);
            result.add(currentLine);
        }

        return result;
    }

    /**
     * Converts a list of items into a CSV-formatted string representation as a single row. Items are converted to
     * strings with {@link String#valueOf(Object)}.
     * @param fields The fields of the CSV row.
     * @param spaceAfterCommas Whether or not to add a space after delimiting commas. Passing true to this will produce
     *                         a more human-readable CSV-formatted string representation, but one that will be
     *                         incompatible with RFC-4180.
     * @param <T> The type of the items in the list.
     * @return A CSV-formatted string representation of the provided list of items. If not allowing spaces after commas,
     *         the resulting CSV text will be compatible with RFC-4180.
     */
    public static <T> String toCSVRow(List<T> fields, boolean spaceAfterCommas)
    {
        if(fields.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        String delimiter = spaceAfterCommas ? ", " : ",";

        for(T t : fields)
            sb.append(delimiter).append(formatForCSVField(String.valueOf(t)));

        return sb.substring(spaceAfterCommas ? 2 : 1);
    }

    /**
     * Converts a list of items into a CSV-formatted string representation as a single row. Items are converted to
     * strings with {@link String#valueOf(Object)} and enclosed in quotes if required to produce a valid CSV table and
     * preserve it as a string.
     * @param fields The fields of the CSV row.
     * @param <T> The type of the items in the list.
     * @return An RFC-4180 CSV-formatted string representation of the provided list of items.
     */
    public static <T> String toCSVRow(List<T> fields)
    { return toCSVRow(fields, false); }

    /**
     * Converts a list of lists of items into a CSV-formatted string representation, where each list of items is a row
     * and each item is a field/cell. Items are converted to strings with {@link String#valueOf(Object)} and enclosed in
     * quotes if required to produce a valid CSV table and preserve it as a string.
     * @param rows The list of rows to be converted into a string representation.
     * @param spaceAfterCommas Whether or not to add a space after delimiting commas. Passing true to this will produce
     *                         a more human-readable CSV-formatted string representation, but one that will be
     *                         incompatible with RFC-4180.
     * @param <T> The type of the items in the table.
     * @return A CSV-formatted string representation of the provided list of lists of items. If not allowing spaces
     * after commas, the resulting CSV text will be compatible with RFC-4180.
     */
    public static <T> String toCSV(List<List<T>> rows, boolean spaceAfterCommas)
    {
        if(rows.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();

        for(List<T> row : rows)
            sb.append("\n").append(toCSVRow(row, spaceAfterCommas));

        return sb.substring(1);
    }

    /**
     * Converts a list of lists of items into a CSV-formatted string representation, where each list of items is a row
     * and each item is a field/cell. Items are converted to strings with {@link String#valueOf(Object)} and enclosed in
     * quotes if required to produce a valid CSV table and preserve it as a string.
     * @param rows The list of rows to be converted into a string representation.
     * @param <T> The type of the items in the table.
     * @return An RFC-4180 CSV-formatted string representation of the provided list of lists of items.
     */
    public static <T> String toCSV(List<List<T>> rows)
    { return toCSV(rows, false); }

    /**
     * Converts a map into a CSV-formatted string representation, where each entry in the map is a row, containing the
     * key then the value. Items are converted to strings with {@link String#valueOf(Object)} and enclosed in quotes if
     * required to produce a valid CSV table and preserve it as a string. Rows are in the order returned by the iterator
     * of the provided entry set, which may be no guaranteed order.
     * @param map The item to convert into a CSV-formatted string representation.
     * @param spaceAfterCommas Whether or not to add a space after delimiting commas. Passing true to this will produce
     *                         a more human-readable CSV-formatted string representation, but one that will be
     *                         incompatible with RFC-4180.
     * @param <k> The type of the keys in the map.
     * @param <v> The type of the values in the map.
     * @return A CSV-formatted string representation of the provided map. If not allowing spaces after commas, the
     *         resulting CSV text will be compatible with RFC-4180.
     */
    public static <k, v> String toCSV(Map<k, v> map, boolean spaceAfterCommas)
    {
        if(map.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        String delimiter = spaceAfterCommas ? ", " : ",";

        for(Map.Entry<k, v> entry : map.entrySet())
            sb.append("\n")
              .append(formatForCSVField(String.valueOf(entry.getKey())))
              .append(delimiter)
              .append(formatForCSVField(String.valueOf(entry.getValue())));

        return sb.substring(1);
    }

    /**
     * Converts a map into a CSV-formatted string representation, where each entry in the map is a row, containing the
     * key then the value. Items are converted to strings with {@link String#valueOf(Object)} and enclosed in quotes if
     * required to produce a valid CSV table and preserve it as a string. Rows are in the order returned by the iterator
     * of the provided entry set, which may be no guaranteed order.
     * @param map The item to convert into a CSV-formatted string representation.
     * @param <k> The type of the keys in the map.
     * @param <v> The type of the values in the map.
     * @return An RFC-4180 CSV-formatted string representation of the provided map.
     */
    public static <k, v> String toCSV(Map<k, v> map)
    { return toCSV(map, false); }

    /**
     * Converts a map into a CSV-formatted string representation, where each entry in the map is a row, containing the
     * key then the value. Items are converted to strings with {@link String#valueOf(Object)} and enclosed in quotes if
     * required to produce a valid CSV table and preserve it as a string. Rows are in the order dictated by the provided
     * comparator.
     * @param map The item to convert into a CSV-formatted string representation.
     * @param spaceAfterCommas Whether or not to add a space after delimiting commas. Passing true to this will produce
     *                         a more human-readable CSV-formatted string representation, but one that will be
     *                         incompatible with RFC-4180.
     * @param comparator The comparator used to sort the rows of the resulting CSV table.
     * @param <k> The type of the keys in the map.
     * @param <v> The type of the values in the map.
     * @return A CSV-formatted string representation of the provided map. If not allowing spaces after commas, the
     *         resulting CSV text will be compatible with RFC-4180.
     */
    public static <k, v> String toCSV(Map<k, v> map, boolean spaceAfterCommas, Comparator<Map.Entry<k, v>> comparator)
    {
        if(map.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        String delimiter = spaceAfterCommas ? ", " : ",";

        for(Map.Entry<k, v> entry : map.entrySet().stream().sorted(comparator).collect(Collectors.toList()))
            sb.append("\n")
              .append(formatForCSVField(String.valueOf(entry.getKey())))
              .append(delimiter)
              .append(formatForCSVField(String.valueOf(entry.getValue())));

        return sb.substring(1);
    }

    /**
     * Converts a map into a CSV-formatted string representation, where each entry in the map is a row, containing the
     * key then the value. Items are converted to strings with {@link String#valueOf(Object)} and enclosed in quotes if
     * required to produce a valid CSV table and preserve it as a string. Rows are in the order dictated by the provided
     * comparator.
     * @param map The item to convert into a CSV-formatted string representation.
     * @param comparator The comparator used to sort the rows of the resulting CSV table.
     * @param <k> The type of the keys in the map.
     * @param <v> The type of the values in the map.
     * @return An RFC-4180 CSV-formatted string representation of the provided map.
     */
    public static <k, v> String toCSV(Map<k, v> map, Comparator<Map.Entry<k, v>> comparator)
    { return toCSV(map, false, comparator); }

    private static int getClosingUnescapedQuotePositionInCSVText(String csv, int openingQuotePosition)
    {
        for(int i = openingQuotePosition + 1; i < csv.length(); i++)
        {
            char ichar = csv.charAt(i);

            if(ichar == '"')
            {
                if(i + 1 == csv.length())
                    return i;

                if(csv.charAt(i + 1) == '"')
                    i++;
                else
                    return i;
            }
        }

        return -1;
    }

    private static boolean charAtPositionIsEndOfCSVField(String csv, int position)
    {
        for(int i = position + 1; i < csv.length(); i++)
        {
            char ichar = csv.charAt(i);

            if(ichar == ',')
                return true;

            if(ichar != ' ')
                return false;
        }

        return true;
    }

    private static String formatForCSVField(String s)
    {
        if(s.matches("(?s)^\\s.*|.*\\s$|.*[,\"\n\r].*"))
            return "\"" + s.replaceAll("\"", "\"\"") + "\"";
        else
            return s;
    }
}
