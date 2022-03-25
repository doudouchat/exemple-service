package com.exemple.service.schema.validation.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.validation.ConstraintViolationException;
import javax.validation.Path.Node;

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

@SpringJUnitConfig(SchemaTestConfiguration.class)
public class PatchValidatorTest {

    @Autowired
    private IExemple exemple;

    private static Stream<Arguments> patchSuccess() {

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/lastname");
        patch.put("value", "Dupond");

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
    public void patchSuccess(ArrayNode argument) {

        exemple.exemple(argument);

    }

    @Test
    public void patchFailure() {

        // setup source
        Map<String, Object> patch0 = new HashMap<>();
        patch0.put("op", "add");
        patch0.put("value", "Dupond");

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "replace");
        patch1.put("value", "Joe");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode argument = mapper.createArrayNode();
        argument.add(mapper.convertValue(patch0, JsonNode.class));
        argument.add(mapper.convertValue(patch1, JsonNode.class));

        // When perform
        Throwable throwable = catchThrowable(() -> exemple.exemple(argument));

        // Then check throwable
        assertThat(throwable).isInstanceOfSatisfying(ConstraintViolationException.class,
                exception -> assertAll(
                        () -> assertThat(exception.getConstraintViolations()).hasSize(2),
                        () -> assertThat(exception.getConstraintViolations())
                                .extracting(violation -> Iterables.getLast(violation.getPropertyPath()))
                                .extracting(Node::getName).contains("/0/path", "/1/path")));
    }

    @Test
    public void patchNullFailure() {

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
