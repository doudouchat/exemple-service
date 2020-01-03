package com.exemple.service.customer.subcription.validation.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.customer.subcription.validation.SubscriptionValidation;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;

@Component
@Validated
class SubscriptionValidationImpl implements SubscriptionValidation {

    @Autowired
    private SchemaValidation schemaValidation;

    @Override
    public void validate(JsonNode form, JsonNode old, String app, String version) {

        schemaValidation.validate(app, version, "subscription", form, old);
    }
}
