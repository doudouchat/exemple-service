package com.exemple.service.resource.common;

import lombok.Getter;

@Getter
public class JsonValidatorException extends Exception {

    private final String key;

    private final String node;

    public JsonValidatorException(String key, String node) {
        this(key, node, null);
    }

    public JsonValidatorException(String key, String node, Throwable cause) {
        super(cause);
        this.key = key;
        this.node = node;
    }

}
