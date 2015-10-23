package com.thesett.util.queue;

/**
 * A Sink is the point at which a producer may add items into a queue. This interface is compatable with
 * java.util.Queue, in that any Queue implementation can also implement this interface with no further work. Sink has
 * been created as a seperate interface, to inroduce the possibility of a data structure that only exposes its Sink, and
 * hides the remainder of its operations.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Add elements to a queue.
 * </table></pre>
 */
public interface Sink<E> {
    /**
     * Inserts the specified element into this queue, if possible. When using queues that may impose insertion
     * restrictions (for example capacity bounds), method <tt>offer</tt> is generally preferable to method
     * {@link java.util.Collection#add}, which can fail to insert an element only by throwing an exception.
     *
     * @param  o T The element to insert.
     *
     * @return <tt>true</tt> if it was possible to add the element to this queue, else <tt>false</tt>.
     */
    boolean offer(E o);
}
