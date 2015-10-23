package com.thesett.util.entity;

import java.io.Serializable;

/**
 * Retrieve defines the 'retrieve' operation on an entity.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Retrieve an entity by its unique id. </td></tr>
 * </table></pre>
 *
 * @param <E> The type of entity.
 * @param <K> The type of the entities id.
 */
public interface Retrieve<E, K extends Serializable> {
    /**
     * Obtains an entity instance from the database using its primary key.
     *
     * <p/><em>Note: The entity instance returned by this will be attached, provided this is invoked in a transaction,
     * which is the usual case.</em>
     *
     * @param  id The primary key.
     *
     * @return The entity for the key if it exists (attached), or <tt>null</tt> if no matching entity can be found.
     */
    E retrieve(K id);
}
