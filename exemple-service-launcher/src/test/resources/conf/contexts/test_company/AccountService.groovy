package com.exemple.service.customer.account

import com.exemple.service.context.ServiceContextExecution
import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ObjectNode

import groovy.transform.CompileDynamic

@CompileDynamic
class AccountServiceTestImpl implements AccountService {

    AccountResource accountResource

    @Override
    JsonNode create(JsonNode account) {

        UUID id = UUID.randomUUID()

        ((ObjectNode) account).put('id', id.toString())
        ((ObjectNode) account).put('creation_date', ServiceContextExecution.context().date.toString())

        accountResource.create(account)

        account
    }

    @Override
    JsonNode update(JsonNode account) {

        ((ObjectNode) account).put('update_date', ServiceContextExecution.context().date.toString())

        accountResource.update(account)

        account
    }

    Optional<JsonNode> get(UUID id) {

        accountResource.get(id)
    }
}
