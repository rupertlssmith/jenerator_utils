package com.thesett.util.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class ViewsBaseDAO<E> {
    /** The Hibernate session factory. */
    protected final SessionFactory sessionFactory;

    public ViewsBaseDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Returns the current {@link org.hibernate.Session}.
     *
     * @return the current session
     */
    protected Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Applies a query and returns the results of it in a list.
     *
     * @param  query The query to apply.
     *
     * @return A list of query results.
     */
    protected List<E> list(Query query) {
        return checkNotNull(query).list();
    }

    /**
     * Applies a query that is expected to find at most one match.
     *
     * @param  query The query to apply.
     *
     * @return The single match, or <tt>null</tt> if none is found.
     */
    protected E findOne(Query query) {
        return (E) checkNotNull(query).uniqueResult();
    }

    /**
     * Retrieves a named query.
     *
     * @param  queryName The name of the query to retrieve.
     *
     * @return The matching query.
     */
    protected Query namedQuery(String queryName) {
        return currentSession().getNamedQuery(queryName);
    }

    protected <O> O checkNotNull(O object) {
        if (object == null) {
            throw new IllegalArgumentException();
        }

        return object;
    }
}
