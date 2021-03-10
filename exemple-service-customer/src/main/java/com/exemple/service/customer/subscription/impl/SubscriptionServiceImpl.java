package com.exemple.service.customer.subscription.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.core.script.CustomiseResourceHelper;
import com.exemple.service.customer.core.script.CustomiseValidationHelper;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.customer.subscription.exception.SubscriptionServiceNotFoundException;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.exemple.service.resource.subscription.SubscriptionResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final String SUBSCRIPTION = "subscription";

    private final SubscriptionResource subscriptionResource;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final CustomiseResourceHelper customiseResourceHelper;

    private final CustomiseValidationHelper customiseValidationHelper;

    public SubscriptionServiceImpl(SubscriptionResource subscriptionResource, ApplicationEventPublisher applicationEventPublisher,
            CustomiseResourceHelper customiseResourceHelper, CustomiseValidationHelper customiseValidationHelper) {

        this.subscriptionResource = subscriptionResource;
        this.applicationEventPublisher = applicationEventPublisher;
        this.customiseResourceHelper = customiseResourceHelper;
        this.customiseValidationHelper = customiseValidationHelper;
    }

    @Override
    public boolean save(JsonNode source) {

        customiseValidationHelper.validate(SUBSCRIPTION, source);

        Assert.isTrue(source.path(SubscriptionField.EMAIL.field).isTextual(), SubscriptionField.EMAIL.field + " is required");

        String email = source.get(SubscriptionField.EMAIL.field).textValue();

        boolean created = !subscriptionResource.get(email).isPresent();

        JsonNode subscription = customiseResourceHelper.customise(SUBSCRIPTION, source);

        subscriptionResource.save(subscription);

        publish(subscription, EventType.CREATE);

        return created;

    }

    @Override
    public JsonNode get(String email) throws SubscriptionServiceNotFoundException {

        return subscriptionResource.get(email).orElseThrow(SubscriptionServiceNotFoundException::new);
    }

    private void publish(JsonNode subscription, EventType type) {

        ServiceContext context = ServiceContextExecution.context();

        EventData eventData = new EventData(subscription, SUBSCRIPTION, type, context.getApp(), context.getVersion(), context.getDate().toString());
        applicationEventPublisher.publishEvent(eventData);
    }
}
