package com.thesett.util.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityNotExistsException;
import com.thesett.util.entity.EntityValidationException;
import com.thesett.util.generics.Generics;
import com.thesett.util.memento.BeanMemento;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;

/**
 * HibernateBaseDAO is an implementation of the {@link BaseDAO} on top of a hibernate session factory.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> CRUD and simple finds on an entity. </td></tr>
 * </table></pre>
 *
 * @param <E> The type of entities that this DAO manages.
 * @param <K> The type of database K that the entity uses.
 */
public abstract class HibernateBaseDAO<E extends Entity<K>, K extends Serializable> implements BaseDAO<E, K> {
    /** The Hibernate session factory. */
    protected final SessionFactory sessionFactory;

    /** The type of the entity that the DAO persists. */
    protected final Class<?> entityClass;

    /** The bean validator to apply prior to all data insertion. */
    protected final Validator validator;

    /**
     * Creates the Hibernate DAO on top of the specified session factory.
     *
     * @param sessionFactory   The Hibernate session factory to use.
     * @param validatorFactory The bean validator factory to use to validate all data prior to insertion.
     */
    public HibernateBaseDAO(SessionFactory sessionFactory, ValidatorFactory validatorFactory) {
        this.sessionFactory = sessionFactory;
        this.validator = validatorFactory.getValidator();

        // Note: this only works because this class is abstract, and sub-classes will provide the type parameter
        // directly.
        this.entityClass = Generics.getTypeParameter(getClass());
    }

    /** {@inheritDoc} */
    public E create(E entity) throws EntityAlreadyExistsException, EntityValidationException {
        checkNotNull(entity);

        validate(entity);

        // Check that an entity with matching id does not already exist.
        if (entity.getId() != null && retrieve(entity.getId()) != null) {
            throw new EntityAlreadyExistsException();
        }

        currentSession().save(entity);

        return entity;
    }

    /** {@inheritDoc} */
    public E retrieve(K id) {
        checkNotNull(id);

        return (E) currentSession().get(entityClass, id);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>The id passed to this method must not by <tt>null</tt>. It will be set on the entity as its id, potentially
     * overwriting a different id that may already be set on the entity, meaning that this method can be used to copy
     * the value of one entity over another. It is up to the caller to ensure that this behaviour is only used
     * intentionally, this method does not prevent it or check for it in any way. The entity being updated can be given
     * to this method without an id on it.
     *
     * @throws EntityValidationException Iff the id on the entity is <tt>null</tt>.
     */
    public E update(K id, E entity) throws EntityNotExistsException, EntityValidationException {
        checkNotNull(id);
        checkNotNull(entity);

        entity.setId(id);

        validate(entity);

        // Check that an entity with matching id already exists, in order to update it.
        if (retrieve(entity.getId()) == null) {
            throw new EntityNotExistsException();
        }

        return (E) currentSession().merge(entity);
    }

    /** {@inheritDoc} */
    public void delete(K id) {
        checkNotNull(id);

        currentSession().delete(retrieve(id));
    }

    /** {@inheritDoc} */
    public E detach(E e) {
        currentSession().flush(); // Sometimes needed because new objects cannot be evicted without flushing first.
        currentSession().evict(e);

        return e;
    }

    /** {@inheritDoc} */
    public List<E> browse(String entityTypeName) {
        Session session = currentSession();

        // Create the selection criteria for the block.
        Criteria selectCriteria = session.createCriteria(entityClass);

        // Execute the query to get the block.
        List results = selectCriteria.list();

        return results;
    }

    /** {@inheritDoc} */
    public List<E> findByExample(E example, String entityTypeName) {
        // Create the basic example criteria.
        Criteria exampleCriteria = currentSession().createCriteria(example.getClass()).add(Example.create(example));

        BeanMemento memento = new BeanMemento(example);
        memento.captureNonNull();

        // Add criteria for all relationships (including relationships to reference data).
        for (String field : memento.getAllFieldNames(example.getClass())) {
            try {
                Object relatedItem = memento.get(example.getClass(), field);

                if (relatedItem != null) {
                    if (relatedItem instanceof Entity) {
                        exampleCriteria.createCriteria(field).add(Example.create(relatedItem));
                    } else {
                        // TODO: Improve this, it uses exceptions for flow control. Would be better if enum types
                        // implemented a marker interface, and an instanceof check could be done here.
                        try {
                            relatedItem.getClass().getConstructor(EnumeratedStringAttribute.class);
                        } catch (NoSuchMethodException e) {
                            e = null;

                            continue;
                        }

                        exampleCriteria.add(Restrictions.eq(field, relatedItem));
                    }
                }
            } catch (NoSuchFieldException e) {
                // Ignore unknown fields.
                e = null;
            }
        }

        return exampleCriteria.list();
    }

    /**
     * Retrieves a named query.
     *
     * @param  queryName The name of the query to retrieve.
     *
     * @return The matching query.
     */
    protected Query namedQuery(String queryName) {
        return currentSession().getNamedQuery(queryName);
    }

    /**
     * Applies a query and returns the results of it in a list.
     *
     * @param  query The query to apply.
     *
     * @return A list of query results.
     */
    protected List<E> list(Query query) {
        return checkNotNull(query).list();
    }

    /**
     * Applies a query that is expected to find at most one match.
     *
     * @param  query The query to apply.
     *
     * @return The single match, or <tt>null</tt> if none is found.
     */
    protected E findOne(Query query) {
        return (E) checkNotNull(query).uniqueResult();
    }

    /**
     * Applies a query that may find more than one match, but returns only the first result.
     *
     * @param  query The query to apply.
     *
     * @return The single match, or <tt>null</tt> if none is found.
     */
    protected E findFirst(Query query) {
        List list = checkNotNull(query).list();

        if (!list.isEmpty()) {
            return (E) list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the current {@link Session}.
     *
     * @return the current session
     */
    protected Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    protected <O> O checkNotNull(O object) {
        if (object == null) {
            throw new IllegalArgumentException();
        }

        return object;
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
}
