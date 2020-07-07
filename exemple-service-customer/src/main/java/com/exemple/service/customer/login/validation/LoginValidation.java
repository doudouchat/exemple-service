package com.exemple.service.customer.login.validation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

public interface LoginValidation {

    void validate(@NotNull JsonNode form, @NotBlank String app, @NotBlank String version, @NotBlank String profile);

    void validate(@NotNull JsonNode form, @NotNull JsonNode old, @NotBlank String app, @NotBlank String version, @NotBlank String profile);

}
