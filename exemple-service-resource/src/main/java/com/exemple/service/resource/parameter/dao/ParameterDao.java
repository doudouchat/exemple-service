package com.exemple.service.resource.parameter.dao;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.service.resource.parameter.model.ParameterEntity;

@Dao
public interface ParameterDao {

    @Select
    ParameterEntity findByApplication(String application);

}
