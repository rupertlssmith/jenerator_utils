package com.thesett.util.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;

import com.thesett.aima.state.Attribute;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.PagingResult;
import com.thesett.util.entity.Entity;

/**
 * ModelAwareDAO defines a DAO that makes use of the runtime model ({@link com.thesett.catalogue.model.Catalogue}) to
 * provide more flexible queries that can incorporate the meta-level into the query.
 *
 * <p/>The browse operations take a mapping of property names and attribute values to be matched. The use of a map
 * allows multiple properties to be specified in a single call. The values to be matched are specified as attributes and
 * the query to fetch elements matching the specified values is built from this. It is possible to use attributes that
 * specify ranges of values, to retrieve entities matching the range. The browse method
 * {@link #browse(java.util.Map, String, Class, String)} takes the type of entity to be retrieved, and restricts its
 * results to just that entity type, the other browse method {@link #browse(java.util.Map, String, Class)} does not
 * restrict to a type, so can retrieve multiple types of entity in a single query.
 *
 * <p/>The attributes do not have to be fully specified, range and wild-card attributes are accepted. Any attribute that
 * is a member of an entity, and is not specified at all in a query will match any value of that attribute in the
 * returned entities.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><td> Perform view queries with partial attribute specification across all entities.
 * <tr><td> Perform view queries with partial attribute specification over a single entity type.
 * </table></pre>
 */
public interface ModelAwareDAO<E extends Entity<K>, K extends Serializable> extends BaseDAO<E, K> {
    /**
     * Lists all values of an entity matching a set of named attributes.
     *
     * @param  matchings      The attributes to match.
     * @param  entityTypeName The name of the entity type to query.
     *
     * @return A list of matching entities.
     */
    List<E> browse(Map<String, Attribute> matchings, String entityTypeName);

    /**
     * Provides a listing by a view onto an entity matching a set of named attributes.
     *
     * <p/>A view type is passed to specify the view onto the entity that is to be returned by the browse operation,
     * allowing a subset of the entities fields to be retrieved in order to provide a summary of the available entities.
     *
     * <p/>Only entities of the specified type will be returned, other entities conforming to the view will not be.
     *
     * @param  matchings      The attributes to match.
     * @param  viewTypeName   The name of the view type to match and return.
     * @param  viewClass      The class of the view type.
     * @param  entityTypeName The name of the entity type to return.
     *
     * @return A map from entity types to matching entities.
     */
    <T> List<T> browse(Map<String, Attribute> matchings, String viewTypeName, Class<T> viewClass,
        String entityTypeName);

    /**
     * Provides a listing by view of entities matching a set of named attributes.
     *
     * <p/>A view type is passed to specify the view onto the entity that is to be returned by the browse operation,
     * allowing a subset of the entities fields to be retrieved in order to provide a summary of the available entities.
     *
     * <p/>All entity types that conform to the specified view will be returned, so the results can include multiple
     * entity types.
     *
     * @param  matchings    The attributes to match.
     * @param  viewTypeName The name of the view type to match and return.
     * @param  viewClass    The class of the view type.
     *
     * @return A map from entity types to matching entities.
     */
    <T> Map<EntityType, List> browse(Map<String, Attribute> matchings, String viewTypeName, Class<T> viewClass);

    /**
     * Executes a query specified in parts and returns the results in pages. The query to execute consists of an entity
     * name to query on, optional criterion to apply to that entity, and optional joined entity names and criterion to
     * restrict by. This is built into two criteria to be executed against the current session; one to count how many
     * rows the result will contain and one to fetch a single page of those results.
     *
     * @param  from               The index to get from (the start of the page).
     * @param  number             The number of results to return (the size of the page).
     * @param  databaseEntityName The database entity to query.
     * @param  entityTypeName     The type name of the entity to query.
     * @param  viewTypeName       The view type to project the results onto.
     * @param  criterion          The optional criterion to apply to the entity.
     * @param  joins              A map of related entities and criterion to restrict the query by.
     *
     * @return A list of dimension element summaries.
     */
    PagingResult executePagedQuery(int from, int number, String databaseEntityName, String entityTypeName,
        String viewTypeName, Criterion criterion, Map<String, Criterion> joins);
}
