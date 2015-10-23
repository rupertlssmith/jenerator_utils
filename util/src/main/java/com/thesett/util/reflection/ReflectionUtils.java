package com.thesett.util.reflection;

/**
 * ReflectionUtils provides some helper methods for working with reflection.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Obtain a class by name. </td></tr>
 * </table></pre>
 */
public class ReflectionUtils {
    /** Private constructor to prevent instantiation of utility class. */
    private ReflectionUtils() {
    }

    /**
     * Provides the class for a fully qualified class name.
     *
     * @param  className The fully qualified class name.
     *
     * @return The corresponding class.
     */
    public static Class getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("'className' " + className + " cannot be found.", e);
        }
    }

    /**
     * Creates an instance of a Class, instantiated through its no-args constructor.
     *
     * @param  cls The Class to instantiate.
     * @param  <T> The Class type.
     *
     * @return An instance of the class.
     */
    public static <T> T newInstance(Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("InstantiationException whilst instantiating class.", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("IllegalAccessException whilst instantiating class.", e);
        }
    }
}
