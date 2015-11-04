package com.thesett.test.stack;

import java.io.Serializable;
import java.util.List;

import com.thesett.util.dao.BaseDAO;
import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityDeletionException;
import com.thesett.util.entity.EntityException;
import com.thesett.util.entity.EntityNotExistsException;
import com.thesett.util.entity.EntityValidationException;

/**
 * CRUDTestController allows a standard set of tests to be run against entities in a data model, in order to check the
 * correct operation of the basic CRUD functionality against a particular entity. This provides a standard interface
 * onto a test stack in order to run such a set of tests.
 *
 * <p/>The test stacks themselves can be set up in a variety of ways. For example, with entities as beans only plus
 * serialization (of various sorts) in order to check that serialization works, with mocked out DAO layer in order to
 * test correct operation against a test DAO layer, with a test database in place in order to test the full stack.
 *
 * <p/>Complete testing of CRUD functionality is carried out using pairs of operations. An 'action' is carried out
 * against the system under test, using the first operation in a pair, and a second operation is used to determine
 * whether the outcome of the action is behaving as expected. In this way, all testing can be carried out with
 * action-observation pairs.
 *
 * <p/>Prior to a test starting, it is assumed that either no instances of an entity exist, or one instance exists,
 * depending on what the first operation is. This is done in such a way, that the first operation is always assumed to
 * be valid, so if it creates data, no entity should exist yet, if it modifies data, an entity to be modified is assumed
 * to exist.
 *
 * <p/>After a set of test operations has been performed, observations may be made directly against a data store (by
 * some layer in the test stack), in order to check that entities exist or not and in the expected state.
 *
 * <p/>The above pre and post conditions may not apply in some test scenarios. If no actual CRUD persistence operations
 * are performed, for example, in a simple test stack that exercises serialization, the pre and post conditions can be
 * ignored; it is sufficient for the pairs of operations to produce the expected return values defined for test stack
 * operations.
 *
 * <p/>The action-observation pairs to test are:
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Action-Observation </th><th> Expected Result of Operations </th><th> Observable Post Condition. </th>
 * <tr><td> Create-Retrieve    </td><td> Entity created is returned (equals). </td><td> Created entity. </td></tr>
 * <tr><td> Update-Retrieve    </td><td> Entity written is read back (equals). </td><td> Updated entity .</td></tr>
 * <tr><td> Delete-Retrieve    </td><td> Entity cannot be found (fail). </td><td> No entity. </td></tr>
 * <tr><td> Create-Delete      </td><td> Nothing (void). </td><td> No entity. </td></tr>
 * <tr><td> Update-Delete      </td><td> Nothing (void). </td><td> No entity. </td></tr>
 * <tr><td> Retrieve-Delete    </td><td> Nothing (void). </td><td> No entity. </td></tr>
 * <tr><td> Retrieve-Create    </td><td> Cannot create, as already exists (fail). </td><td> Original entity. </td></tr>
 * <tr><td> Update-Create      </td><td> Cannot create, as already exists (fail). </td><td> Updated entity .</td></tr>
 * <tr><td> Delete-Create      </td><td> New entity must have a different id (not equal). </td><td> Created entity. </td></tr>
 * <tr><td> Create-Update      </td><td> Ok, update version returned (equal). </td><td> Updated entity .</td></tr>
 * <tr><td> Delete-Update      </td><td> No entity to update (fail). </td><td> No entity. </td></tr>
 * <tr><td> Retrieve-Update    </td><td> Ok, update version returned (equal). </td><td> Updated entity .</td></tr>
 * </table></pre>
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Allow a delegate to be set to forward all CRUD operations onto. <td> {@link CRUD} </td>
 * <tr><td> Allow custom functionality to be hooked into a stack of CRUD operations.
 * </table></pre>
 *
 * @param <E> The type of entity being tested.
 * @param <K> The type of the entities id.
 */
public abstract class CRUDTestController<E extends Entity<K>, K extends Serializable> implements CRUD<E, K>,
    BaseDAO<E, K> {
    /** The optional delegate, or level below this in the test stack. */
    protected final CRUD<E, K> delegate;

    /**
     * Creates a new test visitor, with an optional delegate (the level of the stack below this).
     *
     * @param delegate The level of the stack below this.
     */
    public CRUDTestController(CRUD<E, K> delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Delegates the operation if a {@link #delegate} is set, otherwise returns <tt>null</tt>.
     */
    public E create(E entity) throws EntityException {
        if (delegate != null) {
            return delegate.create(entity);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Delegates the operation if a {@link #delegate} is set, otherwise returns <tt>null</tt>.
     */
    public E retrieve(K id) {
        if (delegate != null) {
            return delegate.retrieve(id);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Delegates the operation if a {@link #delegate} is set, otherwise returns <tt>null</tt>.
     */
    public E update(K id, E entity) throws EntityException {
        if (delegate != null) {
            return delegate.update(id, entity);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Delegates the operation if a {@link #delegate} is set, otherwise does nothing.
     */
    public void delete(K id) throws EntityException {
        if (delegate != null) {
            delegate.delete(id);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Default implementation of detach which does nothing, simple returns its argument.
     */
    public E detach(E e) {
        return e;
    }

    /** {@inheritDoc} */
    public List<E> browse(String entityTypeName) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public List<E> findByExample(E example, String entityTypeName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Invoked the custom apply functionality on the stack below this point.
     *
     * <p/>{@link #preApply()} is invoked, then the layer below is applied, and {@link #postApply()} is invoked after
     * that. This means that the pre-apply calls are invoked top-to-bottom working down the stack, and the post-apply
     * calls are invoked bottom-to-top working back up the stack.
     */
    public final void apply() {
        preApply();
        delegateApply();
        postApply();
    }

    /** Override to provide a custom pre-apply function. */
    protected void preApply() {
    }

    /** Override to provide a custom post-apply function. */
    protected void postApply() {
    }

    /**
     * Applies to the level of the stack below this, providing a delegate is set, and the delegate is also a
     * TestVisitor.
     */
    private void delegateApply() {
        if ((delegate != null) && (delegate instanceof CRUDTestController)) {
            CRUDTestController testController = (CRUDTestController) delegate;
            testController.apply();
        }
    }
}
