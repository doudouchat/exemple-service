package com.exemple.service.customer.subscription

import org.springframework.util.Assert

import com.exemple.service.context.ServiceContextExecution
import com.exemple.service.customer.common.event.EventType
import com.exemple.service.customer.common.event.ResourceEventPublisher
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

import groovy.transform.CompileDynamic

@CompileDynamic
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final String SUBSCRIPTION = "subscription"

    SubscriptionResource subscriptionResource

    ResourceEventPublisher resourceEventPublisher

    @Override
    boolean save(JsonNode subscription) {

        Assert.isTrue(subscription.path('email').isTextual(), 'email is required')

        String email = subscription.get('email').textValue()

        boolean created = !subscriptionResource.get(email).isPresent()

        ((ObjectNode) subscription).set('subscription_date', new TextNode(ServiceContextExecution.context().date.toString()))

        subscriptionResource.save(subscription)

        resourceEventPublisher.publish(subscription, SUBSCRIPTION, EventType.CREATE)

        created
    }

    @Override
    Optional<JsonNode> get(String email) {

        subscriptionResource.get(email)
    }
}
