package com.exemple.service.resource.parameter.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.service.resource.parameter.dao.ParameterDao;

@Mapper
public interface ParameterMapper {

    @DaoFactory
    ParameterDao parameterDao();

    static MapperBuilder<ParameterMapper> builder(CqlSession session) {
        return new ParameterMapperBuilder(session);
    }
}
