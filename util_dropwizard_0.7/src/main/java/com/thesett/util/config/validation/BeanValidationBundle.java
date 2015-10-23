package com.thesett.util.config.validation;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Sets up a Java Bean Validation ValidatorFactory from the constraints file specified in the DropWizard configuration.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Load a set of Java Bean Validation constraints. </td></tr>
 * <tr><td> Provide a ValidatorFactory. </td></tr>
 * </table></pre>
 */
public abstract class BeanValidationBundle<T extends Configuration> implements ConfiguredBundle<T>,
    ValidationConfiguration<T> {
    /** {@inheritDoc} */
    public void run(T configuration, Environment environment) {
        // No run actions required.
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Does nothing as no bootstrapping required.
     */
    public void initialize(Bootstrap<?> bootstrap) {
        // No bootstrapping required.
    }
}
