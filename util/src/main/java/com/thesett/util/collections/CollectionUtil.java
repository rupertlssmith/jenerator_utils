package com.thesett.util.collections;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * CollectionUtil provides helper methods for working with data structures.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Turn a list of parameters into a set. </td></tr>
 * </table></pre>
 */
public class CollectionUtil {
    /** Private constructor to prevent instantiation of utility class. */
    private CollectionUtil() {
    }

    /**
     * Turns a list of vararg parameters into a set.
     *
     * @param  ts  The vararg parameters.
     * @param  <T> The type of the parameters.
     *
     * @return A set created by adding the parameters in the order presented into a set.
     */
    public static <T> Set<T> paramsToSet(T... ts) {
        Set<T> result = new HashSet<>();

        Collections.addAll(result, ts);

        return result;
    }

    /**
     * Takes up to n items from the head of a list, and returns them in another list. If the input list has less items,
     * then they will all be returned.
     *
     * @param  list The list to take from.
     * @param  num  The number of elements to take.
     * @param  <T>  The type of the list elements.
     *
     * @return A list with up to n items from the input list.
     */
    public static <T> List<T> topN(List<T> list, int num) {
        List<T> exemplars = new LinkedList<>();

        Iterator<T> iterator = list.iterator();

        for (int i = 0; i < num; i++) {
            if (iterator.hasNext()) {
                T examplar = iterator.next();
                exemplars.add(examplar);
            } else {
                break;
            }
        }

        return exemplars;
    }

    /**
     * Provides the first item from a list, unless the list is <tt>null</tt> or empty, in which case <tt>null</tt> is
     * returned. This is useful because the behaviour of List.get(0) is to throw an exception is the list is empty.
     *
     * @param  list The list to take the first item from.
     * @param  <T>  The type of the items in the list.
     *
     * @return The first item from a list, unless the list is <tt>null</tt> or empty, in which case <tt>null</tt> is
     *         returned.
     */
    public static <T> T first(List<T> list) {
        return list.isEmpty() || list == null ? null : list.get(0);
    }
}
