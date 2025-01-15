package com.exemple.service.resource.stock.history.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.service.resource.stock.history.dao.StockHistoryDao;

@Mapper
public interface StockHistoryMapper {

    @DaoFactory
    StockHistoryDao stockHistoryDao();

    static MapperBuilder<StockHistoryMapper> builder(CqlSession session) {
        return new StockHistoryMapperBuilder(session);
    }
}
