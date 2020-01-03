package com.exemple.service.schema.common.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<ValidationExceptionModel> allExceptions;

    public ValidationException(Throwable cause) {
        super(cause);
        this.allExceptions = new ArrayList<>();
    }

    public ValidationException() {
        this(null);
    }

    public List<ValidationExceptionModel> getAllExceptions() {
        return Collections.unmodifiableList(allExceptions);
    }

    public void add(ValidationExceptionModel cause) {
        allExceptions.add(cause);
    }

    public boolean contains(String path) {
        Assert.notNull(path, "Path is required");
        return allExceptions.stream().anyMatch(e -> path.equals(e.getPath()));
    }

    public static class ValidationExceptionModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String path;

        private final String code;

        private final String message;

        public ValidationExceptionModel(String path, String code, String message) {
            this.path = path;
            this.code = code;
            this.message = message;
        }

        public String getPath() {
            return path;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

    }

}
