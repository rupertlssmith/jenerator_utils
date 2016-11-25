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
package com.thesett.util.security.model;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * AnonymousToken is a Shiro authentication token, that provides no principal or credentials. It can be used if
 * non-authenticated users are allowed to access a protected resource as a default user.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide default non-authenticated principal and credentials.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class AnonymousToken implements AuthenticationToken, AuthenticationInfo, AuthorizationInfo
{
    private static final LinkedList<String> EMPTY_ROLES = new LinkedList<>();
    private static final LinkedList<String> EMPTY_PERMISSIONS = new LinkedList<>();
    private static final LinkedList<Permission> EMPTY_OBJECT_PERMISSIONS = new LinkedList<>();

    /** The Shiro security principals. */
    private final PrincipalCollection principals;

    /** Creates an anonymous token. */
    public AnonymousToken()
    {
        this.principals = new SimplePrincipalCollection(this, "");
    }

    /** {@inheritDoc} */
    public Object getPrincipal()
    {
        return "anonymous";
    }

    /** {@inheritDoc} */
    public Object getCredentials()
    {
        return "anonymous";
    }

    /** {@inheritDoc} */
    public PrincipalCollection getPrincipals()
    {
        return principals;
    }

    /** {@inheritDoc} */
    public Collection<String> getRoles()
    {
        return EMPTY_ROLES;
    }

    /** {@inheritDoc} */
    public Collection<String> getStringPermissions()
    {
        return EMPTY_PERMISSIONS;
    }

    /** {@inheritDoc} */
    public Collection<Permission> getObjectPermissions()
    {
        return EMPTY_OBJECT_PERMISSIONS;
    }
}
