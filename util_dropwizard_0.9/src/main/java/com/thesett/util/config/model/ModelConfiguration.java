package com.thesett.util.config.model;

import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;

/**
 * ModelConfiguration defines methods to supply a data source factory, and a resource name to load the a Jenerator model
 * definition from.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Supply a data source factory to connect to the database with. </td></tr>
 * <tr><td> Supply a resource name to load a Jenerator model definition from. </td></tr>
 * </table></pre>
 */
public interface ModelConfiguration<T extends Configuration> extends DatabaseConfiguration<T> {
    /**
     * Supplies the name of a resource to load the a Jenerator model definition from.
     *
     * @return The name of a resource to load the a Jenerator model definition from.
     */
    String getModelResource(T configuration);
}
