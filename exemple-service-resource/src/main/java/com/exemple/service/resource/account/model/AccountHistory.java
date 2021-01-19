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

    private String application;

    private String version;

    private String user;

    @CqlName("previous_date")
    private Instant previousDate;

    @CqlName("previous_value")
    private JsonNode previousValue;

    @CqlName("previous_application")
    private String previousApplication;

    @CqlName("previous_version")
    private String previousVersion;

    @CqlName("previous_user")
    private String previousUser;

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

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Instant getPreviousDate() {
        return previousDate;
    }

    public void setPreviousDate(Instant previousDate) {
        this.previousDate = previousDate;
    }

    public JsonNode getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(JsonNode previousValue) {
        this.previousValue = previousValue;
    }

    public String getPreviousApplication() {
        return previousApplication;
    }

    public void setPreviousApplication(String previousApplication) {
        this.previousApplication = previousApplication;
    }

    public String getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(String previousVersion) {
        this.previousVersion = previousVersion;
    }

    public String getPreviousUser() {
        return previousUser;
    }

    public void setPreviousUser(String previousUser) {
        this.previousUser = previousUser;
    }

}
