package com.exemple.service.resource.common.history;

import java.time.Instant;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class HistoryModel<T> {

    private T id;

    @ClusteringColumn
    private String field;

    private Instant date;

    private JsonNode value;

    private String application;

    private String version;

    private String user;

    @CqlName("previous_value")
    private JsonNode previousValue;

}
