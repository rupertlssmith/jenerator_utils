package com.thesett.util.entity;

import java.io.Serializable;

/**
 * Update defines the 'update' operation on an entity.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Update the state of an entity. </td></tr>
 * </table></pre>
 *
 * @param <E> The type of entity.
 */
public interface Update<E, K extends Serializable> {
    /**
     * Merges changes to an entity instance into the database.
     *
     * <p/><em>Note: The entity instance returned by this will be attached.</em>
     *
     * <p/><em>Note: The entity instance supplied to this can be attached or detached. See documentation on JPA
     * EntityManager for a description of how it behaves differently when the entity is attached or detached.</em>
     *
     * @param  id     The primary key of the entity to update.
     * @param  entity The entity to merge into the database.
     *
     * @return The modified entity (attached).
     *
     * @throws EntityException If the entity cannot be modified because it does not exist (EntityNotExistsException). If
     *                         the entity cannot be saved because it is invalid (EntityValidationException).
     */
    E update(K id, E entity) throws EntityException;
}
