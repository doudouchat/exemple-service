package com.exemple.service.schema.core.validator.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
public class CreateOnlyValidatorTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private CreateOnlyValidator validator;

    @DataProvider(name = "createOnlyKO")
    private static Object[][] createOnlyKO() {

        Map<String, Object> model1 = new HashMap<>();
        model1.put("id", "ID1");

        Map<String, Object> old1 = new HashMap<>();
        old1.put("id", "ID2");

        Map<String, Object> model2 = new HashMap<>();
        model2.put("id", null);

        return new Object[][] {
                // id different
                { JsonNodeUtils.create(model1), JsonNodeUtils.create(old1) },
                // id different
                { JsonNodeUtils.create(model1), JsonNodeUtils.init() },
                // id null
                { JsonNodeUtils.create(model2), JsonNodeUtils.create(old1) },
                //
        };
    }

    @Test(dataProvider = "createOnlyKO")
    public void createOnlyKO(JsonNode model, JsonNode old) {

        ValidationException validationException = new ValidationException();

        validator.validate("/id", model, old, validationException);

        assertThat(validationException.getAllExceptions().size(), is(1));
        assertThat(validationException.getAllExceptions().get(0).getPath(), is("/id"));
        assertThat(validationException.getAllExceptions().get(0).getCode(), is("createOnly"));

    }

    @DataProvider(name = "createOnlyOK")
    private static Object[][] createOnlyOK() {

        Map<String, Object> model1 = new HashMap<>();
        model1.put("id", "ID1");

        Map<String, Object> old1 = new HashMap<>();
        old1.put("id", "ID1");

        return new Object[][] {
                // id not different
                { JsonNodeUtils.create(model1), JsonNodeUtils.create(old1) },
                // old null
                { JsonNodeUtils.create(model1), null },
                // id not mandatory
                { JsonNodeUtils.init(), JsonNodeUtils.create(old1) },
                //
        };
    }

    @Test(dataProvider = "createOnlyOK")
    public void createOnlyOK(JsonNode model, JsonNode old) {

        ValidationException validationException = new ValidationException();

        validator.validate("/id", model, old, validationException);

        assertThat(validationException.getAllExceptions().size(), is(0));
    }

}
