package com.exemple.service.resource.stock.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.service.resource.stock.dao.StockDao;

@Mapper
public interface StockMapper {

    @DaoFactory
    StockDao stockDao();

    static MapperBuilder<StockMapper> builder(CqlSession session) {
        return new StockMapperBuilder(session);
    }
}
