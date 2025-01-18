package com.exemple.service.resource.account.username;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.resource.account.AccountField;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountUsernameService {

    private final AccountResource accountResource;

    private final AccountUsernameResource accountUsernameResource;

    public void saveUsername(AccountUsername username) {

        if (username.hasValue()) {
            accountUsernameResource.save(username.value(), username.field(), UUID.fromString(username.id()));
        }

        if (username.hasPreviousValue()) {
            accountUsernameResource.delete(username.previousValue(), username.field());
        }
    }

    public List<AccountUsername> findAllAlreadyExistsUsernames(Collection<AccountUsername> changedUsernames) {

        return changedUsernames.stream()
                .filter(AccountUsername::hasValue)
                .filter((AccountUsername username) -> accountResource.getIdByUsername(username.field(), username.value()).isPresent())
                .toList();
    }

    public List<AccountUsername> findAllUsernames(ApplicationDetail applicationDetail, JsonNode account, JsonNode previousAccount) {
        var id = account.path(AccountField.ID.field).textValue();
        return applicationDetail.getAccount().getUniqueProperties().stream()
                .map((String property) -> new AccountUsername(id, property, account.path(property), previousAccount.path(property)))
                .toList();
    }

}
