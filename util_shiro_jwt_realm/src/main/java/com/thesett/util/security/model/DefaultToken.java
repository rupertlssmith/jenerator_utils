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

import org.apache.shiro.authc.AuthenticationToken;

/**
 * DefaultToken is a Shiro authentication token, that provides no principal or credentials. It can be used if
 * non-authenticated users are allowed to access a protected resource as a default user.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide default non-authenticated principal and credentials.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class DefaultToken implements AuthenticationToken
{
    public Object getPrincipal()
    {
        return null;
    }

    public Object getCredentials()
    {
        return null;
    }
}
