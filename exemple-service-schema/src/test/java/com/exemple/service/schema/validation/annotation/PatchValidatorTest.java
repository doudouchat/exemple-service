package com.exemple.service.schema.validation.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterables;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path.Node;

@SpringJUnitConfig(SchemaTestConfiguration.class)
class PatchValidatorTest {

    @Autowired
    private IExemple exemple;

    private static Stream<Arguments> patchSuccess() {

        Map<String, Object> patch = Map.of(
                "op", "add",
                "path", "/lastname",
                "value", "Dupond");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode argumentPatch = mapper.createArrayNode();
        argumentPatch.add(mapper.convertValue(patch, JsonNode.class));

        return Stream.of(
                // patch correct
                Arguments.of(argumentPatch),
                // empty
                Arguments.of(mapper.createArrayNode()));

    }

    @ParameterizedTest
    @MethodSource
    void patchSuccess(ArrayNode argument) {

        // When perform validate
        Throwable throwable = catchThrowable(() -> exemple.exemple(argument));

        // Then check none exception
        assertThat(throwable).as("None exception is expected").isNull();

    }

    private static Stream<Arguments> patchFailure() {

        Map<String, Object> patch1 = Map.of(
                "op", "bad",
                "path", "/lastname",
                "value", "Dupond");

        Map<String, Object> patch2 = Map.of(
                "op", "add",
                "path", "lastname",
                "value", "Dupond");

        return Stream.of(
                // bad op
                Arguments.of(patch1, 2, new String[] { "/0/from", "/0/op" }),
                // bad pattern
                Arguments.of(patch2, 1, new String[] { "/0/path" }));
    }

    @ParameterizedTest
    @MethodSource
    void patchFailure(Map<String, Object> patch, int expectedExceptionSize, String... expectedPropertyPath) {

        // setup source
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode argument = mapper.createArrayNode();
        argument.add(mapper.convertValue(patch, JsonNode.class));

        // When perform
        Throwable throwable = catchThrowable(() -> exemple.exemple(argument));

        // Then check throwable
        assertThat(throwable).isInstanceOfSatisfying(ConstraintViolationException.class,
                exception -> assertAll(
                        () -> assertThat(exception.getConstraintViolations()).hasSize(expectedExceptionSize),
                        () -> assertThat(exception.getConstraintViolations())
                                .extracting(violation -> Iterables.getLast(violation.getPropertyPath()))
                                .extracting(Node::getName).contains(expectedPropertyPath)));
    }

    @Test
    void patchNullFailure() {

        // When perform
        Throwable throwable = catchThrowable(() -> exemple.exemple(null));

        // Then check throwable
        assertThat(throwable).isInstanceOfSatisfying(ConstraintViolationException.class,
                exception -> assertThat(exception.getConstraintViolations()).hasSize(1));
    }

    private static interface IExemple {

        void exemple(@Patch ArrayNode patch);

    }

    @Component
    @Validated
    private static class Exemple implements IExemple {

        @Override
        public void exemple(ArrayNode patch) {

        }

    }

}
