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
package com.thesett.util.jackson;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JacksonUtils provides some helper methods for working with Jackson.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Parse an HTTP entity as json.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class JacksonUtils
{
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MapTypeReference MAP_TYPE_REF = new MapTypeReference();

    /**
     * Parses the entity in an HTTP response as json.
     *
     * @param  response The HTTP response.
     *
     * @return The parsed entity as a map.
     */
    public static Map<String, Object> getResponseEntity(Response response)
    {
        try
        {
            return MAPPER.readValue(response.readEntity(String.class), MAP_TYPE_REF);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static class MapTypeReference extends TypeReference<Map<String, Object>>
    {
    }
}
