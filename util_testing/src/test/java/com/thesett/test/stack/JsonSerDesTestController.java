package com.thesett.test.stack;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityDeletionException;
import com.thesett.util.entity.EntityNotExistsException;
import com.thesett.util.entity.EntityValidationException;
import com.thesett.util.json.JsonClientProxy;

/**
 * JsonSerDesTestController applies serialisation/deserialisation to all data forwarded on to its delegate, and
 * serialisation/deserialisation to all return values being passed back to the caller. In this way, it simulates passing
 * data transfer objects over a webservice call.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class JsonSerDesTestController<E extends Entity<K>, K extends Serializable> extends CRUDTestController<E, K> {
    /** Holds the proxied delegate, that uses pass-by-value through JSON. */
    private final CRUD<E, K> proxiedDelegate;

    /**
     * Builds a serialisation through JSON layer of the test stack.
     *
     * @param delegate The delegate to invoke using pass-by-value through JSON.
     */
    public JsonSerDesTestController(CRUD<E, K> delegate) {
        super(delegate);
        proxiedDelegate =
            (CRUD<E, K>) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), new Class[] { CRUD.class },
                new JsonClientProxy(delegate));
    }

    /** {@inheritDoc} */
    public E create(E entity) throws EntityAlreadyExistsException, EntityValidationException {
        return proxiedDelegate.create(entity);
    }

    /** {@inheritDoc} */
    public E retrieve(K id) {
        return proxiedDelegate.retrieve(id);
    }

    /** {@inheritDoc} */
    public E update(K id, E entity) throws EntityNotExistsException, EntityValidationException {
        return proxiedDelegate.update(id, entity);
    }

    /** {@inheritDoc} */
    public void delete(K id) throws EntityDeletionException {
        proxiedDelegate.delete(id);
    }
}
