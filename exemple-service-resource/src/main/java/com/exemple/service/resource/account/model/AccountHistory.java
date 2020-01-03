package com.exemple.service.resource.account.model;

import java.time.Instant;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
@CqlName("account_history")
public class AccountHistory {

    @PartitionKey
    private UUID id;

    @ClusteringColumn
    private String field;

    private Instant date;

    private JsonNode value;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public JsonNode getValue() {
        return value;
    }

    public void setValue(JsonNode value) {
        this.value = value;
    }

}
