package com.thesett.util.json;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JodaTimeObjectMapperProvider provides an object mapper that can handle Joda time.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide an object mapper for Joda time. </td></tr>
 * </table></pre>
 */
@Provider
public class JodaTimeObjectMapperProvider implements ContextResolver<ObjectMapper> {
    /** {@inheritDoc} */
    public ObjectMapper getContext(Class<?> type) {
        final ObjectMapper result = new ObjectMapper();
        result.registerModule(new JodaTimeModule());

        return result;
    }
}
