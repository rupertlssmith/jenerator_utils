package com.thesett.util.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.context.internal.ManagedSessionContext;

import com.thesett.util.hibernate.HibernateDetachUtil;

/**
 * HibernateSessionAndDetachProxy wraps all proxied method calls in a new transaction in a new Hibernate session. If the
 * method throws a runtime exception, the transaction will be rolled back, otherwise it will be committed.
 *
 * <p/>Additionally, all values returns from methods invoked through this proxy will be run through the
 * {@link HibernateDetachUtil} in order to completely detach them from the Hibernate session. Uninitialized values will
 * be nulled out.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Wrap all method calls in a new Hibernate session and transaction. </td></tr>
 * </table></pre>
 */
public class HibernateSessionAndDetachProxy implements InvocationHandler {
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(HibernateSessionAndDetachProxy.class.getName());

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
    public HibernateSessionAndDetachProxy(Object obj, SessionFactory sessionFactory) {
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
            new HibernateSessionAndDetachProxy(toProxy, sessionFactory));
    }

    /** {@inheritDoc} */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Session session = sessionFactory.openSession();
        Session oldSession = null;

        boolean applyTx = false;

        try {
            oldSession = ManagedSessionContext.bind(session);

            Transaction transaction = sessionFactory.getCurrentSession().getTransaction();

            applyTx = !transaction.isActive();

            if (applyTx) {
                transaction.begin();
            }

            Object result = method.invoke(obj, args);

            if (applyTx) {
                transaction.commit();
            }

            if (result != null) {
                HibernateDetachUtil.nullOutUninitializedFields(result, HibernateDetachUtil.FieldAccessType.Field);
            }

            return result;
        } catch (InvocationTargetException e) {
            if (applyTx) {
                if (e.getCause() instanceof RuntimeException) {
                    tryRollback();
                } else {
                    tryCommit();
                }
            }

            throw e.getCause();
        } catch (Throwable e) {
            if (applyTx) {
                tryRollback();
            }

            throw e;
        } finally {
            session.close();
            ManagedSessionContext.unbind(sessionFactory);

            // Put the previous session back again, iff there was one.
            if (oldSession != null) {
                ManagedSessionContext.bind(oldSession);
            }
        }
    }

    /**
     * Tries to commit the current Hibernate transaction, logging a SEVERE error if it should fail. Logging is used
     * rather than re-throwing the exception, as this should only be used in a catch or finally block and should not
     * mask the original exception.
     */
    private void tryCommit() {
        try {
            sessionFactory.getCurrentSession().getTransaction().commit();
        } catch (TransactionException e1) {
            LOG.log(Level.SEVERE, "Failed to commit Hibernate transaction on checked exception.", e1);
        }
    }

    /**
     * Tries to rollback the current Hibernate transaction, logging a SEVERE error if it should fail. Logging is used
     * rather than re-throwing the exception, as this should only be used in a catch or finally block and should not
     * mask the original exception.
     */
    private void tryRollback() {
        try {
            sessionFactory.getCurrentSession().getTransaction().rollback();
        } catch (TransactionException e1) {
            LOG.log(Level.SEVERE, "Failed to rollback Hibernate transaction on runtime exception.", e1);
        }
    }
}
