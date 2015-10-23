package com.thesett.util.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.thesett.util.error.toplevelhandlers.SLF4JLenientTopLevelErrorHandler;
import com.thesett.util.error.toplevelhandlers.TopLevelErrorHandler;

/**
 * ErrorHandlerFilter provides an HTTP filter that is placed first around all web requests. Any unhandled exceptions
 * that fall through to this filter are passed to a lenient top level error handler for logging.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Ensure all unhandled HTTP exceptions are handled. </td></tr>
 * </table></pre>
 */
public class ErrorHandlerFilter implements Filter {
    /** For logging all unhandled exceptions. */
    public static final TopLevelErrorHandler TOP_LEVEL_ERROR_HANDLER = new SLF4JLenientTopLevelErrorHandler();

    /** {@inheritDoc} */
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Wraps all HTTP requests in a top-level error handler.
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Throwable t) {
            TOP_LEVEL_ERROR_HANDLER.handleThrowable(t);
            throw t;
        }
    }

    /** {@inheritDoc} */
    public void destroy() {
    }
}
