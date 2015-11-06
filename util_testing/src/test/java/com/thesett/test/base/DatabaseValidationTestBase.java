package com.thesett.test.base;

import java.io.Serializable;

import javax.sql.DataSource;
import javax.validation.ValidatorFactory;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import com.thesett.test.controllers.DropwizardTestController;
import com.thesett.test.controllers.TestSetupController;
import com.thesett.test.stack.CRUDTestDataSupplier;
import com.thesett.test.stack.HibernateDetachTestController;
import com.thesett.test.stack.HibernateTransactionalTestController;
import com.thesett.test.stack.JsonSerDesTestController;
import com.thesett.test.stack.PrerequisitesTestController;
import com.thesett.test.stack.TestIdMaintainerTestController;
import com.thesett.test.stack.ValidationTestBase;
import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;

public abstract class DatabaseValidationTestBase<E extends Entity<K>, K extends Serializable>
    extends ValidationTestBase<E, K> {
    /** An instance of the application to test. The database configuration is taken from this. */
    public static DropwizardTestController dropwizardTestController;

    /** The Hibernate session factory to test with. */
    protected static SessionFactory sessionFactory;

    /**
     * The data source used to create direct connections to the database, bypassing the application. For test data set
     * up and tear down.
     */
    private static DataSource dataSource;

    /** The configured bean validator factory. */
    protected static ValidatorFactory validatorFactory;

    /** The main class of the DropWizard application to test. */
    private final Class starsApplicationClass;

    /** The path to the DropWizard configuration. */
    private final String configPath;

    /** Holds the test setup controller. */
    private final TestSetupController testSetupController;

    /** The type of the entities being tested. */
    private final Class<E> entityType;

    /**
     * Creates a database integration test, using the test data and DAO supplied.
     *
     * @param testData            The test data supplier.
     * @param entityType          The type of the entity being tested.
     * @param example             The example to test.
     * @param expectedValid       <tt>true</tt> iff the example is valid.
     * @param testSetupController The test setup controller.
     * @param dwApplicationClass  The main class of the DropWizard application to test.
     * @param configPath          The path to the DropWizard configuration.
     */
    public DatabaseValidationTestBase(CRUDTestDataSupplier<E, K> testData, Class<E> entityType, E example,
        boolean expectedValid, TestSetupController testSetupController, Class dwApplicationClass, String configPath) {
        super(testData.getEqualityChecker());

        this.entityType = entityType;
        this.testData = testData;
        this.example = example;
        this.expectedValid = expectedValid;
        this.testSetupController = testSetupController;
        this.starsApplicationClass = dwApplicationClass;
        this.configPath = configPath;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (dropwizardTestController != null) {
            dropwizardTestController.stop();
        }

        if (dataSource != null) {
            ((BasicDataSource) dataSource).close();
        }
    }

    @Before
    public void setup() throws Exception {
        startDropWizardApp();
        initDatasource();
        initHibernateSessionFactory();
        insertReferenceData();
        loadReferenceData();
        loadBeanValidation();
        clearDatabase();
        buildTestStack();
    }

    /** Starts the DropWizard application running. */
    public void startDropWizardApp() {
        if (fireOnceRule.shouldFireRule()) {
            dropwizardTestController = new DropwizardTestController(starsApplicationClass, configPath);
            dropwizardTestController.start();
        }
    }

    /** Sets up a data source using the database parameters from the DropWizard configuration. */
    public void initDatasource() {
        if (fireOnceRule.shouldFireRule()) {
            dataSource = testSetupController.initDatasource(dropwizardTestController.getConfiguration());
        }

        Assert.assertNotNull("Failed to initialize 'dataSource'.", dataSource);
    }

    /** Sets up a hibernate session factory using the database parameters from the DropWizard configuration. */
    public void initHibernateSessionFactory() {
        if (fireOnceRule.shouldFireRule()) {
            // Close any previous session factory, to help clean up connections.
            if (sessionFactory != null) {
                sessionFactory.close();
            }

            sessionFactory =
                testSetupController.initHibernateSessionFactory(dropwizardTestController.getConfiguration());
        }
    }

    /** Inserts all the reference data, only needed because Hibernate blew away the whole database. */
    public void insertReferenceData() throws Exception {
        if (fireOnceRule.shouldFireRule()) {
            testSetupController.insertReferenceData(dropwizardTestController.getConfiguration());
        }
    }

    /** Loads all reference data caches into memory. */
    public void loadReferenceData() throws Exception {
        if (fireOnceRule.shouldFireRule()) {
            testSetupController.loadReferenceData(dropwizardTestController.getConfiguration());
        }
    }

    /** Loads the bean validation constraints. */
    public void loadBeanValidation() {
        if (fireOnceRule.shouldFireRule()) {
            validatorFactory = testSetupController.loadBeanValidation(dropwizardTestController.getConfiguration());
        }
    }

    /** Uses DBSetup to put the database into a known clean initial state. */
    public void clearDatabase() {
        testSetupController.clearDatabase(dataSource);
    }

    /** {@inheritDoc} */
    public void buildTestStack() {
        CRUD<E, K> dao = getDao();

        CRUD<E, K> idMaintainer = new TestIdMaintainerTestController<E, K>(testData, dao);
        CRUD<E, K> transactions = new HibernateTransactionalTestController<E, K>(idMaintainer, sessionFactory);
        CRUD<E, K> detach = new HibernateDetachTestController(transactions);
        CRUD<E, K> serdes = new JsonSerDesTestController<E, K>(detach);

        testController = new PrerequisitesTestController<E, K>(testData, serdes);
    }

    /**
     * Should supply a DAO instance to access the database through.
     *
     * @return A DAO instance to access the database through.
     */
    protected abstract CRUD<E, K> getDao();
}
