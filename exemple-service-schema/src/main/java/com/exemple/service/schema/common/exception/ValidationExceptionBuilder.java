package com.exemple.service.schema.common.exception;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationExceptionBuilder {

    public static List<ValidationExceptionCause> buildException(Collection<Error> validationMessages, JsonNode source) {

        return validationMessages.stream().map(validationMessage -> ValidationExceptionBuilder.build(validationMessage, source)).toList();
    }

    private static ValidationExceptionCause build(Error exception, JsonNode source) {

        var path = switch (exception.getKeyword()) {
            case "required" -> exception.getInstanceLocation() + String.valueOf(JsonPointer.SEPARATOR) + exception.getArguments()[0];
            case "additionalProperties" -> exception.getInstanceLocation() + String.valueOf(JsonPointer.SEPARATOR) + exception.getArguments()[0];
            default -> exception.getInstanceLocation().toString();
        };

        JsonNode value = source.at(path);
        if (value.isMissingNode() && !JsonPointer.compile(path).head().toString().isEmpty()) {
            value = source.at(JsonPointer.compile(path).head());
        }

        return new ValidationExceptionCause(JsonPointer.compile(path), exception.getKeyword(), exception.getMessage(), value);
    }

}
