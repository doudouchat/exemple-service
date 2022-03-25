package com.exemple.service.resource.common.validator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(ResourceTestConfiguration.class)
public class NotEmptyConstraintValidatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountResource resource;

    @Test
    public void updateSuccess() throws IOException {

        // Given build account
        UUID id = UUID.randomUUID();
        JsonNode account = MAPPER.readTree("{\"id\": \"" + id + "\", \"email\": \"jean.dupond@gmail\"}");

        // When perform save
        resource.save(account);

        // Then check result
        JsonNode result = resource.get(id).get();
        assertThat(result.get("email"), is(notNullValue()));

    }

    private static Stream<Arguments> updateFailure() {

        return Stream.of(
                Arguments.of(MAPPER.createObjectNode()));
    }

    @DisplayName("save fails because json is empty")
    @ParameterizedTest
    @MethodSource
    @NullSource
    public void updateFailure(JsonNode account) {

        // When perform save
        Throwable throwable = catchThrowable(() -> resource.save(account));

        // Then check throwable
        assertThat(throwable, instanceOf(ConstraintViolationException.class));

    }

}
