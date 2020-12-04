package com.exemple.service.api.core;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;

@ContextConfiguration(classes = AuthorizationTestConfiguration.class)
@ActiveProfiles(inheritProfiles = false)
public abstract class JerseySpringSupportSecure extends JerseySpringSupport {

    static {
        // System.setProperty("mockserver.logLevel", "DEBUG");
    }

    @Value("${api.authorization.port}")
    private int authorizationPort;

    private ClientAndServer authorizationServer;

    protected MockServerClient authorizationClient;

    @BeforeClass
    public final void authorizationServer() {
        this.authorizationServer = ClientAndServer.startClientAndServer(authorizationPort);
        this.authorizationClient = new MockServerClient("localhost", authorizationPort);
    }

    @AfterClass
    public final void closeMockServer() {

        this.authorizationServer.close();
        this.authorizationServer.hasStopped();
    }

}
