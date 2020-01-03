package com.exemple.service.api.core;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

@WebAppConfiguration
@ContextConfiguration(classes = { ApiTestConfiguration.class })
@ActiveProfiles("noSecurity")
public abstract class JerseySpringSupport extends AbstractTestNGSpringContextTests {

    private JerseyTestNg.ContainerPerClassTest jerseyTest;

    public final WebTarget target(final String path) {
        return jerseyTest.target(path);
    }

    @Autowired
    private ApplicationContext context;

    @BeforeClass
    public void setup() throws Exception {

        jerseyTest = new JerseyTestNg.ContainerPerClassTest() {

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

    @AfterClass
    public void tearDown() throws Exception {
        jerseyTest.tearDown();
    }

    protected abstract ResourceConfig configure();

}
