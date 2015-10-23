package com.thesett.util.queue;

import java.util.LinkedList;

/**
 * FifoLinkedQeue implements a Queue with LIFO (or stack) ordering.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Add elements to a queue. </td></tr>
 * <tr><td> Take elements from a queue. </td></tr>
 * </table></pre>
 */
public class LifoLinkedQueue<E> extends LinkedList<E> implements Queue<E> {
    /** {@inheritDoc} */
    public boolean offer(E o) {
        super.add(0, o);

        return true;
    }

    /** {@inheritDoc} */
    public boolean add(E e) {
        super.add(0, e);

        return true;
    }
}
