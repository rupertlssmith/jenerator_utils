package com.thesett.util.queue;

import com.thesett.util.function.Function;

/**
 * MappedSource applies a mapping function to a {@link Source} to produce a new Source where every member has been
 * passed through the mapping function.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Apply a function to a source. </td></tr>
 * </table></pre>
 */
public class MappedSource<A, B> implements Source<B> {
    /** The mapping function. */
    private final Function<A, B> mapper;

    /** The underlying source. */
    private final Source<A> source;

    /**
     * Builds a mapped source on the specified source using the mapping function.
     *
     * @param mapper The mapping function.
     * @param source The underlying source.
     */
    public MappedSource(Function<A, B> mapper, Source<A> source) {
        this.mapper = mapper;
        this.source = source;
    }

    /** {@inheritDoc} */
    public B poll() {
        A sourcePoll = source.poll();

        if (sourcePoll != null) {
            return mapper.apply(sourcePoll);
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public B peek() {
        A sourcePeek = source.peek();

        if (sourcePeek != null) {
            return mapper.apply(sourcePeek);
        } else {
            return null;
        }
    }
}
