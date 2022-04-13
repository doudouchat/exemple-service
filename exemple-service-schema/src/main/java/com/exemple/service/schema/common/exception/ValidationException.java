package com.exemple.service.schema.common.exception;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.SystemUtils;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Set<ValidationExceptionCause> causes;

    public ValidationException(Set<ValidationExceptionCause> causes) {
        super(causes.stream().map(ValidationExceptionCause::getMessage).collect(Collectors.joining(SystemUtils.LINE_SEPARATOR)));
        this.causes = Collections.unmodifiableSet(causes);
    }

}
