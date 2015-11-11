package com.thesett.util.config.refdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.apache.commons.dbcp2.BasicDataSource;
import com.thesett.util.commands.refdata.RefDataBundle;
import com.thesett.util.commands.refdata.RefDataLoadConfiguration;
import com.thesett.util.commands.refdata.RefDataLoadException;
import com.thesett.util.commands.refdata.RefDataUtils;
import com.thesett.util.config.ConfigurationUtils;
import com.thesett.util.jdbc.JDBCUtils;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;

/**
 * RefDataSetupBundle performs checks on the reference data at application start up time.
 *
 * <p/>'Reference types' are types that specify a restricted set of values that are enumerated in a database. For
 * example enumerations such as { male; female } or hierarchical labellings and so on are called reference types.
 *
 * <p/>The start-up configuration consists of checking that the reference types have been created and populated in the
 * database. The data is loaded into memory for each type, to cache the type in memory and finalize it.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Verify or populate the database reference types.
 * </table></pre>
 */
public abstract class RefDataSetupBundle<T extends Configuration> implements ConfiguredBundle<T>,
    RefDataLoadConfiguration<T> {
    /** Holds a list of all of the reference data types. */
    private final List<String> refdataTypes = new LinkedList<>();

    /**
     * {@inheritDoc}
     *
     * <p/>Sets up the knowledge level catalogue model. Ensures that all reference data is loaded from the database and
     * cached in memory.
     *
     * @throws RefDataLoadException If the reference data files cannot be loaded.
     */
    public void run(T config, Environment environment) throws RefDataLoadException {
        initializeRefdataTypes(config);

        // Cache all reference data from the database.
        initializeReferenceData(config);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Does nothing as no bootstrapping required.
     */
    public void initialize(Bootstrap<?> bootstrap) {
        // No bootstrapping required.
    }

    /**
     * Supplies a list of reference data types.
     *
     * @return A list of reference data types.
     */
    public List<String> getRefdataTypes() {
        return Collections.unmodifiableList(refdataTypes);
    }

    private void initializeRefdataTypes(T config) throws RefDataLoadException {
        String refdataPackage = getRefdataPackage(config);
        Set<RefDataBundle> refDataBundles = RefDataUtils.getRefDataBundles(refdataPackage);

        for (RefDataBundle refDataBundle : refDataBundles) {
            refdataTypes.add(refDataBundle.getTypeName());
        }
    }

    /**
     * Loads all reference data tables, and initializes their corresponding types in the model with the contents.
     *
     * <p/>It is possible that prior to this method being called, some reference data items may already have been
     * created (by constructors and so on). For this reason, the enumerated attribute class is not dropped and re-built
     * from scratch, or existing items will be invalid. Instead any items not already in the attribute class are added,
     * and the ids on all items whether already existing or new, are correctly set.
     *
     * @param config The DropWizard configuration, to get the database connection settings from.
     */
    private void initializeReferenceData(T config) {
        BasicDataSource ds = ConfigurationUtils.getBasicDataSource(getDataSourceFactory(config));

        Connection connection = null;
        PreparedStatement sql = null;

        try {
            connection = ds.getConnection();

            // Collection<EnumeratedStringAttribute.EnumeratedStringType> enumTypes = model.getAllEnumTypes();

            for (String enumTypeName : refdataTypes) {
                String tableName = enumTypeName.toLowerCase() + "_" + "enumeration";
                EnumeratedStringAttribute.EnumeratedStringAttributeFactory factory =
                    EnumeratedStringAttribute.getFactoryForClass(enumTypeName);

                // Get the set of already existing values, so that they will not be created again.
                factory.getType().getAllPossibleValuesIterator(false);

                Map<String, EnumeratedStringAttribute> existingValues =
                    factory.getType().getAllPossibleValuesMap(false);

                // Ensure the attribute class is being re-built.
                factory = EnumeratedStringAttribute.getFactoryForClass(enumTypeName);

                sql = connection.prepareStatement("SELECT * FROM " + tableName);

                ResultSet resultSet = sql.executeQuery();

                while (resultSet.next()) {
                    long id = resultSet.getLong(1);
                    String value = resultSet.getString(2);

                    EnumeratedStringAttribute attribute = null;

                    if (!existingValues.containsKey(value)) {
                        attribute = factory.createStringAttribute(value);
                    } else {
                        attribute = existingValues.get(value);
                    }

                    attribute.setId(id);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            JDBCUtils.closeConnection(connection, sql);
        }
    }
}
