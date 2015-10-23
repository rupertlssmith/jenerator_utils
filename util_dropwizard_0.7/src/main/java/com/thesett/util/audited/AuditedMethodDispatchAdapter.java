package com.thesett.util.audited;

import javax.ws.rs.ext.Provider;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;

/**
 * AuditedMethodDispatchAdapter sets up a {@link AuditedMethodDispatchProvider} to log to "HTTP.request" on annotated
 * methods.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Adapt annotated methods to add extra logging.  </td></tr>
 * </table></pre>
 */
@Provider
public class AuditedMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    /** {@inheritDoc} */
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new AuditedMethodDispatchProvider(provider, false);
    }
}
