package com.thesett.util.config;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * ConfigurationUtils provides some utilities to assist with configuration.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Get a data source for direct SQL database access from a data source factory. </td></tr>
 * </table></pre>
 */
public class ConfigurationUtils {
    /** Private constructor to prevent instantiation. */
    private ConfigurationUtils() {
    }

    /**
     * Extracts a data source from a data source factory.
     *
     * @param  pooledDataSourceFactory Dropwizard's pool data source factory.
     *
     * @return A data source to directly access the database.
     */
    public static BasicDataSource getBasicDataSource(PooledDataSourceFactory pooledDataSourceFactory) {
        BasicDataSource ds = new BasicDataSource();

        DataSourceFactory dataSourceFactory = (DataSourceFactory) pooledDataSourceFactory;

        ds.setDriverClassName(dataSourceFactory.getDriverClass());
        ds.setUsername(dataSourceFactory.getUser());
        ds.setPassword(dataSourceFactory.getPassword());
        ds.setUrl(dataSourceFactory.getUrl());
        ds.setMaxIdle(1);
        ds.setInitialSize(1);
        ds.setValidationQuery("SELECT 1");

        return ds;
    }
}
