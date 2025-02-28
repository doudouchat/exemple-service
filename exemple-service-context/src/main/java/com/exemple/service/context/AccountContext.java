package com.exemple.service.context;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class AccountContext {

    private final Optional<JsonNode> previousAccount;

}
