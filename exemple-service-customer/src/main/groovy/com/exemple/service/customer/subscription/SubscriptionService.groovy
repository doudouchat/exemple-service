package com.exemple.service.customer.subscription

import com.exemple.service.context.ServiceContextExecution
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

import groovy.transform.CompileDynamic

@CompileDynamic
public class SubscriptionServiceImpl implements SubscriptionService {

    SubscriptionResource subscriptionResource

    @Override
    void save(String email, JsonNode subscription) {

        ((ObjectNode) subscription).set('email', new TextNode(email))
        ((ObjectNode) subscription).set('subscription_date', new TextNode(ServiceContextExecution.context().date.toString()))

        subscriptionResource.save(subscription)
    }

    @Override
    void save(String email, JsonNode subscription, JsonNode previousSubscription) {

        ((ObjectNode) subscription).set('email', new TextNode(email))
        ((ObjectNode) subscription).set('subscription_date', new TextNode(ServiceContextExecution.context().date.toString()))

        subscriptionResource.save(subscription, previousSubscription)
    }

    @Override
    Optional<JsonNode> get(String email) {

        subscriptionResource.get(email)
    }
}
