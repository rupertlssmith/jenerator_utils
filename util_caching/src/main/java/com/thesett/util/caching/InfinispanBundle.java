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

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class InfinispanBundle implements ConfiguredBundle<InfinispanServiceConfiguration>
{
    private EmbeddedCacheManager defaultCacheManager;

    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    public void run(InfinispanServiceConfiguration configuration, Environment environment)
    {
        InfinispanConfiguration infinispanConfiguration = configuration.getInfinispanConfiguration();

        if (infinispanConfiguration.getType() == InfinispanConfiguration.CacheType.Clustered)
        {
            configureClusteredCache(environment, infinispanConfiguration);
        }
        else
        {
            configureStandaloneCache();
        }
    }

    public EmbeddedCacheManager getCacheManager()
    {
        return defaultCacheManager;
    }

    private void configureStandaloneCache()
    {
        defaultCacheManager = new DefaultCacheManager();
    }

    private void configureClusteredCache(Environment environment, InfinispanConfiguration infinispanConfiguration)
    {
        System.setProperty("jgroups.tcp.bind_addr", infinispanConfiguration.getBindAddress());
        System.setProperty("jgroups.tcp.port", String.valueOf(infinispanConfiguration.getPort()));
        System.setProperty("jgroups.tcpping.initial_hosts", infinispanConfiguration.getInitialHosts());

        defaultCacheManager =
            new DefaultCacheManager(GlobalConfigurationBuilder.defaultClusteredBuilder().transport().defaultTransport()
                .clusterName(infinispanConfiguration.getClusterName()).addProperty("configurationFile", "jgroups.xml")
                .build(), new ConfigurationBuilder().clustering().cacheMode(CacheMode.REPL_SYNC).build());

        ManagedCacheManager managed = new ManagedCacheManager(defaultCacheManager);
        environment.lifecycle().manage(managed);
    }
}
