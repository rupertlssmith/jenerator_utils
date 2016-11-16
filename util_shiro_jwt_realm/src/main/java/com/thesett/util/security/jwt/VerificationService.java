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

/**
 * VerificationService provides information about the verification key and algorithm that can be used to verify tokens
 * issues by the auth server.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Provide verification keys. </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public interface VerificationService
{
    /**
     * Provides information about the verification algorithm and key that should be used to verify all tokens issues by
     * the auth service.
     *
     * @return Information about the verification algorithm and key.
     */
    Verifier retrieve();
}
