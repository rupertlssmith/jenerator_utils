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

import java.util.logging.Logger;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

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

    /** Creates an uninitialized Shiro DB realm. */
    public ShiroJWTRealm()
    {
    }

    /**  */
    public void intialize()
    {
    }

    /** Closes and cleans up this DB realm. */
    public void close()
    {
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Looks up a user by username in the database, and supplies the user id and password for authentication.
     */
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authNToken) throws AuthenticationException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Given an authenticated principal (user id), looks up the roles and permissions for that user.
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
    {
        return null;
    }
}
