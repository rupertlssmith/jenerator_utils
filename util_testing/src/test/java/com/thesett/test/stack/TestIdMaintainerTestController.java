package com.thesett.test.stack;

import java.io.Serializable;

import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityValidationException;

/**
 * TestIdMaintainerTestController keeps the test entities supplied by a {@link CRUDTestDataSupplier} consistent, when
 * ids are assigned during entity creation. The {@link CRUDTestDataSupplier} supplies an initial entity value and an
 * updated entity value, which should always share the same id. When an entity instance is created, its id will be
 * assigned by a DAO. This id needs to be set on the initial and updated values, and that is what the id maintainer
 * does.
 *
 * <p/>Any DAO in the test stack should be wrapped by this.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Keep test data ids in synch. </td></tr>
 * </table></pre>
 */
public class TestIdMaintainerTestController<E extends Entity<K>, K extends Serializable>
    extends CRUDTestController<E, K> {
    /** The test data supplier, used to keep ids in synch. */
    private final CRUDTestDataSupplier<E, K> testDataSupplier;

    /**
     * Creates a new test visitor, with an optional delegate (the level of the stack below this).
     *
     * @param testDataSupplier The test data supplier, used to keep ids in synch.
     * @param delegate         The level of the stack below this.
     */
    public TestIdMaintainerTestController(CRUDTestDataSupplier<E, K> testDataSupplier, CRUD<E, K> delegate) {
        super(delegate);

        this.testDataSupplier = testDataSupplier;
    }

    /** {@inheritDoc} */
    public E create(E entity) throws EntityAlreadyExistsException, EntityValidationException {
        // Perform delegation to create the entity.
        E result = super.create(entity);

        // Update the ids.
        testDataSupplier.setId(result.getId());

        return result;
    }
}
