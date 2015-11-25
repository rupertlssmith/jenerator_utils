package com.thesett.util.commands.refdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.dropwizard.Configuration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.commons.dbcp2.BasicDataSource;

import com.thesett.util.config.ConfigurationUtils;

/**
 * Entry point for the command line tool to manage reference data.
 *
 * <p/>This tool can load reference data into the database from CSV files, and perform some functions to help maintain
 * the reference data files.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Populate the database with reference data. </td></tr>
 * </table></pre>
 */
public abstract class RefDataLoadCommand<T extends Configuration> extends ConfiguredCommand<T>
    implements RefDataLoadConfiguration<T> {
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(RefDataLoadCommand.class.getName());

    /** Creates the reference data command. */
    public RefDataLoadCommand() {
        super("refdata", "Load reference data.");
    }

    /**
     * Finds the reference data as resources on the classpath, parses it, obtains a database connection, loads the
     * reference data, then closes the connection.
     *
     * @throws RefDataLoadException If an error occurs that prevents the reference data loading process from completing.
     */
    public void loadReferenceData(T configuration) throws RefDataLoadException {
        String refdataPackage = getRefdataPackage(configuration);
        Set<RefDataBundle> refDataBundles = RefDataUtils.getRefDataBundles(refdataPackage);

        // Open a connection to the database.
        Connection connection = getConnection(configuration);

        // Insert all of the reference data.
        upsertReferenceData(refDataBundles, connection);

        // Clean up.
        cleanup(connection);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Finds the reference data as resources on the classpath, parses it, obtains a database connection, loads the
     * reference data, then closes the connection.
     *
     * @throws RefDataLoadException If an error occurs that prevents the reference data loading process from completing.
     */
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws RefDataLoadException {
        loadReferenceData(configuration);
    }

    /**
     * Opens a connection to the database, using the database configuration from the DropWizard config.
     *
     * @return A database connection.
     *
     * @throws RefDataLoadException If the database connection cannot be opened.
     */
    private Connection getConnection(T configuration) throws RefDataLoadException {
        // Obtain a connection to the database, using details from the DropWizard config.
        LOG.fine("Getting a database connection.");

        BasicDataSource ds = ConfigurationUtils.getBasicDataSource(getDataSourceFactory(configuration));

        Connection connection = null;

        try {
            connection = ds.getConnection();
        } catch (SQLException e) {
            throw new RefDataLoadException("Unable to open a database connection.", "RDL", 2, e);
        }

        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RefDataLoadException("The database connection could not be set to autoCommit = false.", "RDL", 3,
                e);
        }

        return connection;
    }

    /**
     * Updates or inserts a set of reference data bundles into the database.
     *
     * <p/>On on empty database, reference data values will have no FK references to them, so their tables can simply be
     * dropped and re-built. On an existing database with data in it, this will likely not work, and in that case the
     * existing values in the table will be verified against the reference data bundle, and any new values inserted.
     *
     * @param  refDataBundles The reference data bundles to insert.
     * @param  connection     The database connection.
     *
     * @throws RefDataLoadException If there is a failure during the database insertion.
     */
    private void upsertReferenceData(Set<RefDataBundle> refDataBundles, Connection connection)
        throws RefDataLoadException {
        // Insert all of the reference data.
        LOG.fine("Inserting the reference data.");

        for (RefDataBundle bundle : refDataBundles) {
            PreparedStatement sql = null;
            boolean insertMode = true;

            try {
                sql = connection.prepareStatement("DELETE FROM " + bundle.getTableName());
                sql.execute();
            } catch (SQLException e) {
                LOG.log(Level.FINE,
                    "Unable to clear the reference data table " + bundle.getTableName() +
                    ", will check existing values and add any missing ones instead.", e);

                // The exception is deliberately set to null, to indicate that it is being ignored, since compensating
                // action is being taken. The compensating action is to use update mode, to verify existing reference
                // data and insert any new values required.
                e = null;
                insertMode = false;

                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    throw new IllegalStateException(e1);
                }
            }

            if (insertMode) {
                insertReferenceDataItems(connection, bundle.getTableName(), bundle.getDataMap());

                LOG.fine("Inserted to: " + bundle.getTableName());
            } else {
                // Keep track of values not in the bundle, as those need to be inserted. A copy of the values is taken,
                // so that it can be trimmed down as existing values are seen.
                Map<Long, String> itemsToCheck = new HashMap<>(bundle.getDataMap());

                // Check that existing items exactly match the bundle.
                checkReferenceDataItems(connection, bundle.getTableName(), bundle.getTypeName(), itemsToCheck);

                // Insert any new values not already in the database.
                insertReferenceDataItems(connection, bundle.getTableName(), itemsToCheck);
            }

            try {
                connection.commit();
            } catch (SQLException e) {
                throw new RefDataLoadException("There was a problem committing the reference data to table " +
                    bundle.getTableName() + ".", "RDL", 6, e);
            }

        }
    }

    /**
     * Inserts reference data items into the table named by <tt>bundle.getTableName()</tt>.
     *
     * @param  connection The database connection.
     * @param  tableName  The database table name.
     * @param  items      The items to insert.
     *
     * @throws RefDataLoadException If an insert fails.
     */
    private void insertReferenceDataItems(Connection connection, String tableName, Map<Long, String> items)
        throws RefDataLoadException {
        for (Map.Entry<Long, String> entry : items.entrySet()) {
            Long id = entry.getKey();
            String value = entry.getValue();

            PreparedStatement sql1;

            try {
                sql1 = connection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?)");
                sql1.setLong(1, id);
                sql1.setString(2, value);
                sql1.execute();
            } catch (SQLException e) {
                throw new RefDataLoadException("Unable to insert a row into reference data table " +
                    tableName + ".", "RDL", 5, e);
            }
        }
    }

    /**
     * Checks that existing reference data items in the table named by <tt>bundle.getTableName()</tt> exactly match
     * those in the specified map of items. The id and the value should be the same in both, otherwise an exception is
     * raised.
     *
     * <p/><b>Note:</b>This means that this reference data update command does not handle renames of ref data values.
     * Renames are slightly dangerous, if an id change is actually the intention, because an id change would involve
     * propagating the id into all FK references to it. For this reason an exact match check has been implemented. This
     * could be altered if renames are wanted.
     *
     * @param  connection   The database connection.
     * @param  tableName    The database table name.
     * @param  typeName     The name of the reference data type.
     * @param  itemsToCheck The items to check the table against. Items are removed from this map as they are checked.
     *
     * @throws RefDataLoadException
     */
    private void checkReferenceDataItems(Connection connection, String tableName, String typeName,
        Map<Long, String> itemsToCheck) throws RefDataLoadException {
        PreparedStatement sql;

        try {
            sql = connection.prepareStatement("SELECT id, " + typeName + " FROM " + tableName);

            ResultSet resultSet = sql.executeQuery();

            // Every value in the table should exactly match those in the bundle.
            while (resultSet.next()) {
                Long idToCheck = resultSet.getLong(1);
                String valueToCheck = resultSet.getString(2);

                if (itemsToCheck.containsKey(idToCheck)) {
                    String value = itemsToCheck.remove(idToCheck);

                    if (!value.equals(valueToCheck)) {
                        throw new RefDataLoadException("Found existing reference data value (" + idToCheck +
                            ", " + valueToCheck + ") in table " + tableName +
                            "that does not match what is required in the reference data bundle. The value in the bundle is " +
                            value + ".", "RDL", 8, null);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RefDataLoadException("Unable to read existing items from reference data table " +
                tableName + ".", "RDL", 9, e);
        }
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
            throw new RefDataLoadException("There was a problem closing the database connection.", "RDL", 7, e);
        }
    }

}
