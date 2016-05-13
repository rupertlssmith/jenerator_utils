/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
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
import java.util.logging.Logger;

import com.thesett.util.security.jwt.JwtUtils;
import com.thesett.util.security.model.JWTAuthenticationToken;
import com.thesett.util.string.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * ShiroJWTRealm implements a Shiro realm that looks verifies users and their roles and permissions from a JWT token.
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

    /** The public key for checking access tokens against. */
    private PublicKey publicKey;

    /** Creates an uninitialized Shiro DB realm. */
    public ShiroJWTRealm()
    {
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
        String token = jwtAuthToken.getToken();

        assertValidToken(token);

        PrincipalCollection principals = new SimplePrincipalCollection(authToken, getName());

        return new SimpleAuthenticationInfo(principals, token);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Extracts the roles and permissions set in a JWT token.
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
    {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        JWTAuthenticationToken jwtAuthToken = (JWTAuthenticationToken) principals.getPrimaryPrincipal();
        String token = jwtAuthToken.getToken();

        assertValidToken(token);

        Claims claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();

        String permissionsCSV = claims.get("permissions", String.class);
        String rolesCSV = claims.get("roles", String.class);

        if (!StringUtils.nullOrEmpty(permissionsCSV))
        {
            for (String permission : permissionsCSV.split(","))
            {
                permission = permission.trim();

                info.addStringPermission(permission);
            }
        }

        if (!StringUtils.nullOrEmpty(rolesCSV))
        {
            for (String role : rolesCSV.split(","))
            {
                role = role.trim();

                info.addRole(role);
            }
        }

        return info;
    }

    /**
     * Checks that a JWT token is valid wrt the public key, and wrt its timestamp.
     *
     * @param token The token to validate.
     */
    private void assertValidToken(String token)
    {
        boolean isValidToken = JwtUtils.checkToken(token, publicKey);

        if (!isValidToken)
        {
            throw new AuthenticationException();
        }
    }
}
