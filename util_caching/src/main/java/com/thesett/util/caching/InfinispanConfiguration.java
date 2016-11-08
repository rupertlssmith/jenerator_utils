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

/**
 * Defines an interface that any configuration providing settings for an infinispan cache can implement, to provide
 * those settings.
 */
public interface InfinispanConfiguration
{
    /**
     * Provides the infinispan configuration settings.
     *
     * @return The infinispan configuration settings.
     */
    InfinispanConfigurationImpl getInfinispanConfiguration();
}
