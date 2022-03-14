package com.exemple.service.customer.account

import com.exemple.service.context.ServiceContextExecution
import com.exemple.service.customer.account.AccountResource
import com.exemple.service.customer.common.event.CustomerEventPublisher
import com.exemple.service.event.model.EventType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

import groovy.transform.CompileDynamic

@CompileDynamic
class AccountServiceTestImpl implements AccountService {

    private static final String ACCOUNT = "account"

    AccountResource accountResource

    CustomerEventPublisher customerEventPublisher

    @Override
    JsonNode save(JsonNode account) {

        ((ObjectNode) account).set('creation_date', new TextNode(ServiceContextExecution.context().date.toString()))

        accountResource.save(account)

        customerEventPublisher.publish(account, ACCOUNT, EventType.CREATE)

        account
    }

    @Override
    JsonNode save(JsonNode account, JsonNode previousSource) {

        ((ObjectNode) account).set('update_date', new TextNode(ServiceContextExecution.context().date.toString()))

        accountResource.save(account, previousSource)

        customerEventPublisher.publish(account, ACCOUNT, EventType.UPDATE)

        account
    }

    Optional<JsonNode> get(UUID id) {

        accountResource.get(id)
    }
}
