package com.thesett.util.model;

/**
 * Pair implements a simple tuple of data elements. It is suitable for use with Collections and as they key in Maps, as
 * it implements equality and hash code based on the equality and hash code of the items making up the pair.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Hold a pair of data elements.
 * </table></pre>
 *
 * @param  <A> The type of the first element.
 * @param  <B> The type of the second element.
 *
 * @author Rupert Smith
 */
public class Pair<A, B> {
    /** Holds the first data element. */
    private A first;

    /** Holds the second data element. */
    private B second;

    /**
     * Creates a pair of data elements.
     *
     * @param first  The first element of the tuple.
     * @param second The second element of the tuple.
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Provides the first element of the tuple.
     *
     * @return The first element of the tuple.
     */
    public A getFirst() {
        return first;
    }

    /**
     * Sets the first element if the tuple.
     *
     * @param first The first element of the tuple.
     */
    public void setFirst(A first) {
        this.first = first;
    }

    /**
     * Provides the second element of the tuple.
     *
     * @return The second element of the tuple.
     */
    public B getSecond() {
        return second;
    }

    /**
     * Sets the second element if the tuple.
     *
     * @param second The second element of the tuple.
     */
    public void setSecond(B second) {
        this.second = second;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Equal if the items in the pair to compare too are equal, or null in this pair and the comparator.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Pair)) {
            return false;
        }

        Pair pair = (Pair) o;

        if (first != null ? !first.equals(pair.first) : pair.first != null) {
            return false;
        }

        if (second != null ? !second.equals(pair.second) : pair.second != null) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);

        return result;
    }
}
