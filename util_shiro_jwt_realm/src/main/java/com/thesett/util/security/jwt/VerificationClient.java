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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.thesett.util.client.WebExceptionCodeClientProxy;
import com.thesett.util.json.JodaTimeObjectMapperProvider;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

/**
 * VerificationClient implements a proxied HTTP client to the {@link VerificationService}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide verification keys. </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class VerificationClient implements VerificationService
{
    /** The base URL to access the services through. */
    private final String baseURL;

    /** A client configuration to use for all services. */
    private final ClientConfig clientConfig;

    /**
     * Creates an instance of the client factory.
     *
     * @param baseURL The base URL to access the api through. This should include the full path to the root of the api
     *                resources, for example, "http://localhost:8080/api".
     */
    public VerificationClient(String baseURL)
    {
        this.baseURL = baseURL;

        // Set things up to use Jackson JSON.
        clientConfig = new ClientConfig();
        clientConfig.register(JacksonJsonProvider.class);
        clientConfig.register(JodaTimeObjectMapperProvider.class);
    }

    /** {@inheritDoc} */
    public Verifier retrieve()
    {
        return getVerificationService().retrieve();
    }

    /**
     * Creates a proxied client using the supplied interface.
     *
     * @param  resourceInterface The interface to proxy.
     * @param  <T>               The type of the service being proxied.
     *
     * @return A proxied client service.
     */
    private <T> T createClientProxy(Class<T> resourceInterface)
    {
        Client client = ClientBuilder.newClient(clientConfig);
        WebTarget target = client.target(baseURL);

        T clientProxy = WebResourceFactory.newResource(resourceInterface, target);

        return WebExceptionCodeClientProxy.proxy(clientProxy, resourceInterface);
    }

    /**
     * Creates a proxied client for the verification service.
     *
     * @return A proxied client for the verification service.
     */
    private VerificationService getVerificationService()
    {
        Class<VerificationService> resourceInterface = VerificationService.class;

        return createClientProxy(resourceInterface);
    }
}
