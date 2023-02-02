package com.exemple.service.customer.account

import com.exemple.service.context.ServiceContextExecution
import com.exemple.service.customer.common.event.EventType
import com.exemple.service.customer.common.event.ResourceEventPublisher
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

import groovy.transform.CompileDynamic

@CompileDynamic
class AccountServiceTestImpl implements AccountService {

    private static final String ACCOUNT = "account"

    AccountResource accountResource

    ResourceEventPublisher resourceEventPublisher

    @Override
    JsonNode save(JsonNode account) {

        UUID id = UUID.randomUUID()

        ((ObjectNode) account).put('id', id.toString())
        ((ObjectNode) account).put('creation_date', ServiceContextExecution.context().date.toString())

        accountResource.save(account)

        resourceEventPublisher.publish(account, ACCOUNT, EventType.CREATE)

        account
    }

    @Override
    JsonNode save(JsonNode account, JsonNode previousSource) {

        ((ObjectNode) account).put('update_date', ServiceContextExecution.context().date.toString())

        accountResource.save(account, previousSource)

        resourceEventPublisher.publish(account, ACCOUNT, EventType.UPDATE)

        account
    }

    Optional<JsonNode> get(UUID id) {

        accountResource.get(id)
    }
}
