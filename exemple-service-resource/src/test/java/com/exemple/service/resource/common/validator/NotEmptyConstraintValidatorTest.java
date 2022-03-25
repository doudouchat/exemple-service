package com.exemple.service.resource.common.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.resource.account.model.Account;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class NotEmptyConstraintValidatorTest extends AbstractTestNGSpringContextTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountResource resource;

    @Test
    public void updateSuccess() {

        Account model = Account.builder().email("jean.dupont@gmail.com").build();

        UUID id = resource.save(MAPPER.convertValue(model, JsonNode.class));

        JsonNode account = resource.get(id).get();
        assertThat(account.get("email"), is(notNullValue()));

    }

    @DataProvider(name = "failures")
    public static Object[][] failures() {

        return new Object[][] {
                // null
                { null },
                // empty
                { MAPPER.createObjectNode() } };

    }

    @Test(dataProvider = "failures", expectedExceptions = ConstraintViolationException.class)
    public void updateFailure(JsonNode account) {

        resource.save(account);
    }

}
