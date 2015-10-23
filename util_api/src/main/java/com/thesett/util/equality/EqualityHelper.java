package com.thesett.util.equality;

/**
 * EqualityHelper provides some helper methods for implementing equals and hashCode methods on objects.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide a null safe hash code delegator. </td></tr>
 * <tr><td> Provide a null safe object field equality delegator. </td></tr>
 * </table></pre>
 */
public class EqualityHelper {
    /** Private constructor to prevent instantiation of utility class. */
    private EqualityHelper() {
    }

    /**
     * Compares two objects to see if they are equal by their equals methods, or they are equal because they are both
     * null.
     *
     * @param  o1 The first object.
     * @param  o2 The second object.
     *
     * @return <tt>true</tt> iff the objects are both null, or <tt>o1.equals(o2)</tt>.
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    /**
     * Computes a hash code of an object or returns <tt>0</tt> if the object is null.
     *
     * @param  o The object to compute the hash of.
     *
     * @return The objects hash code of <tt>0</tt> if it is null.
     */
    public static int nullSafeHashCode(Object o) {
        return o == null ? 0 : o.hashCode();
    }
}
