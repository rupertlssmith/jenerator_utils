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

import io.dropwizard.Configuration;
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
public abstract class InfinispanBundle<T extends Configuration> implements ConfiguredBundle<T>
{
    /** The infinispan cache manager. */
    private EmbeddedCacheManager cacheManager;

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
    public void run(T configuration, Environment environment)
    {
        InfinispanConfiguration infinispanConfiguration = getInfinispanConfiguration(configuration);

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
        return cacheManager;
    }

    /**
     * Should be implemented to extract the infinispan config from whatever configuration the application using this
     * bundle is using.
     *
     * @param  config The application configuration.
     *
     * @return The infinispan configuration.
     */
    protected abstract InfinispanConfiguration getInfinispanConfiguration(T config);

    /** Sets up a standalone cache for one JVM only. */
    private void configureStandaloneCache()
    {
        cacheManager = new DefaultCacheManager();
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

        cacheManager =
            new DefaultCacheManager(GlobalConfigurationBuilder.defaultClusteredBuilder().transport().defaultTransport()
                .clusterName(infinispanConfiguration.getClusterName()).addProperty("configurationFile", "jgroups.xml")
                .build(), new ConfigurationBuilder().clustering().cacheMode(CacheMode.REPL_SYNC).build());

        InfinispanManager managed = new InfinispanManager(cacheManager);
        environment.lifecycle().manage(managed);
    }
}
