package com.exemple.service.application.common.model;

import java.util.Collections;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ApplicationDetail {

    @NotBlank
    private String keyspace;

    @NotBlank
    private String company;

    @NotEmpty
    private Set<String> clientIds;

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Set<String> getClientIds() {
        return Collections.unmodifiableSet(clientIds);
    }

    public void setClientIds(Set<String> clientIds) {
        this.clientIds = Collections.unmodifiableSet(clientIds);
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("clientIds", clientIds).append("keyspace", keyspace).append("company", company).toString();

    }

}
