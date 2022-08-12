package com.exemple.service.schema.common.exception;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private final Set<ValidationExceptionCause> causes;

    public ValidationException(Set<ValidationExceptionCause> causes) {
        super(causes.stream().map(ValidationExceptionCause::getMessage).collect(Collectors.joining(System.lineSeparator())));
        this.causes = Collections.unmodifiableSet(causes);
    }

}
