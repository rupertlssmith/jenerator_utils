package com.thesett.test.stack;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityDeletionException;
import com.thesett.util.entity.EntityException;
import com.thesett.util.entity.EntityNotExistsException;
import com.thesett.util.entity.EntityValidationException;
import com.thesett.util.hibernate.HibernateDetachProxy;

/**
 * HibernateTransactionalTestController applies wraps all test methods in Hibernate transactions on the current
 * Hibernate session, using a {@link com.thesett.util.dao.HibernateTransactionalProxy}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Ensure all test methods run within transactions. </td></tr>
 * </table></pre>
 */
public class HibernateDetachTestController<E extends Entity<K>, K extends Serializable>
    extends CRUDTestController<E, K> {
    /** Holds the proxied delegate, that is invoked within transactions. */
    private final CRUD<E, K> proxiedDelegate;

    /**
     * Builds a transactional layer of the test stack.
     *
     * @param delegate The delegate to invoke within transactions.
     */
    public HibernateDetachTestController(CRUD<E, K> delegate) {
        super(delegate);
        proxiedDelegate =
            (CRUD<E, K>) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), new Class[] { CRUD.class },
                new HibernateDetachProxy(delegate));
    }

    /** {@inheritDoc} */
    public E create(E entity) throws EntityException {
        return proxiedDelegate.create(entity);
    }

    /** {@inheritDoc} */
    public E retrieve(K id) {
        return proxiedDelegate.retrieve(id);
    }

    /** {@inheritDoc} */
    public E update(K id, E entity) throws EntityException {
        return proxiedDelegate.update(id, entity);
    }

    /** {@inheritDoc} */
    public void delete(K id) throws EntityException {
        proxiedDelegate.delete(id);
    }
}
