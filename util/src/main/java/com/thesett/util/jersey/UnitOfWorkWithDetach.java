package com.thesett.util.jersey;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;

/**
 * When annotating a Jersey resource method, wraps the method in a Hibernate session, and invokes
 * {@link com.thesett.util.hibernate.HibernateDetachUtil} on any return values.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Mark a method as running in a Hibernate transaction with auto-detach. </td></tr>
 * </table></pre>
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface UnitOfWorkWithDetach {
    /**
     * Iff <tt>true</tt>, the Hibernate session will default to loading read-only entities.
     *
     * @see org.hibernate.Session#setDefaultReadOnly(boolean)
     */
    boolean readOnly() default false;

    /**
     * Ifd <tt>true</tt>, a transaction will be automatically started before the resource method is invoked, committed
     * if the method returned, and rolled back if an exception was thrown.
     */
    boolean transactional() default true;

    /**
     * The {@link org.hibernate.CacheMode} for the session.
     *
     * @see org.hibernate.CacheMode
     * @see org.hibernate.Session#setCacheMode(org.hibernate.CacheMode)
     */
    CacheMode cacheMode() default CacheMode.NORMAL;

    /**
     * The {@link org.hibernate.FlushMode} for the session.
     *
     * @see org.hibernate.FlushMode
     * @see org.hibernate.Session#setFlushMode(org.hibernate.FlushMode)
     */
    FlushMode flushMode() default FlushMode.AUTO;
}
