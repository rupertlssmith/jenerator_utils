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
package com.thesett.util.security.realm;

import java.security.PublicKey;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thesett.util.security.model.JWTAuthenticationToken;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * ShiroJWTRealm implements a Shiro realm that looks verifies users and their roles and permissions from a JWT token.
 *
 * <p/>The creation of the auth token cache is delayed to happen after the constructor is invoked. This is so that the
 * cache timeout can be set as a configuration parameter. It defaults to {@link #DEFAULT_AUTH_CACHE_TIMEOUT_SECONDS}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Verify a user by JWT token. </td><td> </td></tr>
 * <tr><td> Look up roles and permissions by authenticated principal. </td><td> </td></tr>
 * </table></pre>
 */
public class ShiroJWTRealm extends AuthorizingRealm
{
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(ShiroJWTRealm.class.getName());

    /** The default auth cache timeout. */
    private static final int DEFAULT_AUTH_CACHE_TIMEOUT_SECONDS = 1 * 60;

    /** The public key for checking access tokens against. */
    private PublicKey publicKey;

    /** The refresh cache. */
    private Cache<JWTAuthenticationToken, JWTAuthenticationToken> authTokenCache;

    /** A semaphore used to ensure only one thread creates the auth cache. */
    private final Object cacheCreateLock = new Object();

    /** The auth cache timeout to apply. */
    private int authCacheTimeoutSeconds = DEFAULT_AUTH_CACHE_TIMEOUT_SECONDS;

    /** Creates an uninitialized Shiro DB realm. */
    public ShiroJWTRealm()
    {
    }

    /**
     * Sets the auth cache timeout.
     *
     * @param authCacheTimeoutSeconds The auth cache timeout in seconds.
     */
    public void setAuthCacheTimeoutSeconds(int authCacheTimeoutSeconds)
    {
        this.authCacheTimeoutSeconds = authCacheTimeoutSeconds;
    }

    /** @param publicKey */
    public void intialize(PublicKey publicKey)
    {
        this.publicKey = publicKey;
    }

    /** Closes and cleans up this DB realm. */
    public void close()
    {
    }

    /**
     * {@inheritDoc}
     *
     * <p/>This realm supports {@link JWTAuthenticationToken}s only.
     */
    public boolean supports(AuthenticationToken token)
    {
        return (token != null) && (token instanceof JWTAuthenticationToken);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Verifies the JWT token.
     */
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) throws AuthenticationException
    {
        JWTAuthenticationToken jwtAuthToken = (JWTAuthenticationToken) authToken;

        Cache<JWTAuthenticationToken, JWTAuthenticationToken> authTokenCache = getAuthCache();

        JWTAuthenticationToken cachedToken = authTokenCache.getIfPresent(jwtAuthToken);

        if (cachedToken == null)
        {
            // Ensure that the token is validate and extract its claims.
            jwtAuthToken.setPublicKey(publicKey);
            jwtAuthToken.assertValid();
            jwtAuthToken.extractClaims(getName());

            // Cache the auth token for subsequent uses.
            authTokenCache.put(jwtAuthToken, jwtAuthToken);
        }
        else
        {
            jwtAuthToken = cachedToken;

            // Recheck the auth token expiry.
            if (jwtAuthToken.isExpired())
            {
                authTokenCache.invalidate(jwtAuthToken);
                throw new AuthenticationException("Auth token has expired.");
            }
        }

        return jwtAuthToken;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Extracts the roles and permissions set in a JWT token.
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
    {
        return (JWTAuthenticationToken) principals.getPrimaryPrincipal();
    }

    /**
     * Provides the auth cache, initializing it if it does not already exist.
     *
     * @return
     */
    private Cache<JWTAuthenticationToken, JWTAuthenticationToken> getAuthCache()
    {
        if (authTokenCache != null)
        {
            return authTokenCache;
        }

        synchronized (cacheCreateLock)
        {
            if (authTokenCache == null)
            {
                authTokenCache =
                    CacheBuilder.newBuilder().maximumSize(10000)
                        .expireAfterWrite(authCacheTimeoutSeconds, TimeUnit.SECONDS)
                        .build();
            }
        }

        return authTokenCache;
    }
}
