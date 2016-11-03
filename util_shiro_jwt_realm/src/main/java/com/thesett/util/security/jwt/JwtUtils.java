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
package com.thesett.util.security.jwt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.thesett.util.security.model.JWTAuthenticationToken;
import com.thesett.util.string.StringUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

import org.apache.shiro.web.util.WebUtils;

/**
 * JWTUtils provides some helper functions for working with JWT tokens.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Create a JWT token. </td></tr>
 * <tr><td> Check a JWT token. </td></tr>
 * <tr><td> Extract JWT tokens from HTTP requests. </td></tr>
 * <tr><td> Present JWT tokens as Shiro access tokens. </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class JwtUtils
{
    /** For generating random token ids. */
    public static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Builds a JWT token with claims matching the users account and permissions.
     *
     * @param  subject      The name of the authenticated subject.
     * @param  permissions  The users permissions.
     * @param  secretKey    The secret key for signing the token.
     * @param  expiryMillis The number of milliseconds from now that the token is to be valid for. Optional, may be <tt>
     *                      null</tt>.
     *
     * @return A signed JWT token.
     */
    public static String createToken(String subject, Set<String> permissions, PrivateKey secretKey, Long expiryMillis)
    {
        JwtBuilder builder = Jwts.builder();
        builder.setSubject(subject);
        builder.setIssuedAt(new Date());

        if (expiryMillis != null)
        {
            Date expiry = new Date();
            expiry.setTime(expiry.getTime() + expiryMillis);
            builder.setExpiration(expiry);
        }

        byte[] bytes = new byte[8];
        RANDOM.nextBytes(bytes);

        String id = Base64.getEncoder().encodeToString(bytes);

        builder.setId(id);

        builder.claim("scopes", permissions);
        builder.signWith(SignatureAlgorithm.RS512, secretKey);

        return builder.compact();
    }

    /**
     * Parses a JWT token in order to confirm that it is valid.
     *
     * @param  token     The JWT token to parse.
     * @param  publicKey The public key for validating the token.
     *
     * @return <tt>true</tt> iff the token is valid.
     */
    public static boolean checkToken(String token, PublicKey publicKey)
    {
        try
        {
            Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);

            return true;
        }
        catch (SignatureException | UnsupportedJwtException | ExpiredJwtException | MalformedJwtException e)
        {
            return false;
        }
    }

    /**
     * Tries to extract a JWT token from an HTTP request, from a session cookie called 'jwt' or from the Authorization
     * header as a bearer token, in that order. The extracted token is stored against the request as an attribute called
     * 'jwt'.
     *
     * @param  request       The request to extract and set the jwt token against.
     * @param  cookieName    The name of the cookie to get the JWT token from.
     * @param  attributeName The name of the request attribute to store the JWT token in.
     *
     * @return <tt>true</tt> iff a JWT token was found.
     */
    public static boolean extractJWTtoRequestAttribute(ServletRequest request, String cookieName, String attributeName)
    {
        return extractJWTCookieToRequestAttribute(request, cookieName, attributeName) ||
            extractJWTAuthHeaderToRequestAttribute(request);
    }

    /**
     * Tries to extract a JWT token from an HTTP request, from a session cookie called 'jwt'. The extracted token is
     * stored against the request as an attribute called 'jwt'.
     *
     * @param  request       The request to extract and set the jwt token against.
     * @param  cookieName    The name of the cookie to get the JWT token from.
     * @param  attributeName The name of the request attribute to store the JWT token in.
     *
     * @return <tt>true</tt> iff a JWT token was found.
     */
    public static boolean extractJWTCookieToRequestAttribute(ServletRequest request, String cookieName,
        String attributeName)
    {
        String token = getJWTTokenFromCookie(request, cookieName);

        if (token != null)
        {
            request.setAttribute(attributeName, token);

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Tries to extract a JWT token from an HTTP request, from the Authorization header as a bearer token, in that
     * order. The extracted token is stored against the request as an attribute called 'jwt'.
     *
     * @param  request The request to extract and set the jwt token against.
     *
     * @return <tt>true</tt> iff a JWT token was found.
     */
    public static boolean extractJWTAuthHeaderToRequestAttribute(ServletRequest request)
    {
        String token = getJWTTokenFromAuthHeader(request);

        if (token != null)
        {
            request.setAttribute("jwt", token);

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Tries to extract a JWT token from the name cookie.
     *
     * @param  request    The request get the cookie from.
     * @param  cookieName The name of the cookie to get the JWT token from.
     *
     * @return The JWT token, or <tt>null</tt> if no matching cookie is found.
     */
    public static String getJWTTokenFromCookie(ServletRequest request, String cookieName)
    {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);

        Cookie[] cookies = httpRequest.getCookies();

        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookieName.equals(cookie.getName()))
                {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Tries to extract a JWT token from the 'Authorization' header. The value of this header should have the format :
     * 'Bearer &lt;token&gt;'.
     *
     * @param  request The request to get the Authorization header from.
     *
     * @return The JWT token, or <tt>null</tt> if header field value is found.
     */
    public static String getJWTTokenFromAuthHeader(ServletRequest request)
    {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);

        String authorization = httpRequest.getHeader("Authorization");

        if (StringUtils.nullOrEmpty(authorization))
        {
            return null;
        }

        authorization = authorization.trim();

        if (!authorization.startsWith("Bearer "))
        {
            return null;
        }

        authorization = authorization.substring(7);

        if (StringUtils.nullOrEmpty(authorization))
        {
            return null;
        }

        return authorization;
    }

    /**
     * Extracts a JWT token from an attribute on a request, and returns it as a {@link JWTAuthenticationToken}.
     *
     * @param  request       The request to get the token from.
     * @param  attributeName The name of the attribute to get the token from.
     *
     * @return The token as a {@link JWTAuthenticationToken}.
     */
    public static JWTAuthenticationToken getAuthenticationToken(ServletRequest request, String attributeName)
    {
        String jwtToken = (String) request.getAttribute(attributeName);

        return new JWTAuthenticationToken(jwtToken);
    }
}
