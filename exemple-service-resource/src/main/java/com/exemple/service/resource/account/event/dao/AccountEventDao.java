package com.exemple.service.resource.account.event.dao;

import java.time.Instant;
import java.util.UUID;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.service.resource.account.model.AccountEvent;

@Dao
public interface AccountEventDao {

    @Insert
    BoundStatement create(AccountEvent accountEvent);

    @Select
    AccountEvent getByIdAndDate(UUID id, Instant date);

}
