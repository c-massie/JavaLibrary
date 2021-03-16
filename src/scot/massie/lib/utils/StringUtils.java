package scot.massie.lib.utils;

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
}
