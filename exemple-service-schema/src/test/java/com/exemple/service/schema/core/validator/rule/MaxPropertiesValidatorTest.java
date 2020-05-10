package com.exemple.service.schema.core.validator.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { SchemaTestConfiguration.class })
public class MaxPropertiesValidatorTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MaxPropertiesValidator validator;

    @DataProvider(name = "exceeded")
    private static Object[][] exceeded() {

        Map<String, Object> holidays = new HashMap<>();
        holidays.put("holiday1", "Paris");
        holidays.put("holiday2", "Lyon");

        JsonNode model1 = JsonNodeUtils.create(Collections.singletonMap("addresses", holidays));

        holidays = new HashMap<>();
        holidays.put("holiday1", "Paris");

        JsonNode model2 = JsonNodeUtils.create(Collections.singletonMap("addresses", holidays));
        holidays = new HashMap<>();
        holidays.put("holiday2", "Lyon");
        JsonNode old2 = JsonNodeUtils.create(Collections.singletonMap("addresses", holidays));

        return new Object[][] {
                // exceeded
                { model1, null },
                // exceeded
                { model2, old2 }
                //
        };
    }

    @Test(dataProvider = "exceeded")
    public void exceeded(JsonNode model, JsonNode old) {

        ValidationException validationException = new ValidationException();

        validator.validate("/addresses,1", model, old, validationException);

        assertThat(validationException.getAllExceptions().size(), is(1));
        assertThat(validationException.getAllExceptions().get(0).getPath(), is("/addresses"));
        assertThat(validationException.getAllExceptions().get(0).getCode(), is("maxProperties"));

    }

    @DataProvider(name = "notExceeded")
    private static Object[][] notExceeded() {

        Map<String, Object> holidays;

        holidays = new HashMap<>();
        holidays.put("holiday1", "Lyon");
        holidays.put("holiday2", null);
        JsonNode model1 = JsonNodeUtils.create(Collections.singletonMap("addresses", holidays));

        holidays = new HashMap<>();
        holidays.put("holiday2", "Paris");
        JsonNode old1 = JsonNodeUtils.create(Collections.singletonMap("addresses", holidays));

        holidays = new HashMap<>();
        holidays.put("holiday1", "Paris");
        holidays.put("holiday2", "Lyon");

        JsonNode model2 = JsonNodeUtils.create(Collections.singletonMap("addresses", holidays));

        JsonNode model3 = JsonNodeUtils.create(Collections.singletonMap("addresses", null));

        ValidationException validationException = new ValidationException();
        validationException.add(new ValidationException.ValidationExceptionModel("/addresses", "error", "message"));

        return new Object[][] {
                // not exceeded
                { model1, old1, new ValidationException(), 0 },
                // not exceeded
                { model3, null, new ValidationException(), 0 },
                // not exceeded
                { model2, JsonNodeUtils.init(), validationException, 1 }, };
    }

    @Test(dataProvider = "notExceeded")
    public void notExceeded(JsonNode model, JsonNode old, ValidationException validationException, int exceptedExceptionsSize) {

        validator.validate("/addresses,1", model, old, validationException);

        assertThat(validationException.getAllExceptions().size(), is(exceptedExceptionsSize));

    }

}
