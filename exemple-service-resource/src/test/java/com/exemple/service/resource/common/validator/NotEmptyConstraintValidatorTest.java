package com.exemple.service.resource.common.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.resource.core.ResourceTestConfiguration;

import jakarta.validation.ConstraintViolationException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = ResourceTestConfiguration.class)
@ActiveProfiles("test")
class NotEmptyConstraintValidatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountResource resource;

    @Test
    void updateSuccess() {

        // Given build account
        UUID id = UUID.randomUUID();
        JsonNode account = MAPPER.readTree(
                """
                {"id": "%s", "email": "%s"}
                """.formatted(id, id + "@gmail.com"));

        // When perform save
        resource.create(account);

        // Then check result
        JsonNode result = resource.get(id).get();
        assertThat(result.get("email")).isNotNull();

    }

    static Stream<Arguments> updateFailure() {

        return Stream.of(
                Arguments.of(MAPPER.createObjectNode()));
    }

    @DisplayName("save fails because json is empty")
    @ParameterizedTest
    @MethodSource
    @NullSource
    void updateFailure(JsonNode account) {

        // When perform save
        Throwable throwable = catchThrowable(() -> resource.create(account));

        // Then check throwable
        assertThat(throwable).isInstanceOf(ConstraintViolationException.class);

    }

}
