package com.thesett.util.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * CacheUtils provides some helper methods when working with caches.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Locate or create inner caches in various data types. </td></tr>
 * </table></pre>
 */
public class CacheUtils {
    /**
     * Working with a Map containing caches (HashMaps), this method attempts to locate a cache by its key. If no match
     * is found a new cache (HashMap) is created an inserted against the key. This is a frequently used operation when
     * building caches.
     *
     * @param  mapKey   The key of the cache within the map.
     * @param  outerMap The map containing the keyed cache.
     * @param  <K1>     The key type of the cache in the outer map.
     * @param  <K2>     The key type of the inner cache.
     * @param  <V>      The value type of the inner cache.
     *
     * @return A cache <tt>(HashMap&lt;K2, V&gt;)</tt>, within the outer map.
     */
    public static <K1, K2, V> Map<K2, V> getOrCreateCacheInMap(K1 mapKey, Map<K1, Map<K2, V>> outerMap) {
        Map<K2, V> orgsById = outerMap.get(mapKey);

        if (orgsById == null) {
            orgsById = new HashMap<>();
        }

        return orgsById;
    }
}
