package com.thesett.util.config.shiro;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.server.session.SessionHandler;

/**
 * ShiroBundle is a DropWizard configuration bundle for Apache Shiro. It attaches a servlet listener to the Web
 * container to initialize Shiro with the servlet context. A URL pattern is taken from the {@link ShiroConfiguration} to
 * control the set of URLs that are protected by a Shiro realm.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Set up Shiro authentication in the Web layer. </td></tr>
 * </table></pre>
 *
 * @param <T> The type of the application configuration.
 */
public abstract class ShiroBundle<T extends Configuration> implements ConfiguredBundle<T> {
    /**
     * Implement to extract the Shiro configuration from the application configuration.
     *
     * @param  configuration The application configuration.
     *
     * @return The Shiro configuration.
     */
    public abstract ShiroConfiguration getShiroConfiguration(T configuration);

    /** {@inheritDoc} */
    public void initialize(Bootstrap<?> bootstrap) {
    }

    /** {@inheritDoc} */
    public void run(final T configuration, Environment environment) {
        final ShiroConfiguration shiroConfig = getShiroConfiguration(configuration);

        initializeShiro(shiroConfig, environment);
    }

    /**
     * Sets up Shiro on the servlet context lifecycle, and establishes a URL pattern to protect.
     *
     * @param config      The Shiro configuration.
     * @param environment The DropWizard environment.
     */
    private void initializeShiro(ShiroConfiguration config, Environment environment) {
        if (config.isEnabled()) {
            if (config.isDropwizardSessionHandler()) {
                environment.servlets().setSessionHandler(new SessionHandler());
            }

            environment.servlets().addServletListeners(new EnvironmentLoaderListener());

            final String filterUrlPattern = config.getSecuredUrlPattern();
            environment.servlets()
                .addFilter("shiro-filter", new ShiroFilter())
                .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, filterUrlPattern);
        }
    }
}
