package com.thesett.util.console;

import java.io.PrintStream;

/**
 * There are rules in Sonar to check that standard Unix pipes are not used, and loggers are used instead. Sometimes
 * standard unix IO is needed and this exists for those situations where you must. The alternative is to excessively
 * pepper the code with no sonar tags.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide access to stdout and stderr. </td></tr>
 * </table></pre>
 */
public class ConsoleUtils {
    public static PrintStream stdout() {
        return System.out; // NOSONAR
    }

    public static PrintStream stderr() {
        return System.out; // NOSONAR
    }
}
