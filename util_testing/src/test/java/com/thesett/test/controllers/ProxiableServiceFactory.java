package com.thesett.test.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.validation.ValidatorFactory;

import org.hibernate.SessionFactory;

/**
 * ProxiableDAOFactory provides DAO implementations wrapped in supplied proxies. The DAO implementations used are the
 * real DAOs attached to the database under test, through a supplied Hibernate session factory.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide a test proxied DAO to access a particular entity type. </td></tr>
 * </table></pre>
 */
public abstract class ProxiableServiceFactory implements ReflectiveServiceFactory {
    protected final SessionFactory sessionFactory;
    protected final ValidatorFactory validatorFactory;

    /** Holds a mapping from DAO interface to implementation. */
    private Map<Class, Object> services = new HashMap<>();

    public ProxiableServiceFactory(SessionFactory sessionFactory, ValidatorFactory validatorFactory) {
        this.sessionFactory = sessionFactory;
        this.validatorFactory = validatorFactory;

    }

    /**
     * Override this to add a proxy to wrap around the service.
     *
     * @param  toProxy The service to proxy.
     * @param  clazz   The service class to proxy.
     * @param  <T>     The type of the returned proxy interface.
     *
     * @return A proxied service.
     */
    public abstract <T> T proxy(T toProxy, Class<? extends T> clazz);

    /**
     * Adds a DAO to the set of DAOs that can be supplied by this factory.
     *
     * @param serviceClass The service class.
     * @param service      The service implementation.
     */
    public void addService(Class serviceClass, Object service) {
        services.put(serviceClass, service);
    }

    /** {@inheritDoc} */
    public <S> S getService(Class<? extends S> serviceClass) {
        S service = (S) services.get(serviceClass);

        return proxy(service, serviceClass);
    }
}
