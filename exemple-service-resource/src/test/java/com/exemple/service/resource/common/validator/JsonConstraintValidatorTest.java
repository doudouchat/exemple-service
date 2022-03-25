package com.exemple.service.resource.common.validator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringJUnitConfig(ResourceTestConfiguration.class)
public class JsonConstraintValidatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountResource resource;

    @Test
    public void success() throws IOException {

        // Given build account
        UUID id = UUID.randomUUID();
        JsonNode account = MAPPER.readTree("{"
                + "  \"id\" : \"" + id + "\","
                + "  \"email\" : \"jean.dupont@gmail.com\","
                + "  \"address\" : {"
                + "    \"street\" : \"1 rue de la paix\","
                + "    \"city\" : \"Paris\","
                + "    \"zip\" : \"75002\","
                + "    \"floor\" : 5"
                + "  },"
                + "  \"addresses\" : {"
                + "    \"home\" : {"
                + "      \"street\" : \"1 rue de de la poste\""
                + "    }"
                + "  },"
                + "  \"profils\" : [ \"profil 1\" ],"
                + "  \"preferences\" : [ [ \"pref1\", \"value1\", 10, \"2001-01-01 00:00:00.000Z\" ] ]"
                + "}");

        // When perform save
        resource.save(account);

        // Then check result
        JsonNode result = resource.get(id).get();
        assertAll(
                () -> assertThat(result.get("email"), is(notNullValue())),
                () -> assertThat(result.get("address"), is(notNullValue())),
                () -> assertThat(result.get("addresses"), is(notNullValue())),
                () -> assertThat(result.get("profils"), is(notNullValue())),
                () -> assertThat(result.get("preferences"), is(notNullValue())));

    }

    private static Stream<Arguments> saveFailure() {

        return Stream.of(
                // text failure
                Arguments.of("email", 10),
                // int failure
                Arguments.of("age", "age"),
                // field unknown
                Arguments.of("nc", "nc"),
                // date failure
                Arguments.of("birthday", "2019-02-30"), Arguments.of("birthday", "aaa"),
                // timestamp failure
                Arguments.of("creation_date", "2019-02-30T10:00:00Z"), Arguments.of("creation_date", "aaa"),
                // boolean failure
                Arguments.of("enabled", 10),
                // map failure
                Arguments.of("addresses", 10),
                // map int failure
                Arguments.of("addresses", Collections.singletonMap("home", Collections.singletonMap("floor", "toto"))),
                // map field unknown
                Arguments.of("addresses", Collections.singletonMap("home", Collections.singletonMap("nc", "toto"))),
                // map boolean failure
                Arguments.of("addresses", Collections.singletonMap("home", Collections.singletonMap("enable", "toto"))),
                // map date failure
                Arguments.of("children", Collections.singletonMap("1", Collections.singletonMap("birthday", "2019-02-30"))),
                Arguments.of("children", Collections.singletonMap("1", Collections.singletonMap("birthday", "aaa"))),
                // map index int failure
                Arguments.of("children", Collections.singletonMap("aaa", Collections.singletonMap("birthday", "2001-01-01"))),
                // map index timestamp failure
                Arguments.of("notes", Collections.singletonMap("2019-02-30T10:00:00Z", "note 1")),
                Arguments.of("notes", Collections.singletonMap("aaa", "note 1")),
                // set failure
                Arguments.of("cgus", 10),
                // list failure
                Arguments.of("preferences", 10),
                // tuple int failure
                Arguments.of("preferences", Arrays.asList(Arrays.asList("pref2", "value2", "aaa", "2002-01-01 00:00:00.000Z"))),
                // tuple timestamp failure
                Arguments.of("preferences", Arrays.asList(Arrays.asList("pref2", "value2", 100, "aaa"))),
                // tuple too fields
                Arguments.of("preferences", Arrays.asList(Arrays.asList("pref2", "value2", 100, "2002-01-01 00:00:00.000Z", "new"))),
                // tuple fields missing
                Arguments.of("preferences", Arrays.asList(Arrays.asList("pref2", "value2", 100))));

    }

    @DisplayName("save fails because json is incorrect")
    @ParameterizedTest
    @MethodSource
    public void saveFailure(String property, Object value) {

        // setup source
        ObjectNode node = MAPPER.createObjectNode();
        node.set(property, MAPPER.convertValue(value, JsonNode.class));

        // When perform save
        Throwable throwable = catchThrowable(() -> resource.save(node));

        // Then check throwable
        assertThat(throwable, instanceOf(ConstraintViolationException.class));

    }
}
