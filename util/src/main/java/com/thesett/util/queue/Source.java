package com.thesett.util.queue;

/**
 * A Source is the point at which a consumer may take items from a queue. This interface is compatable with
 * java.util.Queue, in that any Queue implementation can also implement this interface with no further work. Source has
 * been created as a seperate interface, to inroduce the possibility of a data structure that only exposes its Source,
 * and hides the remainder of its operations.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Take elements from a queue.
 * </table></pre>
 */
public interface Source<E> {
    /**
     * Retrieves and removes the head of this queue, or <tt>null</tt> if this queue is empty.
     *
     * @return The head of this queue, or <tt>null</tt> if this queue is empty.
     */
    E poll();

    /**
     * Retrieves, but does not remove, the head of this queue, returning <tt>null</tt> if this queue is empty.
     *
     * @return The head of this queue, or <tt>null</tt> if this queue is empty.
     */
    E peek();
}
