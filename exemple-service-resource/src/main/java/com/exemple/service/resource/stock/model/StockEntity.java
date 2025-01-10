package com.exemple.service.resource.stock.model;

import java.io.Serializable;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.Getter;
import lombok.Setter;

@Entity
@CqlName("stock")
@Getter
@Setter
public class StockEntity implements Serializable {

    @PartitionKey
    private String store;

    @ClusteringColumn
    private String product;

    private Long quantity;

}
