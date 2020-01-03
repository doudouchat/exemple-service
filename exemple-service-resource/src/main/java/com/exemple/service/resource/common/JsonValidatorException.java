package com.exemple.service.resource.common;

public class JsonValidatorException extends Exception {

    private static final long serialVersionUID = 1L;

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

    public String getKey() {
        return key;
    }

    public String getNode() {
        return node;
    }

    public String getMessage(String messageTemplate) {

        StringBuilder message = new StringBuilder();
        message.append(messageTemplate.replace("{", "").replace("}", ""));
        message.append('.');
        message.append(getKey());

        return message.toString();
    }

}
