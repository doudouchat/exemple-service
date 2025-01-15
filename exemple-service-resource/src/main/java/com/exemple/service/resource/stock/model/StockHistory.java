package com.exemple.service.resource.stock.model;

import java.io.Serializable;
import java.time.Instant;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.Getter;
import lombok.Setter;

@Entity
@CqlName("stock_history")
@Getter
@Setter
public class StockHistory implements Serializable {

    @PartitionKey
    private String store;

    @PartitionKey(1)
    private String product;

    @ClusteringColumn
    private Instant date;

    @ClusteringColumn(1)
    private String user;

    @ClusteringColumn(2)
    private String application;

    private Long quantity;

}
