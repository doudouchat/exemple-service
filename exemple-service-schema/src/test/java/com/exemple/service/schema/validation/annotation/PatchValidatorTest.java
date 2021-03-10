package com.exemple.service.schema.validation.annotation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.validation.annotation.Validated;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterables;

@ContextConfiguration(classes = { SchemaTestConfiguration.class })
public class PatchValidatorTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private IExemple exemple;

    @DataProvider(name = "success")
    public static Object[][] success() {

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/lastname");
        patch.put("value", "Dupond");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode argumentPatch = mapper.createArrayNode();
        argumentPatch.add(mapper.convertValue(patch, JsonNode.class));

        return new Object[][] {
                // patch correct
                { argumentPatch },
                // empty
                { mapper.createArrayNode() } };

    }

    @Test(dataProvider = "success")
    public void patchSuccess(ArrayNode argument) {

        exemple.exemple(argument);

    }

    @Test
    public void patchFailure() {

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

        try {

            exemple.exemple(argument);

            Assert.fail("expected ValidationException");

        } catch (ConstraintViolationException e) {

            List<String> paths = e.getConstraintViolations().stream()
                    .map((ConstraintViolation<?> violation) -> Iterables.getLast(violation.getPropertyPath()).getName()).collect(Collectors.toList());

            assertThat(paths, hasSize(2));
            assertThat(paths, hasItems("/0/path", "/1/path"));
        }

    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void patchNullFailure() {

        exemple.exemple(null);

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
