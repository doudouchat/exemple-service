package com.exemple.service.schema.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.everit.json.schema.Schema;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionModel;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonPatch;

@SpringJUnitConfig(SchemaTestConfiguration.class)
public class SchemaValidationTest {

    @Autowired
    private SchemaValidation validation;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ValidateToCreation {

        @Test
        public void validation() {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            model.put("lastname", "Dupont");
            model.put("firstname", "Jean");
            model.put("opt_in_email", true);
            model.put("civility", "Mr");
            model.put("creation_date", "2019-06-17T19:16:40Z");

            // When perform validate
            validation.validate("default", "default", "default", "schema_test", MAPPER.convertValue(model, JsonNode.class));

        }

        private Stream<Arguments> validationFailure() {

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

            return Stream.of(
                    // email required
                    Arguments.of("required", "/email", new JsonNode[] { patch1 }),
                    // firstname required
                    Arguments.of("required", "/firstname", new JsonNode[] { patch2 }),
                    // unique cgu
                    Arguments.of("uniqueItems", "/cgus", new JsonNode[] { patch31, patch32 }),
                    // max cgu
                    Arguments.of("maxItems", "/cgus", new JsonNode[] { patch41, patch42, patch43 }),
                    // bad birthday
                    Arguments.of("format", "/birthday", new JsonNode[] { patch5 }),
                    // bad creation date
                    Arguments.of("format", "/creation_date", new JsonNode[] { patch6 }),
                    // id read only
                    Arguments.of("readOnly", "/id", new JsonNode[] { patch7 }),
                    // bad email
                    Arguments.of("format", "/email", new JsonNode[] { patch8 }),
                    // nc unknown
                    Arguments.of("additionalProperties", "/nc", new JsonNode[] { patch9 }),
                    // firstname blank
                    Arguments.of("pattern", "/firstname", new JsonNode[] { patch10 }),
                    // street required
                    Arguments.of("required", "/addresses/home/street", new JsonNode[] { patch11 }),
                    // maxProperties
                    Arguments.of("maxProperties", "/addresses", new JsonNode[] { patch12 }),
                    // email empty
                    Arguments.of("format", "/email", new JsonNode[] { patch13 }),
                    // bad enum
                    Arguments.of("enum", "/civility", new JsonNode[] { patch14 }));
        }

        @ParameterizedTest
        @MethodSource
        public void validationFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

            // Given model
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

            // When perform
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "schema_test", model));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getAllExceptions()).hasSize(1),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getCode).contains(expectedCode),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getPath).contains(expectedPath)));

        }
    }

    @Nested
    class ValidateArrayToCreation {

        @Test
        public void validation() {

            // Given build model
            Map<String, Object> addresse1 = new HashMap<>();
            addresse1.put("street", "1 rue de la paix");
            addresse1.put("city", "Paris");

            Map<String, Object> addresse2 = new HashMap<>();
            addresse2.put("street", "2 rue de la paix");
            addresse2.put("city", "Paris");

            List<Object> addresses = new ArrayList<>();
            addresses.add(addresse1);
            addresses.add(addresse2);

            JsonNode model = MAPPER.convertValue(addresses, JsonNode.class);

            // When perform validate

            validation.validate("default", "default", "default", "array_test", model);

        }

        @Test
        public void validationFailure() {

            // Given build model
            Map<String, Object> addresse = new HashMap<>();
            addresse.put("street", "1 rue de la paix");

            List<Object> addresses = new ArrayList<>();
            addresses.add(addresse);

            JsonNode model = MAPPER.convertValue(addresses, JsonNode.class);

            // When perform
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "array_test", model));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getAllExceptions()).hasSize(1),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getCode).contains("required"),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getPath).contains("/0/city")));

        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ValidateToUpdate {

        private Stream<Arguments> validationPatchSuccess() {

            Map<String, Object> origin0 = new HashMap<>();
            origin0.put("email", "jean.dupont@gmail.com");
            origin0.put("lastname", "Dupont");
            origin0.put("firstname", "Jean");
            origin0.put("opt_in_email", true);
            origin0.put("civility", "Mr");
            origin0.put("addresses", MAPPER.createObjectNode());
            origin0.put("cgus", MAPPER.createArrayNode());

            Map<String, Object> origin1 = new HashMap<>();
            origin1.put("id", UUID.randomUUID().toString());
            origin1.put("email", "jean.dupont@gmail.com");
            origin1.put("civility", "nc");
            Map<String, Object> cgu0 = new HashMap<>();
            cgu0.put("code", "code_0");
            origin1.put("cgus", MAPPER.createArrayNode().add(MAPPER.convertValue(cgu0, JsonNode.class)));
            origin1.put("addresses",
                    MAPPER.createObjectNode().set("job", MAPPER.convertValue(Collections.singletonMap("city", "New York"), JsonNode.class)));

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

            Map<String, Object> cgu = new HashMap<>();
            cgu.put("code", "code_1");
            cgu.put("version", "v1");

            ObjectNode patch5 = MAPPER.createObjectNode();
            patch5.put("op", "add");
            patch5.put("path", "/cgus/0");
            patch5.set("value", MAPPER.convertValue(cgu, JsonNode.class));

            ObjectNode patch6 = MAPPER.createObjectNode();
            patch6.put("op", "add");
            patch6.put("path", "/cgus/1");
            patch6.set("value", MAPPER.convertValue(cgu, JsonNode.class));

            return Stream.of(
                    // replace email
                    Arguments.of(origin0, new JsonNode[] { patch1 }),
                    // add addresses
                    Arguments.of(origin0, new JsonNode[] { patch2, patch3, patch4 }),
                    // replace email with origin with one error
                    Arguments.of(origin1, new JsonNode[] { patch1 }),
                    // add cgu
                    Arguments.of(origin0, new JsonNode[] { patch5 }),
                    // add cgu
                    Arguments.of(origin1, new JsonNode[] { patch6 })

            );
        }

        @ParameterizedTest
        @MethodSource
        public void validationPatchSuccess(Map<String, Object> origin, JsonNode... patchs) {

            // build source
            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

            ArrayNode patch = MAPPER.createArrayNode();
            patch.addAll(Arrays.asList(patchs));

            JsonNode model = JsonPatch.apply(patch, old);

            // When perform validate
            validation.validate("default", "default", "default", "schema_test", model, MAPPER.convertValue(origin, JsonNode.class));
        }

        private Stream<Arguments> validationPatchFailure() {

            ObjectNode patch1 = MAPPER.createObjectNode();
            patch1.put("op", "replace");
            patch1.put("path", "/external_id");
            patch1.put("value", UUID.randomUUID().toString());

            ObjectNode patch2 = MAPPER.createObjectNode();
            patch2.put("op", "add");
            patch2.put("path", "/hide");
            patch2.put("value", false);

            ObjectNode patch3 = MAPPER.createObjectNode();
            patch3.put("op", "remove");
            patch3.put("path", "/external_id");

            return Stream.of(
                    // external_id create only
                    Arguments.of("readOnly", "/external_id", patch1),
                    // additionalProperties hide unknown
                    Arguments.of("additionalProperties", "/hide", patch2),
                    // external_id create only
                    Arguments.of("readOnly", "/external_id", patch3));

        }

        @ParameterizedTest
        @MethodSource
        public void validationPatchFailure(String expectedCode, String expectedPath, JsonNode patchs) {

            Map<String, Object> origin = new HashMap<>();
            origin.put("id", UUID.randomUUID().toString());
            origin.put("external_id", UUID.randomUUID().toString());
            origin.put("opt_in_email", false);
            origin.put("firstname", "john");
            origin.put("cgus", MAPPER.createArrayNode());
            origin.put("addresses", MAPPER.createObjectNode());

            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

            ArrayNode patch = MAPPER.createArrayNode();
            patch.add(patchs);

            JsonNode model = JsonPatch.apply(patch, old);
            ((ObjectNode) old).set("hide", BooleanNode.TRUE);

            // When perform
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "schema_test", model, old));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getAllExceptions()).hasSize(1),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getCode).contains(expectedCode),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getPath).contains(expectedPath)));
        }

        @Test
        public void updateEmptySchemaFailure() {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");

            // When perform
            Throwable throwable = catchThrowable(
                    () -> validation.validate("unknown", "unknown", "unknown", "schema_test", MAPPER.convertValue(model, JsonNode.class),
                            MAPPER.convertValue(new HashMap<>(), JsonNode.class)));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getAllExceptions()).hasSize(1),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getCode)
                                    .contains("additionalProperties"),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getPath).contains("/email")));
        }

    }

    @Nested
    class ValidateArrayToUpdate {

        @Test
        public void validationNotUniqueItemsFailure() {

            // Given build model
            Map<String, Object> addresse = new HashMap<>();
            addresse.put("street", "1 rue de la paix");
            addresse.put("city", "paris");

            List<Object> addresses = new ArrayList<>();
            addresses.add(addresse);
            addresses.add(addresse);

            JsonNode model = MAPPER.convertValue(addresses, JsonNode.class);

            // When perform
            Throwable throwable = catchThrowable(
                    () -> validation.validate("default", "default", "default", "array_test", model, MAPPER.createArrayNode()));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getAllExceptions()).hasSize(1),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getCode).contains("uniqueItems"),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getPath).contains("")));

        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ValidateToPatch {

        @Test
        public void validationPatch() {

            Map<String, Object> patch = new HashMap<>();
            patch.put("op", "add");
            patch.put("path", "/lastname");
            patch.put("value", "Dupond");

            validation.validatePatch(MAPPER.convertValue(Collections.singletonList(patch), ArrayNode.class));

        }

        private Stream<Arguments> validationPatchFailure() {

            Map<String, Object> patch1 = new HashMap<>();
            patch1.put("op", "bad");
            patch1.put("path", "/lastname");
            patch1.put("value", "Dupond");

            Map<String, Object> patch2 = new HashMap<>();
            patch2.put("op", "add");
            patch2.put("path", "lastname");
            patch2.put("value", "Dupond");

            return Stream.of(
                    // bad op
                    Arguments.of(patch1, 4),
                    // bad pattern
                    Arguments.of(patch2, 1));
        }

        @ParameterizedTest
        @MethodSource
        public void validationPatchFailure(Map<String, Object> patch, int expectedExceptionSize) {

            // When perform
            Throwable throwable = catchThrowable(
                    () -> validation.validatePatch(MAPPER.convertValue(Collections.singletonList(patch), ArrayNode.class)));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertThat(exception.getAllExceptions()).hasSize(expectedExceptionSize));
        }

    }

    @Nested
    class ValidateSchema {

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

            // When perform
            Throwable throwable = catchThrowable(
                    () -> validation.validate(schema, MAPPER.convertValue(model, JsonNode.class)));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getAllExceptions()).hasSize(1),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getCode).contains("format"),
                            () -> assertThat(exception.getAllExceptions()).extracting(ValidationExceptionModel::getPath).contains("/email")));
        }

    }
}
