package com.thesett.test.stack;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.thesett.util.entity.Entity;

/**
 * TestDataSupplierBase provides a base implementation of a {@link CRUDTestDataSupplier}. It provides fields for the
 * initial and updated values, and will set ids on these entities as requested to by the test stack.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Synch ids on initial and updated value. </td></tr>
 * </table></pre>
 */
public abstract class TestDataSupplierBase<E extends Entity<K>, K extends Serializable>
    implements CRUDTestDataSupplier<E, K> {
    /** Holds the initial value for testing. */
    protected E initialValue;

    /** Holds the modified value for testing. */
    protected E updatedValue;

    /** {@inheritDoc} */
    public void setId(K id) {
        initialValue.setId(id);
        updatedValue.setId(id);
    }

    /** {@inheritDoc} */
    public E getInitialValue() {
        return initialValue;
    }

    /** {@inheritDoc} */
    public E getUpdatedValue() {
        return updatedValue;
    }

    /** {@inheritDoc} */
    public List<E> examples() {
        return new LinkedList<>();
    }

    /** {@inheritDoc} */
    public List<E> counterExamples() {
        return new LinkedList<>();
    }
}
