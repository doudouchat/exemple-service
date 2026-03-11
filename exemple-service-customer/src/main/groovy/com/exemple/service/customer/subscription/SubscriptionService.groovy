package com.exemple.service.customer.subscription

import com.exemple.service.context.ServiceContextExecution
import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ObjectNode
import tools.jackson.databind.node.StringNode

import groovy.transform.CompileDynamic

@CompileDynamic
public class SubscriptionServiceImpl implements SubscriptionService {

    SubscriptionResource subscriptionResource

    @Override
    void create(String email, JsonNode subscription) {

        ((ObjectNode) subscription).set('email', new StringNode(email))
        ((ObjectNode) subscription).set('subscription_date', new StringNode(ServiceContextExecution.context().date.toString()))

        subscriptionResource.create(subscription)
    }

    @Override
    void update(String email, JsonNode subscription) {

        ((ObjectNode) subscription).set('email', new StringNode(email))
        ((ObjectNode) subscription).set('subscription_date', new StringNode(ServiceContextExecution.context().date.toString()))

        subscriptionResource.update(subscription)
    }

    @Override
    Optional<JsonNode> get(String email) {

        subscriptionResource.get(email)
    }
}
