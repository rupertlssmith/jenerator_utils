package com.thesett.util.config.model;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.apache.commons.dbcp2.BasicDataSource;
import com.thesett.util.config.ConfigurationUtils;
import com.thesett.util.jdbc.JDBCUtils;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.aima.logic.fol.Clause;
import com.thesett.aima.logic.fol.interpreter.ResolutionEngine;
import com.thesett.aima.logic.fol.prolog.PrologCompiledClause;
import com.thesett.aima.logic.fol.prolog.PrologEngine;
import com.thesett.catalogue.core.CatalogueModelFactory;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.setup.CatalogueDefinition;
import com.thesett.common.parsing.SourceCodeException;

/**
 * ModelSetupBundle performs application start-up time configurations to prepare a catalogue model for use. It loads and
 * validates the in-memory knowledge level model of the catalogue.
 *
 * <p/>'Reference types' are types that specify a restricted set of values that are enumerated in a database. For
 * example enumerations such as { male; female } or hierarchical labellings and so on are called reference types.
 *
 * <p/>The start-up configuration consists of checking that the reference types have been created and populated in the
 * database. The data is loaded into memory for each type, to cache the type in memory and finalize it.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Transform the raw catalogue model into the knowledge level catalogue model.
 * <tr><td> Verify or populate the database reference types.
 * </table></pre>
 */
public abstract class ModelSetupBundle<T extends Configuration> implements ConfiguredBundle<T>, ModelConfiguration<T> {
    /** Holds the catalogue as a first order logic model. */
    private static Catalogue model;

    /**
     * Un-marshals the model definition and runs it through the type checking and transformation process to obtain the
     * prepared knowledge level catalogue.
     *
     * @param  modelResource The name of the resource to load the catalogue model from.
     *
     * @return The catalogue.
     */
    public static Catalogue getCatalogue(String modelResource) {
        if (model != null) {
            return model;
        }

        try {
            // Open the specified resource and un-marshal the catalogue model from it.
            JAXBContext jc = JAXBContext.newInstance("com.thesett.catalogue.setup");
            Unmarshaller u = jc.createUnmarshaller();

            InputStream resource = ModelSetupBundle.class.getClassLoader().getResourceAsStream(modelResource);

            if (resource == null) {
                throw new IllegalStateException("The resource 'modelResource' could not be found on the classpath.");
            }

            CatalogueDefinition catalogueDefinition = (CatalogueDefinition) u.unmarshal(resource);

            // Create a first order logic resolution engine to perform the type checking with.
            ResolutionEngine<Clause, PrologCompiledClause, PrologCompiledClause> engine = new PrologEngine();
            engine.reset();

            // Create the catalogue logical model from the raw model and run its type checker.
            CatalogueModelFactory modelFactory = new CatalogueModelFactory(engine, catalogueDefinition, null);
            model = modelFactory.initializeModel();

            return model;
        } catch (JAXBException e) {
            throw new IllegalStateException("The configuration cannot be unmarshalled from " + modelResource + ".", e);
        } catch (SourceCodeException e) {
            throw new IllegalStateException("Got a SourceCodeException whilst creating the catalogue model.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Sets up the knowledge level catalogue model. Ensures that all reference data is loaded from the database and
     * cached in memory.
     */
    public void run(T config, Environment environment) {
        // Create the catalogue logical model from the raw model.
        model = getCatalogue(getModelResource(config));

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
     * Provides the runtime data model, available once this bundle has been run.
     *
     * @return The runtime data model.
     */
    public Catalogue getModel() {
        return model;
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

            Collection<EnumeratedStringAttribute.EnumeratedStringType> enumTypes = model.getAllEnumTypes();

            for (EnumeratedStringAttribute.EnumeratedStringType enumType : enumTypes) {
                String tableName = enumType.getName().toLowerCase() + "_" + "enumeration";
                EnumeratedStringAttribute.EnumeratedStringAttributeFactory factory =
                    EnumeratedStringAttribute.getFactoryForClass(enumType.getName());

                // Get the set of already existing values, so that they will not be created again.
                factory.getType().getAllPossibleValuesIterator(false);

                Map<String, EnumeratedStringAttribute> existingValues =
                    factory.getType().getAllPossibleValuesMap(false);

                // Ensure the attribute class is being re-built.
                factory = EnumeratedStringAttribute.getFactoryForClass(enumType.getName());

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
