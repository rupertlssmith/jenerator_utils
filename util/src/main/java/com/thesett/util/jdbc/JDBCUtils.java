package com.thesett.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBCUtils provides some helper methods for working with JDBC.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Make best attempt to close database connection. </td></tr>
 * </table></pre>
 */
public class JDBCUtils {
    /** Private constructor to prevent instantiation of util class. */
    private JDBCUtils() {
    }

    /**
     * Makes an attempt to cleanly close a database connection, and related resources.
     *
     * @param connection The connection to close, if not <tt>null</tt>.
     * @param sql        The prepared statement to close, if not <tt>null</tt>.
     */
    public static void closeConnection(Connection connection, Statement sql) {
        try {
            if (sql != null) {
                sql.close();
            }

            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
