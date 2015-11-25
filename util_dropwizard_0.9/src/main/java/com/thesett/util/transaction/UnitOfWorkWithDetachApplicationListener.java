package com.thesett.util.transaction;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

import com.thesett.util.config.hibernate.HibernateXmlBundle;
import com.thesett.util.hibernate.HibernateDetachUtil;
import com.thesett.util.jersey.UnitOfWorkWithDetach;

@Provider
public class UnitOfWorkWithDetachApplicationListener implements ApplicationEventListener {
    private Map<Method, UnitOfWorkWithDetach> methodMap = new HashMap<>();
    private Map<String, SessionFactory> sessionFactories = new HashMap<>();

    public UnitOfWorkWithDetachApplicationListener() {
    }

    public UnitOfWorkWithDetachApplicationListener(String name, SessionFactory sessionFactory) {
        registerSessionFactory(name, sessionFactory);
    }

    public void registerSessionFactory(String name, SessionFactory sessionFactory) {
        sessionFactories.put(name, sessionFactory);
    }

    public void onEvent(ApplicationEvent event) {
        if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) {
            for (Resource resource : event.getResourceModel().getResources()) {
                for (ResourceMethod method : resource.getAllMethods()) {
                    registerUnitOfWorkWithDetachAnnotations(method);
                }

                for (Resource childResource : resource.getChildResources()) {
                    for (ResourceMethod method : childResource.getAllMethods()) {
                        registerUnitOfWorkWithDetachAnnotations(method);
                    }
                }
            }
        }
    }

    public RequestEventListener onRequest(RequestEvent event) {
        return new UnitOfWorkWithDetachEventListener(methodMap, sessionFactories);
    }

    private void registerUnitOfWorkWithDetachAnnotations(ResourceMethod method) {
        UnitOfWorkWithDetach annotation =
            method.getInvocable().getDefinitionMethod().getAnnotation(UnitOfWorkWithDetach.class);

        if (annotation == null) {
            annotation = method.getInvocable().getHandlingMethod().getAnnotation(UnitOfWorkWithDetach.class);
        }

        if (annotation != null) {
            this.methodMap.put(method.getInvocable().getDefinitionMethod(), annotation);
        }
    }

    private static class UnitOfWorkWithDetachEventListener implements RequestEventListener {
        private final Map<Method, UnitOfWorkWithDetach> methodMap;
        private final Map<String, SessionFactory> sessionFactories;

        private UnitOfWorkWithDetach UnitOfWorkWithDetach;
        private Session session;
        private SessionFactory sessionFactory;

        public UnitOfWorkWithDetachEventListener(Map<Method, UnitOfWorkWithDetach> methodMap,
            Map<String, SessionFactory> sessionFactories) {
            this.methodMap = methodMap;
            this.sessionFactories = sessionFactories;
        }

        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                this.UnitOfWorkWithDetach =
                    this.methodMap.get(event.getUriInfo()
                        .getMatchedResourceMethod()
                        .getInvocable()
                        .getDefinitionMethod());

                if (UnitOfWorkWithDetach != null) {
                    sessionFactory = sessionFactories.get(UnitOfWorkWithDetach.value());

                    if (sessionFactory == null) {
                        if (UnitOfWorkWithDetach.value().equals(HibernateXmlBundle.DEFAULT_NAME) &&
                                sessionFactories.size() == 1) {
                            sessionFactory = sessionFactories.values().iterator().next();
                        } else {
                            throw new IllegalArgumentException("Unregistered Hibernate bundle: '" +
                                UnitOfWorkWithDetach.value() + "'");
                        }
                    }

                    this.session = this.sessionFactory.openSession();

                    try {
                        configureSession();
                        ManagedSessionContext.bind(this.session);
                        beginTransaction();
                    } catch (Throwable th) {
                        this.session.close();
                        this.session = null;
                        ManagedSessionContext.unbind(this.sessionFactory);
                        throw th;
                    }
                }
            } else if (event.getType() == RequestEvent.Type.RESP_FILTERS_START) {
                if (this.session != null) {
                    try {
                        commitTransaction();
                    } catch (Exception e) {
                        rollbackTransaction();
                        throw new MappableException(e);
                    } finally {
                        this.session.close();
                        this.session = null;
                        ManagedSessionContext.unbind(this.sessionFactory);

                        Object result = event.getContainerResponse().getEntity();

                        if (result != null) {
                            HibernateDetachUtil.nullOutUninitializedFields(result,
                                HibernateDetachUtil.FieldAccessType.Field);
                        }
                    }
                }
            } else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                if (this.session != null) {
                    try {
                        rollbackTransaction();
                    } finally {
                        this.session.close();
                        this.session = null;
                        ManagedSessionContext.unbind(this.sessionFactory);
                    }
                }
            }
        }

        private void beginTransaction() {
            if (this.UnitOfWorkWithDetach.transactional()) {
                this.session.beginTransaction();
            }
        }

        private void configureSession() {
            this.session.setDefaultReadOnly(this.UnitOfWorkWithDetach.readOnly());
            this.session.setCacheMode(this.UnitOfWorkWithDetach.cacheMode());
            this.session.setFlushMode(this.UnitOfWorkWithDetach.flushMode());
        }

        private void rollbackTransaction() {
            if (this.UnitOfWorkWithDetach.transactional()) {
                final Transaction txn = this.session.getTransaction();

                if (txn != null && txn.isActive()) {
                    txn.rollback();
                }
            }
        }

        private void commitTransaction() {
            if (this.UnitOfWorkWithDetach.transactional()) {
                final Transaction txn = this.session.getTransaction();

                if (txn != null && txn.isActive()) {
                    txn.commit();
                }
            }
        }
    }
}
