package com.thesett.util.function;

/**
 * Function specifies the interface of a function from one type to another.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Provide a functional mapping from X to Y.
 * </table></pre>
 */
public interface Function<X, Y> {
    /**
     * Returns the result of type Y from applying this function to an argument of type X.
     *
     * @param  x The argument to the function.
     *
     * @return The result of applying the function to its argument.
     */
    Y apply(X x);
}
