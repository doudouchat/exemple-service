package com.exemple.service.schema.common.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<ValidationExceptionModel> allExceptions;

    public ValidationException() {
        this.allExceptions = new ArrayList<>();
    }

    public ValidationException(Throwable cause) {
        super(cause);
        this.allExceptions = new ArrayList<>();
    }

    public List<ValidationExceptionModel> getAllExceptions() {
        return Collections.unmodifiableList(allExceptions);
    }

    public void add(ValidationExceptionModel cause) {
        allExceptions.add(cause);
    }

}
