package com.thesett.util.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * HibernateTransactionalProxy wraps all proxied method calls in a transaction on the current Hibernate session. If the
 * method throws a runtime exception, the transaction will be rolled back, otherwise it will be committed.
 *
 * <p/>This is useful when testing webservice or other service methods that run within transactions, without using a
 * transaction manager from the application container or Spring and so on.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Wrap all method calls in a Hibernate transaction. </td></tr>
 * </table></pre>
 */
public class HibernateDetachProxy implements InvocationHandler {
    /** The object being proxied. */
    private final Object obj;

    /**
     * Creates an instance of the standalone webservice test proxy.
     *
     * @param obj The object being proxied.
     */
    public HibernateDetachProxy(Object obj) {
        this.obj = obj;
    }

    /**
     * Creates an instance of this proxy.
     *
     * @param  toProxy The object to proxy.
     * @param  clazz   The class of the object being proxied.
     * @param  <T>     The type of the class being proxied.
     *
     * @return The object proxied to run within a transaction.
     */
    public static <T> T proxy(T toProxy, Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz },
            new HibernateDetachProxy(toProxy));
    }

    /** {@inheritDoc} */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Object result = method.invoke(obj, args);

            if (result != null) {
                HibernateDetachUtil.nullOutUninitializedFields(result, HibernateDetachUtil.FieldAccessType.Field);
            }

            return result;
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
