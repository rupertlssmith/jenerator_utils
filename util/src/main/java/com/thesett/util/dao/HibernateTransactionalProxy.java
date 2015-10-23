package com.thesett.util.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.hibernate.SessionFactory;

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
public class HibernateTransactionalProxy implements InvocationHandler {
    /** The Hibernate session factory. */
    private final SessionFactory sessionFactory;

    /** The object being proxied. */
    private final Object obj;

    /**
     * Creates an instance of the standalone webservice test proxy.
     *
     * @param obj            The object being proxied.
     * @param sessionFactory The Hibernate session factory.
     */
    public HibernateTransactionalProxy(Object obj, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.obj = obj;
    }

    /**
     * Creates an instance of this proxy.
     *
     * @param  toProxy        The object to proxy.
     * @param  clazz          The class of the object being proxied.
     * @param  sessionFactory A Hibernate session factory.
     * @param  <T>            The type of the class being proxied.
     *
     * @return The object proxied to run within a transaction.
     */
    public static <T> T proxy(T toProxy, Class<T> clazz, SessionFactory sessionFactory) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz },
            new HibernateTransactionalProxy(toProxy, sessionFactory));
    }

    /** {@inheritDoc} */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            sessionFactory.getCurrentSession().getTransaction().begin();

            Object result = method.invoke(obj, args);

            sessionFactory.getCurrentSession().getTransaction().commit();

            return result;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                sessionFactory.getCurrentSession().getTransaction().rollback();
            } else {
                sessionFactory.getCurrentSession().getTransaction().commit();
            }

            throw e.getCause();
        } catch (Throwable e) {
            sessionFactory.getCurrentSession().getTransaction().rollback();

            throw e;
        }
    }
}
