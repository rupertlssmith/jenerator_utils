package com.thesett.util.function;

import java.util.LinkedList;
import java.util.List;

/**
 * Functions provides util methods using {@link Function}s.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Map a function to a list. </td></tr>
 * </table></pre>
 */
public class Functions {
    /**
     * Maps a function over a list. In this variant the applications of the function are appended onto the end of
     * 'outList'.
     *
     * @param  list    The source list.
     * @param  f       The function.
     * @param  outList The output list to add results to.
     * @param  <X>     The type of items in the input list.
     * @param  <Y>     The type of items in the result list.
     *
     * @return A list consisting of the function applied to each element of the input list.
     */
    public static <X, Y> void map(List<X> list, Function<X, Y> f, List<Y> outList) {
        for (X x : list) {
            outList.add(f.apply(x));
        }
    }

    /**
     * Maps a function over a list.
     *
     * @param  list The source list.
     * @param  f    The function.
     * @param  <X>  The type of items in the input list.
     * @param  <Y>  The type of items in the result list.
     *
     * @return A list consisting of the function applied to each element of the input list.
     */
    public static <X, Y> List<Y> map(List<X> list, Function<X, Y> f) {
        List<Y> result = new LinkedList<>();

        map(list, f, result);

        return result;
    }

    public static <X, Y, Z> Function<X, Y> chain(Function<X, Z> first, Function<Z, Y> second) {
        return new ChainFunction<>(second, first);
    }

    private static class ChainFunction<X, Y, Z> implements Function<X, Y> {
        private final Function<Z, Y> second;
        private final Function<X, Z> first;

        public ChainFunction(Function<Z, Y> second, Function<X, Z> first) {
            this.second = second;
            this.first = first;
        }

        public Y apply(X x) {
            return second.apply(first.apply(x));
        }
    }
}
