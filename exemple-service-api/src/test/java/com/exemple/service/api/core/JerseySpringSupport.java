package com.exemple.service.api.core;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.web.WebAppConfiguration;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;

@WebAppConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class JerseySpringSupport {

    private JerseyTest jerseyTest;

    public final WebTarget target(final String path) {
        return jerseyTest.target(path);
    }

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    public void setup() throws Exception {

        jerseyTest = new JerseyTest() {

            @Override
            protected Application configure() {
                forceSet(TestProperties.CONTAINER_PORT, "0");
                ResourceConfig application = JerseySpringSupport.this.configure();
                application.property("contextConfig", context);

                return application;
            }

            @Override
            protected void configureClient(ClientConfig config) {
                config.connectorProvider(new ApacheConnectorProvider());
            }
        };

        jerseyTest.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jerseyTest.tearDown();
    }

    protected abstract ResourceConfig configure();

}
