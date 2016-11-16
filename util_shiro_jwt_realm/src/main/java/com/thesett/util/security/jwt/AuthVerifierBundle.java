/*
 * Copyright The Sett Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesett.util.security.jwt;

import com.thesett.common.throttle.SleepThrottle;
import com.thesett.common.throttle.Throttle;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * AuthVerifierBundle is a DropWizard bundle that will attempt to obtain the token verification details from an auth
 * service at application startup. It can be configured to retry this operation at a particular rate, and to timeout if
 * it fails to obtain the details for a particular period.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Obtain auth token verification details on application startup.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public abstract class AuthVerifierBundle<T extends Configuration> implements ConfiguredBundle<T>
{
    /** Holds the auth verification key details. */
    private Verifier verifier;

    /** {@inheritDoc} */
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    /** {@inheritDoc} */
    public void run(T config, Environment environment) throws Exception
    {
        AuthVerifierConfiguration authVerifierConfiguration = getAuthVerifierConfiguration(config);
        String authServiceUrl = authVerifierConfiguration.getAuthServiceUrl();
        float delay = authVerifierConfiguration.getRetryDelayMillis();
        int timeout = authVerifierConfiguration.getTimeoutSeconds();

        // Create a throttle to control the retry rate.
        Throttle throttle = new SleepThrottle();
        throttle.setRate(1000f / delay);

        // Build a client to query the auth server with.
        VerificationClient verificationClient = new VerificationClient(authServiceUrl);

        System.out.println("Trying to get auth verification keys from: " + authServiceUrl);

        verifier = verificationClient.retrieve();
    }

    /**
     * Provides the auth verification key details.
     *
     * @return The auth verification key details.
     */
    public Verifier getVerifier()
    {
        return verifier;
    }

    /**
     * Should be implemented to extract the infinispan config from whatever configuration the application using this
     * bundle is using.
     *
     * @param  config The application configuration.
     *
     * @return The auth verifier configuration.
     */
    protected abstract AuthVerifierConfiguration getAuthVerifierConfiguration(T config);
}
