package com.exemple.service.customer.login.validation.impl;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.customer.login.validation.LoginValidation;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;

@Component
@Validated
class LoginValidationImpl implements LoginValidation {

    private final SchemaValidation schemaValidation;

    public LoginValidationImpl(SchemaValidation schemaValidation) {

        this.schemaValidation = schemaValidation;
    }

    @Override
    public void validate(JsonNode form, JsonNode old, String app, String version, String profile) {

        schemaValidation.validate(app, version, "login", profile, form, old);
    }
}
