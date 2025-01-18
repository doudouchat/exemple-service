package com.exemple.service.resource.account.username;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

public record AccountUsername(String id,
                              String field,
                              String value,
                              String previousValue) {

    public AccountUsername(
            String id,
            String field,
            JsonNode value,
            JsonNode previousValue) {
        this(id, field, value.textValue(), previousValue.textValue());
    }

    public boolean hasChanged() {
        return !Objects.equals(value, previousValue);
    }

    public boolean hasValue() {
        return value != null;
    }

    public boolean hasPreviousValue() {
        return previousValue != null;
    }
}
