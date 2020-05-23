package com.exemple.service.schema.core.validator.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.exemple.service.schema.validation.SchemaValidationContext;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { SchemaTestConfiguration.class })
public class DependenciesValidatorTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DependenciesValidator validator;

    @BeforeClass
    private void before() {

        SchemaValidationContext.get().setApp("default");
        SchemaValidationContext.get().setVersion("default");
        SchemaValidationContext.get().setResource("schema_test");
        SchemaValidationContext.get().setProfile("default");

    }

    @AfterClass
    private void excutionContextDestroy() {

        SchemaValidationContext.destroy();
    }

    @DataProvider(name = "failure")
    private static Object[][] failure() {

        Map<String, Object> model1 = new HashMap<>();
        model1.put("email", null);

        Map<String, Object> old1 = new HashMap<>();
        old1.put("opt_in_email", true);

        Map<String, Object> model2 = new HashMap<>();
        model2.put("opt_in_email", true);

        return new Object[][] {
                // email null
                { JsonNodeUtils.create(model1), JsonNodeUtils.create(old1) },
                // opt_in email true
                { JsonNodeUtils.create(model2), JsonNodeUtils.init() }
                //
        };
    }

    @Test(dataProvider = "failure")
    public void failure(JsonNode model, JsonNode old) {

        ValidationException validationException = new ValidationException();

        validator.validate("opt_in_email,email", model, old, validationException);

        assertThat(validationException.getAllExceptions().size(), is(1));
        assertThat(validationException.getAllExceptions().get(0).getPath(), is("/email"));
        assertThat(validationException.getAllExceptions().get(0).getCode(), is("required"));

    }

    @DataProvider(name = "success")
    private static Object[][] success() {

        Map<String, Object> model1 = new HashMap<>();
        model1.put("opt_in_email", true);

        Map<String, Object> model2 = new HashMap<>();
        model2.put("email", null);
        model2.put("opt_in_email", false);

        Map<String, Object> old2 = new HashMap<>();
        old2.put("opt_in_email", true);
        old2.put("email", "toto");

        Map<String, Object> old3 = new HashMap<>();
        old3.put("opt_in_email", true);

        return new Object[][] {
                // old null
                { JsonNodeUtils.create(model1), null },
                // opt_in_email false
                { JsonNodeUtils.create(model1), JsonNodeUtils.create(old2) },
                // old failure
                { JsonNodeUtils.init(), JsonNodeUtils.create(old3) }
                //
        };
    }

    @Test(dataProvider = "success")
    public void success(JsonNode model, JsonNode old) {

        ValidationException validationException = new ValidationException();

        validator.validate("opt_in_email,email", model, old, validationException);

        assertThat(validationException.getAllExceptions().size(), is(0));
    }

}
