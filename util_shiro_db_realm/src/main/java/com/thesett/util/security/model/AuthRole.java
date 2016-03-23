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

/**
 * AuthRole provides the minimum set of fields that a user account needs to supply in order to provide its authenticated
 * roles withina Shiro realm.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide a role name and associated permissions. </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class AuthRole
{
    private String name;
    private Collection<String> permissions;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Collection<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Collection<String> permissions)
    {
        this.permissions = permissions;
    }
}
