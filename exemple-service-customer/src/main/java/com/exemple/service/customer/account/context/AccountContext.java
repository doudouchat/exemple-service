package com.exemple.service.customer.account.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public final class AccountContext {

    private static ThreadLocal<AccountContext> executionContext = new ThreadLocal<>();

    private final Map<UUID, JsonNode> accounts = new HashMap<>();

    private AccountContext() {

    }

    public static AccountContext get() {

        if (executionContext.get() == null) {
            executionContext.set(new AccountContext());
        }

        return executionContext.get();
    }

    public static void destroy() {

        executionContext.remove();
    }

    public JsonNode getAccount(UUID id) {
        return accounts.get(id);
    }

    public void setAccount(UUID id, JsonNode account) {
        this.accounts.put(id, account);
    }

}
