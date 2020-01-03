package com.exemple.service.resource.schema.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

@Entity
@CqlName("resource_schema")
public class ResourceSchema implements Serializable {

    private static final long serialVersionUID = 1L;

    @PartitionKey
    @CqlName("app")
    private String application;

    @ClusteringColumn
    private String resource;

    @ClusteringColumn(1)
    private String version;

    private byte[] content;

    @CqlName("filter")
    private Set<String> filters = Collections.emptySet();

    @CqlName("rule")
    private Map<String, Set<String>> rules = Collections.emptyMap();

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

    public byte[] getContent() {
        return ObjectUtils.clone(content);
    }

    public void setContent(byte[] content) {
        this.content = ObjectUtils.clone(content);
    }

    public Set<String> getFilters() {
        return new HashSet<>(filters);
    }

    public void setFilters(Set<String> filters) {
        this.filters = new HashSet<>(filters);
    }

    public Map<String, Set<String>> getRules() {
        return rules;
    }

    public void setRules(Map<String, Set<String>> rules) {
        this.rules = rules;
    }

}
