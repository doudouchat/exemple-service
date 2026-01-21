package com.exemple.service.schema.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

@Builder
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
