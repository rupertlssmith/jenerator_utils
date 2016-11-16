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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Verifier encapsulates the verificiaton key and algorithm needed to verify the JWT tokens.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Encapsulate token verification details.
 * </table></pre>
 *
 * @author Rupert Smith
 */
@JsonIgnoreProperties(ignoreUnknown = true, value = { "componentType" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Verifier implements Serializable
{
    /** Holds the alg property. */
    protected String alg;

    /** Holds the key property. */
    protected String key;

    /** No-arg constructor for serialization. */
    public Verifier()
    {
        // No-arg constructor for serialization.
    }

    /**
     * Accepts a new value for the alg property.
     *
     * @param  alg The alg property.
     *
     * @return 'this' (so that fluents can be chained methods).
     */
    public Verifier withAlg(String alg)
    {
        this.alg = alg;

        return this;
    }

    /**
     * Accepts a new value for the key property.
     *
     * @param  key The key property.
     *
     * @return 'this' (so that fluents can be chained methods).
     */
    public Verifier withKey(String key)
    {
        this.key = key;

        return this;
    }

    /**
     * Provides the alg property.
     *
     * @return The alg property.
     */
    public String getAlg()
    {
        return alg;
    }

    /**
     * Provides the key property.
     *
     * @return The key property.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Accepts a new value for the alg property.
     *
     * @param alg The alg property.
     */
    public void setAlg(String alg)
    {
        this.alg = alg;
    }

    /**
     * Accepts a new value for the key property.
     *
     * @param key The key property.
     */
    public void setKey(String key)
    {
        this.key = key;
    }
}
