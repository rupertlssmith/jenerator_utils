package com.thesett.util.merge;

/**
 * Merge defines a merge operation between objects. This is intended to be used to more sensitively update an existing
 * object with new data. New data will override existing data, except in the case where the new data object contains
 * <tt>null</tt> entries for some of its fields; a <tt>null</tt> should never overwrite non-null values.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Update state without obliterating existing values with nulls. </td></tr>
 * </table></pre>
 */
public interface Merge<T> {
    /**
     * Merges data from an 'update' object onto an 'original' object. The fields of the update object are copied over
     * the values in the original, unless they are <tt>null</tt>, in which case the original values are left alone.
     *
     * <p/>Note that the 'original' will be altered by this action, but 'update' will not be.
     *
     * @param  original The original value to update.
     * @param  update   The update value to replace with.
     *
     * @return The original value over-written with non-null fields from the update.
     */
    T merge(T original, T update);
}
