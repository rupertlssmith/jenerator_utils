package com.thesett.util.function;

/**
 * ArraySelectFunction implements a function that selects a single item from an array.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Select an item from an array. </td></tr>
 * </table></pre>
 */
public class ArraySelectFunction implements Function<Object[], Object> {
    /** The array index to select. */
    private final int index;

    /**
     * Creates an array selection function for the given index.
     *
     * @param index The array index to select.
     */
    public ArraySelectFunction(int index) {
        this.index = index;
    }

    /** {@inheritDoc} */
    public Object apply(Object[] objects) {
        return objects[index];
    }
}
