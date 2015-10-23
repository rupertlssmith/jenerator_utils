package com.thesett.util.client;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.IOUtils;
import com.thesett.util.entity.EntityValidationException;

/**
 * WebExceptionCodeClientProxy is a client side proxy, that can be used to wrap any javax.ws.rs REST client. javax.ws.rs
 * REST clients will translate certain HTTP response codes into runtime exceptions. As these response codes have been
 * selected to convey certain meanings in terms of how the proxied API operates, this proxy exists to translate some of
 * the runtime exceptions back into checked business exceptions, depending on the response code.
 *
 * <p/>This is effectively the inverse of a javax.ws.rs.ExceptionMapper.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class WebExceptionCodeClientProxy implements InvocationHandler {
    /** The object being proxied. */
    private final Object obj;

    /**
     * Creates the web exception proxy.
     *
     * @param obj The object being proxied.
     */
    public WebExceptionCodeClientProxy(Object obj) {
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
            new WebExceptionCodeClientProxy(toProxy));
    }

    /** {@inheritDoc} */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            try {
                return method.invoke(obj, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        } catch (WebApplicationException we) {
            int status = we.getResponse().getStatus();

            if (status == 422) {
                Object entity = we.getResponse().getEntity();
                String message = "";

                if (entity instanceof InputStream) {
                    message = IOUtils.toString((InputStream) entity, "UTF-8");
                }

                throw new EntityValidationException(message);
            }

            throw we;
        }
    }
}
