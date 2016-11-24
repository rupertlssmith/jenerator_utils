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
    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception
    {
        return SecurityUtils.getSubject().isAuthenticated() || this.onAccessDenied(request, response);
    }

    /**
     * Invoked whenever the currently executing subject is not authenticated.
     *
     * @param  request  The HTTP request.
     * @param  response The HTTP response.
     *
     * @return <tt>true</tt> if a JWT token is supplied in an HTTP header attribute or cookie, which is valid. Also <tt>
     *         true</tt> the path being filtered allows default users, in which case the
     *         {@link com.thesett.util.security.model.DefaultToken} will be used to login and set up a default anonymous
     *         user.
     */
    private boolean onAccessDenied(ServletRequest request, ServletResponse response)
    {
        boolean loggedIn;

        JwtUtils.extractJWTtoRequestAttribute(request, COOKIE_NAME, ATTRIBUTE_NAME);

        AuthenticationToken token = JwtUtils.getAuthenticationToken(request, ATTRIBUTE_NAME);

        if (token == null)
        {
            return false;
        }

        try
        {
            Subject subject = SecurityUtils.getSubject();
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
