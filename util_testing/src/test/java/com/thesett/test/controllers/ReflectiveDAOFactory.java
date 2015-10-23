package com.thesett.test.controllers;

import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;

/**
 * ReflectiveDAOFactory provides a way of obtaining DAOs, which may also have been wrapped with test functionality, for
 * the purpose of creating prerequisite test data.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Provide a DAO to access a particular entity type. </td></tr>
 * </table></pre>
 */
public interface ReflectiveDAOFactory {
    /**
     * Provides a DAO to perform CRUD operations on a specified entity type.
     *
     * @param  daoClass The class of the DAO interface to supply.
     * @param  <E>      The type of the entity.
     *
     * @return A CRUD interface for the specified entity type.
     */
    <E extends Entity<Long>> CRUD<E, Long> getDAO(Class<? extends CRUD<E, Long>> daoClass);
}
