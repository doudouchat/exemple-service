package com.exemple.service.resource.stock.dao;

import java.util.Optional;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.service.resource.stock.model.StockEntity;

@Dao
public interface StockDao {

    @Select
    Optional<StockEntity> findByStoreAndProduct(String store, String product);
}
