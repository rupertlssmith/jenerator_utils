package com.thesett.test.stack;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityDeletionException;
import com.thesett.util.entity.EntityException;
import com.thesett.util.entity.EntityNotExistsException;
import com.thesett.util.entity.EntityValidationException;

/**
 * GenericDAOTestController acts as a mocked out DAO, that is sufficient to emulate the basic DAO behaviour needed to
 * pass the action-observation test cases that can be applied to a {@link CRUDTestController}. Its purpose is simply to
 * act as a baseline for DAO testing.
 *
 * <p/>More sophisticated tests can be build on top of this baseline test controller, for example, to add tests to check
 * that serialization/deserialization works, without using a real DAO.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Emulate simple CRUD DAO behaviour for any entity. </td></tr>
 * </table></pre>
 */
public class GenericDAOTestController<E extends Entity<K>, K extends Serializable> extends CRUDTestController<E, K> {
    /** The validator factory, to be initialized only once. */
    private static ValidatorFactory validatorFactory;

    /** Holds the database of entities. */
    private final Map<K, E> database = new HashMap<K, E>();

    /** The key supplier, used to generate fresh keys. */
    private final KeySupplier<K> keySupplier;

    /** The bean validator to apply prior to all data insertion. */
    private final Validator validator;

    /**
     * Creates a new test visitor, with an optional delegate (the level of the stack below this).
     *
     * @param keySupplier The key supplier, used to generate fresh keys.
     * @param delegate    The level of the stack below this.
     */
    public GenericDAOTestController(KeySupplier<K> keySupplier, CRUD<E, K> delegate) {
        super(delegate);

        this.keySupplier = keySupplier;

        validator = initValidator();
    }

    /** {@inheritDoc} */
    public E create(E entity) throws EntityException {
        if (entity == null) {
            throw new IllegalArgumentException("'entity' cannot be null.");
        }

        // Assign the entity a new id, if it does not already have one.
        if (entity.getId() == null) {
            entity.setId(keySupplier.createFreshKey());
        }

        // Check that an entity with matching id does not already exist.
        if (database.containsKey(entity.getId())) {
            throw new EntityAlreadyExistsException();
        }

        validate(entity);

        database.put(entity.getId(), entity);

        // Pass the operation on to the delegate.
        super.create(entity);

        return entity;
    }

    /** {@inheritDoc} */
    public E retrieve(K id) {
        if (id == null) {
            throw new IllegalArgumentException("'id' cannot be null.");
        }

        E result = database.get(id);

        // Pass the operation on to the delegate.
        super.retrieve(id);

        return result;
    }

    /** {@inheritDoc} */
    public E update(K id, E entity) throws EntityException {
        if (entity == null) {
            throw new IllegalArgumentException("'entity' cannot be null.");
        }

        // Check that an entity with matching id exists to update, or fail if not.
        if (!database.containsKey(entity.getId())) {
            throw new EntityNotExistsException();
        }

        validate(entity);

        database.put(entity.getId(), entity);

        // Pass the operation on to the delegate.
        super.update(id, entity);

        return entity;
    }

    /** {@inheritDoc} */
    public void delete(K id) throws EntityException {
        if (id == null) {
            throw new IllegalArgumentException("'id' cannot be null.");
        }

        database.remove(id);

        // Pass the operation on to the delegate.
        super.delete(id);
    }

    /**
     * Applies bean validation to the entity.
     *
     * @param  entity The entity to validate.
     *
     * @throws EntityValidationException If the entity fails validation checks.
     */
    private void validate(E entity) throws EntityValidationException {
        Set<ConstraintViolation<E>> violations = validator.validate(entity);

        if (!violations.isEmpty()) {
            throw new EntityValidationException(violations.toString());
        }
    }

    /**
     * Loads the bean validator from its configuration.
     *
     * @return A bean validator.
     */
    private Validator initValidator() {
        if (validatorFactory == null) {
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream("constraints.xml");
            validatorFactory = Validation.byDefaultProvider().configure().addMapping(resource).buildValidatorFactory();
        }

        return validatorFactory.getValidator();
    }
}
