package com.exemple.service.resource.account.event.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.service.resource.account.event.dao.AccountEventDao;

@Mapper
public interface AccountEventMapper {

    @DaoFactory
    AccountEventDao accountEventDao();

    static MapperBuilder<AccountEventMapper> builder(CqlSession session) {
        return new AccountEventMapperBuilder(session);
    }
}
