package com.exemple.service.resource.account.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.exemple.service.resource.common.model.EventType;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;

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

    private LocalDate localDate;

}
