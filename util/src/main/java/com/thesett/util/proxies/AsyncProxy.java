package com.thesett.util.proxies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AsyncProxy is a proxy that makes any method call asynchronous.
 *
 * <p/>Return values and exceptions are awkward, because they cannot be returned to the caller in the normal way. A
 * mechanism could be added to allow these to be collected at a later time, but this has not been implemented. Return
 * values and exceptions are ignored.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Invoke methods asynchronously. </td></tr>
 * </table></pre>
 */
public class AsyncProxy implements InvocationHandler {
    /** The object being proxied. */
    private final Object obj;

    ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * Creates an instance of the asynchronous proxy.
     *
     * @param obj The object being proxied.
     */
    public AsyncProxy(Object obj) {
        this.obj = obj;
    }

    /** {@inheritDoc} */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        executor.submit(new AsyncMethodCall(method, args, obj));

        return null;
    }

    private class AsyncMethodCall implements Runnable {
        Method method;
        Object[] args;
        Object target;

        private AsyncMethodCall(Method method, Object[] args, Object target) {
            this.method = method;
            this.args = args;
            this.target = target;
        }

        public void run() {
            try {
                method.invoke(target, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
