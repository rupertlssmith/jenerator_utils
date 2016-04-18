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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thesett.util.security.model.JWTAuthenticationToken;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ShiroJWTAuthenticatingFilter extends AuthenticatingFilter
{
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    /** {@inheritDoc} */
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse)
        throws Exception
    {
        String jwtToken = getAuthzHeader(servletRequest);

        if (jwtToken != null)
        {
            return new JWTAuthenticationToken(null, jwtToken);
        }

        return null;
    }

    /** {@inheritDoc} */
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception
    {
        HttpServletResponse httpResponse = WebUtils.toHttp(servletResponse);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        return false;
    }

    private String getAuthzHeader(ServletRequest request)
    {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);

        Cookie[] cookies = httpRequest.getCookies();

        for (Cookie cookie : cookies)
        {
            cookie.getName();
        }

        return httpRequest.getHeader(AUTHORIZATION_HEADER);
    }
}
