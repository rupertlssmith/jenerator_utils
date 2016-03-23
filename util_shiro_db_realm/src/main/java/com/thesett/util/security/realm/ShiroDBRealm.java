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

import java.util.Set;
import java.util.logging.Logger;

import com.thesett.util.security.dao.UserSecurityDAO;
import com.thesett.util.security.model.AuthRole;
import com.thesett.util.security.model.AuthUser;
import com.thesett.util.string.StringUtils;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * ShiroDBRealm implements a Shiro realm that looks up users and their roles in the database using a
 * {@link UserSecurityDAO}.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Look up users by username. </td><td> {@link UserSecurityDAO} </td></tr>
 * <tr><td> Look up roles and permissions by user id. </td><td> {@link UserSecurityDAO} </td></tr>
 * </table></pre>
 */
public class ShiroDBRealm extends AuthorizingRealm
{
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(ShiroDBRealm.class.getName());

    /** The security DAO to query the security information in the database with. */
    protected UserSecurityDAO userSecurityDAO;

    /** Creates an uninitialized Shiro DB realm. */
    public ShiroDBRealm()
    {
    }

    /**  */
    public void intialize(UserSecurityDAO userSecurityDAO)
    {
        this.userSecurityDAO = userSecurityDAO;
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
        LOG.fine("protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authNToken): called");

        UsernamePasswordToken upToken = (UsernamePasswordToken) authNToken;

        String username = upToken.getUsername();

        if (StringUtils.nullOrEmpty(username))
        {
            throw new AccountException("'username' is required by this realm.");
        }

        AuthUser user = userSecurityDAO.findUserByUsername(username);

        if (user != null)
        {
            String password = user.getPassword();

            PrincipalCollection principals = new SimplePrincipalCollection(user.getId(), getName());

            return new SimpleAuthenticationInfo(principals, password);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Given an authenticated principal (user id), looks up the roles and permissions for that user.
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
    {
        LOG.fine("protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals): called");

        if (principals == null)
        {
            return null;
        }

        Object principalId = getAvailablePrincipal(principals);

        if (principalId == null)
        {
            return null;
        }

        AuthUser user = userSecurityDAO.retrieve((Long) principalId);

        if (user == null)
        {
            return null;
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        Set<AuthRole> roles = user.getRoles();

        if (roles != null)
        {
            for (AuthRole role : roles)
            {
                info.addRole(role.getName());
                info.addStringPermissions(role.getPermissions());
            }
        }

        return info;
    }
}
