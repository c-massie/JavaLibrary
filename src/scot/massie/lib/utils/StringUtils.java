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
}
