package com.thesett.test.stack;

/**
 * KeySupplier supplies unique keys, which can be useful when creating test data.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Supply unique keys. </td></tr>
 * </table></pre>
 */
public interface KeySupplier<K> {
    /**
     * Supplies unique keys.
     *
     * @return A new unique key.
     */
    K createFreshKey();
}
