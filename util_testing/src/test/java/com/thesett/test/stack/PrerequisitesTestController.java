package com.thesett.test.stack;

import java.io.Serializable;

import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityDeletionException;
import com.thesett.util.entity.EntityNotExistsException;
import com.thesett.util.entity.EntityValidationException;

/**
 * PrerequisitesTestController ensures that test data is inserted, in a just-in-time fashion, prior to running a test
 * case. In the comment for {@link CRUDTestController} it is stated that a test case starting with a create operation
 * should expect no entity to exist yet, but the other operations should expect and entity to already exist to perform
 * the first test action on.
 *
 * <p/>Important: When the first action operation is retrieve or delete, the value to retrieve or delete will not have
 * an id assigned, when the test requests this operation. This prerequisites controller will create an entity to
 * retrieve or delete, assigning an id for it. This id is then substituted in the forwarding call to the delegate.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Ensure entities already exist for the first retrieve, update or delete operation. </td></tr>
 * </table></pre>
 */
public class PrerequisitesTestController<E extends Entity<K>, K extends Serializable> extends CRUDTestController<E, K> {
    /** The test data supplier, used to initialize test data. */
    private final CRUDTestDataSupplier<E, K> testDataSupplier;

    /** Tracks if this is the first operation or not. */
    private boolean firstOp = true;

    /**
     * Creates a new test visitor, with an optional delegate (the level of the stack below this).
     *
     * @param testDataSupplier The test data supplier, used to initialize test data.
     * @param delegate         The level of the stack below this.
     */
    public PrerequisitesTestController(CRUDTestDataSupplier<E, K> testDataSupplier, CRUD<E, K> delegate) {
        super(delegate);

        this.testDataSupplier = testDataSupplier;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Consumes the first operation, then forwards to the delegate.
     */
    public E create(E entity) throws EntityAlreadyExistsException, EntityValidationException {
        if (firstOp) {
            firstOp = false;
        }

        // Forward the operation to the delegate.
        return super.create(entity);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Creates an entity instance, then forwards to the delegate substituting the newly assigned id.
     */
    public E retrieve(K id) {
        try {
            if (firstOp) {
                super.create(testDataSupplier.getInitialValue());
                firstOp = false;
            }
        } catch (EntityAlreadyExistsException e) {
            throw new IllegalStateException("Create of initial test data failed.", e);
        } catch (EntityValidationException e) {
            throw new IllegalStateException("Create of initial test data failed.", e);
        }

        // Forward the operation to the delegate, but substitute the newly created id.
        return super.retrieve(testDataSupplier.getInitialValue().getId());
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Creates an entity instance, then forwards to the delegate. No id substitution is needed as the test data
     * supplier itself will update the id of the entity to update, if needed.
     */
    public E update(K id, E entity) throws EntityNotExistsException, EntityValidationException {
        try {
            if (firstOp) {
                super.create(testDataSupplier.getInitialValue());
                firstOp = false;
            }
        } catch (EntityAlreadyExistsException e) {
            throw new IllegalStateException("Create of initial test data failed.", e);
        }

        // Forward the operation to the delegate.
        return super.update(testDataSupplier.getInitialValue().getId(), entity);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Creates an entity instance, then forwards to the delegate substituting the newly assigned id.
     */
    public void delete(K id) throws EntityDeletionException {
        try {
            if (firstOp) {
                super.create(testDataSupplier.getInitialValue());
                firstOp = false;
            }
        } catch (EntityAlreadyExistsException e) {
            throw new IllegalStateException("Create of initial test data failed.", e);
        } catch (EntityValidationException e) {
            throw new IllegalStateException("Create of initial test data failed.", e);
        }

        // Forward the operation to the delegate, but substitute the newly created id.
        super.delete(testDataSupplier.getInitialValue().getId());
    }
}
