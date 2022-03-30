package com.exemple.service.customer.account

import com.exemple.service.context.ServiceContextExecution
import com.exemple.service.customer.common.event.EventType
import com.exemple.service.customer.common.event.ResourceEventPublisher
import com.exemple.service.customer.login.LoginResource
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

import groovy.transform.CompileDynamic

@CompileDynamic
class AccountServiceImpl implements AccountService {

    private static final String ACCOUNT = "account"

    AccountResource accountResource

    LoginResource loginResource

    ResourceEventPublisher resourceEventPublisher

    @Override
    JsonNode save(JsonNode account) {

        UUID id = UUID.randomUUID()
        
        loginResource.save(id, account.get('email').textValue())

        ((ObjectNode) account).put('id', id.toString())
        ((ObjectNode) account).put('creation_date', ServiceContextExecution.context().date.toString())

        accountResource.save(account)

        resourceEventPublisher.publish(account, ACCOUNT, EventType.CREATE)

        account
    }

    @Override
    JsonNode save(JsonNode account, JsonNode previousSource) {
        
        if(!account.get('email').equals(previousSource.get('email'))) {
            UUID id = UUID.fromString(account.get('id').textValue())
            loginResource.save(id, account.get('email').textValue())
            loginResource.delete(previousSource.get('email').textValue())
        }

        ((ObjectNode) account).put('update_date', ServiceContextExecution.context().date.toString())

        accountResource.save(account, previousSource)

        resourceEventPublisher.publish(account, ACCOUNT, EventType.UPDATE)

        account
    }

    Optional<JsonNode> get(UUID id) {

        accountResource.get(id)
    }
}
