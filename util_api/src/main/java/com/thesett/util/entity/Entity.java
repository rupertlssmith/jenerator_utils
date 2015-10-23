package com.thesett.util.entity;

import java.io.Serializable;

/**
 * Entity defines the basic properties that all entities have; a Serializable id.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Associate an entity with an id. </td></tr>
 * </table></pre>
 *
 * @param <K> The type of id for the entity.
 */
public interface Entity<K extends Serializable> {
    /**
     * Supplies the entities id.
     *
     * @return The entities id.
     */
    K getId();

    /**
     * Establishes the entities id.
     *
     * @param id The entities id.
     */
    void setId(K id);
}
