package com.exemple.service.resource.subscription.model;

import java.time.Instant;

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
@CqlName("subscription_event")
public class SubscriptionEvent {

    @PartitionKey
    private String email;

    @ClusteringColumn
    private Instant date;

    private EventType eventType;

    private String user;

    private String application;

    private String version;

    private JsonNode data;
}
