package com.exemple.service.resource.stock.history.dao;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.service.resource.stock.model.StockHistory;

@Dao
public interface StockHistoryDao {

    @Select
    PagingIterable<StockHistory> findByStoreAndProduct(String store, String product);
}
