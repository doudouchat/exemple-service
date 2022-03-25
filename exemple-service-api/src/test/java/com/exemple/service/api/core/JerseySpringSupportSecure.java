package com.exemple.service.api.core;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;

@SpringJUnitConfig(AuthorizationTestConfiguration.class)
@ActiveProfiles(value = "AuthorizationMock", inheritProfiles = false)
public abstract class JerseySpringSupportSecure extends JerseySpringSupport {

}
