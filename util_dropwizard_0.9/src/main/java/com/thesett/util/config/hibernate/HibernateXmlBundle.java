package com.thesett.util.config.hibernate;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.SessionFactoryHealthCheck;
import io.dropwizard.hibernate.UnitOfWorkApplicationListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

import org.hibernate.SessionFactory;

import com.thesett.util.transaction.UnitOfWorkWithDetachApplicationListener;

/**
 * HibernateXmlBundle is a Hibernate resource bundle that uses a Hibernate XML configuration file to build the Hibernate
 * session factory, and not annotated Java classes.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Register the Hibernate module on bootstrap. </td></tr>
 * <tr><td> Build a Hibernate session factory from XML configuration. </td></tr>
 * </table></pre>
 */
public abstract class HibernateXmlBundle<T extends Configuration> implements ConfiguredBundle<T>,
    DatabaseConfiguration<T> {
    /** The default name of the hibernate bundle. */
    public static final String DEFAULT_NAME = "hibernate";

    /** The Hibernate session factory built from the configuration. */
    private SessionFactory sessionFactory;

    /** The session factory builder that sets up from a Hibernate XML mapping. */
    private final XmlSessionFactoryFactory sessionFactoryFactory = new XmlSessionFactoryFactory();

    /** The name of the resource on the classpath to load the Hiberate XML config from. */
    private final String hibernateXmlResourceName;

    /**
     * Creates a Hibernate config bundle that uses an XML configuration file, instead of annotated Java classes.
     *
     * @param hibernateXmlResourceName The name of the resource on the classpath to load the Hibernate XML config from.
     */
    protected HibernateXmlBundle(String hibernateXmlResourceName) {
        this.hibernateXmlResourceName = hibernateXmlResourceName;
    }

    /** {@inheritDoc} */
    public final void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(new Hibernate4Module());
    }

    /** {@inheritDoc} */
    public final void run(T configuration, Environment environment) throws ClassNotFoundException {
        final PooledDataSourceFactory dbConfig = getDataSourceFactory(configuration);
        this.sessionFactory = sessionFactoryFactory.build(this, environment, dbConfig, hibernateXmlResourceName);

        // Register the annotations.
        registerUnitOfWorkWithDetachListerIfAbsent(environment).registerSessionFactory(DEFAULT_NAME, sessionFactory);
        registerUnitOfWorkListerIfAbsent(environment).registerSessionFactory(DEFAULT_NAME, sessionFactory);

        environment.healthChecks()
            .register(DEFAULT_NAME,
                new SessionFactoryHealthCheck(environment.getHealthCheckExecutorService(),
                    dbConfig.getValidationQueryTimeout().or(Duration.seconds(5)), sessionFactory,
                    dbConfig.getValidationQuery()));
    }

    /**
     * Provides the configured Hibernate session factory.
     *
     * @return The configured Hibernate session factory.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Call-back from the {@link XmlSessionFactoryFactory} to provide an opportunity to adjust the Hibernate
     * configuration.
     *
     * @param configuration The Hibernate configuration.
     */
    protected void configure(org.hibernate.cfg.Configuration configuration) {
    }

    private UnitOfWorkWithDetachApplicationListener registerUnitOfWorkWithDetachListerIfAbsent(
        Environment environment) {
        for (Object singleton : environment.jersey().getResourceConfig().getSingletons()) {
            if (singleton instanceof UnitOfWorkApplicationListener) {
                return (UnitOfWorkWithDetachApplicationListener) singleton;
            }
        }

        final UnitOfWorkWithDetachApplicationListener listener = new UnitOfWorkWithDetachApplicationListener();
        environment.jersey().register(listener);

        return listener;
    }

    private UnitOfWorkApplicationListener registerUnitOfWorkListerIfAbsent(Environment environment) {
        for (Object singleton : environment.jersey().getResourceConfig().getSingletons()) {
            if (singleton instanceof UnitOfWorkApplicationListener) {
                return (UnitOfWorkApplicationListener) singleton;
            }
        }

        final UnitOfWorkApplicationListener listener = new UnitOfWorkApplicationListener();
        environment.jersey().register(listener);

        return listener;
    }
}
