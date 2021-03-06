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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.thesett.util.security.jwt.JwtUtils;
import com.thesett.util.security.shiro.LocalSubject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

/**
 * JWTAuthenticationToken wraps a JWT token as a Shiro {@link AuthenticationToken}. The logic to check the token is
 * valid and to extract its claims is encapsulated here for convenience.
 *
 * <p/>Prior to invoking the {@link #assertValid()} and {@link #extractClaims(String)} methods, the public key used to
 * verify the token must be set using the {@link #setPublicKey(PublicKey)} method.
 *
 * <p/>For the purpose of caching authentication tokens, the {@link #token} field containing the raw token is used. Once
 * a token has been seen once it can be accepted (up to its expiry time).
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
public class JWTAuthenticationToken implements AuthenticationToken, AuthenticationInfo, AuthorizationInfo
{
    /** Holds the raw JWT token as a base64 encoded string. */
    private String token;

    /** The extracted subject claim. */
    private String subject;

    /** The extracted permissions claims. */
    private Set<String> permissions;

    /** The public key for checking access tokens against. */
    private PublicKey publicKey;

    /** Optional issuing time of the token. May be <tt>null</tt>. */
    private Date issuedAt;

    /** Optional expiry time of the token. May be <tt>null</tt>. */
    private Date expiresAt;

    /** Used to cache the hash code. */
    private volatile int hashCode;

    /** Used to indicate the hash code is cached. */
    private volatile boolean hashCodeCached;

    private AuthenticationInfo authenticationInfo;
    private AuthorizationInfo authorizationInfo;

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
    public Set<String> getPermissions()
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
     * Checks if this token has expired before now.
     *
     * <p/>{@link #extractClaims()} needs to have been called before this method can be used.
     *
     * @return <tt>true</tt> if this token has expired before now.
     */
    public boolean isExpired()
    {
        // Recheck the auth token expiry.
        long now = System.currentTimeMillis();
        long expiry = getExpiresAt().getTime();

        return (expiry < now);
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
     * @param  realmName The name of the Shiro realm to extract claims for.
     *
     * @throws AuthenticationException If the token has expired.
     */
    public void extractClaims(String realmName)
    {
        Claims claims = null;

        try
        {
            claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();
        }
        catch (ExpiredJwtException e)
        {
            throw new AuthenticationException(e);
        }

        subject = claims.get("sub", String.class);

        permissions = new LinkedHashSet<>(claims.get("scopes", List.class));

        if (permissions == null)
        {
            permissions = new HashSet<>();
        }

        expiresAt = claims.getExpiration();
        issuedAt = claims.getIssuedAt();

        // Extract the authentication and authorization info in the form that Shiro uses.
        PrincipalCollection principals = new SimplePrincipalCollection(this, realmName);

        authenticationInfo = new SimpleAuthenticationInfo(principals, token);

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        getPermissions().forEach(info::addStringPermission);

        authorizationInfo = info;
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

    /** {@inheritDoc} */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        JWTAuthenticationToken that = (JWTAuthenticationToken) o;

        return token.equals(that.token);
    }

    /** {@inheritDoc} */
    public int hashCode()
    {
        int result = hashCode;

        if (!hashCodeCached)
        {
            result = token.hashCode();
            hashCode = result;
            hashCodeCached = true;
        }

        return result;
    }

    /** {@inheritDoc} */
    public PrincipalCollection getPrincipals()
    {
        return authenticationInfo.getPrincipals();
    }

    /** {@inheritDoc} */
    public Collection<String> getRoles()
    {
        return authorizationInfo.getRoles();
    }

    /** {@inheritDoc} */
    public Collection<String> getStringPermissions()
    {
        return authorizationInfo.getStringPermissions();
    }

    /** {@inheritDoc} */
    public Collection<Permission> getObjectPermissions()
    {
        return authorizationInfo.getObjectPermissions();
    }
}
