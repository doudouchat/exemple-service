package com.exemple.service.schema.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { SchemaTestConfiguration.class })
public class SchemaValidationArrayTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SchemaValidation validation;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void validation() {

        Map<String, Object> addresse1 = new HashMap<>();
        addresse1.put("street", "1 rue de la paix");
        addresse1.put("city", "Paris");

        Map<String, Object> addresse2 = new HashMap<>();
        addresse2.put("street", "2 rue de la paix");
        addresse2.put("city", "Paris");

        List<Object> addresses = new ArrayList<>();
        addresses.add(addresse1);
        addresses.add(addresse2);

        validation.validate("default", "default", "default", "array_test", MAPPER.convertValue(addresses, JsonNode.class));

    }

    @Test
    public void validationFailure() {

        Map<String, Object> addresse = new HashMap<>();
        addresse.put("street", "1 rue de la paix");

        List<Object> addresses = new ArrayList<>();
        addresses.add(addresse);

        try {

            validation.validate("default", "default", "default", "array_test", MAPPER.convertValue(addresses, JsonNode.class));

        } catch (ValidationException e) {

            assertThat(e.getAllExceptions().size(), is(1));

            assertThat(e.getAllExceptions(), contains(hasProperty("path", is("/0/city"))));
            assertThat(e.getAllExceptions(), contains(hasProperty("code", is("required"))));
        }

    }

}
