package com.exemple.service.schema.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ValidationExceptionCause {

    private final String path;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final JsonPointer pointer;

    private final String code;

    private final String message;

    private final JsonNode value;

    public ValidationExceptionCause(JsonPointer pointer, String code, String message, JsonNode value) {
        this.pointer = pointer;
        this.path = this.pointer.toString();
        this.code = code;
        this.message = message;
        this.value = value;
    }

}
