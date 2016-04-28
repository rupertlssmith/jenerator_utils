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
    public void setLoginUrl(String loginUrl)
    {
        String previous = getLoginUrl();

        if (previous != null)
        {
            this.appliedPaths.remove(previous);
        }

        super.setLoginUrl(loginUrl);
        this.appliedPaths.put(getLoginUrl(), null);
    }

    /** {@inheritDoc} */
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception
    {
        String jwtToken = getJWTTokenFromCookie(request);

        return new JWTAuthenticationToken(null, jwtToken);
    }

    /** {@inheritDoc} */
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception
    {
        boolean loggedIn = false;

        if (isJWTCookieRequest(request, response))
        {
            loggedIn = executeLogin(request, response);
        }

        if (!loggedIn)
        {
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return loggedIn;
    }

    private boolean isJWTCookieRequest(ServletRequest request, ServletResponse response)
    {
        return getJWTTokenFromCookie(request) != null;
    }

    private String getJWTTokenFromCookie(ServletRequest request)
    {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);

        Cookie[] cookies = httpRequest.getCookies();

        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if ("jwt".equals(cookie.getName()))
                {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
