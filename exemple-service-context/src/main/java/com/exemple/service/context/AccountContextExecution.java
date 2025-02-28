package com.exemple.service.context;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class AccountContextExecution {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ThreadLocal<AccountContextExecution> executionContext = ThreadLocal.withInitial(AccountContextExecution::new);

    private AccountContext model;

    private AccountContextExecution() {

        this.model = AccountContext.builder()
                .previousAccount(Optional.empty())
                .build();
    }

    private void reset(AccountContext model) {
        this.model = model;
    }

    public static void setPreviousAccount(JsonNode previousAccount) {

        var model = executionContext.get().model.toBuilder().previousAccount(Optional.of(previousAccount)).build();
        executionContext.get().reset(model);
    }

    public static JsonNode getPreviousAccount() {

        return executionContext.get().model.getPreviousAccount().orElseGet(MAPPER::createObjectNode);
    }

    public static void destroy() {

        executionContext.remove();
    }

}
