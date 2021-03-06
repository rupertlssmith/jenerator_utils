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
 * AuthVerifierConfiguration defines the configuration for the {@link AuthVerifierBundle} including the retry frequency
 * and eventual timeout.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Define the retry rate and timeout for obtaining the verification details. </td></tr>
 * <tr><td> Specify the location of the auth service to use. </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class AuthVerifierConfiguration
{
    private int retryDelayMillis;
    private int timeoutSeconds;
    private String authServiceUrl;

    public int getRetryDelayMillis()
    {
        return retryDelayMillis;
    }

    public void setRetryDelayMillis(int retryDelayMillis)
    {
        this.retryDelayMillis = retryDelayMillis;
    }

    public int getTimeoutSeconds()
    {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds)
    {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getAuthServiceUrl()
    {
        return authServiceUrl;
    }

    public void setAuthServiceUrl(String authServiceUrl)
    {
        this.authServiceUrl = authServiceUrl;
    }
}
