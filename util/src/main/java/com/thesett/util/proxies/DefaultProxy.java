package com.thesett.util.proxies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * DefaultProxy is a default dynamic proxy implementation to coerce any class to any interface. It only works when the
 * class has a method that exactly matches the interface; otherwise, InvocationTargetException.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Default coercion to any interface. </td></tr>
 * </table></pre>
 */
public class DefaultProxy implements InvocationHandler {
    /** The object being proxied. */
    private final Object obj;

    /**
     * Creates an instance of the standalone webservice test proxy.
     *
     * @param obj The object being proxied.
     */
    public DefaultProxy(Object obj) {
        this.obj = obj;
    }

    /** {@inheritDoc} */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
