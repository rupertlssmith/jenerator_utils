package com.thesett.util.audited;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

/**
 * AuditedMethodRequestDispatcher adds audit logging to request methods.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Add audit logging to reqest methods.  </td></tr>
 * </table></pre>
 */
public class AuditedMethodRequestDispatcher implements RequestDispatcher {
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(AuditedMethodRequestDispatcher.class.getName());

    private static final Set<String> REDACTED_HEADERS = ImmutableSet.of(HttpHeaders.AUTHORIZATION);

    private final RequestDispatcher dispatcher;

    private final boolean requireRemoteIPAddressInformation;

    /**
     * @param dispatcher                        The chained request dispatcher to forward onto.
     * @param requireRemoteIPAddressInformation <tt>true</tt> iff remote ip info should be logged too.
     */
    public AuditedMethodRequestDispatcher(RequestDispatcher dispatcher, boolean requireRemoteIPAddressInformation) {
        this.requireRemoteIPAddressInformation = requireRemoteIPAddressInformation;
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Performs extra logging, then forward onto the chained dispatcher to process the request.
     */
    public void dispatch(Object resource, HttpContext context) {
        logRequest(resource, context);
        dispatcher.dispatch(resource, context);
    }

    /**
     * Logs a request method in extra detail.
     *
     * @param resource The resource requested.
     * @param context  The HTTP context, providing the request.
     */
    private void logRequest(Object resource, HttpContext context) {
        StringBuilder builder = new StringBuilder();

        HttpRequestContext request = context.getRequest();

        builder.append("\n Audited Resource Access \n");
        builder.append("  Resource : " + resource.getClass() + "\n");

        if (requireRemoteIPAddressInformation &&
                !request.getRequestHeaders().keySet().contains(HttpHeaders.X_FORWARDED_FOR)) {
            throw new RuntimeException("Header " + HttpHeaders.X_FORWARDED_FOR +
                " is required but was not found in the request");
        }

        for (Map.Entry<String, List<String>> entry : request.getRequestHeaders().entrySet()) {
            if (!REDACTED_HEADERS.contains(entry.getKey())) {
                builder.append("  Header   : " + entry.getKey() + " = " + entry.getValue() + "\n");
            }
        }

        builder.append("  Method   : " + request.getMethod() + "\n");
        builder.append("  URI      : " + request.getRequestUri() + "\n");

        for (Map.Entry<String, List<String>> entry : request.getQueryParameters(true).entrySet()) {
            final String name = entry.getKey();
            final List<String> value = entry.getValue();
            builder.append("  Param    : " + name + " = " + value + " \n");
        }

        LOG.info(builder.toString());
    }
}
