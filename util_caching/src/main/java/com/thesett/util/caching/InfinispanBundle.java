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

/**
 * InfinispanBundle defines a DropWizard bundle for using an infinispan cache.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Create single JVM caches. </td></tr>
 * <tr><td> Create clustered caches. </td></tr>
 * </table></pre>
 */
public class InfinispanBundle implements ConfiguredBundle<InfinispanServiceConfiguration>
{
    /** The infinispan cache manager. */
    private EmbeddedCacheManager defaultCacheManager;

    /**
     * {@inheritDoc}
     *
     * <p/>Does nothing.
     */
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Starts the infinispan cache.
     */
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

    /**
     * Supplies a configured infinispan cache manager.
     *
     * @return A configured infinispan cache manager.
     */
    public EmbeddedCacheManager getCacheManager()
    {
        return defaultCacheManager;
    }

    /** Sets up a standalone cache for one JVM only. */
    private void configureStandaloneCache()
    {
        defaultCacheManager = new DefaultCacheManager();
    }

    /**
     * Sets up a clustered cache to span multiple JVMs.
     *
     * @param environment             The DropWizard environment.
     * @param infinispanConfiguration The infinispan configuration.
     */
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
