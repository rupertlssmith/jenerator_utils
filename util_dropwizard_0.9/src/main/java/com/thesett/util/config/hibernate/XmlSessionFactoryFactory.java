package com.thesett.util.config.hibernate;

import java.util.Map;

import javax.sql.DataSource;

import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.SessionFactoryManager;
import io.dropwizard.setup.Environment;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class XmlSessionFactoryFactory {
    /**
     * Builds a Hibernate session factory from a {@link HibernateXmlBundle} configuration bundle.
     *
     * @param  bundle                   The Hibernate XML configuration bundle.
     * @param  environment              The Drop Wizard environment.
     * @param  PooledDataSourceFactory  A Drop Wizard data source factory.
     * @param  hibernateXmlResourceName The name of the resource on the classpath to load the Hiberate XML config from.
     *
     * @return A Hibernate session factory.
     *
     * @throws ClassNotFoundException If some class cannot be found, such as a JDBC driver.
     */
    public SessionFactory build(HibernateXmlBundle<?> bundle, Environment environment,
        PooledDataSourceFactory PooledDataSourceFactory, String hibernateXmlResourceName)
        throws ClassNotFoundException {
        ManagedDataSource dataSource = PooledDataSourceFactory.build(environment.metrics(), "hibernate");

        return build(bundle, environment, PooledDataSourceFactory, dataSource, hibernateXmlResourceName);
    }

    /**
     * Builds a Hibernate session factory from a {@link HibernateXmlBundle} configuration bundle.
     *
     * @param  bundle                   The Hibernate XML configuration bundle.
     * @param  environment              The Drop Wizard environment.
     * @param  PooledDataSourceFactory  A Drop Wizard data source factory.
     * @param  dataSource               A managed data source built from the data source factory.
     * @param  hibernateXmlResourceName The name of the resource on the classpath to load the Hiberate XML config from.
     *
     * @return A Hibernate session factory.
     *
     * @throws ClassNotFoundException If some class cannot be found, such as a JDBC driver.
     */
    public SessionFactory build(HibernateXmlBundle<?> bundle, Environment environment,
        PooledDataSourceFactory PooledDataSourceFactory, ManagedDataSource dataSource, String hibernateXmlResourceName)
        throws ClassNotFoundException {
        ConnectionProvider provider = buildConnectionProvider(dataSource, PooledDataSourceFactory.getProperties());
        SessionFactory factory =
            buildSessionFactory(bundle, PooledDataSourceFactory, provider, PooledDataSourceFactory.getProperties(),
                hibernateXmlResourceName);
        SessionFactoryManager managedFactory = new SessionFactoryManager(factory, dataSource);

        environment.lifecycle().manage(managedFactory);

        return factory;
    }

    /**
     * Creates a {@link ConnectionProvider} from a data source.
     *
     * @param  dataSource The data source to create a connection provider from.
     * @param  properties Additional configuration properties.
     *
     * @return A connection provider for the data source.
     */
    private ConnectionProvider buildConnectionProvider(DataSource dataSource, Map<String, String> properties) {
        DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
        connectionProvider.setDataSource(dataSource);
        connectionProvider.configure(properties);

        return connectionProvider;
    }

    /**
     * Builds a Hibernate session factory from a {@link HibernateXmlBundle} configuration bundle.
     *
     * @param  bundle                   The Hibernate XML configuration bundle.
     * @param  PooledDataSourceFactory  A Drop Wizard data source factory.
     * @param  connectionProvider       A Drop Wizard connection provider.
     * @param  properties               Additional data source configuration properties.
     * @param  hibernateXmlResourceName The name of the resource on the classpath to load the Hiberate XML config from.
     *
     * @return A Hibernate session factory.
     */
    private SessionFactory buildSessionFactory(HibernateXmlBundle<?> bundle,
        PooledDataSourceFactory PooledDataSourceFactory, ConnectionProvider connectionProvider,
        Map<String, String> properties, String hibernateXmlResourceName) {
        Configuration configuration = new Configuration();

        // Set up some configuration properties for Hibernate.
        configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
        configuration.setProperty(AvailableSettings.USE_SQL_COMMENTS,
            Boolean.toString(PooledDataSourceFactory.isAutoCommentsEnabled()));
        configuration.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
        configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, "false");
        configuration.setProperty(AvailableSettings.SHOW_SQL, "true");
        configuration.setProperty(AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");
        configuration.setProperty(AvailableSettings.ORDER_UPDATES, "true");
        configuration.setProperty(AvailableSettings.ORDER_INSERTS, "true");
        configuration.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        configuration.setProperty("jadira.usertype.autoRegisterUserTypes", "true");

        // Apply the configuration properties from the data source to the hibernate configuration, it needs
        // this to know where to get its data source from.
        for (Map.Entry<String, String> property : properties.entrySet()) {
            configuration.setProperty(property.getKey(), property.getValue());
        }

        // Call back onto the HibernateXMLBundle to allow it to adjust the configuration as needed.
        bundle.configure(configuration);

        configuration.addResource(hibernateXmlResourceName);

        // Add the connection provider to the Hibernate service registry, so it knows where to find it when it needs
        // to create connections.
        final ServiceRegistry registry =
            new StandardServiceRegistryBuilder().addService(ConnectionProvider.class, connectionProvider)
            .applySettings(properties)
            .build();

        return configuration.buildSessionFactory(registry);
    }
}
