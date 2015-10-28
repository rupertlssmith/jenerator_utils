package com.thesett.util.entity;

/**
 * Create defines the 'create' operation on an entity.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Create a new instance of an entity. </td></tr>
 * </table></pre>
 *
 * @param <E> The type of entity.
 */
public interface Create<E> {
    /**
     * Creates a new instance of the entity in the database.
     *
     * <p/><em>Note: The entity instance returned by this will be attached.</em>
     *
     * @param  entity The entity to create.
     *
     * @return A new instance of the entity (attached).
     *
     * @throws EntityException If the entity cannot be created because it already exists (EntityAlreadyExistsException).
     *                         If the entity cannot be created because it is invalid (EntityValidationException).
     */
    E create(E entity) throws EntityException;
}
