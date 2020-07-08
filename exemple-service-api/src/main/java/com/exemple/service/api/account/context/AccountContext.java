package com.exemple.service.api.account.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.fasterxml.jackson.databind.JsonNode;

@Component
@RequestScope
public class AccountContext {

    private final Map<UUID, JsonNode> accounts = new HashMap<>();

    public JsonNode getAccount(UUID id) {
        return accounts.get(id);
    }

    public void setAccount(UUID id, JsonNode account) {
        this.accounts.put(id, account);
    }

}
