package com.exemple.service.api.core;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@SpringJUnitConfig(ApiTestConfiguration.class)
@ActiveProfiles("noSecurity")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class JerseySpringSupport {

    private JerseyTest jerseyTest;

    public final WebTarget target(final String path) {
        return jerseyTest.target(path);
    }

    @Autowired
    private ApplicationContext context;

    @BeforeAll
    public void setup() throws Exception {

        jerseyTest = new JerseyTest() {

            @Override
            protected Application configure() {
                forceSet(TestProperties.CONTAINER_PORT, "0");
                ResourceConfig application = JerseySpringSupport.this.configure();
                application.property("contextConfig", context);

                return application;
            }
        };

        jerseyTest.setUp();
    }

    @AfterAll
    public void tearDown() throws Exception {
        jerseyTest.tearDown();
    }

    protected abstract ResourceConfig configure();

}
