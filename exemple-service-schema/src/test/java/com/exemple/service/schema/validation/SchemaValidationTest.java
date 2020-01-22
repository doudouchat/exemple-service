package com.exemple.service.schema.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.everit.json.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationException.ValidationExceptionModel;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@ContextConfiguration(classes = { SchemaTestConfiguration.class })
public class SchemaValidationTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaValidationTest.class);

    @Autowired
    private SchemaValidation validation;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void validation() {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("lastname", "Dupont");
        model.put("firstname", "Jean");
        model.put("opt_in_email", true);
        model.put("civility", "Mr");

        validation.validate("default", "default", "schema_test", MAPPER.convertValue(model, JsonNode.class), null);

    }

    @DataProvider(name = "failures")
    private static Object[][] failure() {

        Map<String, Object> model1 = new HashMap<>();
        model1.put("opt_in_email", true);

        Map<String, Object> model2 = new HashMap<>();
        model2.put("firstname", null);

        Map<String, Object> model3 = new HashMap<>();
        List<Map<String, Object>> cgus = new ArrayList<>();
        Map<String, Object> cgu1 = new HashMap<>();
        cgu1.put("code", "code_1");
        cgu1.put("version", "v1");
        cgus.add(cgu1);
        cgus.add(cgu1);
        model3.put("cgus", cgus);

        Map<String, Object> model4 = new HashMap<>();
        cgus = new ArrayList<>();
        Map<String, Object> cgu2 = new HashMap<>();
        cgu2.put("code", "code_1");
        cgu2.put("version", "v2");
        Map<String, Object> cgu3 = new HashMap<>();
        cgu3.put("code", "code_2");
        cgu3.put("version", "v1");
        cgus.add(cgu1);
        cgus.add(cgu2);
        cgus.add(cgu3);
        model4.put("cgus", cgus);

        Map<String, Object> model5 = new HashMap<>();
        model5.put("birthday", "2018-02-30");

        Map<String, Object> model6 = new HashMap<>();
        model6.put("creation_date", "2018-02-30T12:00:00Z");

        Map<String, Object> model7 = new HashMap<>();
        model7.put("id", UUID.randomUUID().toString());

        Map<String, Object> model8 = new HashMap<>();
        model8.put("email", "toto");

        Map<String, Object> model9 = new HashMap<>();
        model9.put("nc", "nc");

        Map<String, Object> model10 = new HashMap<>();
        model10.put("firstname", " ");

        Map<String, Object> holidays = new HashMap<>();

        Map<String, Object> holiday = new HashMap<>();
        holiday.put("city", "Paris");
        holiday.put("street", "rue de la paix");

        holidays.put("holiday1", holiday);
        holidays.put("holiday2", holiday);
        holidays.put("holiday3", holiday);

        Map<String, Object> model11 = new HashMap<>();
        model11.put("addresses", holidays);

        Map<String, Object> model12 = new HashMap<>();
        model12.put("nc", null);

        Map<String, Object> model13 = new HashMap<>();
        model13.put("email", "");

        Map<String, Object> model14 = new HashMap<>();
        model14.put("civility", "Mlle");

        return new Object[][] {
                // email required
                { "required", "/email", model1 },
                // firstname required
                { "required", "/firstname", model2 },
                // unique cgu
                { "uniqueItems", "/cgus", model3 },
                // max cgu
                { "maxItems", "/cgus", model4 },
                // bad birthday
                { "format", "/birthday", model5 },
                // FIXME bad date
                // bad creation date
                // { "format", "/creation_date", model6 },
                // id read only
                { "readOnly", "/id", model7 },
                // bad email
                { "format", "/email", model8 },
                // nc unknown
                { "additionalProperties", "/nc", model9 },
                // firstname blank
                { "pattern", "/firstname", model10 },
                // maxProperties
                { "maxProperties", "/addresses", model11 },
                // FIXME additionalProperties null
                // nc unknown
                // { "additionalProperties", "/nc", model12 },
                // mail empty
                { "format", "/email", model13 },
                // bad enum
                { "enum", "/civility", model14 }
                //
        };
    }

    @Test(dataProvider = "failures")
    public void validationFailure(String expectedCode, String expectedPath, Map<String, Object> model) {

        try {

            validation.validate("default", "default", "schema_test", MAPPER.convertValue(model, JsonNode.class),
                    MAPPER.convertValue(new HashMap<>(), JsonNode.class));

            Assert.fail("expected ValidationException");

        } catch (ValidationException e) {

            e.getAllExceptions().stream()
                    .forEach(exception -> LOG.debug("code:{} path:{} message:{}", exception.getCode(), exception.getPath(), exception.getMessage()));

            assertThat(e.getAllExceptions().size(), is(1));

            ValidationExceptionModel exception = e.getAllExceptions().stream().findFirst().get();

            assertThat(exception.getCode(), is(expectedCode));
            assertThat(exception.getPath(), is(expectedPath));
        }
    }

    @Test
    public void updateEmptySchemaFailure() {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");

        try {

            validation.validate("unknown", "unknown", "schema_test", MAPPER.convertValue(model, JsonNode.class),
                    MAPPER.convertValue(new HashMap<>(), JsonNode.class));

            Assert.fail("expected ValidationException");

        } catch (ValidationException e) {

            e.getAllExceptions().stream()
                    .forEach(exception -> LOG.debug("code:{} path:{} message:{}", exception.getCode(), exception.getPath(), exception.getMessage()));

            assertThat(e.getAllExceptions().size(), is(1));

            ValidationExceptionModel exception = e.getAllExceptions().stream().findFirst().get();

            assertThat(exception.getCode(), is("additionalProperties"));
            assertThat(exception.getPath(), is("/email"));
        }
    }

    @Test
    public void validationPatch() {

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/lastname");
        patch.put("value", "Dupond");

        validation.validatePatch(MAPPER.convertValue(Collections.singletonList(patch), ArrayNode.class));

    }

    @DataProvider(name = "updateFailure")
    private static Object[][] updateFailure() {

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "bad");
        patch1.put("path", "/lastname");
        patch1.put("value", "Dupond");

        Map<String, Object> patch2 = new HashMap<>();
        patch2.put("op", "add");
        patch2.put("path", "lastname");
        patch2.put("value", "Dupond");

        return new Object[][] {

                // bad op
                { patch1, 4 },
                // bad pattern
                { patch2, 1 },
                //
        };
    }

    @Test(dataProvider = "updateFailure")
    public void validationPatchFailure(Map<String, Object> patch, int expectedExceptionSize) {

        try {

            validation.validatePatch(MAPPER.convertValue(Collections.singletonList(patch), ArrayNode.class));

            Assert.fail("expected ValidationException");

        } catch (ValidationException e) {

            e.getAllExceptions().stream()
                    .forEach(exception -> LOG.debug("code:{} path:{} message:{}", exception.getCode(), exception.getPath(), exception.getMessage()));

            assertThat(e.getAllExceptions().size(), is(expectedExceptionSize));

        }

    }

    @Test
    public void validationSchema() throws IOException {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("lastname", "Dupont");
        model.put("firstname", "Jean");
        model.put("opt_in_email", true);
        model.put("civility", "Mr");

        Schema schema = SchemaBuilder.build(new ClassPathResource("schema_test.json").getInputStream());

        validation.validate(schema, MAPPER.convertValue(model, JsonNode.class));

    }

    @Test
    public void validationSchemaFailure() throws IOException {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont");
        model.put("lastname", "Dupont");
        model.put("firstname", "Jean");
        model.put("opt_in_email", true);
        model.put("civility", "Mr");

        Schema schema = SchemaBuilder.build(new ClassPathResource("schema_test.json").getInputStream());

        try {

            validation.validate(schema, MAPPER.convertValue(model, JsonNode.class));

            Assert.fail("expected ValidationException");

        } catch (ValidationException e) {

            e.getAllExceptions().stream()
                    .forEach(exception -> LOG.debug("code:{} path:{} message:{}", exception.getCode(), exception.getPath(), exception.getMessage()));

            assertThat(e.getAllExceptions().size(), is(1));

            ValidationExceptionModel exception = e.getAllExceptions().stream().findFirst().get();

            assertThat(exception.getCode(), is("format"));
            assertThat(exception.getPath(), is("/email"));
        }
    }
}
