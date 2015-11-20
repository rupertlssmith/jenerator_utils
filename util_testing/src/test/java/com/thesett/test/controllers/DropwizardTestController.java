package com.thesett.test.controllers;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import net.sourceforge.argparse4j.inf.Namespace;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.component.LifeCycle;

/**
 * A controller for starting and stopping a DropWizard application under test.
 *
 * @param <C> the configuration type
 */
public class DropwizardTestController<C extends Configuration> {
    private final Class<? extends Application<C>> applicationClass;
    private final String configPath;
    private C configuration;
    private Application<C> application;
    private Environment environment;
    private Server jettyServer;

    public DropwizardTestController(Class<? extends Application<C>> applicationClass, String configPath) {
        this.applicationClass = applicationClass;
        this.configPath = configPath;

    }

    public void start() {
        startIfRequired();
    }

    public void stop() throws Exception {
        jettyServer.stop();

        List<LifeCycle> managedObjects = environment.lifecycle().getManagedObjects();

        for (LifeCycle lifeCycle : managedObjects) {
            lifeCycle.stop();
        }
    }

    public C getConfiguration() {
        return configuration;
    }

    public int getLocalPort() {
        return ((ServerConnector) jettyServer.getConnectors()[0]).getLocalPort();
    }

    public int getAdminPort() {
        return ((ServerConnector) jettyServer.getConnectors()[1]).getLocalPort();
    }

    public <A extends Application<C>> A getApplication() {
        return (A) application;
    }

    public Environment getEnvironment() {
        return environment;
    }

    protected Application<C> newApplication() {
        try {
            return applicationClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startIfRequired() {
        if (jettyServer != null) {
            return;
        }

        try {
            application = newApplication();

            final Bootstrap<C> bootstrap =
                new Bootstrap<C>(application) {
                    /** {@inheritDoc} */
                    public void run(C configuration, Environment environment) throws Exception {
                        environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
                                /** {@inheritDoc} */
                                public void serverStarted(Server server) {
                                    jettyServer = server;
                                }
                            });
                        DropwizardTestController.this.configuration = configuration;
                        DropwizardTestController.this.environment = environment;
                        super.run(configuration, environment);
                    }
                };

            application.initialize(bootstrap);

            final ServerCommand<C> command = new ServerCommand<>(application);

            ImmutableMap.Builder<String, Object> file = ImmutableMap.builder();

            if (!Strings.isNullOrEmpty(configPath)) {
                file.put("file", configPath);
            }

            final Namespace namespace = new Namespace(file.build());

            command.run(bootstrap, namespace);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
