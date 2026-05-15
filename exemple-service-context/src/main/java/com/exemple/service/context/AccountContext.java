package com.exemple.service.context;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public record AccountContext(JsonNode previousAccount) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final ScopedValue<AccountContext> ACCOUNT_CONTEXT = ScopedValue.newInstance();

    public AccountContext() {
        this(MAPPER.createObjectNode());
    }

}
