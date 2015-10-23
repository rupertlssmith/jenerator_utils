package com.thesett.test.stack;

import java.util.concurrent.atomic.AtomicLong;

import com.thesett.util.entity.Entity;

/**
 * TestDataSupplierLongKey extends the {@link TestDataSupplierBase} to implement a data supplier that works with long
 * keys, which is typical for the majority of database tables.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Supply fresh long keys. </td></tr>
 * </table></pre>
 */
public abstract class TestDataSupplierLongKey<E extends Entity<Long>> extends TestDataSupplierBase<E, Long> {
    /** The unique key generator. */
    private static final AtomicLong ID_GEN = new AtomicLong();

    /** {@inheritDoc} */
    public Long createFreshKey() {
        return ID_GEN.getAndIncrement();
    }
}
