package com.thesett.util.parsing;

/**
 * TryParseLong provides a neater encapsulation of attempting to parse a number in Java, where non-matches are expected.
 * Java does not provide 'tryParse' methods such as those found in C#, and uses runtime exceptions to indicate parsing
 * errors. This is somewhat inconvenient, as either pre-validation is required, resulting in parsing twice, or try/catch
 * blocks need to be inserted in the code with control flow by exceptions.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Parse a string or indicate using a boolean that it was not parsable. </td></tr>
 * </table></pre>
 */
public class TryParseLong {
    /** Holds a flag inidcating whether the value is parsable. */
    private boolean isParseable;

    /** Holds the parsed value. */
    private long value;

    /**
     * Evaluates whether a string is parsable, and sets up its parsed value or a flag indicating that it is not
     * parsable.
     *
     * @param toParse The string to parse.
     */
    public TryParseLong(String toParse) {
        try {
            value = Long.parseLong(toParse);
            isParseable = true;
        } catch (NumberFormatException e) {
            // Exception set to null to indicate it is deliberately being ignored, since the compensating action
            // of clearing the parsable flag is being taken.
            e = null;

            isParseable = false;
        }
    }

    /**
     * Provides a flag indicating whether the string was parsable.
     *
     * @return <tt>true</tt> iff the string can be parsed.
     */
    public boolean isParsable() {
        return isParseable;
    }

    /**
     * The string parsed as a long.
     *
     * @return The string parsed as a long.
     */
    public long getLong() {
        return value;
    }
}
