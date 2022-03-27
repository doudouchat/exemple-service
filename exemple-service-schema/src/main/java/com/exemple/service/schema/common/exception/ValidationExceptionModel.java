package com.exemple.service.schema.common.exception;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;

import lombok.Getter;

@Getter
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

    @Override
    public String toString() {
        return String.format("ValidationExceptionModel [path=%s, pointer=%s, code=%s, message=%s]", path, pointer, code, message);
    }

}
