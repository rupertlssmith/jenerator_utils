package com.thesett.test.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.validation.ValidatorFactory;

import org.hibernate.SessionFactory;
import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;

/**
 * ProxiableDAOFactory provides DAO implementations wrapped in supplied proxies. The DAO implementations used are the
 * real DAOs attached to the database under test, through a supplied Hibernate session factory.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide a test proxied DAO to access a particular entity type. </td></tr>
 * </table></pre>
 */
public abstract class ProxiableDAOFactory implements ReflectiveDAOFactory {
    protected final SessionFactory sessionFactory;
    protected final ValidatorFactory validatorFactory;

    /** Holds a mapping from DAO interface to implementation. */
    private Map<Class, CRUD> daos = new HashMap<>();

    public ProxiableDAOFactory(SessionFactory sessionFactory, ValidatorFactory validatorFactory) {
        this.sessionFactory = sessionFactory;
        this.validatorFactory = validatorFactory;
    }

    /**
     * Override this to add a proxy to wrap around the DAO.
     *
     * @param  toProxy The DAO to proxy.
     * @param  clazz   The DAO class to proxy.
     * @param  <T>     The type of the returned proxy interface.
     *
     * @return A proxies DAO.
     */
    public abstract <T> T proxy(T toProxy, Class<? extends T> clazz);

    /**
     * Adds a DAO to the set of DAOs that can be supplied by this factory.
     *
     * @param daoClass The DAO class.
     * @param dao      The DAO implementation.
     */
    public void addDAO(Class daoClass, CRUD dao) {
        daos.put(daoClass, dao);
    }

    /** {@inheritDoc} */
    public <E extends Entity<Long>> CRUD<E, Long> getDAO(Class<? extends CRUD<E, Long>> daoClass) {
        CRUD<E, Long> dao = daos.get(daoClass);

        return proxy(dao, daoClass);
    }
}
