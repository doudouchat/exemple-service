package com.exemple.service.resource.account.model;

import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CqlName("account_username")
public class AccountUsername {

    @PartitionKey
    private String username;

    @PartitionKey(1)
    private String field;

    private UUID id;

}
