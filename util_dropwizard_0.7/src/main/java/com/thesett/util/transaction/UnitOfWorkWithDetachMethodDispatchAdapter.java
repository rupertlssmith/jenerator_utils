package com.thesett.util.transaction;

import javax.ws.rs.ext.Provider;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;

import org.hibernate.SessionFactory;

@Provider
public class UnitOfWorkWithDetachMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private final SessionFactory sessionFactory;

    public UnitOfWorkWithDetachMethodDispatchAdapter(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new UnitOfWorkWithDetachMethodDispatchProvider(provider, sessionFactory);
    }
}
