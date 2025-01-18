package com.exemple.service.resource.account.username.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.service.resource.account.username.dao.AccountUsernameDao;

@Mapper
public interface AccountUsernameMapper {

    @DaoFactory
    AccountUsernameDao accountUsernameDao();

    static MapperBuilder<AccountUsernameMapper> builder(CqlSession session) {
        return new AccountUsernameMapperBuilder(session);
    }
}
