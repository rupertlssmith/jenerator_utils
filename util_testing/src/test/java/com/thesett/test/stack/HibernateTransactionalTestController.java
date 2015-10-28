package com.thesett.test.stack;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import org.hibernate.SessionFactory;
import com.thesett.util.dao.HibernateTransactionalProxy;
import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityException;

/**
 * HibernateTransactionalTestController applies wraps all test methods in Hibernate transactions on the current
 * Hibernate session, using a {@link HibernateTransactionalProxy}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Ensure all test methods run within transactions. </td></tr>
 * </table></pre>
 */
public class HibernateTransactionalTestController<E extends Entity<K>, K extends Serializable>
    extends CRUDTestController<E, K> {
    /** Holds the proxied delegate, that is invoked within transactions. */
    private final CRUD<E, K> proxiedDelegate;

    /**
     * Builds a transactional layer of the test stack.
     *
     * @param delegate       The delegate to invoke within transactions.
     * @param sessionFactory The Hiberate session factory.
     */
    public HibernateTransactionalTestController(CRUD<E, K> delegate, SessionFactory sessionFactory) {
        super(delegate);
        proxiedDelegate =
            (CRUD<E, K>) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), new Class[] { CRUD.class },
                new HibernateTransactionalProxy(delegate, sessionFactory));
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
