package com.exemple.service.customer.core.script;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;
import java.util.UUID;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceException;
import com.exemple.service.customer.core.CustomerScriptConfiguration;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;

@ContextHierarchy({ @ContextConfiguration(classes = CustomerTestConfiguration.class),
        @ContextConfiguration(classes = CustomerScriptConfiguration.class) })
public class CustomerScriptFactoryTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AccountService service;

    @Autowired
    private AccountResource resource;

    @BeforeClass
    private void initServiceContextExecution() {

        ServiceContext context = ServiceContextExecution.context();
        context.setApp("test");
    }

    @AfterClass
    private void resetServiceContextExecution() {

        ServiceContext context = ServiceContextExecution.context();
        context.setApp("default");
    }

    @BeforeMethod
    private void before() {

        Mockito.reset(resource);

        ServiceContext context = ServiceContextExecution.context();
        context.setApp("test");
    }

    @Test
    public void save() throws AccountServiceException {

        JsonNode model = JsonNodeUtils.create(Collections.singletonMap("TEST_KEY", "TEST_VALUE"));

        Mockito.when(resource.save(Mockito.eq(model))).thenReturn(UUID.randomUUID());

        JsonNode account = service.save(JsonNodeUtils.init());
        assertThat(account, is(notNullValue()));

    }
}
