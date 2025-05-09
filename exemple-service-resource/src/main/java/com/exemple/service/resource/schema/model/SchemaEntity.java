package com.exemple.service.resource.schema.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;

@Entity
@CqlName("resource_schema")
@Getter
@Setter
public class SchemaEntity implements Serializable {

    @PartitionKey
    private String resource;

    @ClusteringColumn
    private String version;

    @ClusteringColumn(1)
    private String profile;

    private JsonNode content;

    @CqlName("patch")
    private Set<JsonNode> patchs = Collections.emptySet();

}
