package com.exemple.service.schema.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ValidationExceptionCause {

    @JsonIgnore
    private final JsonPointer pointer;

    private final String code;

    @EqualsAndHashCode.Exclude
    private final String message;

    private final JsonNode value;

    public String getPath() {
        return this.pointer.toString();
    }

}
