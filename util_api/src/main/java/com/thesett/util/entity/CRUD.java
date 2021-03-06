package com.thesett.util.entity;

import java.io.Serializable;

/**
 * BaseDAO defines the basic operations of a DAO; CRUD operations on an entity and some default find operations.
 *
 * <p/>Please standardize generic database operations on this DAO, and not in the specific DAO implementations for each
 * entity type. For example, if adding a delete by primary key method, add it here, so that the code can be re-used
 * across all entity types. This will keep our DAO code consistent. Consistency is important because JPA development can
 * be very awkward to get right, therefore it is important to document conditions such as whether entities are returned
 * in attached or detached states, and so on.
 *
 * <p/>To add to the comment above, finder methods specific to particular entity types are expected to go in the
 * specific DAO implementations, as is other behaviour which is specific to a particular entity.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> CRUD and simple finds on an entity. </td></tr>
 * </table></pre>
 *
 * @param <E> The type of entities that this DAO manages.
 * @param <K> The type of database K that the entity uses.
 */
public interface CRUD<E extends Entity<K>, K extends Serializable> extends Create<E>, Retrieve<E, K>, Update<E, K>,
    Delete<K> {
}
