package com.thesett.util.function;

/**
 * ConstantFunction is a degenerate function that always returns a constant value.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Supply a constant value.  </td></tr>
 * </table></pre>
 */
public class ConstantFunction<X, Y> implements Function<X, Y> {
    private final Y value;

    public ConstantFunction(Y value) {
        this.value = value;
    }

    /** {@inheritDoc} */
    public Y apply(X x) {
        return value;
    }
}
