package com.thesett.util.string;

import com.google.common.base.CaseFormat;

/**
 * StringUtils provides some helper methods for working with strings.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Convert a string to upper camel case. </td></tr>
 * </table></pre>
 */
public class StringUtils {
    /** Private constructor to prevent instantiation of utility class. */
    private StringUtils() {
    }

    /**
     * Converts a string to upper camel case. That is '_' characters separate words, and are removed with the following
     * word capitalized. The first letter is also changed to upper case, to make this upper camel case.
     *
     * @param  value The string to convert to upper camel case.
     *
     * @return The string as upper camel case.
     */
    public static String toUpperCamel(String value) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, value);
    }

    /**
     * Converts a string to camel case.
     *
     * @param  name The string to convert to camel case.
     *
     * @return The string in camel case.
     */
    public static String toCamelCase(String name) {
        StringBuilder result = new StringBuilder(name.length());
        String[] parts = name.split("_");
        result.append(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                result.append(upperFirstChar(parts[i]));
            }
        }

        return result.toString();
    }

    /**
     * Converts the first character of a string to upper case.
     *
     * @param  name The string to convert the first character of.
     *
     * @return The string with its first character in upper case.
     */
    public static String upperFirstChar(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Checks if a string is null or empty.
     *
     * @param  value The string to check.
     *
     * @return <tt>true</tt> iff the string is null or empty.
     */
    public static boolean nullOrEmpty(String value) {
        return value == null || "".equals(value);
    }
}
