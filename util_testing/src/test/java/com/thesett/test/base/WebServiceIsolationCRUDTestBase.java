package com.thesett.test.base;

import java.io.Serializable;

import org.junit.Before;
import com.thesett.test.stack.CRUDTestBase;
import com.thesett.test.stack.CRUDTestDataSupplier;
import com.thesett.test.stack.GenericDAOTestController;
import com.thesett.test.stack.JsonSerDesTestController;
import com.thesett.test.stack.PrerequisitesTestController;
import com.thesett.test.stack.TestIdMaintainerTestController;
import com.thesett.util.commands.refdata.RefDataUtils;
import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;

/**
 * WebServiceIsolationCRUDTestBase test a service interface, that exposes some CRUD methods. A
 * {@link GenericDAOTestController} is used to mock out the DAO so no database is involved in the test. The service
 * layer is being tested in isolation from the database.
 *
 * <p/>The {@link JsonSerDesTestController} is used in this test stack, to simulate a real webservice call.
 *
 * @param <E> The type of entity being tested.
 * @param <K> The typ of the keys of the entity.
 */
public abstract class WebServiceIsolationCRUDTestBase<E extends Entity<K>, K extends Serializable>
    extends CRUDTestBase<E, K> {
    /** Maintains a reference to the generic DAO, for setting up prerequisite test data. */
    public CRUD<E, K> genericDao;

    /**
     * Creates the simulated isolated webservice test cases.
     *
     * @param testData The test data supplier to use.
     */
    public WebServiceIsolationCRUDTestBase(CRUDTestDataSupplier<E, K> testData) {
        super(testData.getEqualityChecker());
        this.testData = testData;
    }

    /** Loads all reference data caches into memory, from their defining CSV files. */
    @Before
    public void loadReferenceData() {
        if (fireOnceRule.shouldFireRule()) {
            RefDataUtils.loadReferenceDataToCacheOnly();
        }
    }

    /** {@inheritDoc} */
    @Before
    public void buildTestStack() {
        genericDao = new GenericDAOTestController<E, K>(testData, null);

        CRUD<E, K> service = getServiceLayer(genericDao);
        CRUD<E, K> serdes = new JsonSerDesTestController<E, K>(service);
        CRUD<E, K> idMaintainer = new TestIdMaintainerTestController<E, K>(testData, serdes);

        testController = new PrerequisitesTestController<E, K>(testData, idMaintainer);
    }

    /**
     * Implementations should override this to supply a service layer implementation on top of the supplied (generic)
     * dao.
     *
     * @param  dao The DAO implementation to build the service layer on top of.
     *
     * @return A service layer implementation.
     */
    protected abstract CRUD<E, K> getServiceLayer(CRUD<E, K> dao);
}
