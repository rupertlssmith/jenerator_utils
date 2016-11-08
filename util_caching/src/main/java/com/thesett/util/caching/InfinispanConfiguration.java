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
package com.thesett.util.caching;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class InfinispanConfiguration
{
    public enum CacheType
    {
        Standalone, Clustered
    }

    @JsonProperty
    @Valid
    private Optional<String> bindAddress;

    @JsonProperty
    private int port = 7800;

    @JsonProperty
    @Valid
    private Optional<String> initialHosts;

    @JsonProperty
    @Valid
    private Optional<String> clusterName;

    @JsonProperty
    @NotNull
    private CacheType type;

    public String getBindAddress()
    {
        return bindAddress.get();
    }

    public int getPort()
    {
        return port;
    }

    public String getInitialHosts()
    {
        return initialHosts.get();
    }

    public String getClusterName()
    {
        return clusterName.get();
    }

    public CacheType getType()
    {
        return type;
    }

    public boolean isValid()
    {
        switch (getType())
        {
        case Clustered:
            return clusterName.isPresent() && bindAddress.isPresent() && initialHosts.isPresent();

        case Standalone:
            return true;
        }

        return false;
    }
}
