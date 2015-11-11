package com.thesett.util.errors;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import com.thesett.util.entity.EntityValidationException;

/**
 * StarsExceptionMapper maps unhandled runtime exceptions to appropriate HTTP responses.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Map Hibernate constraint violations to validation errors. </td></tr>
 * <tr><td> Map Jersey not found to an HTTP No Content. </td></tr>
 * </table></pre>
 */
public class StandardExceptionMapper implements ExceptionMapper<Exception> {
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(StandardExceptionMapper.class.getName());

    private static final String TEXT_PLAIN = "text/plain";

    /** http://greenbytes.de/tech/webdav/rfc4918.html#rfc.section.11.2 */
    public static final int UNPROCESSABLE_ENTITY = 422;

    /** {@inheritDoc} */
    public Response toResponse(Exception runtime) {
        if (runtime instanceof ConstraintViolationException) {
            return Response.status(UNPROCESSABLE_ENTITY).entity(runtime.getMessage()).type(TEXT_PLAIN).build();
        } else if (runtime instanceof ObjectNotFoundException) {
            return Response.status(UNPROCESSABLE_ENTITY).entity(runtime.getMessage()).type(TEXT_PLAIN).build();
        } else if (runtime instanceof NotFoundException) {
            return Response.status(Response.Status.NO_CONTENT).entity(runtime.getMessage()).type(TEXT_PLAIN).build();
        } else if (runtime instanceof EntityValidationException) {
            return Response.status(UNPROCESSABLE_ENTITY).entity(runtime.getMessage()).type(TEXT_PLAIN).build();
        }

        return defaultResponse(runtime);
    }

    /**
     * Ensures that all unhandled exceptions are reported.
     *
     * @param  runtime An unhandled runtime exception.
     *
     * @return A server 500 error code.
     */
    private Response defaultResponse(Exception runtime) {
        // Ensure all unhandled exceptions are logged.
        log(runtime);

        return Response.serverError().entity(runtime.getMessage()).type(TEXT_PLAIN).build();
    }

    /**
     * Used to log all unhandled exceptions.
     *
     * @param runtime An unhanlded exception to log.
     */
    private void log(Exception runtime) {
        LOG.log(Level.SEVERE, runtime.getMessage(), runtime);
    }
}
