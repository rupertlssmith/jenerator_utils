package com.thesett.util.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ValidatorFactory;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.ResultTransformer;

import com.thesett.aima.attribute.impl.HierarchyAttribute;
import com.thesett.aima.attribute.impl.HierarchyAttributeFactory;
import com.thesett.aima.state.Attribute;
import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.PagingResult;
import com.thesett.catalogue.model.ViewType;
import com.thesett.util.entity.Entity;
import com.thesett.util.memento.BeanMemento;
import com.thesett.util.reflection.ReflectionUtils;

/**
 * ModelAwareBaseDAO is a DAO that makes use of the runtime model ({@link Catalogue}) to provide more flexible queries
 * that make use of the meta-level.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Perform view queries with partial attribute specification across all entities.
 * <tr><td> Perform view queries with partial attribute specification over a single entity type.
 * </table></pre>
 */
public class HibernateModelAwareDAO<E extends Entity<K>, K extends Serializable> extends HibernateBaseDAO<E, K>
    implements ModelAwareDAO<E, K> {
    /** Used for debugging purposes. */
    private static final java.util.logging.Logger LOG =
        java.util.logging.Logger.getLogger(HibernateModelAwareDAO.class.getName());

    /** The run-time model. */
    private final Catalogue catalogue;

    public HibernateModelAwareDAO(SessionFactory sessionFactory, ValidatorFactory validatorFactory,
        Catalogue catalogue) {
        super(sessionFactory, validatorFactory);
        this.catalogue = catalogue;
    }

    /** {@inheritDoc} */
    public List<E> browse(Map<String, Attribute> matchings, String entityTypeName) {
        // Ensure that an entity type has been specified.
        if (entityTypeName == null) {
            throw new IllegalArgumentException("The 'entityTypeName' parameter must not be null.");
        }

        EntityType entityType = catalogue.getEntityType(entityTypeName);

        // Get the name of the entity table to fetch the matching entity from.
        String entityTableName = entityType.getBaseClassName();

        if (entityType == null) {
            throw new IllegalArgumentException("The 'entityTypeName' parameter must specify an entity type in the " +
                "model, but no matching type could be found.");
        }

        // Check that the specified entity type contains attributes of the correct type to match the query.
        for (Map.Entry<String, Attribute> entry : matchings.entrySet()) {
            String propName = entry.getKey();
            Attribute attribute = entry.getValue();

            // Get the type name of the attribute in the parameter.
            String attributeTypeName = attribute.getType().getName();

            // Get the type name of the field in the entity.
            Type type = entityType.getPropertyType(propName);

            if (type != null) {
                String fieldTypeName = type.getName();

                // Check that they are compatible.
                if (!attributeTypeName.equals(fieldTypeName)) {
                    throw new IllegalArgumentException("The type of query parameter " + propName + " is " +
                        attributeTypeName + " which is not compatible with the field of type " + fieldTypeName +
                        " on entity type " + entityType.getName());
                }
            } else {
                throw new IllegalArgumentException("The query parameter " + propName +
                    " does not match any field name of entity type " + entityType.getName());
            }
        }

        // Get the hibernate query criterions for the requested attribute matchings.
        Map<String, Criterion> joins = getByAttributeCriterions(matchings);

        return executeEntityQuery(entityTableName, entityTypeName, null, joins);
    }

    /** {@inheritDoc} */
    public <T> List<T> browse(Map<String, Attribute> matchings, String viewTypeName, Class<T> viewClass,
        String entityTypeName) {
        // Ensure that a view type has been specified.
        if (viewTypeName == null) {
            throw new IllegalArgumentException("The 'view' parameter must not be null.");
        }

        // Ensure that the entity type exists in the model.
        EntityType entityType = catalogue.getEntityType(entityTypeName);

        if (entityType == null) {
            throw new IllegalArgumentException("The 'entityTypeName' parameter must specify an entity type in the " +
                "model, but no matching type could be found.");
        }

        return browse(entityType, matchings, viewTypeName);
    }

    /** {@inheritDoc} */
    public <T> Map<EntityType, List> browse(Map<String, Attribute> matchings, String viewTypeName, Class<T> viewClass) {
        // Ensure that a view type has been specified.
        if (viewTypeName == null) {
            throw new IllegalArgumentException("The 'view' parameter must not be null.");
        }

        ViewType viewType = catalogue.getViewType(viewTypeName);

        // Get all entities in the catalogue and then filter down to just those that match the specified set of field
        // names and types, and conform to the specified view type.
        Collection<EntityType> allEntities = catalogue.getAllEntityTypes();
        Collection<EntityType> entitiesMatchingFields = filterEntitiesMatchingFields(allEntities, matchings);
        Collection<EntityType> entitiesMatchingViews = filterEntitiesMatchingViews(entitiesMatchingFields, viewType);

        // Run a query against each matching entity type to build up the results.
        Map<EntityType, List> results = new HashMap<EntityType, List>();

        for (EntityType nextEntityType : entitiesMatchingViews) {
            List nextResult = browse(nextEntityType, matchings, viewTypeName);

            // Check that it actually contains some matches before adding it to the results.
            if (!nextResult.isEmpty()) {
                results.put(nextEntityType, nextResult);
                LOG.fine("Got results for entity: " + nextEntityType + ".");
            }
        }

        return results;
    }

    /** {@inheritDoc} */
    public PagingResult executePagedQuery(int from, int number, String databaseEntityName, String entityTypeName,
        String viewTypeName, Criterion criterion, Map<String, Criterion> joins) {
        Session session = currentSession();

        // Project the id and external id properties and just the remaining properties that are required to project
        // the results onto the specified view type.
        ProjectionList properties = Projections.projectionList().add(Projections.id());

        ViewType viewType = catalogue.getViewType(viewTypeName);

        for (String fieldName : viewType.getAllPropertyTypes().keySet()) {
            properties.add(Property.forName(fieldName));
        }

        // Create the selection criteria for the block.
        Criteria selectCriteria = session.createCriteria(databaseEntityName);

        if (criterion != null) {
            selectCriteria.add(criterion);
        }

        if (joins != null) {
            for (Map.Entry<String, Criterion> entry : joins.entrySet()) {
                String joinEntity = entry.getKey();
                Criterion joinCriterion = entry.getValue();

                selectCriteria.createCriteria(joinEntity).add(joinCriterion);
            }
        }

        selectCriteria.setProjection(properties)
            .setFirstResult(from)
            .setMaxResults(number)
            .setResultTransformer(new ViewInstanceTransformer(viewType, entityTypeName));

        // Create the count criteria.
        Criteria countCriteria = session.createCriteria(databaseEntityName);

        if (criterion != null) {
            countCriteria.add(criterion);
        }

        if (joins != null) {
            for (Map.Entry<String, Criterion> entry : joins.entrySet()) {
                String joinEntity = entry.getKey();
                Criterion joinCriterion = entry.getValue();

                countCriteria.createCriteria(joinEntity).add(joinCriterion);
            }
        }

        countCriteria.setProjection(Projections.rowCount());

        // Run a query to find out how many results there will be and update the list size.
        int count = ((Long) countCriteria.uniqueResult()).intValue();

        // Execute the query to get the block.
        List results = selectCriteria.list();

        return new PagingResult(count, results);
    }

    public List executeViewProjectionQuery(String databaseEntityName, String entityTypeName, String viewTypeName,
        Criterion criterion, Map<String, Criterion> joins) {
        Session session = currentSession();

        ViewType viewType = catalogue.getViewType(viewTypeName);

        // Create the projection to the view plus the id.
        ProjectionList properties = projectToIdAndView(viewType);

        // Create the selection criteria for the block.
        Criteria selectCriteria = session.createCriteria(databaseEntityName);

        if (criterion != null) {
            selectCriteria.add(criterion);
        }

        if (joins != null) {
            for (Map.Entry<String, Criterion> entry : joins.entrySet()) {
                String joinEntity = entry.getKey();
                Criterion joinCriterion = entry.getValue();

                selectCriteria.createCriteria(joinEntity).add(joinCriterion);
            }
        }

        selectCriteria.setProjection(properties)
            .setResultTransformer(new ViewInstanceTransformer(viewType, entityTypeName));

        // Execute the query to get the block.
        List results = selectCriteria.list();

        return results;
    }

    public List executeEntityQuery(String databaseEntityName, String entityTypeName, Criterion criterion,
        Map<String, Criterion> joins) {
        Session session = currentSession();

        // Create the selection criteria for the block.
        Criteria selectCriteria = session.createCriteria(databaseEntityName);

        if (criterion != null) {
            selectCriteria.add(criterion);
        }

        if (joins != null) {
            for (Map.Entry<String, Criterion> entry : joins.entrySet()) {
                String joinEntity = entry.getKey();
                Criterion joinCriterion = entry.getValue();

                selectCriteria.createCriteria(joinEntity).add(joinCriterion);
            }
        }

        // Execute the query to get the block.
        List results = selectCriteria.list();

        return results;
    }

    /**
     * Creates a projection onto a view, and also include the id in the projection.
     *
     * @param  viewType The view to project onto.
     *
     * @return A projection onto a view plus the id.
     */
    private ProjectionList projectToIdAndView(ViewType viewType) {
        // Project the id property and the remaining properties that are required to project
        // the results onto the specified view type.
        ProjectionList properties = Projections.projectionList().add(Projections.id());

        for (String fieldName : viewType.getAllPropertyTypes().keySet()) {
            properties.add(Property.forName(fieldName));
        }

        return properties;
    }

    /**
     * Provides a listing by entity type of views of entities matching a set of named attributes. The attributes do not
     * have to be fully specified, range and wild-card attributes are accepted. Any attribute that is a member of an
     * entity, and is not specified at all in the query will match any value of that attribute in the returned entities.
     * A view type is passed to specify the view onto the entity that is to be returned by the browse operation,
     * allowing a subset of the entities fields to be retrieved in order to provide a summary of the available entities.
     * Only entities that conform to the specified view will be returned.
     *
     * @param  entityType   The type of entity to restrict the results to.
     * @param  matchings    The attributes to match.
     * @param  viewTypeName The name of the view type to match and return.
     *
     * @return A map from entity types to matching entities.
     */
    private List browse(EntityType entityType, Map<String, Attribute> matchings, String viewTypeName) {
        // Ensure that a view type has been specified.
        if (viewTypeName == null) {
            throw new IllegalArgumentException("The 'viewTypeName' parameter must not be null.");
        }

        ViewType viewType = catalogue.getViewType(viewTypeName);

        // Get the name of the entity table to fetch the matching entity from.
        String entityTableName = entityType.getBaseClassName();
        String entityTypeName = entityType.getName();

        // Check that the specified entity type contains attributes of the correct type to match the query.
        for (Map.Entry<String, Attribute> entry : matchings.entrySet()) {
            String propName = entry.getKey();
            Attribute attribute = entry.getValue();

            // Get the type name of the attribute in the parameter.
            String attributeTypeName = attribute.getType().getName();

            // Get the type name of the field in the entity.
            Type type = entityType.getPropertyType(propName);

            if (type != null) {
                String fieldTypeName = type.getName();

                // Check that they are compatible.
                if (!attributeTypeName.equals(fieldTypeName)) {
                    throw new IllegalArgumentException("The type of query parameter " + propName + " is " +
                        attributeTypeName + " which is not compatible with the field of type " + fieldTypeName +
                        " on entity type " + entityType.getName());
                }
            } else {
                throw new IllegalArgumentException("The query parameter " + propName +
                    " does not match any field name of entity type " + entityType.getName());
            }
        }

        // Get the hibernate query criterions for the requested attribute matchings.
        Map<String, Criterion> joins = getByAttributeCriterions(matchings);

        return executeViewProjectionQuery(entityTableName, entityTypeName, viewTypeName, null, joins);
    }

    /**
     * Build a map of entity field names and criterion to apply to them in order to select entities by the specified
     * attributes.
     *
     * @param  matchings The attributes to match.
     *
     * @return A map of entity field names and criterion to apply to them.
     */
    private Map<String, Criterion> getByAttributeCriterions(Map<String, Attribute> matchings) {
        Map<String, Criterion> criterions = new HashMap<String, Criterion>();

        // Loop over all the attribute matchings to create criterions for.
        for (Map.Entry<String, Attribute> entry : matchings.entrySet()) {
            String propName = entry.getKey();
            Attribute attribute = entry.getValue();

            // Get the attribute type name of the parameter to match against.
            String attributeTypeName = attribute.getType().getName();

            if (attribute instanceof HierarchyAttribute) {
                // Create join criteria for selecting entities by hierarchies.
                // One join criterion, plus entity name, will be created for each property to be restricted by hierarchy.
                HierarchyAttribute hierarchyAttribute = (HierarchyAttribute) attribute;
                HierarchyAttributeFactory factory = hierarchyAttribute.getFactory();

                // Get the next property to restrict by attribute value and create a criterion to restrict on that property.
                Conjunction subCriterion = Restrictions.conjunction();
                criterions.put(propName, subCriterion);

                // Get the level names in the hierarchy.
                String[] levelNames = factory.getLevelNames();

                // Build up the property matching clause for this restriction. Levels defined in the restricting
                // hierarchy must be matched exactly on the joined hierarchy property.
                for (String level : levelNames) {
                    String value = hierarchyAttribute.getValueAtLevel(level);

                    // Only add restrictions for non null values specified in the grouping hierarchy.
                    if (value != null) {
                        subCriterion.add(Restrictions.eq(attributeTypeName + "." + level, value));
                    }
                }
            }
        }

        return criterions;
    }

    /**
     * Filters a collection of entities down to those that have fields of matching name and type to a specified set of
     * fields.
     *
     * @param  entities  The set of entities to filter.
     * @param  matchings The set of field names and types to match.
     *
     * @return A collection of entities from the original set that match the specified fields.
     */
    private Collection<EntityType> filterEntitiesMatchingFields(Collection<EntityType> entities,
        Map<String, Attribute> matchings) {
        // Build a list of all entity types that contain the named attributes as field with matching name and type.
        List<EntityType> results = new ArrayList<EntityType>();

        for (EntityType entityType : entities) {
            // Loop over all the properties and their attributes. An entity must match all before it is added to
            // the query. Start by assuming that the entity does match.
            boolean match = true;

            for (Map.Entry<String, Attribute> entry : matchings.entrySet()) {
                String propName = entry.getKey();
                Attribute attribute = entry.getValue();

                // Get the attribute type name of the attribute in the parameter.
                String attributeTypeName = attribute.getType().getName();

                // Get the attribute type name of the attribute in the entity.
                Type type = entityType.getPropertyType(propName);

                if (type != null) {
                    String entityTypeName = type.getName();

                    // Check that they are compatible.
                    if (!attributeTypeName.equals(entityTypeName)) {
                        // The entity parameter type does not match the type of the query.
                        match = false;

                        break;
                    }
                } else {
                    // The entity does not have a field with name matching the attribute name in the query.
                    match = false;

                    break;
                }
            }

            // Check if the entity type matched the query and add it to the list if so.
            if (match) {
                results.add(entityType);
                LOG.fine("Matched entity type: " + entityType + ".");
            }
        }

        return results;
    }

    /**
     * Filters a collection of entities down to those that conform to a specified view type.
     *
     * @param  entities The set of entities to filter.
     * @param  view     The view type to filter to.
     *
     * @return A collection of entities from the original set that conform to the specified view type.
     */
    private Collection<EntityType> filterEntitiesMatchingViews(Collection<EntityType> entities, ViewType view) {
        // Build a list of all entity types that conform to the specified view type.
        List<EntityType> results = new ArrayList<EntityType>();

        for (EntityType entityType : entities) {
            Set<ComponentType> ancestors = entityType.getImmediateAncestors();

            if (ancestors.contains(view)) {
                results.add(entityType);
                LOG.fine("Matched entity type: " + entityType + ".");
            }
        }

        return results;
    }

    /**
     * ViewInstanceTransformer transforms results sets containing an Object array, consisting of an id, and the fields
     * that make up a particular view instance into a projection class implementing the specified view type. The
     * projection class is one that has some fields with setters that match some of the fields in the view. The
     * intersection of matching fields will be set on the projection class.
     */
    public static class ViewInstanceTransformer implements ResultTransformer {
        /** Holds the view type to project onto. */
        ViewType viewType;

        /** The name of the dimension that the summary belongs to. */
        String entityTypeName;

        /**
         * Creates a result transformer to transform tuples from result sets into view instances.
         *
         * @param viewType       The view type to project onto.
         * @param entityTypeName The name of the entity type that the view is of.
         */
        public ViewInstanceTransformer(ViewType viewType, String entityTypeName) {
            this.viewType = viewType;
            this.entityTypeName = entityTypeName;
        }

        /**
         * Returns the input list with no transformation applied to it.
         *
         * @param  collection The input list.
         *
         * @return The input list untouched.
         */
        public List transformList(List collection) {
            return collection;
        }

        /**
         * Transforms the object tuple, { id, external id, ... } into an object, where fields in the projection class
         * intersect with fields on the view.
         *
         * @param  tuple   The object tuple.
         * @param  aliases The alias names for the fields in the tuple. Ignored.
         *
         * @return An instance of ViewInstance.
         */
        public Object transformTuple(Object[] tuple, String[] aliases) {
            Class viewImplClass = viewType.getBaseClass();
            LOG.fine("viewImplClass = " + viewImplClass);

            String[] fieldNames = new String[tuple.length];
            fieldNames[0] = "id";

            int i = 1;

            for (Map.Entry<String, Type> entry : viewType.getAllPropertyTypes().entrySet()) {
                fieldNames[i] = entry.getKey();
                i++;
            }

            Object instance = ReflectionUtils.newInstance(viewImplClass);

            BeanMemento memento = new BeanMemento(instance);

            for (int j = 0; j < tuple.length; j++) {
                memento.put(viewImplClass, fieldNames[j], tuple[j]);
            }

            try {
                memento.restore(instance);
            } catch (NoSuchFieldException e) {
                // This exception is deliberately ignored, as the compensating action for not having a matching field
                // on the projection class, is to not include that field. The exception is set to null to indicate
                // that it has been deliberately ignored.
                e = null;
            }

            return instance;
        }
    }
}
