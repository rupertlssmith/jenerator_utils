package com.thesett.test.controllers;

import javax.validation.ValidatorFactory;

import org.hibernate.SessionFactory;
import com.thesett.util.dao.HibernateTransactionalProxy;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class HibernateTransactionDAOFactory extends ProxiableDAOFactory {
    public HibernateTransactionDAOFactory(SessionFactory sessionFactory, ValidatorFactory validatorFactory) {
        super(sessionFactory, validatorFactory);
    }

    @Override
    public <T> T proxy(T toProxy, Class<? extends T> clazz) {
        return HibernateTransactionalProxy.proxy(toProxy, (Class<T>) clazz, sessionFactory);
    }
}
