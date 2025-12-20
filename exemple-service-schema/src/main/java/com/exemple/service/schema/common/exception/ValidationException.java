package com.exemple.service.schema.common.exception;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private final transient List<ValidationExceptionCause> causes;

    public ValidationException(List<ValidationExceptionCause> causes) {
        super(causes.stream().map(ValidationExceptionCause::getMessage).collect(Collectors.joining(System.lineSeparator())));
        this.causes = causes.stream().distinct().toList();
    }

    public ValidationException(List<Error> messages, JsonNode source) {
        this(ValidationExceptionBuilder.buildException(messages, source));
    }

}
