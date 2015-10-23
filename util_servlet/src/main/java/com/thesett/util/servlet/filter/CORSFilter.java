package com.thesett.util.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * CORSFilter sets the HTTPS header fields for cross origin resource sharing, to allow access from front-ends that are
 * not served by this instance.
 *
 * <p/><b>Note:</b>This should be for development purposes only, as it enables all access.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Add the 'Access-Control-Allow-Origin' header. </td></tr>
 * </table></pre>
 */
public class CORSFilter implements Filter {
    private static final String ACAO = "Access-Control-Allow-Origin";
    private static final String ACAM = "Access-Control-Allow-Methods";
    private static final String ACMA = "Access-Control-Max-Age";
    private static final String ACAH = "Access-Control-Allow-Headers";

    /**
     * {@inheritDoc}
     *
     * <p/>Adds headers to the response to allow cross origin use of the API.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader(ACAO, "*");
        resp.setHeader(ACAM, "PUT, POST, GET, OPTIONS, DELETE");
        resp.setHeader(ACMA, "3600");
        resp.addHeader(ACAH, "x-requested-with");
        resp.addHeader(ACAH, "Content-Type");
        resp.addHeader(ACAH, "Authorization");
        resp.addHeader(ACAH, "Bearer");
        chain.doFilter(request, response);
    }

    /** {@inheritDoc} */
    public void destroy() {
        // No actions on destroy.
    }

    /** {@inheritDoc} */
    public void init(FilterConfig arg0) throws ServletException {
        // No actions on init.
    }
}
