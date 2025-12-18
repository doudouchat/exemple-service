package com.exemple.service.schema.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.stream.Streams;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.flipkart.zjsonpatch.JsonPatch;

import lombok.Builder;

@SpringJUnitConfig(SchemaTestConfiguration.class)
class SchemaValidationTest {

    @Autowired
    private SchemaValidation validation;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Stream<Arguments> failures() {

        var patch1 = MAPPER.createObjectNode();
        patch1.put("op", "replace");
        patch1.put("path", "/opt_in_email");
        patch1.put("value", true);

        var patch2 = MAPPER.createObjectNode();
        patch2.put("op", "remove");
        patch2.put("path", "/firstname");

        var cgu1 = Map.of("code", "code_1", "version", "v1");
        var cgu2 = Map.of("code", "code_1", "version", "v2");
        var cgu3 = Map.of("code", "code_2", "version", "v1");

        var patch31 = MAPPER.createObjectNode();
        patch31.put("op", "add");
        patch31.put("path", "/cgus/0");
        patch31.set("value", MAPPER.convertValue(cgu1, JsonNode.class));

        var patch32 = MAPPER.createObjectNode();
        patch32.put("op", "add");
        patch32.put("path", "/cgus/1");
        patch32.set("value", MAPPER.convertValue(cgu1, JsonNode.class));

        var patch41 = MAPPER.createObjectNode();
        patch41.put("op", "add");
        patch41.put("path", "/cgus/0");
        patch41.set("value", MAPPER.convertValue(cgu1, JsonNode.class));

        var patch42 = MAPPER.createObjectNode();
        patch42.put("op", "add");
        patch42.put("path", "/cgus/1");
        patch42.set("value", MAPPER.convertValue(cgu2, JsonNode.class));

        var patch43 = MAPPER.createObjectNode();
        patch43.put("op", "add");
        patch43.put("path", "/cgus/2");
        patch43.set("value", MAPPER.convertValue(cgu3, JsonNode.class));

        var patch5 = MAPPER.createObjectNode();
        patch5.put("op", "add");
        patch5.put("path", "/birthday");
        patch5.put("value", "2018-02-30");

        var patch6 = MAPPER.createObjectNode();
        patch6.put("op", "add");
        patch6.put("path", "/creation_date");
        patch6.put("value", "2018-02-30T12:00:00Z");

        var patch7 = MAPPER.createObjectNode();
        patch7.put("op", "add");
        patch7.put("path", "/id");
        patch7.put("value", UUID.randomUUID().toString());

        var patch8 = MAPPER.createObjectNode();
        patch8.put("op", "add");
        patch8.put("path", "/email");
        patch8.put("value", "toto");

        var patch9 = MAPPER.createObjectNode();
        patch9.put("op", "add");
        patch9.put("path", "/hide");
        patch9.put("value", false);

        var patch10 = MAPPER.createObjectNode();
        patch10.put("op", "add");
        patch10.put("path", "/firstname");
        patch10.put("value", " ");

        var patch11 = MAPPER.createObjectNode();
        patch11.put("op", "add");
        patch11.put("path", "/addresses/home");
        patch11.set("value", MAPPER.convertValue(Map.of("city", "Paris"), JsonNode.class));

        var patch12 = MAPPER.createObjectNode();
        patch12.put("op", "add");
        patch12.put("path", "/addresses");
        patch12.set("value", MAPPER.convertValue(Map.of(
                "holiday1", Map.of("city", "Paris", "street", "rue de la paix"),
                "holiday2", Map.of("city", "Paris", "street", "rue de la paix"),
                "holiday3", Map.of("city", "Paris", "street", "rue de la paix")), JsonNode.class));

        var patch13 = MAPPER.createObjectNode();
        patch13.put("op", "add");
        patch13.put("path", "/email");
        patch13.put("value", "");

        var patch14 = MAPPER.createObjectNode();
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
        void creationSuccess() {

            var model = Map.of(
                    "email", "jean.dupont@gmail.com",
                    "lastname", "Dupont",
                    "firstname", "Jean",
                    "opt_in_email", true,
                    "civility", "Mr",
                    "creation_date", "2019-06-17T19:16:40Z");

            // When perform validate
            var throwable = catchThrowable(
                    () -> validation.validate("schema_test", "default", "default", MAPPER.convertValue(model, JsonNode.class)));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();

        }

        private Stream<Arguments> creationFailure() {
            return failures();
        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source creation fails")
        void creationFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

            // Given model
            var origin = Map.of(
                    "lastname", "Dupont",
                    "firstname", "Jean",
                    "opt_in_email", false,
                    "civility", "Mr",
                    "cgus", MAPPER.createArrayNode(),
                    "addresses", MAPPER.createObjectNode());

            var old = MAPPER.convertValue(origin, JsonNode.class);

            var patch = MAPPER.createArrayNode();
            patch.addAll(Arrays.asList(patchs));

            var model = JsonPatch.apply(patch, old);

            // When perform
            var throwable = catchThrowable(() -> validation.validate("schema_test", "default", "default", model));

            // Then check throwable
            var validationExceptionCause = ExpectedValidationExceptionCause.builder().code(expectedCode).path(expectedPath).build();
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> Assertions.assertThat(exception).isCause(validationExceptionCause));

        }

        @Test
        @DisplayName("source creation fails because schema not exists")
        void creationFailureWhenSchemaNotExists() {

            // Given source
            var model = Map.of("email", "jean.dupont@gmail.com");
            var source = MAPPER.convertValue(model, JsonNode.class);

            // When perform
            var throwable = catchThrowable(
                    () -> validation.validate("schema_test", "unknown", "unknown", source));

            // Then check throwable
            var validationExceptionCause = ExpectedValidationExceptionCause.builder().code("additionalProperties").path("/email").build();
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> Assertions.assertThat(exception).isCause(validationExceptionCause));
        }
    }

    @Nested
    @DisplayName("source array creation validation")
    class ValidateArrayToCreation {

        @Test
        @DisplayName("source creation validation")
        void creationSuccess() {

            // Given build model
            var addresse1 = Map.of("street", "1 rue de la paix", "city", "Paris");
            var addresse2 = Map.of("street", "2 rue de la paix", "city", "Paris");

            var addresses = List.of(addresse1, addresse2);

            var model = MAPPER.convertValue(addresses, JsonNode.class);

            // When perform validate

            validation.validate("array_test", "default", "default", model);

        }

        @Test
        @DisplayName("source creation fails")
        void creationFailure() {

            // Given build model
            var addresse = Map.of("street", "1 rue de la paix");

            var addresses = List.of(addresse);

            var model = MAPPER.convertValue(addresses, JsonNode.class);

            // When perform
            var throwable = catchThrowable(() -> validation.validate("array_test", "default", "default", model));

            // Then check throwable
            var validationExceptionCause = ExpectedValidationExceptionCause.builder().code("required").path("/0/city").build();
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> Assertions.assertThat(exception).isCause(validationExceptionCause));

        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("source patch validation")
    class ValidateToPatch {

        Stream<Arguments> patchSuccess() {

            var patch1 = MAPPER.createObjectNode();
            patch1.put("op", "replace");
            patch1.put("path", "/email");
            patch1.put("value", "jack.dupond@gmail.com");

            var patch2 = MAPPER.createObjectNode();
            patch2.put("op", "add");
            patch2.put("path", "/addresses/holidays");
            patch2.set("value", MAPPER.convertValue(Map.of("city", "New York", "street", "5th avenue"), JsonNode.class));

            var cgu = Map.of("code", "code_1", "version", "v1");

            var patch3 = MAPPER.createObjectNode();
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
        void patchSuccess(JsonNode patch) {

            // build source
            var origin = Map.of(
                    "email", "jean.dupont@gmail.com",
                    "lastname", "Dupont",
                    "firstname", "Jean",
                    "opt_in_email", true,
                    "civility", "Mr",
                    "addresses", MAPPER.createObjectNode(),
                    "cgus", MAPPER.createArrayNode());
            var old = MAPPER.convertValue(origin, JsonNode.class);

            var patchs = MAPPER.createArrayNode();
            patchs.add(patch);

            // When perform validate
            var throwable = catchThrowable(() -> validation.validate("schema_test", "default", "default", patchs, old));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();
        }

        Stream<Arguments> patchFailure() {

            var patch1 = MAPPER.createObjectNode();
            patch1.put("op", "add");
            patch1.put("path", "/external_id");
            patch1.put("value", UUID.randomUUID().toString());

            var patch2 = MAPPER.createObjectNode();
            patch2.put("op", "remove");
            patch2.put("path", "/id");

            var patch3 = MAPPER.createObjectNode();
            patch3.put("op", "remove");
            patch3.put("path", "/hide");

            return Stream.concat(
                    failures(),
                    Stream.of(
                            // external_id read only
                            Arguments.of("readOnly", "/external_id", new JsonNode[] { patch1 }),
                            // id read only
                            Arguments.of("readOnly", "/id", new JsonNode[] { patch2 }),
                            // hide additionalProperties
                            Arguments.of("additionalProperties", "/hide", new JsonNode[] { patch3 })));
        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source patch fails")
        void patchFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

            // Given origin
            var origin = Map.of(
                    "opt_in_email", false,
                    "lastname", "doe",
                    "firstname", "john",
                    "cgus", MAPPER.createArrayNode(),
                    "addresses", MAPPER.createObjectNode(),
                    "hide", BooleanNode.TRUE);

            var old = MAPPER.convertValue(origin, JsonNode.class);

            // And form
            var patch = MAPPER.createArrayNode();
            patch.addAll(Arrays.asList(patchs));

            // When perform
            var throwable = catchThrowable(() -> validation.validate("schema_test", "default", "default", patch, old));

            // Then check throwable
            var validationExceptionCause = ExpectedValidationExceptionCause.builder().code(expectedCode).path(expectedPath).build();
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> Assertions.assertThat(exception).isCause(validationExceptionCause));
        }

        private Stream<Arguments> patchFailures() {

            var patch1 = MAPPER.createObjectNode();
            patch1.put("op", "add");
            patch1.put("path", "/external_id");
            patch1.set("value", NullNode.instance);

            return Stream.of(
                    // external_id create only
                    Arguments.of(new String[] { "readOnly", "type" }, "/external_id", new JsonNode[] { patch1 }));
        }

        @ParameterizedTest
        @MethodSource("patchFailures")
        @DisplayName("source patch fails")
        void patchFailure(String[] expectedCodes, String expectedPath, JsonNode... patchs) {

            // Given origin
            var origin = Map.of(
                    "opt_in_email", false,
                    "lastname", "doe",
                    "firstname", "john",
                    "cgus", MAPPER.createArrayNode(),
                    "addresses", MAPPER.createObjectNode(),
                    "hide", BooleanNode.TRUE);

            var old = MAPPER.convertValue(origin, JsonNode.class);

            // And form
            var patch = MAPPER.createArrayNode();
            patch.addAll(Arrays.asList(patchs));

            // When perform
            var throwable = catchThrowable(() -> validation.validate("schema_test", "default", "default", patch, old));

            // Then check throwable
            var validationExceptionCauses = Streams.of(expectedCodes)
                    .map(expectedCode -> ExpectedValidationExceptionCause.builder().code(expectedCode).path(expectedPath).build())
                    .toList();
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> Assertions.assertThat(exception).containCauses(validationExceptionCauses));
        }

        Stream<Arguments> fixIncorrectSource() {

            var patch1 = MAPPER.createObjectNode();
            patch1.put("op", "replace");
            patch1.put("path", "/email");
            patch1.put("value", "jean.dupond@gmail.com");

            var patch2 = MAPPER.createObjectNode();
            patch2.put("op", "remove");
            patch2.put("path", "/cgus/0");

            var patch3 = MAPPER.createObjectNode();
            patch3.put("op", "add");
            patch3.put("path", "/addresses/home/street");
            patch3.put("value", "1 rue de la paix");

            var patch4 = MAPPER.createObjectNode();
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
        void fixIncorrectSource(JsonNode patch) {

            // Given origin
            var origin = Map.of(
                    // And email incorrect
                    "email", "toto",
                    // And cgu incorrect
                    "cgus", List.of(Map.of("code", "code_1", "version", "v1"), Map.of("code", "code_1", "version", "v1")),
                    // And max cgv
                    "cgvs", List.of(Map.of("code", "code_1", "version", "v1"), Map.of("code", "code_2", "version", "v1"),
                            Map.of("code", "code_3", "version", "v1")),
                    // And addresses incorrect
                    "addresses", Map.of("home", Map.of("city", "Paris")));

            // And form
            var patchs = MAPPER.createArrayNode();
            patchs.add(patch);

            // When perform
            var old = MAPPER.convertValue(origin, JsonNode.class);
            var throwable = catchThrowable(() -> validation.validate("schema_test", "default", "default", patchs, old));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();
        }

        Stream<Arguments> fixIncorrectSourceFailure() {

            var patch1 = MAPPER.createObjectNode();
            patch1.put("op", "replace");
            patch1.put("path", "/email");
            patch1.put("value", "tata");

            var patch21 = MAPPER.createObjectNode();
            patch21.put("op", "replace");
            patch21.put("path", "/cgus/0");
            patch21.set("value", MAPPER.convertValue(Map.of("code", "code_1", "version", "v2"), JsonNode.class));

            var patch22 = MAPPER.createObjectNode();
            patch22.put("op", "replace");
            patch22.put("path", "/cgus/1");
            patch22.set("value", MAPPER.convertValue(Map.of("code", "code_1", "version", "v2"), JsonNode.class));

            var patch3 = MAPPER.createObjectNode();
            patch3.put("op", "replace");
            patch3.put("path", "/addresses/holiday");
            patch3.set("value", MAPPER.convertValue(Map.of("city", "Paris", "street", "2 rue de la paix"), JsonNode.class));

            var patch41 = MAPPER.createObjectNode();
            patch41.put("op", "remove");
            patch41.put("path", "/addresses/holiday");

            var patch42 = MAPPER.createObjectNode();
            patch42.put("op", "replace");
            patch42.put("path", "/addresses/home");
            var home = Map.of("city", "Londres");
            patch42.set("value", MAPPER.convertValue(home, JsonNode.class));

            var patch5 = MAPPER.createObjectNode();
            patch5.put("op", "add");
            patch5.put("path", "/cgvs/0");
            patch5.set("value", MAPPER.convertValue(Map.of("code", "code_4", "version", "v1"), JsonNode.class));

            var patch6 = MAPPER.createObjectNode();
            patch6.put("op", "replace");
            patch6.put("path", "/lastname");
            patch6.put("value", " ");

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
                    Arguments.of("maxItems", "/cgvs", new JsonNode[] { patch5 }),
                    // pattern
                    Arguments.of("pattern", "/lastname", new JsonNode[] { patch6 }));

        }

        @ParameterizedTest
        @MethodSource
        @DisplayName("source patch fails when fixing error")
        void fixIncorrectSourceFailure(String expectedCode, String expectedPath, JsonNode... patchs) {

            // Given origin
            var origin = Map.of(
                    // And lastname is empty
                    "lastname", "",
                    // And firstname is blank
                    "firstname", " ",
                    // And email incorrect
                    "email", "toto",
                    // And cgu incorrect
                    "cgus", List.of(Map.of("code", "code_1", "version", "v1"), Map.of("code", "code_1", "version", "v1")),
                    // And max cgv
                    "cgvs", List.of(Map.of("code", "code_1", "version", "v1"), Map.of("code", "code_2", "version", "v1"),
                            Map.of("code", "code_3", "version", "v1")),
                    // And addresses incorrect
                    "addresses", Map.of(
                            "holiday", Map.of("city", "Paris", "street", "rue de la paix"),
                            "job", Map.of("city", "Paris", "street", "rue de la paix"),
                            "home", Map.of("city", "Paris")));

            // And form
            var patch = MAPPER.createArrayNode();
            patch.addAll(Arrays.asList(patchs));

            // When perform
            var old = MAPPER.convertValue(origin, JsonNode.class);
            var throwable = catchThrowable(() -> validation.validate("schema_test", "default", "default", patch, old));

            // Then check throwable
            var validationExceptionCause = ExpectedValidationExceptionCause.builder().code(expectedCode).path(expectedPath).build();
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> Assertions.assertThat(exception).isCause(validationExceptionCause));
        }

        @Test
        @DisplayName("source patch fails because schema not exists")
        void patchFailureWhenSchemaNotExists() {

            // Given origin

            var origin = Map.of("email", "jean.dupond@gmail.com");
            var old = MAPPER.convertValue(origin, JsonNode.class);

            // And Patch
            var patch = MAPPER.createObjectNode();
            patch.put("op", "replace");
            patch.put("path", "/email");
            patch.put("value", "jack.dupont@gmail.com");

            var patchs = MAPPER.createArrayNode();
            patchs.add(patch);

            // When perform
            var throwable = catchThrowable(
                    () -> validation.validate("schema_test", "unknown", "unknown", patchs, old));

            // Then check throwable
            var validationExceptionCause = ExpectedValidationExceptionCause.builder().code("additionalProperties").path("/email").build();
            assertThat(throwable).isInstanceOfSatisfying(ValidationException.class,
                    exception -> Assertions.assertThat(exception).isCause(validationExceptionCause));
        }

    }

    static class ValidationExceptionAssert extends AbstractAssert<ValidationExceptionAssert, ValidationException> {

        public ValidationExceptionAssert(ValidationException exception) {
            super(exception, ValidationExceptionAssert.class);
        }

        public ValidationExceptionAssert containCauses(List<ExpectedValidationExceptionCause> expectedCauses) {
            isNotNull();

            assertThat(this.actual.getCauses()).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFields("message", "value")
                    .isEqualTo(expectedCauses.stream()
                            .map(expectedCause -> expectedCause.buildValidationExceptionCause())
                            .toList());
            return this;
        }

        public ValidationExceptionAssert isCause(ExpectedValidationExceptionCause expectedCause) {
            return containCauses(List.of(expectedCause));
        }

    }

    class Assertions {
        public static ValidationExceptionAssert assertThat(ValidationException actual) {
            return new ValidationExceptionAssert(actual);
        }
    }

    @Builder
    public static record ExpectedValidationExceptionCause(String code,
                                                          String path) {

        ValidationExceptionCause buildValidationExceptionCause() {
            return ValidationExceptionCause.builder().code(code).pointer(JsonPointer.valueOf(path)).build();
        }

    }
}
