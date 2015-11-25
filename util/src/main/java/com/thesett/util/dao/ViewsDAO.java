package com.thesett.util.dao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;

import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.transform.BasicTransformerAdapter;

import com.thesett.common.util.TypeConverter;
import com.thesett.util.function.Functions;
import com.thesett.util.function.ToStringFunction;
import com.thesett.util.model.Pair;

/**
 * ViewsDAO is a cut-down read-only DAO for querying views in a simple manner.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide a list of all views by name. </td></tr>
 * <tr><td> Provide the full contents of a named view. </td></tr>
 * </table></pre>
 */
@MappedSuperclass
@NamedNativeQueries(
    {
        @NamedNativeQuery(
            name = "com.thesett.util.dao.ViewsDAO.findAllWithPrefix", query =
                "SELECT table_name" +
                " FROM INFORMATION_SCHEMA.views" +
                " WHERE table_schema = ANY (current_schemas(false))" +
                " AND table_name LIKE :prefix"
        )
    }
)
public class ViewsDAO extends ViewsBaseDAO<Object[]> {
    /**
     * Creates the views DAO on top of the specified session factory.
     *
     * @param sessionFactory The Hibernate session factory to use.
     */
    public ViewsDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /** {@inheritDoc} */
    public List<String> findAllViewsPrefixedBy(String prefix) {
        List views =
            list(namedQuery("com.thesett.util.dao.ViewsDAO.findAllWithPrefix").setString("prefix", prefix + "%"));

        return Functions.map(views, new ToStringFunction());
    }

    /**
     * Lists the entire contents of a view.
     *
     * @param  name The name of the view.
     *
     * @return The entire contents of the view.
     */
    public List<Map<String, Object>> queryViewWithName(String name) {
        viewIsAccessable(name);

        return currentSession().createSQLQuery("SELECT * FROM " + name)
            .setResultTransformer(new MapWithAliasNamesTransformer())
            .list();
    }

    /**
     * Provides the set of the names of the fields that a view provides.
     *
     * @param  name The name of the view.
     *
     * @return The set of fields in the view.
     */
    public Set<String> queryViewFields(String name) {
        // Ensure that only the views exposed by this DAO can be accessed.
        viewIsAccessable(name);

        LinkedHashSet<String> result = new LinkedHashSet<>();
        result.addAll(currentSession().createSQLQuery(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = :name")
            .addScalar("column_name")
            .setString("name", name)
            .list());

        return result;
    }

    /**
     * Lists the contents of a view, filtered by exact matches against a subset of its fields.
     *
     * <p/>The matches are translated in a WHERE clause, and if more than one match is specified they are ANDed
     * together. This provides a simple mechanism to filter a view.
     *
     * <p/>String values are matched using LIKE '%value%', that is, the string only needs to substring match.
     *
     * @param  name         The name of the view.
     * @param  filterParams The fields and values to filter by.
     *
     * @return The contents of the filtered view.
     */
    public List<Map<String, Object>> queryViewWithNameAndFilter(String name, Map<String, Object> filterParams) {
        // Ensure that only the views exposed by this DAO can be accessed.
        viewIsAccessable(name);

        // Get the type meta data for the view, so that parameters can be mapped to the correct types in the query.
        Map<String, Class> typeMetaData = fetchViewFieldsMetaData(name);

        // Get the available set of fields that can be filtered against, and filter fields not in this set are to be
        // ignored.
        Set<String> allowedFields = queryViewFields(name);

        StringBuilder filterClauseBuilder = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : filterParams.entrySet()) {
            String fieldName = entry.getKey();

            if (allowedFields.contains(fieldName)) {
                filterClauseBuilder.append(first ? " WHERE " : " AND ").append(fieldName);

                Class type = typeMetaData.get(fieldName);

                if (String.class.equals(type)) {
                    filterClauseBuilder.append(" LIKE ");
                } else {
                    filterClauseBuilder.append(" = ");
                }

                filterClauseBuilder.append(":").append(fieldName);

                first = false;
            }
        }

        SQLQuery sqlQuery = currentSession().createSQLQuery("SELECT * FROM " + name + filterClauseBuilder.toString());

        for (Map.Entry<String, Object> entry : filterParams.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            if (allowedFields.contains(fieldName)) {
                TypeConverter.MultiTypeData multiTypeData = TypeConverter.getMultiTypeData(fieldValue);
                Class type = typeMetaData.get(fieldName);
                Object convertedValue = TypeConverter.convert(multiTypeData, type);

                if (String.class.equals(type)) {
                    convertedValue = "%" + convertedValue + "%";
                }

                if (convertedValue != null) {
                    sqlQuery.setParameter(fieldName, convertedValue);
                }
            }
        }

        return sqlQuery.setResultTransformer(new MapWithAliasNamesTransformer()).list();
    }

    /**
     * Ensures that the names view is exposed by this DAO and is allowed to be accessed.
     *
     * <p/>A check is made on the name of the view being requested, to ensure that it is a view exposed by this DAO.
     * This is done because a dynamic query is used to simply select all from a named relation, and SQL injection attack
     * could be used here to list the contents of any table, if an extra check is not made to restrict this to views
     * only. <b>This should be used on all methods that read from view.</b>
     *
     * @param name The name of the view to check.
     */
    protected void viewIsAccessable(String name) {
        // Ensure that only the views exposed by this DAO can be accessed.
        List<String> allowedViewNames = findAllViewsPrefixedBy("");

        if (!allowedViewNames.contains(name)) {
            throw new IllegalArgumentException("'name' is " + name +
                " which is not the name of a view that is legitimately accessable from this view DAO.");
        }
    }

    /**
     * Queries meta-data on the view to figure out what type its fields are.
     *
     * @param  name The name of the view to check.
     *
     * @return
     */
    private Map<String, Class> fetchViewFieldsMetaData(String name) {
        Map<String, Class> result = new HashMap<>();

        List<Pair<String, Class>> typeMetaData =
            currentSession().createSQLQuery(
                    "SELECT column_name, udt_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = :name")
            .addScalar("column_name")
            .addScalar("udt_name")
            .setString("name", name)
            .setResultTransformer(new ColumnMetaDataTransformer())
            .list();

        for (Pair<String, Class> pair : typeMetaData) {
            result.put(pair.getFirst(), pair.getSecond());
        }

        return result;
    }

    /**
     * MapWithAliasNamesTransformer transforms a result set, into a hash set where the key names are the alias names
     * from the results, and the values are the data values from the results. This provides a simple mechanism to dump a
     * table as a map.
     */
    private static class MapWithAliasNamesTransformer extends BasicTransformerAdapter {
        /**
         * {@inheritDoc}
         *
         * <p/>Transforms table rows into (alias, data) items in a map.
         *
         * @return A map containing (alias, data) items.
         */
        public Object transformTuple(Object[] tuple, String[] aliases) {
            HashMap<String, Object> result = new HashMap<>();

            for (int i = 0; i < tuple.length; i++) {
                result.put(aliases[i], tuple[i]);
            }

            return result;
        }
    }

    /**
     * ColumnMetaDataTransformer transforms a result set consisting of column names and column types, into &lt;String,
     * Class&gt; pairs, describing the corresponding Java types that best match the columns.
     */
    private static class ColumnMetaDataTransformer extends BasicTransformerAdapter {
        /** {@inheritDoc} */
        public Object transformTuple(Object[] tuple, String[] aliases) {
            Pair<String, Class> result;

            String typeTag = (String) tuple[1];

            switch (typeTag) {
            case "int4":
                result = new Pair<String, Class>((String) tuple[0], int.class);
                break;

            case "int8":
                result = new Pair<String, Class>((String) tuple[0], long.class);
                break;

            case "numeric":
                result = new Pair<String, Class>((String) tuple[0], BigDecimal.class);
                break;

            case "float8":
                result = new Pair<String, Class>((String) tuple[0], float.class);
                break;

            case "text":
                result = new Pair<String, Class>((String) tuple[0], String.class);
                break;

            default:
                result = new Pair<String, Class>((String) tuple[0], String.class);
                break;
            }

            return result;
        }
    }
}
