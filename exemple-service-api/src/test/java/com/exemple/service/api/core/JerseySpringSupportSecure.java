package com.exemple.service.api.core;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;

@ContextConfiguration(classes = AuthorizationTestConfiguration.class)
@ActiveProfiles(value = "AuthorizationMock", inheritProfiles = false)
public abstract class JerseySpringSupportSecure extends JerseySpringSupport {

}
