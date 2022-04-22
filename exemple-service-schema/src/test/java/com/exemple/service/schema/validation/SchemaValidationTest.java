package com.exemple.service.schema.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.everit.json.schema.Schema;
import org.junit.jupiter.api.DisplayName;
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
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonPatch;

@SpringJUnitConfig(SchemaTestConfiguration.class)
public class SchemaValidationTest {

    @Autowired
    private SchemaValidation validation;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Stream<Arguments> failures() {

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
        patch9.put("path", "/hide");
        patch9.put("value", false);

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
                // hide unknown
                Arguments.of("additionalProperties", "/hide", new JsonNode[] { patch9 }),
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

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("source creation validation")
    class ValidateToCreation {

        @Test
        @DisplayName("source creation success")
        public void creationSuccess() {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            model.put("lastname", "Dupont");
            model.put("firstname", "Jean");
            model.put("opt_in_email", true);
            model.put("civility", "Mr");
            model.put("creation_date", "2019-06-17T19:16:40Z");

            // When perform validate
            Throwable throwable = catchThrowable(
                    () -> validation.validate("default", "default", "default", "schema_test", MAPPER.convertValue(model, JsonNode.class)));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();

        }

        private Stream<Arguments> creationFailure() {
            return failures();
        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source creation fails")
        public void creationFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

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
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode).contains(expectedCode),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains(expectedPath)));

        }

        @Test
        @DisplayName("source creation fails because schema not exists")
        public void creationFailureWhenSchemaNotExists() {

            // Given source
            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");

            JsonNode source = MAPPER.convertValue(model, JsonNode.class);

            // When perform
            Throwable throwable = catchThrowable(
                    () -> validation.validate("unknown", "unknown", "unknown", "schema_test", source));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode)
                                    .contains("additionalProperties"),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains("/email")));
        }
    }

    @Nested
    @DisplayName("source array creation validation")
    class ValidateArrayToCreation {

        @Test
        @DisplayName("source creation validation")
        public void creationSuccess() {

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
        @DisplayName("source creation fails")
        public void creationFailure() {

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
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode).contains("required"),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains("/0/city")));

        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("source patch validation")
    class ValidateToPatch {

        private Stream<Arguments> patchSuccess() {

            ObjectNode patch1 = MAPPER.createObjectNode();
            patch1.put("op", "replace");
            patch1.put("path", "/email");
            patch1.put("value", "jack.dupond@gmail.com");

            Map<String, Object> addresse = new HashMap<>();
            addresse.put("city", "New York");
            addresse.put("street", "5th avenue");

            ObjectNode patch2 = MAPPER.createObjectNode();
            patch2.put("op", "add");
            patch2.put("path", "/addresses/holidays");
            patch2.set("value", MAPPER.convertValue(addresse, JsonNode.class));

            Map<String, Object> cgu = new HashMap<>();
            cgu.put("code", "code_1");
            cgu.put("version", "v1");

            ObjectNode patch3 = MAPPER.createObjectNode();
            patch3.put("op", "add");
            patch3.put("path", "/cgus/0");
            patch3.set("value", MAPPER.convertValue(cgu, JsonNode.class));

            return Stream.of(
                    // replace email
                    Arguments.of(patch1),
                    // add addresses
                    Arguments.of(patch2),
                    // add cgu
                    Arguments.of(patch3)

            );
        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source patch success")
        public void patchSuccess(JsonNode patch) {

            // build source
            Map<String, Object> origin = new HashMap<>();
            origin.put("email", "jean.dupont@gmail.com");
            origin.put("lastname", "Dupont");
            origin.put("firstname", "Jean");
            origin.put("opt_in_email", true);
            origin.put("civility", "Mr");
            origin.put("addresses", MAPPER.createObjectNode());
            origin.put("cgus", MAPPER.createArrayNode());
            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

            ArrayNode patchs = MAPPER.createArrayNode();
            patchs.add(patch);

            // When perform validate
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "schema_test", patchs, old));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();
        }

        private Stream<Arguments> patchFailure() {

            ObjectNode patch1 = MAPPER.createObjectNode();
            patch1.put("op", "add");
            patch1.put("path", "/external_id");
            patch1.put("value", UUID.randomUUID().toString());

            ObjectNode patch2 = MAPPER.createObjectNode();
            patch2.put("op", "add");
            patch2.put("path", "/external_id");
            patch2.set("value", NullNode.instance);

            return Stream.concat(
                    failures(),
                    Stream.of(
                            // external_id create only
                            Arguments.of("readOnly", "/external_id", new JsonNode[] { patch1 })
                    // external_id create only
                    // FIXME patch readOnly with null
                    // Arguments.of("readOnly", "/external_id", new JsonNode[] { patch2 })
                    ));
        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source patch fails")
        public void patchFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

            // Given origin
            Map<String, Object> origin = new HashMap<>();
            origin.put("opt_in_email", false);
            origin.put("lastname", "doe");
            origin.put("firstname", "john");
            origin.put("cgus", MAPPER.createArrayNode());
            origin.put("addresses", MAPPER.createObjectNode());
            origin.put("hide", BooleanNode.TRUE);

            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

            // And form
            ArrayNode patch = MAPPER.createArrayNode();
            patch.addAll(Arrays.asList(patchs));

            // When perform
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "schema_test", patch, old));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode).contains(expectedCode),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains(expectedPath)));
        }

        private Stream<Arguments> fixIncorrectSource() {

            ObjectNode patch1 = MAPPER.createObjectNode();
            patch1.put("op", "replace");
            patch1.put("path", "/email");
            patch1.put("value", "jean.dupond@gmail.com");

            ObjectNode patch2 = MAPPER.createObjectNode();
            patch2.put("op", "remove");
            patch2.put("path", "/cgus/0");

            ObjectNode patch3 = MAPPER.createObjectNode();
            patch3.put("op", "add");
            patch3.put("path", "/addresses/home/street");
            patch3.put("value", "1 rue de la paix");

            ObjectNode patch4 = MAPPER.createObjectNode();
            patch4.put("op", "remove");
            patch4.put("path", "/cgvs/0");

            return Stream.of(
                    // email format
                    Arguments.of(patch1),
                    // unique cgu
                    Arguments.of(patch2),
                    // street required
                    Arguments.of(patch3),
                    // maxItems
                    Arguments.of(patch4));

        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source patch success when fixing error")
        public void fixIncorrectSource(JsonNode patch) {

            // Given origin
            Map<String, Object> origin = new HashMap<>();

            // And email incorrect
            origin.put("email", "toto");

            // And cgu incorrect
            Map<String, Object> cgu = new HashMap<>();
            cgu.put("code", "code_1");
            cgu.put("version", "v1");

            List<Map<String, Object>> cgus = new ArrayList<>();
            cgus.add(cgu);
            cgus.add(cgu);

            origin.put("cgus", cgus);

            // And max cgv
            Map<String, Object> cgv1 = new HashMap<>();
            cgv1.put("code", "code_1");
            cgv1.put("version", "v1");

            Map<String, Object> cgv2 = new HashMap<>();
            cgv2.put("code", "code_2");
            cgv2.put("version", "v1");

            Map<String, Object> cgv3 = new HashMap<>();
            cgv3.put("code", "code_3");
            cgv3.put("version", "v1");

            List<Map<String, Object>> cgvs = new ArrayList<>();
            cgvs.add(cgv1);
            cgvs.add(cgv2);
            cgvs.add(cgv3);

            origin.put("cgvs", cgvs);

            // And addresses incorrect
            Map<String, Object> home = new HashMap<>();
            home.put("city", "Paris");

            Map<String, Object> addresses = new HashMap<>();
            addresses.put("home", home);

            origin.put("addresses", addresses);

            // And form
            ArrayNode patchs = MAPPER.createArrayNode();
            patchs.add(patch);

            // When perform
            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "schema_test", patchs, old));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();
        }

        private Stream<Arguments> fixIncorrectSourceFailure() {

            ObjectNode patch1 = MAPPER.createObjectNode();
            patch1.put("op", "replace");
            patch1.put("path", "/email");
            patch1.put("value", "tata");

            Map<String, Object> cgu = new HashMap<>();
            cgu.put("code", "code_1");
            cgu.put("version", "v2");

            ObjectNode patch21 = MAPPER.createObjectNode();
            patch21.put("op", "replace");
            patch21.put("path", "/cgus/0");
            patch21.set("value", MAPPER.convertValue(cgu, JsonNode.class));

            ObjectNode patch22 = MAPPER.createObjectNode();
            patch22.put("op", "replace");
            patch22.put("path", "/cgus/1");
            patch22.set("value", MAPPER.convertValue(cgu, JsonNode.class));

            Map<String, Object> holiday = new HashMap<>();
            holiday.put("city", "Paris");
            holiday.put("street", "2 rue de la paix");

            ObjectNode patch3 = MAPPER.createObjectNode();
            patch3.put("op", "replace");
            patch3.put("path", "/addresses/holiday");
            patch3.set("value", MAPPER.convertValue(holiday, JsonNode.class));

            ObjectNode patch41 = MAPPER.createObjectNode();
            patch41.put("op", "remove");
            patch41.put("path", "/addresses/holiday");

            ObjectNode patch42 = MAPPER.createObjectNode();
            patch42.put("op", "replace");
            patch42.put("path", "/addresses/home");
            Map<String, Object> home = new HashMap<>();
            home.put("city", "Londres");
            patch42.set("value", MAPPER.convertValue(home, JsonNode.class));

            Map<String, Object> cgv = new HashMap<>();
            cgv.put("code", "code_4");
            cgv.put("version", "v1");

            ObjectNode patch5 = MAPPER.createObjectNode();
            patch5.put("op", "add");
            patch5.put("path", "/cgvs/0");
            patch5.set("value", MAPPER.convertValue(cgv, JsonNode.class));

            return Stream.of(
                    // email format
                    Arguments.of("format", "/email", new JsonNode[] { patch1 }),
                    // unique cgu
                    Arguments.of("uniqueItems", "/cgus", new JsonNode[] { patch21, patch22 }),
                    // maxProperties
                    Arguments.of("maxProperties", "/addresses", new JsonNode[] { patch3 }),
                    // street required
                    Arguments.of("required", "/addresses/home/street", new JsonNode[] { patch41, patch42 }),
                    // maxItems
                    Arguments.of("maxItems", "/cgvs", new JsonNode[] { patch5 }));

        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source patch fails when fixing error")
        public void fixIncorrectSourceFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

            // Given origin
            Map<String, Object> origin = new HashMap<>();

            // And email incorrect
            origin.put("email", "toto");

            // And cgu incorrect
            Map<String, Object> cgu = new HashMap<>();
            cgu.put("code", "code_1");
            cgu.put("version", "v1");

            List<Map<String, Object>> cgus = new ArrayList<>();
            cgus.add(cgu);
            cgus.add(cgu);

            origin.put("cgus", cgus);

            // And max cgv
            Map<String, Object> cgv1 = new HashMap<>();
            cgv1.put("code", "code_1");
            cgv1.put("version", "v1");

            Map<String, Object> cgv2 = new HashMap<>();
            cgv2.put("code", "code_2");
            cgv2.put("version", "v1");

            Map<String, Object> cgv3 = new HashMap<>();
            cgv3.put("code", "code_3");
            cgv3.put("version", "v1");

            List<Map<String, Object>> cgvs = new ArrayList<>();
            cgvs.add(cgv1);
            cgvs.add(cgv2);
            cgvs.add(cgv3);

            origin.put("cgvs", cgvs);

            // And addresses incorrect
            Map<String, Object> holiday = new HashMap<>();
            holiday.put("city", "Paris");
            holiday.put("street", "rue de la paix");

            Map<String, Object> job = new HashMap<>();
            job.put("city", "Paris");
            job.put("street", "rue de la paix");

            Map<String, Object> home = new HashMap<>();
            home.put("city", "Paris");

            Map<String, Object> addresses = new HashMap<>();
            addresses.put("holiday", holiday);
            addresses.put("job", job);
            addresses.put("home", home);

            origin.put("addresses", addresses);

            // And form
            ArrayNode patch = MAPPER.createArrayNode();
            patch.addAll(Arrays.asList(patchs));

            // When perform
            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "schema_test", patch, old));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode).contains(expectedCode),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains(expectedPath)));
        }

        @Test
        @DisplayName("source patch fails because schema not exists")
        public void patchFailureWhenSchemaNotExists() {

            // Given origin

            Map<String, Object> origin = new HashMap<>();
            origin.put("email", "jean.dupond@gmail.com");
            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

            // And Patch
            ObjectNode patch = MAPPER.createObjectNode();
            patch.put("op", "replace");
            patch.put("path", "/email");
            patch.put("value", "jack.dupont@gmail.com");

            ArrayNode patchs = MAPPER.createArrayNode();
            patchs.add(patch);

            // When perform
            Throwable throwable = catchThrowable(
                    () -> validation.validate("unknown", "unknown", "unknown", "schema_test", patchs, old));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode)
                                    .contains("additionalProperties"),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains("/email")));
        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("source update validation")
    class ValidateToUpdate {

        private Stream<Arguments> updateSuccess() {

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

            Map<String, Object> cgu = new HashMap<>();
            cgu.put("code", "code_1");
            cgu.put("version", "v1");

            ObjectNode patch3 = MAPPER.createObjectNode();
            patch3.put("op", "add");
            patch3.put("path", "/cgus/0");
            patch3.set("value", MAPPER.convertValue(cgu, JsonNode.class));

            return Stream.of(
                    // replace email
                    Arguments.of(patch1),
                    // add addresses
                    Arguments.of(patch2),
                    // add cgu
                    Arguments.of(patch3));
        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source update success")
        public void updateSuccess(JsonNode patch) {

            // build source
            Map<String, Object> origin = new HashMap<>();
            origin.put("email", "jean.dupont@gmail.com");
            origin.put("lastname", "Dupont");
            origin.put("firstname", "Jean");
            origin.put("opt_in_email", true);
            origin.put("civility", "Mr");
            origin.put("addresses", MAPPER.createObjectNode());
            origin.put("cgus", MAPPER.createArrayNode());

            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

            // And form
            ArrayNode patchs = MAPPER.createArrayNode();
            patchs.add(patch);
            JsonNode model = JsonPatch.apply(patchs, old);

            // When perform validate
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "schema_test", model, old));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();

        }

        private Stream<Arguments> updateFailure() {

            new ValidateToCreation().creationFailure();

            ObjectNode patch1 = MAPPER.createObjectNode();
            patch1.put("op", "add");
            patch1.put("path", "/external_id");
            patch1.put("value", UUID.randomUUID().toString());

            return Stream.concat(
                    failures(),
                    Stream.of(
                            // external_id create only
                            Arguments.of("readOnly", "/external_id", new JsonNode[] { patch1 })));

        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source update fails")
        public void updateFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

            // Given origin
            Map<String, Object> origin = new HashMap<>();
            origin.put("id", UUID.randomUUID().toString());
            origin.put("opt_in_email", false);
            origin.put("firstname", "john");
            origin.put("lastname", "doe");
            origin.put("cgus", MAPPER.createArrayNode());
            origin.put("addresses", MAPPER.createObjectNode());
            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

            // And form
            ArrayNode patch = MAPPER.createArrayNode();
            patch.addAll(Arrays.asList(patchs));
            JsonNode model = JsonPatch.apply(patch, old);

            // And add hide
            ((ObjectNode) old).set("hide", BooleanNode.TRUE);

            // When perform
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "schema_test", model, old));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode).contains(expectedCode),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains(expectedPath)));
        }

        @Test
        @DisplayName("source update success when fixing error")
        public void fixIncorrectSource() {

            // Given origin
            Map<String, Object> origin = new HashMap<>();
            origin.put("id", UUID.randomUUID().toString());
            origin.put("email", "toto");
            origin.put("opt_in_email", false);
            origin.put("firstname", "john");
            origin.put("lastname", "doe");

            // And email incorrect
            Map<String, Object> model = new HashMap<>();
            model.put("id", origin.get("id"));
            model.put("email", "jean.dupond@gmail.com");
            model.put("opt_in_email", false);
            model.put("firstname", "john");
            model.put("lastname", "doe");

            // When perform
            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);
            JsonNode source = MAPPER.convertValue(model, JsonNode.class);
            Throwable throwable = catchThrowable(() -> validation.validate("default", "default", "default", "schema_test", source, old));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();
        }

        @Test
        @DisplayName("source udpate fails because schema not exists")
        public void updateFailureWhenSchemaNotExists() {

            // Given origin
            Map<String, Object> origin = new HashMap<>();
            origin.put("email", "jean.dupont@gmail.com");
            JsonNode old = MAPPER.convertValue(origin, JsonNode.class);

            // And Patch
            ObjectNode patch = MAPPER.createObjectNode();
            patch.put("op", "replace");
            patch.put("path", "/email");
            patch.put("value", "jack.dupont@gmail.com");

            // And form
            ArrayNode patchs = MAPPER.createArrayNode();
            patchs.add(patch);
            JsonNode model = JsonPatch.apply(patchs, old);

            // When perform
            Throwable throwable = catchThrowable(
                    () -> validation.validate("unknown", "unknown", "unknown", "schema_test", model, old));

            // Then check throwable
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> assertAll(
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode)
                                    .contains("additionalProperties"),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains("/email")));
        }

    }

    @DisplayName("source array update validation")
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
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode).contains("uniqueItems"),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains("")));

        }
    }

    @Nested
    @DisplayName("json schema validation")
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
                            () -> assertThat(exception.getCauses()).hasSize(1),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getCode).contains("format"),
                            () -> assertThat(exception.getCauses()).extracting(ValidationExceptionCause::getPath).contains("/email")));
        }

    }
}
