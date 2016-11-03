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
package com.thesett.util.security.model;

import java.security.PublicKey;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.thesett.util.security.jwt.JwtUtils;
import com.thesett.util.security.shiro.LocalSubject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

/**
 * JWTAuthenticationToken wraps a JWT token as a Shiro {@link AuthenticationToken}. The logic to check the token is
 * valid and to extract its claims is encapsulated here for convenience.
 *
 * <p/>Prior to invoking the {@link #assertValid()} and {@link #extractClaims()} methods, the public key used to verify
 * the token must be set using the {@link #setPublicKey(PublicKey)} method.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Encapsulate a JWT token as a Shiro access token. </td></tr>
 * <tr><td> Assert that a token is valid. </td><td> {@link JwtUtils} </td></tr>
 * <tr><td> Extract subject, roles and permissions claims. </td><td> {@link JwtUtils} </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class JWTAuthenticationToken implements AuthenticationToken
{
    /** Holds the raw JWT token as a base64 encoded string. */
    private String token;

    /** The extracted subject claim. */
    private String subject;

    /** The extracted permissions claims. */
    private List<String> permissions;

    /** The public key for checking access tokens against. */
    private PublicKey publicKey;

    /** Optional issuing time of the token. May be <tt>null</tt>. */
    private Date issuedAt;

    /** Optional expiry time of the token. May be <tt>null</tt>. */
    private Date expiresAt;

    /**
     * Creates an encapsulated JWT token from its raw representation.
     *
     * @param token The raw JWT token as a base64 encoded string.
     */
    public JWTAuthenticationToken(String token)
    {
        this.token = token;
    }

    /** {@inheritDoc} */
    public Object getPrincipal()
    {
        return getSubject();
    }

    /** {@inheritDoc} */
    public Object getCredentials()
    {
        return getToken();
    }

    /**
     * Provides the raw JWT token as a string.
     *
     * @return The raw JWT token as a string.
     */
    public String getToken()
    {
        return token;
    }

    /**
     * Provides the subject, which is the same as the username of the authenticated user.
     *
     * @return The username of the authenticated user.
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * Provides a list of permissions of the authenticated user.
     *
     * @return A list of permissions of the authenticated user.
     */
    public List<String> getPermissions()
    {
        return permissions;
    }

    /**
     * Provides a {@link LocalSubject} that matches the subject, and permissions on this token.
     *
     * @return A Shiro subject matching this JWT token.
     */
    public Subject asLocalSubject()
    {
        LocalSubject subject = new LocalSubject();

        subject.withPrimaryPrincipal(subject);
        permissions.forEach(subject::withPermission);

        return subject;
    }

    /**
     * Sets up the public key needed to validate the token.
     *
     * @param publicKey The public key needed to validate the token.
     */
    public void setPublicKey(PublicKey publicKey)
    {
        this.publicKey = publicKey;
    }

    /**
     * Parses the token and checks that it, is well-formed, has a valid signature and has nto expired.
     *
     * <p/>The {@link #setPublicKey(PublicKey)} method needs to the invoked with the correct verification key, prior to
     * calling this.
     *
     * @throws AuthenticationException If the token is not valid.
     */
    public void assertValid()
    {
        boolean isValidToken = checkValid();

        if (!isValidToken)
        {
            throw new AuthenticationException();
        }
    }

    /**
     * Parses the token and checks that it, is well-formed, has a valid signature and has nto expired.
     *
     * <p/>The {@link #setPublicKey(PublicKey)} method needs to the invoked with the correct verification key, prior to
     * calling this.
     *
     * @return <tt>true</tt> iff the token is valid.
     */
    public boolean checkValid()
    {
        return JwtUtils.checkToken(token, publicKey);
    }

    /**
     * Extracts the subject, roles and permissions from the token. {@link #assertValid()} or {@link #checkValid()}
     * should be called prior to this to check that the token can be decoded.
     *
     * <p/>Token expiry is re-checked for when extracting the tokens claims. If the token has expired, an authentication
     * exception will be raised.
     *
     * <p/>The {@link #setPublicKey(PublicKey)} method needs to the invoked with the correct verification key, prior to
     * calling this.
     *
     * @throws AuthenticationException If the token has expired.
     */
    public void extractClaims()
    {
        Claims claims = null;

        try
        {
            claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();
        }
        catch (ExpiredJwtException e)
        {
            throw new AuthenticationException();
        }

        subject = claims.get("sub", String.class);

        permissions = claims.get("scopes", List.class);

        if (permissions == null)
        {
            permissions = new LinkedList<>();
        }

        expiresAt = claims.getExpiration();
        issuedAt = claims.getIssuedAt();
    }

    /**
     * Optional issuing time of the token. May be <tt>null</tt>.
     *
     * @return Issuing time of the token. May be <tt>null</tt> if not set.
     */
    public Date getIssuedAt()
    {
        return issuedAt;
    }

    /**
     * Optional expiry time of the token. May be <tt>null</tt>.
     *
     * @return Expiry time of the token. May be <tt>null</tt> if not set.
     */
    public Date getExpiresAt()
    {
        return expiresAt;
    }
}
