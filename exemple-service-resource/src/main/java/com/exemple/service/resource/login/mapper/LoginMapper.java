package com.exemple.service.resource.login.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.service.resource.login.dao.LoginDao;

@Mapper
public interface LoginMapper {

    @DaoFactory
    LoginDao loginDao();

    static MapperBuilder<LoginMapper> builder(CqlSession session) {
        return new LoginMapperBuilder(session);
    }
}
