package com.thesett.test.base;

import java.io.Serializable;
import java.util.List;

import javax.sql.DataSource;
import javax.validation.ValidatorFactory;

import com.thesett.catalogue.model.Catalogue;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import com.thesett.test.controllers.DropwizardTestController;
import com.thesett.test.controllers.TestSetupController;
import com.thesett.test.stack.CRUDTestBase;
import com.thesett.test.stack.CRUDTestDataSupplier;
import com.thesett.test.stack.HibernateDetachTestController;
import com.thesett.test.stack.HibernateTransactionalTestController;
import com.thesett.test.stack.JsonSerDesTestController;
import com.thesett.test.stack.PrerequisitesTestController;
import com.thesett.test.stack.TestIdMaintainerTestController;
import com.thesett.util.dao.BaseDAO;
import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityValidationException;

import com.thesett.common.util.ReflectionUtils;

public abstract class DatabaseCRUDTestBase<E extends Entity<K>, K extends Serializable> extends CRUDTestBase<E, K> {
    /** The Hibernate session factory to test with. */
    protected static SessionFactory sessionFactory;

    /**
     * The data source used to create direct connections to the database, bypassing the application. For test data set
     * up and tear down.
     */
    private static DataSource dataSource;

    /** The data model catalogue for the data model under test. */
    protected static Catalogue catalogue;

    /** The configured bean validator factory. */
    protected static ValidatorFactory validatorFactory;

    /** An instance of the application to test. The database configuration is taken from this. */
    public static DropwizardTestController dropwizardTestController;

    /** The main class of the DropWizard application to test. */
    private final Class starsApplicationClass;

    /** The path to the DropWizard configuration. */
    private final String configPath;

    /** Holds the test setup controller. */
    protected final TestSetupController testSetupController;

    /** The type of the entities being tested. */
    private final Class<E> entityType;

    /**
     * Creates a database integration test, using the test data and DAO supplied.
     *
     * @param testData            The test data supplier.
     * @param entityType          The type of the entity being tested.
     * @param testSetupController The test setup controller.
     * @param dwApplicationClass  The main class of the DropWizard application to test.
     * @param configPath          The path to the DropWizard configuration.
     */
    public DatabaseCRUDTestBase(CRUDTestDataSupplier<E, K> testData, Class<E> entityType,
        TestSetupController testSetupController, Class dwApplicationClass, String configPath) {
        super(testData.getEqualityChecker());

        this.entityType = entityType;
        this.testData = testData;
        this.testSetupController = testSetupController;
        this.starsApplicationClass = dwApplicationClass;
        this.configPath = configPath;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        dropwizardTestController.stop();
    }

    @Before
    public void setup() throws Exception {
        startDropWizardApp();
        initCatalogue();
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

    /** Sets up the data model catalogue for the data model under test. */
    public void initCatalogue() {
        if (fireOnceRule.shouldFireRule()) {
            catalogue = testSetupController.initCatalogue(dropwizardTestController.getConfiguration());
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
    public void loadReferenceData() {
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
     * Standardized test for find all, should return nothing when their is no test data present.
     *
     * @param findAllMethodName The name of the find all method to invoke.
     */
    public void testFindAllEmpty(String findAllMethodName) {
        BaseDAO<E, K> txEntityDAO = getTransactionalDAO();

        List<Entity> all = (List<Entity>) ReflectionUtils.callMethod(txEntityDAO, findAllMethodName, new Object[] {});

        Assert.assertEquals("Find all should return nothing on empty data.", 0, all.size());
    }

    /**
     * Standardized test for find all, should return 1 item, when a single test data item is present.
     *
     * @param findAllMethodName The name of the find all method to invoke.
     */
    public void testFindAllNotEmpty(String findAllMethodName) throws EntityValidationException,
        EntityAlreadyExistsException {
        BaseDAO<E, K> txEntityDAO = getTransactionalDAO();

        Entity created = txEntityDAO.create(testData.getInitialValue());
        List<Entity> all = (List<Entity>) ReflectionUtils.callMethod(txEntityDAO, findAllMethodName, new Object[] {});

        Assert.assertEquals("Find all should find one item.", 1, all.size());
    }

    /**
     * Should supply a DAO instance to access the database through.
     *
     * @return A DAO instance to access the database through.
     */
    protected abstract CRUD<E, K> getDao();

    /**
     * Override to supply a DAO instance to access the database through, that runs all its methods in single
     * transactions.
     *
     * @return A transactional DAO instance to access the database through.
     */
    protected BaseDAO getTransactionalDAO() {
        return null;
    }
}
