package com.thesett.util.commands.refdata;

import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;

/**
 * RefDataLoadConfiguration defines methods to supply a data source factory, and a source package to load reference data
 * from.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Supply a data source factory to connect to the database with. </td></tr>
 * <tr><td> Supply the name of the package to load the raw reference data from. </td></tr>
 * </table></pre>
 */
public interface RefDataLoadConfiguration<T extends Configuration> extends DatabaseConfiguration<T> {
    /**
     * Supplies the name of a package to load raw reference data CSV files from.
     *
     * @return The name of a package to load raw reference data CSV files from.
     */
    String getRefdataPackage(T configuration);
}
