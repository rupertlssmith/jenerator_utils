package com.thesett.util.commands.dropviews;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import io.dropwizard.Configuration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.setup.Bootstrap;

import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.commons.dbcp2.BasicDataSource;

import com.thesett.util.commands.refdata.RefDataLoadException;
import com.thesett.util.config.ConfigurationUtils;

/**
 * Liquibase cannot successfully drop all database views. This command exists to successfully drop all database views,
 * in order to clear out a database prior to it being re-built with the liquibase migration scripts.
 *
 * <pre>
 * https://liquibase.jira.com/browse/CORE-2268
 * </pre>
 *
 * <p><b>NOTE:</b> This is specific to Postgres.</p>
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Drop all database views. </td></tr>
 * </table></pre>
 */
public abstract class DropViewsCommand<T extends Configuration> extends ConfiguredCommand<T>
    implements DatabaseConfiguration<T> {
    /** SQL for Postgres to drop all views in a schema. */
    public static final String DROP_VIEWS_SQL =
        "DO $$DECLARE r record;\n" +
        "DECLARE s TEXT;\n" +
        "BEGIN\n" +
        "    FOR r IN select table_schema,table_name\n" +
        "        from information_schema.views\n" +
        "        where table_schema = 'public'\n" +
        "    LOOP\n" +
        "        s := 'DROP VIEW IF EXISTS ' ||  quote_ident(r.table_schema) || '.'\n" +
        "             || quote_ident(r.table_name) || ' CASCADE ;';\n" +
        "        EXECUTE s;\n" +
        "        RAISE NOTICE 's = % ',s;\n" +
        "    END LOOP;\n" +
        "END$$;";

    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(DropViewsCommand.class.getName());

    public DropViewsCommand() {
        super("drop-views", "Drops all database views.");
    }

    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T t) throws Exception {
        Connection connection = getConnection(t);

        PreparedStatement sql = connection.prepareStatement(DROP_VIEWS_SQL);

        sql.execute();

        cleanup(connection);
    }

    /**
     * Opens a connection to the database, using the database configuration from the DropWizard config.
     *
     * @return A database connection.
     *
     * @throws com.thesett.util.commands.refdata.RefDataLoadException If the database connection cannot be opened.
     */
    private Connection getConnection(T configuration) throws RefDataLoadException {
        // Obtain a connection to the database, using details from the DropWizard config.
        LOG.fine("Getting a database connection.");

        BasicDataSource ds = ConfigurationUtils.getBasicDataSource(getDataSourceFactory(configuration));

        Connection connection = null;

        try {
            connection = ds.getConnection();
        } catch (SQLException e) {
            throw new RefDataLoadException("Unable to open a database connection.", "DVC", 2, e);
        }

        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RefDataLoadException("The database connection could not be set to autoCommit = true.", "DVC", 3,
                e);
        }

        return connection;
    }

    /**
     * Cleans up the database connection.
     *
     * @param  connection The database connection.
     *
     * @throws RefDataLoadException If the connection cannot be cleanly closed.
     */
    private void cleanup(Connection connection) throws RefDataLoadException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RefDataLoadException("There was a problem closing the database connection.", "DVC", 7, e);
        }
    }
}
