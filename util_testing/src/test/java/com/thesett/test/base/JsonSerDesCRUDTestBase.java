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
 * JsonSerDesCRUDTestBase expands on the baseline test cases provided by {@link BaselineCRUDTestBase}, to add
 * serialization and deserialization of entities through JSON into the test stack. This simulates passing entities
 * through a webservice layer, and checks that all required fields are serializing properly.
 *
 * @param <E> The type of entity being tested.
 * @param <K> The typ of the keys of the entity.
 */
public class JsonSerDesCRUDTestBase<E extends Entity<K>, K extends Serializable> extends CRUDTestBase<E, K> {
    /**
     * Creates the serialisation test cases.
     *
     * @param testData The test data supplier to use.
     */
    public JsonSerDesCRUDTestBase(CRUDTestDataSupplier<E, K> testData) {
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
        CRUD<E, K> genericDao = new GenericDAOTestController<E, K>(testData, null);
        CRUD<E, K> serdes = new JsonSerDesTestController<E, K>(genericDao);
        CRUD<E, K> idMaintainer = new TestIdMaintainerTestController<E, K>(testData, serdes);

        testController = new PrerequisitesTestController<E, K>(testData, idMaintainer);
    }
}
