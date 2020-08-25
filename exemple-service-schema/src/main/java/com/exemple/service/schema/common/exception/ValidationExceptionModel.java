package com.exemple.service.schema.common.exception;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;

public class ValidationExceptionModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String path;

    @JsonIgnore
    private final JsonPointer pointer;

    private final String code;

    private final String message;

    public ValidationExceptionModel(JsonPointer pointer, String code, String message) {
        this.pointer = pointer;
        this.path = this.pointer.toString();
        this.code = code;
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public JsonPointer getPointer() {
        return pointer;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, path);
    }

    @Override
    public boolean equals(Object obj) {
        Assert.notNull(obj, "obj is required");
        ValidationExceptionModel other = (ValidationExceptionModel) obj;
        return new EqualsBuilder().append(code, other.code).append(message, other.message).append(path, other.path).isEquals();
    }

}
