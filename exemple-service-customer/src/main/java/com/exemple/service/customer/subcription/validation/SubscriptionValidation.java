package com.exemple.service.customer.subcription.validation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

public interface SubscriptionValidation {

    void validate(@NotNull JsonNode form, JsonNode old, @NotBlank String app, @NotBlank String version);

}
