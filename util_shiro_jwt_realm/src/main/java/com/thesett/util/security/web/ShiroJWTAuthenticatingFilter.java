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
package com.thesett.util.security.web;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.thesett.util.security.jwt.JwtUtils;
import com.thesett.util.security.model.JWTAuthenticationToken;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * ShiroJWTAuthenticatingFilter is a Shiro AuthenticatingFilter that looks for the presence of a JWT token in the HTTP
 * request. If a JWT token is found, it is extracted as a {@link JWTAuthenticationToken} and used as the access token
 * for the downstream Shiro realm.
 *
 * <p/>The JWT token may be passed as a bearer token in the authorization header (Authorization: Bearer ...), or as a
 * session cookie. If both are present, the cookie is given preference and the authorization header field ignored.
 *
 * <p/>This filter only extracts the JWT token as a Shiro AuthenticationToken. It does not validate it in any way, that
 * is left up to the Shiro security realm.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Extract JWT tokens from HTTP requests.
 * <tr><td> Reject requests without a JWT token as unauthorized.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ShiroJWTAuthenticatingFilter extends PathMatchingFilter
{
    /** The name of the cookie used to present JWT tokens with requests. */
    public static final String COOKIE_NAME = "jwt";

    /** The name of the request attribute used to hold JWT tokens in. */
    public static final String ATTRIBUTE_NAME = COOKIE_NAME;

    /** {@inheritDoc} */
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception
    {
        return JwtUtils.getAuthenticationToken(request, ATTRIBUTE_NAME);
    }

    /**
     * Convenience method that acquires the Subject associated with the request.
     *
     * <p/>The default implementation simply returns
     * {@link org.apache.shiro.SecurityUtils#getSubject() SecurityUtils.getSubject()}.
     *
     * @param  request  the incoming <code>ServletRequest</code>
     * @param  response the outgoing <code>ServletResponse</code>
     *
     * @return the Subject associated with the request.
     */
    protected Subject getSubject(ServletRequest request, ServletResponse response)
    {
        return SecurityUtils.getSubject();
    }

    /**
     * Determines whether the current subject is authenticated.
     *
     * <p/>The default implementation
     * {@link #getSubject(javax.servlet.ServletRequest, javax.servlet.ServletResponse) acquires} the currently executing
     * Subject and then returns {@link org.apache.shiro.subject.Subject#isAuthenticated() subject.isAuthenticated()};
     *
     * @return true if the subject is authenticated; false if the subject is unauthenticated
     */
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
    {
        Subject subject = getSubject(request, response);

        return subject.isAuthenticated();
    }

    /** {@inheritDoc} */
    protected boolean onPreHandle(ServletRequest request, ServletResponse response) throws Exception
    {
        boolean loggedIn;

        AuthenticationToken token = createToken(request, response);

        if (token == null)
        {
            String msg =
                "createToken method implementation returned null. A valid non-null AuthenticationToken " +
                "must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        }

        try
        {
            Subject subject = getSubject(request, response);
            subject.login(token);

            loggedIn = true;
        }
        catch (AuthenticationException e)
        {
            // The authentication exception is set to null to indicate that it is being deliberately ignored.
            // The compensation action is to set the loggedIn flag to false, which will cause an appropriate
            // response to be generated.
            e = null;
            loggedIn = false;
        }

        if (!loggedIn)
        {
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return loggedIn;
    }
}
