package com.exemple.service.schema.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.everit.json.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionModel;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonPatch;

@ContextConfiguration(classes = { SchemaTestConfiguration.class })
public class SchemaValidationTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaValidationTest.class);

    @Autowired
    private SchemaValidation validation;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeClass
    private void before() {

        ServiceContextExecution.context().setApp("default");
        ServiceContextExecution.context().setVersion("default");
        ServiceContextExecution.context().setProfile("default");

    }

    @Test
    public void validation() {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("lastname", "Dupont");
        model.put("firstname", "Jean");
        model.put("opt_in_email", true);
        model.put("civility", "Mr");

        validation.validate("default", "default", "schema_test", "default", MAPPER.convertValue(model, JsonNode.class));

    }

    @DataProvider(name = "failures")
    private static Object[][] failure() {

        ObjectNode patch1 = MAPPER.createObjectNode();
        patch1.put("op", "replace");
        patch1.put("path", "/opt_in_email");
        patch1.put("value", true);

        ObjectNode patch2 = MAPPER.createObjectNode();
        patch2.put("op", "remove");
        patch2.put("path", "/firstname");

        Map<String, Object> cgu1 = new HashMap<>();
        cgu1.put("code", "code_1");
        cgu1.put("version", "v1");

        Map<String, Object> cgu2 = new HashMap<>();
        cgu2.put("code", "code_1");
        cgu2.put("version", "v2");
        Map<String, Object> cgu3 = new HashMap<>();
        cgu3.put("code", "code_2");
        cgu3.put("version", "v1");

        ObjectNode patch31 = MAPPER.createObjectNode();
        patch31.put("op", "add");
        patch31.put("path", "/cgus/0");
        patch31.set("value", MAPPER.convertValue(cgu1, JsonNode.class));

        ObjectNode patch32 = MAPPER.createObjectNode();
        patch32.put("op", "add");
        patch32.put("path", "/cgus/1");
        patch32.set("value", MAPPER.convertValue(cgu1, JsonNode.class));

        ObjectNode patch41 = MAPPER.createObjectNode();
        patch41.put("op", "add");
        patch41.put("path", "/cgus/0");
        patch41.set("value", MAPPER.convertValue(cgu1, JsonNode.class));

        ObjectNode patch42 = MAPPER.createObjectNode();
        patch42.put("op", "add");
        patch42.put("path", "/cgus/1");
        patch42.set("value", MAPPER.convertValue(cgu2, JsonNode.class));

        ObjectNode patch43 = MAPPER.createObjectNode();
        patch43.put("op", "add");
        patch43.put("path", "/cgus/2");
        patch43.set("value", MAPPER.convertValue(cgu3, JsonNode.class));

        ObjectNode patch5 = MAPPER.createObjectNode();
        patch5.put("op", "add");
        patch5.put("path", "/birthday");
        patch5.put("value", "2018-02-30");

        ObjectNode patch6 = MAPPER.createObjectNode();
        patch6.put("op", "add");
        patch6.put("path", "/creation_date");
        patch6.put("value", "2018-02-30T12:00:00Z");

        ObjectNode patch7 = MAPPER.createObjectNode();
        patch7.put("op", "add");
        patch7.put("path", "/id");
        patch7.put("value", UUID.randomUUID().toString());

        ObjectNode patch8 = MAPPER.createObjectNode();
        patch8.put("op", "add");
        patch8.put("path", "/email");
        patch8.put("value", "toto");

        ObjectNode patch9 = MAPPER.createObjectNode();
        patch9.put("op", "add");
        patch9.put("path", "/nc");
        patch9.put("value", "nc");

        ObjectNode patch10 = MAPPER.createObjectNode();
        patch10.put("op", "add");
        patch10.put("path", "/firstname");
        patch10.put("value", " ");

        Map<String, Object> holidays = new HashMap<>();

        Map<String, Object> holiday = new HashMap<>();
        holiday.put("city", "Paris");
        holiday.put("street", "rue de la paix");

        Map<String, Object> home = new HashMap<>();
        home.put("city", "Paris");

        holidays.put("holiday1", holiday);
        holidays.put("holiday2", holiday);
        holidays.put("holiday3", holiday);

        ObjectNode patch11 = MAPPER.createObjectNode();
        patch11.put("op", "add");
        patch11.put("path", "/addresses/home");
        patch11.set("value", MAPPER.convertValue(home, JsonNode.class));

        ObjectNode patch12 = MAPPER.createObjectNode();
        patch12.put("op", "add");
        patch12.put("path", "/addresses");
        patch12.set("value", MAPPER.convertValue(holidays, JsonNode.class));

        ObjectNode patch13 = MAPPER.createObjectNode();
        patch13.put("op", "add");
        patch13.put("path", "/email");
        patch13.put("value", "");

        ObjectNode patch14 = MAPPER.createObjectNode();
        patch14.put("op", "add");
        patch14.put("path", "/civility");
        patch14.put("value", "Mlle");

        return new Object[][] {
                // email required
                { "required", "/email", patch1 },
                // firstname required
                { "required", "/firstname", patch2 },
                // unique cgu
                { "uniqueItems", "/cgus", patch31, patch32 },
                // max cgu
                { "maxItems", "/cgus", patch41, patch42, patch43 },
                // bad birthday
                { "format", "/birthday", patch5 },
                // FIXME bad creation date
                // { "format", "/creation_date", patch6 },
                // id read only
                { "readOnly", "/id", patch7 },
                // bad email
                { "format", "/email", patch8 },
                // nc unknown
                { "additionalProperties", "/nc", patch9 },
                // firstname blank
                { "pattern", "/firstname", patch10 },
                // street required
                { "required", "/addresses/home/street", patch11 },
                // maxProperties
                { "maxProperties", "/addresses", patch12 },
                // email empty
                { "format", "/email", patch13 },
                // bad enum
                { "enum", "/civility", patch14 }

        };
    }

    @Test(dataProvider = "failures")
    public void validationFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

        Map<String, Object> origin = new HashMap<>();
        origin.put("lastname", "Dupont");
        origin.put("firstname", "Jean");
        origin.put("opt_in_email", false);
        origin.put("civility", "Mr");
        origin.put("cgus", MAPPER.createArrayNode());
        origin.put("addresses", MAPPER.createObjectNode());

        JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

        ArrayNode patch = MAPPER.createArrayNode();
        patch.addAll(Arrays.asList(patchs));

        JsonNode model = JsonPatch.apply(patch, old);

        try {

            validation.validate("default", "default", "schema_test", "default", model);

            Assert.fail("expected ValidationException");

        } catch (ValidationException e) {

            LOG.debug("validationFailure {}", e.getMessage());

            e.getAllExceptions().stream()
                    .forEach(exception -> LOG.debug("code:{} path:{} message:{}", exception.getCode(), exception.getPath(), exception.getMessage()));

            assertThat(e.getAllExceptions().size(), is(1));

            ValidationExceptionModel exception = e.getAllExceptions().stream().findFirst().get();

            assertThat(exception.getCode(), is(expectedCode));
            assertThat(exception.getPath(), is(expectedPath));
        }
    }

    @DataProvider(name = "validationPatchSuccess")
    private static Object[][] validationPatchSuccess() {

        ObjectNode patch1 = MAPPER.createObjectNode();
        patch1.put("op", "replace");
        patch1.put("path", "/email");
        patch1.put("value", "jack.dupont@gmail.com");

        Map<String, Object> addresse = new HashMap<>();
        addresse.put("city", "New York");
        addresse.put("street", "5th avenue");

        ObjectNode patch2 = MAPPER.createObjectNode();
        patch2.put("op", "add");
        patch2.put("path", "/addresses/holidays");
        patch2.set("value", MAPPER.convertValue(addresse, JsonNode.class));

        ObjectNode patch3 = MAPPER.createObjectNode();
        patch3.put("op", "add");
        patch3.put("path", "/addresses/home");
        patch3.set("value", MAPPER.convertValue(addresse, JsonNode.class));

        ObjectNode patch4 = MAPPER.createObjectNode();
        patch4.put("op", "remove");
        patch4.put("path", "/addresses/job");

        return new Object[][] {

                // replace email
                { patch1 },
                // add addresses
                { patch2, patch3, patch4 }

        };
    }

    @Test(dataProvider = "validationPatchSuccess")
    public void validationPatchSuccess(JsonNode... patchs) {

        Map<String, Object> origin = new HashMap<>();
        origin.put("email", "jean.dupont@gmail.com");
        origin.put("lastname", "Dupont");
        origin.put("firstname", "Jean");
        origin.put("opt_in_email", true);
        origin.put("civility", "Mr");
        origin.put("civility", "Mr");

        Map<String, Object> addresse = new HashMap<>();
        addresse.put("city", "New York");
        addresse.put("street", "5th avenue");
        origin.put("addresses", MAPPER.createObjectNode().set("job", MAPPER.convertValue(addresse, JsonNode.class)));

        JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

        ArrayNode patch = MAPPER.createArrayNode();
        patch.addAll(Arrays.asList(patchs));

        JsonNode model = JsonPatch.apply(patch, old);

        validation.validate("default", "default", "schema_test", "default", MAPPER.convertValue(model, JsonNode.class),
                MAPPER.convertValue(origin, JsonNode.class));
    }

    @Test
    public void validationPatchSuccessWithNotAccessProperty() {

        Map<String, Object> origin = new HashMap<>();
        origin.put("email", "jean.dupont@gmail.com");
        origin.put("lastname", "Dupont");
        origin.put("firstname", "Jean");
        origin.put("opt_in_email", true);
        origin.put("civility", "Mr");
        origin.put("hide", true);

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jack.dupont@gmail.com");
        model.put("lastname", "Dupont");
        model.put("firstname", "Jean");
        model.put("opt_in_email", true);
        model.put("civility", "Mr");
        model.put("hide", true);

        validation.validate("default", "default", "schema_test", "default", MAPPER.convertValue(model, JsonNode.class),
                MAPPER.convertValue(origin, JsonNode.class));

    }

    @DataProvider(name = "validationPatchFailures")
    private static Object[][] validationPatchFailures() {

        ObjectNode patch1 = MAPPER.createObjectNode();
        patch1.put("op", "replace");
        patch1.put("path", "/external_id");
        patch1.put("value", UUID.randomUUID().toString());

        ObjectNode patch2 = MAPPER.createObjectNode();
        patch2.put("op", "remove");
        patch2.put("path", "/hide");

        ObjectNode patch3 = MAPPER.createObjectNode();
        patch3.put("op", "remove");
        patch3.put("path", "/external_id");

        return ArrayUtils.addAll(failure(), new Object[][] {
                // external_id create only
                { "readOnly", "/external_id", patch1 },
                // additionalProperties hide unknown
                { "additionalProperties", "/hide", patch2 },
                // external_id create only
                { "readOnly", "/external_id", patch3 } });

    }

    @Test(dataProvider = "validationPatchFailures")
    public void validationPatchFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

        Map<String, Object> origin = new HashMap<>();
        origin.put("id", UUID.randomUUID().toString());
        origin.put("external_id", UUID.randomUUID().toString());
        origin.put("opt_in_email", false);
        origin.put("firstname", "john");
        origin.put("cgus", MAPPER.createArrayNode());
        origin.put("addresses", MAPPER.createObjectNode());
        origin.put("hide", true);

        JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

        ArrayNode patch = MAPPER.createArrayNode();
        patch.addAll(Arrays.asList(patchs));

        JsonNode model = JsonPatch.apply(patch, old);

        try {

            validation.validate("default", "default", "schema_test", "default", model, old);

            Assert.fail("expected ValidationException");

        } catch (ValidationException e) {

            LOG.debug("validationFailure {}", e.getMessage());

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

            validation.validate("unknown", "unknown", "schema_test", "unknown", MAPPER.convertValue(model, JsonNode.class),
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
            assertThat(exception.getPath().toString(), is("/email"));
        }
    }
}
