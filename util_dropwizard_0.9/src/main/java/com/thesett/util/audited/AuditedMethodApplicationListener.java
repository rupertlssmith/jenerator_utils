package com.thesett.util.audited;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.ext.Provider;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import com.thesett.util.jersey.Audited;

@Provider
public class AuditedMethodApplicationListener implements ApplicationEventListener {
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(AuditedMethodApplicationListener.class.getName());

    private static final Set<String> REDACTED_HEADERS = ImmutableSet.of(HttpHeaders.AUTHORIZATION);

    private Map<Method, Audited> methodMap = new HashMap<>();

    public AuditedMethodApplicationListener() {
    }

    public void onEvent(ApplicationEvent event) {
        if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) {
            for (Resource resource : event.getResourceModel().getResources()) {
                for (ResourceMethod method : resource.getAllMethods()) {
                    registerAuditedMethodAnnotations(method);
                }

                for (Resource childResource : resource.getChildResources()) {
                    for (ResourceMethod method : childResource.getAllMethods()) {
                        registerAuditedMethodAnnotations(method);
                    }
                }
            }
        }
    }

    public RequestEventListener onRequest(RequestEvent event) {
        return new AuditedEventListener(methodMap);
    }

    private void registerAuditedMethodAnnotations(ResourceMethod method) {
        Audited annotation = method.getInvocable().getDefinitionMethod().getAnnotation(Audited.class);

        if (annotation == null) {
            annotation = method.getInvocable().getHandlingMethod().getAnnotation(Audited.class);
        }

        if (annotation != null) {
            this.methodMap.put(method.getInvocable().getDefinitionMethod(), annotation);
        }
    }

    private static class AuditedEventListener implements RequestEventListener {
        private final Map<Method, Audited> methodMap;

        private Audited Audited;

        private final boolean requireRemoteIPAddressInformation = false;

        public AuditedEventListener(Map<Method, Audited> methodMap) {
            this.methodMap = methodMap;
        }

        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                this.Audited =
                    this.methodMap.get(event.getUriInfo()
                        .getMatchedResourceMethod()
                        .getInvocable()
                        .getDefinitionMethod());

                if (Audited != null) {
                    logRequest(event);
                }
            } else if (event.getType() == RequestEvent.Type.RESP_FILTERS_START) {
            } else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
            }
        }

        /**
         * Logs a request method in extra detail.
         *
         * @param event The Jersey request event.
         */
        private void logRequest(RequestEvent event) {
            StringBuilder builder = new StringBuilder();

            ContainerRequest request = event.getContainerRequest();

            Resource resource = event.getUriInfo().getMatchedResourceMethod().getParent();

            builder.append("\n Audited Resource Access \n");
            builder.append("  Resource : " + resource.getClass() + "\n");

            if (requireRemoteIPAddressInformation &&
                    !request.getRequestHeaders().keySet().contains(HttpHeaders.X_FORWARDED_FOR)) {
                throw new RuntimeException("Header " + HttpHeaders.X_FORWARDED_FOR +
                    " is required but was not found in the request");
            }

            for (Map.Entry<String, List<String>> entry : request.getRequestHeaders().entrySet()) {
                if (!REDACTED_HEADERS.contains(entry.getKey())) {
                    builder.append("  Header   : " + entry.getKey() + " = " + entry.getValue() + "\n");
                }
            }

            builder.append("  Method   : " + request.getMethod() + "\n");
            builder.append("  URI      : " + request.getRequestUri() + "\n");

            for (Map.Entry<String, List<String>> entry : request.getUriInfo().getQueryParameters(true).entrySet()) {
                final String name = entry.getKey();
                final List<String> value = entry.getValue();
                builder.append("  Param    : " + name + " = " + value + " \n");
            }

            LOG.info(builder.toString());
        }
    }
}
