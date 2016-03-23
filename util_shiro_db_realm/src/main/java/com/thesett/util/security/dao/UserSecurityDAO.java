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
package com.thesett.util.security.dao;

import com.thesett.util.security.model.AuthUser;

/**
 * UserSecurityDAO defines a DAO for working with users in a database.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Find users by username. </td></tr>
 * <tr><td> Find users by database id. </td></tr>
 * </table></pre>
 */
public interface UserSecurityDAO
{
    /**
     * Finds a users authentication data by username.
     *
     * @param  username The username to find.
     *
     * @return The matching user or <tt>null</tt> if no matching user can be found.
     */
    AuthUser findUserByUsername(String username);

    /**
     * Finds a users authentication data by id.
     *
     * @param  id The users id.
     *
     * @return The matching user or <tt>null</tt> if no matching user can be found.
     */
    AuthUser retrieve(Long id);
}
