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
package com.thesett.util.security.jwt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.Set;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

/**
 * JWTUtils provides some helper functions for working with JWT tokens.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Create a JWT token. </td></tr>
 * <tr><td> Check a JWT token. </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class JwtUtils
{
    /**
     * Builds a JWT token with claims matching the users account and permissions.
     *
     * @param  subject     The name of the authenticated subject.
     * @param  permissions The users permissions.
     * @param  secretKey   The secret key for signing the token.
     *
     * @return A signed JWT token.
     */
    public static String createToken(String subject, Set<String> permissions, PrivateKey secretKey)
    {
        JwtBuilder builder = Jwts.builder();
        builder.setSubject(subject);

        StringBuilder permissionsCSV = new StringBuilder();

        for (Iterator<String> i = permissions.iterator(); i.hasNext();)
        {
            String permission = i.next();
            permissionsCSV.append(permission);

            if (i.hasNext())
            {
                permissionsCSV.append(", ");
            }
        }

        builder.claim("permissions", permissionsCSV);

        builder.signWith(SignatureAlgorithm.RS512, secretKey);

        return builder.compact();
    }

    /**
     * Parses a JWT token in order to confirm that it is valid.
     *
     * @param  token     The JWT token to parse.
     * @param  publicKey The public key for validating the token.
     *
     * @return <tt>true</tt> iff the token is valid.
     */
    public static boolean checkToken(String token, PublicKey publicKey)
    {
        try
        {
            Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);

            return true;
        }
        catch (SignatureException | UnsupportedJwtException | ExpiredJwtException | MalformedJwtException e)
        {
            return false;
        }
    }
}
