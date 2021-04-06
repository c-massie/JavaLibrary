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

    public static Map.Entry<String, String> splitColonSeparatedValuePair(String s)
    {
        String[] split = s.split(":", 2);
        return new AbstractMap.SimpleImmutableEntry<>(split[0].trim(), split.length == 1 ? null : split[1].trim());
    }

    public static Map<String, String> splitColonSeparatedValuePairs(String s)
    {
        String[] lines = s.split("\\r?\\n|\\r");
        Map<String, String> result = new HashMap<>();

        for(String line : lines)
        {
            String[] split = line.split(":", 2);
            result.put(split[0].trim(), split.length == 1 ? null : split[1].trim());
        }

        return result;
    }

    public static List<String> parseCSVLine(String line)
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

    public static <T> String toCSVLine(List<T> fields, boolean spaceAfterCommas)
    {
        if(fields.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        String delimiter = spaceAfterCommas ? ", " : ",";

        for(T t : fields)
            sb.append(delimiter).append(formatForCSVField(String.valueOf(t)));

        return sb.substring(spaceAfterCommas ? 2 : 1);
    }

    public static String toCSVLine(List<String> fields)
    { return toCSVLine(fields, false); }

    public static <T> String toCSV(List<List<T>> rows, boolean spaceAfterCommas)
    {
        if(rows.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();

        for(List<T> row : rows)
            sb.append("\n").append(toCSVLine(row, spaceAfterCommas));

        return sb.substring(1);
    }

    public static String toCSV(List<List<String>> rows)
    { return toCSV(rows, false); }

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

    public static <k, v> String toCSV(Map<k, v> map)
    { return toCSV(map, false); }

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
