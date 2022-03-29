package com.exemple.service.schema.common.exception;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ValidationExceptionModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String path;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final JsonPointer pointer;

    private final String code;

    private final String message;

    public ValidationExceptionModel(JsonPointer pointer, String code, String message) {
        this.pointer = pointer;
        this.path = this.pointer.toString();
        this.code = code;
        this.message = message;
    }

}
