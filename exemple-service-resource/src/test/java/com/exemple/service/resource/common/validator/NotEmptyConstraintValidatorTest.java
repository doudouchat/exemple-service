package com.exemple.service.resource.common.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.account.model.Account;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class NotEmptyConstraintValidatorTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AccountResource resource;

    @AfterClass
    public void executionContextDestroy() {

        ResourceExecutionContext.destroy();

        ResourceExecutionContext.get().setKeyspace("test");
    }

    @Test
    public void updateSuccess() {

        Account model = new Account();
        model.setEmail("jean.dupont@gmail.com");

        UUID id = UUID.randomUUID();

        JsonNode account = resource.update(id, JsonNodeUtils.create(model));
        assertThat(account.get("email"), is(notNullValue()));

    }

    @DataProvider(name = "failures")
    public static Object[][] failures() {

        return new Object[][] {
                // null
                { null },
                // empty
                { JsonNodeUtils.init() } };

    }

    @Test(dataProvider = "failures", expectedExceptions = ConstraintViolationException.class)
    public void updateFailure(JsonNode account) {

        resource.update(UUID.randomUUID(), account);
    }

}
