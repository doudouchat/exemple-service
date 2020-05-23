package com.exemple.service.customer.subcription;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.customer.subcription.exception.SubscriptionServiceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;

public interface SubscriptionService {

    boolean save(@NotBlank String email, @NotNull JsonNode source, @NotBlank String app, @NotBlank String version, @NotBlank String profile);

    JsonNode get(@NotBlank String email, @NotBlank String app, @NotBlank String version, @NotBlank String profile)
            throws SubscriptionServiceNotFoundException;
}
