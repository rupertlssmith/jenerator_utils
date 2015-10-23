package com.thesett.util.function;

/**
 * ToStringFunction implements a function that invokes toString().
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Apply toString() to an object. </td></tr>
 * </table></pre>
 */
public class ToStringFunction implements Function<Object, String> {
    /** {@inheritDoc} */
    public String apply(Object o) {
        return o.toString();
    }
}
