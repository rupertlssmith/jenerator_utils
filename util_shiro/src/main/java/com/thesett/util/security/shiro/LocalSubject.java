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
package com.thesett.util.security.shiro;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

/**
 * LocalSubject is a shiro security subject that can be configured locally, and then attached to the current thread to
 * provide access rights locally to that thread.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Capture principals, roles and permissions as a subject. </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class LocalSubject implements Subject
{
    /** Holds the principals associated with this subject. */
    SimplePrincipalCollection principalCollection;

    /** Holds the permissions of this subject. */
    Set<String> permissions = new HashSet<>();

    /** Holds the roles of this subject. */
    Set<String> roles = new HashSet<>();

    /**
     * Adds a permission to this subject.
     *
     * @param  permission The permission to add.
     *
     * @return <tt>this</tt>, fluent style.
     */
    public LocalSubject withPermission(String permission)
    {
        permissions.add(permission);

        return this;
    }

    /**
     * Adds a role to this subject.
     *
     * @param  roleName The role to add.
     *
     * @return <tt>this</tt>, fluent style.
     */
    public LocalSubject withRole(String roleName)
    {
        roles.add(roleName);

        return this;
    }

    /**
     * Sets the primary principal on this subject.
     *
     * @param  principal The primary principal.
     *
     * @return <tt>this</tt>, fluent style.
     */
    public LocalSubject withPrimaryPrincipal(Object principal)
    {
        principalCollection = new SimplePrincipalCollection(principal, "localRealm");

        return this;
    }

    /** {@inheritDoc} */
    public Object getPrincipal()
    {
        if (principalCollection != null)
        {
            return principalCollection.getPrimaryPrincipal();
        }
        else
        {
            return null;
        }
    }

    /** {@inheritDoc} */
    public PrincipalCollection getPrincipals()
    {
        return principalCollection;
    }

    /** {@inheritDoc} */
    public boolean isPermitted(String permission)
    {
        return permissions.contains(permission);
    }

    /** {@inheritDoc} */
    public boolean isPermitted(org.apache.shiro.authz.Permission permission)
    {
        return false;
    }

    /** {@inheritDoc} */
    public boolean[] isPermitted(String... permissions)
    {
        return new boolean[permissions.length];
    }

    /** {@inheritDoc} */
    public boolean[] isPermitted(List<Permission> permissions)
    {
        return new boolean[permissions.size()];
    }

    /** {@inheritDoc} */
    public boolean isPermittedAll(String... permissions)
    {
        return this.permissions.containsAll(Arrays.asList(permissions));
    }

    /** {@inheritDoc} */
    public boolean isPermittedAll(Collection<Permission> permissions)
    {
        return this.permissions.containsAll(permissions);
    }

    /** {@inheritDoc} */
    public void checkPermission(String permission) throws AuthorizationException
    {
        if (!isPermitted(permission))
        {
            throw new AuthorizationException();
        }
    }

    /** {@inheritDoc} */
    public void checkPermission(org.apache.shiro.authz.Permission permission) throws AuthorizationException
    {
        if (!isPermitted(permission))
        {
            throw new AuthorizationException();
        }
    }

    /** {@inheritDoc} */
    public void checkPermissions(String... permissions) throws AuthorizationException
    {
        if (!isPermittedAll(permissions))
        {
            throw new AuthorizationException();
        }
    }

    /** {@inheritDoc} */
    public void checkPermissions(Collection<org.apache.shiro.authz.Permission> permissions)
        throws AuthorizationException
    {
        if (!isPermittedAll(permissions))
        {
            throw new AuthorizationException();
        }
    }

    /** {@inheritDoc} */
    public boolean hasRole(String roleIdentifier)
    {
        return false;
    }

    /** {@inheritDoc} */
    public boolean[] hasRoles(List<String> roleIdentifiers)
    {
        return new boolean[0];
    }

    /** {@inheritDoc} */
    public boolean hasAllRoles(Collection<String> roleIdentifiers)
    {
        return false;
    }

    /** {@inheritDoc} */
    public void checkRole(String roleIdentifier) throws AuthorizationException
    {
    }

    /** {@inheritDoc} */
    public void checkRoles(Collection<String> roleIdentifiers) throws AuthorizationException
    {
    }

    /** {@inheritDoc} */
    public void checkRoles(String... roleIdentifiers) throws AuthorizationException
    {
    }

    /** {@inheritDoc} */
    public void login(AuthenticationToken token) throws AuthenticationException
    {
    }

    /** {@inheritDoc} */
    public boolean isAuthenticated()
    {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isRemembered()
    {
        return false;
    }

    /** {@inheritDoc} */
    public Session getSession()
    {
        return null;
    }

    /** {@inheritDoc} */
    public Session getSession(boolean create)
    {
        return null;
    }

    /** {@inheritDoc} */
    public void logout()
    {
    }

    /** {@inheritDoc} */
    public <V> V execute(Callable<V> callable) throws ExecutionException
    {
        return null;
    }

    /** {@inheritDoc} */
    public void execute(Runnable runnable)
    {
    }

    /** {@inheritDoc} */
    public <V> Callable<V> associateWith(Callable<V> callable)
    {
        return null;
    }

    /** {@inheritDoc} */
    public Runnable associateWith(Runnable runnable)
    {
        return null;
    }

    /** {@inheritDoc} */
    public void runAs(PrincipalCollection principals) throws NullPointerException, IllegalStateException
    {
    }

    /** {@inheritDoc} */
    public boolean isRunAs()
    {
        return false;
    }

    /** {@inheritDoc} */
    public PrincipalCollection getPreviousPrincipals()
    {
        return null;
    }

    /** {@inheritDoc} */
    public PrincipalCollection releaseRunAs()
    {
        return null;
    }
}
