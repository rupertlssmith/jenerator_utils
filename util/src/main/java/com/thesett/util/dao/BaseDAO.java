/*
 * Copyright The Sett Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesett.util.dao;

import java.io.Serializable;
import java.util.List;

import com.thesett.util.entity.CRUD;
import com.thesett.util.entity.Entity;

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
 * <tr><td> List all entity instances. </td></tr>
 * <tr><td> Perform deep query by example. </td></tr>
 * </table></pre>
 *
 * @param <E> The type of entities that this DAO manages.
 * @param <K> The type of database K that the entity uses.
 */
public interface BaseDAO<E extends Entity<K>, K extends Serializable> extends CRUD<E, K>
{
    /**
     * Lists all values of an entity.
     *
     * @return A list of matching entities (all of a particular type).
     */
    List<E> browse();

    /**
     * Lists all values that have fields that match the non-null fields in the example. Id fields are ignored.
     * Associations are expanded into join criteria recursively, treating the associated values as examples. The ability
     * to supported associated examples goes deeper than the default behaviour of Hibernate for example.
     *
     * @param  example The example to query by.
     *
     * @return A list of all matching values.
     */
    List<E> findByExample(E example);

    /**
     * Detaches an entity from the current session. Further changes to it will not be saved back to the database.
     *
     * <p/>This can be used for any entity type, not just the entities managed by this DAO.
     *
     * @param  e The entity to detach.
     *
     * @return The detached entity.
     */
    <T> T detach(T e);
}
