package com.exemple.service.resource.schema.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
@CqlName("resource_schema")
public class SchemaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @PartitionKey
    @CqlName("app")
    private String application;

    @ClusteringColumn
    private String resource;

    @ClusteringColumn(1)
    private String version;

    @ClusteringColumn(2)
    private String profile;

    private JsonNode content;

    @CqlName("filter")
    private Set<String> filters = Collections.emptySet();

    @CqlName("field")
    private Set<String> fields = Collections.emptySet();

    @CqlName("patch")
    private Set<JsonNode> patchs = Collections.emptySet();

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }

    public Set<String> getFilters() {
        return new LinkedHashSet<>(filters);
    }

    public void setFilters(Set<String> filters) {
        this.filters = new LinkedHashSet<>(filters);
    }

    public Set<String> getFields() {
        return new LinkedHashSet<>(fields);
    }

    public void setFields(Set<String> fields) {
        this.fields = new LinkedHashSet<>(fields);
    }

    public Set<JsonNode> getPatchs() {
        return patchs.stream().collect(Collectors.toSet());
    }

    public void setPatchs(Set<JsonNode> patchs) {
        this.patchs = patchs.stream().collect(Collectors.toSet());
    }

}
