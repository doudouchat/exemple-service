package com.exemple.service.customer.subscription.validation.impl;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.customer.subscription.validation.SubscriptionValidation;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;

@Component
@Validated
class SubscriptionValidationImpl implements SubscriptionValidation {

    private final SchemaValidation schemaValidation;

    public SubscriptionValidationImpl(SchemaValidation schemaValidation) {

        this.schemaValidation = schemaValidation;
    }

    @Override
    public void validate(JsonNode form, String app, String version, String profile) {

        schemaValidation.validate(app, version, "subscription", profile, form);
    }

}
