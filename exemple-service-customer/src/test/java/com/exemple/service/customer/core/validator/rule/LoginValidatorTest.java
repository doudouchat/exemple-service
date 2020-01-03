package com.exemple.service.customer.core.validator.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.schema.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class LoginValidatorTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LoginValidator loginValidator;

    @Autowired
    private LoginResource loginResource;

    @BeforeMethod
    private void before() {

        Mockito.reset(loginResource);

    }

    @DataProvider(name = "notUnique")
    private static Object[][] notUnique() {

        Map<String, Object> model1 = new HashMap<>();
        model1.put("email", "jean.dupond@gmail.com");

        return new Object[][] {
                // email not unique
                { model1, null },
                // email not unique && old
                { model1, JsonNodeUtils.init() }
                //
        };
    }

    @Test(dataProvider = "notUnique")
    public void notUnique(Map<String, Object> model, JsonNode old) {

        Mockito.when(loginResource.get(Mockito.eq((String) model.get("email")))).thenReturn(Optional.of(JsonNodeUtils.init()));

        ValidationException validationException = new ValidationException();

        loginValidator.validate("/email", JsonNodeUtils.create(model), old, validationException);

        assertThat(validationException.getAllExceptions().size(), is(1));
        assertThat(validationException.getAllExceptions().get(0).getPath(), is("/email"));
        assertThat(validationException.getAllExceptions().get(0).getCode(), is("login"));

        // Mockito.verify(loginResource.get(Mockito.eq((String) model.getEmail())));
    }

    @DataProvider(name = "unique")
    private static Object[][] unique() {

        Map<String, Object> model1 = new HashMap<>();
        model1.put("email", "jean.dupond@gmail.com");

        Map<String, Object> old1 = new HashMap<>();
        old1.put("email", "jean.dupond@gmail.com");

        ValidationException validationException = new ValidationException();
        validationException.add(new ValidationException.ValidationExceptionModel("/email", "error", "message"));

        return new Object[][] {
                // email unique
                { model1, null, Optional.empty(), new ValidationException(), 0 },
                // email and old equals
                { model1, JsonNodeUtils.create(old1), Optional.of(JsonNodeUtils.init()), new ValidationException(), 0 },
                // email empty
                { new HashMap<>(), null, Optional.of(JsonNodeUtils.init()), new ValidationException(), 0 },
                // email has already exception
                { model1, null, Optional.of(JsonNodeUtils.init()), validationException, 1 },
                //
        };
    }

    @Test(dataProvider = "unique")
    public void unique(Map<String, Object> model, JsonNode old, Optional<JsonNode> response, ValidationException validationException,
            int expectedExceptionSize) {

        Mockito.when(loginResource.get(Mockito.eq((String) model.get("email")))).thenReturn(response);

        loginValidator.validate("/email", JsonNodeUtils.create(model), old, validationException);

        assertThat(validationException.getAllExceptions().size(), is(expectedExceptionSize));

        // Mockito.verify(loginResource.get(Mockito.eq((String) model.getEmail())));
    }

}
