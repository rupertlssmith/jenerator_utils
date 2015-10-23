package com.thesett.util.audited;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import com.thesett.util.jersey.Audited;

/**
 * AuditedMethodDispatchProvider provides a request dispatcher that adds extra logging to methods annotated with
 * {@link com.thesett.util.jersey.Audited}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide a request dispatcher for Audited methods.  </td></tr>
 * </table></pre>
 */
public class AuditedMethodDispatchProvider implements ResourceMethodDispatchProvider {
    private final ResourceMethodDispatchProvider provider;
    private final boolean requireRemoteIPAddressInformation;

    /**
     * Creates a request dispatcher provider for extra logging.
     *
     * @param provider                          The method dispatcher.
     * @param requireRemoteIPAddressInformation <tt>true</tt> iff remote ip info should be logged too.
     */
    public AuditedMethodDispatchProvider(ResourceMethodDispatchProvider provider,
        boolean requireRemoteIPAddressInformation) {
        this.requireRemoteIPAddressInformation = requireRemoteIPAddressInformation;
        this.provider = Preconditions.checkNotNull(provider);
    }

    /** {@inheritDoc} */
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        final RequestDispatcher dispatcher = provider.create(abstractResourceMethod);
        final Audited audited = abstractResourceMethod.getAnnotation(Audited.class);

        if (audited != null) {
            return new AuditedMethodRequestDispatcher(dispatcher, requireRemoteIPAddressInformation);
        }

        return dispatcher;
    }
}
