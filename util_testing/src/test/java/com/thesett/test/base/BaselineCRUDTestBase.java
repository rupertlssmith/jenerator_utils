package com.thesett.test.base;

import java.io.Serializable;

import org.junit.Before;
import com.thesett.test.stack.CRUDTestBase;
import com.thesett.test.stack.CRUDTestDataSupplier;
import com.thesett.test.stack.GenericDAOTestController;
import com.thesett.test.stack.PrerequisitesTestController;
import com.thesett.test.stack.TestIdMaintainerTestController;
import com.thesett.util.commands.refdata.RefDataUtils;
import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;

/**
 * BaselineCRUDTestBase provides a baseline implementation of the test stack, for an entity. This checks that the entity
 * and its test data supplier behave as expected, against the generic test DAO (not a real database).
 *
 * @param <E> The type of entity being tested.
 * @param <K> The typ of the keys of the entity.
 */
public class BaselineCRUDTestBase<E extends Entity<K>, K extends Serializable> extends CRUDTestBase<E, K> {
    /**
     * Creates the baseline test cases.
     *
     * @param testData The test data supplier to use.
     */
    public BaselineCRUDTestBase(CRUDTestDataSupplier<E, K> testData) {
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
        CRUD<E, K> idMaintainer = new TestIdMaintainerTestController<E, K>(testData, genericDao);

        testController = new PrerequisitesTestController<E, K>(testData, idMaintainer);
    }
}
