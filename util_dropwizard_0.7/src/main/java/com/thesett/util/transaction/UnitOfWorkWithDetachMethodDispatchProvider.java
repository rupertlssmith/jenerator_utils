package com.thesett.util.transaction;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import org.hibernate.SessionFactory;
import com.thesett.util.jersey.UnitOfWorkWithDetach;

public class UnitOfWorkWithDetachMethodDispatchProvider implements ResourceMethodDispatchProvider {
    private final ResourceMethodDispatchProvider provider;
    private final SessionFactory sessionFactory;

    public UnitOfWorkWithDetachMethodDispatchProvider(ResourceMethodDispatchProvider provider,
        SessionFactory sessionFactory) {
        this.provider = provider;
        this.sessionFactory = sessionFactory;
    }

    public ResourceMethodDispatchProvider getProvider() {
        return provider;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        final RequestDispatcher dispatcher = provider.create(abstractResourceMethod);
        final UnitOfWorkWithDetach unitOfWork =
            abstractResourceMethod.getMethod().getAnnotation(UnitOfWorkWithDetach.class);

        if (unitOfWork != null) {
            return new UnitOfWorkWithDetachRequestDispatcher(unitOfWork, dispatcher, sessionFactory);
        }

        return dispatcher;
    }
}
