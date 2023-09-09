package com.exemple.service.schema.common.exception;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationExceptionBuilder {

    public static Set<ValidationExceptionCause> buildException(Collection<ValidationMessage> validationMessages, JsonNode source) {

        return validationMessages.stream().map(validationMessage -> ValidationExceptionBuilder.build(validationMessage, source))
                .collect(Collectors.toSet());
    }

    private static ValidationExceptionCause build(ValidationMessage exception, JsonNode source) {

        String path = switch (exception.getType()) {
            case "required" -> exception.getPath().concat("/").concat(exception.getArguments()[0]);
            case "additionalProperties" -> exception.getPath().concat("/").concat(exception.getArguments()[0]);
            default -> exception.getPath();
        };

        JsonNode value = source.at(path);
        if (value.isMissingNode() && !JsonPointer.compile(path).head().toString().isEmpty()) {
            value = source.at(JsonPointer.compile(path).head());
        }

        return new ValidationExceptionCause(JsonPointer.compile(path), exception.getType(), exception.getMessage(), value);
    }

}
