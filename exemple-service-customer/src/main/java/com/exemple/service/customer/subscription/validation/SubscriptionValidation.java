package com.exemple.service.customer.subscription.validation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

public interface SubscriptionValidation {

    void validate(@NotNull JsonNode form, @NotBlank String app, @NotBlank String version, @NotBlank String profile);

}
