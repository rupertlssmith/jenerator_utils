package com.thesett.util.entity;

import java.io.Serializable;

/**
 * Delete defines the 'delete' operation on an entity.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Delete an entity. </td></tr>
 * </table></pre>
 *
 * @param <K> The type of the entities id.
 */
public interface Delete<K extends Serializable> {
    /**
     * Removes an entity instance from the database.
     *
     * @param  id The id of the entity to remove.
     *
     * @throws EntityException If the entity cannot be deleted (EntityDeletionException).
     */
    void delete(K id) throws EntityException;
}
