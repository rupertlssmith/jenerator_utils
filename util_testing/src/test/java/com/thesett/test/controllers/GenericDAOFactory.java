package com.thesett.test.controllers;

import java.lang.reflect.Proxy;

import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;
import com.thesett.util.proxies.DefaultProxy;

/**
 * GenericDAOFactory provides a generic DAO to access any entity type, by simulating standard DAO behaviour in memory
 * only.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide a generic DAO to access any entity type. </td></tr>
 * </table></pre>
 */
public class GenericDAOFactory implements ReflectiveDAOFactory {
    private final CRUD genericDao;

    public GenericDAOFactory(CRUD genericDao) {
        this.genericDao = genericDao;
    }

    /** {@inheritDoc} */
    public <E extends Entity<Long>> CRUD<E, Long> getDAO(Class<? extends CRUD<E, Long>> daoClass) {
        CRUD<E, Long> dao =
            (CRUD<E, Long>) Proxy.newProxyInstance(genericDao.getClass().getClassLoader(), new Class[] { daoClass },
                new DefaultProxy(genericDao));

        return dao;
    }
}
