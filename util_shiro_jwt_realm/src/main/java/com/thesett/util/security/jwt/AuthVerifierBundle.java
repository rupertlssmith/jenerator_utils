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
 * AuthVerifierBundle is a DropWizard bundle that will attempt to obtain the token verification details from an auth
 * service at application startup. It can be configured to retry this operation at a particular rate, and to timeout if
 * it fails to obtain the details for a particular period.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Obtain auth token verification details on application startup.
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class AuthVerifierBundle
{
}
