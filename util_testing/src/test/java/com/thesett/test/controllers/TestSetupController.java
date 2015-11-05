package com.thesett.test.controllers;

import javax.sql.DataSource;
import javax.validation.ValidatorFactory;

import io.dropwizard.Configuration;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import com.thesett.util.commands.refdata.RefDataLoadException;

/**
 * TestSetupController defines a set of operations that test code can use to initialize and reset the state of the
 * application under test. The configuration is taken from a DropWizard configuration.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Provide a data source for direct access to the database. </td></tr>
 * <tr><td> Create Hibernate session factory. </td></tr>
 * <tr><td> Load all reference data into the database. </td></tr>
 * <tr><td> Read all reference data from the database into memory. </td></tr>
 * <tr><td> Load the bean validator configuration. </td></tr>
 * <tr><td> Clear the database into a clean state. </td></tr>
 * </table></pre>
 */
public interface TestSetupController<T extends Configuration> {
    /**
     * Provides a data source for direct access to the database.
     *
     * @param  configuration The DropWizard configuration.
     *
     * @return A data source for direct access to the database.
     */
    BasicDataSource initDatasource(T configuration);

    /**
     * Sets up a hibernate session factory using the database parameters from the DropWizard configuration.
     *
     * @param configuration The configuration.
     */
    SessionFactory initHibernateSessionFactory(T configuration);

    /**
     * Inserts all reference data into the database.
     *
     * @param  configuration The DropWizard configuration.
     *
     * @throws RefDataLoadException If any reference data fails to load.
     */
    void insertReferenceData(T configuration) throws RefDataLoadException;

    /**
     * Reads all reference data from the database into the reference data caches.
     *
     * @param configuration The configuration.
     */
    void loadReferenceData(T configuration) throws RefDataLoadException;

    /**
     * Loads the bean validator configuration, and provides a factory to access it with.
     *
     * @param  configuration The DropWizard configuration.
     *
     * @return A bean validator factory.
     */
    ValidatorFactory loadBeanValidation(T configuration);

    /**
     * Clears the database into a known clean initial state. This should not remove any reference data, as the reference
     * data will only be set up once per test class, but the database clear will be used on every test.
     *
     * @param dataSource A data source to access the database directly with.
     */
    void clearDatabase(DataSource dataSource);

    /**
     * Provides a reflective factory for the DAO layer, that provides DAOs that run every operation in a single
     * transaction. This can be useful for setting up test data.
     *
     * @return A reflective transaction DAO factory.
     */
    ReflectiveDAOFactory getTransactionalDAOFactory(SessionFactory sessionFactory, ValidatorFactory validatorFactory);

    /**
     * Provides a reflective factory for the service layer, that provides services than run locally, with the same
     * transactional properties as the application services.
     *
     * @return A reflective service factory.
     */
    ReflectiveServiceFactory getLocalServiceFactory(SessionFactory sessionFactory, ValidatorFactory validatorFactory);
}
