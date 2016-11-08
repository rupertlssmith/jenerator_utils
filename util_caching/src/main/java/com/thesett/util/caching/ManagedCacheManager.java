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

import io.dropwizard.lifecycle.Managed;

import org.infinispan.manager.EmbeddedCacheManager;

/**
 * ManagedCacheManager wraps the infinispan cache manager as a DropWizard managed lifecycle. This only requires that the
 * {@link #start()} and {@link #stop()} methods are forwarded.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Present infinispan lifecycle as a DropWiazrd lifecycle. </td></tr>
 * </table></pre>
 */
public class ManagedCacheManager implements Managed
{
    /** The infinispan cache manager. */
    private final EmbeddedCacheManager defaultCacheManager;

    /**
     * Creates a DropWizard managed lifecycle from the infinispan cache manager.
     *
     * @param defaultCacheManager The infinispan cache manager.
     */
    public ManagedCacheManager(EmbeddedCacheManager defaultCacheManager)
    {
        this.defaultCacheManager = defaultCacheManager;
    }

    /** {@inheritDoc} */
    public void start() throws Exception
    {
        defaultCacheManager.start();
    }

    /** {@inheritDoc} */
    public void stop() throws Exception
    {
        defaultCacheManager.stop();
    }
}
