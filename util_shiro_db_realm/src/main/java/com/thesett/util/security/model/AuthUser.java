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

import java.util.Set;

/**
 * AuthUser defines the minimum set of fields that a user account needs to supply in order to authenticate with a Shiro
 * realm.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide a users password, id and roles. </td><td> {@link AuthUser} </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class AuthUser
{
    private String password;
    private Object id;
    private Set<AuthRole> roles;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Object getId()
    {
        return id;
    }

    public void setId(Object id)
    {
        this.id = id;
    }

    public Set<AuthRole> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<AuthRole> roles)
    {
        this.roles = roles;
    }
}
