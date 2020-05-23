package com.exemple.service.customer.account.validation.impl;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.customer.account.validation.AccountValidation;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;

@Component
@Validated
class AccountValidationImpl implements AccountValidation {

    private final SchemaValidation schemaValidation;

    public AccountValidationImpl(SchemaValidation schemaValidation) {

        this.schemaValidation = schemaValidation;
    }

    @Override
    public void validate(JsonNode form, JsonNode old, String app, String version, String profile) {

        schemaValidation.validate(app, version, "account", profile, form, old);
    }
}
