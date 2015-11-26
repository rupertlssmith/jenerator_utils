package com.thesett.util.hibernate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

/**
 * ChainedInterceptor allows multiple Hibernate interceptors to be linked together in a chain, and for each of them to
 * be successively applied. Hibernate only supports one interceptor per session, and this chained interceptor allows
 * that limitation to be overcome.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Chain the application of multiple Hibernate interceptors together. </td></tr>
 * </table></pre>
 */
public class ChainedInterceptor extends EmptyInterceptor {
    private final List<Interceptor> chain = new LinkedList<>();

    public void addInterceptor(Interceptor interceptor) {
        chain.add(interceptor);
    }

    /** {@inheritDoc} */
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        for (Interceptor interceptor : chain) {
            interceptor.onDelete(entity, id, state, propertyNames, types);
        }
    }

    /** {@inheritDoc} */
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
        String[] propertyNames, Type[] types) {
        boolean result = false;

        for (Interceptor interceptor : chain) {
            result |= interceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        }

        return result;
    }

    /** {@inheritDoc} */
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        boolean result = false;

        for (Interceptor interceptor : chain) {
            result |= interceptor.onLoad(entity, id, state, propertyNames, types);
        }

        return result;
    }

    /** {@inheritDoc} */
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        boolean result = false;

        for (Interceptor interceptor : chain) {
            result |= interceptor.onSave(entity, id, state, propertyNames, types);
        }

        return result;
    }

    /** {@inheritDoc} */
    public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
        for (Interceptor interceptor : chain) {
            interceptor.onCollectionRemove(collection, key);
        }
    }

    /** {@inheritDoc} */
    public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
        for (Interceptor interceptor : chain) {
            interceptor.onCollectionRecreate(collection, key);
        }
    }

    /** {@inheritDoc} */
    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
        for (Interceptor interceptor : chain) {
            interceptor.onCollectionUpdate(collection, key);
        }
    }

    /** {@inheritDoc} */
    public void postFlush(Iterator entities) {
        for (Interceptor interceptor : chain) {
            interceptor.postFlush(entities);
        }
    }

    /** {@inheritDoc} */
    public void preFlush(Iterator entities) {
        for (Interceptor interceptor : chain) {
            interceptor.preFlush(entities);
        }
    }

    /** {@inheritDoc} */
    public void afterTransactionBegin(Transaction tx) {
        for (Interceptor interceptor : chain) {
            interceptor.afterTransactionBegin(tx);
        }
    }

    /** {@inheritDoc} */
    public void afterTransactionCompletion(Transaction tx) {
        for (Interceptor interceptor : chain) {
            interceptor.afterTransactionCompletion(tx);
        }
    }

    /** {@inheritDoc} */
    public void beforeTransactionCompletion(Transaction tx) {
        for (Interceptor interceptor : chain) {
            interceptor.beforeTransactionCompletion(tx);
        }
    }
}
