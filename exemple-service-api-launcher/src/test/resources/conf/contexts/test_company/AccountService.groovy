package com.exemple.service.customer.account

import com.exemple.service.context.ServiceContextExecution
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

import groovy.transform.CompileDynamic

@CompileDynamic
class AccountServiceTestImpl implements AccountService {

    AccountResource accountResource

    @Override
    JsonNode save(JsonNode account) {

        UUID id = UUID.randomUUID()

        ((ObjectNode) account).put('id', id.toString())
        ((ObjectNode) account).put('creation_date', ServiceContextExecution.context().date.toString())

        accountResource.save(account)

        account
    }

    @Override
    JsonNode save(JsonNode account, JsonNode previousSource) {

        ((ObjectNode) account).put('update_date', ServiceContextExecution.context().date.toString())

        accountResource.save(account, previousSource)

        account
    }

    Optional<JsonNode> get(UUID id) {

        accountResource.get(id)
    }
}
