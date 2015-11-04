package com.thesett.util.orientdb;

import java.io.Serializable;
import java.util.List;

import javax.validation.ValidatorFactory;

import com.orientechnologies.orient.core.db.ODatabase;

import com.thesett.util.dao.BaseDAO;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityNotExistsException;
import com.thesett.util.entity.EntityValidationException;

/**
 * OrientDBBaseDAO is an implementation of the {@link BaseDAO} on top of OrientDB.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> CRUD and simple finds on an entity. </td></tr>
 * </table></pre>
 *
 * @param <E> The type of entities that this DAO manages.
 * @param <K> The type of database K that the entity uses.
 */
public class OrientDBBaseDAO<E extends Entity<K>, K extends Serializable> implements BaseDAO<E, K> {
    public OrientDBBaseDAO(ODatabase orientDB, ValidatorFactory validatorFactory) {
    }

    /** {@inheritDoc} */
    public E detach(E e) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public E create(E entity) throws EntityAlreadyExistsException, EntityValidationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public void delete(K id) {
    }

    /** {@inheritDoc} */
    public E retrieve(K id) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public E update(K id, E entity) throws EntityNotExistsException, EntityValidationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public List<E> browse(String entityTypeName) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public List<E> findByExample(E example, String entityTypeName) {
        throw new UnsupportedOperationException();
    }
}
