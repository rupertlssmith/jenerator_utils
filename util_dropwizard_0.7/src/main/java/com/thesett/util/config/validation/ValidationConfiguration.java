package com.thesett.util.config.validation;

import javax.validation.ValidatorFactory;

import io.dropwizard.Configuration;

/**
 * ValidationConfiguration defines a method to extract a Bean Validator Factory from a DropWizard configuration.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Extract a bean validator factory from a DropWizard configuration. </td></tr>
 * </table></pre>
 */
public interface ValidationConfiguration<T extends Configuration> {
    /**
     * Extracts a bean validator factory from the supplied configuration.
     *
     * @param  configuration The configuration.
     *
     * @return A bean validator factory.
     */
    ValidatorFactory getValidatorFactory(T configuration);
}
