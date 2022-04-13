package com.exemple.service.schema.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class ValidationExceptionCause extends Exception {

    private static final long serialVersionUID = 1L;

    private final String path;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final JsonPointer pointer;

    private final String code;

    private final JsonNode value;

    public ValidationExceptionCause(JsonPointer pointer, String code, String message, JsonNode value) {
        super(message);
        this.pointer = pointer;
        this.path = this.pointer.toString();
        this.code = code;
        this.value = value;
    }

}
