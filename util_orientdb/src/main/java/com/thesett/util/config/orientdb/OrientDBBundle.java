package com.thesett.util.config.orientdb;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * OrientDBBundle is a DropWizard configuration bundle for using Orient DB.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Register the Orient DB on bootstrap. </td></tr>
 * <tr><td> Supply a session factory to work with the DB. </td></tr>
 * </table></pre>
 */
public class OrientDBBundle<T extends Configuration> implements ConfiguredBundle<T> {
    /** Holds a reference to the database for creating sessions. */
    private ODatabase database;

    /** {@inheritDoc} */
    public void initialize(Bootstrap<?> bootstrap) {
    }

    /** {@inheritDoc} */
    public void run(T t, Environment environment) throws Exception {
        database = new OObjectDatabaseTx("memory:publishing").create();
    }

    /**
     * Provides the configured database reference.
     *
     * @return The configured database reference.
     */
    public ODatabase getDatabase() {
        return database;
    }
}
