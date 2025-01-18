package com.exemple.service.resource.account.username.dao;

import java.util.Optional;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.service.resource.account.model.AccountUsername;

@Dao
public interface AccountUsernameDao {

    @Insert
    void save(AccountUsername username);

    @Select
    Optional<AccountUsername> findByUsernameAndField(String username, String field);

    @Delete(entityClass = AccountUsername.class)
    void deleteByUsernameAndField(String username, String field);
}
