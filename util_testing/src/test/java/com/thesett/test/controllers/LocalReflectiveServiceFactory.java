package com.thesett.test.controllers;

import javax.validation.ValidatorFactory;

import org.hibernate.SessionFactory;
import com.thesett.util.dao.HibernateSessionAndDetachProxy;

/**
 * LocalReflectiveServiceFactory provides local service instance.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Provide a service reflectively by its interface. </td></tr>
 * </table></pre>
 */
public class LocalReflectiveServiceFactory extends ProxiableServiceFactory {
    public LocalReflectiveServiceFactory(SessionFactory sessionFactory, ValidatorFactory validatorFactory) {
        super(sessionFactory, validatorFactory);
    }

    /** {@inheritDoc} */
    public <T> T proxy(T toProxy, Class<? extends T> clazz) {
        return HibernateSessionAndDetachProxy.proxy(toProxy, (Class<T>) clazz, sessionFactory);
    }
}
