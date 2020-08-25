package com.exemple.service.customer.subscription.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.customer.subscription.exception.SubscriptionServiceNotFoundException;
import com.exemple.service.customer.subscription.validation.SubscriptionValidation;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.exemple.service.resource.subscription.SubscriptionResource;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionResource subscriptionResource;

    private final SubscriptionValidation subscriptionValidation;

    private final SchemaFilter schemaFilter;

    private final ApplicationEventPublisher applicationEventPublisher;

    public SubscriptionServiceImpl(SubscriptionResource subscriptionResource, SubscriptionValidation subscriptionValidation,
            SchemaFilter schemaFilter, ApplicationEventPublisher applicationEventPublisher) {

        this.subscriptionResource = subscriptionResource;
        this.subscriptionValidation = subscriptionValidation;
        this.schemaFilter = schemaFilter;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public boolean save(String email, JsonNode source) {

        ServiceContext context = ServiceContextExecution.context();

        JsonNode subscription = JsonNodeUtils.clone(source);
        JsonNodeUtils.set(subscription, email, SubscriptionField.EMAIL.field);

        subscriptionValidation.validate(subscription, context.getApp(), context.getVersion(), context.getProfile());

        boolean created = !subscriptionResource.get(email).isPresent();

        subscriptionResource.save(email, source);

        EventData eventData = new EventData(subscription, "subscription", EventType.CREATE, context.getApp(), context.getVersion(),
                context.getDate().toString());
        applicationEventPublisher.publishEvent(eventData);

        return created;

    }

    @Override
    public JsonNode get(String email) throws SubscriptionServiceNotFoundException {

        ServiceContext context = ServiceContextExecution.context();

        JsonNode source = subscriptionResource.get(email).orElseThrow(SubscriptionServiceNotFoundException::new);

        return schemaFilter.filter(context.getApp(), context.getVersion(), "subscription", context.getProfile(), source);
    }
}
