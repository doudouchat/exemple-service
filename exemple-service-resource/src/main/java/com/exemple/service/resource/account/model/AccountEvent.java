package com.exemple.service.resource.account.model;

import java.time.Instant;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.exemple.service.resource.common.model.EventType;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Entity
@Getter
@Setter
@CqlName("account_event")
public class AccountEvent {

    @PartitionKey
    private UUID id;

    @ClusteringColumn
    private Instant date;

    private EventType eventType;

    private String user;

    private String application;

    private String version;

    private JsonNode data;

}
