package com.thesett.test.base;

import javax.sql.DataSource;
import javax.validation.ValidatorFactory;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import com.thesett.test.controllers.DropwizardTestController;
import com.thesett.test.controllers.ReflectiveServiceFactory;
import com.thesett.test.controllers.TestSetupController;
import com.thesett.test.rules.BeforeClassResetRule;
import com.thesett.test.rules.FireOnceRule;

/**
 * FullStackTestBase provides a base class for full-stack testing of DropWizard applications.
 */
public abstract class FullStackTestBase {
    /** The reset rule for the fire once rule. */
    @ClassRule
    public static BeforeClassResetRule resetRule = new BeforeClassResetRule();

    /** The Hibernate session factory to test with. */
    protected static SessionFactory sessionFactory;

    /**
     * The data source used to create direct connections to the database, bypassing the application. For test data set
     * up and tear down.
     */
    protected static DataSource dataSource;

    /** The configured bean validator factory. */
    protected static ValidatorFactory validatorFactory;

    /** An instance of the application to test. The database configuration is taken from this. */
    public static DropwizardTestController dropwizardTestController;

    /** Fire once detector to permit non-static per-class setups. */
    @Rule
    public FireOnceRule fireOnceRule = new FireOnceRule(resetRule);

    /** The main class of the DropWizard application to test. */
    protected final Class starsApplicationClass;

    /** The path to the DropWizard configuration. */
    protected final String configPath;

    /** Holds the test setup controller. */
    protected final TestSetupController testSetupController;

    /**
     * Creates a database integration test, using the test data and DAO supplied.
     *
     * @param testSetupController The test setup controller.
     * @param dwApplicationClass  The main class of the DropWizard application to test.
     * @param configPath          The path to the DropWizard configuration.
     */
    public FullStackTestBase(TestSetupController testSetupController, Class dwApplicationClass, String configPath) {
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

        if (sessionFactory != null) {
            sessionFactory.close();
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

    protected ReflectiveServiceFactory getServiceFactory() {
        return testSetupController.getLocalReflectiveServiceFactory(sessionFactory, validatorFactory);
    }
}
