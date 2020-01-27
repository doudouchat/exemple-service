package com.exemple.service.resource.account.history.dao;

import java.util.UUID;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.service.resource.account.model.AccountHistory;

@Dao
public interface AccountHistoryDao {

    @Select
    PagingIterable<AccountHistory> findById(UUID id);
}
