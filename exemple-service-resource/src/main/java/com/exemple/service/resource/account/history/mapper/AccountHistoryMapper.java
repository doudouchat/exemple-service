package com.exemple.service.resource.account.history.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.service.resource.account.history.dao.AccountHistoryDao;

@Mapper
public interface AccountHistoryMapper {

    @DaoFactory
    AccountHistoryDao accountHistoryDao();

    static MapperBuilder<AccountHistoryMapper> builder(CqlSession session) {
        return new AccountHistoryMapperBuilder(session);
    }
}
