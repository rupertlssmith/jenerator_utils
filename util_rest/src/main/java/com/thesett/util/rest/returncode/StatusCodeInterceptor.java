package com.thesett.util.rest.returncode;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * StatusCodeInterceptor is a response filter for the {@link StatusCode} annotation. It will replace a success response
 * code (in the 200 range), with one specified on the annotation.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Replace the default HTTP successful response code. </td></tr>
 * </table></pre>
 */
@StatusCode(code = 0)
@Provider
public class StatusCodeInterceptor implements ContainerResponseFilter {
    /** Used to obtain a reference to the annotation. */
    @Context
    private ResourceInfo resourceInfo;

    /**
     * {@inheritDoc}
     *
     * <p/>Replaces response codes in the 200 range, with the value set on the {@link StatusCode} annotation.
     */
    public void filter(ContainerRequestContext containerRequestContext,
        ContainerResponseContext containerResponseContext) throws IOException {
        Annotation[] annotations = resourceInfo.getResourceMethod().getAnnotations();

        StatusCode statusCodeOverride = null;

        for (Annotation annotation : annotations) {
            if (annotation instanceof StatusCode) {
                statusCodeOverride = (StatusCode) annotation;
            }
        }

        if (statusCodeOverride != null) {
            int statusCode = containerResponseContext.getStatus();

            // Only override in the 200 range.
            if (statusCode >= 200 && statusCode <= 299) {
                containerResponseContext.setStatus(statusCodeOverride.code());
            }
        }
    }
}
