package com.exemple.service.resource.account.username;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import tools.jackson.databind.JsonNode;

public record AccountUsername(String id,
                              String field,
                              String value,
                              String previousValue) {

    public AccountUsername(
            String id,
            String field,
            JsonNode value,
            JsonNode previousValue) {
        this(id, field, value.asString(null), previousValue.asString(null));
    }

    public boolean hasChanged() {
        return !Objects.equals(value, previousValue);
    }

    public boolean hasValue() {
        return StringUtils.isNotEmpty(value);
    }

    public boolean hasPreviousValue() {
        return previousValue != null;
    }
}
