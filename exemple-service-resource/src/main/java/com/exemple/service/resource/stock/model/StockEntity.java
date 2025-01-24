package com.exemple.service.resource.stock.model;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.Getter;
import lombok.Setter;

@Entity
@CqlName("stock")
@Getter
@Setter
public class StockEntity {

    @PartitionKey
    private String store;

    @PartitionKey(1)
    private String product;

    private Long quantity;

}
